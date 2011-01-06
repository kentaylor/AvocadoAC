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
    private float[] ignore={0};
    private float[] data;
    private static float[] ssd={0,0,0};
    private int size;
    private String lastActi;
    //when this ClassifierService started, data(acceleration), strStatus(battery status),
    //ssd(sensor standard deviation), ignore(practical purpose, first classification ignored but would not be used later)
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        //Log.i(getClass().getName(), "Starting classifier");

        data = intent.getFloatArrayExtra("data");
        strStatus=intent.getStringExtra("status");
        ssd=intent.getFloatArrayExtra("sd");
        ignore=intent.getFloatArrayExtra("ignore");
        size = intent.getIntExtra("size", 128);
        wakelock = intent.getBooleanExtra("wake", true);
//        lastActi = intent.getStringExtra("lastClass");
        new Thread(this).start();
    }
    
    
 
    @Override
    public void onDestroy() {
        super.onDestroy();
//        dbAdapter.close();
        unbindService(connection);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    private DbAdapter dbAdapter;
    
    //Classification start!
    public void run() {

    	
    	//---------------------Classification for Charging-------------------------//
        if(strStatus.equals("Charging")){
        	 
             Log.i("STATUS", "Charging");
//        	classification="CLASSIFIED/CHARGING";

        }
    	//---------------------Classification for the rest of activities-------------------------//
 //       else{
        	float[] sd= new float[3];
        	float[] average={0,0,0};
        	boolean AlwaysFalse=false;
        	CalcStatistics calc;  // Computes stats for numbers entered by user.
            calc = new CalcStatistics(data,size);
        	average=calc.getMean();
        	sd=calc.getStandardDeviation();
        	dbAdapter = new DbAdapter(this);

        	if(next==0 && possibly_uncarried  &&( (
 //remove logic for comparing to this phone's sensor standard deviation as cliabration functionality does not work
   				(lastaverage[0]-((3)*ssd[0])>=average[0] &&
           				 lastaverage[1]-((3)*ssd[1])>=average[1] &&
           			     lastaverage[2]-((3)*ssd[2])>=average[2])||
           			     (lastaverage[0]+((3)*ssd[0])<=average[0] &&
           	        	   lastaverage[1]+((3)*ssd[1])<=average[1] &&
           	        	   lastaverage[2]+((3)*ssd[2])<=average[2])
           	        	   )
//        			(AlwaysFalse==false)
          			     ||(
        			
        			
           			    		 (lastaverage[0]-(0.05)>=average[0] &&
                   				 lastaverage[1]-(0.05)>=average[1] &&
                   			     lastaverage[2]-(0.05)>=average[2])||
                   			     (lastaverage[0]+(0.05)<=average[0] &&
                   	        	   lastaverage[1]+(0.05)<=average[1] &&
                   	        	   lastaverage[2]+(0.05)<=average[2])
                   	        	   )
                   	        	   )){
        		next=0;
        		next1=0;
        		possibly_uncarried=false;
        		step=false;
        	}
        	else if(sd[0]<3*ssd[0] && sd[1]<3*ssd[1] && sd[2]<3*ssd[2] && next==0){
        		next++;
        		possibly_uncarried = true;
        		lastaverage=average;
        		next1=1;
        		Log.i("STATUS", "1possibly uncarried  "+next);
        	}
        	else if(next==1){
        		
//        		Log.i("STATUS", "2possibly uncarried  "+next);
        		if(possibly_uncarried &&( (
        				(lastaverage[0]-((3)*ssd[0])<=average[0] &&
        				 lastaverage[1]-((3)*ssd[1])<=average[1] &&
        			     lastaverage[2]-((3)*ssd[2])<=average[2])||
        			     (lastaverage[0]+((3)*ssd[0])>=average[0] &&
        	        	   lastaverage[1]+((3)*ssd[1])>=average[1] &&
        	        	   lastaverage[2]+((3)*ssd[2])>=average[2])
        	        	   )
        			     ||(
        			    		 (lastaverage[0]-(0.05)<=average[0] &&
                				 lastaverage[1]-(0.05)<=average[1] &&
                			     lastaverage[2]-(0.05)<=average[2])||
                			     (lastaverage[0]+(0.05)>=average[0] &&
                	        	   lastaverage[1]+(0.05)>=average[1] &&
                	        	   lastaverage[2]+(0.05)>=average[2])
                	        	   )
                	        	   )
        	)
                			     {
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
        	Log.i("ssd",ssd[0]+" "+ssd[1]+" "+ssd[2]+" ");
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
		 //---------------------Classification for possibly Uncarried-------------------------//
//		   else if(possibly_uncarried || step){
//			   Log.i("STATUS", "possibly uncarried  "+next);
//			   classification="CLASSIFIED/WAITING";
//		   }
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
    private static int next1 = 0;
    private static int next = 0;
    private static boolean step = false;
    private static boolean possibly_uncarried = false;
    private static float[] lastaverage={0,0,0};
    float[] sum_sqr ={0,0,0};
    float[] sum = {0,0,0};
    //Function for the calculation of average acceleration
    private float[] average(float[] ArrayIn, int SizeOfArray){

    	sum_sqr[0]=0;
    	sum_sqr[1]=0;
    	sum_sqr[2]=0;
    	sum[0]=0;
    	sum[1]=0;
    	sum[2]=0;
    	
    	float[] average = {0,0,0};
	    if(size!=1){
    	for(int i=0;i<128;i++){
				sum[0]+=ArrayIn[(i * 3)];
				sum[1]+=ArrayIn[(i * 3+1)];
				sum[2]+=ArrayIn[(i * 3+2)];
				sum_sqr[0]+=ArrayIn[(i * 3)]*ArrayIn[(i * 3)];
				sum_sqr[1]+=ArrayIn[(i * 3+1)]*ArrayIn[(i * 3+1)];
				sum_sqr[2]+=ArrayIn[(i * 3+2)]*ArrayIn[(i * 3+2)];

			}
	    }else{
	    	sum[0]+=ArrayIn[0];
			sum[1]+=ArrayIn[1];
			sum[2]+=ArrayIn[2];
			sum_sqr[0]+=ArrayIn[0]*ArrayIn[0];
			sum_sqr[1]+=ArrayIn[1]*ArrayIn[1];
			sum_sqr[2]+=ArrayIn[2]*ArrayIn[2];
	    }
	      average[0]=sum[0]/SizeOfArray;
	      average[1]=sum[1]/SizeOfArray;
	      average[2]=sum[2]/SizeOfArray;
//	      average[3]=sum[3]/128;
//	      average[4]=sum[4]/128;
//	      average[5]=sum[5]/128;
	      return average;
    }
    
    //Function for calculating the standard deviation of the sampling
    private float[] standard(float[] average, float[] sum_sqr, float[] sum){
    	
    	float[] diff = {0,0,0};
    	float[] sd = {0,0,0};
    	
    	sd[0]=(float)(sum_sqr[0] - sum[0]*average[0])/(float)(size);
	      sd[1]=(float)(sum_sqr[1] - sum[1]*average[1])/(float)(size);
	      sd[2]=(float)(sum_sqr[2] - sum[2]*average[2])/(float)(size);
	      return sd;
    }

}


