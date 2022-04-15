package emt.clt.tools;

import emt.clt.CommandLineParser;
import javastraw.feature2D.Feature2D;
import javastraw.feature2D.Feature2DList;
import javastraw.feature2D.Feature2DParser;
import javastraw.reader.basics.ChromosomeHandler;
import javastraw.reader.basics.ChromosomeTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BedpeSelect extends CLT {

    private ChromosomeHandler handler;
    private Feature2DList features, boundingBoxes;
    private String output;

    public BedpeSelect() {
        super("bedpe-select <genomeID> <features.bedpe> <bounding_boxes.bedpe> <output.bedpe>");
    }

    @Override
    protected void readAdditionalArguments(String[] args, CommandLineParser parser) {
        if (args.length != 5) {
            printUsageAndExit(11);
        }

        handler = ChromosomeTools.loadChromosomes(args[1]);
        features = Feature2DParser.loadFeatures(args[2], handler, false, null, false);
        boundingBoxes = Feature2DParser.loadFeatures(args[3], handler, false, null, false);

        output = args[4];
    }

    @Override
    public void run() {

        Feature2DList filtered = grabSubsetOfFeatures(features, boundingBoxes);
        filtered.exportFeatureList(new File(output), false, Feature2DList.ListFormat.NA);

    }

    private Feature2DList grabSubsetOfFeatures(Feature2DList features, Feature2DList boundingBoxes) {
        features.filterLists((key, list) -> {
            List<Feature2D> bounds = boundingBoxes.get(key);
            List<Feature2D> featuresToKeep = new ArrayList<>();
            for (Feature2D feature : list) {
                for (Feature2D bound : bounds) {
                    if (containedBy(feature, bound)) {
                        featuresToKeep.add(feature);
                        break;
                    }
                }
            }
            return featuresToKeep;
        });
        return features;
    }

    private boolean containedBy(Feature2D feature, Feature2D bound) {
        long fr = feature.getMidPt1();
        long fc = feature.getMidPt2();

        long br1 = bound.getStart1();
        long br2 = bound.getEnd1();
        long bc1 = bound.getStart2();
        long bc2 = bound.getEnd2();

        return withinBounds(fr, fc, br1, br2, bc1, bc2) ||
                withinBounds(fc, fr, br1, br2, bc1, bc2);
    }

    private boolean withinBounds(long fr, long fc, long br1, long br2, long bc1, long bc2) {
        return br1 <= fr && fr <= br2 && bc1 <= fc && fc <= bc2;
    }
}
