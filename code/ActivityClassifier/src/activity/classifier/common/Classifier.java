/*
 * Copyright (c) 2009-2010 Chris Smith
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package activity.classifier.common;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import activity.classifier.CalcStatistics;
import android.util.Log;

/**
 * Extracts basic features and applies a K-Nearest Network algorithm to an
 * array of data in order to determine the classification. The data consists
 * of two interleaved data sets, and each set has two features extracted -
 * the range and the mean.
 * 
 * @author chris
 */
public class Classifier {

    private final Set<Map.Entry<Float[], String>> model;
    CalcStatistics calc;
    public Classifier(final Set<Entry<Float[], String>> model) {
        this.model = model;
        
    }

    public String classify(final float[] calData, final float[] data, int size) {
        final float oddTotal = data[8], evenTotal = data[5];
        final float oddMin = data[6], oddMax = data[7];
        final float evenMin = data[3], evenMax = data[4];
        calc = new CalcStatistics(calData,size);
        float[] max= new float[3];
        float[] min= new float[3];
        float[] mean= new float[3];
        float[] sum= new float[3];
        
        Log.i("calData",calData.length+"");
        
        max = calc.getMax();
        min = calc.getMin();
        mean = calc.getMean();
        sum = calc.getSum();
//        final float[] points = {
//                Math.abs(evenTotal / 128),
//                Math.abs(oddTotal / 128),
//                evenMax - evenMin,
//                oddMax - oddMin
//            };
        final float[] points = {
            Math.abs(mean[1]),
            Math.abs(mean[2]),
            max[1] - min[1],
            max[2] - min[2]
        };
        Log.i("stat","evenMax: "+evenMax+" max[1]: "+max[1]);
        Log.i("stat","evenMin: "+evenMin+" min[1]: "+min[1]);
        Log.i("stat","oddMax: "+oddMax+" max[2]: "+max[2]);
        Log.i("stat","oddMin: "+oddMin+" min[2]: "+min[2]);
        Log.i("stat","absEven: "+(evenTotal/128)+" mean[1]: "+mean[1]);
        Log.i("stat","absOdd: "+(oddTotal/128)+" mean[2]: "+mean[2]);

        float bestDistance = Float.MAX_VALUE;
        String bestActivity = "UNCLASSIFIED/UNKNOWN";

        for (Map.Entry<Float[], String> entry : model) {
            float distance = 0;

            for (int i = 0; i < points.length; i++) {
                distance += Math.pow(points[i] - entry.getKey()[i], 2);
            }

            if (distance < bestDistance) {
                bestDistance = distance;
                bestActivity = entry.getValue();
            }
        }

        return bestActivity;
    }
}
