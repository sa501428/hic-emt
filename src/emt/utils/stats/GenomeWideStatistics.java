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

package emt.utils.stats;

import emt.utils.common.FloatMatrixTools;
import emt.utils.structures.SubcompartmentInterval;
import javastraw.feature1D.GenomeWideList;
import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GenomeWideStatistics {
    private final Dataset ds;
    private final int resolution;
    private final NormalizationType norm;
    private final GenomeWideList<SubcompartmentInterval> subcompartments;
    private final Set<Integer> clusterIDs = new HashSet<>();
    private final Map<String, Double> contacts = new HashMap<>();
    private final Map<String, Long> areas = new HashMap<>();

    private double totalContact = 0.0;
    private long totalCounts = 0L;
    private double[][] contactsMatrix;
    private long[][] countsMatrix;

    public GenomeWideStatistics(Dataset ds, int resolution, NormalizationType norm, GenomeWideList<SubcompartmentInterval> subcompartments) {
        this.ds = ds;
        this.resolution = resolution;
        this.norm = norm;
        this.subcompartments = subcompartments;
        populateStatistics();
    }

    private void populateStatistics() {

        Chromosome[] chromosomes = ds.getChromosomeHandler().getAutosomalChromosomesArray();
        for (int chr1 = 0; chr1 < chromosomes.length; chr1++) {
            Chromosome chrom1 = chromosomes[chr1];
            List<SubcompartmentInterval> intervals1 = subcompartments.getFeatures("" + chrom1.getIndex());
            int lengthChr1 = (int) Math.ceil((float) chrom1.getLength() / resolution);

            for (int chr2 = chr1 + 1; chr2 < chromosomes.length; chr2++) {
                Chromosome chrom2 = chromosomes[chr2];
                List<SubcompartmentInterval> intervals2 = subcompartments.getFeatures("" + chrom2.getIndex());
                int lengthChr2 = (int) Math.ceil((float) chrom2.getLength() / resolution);

                final MatrixZoomData zd = HiCFileTools.getMatrixZoomData(ds, chrom1, chrom2, resolution);
                if (zd == null) continue;
                try {
                    float[][] data = HiCFileTools.extractLocalBoundedRegionFloatMatrix(zd,
                            0, lengthChr1, 0, lengthChr2,
                            lengthChr1, lengthChr2, norm, false);
                    populateCounts(FloatMatrixTools.cleanUpMatrix(data, true), intervals1, intervals2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        convertResultToMatrix();
    }

    private void populateCounts(float[][] data, List<SubcompartmentInterval> intervals1,
                                List<SubcompartmentInterval> intervals2) {
        for (SubcompartmentInterval interval1 : intervals1) {
            int xStart = interval1.getX1() / resolution;
            int xEnd = interval1.getX2() / resolution;

            for (SubcompartmentInterval interval2 : intervals2) {
                int yStart = interval2.getX1() / resolution;
                int yEnd = interval2.getX2() / resolution;

                String key = makeKey(interval1, interval2);
                double contact = 0;
                long area = 0;
                if (contacts.containsKey(key)) {
                    contact = contacts.get(key);
                    area = areas.get(key);
                }

                for (int r = xStart; r < xEnd; r++) {
                    for (int c = yStart; c < yEnd; c++) {
                        float val = data[r][c];
                        contact += val;
                        totalContact += val;
                    }
                }
                int localArea = (xEnd - xStart) * (yEnd - yStart);
                area += localArea;
                totalCounts += localArea;

                contacts.put(key, contact);
                areas.put(key, area);
            }
        }
    }

    private String makeKey(SubcompartmentInterval x, SubcompartmentInterval y) {
        clusterIDs.add(x.getClusterID());
        clusterIDs.add(y.getClusterID());
        return x.getClusterID() + "_" + y.getClusterID();
    }

    private String makeKey(int x, int y) {
        return x + "_" + y;
    }

    private void convertResultToMatrix() {
        List<Integer> ids = new ArrayList<>(clusterIDs);
        Collections.sort(ids);
        contactsMatrix = new double[ids.size()][ids.size()];
        countsMatrix = new long[ids.size()][ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            for (int j = 0; j < ids.size(); j++) {
                String key = makeKey(ids.get(i), ids.get(j));
                if (contacts.containsKey(key)) {
                    contactsMatrix[i][j] = contacts.get(key);
                    countsMatrix[i][j] = areas.get(key);
                }
            }
        }
    }

    public float[][] getBasicResult() {
        double averageContact = (Math.log(2 * totalContact) / (2 * totalCounts));
        float[][] result = new float[countsMatrix.length][countsMatrix.length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                result[i][j] = (float) Math.log(contactsMatrix[i][j] + contactsMatrix[j][i]);
            }
        }

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                result[i][j] /= (averageContact * (countsMatrix[i][j] + countsMatrix[j][i]));
            }
        }

        return result;
    }

    public void saveInteractionMap(File outfolder) {

        boolean useLog = false;
        for (boolean useBalancing : new boolean[]{false}) { // . true
            for (boolean useSymm : new boolean[]{true, false}) {
                double averageContact = totalContact / totalCounts;

                float[][] result = new float[countsMatrix.length][countsMatrix.length];
                for (int i = 0; i < result.length; i++) {
                    for (int j = 0; j < result[i].length; j++) {
                        double contacts = contactsMatrix[i][j];
                        if (useSymm) {
                            contacts += contactsMatrix[j][i];
                        }
                        result[i][j] = (float) contacts;
                    }
                }

                for (int i = 0; i < result.length; i++) {
                    for (int j = 0; j < result[i].length; j++) {
                        long counts = countsMatrix[i][j];
                        if (useSymm) {
                            counts += countsMatrix[j][i];
                        }
                        result[i][j] = result[i][j] / counts;
                    }
                }

                for (int i = 0; i < result.length; i++) {
                    for (int j = 0; j < result[i].length; j++) {
                        result[i][j] = (float) (result[i][j] / averageContact);
                    }
                }


                String name = "cluster_interactions";
                if (useSymm) {
                    name = "symm_" + name;
                }

                File temp = new File(outfolder, name + ".npy");
                File png = new File(outfolder, name + ".png");
                FloatMatrixTools.saveMatrixTextNumpy(temp.getAbsolutePath(), result);
                FloatMatrixTools.saveOEMatrixToPNG(png, result);
            }
        }
    }
}
