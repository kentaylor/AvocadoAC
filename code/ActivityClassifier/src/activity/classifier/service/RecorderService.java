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
import activity.classifier.accel.SampleBatch;
import activity.classifier.accel.SampleBatchBuffer;
import activity.classifier.accel.Sampler;
import activity.classifier.accel.SimpleSampler;
import activity.classifier.activity.ActivityRecorderActivity;
import activity.classifier.aggregator.Aggregator;
import activity.classifier.common.Constants;
import activity.classifier.model.ModelReader;
import activity.classifier.repository.ActivityQueries;
import activity.classifier.repository.OptionQueries;
import activity.classifier.rpc.ActivityRecorderBinder;
import activity.classifier.rpc.Classification;
import activity.classifier.service.threads.AccountThread;
import activity.classifier.service.threads.ClassifierThread;
import activity.classifier.service.threads.UploadActivityHistoryThread;
import activity.classifier.utils.PhoneInfo;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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
public class RecorderService extends Service implements Runnable {
	
	private AccelReader reader;
	private Sampler sampler;
	private final Aggregator aggregator = new Aggregator();
	
	private boolean charging = false;
	
	private Looper threadLooper = null;
	private Handler handler = null;
	
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
	
	private SampleBatchBuffer batchBuffer;
	private ClassifierThread classifierThread;
	private AccountThread registerAccountThread;
	
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
				charging = true;
				Log.i(Constants.DEBUG_TAG, "charging");
			} else {
				charging = false;
				Log.i(Constants.DEBUG_TAG, "not charging");
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
		
		private Handler mainLooperHandler = null;
		
		//	To use that main thread's looper, we get a reference to it, and create a handler.
		//	Later any events that require the current thread to have a looper,
		//	(e.g. toasts) can post as a <code>Runnable</code> and the <code>Runnable</code>
		//	would be executed in the main thread.
		private Handler getMainLooperHandler() {
			
			if (mainLooperHandler!=null) {
				return mainLooperHandler;
			}
			else {
				Looper mainLooper = RecorderService.this.getMainLooper();
				
				if (mainLooper==null)
					return null;
				
				mainLooperHandler = new Handler(mainLooper);
			}
			
			return mainLooperHandler;
		}
		
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
		
		public void showServiceToast(final String message) {
			Handler mainLooperHandler = getMainLooperHandler();
			
			mainLooperHandler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RecorderService.this, message, Toast.LENGTH_LONG).show();
				}
			});
		}
	};
	
	private final Runnable registerRunnable = new Runnable() {
		
		public void run() {
			
			//	if the sampler is not sampling...
			if (!sampler.isSampling()) {
				//	take an empty batch and give it to the sampler to sample...
				try {
					//	to make the sample timing more accurate,
					//		we invoke the gc before we start sampling,
					//		in the hopes that it wont happen in the sampling
					//		period.
					System.gc();
					
//					Log.v(Constants.DEBUG_TAG, "Sending an empty batch for sampling.");
					
					//	please note that this function blocks until an empty batch is found
					//	which is why it is important to start with a sufficient number of batches
					//	and process the batches as fast as possible
					SampleBatch batch = batchBuffer.takeEmptyBatch();
					sampler.start(batch);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			//	run next batch after some time
			handler.postDelayed(registerRunnable, Constants.DELAY_SAMPLE_BATCH);
		}
		
	};
	
	//	called by the sampler when sampling a batch of samples is done.
	private final Runnable analyseRunnable = new Runnable() {
		
		public void run() {
//			Log.v(Constants.DEBUG_TAG, "Sampling done. Sending batch for analysis.");
			
			//	get the sample batch from the sampler
			SampleBatch sample = sampler.getSampleBatch();
			
			if (ignore[0] <= 1) {
				ignore[0]++;
			}
			
			//	set any required properties
			sample.setCharging(charging);
			sample.setIgnore(ignore);
			sample.setLastClassificationName(
					(!classifications.isEmpty()) ?
							classifications.get(classifications.size() - 1).getNiceClassification()
							: null );
			
			//	put it back into the buffer as a filled batch
			try {
				batchBuffer.returnFilledBatch(sample);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	};
	


	/**
	 * 
	 * @throws ParseException
	 */
	private void insertNewActivity() throws ParseException {
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
	
	private void updateScores(final String classification) {
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
			insertNewActivity();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
     * 
     */
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		Log.v(Constants.DEBUG_TAG, "RecorderService.onCreate()");
	}
	
	/**
     * 
     */
	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);
		
		Log.v(Constants.DEBUG_TAG, "RecorderService.onStart()");
		
		//	all the initialization code that was here,
		//		has been moved to the run() function		
		(new Thread(this)).start();
		running = true;
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (running) {
			running = false;
			
			//	destroy any required features of the service
			destroyService();
			
			//	signal the looper to exit
			if (threadLooper!=null)
				threadLooper.quit();
			
		}
	}

	@Override
	public void run() {		
		Log.v(Constants.DEBUG_TAG, "Recorder Service Started!!");
		
		//	create a looper for this thread
		Looper.prepare();
		
		//	obtain a reference to the looper, and create a handler for the looper
		this.threadLooper = Looper.myLooper();
		this.handler = new Handler(threadLooper);
		
		//	initialise any required features of the service
		initService();
		
		//	loop, processing any messages queued
		//		please note that this function blocks until the looper's quit function is called
		//		for this case, the quit function is called when the service is being destroyed
		//		in the onDestroy method.
		Looper.loop();
		
		Log.v(Constants.DEBUG_TAG, "Recorder Service Exitting!!");		
	}
	
	/**
	 * This code is run in the service's separate thread.
	 * The code is used to initialise all the features of the
	 * 	service when the service starts running. 
	 */
	private void initService()
	{
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		// receive phone battery status
		this.registerReceiver(	this.myBatteryReceiver,
								new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		this.registerReceiver(this.myScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		this.registerReceiver(this.myScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		
		model = ModelReader.getModel(this, R.raw.basic_model);
		phoneInfo = new PhoneInfo(this);
		activityQuery = new ActivityQueries(this);
		optionQuery = new OptionQueries(this);
		batchBuffer = new SampleBatchBuffer();
		
		optionQuery.setServiceRunningState("1");
		
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
		sampler = new SimpleSampler(handler, reader, analyseRunnable);
		
		classifierThread = new ClassifierThread(this, binder, batchBuffer);
		classifierThread.start();
		
		// if the account wasn't previously sent,
		// start a thread to register the account.
		if (optionQuery.getAccountState() == 0) {
			registerAccountThread = new AccountThread(this, binder, phoneInfo, optionQuery);
			registerAccountThread.start();
		} else {
			registerAccountThread = null;
		}
		
		// start to upload un-posted activities to Web server
		uploadActivityHistory = new UploadActivityHistoryThread(this, activityQuery, phoneInfo);		
		uploadActivityHistory.startUploads();
		
		handler.postDelayed(registerRunnable, 1000);
		
		// classifications.add(new Classification("CLASSIFIED/WAITING",
		// System.currentTimeMillis()));
		// classifications.add(new Classification("",
		// System.currentTimeMillis()));		
	}
	
	/**
	 * The code is used to destroy all the features of the
	 * 	service when the service is being destroyed. 
	 */
	private void destroyService()
	{
		//	stop sampling
		handler.removeCallbacks(registerRunnable);
		if (sampler != null) {
			sampler.stop();
		}
		
		//	stop threads
		uploadActivityHistory.cancelUploads();
		classifierThread.exit();
		if (registerAccountThread!=null) {
			registerAccountThread.exit();
		}
		
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z z");
		String startTime = dateFormat.format(date);
		// save message "END" to recognise when the background service is
		// finished.
		activityQuery.insertActivities("END", startTime, 0);
		ignore[0] = 0;
		optionQuery.setServiceRunningState("0");
		
		ActivityRecorderActivity.serviceIsRunning = false;
		
		this.unregisterReceiver(myBatteryReceiver);
		this.unregisterReceiver(myScreenReceiver);
		
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
