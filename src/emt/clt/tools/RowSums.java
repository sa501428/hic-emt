package emt.clt.tools;

import emt.clt.CommandLineParser;
import emt.utils.validation.MatrixSum;
import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.norm.NormalizationVector;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;

import java.util.List;

public class RowSums extends CLT {

    private String file1;

    public RowSums() {
        super("row-sums <file.hic>");
    }

    public static void calculateSums(Dataset ds1) {
        Chromosome[] array = ds1.getChromosomeHandler().getChromosomeArrayWithoutAllByAll();
        List<NormalizationType> norms = ds1.getNormalizationTypes();
        List<HiCZoom> zooms = ds1.getBpZooms();

        for (Chromosome chrom : array) {
            for (HiCZoom zoom : zooms) {
                for (NormalizationType norm : norms) {
                    if (norm.getLabel().toLowerCase().contains("vc")) continue;
                    NormalizationVector nv1 = ds1.getNormalizationVector(chrom.getIndex(), zoom, norm);
                    if (nv1 == null) {
                        continue;
                    }

                    MatrixSum.getRowSums(ds1, chrom, norm, zoom, nv1.getData(),
                            chrom.getName() + "_" + norm.getLabel() + "_" + zoom.getBinSize() + "_f1", true);
                }
            }
        }
    }

    @Override
    protected void readAdditionalArguments(String[] args, CommandLineParser parser) {
        if (args.length != 2) {
            printUsageAndExit(9);
        }
        file1 = args[1];
    }

    @Override
    public void run() {
        Dataset ds1 = HiCFileTools.extractDatasetForCLT(file1, false, false, false);
        calculateSums(ds1);
    }
}
