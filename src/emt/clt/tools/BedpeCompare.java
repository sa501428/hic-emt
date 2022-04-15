package emt.clt.tools;

import emt.clt.CommandLineParser;
import javastraw.feature2D.Feature2DList;
import javastraw.feature2D.Feature2DParser;
import javastraw.reader.basics.ChromosomeHandler;
import javastraw.reader.basics.ChromosomeTools;
import javastraw.tools.HiCFileTools;

import java.awt.*;

public class BedpeCompare extends CLT {
    public final static String PARENT_ATTRIBUTE = "parent_list";
    public static final Color AAA = new Color(102, 0, 153);
    public static final Color BBB = new Color(255, 102, 0);
    /**
     * Arbitrary colors for comparison list
     **/
    private static final Color AB = new Color(34, 139, 34);
    private static final Color AA = new Color(0, 255, 150);
    private static final Color BB = new Color(150, 255, 0);
    private final int threshold = 10000;
    private int compareTypeID = 0;
    private String genomeID, inputFileA, inputFileB, outputPath = "comparison_list.bedpe";

    public BedpeCompare() {
        super("bedpe-compare <genomeID> <list1> <list2> [output_path]");
    }


    @Override
    protected void readAdditionalArguments(String[] args, CommandLineParser parser) {
        if (args.length != 4 && args.length != 5) {
            printUsageAndExit(11);
        }

        compareTypeID = Integer.parseInt(args[1]);
        genomeID = args[2];
        inputFileA = args[3];
        inputFileB = args[4];
        if (args.length == 6) {
            outputPath = args[5];
        } else {
            if (inputFileB.endsWith(".txt")) {
                outputPath = inputFileB.substring(0, inputFileB.length() - 4) + "_comparison_results.bedpe";
            } else if (inputFileB.endsWith(".bedpe")) {
                outputPath = inputFileB.substring(0, inputFileB.length() - 6) + "_comparison_results.bedpe";
            } else {
                outputPath = inputFileB + "_comparison_results.bedpe";
            }
        }

        /*
        int specifiedMatrixSize = parser.getMatrixSizeOption();
        if (specifiedMatrixSize >= 0) {
            threshold = specifiedMatrixSize;
        }
        */
    }

    @Override
    public void run() {

        ChromosomeHandler handler = ChromosomeTools.loadChromosomes(genomeID);
        if (givenChromosomes != null)
            handler = HiCFileTools.stringToChromosomes(givenChromosomes, handler);

        Feature2DList listA = Feature2DParser.loadFeatures(inputFileA, handler, false, null, false);
        Feature2DList listB = Feature2DParser.loadFeatures(inputFileB, handler, false, null, false);
        compareTwoLists(listA, listB, compareTypeID);
    }

    private void compareTwoLists(Feature2DList listA, Feature2DList listB, int compareTypeID) {
        int sizeA = listA.getNumTotalFeatures();
        int sizeB = listB.getNumTotalFeatures();
        System.out.println("List Size: " + sizeA + "(A) " + sizeB + "(B)");

        /*

        Feature2D.tolerance = 0;

        Feature2DList exactMatches = Feature2DList.getIntersection(listA, listB);
        int numExactMatches = exactMatches.getNumTotalFeatures();
        System.out.println("Number of exact matches: " + numExactMatches);

        Feature2D.tolerance = this.threshold;
        //Feature2DList matchesWithinToleranceFromA = Feature2DList.getIntersection(listA, listB);
        //Feature2DList matchesWithinToleranceFromB = Feature2DList.getIntersection(listB, listA);

        if (compareTypeID == 0 || compareTypeID == 1) {
            Feature2D.tolerance = threshold;
        }

        Feature2DList matchesWithinToleranceUniqueToA = Feature2DTools.subtract(listA, exactMatches);
        matchesWithinToleranceUniqueToA = Feature2DList.getIntersection(matchesWithinToleranceUniqueToA, listB);

        Feature2DList matchesWithinToleranceUniqueToB = Feature2DTools.subtract(listB, exactMatches);
        matchesWithinToleranceUniqueToB = Feature2DList.getIntersection(matchesWithinToleranceUniqueToB, listA);

        int numMatchesWithinTolA = matchesWithinToleranceUniqueToA.getNumTotalFeatures();
        int numMatchesWithinTolB = matchesWithinToleranceUniqueToB.getNumTotalFeatures();

        System.out.println("Number of matches within tolerance: " + numMatchesWithinTolA + "(A) " + numMatchesWithinTolB + "(B)");

        Feature2DList uniqueToA = Feature2DTools.subtract(listA, exactMatches);
        uniqueToA = Feature2DTools.subtract(uniqueToA, matchesWithinToleranceUniqueToA);
        uniqueToA = Feature2DTools.subtract(uniqueToA, listB);

        Feature2DList uniqueToB = Feature2DTools.subtract(listB, exactMatches);
        uniqueToB = Feature2DTools.subtract(uniqueToB, matchesWithinToleranceUniqueToB);
        uniqueToB = Feature2DTools.subtract(uniqueToB, listA);

        int numUniqueToA = uniqueToA.getNumTotalFeatures();
        int numUniqueToB = uniqueToB.getNumTotalFeatures();

        System.out.println("Number of unique features: " + numUniqueToA + "(A) " + numUniqueToB + "(B)");

        // set parent attribute
        exactMatches.addAttributeFieldToAll(PARENT_ATTRIBUTE, "Common");
        matchesWithinToleranceUniqueToA.addAttributeFieldToAll(PARENT_ATTRIBUTE, "A");
        matchesWithinToleranceUniqueToB.addAttributeFieldToAll(PARENT_ATTRIBUTE, "B");
        uniqueToA.addAttributeFieldToAll(PARENT_ATTRIBUTE, "A*");
        uniqueToB.addAttributeFieldToAll(PARENT_ATTRIBUTE, "B*");

        // set colors
        exactMatches.setColor(AB);
        matchesWithinToleranceUniqueToA.setColor(AA);
        matchesWithinToleranceUniqueToB.setColor(BB);
        uniqueToA.setColor(AAA);
        uniqueToB.setColor(BBB);

        Feature2DList finalResults = new Feature2DList(exactMatches);
        finalResults.add(matchesWithinToleranceUniqueToA);
        finalResults.add(matchesWithinToleranceUniqueToB);
        finalResults.add(uniqueToA);
        finalResults.add(uniqueToB);

        uniqueToA.exportFeatureList(new File(outputPath + "_AAA.bedpe"), false, Feature2DList.ListFormat.NA);
        uniqueToB.exportFeatureList(new File(outputPath + "_BBB.bedpe"), false, Feature2DList.ListFormat.NA);
        finalResults.exportFeatureList(new File(outputPath), false, Feature2DList.ListFormat.NA);

        int percentMatch = (int) Math.round(100 * ((double) (sizeB - numUniqueToB)) / ((double) sizeB));
        if (percentMatch > 95) {
            System.out.println("Test passed");
        } else {
            System.out.println("Test failed - " + percentMatch + "% match with reference list");
        }
        */
    }
}
