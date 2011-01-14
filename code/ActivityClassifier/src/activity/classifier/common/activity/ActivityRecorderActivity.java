/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

package activity.classifier.common.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import activity.classifier.R;
import activity.classifier.R.id;
import activity.classifier.R.layout;
import activity.classifier.R.string;
import activity.classifier.common.CommonDef;
import activity.classifier.common.repository.OptionQueries;
import activity.classifier.common.service.RecorderService;
import activity.classifier.rpc.ActivityRecorderBinder;
import activity.classifier.rpc.Classification;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

/**
 * 
 * @author chris, modified Justin Lee
 * 
 */
public class ActivityRecorderActivity extends Activity {

	private static final String PATH_ACTIVITY_RECORDS_DB = "data/data/activity.classifier/databases/activityrecords.db";
	private static final String URL_ACTIVITY_HISTORY = "http://testingjungoo.appspot.com/actihistory.jsp";

	private static final int DELAY_UI_UPDATE = 500;
	private static final int DELAY_SERVICE_START = 500;

	public static boolean serviceIsRunning = false;
	
	private final Handler handler = new Handler();
	
	private ActivityRecorderBinder service = null;
	
	private ProgressDialog dialog;
	
	/**
	 * enable to delete database in the device repository.
	 */
	private boolean EnableDeletion;
	
	private OptionQueries optionQuery;
	
	/**
	 * Updates the user interface.
	 */
	private final UpdateInterfaceRunnable updateInterfaceRunnable = new UpdateInterfaceRunnable();
	
	/**
	 * Displays the progress dialog, waits some time, starts the service, waits some more,
	 * 	then hides the dialog.
	 */
	private final StartServiceRunnable startServiceRunnable = new StartServiceRunnable();
	
	/**
	 *	Performs necessary tasks when the connection to the service
	 *	is established, and after it is disconnected.
	 */
	private final ServiceConnection connection = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			service = ActivityRecorderBinder.Stub.asInterface(iBinder);
			
			((Button) findViewById(R.id.togglebutton)).setEnabled(true);
			
			updateInterfaceRunnable.updateNow();
		}
		
		public void onServiceDisconnected(ComponentName componentName) {
			service = null;
			
			((Button) findViewById(R.id.togglebutton)).setEnabled(false);
			
			Log.i(CommonDef.DEBUG_TAG, "Service Disconnected");
		}
		
		
	};
	
	/**
	 * Click Listener for start/stop button on the main UI.
	 */
	private OnClickListener clickListener = new OnClickListener() {
		
		public void onClick(View clickedView) {
			try {
				if (service.isRunning()) {
					EnableDeletion = true;

					
					FlurryAgent.onEvent("recording_stop");
					
					stopService(new Intent(ActivityRecorderActivity.this, RecorderService.class));
					unbindService(connection);
					bindService(new Intent(ActivityRecorderActivity.this, RecorderService.class),
								connection, BIND_AUTO_CREATE);
					
				} else {
					EnableDeletion = false;
					FlurryAgent.onEvent("recording_start");
					
					startServiceRunnable.startService();
					
				}
			} catch (RemoteException ex) {
				Log.e(CommonDef.DEBUG_TAG, "Unable to get service state", ex);
			}

		}
	};

	/**
	 * 
	 * @param intent
	 * @return null
	 */
	public IBinder onBind(Intent intent) {
		return null;
	}
	

	/**
	 * Enable to use menu button to make option menus.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);
		return true;
	}
	
	/**
	 * Set the number of option menus on this Activity.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.showCalibration).setIcon(android.R.drawable.ic_menu_info_details);	
		menu.findItem(R.id.resetCalibration).setIcon(android.R.drawable.ic_menu_edit);
		menu.findItem(R.id.wake).setIcon(android.R.drawable.ic_menu_set_as);
		menu.findItem(R.id.link).setIcon(android.R.drawable.ic_menu_view);
		menu.findItem(R.id.copydata).setIcon(android.R.drawable.ic_menu_save);
		menu.findItem(R.id.deletedata).setEnabled(EnableDeletion);
		menu.findItem(R.id.deletedata).setIcon(android.R.drawable.ic_menu_delete);

		return true;
	}
	
	/**
	 * Performs the option menu with an appropriate option clicked.   
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			
		//Performs Wake Lock menu.
			case R.id.wake:
				try {
					if (service.isRunning()) {
						startActivity(new Intent(this, ScreenSettingActivity.class));
					} else {
						Toast.makeText(ActivityRecorderActivity.this, "Background service is not running at the moment.\n" +
								"Please run the service and try again.", Toast.LENGTH_LONG).show();
					}
					
				} catch (Exception ex) {
					Log.e(CommonDef.DEBUG_TAG, "Unable to get service state", ex);
				}
				break;

			//Performs copying database file to SDcard.	
			case R.id.copydata:
				try {
					File sdcard = Environment.getExternalStorageDirectory();
					File dbpath = new File(sdcard.getAbsolutePath() + File.separator
							+ "activityclassfier");
					if (!dbpath.exists()) {
						if (true)
							Log.d(CommonDef.DEBUG_TAG, "Create DB directory. " + dbpath.getAbsolutePath());
						dbpath.mkdirs();
					}
					
					String dbfile = dbpath.getAbsolutePath() + File.separator
							+ "activityrecords.db";
					copy(PATH_ACTIVITY_RECORDS_DB, dbfile);
					Toast.makeText(ActivityRecorderActivity.this, "Database copied into " + dbpath.getAbsolutePath()+"/ directory.", Toast.LENGTH_LONG).show();
					
				} catch (Exception ex) {
					Log.e(CommonDef.DEBUG_TAG, "Unable to get service state", ex);
				}
				break;
			
			//Performs to show the Web site so that an user can see the history of activities.
			case R.id.link:
				try {
					String url = URL_ACTIVITY_HISTORY;
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
				} catch (Exception ex) {
					Log.e(CommonDef.DEBUG_TAG, "Unable to get service state", ex);
				}
				break;

			//Performs to show the current calibration Standard Deviation values.
			case R.id.showCalibration:
				try{
        			Toast.makeText(ActivityRecorderActivity.this, "Calibration values : \n" +
        					"Standard Deviation X = "+optionQuery.getStandardDeviationX()+"\n" +
        					"Standard Deviation Y = "+optionQuery.getStandardDeviationY()+"\n" +
        					"Standard Deviation Z = "+optionQuery.getStandardDeviationZ(), Toast.LENGTH_LONG).show();
				}
				catch (Exception ex) {
					Log.e(CommonDef.DEBUG_TAG, "Unable to get service state", ex);
				}
				break;
				
			//Performs to reset the current calibration Standard Deviation values to default value(0.1).
			case R.id.resetCalibration:
				try{
					
					optionQuery.setCalibrationState("0");
        			optionQuery.setStandardDeviationX("0.1");
        			optionQuery.setStandardDeviationY("0.1");
        			optionQuery.setStandardDeviationZ("0.1");
        			Toast.makeText(ActivityRecorderActivity.this, "Calibration reset to : \n" +
        					"Standard Deviation X = 0.1\n" +
        					"Standard Deviation Y = 0.1\n" +
        					"Standard Deviation Z = 0.1", Toast.LENGTH_LONG).show();
				}
				catch (Exception ex) {
					Log.e(CommonDef.DEBUG_TAG, "Unable to get service state", ex);
				}
				break;

			//Performs to delete the database in the device repository.
			case R.id.deletedata:
				try {
					if (service.isRunning()) {
						
					} else {
						File f1 = new File(PATH_ACTIVITY_RECORDS_DB);
						f1.delete();
						Toast.makeText(ActivityRecorderActivity.this, "Database deleted", Toast.LENGTH_LONG).show();
					}
				} catch (RemoteException ex) {
					Log.e(CommonDef.DEBUG_TAG, "Unable to get service state", ex);
				}
				break;
		}
		return true;
		
	}
	
    
	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		EnableDeletion = false;
		optionQuery = new OptionQueries(this);
		setContentView(R.layout.main);
		((Button) findViewById(R.id.togglebutton)).setEnabled(false);
		((Button) findViewById(R.id.togglebutton)).setOnClickListener(clickListener);
		((ListView) findViewById(R.id.list)).setAdapter(new ArrayAdapter<Classification>(	this,
																							R.layout.item));
		
		bindService(new Intent(this, RecorderService.class), connection, BIND_AUTO_CREATE);
		
	}

	/**
	 * 
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(connection);
	}
	
	/**
	 * 
	 */
	protected void onResume() {
		super.onResume();
		updateInterfaceRunnable.start();
	}
	
	/**
	 * 
	 */
	protected void onPause() {
		super.onPause();
		updateInterfaceRunnable.stop();
	}

	/**
	 * 
	 */
	@Override
	protected void onStart() {
		super.onStart();
		
		FlurryAgent.onStartSession(this, "EMKSQFUWSCW51AKBL2JJ");
	}
	
	/**
	 * 
	 */
	@Override
	protected void onStop() {
		super.onStop();
		// wl.release();
		FlurryAgent.onEndSession(this);
	}
	
	/**
	 * A utility method which copy the database to SD card.
	 * @param targetFile database in the device repository.
	 * @param copyFile path to be copied
	 */
	private void copy(String targetFile, String copyFile) {
		try {
			InputStream lm_oInput = new FileInputStream(new File(targetFile));
			byte[] buff = new byte[128];
			FileOutputStream lm_oOutPut = new FileOutputStream(copyFile);
			while (true) {
				int bytesRead = lm_oInput.read(buff);
				if (bytesRead == -1)
					break;
				lm_oOutPut.write(buff, 0, bytesRead);
			}
			
			lm_oInput.close();
			lm_oOutPut.close();
			lm_oOutPut.flush();
			lm_oOutPut.close();
		} catch (Exception e) {
			Log.e(CommonDef.DEBUG_TAG, "Copy database to SD Card Error", e);
		}
	}
	
	
	/**
	 * 
	 * @author Umran
	 *
	 */
	private class StartServiceRunnable implements Runnable {
		
		private static final int DISPLAY_START_DIALOG = 0;
		private static final int CLOSE_START_DIALOG = 1;
		
		private int nextStep;
		
		public void startService() {
			if (dialog!=null) {
				return;
			}
			
			((Button) findViewById(R.id.togglebutton)).setEnabled(false);
			
			dialog = ProgressDialog.show(	ActivityRecorderActivity.this,
			                             	"Starting service",
											"Please wait...", true);			
			nextStep = DISPLAY_START_DIALOG;
			handler.postDelayed(this, 100);
		}

		public void run() {
			
			switch (nextStep) {
				case DISPLAY_START_DIALOG:
				{
					Intent intent = new Intent(	ActivityRecorderActivity.this,
                                              	RecorderService.class	);
					ActivityRecorderActivity.this.startService(intent);
					Log.i(CommonDef.DEBUG_TAG, "RecorderService Started");
					
					nextStep = CLOSE_START_DIALOG;					
					handler.postDelayed(this, DELAY_SERVICE_START);
					break;
				}
				case CLOSE_START_DIALOG:
				{
					try {
						//	hide the progress dialog box
						dialog.dismiss();
						dialog = null;
						
						//	enable the toggle button
						((Button) findViewById(R.id.togglebutton)).setEnabled(true);
						
						//	if the service is still not running by the end of the delay
						//		display an error message
						if (service==null || !service.isRunning())
						{
							AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecorderActivity.this);
							builder.setTitle("Error");
							builder.setMessage("Unable to start service"); // TODO: Put this into the string file
							builder.show();							
						}
					} catch (RemoteException e) {
						Log.e(CommonDef.DEBUG_TAG, "Error Starting Recorder Service", e);
					}
					break;
				}
			}
			
		}
		
	}

	/**
	 * Performs scheduled user interface updates, also allows
	 * other components to request the user interface to be updated,
	 * without interfering with normal scheduled updates.
	 * @author Umran
	 *
	 */
	private class UpdateInterfaceRunnable implements Runnable {
		
		//	save the state of the service, if it was previously running or not
		//		to avoid unnecessary updates
		private boolean prevServiceRunning = false;
		
		//	avoids conflicts between scheduled updates,
		//		and once-off updates 
		private ReentrantLock reentrantLock = new ReentrantLock();
		
		//	starts scheduled interface updates
		public void start() {
			handler.postDelayed(updateInterfaceRunnable, DELAY_UI_UPDATE);
		}
		
		//	stops scheduled interface updates
		public void stop() {
			handler.removeCallbacks(updateInterfaceRunnable);
		}
		
		//	performs a once-off unsynchronised (unscheduled) interface update
		//		please note that this can be called from another thread
		//		without interfering with the normal scheduled updates.
		public void updateNow() {
			if (reentrantLock.tryLock()) {
				
				try {
					updateUI();
				} catch (ParseException ex) {
					Log.e(CommonDef.DEBUG_TAG, "Error while performing scheduled UI update.", ex);
				}
				
				reentrantLock.unlock();
			}
		}

		public void run() {
			if (reentrantLock.tryLock()) {
				
				try {
					updateUI();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				reentrantLock.unlock();
			}
			
			handler.postDelayed(updateInterfaceRunnable, DELAY_UI_UPDATE);
		}
		
		

		/**
		 * 
		 * changed from updateButton to updateUI
		 * 
		 * updates the user interface:
		 * 	the toggle button's text is changed.
		 * 	the classification list's entries are updated.
		 * 
		 * @throws ParseException
		 */
		@SuppressWarnings("unchecked")
		private void updateUI() throws ParseException {
			
			try {
				boolean isServiceRunning = service!=null && service.isRunning();
				
				//	update toggle text only if service state has changed
				if (isServiceRunning!=prevServiceRunning) {
					((Button) findViewById(R.id.togglebutton)).setText( isServiceRunning	? R.string.service_enabled
																							: R.string.service_disabled);
				}
				
				//	update list either if service state has changed, or it's still running
				if (isServiceRunning!=prevServiceRunning || isServiceRunning) {
					List<Classification> classifications = Collections.EMPTY_LIST;
					
					if (isServiceRunning)
						classifications = service.getClassifications();
					
					final ArrayAdapter<Classification> adapter = (ArrayAdapter<Classification>) ((ListView) findViewById(R.id.list)).getAdapter();
					
					if (classifications.isEmpty()) {
						adapter.clear();
					} else
						if (!adapter.isEmpty()) {
							final Classification myLast = adapter.getItem(adapter.getCount() - 1);
							final Classification expected = classifications.get(adapter.getCount() - 1);
							
							if (myLast.getClassification().equals(expected.getClassification())) {
								myLast.updateEnd(expected.getEnd());
								adapter.notifyDataSetChanged();
							} else {
								adapter.clear();
							}
						}
					
					for (int i = adapter.getCount(); i < classifications.size(); i++) {
						adapter.add(classifications.get(i).withContext(ActivityRecorderActivity.this));
					}
				}
				
				prevServiceRunning = isServiceRunning;
				
			} catch (RemoteException ex) {
				Log.e(CommonDef.DEBUG_TAG, "Error while updating user interface", ex);
			}
		}
				
	}
	
}

/*
Changes made by Umran:

1) formatting.
2) changed method name updateButton() to updateUI(0
3) changed field name updateRunnable to updateInterfaceRunnable
4) removed initialisation of update UI sequence
	from
 		connection.onServiceConnected(ComponentName,IBinder)
 		startServiceRunnable.run()
 	to
 		onResume()
5) removed stopping of update UI sequence
	from
		onCreate(Bundle)
		clickListener.onClick(View)
		onServiceDisconnected(ComponentName)
	to
		onPause()
6) Added StartServiceRunnable to handle the service-starting sequence. i.e.
		i) Display progress dialog for 500ms
		ii) Start service
		iii) Display progress dialog for another 500ms
		iv) Close progress dialog
7) Added UpdateInterfaceRunnable to handle user interface updates.

*/
