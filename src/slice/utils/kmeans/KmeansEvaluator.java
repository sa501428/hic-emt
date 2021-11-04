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

import javastraw.tools.MatrixTools;

import java.io.File;
import java.util.Arrays;

public class KmeansEvaluator {

    private final double[][] iterToWcssAicBic;

    public KmeansEvaluator(int numClusterSizeKValsUsed) {
        iterToWcssAicBic = new double[4][numClusterSizeKValsUsed];
        for (double[] row : iterToWcssAicBic) {
            Arrays.fill(row, Double.MAX_VALUE);
        }
    }

    public double getWCSS(int index) {
        return iterToWcssAicBic[1][index];
    }

    public void setMseAicBicValues(int z, int numClusters, double sumOfSquares, int numRows, int numColumns) {
        iterToWcssAicBic[0][z] = numClusters;
        iterToWcssAicBic[1][z] = sumOfSquares;
        // AIC
        iterToWcssAicBic[2][z] = sumOfSquares + 2 * numColumns * numClusters;
        // BIC .5*k*d*log(n)
        iterToWcssAicBic[3][z] = sumOfSquares + 0.5 * numColumns * numClusters * Math.log(numRows);
    }

    public void export(File outputDirectory, String kstem) {
        String outIterPath = new File(outputDirectory, kstem + "_cluster_size_WCSS_AIC_BIC.npy").getAbsolutePath();
        MatrixTools.saveMatrixTextNumpy(outIterPath, iterToWcssAicBic);
    }
}
