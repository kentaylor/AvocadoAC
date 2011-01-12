/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier.common.service;

import activity.classifier.CalcStatistics;
import activity.classifier.Calibration;
import activity.classifier.R;
import activity.classifier.R.string;
import activity.classifier.common.Classifier;
import activity.classifier.common.repository.OptionQueries;
import activity.classifier.rpc.ActivityRecorderBinder;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 *
 * @author chris
 * @modified by Justin
 * 
 * ClassifierService class is a Service analyse the sensor data to classify activities.
 * RecorderService class invokes this class when sampling is done, 
 * and send parameters (data collection, size of data array,battery status, etc) which is useful to determine the activities.
 * After done with classification, it notices RecorderService about what activity is classified.
 * 
 * 
 * Standard Deviation(sd) and Average values for accelerations(average) are used to classify Uncarried state.
 * strStatus(battery status) is used to classify Charging state.
 * 
 * Other activities are classified through KNN algorithm (with K=1).
 * (This KNN classification is implemented in Aggregator.java)
 * 
 * Local database is used to store some meaningful information such as sd, average, 
 * lastaverage (the average of acceleration values when the activity is Uncarried, if the activity is not a Uncarried, then the values is 0.0).
 * 
 */
public class ClassifierService extends Service  implements Runnable {

	ActivityRecorderBinder service = null;
    private String classification;

    private String strStatus="";
    private float[] data;
    private int size;
    private float[] ignore={0};
    private String LastClassificationName;
    
    private static boolean possibly_uncarried = false;
    private static boolean keepLastAvgAccel = false;
    private static float[] lastaverage = {0, 0, 0};
    private boolean uncarried;
    
    private int isCalibrated;
    private Calibration calibration;
    
    private float[] ssd = new float[3];
    
    private OptionQueries optionQuery;
    
    private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ActivityRecorderBinder.Stub.asInterface(arg1);

            try {
                service.submitClassification(classification);
                
            } catch (RemoteException ex) {
            	Log.e("connection", "Exception error occured in connection in ClassifierService class");
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
        
        strStatus=intent.getStringExtra("status");
        data = intent.getFloatArrayExtra("data");
        size = intent.getIntExtra("size", 128);
        ignore=intent.getFloatArrayExtra("ignore");
        LastClassificationName = intent.getStringExtra("LastClassificationName");

        uncarried =  false;

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
        	optionQuery = new OptionQueries(this);
	        isCalibrated = optionQuery.getCalibrationState();
	        ssd[0] = optionQuery.getStandardDeviationX();
        	ssd[1] = optionQuery.getStandardDeviationY();
    		ssd[2] = optionQuery.getStandardDeviationZ();
        
        	float[] sd= new float[3];
        	float[] average={0,0,0};

        	CalcStatistics calc;  // Computes stats for numbers entered by user.
            calc = new CalcStatistics(data,size);
        	
            average=calc.getMean();
        	sd=calc.getStandardDeviation();
        	
        	if(isCalibrated==0){
        		calibration= new Calibration();
        		
	        	if(LastClassificationName!=null && LastClassificationName.equalsIgnoreCase("uncarried")){
	        		
	        		Log.i("Calibration","Calibration "+(5-(calibration.getCount()-1))+ " to go");
	        		
		        	calibration.doCalibration(average, sd);
		        	if(calibration.getCount() == 5){
		        		float[] tempSSD = new float[3];
		        		tempSSD = calibration.getSSD();
		        		
		        		for(int i=0;i<3;i++){
		        			ssd[i]=tempSSD[i];
		        		}
		        		
	        			Log.i("Calibration","saved in datastore");
	        			optionQuery.setCalibrationState("1");
	        			optionQuery.setStandardDeviationX(ssd[0]+"");
	        			optionQuery.setStandardDeviationY(ssd[1]+"");
	        			optionQuery.setStandardDeviationZ(ssd[2]+"");
	        			
		        	}
	        	}else{
	        		Log.i("Calibration","Canceled");
	        		calibration.setCount(0);
	        	}
        	}
        	
        	Log.i("Calibration","ssd[0] : "+ssd[0]+", "+"ssd[1] : "+ssd[1]+", "+"ssd[2] : "+ssd[2]);

        	if(sd[0]<4*ssd[0] && sd[1]<4*ssd[1] && sd[2]<4*ssd[2] && !possibly_uncarried){
        		
        		lastaverage[0]=average[0];
        		lastaverage[1]=average[1];
        		lastaverage[2]=average[2];
        		
        		possibly_uncarried = true;
        		keepLastAvgAccel=true;
        		Log.i("STATUS", "1possibly uncarried  ");
        		
        	}else if(possibly_uncarried){
        		Log.i("compare", "CompareX : "+(lastaverage[0]-4*ssd[0])+" <= "+ average[0] +" <= "+(lastaverage[0]+4*ssd[0]));
        		Log.i("compare", "CompareY : "+(lastaverage[1]-4*ssd[0])+" <= "+ average[1] +" <= "+(lastaverage[1]+4*ssd[0]));
        		Log.i("compare", "CompareZ : "+(lastaverage[2]-4*ssd[0])+" <= "+ average[2] +" <= "+(lastaverage[2]+4*ssd[0]));
        		if(		(lastaverage[0]-4*ssd[0]<=average[0] &&lastaverage[0]+4*ssd[0]>=average[0]) &&
        				(lastaverage[1]-4*ssd[0]<=average[1] &&lastaverage[1]+4*ssd[0]>=average[1]) &&
        				(lastaverage[2]-4*ssd[0]<=average[2] &&lastaverage[2]+4*ssd[0]>=average[2])){
        			uncarried=true;
        			
        		}else{
        			keepLastAvgAccel=false;
        			possibly_uncarried=false;
      			}
        		Log.i("STATUS", "2possibly uncarried  ");
        	}else{
        		possibly_uncarried = false;
        		uncarried = false;
        		keepLastAvgAccel = false;
        	}
        	
//        	dbAdapter.open();
//        	dbAdapter.insertTestAV(sd[0]+"",sd[1]+"",sd[2]+"",lastaverage[0]+"", lastaverage[1]+"", lastaverage[2]+"", average[0]+"", average[1]+"", average[2]+"");
//        	dbAdapter.close();
        	Log.i("sd",sd[0]+" "+sd[1]+" "+sd[2]+" ");
        	Log.i("last",lastaverage[0]+" "+lastaverage[1]+" "+lastaverage[2]+" ");
        	Log.i("curr",average[0]+" "+average[1]+" "+average[2]+" ");
//        	---------------------------------------------------------------------
        	//---------------------Classification for Uncarried-------------------------//
		   if(uncarried){
			   uncarried=false;
			   keepLastAvgAccel=true;
			   classification="CLASSIFIED/UNCARRIED";
		    }
		 //---------------------Classification for the rest of activities by using Chris's-------------------------//
		   else{
			   if(ignore[0]!=1){
				   if(!keepLastAvgAccel){
					   lastaverage[0]=0;
					   lastaverage[1]=0;
					   lastaverage[2]=0;
				   }
				   classification = new Classifier(RecorderService.model.entrySet()).classify(data, size);
				   keepLastAvgAccel=false;
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


