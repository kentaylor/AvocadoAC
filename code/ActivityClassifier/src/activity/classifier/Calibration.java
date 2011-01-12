package activity.classifier;

public class Calibration {

	private float[] sd = new float[3];
	private float[] mean = new float[3];
	private boolean isCalibrated;
	private static int count=0;
	
	public Calibration(){
		for(int i=0;i<3;i++){
			sd[i] = 0;
			mean[i] = 0;
		}
		isCalibrated=false;
		increaseCount();
		
	}
	
	public void setCount(int count){
		this.count = count;
	}

	public int getCount(){
		return count;
	}
	
	public float[] getSSD(){
		return sd;
	}
	
	public float[] getMean(){
		return mean;
	}
	
	public boolean isCalibrated(){
		return isCalibrated;
	}
	
	public void increaseCount(){
		count++;
	}
	
	public void doCalibration(float[] mean, float[] sd){
		for(int i=0;i<3;i++){
			this.mean[i] = (this.mean[i]+mean[i])/2;
			this.sd[i] = (float) Math.sqrt((this.sd[i]*this.sd[i]*sd[i])/2 + ((this.mean[i]-mean[i])*(this.mean[i]-mean[i]))/4);
		}
		
	}
	

}
