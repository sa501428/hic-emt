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

package slice.utils.common;

import javastraw.tools.MatrixTools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;


/**
 * Helper methods to handle matrix operations
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class FloatMatrixTools {

    public static void fill(float[][] allDataForRegion, float val) {
        for (int i = 0; i < allDataForRegion.length; i++) {
            Arrays.fill(allDataForRegion[i], val);
        }
    }

    public static void saveMatrixTextNumpy(String filename, float[][] matrix) {
        long size = matrix.length;
        size *= matrix[0].length;
        if (size > Integer.MAX_VALUE - 10) {
            System.err.println("Matrix is too big to save :(");
        } else {
            MatrixTools.saveMatrixTextNumpy(filename, matrix);
        }
    }


    public static float[][] cleanUpMatrix(float[][] matrix, boolean shouldZeroNans) {
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                float val = matrix[r][c];
                if (Float.isInfinite(val) || Math.abs(val) < 1E-10) {
                    matrix[r][c] = 0;
                }
                if (shouldZeroNans && Float.isNaN(val)) {
                    matrix[r][c] = 0;
                }
            }
        }
        return matrix;
    }


    public static void saveOEMatrixToPNG(File file, float[][] matrix) {
        double max = LogTools.getMaxAbsLogVal(matrix);
        int zoom = 50;

        BufferedImage image = new BufferedImage(zoom * matrix.length, zoom * matrix[0].length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                int color = mixOEColors(Math.log(matrix[i][j]), max);
                for (int zi = zoom * i; zi < zoom * (i + 1); zi++) {
                    for (int zj = zoom * j; zj < zoom * (j + 1); zj++) {
                        image.setRGB(zi, zj, color);
                    }
                }
            }
        }

        try {
            ImageIO.write(image, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int maskColors(double ratio, int color1, int color2) {
        int mask1 = 0x00ff00ff;
        int mask2 = 0xff00ff00;

        int f2 = (int) (256 * ratio);
        int f1 = 256 - f2;

        return (((((color1 & mask1) * f1) + ((color2 & mask1) * f2)) >> 8) & mask1)
                | (((((color1 & mask2) * f1) + ((color2 & mask2) * f2)) >> 8) & mask2);
    }

    private static int mixOEColors(double val0, double max) {
        int color1 = Color.WHITE.getRGB();
        int color2 = Color.RED.getRGB();
        double val = Math.abs(val0);
        if (val0 < 0) {
            color2 = Color.BLUE.getRGB();
        }

        double ratio = val / max;
        return maskColors(ratio, color1, color2);
    }
}