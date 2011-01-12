

package activity.classifier;

import activity.classifier.common.service.RecorderService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * @author Justin
 * BootReceiver: 
 * 				class for forcing to start RecorderService class at the phone booting
 * 				To use boot status, a permission should be resistered in AndroidManifest.xml file
 * 								(<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />)	 
 */
public class BootReceiver extends BroadcastReceiver{
    
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
