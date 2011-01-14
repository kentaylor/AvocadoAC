package activity.classifier;

import android.util.Log;

/**
 * An utility class which provide the function for the calculation of combining standard deviation. 
 * 
 * 
 * @author Justin Lee
 *
 */
public class Calibration {

	private static float[] sd = {0,0,0};
	private static float[] mean = {0,0,0};
	
	private boolean isCalibrated;
	
	private static int count=0;
	
	/**
	 * Initialise {@link #isCalibrated}, and increase {@link #count} by 1 through {@link #increaseCount()} every time this class get called. 
	 */
	public Calibration(){
		isCalibrated=false;
		increaseCount();
	}

	/**
	 * A public method which other classes can set the count value.
	 * @see activity#classifier#common#service#ClassifierService
	 */
	public void setCount(int count){
		this.count = count;
	}
	
	/**
	 * A public method which other classes call and use this number in different purposes.
	 * @see activity#classifier#common#service#ClassifierService
	 * @return the number of how many calibrations have done so far.
	 */
	public int getCount(){
		return count;
	}
	
	/**
	 * A public method which other classes call and use these standard deviation values for classification.
	 * @see activity#classifier#common#service#ClassifierService
	 * @return the values of standard deviation for X, Y, Z axis
	 */
	public float[] getSSD(){
		return sd;
	}
	
	/**
	 * 
	 * @return true if the application have done the calibration.
	 */
	public boolean isCalibrated(){
		return isCalibrated;
	}
	
	/**
	 * Increase the count value;
	 */
	private void increaseCount(){
		count++;
	}
	
	/**
	 * Calculate standard deviation by Combining Standard Deviation method.
	 * @see <a href="http://en.wikipedia.org/wiki/Standard_deviation">Combining Standard Deviation method</a>
	 * 
	 * @param mean Acceleration mean values
	 * @param sd Acceleration standard deviation values
	 */
	public void doCalibration(float[] mean, float[] sd){
		Log.i("Calitest"," ");
		Log.i("Calitest",this.mean[0]+" "+this.mean[1]+" "+this.mean[2]+" ");
		Log.i("Calitest",mean[0]+" "+mean[1]+" "+mean[2]+" ");
		Log.i("Calitest",this.sd[0]+" "+this.sd[1]+" "+this.sd[2]+" ");
		Log.i("Calitest",sd[0]+" "+sd[1]+" "+sd[2]+" ");
		for(int i=0;i<3;i++){
			if(this.mean[i]==0){
				this.mean[i]=mean[i];
			}
			if(this.sd[i]==0){
				this.sd[i]=sd[i];
			}
			this.mean[i] = (this.mean[i]+mean[i])/2;
			this.sd[i] = (float) Math.sqrt((this.sd[i]*this.sd[i]+sd[i]*sd[i])/2 + ((this.mean[i]-mean[i])*(this.mean[i]-mean[i]))/4);
		}
		Log.i("Calitest",this.mean[0]+" "+this.mean[1]+" "+this.mean[2]+" ");
		Log.i("Calitest",this.sd[0]+" "+this.sd[1]+" "+this.sd[2]+" ");
	}
	
}
