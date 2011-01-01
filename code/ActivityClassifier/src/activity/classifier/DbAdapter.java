
package activity.classifier;

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
    public static final String KEY_START = "start";
    public static final String KEY_ACTIVITY = "activity";
    public static final String KEY_CHECK1 = "check1";
    public static final String KEY_CHECK2 = "check2";
    public static final String KEY_TIME = "time";
    public static final String KEY_DATE = "date";
    public static final String KEY_AVERAGEX = "averagex";
    public static final String KEY_AVERAGEY = "averagey";
    public static final String KEY_AVERAGEZ = "averagez";
    public static final String KEY_SENSORSDX = "sensorsdx";
    public static final String KEY_SENSORSDY = "sensorsdy";
    public static final String KEY_SENSORSDZ = "sensorsdz";
    public static final String KEY_1 = "a1";
    public static final String KEY_2 = "a2";
    public static final String KEY_3 = "a3";
    public static final String KEY_LASTX = "lastx";
    public static final String KEY_LASTY = "lasty";
    public static final String KEY_LASTZ = "lastz";
    public static final String KEY_CURRX = "currx";
    public static final String KEY_CURRY = "curry";
    public static final String KEY_CURRZ = "currz";
    public static final String KEY_SDX = "sdx";
    public static final String KEY_SDY = "sdy";
    public static final String KEY_SDZ = "sdz";
    public static final String KEY_UID = "UID";
    public static final String KEY_IMEI = "IMEI";


    private static final String TAG = "DbAdapter";
    private DatabaseHelper mDbHelper;
//    private SQLiteDatabase mDb;
    /**
     * Database creation sql statement
     */

    private static final String DATABASE_STARTINFO_CREATE =
    	"create table startinfo (_id integer primary key autoincrement, "
    	+ "start text not null);";
    private static final String DATABASE_STARTINFO_INIT1 =
    	"insert into startinfo values (null,1);";
    private static final String DATABASE_STARTINFO_INIT2 =
    	"insert into startinfo values (null,1);";
    private static final String DATABASE_STARTINFO_INIT3 =
    	"insert into startinfo values (null,1);";
    private static final String DATABASE_STARTINFO_INIT4 =
    	"insert into startinfo values (null,0.01);";
    private static final String DATABASE_STARTINFO_INIT5 =
    	"insert into startinfo values (null,0.01);";
    private static final String DATABASE_STARTINFO_INIT6 =
    	"insert into startinfo values (null,0.01);";
    private static final String DATABASE_STARTINFO_INIT7 =
    	"insert into startinfo values (null,1);";
    private static final String DATABASE_STARTINFO_INIT8 =
    	"insert into startinfo values (null,1);";
    private static final String DATABASE_ACTIVIT_CREATE =
    	"create table activity (_id integer primary key autoincrement, "
    	+ "activity text not null, date DATE not null, check1 integer not null, check2 integer not null);";
    private static final String DATABASE_TESTAV_CREATE =
    	"create table testav (_id integer primary key autoincrement, "
    	+ "date DATE not null, sdx text not null, sdy text not null, sdz text not null, lastx text not null, lasty text not null, lastz text not null," +
    			" currx text not null, curry text not null, currz text not null);";
    private static final String DATABASE_CALI_TEST_CREATE =
    	"create table calitest (_id integer primary key autoincrement, "
    	+ "averagex text not null, averagey text not null, averagez text not null);";
    private static final String DATABASE_CALI_TEST_INIT1 =
    	"insert into calitest values (null,1,1,1);";
//    private static final String DATABASE_ACTIVITY_INIT1 =
//    	"insert into activity values (null,\"Walking\",300,\"09-30-2010\");";
    private static final String DATABASE_TEST_CREATE =
    	"create table test (_id integer primary key autoincrement, "
    	+ "averagex text not null, averagey text not null" +
    			", averagez text not null, sensorsdx text not null, sensorsdy text not null" +
    			", sensorsdz text not null, a1 text not null, a2 text not null);";
    private static final String DATABASE_USERINFO_CREATE =
    	"create table userinfo (_id integer primary key autoincrement, "
    	+ "UID text not null, IMEI text not null);";
    private static  String DATABASE_NAME = "activityrecords.db";
    private static final String DATABASE_STARTINFO_TABLE = "startinfo";
    private static final String DATABASE_ACTIVITY_TABLE = "activity";
    private static final String DATABASE_CALI_TEST_TABLE = "calitest";
    private static final String DATABASE_TESTAV_TABLE = "testav";
    private static final String DATABASE_USERINFO_TABLE = "userinfo";
    
    private static final String DATABASE_TEST_TABLE = "test";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
        	
        	super(context, DATABASE_NAME, null, DATABASE_VERSION);
        	
        	
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	db.execSQL(DATABASE_STARTINFO_CREATE);
        	db.execSQL(DATABASE_STARTINFO_INIT1);
        	db.execSQL(DATABASE_STARTINFO_INIT2);
        	db.execSQL(DATABASE_STARTINFO_INIT3);
        	db.execSQL(DATABASE_STARTINFO_INIT4);
        	db.execSQL(DATABASE_STARTINFO_INIT5);
        	db.execSQL(DATABASE_STARTINFO_INIT6);
        	db.execSQL(DATABASE_STARTINFO_INIT7);
        	db.execSQL(DATABASE_STARTINFO_INIT8);
        	Log.i("Created DataStrart"," OK");
        	db.execSQL(DATABASE_CALI_TEST_CREATE);
        	db.execSQL(DATABASE_CALI_TEST_INIT1);
        	db.execSQL(DATABASE_CALI_TEST_INIT1);
        	db.execSQL(DATABASE_CALI_TEST_INIT1);
        	db.execSQL(DATABASE_ACTIVIT_CREATE);
        	db.execSQL(DATABASE_TESTAV_CREATE);
        	db.execSQL(DATABASE_USERINFO_CREATE);
//        	db.execSQL(DATABASE_ACTIVITY_INIT1);
        	Log.i("Created ACTIVITY"," OK");
        	db.execSQL(DATABASE_TEST_CREATE);
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
    private static SQLiteDatabase _db;  

    public DbAdapter open() throws SQLException {

        mDbHelper = new DatabaseHelper(mCtx);
        _db = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    
//  ---------------------Start Start-info Table----------------------------------
    public long insertStart(String start) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_START, start);

        return _db.insert(DATABASE_STARTINFO_TABLE, null, initialValues);
    }

    public boolean deleteStart(long rowId) {

        Log.i("Delete called", "value__" + rowId);
        return _db.delete(DATABASE_STARTINFO_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllStart() {

        return _db.query(DATABASE_STARTINFO_TABLE, 
        		new String[] { KEY_ROWID, KEY_START }, null, null, null, null, null);
    }

    public Cursor fetchStart(long rowId) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_STARTINFO_TABLE, 
        			new String[] { KEY_ROWID, KEY_START }, KEY_ROWID + "=" + rowId, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public boolean updateStart(long rowId, String start) {
        ContentValues args = new ContentValues();
        args.put(KEY_START, start);
        return _db.update(DATABASE_STARTINFO_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    public void deleteStart() {
        this._db.delete(DATABASE_STARTINFO_TABLE, null, null);
     }

//    ---------------------End Start-info Table----------------------------------
//  ---------------------Start USER-info Table----------------------------------
    public long insertUSER(String UID, String IMEI) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_UID, UID);
        initialValues.put(KEY_IMEI, IMEI);

        return _db.insert(DATABASE_USERINFO_TABLE, null, initialValues);
    }

    public boolean deleteUSER(long rowId) {

        Log.i("Delete called", "value__" + rowId);
        return _db.delete(DATABASE_USERINFO_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllUSER() {

        return _db.query(DATABASE_USERINFO_TABLE, 
        		new String[] { KEY_ROWID, KEY_UID, KEY_IMEI }, null, null, null, null, null);
    }

    public Cursor fetchUSER(long rowId) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_USERINFO_TABLE, 
        			new String[] { KEY_ROWID, KEY_UID, KEY_IMEI }, KEY_ROWID + "=" + rowId, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public boolean updateUSER(long rowId, String UID) {
        ContentValues args = new ContentValues();
        args.put(KEY_UID, UID);
        return _db.update(DATABASE_USERINFO_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    public void deleteUSER() {
        this._db.delete(DATABASE_USERINFO_TABLE, null, null);
     }

//    ---------------------End USER-info Table----------------------------------
//  ---------------------Start Activity Table----------------------------------


    public long insertActivity(String activity,String time,   int check1, int check2) {
        ContentValues initialValues = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        Date date1 = new Date(); 
 
        initialValues.put(KEY_ACTIVITY, activity);
//        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_DATE, time);
        initialValues.put(KEY_CHECK1, check1);
        initialValues.put(KEY_CHECK2, check2);

        return _db.insert(DATABASE_ACTIVITY_TABLE, null, initialValues);
    }

    public boolean deleteActivity(long rowId) {

        Log.i("Delete called", "value__" + rowId);
        return _db.delete(DATABASE_ACTIVITY_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllActivity() {

        return _db.query(DATABASE_ACTIVITY_TABLE, 
        		new String[] { KEY_ROWID, KEY_ACTIVITY, KEY_DATE, KEY_CHECK1, KEY_CHECK2 }, null, null, null, null, null);
    }

    public Cursor fetchActivity(long rowId) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_ACTIVITY_TABLE, 
        			new String[] { KEY_ROWID, KEY_ACTIVITY,KEY_DATE,KEY_CHECK1, KEY_CHECK2 }, KEY_ROWID + "=" + rowId, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }
    public Cursor fetchActivityCheck1(int check1) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_ACTIVITY_TABLE, 
        			new String[] { KEY_ROWID, KEY_ACTIVITY,KEY_DATE,KEY_CHECK1, KEY_CHECK2 }, KEY_CHECK1 + "=" + check1, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }
    public Cursor fetchActivityCheck2(int check2) throws SQLException {
        Cursor mCursor =
        	_db.query(true, DATABASE_ACTIVITY_TABLE, 
        			new String[] { KEY_ROWID, KEY_ACTIVITY,KEY_DATE,KEY_CHECK1, KEY_CHECK2 }, KEY_CHECK2 + "=" + check2, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public boolean updateActivity(long rowId, String activity,String date, int check1,int check2) {
        ContentValues args = new ContentValues();
        args.put(KEY_ACTIVITY, activity);
//        args.put(KEY_TIME, time);
        args.put(KEY_DATE, date);
        args.put(KEY_CHECK1, check1);
        args.put(KEY_CHECK2, check2);
        return _db.update(DATABASE_ACTIVITY_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    public void deleteActivity() {
        this._db.delete(DATABASE_ACTIVITY_TABLE, null, null);
     }

//    ---------------------End Activity Table----------------------------------
//  ---------------------Start TEST Table----------------------------------
    public long insertTest(String ax,String ay,String az,String sdx,String sdy,String sdz,String a1,String a2) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_AVERAGEX, ax);
        initialValues.put(KEY_AVERAGEY, ay);
        initialValues.put(KEY_AVERAGEZ, az);
        initialValues.put(KEY_SENSORSDX, sdx);
        initialValues.put(KEY_SENSORSDY, sdy);
        initialValues.put(KEY_SENSORSDZ, sdz);
        initialValues.put(KEY_1, a1);
        initialValues.put(KEY_2, a2);
//        initialValues.put(KEY_3, a3);

        
        return _db.insert(DATABASE_TEST_TABLE, null, initialValues);
    }

    public void deleteTest() {
        this._db.delete(DATABASE_TEST_TABLE, null, null);
     }

//    ---------------------End TEST Table----------------------------------
//  ---------------------Start CALI TEST Table----------------------------------
    public long insertCaliTest(String ax,String ay,String az) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_AVERAGEX, ax);
        initialValues.put(KEY_AVERAGEY, ay);
        initialValues.put(KEY_AVERAGEZ, az);
        
//        initialValues.put(KEY_3, a3);

        
        return _db.insert(DATABASE_CALI_TEST_TABLE, null, initialValues);
    }
    public boolean updateCaliTest(long rowId,String ax,String ay,String az) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_AVERAGEX, ax);
        initialValues.put(KEY_AVERAGEY, ay);
        initialValues.put(KEY_AVERAGEZ, az);
        return _db.update(DATABASE_CALI_TEST_TABLE, initialValues, KEY_ROWID + "=" + rowId, null) > 0;
    }
    public void deleteCaliTest() {
        this._db.delete(DATABASE_CALI_TEST_TABLE, null, null);
     }

//    ---------------------End CALI TEST Table----------------------------------
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
        
//        initialValues.put(KEY_3, a3);

        
        return _db.insert(DATABASE_TESTAV_TABLE, null, initialValues);
    }
//    public boolean updateTestAV(long rowId,String ax,String ay,String az) {
//        ContentValues initialValues = new ContentValues();
//        initialValues.put(KEY_AVERAGEX, ax);
//        initialValues.put(KEY_AVERAGEY, ay);
//        initialValues.put(KEY_AVERAGEZ, az);
//        return _db.update(DATABASE_CALI_TEST_TABLE, initialValues, KEY_ROWID + "=" + rowId, null) > 0;
//    }
    public void deleteTestAV() {
        this._db.delete(DATABASE_CALI_TEST_TABLE, null, null);
     }

//    ---------------------End AVERAGE TEST Table----------------------------------
}