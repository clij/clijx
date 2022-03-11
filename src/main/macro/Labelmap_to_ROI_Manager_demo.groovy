import groovy.time.TimeCategory
import ij.IJ
import ij.ImagePlus
import net.haesleinhuepf.clij2.CLIJ2
import net.haesleinhuepf.clij2.plugins.StatisticsOfLabelledPixels
import ij.plugin.frame.RoiManager

ImagePlus particles = IJ.openImage("http://imagej.nih.gov/ij/images/particles.gif")
CLIJ2 clij2 = CLIJ2.getInstance()
def particles_cl = clij2.push(particles)
def lbl_img_cl = clij2.create([particles_cl.getWidth(), particles_cl.getHeight()])
clij2.connectedComponentsLabelingBox(particles_cl, lbl_img_cl)
def lblimg = clij2.pull(lbl_img_cl)

//Remove a square to show that label values are retained
clij2.release(lbl_img_cl)
lblimg.setRoi(586,164,251,218);
IJ.setBackgroundColor(0, 0, 0);
IJ.run(lblimg, "Clear", "slice");
lblimg.deleteRoi()

lbl_img_cl = clij2.push(lblimg)

def start = new Date()
// Get largest bounding box for labels
int maxW = 0
int maxH = 0
def stats = clij2.statisticsOfLabelledPixels(lbl_img_cl, lbl_img_cl)
for (int j=0; j<stats.length ; j++){
    if (stats[j][StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_WIDTH.value]>maxW){maxW=(int)stats[j][StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_WIDTH.value]}
    if (stats[j][StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_HEIGHT.value]>maxH){maxH=(int)stats[j][StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_HEIGHT.value]}
}
IJ.log('Largest boundingbox: W= ' +maxW.toString()+ ' ; H= ' +maxH.toString())
def roi_lbl_cl = clij2.create([maxW, maxH])
def roi_msk_cl = clij2.create([maxW, maxH])
// reset RoiManager
def rm = new RoiManager()
rm = rm.getRoiManager()
rm.reset()
rm.setVisible(false)

for (int j=0; j<stats.length ; j++){
    if (stats[j][StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_X.value]==Double.MAX_VALUE){continue}
    def i = (int) stats[j][StatisticsOfLabelledPixels.STATISTICS_ENTRY.MINIMUM_INTENSITY.value]
    def x = stats[j][StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_X.value]
    def y = stats[j][StatisticsOfLabelledPixels.STATISTICS_ENTRY.BOUNDING_BOX_Y.value]
    clij2.crop2D(lbl_img_cl, roi_lbl_cl, x.round(), y.round())
    clij2.labelToMask(roi_lbl_cl, roi_msk_cl, i)
    def myroi = clij2.pullAsROI(roi_msk_cl)
    myroi.setLocation(x,y)
    myroi.setName(i.toString())
    rm.addRoi(myroi)
}

rm.runCommand("UseNames", "true");
rm.setVisible(true)

def duration = TimeCategory.minus(new Date(), start)
IJ.log('Adding labels to ROI Manager took: ' +duration.toString())

IJ.run(lblimg, "glasbey_on_dark", "")
lblimg.resetDisplayRange()
lblimg.show()
rm.runCommand(lblimg,"Show All with labels");