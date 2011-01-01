/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package activity.classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import activity.classifier.rpc.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

/**
 *
 * @author chris
 * @modified Justin
 * 
 * 
 */
public class ActivityRecorderActivity extends Activity {

    ActivityRecorderBinder service = null;
    ProgressDialog dialog;
    boolean cData = false;
    final Handler handler = new Handler();
    private DbAdapter dbAdapter;
    private static final int up_id = 1;
    private static final int sleep_id = 2;
    private static final int style_group = 1;
    
    public IBinder onBind(Intent arg0) {
        return null;
    }
    //Function for creating Menu
    public boolean onCreateOptionsMenu(Menu menu){

    	super.onCreateOptionsMenu(menu);

    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.layout.menu, menu);
    	return true;
    }
    //Function for being disable menu items when app is running 
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	menu.findItem(R.id.deletedata).setEnabled(cData);
    	menu.findItem(R.id.deletedata).setIcon(android.R.drawable.ic_menu_delete);
    	menu.findItem(R.id.copydata);
    	menu.findItem(R.id.copydata).setIcon(android.R.drawable.ic_menu_edit);
    	menu.findItem(R.id.link);
    	menu.findItem(R.id.link).setIcon(android.R.drawable.ic_menu_view);
    	menu.findItem(R.id.wake);
    	menu.findItem(R.id.wake).setIcon(android.R.drawable.ic_menu_set_as);
    	return true;
    }

    //Function for Menu item listener
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	dbAdapter = new DbAdapter(this);
    	switch(item.getItemId()){
    	//If user click the "delete database"
    	case R.id.deletedata:
    		 try {
                 if (service.isRunning()) {
                 	
                 } else {
                 	File f1 = new File("data/data/activity.classifier/databases/activityrecords.db");
                 	f1.delete();
                 }
             } catch (RemoteException ex) {
                 Log.e(getClass().getName(), "Unable to get service state", ex);
             }
             break;
         //if user click the "copy database to SDcard"
    	case R.id.copydata:
    		try {
               
            		File sdcard = Environment.getExternalStorageDirectory();
            	    File dbpath = new File(sdcard.getAbsolutePath() + File.separator + "activityclassfier");
            	    if(!dbpath.exists()){
            	    	if(true) Log.d("", "Create DB directory. " + dbpath.getAbsolutePath());
            	    	dbpath.mkdirs();
            	    }
            	    	
            	    String dbfile = dbpath.getAbsolutePath() + File.separator + "activityrecords.db";
                	copy("data/data/activity.classifier/databases/activityrecords.db",dbfile);
                
            } catch (Exception ex) {
                Log.e(getClass().getName(), "Unable to get service state", ex);
            }
            break;
    	case R.id.link:
    		try {
    			String url = "http://testingjungoo.appspot.com/actihistory.jsp";
    			Intent i = new Intent(Intent.ACTION_VIEW);
    			i.setData(Uri.parse(url));
    			startActivity(i);
            } catch (Exception ex) {
                Log.e(getClass().getName(), "Unable to get service state", ex);
            }
            break;
    	case R.id.wake: 
   		 try {
               
   			startActivity(new Intent(this, ScreenSettingActivity.class));
            } catch (Exception ex) {
                Log.e(getClass().getName(), "Unable to get service state", ex);
            }
            break;
    	}
    	return true;
    	
    }
    private boolean wakelock=false;
    
    //Chris work
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
            handler.postDelayed(updateRunnable, 500);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
            handler.removeCallbacks(updateRunnable);
            ((Button) findViewById(R.id.togglebutton)).setEnabled(false);
        }
    };

    private final Runnable updateRunnable = new Runnable() {

        public void run() {
            try {
				updateButton();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            handler.postDelayed(updateRunnable, 500);
        }
    };

    private final Runnable startRunnable = new Runnable() {

        public void run() {
        	
            startService(new Intent(ActivityRecorderActivity.this,
                    RecorderService.class));

            Log.i("Activity","Started!!!");
            updateRunnable.run();
        }
    };

    //Function for Start/Stop button listener
    private OnClickListener clickListener = new OnClickListener() {

        public void onClick(View arg0) {
            try {
                if (service.isRunning()) {
                	cData=true;
                	stop=true;
                	Date date = new Date(System.currentTimeMillis());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

                    String startTime = dateFormat.format(date) ;
                    dbAdapter.open();
                    dbAdapter.insertActivity("END",startTime,   0,0);
                    dbAdapter.close();

                    FlurryAgent.onEvent("recording_stop");

                    stopService(new Intent(ActivityRecorderActivity.this,
                            RecorderService.class));
                    unbindService(connection);
                    bindService(new Intent(ActivityRecorderActivity.this, RecorderService.class),
                            connection, BIND_AUTO_CREATE);

                } 
                else {
                	cData=false;
                    FlurryAgent.onEvent("recording_start");
                    handler.removeCallbacks(updateRunnable);
                    ((Button) findViewById(R.id.togglebutton)).setEnabled(false);

                    dialog = ProgressDialog.show(ActivityRecorderActivity.this, "Starting service",
                        "Please wait...", true);
                    
    				
    				    handler.postDelayed(startRunnable, 500);
    				
                    
                }
            } catch (RemoteException ex) {
                Log.e(getClass().getName(), "Unable to get service state", ex);
            }
        }
    };

    private static boolean stop = false;
    private static int start=1;
    private static int setWL=1;
    private PowerManager.WakeLock wl;
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        dbAdapter = new DbAdapter(this);
        dbAdapter.open();
        Cursor result =    dbAdapter.fetchStart(1);
        start = (int) Float.valueOf(result.getString(1).trim()).floatValue();;
        result.close();
        Cursor result6 =    dbAdapter.fetchStart(8);
        setWL = (int) Float.valueOf(result6.getString(1).trim()).floatValue();;
        result6.close();
    	dbAdapter.close();
    	if(setWL==1){
    		this.wakelock=false;
    	}else{
    		this.wakelock=true;
    	}
        bindService(new Intent(this, RecorderService.class), connection, BIND_AUTO_CREATE);

        setContentView(R.layout.main);
        ((Button) findViewById(R.id.togglebutton)).setEnabled(false);
        ((Button) findViewById(R.id.togglebutton)).setOnClickListener(clickListener);
        ((ListView) findViewById(R.id.list)).setAdapter(
                new ArrayAdapter<Classification>(this, R.layout.item));
        if(start==0){
        }else{
			if(!stop){
			FlurryAgent.onEvent("recording_start");
			handler.removeCallbacks(updateRunnable);
			((Button) findViewById(R.id.togglebutton)).setEnabled(false);
			dialog = ProgressDialog.show(ActivityRecorderActivity.this, "Starting service",
			    "Please wait...", true);
		    handler.postDelayed(startRunnable, 500);
			}
        }
    }
    public static boolean serviceIsRunning =false;
    /** {@inheritDoc} */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop=true;
        start = 0;
        unbindService(connection);

    }
    protected void onResume() {
        super.onResume();
        
    }
    protected void onPause() {
        super.onPause();
        
    }

    @SuppressWarnings("unchecked")
    void updateButton() throws ParseException {
    	
        try {
            ((Button) findViewById(R.id.togglebutton)).setText(service.isRunning()
                    ? R.string.service_enabled : R.string.service_disabled);
            ((Button) findViewById(R.id.togglebutton)).setEnabled(true);

            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
            
            final List<Classification> classifications = service.getClassifications();
            final ArrayAdapter<Classification> adapter = (ArrayAdapter<Classification>)
                    ((ListView) findViewById(R.id.list)).getAdapter();

            if (classifications.isEmpty()) {
                adapter.clear();
            } else if (!adapter.isEmpty()) {
                final Classification myLast = adapter.getItem(adapter.getCount() - 1);
                final Classification expected = classifications.get(adapter.getCount() - 1);

                if (myLast.getClassification().equals(expected.getClassification())) {
                    myLast.updateEnd(expected.getEnd());
                    adapter.notifyDataSetChanged();
                    
                } 
                else {
                    adapter.clear();
                }
            }

            for (int i = adapter.getCount(); i < classifications.size(); i++) {
                adapter.add(classifications.get(i).withContext(this));
            }

        } catch (RemoteException ex) {
            Log.e(getClass().getName(), "Unable to get service state", ex);
        } 
        
    }

    public String getVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException ex) {
            return "Unknown";
        }
    }

    public String getIMEI() {
        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
    }

    /** {@inheritDoc} */
    @Override
    protected void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this, "EMKSQFUWSCW51AKBL2JJ");
    }

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        super.onStop();
        stop=true;
//        wl.release();
        FlurryAgent.onEndSession(this);
    }
    
    //Function for copy database file to SDcard
    public static void copy( String targetFile, String copyFile )
    {
     try {
      
        
      InputStream lm_oInput = new FileInputStream(new File(targetFile));
      byte[] buff = new byte[ 128 ];
      FileOutputStream lm_oOutPut = new FileOutputStream( copyFile );
      while( true )
      {
       int bytesRead = lm_oInput.read( buff );
       if( bytesRead == -1 ) break;
       lm_oOutPut.write( buff, 0, bytesRead );
      }

      lm_oInput.close();
      lm_oOutPut.close();
      lm_oOutPut.flush();
      lm_oOutPut.close();
     }
     catch( Exception e )
     {
     }
    }
   
}
