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

package slice.utils.matrices;

import javastraw.feature1D.GenomeWideList;
import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.basics.ChromosomeHandler;
import javastraw.reader.type.NormalizationType;
import robust.concurrent.kmeans.clustering.Cluster;
import slice.MixerGlobals;
import slice.clt.Slice;
import slice.utils.cleaning.GWBadIndexFinder;
import slice.utils.cleaning.SliceMatrixCleaner;
import slice.utils.common.ZScoreTools;
import slice.utils.similaritymeasures.RobustEuclideanDistance;
import slice.utils.structures.SliceUtils;
import slice.utils.structures.SubcompartmentInterval;

import java.io.File;
import java.util.*;

public abstract class CompositeGenomeWideMatrix {
    protected final NormalizationType[] norms;
    protected final int resolution;
    protected final Map<Integer, SubcompartmentInterval> rowIndexToIntervalMap = new HashMap<>();
    protected final Chromosome[] chromosomes;
    protected final Random generator = new Random(0);
    protected final File outputDirectory;
    protected final GWBadIndexFinder badIndexLocations;
    protected final int maxClusterSizeExpected;
    private MatrixAndWeight gwCleanMatrix;

    public CompositeGenomeWideMatrix(ChromosomeHandler chromosomeHandler, Dataset ds,
                                     NormalizationType[] norms,
                                     int resolution,
                                     File outputDirectory, long seed,
                                     GWBadIndexFinder badIndexLocations,
                                     int maxClusterSizeExpected) {
        this.maxClusterSizeExpected = maxClusterSizeExpected;
        this.norms = norms;
        if (MixerGlobals.printVerboseComments) {
            System.out.println("Norms: " + Arrays.toString(norms));
        }
        this.resolution = resolution;
        this.outputDirectory = outputDirectory;
        this.generator.setSeed(seed);
        this.badIndexLocations = badIndexLocations;

        chromosomes = chromosomeHandler.getAutosomalChromosomesArray();
        gwCleanMatrix = makeCleanScaledInterMatrix(ds, norms[Slice.INTER_SCALE_INDEX]);
    }

    abstract MatrixAndWeight makeCleanScaledInterMatrix(Dataset ds, NormalizationType interNorm);

    public void cleanUpMatricesBySparsity() {

        SliceMatrixCleaner matrixCleanupReduction = new SliceMatrixCleaner(gwCleanMatrix.matrix,
                generator.nextLong(), outputDirectory, resolution);
        gwCleanMatrix = matrixCleanupReduction.getCleanFilteredZscoredMatrix(rowIndexToIntervalMap,
                gwCleanMatrix.weights);

        inPlaceScaleSqrtWeightCol();
    }

    public void inPlaceScaleSqrtWeightCol() {
        ZScoreTools.inPlaceScaleSqrtWeightCol(gwCleanMatrix.matrix, gwCleanMatrix.weights);
    }


    public synchronized double processKMeansClusteringResult(Cluster[] clusters,
                                                             GenomeWideList<SubcompartmentInterval> subcompartments) {

        Set<SubcompartmentInterval> subcompartmentIntervals = new HashSet<>();
        if (MixerGlobals.printVerboseComments) {
            System.out.println("GW Composite data vs clustered into " + clusters.length + " clusters");
        }

        double withinClusterSumOfSquares = 0;
        int numGoodClusters = 0;
        int genomewideCompartmentID = 0;


        for (Cluster cluster : clusters) {
            int currentClusterID = ++genomewideCompartmentID;

            if (MixerGlobals.printVerboseComments) {
                System.out.println("Size of cluster " + currentClusterID + " - " + cluster.getMemberIndexes().length);
            }

            if (cluster.getMemberIndexes().length < 5) {
                withinClusterSumOfSquares += Float.MAX_VALUE;
            } else {
                numGoodClusters++;
            }

            for (int i : cluster.getMemberIndexes()) {
                withinClusterSumOfSquares += getDistance(cluster.getCenter(), gwCleanMatrix.matrix[i]);

                if (rowIndexToIntervalMap.containsKey(i)) {
                    SubcompartmentInterval interv = rowIndexToIntervalMap.get(i);
                    if (interv != null) {
                        subcompartmentIntervals.add(generateNewSubcompartment(interv, currentClusterID));
                    }
                }
            }
        }

        withinClusterSumOfSquares = withinClusterSumOfSquares / numGoodClusters;
        if (MixerGlobals.printVerboseComments) {
            System.out.println("Final WCSS " + withinClusterSumOfSquares);
        }

        subcompartments.addAll(new ArrayList<>(subcompartmentIntervals));
        SliceUtils.reSort(subcompartments);

        return withinClusterSumOfSquares;
    }

    protected double getDistance(float[] center, float[] vector) {
        return RobustEuclideanDistance.getNonNanMeanSquaredError(center, vector);
    }


    protected SubcompartmentInterval generateNewSubcompartment(SubcompartmentInterval interv, int currentClusterID) {
        SubcompartmentInterval newInterv = (SubcompartmentInterval) interv.deepClone();
        newInterv.setClusterID(currentClusterID);
        return newInterv;
    }

    public float[][] getData() {
        return gwCleanMatrix.matrix;
    }

}
