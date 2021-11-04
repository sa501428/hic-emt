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

import slice.utils.matrices.MatrixAndWeight;

import java.util.HashSet;
import java.util.Set;

public abstract class DimensionCleaner {

    protected final float[][] data;
    protected final int[] weights;

    public DimensionCleaner(float[][] data, int[] weights) {
        this.data = data;
        this.weights = weights;
    }

    public MatrixAndWeight getCleanedData() {
        return filterOutDimension(data);
    }

    private MatrixAndWeight filterOutDimension(float[][] matrix) {
        Set<Integer> badIndices = getSparseIndices(matrix);

        if (badIndices.size() == 0) {
            return new MatrixAndWeight(matrix, weights);
        }

        return filterOutBadIndices(badIndices, matrix, weights);
    }

    protected Set<Integer> getSparseIndices(float[][] matrix) {
        // sparse rows
        Set<Integer> badIndices = new HashSet<>();
        int[] numNans = getNumberOfNansInDimension(matrix);
        int maxBadEntriesAllowed = getLimit();
        for (int i = 0; i < numNans.length; i++) {
            if (numNans[i] > maxBadEntriesAllowed) {
                badIndices.add(i);
            }
        }
        return badIndices;
    }

    protected abstract int getLimit();

    protected abstract int[] getNumberOfNansInDimension(float[][] matrix);

    protected abstract MatrixAndWeight filterOutBadIndices(Set<Integer> badIndices, float[][] matrix, int[] weights);

}
