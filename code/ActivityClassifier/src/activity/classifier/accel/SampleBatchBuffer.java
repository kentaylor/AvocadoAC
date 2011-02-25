package activity.classifier.accel;

import java.util.concurrent.ArrayBlockingQueue;

import activity.classifier.common.Constants;
import android.util.Log;

/**
 * 
 * @author Umran
 * 
 * <p>
 * This buffer ensures that there is always a fixed number of {@link SampleBatch} instances
 * at any one time. The buffer contains all empty and filled batches waiting to be processed.
 * 
 * <p>
 *  Empty batches are waiting to be filled (by {@link activity.classifier.service.RecorderService}), while
 *  Filled batches are waiting to be processed (by {@link activity.classifier.service.ClassifierService})
 *
 */
public class SampleBatchBuffer {
	
	public static final int TOTAL_BATCH_COUNT = 20;

	private ArrayBlockingQueue<SampleBatch> filledBatches;
	private ArrayBlockingQueue<SampleBatch> emptyBatches;
	
	public SampleBatchBuffer() {
		
		this.filledBatches = new ArrayBlockingQueue<SampleBatch>(TOTAL_BATCH_COUNT, true);
		this.emptyBatches = new ArrayBlockingQueue<SampleBatch>(TOTAL_BATCH_COUNT, true);
		
		for (int i=0; i<TOTAL_BATCH_COUNT; ++i)
			this.emptyBatches.add(new SampleBatch());
		
	}
	
	public SampleBatch takeEmptyBatch() throws InterruptedException {
		//Log.v(Constants.DEBUG_TAG, emptyBatches.size()+" empty batches available.");
		return emptyBatches.take();
	}
	
	public void returnEmptyBatch(SampleBatch batch) throws InterruptedException {
		emptyBatches.put(batch);
		//Log.v(Constants.DEBUG_TAG, "Empty batch added to queue.");
	}
	
	public SampleBatch takeFilledBatch() throws InterruptedException {
		//Log.v(Constants.DEBUG_TAG, filledBatches.size()+" filled batches available.");
		return filledBatches.take();
	}
	
	public void returnFilledBatch(SampleBatch batch) throws InterruptedException {
		filledBatches.put(batch);
		//Log.v(Constants.DEBUG_TAG, "Filled batch added to queue.");
	}
	
	public int getPendingFilledBatches() {
		return filledBatches.size();
	}
	
}
