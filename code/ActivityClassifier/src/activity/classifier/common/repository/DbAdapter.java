
package activity.classifier.common.repository;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/**
 * @author Justin
 * AbAdapter:
 * 				class for creating SQLite database in the device memory
 * Three tables:
 * 				 	startinfo: _id, start
 * 					activity : -id, activity, date, check1
 * 					calitest : -id, averagex, averagey, averagez
 * 					
 * 		startinfo Table : store the system information 
 * 						  1st row : the value is 0 if it is running, otherwise 1
 * 						  2nd row : the value is 0 if calibration value is calculated, otherwise 1
 * 						  3rd row : Sensor SD x-axis if calculated, otherwise 1
 * 						  4th row : Sensor SD y-axis if calculated, otherwise 1
 * 						  5th row : Sensor SD z-axis if calculated, otherwise 1
 * 
 * 		activity Table  : store the history of user's activities
 * 		calitest Table  : store the average acceleration values (mainly for testing for calibration value)
 * 			
 */
public class DbAdapter {

    public static final String KEY_ROWID = "_id";
    
    public static final String KEY_isServiceRunning = "isServiceRunning";
    public static final String KEY_isCalibrated = "isCalibrated";
    public static final String KEY_CalibrationValue = "CalibrationValue";
    public static final String KEY_sdX = "sdX";
    public static final String KEY_sdY = "sdY";
    public static final String KEY_sdZ = "sdZ";
    public static final String KEY_isAccountSent = "isAccountSent";
    public static final String KEY_isWakeLockSet = "isWakeLockSet";
    
    public static final String KEY_ACTIVITY = "activity";
    public static final String KEY_isChecked = "isChecked";
    public static final String KEY_DATE = "date";
    
    public static final String KEY_LASTX = "lastx";
    public static final String KEY_LASTY = "lasty";
    public static final String KEY_LASTZ = "lastz";
    public static final String KEY_CURRX = "currx";
    public static final String KEY_CURRY = "curry";
    public static final String KEY_CURRZ = "currz";
    public static final String KEY_SDX = "sdx";
    public static final String KEY_SDY = "sdy";
    public static final String KEY_SDZ = "sdz";


    private static final String TAG = "DbAdapter";
    private DatabaseHelper mDbHelper;
    private static SQLiteDatabase _db;  
    /**
     * Database creation sql statement
     */

    private static final String DATABASE_STARTINFO_CREATE =
    	"create table startinfo (_id integer primary key autoincrement, "
    	+ "isServiceRunning text not null, isCalibrated text not null, CalibrationValue text not null, " +
    			"sdX text not null, sdY text not null, sdZ text not null, isAccountSent text not null, " +
    			"isWakeLockSet text not null);";
    
    private static final String DATABASE_STARTINFO_INIT =
    	"insert into startinfo values (null, 0, 0, 1, 0.1, 0.1, 0.1, 0, 0);";
    
    private static final String DATABASE_ACTIVIT_CREATE =
    	"create table activity (_id integer primary key autoincrement, "
    	+ "activity text not null, date DATE not null, isChecked integer not null);";
   
    private static final String DATABASE_TESTAV_CREATE =
    	"create table testav (_id integer primary key autoincrement, "
    	+ "date DATE not null, sdx text not null, sdy text not null, sdz text not null, lastx text not null, lasty text not null, lastz text not null," +
    			" currx text not null, curry text not null, currz text not null);";
    
    private static  String DATABASE_NAME = "activityrecords.db";
    
    private static final String DATABASE_STARTINFO_TABLE = "startinfo";
    private static final String DATABASE_ACTIVITY_TABLE = "activity";
    private static final String DATABASE_TESTAV_TABLE = "testav";

    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
        	super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
 
        @Override
        public void onCreate(SQLiteDatabase db) {
        	db.execSQL(DATABASE_STARTINFO_CREATE);
        	db.execSQL(DATABASE_STARTINFO_INIT);
        	db.execSQL(DATABASE_ACTIVIT_CREATE);
        	db.execSQL(DATABASE_TESTAV_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
            		+ newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS startinfo");
            onCreate(db);

        }
    }

    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
		
    }
    
    public DbAdapter open() throws SQLException {

        mDbHelper = new DatabaseHelper(mCtx);
        _db = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
//  ---------------------Start Start-info Table----------------------------------

    public boolean deleteStartTable(long rowId) {

        Log.i("Delete called", "value__" + rowId);
        return _db.delete(DATABASE_STARTINFO_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public String fetchFromStartTableString(String fieldName) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_STARTINFO_TABLE, 
        			new String[] { KEY_ROWID, fieldName }, KEY_ROWID + "=" + 1, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        
        return mCursor.getString(1);
    }
    public int fetchFromStartTableInt(String fieldName) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_STARTINFO_TABLE, 
        			new String[] { KEY_ROWID, fieldName }, KEY_ROWID + "=" + 1, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        
        return (int) Float.valueOf(mCursor.getString(1).trim()).floatValue();
    }
    public float fetchFromStartTableFloat(String fieldName) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_STARTINFO_TABLE, 
        			new String[] { KEY_ROWID, fieldName }, KEY_ROWID + "=" + 1, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        
        return Float.valueOf(mCursor.getString(1).trim()).floatValue();
    }

    
    public boolean updateToSelectedStartTable(String fieldName, String value) {
    	ContentValues args = new ContentValues();
        args.put(fieldName, value);
        return _db.update(DATABASE_STARTINFO_TABLE, args, KEY_ROWID + "=" + 1, null) > 0;
    }
    
    public void deleteStart() {
        this._db.delete(DATABASE_STARTINFO_TABLE, null, null);
     }

//    ---------------------End Start-info Table----------------------------------
//  ---------------------Start Activity Table----------------------------------


    public long insertToActivityTable(String activity, String time, int isChecked) {
        ContentValues initialValues = new ContentValues();
 
        initialValues.put(KEY_ACTIVITY, activity);
        initialValues.put(KEY_DATE, time);
        initialValues.put(KEY_isChecked,isChecked);

        return _db.insert(DATABASE_ACTIVITY_TABLE, null, initialValues);
    }

    public boolean deleteActivity(long rowId) {

        Log.i("Delete called", "value__" + rowId);
        return _db.delete(DATABASE_ACTIVITY_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllActivity() {

        return _db.query(DATABASE_ACTIVITY_TABLE, 
        		new String[] { KEY_ROWID, KEY_ACTIVITY, KEY_DATE, KEY_isChecked }, null, null, null, null, null);
    }

    public Cursor fetchActivity(long rowId) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_ACTIVITY_TABLE, 
        			new String[] { KEY_ROWID, KEY_ACTIVITY,KEY_DATE,KEY_isChecked }, KEY_ROWID + "=" + rowId, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }
    public Cursor fetchUnCheckedItemsFromActivityTable(int isChecked) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_ACTIVITY_TABLE, 
        			new String[] { KEY_ROWID, KEY_ACTIVITY,KEY_DATE,KEY_isChecked }, KEY_isChecked + "=" + isChecked, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public boolean updateItemsToActivityTable(long rowId, String activity, String date, int isChecked) {
        ContentValues args = new ContentValues();
        args.put(KEY_ACTIVITY, activity);
//        args.put(KEY_TIME, time);
        args.put(KEY_DATE, date);
        args.put(KEY_isChecked, isChecked);
        return _db.update(DATABASE_ACTIVITY_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    public void deleteActivity() {
        this._db.delete(DATABASE_ACTIVITY_TABLE, null, null);
     }

//    ---------------------End Activity Table----------------------------------
//  ---------------------Start AVERAGE TEST Table----------------------------------
    public long insertTestAV(String sdx,String sdy,String sdz, String lastx,String lasty,String lastz,String currx,String curry,String currz) {
        ContentValues initialValues = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        Date date1 = new Date(); 
        String time = dateFormat.format(date1);
        initialValues.put(KEY_DATE, time);
        initialValues.put(KEY_SDX, sdx);
        initialValues.put(KEY_SDY, sdy);
        initialValues.put(KEY_SDZ, sdz);
        initialValues.put(KEY_LASTX, lastx);
        initialValues.put(KEY_LASTY, lasty);
        initialValues.put(KEY_LASTZ, lastz);
        initialValues.put(KEY_CURRX, currx);
        initialValues.put(KEY_CURRY, curry);
        initialValues.put(KEY_CURRZ, currz);

        
        return _db.insert(DATABASE_TESTAV_TABLE, null, initialValues);
    }
    
    public void deleteTestAV() {
        this._db.delete(DATABASE_TESTAV_TABLE, null, null);
     }

//    ---------------------End AVERAGE TEST Table----------------------------------
}