package emt.main;

import javastraw.reader.block.Block;
import javastraw.reader.block.ContactRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
        int[] bpBinSizes = {1000000, 500000, 250000, 100000, 50000, 25000, 10000, 5000, 2000, 1000, 500, 100};
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
                processContactRecordForWriteOutMND(cr, resolution, xChrom, yChrom, xOrigin, yOrigin, bwMND);
            }
        }
    }

    public static void writeOutMND(Iterator<ContactRecord> iterator, int resolution, int xOrigin, int yOrigin,
                                   BufferedWriter bwMND, String xChrom, String yChrom) throws IOException {
        while (iterator.hasNext()) {
            ContactRecord cr = iterator.next();
            processContactRecordForWriteOutMND(cr, resolution, xChrom, yChrom, xOrigin, yOrigin, bwMND);
        }
    }

    private static void processContactRecordForWriteOutMND(ContactRecord cr, int resolution,
                                                           String xChrom, String yChrom,
                                                           int xOrigin, int yOrigin, BufferedWriter bwMND) throws IOException {
        if (cr.getCounts() > 0) {
            int gx = (cr.getBinX() * resolution) - xOrigin;
            int gy = (cr.getBinY() * resolution) - yOrigin;
            bwMND.write(xChrom + " " + gx + " " + yChrom + " " + gy + " " + cr.getCounts());
            bwMND.newLine();
        }
    }

    public static void writeOutSubsampledMND(Iterator<ContactRecord> iterator, int resolution, int xOrigin, int yOrigin,
                                             BufferedWriter bwMND, String xChrom, String yChrom, double ratio,
                                             Random generator) throws IOException {
        while (iterator.hasNext()) {
            ContactRecord cr = iterator.next();
            processContactRecordForWriteOutSubsampledMND(cr, resolution, xChrom, yChrom, xOrigin, yOrigin, bwMND,
                    ratio, generator);
        }
    }

    private static void processContactRecordForWriteOutSubsampledMND(ContactRecord cr, int resolution,
                                                                     String xChrom, String yChrom,
                                                                     int xOrigin, int yOrigin, BufferedWriter bwMND,
                                                                     double ratio, Random generator) throws IOException {
        if (cr.getCounts() > 0) {
            int counts = getSubsampledNumberOfContacts(cr.getCounts(), ratio, generator);
            if (counts > 0) {
                int gx = (cr.getBinX() * resolution) - xOrigin;
                int gy = (cr.getBinY() * resolution) - yOrigin;
                bwMND.write(xChrom + " " + gx + " " + yChrom + " " + gy + " " + counts);
                bwMND.newLine();
            }
        }
    }

    private static int getSubsampledNumberOfContacts(float counts, double ratio, Random generator) {
        int newCounts = 0;
        for (int z = 0; z < counts; z++) {
            if (generator.nextFloat() < ratio) {
                newCounts++;
            }
        }
        return newCounts;
    }
}
