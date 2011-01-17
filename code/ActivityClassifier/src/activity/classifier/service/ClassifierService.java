/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier.service;

import activity.classifier.Calibration;
import activity.classifier.R;
import activity.classifier.common.Classifier;
import activity.classifier.repository.OptionQueries;
import activity.classifier.repository.TestAVQueries;
import activity.classifier.rpc.ActivityRecorderBinder;
import activity.classifier.utils.CalcStatistics;
import activity.classifier.utils.RotateSamplesToVerticalHorizontal;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 * ClassifierService class is a Service analyse the sensor data to classify activities.
 * RecorderService class invokes this class when sampling is done, 
 * and send parameters (data collection, size of data array,battery status, etc) which is useful to determine the activities.
 * After done with classification, it notices RecorderService about what activity is classified.
 * 
 * 
 * Standard Deviation(sd) and Average values for accelerations(average) are used to classify Uncarried state.
 * chargingState(battery status) is used to classify Charging state.
 * 
 * Other activities are classified through KNN algorithm (with K=1).
 * (This KNN classification is implemented in Aggregator.java)
 * 
 * Local database is used to store some meaningful information such as sd, average, 
 * lastaverage (the average of acceleration values when the activity is Uncarried, if the activity is not a Uncarried, then the values is 0.0).
 *
 * @author chris, modified by Justin Lee
 * 
 * 
 */
public class ClassifierService extends Service  implements Runnable {

	ActivityRecorderBinder service = null;
    
	private String classification;

	/**
	 *  useful informations from RecorderService.
	 */
    private String chargingState="";
    private float[] data; //sampled data
    private int size; //sampled data size
    private float[] ignore={0}; //practical purpose, first classification ignored but would not be used later
    private String lastClassificationName;
    
    /**
     * variables when classify Uncarried state
     */
    private static boolean possiblyUncarried = false;
    private static boolean keepLastAvgAccel = false;
    private static float[] lastaverage = {0, 0, 0};
    private boolean uncarried;
    
    private Calibration calibration;
    private final int CALIBRATION_PERIOD = 5;
    
    private float[] ssd = new float[3];
    
    private OptionQueries optionQuery;
    private TestAVQueries testavQuery;
    
    private RotateSamplesToVerticalHorizontal rotateSamples = new RotateSamplesToVerticalHorizontal();
    
    /**
     * when the connection is binded, it submit the result of the classification
     */
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
    

    /**
     * when this ClassifierService started, data(acceleration), chargingState(battery status),
     * ssd(sensor standard deviation), ignore(practical purpose, first classification ignored but would not be used later)
     */
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        
        chargingState=intent.getStringExtra("status");
        data = intent.getFloatArrayExtra("data");
        size = intent.getIntExtra("size", 128);
        ignore=intent.getFloatArrayExtra("ignore");
        lastClassificationName = intent.getStringExtra("LastClassificationName");
        
        uncarried =  false;
        testavQuery = new TestAVQueries(this);
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
    
    
    /**
     * Classification start
     */
    public void run() {
    	//---------------------Classification for Charging-------------------------//
    	/*
    	 *  Commented for practical reason, DO NOT delete this part. 
    	 */
//        if(chargingState.equals("Charging")){
//             Log.i("STATUS", "Charging");
//        	classification="CLASSIFIED/CHARGING";
//        }
//      else{
    	//---------------------Classification for the rest of activities-------------------------//
		
    	//	first rotate samples to world-orientation
    	
    	rotateSamples.rotateToWorldCoordinates(data);
    	
		int isCalibrated;
		optionQuery = new OptionQueries(this);
		isCalibrated = optionQuery.getCalibrationState();
		// read sensor standard deviation from the database
		ssd[0] = optionQuery.getStandardDeviationX();
		ssd[1] = optionQuery.getStandardDeviationY();
		ssd[2] = optionQuery.getStandardDeviationZ();
		
		float[] sd = new float[3];
		float[] average = { 0, 0, 0 };
		
		CalcStatistics calc; // Computes stats for numbers entered by user.
		calc = new CalcStatistics(data, size);
		
		average = calc.getMean();
		sd = calc.getStandardDeviation();
		
		// Performs calibration when the calibration state is 0 (false)
		if (isCalibrated == 0) {
			calibration = new Calibration();
			
			// calibrate only when the previous classification was Uncarried
			if (lastClassificationName != null
					&& lastClassificationName.equalsIgnoreCase("uncarried")) {
				
				Log.i("Calibration", "Calibration " + (5 - (calibration.getCount() - 1)) + " to go");
				
				calibration.doCalibration(average, sd);
				/*
				 * if calibration is done over the calibration period with
				 * Uncarried state in a row, then calculate the standard
				 * deviation over this period, set it as sensor standard
				 * deviation, and set the calibration state to 1 (true)
				 */
				if (calibration.getCount() == CALIBRATION_PERIOD) {
					float[] tempSSD = new float[3];
					tempSSD = calibration.getSSD();
					
					for (int i = 0; i < 3; i++) {
						ssd[i] = tempSSD[i];
					}
					
					Log.i("Calibration", "saved in datastore");
					optionQuery.setCalibrationState("1");
					optionQuery.setStandardDeviationX(ssd[0] + "");
					optionQuery.setStandardDeviationY(ssd[1] + "");
					optionQuery.setStandardDeviationZ(ssd[2] + "");
					
				}
			} else {
				// when any movement is detected, then calibration is cancelled.
				Log.i("Calibration", "Canceled");
				calibration.setCount(0);
			}
		}
		
		Log.i("Calibration", "ssd[0] : " + ssd[0] + ", " + "ssd[1] : " + ssd[1] + ", "
				+ "ssd[2] : " + ssd[2]);
		
		if (sd[0] < 4 * ssd[0] && sd[1] < 4 * ssd[1] && sd[2] < 4 * ssd[2] && !possiblyUncarried) {
			
			lastaverage[0] = average[0];
			lastaverage[1] = average[1];
			lastaverage[2] = average[2];
			
			possiblyUncarried = true;
			keepLastAvgAccel = true;
			Log.i("STATUS", "1possibly uncarried  ");
			
		} else
			if (possiblyUncarried) {
				Log.i("compare", "CompareX : " + (lastaverage[0] - 4 * ssd[0]) + " <= "
						+ average[0] + " <= " + (lastaverage[0] + 4 * ssd[0]));
				Log.i("compare", "CompareY : " + (lastaverage[1] - 4 * ssd[0]) + " <= "
						+ average[1] + " <= " + (lastaverage[1] + 4 * ssd[0]));
				Log.i("compare", "CompareZ : " + (lastaverage[2] - 4 * ssd[0]) + " <= "
						+ average[2] + " <= " + (lastaverage[2] + 4 * ssd[0]));
				if ((lastaverage[0] - 4 * ssd[0] <= average[0] && lastaverage[0] + 4 * ssd[0] >= average[0])
						&& (lastaverage[1] - 4 * ssd[0] <= average[1] && lastaverage[1] + 4
								* ssd[0] >= average[1])
						&& (lastaverage[2] - 4 * ssd[0] <= average[2] && lastaverage[2] + 4
								* ssd[0] >= average[2])) {
					uncarried = true;
					
				} else {
					keepLastAvgAccel = false;
					possiblyUncarried = false;
				}
				Log.i("STATUS", "2possibly uncarried  ");
			} else {
				possiblyUncarried = false;
				uncarried = false;
				keepLastAvgAccel = false;
			}
		
		testavQuery.insertTestValues(	sd[0] + "", sd[1] + "", sd[2] + "", lastaverage[0] + "",
										lastaverage[1] + "", lastaverage[2] + "", average[0] + "",
										average[1] + "", average[2] + "");
		Log.i("sd", sd[0] + " " + sd[1] + " " + sd[2] + " ");
		Log.i("last", lastaverage[0] + " " + lastaverage[1] + " " + lastaverage[2] + " ");
		Log.i("curr", average[0] + " " + average[1] + " " + average[2] + " ");
		// ---------------------------------------------------------------------
		// ---------------------Classification for
		// Uncarried-------------------------//
		if (uncarried) {
			uncarried = false;
			keepLastAvgAccel = true;
			classification = "CLASSIFIED/UNCARRIED";
		}
		// ---------------------Classification for the rest of activities by
		// using Chris's-------------------------//
		else {
			// ignore the very first classification
			if (ignore[0] != 1) {
				if (!keepLastAvgAccel) {
					lastaverage[0] = 0;
					lastaverage[1] = 0;
					lastaverage[2] = 0;
				}
				classification = new Classifier(RecorderService.model.entrySet()).classify(	data,
																							size);
				keepLastAvgAccel = false;
			} else {
				classification = "CLASSIFIED/WAITING";
			}
		}
		// }
		
		// Log.i(getClass().getName(), "Classification: " + classification);
		
		bindService(new Intent(this, RecorderService.class), connection, BIND_AUTO_CREATE);
    }
}


