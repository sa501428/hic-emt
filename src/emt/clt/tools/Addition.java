package emt.clt.tools;

import emt.clt.CommandLineParser;
import javastraw.reader.Dataset;
import javastraw.reader.basics.ChromosomeHandler;
import javastraw.tools.HiCFileTools;
import javastraw.tools.UNIXTools;

public class Addition extends CLT {

    private int highestResolution = 1000;
    private String file, folder;
    private boolean onlyIntra = false;
    private boolean earlyExit = false;

    public Addition() {
        super("combine [-r resolution] [-c chromosomes] [--only-intra] " +
                "[--early-exit] <file> <out_folder>");
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

        onlyIntra = parser.getIntraOption();
        earlyExit = parser.getEarlyExitOption();
    }

    @Override
    public void run() {
        UNIXTools.makeDir(folder);

        Dataset ds = HiCFileTools.extractDatasetForCLT(file, true, false);
        ChromosomeHandler chromosomeHandler = ds.getChromosomeHandler();
        if (givenChromosomes != null)
            chromosomeHandler = HiCFileTools.stringToChromosomes(givenChromosomes, chromosomeHandler);

        /*
        FileBuildingMethod excision = new Addition(ds, chromosomeHandler, highestResolution, folder,
                numberOfReadsToSubsample > 1, ratioToKeep, doCleanUp, seed, onlyIntra);
        FileBuildingMethod.tryToBuild(excision, onlyIntra);
        */
    }
}
