/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier;

import activity.classifier.rpc.ActivityRecorderBinder;
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
 * @author chris
 */
public class ScreenSettingActivity extends Activity implements OnCheckedChangeListener  {
	private DbAdapter dbAdapter;
	ActivityRecorderBinder service = null;
	CheckBox c1;
    private final ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ActivityRecorderBinder.Stub.asInterface(arg1);
            try {
            	Log.i("WAKE","connection");
				service.SetWakeLock(wakelock);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			
		}

    };
    public int setWL;
    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.setting);
        c1 = (CheckBox)findViewById(R.id.check1);
        c1.setOnCheckedChangeListener(this);
        bindService(new Intent(this, RecorderService.class),
                connection, BIND_AUTO_CREATE);
        dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        Cursor result6 =    dbAdapter.fetchStart(8);
        setWL = (int) Float.valueOf(result6.getString(1).trim()).floatValue();;
        result6.close();
    	dbAdapter.close();
    	if(setWL==1){
    		this.wakelock=false;
    		
    	}else{
    		this.wakelock=true;
    	}
    	c1.setChecked(wakelock);
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


    private boolean wakelock;
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		dbAdapter = new DbAdapter(this);
		if(arg0.getId() == R.id.check1){
			if(arg1){
				wakelock=arg1;
				Toast.makeText(this, "Screen Locked On", Toast.LENGTH_SHORT).show();
		    	dbAdapter.open();
	    		dbAdapter.updateStart(8, 0+"");
		    	dbAdapter.close();
		    	unbindService(connection);
	            bindService(new Intent(this, RecorderService.class),
	                    connection, BIND_AUTO_CREATE);
			}
			else{
				wakelock=arg1;
				Toast.makeText(this, "Screen Locked Off", Toast.LENGTH_SHORT).show();
		    	dbAdapter.open();
	    		dbAdapter.updateStart(8, 1+"");
		    	dbAdapter.close();
		    	unbindService(connection);
	            bindService(new Intent(this, RecorderService.class),
	                    connection, BIND_AUTO_CREATE);
			}
		}
	}

	

}
