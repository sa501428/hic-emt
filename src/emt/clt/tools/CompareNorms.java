package emt.clt.tools;

import emt.Globals;
import emt.clt.CommandLineParser;
import emt.utils.validation.MatrixSum;
import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.datastructures.ListOfDoubleArrays;
import javastraw.reader.norm.NormalizationVector;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;
import javastraw.tools.MatrixTools;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;

public class CompareNorms extends CLT {

    private String file1, file2;

    public CompareNorms() {
        super("compare-norms <file1.hic> <file2.hic>");
    }

    public static void compareNormVectors(Dataset ds1, Dataset ds2) {
        Chromosome[] array = ds1.getChromosomeHandler().getChromosomeArrayWithoutAllByAll();
        List<NormalizationType> norms = ds1.getNormalizationTypes();
        List<HiCZoom> zooms = ds1.getBpZooms();

        double magnitude = 0;

        for (Chromosome chrom : array) {
            for (NormalizationType norm : norms) {
                //if (norm.getLabel().toLowerCase().contains("vc")) continue;

                int[] res = new int[zooms.size()];
                double[][] errMeans = new double[2][zooms.size()];
                double[][] errMedians = new double[2][zooms.size()];
                int[][] numNans = new int[3][zooms.size()];
                boolean somethingWasAdded = false;

                for (int q = 0; q < zooms.size(); q++) {
                    HiCZoom zoom = zooms.get(q);
                    res[q] = zoom.getBinSize();
                    NormalizationVector nv1 = ds1.getNormalizationVector(chrom.getIndex(), zoom, norm);
                    NormalizationVector nv2 = ds2.getNormalizationVector(chrom.getIndex(), zoom, norm);
                    if (nv1 == nv2) continue;
                    if (nv1 == null || nv2 == null) {
                        System.err.println("NULL ERROR IN NORM " + norm.getLabel() + " at " + zoom.getBinSize() + "!!! nv1 " + nv1 + "  nv2 " + nv2);
                        continue;
                    }
                    if (nv1.getData() == nv2.getData()) continue;
                    System.out.println("Norm vector " + chrom.getName() + " " + norm.getLabel() + " " + zoom.getBinSize());
                    compareVectorsForNans(nv1.getData(), nv2.getData(), "Norm vector " +
                                    chrom.getName() + " " + norm.getLabel() + " " + zoom.getBinSize(),
                            numNans, q);
                    compareRowSums(ds1, ds2, chrom, norm, zoom, nv1.getData(), nv2.getData(),
                            "Norm vector " + chrom.getName() + " " + norm.getLabel() + " " + zoom.getBinSize(),
                            errMeans, errMedians, q, chrom.getName() + "_" + norm.getLabel() + "_" + zoom.getBinSize());
                    somethingWasAdded = true;
                }

                if (somethingWasAdded) {
                    MatrixTools.saveMatrixTextNumpy(chrom.getName() + "_" + norm.getLabel() + "_res.npy", res);
                    MatrixTools.saveMatrixTextNumpy(chrom.getName() + "_" + norm.getLabel() + "_err_means.npy", errMeans);
                    MatrixTools.saveMatrixTextNumpy(chrom.getName() + "_" + norm.getLabel() + "_err_medians.npy", errMedians);
                    MatrixTools.saveMatrixTextNumpy(chrom.getName() + "_" + norm.getLabel() + "_num_nans.npy", numNans);
                }
            }
        }
        System.out.println("Normalization vectors are equivalent (" + magnitude + ")");
    }

    private static void compareRowSums(Dataset ds1, Dataset ds2, Chromosome chrom, NormalizationType norm, HiCZoom zoom,
                                       ListOfDoubleArrays nv1, ListOfDoubleArrays nv2, String description,
                                       double[][] errMeans, double[][] errMedians, int zIndex, String stem) {
        DescriptiveStatistics rowSumsStats1 = MatrixSum.getRowSums(ds1, chrom, norm, zoom, nv1, stem + "_f1", false);
        if (rowSumsStats1 != null) {
            printErrs(rowSumsStats1, description, "Norm 1", errMeans, errMedians, 0, zIndex);
        }
        DescriptiveStatistics rowSumsStats2 = MatrixSum.getRowSums(ds2, chrom, norm, zoom, nv2, stem + "_f2", false);
        if (rowSumsStats2 != null) {
            printErrs(rowSumsStats2, description, "Norm 2", errMeans, errMedians, 1, zIndex);
        }
    }

    private static void printErrs(DescriptiveStatistics stats, String description, String id,
                                  double[][] errMeans, double[][] errMedians, int index, int zIndex) {
        double median = stats.getPercentile(50);
        double mean = stats.getMean();
        double min = stats.getMin();
        double max = stats.getMax();

        double errMean = Math.max(mean - min, max - mean) / mean;
        double errMedian = Math.max(median - min, max - median) / median;
        errMeans[index][zIndex] = errMean;
        errMedians[index][zIndex] = errMedian;
        if (Globals.printVerboseComments) {
            System.out.println(id + " ErrMean " + errMean + "  ErrMedian " + errMedian + "  " + description);
        }
    }

    public static void compareVectorsForNans(ListOfDoubleArrays data1, ListOfDoubleArrays data2, String description,
                                             int[][] numNans, int zIndex) {

        if (data1.getLength() != data2.getLength()) {
            if (Globals.printVerboseComments) {
                System.err.println("Vector length mismatch: " + data1.getLength() + " vs " + data2.getLength() + " " + description);
            }
        }

        try {
            int n = (int) Math.min(data1.getLength(), data2.getLength());
            int both = 0;
            int onlyOne = 0;
            int onlyTwo = 0;

            for (long q = 0; q < n; q++) {
                if (Double.isNaN(data1.get(q)) && Double.isNaN(data2.get(q))) {
                    both++;
                } else if (Double.isNaN(data1.get(q))) {
                    onlyOne++;
                } else if (Double.isNaN(data2.get(q))) {
                    onlyTwo++;
                }
            }
            if (Globals.printVerboseComments) {
                System.out.println("Nans both: " + both + " first: " + onlyOne + " second: " + onlyTwo + " N " + n + "  " + description);
            }
            numNans[0][zIndex] = both + onlyOne;
            numNans[1][zIndex] = both + onlyTwo;
            numNans[2][zIndex] = n;
        } catch (Exception e) {
            System.exit(26);
        }
    }

    @Override
    protected void readAdditionalArguments(String[] args, CommandLineParser parser) {
        if (args.length != 3) {
            printUsageAndExit(9);
        }

        file1 = args[1];
        file2 = args[2];
    }

    @Override
    public void run() {
        Dataset ds1 = HiCFileTools.extractDatasetForCLT(file1, true, false);
        Dataset ds2 = HiCFileTools.extractDatasetForCLT(file2, true, false);
        compareNormVectors(ds1, ds2);
    }
}
