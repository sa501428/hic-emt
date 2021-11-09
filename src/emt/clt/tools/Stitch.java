package emt.clt.tools;

import emt.clt.CommandLineParser;
import emt.main.Stitcher;
import javastraw.tools.UNIXTools;

public class Stitch extends CLT {

    private int resolution = 500;
    private String norm = "SCALE", folder;
    private boolean adjustOrigin = false;
    private String[] files, stems, regions;

    public Stitch() {
        super("stitch [-r resolution] [-k NONE/VC/VC_SQRT/KR] [--reset-origin]" +
                " <file1,file2,...> <name1,name2,...> <chr1:x1:y1,chr2:x2:y2,...> <out_folder>");
    }

    @Override
    protected void readAdditionalArguments(String[] args, CommandLineParser parser) {
        if (args.length != 5) {
            printUsageAndExit(6);
        }

        files = args[1].split(",");
        stems = args[2].split(",");
        if (files.length != stems.length) {
            System.err.println("Each file should have a matching name");
            printUsageAndExit(8);
        }
        regions = args[3].split(",");
        folder = args[4];

        String preferredNorm = parser.getNormalizationStringOption();
        if (preferredNorm != null) {
            norm = preferredNorm;
        }

        Integer res = parser.getResolutionOption();
        if (res != null) {
            resolution = res;
        }

        adjustOrigin = parser.getResetOrigin();
    }

    @Override
    public void run() {

        UNIXTools.makeDir(folder);
        Stitcher stitcher = new Stitcher(files, stems, regions, norm, adjustOrigin, resolution);
        try {
            stitcher.buildTempFiles(folder);
            stitcher.buildNewHiCFile(folder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
