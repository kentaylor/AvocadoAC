/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier.service.threads;

import activity.classifier.Calibration;
import activity.classifier.R;
import activity.classifier.accel.SampleBatch;
import activity.classifier.accel.SampleBatchBuffer;
import activity.classifier.common.Classifier;
import activity.classifier.common.Constants;
import activity.classifier.repository.OptionQueries;
import activity.classifier.repository.TestAVQueries;
import activity.classifier.rpc.ActivityRecorderBinder;
import activity.classifier.service.RecorderService;
import activity.classifier.utils.CalcStatistics;
import activity.classifier.utils.RotateSamplesToVerticalHorizontal;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
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
 *	<p>
 *	Changes made by Umran: <br>
 *	The class used to be called ClassifierService. Now changed to a thread.
 *	Communication between {@link RecorderService} and this class is done through
 *	the {@link SampleBatch} and {@link SampleBatchBuffer}.
 *	<p>
 *	Filled batches are posted into the buffer in {@link RecorderService}
 *	and removed here, after analysis, the batches are posted back into the
 *	buffer as empty batches where the recorder class removes them and fills them
 *	with sampled data. 
 *
 * @author chris, modified by Justin Lee
 * 
 * 
 */
public class ClassifierThread extends Thread {

	private ActivityRecorderBinder service;
	private SampleBatchBuffer batchBuffer;
    
	private String classification;

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
    float valueOfGravity;
    private OptionQueries optionQuery;
    private TestAVQueries testavQuery;
    
    private CalcStatistics calcSampleStatistics = new CalcStatistics(3);
    private RotateSamplesToVerticalHorizontal rotateSamples = new RotateSamplesToVerticalHorizontal();
    private Classifier classifier;
    
    private boolean shouldExit;

    public ClassifierThread(Context context, ActivityRecorderBinder service, SampleBatchBuffer sampleBatchBuffer) {
    	this.service = service;
    	this.batchBuffer = sampleBatchBuffer;
    	
        uncarried =  false;
        testavQuery = new TestAVQueries(context);
        
        this.classifier = new Classifier(RecorderService.model.entrySet());
        this.optionQuery = new OptionQueries(context);
        
        this.shouldExit = false;
	}
    
    /**
     * Stops the thread cautiously
     */
    public synchronized void exit() {
    	//	signal the thread to exit
    	this.shouldExit = false;
    	
		//	if the thread is blocked waiting for a filled batch
		//		interrupt the thread
		this.interrupt();
    }
    
    /**
     * Classification start
     */
    public void run() {
    	
    	Log.v(Constants.DEBUG_TAG, "Classification thread started.");
    	while (!this.shouldExit) {
	        try {
	        	//	incase of too sampling too fast, or too slow CPU, or the classification taking too long
	        	//		check how many batches are pending
	        	int pendingBatches = batchBuffer.getPendingFilledBatches();
	        	if (pendingBatches==SampleBatchBuffer.TOTAL_BATCH_COUNT) {
	        		//	issue an error if too many
	        		service.showServiceToast("Unable to classify sensor data fast enough!");
	        	}
	        	
	        	// this function blocks until a filled sample batch is obtained
	        	SampleBatch batch = batchBuffer.takeFilledBatch();
//	        	Log.v(Constants.DEBUG_TAG, "Received filled batch for analysis.");
	        	
	        	//	process the sample batch to obtain the classification
	        	processData(batch);

//	        	Log.v(Constants.DEBUG_TAG, "Analysis done. Returning batch.");
	        	//	return the sample batch to the buffer as an empty batch
	        	batchBuffer.returnEmptyBatch(batch);
	        	//	submit the classification
	        	service.submitClassification(classification);
	        } catch (RemoteException ex) {
	        	Log.e(Constants.DEBUG_TAG, "Exception error occured in connection in ClassifierService class");
	        } catch (InterruptedException e) {
			}
    	}
    	Log.v(Constants.DEBUG_TAG, "Classification thread exiting.");

    }

    private void processData(SampleBatch batch) {
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

    	float[][] data = batch.data;
    	int size = batch.getSize();
		String lastClassificationName = batch.getLastClassificationName();
		float[] ignore = batch.getIgnore();
		int isCalibrated;		
		
    	//	first rotate samples to world-orientation
    	
		//	the model data isn't rotated yet...
    	if (rotateSamples.rotateToWorldCoordinates(data)) {
	    	
			isCalibrated = optionQuery.getCalibrationState();
			// read sensor standard deviation from the database
			ssd[0] = optionQuery.getStandardDeviationX();
			ssd[1] = optionQuery.getStandardDeviationY();
			ssd[2] = optionQuery.getStandardDeviationZ();
			valueOfGravity = optionQuery.getCalibrationValue();
			
			float[] sd = new float[3];
			float[] average = { 0, 0, 0 };
			
			calcSampleStatistics.assign(data, size);
			
			average = calcSampleStatistics.getMean();
			sd = calcSampleStatistics.getStandardDeviation();
			
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
						valueOfGravity = calibration.getValueOfGravity();
						Log.i("Calibration", "saved in datastore");
						optionQuery.setCalibrationState("1");
						optionQuery.setCalibrationValue(valueOfGravity+"");
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
			else
			{
				// ignore the very first classification
				if (ignore[0] != 1) {
					if (!keepLastAvgAccel) {
						lastaverage[0] = 0;
						lastaverage[1] = 0;
						lastaverage[2] = 0;
					}
					classification = classifier.classifyRotated(data);
					keepLastAvgAccel = false;
				} else {
					classification = "CLASSIFIED/WAITING";
				}
			}
			// }
			
			// Log.i(getClass().getName(), "Classification: " + classification);
    	}
    }
    
}


