/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier;

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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import activity.classifier.R;

import com.flurry.android.FlurryAgent;



/**
 *
 * @author chris
 */
public class AccountActivity extends Activity  {
	  private ProgressDialog dialog;
    /** {@inheritDoc} */
	  protected AccountManager accountManager;
		 private DbAdapter dbAdapter;
    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.account);
        
        accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        
        if(accounts.length!=0){
        	dbAdapter = new DbAdapter(this);
//        	dialog = ProgressDialog.show(AccountActivity.this, "Please wait",
//                    "Submitting...", true);
        	sendpost(accounts[0].name);

            finish();
        }
        else{
        	Log.i("sendpost","elseok");
//        	dialog = ProgressDialog.show(AccountActivity.this, "Please wait",
//                    "Submitting...", true);
            finish();
        }

    }

    public String getIMEI() {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
    }
    public String getModel() {
        return android.os.Build.MODEL;

    }
    public void sendpost(String account){

		HttpClient client = new DefaultHttpClient();
	      final HttpPost post = new HttpPost("http://testingjungoo.appspot.com/accountservlet");
	      final File file = getFileStreamPath("activityrecords.db");
	      final FileEntity entity = new FileEntity(file, "text/plain");
	      
	    
		      try {
	  	      	 
		    	 post.setHeader("UID",account);
		    	 post.setHeader("IMEI",getIMEI());
		    	 post.setHeader("Model",getModel());
	  	         post.setEntity(entity);
	   	    	int code = new DefaultHttpClient().execute(post).getStatusLine().getStatusCode();
	   	    	Toast.makeText(AccountActivity.this, "Submission completed", Toast.LENGTH_SHORT).show();
	   	    	//Toast.makeText(AccountActivity.this, getModel(), Toast.LENGTH_LONG).show();
	   	    	dbAdapter.open();
	   	    	dbAdapter.updateStart(7, 0+"");
	   	    	Log.i("sendpost","Sented!!");
	   	    	dbAdapter.close();
		            } catch (IOException ex) {
		                Log.e(getClass().getName(), "Unable to upload sensor logs", ex);
		                Toast.makeText(AccountActivity.this, "Submission failed, check phone's Internet connectivity and try again.", Toast.LENGTH_SHORT).show();
		                dbAdapter.open();
			   	    	dbAdapter.updateStart(7, 1+"");
			   	    	Log.i("sendpost","NotSented!!");
			   	    	dbAdapter.close();
		            }
	     

    }
   

    /** {@inheritDoc} */
    @Override
    protected void onStart() {
        super.onStart();
        
        FlurryAgent.onStartSession(this, "TFBJJPQUQX3S1Q6IUHA6");
    }

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        super.onStop();

        FlurryAgent.onEndSession(this);
    }

	

}
