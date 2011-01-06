/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 *
 * @author chris
 * modified by justin
 */
public class UploaderService extends Service {
	protected AccountManager accountManager;
	private String MODEL="";
    @Override 
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        
        MODEL=getModel();
        accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        
        runPost(accounts[0].name);

    }

    ArrayList<String> activity = new ArrayList<String>();
    ArrayList<String> date = new ArrayList<String>();
    public DbAdapter dbAdapter;
    public void runPost(String account) {
	      dbAdapter = new DbAdapter(this);
	      dbAdapter.open();
    	
		HttpClient client = new DefaultHttpClient();
	      final HttpPost post = new HttpPost("http://testingjungoo.appspot.com/activity");
	      final File file = getFileStreamPath("activityrecords.db");
	      final FileEntity entity = new FileEntity(file, "text/plain");
	      
	      ArrayList<String> activity = new ArrayList<String>();
	      ArrayList<String> date = new ArrayList<String>();
	      ArrayList<Integer> id = new ArrayList<Integer>();
	      //open database and check the un-posted data and send that data 

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
	//    	      for(int i=0;i<activity.size();i++){
	//    	    	  for(int j=0;j<activity.size();j++){
	//    	    		  if(i!=j){
	//	    	    		  if(!date.get(i).equals(date.get(j))){
	//	    	    			  this.activity.add(activity.get(i));
	//	    	    			  this.date.add(date.get(i));
	//	    	    		  }
	//    	    		  }
	//    	    	  }
	//    	    	  
	//    	      }
	    	      
	    	      
		      String message = "";
		      Log.i("size?",activity.size()+"");
	//	      for(int i = 0 ; i<this.activity.size();i++){
	//	    	  Log.i("Series",this.activity.get(i));
	//	    	  message +=  this.activity.get(i)+"&&"+this.date.get(i)+"##";
	//
	//	      }
		      for(int i = 0 ; i<activity.size();i++){
		    	  
		    	  if(i==activity.size()){
		    		  message +=  activity.get(i)+"&&"+date.get(i);
		    	  }
		    	  else{
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
	//	            	HttpResponse response = client.execute(post);
	//	            	HttpEntity resEntity = response.getEntity();
		            	Log.i("m",message);
		            		for(int i=0;i<id.size();i++){
		            			dbAdapter.open();
		            			dbAdapter.updateActivity(id.get(i), activity.get(i), date.get(i), 1,1);
		            			dbAdapter.close();
		            		}
		            	
	//	                
		            } catch (IOException ex) {
		                Log.e(getClass().getName(), "Unable to upload sensor logs", ex);
		                for(int i=0;i<id.size();i++){
		                	dbAdapter.open();
	            			dbAdapter.updateActivity(id.get(i), activity.get(i), date.get(i), 0,0);
	            			dbAdapter.close();
		                }
		            }
	      }
	      this.activity.clear();
	      this.date.clear();
	      

		stopSelf();
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    public String getIMEI() {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
    }
    public String getModel() {
        return android.os.Build.MODEL;

    }
}
