package emt.clt.tools;

import emt.clt.CommandLineParser;
import emt.utils.validation.ValidationTools;
import javastraw.reader.Dataset;
import javastraw.tools.HiCFileTools;

public class Equals extends CLT {

    private String file1, file2;

    public Equals() {
        super("info <file>");
    }

    @Override
    protected void readAdditionalArguments(String[] args, CommandLineParser parser) {
        if (args.length != 2) {
            printUsageAndExit(8);
        }

        file1 = args[1];
        file2 = args[2];
    }

    @Override
    public void run() {
        Dataset ds1 = HiCFileTools.extractDatasetForCLT(file1, true, false);
        Dataset ds2 = HiCFileTools.extractDatasetForCLT(file2, true, false);

        ValidationTools.validateGenomes(ds1, ds2);
        ValidationTools.validateNormalizationTypes(ds1, ds2);
        int highestRes = ValidationTools.validateResolutions(ds1, ds2);
        ValidationTools.validateNormVectors(ds1, ds2);
        ValidationTools.validateExpectedVectors(ds1, ds2);
        // if the norm and expected vectors are identical, no need to check the actual reads?
        // heuristic for now, but I think this should be true
        ValidationTools.validateRawCounts(ds1, ds2, highestRes);
        System.out.println("(-: Validation successful, files are equivalent");
    }
}
