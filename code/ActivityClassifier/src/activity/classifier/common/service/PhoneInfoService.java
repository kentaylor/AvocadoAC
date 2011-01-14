package activity.classifier.common.service;

import activity.classifier.R;
import activity.classifier.R.string;
import activity.classifier.rpc.ActivityRecorderBinder;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * a utility class which read user and device information and pass them to other classes to use.
 * 
 * @author Justin Lee
 *
 */
public class PhoneInfoService extends Service {
	
	protected AccountManager accountManager;
	
	ActivityRecorderBinder service = null;
	
	/**
	 * whe the connection is established, submit the user and device information 
	 */
	private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ActivityRecorderBinder.Stub.asInterface(arg1);

            try {
            	service.SetPhoneInformation(getAccountName(), getModel(), getIMEI());
            } catch (RemoteException ex) {
            	Log.e("connection", "Exception error occured in connection in PhoneInfoService class");
            }
            
            stopSelf();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Toast.makeText(PhoneInfoService.this, R.string.error_disconnected, Toast.LENGTH_LONG);
        }
    };
    
    /**
     * Get the device model name
     * @return device model name
     */
    public String getModel() {
        return android.os.Build.MODEL;
    }
    
    /**
     * Get the device IMEI number
     * @return device IMEI number
     */
    public String getIMEI() {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
    }
    
    /**
     * Get the user's Google account name
     * @return user's Google account name
     */
    public String getAccountName() {
    	accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        final String account = (accounts.length!=0 ? accounts[0].name : null);
        return account;
    }
    
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        bindService(new Intent(this, RecorderService.class), connection, BIND_AUTO_CREATE);
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
}
