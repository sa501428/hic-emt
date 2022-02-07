package emt.utils.validation;

import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.datastructures.ListOfDoubleArrays;
import javastraw.reader.norm.NormalizationVector;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.NormalizationType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ValidationTools {


    public static void validateGenomes(Dataset ds1, Dataset ds2) {
        assert ds1.getGenomeId() != null;
        System.out.println("Genome1 ID: " + ds1.getGenomeId());
        assert ds2.getGenomeId() != null;
        System.out.println("Genome2 ID: " + ds2.getGenomeId());

        assert ds1.getChromosomeHandler().size() == ds2.getChromosomeHandler().size();

        Chromosome[] chromsArray1 = ds1.getChromosomeHandler().getChromosomeArray();
        Chromosome[] chromsArray2 = ds2.getChromosomeHandler().getChromosomeArray();
        Arrays.sort(chromsArray1, Comparator.comparing(Chromosome::getIndex));
        Arrays.sort(chromsArray2, Comparator.comparing(Chromosome::getIndex));

        for (int c = 0; c < chromsArray1.length; c++) {
            assert chromsArray1[c].getIndex() == chromsArray2[c].getIndex();
            assert chromsArray1[c].getName().equals(chromsArray2[c].getName());
            assert chromsArray1[c].getLength() == chromsArray2[c].getLength();
        }
        System.out.println("Genomes/Chromosomes are equivalent");
    }

    public static void validateNormalizationTypes(Dataset ds1, Dataset ds2) {
        List<NormalizationType> norms1 = ds1.getNormalizationTypes();
        List<NormalizationType> norms2 = ds2.getNormalizationTypes();

        assert norms1.size() == norms2.size();
        norms1.sort(Comparator.comparing(NormalizationType::getLabel));
        norms2.sort(Comparator.comparing(NormalizationType::getLabel));

        for (int q = 0; q < norms1.size(); q++) {
            assert norms1.get(q).getLabel().equals(norms2.get(q).getLabel());
        }
    }

    public static int validateResolutions(Dataset ds1, Dataset ds2) {
        List<HiCZoom> zooms1 = ds1.getBpZooms();
        List<HiCZoom> zooms2 = ds2.getBpZooms();

        assert zooms1.size() == zooms2.size();
        zooms1.sort(Comparator.comparing(HiCZoom::getBinSize));
        zooms2.sort(Comparator.comparing(HiCZoom::getBinSize));

        for (int q = 0; q < zooms1.size(); q++) {
            assert zooms1.get(q).getBinSize() == zooms2.get(q).getBinSize();
        }

        // no need to do this given already sorted list...
        return min(zooms1);
    }

    private static int min(List<HiCZoom> zooms) {
        int minVal = zooms.get(0).getBinSize();
        HiCZoom zoom = zooms.get(zooms.size() - 1);
        if (zoom.getBinSize() < minVal) {
            minVal = zoom.getBinSize();
        }
        return minVal;
    }

    public static void validateNormVectors(Dataset ds1, Dataset ds2) {
        Chromosome[] array = ds1.getChromosomeHandler().getChromosomeArrayWithoutAllByAll();
        List<NormalizationType> norms = ds1.getNormalizationTypes();
        List<HiCZoom> zooms = ds1.getBpZooms();

        for (Chromosome chrom : array) {
            for (NormalizationType norm : norms) {
                for (HiCZoom zoom : zooms) {
                    NormalizationVector nv1 = ds1.getNormalizationVector(chrom.getIndex(), zoom, norm);
                    NormalizationVector nv2 = ds2.getNormalizationVector(chrom.getIndex(), zoom, norm);
                    if (nv1 == nv2) continue;
                    if (nv1 == null || nv2 == null) {
                        System.err.println("NULL ERROR IN NORM " + norm.getLabel() + " at " + zoom.getBinSize() + "!!! nv1 " + nv1 + "  nv2 " + nv2);
                        continue;
                    }
                    if (nv1.getData() == nv2.getData()) continue;
                    VectorTools.assertAreEqual(nv1.getData(), nv2.getData());
                }
            }
        }
    }

    public static void validateExpectedVectors(Dataset ds1, Dataset ds2) {
        Chromosome[] array = ds1.getChromosomeHandler().getChromosomeArrayWithoutAllByAll();
        List<NormalizationType> norms = ds1.getNormalizationTypes();
        List<HiCZoom> zooms = ds1.getBpZooms();

        for (Chromosome chrom : array) {
            for (NormalizationType norm : norms) {
                for (HiCZoom zoom : zooms) {
                    ListOfDoubleArrays d1 = ds1.getExpectedValues(zoom, norm,
                            false).getExpectedValuesWithNormalization(chrom.getIndex());
                    ListOfDoubleArrays d2 = ds2.getExpectedValues(zoom, norm,
                            false).getExpectedValuesWithNormalization(chrom.getIndex());

                    if (d1 == d2) continue;
                    VectorTools.assertAreEqual(d1, d2);
                }
            }
        }
    }

    public static void validateRawCounts(Dataset ds1, Dataset ds2, int highestRes) {
        /*
        NormalizationType none = ds1.getNormalizationHandler().getNormTypeFromString("NONE");

        for (Chromosome chr : array) {
                for (Chromosome chr2 : array) {
                    System.out.print(".");
                    Matrix matrix = ds.getMatrix(chr, chr2);
                    if (matrix == null) {
                        System.err.println("Warning: no reads in " + chr.getName() + "-" + chr2.getName());
                    } else {
                        for (HiCZoom zoom : zooms) {
                            MatrixZoomData zd = matrix.getZoomData(zoom);
                            if (zd == null) {
                                System.err.println("Warning: no reads in " +
                                        chr.getName() + "-" + chr2.getName() + " at resolution " + zoom.getBinSize());
                            } else {
                                try {
                                    reader.readNormalizedBlock(0, zd, none);
                                } catch (Exception e) {
                                    System.err.println("Unable to read block 0 in " +
                                            getBlockDescription(chr, chr2, zoom, none) +
                                            "\n" + e.getLocalizedMessage() + "\n");
                                }
                            }
                        }
                    }
                }
                System.out.println();
            }
         */
    }

    private String getBlockDescription(Chromosome chr, Chromosome chr2, HiCZoom zoom, NormalizationType type) {
        return chr.getName() + "-" + chr2.getName() + "-" + zoom.getBinSize() + "-" + type.toString();
    }
}
