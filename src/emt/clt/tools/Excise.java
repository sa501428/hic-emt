package emt.clt.tools;

import emt.clt.CommandLineParser;
import emt.main.DatasetUtils;
import emt.main.Excision;
import emt.main.FileBuildingMethod;
import javastraw.reader.Dataset;
import javastraw.reader.basics.ChromosomeHandler;
import javastraw.tools.HiCFileTools;
import javastraw.tools.UNIXTools;

public class Excise extends CLT {

    private int highestResolution = 1000;
    private String file, folder;
    private long numberOfReadsToSubsample = 0L;
    private double ratioToKeep = 1.0;
    private boolean doCleanUp = false;
    private long seed;

    public Excise() {
        super("excise [-r resolution] [-c chromosomes] [--subsample num_contacts] " +
                "[--cleanup] <file> <out_folder>");
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

        long numReadsSubsample = parser.getSubsamplingOption();
        if(numReadsSubsample > 1){
            numberOfReadsToSubsample = numReadsSubsample;
        }
        doCleanUp = parser.getCleanupOption();
        seed = parser.getSeedOption();
    }

    @Override
    public void run() {

        UNIXTools.makeDir(folder);

        Dataset ds = HiCFileTools.extractDatasetForCLT(file, true, false);
        ChromosomeHandler chromosomeHandler = ds.getChromosomeHandler();
        if (givenChromosomes != null)
            chromosomeHandler = HiCFileTools.stringToChromosomes(givenChromosomes, chromosomeHandler);

        if (numberOfReadsToSubsample > 0) {
            long numTotalContacts = DatasetUtils.getTotalContacts(ds);
            ratioToKeep = ((double) numberOfReadsToSubsample) / ((double) numTotalContacts);
            System.out.println("Aiming to retain ~" + numberOfReadsToSubsample + "/" + numTotalContacts + " \n" +
                    "Ratio: " + ratioToKeep);
        }

        FileBuildingMethod excision = new Excision(ds, chromosomeHandler, highestResolution, folder,
                numberOfReadsToSubsample > 1, ratioToKeep, doCleanUp, seed);
        FileBuildingMethod.tryToBuild(excision, false);
    }
}
