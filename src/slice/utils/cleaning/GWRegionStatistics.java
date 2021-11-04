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

import javastraw.reader.basics.Chromosome;
import javastraw.reader.block.Block;
import javastraw.reader.block.ContactRecord;
import slice.utils.common.ArrayTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GWRegionStatistics {

    private final Map<Integer, float[]> sums = new HashMap<>();
    private final Map<Integer, float[]> nonZeros = new HashMap<>();
    private final Map<Integer, Integer> lengths = new HashMap<>();

    private float sumMean = 0;
    private float sumStd = 0;
    private float nzMean = 0;
    private float nzStd = 0;

    private static void add(ContactRecord cr, float[] rowSums, float[] colSums, float[] rowNonZeros, float[] colNonZeros) {
        float val = (float) Math.log(cr.getCounts() + 1);
        if (Float.isNaN(val) || val < 1e-10 || Float.isInfinite(val)) {
            return;
        }
        int x = cr.getBinX();
        int y = cr.getBinY();
        rowSums[x] += val;
        colSums[y] += val;
        rowNonZeros[x]++;
        colNonZeros[y]++;
    }

    public void update(int chr1indx, int chr2indx, int numRows, int numCols, List<Block> blocks) {

        float[] rowSums = sums.getOrDefault(chr1indx, new float[numRows]);
        float[] colSums = sums.getOrDefault(chr2indx, new float[numCols]);
        float[] rowNonZeros = nonZeros.getOrDefault(chr1indx, new float[numRows]);
        float[] colNonZeros = nonZeros.getOrDefault(chr2indx, new float[numCols]);

        for (Block b : blocks) {
            if (b != null) {
                for (ContactRecord cr : b.getContactRecords()) {
                    add(cr, rowSums, colSums, rowNonZeros, colNonZeros);
                }
            }
        }

        sums.put(chr1indx, rowSums);
        sums.put(chr2indx, colSums);
        nonZeros.put(chr1indx, rowNonZeros);
        nonZeros.put(chr2indx, colNonZeros);

        int currLength1 = lengths.getOrDefault(chr1indx, 0);
        int currLength2 = lengths.getOrDefault(chr2indx, 0);
        lengths.put(chr1indx, currLength1 + numCols);
        lengths.put(chr2indx, currLength2 + numRows);
    }

    public void postprocess() {
        for (int key : lengths.keySet()) {
            float[] sum = sums.get(key);
            float[] nonZero = nonZeros.get(key);
            int length = lengths.get(key);

            for (int k = 0; k < sum.length; k++) {
                sum[k] /= length;
                nonZero[k] /= length;
            }
        }

        calculateMeansStds();
    }

    private void calculateMeansStds() {
        sumMean = ArrayTools.getNonZeroMean(sums.values());
        nzMean = ArrayTools.getNonZeroMean(nonZeros.values());

        sumStd = ArrayTools.getNonZeroStd(sums.values(), sumMean);
        nzStd = ArrayTools.getNonZeroStd(nonZeros.values(), nzMean);
    }

    public float[] getSums(Chromosome chromosome) {
        return sums.get(chromosome.getIndex());
    }

    public float[] getNonZeros(Chromosome chromosome) {
        return nonZeros.get(chromosome.getIndex());
    }

    public float getSumMean() {
        return sumMean;
    }

    public float getSumStd() {
        return sumStd;
    }

    public float getNonZeroMean() {
        return nzMean;
    }

    public float getNonZeroStd() {
        return nzStd;
    }
}
