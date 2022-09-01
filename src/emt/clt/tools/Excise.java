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
    private String file, folder, stem;
    private long numberOfReadsToSubsample = 0L;
    private double ratioToKeep = 1.0;
    private boolean doCleanUp = false;
    private long seed;
    private boolean onlyIntra = false;

    public Excise() {
        super("excise [-r resolution] [-c chromosomes] [--subsample num_contacts] " +
                "[--cleanup] [--only-intra] <file> <out_folder> <stem>");
    }

    @Override
    protected void readAdditionalArguments(String[] args, CommandLineParser parser) {
        if (args.length != 4) {
            printUsageAndExit(6);
        }

        file = args[1];
        folder = args[2];
        stem = args[3];

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
        onlyIntra = parser.getIntraOption();
    }

    @Override
    public void run() {

        UNIXTools.makeDir(folder);

        Dataset ds = HiCFileTools.extractDatasetForCLT(file, false, false, false);
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
                numberOfReadsToSubsample > 1, ratioToKeep, doCleanUp, seed, onlyIntra,
                stem);
        FileBuildingMethod.tryToBuild(excision, onlyIntra);
    }
}
