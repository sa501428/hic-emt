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

package emt.utils.common;

import javastraw.tools.ParallelizationTools;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelizedStatTools {
    public static double getParGlobalNonZeroMean(float[][] data, int[] weights) {
        final double[] accumTotal = new double[1];
        final long[] accumCount = new long[1];
        accumTotal[0] = 0;

        AtomicInteger index = new AtomicInteger(0);
        ParallelizationTools.launchParallelizedCode(() -> {
            int i = index.getAndIncrement();
            double total = 0;
            long count = 0;
            while (i < data.length) {
                for (int j = 0; j < data[i].length; j++) {
                    if (!Float.isNaN(data[i][j]) && data[i][j] > 0) {
                        total += data[i][j] * weights[j];
                        count += weights[j];
                    }
                }
                i = index.getAndIncrement();
            }
            synchronized (accumTotal) {
                accumTotal[0] += total;
                accumCount[0] += count;
            }
        });

        return accumTotal[0] / accumCount[0];
    }

    public static double getGlobalNonZeroStdDev(float[][] data, int[] weights, double mu) {
        final double[] squaresTotal = new double[1];
        final long[] countsTotal = new long[1];
        squaresTotal[0] = 0;

        AtomicInteger index = new AtomicInteger(0);
        ParallelizationTools.launchParallelizedCode(() -> {
            int i = index.getAndIncrement();
            double squares = 0;
            long count = 0;
            while (i < data.length) {
                for (int j = 0; j < data[i].length; j++) {
                    if (!Float.isNaN(data[i][j]) && data[i][j] > 0) {
                        double diff = data[i][j] - mu;
                        squares += diff * diff * weights[j];
                        count += weights[j];
                    }
                }
                i = index.getAndIncrement();
            }
            synchronized (squaresTotal) {
                squaresTotal[0] += squares;
                countsTotal[0] += count;
            }
        });

        return Math.sqrt(squaresTotal[0] / countsTotal[0]);
    }

    public static double[] getMeanAndStandardDev(float[][] data, int[] weights, boolean useWeights) {
        double mu, std;
        if (useWeights) {
            mu = ParallelizedStatTools.getParGlobalNonZeroMean(data, weights);
            std = ParallelizedStatTools.getGlobalNonZeroStdDev(data, weights, mu);
        } else {
            int[] weights2 = new int[data[0].length];
            Arrays.fill(weights2, 1);
            mu = ParallelizedStatTools.getParGlobalNonZeroMean(data, weights2);
            std = ParallelizedStatTools.getGlobalNonZeroStdDev(data, weights2, mu);
        }
        return new double[]{mu, std};
    }

    public static void scaleDown(float[][] data, int[] weights) {
        AtomicInteger index = new AtomicInteger(0);
        ParallelizationTools.launchParallelizedCode(() -> {
            int i = index.getAndIncrement();
            while (i < data.length) {
                for (int j = 0; j < data[i].length; j++) {
                    if (!Float.isNaN(data[i][j])) {
                        data[i][j] /= weights[j];
                    }
                }
                i = index.getAndIncrement();
            }
        });
    }

    public static void setZerosToNan(float[][] matrix) {
        AtomicInteger index = new AtomicInteger(0);
        ParallelizationTools.launchParallelizedCode(() -> {
            int i = index.getAndIncrement();
            while (i < matrix.length) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] < 1e-20) {
                        matrix[i][j] = Float.NaN;
                    }
                }
                i = index.getAndIncrement();
            }
        });
    }
}
