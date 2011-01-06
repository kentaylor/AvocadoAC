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
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import activity.classifier.rpc.Classification;
import activity.classifier.rpc.*;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

/**
 *
 * @author chris
 * modified by Justin
 */
public class RecorderService extends Service {

    static final double DELTA = 0.25;
    static final double THRESHOLD = 0.4;
    public String strStatus="";
    private String MODEL="";
    protected AccountManager accountManager;
    Sensor mSensor;
    private final Handler handler = new Handler();
    private DbAdapter dbAdapter;

    private static int service = 0;
    private static int IM = 1;
    private Timer timer;
    private SensorManager manager;
    private PowerManager.WakeLock wl;
    private PowerManager.WakeLock wl2;
    private PowerManager.WakeLock wl3;
    private static float[] ignore={0};
    private float[] values = new float[3];

    //data size is 128*3 (xyz-axis)
    private float[] data = new float[384];
    private volatile int nextSample = 0;

    boolean running;
   
    public static Map<Float[], String> model;
    private final List<Classification> classifications = new ArrayList<Classification>();

    private boolean wakelock;
    private Boolean wl2IsAcquired=false;
    private Boolean wlIsAcquired=false;
    PowerManager pm1;
    
    private final HashMap<String, HashMap<String, Double>> scores
            = new HashMap<String, HashMap<String, Double>>() {{
        put("", new HashMap<String, Double>() {{
            put("null", 0.5d);
            put("CLASSIFIED", 0.5d);
//            put("UNCLASSIFIED", 0.333d);
        }});

        put("CLASSIFIED", new HashMap<String, Double>() {{
//            put("null", 0.1d);
            put("CHARGING", 0.1d);
            put("UNCARRIED", 0.1d);
            put("END", 0.1d);
            put("WALKING", 0.1d);
            put("PADDLING", 0.1d);
            put("TRAVELLING", 0.1d);
            put("ACTIVE", 0.1d);
            put("DANCING", 0.1d);
            put("VEHICLE",0.1d);
            put("IDLE", 0.1d);
//            put("WAITING", 0.1428d);
        }});
       

        put("CLASSIFIED/WALKING", new HashMap<String, Double>() {{
//            put("null", 0.5d);
//            put("STAIRS",0.5d);
        }});

        put("CLASSIFIED/VEHICLE", new HashMap<String, Double>() {{
//            put("null", 0.333d);
//            put("CAR", 0.333d);
//            put("BUS", 0.333d);
        }});

        put("CLASSIFIED/IDLE", new HashMap<String, Double>() {{
            put("null", 0.333d);
            put("STANDING", 0.333d);
            put("SITTING", 0.333d);
        }});

        put("CLASSIFIED/WALKING/STAIRS", new HashMap<String, Double>() {{
//            put("null", 0.333d);
//            put("UP", 0.333d);
//            put("DOWN", 0.333d);
        }});
      
      
    }};

    //Broadcast receiver for battery manager
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
    };

    private final Runnable sampleRunnable = new Runnable() {

        public void run() {
        		sample();
        }
        
    };

    private final Runnable registerRunnable = new Runnable() {

        public void run() {
            register();
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



    private final SensorEventListener accelListener = new SensorEventListener() {

        /** {@inheritDoc} */
        public void onSensorChanged(final SensorEvent event) {
            setAccelValues(event.values);
//            Log.i("onsensor",event.values+"");
        }

        /** {@inheritDoc} */
        public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
            // Don't really care
        }

    };

    public void setAccelValues(float[] accelValues) {
        this.values = new float[]{
    		accelValues[SensorManager.DATA_X],
            accelValues[SensorManager.DATA_Y],
            accelValues[SensorManager.DATA_Z]
        };
    }
    
    //sampling accelerometer data per 50ms
    public void sample() {
        data[(nextSample * 3) % 384] = values[0];
        data[(nextSample * 3 + 1) % 384] = values[1];
        data[(nextSample * 3 + 2) % 384] = values[2];
        Log.i("accel50",values[0]+" "+values[1]+" "+values[2]);
	        //when data is reached 128 data in each axis
	        if (++nextSample % 64 == 0 && nextSample >= 128) {
	        	Log.i("accel50","End");
	            float[] cache = new float[384];
	            System.arraycopy(data, 0, cache, 0, 384);
	        	Log.i("next",nextSample+"");
	        	unregister();
            	analyse(cache,strStatus,128);
            	return;
	        }
//        }
	        handler.postDelayed(sampleRunnable, 50);
       
    }

    public void analyse(float[] data, String status, int size) {

        final Intent intent = new Intent(this, ClassifierService.class);
        if(ignore[0]<=1){
        	ignore[0]++;
        }
        intent.putExtra("data", data);
        intent.putExtra("status", status);
        intent.putExtra("size", size);
        intent.putExtra("ignore", ignore);
        intent.putExtra("wake", wakelock);
//        intent.putExtra("lastClass", classifications.get(classifications.size()-1).getNiceClassification());
        startService(intent);

//        Log.i("analyse","--exit--");
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    public String getModel() {
        return android.os.Build.MODEL;

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
        dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        dbAdapter.updateStart(1, 0+"");
        Cursor result5 =    dbAdapter.fetchStart(7);
        IM = (int) Float.valueOf(result5.getString(1).trim()).floatValue();;
        result5.close();
        dbAdapter.close();

        timer = new Timer("Data logger");
        MODEL=getModel();
        accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        final String account = accounts[0].name;
        		
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            	
        		HttpClient client = new DefaultHttpClient();
        	    final HttpPost post = new HttpPost("http://testingjungoo.appspot.com/activity");
        	    final File file = getFileStreamPath("activityrecords.db");
        	    final FileEntity entity = new FileEntity(file, "text/plain");
        	      
        	    ArrayList<String> activity = new ArrayList<String>();
        	    ArrayList<String> date = new ArrayList<String>();
        	    ArrayList<Integer> id = new ArrayList<Integer>();
        	    //open database and check the un-posted data and send that data 
        	    dbAdapter.open();
        	    Cursor result =    dbAdapter.fetchActivityCheck1(0);
        	      
        	    for(result.moveToFirst(); result.moveToNext(); result.isAfterLast()) {
        	    	id.add(Integer.parseInt(result.getString(0)));
        	    	activity.add(result.getString(1));
        	    	date.add(result.getString(2));
        	    	  
        	    	Log.i("acti",result.getString(1)+"");
            	    Log.i("date",result.getString(2)+"");
        	    }
        	    Log.i("spe",activity.size()+"");
        	    result.close();
        	    dbAdapter.close();

        	    if(activity.size()!=0){
        	    	String message = "";
        		    Log.i("size?",activity.size()+"");
        		    for(int i = 0 ; i<activity.size();i++){
        		     
        		    	if(i==activity.size()){
        		    		message +=  activity.get(i)+"&&"+date.get(i);
        		    	}else{
        		    		message +=  activity.get(i)+"&&"+date.get(i)+"##";
        		    	}
        		    }
        		    String[] chunk = message.split("##");
        		    Log.i("s",chunk.length+"");
        		    for(int i=0;i<chunk.length;i++){
        		    	Log.i("Series",chunk[i]);
        		    }
        		    try {
        		    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        		    	Date systemdate = Calendar.getInstance().getTime();
        		    	String reportDate = df.format(systemdate);
        		    	post.setHeader("sysdate",reportDate);
        		    	post.setHeader("size",activity.size()+"");
        	  	        post.setHeader("message", message);
        	  	        post.setHeader("UID", account);
        	  	        post.setEntity(entity);
        	   	    	
        	  	        int code = new DefaultHttpClient().execute(post).getStatusLine().getStatusCode();
        		        Log.i("m",message);
        		        
        		        dbAdapter.open();
        		        for(int i=0;i<id.size();i++){
        		        	dbAdapter.updateActivity(id.get(i), activity.get(i), date.get(i), 1,1);
	            		}
	            		dbAdapter.close();
   		            } catch (IOException ex) {
   		            	Log.e(getClass().getName(), "Unable to upload sensor logs", ex);
        		        dbAdapter.open();
        		        for(int i=0;i<id.size();i++){
        		        	dbAdapter.updateActivity(id.get(i), activity.get(i), date.get(i), 0,0);
        		        }
        		        dbAdapter.close();
        		    }
        	    }
            }
        }, 300000, 300000);
        if(IM==1){
        	startActivity(new Intent(this, AccountActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        init();
        
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
    	
    	InputStream is = null;
        try {
            is = getResources().openRawResource(R.raw.basic_model);
            model = (Map<Float[], String>) new ObjectInputStream(is).readObject();
        } catch (Exception ex) {
            Log.e(getClass().getName(), "Unable to load model", ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    // Don't care
                }
            }
        }

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        handler.postDelayed(registerRunnable, 1000);
        handler.postDelayed(updateRunnable, 500);

        handler.postDelayed(screenRunnable, 1000);
//        classifications.add(new Classification("CLASSIFIED/WAITING", System.currentTimeMillis(),service));
//        classifications.add(new Classification("", System.currentTimeMillis(),service));
        
    }

    private final Runnable updateRunnable = new Runnable() {

        public void run() {
            try {
				updateButton();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            handler.postDelayed(updateRunnable, 500);
        }
    };
    
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
			            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
			                
			            String date = adapter.get(adapter.size()-1).getStartTime();
			            Date date1 = dateFormat.parse(date); 
			            dbAdapter.open();
			            dbAdapter.insertActivity(activity,date,   0,0);
			            dbAdapter.close();
		            }
		            lastAc = newAc;
	        }	           

	    } catch (Exception ex) {
	    	Log.e(getClass().getName(), "Unable to get service state", ex);
	    } 
	        
	}
	
    //register calls itself every 30sec which means the app classfy activity every 30sec
    void register() {
        //Log.i(getClass().getName(), "Registering");
    	
        nextSample = 0;
        manager.registerListener(accelListener,
                manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        handler.postDelayed(sampleRunnable, 50);
        handler.postDelayed(registerRunnable, 30000);
        
    }
    ArrayList<String> activity = new ArrayList<String>();
    ArrayList<String> date = new ArrayList<String>();
    //post call itself every 5min,

    void unregister() {
        manager.unregisterListener(accelListener);
    }

    
    //this is for Chris's classification ()
    void updateScores(final String classification) {
        String path = "";
        if(!classification.equals("CLASSIFIED/WAITING")){
	        for (String part : classification.split("/")) {
	            if (!scores.containsKey(path)) {
	                throw new RuntimeException("Path not found: " + path
	                        + " (classification: " + classification + ")");
	            }
	            updateScores(scores.get(path), part);
	            path = path + (path.length() == 0 ? "" : "/") + part;
	        }
	
	        if (scores.containsKey(path)) {
	            // This classification has children which we're not using
	            // e.g. we've received CLASSIFIED/WALKING, but we're not walking
	            //      up or down stairs
	            if(classification.equalsIgnoreCase("classified/charging")
	            		||classification.equalsIgnoreCase("classified/uncarried")){
	            	
	            }else
	            updateScores(scores.get(path), "null");
	        }
	        final String best = getClassification();
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
    private String getClassification() {
        String path = "";

        do {
            final Map<String, Double> map = scores.get(path);
            double best = THRESHOLD;
            String bestPath = "null";
	            for (Map.Entry<String, Double> entry : map.entrySet()) {
	                if (entry.getValue() >= best) {
	                    best = entry.getValue();
	                    bestPath = entry.getKey();
	                }
	            }
	
	            path = path + (path.length() == 0 ? "" : "/") + bestPath;
	            Log.i("PATH",path);
	            Log.i("bestPATH",bestPath);
//            }
        } while (scores.containsKey(path));

        return path.replaceAll("(^CLASSIFIED)?/?null$", "");
    }

    void updateScores(final Map<String, Double> map, final String target) {
    	
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            Log.d(getClass().getName(), "Score for " + entry.getKey() + " was: " + entry.getValue());
            entry.setValue(entry.getValue() * (1 - DELTA));

            if (entry.getKey().equals(target)) {
                entry.setValue(entry.getValue() + DELTA);
            }
            if (target.equalsIgnoreCase("uncarried") ||target.equalsIgnoreCase("charging")) {
                if (entry.getKey().equals(target)) {
                	entry.setValue((double) 1);
                }
            
            }else{
            	for(Map.Entry<String, Double> entry1 : map.entrySet()){
            		if (entry1.getKey().equalsIgnoreCase("uncarried") ||entry1.getKey().equalsIgnoreCase("charging")) {
                        entry1.setValue((double) 0);
                    }
            	}
            }
            Log.d(getClass().getName(), "Score for " + entry.getKey() + " is now: " + entry.getValue());
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
            ignore[0] = 0;
            service = 0;
            dbAdapter.open();
            dbAdapter.updateStart(1, 1+"");
            dbAdapter.close();
            handler.removeCallbacks(sampleRunnable);
            handler.removeCallbacks(registerRunnable);
            handler.removeCallbacks(updateRunnable);
            handler.removeCallbacks(screenRunnable);
            unregister();
            this.unregisterReceiver(myBatteryReceiver);
            timer.cancel();
           
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
