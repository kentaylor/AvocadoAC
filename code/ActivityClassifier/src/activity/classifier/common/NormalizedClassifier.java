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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import activity.classifier.utils.CalcStatistics;
import activity.classifier.utils.FeatureExtractor;
import android.util.Log;

/**
 * Extracts basic features and applies a K-Nearest Network algorithm to an
 * array of data in order to determine the classification. The data consists
 * of two interleaved data sets, and each set has two features extracted -
 * the range and the mean.
 * 
 * @author chris
 */
public class NormalizedClassifier {

    private final Set<Map.Entry<Float[], String>> model;
    
    /**
     * {@link FeatureExtractor} instance to extract features from samples.
     */
    private FeatureExtractor featureExtractor;
    
    private Map<String, float[][]> meanStats;
    
    /**
     * Set the clustered data set for classification.
     * @param model clustered data set
     */
    public NormalizedClassifier(final Set<Entry<Float[], String>> model) {
        this.model = model;
        this.featureExtractor = new FeatureExtractor(Constants.NUM_OF_SAMPLES_PER_BATCH);
        
        this.meanStats = new TreeMap<String,float[][]>(new StringComparator(false));
        
        computeMeanStats();
    }

    private void computeMeanStats() {
        meanStats.clear();

        Map<String,List<float[]>> groups = new TreeMap<String,List<float[]>>(new StringComparator(false));

        for (Map.Entry<Float[], String> cl:model) {
        	String activity = cl.getValue();
        	Float[] stats = cl.getKey();
        	
            if (!groups.containsKey(activity))
                groups.put(activity, new ArrayList<float[]>());
            
            float[] statsArray = new float[stats.length];
            for (int i=0; i<stats.length; ++i)
            	statsArray[i] = stats[i];

            groups.get(activity).add(statsArray);
        }

        CalcStatistics cal = new CalcStatistics(FeatureExtractor.NUM_FEATURES);

        for (String activity:groups.keySet()) {
            List<float[]> data = groups.get(activity);

            float[][] array = new float[data.size()][];

            array = data.toArray(array);

            cal.assign(array, array.length);
            
            float[] mean = cal.getMean();
            float[] stddev = cal.getStandardDeviation();

            float[][] stats = new float[2][FeatureExtractor.NUM_FEATURES];
            
            for (int i=0; i<FeatureExtractor.NUM_FEATURES; ++i) {
            	stats[0][i] = mean[i];
            	stats[1][i] = stddev[i];
            }

            meanStats.put(activity, stats);

            Log.v(Constants.DEBUG_TAG, "\t"+activity+":\n\tmean   ="+Arrays.toString(stats[0])+"\n\tstd dev="+Arrays.toString(stats[1]));
        }

    }
    
    /**
     * Extracts basic features and applies a K-Nearest Network algorithm (K=1).
     * @param data sampled data array
     * @param size sampled data size 
     * @return best classification name
     */
    synchronized
    public String classifyRotated(final float[][] data) {
    	return internClassify(featureExtractor.extractRotated(data, 0));
    }
    
    /**
     * Extracts basic features and applies a K-Nearest Network algorithm (K=1).
     * @param data sampled data array
     * @param size sampled data size 
     * @return best classification name
     */
    synchronized
    public String classifyUnrotated(final float[][] data) {
    	return internClassify(featureExtractor.extractUnrotated(data, 0));
    }
    
    private String internClassify(float[] features) {
    	Log.v(Constants.DEBUG_TAG, "Classifier.classify: "+Arrays.toString(features));
    	
    	float temp;
        float bestDistance = Float.MAX_VALUE;
        String bestActivity = "UNCLASSIFIED/UNKNOWN";

        /*
         *  Compare between the points from the sample data and the points from the clustered data set.
         *  Get the closest points in the clustered data set, and classify the activity.
         */
        
        for (Map.Entry<Float[], String> entry : model) {
        	String activity = entry.getValue();
        	Float[] activityFeatures = entry.getKey();
        	float[][] activityStats = this.meanStats.get(activity);
        	
            float distance = 0;

            for (int i = 0; i < features.length; i++) {
            	temp = features[i] - activityFeatures[i];
            	temp = temp * (activityFeatures[i] - activityStats[0][i]) / activityStats[1][i];
                distance += temp*temp;
            }

            if (distance < bestDistance) {
                bestDistance = distance;
                bestActivity = entry.getValue();
            }
        }
        
        Log.v(Constants.DEBUG_TAG, "Best Activity: "+bestActivity+" by "+bestDistance);

        return bestActivity;
    }
}
