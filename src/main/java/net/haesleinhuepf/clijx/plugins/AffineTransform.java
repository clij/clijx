package net.haesleinhuepf.clijx.plugins;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.haesleinhuepf.clij.clearcl.interfaces.ClearCLImageInterface;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.AffineTransform2D;
import net.haesleinhuepf.clij2.plugins.AffineTransform3D;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

/**
 * Author: @haesleinhuepf
 * 10 2020
 */
@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_affineTransform")
public class AffineTransform extends AffineTransform3D implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized {
    @Override
    public boolean executeCL() {
        ClearCLBuffer input = (ClearCLBuffer) args[0];
        ClearCLBuffer output = (ClearCLBuffer) args[1];
        String transform = (String) args[2];

        if (input.getDimension() == 2) {
            net.imglib2.realtransform.AffineTransform2D at = parseAffineTransform2D(input, transform);
            at = at.inverse();
            return getCLIJ2().affineTransform2D(input, output, at);
        } else {
            net.imglib2.realtransform.AffineTransform3D at = parseAffineTransform3D(input, transform);
            at = at.inverse();
            return getCLIJ2().affineTransform3D(input, output, at);
        }
    }

    public static net.imglib2.realtransform.AffineTransform2D parseAffineTransform2D(ClearCLBuffer input, String transform) {
        String[] transformCommands = transform.trim().toLowerCase().split(" ");
        net.imglib2.realtransform.AffineTransform2D at = new net.imglib2.realtransform.AffineTransform2D();
        for(String transformCommand : transformCommands) {
            String[] commandParts = transformCommand.split("=");
            //System.out.print("Command: " + commandParts[0]);
            if (commandParts[0].compareTo("center") == 0) {
                net.imglib2.realtransform.AffineTransform2D translateTransform = new net.imglib2.realtransform.AffineTransform2D();
                translateTransform.translate(-input.getWidth() / 2, -input.getHeight() / 2);
                at.concatenate(translateTransform);
            } else if (commandParts[0].compareTo("-center") == 0) {
                net.imglib2.realtransform.AffineTransform2D translateTransform = new net.imglib2.realtransform.AffineTransform2D();
                translateTransform.translate(input.getWidth() / 2, input.getHeight() / 2);
                at.concatenate(translateTransform);
            } else if (commandParts[0].compareTo("scale") == 0) {
                net.imglib2.realtransform.AffineTransform2D scaleTransform = new net.imglib2.realtransform.AffineTransform2D();
                scaleTransform.scale(Double.parseDouble(commandParts[1]));
                at.concatenate(scaleTransform);
            } else if (commandParts[0].compareTo("scalex") == 0) {
                net.imglib2.realtransform.AffineTransform2D scaleTransform = new net.imglib2.realtransform.AffineTransform2D();
                scaleTransform.set(Double.parseDouble(commandParts[1]),0,0);
                scaleTransform.set(1.0 , 1, 1);
                at.concatenate(scaleTransform);
            } else if (commandParts[0].compareTo("scaley") == 0) {
                net.imglib2.realtransform.AffineTransform2D scaleTransform = new net.imglib2.realtransform.AffineTransform2D();
                scaleTransform.set(1.0,0,0);
                scaleTransform.set(Double.parseDouble(commandParts[1]) , 1, 1);
                at.concatenate(scaleTransform);
            } else if (commandParts[0].compareTo("rotate") == 0 || commandParts[0].compareTo("rotate") == 0) {
                net.imglib2.realtransform.AffineTransform2D rotateTransform = new net.imglib2.realtransform.AffineTransform2D();
                float angle = (float)(asFloat(commandParts[1]) / 180.0f * Math.PI);
                rotateTransform.rotate(angle);
                at.concatenate(rotateTransform);
            } else if (commandParts[0].compareTo("translatex") == 0) {
                net.imglib2.realtransform.AffineTransform2D translateTransform = new net.imglib2.realtransform.AffineTransform2D();
                translateTransform.translate(Double.parseDouble(commandParts[1]), 0);
                at.concatenate(translateTransform);
            } else if (commandParts[0].compareTo("translatey") == 0) {
                net.imglib2.realtransform.AffineTransform2D translateTransform = new net.imglib2.realtransform.AffineTransform2D();
                translateTransform.translate(0,Double.parseDouble(commandParts[1]), 0);
                at.concatenate(translateTransform);
            } else if (commandParts[0].compareTo("shearxy") == 0) {
                double shear = Double.parseDouble(commandParts[1]);
                net.imglib2.realtransform.AffineTransform2D shearTransform = new net.imglib2.realtransform.AffineTransform2D();
                shearTransform.set(1.0, 0, 0 );
                shearTransform.set(1.0, 1, 1 );
                shearTransform.set(shear, 0, 1);
                //shearTransform.set(shear, 0, 2);
                at.concatenate(shearTransform);
            } else {
                System.out.print("Unknown transform: " + commandParts[0]);
            }
        }

        return at;
    }


    public static net.imglib2.realtransform.AffineTransform3D parseAffineTransform3D(ClearCLBuffer input, String transform) {
        String[] transformCommands = transform.trim().toLowerCase().split(" ");
        net.imglib2.realtransform.AffineTransform3D at = new net.imglib2.realtransform.AffineTransform3D();
        for(String transformCommand : transformCommands) {
            String[] commandParts = transformCommand.split("=");
            //System.out.print("Command: " + commandParts[0]);
            if (commandParts[0].compareTo("center") == 0) {
                net.imglib2.realtransform.AffineTransform3D translateTransform = new net.imglib2.realtransform.AffineTransform3D();
                translateTransform.translate(-input.getWidth() / 2, -input.getHeight() / 2, -input.getDepth() / 2);
                at.concatenate(translateTransform);
            } else if (commandParts[0].compareTo("-center") == 0) {
                net.imglib2.realtransform.AffineTransform3D translateTransform = new net.imglib2.realtransform.AffineTransform3D();
                translateTransform.translate(input.getWidth() / 2, input.getHeight() / 2, input.getDepth() / 2);
                at.concatenate(translateTransform);
            } else if (commandParts[0].compareTo("scale") == 0) {
                net.imglib2.realtransform.AffineTransform3D scaleTransform = new net.imglib2.realtransform.AffineTransform3D();
                scaleTransform.scale(Double.parseDouble(commandParts[1]));
                at.concatenate(scaleTransform);
            } else if (commandParts[0].compareTo("scalex") == 0) {
                net.imglib2.realtransform.AffineTransform3D scaleTransform = new net.imglib2.realtransform.AffineTransform3D();
                scaleTransform.set(Double.parseDouble(commandParts[1]),0,0);
                scaleTransform.set(1.0 , 1, 1);
                scaleTransform.set(1, 2, 2);
                at.concatenate(scaleTransform);
            } else if (commandParts[0].compareTo("scaley") == 0) {
                net.imglib2.realtransform.AffineTransform3D scaleTransform = new net.imglib2.realtransform.AffineTransform3D();
                scaleTransform.set(1.0,0,0);
                scaleTransform.set(Double.parseDouble(commandParts[1]) , 1, 1);
                scaleTransform.set(1, 2, 2);
                at.concatenate(scaleTransform);
            } else if (commandParts[0].compareTo("scalez") == 0) {
                net.imglib2.realtransform.AffineTransform3D scaleTransform = new net.imglib2.realtransform.AffineTransform3D();
                scaleTransform.set(1.0,0,0);
                scaleTransform.set(1.0 , 1, 1);
                scaleTransform.set(Double.parseDouble(commandParts[1]) , 2, 2);
                at.concatenate(scaleTransform);
            } else if (commandParts[0].compareTo("rotatex") == 0) {
                net.imglib2.realtransform.AffineTransform3D rotateTransform = new net.imglib2.realtransform.AffineTransform3D();
                float angle = (float)(asFloat(commandParts[1]) / 180.0f * Math.PI);
                rotateTransform.rotate(0, angle);
                at.concatenate(rotateTransform);
            } else if (commandParts[0].compareTo("rotatey") == 0) {
                net.imglib2.realtransform.AffineTransform3D rotateTransform = new net.imglib2.realtransform.AffineTransform3D();
                float angle = (float)(asFloat(commandParts[1]) / 180.0f * Math.PI);
                rotateTransform.rotate(1, angle);
                at.concatenate(rotateTransform);
            } else if (commandParts[0].compareTo("rotatez") == 0 || commandParts[0].compareTo("rotate") == 0) {
                net.imglib2.realtransform.AffineTransform3D rotateTransform = new net.imglib2.realtransform.AffineTransform3D();
                float angle = (float)(asFloat(commandParts[1]) / 180.0f * Math.PI);
                rotateTransform.rotate(2, angle);
                at.concatenate(rotateTransform);
            } else if (commandParts[0].compareTo("translatex") == 0) {
                net.imglib2.realtransform.AffineTransform3D translateTransform = new net.imglib2.realtransform.AffineTransform3D();
                translateTransform.translate(Double.parseDouble(commandParts[1]), 0, 0);
                at.concatenate(translateTransform);
            } else if (commandParts[0].compareTo("translatey") == 0) {
                net.imglib2.realtransform.AffineTransform3D translateTransform = new net.imglib2.realtransform.AffineTransform3D();
                translateTransform.translate(0,Double.parseDouble(commandParts[1]), 0);
                at.concatenate(translateTransform);
            } else if (commandParts[0].compareTo("translatez") == 0) {
                net.imglib2.realtransform.AffineTransform3D translateTransform = new net.imglib2.realtransform.AffineTransform3D();
                translateTransform.translate(0, 0, Double.parseDouble(commandParts[1]));
                at.concatenate(translateTransform);
            } else if (commandParts[0].compareTo("shearxy") == 0) {
                double shear = Double.parseDouble(commandParts[1]);
                net.imglib2.realtransform.AffineTransform3D shearTransform = new net.imglib2.realtransform.AffineTransform3D();
                shearTransform.set(1.0, 0, 0 );
                shearTransform.set(1.0, 1, 1 );
                shearTransform.set(1.0, 2, 2 );
                shearTransform.set(shear, 0, 1);
                //shearTransform.set(shear, 0, 2);
                at.concatenate(shearTransform);
            } else if (commandParts[0].compareTo("shearxz") == 0) {
                double shear = Double.parseDouble(commandParts[1]);
                net.imglib2.realtransform.AffineTransform3D shearTransform = new net.imglib2.realtransform.AffineTransform3D();
                shearTransform.set(1.0, 0, 0 );
                shearTransform.set(1.0, 1, 1 );
                shearTransform.set(1.0, 2, 2 );
                shearTransform.set(shear, 0, 2);
                at.concatenate(shearTransform);
            } else if (commandParts[0].compareTo("shearyx") == 0) {
                double shear = Double.parseDouble(commandParts[1]);
                net.imglib2.realtransform.AffineTransform3D shearTransform = new net.imglib2.realtransform.AffineTransform3D();
                shearTransform.set(1.0, 0, 0 );
                shearTransform.set(1.0, 1, 1 );
                shearTransform.set(1.0, 2, 2 );
                shearTransform.set(shear, 1, 0);
                at.concatenate(shearTransform);
            } else if (commandParts[0].compareTo("shearyz") == 0) {
                double shear = Double.parseDouble(commandParts[1]);
                net.imglib2.realtransform.AffineTransform3D shearTransform = new net.imglib2.realtransform.AffineTransform3D();
                shearTransform.set(1.0, 0, 0 );
                shearTransform.set(1.0, 1, 1 );
                shearTransform.set(1.0, 2, 2 );
                shearTransform.set(shear, 1, 2);
                at.concatenate(shearTransform);
            } else if (commandParts[0].compareTo("shearzx") == 0) {
                double shear = Double.parseDouble(commandParts[1]);
                net.imglib2.realtransform.AffineTransform3D shearTransform = new net.imglib2.realtransform.AffineTransform3D();
                shearTransform.set(1.0, 0, 0 );
                shearTransform.set(1.0, 1, 1 );
                shearTransform.set(1.0, 2, 2 );
                shearTransform.set(shear, 2, 0);
                at.concatenate(shearTransform);
            } else if (commandParts[0].compareTo("shearzy") == 0) {
                double shear = Double.parseDouble(commandParts[1]);
                net.imglib2.realtransform.AffineTransform3D shearTransform = new net.imglib2.realtransform.AffineTransform3D();
                shearTransform.set(1.0, 0, 0 );
                shearTransform.set(1.0, 1, 1 );
                shearTransform.set(1.0, 2, 2 );
                shearTransform.set(shear, 2, 1);
                at.concatenate(shearTransform);
            } else {
                System.out.print("Unknown transform: " + commandParts[0]);
            }
        }

        return at;
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    @Override
    public String getCategories() {
        return "Transform";
    }


    @Override
    public String getDescription() {
        return "Applies an affine transform to a 2D or 3D image\n\n" +
                "Note: This operation applies inverted transforms compared to CLIJ2 affineTransform2D and affineTransform3D.\n" +
                "Individual transforms must be separated by spaces.\n" +
                "\n\nSupported transforms in 2D and 3D:" +
                "\n* center: translate the coordinate origin to the center of the image" +
                "\n* -center: translate the coordinate origin back to the initial origin" +
                "\n* rotate=[angle]: rotate in X/Y plane (around Z-axis) by the given angle in degrees" +
                "\n* scale=[factor]: isotropic scaling according to given zoom factor" +
                "\n* scaleX=[factor]: scaling along X-axis according to given zoom factor" +
                "\n* scaleY=[factor]: scaling along Y-axis according to given zoom factor" +
                "\n* shearXY=[factor]: shearing along X-axis in XY plane according to given factor" +
                "\n* translateX=[distance]: translate along X-axis by distance given in pixels" +
                "\n* translateY=[distance]: translate along X-axis by distance given in pixels" +
                "\nAdditionally supported transforms in 3D:" +
                "\n* rotateX=[angle]: rotate in Y/Z plane (around X-axis) by the given angle in degrees" +
                "\n* rotateY=[angle]: rotate in X/Z plane (around Y-axis) by the given angle in degrees" +
                "\n* rotateZ=[angle]: rotate in X/Y plane (around Z-axis) by the given angle in degrees" +
                "\n* scaleZ=[factor]: scaling along Z-axis according to given zoom factor" +
                "\n* shearXZ=[factor]: shearing along X-axis in XZ plane according to given factor" +
                "\n* shearYX=[factor]: shearing along Y-axis in XY plane according to given factor" +
                "\n* shearYZ=[factor]: shearing along Y-axis in YZ plane according to given factor" +
                "\n* shearZX=[factor]: shearing along Z-axis in XZ plane according to given factor" +
                "\n* shearZY=[factor]: shearing along Z-axis in YZ plane according to given factor" +
                "\n* translateZ=[distance]: translate along X-axis by distance given in pixels" +
                "\n\nExample transform:" +
                "\ntransform = \"center scale=2 rotate=45 -center\";";
    }

    @Override
    public String getAuthorName() {
        return "Robert Haase based on work by Martin Weigert";
    }

    @Override
    public String getLicense() {
        return " adapted from: https://github.com/maweigert/gputools/blob/master/gputools/transforms/kernels/transformations.cl\n" +
                "\n" +
                " Copyright (c) 2016, Martin Weigert\n" +
                " All rights reserved.\n" +
                "\n" +
                " Redistribution and use in source and binary forms, with or without\n" +
                " modification, are permitted provided that the following conditions are met:\n" +
                "\n" +
                " * Redistributions of source code must retain the above copyright notice, this\n" +
                "   list of conditions and the following disclaimer.\n" +
                "\n" +
                " * Redistributions in binary form must reproduce the above copyright notice,\n" +
                "   this list of conditions and the following disclaimer in the documentation\n" +
                "   and/or other materials provided with the distribution.\n" +
                "\n" +
                " * Neither the name of gputools nor the names of its\n" +
                "   contributors may be used to endorse or promote products derived from\n" +
                "   this software without specific prior written permission.\n" +
                "\n" +
                " THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"\n" +
                " AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\n" +
                " IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\n" +
                " DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE\n" +
                " FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL\n" +
                " DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR\n" +
                " SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER\n" +
                " CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,\n" +
                " OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n" +
                " OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
    }

}
