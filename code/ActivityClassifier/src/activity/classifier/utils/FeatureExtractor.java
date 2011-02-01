/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier.utils;

import activity.classifier.common.Constants;
import android.util.Log;

/**
 *	Extracts features from a given sample window.
 *	The window could be already rotated to world-orientation, hence
 *	use the function {@link #extractRotated(float[][], int)} or
 *	if not previously rotated, use the function {@link #extractUnrotated(float[][], int)}.
 *
 *
 * @author Umran
 */
public class FeatureExtractor {

    public static final int NUM_FEATURES = 4;

    public static final int FEATURE_RANGE_HOR   = 0;
    public static final int FEATURE_RANGE_VER   = 1;
    public static final int FEATURE_MEAN_HOR    = 2;
    public static final int FEATURE_MEAN_VER    = 3;

    private int windowSize;
    private RotateSamplesToVerticalHorizontal rotate;
    private float[][] samples;
    private float[][] twoDimSamples;
    private CalcStatistics sampleStats;
    private float[] features;

    public FeatureExtractor(int windowSize) {
        this.windowSize = windowSize;

        this.rotate = new RotateSamplesToVerticalHorizontal();
        this.samples = new float[windowSize][3];
        this.twoDimSamples = new float[windowSize][2];
        this.sampleStats = new CalcStatistics(2);
        this.features = new float[NUM_FEATURES];
    }

    synchronized
    public float[] extractUnrotated(float[][] input, int windowStart)
    {
        if (windowStart+windowSize>input.length) {
            Log.w(Constants.DEBUG_TAG, "attempting to extract features past " +
                    "the end of samples (windowStart="+windowStart+", size="+samples.length+")");
            return null;
        }

        for (int j=0; j<windowSize; ++j) {
            samples[j][0] = input[windowStart+j][0];
            samples[j][1] = input[windowStart+j][1];
            samples[j][2] = input[windowStart+j][2];
        }

        if (!rotate.rotateToWorldCoordinates(samples)) {
//            System.out.println("WARNING: Unable to rotate samples)");
            return null;
        }

        for (int j=0; j<windowSize; ++j) {
            twoDimSamples[j][0] = (float)Math.sqrt(
                    samples[j][0]*samples[j][0] +
                    samples[j][1]*samples[j][1]);
            twoDimSamples[j][1] = samples[j][2];
        }

        return internExtract();
    }
    
    synchronized
    public float[] extractRotated(float[][] input, int windowStart)
    {
        if (windowStart+windowSize>input.length) {
        	Log.w(Constants.DEBUG_TAG, "attempting to extract features past " +
                    "the end of samples (windowStart="+windowStart+", size="+samples.length+")");
            return null;
        }

        for (int j=0; j<windowSize; ++j) {
            twoDimSamples[j][0] = (float)Math.sqrt(
                    input[j][0]*input[j][0] +
                    input[j][1]*input[j][1]);
            twoDimSamples[j][1] = input[j][2];
        }

        return internExtract();
    }

    private float[] internExtract() {
        sampleStats.assign(twoDimSamples, windowSize);

        float[] min = sampleStats.getMin();
        float[] max = sampleStats.getMax();
        float[] mean = sampleStats.getMean();

        features[FEATURE_RANGE_HOR] = max[0]-min[0];
        features[FEATURE_RANGE_VER] = max[1]-min[1];
        features[FEATURE_MEAN_HOR] = mean[0];
        features[FEATURE_MEAN_VER] = mean[1];

        return features;
    }

}
