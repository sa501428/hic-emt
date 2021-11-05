package stitch;

import javastraw.reader.Dataset;
import javastraw.reader.Matrix;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.block.Block;
import javastraw.reader.block.ContactRecord;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class Stitcher {

    private String newChromSizes = "custom.chrom.sizes";
    private String newMND = "custom.mnd.txt";
    private String[] files, stems, regions;
    private String normalization;
    private boolean adjustOrigin;
    private int resolution;
    private HiCZoom zoom;

    public Stitcher(String[] files, String[] stems, String[] regions,
                    String normalization, boolean adjustOrigin, int resolution) {
        this.files = files;
        this.stems = stems;
        this.regions = regions;
        this.normalization = normalization;
        this.adjustOrigin = adjustOrigin;
        this.resolution = resolution;
        zoom = new HiCZoom(HiCZoom.HiCUnit.BP, resolution);
    }

    public void buildTempFiles() throws IOException {
        BufferedWriter bwChromDotSizes = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newChromSizes)));
        BufferedWriter bwMND = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newMND)));
        for (int s = 0; s < files.length; s++) {
            String file = files[s];
            String stem = stems[s];
            Dataset ds = HiCFileTools.extractDatasetForCLT(file, true, false);
            NormalizationType norm = ds.getNormalizationHandler().getNormTypeFromString(normalization);
            for (String region : regions) {
                processRegion(region, ds, norm, stem, bwChromDotSizes, bwMND);
            }
        }
        bwChromDotSizes.close();
        bwMND.close();
    }

    private void processRegion(String region, Dataset ds, NormalizationType norm, String stem,
                               BufferedWriter bwChromDotSizes, BufferedWriter bwMND) throws IOException {

        String[] regionSplit = region.split(":");
        Chromosome chrom = ds.getChromosomeHandler().getChromosomeFromName(regionSplit[0]);
        int posStart = Integer.parseInt(regionSplit[1]);
        int posEnd = Integer.parseInt(regionSplit[2]);

        Matrix matrix = ds.getMatrix(chrom, chrom);
        MatrixZoomData zd = matrix.getZoomData(zoom);

        List<Block> blocks = HiCFileTools.getAllRegionBlocks(zd,
                posStart / resolution, posEnd / resolution + 1,
                posStart / resolution, posEnd / resolution + 1,
                norm, false);


        String newChromName = stem + "_" + chrom.getName();
        long newLength;
        if (adjustOrigin) {
            newLength = posEnd - posStart + (5L * resolution);
        } else {
            newLength = posEnd + (2L * resolution);
        }

        bwChromDotSizes.write(newChromName + "\t" + newLength);
        bwChromDotSizes.newLine();

        for (Block block : blocks) {
            for (ContactRecord cr : block.getContactRecords()) {
                int gx = cr.getBinX() * resolution;
                int gy = cr.getBinY() * resolution;
                if (adjustOrigin) {
                    gx -= posStart;
                    gy -= posStart;
                }
                bwMND.write(newChromName + " " + gx + " " + newChromName + " " + gy + " " + cr.getCounts());
                bwMND.newLine();
            }
        }
    }
}
