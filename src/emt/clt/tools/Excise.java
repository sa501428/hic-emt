package emt.clt.tools;

import emt.clt.CommandLineParser;
import emt.main.Excision;
import javastraw.reader.Dataset;
import javastraw.reader.basics.ChromosomeHandler;
import javastraw.tools.HiCFileTools;
import javastraw.tools.UNIXTools;

public class Excise extends CLT {

    private int highestResolution = 1000;
    private String file, folder;

    public Excise() {
        super("excise [-r resolution] [-c chromosomes] <file> <out_folder>");
    }

    @Override
    protected void readAdditionalArguments(String[] args, CommandLineParser parser) {
        if (args.length != 3) {
            printUsageAndExit(6);
        }

        file = args[1];
        folder = args[2];

        Integer res = parser.getResolutionOption();
        if (res != null) {
            highestResolution = res;
        }
    }

    @Override
    public void run() {

        UNIXTools.makeDir(folder);

        Dataset ds = HiCFileTools.extractDatasetForCLT(file, true, false);
        ChromosomeHandler chromosomeHandler = ds.getChromosomeHandler();
        if (givenChromosomes != null)
            chromosomeHandler = HiCFileTools.stringToChromosomes(givenChromosomes, chromosomeHandler);


        Excision excision = new Excision(ds, chromosomeHandler, highestResolution, folder);
        try {
            excision.buildTempFiles();
            excision.buildNewHiCFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
