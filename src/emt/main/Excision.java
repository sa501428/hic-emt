package emt.main;

import jargs.gnu.CmdLineParser;
import javastraw.reader.Dataset;
import javastraw.reader.Matrix;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.basics.ChromosomeHandler;
import javastraw.reader.block.Block;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.NormalizationType;
import javastraw.tools.HiCFileTools;
import juicebox.tools.HiCTools;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class Excision {
    private final String newMND, newCDS, newHiCFile;
    private final int highestResolution;
    private final HiCZoom zoom;
    private final Dataset dataset;
    private final ChromosomeHandler chromosomeHandler;
    private final boolean useCustomCDS;
    private final NormalizationType norm;

    public Excision(Dataset dataset, ChromosomeHandler chromosomeHandler, int highestResolution, String folder) {
        this.dataset = dataset;
        this.chromosomeHandler = chromosomeHandler;
        this.highestResolution = highestResolution;
        zoom = new HiCZoom(HiCZoom.HiCUnit.BP, highestResolution);
        useCustomCDS = !Utils.checkIfStandardGenome(dataset.getGenomeId());
        newMND = folder + "/custom.mnd.txt";
        newCDS = dataset.getGenomeId();
        newHiCFile = folder + "/custom.hic";
        norm = dataset.getNormalizationHandler().getNormTypeFromString("NONE");
    }

    public void buildTempFiles() throws IOException {
        if (useCustomCDS) {
            writeOutCustomCDS();
        }

        BufferedWriter bwMND = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newMND)));
        Chromosome[] chromosomes = chromosomeHandler.getChromosomeArrayWithoutAllByAll();
        for (int i = 0; i < chromosomes.length; i++) {
            for (int j = i; j < chromosomes.length; j++) {
                processRegion(bwMND, chromosomes[i], chromosomes[j]);
                Utils.printProgressDot();
            }
        }
        bwMND.close();
    }

    private void processRegion(BufferedWriter bwMND, Chromosome c1, Chromosome c2) throws IOException {

        int end1 = (int) (c1.getLength() / highestResolution) + 1;
        int end2 = (int) (c2.getLength() / highestResolution) + 1;

        Matrix matrix = dataset.getMatrix(c1, c2);
        MatrixZoomData zd = matrix.getZoomData(zoom);

        List<Block> blocks = HiCFileTools.getAllRegionBlocks(zd,
                0, end1, 0, end2,
                norm, false);

        Utils.writeOutMND(blocks, highestResolution, 0, 0, bwMND,
                c1.getName(), c2.getName());
    }

    public void buildNewHiCFile() throws CmdLineParser.UnknownOptionException, CmdLineParser.IllegalOptionValueException {

        String resolutionsToBuild = Utils.getResolutionsToBuild(highestResolution);
        String[] line = {"pre", "-r", resolutionsToBuild, newMND, newHiCFile, newCDS};
        HiCTools.main(line);
    }

    private void writeOutCustomCDS() throws IOException {
        BufferedWriter bwChromDotSizes = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newCDS)));
        for (Chromosome chromosome : chromosomeHandler.getChromosomeArrayWithoutAllByAll()) {
            bwChromDotSizes.write(chromosome.getName() + "\t" + chromosome.getLength());
            bwChromDotSizes.newLine();
        }
        bwChromDotSizes.close();
    }
}
