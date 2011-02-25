package activity.classifier.repository;

import android.content.Context;

/**
 * A utility class which extends superclass {@link Queries} 
 * for handling queries to get the system information.
 * 
 * Edit from Umran:
 * Changed all methods dealing with database open/close methods
 * to <code>synchronized</code> in order to ensure thread safety when called 
 * from multiple threads.
 * 
 * @author Justin Lee
 *
 */
public class OptionQueries extends Queries {

	private DbAdapter dbAdapter;
	
	private boolean isServiceRunning;
	private boolean isCalibrated;
	private boolean isAccountSent;
	private boolean isWakeLockSet;
	private float valueOfGravity;
	private float[] standardDeviation = new float[3];
	
	/**
	 * @see Queries
	 * @param context context from Activity or Service classes 
	 */
	public OptionQueries(Context context){
		super(context);
		dbAdapter = super.dbAdapter;
//		isServiceRunning = false;
//		isCalibrated = false;
//		isAccountSent = false;
//		isWakeLockSet = false;
//		valueOfGravity = 1;
//		for(int i = 0; i < 3; i++){
//			standardDeviation[i] = (float)0.1;
//		}
	}

	
	public synchronized void load(){
		int isServiceRunning;
		int isCalibrated;
		float valueOfGravity;
		int isAccountSent;
		int isWakeLockSet;
		float[] standardDeviation = new float[3];
		dbAdapter.open();
		isServiceRunning = dbAdapter.fetchFromStartTableInt("isServiceRunning");
		isCalibrated = dbAdapter.fetchFromStartTableInt("isCalibrated");
		valueOfGravity = dbAdapter.fetchFromStartTableFloat("valueOfGravity");
		isAccountSent = dbAdapter.fetchFromStartTableInt("isAccountSent");
		isWakeLockSet = dbAdapter.fetchFromStartTableInt("isWakeLockSet");
		standardDeviation[0] = dbAdapter.fetchFromStartTableFloat("sdX");
		standardDeviation[1] = dbAdapter.fetchFromStartTableFloat("sdY");
		standardDeviation[2] = dbAdapter.fetchFromStartTableFloat("sdZ");
		dbAdapter.close();
		
		if(isServiceRunning==0){
			this.isServiceRunning = false;
		}else if(isServiceRunning==1){
			this.isServiceRunning = true;
		}
		if(isCalibrated==0){
			this.isCalibrated = false;
		}else if(isCalibrated==1){
			this.isCalibrated = true;
		}
		if(isAccountSent==0){
			this.isAccountSent = false;
		}else if(isAccountSent==1){
			this.isAccountSent = true;
		}
		if(isWakeLockSet==0){
			this.isWakeLockSet = false;
		}else if(isWakeLockSet==1){
			this.isWakeLockSet = true;
		}
		for(int i = 0; i < 3; i++){
			this.standardDeviation[i] = standardDeviation[i];
		}
		this.valueOfGravity = valueOfGravity;
	}

	public synchronized void save(){
		String isServiceRunning = "";
		String isCalibrated = "";
		String isAccountSent = "";
		String isWakeLockSet = "";
		String valueOfGravity = "";
		String[] standardDeviation = new String[3];
		if(this.isServiceRunning){
			isServiceRunning = "1";
		}else{
			isServiceRunning = "0";
		}
		if(this.isCalibrated){
			isCalibrated = "1";
		}else{
			isCalibrated = "0";
		}
		if(this.isAccountSent){
			isAccountSent = "1";
		}else{
			isAccountSent = "0";
		}
		if(this.isWakeLockSet){
			isWakeLockSet = "1";
		}else{
			isWakeLockSet = "0";
		}
		for(int i = 0; i < 3; i++){
			standardDeviation[i] = this.standardDeviation[i]+"";
		}
		valueOfGravity = this.valueOfGravity+"";
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("isServiceRunning", isServiceRunning);
		dbAdapter.updateToSelectedStartTable("isCalibrated", isCalibrated);
		dbAdapter.updateToSelectedStartTable("valueOfGravity", valueOfGravity);
		dbAdapter.updateToSelectedStartTable("sdX", standardDeviation[0]);
		dbAdapter.updateToSelectedStartTable("sdY", standardDeviation[1]);
		dbAdapter.updateToSelectedStartTable("sdZ", standardDeviation[2]);
		dbAdapter.updateToSelectedStartTable("isAccountSent", isAccountSent);
		dbAdapter.updateToSelectedStartTable("isWakeLockSet", isWakeLockSet);
		dbAdapter.close();
	}
	

	/**
	 * Set the background service running state
	 * @param value should be 1 if service is running, 0 otherwise.
	 */
	public synchronized void setServiceRunningState(boolean value){
		this.isServiceRunning = value;
	}
	
	/**
	 * Get the background service running state
	 * @return 1 if service is running, 0 otherwise.
	 */
	public synchronized boolean getServiceRunningState(){
		return this.isServiceRunning;
	}

	/**
	 * Set the calibration state
	 * @param value should be 1 if calibration is done, 0 otherwise.
	 */
	public synchronized void setCalibrationState(boolean value){
		this.isCalibrated = value;
	}

	/**
	 * Get the calibration state
	 * @return 1 if calibration is done, 0 otherwise.
	 */
	public synchronized boolean isCalibrated(){
		return this.isCalibrated;
	}

	/**
	 * Set the calibration value
	 * @param value calibration value
	 */
	public synchronized void setValueOfGravity(float value){
		this.valueOfGravity = value;
	}

	/**
	 * Get the calibration value
	 * @return return float data type of calibration value
	 */
	public synchronized float getValueOfGravity(){
		return this.valueOfGravity;
	}

	/**
	 * Set the standard deviation of X axis over certain amount of times
	 * @param value standard deviation of X axis
	 */
	public synchronized void setStandardDeviationX(float value){
		this.standardDeviation[0] = value;
	}

	/**
	 * Get the standard deviation of X axis
	 * @return float data type of standard deviation of X axis
	 */
	public synchronized float getStandardDeviationX(){
		return this.standardDeviation[0];
	}

	/**
	 * Set the standard deviation of Y axis over certain amount of times
	 * @param value standard deviation of Y axis
	 */
	public synchronized void setStandardDeviationY(float value){
		this.standardDeviation[1] = value;
	}

	/**
	 * Get the standard deviation of Y axis
	 * @return float data type of standard deviation of Y axis
	 */
	public synchronized float getStandardDeviationY(){
		return this.standardDeviation[1];
	}
	
	/**
	 * Set the standard deviation of Z axis over certain amount of times
	 * @param value standard deviation of Z axis
	 */
	public synchronized void setStandardDeviationZ(float value){
		this.standardDeviation[2] = value;
	}

	/**
	 * Get the standard deviation of Z axis
	 * @return float data type of standard deviation of Z axis
	 */
	public synchronized float getStandardDeviationZ(){
		return this.standardDeviation[2];
	}

	/**
	 * Set the posting account state 
	 * @param value should be 1 if account details is posted, 0 otherwise
	 */
	public synchronized void setAccountState(boolean value){
		this.isAccountSent = value;
	}

	/**
	 * Get the posting account state 
	 * @return 1 if account details is posted, 0 otherwise
	 */
	public synchronized boolean isAccountSent(){
		return this.isAccountSent;
	}

	/**
	 * Set the Wake Lock state
	 * @param value should be 1 if wake lock is set, 0 otherwise
	 */
	public synchronized void setWakeLockState(boolean value){
		this.isWakeLockSet = value;
	}

	/**
	 * Get the Wake Lock state
	 * @return 1 if wake lock is set, 0 otherwise
	 */
	public synchronized boolean isWakeLockSet(){
		return this.isWakeLockSet;
	}
}
