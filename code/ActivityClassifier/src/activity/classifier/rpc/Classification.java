/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier.rpc;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

/**
 *
 * @author chris
 */
public class Classification implements Parcelable {

    private CharSequence niceClassification;
    private String startTime;
    private String duration="";
    private final String classification;
    private final long start;
    private long end;
    private static int service;
	public Classification(final String classification, final long start,final int service) {
        this.classification = classification;
        this.start = start;
        this.end = start;
        this.service=service;
    }

    public void updateEnd(final long end) {
        this.end = end;
    }

    public String getStartTime(){
    	return startTime;
    }
    public String getDuration(){
    	return duration;
    }
    public String getNiceClassification(){
    	return (String) niceClassification;
    }
    public int describeContents() {
        return 0;
    }

    public String getClassification() {
        return classification;
    }

    public long getEnd() {
        return end;
    }

    public long getStart() {
        return start;
    }

    public int isServiceStarted(){
    	return service;
    }
    @Override 
    public String toString() {
//        final String duration;
        final int length = (int) ((end - start) / 1000);

        if (length < 60) {
            duration = "<1 min";
        } else if (length < 60 * 60) {
            duration = (length / 60) + " mins";
        } else {
            duration = (length / (60 * 60)) + " hrs";
        }

        if(niceClassification.equals("waiting")){
        	return niceClassification+"";
        }
        
        else{
        return niceClassification + "\n" + startTime + " for " + duration;
        }
    }

    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(classification);
        arg0.writeLong(start);
        arg0.writeLong(end);
    }

    public Classification withContext(final Context context) {
        String name = "activity" + (getClassification().length() == 0
                ? "_unknown" : getClassification().substring(10)
                .replace("/", "_").toLowerCase());

        if(!name.equals("activity_waitng")){
        niceClassification = context.getResources().getText(
                context.getResources().getIdentifier(name, "string",
                "activity.classifier"));
        }else{
        	niceClassification = "waitng";
        }
        

        Date date = new Date(start);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
//        java.text.SimpleDateFormat timeFormat = (SimpleDateFormat) SimpleDateFormat.getTimeInstance();

        startTime = dateFormat.format(date);
        
        return this;
    }

    public static final Parcelable.Creator<Classification> CREATOR
             = new Parcelable.Creator<Classification>() {

        public Classification createFromParcel(Parcel arg0) {
            final Classification res = new Classification(arg0.readString(), arg0.readLong(),arg0.readInt());
            res.updateEnd(arg0.readLong());
            return res;
        }

        public Classification[] newArray(int arg0) {
            return new Classification[arg0];
        }

    };

}
