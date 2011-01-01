package activity.classifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import activity.classifier.rpc.ActivityRecorderBinder;
import activity.classifier.rpc.Classification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

public class StoreDatabase extends Service {
	private Context context;
	ActivityRecorderBinder service = null;
	final Handler handler = new Handler();
    private final ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ActivityRecorderBinder.Stub.asInterface(arg1);

        }

        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
            stopSelf();
        }
    };
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);
        Log.i("Storeservice","Started!!!!!!!!!!!"); 
        context = this.getApplicationContext();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Activity recorder");
        bindService(new Intent(this, RecorderService.class), connection, BIND_AUTO_CREATE);
        wl.acquire();

        	handler.postDelayed(updateRunnable, 2000);
		
		
	}
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
    final List<Classification> adapter = new ArrayList<Classification>();
	private DbAdapter dbAdapter = new DbAdapter(this);
	 void updateButton() throws ParseException {
	    	
	        try {
	          
	  
//	          Intent intent2 = new Intent(context, ActivityRecorderActivity.class); 
	            final List<Classification> classifications = service.getClassifications();
	            
//	            final ArrayAdapter<Classification> adapter = new ArrayAdapter<Classification>(context, R.id.list);

	            if (classifications.isEmpty()) {
	                adapter.clear();
	            } else if (!adapter.isEmpty()) {
	                final Classification myLast = adapter.get(adapter.size()-1);
	                final Classification expected = classifications.get(adapter.size() - 1);

	                if (myLast.getClassification().equals(expected.getClassification())) {
	                    // Just update the end time
	                    myLast.updateEnd(expected.getEnd());

	                    
	                } 
//	                else if(myLast.getClassification().equals("waiting")){
//	                	adapter.clear();
//	                }
	                else {
	                    // Something's gone wrong - the entries should match
	                    adapter.clear();
	                }
	            }

	            for (int i = adapter.size(); i < classifications.size(); i++) {
	            	String lastAc = "NONE";
		            Log.i("Context",context.toString()); 
		            Log.i("AdaperSize",adapter.size()+""); 
		            Log.i("ClassSize",classifications.size()+""); 

	                adapter.add(classifications.get(i));
		            Log.i("Adaper",adapter.get(i)+""); 
		            Log.i("Class",classifications.get(i)+""); 
	                String activity = classifications.get(i).getNiceClassification();
	                
	                String newAc = activity;
	                if(!lastAc.equals(newAc)){
		                Log.i("AccessedDatabase","activity"+activity);
		                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		                
		                String date = classifications.get(i).getStartTime();
		                Date date1 = dateFormat.parse(date); 
		                dbAdapter.open();
		                dbAdapter.insertActivity(activity,date,   0,0);
		                dbAdapter.close();
	                }
	                lastAc = newAc;
	            }

	        } catch (RemoteException ex) {
	            Log.e(getClass().getName(), "Unable to get service state", ex);
	        } 
	        
	    }
	    private PowerManager.WakeLock wl;
	 @Override
	    public void onDestroy() {
	        super.onDestroy();
	        Log.i("Ondestroy","HERE");
	            handler.removeCallbacks(updateRunnable);

	            unbindService(connection);
//	            bindService(new Intent(this, RecorderService.class), connection, BIND_AUTO_CREATE);
	            wl.release();
	            
	       
	    }
}
