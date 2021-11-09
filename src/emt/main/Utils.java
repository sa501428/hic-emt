package emt.main;

import javastraw.reader.block.Block;
import javastraw.reader.block.ContactRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Utils {

    private static int progressCounter = 0;

    public static void printProgressDot() {
        if (++progressCounter % 10 == 0) {
            System.out.println(".");
        } else {
            System.out.print(".");
        }
    }

    static boolean checkIfStandardGenome(String genomeId) {
        String gID = genomeId.toLowerCase();
        return gID.equals("hg19") || gID.equals("hg38") ||
                gID.equals("mm9") || gID.equals("mm10");
    }

    public static String getResolutionsToBuild(int highestResolution) {
        StringBuilder resolutionsToBuild = new StringBuilder("2500000");
        int[] bpBinSizes = {1000000, 500000, 250000, 100000, 50000, 25000, 10000, 5000, 1000, 500, 100};
        for (int res : bpBinSizes) {
            if (res >= highestResolution) {
                resolutionsToBuild.append(",").append(res);
            }
        }
        return resolutionsToBuild.toString();
    }

    public static void writeOutMND(List<Block> blocks, int resolution, int xOrigin, int yOrigin,
                                   BufferedWriter bwMND, String xChrom, String yChrom) throws IOException {
        for (Block block : blocks) {
            for (ContactRecord cr : block.getContactRecords()) {
                if (Float.isNaN(cr.getCounts())) continue;
                int gx = (cr.getBinX() * resolution) - xOrigin;
                int gy = (cr.getBinY() * resolution) - yOrigin;
                bwMND.write(xChrom + " " + gx + " " + yChrom + " " + gy + " " + cr.getCounts());
                bwMND.newLine();
            }
        }
    }
}