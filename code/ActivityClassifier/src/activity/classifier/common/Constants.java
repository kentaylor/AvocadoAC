package activity.classifier.common;

import activity.classifier.service.RecorderService;
import activity.classifier.service.threads.AccountThread;
import activity.classifier.service.threads.UploadActivityHistoryThread;

/**
 * 
 * @author Umran
 *
 */
public class Constants {
	
	/**
	 * Duration for the {@link AccountThread} to wait for the user's account
	 * to be set on the phone before checking again.
	 */
	public static final int DURATION_WAIT_FOR_USER_ACCOUNT = 60*60*1000; // 1 hr in ms
	
	/**
	 * The interval between successive user interface updates in the {@link ActivityRecorderActivity}
	 */
	public static final int DELAY_UI_UPDATE = 500;
	
	public static final int DELAY_UI_GRAPHIC_UPDATE = 15000;
	
	/**
	 * The interval between successive data uploads in {@link UploadActivityHistoryThread}
	 */
	public static final int DELAY_UPLOAD_DATA = 300*1000;	//	5min in ms
	
	/**
	 * The delay after the dialog appears and before the {@link RecorderService} in
	 * the 'start service' display sequence in the class {@link ActivityRecorderActivity}
	 */
	public static final int DELAY_SERVICE_START = 500;

	/**
	 * The delay between two consecutive sampling batches.
	 */
	public static final int DELAY_SAMPLE_BATCH = 30*1000; //	30 secs in ms
//	public static final int DELAY_SAMPLE_BATCH = 10*1000; //	debugging use
	
	/**
	 * The delay between two consecutive samples in a sample batch.
	 */
	public static final int DELAY_BETWEEN_SAMPLES = 50; //	50ms
	
	/**
	 * A tag that can be used to identify this application's log entries.
	 */
	public static final String DEBUG_TAG = "ActivityClassifier";
	
	/**
	 * Records file name.
	 */
	public static final String RECORDS_FILE_NAME = "activityrecords.db";	
	
	/**
	 * Path to the activity records database
	 */
	public static final String PATH_ACTIVITY_RECORDS_DB = "data/data/activity.classifier/databases/"+RECORDS_FILE_NAME;

	/**
	 * Path to the activity records file
	 */
	public static final String PATH_ACTIVITY_RECORDS_FILE = "data/data/activity.classifier/files/"+RECORDS_FILE_NAME;
	
	/**
	 * URL where the user's information is posted.
	 */
	public static final String URL_USER_DETAILS_POST = "http://activity.urremote.com/accountservlet";
	
	/**
	 * URL to where the user's activity history can be viewed.
	 */
	public static final String URL_ACTIVITY_HISTORY = "http://activity.urremote.com/actihistory.jsp";
	
	/**
	 * URL where the user's activity data is posted
	 */
	public static final String URL_ACTIVITY_POST = "http://activity.urremote.com/activity";

	
	/**
	 *	The number of accelerometer (x,y & z) samples in a batch of samples.
	 */
	public static final int NUM_OF_SAMPLES_PER_BATCH = 128;
}
