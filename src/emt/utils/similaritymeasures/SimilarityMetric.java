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

public abstract class SimilarityMetric {
    private final boolean mIsSymmmetric;

    public SimilarityMetric(boolean isSymmmetric) {
        mIsSymmmetric = isSymmmetric;
    }

    protected static float arctanh(double x) {
        float val = (float) Math.max(x, -.99f);
        val = Math.min(val, .99f);
        val = (float) (Math.log(1 + val) - Math.log(1 - val)) / 2f;
        if (Float.isInfinite(val)) {
            val = Float.NaN;
        }
        return val;
    }

    public boolean isSymmetric() {
        return this.mIsSymmmetric;
    }

    abstract public float distance(final float[] x, final float[] y);
}