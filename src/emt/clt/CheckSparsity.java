package emt.clt;

import emt.clt.tools.CLT;
import emt.utils.validation.SparsityCheck;
import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.type.HiCZoom;
import javastraw.tools.HiCFileTools;

import java.util.List;

public class CheckSparsity extends CLT {

    private String file1;

    public CheckSparsity() {
        super("check-sparsity <file1.hic>");
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
        assessSparsity(ds1);
    }

    private void assessSparsity(Dataset ds1) {
        Chromosome[] array = ds1.getChromosomeHandler().getChromosomeArrayWithoutAllByAll();
        List<HiCZoom> zooms = ds1.getBpZooms();

        for (Chromosome chrom : array) {
            for (HiCZoom zoom : zooms) {
                System.out.println("Check " + chrom.getName() + " " + zoom.getBinSize());
                SparsityCheck.getNonNanCounts(ds1, chrom, zoom);
            }
        }
    }
}
