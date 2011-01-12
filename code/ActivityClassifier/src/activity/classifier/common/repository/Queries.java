package activity.classifier.common.repository;

import android.content.Context;

public abstract class Queries {

	public DbAdapter dbAdapter;
	
	private Context context;
	
	public Queries(Context context){
		this.context = context;
		dbAdapter = new DbAdapter(context);
	}
}
