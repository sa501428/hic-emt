package emt.clt.tools;

import emt.clt.CommandLineParser;
import javastraw.reader.Dataset;
import javastraw.tools.HiCFileTools;

public class Graphs extends CLT {

    private String file;

    public Graphs() {
        super("graphs <file>");
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
        Dataset ds = HiCFileTools.extractDatasetForCLT(file, false, false, false);
        System.out.println(ds.getGraphs());
    }
}
