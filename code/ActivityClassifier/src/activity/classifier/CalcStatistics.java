package activity.classifier;

import android.util.Log;

/* 
An object of class CalcStatistics can be used to compute several simple statistics
for a set of numbers.  Numbers are passed in a 3D array of X,Y,Z.  Methods are provided to return the following
statistics for the set of numbers that have been entered: The number
of items, the sum of the items, the average, the standard deviation,
the maximum, the minimum and VerticalAccel. The vertical acceleration 
would normally equal gravity. If higher than gravity then device was
accelerated during the sampling interval most likely in a car. 
If it is less than gravity then device was rotated during sampling.
* @author ken
*/
public class CalcStatistics {
	private final int count;   // Number of numbers in the array.
	private float sum[] ={0,0,0};  // The sum of all the items in the array.
	private float sum_sqr[]={0,0,0};  // The sum of the squares of all the items.
	private float max[] = {Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};  // Largest item seen.
	private float min[] = {Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY};  // Smallest item seen.
	private float mean[]= new float[3]; //the mean of the array data
	
	public CalcStatistics(float[] ArrayIn, int samples) {
		//ArrayIn is a 3D array i.e. X,Y,Z  of a number of samples
	//Summarise array passed in	
		count = samples;
		
		for(int i=0;i<samples*3;i=i+3){//step through array in groups of 3
			
			for(int j=0;j<3;j++){
				float val = ArrayIn[(i + j)]; 
				sum[j]+=val;
				sum_sqr[j]+=val*val;
				if (val > max[j])
					max[j] = val;
				if (val < min[j])
				      min[j] = val;
			}
		}
		for(int j=0;j<3;j++){
			mean[j]= sum[j]/(samples);
			}
		
	}
	public int getCount() {   
	      // Return number of items in array as passed in.
	   return count;
	}

	public float[] getSum() {
	      // Return the sum of all the items that have been entered.
	   return sum;
	}

	public float[] getMean() {
	      // Return average of all the items that have been entered.
	      // Value is Float.NaN if count == 0.
	   return mean;  
	}
	
	public float getVerticalAccel() {
		float VerticalAccel=0;
		for(int j=0;j<3;j++){
    	Log.i("sd","count"+count+" sum_sqr "+sum_sqr[j]+" mean "+mean[j]+" ");
		VerticalAccel+=  mean[j]*mean[j];
		}
		VerticalAccel=(float) Math.sqrt( VerticalAccel);
	   return VerticalAccel;  
	}

	public float[] getStandardDeviation() {  
	     // Return standard deviation of all the items that have been entered.
	     // Value will be Double.NaN if count == 0.
		float StandardDeviation[] = new float[3];
		for(int j=0;j<3;j++){
        	Log.i("sd","count"+count+" sum_sqr "+sum_sqr[j]+" mean "+mean[j]+" ");
			StandardDeviation[j]= (float) Math.sqrt( sum_sqr[j]/count - mean[j]*mean[j]);
			}
	   return StandardDeviation;
	}

	public float[] getMin() {
	     // Return the smallest item that has been entered.
	     // Value will be - infinity if no items in array.
	   return min;
	}

	public float[] getMax() {
	     // Return the largest item that has been entered.
	     // Value will be -infinity if no items have been entered.
	   return max;
	}
}  // end class CalcStatistics

