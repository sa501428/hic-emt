package emt.utils.validation;

import javastraw.reader.Dataset;
import javastraw.reader.Matrix;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.block.ContactRecord;
import javastraw.reader.datastructures.ListOfIntArrays;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.HiCZoom;
import javastraw.tools.MatrixTools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SparsityCheck {

    public static void getNonNanCounts(Dataset ds1, Chromosome chrom,
                                       HiCZoom zoom) {

        Matrix matrix = ds1.getMatrix(chrom, chrom);
        if (matrix == null) return;
        MatrixZoomData zd = matrix.getZoomData(zoom);
        if (zd == null) return;

        long maxBin = (chrom.getLength() / zoom.getBinSize()) + 1;
        ListOfIntArrays counts = new ListOfIntArrays(maxBin);

        boolean somethingWasFound = false;
        Iterator<ContactRecord> it = zd.getDirectIterator();
        while (it.hasNext()) {
            ContactRecord cr = it.next();
            if (cr.getCounts() > 0) {
                somethingWasFound = true;
                counts.addTo(cr.getBinX(), 1);
                if (cr.getBinX() != cr.getBinY()) {
                    counts.addTo(cr.getBinY(), 1);
                }
            }
        }

        if (somethingWasFound) {
            justPositivesAsArray(counts, chrom.getName() + "_" + zoom.getBinSize());
        }
    }

    private static void justPositivesAsArray(ListOfIntArrays counts, String description) {
        List<Integer> positives = new ArrayList<>();
        int countZeros = 0;
        for (int[] values : counts.getValues()) {
            for (int val : values) {
                if (val > 0) {
                    positives.add(val);
                } else {
                    countZeros++;
                }
            }
        }
        double totalZeros = countZeros;
        totalZeros /= counts.getLength();

        System.out.println(description + " " + totalZeros);

        MatrixTools.saveMatrixTextNumpy(
                description + "_num_non_zeros.npy", toArray(positives));
    }

    private static int[] toArray(List<Integer> ilist) {
        int[] arr = new int[ilist.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = ilist.get(i);
        }
        return arr;
    }
}
