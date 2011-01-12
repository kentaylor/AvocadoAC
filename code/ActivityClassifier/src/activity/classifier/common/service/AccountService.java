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
 * AccountActivity class is used for sending user's google account nick-name in order to match the user's activity history
 * in web server.
 * It happens when the application runs at the very first time or next time if there is no account synced with the phone.
 * AccountManager is an API to get a user's current account name.
 * The type of account would be many depended on users, so it is required to specify the type as "com.google"
 * in order to get google account.
 * The number of google accounts would also be more than one but only single account is sent to the website for now.
 * 
 * Sending components : User account name, IMEI number, device model name
 */
/** {@inheritDoc} */
public class AccountService extends Service implements Runnable {
	
	ActivityRecorderBinder service = null;
    
	private String toastString;
    private String AccountName;
    private String ModelName;
    private String IMEI;
	
    private OptionQueries optionQuery;
	
	private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ActivityRecorderBinder.Stub.asInterface(arg1);

            try {
            	service.SetAccountStateToastString(toastString);
            } catch (RemoteException ex) {
            	Log.e("connection", "Exception error occured in connection in AccountService class");
            }
            
            stopSelf();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Toast.makeText(AccountService.this, R.string.error_disconnected, Toast.LENGTH_LONG);
        }
    };


    public void sendpost(String AccountName, String ModelName, String IMEI){
    	
    	if (AccountName!=null){
			HttpClient client = new DefaultHttpClient();
			final HttpPost post = new HttpPost("http://testingjungoo.appspot.com/accountservlet");
			final File file = getFileStreamPath("activityrecords.db");
			final FileEntity entity = new FileEntity(file, "text/plain");
			try {
				post.setHeader("UID",AccountName);
		    	post.setHeader("IMEI",IMEI);
		    	post.setHeader("Model",ModelName);
	  	        post.setEntity(entity);
	   	    	int code = new DefaultHttpClient().execute(post).getStatusLine().getStatusCode();
	   	    	setToastString("User Information submission completed.\n" +
	   	    			"   phone model  : "+ModelName+"\n" +
	   	    			"   account name : "+AccountName+"\n"+
	   	    			"   IMEI number  : "+IMEI);
	   	    	
	   	    	optionQuery.setAccountState("1");
	   	    	Log.i("sendpost","Sented!!");
	            } catch (IOException ex) {
	                Log.e(getClass().getName(), "Unable to upload sensor logs", ex);
	                setToastString("Submission failed, check phone's Internet connectivity and try again.");
	                optionQuery.setAccountState("0");
		   	    	Log.i("sendpost","NotSented!!");
	            } finally{
	            	Log.i("sendpost","getModel() "+ModelName);
	            	Log.i("sendpost","getIMEI() "+IMEI);
	            	Log.i("sendpost","account "+AccountName);
	            	 bindService(new Intent(this, RecorderService.class), connection, BIND_AUTO_CREATE);
	            }
    	}
	     

    }
    
    private void setToastString(String toastString){
    	this.toastString = toastString;
    }

    /** {@inheritDoc} */
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        optionQuery = new OptionQueries(this);
        AccountName = intent.getStringExtra("AccountName");
        ModelName = intent.getStringExtra("ModelName");
        IMEI = intent.getStringExtra("IMEI");
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
    
	public void run() {
    	sendpost(AccountName,ModelName,IMEI);
    	stopSelf();
	}

}
