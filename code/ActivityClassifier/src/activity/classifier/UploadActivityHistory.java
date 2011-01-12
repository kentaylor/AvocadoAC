package activity.classifier;

import java.io.File;
import java.io.IOException;
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

public class UploadActivityHistory {
	
	protected AccountManager accountManager;
	
	private Timer timer;
	
	private ActivityQueries activityQuery;
    
	public UploadActivityHistory(Context context){
		activityQuery = new ActivityQueries(context);
	}
	
	public void CancelTimer(){
		timer.cancel();
	}
	
	public void uploadDataToWeb(final String AccountName){
		timer = new Timer("Data logger");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
        		HttpClient client = new DefaultHttpClient();
        	    final HttpPost post = new HttpPost("http://testingjungoo.appspot.com/activity");
        	    final File file = new File("data/data/activity.classifier/files/activityrecords.db");
        	    final FileEntity entity = new FileEntity(file, "text/plain");
        	      
        	    ArrayList<String> ItemNames = new ArrayList<String>();
        	    ArrayList<String> ItemDates = new ArrayList<String>();
        	    ArrayList<Integer> ItemIDs = new ArrayList<Integer>();
        	    
        	   
        	    
        	    //open database and check the un-posted data and send that data 
        	   
        	    activityQuery.getUncheckedItemsFromActivityTable(0);
        	    ItemIDs = activityQuery.getUncheckedItemIDs();
        	    ItemNames = activityQuery.getUncheckedItemNames();
        	    ItemDates = activityQuery.getUncheckedItemDates();
        	    int size=activityQuery.getUncheckedItemsSize();
        	    Log.i("uncheckedItems",size+"");
        	    if(size!=0){
        	    	String message = "";
        		    Log.i("size?",size+"");
        		    for(int i = 0 ; i<size;i++){
        		     
        		    	if(i==size){
        		    		message +=  ItemNames.get(i)+"&&"+ItemDates.get(i);
        		    	}else{
        		    		message +=  ItemNames.get(i)+"&&"+ItemDates.get(i)+"##";
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
        		    	post.setHeader("size",size+"");
        	  	        post.setHeader("message", message);
        	  	        post.setHeader("UID", AccountName);
        	  	        post.setEntity(entity);
        	   	    	
        	  	        int code = new DefaultHttpClient().execute(post).getStatusLine().getStatusCode();
        		        Log.i("m",message);
        		        
        		        for(int i=0;i<size;i++){
        		        	activityQuery.updateUncheckedItems(ItemIDs.get(i), ItemNames.get(i), ItemDates.get(i), 1);
                		}
        	            } catch (IOException ex) {
        	            	Log.e(getClass().getName(), "Unable to upload sensor logs", ex);
        		        for(int i=0;i<size;i++){
        		        	activityQuery.updateUncheckedItems(ItemIDs.get(i), ItemNames.get(i), ItemDates.get(i), 0);
        		        }
        		    }
        	    }
        		
            }
        }, 300000, 300000);

	}
}
