

package activity.classifier;

import activity.classifier.service.RecorderService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * 
 * BootReceiver is the class, which extends BroadcastReceiver, for starting RecorderService class at the phone booting.
 * 
 * To receive the boot status, a permission should be registered in AndroidManifest.xml file
 * (<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />)	 
 * 
 * @author Justin Lee
 * @see android.content.BroadcastReceiver
 */
public class BootReceiver extends BroadcastReceiver{

	/**
	 * 
	 */
	static final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";


	@Override 
	public void onReceive(Context context, Intent intent) { 

		if(intent.getAction().equals(BOOT_ACTION)){
			Intent i = new Intent(context, RecorderService.class);   
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			context.startService(i); 
			Log.i("Check", "Confirmed running application");
		}
	}  
}
