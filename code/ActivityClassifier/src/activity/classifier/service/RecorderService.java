/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

package activity.classifier.service;

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

import activity.classifier.R;
import activity.classifier.R.raw;
import activity.classifier.accel.AccelReader;
import activity.classifier.accel.AccelReaderFactory;
import activity.classifier.accel.Sampler;
import activity.classifier.activity.ActivityRecorderActivity;
import activity.classifier.aggregator.Aggregator;
import activity.classifier.model.ModelReader;
import activity.classifier.repository.ActivityQueries;
import activity.classifier.repository.OptionQueries;
import activity.classifier.rpc.ActivityRecorderBinder;
import activity.classifier.rpc.Classification;
import activity.classifier.service.threads.AccountThread;
import activity.classifier.service.threads.UploadActivityHistoryThread;
import activity.classifier.utils.PhoneInfo;
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
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.widget.Toast;

/**
 * RecorderService is main background service. This service uses broadcast to
 * get the information of charging status and screen status. The battery status
 * is sent to ClassifierService to determine Charging state. The screen status
 * is used when turns the screen on during sampling if Screen Lock setting is
 * on.
 * 
 * It calls Sampler and AccelReader to sample for 6.4 sec (128 sample point
 * every 50 msec), and it repeats every 30 sec.
 * 
 * Update activity history to web server every 5 min. If there is bad internet
 * connection, then it does not send them and waits for next time.
 * 
 * @author chris, modified by Justin
 * 
 * 
 */
public class RecorderService extends Service {
	
	private AccelReader reader;
	private Sampler sampler;
	private final Aggregator aggregator = new Aggregator();
	
	private String chargingState = "";
	
	private final Handler handler = new Handler();
	
	private int isWakeLockSet;
	
	private PowerManager.WakeLock PARTIAL_WAKE_LOCK_MANAGER;
	private PowerManager.WakeLock SCREEN_DIM_WAKE_LOCK_MANAGER;
	
	private float[] ignore = { 0 };
	
	private UploadActivityHistoryThread uploadActivityHistory;
	
	private OptionQueries optionQuery;
	private ActivityQueries activityQuery;
	
	private boolean running;
	
	public static Map<Float[], String> model;
	
	private final List<Classification> classifications = new ArrayList<Classification>();
	
	private Boolean SCREEN_DIM_WAKE_LOCK_MANAGER_IsAcquired = false;
	private Boolean PARTIAL_WAKE_LOCK_MANAGER_IsAcquired = false;
	
	private PowerManager pm;
	
	private final List<Classification> adapter = new ArrayList<Classification>();
	private String lastAc = "NONE";
	
	private PhoneInfo phoneInfo;
	
	/**
	 * broadcastReceiver that receive phone's screen state
	 */
	private BroadcastReceiver myScreenReceiver = new BroadcastReceiver() {
		
		public void onReceive(Context arg0, Intent arg1) {
			
			if (arg1.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.i("screen", "off");
				if (!PARTIAL_WAKE_LOCK_MANAGER_IsAcquired) {
					SCREEN_DIM_WAKE_LOCK_MANAGER_IsAcquired = false;
					applyWakeLock(true);
				}
			} else
				if (arg1.getAction().equals(Intent.ACTION_SCREEN_ON)) {
					Log.i("screen", "on");
				}
		}
	};
	
	/**
	 * Broadcast receiver for battery manager
	 */
	private BroadcastReceiver myBatteryReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			int status = arg1.getIntExtra("plugged", -1);
			if (status != 0) {
				chargingState = "Charging";
				Log.i("charging", "charging");
			} else {
				chargingState = "NotCharging";
				Log.i("charging", "notcharging");
			}
		}
	};
	
	/**
	 * Performs screen wake lock depends on the screen on/off
	 * 
	 * @param wakelock
	 */
	private void applyWakeLock(boolean wakelock) {
		/*
		 * if wake lock is set, PARTIAL_WAKE_LOCK is released, then use
		 * SCREEN_DIM_WAKE_LOCK to turn the screen on.
		 */
		if (wakelock) {
			if (PARTIAL_WAKE_LOCK_MANAGER != null) {
				PARTIAL_WAKE_LOCK_MANAGER.release();
				PARTIAL_WAKE_LOCK_MANAGER = null;
				PARTIAL_WAKE_LOCK_MANAGER_IsAcquired = false;
				Log.i("Wakelock", "PARTIAL_WAKE_LOCK_MANAGER is released");
			}
			if (!SCREEN_DIM_WAKE_LOCK_MANAGER_IsAcquired) {
				SCREEN_DIM_WAKE_LOCK_MANAGER = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.SCREEN_DIM_WAKE_LOCK, "screen onon");
				SCREEN_DIM_WAKE_LOCK_MANAGER.acquire();
				SCREEN_DIM_WAKE_LOCK_MANAGER_IsAcquired = true;
				Log.i("Wakelock", "SCREEN_DIM_WAKE_LOCK_MANAGE is 2acquired");
			}
		} else {
			if (!PARTIAL_WAKE_LOCK_MANAGER_IsAcquired) {
				PARTIAL_WAKE_LOCK_MANAGER = pm.newWakeLock(	PowerManager.PARTIAL_WAKE_LOCK,
															"Activity recorder");
				PARTIAL_WAKE_LOCK_MANAGER.acquire();
				PARTIAL_WAKE_LOCK_MANAGER_IsAcquired = true;
				Log.i("Wakelock", "PARTIAL_WAKE_LOCK_MANAGER is acquired");
			}
			if (SCREEN_DIM_WAKE_LOCK_MANAGER != null) {
				SCREEN_DIM_WAKE_LOCK_MANAGER.release();
				SCREEN_DIM_WAKE_LOCK_MANAGER = null;
				SCREEN_DIM_WAKE_LOCK_MANAGER_IsAcquired = false;
			}
		}
	}
	
	/**
	 * when the connection is established, some information such as
	 * classification name, service running state, wake lock state, phone
	 * information are passed in this RecorderService.
	 */
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
		
		public void setWakeLock(boolean wakelock) throws RemoteException {
			applyWakeLock(wakelock);
		}
	};
	
	private final Runnable registerRunnable = new Runnable() {
		
		public void run() {
			// Log.i(getClass().getName(), "Registering");
			sampler.start();
			
			handler.postDelayed(registerRunnable, 30000);
		}
		
	};
	
	private final Runnable analyseRunnable = new Runnable() {
		
		public void run() {
			final Intent intent = new Intent(RecorderService.this, ClassifierService.class);
			if (ignore[0] <= 1) {
				ignore[0]++;
			}
			intent.putExtra("data", sampler.getData());
			intent.putExtra("status", chargingState);
			intent.putExtra("size", sampler.getSize());
			intent.putExtra("ignore", ignore);
			intent.putExtra("LastClassificationName",
							(!classifications.isEmpty()	? classifications.get(	classifications.size() - 1).getNiceClassification()
														: null));
			
			startService(intent);

		}
		
	};
	
	/**
     * 
     */
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	/**
     * 
     */
	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);
		Log.i("RecorderService", "Strated!!");
		
		running = true;
		
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		// receive phone battery status
		this.registerReceiver(	this.myBatteryReceiver,
								new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		this.registerReceiver(this.myScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		this.registerReceiver(this.myScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		
		phoneInfo = new PhoneInfo(this);
		activityQuery = new ActivityQueries(this);
		optionQuery = new OptionQueries(this);
		
		optionQuery.setServiceRunningState("1");
		
		uploadActivityHistory = new UploadActivityHistoryThread(this, activityQuery, phoneInfo);
		
		isWakeLockSet = optionQuery.getWakeLockState();
		
		if (isWakeLockSet == 0) {
			applyWakeLock(false);
		} else {
			applyWakeLock(true);
		}
		
		/*
		 * if the background service is dead somehow last time, there is no clue when the service is finished.
		 * Check the last activity name whether it's finished properly or not by the activity name "END",
		 * then if it was not "END", then insert "END" data into the database with the end time of the last activity. 
		 */
		int count = activityQuery.getSizeOfTable()+1;
        Log.i("countActivityTable","#"+count);
        if(count > 1){
	    	String lastActivity = activityQuery.getItemNameFromActivityTable(count);
	    	if(!lastActivity.equals("END")){
	    		String endDate = activityQuery.getItemEndDateFromActivityTable(count);
	    		
	    		activityQuery.insertActivities("END", endDate, 0);
	    	}
        }
        
        
		reader = new AccelReaderFactory().getReader(this);
		sampler = new Sampler(handler, reader, analyseRunnable);
		
		init();
		
		// if the account wasn't previously sent,
		// start a thread to register the account.
		if (optionQuery.getAccountState() == 0) {
			AccountThread registerAccount = new AccountThread(this, phoneInfo,
																				optionQuery);
			registerAccount.start();
		}
		
		// start to upload un-posted activities to Web server
		uploadActivityHistory.startUploads();
	}
	
	public void init() {
		
		model = ModelReader.getModel(this, R.raw.basic_model);
		
		handler.postDelayed(registerRunnable, 1000);
		
		// classifications.add(new Classification("CLASSIFIED/WAITING",
		// System.currentTimeMillis()));
		// classifications.add(new Classification("",
		// System.currentTimeMillis()));
		
	}
	
	/**
	 * 
	 * @throws ParseException
	 */
	void InsertNewActivity() throws ParseException {
    	try {
    		String activity = classifications.get(classifications.size()-1).withContext(this).getNiceClassification();
    		String startDate  = classifications.get(classifications.size()-1).getStartTime();
    		
    		if(activity!=null && !lastAc.equals(activity)){
    			Log.i("classification",activity);
    			int count = activityQuery.getSizeOfTable()+1;
    			activityQuery.updateNewItems(count, startDate);
    			activityQuery.insertActivities(activity, startDate, 0);
    			
    			
    		}else{
    			int count = activityQuery.getSizeOfTable()+1;
    			activityQuery.updateNewItems(count, classifications.get(classifications.size()-1).getEndTime());
    		}
    		
    		lastAc=activity;
	    } catch (Exception ex) {
	    	Log.e(getClass().getName(), "Unable to get service state", ex);
	    } 
		
	}
	
	void updateScores(final String classification) {
		aggregator.addClassification(classification);
		if (!aggregator.getClassification().equals("CLASSIFIED/WAITING")) {
			final String best = aggregator.getClassification();
			String[] cl = classification.split("/");
			
			if (!classifications.isEmpty()
					&& best.equals(classifications.get(classifications.size() - 1).getClassification())) {
				classifications.get(classifications.size() - 1).updateEnd(	System.currentTimeMillis());
			} else {
				classifications.add(new Classification(best, System.currentTimeMillis()));
			}
			// }
		}
		try {
			InsertNewActivity();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (running) {
			Log.i("Ondestroy", "HERE");
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z z");
			String startTime = dateFormat.format(date);
			// save message "END" to recognise when the background service is
			// finished.
			activityQuery.insertActivities("END", startTime, 0);
			
			ActivityRecorderActivity.serviceIsRunning = false;
			
			running = false;
			if (sampler != null) {
				sampler.stop();
			}
			ignore[0] = 0;
			optionQuery.setServiceRunningState("0");
			handler.removeCallbacks(registerRunnable);
			this.unregisterReceiver(myBatteryReceiver);
			this.unregisterReceiver(myScreenReceiver);
			uploadActivityHistory.cancelUploads();
			
			if (PARTIAL_WAKE_LOCK_MANAGER != null) {
				PARTIAL_WAKE_LOCK_MANAGER.release();
				PARTIAL_WAKE_LOCK_MANAGER = null;
			}
			if (SCREEN_DIM_WAKE_LOCK_MANAGER != null) {
				SCREEN_DIM_WAKE_LOCK_MANAGER.release();
				SCREEN_DIM_WAKE_LOCK_MANAGER = null;
			}
			
		}
	}
	
}
