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


import stitch.Stitcher;

/**
 * Created for testing multiple CLTs at once
 * Basically scratch space
 */
@SuppressWarnings({"UnusedAssignment", "unused"})
public class AggregateProcessing {

    public static void main(String[] argv) throws Exception {


        String server = "https://s3.wasabisys.com/hicfiles/internal/a6b4caff-d7a7-4dff-a341-ef61b5dd39f7/";
        String[] files = new String[]{
                server + "GM_Intact_18B_mapq0/inter.hic",
                server + "GM_Intact_18B_mapq30/inter_30.hic",
                server + "HCT116_6.5B_mapq0/inter.hic",
                server + "HCT116_6.5B_mapq30/inter_30.hic",
                server + "HCT116_RAD21_DEGRON_mapq0/inter.hic",
                server + "HCT116_RAD21_DEGRON_mapq30/inter_30.hic",
                server + "K562_5B_mapq0/inter.hic",
                server + "K562_5B_mapq30/inter_30.hic",
                server + "Ultima_2.5B_mapq0/inter.hic",
                server + "Ultima_2.5B_mapq30/inter_30.hic",
                server + "Ultima_8B_mapq0/inter.hic",
                server + "Ultima_8B_mapq30/inter_30.hic"
        };
        String[] stems = new String[]{
                "GM18B_1",
                "GM18B_30",
                "HCT116_1",
                "HCT116_30",
                "RAD21_1",
                "RAD21_30",
                "K562_1",
                "K562_30",
                "U2B_1",
                "U2B_30",
                "U8B_1",
                "U8B_30"
        };
        String[] regions = new String[]{
                "1:100000000:110005000",
                "2:115000000:125000000",
                "3:80010000:90005000",
                "5:9900000:29900000",
                "8:60000000:80000000",
                "17:10000000:15000000"
        };

        int resolution = 500;
        String normalization = "SCALE";
        boolean adjustOrigin = false;

        ;

        Stitcher stitcher = new Stitcher(files, stems, regions, normalization, adjustOrigin, resolution);
        stitcher.buildTempFiles();




        /*
        String[] line = {"pre",
                            newShortMND,
                            "custom.hic",
                            newChromSizes};
                    HiCTools.main(line);
         */


    }
}
