/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier.common.service;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import activity.classifier.R;
import activity.classifier.R.string;
import activity.classifier.common.repository.OptionQueries;
import activity.classifier.rpc.ActivityRecorderBinder;

import com.flurry.android.FlurryAgent;



/**
 * 
 * AccountService class is used for sending user's google account nick-name in order to match the user's activity history
 * in web server.
 * It happens when the application runs at the very first time or next time if there is no account synced with the phone.
 * AccountManager is an API to get a user's current account name.
 * The type of account would be many depended on users, so it is required to specify the type as "com.google"
 * in order to get google account.
 * The number of google accounts would also be more than one but only single account is sent to the website for now.
 * 
 * Sending components : User account name, IMEI number, device model name
 * 
 * 
 * @author Justin Lee
 *
 */
public class AccountService extends Service implements Runnable {
	
    
	private String toastString;
    private String AccountName;
    private String ModelName;
    private String IMEI;
	
    private OptionQueries optionQuery;
	


    /**
     * A method to post user's Google account, device model name, and IMEI to Web server
     * @param AccountName user's Google account
     * @param ModelName device model name
     * @param IMEI IMEI number
     */
    private void postUserDetail(String AccountName, String ModelName, String IMEI){
    	
    	if (AccountName!=null){
			HttpClient client = new DefaultHttpClient();
			final HttpPost post = new HttpPost("http://testingjungoo.appspot.com/accountservlet");
			final File file = getFileStreamPath("activityrecords.db");
			final FileEntity entity = new FileEntity(file, "text/plain");
			
			//post user's information
			try {
				post.setHeader("UID",AccountName);
		    	post.setHeader("IMEI",IMEI);
		    	post.setHeader("Model",ModelName);
	  	        post.setEntity(entity);
	  	        
	  	        /*
	  	         *  integer data type variable, code, store a state value of the Internet response.
	  	         *  For now, an error occurs when there is just no Internet connection, 
	  	         *  but will use this variable to filter among the various of the Internet response states.
	  	         */
	   	    	int code = new DefaultHttpClient().execute(post).getStatusLine().getStatusCode();
	   	    	
	   	    	//set the pop-up message
	   	    	setToastString("User Information submission completed.\n" +
	   	    			"   phone model  : "+ModelName+"\n" +
	   	    			"   account name : "+AccountName+"\n"+
	   	    			"   IMEI number  : "+IMEI);
	   	    	
	   	    	//set the account state to 1 (true)
	   	    	optionQuery.setAccountState("1");
	   	    	Log.i("postUserDetail","posted");
            
			} catch (IOException ex) {
	                Log.e(getClass().getName(), "Unable to upload sensor logs", ex);
	                //set the pop-up message
	                setToastString("Submission failed,\n check phone's Internet connectivity and try again.");
	                
	                //set the account state to 0 (false)
	                optionQuery.setAccountState("0");
		   	    	Log.i("postUserDetail","Not posted");
            } 
    	}
    }

    /**
     * set the toast message to feedback to the user
     * @param toastString feedback message based on the account states
     */
    private void setToastString(String toastString){
    	this.toastString = toastString;
    }


    /**
     * Initialise variables (AccountName, ModelName, IMEI)
     */
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        optionQuery = new OptionQueries(this);
        
        //get these values from RecorderService.java 
        AccountName = intent.getStringExtra("AccountName");
        ModelName = intent.getStringExtra("ModelName");
        IMEI = intent.getStringExtra("IMEI");
        
        new Thread(this).start();
    
    }

    /**
     * when posting is finished, toast a feedback message
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(AccountService.this, toastString, Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    /**
     * run once, then stop
     */
	public void run() {
		postUserDetail(AccountName,ModelName,IMEI);
    	stopSelf();
	}

}
