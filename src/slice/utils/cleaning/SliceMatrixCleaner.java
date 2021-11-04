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

import slice.MixerGlobals;
import slice.clt.ParallelizedMixerTools;
import slice.clt.Slice;
import slice.utils.common.LogTools;
import slice.utils.common.ParallelizedStatTools;
import slice.utils.common.ZScoreTools;
import slice.utils.matrices.MatrixAndWeight;
import slice.utils.structures.SubcompartmentInterval;

import java.io.File;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class SliceMatrixCleaner {
    public static int NUM_PER_CENTROID = 100;
    protected final File outputDirectory;
    protected final Random generator = new Random(0);
    protected final int resolution;
    protected float[][] data;

    public SliceMatrixCleaner(float[][] data, long seed, File outputDirectory, int resolution) {
        this.outputDirectory = outputDirectory;
        generator.setSeed(seed);
        this.resolution = resolution;
        this.data = data;
    }

    public MatrixAndWeight getCleanFilteredZscoredMatrix(Map<Integer, SubcompartmentInterval> rowIndexToIntervalMap,
                                                         int[] weights) {
        if (Slice.FILTER_OUTLIERS) {
            ParallelizedStatTools.setZerosToNan(data);
            ParallelizedStatTools.scaleDown(data, weights);
            LogTools.simpleLogWithCleanup(data, Float.NaN);
            removeHighGlobalThresh(data, weights);
            renormalize(data, weights);
            LogTools.simpleExpm1(data);
        }

        if (MixerGlobals.printVerboseComments) {
            System.out.println("Initial matrix size " + data.length + " x " + data[0].length);
        }

        if (MixerGlobals.printVerboseComments) {
            System.out.println("Matrix size before row cleanup " + data.length + " x " + data[0].length);
        }
        data = (new RowCleaner(data, rowIndexToIntervalMap, weights)).getCleanedData().matrix;
        if (MixerGlobals.printVerboseComments) {
            System.out.println("Matrix size after row cleanup " + data.length + " x " + data[0].length);
        }

        ZScoreTools.inPlaceZscoreDownCol(data);

        return new MatrixAndWeight(data, weights);
    }

    private void renormalize(float[][] data, int[] weights) {
        double[] muAndStd = ParallelizedStatTools.getMeanAndStandardDev(data, weights, Slice.USE_WEIGHTED_MEAN);
        if (MixerGlobals.printVerboseComments) {
            System.out.println("mu " + muAndStd[0] + " std" + muAndStd[1]);
        }
        fixToNormalRange(data, muAndStd[0], muAndStd[1]);
    }

    private void fixToNormalRange(float[][] data, double mu, double std) {
        AtomicInteger totalNumFixed = new AtomicInteger();
        AtomicInteger index = new AtomicInteger(0);
        ParallelizedMixerTools.launchParallelizedCode(() -> {
            int i = index.getAndIncrement();
            int numFixed = 0;
            while (i < data.length) {
                for (int j = 0; j < data[i].length; j++) {
                    if (!Float.isNaN(data[i][j])) {
                        double zscore = (data[i][j] - mu) / std;
                        if (zscore < -2 || zscore > 2) { //
                            data[i][j] = Float.NaN;
                            numFixed++;
                        }
                    }
                }
                i = index.getAndIncrement();
            }
            totalNumFixed.addAndGet(numFixed);
        });

        if (MixerGlobals.printVerboseComments) {
            System.out.println("Num fixed part 2: z < -2 : " + totalNumFixed.get());
        }
    }

    private void removeHighGlobalThresh(float[][] data, int[] weights) {
        double[] muAndStd = ParallelizedStatTools.getMeanAndStandardDev(data, weights, Slice.USE_WEIGHTED_MEAN);
        if (MixerGlobals.printVerboseComments) {
            System.out.println("mu " + muAndStd[0] + " std" + muAndStd[1]);
        }
        thresholdByMax(data, muAndStd[0], muAndStd[1]);
    }

    private void thresholdByMax(float[][] data, double mu, double std) {
        AtomicInteger totalNumFixed = new AtomicInteger();
        AtomicInteger index = new AtomicInteger(0);
        ParallelizedMixerTools.launchParallelizedCode(() -> {
            int i = index.getAndIncrement();
            int numFixed = 0;
            while (i < data.length) {
                for (int j = 0; j < data[i].length; j++) {
                    if (!Float.isNaN(data[i][j]) && data[i][j] > 0) {
                        double zscore = (data[i][j] - mu) / std;
                        if (zscore > 5) {
                            data[i][j] = Float.NaN;
                            numFixed++;
                        }
                    }
                }
                i = index.getAndIncrement();
            }
            totalNumFixed.addAndGet(numFixed);
        });

        if (MixerGlobals.printVerboseComments) {
            System.out.println("Num fixed z > " + 5 + " : " + totalNumFixed.get());
        }
    }
}
