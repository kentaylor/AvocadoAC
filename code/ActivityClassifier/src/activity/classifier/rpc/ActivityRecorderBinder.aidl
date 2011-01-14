/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package activity.classifier.rpc;

import activity.classifier.rpc.Classification;

/**
 *
 * @author chris
 */
interface ActivityRecorderBinder {

    boolean isRunning();

    void submitClassification(String classification);

    List<Classification> getClassifications();
    
    void SetWakeLock(boolean wakelock);
    
    void SetPhoneInformation(String AccountName, String ModelName, String IMEI);
    
    

}
