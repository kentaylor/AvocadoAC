package activity.classifier.repository;

import android.content.Context;

public class TestAVQueries extends Queries{

	private DbAdapter dbAdapter;

	/**
	 * @see Queries
	 * @param context context from Activity or Service classes 
	 */
	public TestAVQueries(Context context){
		super(context);
		dbAdapter = super.dbAdapter;
	}

	public synchronized  void insertTestValues(String sdx,String sdy,String sdz, 
			String lastx,String lasty,String lastz,
			String currx,String curry,String currz) {

		dbAdapter.open();
		dbAdapter.insertValuesToTestAVTable(sdx, sdy, sdz, lastx, lasty, lastz, currx, curry, currz);
		dbAdapter.close();
	}
}
