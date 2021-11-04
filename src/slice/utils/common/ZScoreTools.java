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

package slice.utils.common;

import javastraw.tools.ParallelizedJuicerTools;

import java.util.concurrent.atomic.AtomicInteger;

public class ZScoreTools {

    private static final float ZERO = 0;//1e-10f;

    public static void inPlaceScaleSqrtWeightCol(float[][] matrix, int[] weights) {
        if (weights.length != matrix[0].length) {
            System.err.println("Weights mismatch error " + weights.length + " vs " + matrix[0].length);
            System.exit(54);
        }

        AtomicInteger index = new AtomicInteger(0);
        ParallelizedJuicerTools.launchParallelizedCode(() -> {
            int i = index.getAndIncrement();
            while (i < matrix.length) {
                for (int j = 0; j < matrix[i].length; j++) {
                    float val = matrix[i][j];
                    if (!Float.isNaN(val)) {
                        matrix[i][j] = (float) (Math.sqrt(weights[j]) * val);
                    }
                }
                i = index.getAndIncrement();
            }
        });
    }

    public static void inPlaceZscoreDownCol(float[][] matrix) {
        float[] colMeans = getColMean(matrix);
        float[] colStdDevs = getColStdDev(matrix, colMeans);

        AtomicInteger index = new AtomicInteger(0);
        ParallelizedJuicerTools.launchParallelizedCode(() -> {
            int i = index.getAndIncrement();
            while (i < matrix.length) {
                for (int j = 0; j < matrix[i].length; j++) {
                    float val = matrix[i][j];
                    if (!Float.isNaN(val)) {
                        matrix[i][j] = (val - colMeans[j]) / colStdDevs[j];
                    }
                }
                i = index.getAndIncrement();
            }
        });
    }


    public static float[] getColMean(float[][] matrix) {
        final double[] totalColSums = new double[matrix[0].length];
        final int[] totalColSize = new int[totalColSums.length];

        AtomicInteger index = new AtomicInteger(0);
        ParallelizedJuicerTools.launchParallelizedCode(() -> {
            int i = index.getAndIncrement();
            double[] colSums = new double[matrix[0].length];
            int[] colSize = new int[totalColSums.length];
            while (i < matrix.length) {
                for (int j = 0; j < matrix[i].length; j++) {
                    float val = matrix[i][j];
                    if (isValid(val)) {
                        colSums[j] += val;
                        colSize[j] += 1;
                    }
                }
                i = index.getAndIncrement();
            }
            synchronized (totalColSize) {
                for (int k = 0; k < totalColSize.length; k++) {
                    totalColSums[k] += colSums[k];
                    totalColSize[k] += colSize[k];
                }
            }
        });

        float[] colMeans = new float[totalColSums.length];
        for (int k = 0; k < totalColSums.length; k++) {
            colMeans[k] = (float) (totalColSums[k] / Math.max(totalColSize[k], 1));
        }
        return colMeans;
    }

    public static float[] getColStdDev(float[][] matrix, float[] means) {

        double[] totalSquares = new double[means.length];
        int[] totalColSize = new int[means.length];

        AtomicInteger index = new AtomicInteger(0);
        ParallelizedJuicerTools.launchParallelizedCode(() -> {
            int i = index.getAndIncrement();
            double[] squares = new double[matrix[0].length];
            int[] colSize = new int[squares.length];
            while (i < matrix.length) {
                for (int j = 0; j < matrix[i].length; j++) {
                    float val = matrix[i][j];
                    if (isValid(val)) {
                        float diff = val - means[j];
                        squares[j] += diff * diff;
                        colSize[j] += 1;
                    }
                }
                i = index.getAndIncrement();
            }
            synchronized (totalColSize) {
                for (int k = 0; k < totalColSize.length; k++) {
                    totalSquares[k] += squares[k];
                    totalColSize[k] += colSize[k];
                }
            }
        });

        float[] stdDev = new float[means.length];
        for (int k = 0; k < totalSquares.length; k++) {
            stdDev[k] = (float) Math.sqrt(totalSquares[k] / Math.max(totalColSize[k], 1));
        }
        return stdDev;
    }

    private static boolean isValid(float val) {
        return !Float.isNaN(val) && val > ZERO; //
    }
}
