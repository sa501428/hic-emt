package emt.main;

import jargs.gnu.CmdLineParser;
import javastraw.reader.type.HiCZoom;
import juicebox.tools.HiCTools;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public abstract class FileBuildingMethod {

    protected final String newMND;
    protected final String newCDS;
    protected final String newHiCFile;
    protected final int resolution;
    protected final HiCZoom zoom;
    protected final boolean doCleanUp;
    protected Random generator = new Random(0);

    protected FileBuildingMethod(int resolution, String path, String cds, boolean doCleanUp, long seed) {
        this.resolution = resolution;
        this.zoom = new HiCZoom(HiCZoom.HiCUnit.BP, resolution);
        this.newMND = path + "/custom.mnd.txt";
        this.newHiCFile = path + "/custom.hic";
        this.newCDS = cds;
        this.doCleanUp = doCleanUp;
        this.generator.setSeed(seed);
    }

    abstract public void buildTempFiles() throws IOException;

    public void deleteTempFilesIfNeedBe() {
        if (doCleanUp) {
            for (String path : new String[]{newMND, newCDS}) {
                File file = new File(path);
                if (!file.delete()) {
                    System.err.println("Unable to delete " + path);
                }
            }
        }
    }

    public void buildNewHiCFile(boolean onlyDiagNoNorms) throws CmdLineParser.UnknownOptionException, CmdLineParser.IllegalOptionValueException {
        String resolutionsToBuild = Utils.getResolutionsToBuild(resolution);
        String[] line = new String[]{"pre", "-r", resolutionsToBuild, newMND, newHiCFile, newCDS};
        if (onlyDiagNoNorms) {
            line = new String[]{"pre", "-d", "-n", "-r", resolutionsToBuild,
                    newMND, newHiCFile, newCDS};
        }
        HiCTools.main(line);
    }

    public static void tryToBuild(FileBuildingMethod method, boolean onlyDiagNoNorms) {
        try {
            method.buildTempFiles();
            method.buildNewHiCFile(onlyDiagNoNorms);
            method.deleteTempFilesIfNeedBe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
