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

import robust.concurrent.kmeans.clustering.Cluster;
import robust.concurrent.kmeans.clustering.KMeansListener;
import robust.concurrent.kmeans.clustering.RobustConcurrentKMeans;
import slice.MixerGlobals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class QuickCentroids {

    private final float[][] matrix;
    private final int initialNumClusters;
    private final Random generator = new Random(0);
    private final AtomicInteger numActualClusters = new AtomicInteger(0);
    private int maxIters = 1;
    private float[][] centroids = null;
    private int[] weights = null;

    public QuickCentroids(float[][] matrix, int numCentroids, long seed) {
        this.matrix = matrix;
        this.initialNumClusters = numCentroids;
        generator.setSeed(seed);
        if (matrix.length == 0 || matrix[0].length == 0) {
            System.err.println("Empty matrix provided for quick centroids");
            System.exit(5);
        }
    }

    public QuickCentroids(float[][] matrix, int numCentroids, long seed, int numIters) {
        this(matrix, numCentroids, seed);
        this.maxIters = numIters;
    }

    public float[][] generateCentroids(int minSizeNeeded) {
        RobustConcurrentKMeans kMeans = new RobustConcurrentKMeans(matrix, initialNumClusters, maxIters, generator.nextLong());

        KMeansListener kMeansListener = new KMeansListener() {
            @Override
            public void kmeansMessage(String s) {
                if (MixerGlobals.printVerboseComments) {
                    System.out.println(s);
                }
            }

            @Override
            public void kmeansComplete(Cluster[] clusters) {
                convertClustersToFloatMatrix(clusters, minSizeNeeded);
                System.out.print(".");
            }

            @Override
            public void kmeansError(Throwable throwable) {
                throwable.printStackTrace();
                System.err.println("Error - " + throwable.getLocalizedMessage());
                System.exit(18);
            }
        };
        kMeans.addKMeansListener(kMeansListener);
        kMeans.run();

        waitUntilDone();
        return centroids;
    }

    private void waitUntilDone() {
        while (numActualClusters.get() < 1) {
            System.out.print(".");
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void convertClustersToFloatMatrix(Cluster[] initialClusters, int minSizeNeeded) {
        List<Cluster> actualClusters = new ArrayList<>();
        for (Cluster c : initialClusters) {
            if (c.getMemberIndexes().length > minSizeNeeded) {
                actualClusters.add(c);
            }
        }

        int numCPUThreads = Runtime.getRuntime().availableProcessors();
        if (MixerGlobals.printVerboseComments) {
            System.out.println("Using " + numCPUThreads + " threads");
        }
        AtomicInteger currRowIndex = new AtomicInteger(0);
        centroids = new float[actualClusters.size()][matrix[0].length];
        weights = new int[actualClusters.size()];
        ExecutorService executor = Executors.newFixedThreadPool(numCPUThreads);
        for (int l = 0; l < numCPUThreads; l++) {
            executor.execute(() -> {
                int c = currRowIndex.getAndIncrement();
                while (c < actualClusters.size()) {
                    processCluster(actualClusters.get(c), c);
                    c = currRowIndex.getAndIncrement();
                }
            });
        }
        executor.shutdown();
        // Wait until all threads finish
        //noinspection StatementWithEmptyBody
        while (!executor.isTerminated()) {
        }

        numActualClusters.set(actualClusters.size());
    }

    private void processCluster(Cluster cluster, int cIndex) {
        if (cluster.getMemberIndexes().length < 3) {
            // likely an outlier / too small a cluster
            return;
        }

        weights[cIndex] = cluster.getMemberIndexes().length;

        int[] counts = new int[matrix[0].length];
        for (int i : cluster.getMemberIndexes()) {
            for (int j = 0; j < matrix[i].length; j++) {
                float val = matrix[i][j];
                if (!Float.isNaN(val)) {
                    centroids[cIndex][j] += val;
                    counts[j]++;
                }
            }
        }
        for (int j = 0; j < counts.length; j++) {
            if (counts[j] > 0) {
                centroids[cIndex][j] /= counts[j];
            } else {
                centroids[cIndex][j] = Float.NaN;
            }
        }
    }

    public int[] getWeights() {
        return weights;
    }
}
