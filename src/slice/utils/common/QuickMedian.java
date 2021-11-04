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

import java.util.List;

public class QuickMedian {

    static int partition(float[] arr, int low, int high) {
        float pivot = arr[high];          //taken pivot element as last element
        int z = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {                                  //arranging all elements that are less than pivot
                z++;
                float temp = arr[z];
                arr[z] = arr[j];
                arr[j] = temp;
            }
        }
        float temp = arr[z + 1];
        arr[z + 1] = arr[high];     //finalizing th position of piviot element in array which is sorted position
        arr[high] = temp;

        return z + 1;
    }

    public static float kselection(float[] arr, int low, int high, int k) {
        int partitionSortingValue = partition(arr, low, high);
        if (partitionSortingValue == k)           //comparing the position returned with the desired position k
            return arr[partitionSortingValue];
        else if (partitionSortingValue < k)      //partition value is less than k search left half array
            return kselection(arr, partitionSortingValue + 1, high, k);
        else                //partition value is greater than k search right half array
            return kselection(arr, low, partitionSortingValue - 1, k);
    }

    public static float fastMedian(float[] arr) {
        int len = arr.length;
        if (len % 2 == 1) {
            return kselection(arr, 0, len - 1, len / 2);  //median is at n/2 position if length is odd
        } else {
            float a = kselection(arr, 0, len - 1, len / 2);
            float b = kselection(arr, 0, len - 1, len / 2 - 1);
            return (a + b) / 2;       //median by performing average between n/2 and n/2-1
        }
    }

    public static float fastMedian(List<Float> list) {
        int lsize = list.size();
        if (lsize < 1) {
            return 0f;
        } else if (lsize == 1) {
            return list.get(0);
        }

        float[] arr = new float[lsize];
        for (int k = 0; k < arr.length; k++) {
            arr[k] = list.get(k);
        }
        return fastMedian(arr);
    }
}
