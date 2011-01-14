package activity.classifier.common.repository;

import android.content.Context;

/**
 * A utility class which extends superclass {@link Queries} 
 * for handling queries to get the system information.
 * 
 * @author Justin Lee
 *
 */
public class OptionQueries extends Queries {

	private DbAdapter dbAdapter;
	
	/**
	 * @see Queries
	 * @param context context from Activity or Service classes 
	 */
	public OptionQueries(Context context){
		super(context);
		dbAdapter = super.dbAdapter;
	}
	
	/**
	 * Set the background service running state
	 * @param value should be 1 if service is running, 0 otherwise.
	 */
	public void setServiceRunningState(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("isServiceRunning", value);
		dbAdapter.close();
	}
	
	/**
	 * Get the background service running state
	 * @return 1 if service is running, 0 otherwise.
	 */
	public int getServiceRunningState(){
		int isServiceRunning;
		dbAdapter.open();
		isServiceRunning = dbAdapter.fetchFromStartTableInt("isServiceRunning");
		dbAdapter.close();
		
		return isServiceRunning;
	}
	
	/**
	 * Set the calibration state
	 * @param value should be 1 if calibration is done, 0 otherwise.
	 */
	public void setCalibrationState(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("isCalibrated", value);
		dbAdapter.close();
	}
	
	/**
	 * Get the calibration state
	 * @return 1 if calibration is done, 0 otherwise.
	 */
	public int getCalibrationState(){
		int isCalibrated;
		dbAdapter.open();
		isCalibrated = dbAdapter.fetchFromStartTableInt("isCalibrated");
		dbAdapter.close();
		
		return isCalibrated;
	}
	
	/**
	 * Set the calibration value
	 * @param value calibration value
	 */
	public void setCalibrationValue(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("CalibrationValue", value);
		dbAdapter.close();
	}
	
	/**
	 * Get the calibration value
	 * @return return float data type of calibration value
	 */
	public float getCalibrationValue(){
		float CalibrationValue;
		dbAdapter.open();
		CalibrationValue = dbAdapter.fetchFromStartTableFloat("CalibrationValue");
		dbAdapter.close();
		
		return CalibrationValue;
	}
	
	/**
	 * Set the standard deviation of X axis over certain amount of times
	 * @param value standard deviation of X axis
	 */
	public void setStandardDeviationX(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("sdX", value);
		dbAdapter.close();
	}
	
	/**
	 * Get the standard deviation of X axis
	 * @return float data type of standard deviation of X axis
	 */
	public float getStandardDeviationX(){
		float sdX;
		dbAdapter.open();
		sdX = dbAdapter.fetchFromStartTableFloat("sdX");
		dbAdapter.close();
		
		return sdX;
	}
	
	/**
	 * Set the standard deviation of Y axis over certain amount of times
	 * @param value standard deviation of Y axis
	 */
	public void setStandardDeviationY(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("sdY", value);
		dbAdapter.close();
	}
	
	/**
	 * Get the standard deviation of Y axis
	 * @return float data type of standard deviation of Y axis
	 */
	public float getStandardDeviationY(){
		float sdY;
		dbAdapter.open();
		sdY = dbAdapter.fetchFromStartTableFloat("sdY");
		dbAdapter.close();
		
		return sdY;
	}
	
	/**
	 * Set the standard deviation of Z axis over certain amount of times
	 * @param value standard deviation of Z axis
	 */
	public void setStandardDeviationZ(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("sdZ", value);
		dbAdapter.close();
	}
	
	/**
	 * Get the standard deviation of Z axis
	 * @return float data type of standard deviation of Z axis
	 */
	public float getStandardDeviationZ(){
		float sdZ;
		dbAdapter.open();
		sdZ = dbAdapter.fetchFromStartTableFloat("sdZ");
		dbAdapter.close();
		
		return sdZ;
	}
	
	/**
	 * Set the posting account state 
	 * @param value should be 1 if account details is posted, 0 otherwise
	 */
	public void setAccountState(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("isAccountSent", value);
		dbAdapter.close();
	}
	
	/**
	 * Get the posting account state 
	 * @return 1 if account details is posted, 0 otherwise
	 */
	public int getAccountState(){
		int isAccountSent;
		dbAdapter.open();
		isAccountSent = dbAdapter.fetchFromStartTableInt("isAccountSent");
		dbAdapter.close();
		
		return isAccountSent;
	}
	
	/**
	 * Set the Wake Lock state
	 * @param value should be 1 if wake lock is set, 0 otherwise
	 */
	public void setWakeLockState(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("isWakeLockSet", value);
		dbAdapter.close();
	}
	
	/**
	 * Get the Wake Lock state
	 * @return 1 if wake lock is set, 0 otherwise
	 */
	public int getWakeLockState(){
		int isWakeLockSet;
		dbAdapter.open();
		isWakeLockSet = dbAdapter.fetchFromStartTableInt("isWakeLockSet");
		dbAdapter.close();
		
		return isWakeLockSet;
	}
}
