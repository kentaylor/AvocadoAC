/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import activity.classifier.common.ModelReader;
import activity.classifier.common.accel.AccelReader;
import activity.classifier.common.accel.AccelReaderFactory;
import activity.classifier.common.accel.Sampler;
import activity.classifier.common.aggregator.Aggregator;
import activity.classifier.rpc.ActivityRecorderBinder;
import activity.classifier.rpc.Classification;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 *
 * @author chris
 * modified by Justin
 * 
 * RecorderService is main background service.
 * This service uses broadcast to get the information of charging status and screen status.
 * The battery status is sent to ClassifierService to determine Charging state.
 * The screen status is used when turns the screen on during sampling if Screen Lock setting is on.
 * 
 * It calls Sampler and AccelReader to sample for 6.4 sec (128 sample point every 50 msec), and it repeats every 30 sec.
 * 
 * Update activity history to web server every 5 min.
 * If there is bad internet connection, then it does not send them and waits for next time.
 * 
 *
 */
public class RecorderService extends Service {
    AccelReader reader;
    Sampler sampler;
	final Aggregator aggregator = new Aggregator();

    public String strStatus="";
    private String MODEL="";
    
    Sensor mSensor;
    private final Handler handler = new Handler();
    
    private String toastString;
    private String AccountName;
    private String ModelName;
    private String IMEI;
    private float[] ssd = new float[3];
    private int service = 0;
    private int isAccountSent = 0;
    private Timer timer;
    private SensorManager manager;
    private PowerManager.WakeLock wl;
    private PowerManager.WakeLock wl2;
    private PowerManager.WakeLock wl3;
    private static float[] ignore={0};
    private float[] data = new float[384];
    private UploadActivityHistory uploadActivityHistory;
    
    private OptionQueries optionQuery;
    private ActivityQueries activityQuery;
//    private boolean possibly_uncarried;
//    private boolean keepLastAvgAccel;
//    private float[] lastaverage = new float[3];
    
    boolean running;
   
    public static Map<Float[], String> model;
    private final List<Classification> classifications = new ArrayList<Classification>();

    private boolean wakelock;
    private Boolean wl2IsAcquired=false;
    private Boolean wlIsAcquired=false;
    PowerManager pm1;

    
    private BroadcastReceiver myScreenReceiver = new BroadcastReceiver(){
    	
    	public void onReceive(Context arg0, Intent arg1) {
    		
    		if (arg1.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
    			Log.i("screen","off");
    		    wl2IsAcquired=false;
    		}else if(arg1.getAction().equals(Intent.ACTION_SCREEN_ON)) {
    			Log.i("screen","on");
    		}
    	}
    };
  //Broadcast receiver for battery manager
    private BroadcastReceiver myBatteryReceiver = new BroadcastReceiver(){

		  @Override
		  public void onReceive(Context arg0, Intent arg1) {
			  int status = arg1.getIntExtra("plugged", -1);
		      if (status != 0  ){
		    	  strStatus = "Charging";
		    	  Log.i("charging","charging");
		      }else{
		    	  strStatus = "NotCharging";
		    	  Log.i("charging","notcharging");
		      }
		  }
    };
    public void setWake(boolean wakelock){
    	Log.i("setwake","ok");
    	if(this.wakelock!=wakelock){
	    	this.wakelock=wakelock;
    	}
    }
    
//    public void setPossiblyUncarried(boolean possibly_uncarried){
//    	this.possibly_uncarried = possibly_uncarried;
//    }
//    
//    public void setLastAvgAccelToZero(boolean keepLastAvgAccel){
//    	this.keepLastAvgAccel = keepLastAvgAccel;
//    }
//    
//    public void setLastAvgAccel(float LastAvgAccelX, float LastAvgAccelY,
//			float LastAvgAccelZ){
//    	this.lastaverage[0] = LastAvgAccelX;
//    	this.lastaverage[1] = LastAvgAccelY;
//    	this.lastaverage[2] = LastAvgAccelZ;
//    }
    public void setAccountStateToastString(String toastString){
    	this.toastString = toastString;
    }
    public void setPhoneInfo(String AccountName, String ModelName, String IMEI){
    	this.AccountName = AccountName;
    	this.ModelName = ModelName;
    	this.IMEI = IMEI;
    }
    private final ActivityRecorderBinder.Stub binder = new ActivityRecorderBinder.Stub() {

        public void submitClassification(String classification) throws RemoteException {
            Log.i(getClass().getName(), "Received classification: " + classification);
            updateScores(classification);

        }

        public List<Classification> getClassifications() throws RemoteException {
            return classifications;
        }

        public boolean isRunning() throws RemoteException {
            return running;
        }
        
        public void SetWakeLock(boolean wakelock)throws RemoteException{
			setWake(wakelock);
        }

//		public void IsPossiblyUncarried(boolean possibly_uncarried)
//				throws RemoteException {
//			setPossiblyUncarried(possibly_uncarried);			
//		}
//
//		public void SetLastAvgAccelToZero(boolean keepLastAvgAccel)
//				throws RemoteException {
//			setLastAvgAccelToZero(keepLastAvgAccel);	
//		}
//
//		public void SetLastAvgAccel(float LastAvgAccelX, float LastAvgAccelY,
//				float LastAvgAccelZ) throws RemoteException {
//			setLastAvgAccel(LastAvgAccelX,LastAvgAccelY,LastAvgAccelZ);
//			
//		}

		public void SetAccountStateToastString(String toastString)
				throws RemoteException {
			setAccountStateToastString(toastString);
   	    	Toast.makeText(RecorderService.this, toastString, Toast.LENGTH_LONG).show();
		}

		public void SetPhoneInformation(String AccountName, String ModelName, String IMEI)
				throws RemoteException {
	        Log.i("PhoneInfo","0 "+AccountName);
	    	Log.i("PhoneInfo","0 "+ModelName);
	    	Log.i("PhoneInfo","0 "+IMEI);
			setPhoneInfo(AccountName, ModelName, IMEI);	
			registerAccount(isAccountSent,AccountName,ModelName,IMEI);
			uploadActivityHistory.uploadDataToWeb(AccountName);
		}
    };

    private final Runnable registerRunnable = new Runnable() {

        public void run() {
            //Log.i(getClass().getName(), "Registering");
        	sampler.start();

       		handler.postDelayed(registerRunnable, 30000);
        }

    };
    private final Runnable analyseRunnable = new Runnable() {

        public void run() {
            final Intent intent = new Intent(RecorderService.this, ClassifierService.class);
            if(ignore[0]<=1){
            	ignore[0]++;
            }
            intent.putExtra("data", sampler.getData());
            intent.putExtra("status", strStatus);
            intent.putExtra("size", sampler.getSize());
            intent.putExtra("ignore", ignore);
            intent.putExtra("wake", wakelock);
            intent.putExtra("LastClassificationName", (!classifications.isEmpty() ? classifications.get(classifications.size() - 1).getNiceClassification() : null));
//            intent.putExtra("possibly_uncarried", possibly_uncarried);
//            intent.putExtra("keepLastAvgAccel", keepLastAvgAccel);
//            intent.putExtra("lastaverage", lastaverage);
//            Log.i("ExtraInRecorderService","possibly_uncarried: "+possibly_uncarried+"");
//            Log.i("ExtraInRecorderService","keepLastAvgAccel  : "+keepLastAvgAccel+"");
//            Log.i("ExtraInRecorderService",lastaverage[0]+" "+lastaverage[1]+" "+lastaverage[2]+" ");
            startService(intent);
        	try {
				updateButton();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

    };
    private final Runnable screenRunnable = new Runnable() {

        public void run() {
        	if(wakelock){
        		if(wl!=null){
        			wl.release();
        			wl=null;
        			wlIsAcquired=false;
        			Log.i("newtimer","WLreleased");
        		}
	            if(!wl2IsAcquired ){
	            	wl2=pm1.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.SCREEN_DIM_WAKE_LOCK, "screen onon");
		            wl2.acquire();
		            wl2IsAcquired=true;
		            Log.i("newtimer","WL2acquired");
	            }
        	}
        	else{
	            if(!wlIsAcquired){
	            	wl = pm1.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Activity recorder");
		            wl.acquire();
		            wlIsAcquired=true;
		            Log.i("newtimer","WLacquired");
	            }
	            if(wl2!=null){
	            	wl2.release();
	            	wl2=null;
	            }
        	}
            handler.postDelayed(screenRunnable, 5000);
        }
        
    };




    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);
        Log.i("RecorderService","Strated!!");
        service = 1;

        running = true;
        pm1 = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //receive phone battery status
        this.registerReceiver(this.myBatteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        this.registerReceiver(this.myScreenReceiver,
                new IntentFilter(Intent.ACTION_SCREEN_OFF));
        this.registerReceiver(this.myScreenReceiver,
                new IntentFilter(Intent.ACTION_SCREEN_ON));
        Log.i("PhoneInfo","1 "+AccountName);
    	Log.i("PhoneInfo","1 "+ModelName);
    	Log.i("PhoneInfo","1 "+IMEI);
        getPhoneInfo();
        Log.i("PhoneInfo","2 "+AccountName);
    	Log.i("PhoneInfo","2 "+ModelName);
    	Log.i("PhoneInfo","2 "+IMEI);
        uploadActivityHistory = new UploadActivityHistory(this);
        activityQuery = new ActivityQueries(this);
        optionQuery = new OptionQueries(this);
        optionQuery.setServiceRunningState("1");
        isAccountSent = optionQuery.getAccountState();

        reader = new AccelReaderFactory().getReader(this);
        sampler = new Sampler(handler, reader, analyseRunnable);
        
//        for(int i = 0; i < 3; i++){
//        	lastaverage[i]=0;
//        }
//        possibly_uncarried = false;
//        keepLastAvgAccel = false;
        
        
        
        
        Log.i("Account",isAccountSent+"");
        init();
        
    }
    private void getPhoneInfo(){
    	Intent intent = new Intent(RecorderService.this, PhoneInfoService.class);
    	Log.i("PhoneInfo","Accessed");
    	startService(intent);
    }
	private void registerAccount(int isAccountSent, String AccountName, String ModelName, String IMEI){
        if(isAccountSent==0){
        	Intent intent = new Intent(RecorderService.this, AccountService.class);
        	intent.putExtra("AccountName", AccountName);
        	intent.putExtra("ModelName", ModelName);
        	intent.putExtra("IMEI", IMEI);
        	startService(intent);
        }
	}
    
    public static void copy( String targetFile, String copyFile ){
    	try {
    		InputStream lm_oInput = new FileInputStream(new File(targetFile));
    		byte[] buff = new byte[ 128 ];
    		FileOutputStream lm_oOutPut = new FileOutputStream( copyFile );
    		while(true){
    			int bytesRead = lm_oInput.read( buff );
    			if( bytesRead == -1 ) break;
    			lm_oOutPut.write( buff, 0, bytesRead );
    		}

    		lm_oInput.close();
    		lm_oOutPut.close();
    		lm_oOutPut.flush();
    		lm_oOutPut.close();
    	}catch( Exception e ){
    	}
    }

    @SuppressWarnings("unchecked")
    public void init() {
    	String dbfile ="data/data/activity.classifier/files/activityrecords.db";
    	copy("data/data/activity.classifier/databases/activityrecords.db",dbfile);
    	
        
    	model = ModelReader.getModel(this, R.raw.basic_model);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        handler.postDelayed(registerRunnable, 1000);
//        handler.postDelayed(updateRunnable, 500);

        handler.postDelayed(screenRunnable, 1000);
//        classifications.add(new Classification("CLASSIFIED/WAITING", System.currentTimeMillis(),service));
//        classifications.add(new Classification("", System.currentTimeMillis(),service));
        
    }


    
    final List<Classification> adapter = new ArrayList<Classification>();
    String lastAc = "NONE";
	
    void updateButton() throws ParseException {
    	try {
    		if (classifications.isEmpty()) {
    			adapter.clear();
	        }else{
	        	if (!adapter.isEmpty()) {
	        		final Classification myLast = adapter.get(adapter.size()-1);
		            final Classification expected = classifications.get(classifications.size() - 1);
	
		            if (!myLast.getClassification().equals(expected.getClassification())) {
		            	// Just update the end time
		                adapter.add(expected);
		            } 
		        }else if(adapter.isEmpty()){
		        	adapter.add(classifications.get(0));
		            Log.i("Empty?","yes");
		        }
	            	String activity = adapter.get(adapter.size() - 1).getNiceClassification();
		            String newAc = activity;

		            if(!lastAc.equals(newAc)){
		            	Log.i("lastAc",lastAc);
			            Log.i("newAc",newAc);
			                
			            String date = adapter.get(adapter.size()-1).getStartTime();
			            activityQuery.insertActivities(activity, date, 0);
		            }
		            lastAc = newAc;
	        }	           

	    } catch (Exception ex) {
	    	Log.e(getClass().getName(), "Unable to get service state", ex);
	    } 
	        
	}


    
    //this is for Chris's classification ()
    void updateScores(final String classification) {
    	aggregator.addClassification(classification);
        if(!aggregator.getClassification().equals("CLASSIFIED/WAITING")){
	        final String best = aggregator.getClassification();
	        String[] cl = classification.split("/");
	        
		        if (!classifications.isEmpty() && best.equals(classifications
		                    .get(classifications.size() - 1).getClassification())) {
		            classifications.get(classifications.size() - 1).updateEnd(System.currentTimeMillis());
		        } else {
		            classifications.add(new Classification(best, System.currentTimeMillis(),service));
		        }
//            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        classifications.add(new Classification("CLASSIFIED/END", System.currentTimeMillis(),service));
        if (running) {
        	Log.i("Ondestroy","HERE");
        	ActivityRecorderActivity.serviceIsRunning=false;

            running = false;
            if (sampler != null) {
                sampler.stop();
            }
            ignore[0] = 0;
            service = 0;
//            dbAdapter.open();
//            dbAdapter.updateToSelectedStartTable("isServiceRunning", 0+"");
//            dbAdapter.close();
            optionQuery.setServiceRunningState("0");
            handler.removeCallbacks(registerRunnable);
//            handler.removeCallbacks(updateRunnable);
            handler.removeCallbacks(screenRunnable);
            this.unregisterReceiver(myBatteryReceiver);
            this.unregisterReceiver(myScreenReceiver);
            uploadActivityHistory.CancelTimer();
           
//            dbAdapter.close();
            if(wl!=null){
                wl.release();
                wl=null;
            }
            if(wl2!=null){
                wl2.release();
                wl2=null;
            }
            if(wl3!=null){
                wl3.release();
                wl3=null;
            }
        }
    }

}
