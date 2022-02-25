package emt.utils.validation;

import javastraw.reader.Dataset;
import javastraw.reader.Matrix;
import javastraw.reader.basics.Chromosome;
import javastraw.reader.block.ContactRecord;
import javastraw.reader.datastructures.ListOfDoubleArrays;
import javastraw.reader.mzd.MatrixZoomData;
import javastraw.reader.type.HiCZoom;
import javastraw.reader.type.NormalizationType;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Iterator;

public class MatrixSum {
    public static DescriptiveStatistics getRowSums(Dataset ds1, Chromosome chrom, NormalizationType norm,
                                                   HiCZoom zoom, ListOfDoubleArrays nv1) {
        Matrix matrix = ds1.getMatrix(chrom, chrom);
        if (matrix == null) return null;
        MatrixZoomData zd = matrix.getZoomData(zoom);
        if (zd == null) return null;

        long maxBin = nv1.getLength();
        ListOfDoubleArrays sums = new ListOfDoubleArrays(maxBin);

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

        return justPositiveValues(sums);
    }

    private static DescriptiveStatistics justPositiveValues(ListOfDoubleArrays sums) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double[] vals : sums.getValues()) {
            for (double val : vals) {
                if (val > 0) {
                    stats.addValue(val);
                }
            }
        }
        return stats;
    }
}
