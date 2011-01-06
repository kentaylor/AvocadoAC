/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier;

import java.util.Map;

import activity.classifier.CalcStatistics;
import activity.classifier.rpc.ActivityRecorderBinder;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 *
 * @author chris
 * @modified by Justin
 */
public class ClassifierService extends Service  implements Runnable {

	ActivityRecorderBinder service = null;
    String classification;
    String strStatus="";
    private boolean wakelock;
    boolean serviceStart=false;
    private float[] data;
    private int size;
    private static int next1 = 0;
    private static int next = 0;
    private static boolean step = false;
    private static boolean possibly_uncarried = false;
    private static float[] lastaverage={0,0,0};
    float[] sum_sqr ={0,0,0};
    float[] sum = {0,0,0};
    private DbAdapter dbAdapter;
    private float[] ignore={0};
    
    private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ActivityRecorderBinder.Stub.asInterface(arg1);

            try {
                service.submitClassification(classification);
                service.SetWakeLock(wakelock);
            } catch (RemoteException ex) {
// put something here. At least log an error
            }
            
            stopSelf();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Toast.makeText(ClassifierService.this, R.string.error_disconnected, Toast.LENGTH_LONG);
        }
    };
    

    //when this ClassifierService started, data(acceleration), strStatus(battery status),
    //ssd(sensor standard deviation), ignore(practical purpose, first classification ignored but would not be used later)
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        data = intent.getFloatArrayExtra("data");
        strStatus=intent.getStringExtra("status");
        size = intent.getIntExtra("size", 128);
        wakelock = intent.getBooleanExtra("wake", true);
        ignore=intent.getFloatArrayExtra("ignore");
        new Thread(this).start();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    //Classification start!
    public void run() {
    	//---------------------Classification for Charging-------------------------//
        if(strStatus.equals("Charging")){
             Log.i("STATUS", "Charging");
//        	classification="CLASSIFIED/CHARGING";
        }
    	//---------------------Classification for the rest of activities-------------------------//
//        else{
        	float[] sd= new float[3];
        	float[] average={0,0,0};
        	boolean AlwaysFalse=false;
        	CalcStatistics calc;  // Computes stats for numbers entered by user.
            calc = new CalcStatistics(data,size);
        	average=calc.getMean();
        	sd=calc.getStandardDeviation();
        	dbAdapter = new DbAdapter(this);

        	if(next==0 && possibly_uncarried  &&(     			
        			(lastaverage[0]-(0.05)>=average[0] && lastaverage[1]-(0.05)>=average[1] && lastaverage[2]-(0.05)>=average[2])
        			||(lastaverage[0]+(0.05)<=average[0] && lastaverage[1]+(0.05)<=average[1] && lastaverage[2]+(0.05)<=average[2]))){
        		next=0;
        		next1=0;
        		possibly_uncarried=false;
        		step=false;
        	}else if(sd[0]<0.05 && sd[1]<0.05 && sd[2]<0.05 && next==0){
        		next++;
        		possibly_uncarried = true;
        		lastaverage=average;
        		next1=1;
        		Log.i("STATUS", "1possibly uncarried  "+next);
        	}else if(next==1){
        		if(possibly_uncarried &&(
    				(lastaverage[0]-(0.05)<=average[0] && lastaverage[1]-(0.05)<=average[1] && lastaverage[2]-(0.05)<=average[2])
    				|| (lastaverage[0]+(0.05)>=average[0] && lastaverage[1]+(0.05)>=average[1] && lastaverage[2]+(0.05)>=average[2]))){
        			next++;
        			step=true;
        		}else{
        			next=0;
        			next1=0;
      			}
        		Log.i("STATUS", "2possibly uncarried  "+next);
        	}
        	
        	dbAdapter.open();
        	dbAdapter.insertTestAV(sd[0]+"",sd[1]+"",sd[2]+"",lastaverage[0]+"", lastaverage[1]+"", lastaverage[2]+"", average[0]+"", average[1]+"", average[2]+"");
        	dbAdapter.close();
        	Log.i("sd",sd[0]+" "+sd[1]+" "+sd[2]+" ");
        	Log.i("last",lastaverage[0]+" "+lastaverage[1]+" "+lastaverage[2]+" ");
        	Log.i("curr",average[0]+" "+average[1]+" "+average[2]+" ");
//        	---------------------------------------------------------------------
        	//---------------------Classification for Uncarried-------------------------//
		   if(next==2&&possibly_uncarried && step){
			   Log.i("STATUS", "next1  "+next1);
			   Log.i("STATUS", "next  "+next);
			   next=1;
			   next1=1;
			   classification="CLASSIFIED/UNCARRIED";
		    }
		 //---------------------Classification for the rest of activities by using Chris's-------------------------//
		   else{
			   Log.i("STATUS", "next1  "+next1);
			   Log.i("STATUS", "next  "+next);
			   if(ignore[0]!=1){
				   if(next1!=1){
					   lastaverage[0]=0;
					   lastaverage[1]=0;
					   lastaverage[2]=0;
					   next=0;
				   }
			       float oddTotal = 0, evenTotal = 0;
			       float oddMin = Float.MAX_VALUE, oddMax = Float.MIN_VALUE;
			       float evenMin = Float.MAX_VALUE, evenMax = Float.MIN_VALUE;
	
			       for (int i = 0; i < size; i++) {
			    	   evenTotal += data[i * 3 + 1];
			           oddTotal += data[i * 3 + 2];
			           evenMin = Math.min(evenMin, data[i * 3 + 1]);
			           oddMin = Math.min(oddMin, data[i * 3 + 2]);
	
			           evenMax = Math.max(evenMax, data[i * 3 + 1]);
			           oddMax = Math.max(oddMax, data[i * 3 + 2]);
			       }
	
			       final float[] points = {
		    		   Math.abs(evenTotal / size),
		    		   Math.abs(oddTotal / size),
		    		   evenMax - evenMin,
		    		   oddMax - oddMin
			       };
			    	
			       float bestDistance = Float.MAX_VALUE;
			       String bestActivity = "UNCLASSIFIED/UNKNOWN";
			        
			       for (Map.Entry<Float[], String> entry : RecorderService.model.entrySet()) {
			    	   float distance = 0;
	
			           for (int i = 0; i < points.length; i++) {
			        	   distance += Math.pow(points[i] - entry.getKey()[i], 2);
			           }
	
			           if (distance < bestDistance) {
			        	   bestDistance = distance;
			        	   bestActivity = entry.getValue();
			           }
			       }
			       classification = bestActivity;
			       next1=0;
			   }
			   else{
				   classification="CLASSIFIED/WAITING";
			   }
		   }
//        }

        //Log.i(getClass().getName(), "Classification: " + classification);

        bindService(new Intent(this, RecorderService.class), connection, BIND_AUTO_CREATE);
    }
}


