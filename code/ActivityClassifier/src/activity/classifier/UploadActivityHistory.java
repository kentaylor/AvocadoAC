package activity.classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import activity.classifier.common.repository.ActivityQueries;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

/**
 * 
 * @author Justin Lee
 *
 */
public class UploadActivityHistory {
	
	protected AccountManager accountManager;

	private Timer timer;
	private ActivityQueries activityQuery;
    
	/**
	 * Initialise {@link ActivityQueries} class instance.
	 * @param context context from Activity or Service classes passes to {@link ActivityQueries} class instance.
	 */
	public UploadActivityHistory(Context context){
		activityQuery = new ActivityQueries(context);
    	String dbfile ="data/data/activity.classifier/files/activityrecords.db";
    	copy("data/data/activity.classifier/databases/activityrecords.db",dbfile);
	}
	
	/**
	 * cancel timer when the background service is destroyed.
	 */
	public void CancelTimer(){
		timer.cancel();
	}
	
	/**
	 * By using {@link ActivityQueries} class, check every un-sent activities from device repository,
	 * and upload un-sent activities from device repository to Web server.
	 * Timer scheduler is set to every 5 min.
	 *
	 * @param AccountName
	 */
	public void uploadDataToWeb(final String AccountName){
		/*
		 * get Timer instance and set the schedule to every 5 min.
		 */
		timer = new Timer("Data logger");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
        		HttpClient client = new DefaultHttpClient();
        		
        	    final HttpPost post = new HttpPost("http://testingjungoo.appspot.com/activity");
        	    final File file = new File("data/data/activity.classifier/files/activityrecords.db");
        	    final FileEntity entity = new FileEntity(file, "text/plain");
        	    
        	    // ArrayList data type for un-sent activities.
        	    ArrayList<String> ItemNames = new ArrayList<String>();
        	    ArrayList<String> ItemDates = new ArrayList<String>();
        	    ArrayList<Integer> ItemIDs = new ArrayList<Integer>();
        	    
        	   
        	    
        	    //open database and check the un-posted data
        	    activityQuery.getUncheckedItemsFromActivityTable(0);
        	    
        	    ItemIDs = activityQuery.getUncheckedItemIDs();
        	    ItemNames = activityQuery.getUncheckedItemNames();
        	    ItemDates = activityQuery.getUncheckedItemDates();
        	    
        	    //get the number of un-posted activities
        	    int size=activityQuery.getUncheckedItemsSize();
        	    
        	    Log.i("uncheckedItems",size+"");
        	    
        	    /*
        	     * if there are un-posted activities in the device repository,
        	     * then merge every information of activity into one string(message),
        	     * then upload to Web server.
        	     */
        	    if(size!=0){
        	    	String message = "";
        	    	//merge information of activities
        		    for(int i = 0 ; i<size;i++){
        		    	if(i==size){
        		    		message +=  ItemNames.get(i)+"&&"+ItemDates.get(i);
        		    	}else{
        		    		message +=  ItemNames.get(i)+"&&"+ItemDates.get(i)+"##";
        		    	}
        		    }
        		    
        		    //send un-posted activities with the size, date, and Google account to Web server.
        		    try {
        		    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        		    	Date systemdate = Calendar.getInstance().getTime();
        		    	String reportDate = df.format(systemdate);
        		    	post.setHeader("sysdate",reportDate);
        		    	post.setHeader("size",size+"");
        	  	        post.setHeader("message", message);
        	  	        post.setHeader("UID", AccountName);
        	  	        post.setEntity(entity);
        	   	    	
        	  	        int code = new DefaultHttpClient().execute(post).getStatusLine().getStatusCode();
        		        Log.i("m",message);
        		        
        		        // update un-posted items to posted in device repository.
        		        for(int i=0;i<size;i++){
        		        	activityQuery.updateUncheckedItems(ItemIDs.get(i), ItemNames.get(i), ItemDates.get(i), 1);
                		}
    	            // if any failure of the response
        		    } catch (IOException ex) {
        	            	Log.e(getClass().getName(), "Unable to upload sensor logs", ex);
        		    }
        	    }
        		
            }
        }, 300000, 300000);

	}
	/**
	 * A utility method which copy the database to SD card.
	 * @param targetFile database in the device repository.
	 * @param copyFile path to be copied
	 */
    public void copy( String targetFile, String copyFile ){
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
}
