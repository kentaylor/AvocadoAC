package activity.classifier;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SentToAccount extends Service{
	private ProgressDialog dialog;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	 public void onStart(final Intent intent, final int startId) {
	        super.onStart(intent, startId);
	        dbAdapter = new DbAdapter(this);
	        String account = intent.getStringExtra("UID");
	        String[] UID = account.split("@");
	        Log.i("sendpost","ok");
	        sendpost(UID[0]);
   	    	dbAdapter.open();
   	    	dbAdapter.updateStart(7, 0+"");
   	    	Log.i("sendpost","Sented!!");
   	    	dbAdapter.close();
   	    	stopSelf();
//            dialog = ProgressDialog.show(SentToAccount.this, "Please wait",
//                    "Submitting...", true);
	    }
	 private DbAdapter dbAdapter;
	 public String getIMEI() {
	        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
	    }
	    public void sendpost(String account){
    		Log.i("sendpost","UID"+account);
			HttpClient client = new DefaultHttpClient();
		      final HttpPost post = new HttpPost("http://testingjungoo.appspot.com/accountservlet");
		      final File file = getFileStreamPath("activityrecords.db");
		      final FileEntity entity = new FileEntity(file, "text/plain");
		      
		    
			      try {
		  	      	
			    	 post.setHeader("UID",account);
			    	 post.setHeader("IMEI",getIMEI());
		  	         post.setEntity(entity);
		   	    	int code = new DefaultHttpClient().execute(post).getStatusLine().getStatusCode();

//		   	    	Toast.makeText(SentToAccount.this, "Submission completed", Toast.LENGTH_SHORT).show();
	     
			            } catch (IOException ex) {
			                Log.e(getClass().getName(), "Unable to upload sensor logs", ex);
//			                Toast.makeText(SentToAccount.this, "Submission failed, check phone's Internet connectivity and try again.", Toast.LENGTH_SHORT).show();
				   	    	dbAdapter.open();
				   	    	dbAdapter.updateStart(7, 1+"");
				   	    	Log.i("sendpost","NotSented!!");
				   	    	dbAdapter.close();
			            }
		     

	    }
}
