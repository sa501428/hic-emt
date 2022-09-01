package emt.utils.validation;

import javastraw.reader.Dataset;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.block.ContactRecord;
import javastraw.reader.datastructures.ListOfDoubleArrays;
import javastraw.reader.mzd.Matrix;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.NormalizationType;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Iterator;

public class MatrixSum {
    public static DescriptiveStatistics getRowSums(Dataset ds1, Chromosome chrom, NormalizationType norm,
                                                   HiCZoom zoom, ListOfDoubleArrays nv1,
                                                   String stem, boolean printSums) {
        Matrix matrix = ds1.getMatrix(chrom, chrom);
        if (matrix == null) return null;
        MatrixZoomData zd = matrix.getZoomData(zoom);
        if (zd == null) return null;

        long maxBin = nv1.getLength();
        ListOfDoubleArrays sums = new ListOfDoubleArrays(maxBin);

        try {
            Iterator<ContactRecord> it = zd.getNormalizedIterator(norm);
            while (it.hasNext()) {
                ContactRecord cr = it.next();
                if (cr.getCounts() > 0) {
                    sums.addTo(cr.getBinX(), cr.getCounts());
                    if (cr.getBinX() != cr.getBinY()) {
                        sums.addTo(cr.getBinY(), cr.getCounts());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return justPositiveValues(sums, stem, printSums);
    }

    private static DescriptiveStatistics justPositiveValues(ListOfDoubleArrays sums, String stem, boolean printSums) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        long totalNumValues = 0;
        long totalNumPositives = 0;
        for (double[] vals : sums.getValues()) {
            totalNumValues += vals.length;
            for (double val : vals) {
                if (val > 0) {
                    stats.addValue(val);
                    totalNumPositives++;
                }
            }
        }

        if (printSums) {
            long numErrors = 0;
            double median = stats.getPercentile(50);

            for (double[] vals : sums.getValues()) {
                for (double val : vals) {
                    if (val > 0) {
                        double err = Math.abs((val / median) - 1);
                        if (err > 0.1) {
                            numErrors++;
                        }
                    }
                }
            }

            float percentErrorMedian = (float) (100f * (numErrors * 1.0) / (totalNumPositives * 1.0));
            System.out.println(stem + " percent rows with row sum error > 0.1 (" + percentErrorMedian +
                    " %)  <" + numErrors + ":" + totalNumPositives + ":" + totalNumValues + ">");
        }
        return stats;
    }
}
