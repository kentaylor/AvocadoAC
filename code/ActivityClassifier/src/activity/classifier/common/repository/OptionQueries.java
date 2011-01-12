package activity.classifier.common.repository;

import android.content.Context;

public class OptionQueries extends Queries {

	private DbAdapter dbAdapter;
	
	public OptionQueries(Context context){
		super(context);
		dbAdapter = super.dbAdapter;
	}
	
	public void setServiceRunningState(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("isServiceRunning", value);
		dbAdapter.close();
	}
	
	public int getServiceRunningState(){
		int isServiceRunning;
		dbAdapter.open();
		isServiceRunning = dbAdapter.fetchFromStartTableInt("isServiceRunning");
		dbAdapter.close();
		
		return isServiceRunning;
	}
	
	public void setCalibrationState(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("isCalibrated", value);
		dbAdapter.close();
	}
	
	public int getCalibrationState(){
		int isCalibrated;
		dbAdapter.open();
		isCalibrated = dbAdapter.fetchFromStartTableInt("isCalibrated");
		dbAdapter.close();
		
		return isCalibrated;
	}
	
	public void setCalibrationValue(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("CalibrationValue", value);
		dbAdapter.close();
	}
	
	public float getCalibrationValue(){
		float CalibrationValue;
		dbAdapter.open();
		CalibrationValue = dbAdapter.fetchFromStartTableFloat("CalibrationValue");
		dbAdapter.close();
		
		return CalibrationValue;
	}
	
	public void setStandardDeviationX(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("sdX", value);
		dbAdapter.close();
	}
	
	public float getStandardDeviationX(){
		float sdX;
		dbAdapter.open();
		sdX = dbAdapter.fetchFromStartTableFloat("sdX");
		dbAdapter.close();
		
		return sdX;
	}
	
	public void setStandardDeviationY(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("sdY", value);
		dbAdapter.close();
	}
	
	public float getStandardDeviationY(){
		float sdY;
		dbAdapter.open();
		sdY = dbAdapter.fetchFromStartTableFloat("sdY");
		dbAdapter.close();
		
		return sdY;
	}
	
	public void setStandardDeviationZ(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("sdZ", value);
		dbAdapter.close();
	}
	
	public float getStandardDeviationZ(){
		float sdZ;
		dbAdapter.open();
		sdZ = dbAdapter.fetchFromStartTableFloat("sdZ");
		dbAdapter.close();
		
		return sdZ;
	}
	
	public void setAccountState(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("isAccountSent", value);
		dbAdapter.close();
	}
	
	public int getAccountState(){
		int isAccountSent;
		dbAdapter.open();
		isAccountSent = dbAdapter.fetchFromStartTableInt("isAccountSent");
		dbAdapter.close();
		
		return isAccountSent;
	}
	
	public void setWakeLockState(String value){
		dbAdapter.open();
		dbAdapter.updateToSelectedStartTable("isWakeLockSet", value);
		dbAdapter.close();
	}
	
	public int getWakeLockState(){
		int isWakeLockSet;
		dbAdapter.open();
		isWakeLockSet = dbAdapter.fetchFromStartTableInt("isWakeLockSet");
		dbAdapter.close();
		
		return isWakeLockSet;
	}
}
