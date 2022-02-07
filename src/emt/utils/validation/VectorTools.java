package emt.utils.validation;

import javastraw.reader.datastructures.ListOfDoubleArrays;

public class VectorTools {
    public static void assertAreEqual(ListOfDoubleArrays data1, ListOfDoubleArrays data2, String description) {
        try {
            if (data1.getLength() != data2.getLength()) {
                //System.err.println("Vector length mismatch: "+data1.getLength() +" vs "+ data2.getLength() + " " + description);
                //System.exit(24);
            }

            long n = Math.min(data1.getLength(), data2.getLength());

            double absError = 0;
            for (long q = 0; q < n; q++) {
                double err = Math.abs(data1.get(q) - data2.get(q));
                if (!Double.isNaN(err)) {
                    absError += err;
                }
            }

            if (absError > 1e-10) {
                System.err.println("Vector difference too big " + absError);
                System.exit(25);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.exit(26);
        }
        //assertAreEqual(data1.getValues(), data2.getValues());
    }

    /*
    private static void assertAreEqual(List<double[]> values1, List<double[]> values2) {
        for (int q = 0; q < values1.size(); q++) {
            assertAreEqual(values1.get(q), values2.get(q));
        }
    }

    private static void assertAreEqual(double[] d1, double[] d2) {
        assert d1.length == d2.length;

        for (int q = 0; q < d1.length; q++) {
            absError += Math.abs(d1[q] - d2[q]);
        }
        assert absError < 1e-5;
    }
    */
}
