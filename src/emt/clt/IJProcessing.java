package emt.clt;

import emt.Tools;

@SuppressWarnings({"UnusedAssignment", "unused"})
public class IJProcessing {
    public static void main(String[] argv) throws Exception {

        String[] command = new String[]{"excise", "-r", "1000", "--only-intra",
                "-c", "GM18B_1,U2B_1,U8B_1",
                "/Users/mshamim/Desktop/stitch/adj_mapq30/intact_survey_30.hic",
                "/Users/mshamim/Desktop/stitch/adj_mapq30/v10"
        };
        Tools.main(command);

        System.gc();

        command = new String[]{"excise", "-r", "500", "--only-intra",
                "-c", "GM18B_1,GM18B_17," +
                "HCT116_1,HCT116_17," +
                "RAD21_1,RAD21_17," +
                "K562_1,K562_17," +
                "U2B_1,U2B_17," +
                "U8B_1,U8B_17",
                "/Users/mshamim/Desktop/stitch/adj_mapq0/intact_survey.hic",
                "/Users/mshamim/Desktop/stitch/adj_mapq0/v9"
        };
        //Tools.main(command);

        command = new String[]{"info",
                "/Users/mshamim/Desktop/stitch/adj_mapq0/intact_survey.hic"
        };
        //Tools.main(command);

    }
}