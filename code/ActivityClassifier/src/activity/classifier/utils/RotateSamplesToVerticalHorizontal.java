package activity.classifier.utils;

import java.util.Arrays;

import android.hardware.SensorManager;
import android.util.Log;

/**
 * 
 * An object of class RotateSamplesToVerticalHorizontal will rotate a set 
 * of sampled accelerometer data to vertical axis Z dominant horizontal 
 * axis Y and minor horizontal axis Y.  Numbers are passed in a 3D array 
 * of X,Y,Z. Methods are provided to return: 
 * The rotated array.
 *  
 * @author Ken Taylor
 */

public class RotateSamplesToVerticalHorizontal {
	
	// number of dimensions
	private final static int DIM = 3;
	
	private final static int X_AXIS = 0;
	private final static int Y_AXIS = 1;
	private final static int Z_AXIS = 2;
	
	
	
	//	gravity vector in use
	private float[] gravityVec = new float[DIM];
	
	//	derived horizontal vector
	private float[] horizontalVec = new float[DIM];
	
	//	computed rotation matrix
	private float[] rotationMat = new float[DIM*DIM];
	
	//	a temporary vector to hold values while doing matrix multiplication
	private float[] tempVec = new float[DIM];
	
	/**
	 * Rotates the accelerometer samples to world coordinates,
	 * using a gravity vector derived from the same samples.
	 * 
	 * Note that, a horizontal vector is then derived from the
	 * gravity vector, which is used to rotate the samples to the
	 * world coordinates. This makes the final samples' direction-less
	 * and hence only the magnitude of the horizontal component should
	 * be used.
	 * 
	 * Use {@link #rotateToWorldCoordinates(float[], float[])}
	 * in order to keep the direction of the horizontal component
	 * of the sampled vectors relative to magnetic north.
	 * 
	 * @param samples
	 * The samples to convert to world coordinates. The array will
	 * contain the world coordinates upon return. Unless the
	 * function returns false, which means that the samples haven't
	 * been altered. 
	 * 
	 * @return
	 * Returns false if the function is unable to compute the rotation
	 * matrix and hence unable to change the samples to world coordinates.
	 */
	public synchronized boolean rotateToWorldCoordinates(float[][] samples)
	{
		computeMeanVector(samples, gravityVec);
		
//		Log.v("TEST", "Gravity Vec="+vec2str(gravityVec)+" = "+calcMag(gravityVec));
		
		convertToHorVec(gravityVec, horizontalVec);
		
//		Log.v("TEST", "Hor Vec="+vec2str(horizontalVec)+" = "+calcMag(horizontalVec));
		
		return internRotateToWorldCoordinates(samples, gravityVec, horizontalVec);
	}
	
	/**
	 * 
	 * Rotates the accelerometer samples to world coordinates,
	 * using a gravity vector derived from the same samples,
	 * and the given geo-magnetic vector.
	 * 
	 * Note that, the geo-magnetic vector is used as the horizontal
	 * component to compute the rotation matrix, which is used to 
	 * rotate the samples to world coordinates. This makes the 
	 * final samples' direction relative to magnetic-north.
	 * 
	 * Use {@link #rotateToWorldCoordinates(float[])}
	 * if the horizontal direction is not required, or if a
	 * geo-magnetic vector can not be obtained.
	 * 
	 * @param samples
	 * The samples to convert to world coordinates. The array will
	 * contain the world coordinates upon return. Unless the
	 * function returns false, which means that the samples haven't
	 * been altered. 
	 * 
	 * @param geoMagSamples
	 * Geo-magnetic vector to use in obtaining the rotation matrix.
	 * 
	 * @return
	 * Returns false if the function is unable to compute the rotation
	 * matrix and hence unable to change the samples to world coordinates.
	 * 
	 */
	public synchronized boolean rotateToWorldCoordinates(float[][] samples, float[][] geoMagSamples)
	{
		computeMeanVector(samples, gravityVec);
		
		computeMeanVector(geoMagSamples, horizontalVec);
		
		return internRotateToWorldCoordinates(samples, gravityVec, horizontalVec);
	}
	
	private boolean internRotateToWorldCoordinates(float[][] samples, float[] gravityVec, float[] horVec)
	{
//		Log.v("TEST", "Hor Vec="+vec2str(horizontalVec)+" = "+calcMag(horizontalVec));
		
		if (!SensorManager.getRotationMatrix(rotationMat, null, gravityVec, horVec)) {
			//	sometimes fails, according to the api
			return false;
		}
		
		//	apply to current samples
		applyRotation(samples);
		/*
		CalcStatistics st = new CalcStatistics(samples, samples.length/3);
		float[] min = st.getMin();
		float[] max = st.getMax();
		float[] mean = st.getMean();
		float[] sd = st.getStandardDeviation();
		if (	Math.abs(max[0]-min[0])>1.0 || 
				Math.abs(max[1]-min[1])>1.0 || 
				Math.abs(max[2]-min[2])>1.0	) {
			for (int i=0; i<samples.length; i+=DIM) {
				Log.v("TEST", "sample="+vec2str(samples,i));
			}
			Log.v("TEST", "min="+vec2str(min)+", max="+vec2str(max)+", mean="+vec2str(mean)+", s.d.="+vec2str(sd));
		}
		*/
		return true;
	}
	
	@SuppressWarnings("unused")
	private static float calcMag(float[] vec) {
		double mag = 0.0f;
		for (int i=0; i<DIM; ++i)
			mag += vec[i]*vec[i];
		return (float)Math.sqrt(mag);
	}
	
	/**
	 * Applies the current rotation matrix to the samples
	 * 
	 * @param samples
	 * samples to apply the current rotation matrix to
	 * index:		[ 0 ][ 1 ][ 2 ][ 3 ][ 4 ][ 5 ]...
	 * dimension:   [ x ][ y ][ z ][ x ][ y ][ z ]... 
	 * 
	 * rotation matrix:
	 * [ 0 ][ 1 ][ 2 ]
	 * [ 3 ][ 4 ][ 5 ]
	 * [ 6 ][ 7 ][ 8 ]
	 *
	 */
	private void applyRotation(float[][] samples)
	{
		for (int s=0; s<samples.length; ++s) {
			for (int d=0; d<DIM; ++d) {
				tempVec[d] = 0.0f;
				
				for (int k=0; k<DIM; ++k) {
					tempVec[d] += rotationMat[(d*3)+k] * samples[s][k];
				}
			}
			
			for (int d=0; d<DIM; ++d) {
				samples[s][d] = tempVec[d];
			}
		}
	}
	
	/**
	 * 
	 * @param samples
	 * samples to compute the mean vector of, should be in the form:
	 * index:		[ 0 ][ 1 ][ 2 ][ 3 ][ 4 ][ 5 ]...
	 * dimension:   [ x ][ y ][ z ][ x ][ y ][ z ]... 
	 * 
	 * @param outVec
	 * an array of 3 floats to save the final mean vector in
	 */
	private static void computeMeanVector(float[][] samples, float[] outVec)
	{
		for (int d=0; d<DIM; ++d)
			outVec[d] = 0.0f;
		
		//	find the total and number of samples (each having x, y, and z)
		int count = 0;
		for (int s=0; s<samples.length; ++s) {
			for (int d=0; d<DIM; ++d)
				outVec[d] += samples[s][d];
			++count;
		}
		
		//	convert total to the mean
		for (int d=0; d<DIM; ++d) {
			outVec[d] /= count;
		}
		
	}
	
	/**
	 * 
	 * @param inGravityVec
	 * the gravity vector(3 dimensions) to convert to a horizontal vector
	 * 
	 * @param outVec
	 * an array of 3 floats to save the final horizontal vector in
	 */
	private static void convertToHorVec(float[] inGravityVec, float[] outVec)
	{
		int indexSmallest = findIndexOfSmallest(inGravityVec);
		
		if (indexSmallest<0) {
			throw new RuntimeException("index of the smallest element among "+
			                           vec2str(inGravityVec)+" couldn't be found!");
		}
		
		//	assign the smallest to the first value of the vector (x)
		outVec[X_AXIS] = inGravityVec[indexSmallest];
		//	assign the other two to the other two values (y and z)
		int ddst = Y_AXIS; 
		for (int dsrc=0; dsrc<DIM; ++dsrc)
			if (dsrc!=indexSmallest) {
				outVec[ddst] = inGravityVec[dsrc];
				++ddst;
			}
		
		//	negate either y or z
		outVec[Y_AXIS] = -outVec[Y_AXIS];
	}
	
	/**
	 * 
	 * @param vector
	 * The 3 dimensional vector to find the minimum value
	 * 
	 * @return
	 * The index of the minimum value in the vector
	 */
	private static int findIndexOfSmallest(float[] vector) {
		int index = -1;
		float value = Float.MAX_VALUE;
		float temp;
		for (int d=0; d<DIM; ++d) {
			temp = vector[d];
			if (temp<0.0f)
				temp = -temp;
			
			if (temp<value) {
				value = temp;
				index = d;
			}
		}
		return index;
	}
	
	private static String vec2str(float[] vec) {
		return String.format("{x=% 3.2f, y=% 3.2f, z=% 3.2f}", vec[X_AXIS], vec[Y_AXIS], vec[Z_AXIS]);
	}

	@SuppressWarnings("unused")
	private static String vec2str(float[] vec, int start) {
		return String.format("{x=% 3.2f, y=% 3.2f, z=% 3.2f}", vec[start+X_AXIS], vec[start+Y_AXIS], vec[start+Z_AXIS]);
	}
	
}
