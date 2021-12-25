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

package emt.utils.structures;


import javastraw.feature1D.Feature1D;

public class SimpleInterval extends Feature1D implements Comparable<SimpleInterval> {
    private final Integer x1; // genomic position, not bin position
    private final Integer x2;
    private final String chrName;
    private final Integer chrIndex;


    public SimpleInterval(int chrIndex, String chrName, int x1, int x2) {
        this.chrIndex = chrIndex;
        this.chrName = chrName;
        this.x1 = x1;
        this.x2 = x2;
    }

    @Override
    public String getKey() {
        return "" + chrIndex;
    }

    @Override
    public Feature1D deepClone() {
        return new SimpleInterval(chrIndex, chrName, x1, x2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SimpleInterval) {
            SimpleInterval o = (SimpleInterval) obj;
            return chrIndex.equals(o.chrIndex) && x1.equals(o.x1) && x2.equals(o.x2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return chrIndex ^ 3 * 37 + x1 ^ 3 + x2 * x1;
    }

    public Integer getChrIndex() {
        return chrIndex;
    }

    public Integer getX1() {
        return x1;
    }

    public Integer getX2() {
        return x2;
    }

    public Integer getWidthForResolution(int resolution) {
        return (x2 - x1) / resolution;
    }

    public String getChrName() {
        return chrName;
    }

    public SimpleInterval getSimpleIntervalKey() {
        return (SimpleInterval) deepClone();
    }

    @Override
    public int compareTo(SimpleInterval o) {
        int comparison = getChrIndex().compareTo(o.getChrIndex());
        if (comparison == 0) comparison = getX1().compareTo(o.getX1());
        if (comparison == 0) comparison = getX2().compareTo(o.getX2());
        return comparison;
    }

    @Override
    public String toString() {
        return "chr" + getChrName() + "\t" + getX1() + "\t" + getX2() + "\tnull\tnull\t.\t"
                + getX1() + "\t" + getX2() + "\t0,0,0";
    }
}
