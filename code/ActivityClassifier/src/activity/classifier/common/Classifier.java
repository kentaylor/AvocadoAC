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

import activity.classifier.utils.CalcStatistics;
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
    
    /**
     * {@link CalcStatistics} instance to get maximum, minimum, and mean acceleration values.
     */
    public CalcStatistics calc;
    
    /**
     * Set the clustered data set for classification.
     * @param model clustered data set
     */
    public Classifier(final Set<Entry<Float[], String>> model) {
        this.model = model;
        this.calc = new CalcStatistics(3);
    }

    /**
     * Extracts basic features and applies a K-Nearest Network algorithm (K=1).
     * @param data sampled data array
     * @param size sampled data size 
     * @return best classification name
     */
    public synchronized String classify(final float[][] data, int size) {
    	//new CalcStatistics instance
        calc.assign(data,size);
        float[] max= new float[3];
        float[] min= new float[3];
        float[] mean= new float[3];
        
        //get maximum, minimum, and mean acceleration values.
        max = calc.getMax();
        min = calc.getMin();
        mean = calc.getMean();

        //set the points to compare to the clustered data set.
        final float[] points = {
            Math.abs(mean[1]),
            Math.abs(mean[2]),
            max[1] - min[1],
            max[2] - min[2]
        };

        float bestDistance = Float.MAX_VALUE;
        String bestActivity = "UNCLASSIFIED/UNKNOWN";

        /*
         *  Compare between the points from the sample data and the points from the clustered data set.
         *  Get the closest points in the clustered data set, and classify the activity.
         */
        
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
