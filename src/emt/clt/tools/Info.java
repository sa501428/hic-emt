package emt.clt.tools;

import emt.clt.CommandLineParser;
import javastraw.reader.Dataset;
import javastraw.reader.DatasetReader;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.mzd.Matrix;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.norm.NormalizationVector;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Info extends CLT {

    private String file;

    public Info() {
        super("info <file>");
    }

    @Override
    protected void readAdditionalArguments(String[] args, CommandLineParser parser) {
        if (args.length != 2) {
            printUsageAndExit(8);
        }

        file = args[1];
    }

    @Override
    public void run() {
        try {
            DatasetReader reader = HiCFileTools.extractDatasetReaderForCLT(
                    Arrays.asList(file.split("\\+")), false, false, false);
            Dataset ds = reader.read();
            if (ds.getGenomeId() == null) {
                System.err.println("Null genome ID");
                System.exit(12);
            }
            System.out.println("Genome ID: " + ds.getGenomeId());

            if (ds.getChromosomeHandler().size() < 1) {
                System.err.println("Invalid chromosomes");
                System.exit(13);
            }

            for (Chromosome chrom : ds.getChromosomeHandler().getChromosomeArray()) {
                System.out.println("Chromosome: index = " + chrom.getIndex() +
                        " name = " + chrom.getName() + " length = " + chrom.getLength());
            }


            List<NormalizationType> norms = ds.getNormalizationTypes();
            if (norms.size() > 0) {
                for (NormalizationType type : norms) {
                    System.out.println("File has normalization: " + type.getLabel());
                    System.out.println("Description: " + type.getDescription());
                }
            } else {
                System.err.println("No normalization vectors in file");
            }

            NormalizationType none = ds.getNormalizationHandler().getNormTypeFromString("NONE");

            List<HiCZoom> zooms = ds.getBpZooms();
            if (zooms.isEmpty()) {
                System.err.println("No valid zooms");
                System.exit(14);
            }

            for (HiCZoom zoom : zooms) {
                System.out.println("File has zoom: " + zoom);
            }

            Chromosome[] array = ds.getChromosomeHandler().getChromosomeArrayWithoutAllByAll();
            for (Chromosome chrom : array) {
                for (NormalizationType norm : norms) {
                    for (HiCZoom zoom : zooms) {
                        NormalizationVector nv = ds.getNormalizationVector(chrom.getIndex(), zoom, norm);
                        try {
                            if (nv == null || nv.getData() == null) {
                                System.err.println("Warning: no norm vector for: " + chrom.getName()
                                        + " - " + norm.getLabel()
                                        + " - " + zoom.getBinSize()
                                );
                            }
                        } catch (Exception e) {
                            System.err.println("Error: no norm vector for: " + chrom.getName()
                                    + " - " + norm.getLabel()
                                    + " - " + zoom.getBinSize()
                            );
                        }
                    }
                }
            }

            for (Chromosome chrom1 : array) {
                for (Chromosome chrom2 : array) {
                    System.out.print(".");
                    Matrix matrix = ds.getMatrix(chrom1, chrom2);
                    if (matrix == null) {
                        System.err.println("Warning: no reads in " + chrom1.getName() + "-" + chrom2.getName());
                    } else {
                        for (HiCZoom zoom : zooms) {
                            MatrixZoomData zd = matrix.getZoomData(zoom);
                            if (zd == null) {
                                System.err.println("Warning: no reads in " +
                                        chrom1.getName() + "-" + chrom2.getName() + " at resolution " + zoom.getBinSize());
                            } else {
                                /* todo
                                try {
                                    reader.readNormalizedBlock(0, zd.getKey(), none, chrom1.getIndex(),
                                            chrom2.getIndex(), zoom);
                                } catch (Exception e) {
                                    System.err.println("Unable to read block 0 in " +
                                            getBlockDescription(chrom1, chrom2, zoom, none) +
                                            "\n" + e.getLocalizedMessage() + "\n");
                                }
                                */
                            }
                        }
                    }
                }
                System.out.println();
            }
            System.out.println("(-: Validation successful");
        } catch (IOException error) {
            System.err.println(":( Validation failed");
            error.printStackTrace();
            System.exit(15);
        }
    }

    private String getBlockDescription(Chromosome chr, Chromosome chr2, HiCZoom zoom, NormalizationType type) {
        return chr.getName() + "-" + chr2.getName() + "-" + zoom.getBinSize() + "-" + type.toString();
    }
}
