package emt.main;

import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.basics.ChromosomeHandler;
import javastraw.reader.block.Block;
import javastraw.reader.block.ContactRecord;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;

import java.util.List;

public class DatasetUtils {

    public static long getTotalContacts(Dataset ds) {
        ChromosomeHandler handler = ds.getChromosomeHandler();
        Chromosome[] chroms = handler.getChromosomeArrayWithoutAllByAll();

        long totalCounts = 0L;
        int lowestResZoom = getLowestResolution(ds);
        NormalizationType normNone = ds.getNormalizationHandler().getNormTypeFromString("NONE");

        for (int i = 0; i < chroms.length; i++) {
            Chromosome chr1 = chroms[i];
            for (int j = i; j < chroms.length; j++) {
                Chromosome chr2 = chroms[j];
                final MatrixZoomData zd = HiCFileTools.getMatrixZoomData(ds, chr1, chr2, lowestResZoom);
                int lengthChr1 = (int) (chr1.getLength() / lowestResZoom ) + 1;
                int lengthChr2 = (int) (chr2.getLength() / lowestResZoom ) + 1;

                try {
                    List<Block> blocks = HiCFileTools.getAllRegionBlocks(zd,
                            0, lengthChr1, 0, lengthChr2, normNone, false);
                    totalCounts += getTotalCountsFromRegion(blocks);
                } catch (Exception e) {
                    System.err.println(chr1.getName() + " - " + chr2.getName());
                    e.printStackTrace();
                }
            }
        }

        return totalCounts;
    }

    private static int getLowestResolution(Dataset ds) {
        List<HiCZoom> zooms = ds.getBpZooms();
        int maxResolution = zooms.get(0).getBinSize();
        for (HiCZoom zoom : zooms) {
            if (zoom.getBinSize() > maxResolution) {
                maxResolution = zoom.getBinSize();
            }
        }
        return maxResolution;
    }

    private static long getTotalCountsFromRegion(List<Block> blocks) {
        double total = 0;

        for (Block b : blocks) {
            if (b != null) {
                for (ContactRecord cr : b.getContactRecords()) {
                    if(!Float.isNaN(cr.getCounts())) {
                        total += cr.getCounts();
                    }
                }
            }
        }

        return (long) total;
    }
}