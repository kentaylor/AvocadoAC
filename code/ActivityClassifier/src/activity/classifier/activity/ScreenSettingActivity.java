/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier.activity;

import activity.classifier.R;
import activity.classifier.R.id;
import activity.classifier.R.layout;
import activity.classifier.repository.OptionQueries;
import activity.classifier.rpc.ActivityRecorderBinder;
import activity.classifier.service.RecorderService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;



/**
 * 
 * ScreensettingActivity is an UI Activity to set the screen lock.
 * User have an option to decide to lock the screen or not.
 * It is due to the bug related to onSensorChanged() in Android SensorEventListener API.
 * Recommend to keep the screen on except the phone is NEXUS One.
 * 
 * @author Justin Lee
 *
 */
public class ScreenSettingActivity extends Activity implements OnCheckedChangeListener  {
	

	ActivityRecorderBinder service = null;
	CheckBox checkBox;

	private int isWakeLockSet;
	private boolean wakelock;
	
	private OptionQueries optionQuery;

	/**
	 * When the Service connection is established in this class,
	 * bind the Wake Lock status to notify RecorderService.
	 */
    private final ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ActivityRecorderBinder.Stub.asInterface(arg1);
            try {
				service.setWakeLock(wakelock);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			
		}

    };
    
    /**
     * 
     */
    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        // set the screen display check box and text message.
        setContentView(R.layout.setting);
        checkBox = (CheckBox)findViewById(R.id.check1);
        checkBox.setOnCheckedChangeListener(this);

    	//make new OptionQueries instance to get the current Wake Lock status. 
    	optionQuery = new OptionQueries(this);
    	isWakeLockSet = optionQuery.getWakeLockState();
    	
    	// 0 (false) if Wake lock is not set 
    	if(isWakeLockSet==0){
    		this.wakelock=false;
    		
    	}else{
    		this.wakelock=true;
    	}

    	bindService(new Intent(this, RecorderService.class),
                connection, BIND_AUTO_CREATE);
    	checkBox.setChecked(wakelock);
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


    /**
     * Update Wake Lock status in the database.
     */
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		if(arg0.getId() == R.id.check1){
			//if Wake Lock is checked
			if(arg1){
				wakelock=arg1;//true value
				Toast.makeText(this, "Screen Locked On", Toast.LENGTH_SHORT).show();
				//update Wake Lock state to 1 (true)
				optionQuery.setWakeLockState("1");
				
		    	unbindService(connection);
	            bindService(new Intent(this, RecorderService.class),
	                    connection, BIND_AUTO_CREATE);
			}
			else{
				wakelock=arg1;
				Toast.makeText(this, "Screen Locked Off", Toast.LENGTH_SHORT).show();
				
				optionQuery.setWakeLockState("0");
				
		    	unbindService(connection);
	            bindService(new Intent(this, RecorderService.class),
	                    connection, BIND_AUTO_CREATE);
			}
		}
	}
	
	/**
	 * 
	 */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(connection);

    }

	

}
