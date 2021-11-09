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

package emt.utils.similaritymeasures;

/**
 * Euclidean distance.
 */
public final class RobustEuclideanDistance extends SimilarityMetric {

  /**
   * Euclidean metric.
   */
  public static final RobustEuclideanDistance SINGLETON = new RobustEuclideanDistance();

  private RobustEuclideanDistance() {
    super(true);
  }

  public static double getNonNanMeanSquaredError(float[] x, float[] y) {
    double sumOfSquares = 0;
    int numVals = 0;
    for (int i = 0; i < x.length; i++) {
      final float v = x[i] - y[i];
      if (!Float.isNaN(v)) {
        sumOfSquares += (v * v);
        numVals++;
      }
    }
    if (numVals < 1) {
      //System.err.println("Vector too sparse");
      return Float.MAX_VALUE;
    }
    return x.length * sumOfSquares / numVals;
  }

  @Override
  public float distance(final float[] x, final float[] y) {
    return (float) Math.sqrt(getNonNanMeanSquaredError(x, y));
  }
}
