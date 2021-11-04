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

import slice.utils.similaritymeasures.RobustCorrelationSimilarity;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SimilarityMatrixTools {


    public static float[][] getSymmNonNanSimilarityMatrixWithMask(float[][] initialMatrix,
                                                                  RobustCorrelationSimilarity metric,
                                                                  int[] newIndexOrderAssignments, int checkVal) {

        float[][] result = new float[initialMatrix.length][initialMatrix.length];
        for (float[] row : result) {
            Arrays.fill(row, Float.NaN);
        }

        int numCPUThreads = Runtime.getRuntime().availableProcessors();

        RobustCorrelationSimilarity.USE_ARC = true;

        AtomicInteger currRowIndex = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(numCPUThreads);
        for (int l = 0; l < numCPUThreads; l++) {
            Runnable worker = () -> {
                int i = currRowIndex.getAndIncrement();
                while (i < initialMatrix.length) {
                    if (newIndexOrderAssignments[i] < checkVal) {
                        result[i][i] = 1;
                        for (int j = i + 1; j < initialMatrix.length; j++) {
                            result[i][j] = metric.distance(initialMatrix[j], initialMatrix[i]);
                            result[j][i] = result[i][j];
                        }
                    }
                    i = currRowIndex.getAndIncrement();
                }
            };
            executor.execute(worker);
        }
        executor.shutdown();
        // Wait until all threads finish
        //noinspection StatementWithEmptyBody
        while (!executor.isTerminated()) {
        }
        RobustCorrelationSimilarity.USE_ARC = false;

        return result;
    }
}
