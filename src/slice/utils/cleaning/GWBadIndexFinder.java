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

import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.block.Block;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;
import slice.clt.Slice;

import java.io.IOException;
import java.util.*;

public class GWBadIndexFinder {

    protected static final float ZSCORE_COVERAGE_MAX_ALLOWED_INTER = 5;
    protected static final float ZSCORE_MIN_NONZERO_COVERAGE_NEEDED_INTER = -3;
    protected final int resolution;
    protected final Map<Integer, Set<Integer>> badIndices = new HashMap<>();
    protected final Map<Integer, Set<Integer>> emptyIndices = new HashMap<>();
    protected final List<NormalizationType[]> norms;
    private final GWRegionStatistics gwStats = new GWRegionStatistics();

    public GWBadIndexFinder(Chromosome[] chromosomes, int resolution, List<NormalizationType[]> norms) {
        this.resolution = resolution;
        this.norms = norms;
        for (Chromosome chrom : chromosomes) {
            badIndices.put(chrom.getIndex(), new HashSet<>());
            emptyIndices.put(chrom.getIndex(), new HashSet<>());
        }
    }

    public static void identifyExtremes(Chromosome chr1, float[] sums, double mean, double stdDev, boolean removeLowCoverage, int k, float zscoreMinNonzeroCoverageNeededInter, Map<Integer, Set<Integer>> badIndices, float zscoreCoverageMaxAllowedInter) {
        double zval = (sums[k] - mean) / stdDev;
        if (removeLowCoverage) {
            if (zval < zscoreMinNonzeroCoverageNeededInter) {
                badIndices.get(chr1.getIndex()).add(k);
            }
        } else if (zval > zscoreCoverageMaxAllowedInter) {
            badIndices.get(chr1.getIndex()).add(k);
        }
    }

    protected void updateCoverageStats(Chromosome chr1, Chromosome chr2, MatrixZoomData zd,
                                       int dIndex) throws IOException {
        int lengthChr1 = (int) (chr1.getLength() / resolution + 1);
        int lengthChr2 = (int) (chr2.getLength() / resolution + 1);

        List<Block> blocks = HiCFileTools.getAllRegionBlocks(zd, 0, lengthChr1, 0, lengthChr2,
                norms.get(dIndex)[Slice.INTRA_SCALE_INDEX], false);
        gwStats.update(chr1.getIndex(), chr2.getIndex(), lengthChr1, lengthChr2, blocks);
    }

    public void createInternalBadList(List<Dataset> datasets, Chromosome[] chromosomes) {
        for (int z = 0; z < datasets.size(); z++) {
            for (int i = 0; i < chromosomes.length; i++) {
                Chromosome chr1 = chromosomes[i];
                for (int j = i + 1; j < chromosomes.length; j++) {
                    Chromosome chr2 = chromosomes[j];
                    final MatrixZoomData zd = HiCFileTools.getMatrixZoomData(datasets.get(z), chr1, chr2, resolution);
                    try {
                        updateCoverageStats(chr1, chr2, zd, z);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        determineBadIndicesFromGenomewideStats(chromosomes);
    }

    protected void determineBadIndicesFromGenomewideStats(Chromosome[] chromosomes) {
        gwStats.postprocess();

        for (Chromosome chromosome : chromosomes) {
            getBadCoverageIndices(chromosome,
                    gwStats.getSums(chromosome), gwStats.getSumMean(), gwStats.getSumStd(), false);
            getBadCoverageIndices(chromosome,
                    gwStats.getNonZeros(chromosome), gwStats.getNonZeroMean(), gwStats.getNonZeroStd(), true);
        }
    }

    protected void getBadCoverageIndices(Chromosome chr1, float[] sums, double mean, double stdDev,
                                         boolean removeLowCoverage) {
        if (sums == null) {
            System.err.println("Skipping " + chr1.getName() + " " + removeLowCoverage);
            return;
        }
        for (int k = 0; k < sums.length; k++) {
            if (sums[k] > 0) {
                identifyExtremes(chr1, sums, mean, stdDev, removeLowCoverage, k, ZSCORE_MIN_NONZERO_COVERAGE_NEEDED_INTER, badIndices, ZSCORE_COVERAGE_MAX_ALLOWED_INTER);
            } else {
                emptyIndices.get(chr1.getIndex()).add(k);
                badIndices.get(chr1.getIndex()).add(k);
            }
        }
    }

    public Set<Integer> getBadIndices(Chromosome chrom) {
        return badIndices.get(chrom.getIndex());
    }

    public Set<Integer> getBadGenomePositionsAtResolution(Chromosome chrom, int newResolution) {
        if (newResolution == resolution) return getBadIndices(chrom);
        Set<Integer> newPositions = new HashSet<>();
        for (Integer pos : getBadIndices(chrom)) {
            long genomePos = (long) resolution * pos;
            int newPos = (int) (genomePos / newResolution);
            newPositions.add(newPos);
        }
        return newPositions;
    }

    public Set<Integer> getEmptyIndices(Chromosome chrom) {
        return emptyIndices.get(chrom.getIndex());
    }
}
