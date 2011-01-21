
package activity.classifier.repository;

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
 * class for creating SQLite database in the device memory.
 * and allow other classes {@link ActivityQueries} {@link OptionQueries} to use this functionality.
 * 
 * 
 * 
 * @author Justin Lee
 * 			
 */
public class DbAdapter {

    public static final String KEY_ROWID = "_id";
    
    /**
     * Column names in startinfo Table
     */
    public static final String KEY_isServiceRunning = "isServiceRunning";
    public static final String KEY_isCalibrated = "isCalibrated";
    public static final String KEY_CalibrationValue = "CalibrationValue";
    public static final String KEY_sdX = "sdX";
    public static final String KEY_sdY = "sdY";
    public static final String KEY_sdZ = "sdZ";
    public static final String KEY_isAccountSent = "isAccountSent";
    public static final String KEY_isWakeLockSet = "isWakeLockSet";
    
    /**
     * Column names in activity Table
     */
    public static final String KEY_ACTIVITY = "activity";
    public static final String KEY_isChecked = "isChecked";
    public static final String KEY_START_DATE = "startDate";
    public static final String KEY_END_DATE = "endDate";
    
    /**
     * Column names in testav Table
     */
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
     * startinfo Table creation sql statement
     */
    private static final String DATABASE_STARTINFO_CREATE =
    	"create table startinfo (_id integer primary key autoincrement, "
    	+ "isServiceRunning text not null, isCalibrated text not null, CalibrationValue text not null, " +
    			"sdX text not null, sdY text not null, sdZ text not null, isAccountSent text not null, " +
    			"isWakeLockSet text not null);";
    
    /**
     *  sql statement for initialising values in startinfo Table 
     */
    private static final String DATABASE_STARTINFO_INIT =
    	"insert into startinfo values (null, 0, 0, 1, 0.1, 0.1, 0.1, 0, 0);";
    
    /**
     * activity Table creation sql statement
     */
    private static final String DATABASE_ACTIVIT_CREATE =
    	"create table activity (_id integer primary key autoincrement, "
    	+ "activity text not null, startDate DATE not null, endDate DATE not null, isChecked integer not null);";
   
    /**
     * testav Table creation sql statement
     */
    private static final String DATABASE_TESTAV_CREATE =
    	"create table testav (_id integer primary key autoincrement, "
    	+ "startDate DATE not null, sdx text not null, sdy text not null, sdz text not null, lastx text not null, lasty text not null, lastz text not null," +
    			" currx text not null, curry text not null, currz text not null);";
    
    private static  String DATABASE_NAME = "activityrecords.db";
    
    /**
     * Table names
     */
    private static final String DATABASE_STARTINFO_TABLE = "startinfo";
    private static final String DATABASE_ACTIVITY_TABLE = "activity";
    private static final String DATABASE_TESTAV_TABLE = "testav";

    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    /**
     * Execute sql statement to create tables & initialise startinfo table
     * @author Justin
     *
     */
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

    /**
     * Initialise Context
     * @param ctx context from Activity or Service classes
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
		
    }
    
    /**
     * Make the database readable/writable
     * @return
     * @throws SQLException
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        _db = mDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * Close the database access 
     */
    public void close() {
        mDbHelper.close();
    }
    

//  ---------------------Start Start-info Table----------------------------------

    /**
     * Fetch the value from a specific column 
     * @param fieldName column name
     * @return String data type value 
     * @throws SQLException
     */
    public String fetchFromStartTableString(String fieldName) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_STARTINFO_TABLE, 
        			new String[] { KEY_ROWID, fieldName }, KEY_ROWID + "=" + 1, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        
        return mCursor.getString(1);
    }
    
    /**
     * Fetch the value from a specific column 
     * @param fieldName column name
     * @return Integer data type value 
     * @throws SQLException
     */
    public int fetchFromStartTableInt(String fieldName) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_STARTINFO_TABLE, 
        			new String[] { KEY_ROWID, fieldName }, KEY_ROWID + "=" + 1, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        
        return (int) Float.valueOf(mCursor.getString(1).trim()).floatValue();
    }
    
    /**
     * Fetch the value from a specific column 
     * @param fieldName column name
     * @return Float data type value 
     * @throws SQLException
     */
    public float fetchFromStartTableFloat(String fieldName) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_STARTINFO_TABLE, 
        			new String[] { KEY_ROWID, fieldName }, KEY_ROWID + "=" + 1, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        
        return Float.valueOf(mCursor.getString(1).trim()).floatValue();
    }

    /**
     * Update changed values in a specific column
     * @param fieldName column name
     * @param value changed value
     * @return true if update is successfully completed
     */
    public boolean updateToSelectedStartTable(String fieldName, String value) {
    	ContentValues args = new ContentValues();
        args.put(fieldName, value);
        return _db.update(DATABASE_STARTINFO_TABLE, args, KEY_ROWID + "=" + 1, null) > 0;
    }
    

//  ---------------------End Start-info Table----------------------------------
    
//  ---------------------Start Activity Table----------------------------------

    public Cursor fetchSizeOfRow()throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_ACTIVITY_TABLE, 
        			new String[] { KEY_ROWID, KEY_ACTIVITY,KEY_START_DATE,KEY_END_DATE,KEY_isChecked },  null, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }
    /**
     * Insert new activity information 
     * @param activity activity name
     * @param time date and time
     * @param isChecked activity sent state
     * @return the row ID of the newly inserted row, or -1 if an error occurred 
     */
    public long insertToActivityTable(String activity, String time, int isChecked) {
        ContentValues initialValues = new ContentValues();
 
        initialValues.put(KEY_ACTIVITY, activity);
        initialValues.put(KEY_START_DATE, time);
        initialValues.put(KEY_END_DATE, time);
        initialValues.put(KEY_isChecked,isChecked);

        return _db.insert(DATABASE_ACTIVITY_TABLE, null, initialValues);
    }

    /**
     * Delete activity information in selected row
     * @param rowId row ID
     * @return 
     */
    public boolean deleteActivity(long rowId) {

        Log.i("Delete called", "value__" + rowId);
        return _db.delete(DATABASE_ACTIVITY_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Fetch all un-posted activities
     * @param isChecked activity sent state
     * @return cursor that contain activity information
     * @throws SQLException
     */
    public Cursor fetchUnCheckedItemsFromActivityTable(int isChecked) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_ACTIVITY_TABLE, 
        			new String[] { KEY_ROWID, KEY_ACTIVITY,KEY_START_DATE,KEY_END_DATE,KEY_isChecked }, KEY_isChecked + "=" + isChecked, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public String fetchLastItemNames(long rowId){
    	Cursor mCursor = _db.query(true, DATABASE_ACTIVITY_TABLE,
    			new String[] { KEY_ACTIVITY}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
    	String activityName = mCursor.getString(0);
    	return activityName;
    }
    public String fetchLastItemEndDate(long rowId){
    	Cursor mCursor = _db.query(true, DATABASE_ACTIVITY_TABLE,
    			new String[] { KEY_END_DATE}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
    	String activityDate = mCursor.getString(0);
    	return activityDate;
    }
    /**
     * Update values in selected row
     * @param rowId row ID
     * @param activity activity name
     * @param date date and time
     * @param isChecked activity sent state
     * @return true if update is successfully completed
     */
    public boolean updateItemsToActivityTable(long rowId, String activity, String startDate, String endDate, int isChecked) {
        ContentValues args = new ContentValues();
        args.put(KEY_ACTIVITY, activity);
//        args.put(KEY_TIME, time);
        args.put(KEY_START_DATE, startDate);
        args.put(KEY_END_DATE, endDate);
        args.put(KEY_isChecked, isChecked);
        return _db.update(DATABASE_ACTIVITY_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateNewItemstoActivityTable(long rowId, String endDate) {
        ContentValues args = new ContentValues();
        args.put(KEY_END_DATE, endDate);
        return _db.update(DATABASE_ACTIVITY_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
//  ---------------------End Activity Table----------------------------------
    
//  ---------------------Start AVERAGE TEST Table----------------------------------
    
    /**
     * Insert average acceleration, previous acceleration, and standard deviation.
     * @param sdx average standard deviation of X axis
     * @param sdy average standard deviation of Y axis
     * @param sdz average standard deviation of Z axis
     * @param lastx previous average acceleration of X axis
     * @param lasty previous average acceleration of Y axis
     * @param lastz previous average acceleration of Z axis
     * @param currx average acceleration of X axis
     * @param curry average acceleration of Y axis
     * @param currz average acceleration of Z axis
     * @return the row ID of the newly inserted row, or -1 if an error occurred 
     */
    public long insertValuesToTestAVTable(String sdx,String sdy,String sdz, String lastx,String lasty,String lastz,String currx,String curry,String currz) {
        ContentValues initialValues = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z z");  
        Date date1 = new Date(); 
        String time = dateFormat.format(date1);
        initialValues.put(KEY_START_DATE, time);
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

//    ---------------------End AVERAGE TEST Table----------------------------------
}