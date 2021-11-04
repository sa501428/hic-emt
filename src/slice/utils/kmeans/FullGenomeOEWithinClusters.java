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

package slice.utils.kmeans;

import javastraw.feature1D.GenomeWideList;
import javastraw.reader.Dataset;
import javastraw.reader.basics.ChromosomeHandler;
import javastraw.reader.type.NormalizationType;
import slice.utils.cleaning.GWBadIndexFinder;
import slice.utils.matrices.SliceMatrix;
import slice.utils.structures.SliceUtils;
import slice.utils.structures.SubcompartmentInterval;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FullGenomeOEWithinClusters {
    public static int startingClusterSizeK = 2;
    public static int numClusterSizeKValsUsed = 10;
    public static int numAttemptsForKMeans = 3;
    private final File outputDirectory;
    private final ChromosomeHandler chromosomeHandler;
    private final SliceMatrix sliceMatrix;

    private final Random generator = new Random(0);

    public FullGenomeOEWithinClusters(List<Dataset> datasets, ChromosomeHandler chromosomeHandler, int resolution,
                                      List<NormalizationType[]> normalizationTypes,
                                      File outputDirectory, long seed) {
        this.chromosomeHandler = chromosomeHandler;
        this.outputDirectory = outputDirectory;
        generator.setSeed(seed);

        GWBadIndexFinder badIndexFinder = new GWBadIndexFinder(chromosomeHandler.getAutosomalChromosomesArray(),
                resolution, normalizationTypes);
        badIndexFinder.createInternalBadList(datasets, chromosomeHandler.getAutosomalChromosomesArray());

        int absMaxClusters = numClusterSizeKValsUsed + startingClusterSizeK;
        sliceMatrix = new SliceMatrix(chromosomeHandler, datasets.get(0), normalizationTypes.get(0), resolution, outputDirectory,
                generator.nextLong(), badIndexFinder, absMaxClusters);
        sliceMatrix.cleanUpMatricesBySparsity();
    }

    public void extractFinalGWSubcompartments(String prefix) {
        System.out.println("Inter-chromosomal clustering");
        runClusteringOnRawMatrixWithNans(prefix);
    }

    public void runClusteringOnRawMatrixWithNans(String prefix) {
        Map<Integer, GenomeWideList<SubcompartmentInterval>> kmeansClustersToResults = new HashMap<>();
        Map<Integer, List<List<Integer>>> kmeansIndicesMap = new HashMap<>();

        GenomeWideKmeansRunner kmeansRunner = new GenomeWideKmeansRunner(chromosomeHandler, sliceMatrix);
        KmeansEvaluator evaluator = new KmeansEvaluator(numClusterSizeKValsUsed);

        for (int z = 0; z < numClusterSizeKValsUsed; z++) {
            int maxIters = 200;
            runRepeatedKMeansClusteringLoop(numAttemptsForKMeans, kmeansRunner, evaluator, z,
                    maxIters, kmeansClustersToResults, kmeansIndicesMap);
            exportKMeansClusteringResults(z, evaluator, kmeansClustersToResults, prefix);
        }
        System.out.println(".");
    }

    public void exportKMeansClusteringResults(int z, KmeansEvaluator evaluator,
                                              Map<Integer, GenomeWideList<SubcompartmentInterval>> numClustersToResults,
                                              String prefix) {
        int k = z + startingClusterSizeK;
        String kstem = "kmeans";
        evaluator.export(outputDirectory, kstem);
        GenomeWideList<SubcompartmentInterval> gwList = numClustersToResults.get(k);
        SliceUtils.collapseGWList(gwList);
        File outBedFile = new File(outputDirectory, prefix + "_" + k + "_" + kstem + "_clusters.bed");
        gwList.simpleExport(outBedFile);
    }

    public void runRepeatedKMeansClusteringLoop(int attemptsForKMeans, GenomeWideKmeansRunner kmeansRunner,
                                                KmeansEvaluator evaluator, int z, int maxIters,
                                                Map<Integer, GenomeWideList<SubcompartmentInterval>> numClustersToResults,
                                                Map<Integer, List<List<Integer>>> indicesMap) {
        int numClusters = z + startingClusterSizeK;
        int numColumns = kmeansRunner.getNumColumns();
        int numRows = kmeansRunner.getNumRows();
        for (int p = 0; p < attemptsForKMeans; p++) {
            kmeansRunner.prepareForNewRun(numClusters);
            kmeansRunner.launchKmeansGWMatrix(generator.nextLong(), maxIters);

            int numActualClustersThisAttempt = kmeansRunner.getNumActualClusters();
            if (numActualClustersThisAttempt == numClusters) {
                double wcss = kmeansRunner.getWithinClusterSumOfSquares();
                if (wcss < evaluator.getWCSS(z)) {
                    evaluator.setMseAicBicValues(z, numClusters, wcss, numRows, numColumns);
                    indicesMap.put(z, kmeansRunner.getIndicesMapCopy());
                    numClustersToResults.put(numClusters, kmeansRunner.getFinalCompartments());
                }
            }
            System.out.print(".");
        }
    }
}
