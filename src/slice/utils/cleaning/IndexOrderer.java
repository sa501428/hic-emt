/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2021 Rice University, Baylor College of Medicine, Aiden Lab
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package slice.utils.cleaning;

import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.ExtractingOEDataUtils;
import javastraw.tools.HiCFileTools;
import slice.MixerGlobals;
import slice.clt.ParallelizedMixerTools;
import slice.utils.common.ArrayTools;
import slice.utils.similaritymeasures.RobustCorrelationSimilarity;
import slice.utils.similaritymeasures.SimilarityMetric;
import slice.utils.structures.SubcompartmentColors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexOrderer {

    private static final int CHECK_VAL = -2;
    private final Map<Chromosome, int[]> chromToReorderedIndices = new HashMap<>();
    private final int DISTANCE = 5000000;
    private final int ONE_HUNDRED_KB = 100000;
    private final int IGNORE = -1;
    private final float CORR_MIN = 0.2f;
    private final Random generator = new Random(0);
    private final Map<Integer, Integer> indexToRearrangedLength = new HashMap<>();
    private final File problemFile, initFile;
    private final int hires;
    private final int resFactor;

    public IndexOrderer(Dataset ds, Chromosome[] chromosomes, int inputResolution, NormalizationType normalizationType,
                        GWBadIndexFinder badIndexLocations, long seed, File outputDirectory) {
        int lowres = Math.max(inputResolution, 100000);
        hires = inputResolution;
        resFactor = lowres / hires;
        if (lowres % hires != 0) {
            System.err.println(hires + "is not a factor of " + lowres + ". Invalid resolutions.");
            System.exit(23);
        }

        problemFile = new File(outputDirectory, "problems.bed");
        initFile = new File(outputDirectory, "initial_split.bed");

        generator.setSeed(seed);
        for (Chromosome chrom : chromosomes) {
            final MatrixZoomData zd = HiCFileTools.getMatrixZoomData(ds, chrom, chrom, lowres);
            try {
                float[][] matrix = HiCFileTools.getOEMatrixForChromosome(ds, zd, chrom, lowres,
                        normalizationType, 10f, ExtractingOEDataUtils.ThresholdType.TRUE_OE,
                        true, 1, 0);
                Set<Integer> badIndices = badIndexLocations.getBadIndices(chrom);

                IntraMatrixCleaner.cleanAndCompress(matrix, badIndices, resFactor);
                int[] newOrderIndexes = getNewOrderOfIndices(chrom, matrix, badIndices);
                int[] hiResNewOrderIndexes = convertToHigherRes(newOrderIndexes, chrom);
                chromToReorderedIndices.put(chrom, hiResNewOrderIndexes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.print(".");
        }

        if (MixerGlobals.printVerboseComments) {
            writeOutInitialResults();
        }
    }

    public static float[][] quickCleanMatrix(float[][] matrix, int[] newIndexOrderAssignments) {
        List<Integer> actualIndices = new ArrayList<>();
        for (int z = 0; z < newIndexOrderAssignments.length; z++) {
            if (newIndexOrderAssignments[z] < CHECK_VAL && ArrayTools.percentNaN(matrix[z]) < .7) {
                actualIndices.add(z);
            }
        }

        float[][] tempCleanMatrix = new float[actualIndices.size()][matrix[0].length];
        for (int i = 0; i < actualIndices.size(); i++) {
            System.arraycopy(matrix[actualIndices.get(i)], 0, tempCleanMatrix[i], 0, tempCleanMatrix[i].length);
        }
        if (MixerGlobals.printVerboseComments) {
            System.out.println("New clean matrix: " + tempCleanMatrix.length + " rows kept from " + matrix.length);
        }
        return tempCleanMatrix;
    }

    public Map<Integer, Integer> getIndexToRearrangedLength() {
        return indexToRearrangedLength;
    }

    public int[] get(Chromosome chrom) {
        return chromToReorderedIndices.get(chrom);
    }

    private int[] convertToHigherRes(int[] lowResOrderIndexes, Chromosome chrom) {
        int hiResLength = (int) (chrom.getLength() / hires) + 1;
        int[] hiResOrderAssignments = new int[hiResLength];
        if ((hiResLength - 1) / resFactor >= lowResOrderIndexes.length) {
            System.err.println("chromosome lengths are off");
            System.exit(32);
        }

        for (int i = 0; i < hiResOrderAssignments.length; i++) {
            hiResOrderAssignments[i] = lowResOrderIndexes[i / resFactor];
        }
        return hiResOrderAssignments;
    }

    private int[] getNewOrderOfIndices(Chromosome chromosome, float[][] initialMatrix, Set<Integer> badIndices) {
        int[] newIndexOrderAssignments = generateNewAssignments(initialMatrix.length, badIndices);
        int TWENTY_MB = 20000000;
        int numPotentialClusters = (int) (chromosome.getLength() / TWENTY_MB) + 5;
        numPotentialClusters = Math.max(numPotentialClusters, 6);

        float[][] matrix = SimilarityMatrixTools.getSymmNonNanSimilarityMatrixWithMask(initialMatrix,
                RobustCorrelationSimilarity.SINGLETON, newIndexOrderAssignments, CHECK_VAL);

        int gCounter = doAssignmentsByCorrWithCentroids(matrix, newIndexOrderAssignments, chromosome.getName(),
                numPotentialClusters);
        indexToRearrangedLength.put(chromosome.getIndex(), gCounter);
        return newIndexOrderAssignments;
    }

    private int doAssignmentsByCorrWithCentroids(float[][] matrix, int[] newIndexOrderAssignments, String chromName,
                                                 int numInitialClusters) {
        float[][] centroids = new QuickCentroids(quickCleanMatrix(matrix, newIndexOrderAssignments),
                numInitialClusters, generator.nextLong(), 100).generateCentroids(5);

        if (MixerGlobals.printVerboseComments) {
            System.out.println("IndexOrderer: num centroids (init " + numInitialClusters + ") for " + chromName + ": " + centroids.length);
        }

        List<Integer> problemIndices = Collections.synchronizedList(new ArrayList<>());
        int[] clusterAssignment = new int[newIndexOrderAssignments.length];
        Arrays.fill(clusterAssignment, IGNORE);

        AtomicInteger currDataIndex = new AtomicInteger(0);
        ParallelizedMixerTools.launchParallelizedCode(() -> {
            SimilarityMetric corrMetric = RobustCorrelationSimilarity.SINGLETON;
            int i = currDataIndex.getAndIncrement();
            while (i < (matrix).length) {
                if (newIndexOrderAssignments[i] < CHECK_VAL) {
                    int bestIndex = IGNORE;
                    float bestCorr = CORR_MIN;

                    for (int j = 0; j < centroids.length; j++) {
                        float corrVal = corrMetric.distance(centroids[j], matrix[i]);
                        if (corrVal > bestCorr) {
                            bestCorr = corrVal;
                            bestIndex = j;
                        }
                    }
                    if (bestIndex < 0) {
                        synchronized (problemIndices) {
                            problemIndices.add(bestIndex);
                        }
                    }
                    clusterAssignment[i] = bestIndex;
                }
                i = currDataIndex.getAndIncrement();
            }
        });


        if (MixerGlobals.printVerboseComments) {
            synchronized (problemIndices) {
                double percentProblem = 100 * (problemIndices.size() + 0.0) / (matrix.length + 0.0);
                System.out.println("IndexOrderer problems: " + problemIndices.size() + " (" + percentProblem + " %)");
            }
        }

        int filtered = 0;
        for (int z = 0; z < clusterAssignment.length; z++) {
            int zMinus1 = Math.max(0, z - 1);
            int zPlus1 = Math.min(z + 1, clusterAssignment.length - 1);
            if (clusterAssignment[z] != clusterAssignment[zMinus1]
                    && clusterAssignment[z] != clusterAssignment[zPlus1]) {
                clusterAssignment[z] = IGNORE;
                filtered++;
            }
        }
        if (MixerGlobals.printVerboseComments) {
            System.out.println("Post filtered: " + filtered);
        }

        for (int i = 0; i < clusterAssignment.length; i++) {
            if (newIndexOrderAssignments[i] < CHECK_VAL) {
                newIndexOrderAssignments[i] = clusterAssignment[i];
            }
        }

        return centroids.length;
    }

    private int[] generateNewAssignments(int length, Set<Integer> badIndices) {
        int[] newIndexOrderAssignments = new int[length];
        int DEFAULT = -5;
        Arrays.fill(newIndexOrderAssignments, DEFAULT);
        for (int k : badIndices) {
            newIndexOrderAssignments[k / resFactor] = IGNORE;
        }
        return newIndexOrderAssignments;
    }

    private void writeOutInitialResults() {
        try {
            final FileWriter fwProblem = new FileWriter(problemFile);
            final FileWriter fwInit = new FileWriter(initFile);

            for (Chromosome chrom : chromToReorderedIndices.keySet()) {
                int[] vals = chromToReorderedIndices.get(chrom);
                for (int i = 0; i < vals.length; i++) {
                    if (vals[i] < 0) {
                        writeRegionToFile(fwProblem, chrom, i, vals[i]);
                    } else {
                        writeRegionToFile(fwInit, chrom, i, vals[i]);
                    }
                }
            }

            try {
                fwProblem.close();
                fwInit.close();
            } catch (IOException ww) {
                ww.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeRegionToFile(FileWriter fw, Chromosome chrom, int pos, int val) throws IOException {

        int x1 = hires * pos;
        int x2 = x1 + hires;
        String bedLine = "chr" + chrom.getName() + "\t" + x1 + "\t" + x2 + "\t" + val + "\t" + val
                + "\t.\t" + x1 + "\t" + x2 + "\t" + SubcompartmentColors.getColorString(Math.abs(val));
        fw.write(bedLine + "\n");
    }
}
