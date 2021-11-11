package emt.main;

import jargs.gnu.CmdLineParser;
import javastraw.reader.type.HiCZoom;
import juicebox.tools.HiCTools;

import java.io.File;
import java.io.IOException;

public abstract class FileBuildingMethod {

    protected final String newMND;
    protected final String newCDS;
    protected final String newHiCFile;
    protected final int resolution;
    protected final HiCZoom zoom;
    protected final boolean doCleanUp;

    protected FileBuildingMethod(int resolution, String path, String cds, boolean doCleanUp) {
        this.resolution = resolution;
        zoom = new HiCZoom(HiCZoom.HiCUnit.BP, resolution);
        newMND = path + "/custom.mnd.txt";
        newHiCFile = path + "/custom.hic";
        newCDS = cds;
        this.doCleanUp = doCleanUp;
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
}
