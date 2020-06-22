package net.haesleinhuepf.clijx.gui.panel;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.process.FloatProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.plugins.*;
import net.haesleinhuepf.clijx.CLIJx;
import org.fife.ui.rsyntaxtextarea.modes.JsonTokenMaker;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

class Panel {
    private int width;
    private int height;
    private CLIJx clijx;
    //private final CLIJx clijxSecondary;

    private CLIJMacroPlugin operation = new Invert();

    ImagePlus panelImp = null;
    ClearCLBuffer mainPanel = null;
    Overlay overlay = null;

    int number_of_columns = 4;
    int margin = 0;
    int font_size = 20;

    int imageCount = 0;

    private Panel(){}

    private static Panel instance = null;
    public static Panel getInstance() {
        if (instance == null) {
            instance = new Panel();
        }
        return instance;
    }

    private void init() {
        if (mainPanel != null) {
            return;
        }
        //this.clijxSecondary = clijxSecondary;

        mainPanel = clijx.create(width, height);

        imageParameterMap = new ImagePlus[number_of_columns];

        int[] idList = WindowManager.getIDList();

        try {
            imageParameterMap[0] = IJ.getImage();
        } catch (Exception e) {
            destroy();
        }
        for (int i = 1; i < imageParameterMap.length; i++) {
            if (idList.length > i) {
                imageParameterMap[i] = WindowManager.getImage(idList[i]);
            } else {
                imageParameterMap[i] = new ImagePlus("", new FloatProcessor(1, 1));
            }
        }
        numericParameterMap = new Double[6];
        for (int i = 0; i < numericParameterMap.length; i++) {
            numericParameterMap[i] = Double.valueOf(2);
        }
    }

    public void show() {
        init();
        refresh();
    }

    int buttonWidth;
    int buttonHeight;

    private synchronized void refresh() {

        buttonWidth = (width - margin * 2) / number_of_columns;
        buttonHeight = (height - margin * 2) / 6;

        String[] imageParameterNames = getInputImageParameterNames(operation);
        String[] numberParameterNames = getNumberParameterNames(operation);

        ClearCLBuffer button_image = clijx.create(buttonWidth, buttonHeight);
        ClearCLBuffer temp = clijx.create(button_image);
        ClearCLBuffer[] inputs = new ClearCLBuffer[imageParameterMap.length];
        overlay = new Overlay();

        clijx.set(mainPanel, 0);


        // =============================================================================================================
        // draw top row - image parameters
        System.out.println("Draw image parameters");
        for (int i = 0; i < imageParameterMap.length; i++) {
            ImagePlus imp = imageParameterMap[i];
            if (imp == null) {
                continue;
            }
            ClearCLBuffer buffer = clijx.pushCurrentSlice(imp);
            inputs[i] = buffer;

            if (i < imageParameterNames.length) {
                scaleImage(buffer, button_image, temp, buttonWidth, buttonHeight);

                int x = margin + i * buttonWidth;
                int y = margin;

                Color color = Color.white;
                if (isImageParameterRequested(i)) {
                    color = Color.white;
                }
                if (mouseX > x && mouseX < x + buttonWidth && mouseY > y && mouseY < y + buttonHeight) {
                    selectInputImage(i);
                }
                if (getSelectedInputImage() == imp) {
                    color = Color.white;
                }

                Roi roi = new Roi(x, y, buttonWidth, buttonHeight);
                roi.setStrokeColor(color);
                overlay.add(roi);

                drawText(x, y, imageParameterNames[i] + "\n" + imp.getTitle(), color);

                clijx.paste2D(button_image, mainPanel, x, y);
            }
        }

        // =============================================================================================================
        // draw second row - numeric parameters
        System.out.println("Draw numeric parameters");

        for (int i = 0; i < numberParameterNames.length && i < numericParameterMap.length; i++) {
            int x = margin + i * buttonWidth;
            int y = margin + buttonHeight;

            Color color = Color.lightGray;
            if (mouseX > x && mouseX < x + buttonWidth && mouseY > y && mouseY < y + buttonHeight / 2) {
                selectNumericInputParameter(i);
            }

            if (isSelectedNumericInputParameter(i) && mouseState != MouseState.NONE) {
                color = Color.white;
                if (mouseState == MouseState.MOVING) {
                    double deltaX = -(mouseX - mouseMoveX);
                    double deltaY = (mouseY - mouseMoveY);

                    System.out.println("delta " + deltaX + " " + deltaY);

                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        numericParameterMap[i] = startingValue + deltaX / panelImp.getWidth() * 10;
                    } else {
                        numericParameterMap[i] = startingValue + deltaY / panelImp.getWidth();
                    }
                }
            }

            Roi roi = new Roi(x, y, buttonWidth, buttonHeight / 2);
            roi.setStrokeColor(color);
            roi.setStrokeWidth(2);
            overlay.add(roi);

            drawText(x, y, numberParameterNames[i] + "\n" + String.format("%.2f", numericParameterMap[i]), color);
        }

        // =============================================================================================================
        // draw second.5 row - operations categories
        System.out.println("Draw operations categories " + getSelectedOperationsPanel());
        int categoryButtonWidth = width /  operationsCategories.length;
        for (int i = 0; i < operationsCategories.length; i++) {
            int x = margin + i * categoryButtonWidth;
            int y = (int)(margin + buttonHeight * 1.75);

            Color color = operationsCategoriesColors[i];
            if (mouseX > x && mouseX < x + categoryButtonWidth && mouseY > y && mouseY < y + buttonHeight * 0.25) {
                setSelectedOperationsPanel(i);
            }

            Roi roi = new Roi(x, y, categoryButtonWidth, buttonHeight * 0.25);
            roi.setStrokeColor(color);
           // roi.setStrokeWidth(2);
            if (getSelectedOperationsPanel() == i) {
                roi.setFillColor(color);
            } else {
                roi.setFillColor(new Color((int)(color.getRed() * 0.7), (int)(color.getGreen() * 0.7), (int)(color.getBlue() * 0.7)));
            }
            overlay.add(roi);

            drawText(x, y, operationsCategories[i], Color.black);
        }



        // =============================================================================================================
        // draw operations map
        System.out.println("Draw operations ");
        ClearCLBuffer result = clijx.create(inputs[0].getDimensions(), NativeTypeEnum.Float);

        int operationsPanel = getSelectedOperationsPanel();
        for (int row = 0; row < operationsMap[operationsPanel].length; row++) {
            for (int col = 0; col < operationsMap[operationsPanel][row].length; col++) {
                int x = margin + col * buttonWidth;
                int y = margin + (row + 2) * buttonHeight;

                CLIJMacroPlugin oneOperation = operationsMap[operationsPanel][row][col];
                //if
                //(mouseState == MouseState.NONE || mouseState == MouseState.MOVING || mouseState == MouseState.UP || operation == oneOperation) {
                applyPlugin(oneOperation, inputs, result);
                scaleImage(result, button_image, temp, buttonWidth, buttonHeight);

                //if (oneOperation == operation && mouseState == MouseState.UP) {
                //    if (mouseX > x && mouseX < x + buttonWidth && mouseY > y && mouseY < y + buttonHeight) {
                //        ImagePlus imp = clijx.showGrey(result, operation.getName());
                //        imp.getWindow().setLocation(panelImp.getWindow().getX() + panelImp.getWindow().getWidth(), panelImp.getWindow().getY() + y);
                //    }
                //}

                Color color = operationsCategoriesColors[operationsPanel];
                if (oneOperation == operation) {
                    color = Color.white;
                }

                Roi roi = new Roi(x, y, buttonWidth, buttonHeight);
                roi.setStrokeColor(color);
                roi.setStrokeWidth(2);
                overlay.add(roi);

                drawText(x, y, niceName(oneOperation.getName()), color);


                clijx.paste2D(button_image, mainPanel, x, y);
                //}

                if (mouseX > x && mouseX < x + buttonWidth && mouseY > y && mouseY < y + buttonHeight && mouseState == MouseState.DOWN) {
                    operation = oneOperation;
                }
                if (mouseState == MouseState.UP && operation == oneOperation && selectedNumericInputParameter == -1 && selectedInputImageIndex == -1) {
                    if (mouseX < 0 || mouseX > width || mouseY < 0 || mouseY > height ) {
                        imageCount++;
                        ImagePlus imp = clijx.showGrey(result, operation.getName() + imageCount);
                        //imp.getWindow().setLocation(mouseX, mouseY);
                        imp.getWindow().setLocation(MouseInfo.getPointerInfo().getLocation());
                        if (operation.getName().contains("Label")) {
                            try {
                                IJ.run(imp, "glasbey_on_dark", "");
                            } catch (Exception e) {
                                // happens only from IDE
                            }
                        }
                        //imp.getWindow().setLocation(panelImp.getWindow().getX() + panelImp.getWindow().getWidth(), panelImp.getWindow().getY() + y);
                    }
                }

                //System.out.println(result);
                //clijx.show(result, "res");
            }
        }

        result.close();
        button_image.close();
        temp.close();

        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] != null) {
                inputs[i].close();
            }
        }

        System.out.println("Drawing done");

        panelImp = clijx.showGrey(mainPanel, "Panel");
        panelImp.setOverlay(overlay);
        panelImp.killRoi();
    }

    private int selectedOperationsPanel = 0;
    private void setSelectedOperationsPanel(int panel) {
        selectedOperationsPanel = panel;
    }
    private int getSelectedOperationsPanel() {
        return selectedOperationsPanel;
    }

    private void drawText(int x, int y, String text, Color color) {
        x = x + 5;
        y = y + 5;
        if (color != Color.black) {
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dy = -1; dy <= 1; dy += 2) {
                    TextRoi textRoi = new TextRoi(x + dx, y + dy, text);
                    textRoi.setCurrentFont(new Font("Arial", 0, font_size));
                    textRoi.setStrokeColor(Color.black);
                    overlay.add(textRoi);
                }
            }
        }

        TextRoi textRoi = new TextRoi(x, y, text);
        textRoi.setCurrentFont(new Font("Arial", 0, font_size));
        textRoi.setStrokeColor(color);
        overlay.add(textRoi);
    }

    private String niceName(String name) {
        String result = "";

        name = name.replace("CLIJ2_", "");

        for (int i = 0; i < name.length(); i++) {
            String ch = name.substring(i,i+1);
            if (!ch.toLowerCase().equals(ch)) {
                result = result + " ";
            }
            result = result + ch;
        }

        result = result.replace("_", "\n");
        result = result.replace(" ", "\n");
        result = result.replace("-", "\n");
        return result;
    }

    private String[] getInputImageParameterNames(CLIJMacroPlugin operation) {
        ArrayList<String> names = new ArrayList<>();
        String[] parameters = operation.getParameterHelpText().split(",");
        Object[] args = new Object[parameters.length];
        int inputImageCount = 0;
        int inputNumberCount = 0;
        for (int p = 0; p < parameters.length; p++) {
            String[] parameterParts = parameters[p].trim().split(" ");
            String parameterType = parameterParts[0];
            String parameterName = parameterParts[1];
            boolean byRef = false;
            if (parameterType.compareTo("ByRef") == 0) {
                parameterType = parameterParts[1];
                parameterName = parameterParts[2];
                byRef = true;
            }
            if (parameterType.compareTo("Image") == 0) {
                if (!(parameterName.contains("destination") || byRef)) {
                    names.add(parameterName);
                }
            }
        }

        String[] namesArray = new String[names.size()];
        if (names.size() > 0) {
            names.toArray(namesArray);
        }
        return namesArray;
    }

    private String[] getNumberParameterNames(CLIJMacroPlugin operation) {
        ArrayList<String> names = new ArrayList<>();
        String[] parameters = operation.getParameterHelpText().split(",");
        Object[] args = new Object[parameters.length];
        int inputImageCount = 0;
        int inputNumberCount = 0;
        for (int p = 0; p < parameters.length; p++) {
            String[] parameterParts = parameters[p].trim().split(" ");
            String parameterType = parameterParts[0];
            String parameterName = parameterParts[1];
            boolean byRef = false;
            if (parameterType.compareTo("ByRef") == 0) {
                parameterType = parameterParts[1];
                parameterName = parameterParts[2];
                byRef = true;
            }
            if (parameterType.compareTo("Number") == 0) {
                if (!byRef) {
                    names.add(parameterName);
                }
            }
        }

        String[] namesArray = new String[names.size()];
        if (names.size() > 0) {
            names.toArray(namesArray);
        }
        return namesArray;
    }

    private void applyPlugin(CLIJMacroPlugin operation, ClearCLBuffer[] inputs, ClearCLBuffer result) {
        if (operation instanceof AbstractCLIJ2Plugin) {
            ((AbstractCLIJ2Plugin) operation).setCLIJ2(clijx);
        } else {
            operation.setClij(clijx.getCLIJ());
        }

        String[] parameters = operation.getParameterHelpText().split(",");
        Object[] args = new Object[parameters.length];
        int inputImageCount = 0;
        int inputNumberCount = 0;
        for (int p = 0; p < parameters.length; p++) {
            String[] parameterParts = parameters[p].trim().split(" ");
            String parameterType = parameterParts[0];
            String parameterName = parameterParts[1];
            boolean byRef = false;
            if (parameterType.compareTo("ByRef") == 0) {
                parameterType = parameterParts[1];
                parameterName = parameterParts[2];
                byRef = true;
            }
            if (parameterType.compareTo("Image") == 0) {
                if (!(parameterName.contains("destination") || byRef)) {
                    args[p] = inputs[inputImageCount];
                    inputImageCount++;
                    // input image
                } else {
                    // output image
                    args[p] = result; // TODO: Only one output supported so far
                }
            } else if (parameterType.compareTo("String") == 0) {
                args[p] = "";
            } else if (parameterType.compareTo("Boolean") == 0) {
                args[p] = Boolean.valueOf(numericParameterMap[inputNumberCount] != 0);
                inputNumberCount++;
            } else { // Number
                args[p] = numericParameterMap[inputNumberCount];
                inputNumberCount++;
            }
        }

        operation.setArgs(args);
        try {
            if (operation instanceof CLIJOpenCLProcessor) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        clijx.activateSizeIndependentKernelCompilation();
                        ((CLIJOpenCLProcessor) operation).executeCL();
                    }
                });
                thread.start();
                thread.join(50);
            }
        } catch (Exception e) {
            clijx.set(result, 0);
        }
    }

    private void scaleImage(ClearCLBuffer buffer, ClearCLBuffer button_image, ClearCLBuffer temp, int buttonWidth, int buttonHeight) {
        float scaleFactor = 1.0f / Math.min((float)buffer.getWidth() / buttonWidth, (float)buffer.getHeight() / buttonHeight);
        clijx.scale2D(buffer, button_image, scaleFactor, scaleFactor);

        double min = clijx.minimumOfAllPixels(button_image);
        double max = clijx.maximumOfAllPixels(button_image);

        clijx.addImageAndScalar(button_image, temp, -min);
        clijx.multiplyImageAndScalar(temp, button_image, 255 / (max - min));

    }

    private boolean isImageParameterRequested(int i) {
        String[] parameters = operation.getParameterHelpText().split(",");
        if (parameters.length <= i) {
            return false;
        }
        return parameters[i].trim().split(" ").equals("Image");
    }

    double startingValue = 0;
    private int selectedNumericInputParameter = -1;
    private void selectNumericInputParameter(int i) {
        selectedNumericInputParameter = i;
        if (mouseState == MouseState.DOWN) {
            startingValue = numericParameterMap[i];
        }
    }
    private boolean isSelectedNumericInputParameter(int i) {
        return selectedNumericInputParameter == i;
    }

    int selectedInputImageIndex = -1;
    private void selectInputImage(int i) {
        selectedInputImageIndex = i;
        ImagePlus imp = getSelectedInputImage();
        if (imp != null) {
            imp.show();
        }
    }

    private ImagePlus getSelectedInputImage() {
        if (selectedInputImageIndex < 0 || selectedInputImageIndex >= imageParameterMap.length) {
            return null;
        }
        return imageParameterMap[selectedInputImageIndex];
    }


    private ImagePlus[] imageParameterMap;
    private Double[] numericParameterMap;

    private String[] operationsCategories = {
            "Math",
            "Filters",
            "Threshold",
            "Binary",
            "Labels"
    };

    private Color[] operationsCategoriesColors = {
            new Color(128, 192, 255),
            new Color(192,  255, 192),
            new Color(255,  255, 128),
            new Color(255,  192, 128),
            new Color(255,  128, 255)
    };

    private CLIJMacroPlugin operationsMap[][][] = {
        {
            {
                new AddImages(),
                new SubtractImages(),
                new MultiplyImages(),
                new AddImagesWeighted()
            },
            {
                new AddImageAndScalar(),
                new SubtractImageFromScalar(),
                new MultiplyImageAndScalar()
            },
            {
                new Invert(),
                new Absolute(),
                new DivideImages()
            }
        },
        {
            {
                new GaussianBlur2D(),
                new DifferenceOfGaussian2D(),
                new Mean2DBox(),
                new Median2DBox()
            },
            {
                new Minimum2DBox(),
                new Maximum2DBox(),
                new TopHatBox(),
                new BottomHatBox()
            },
            {
                new Sobel(),
                new LaplaceBox(),
                new EntropyBox()
            },
            {
                new Power(),
                new PowerImages(),
                new Logarithm(),
                new Exponential()
            }
        },
        {
            {
                new Threshold(),
                new LocalThreshold(),
                new ThresholdDefault(),
                new ThresholdHuang()
            },
            {
                new ThresholdIJ_IsoData(),
                new ThresholdIntermodes(),
                new ThresholdIsoData(),
                new ThresholdLi()
            },
            {
                new ThresholdMaxEntropy(),
                new ThresholdMean(),
                new ThresholdMinError(),
                new ThresholdMinimum()
            },
            {
                new ThresholdMoments(),
                new ThresholdOtsu(),
                new ThresholdPercentile(),
                new ThresholdRenyiEntropy()
            },
            {
                new ThresholdShanbhag(),
                new ThresholdTriangle(),
                new ThresholdYen()
            }
        },
        {
            {
                new Smaller(),
                new SmallerOrEqual(),
                new Greater(),
                new GreaterOrEqual()
            },
            {
                new SmallerConstant(),
                new SmallerOrEqualConstant(),
                new GreaterConstant(),
                new GreaterOrEqualConstant()
            },
            {
                new BinaryNot(),
                new BinaryOr(),
                new BinarySubtract(),
                new BinaryAnd()
            },
            {
                new BinaryXOr(),
                new BinaryEdgeDetection(),
                new BinaryFillHoles()//,
                //new Watershed()
            }
        },
        {
            {
                new ConnectedComponentsLabelingBox(),
                new MaskLabel(),
                new ExcludeLabelsOnEdges(),
                new GenerateParametricImage()
            },
            {
                new LabelVoronoiOctagon()/*,
                new SpotsToPointList(),
                new LabelledSpotsToPointList(),
                new GenerateBinaryOverlapMatrix()
            },
            {
                new GenerateTouchMatrix(),
                new GenerateDistanceMatrix(),
                new TouchMatrixToMesh(),
                new DistanceMatrixToMesh()
            },
            {
                new NeighborsOfNeighbors(),
                new MultiplyMatrix(),
                new PointlistToLabelledSpots()
            */}
        }
    };


    public void setCLIJx(CLIJx clijx) {
        this.clijx = clijx;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isPanel(ImagePlus imp) {
        return imp == panelImp;
    }



    int mouseX = -1;
    int mouseY = -1;

    int mouseMoveX = -1;
    int mouseMoveY = -1;

    public void destroy() {
        mainPanel.close();
        instance = null;
    }

    enum MouseState{DOWN,MOVING,UP,NONE};

    MouseState mouseState = MouseState.NONE;

    public void mouseDown(int x, int y) {
        System.out.println("mouse down " + x + " / " + y);
        selectInputImage(-1);

        selectNumericInputParameter(-1);
        mouseX = x;
        mouseY = y;
        mouseMoveX = x;
        mouseMoveY = y;
        mouseState = MouseState.DOWN;
        refresh();
        mouseState = MouseState.NONE;
    }

    public void mouseMove(int x, int y) {
        System.out.println("mouse move " + x + " / " + y);

        mouseMoveX = x;
        mouseMoveY = y;
        mouseState = MouseState.MOVING;
        refresh();
        mouseState = MouseState.NONE;

    }

    public void mouseUp(int x, int y) {
        mouseX = x;
        mouseY = y;
        System.out.println("mouse up " + x + " / " + y);

        mouseState = MouseState.UP;
        refresh();
        selectInputImage(-1);
        selectNumericInputParameter(-1);
        mouseState = MouseState.NONE;
        mouseX = -1;
        mouseY = -1;
        refresh();
    }

    public void changeSize(int width, int height) {
        /*this.width = width;
        this.height = height;

        ClearCLBuffer buffer = mainPanel;
        mainPanel = null;
        buffer.close();

        init();

         */
    }

    public void considerTaking(ImagePlus imp) {
        if (imageParameterMap == null) {
            return;
        }
        for (int i = 0; i < imageParameterMap.length; i++) {

            int windowBorderX = 0;
            int windowBorderY = 20;

            int minX = panelImp.getWindow().getX() + windowBorderX + i * buttonWidth;
            int minY = panelImp.getWindow().getY() + windowBorderY;
            int maxX = panelImp.getWindow().getX() + windowBorderX + (i + 1) * buttonWidth;
            int maxY = panelImp.getWindow().getY() + windowBorderY + buttonHeight;

            int x = imp.getWindow().getX();
            int y = imp.getWindow().getY();

            System.out.println("Check[" + i + "]: " + minX + " < " + x + " < " + maxX + " && " + minY + " < " + y + " < " + maxY );
            if (minX < x && x < maxX && minY < y && y < maxY) {
                imageParameterMap[i] = imp;
                panelImp.getWindow().toFront();
                refresh();
            }
        }
    }




}
