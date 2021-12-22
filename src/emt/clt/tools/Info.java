package emt.clt.tools;

import emt.clt.CommandLineParser;
import javastraw.reader.Dataset;
import javastraw.reader.DatasetReader;
import javastraw.reader.Matrix;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;
import juicebox.HiCGlobals;

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
                    Arrays.asList(file.split("\\+")), true, false);
            Dataset ds = reader.read();
            HiCGlobals.verifySupportedHiCFileVersion(reader.getVersion());
            assert ds.getGenomeId() != null;
            System.out.println("Genome ID: " + ds.getGenomeId());

            assert ds.getChromosomeHandler().size() > 0;
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


            List<HiCZoom> zooms = ds.getBpZooms();
            assert !zooms.isEmpty();
            for (HiCZoom zoom : zooms) {
                System.out.println("File has zoom: " + zoom);
            }

            Chromosome[] array = ds.getChromosomeHandler().getChromosomeArrayWithoutAllByAll();
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
                                for (NormalizationType type : norms) {
                                    try {
                                        reader.readNormalizedBlock(0, zd, type);
                                    } catch (Exception e) {
                                        System.err.println("Unable to read block 0 in " +
                                                getBlockDescription(chr, chr2, zoom, type) +
                                                "\n" + e.getLocalizedMessage() + "\n");
                                    }
                                }
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
            System.exit(1);
        }
    }

    private String getBlockDescription(Chromosome chr, Chromosome chr2, HiCZoom zoom, NormalizationType type) {
        return chr.getName() + "-" + chr2.getName() + "-" + zoom.getBinSize() + "-" + type.toString();
    }
}