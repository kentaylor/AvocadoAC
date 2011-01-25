package activity.classifier.accel;

import activity.classifier.common.Constants;
import android.util.Log;

/**
 * 
 * @author Umran
 * 
 * <p>
 * Represents a batch of consecutive samples taken within one sampling period.
 * 
 * <p>
 * By placing the samples taken within a single sampling period in one class,
 * it is possible to move a reference to the batch instead of copying the
 * batch. In addition, we can place statistical information extracted from
 * the batch, in the same class.
 *
 */
public class SampleBatch {
	
	public static final int NUM_OF_SAMPLES = 128;

	public final float[][] data;
	private int currentSample;
	private boolean charging;
	private float ignore[];
	private String lastClassificationName;
	
	public SampleBatch() {
		data = new float[NUM_OF_SAMPLES][3];
		currentSample = 0;
	}
	
	public float[] getCurrentSample() {
		return data[currentSample];
	}
	
	public boolean nextSample() {
		++currentSample;
		
		if (currentSample>=NUM_OF_SAMPLES) {
			currentSample = NUM_OF_SAMPLES;
			return false;
		}
		else {
			return true;
		}
	}
	
	public void reset() {
		currentSample = 0;
	}
	
	public int getSize() {
		return currentSample;
	}

	public boolean isCharging() {
		return charging;
	}

	public void setCharging(boolean charging) {
		this.charging = charging;
	}

	public float[] getIgnore() {
		return ignore;
	}

	public void setIgnore(float[] ignore) {
		this.ignore = ignore;
	}

	public String getLastClassificationName() {
		return lastClassificationName;
	}

	public void setLastClassificationName(String lastClassificationName) {
		this.lastClassificationName = lastClassificationName;
	}
	
	
	
}
