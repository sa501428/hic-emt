package emt.main;

import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.basics.ChromosomeHandler;
import javastraw.reader.block.ContactRecord;
import javastraw.reader.mzd.Matrix;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.NormalizationType;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

public class Excision extends FileBuildingMethod {

    private final Dataset dataset;
    private final ChromosomeHandler chromosomeHandler;
    private final boolean useCustomCDS;
    private final NormalizationType norm;
    private final boolean doSubsample;
    private final double ratio;
    private final boolean onlyIntra;

    public Excision(Dataset dataset, ChromosomeHandler chromosomeHandler, int resolution, String path,
                    boolean doSubsample, double ratio, boolean doCleanUp, long seed, boolean onlyIntra, String stem) {
        super(resolution, path, dataset.getGenomeId(), doCleanUp, seed, stem);
        this.dataset = dataset;
        this.chromosomeHandler = chromosomeHandler;
        this.doSubsample = doSubsample;
        this.ratio = ratio;
        useCustomCDS = !Utils.checkIfStandardGenome(dataset.getGenomeId());
        norm = dataset.getNormalizationHandler().getNormTypeFromString("NONE");
        this.onlyIntra = onlyIntra;
    }

    public void buildTempFiles() throws IOException {
        if (useCustomCDS) {
            writeOutCustomCDS();
        }

        BufferedWriter bwMND = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newMND)));
        Chromosome[] chromosomes = chromosomeHandler.getChromosomeArrayWithoutAllByAll();
        for (int i = 0; i < chromosomes.length; i++) {
            for (int j = i; j < chromosomes.length; j++) {
                if (onlyIntra && i != j) continue;
                processRegion(bwMND, chromosomes[i], chromosomes[j], doSubsample, ratio);
                Utils.printProgressDot();
            }
        }
        bwMND.close();
    }

    private void processRegion(BufferedWriter bwMND, Chromosome c1, Chromosome c2,
                               boolean doSubsample, double ratio) throws IOException {
        Matrix matrix = dataset.getMatrix(c1, c2);
        MatrixZoomData zd = matrix.getZoomData(zoom);

        Iterator<ContactRecord> iterator = zd.getDirectIterator();
        if (doSubsample) {
            Utils.writeOutSubsampledMND(iterator, resolution, 0, 0, bwMND,
                    c1.getName(), c2.getName(), ratio, generator);
        } else {
            Utils.writeOutMND(iterator, resolution, 0, 0, bwMND,
                    c1.getName(), c2.getName());
        }
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
