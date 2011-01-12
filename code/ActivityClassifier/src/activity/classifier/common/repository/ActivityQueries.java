package activity.classifier.common.repository;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ActivityQueries extends Queries{

	private DbAdapter dbAdapter;
	
	private ArrayList<String> ItemNames = new ArrayList<String>();
	private ArrayList<String> ItemDates = new ArrayList<String>();
	private ArrayList<Integer> ItemIDs = new ArrayList<Integer>();
	
	private int size;
	
	public ActivityQueries(Context context){
		super(context);
		dbAdapter = super.dbAdapter;
		
	}
	
	public void insertActivities(String activity, String time, int isChecked){
		dbAdapter.open();
		dbAdapter.insertToActivityTable(activity, time, isChecked);
		dbAdapter.close();
	}
	
	public void getUncheckedItemsFromActivityTable(int isChecked){
		ArrayList<String> uncheckedItems = new ArrayList<String>();
		dbAdapter.open();
		Cursor result = dbAdapter.fetchUnCheckedItemsFromActivityTable(isChecked);
		int i=0;
		for(result.moveToFirst(); result.moveToNext(); result.isAfterLast()) {
			
	    	uncheckedItems.add(Integer.parseInt(result.getString(0))+","+result.getString(1)+","+result.getString(2));
	    	Log.i("uncheckedItems",uncheckedItems.get(i));
	    	i++;
	    }
		result.close();
		dbAdapter.close();
		seperateItems(uncheckedItems);
		setUncheckedItemsSize(uncheckedItems.size());
	}
	
	public void updateUncheckedItems(long ItemIDs, String ItemNames, String ItemDates, int isChecked){
		dbAdapter.open();
		dbAdapter.updateItemsToActivityTable(ItemIDs, ItemNames, ItemDates, isChecked);
		dbAdapter.close();
	}
	
	private void setUncheckedItemsSize(int size){
		this.size = size;
	}
	
	private void seperateItems(ArrayList<String> uncheckedItems){
		for(int i = 0; i < uncheckedItems.size(); i++){
			String[] line = uncheckedItems.get(i).split(",");
			ItemIDs.add(Integer.parseInt(line[0]));
			ItemNames.add(line[1]);
			ItemDates.add(line[2]);
		}
	}
	
	public ArrayList<Integer> getUncheckedItemIDs(){
		return ItemIDs;
	}
	
	public ArrayList<String> getUncheckedItemNames(){
		return ItemNames;
	}
	
	public ArrayList<String> getUncheckedItemDates(){
		return ItemDates;
	}
	
	public int getUncheckedItemsSize(){
		return size;
	}
	
}
