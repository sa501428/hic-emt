/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2021 Rice University, Baylor College of Medicine, Aiden Lab
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package slice.clt;


import slice.MixerTools;

/**
 * Created for testing multiple CLTs at once
 * Basically scratch space
 */
@SuppressWarnings({"UnusedAssignment", "unused"})
public class AggregateProcessing {

    public static void main(String[] argv) throws Exception {

        String[] files = new String[]{
                //"/Users/mshamim/Desktop/hicfiles/gm12878_rh14_30.hic" //
        };

        files = new String[]{
                "/Users/mshamim/Desktop/hicfiles/SCALE/hap1_SCALE_30.hic",
                "/Users/mshamim/Desktop/hicfiles/SCALE/imr90_rh14_SCALE_30.hic",
                "/Users/mshamim/Desktop/hicfiles/SCALE/K562_2014_SCALE_30.hic",
                "/Users/mshamim/Desktop/hicfiles/gm12878_rh14_30.hic"
        };

        String[] stems = new String[]{
                "hap1",
                "imr",
                "k562",
                "gm"
        };

        files = new String[]{
                "/Users/mshamim/Desktop/subsampling_experiment/primary_15.hic",
                "/Users/mshamim/Desktop/subsampling_experiment/primary_29.hic",
                "/Users/mshamim/Desktop/subsampling_experiment/primary_43.hic",
                "/Users/mshamim/Desktop/subsampling_experiment/primary_58.hic",
                "/Users/mshamim/Desktop/hicfiles/GM12878_primary14_30.hic",
                "/Users/mshamim/Desktop/hicfiles/gm12878_rh14_30.hic"
        };

        stems = new String[]{"p15", "p29", "p43", "p58", "primary", "gmMega"};


        int id = 810;
        {

            for (int f = 0; f < files.length; f++) {// files.length
                String file = files[f];
                String stem = stems[f];
                for (int res : new int[]{100}) { //  ,100000,   50000,25000,10000 100000 100000 50000
                    String folder = stem + "_SLICE_" + id;
                    String[] strings = new String[]{"slice", "-r", res + "000",
                            file, "2,7,4",
                            "/Users/mshamim/Desktop/reSLICE/phnx_" + id + "_z4_" + res + "000_" + folder,
                            folder + "_"
                    };
                    System.out.println("-----------------------------------------------------");
                    MixerTools.main(strings);
                    System.gc();
                }
            }

            System.gc();
        }
    }
}
