package activity.classifier.repository;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * A utility class which extends superclass {@link Queries} 
 * for handling queries to save or load activity.
 * 
 *	<p>
 *	Changes made by Umran: <br>
 *	Changed all methods dealing with database open/close methods
 *	to <code>synchronized</code> in order to ensure thread safety when called 
 *	from multiple threads.
 * 
 * @author Justin Lee
 *
 */
public class ActivityQueries extends Queries{

	private DbAdapter dbAdapter;
	
	/**
	 * ArrayList data type to store un-posted items.
	 */
	private ArrayList<String> itemNames = new ArrayList<String>();
	private ArrayList<String> itemDates = new ArrayList<String>();
	private ArrayList<Integer> itemIDs = new ArrayList<Integer>();
	
	/**
	 * the number of un-posted items.
	 */
	private int size;
	
	/**
	 * @see Queries
	 * @param context context from Activity or Service classes 
	 */
	public ActivityQueries(Context context) {
		super(context);
		dbAdapter = super.dbAdapter;
	}
	
	/**
	 * Insert activity into the database.
	 *  
	 * @see DbAdapter
	 * @param activity name of activity 
	 * @param time date when the activity is happened
	 * @param isChecked state whether posted or not (1 or 0)
	 */
	public synchronized void insertActivities(String activity, String time, int isChecked){
		dbAdapter.open();
		dbAdapter.insertToActivityTable(activity, time, isChecked);
		dbAdapter.close();
	}
	
	/**
	 * Get un-posted items from the database.
	 * 
	 * @see DbAdapter
	 * @param isChecked state whether posted or not (1 or 0)
	 */
	public synchronized void getUncheckedItemsFromActivityTable(int isChecked){
		
		ArrayList<String> uncheckedItems = new ArrayList<String>();
		
		dbAdapter.open();
		//get un-posted items and assign them to #uncheckedItems.
		Cursor result = dbAdapter.fetchUnCheckedItemsFromActivityTable(isChecked);
		int i=0;
		for(result.moveToFirst(); result.moveToNext(); result.isAfterLast()) {
			
	    	uncheckedItems.add(Integer.parseInt(result.getString(0))+","+result.getString(1)+","+result.getString(2));
	    	Log.i("uncheckedItems",uncheckedItems.get(i));
	    	i++;
	    }
		result.close();
		dbAdapter.close();
		
		/*
		 * since the results from the query have three columns of data (Activity name, Date, state)
		 * it needs to separate to three part as well in order to get them separately.
		 */
		seperateItems(uncheckedItems);
		setUncheckedItemsSize(uncheckedItems.size());
	}
	
	/**
	 * Update un-posted items 
	 * @param itemIDs row Id
	 * @param itemNames activity name
	 * @param itemDates activity date
	 * @param isChecked sent item state
	 */
	public synchronized void updateUncheckedItems(long itemIDs, String itemNames, String itemDates, int isChecked){
		dbAdapter.open();
		dbAdapter.updateItemsToActivityTable(itemIDs, itemNames, itemDates, isChecked);
		dbAdapter.close();
	}
	
	private void setUncheckedItemsSize(int size){
		this.size = size;
	}
	
	private void seperateItems(ArrayList<String> uncheckedItems){
		for(int i = 0; i < uncheckedItems.size(); i++){
			String[] line = uncheckedItems.get(i).split(",");
			itemIDs.add(Integer.parseInt(line[0]));
			itemNames.add(line[1]);
			itemDates.add(line[2]);
		}
	}
	
	/**
	 * Get un-posted row IDs in the database
	 * @return Integer type ArrayList of row IDs in the database  
	 */
	public ArrayList<Integer> getUncheckedItemIDs(){
		return itemIDs;
	}
	
	/**
	 * Get un-posted activity names in the database
	 * @return String type ArrayList of activity names
	 */
	public ArrayList<String> getUncheckedItemNames(){
		return itemNames;
	}
	
	/**
	 * Get un-posted activity date
	 * @return String type ArrayList of activity dates
	 */
	public ArrayList<String> getUncheckedItemDates(){
		return itemDates;
	}
	
	/**
	 * Get the array size of un-posted items
	 * @return the array size of un-posted items
	 */
	public int getUncheckedItemsSize(){
		return size;
	}
	
}
