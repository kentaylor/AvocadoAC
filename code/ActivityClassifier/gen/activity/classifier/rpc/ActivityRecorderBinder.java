/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Documents and Settings\\ContextApi\\code\\ActivityClassifier\\src\\activity\\classifier\\rpc\\ActivityRecorderBinder.aidl
 */
package activity.classifier.rpc;
/**
 *
 * @author chris
 */
public interface ActivityRecorderBinder extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements activity.classifier.rpc.ActivityRecorderBinder
{
private static final java.lang.String DESCRIPTOR = "activity.classifier.rpc.ActivityRecorderBinder";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an activity.classifier.rpc.ActivityRecorderBinder interface,
 * generating a proxy if needed.
 */
public static activity.classifier.rpc.ActivityRecorderBinder asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof activity.classifier.rpc.ActivityRecorderBinder))) {
return ((activity.classifier.rpc.ActivityRecorderBinder)iin);
}
return new activity.classifier.rpc.ActivityRecorderBinder.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_isRunning:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isRunning();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_submitClassification:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.submitClassification(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getClassifications:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<activity.classifier.rpc.Classification> _result = this.getClassifications();
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_SetWakeLock:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.SetWakeLock(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements activity.classifier.rpc.ActivityRecorderBinder
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public boolean isRunning() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isRunning, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void submitClassification(java.lang.String classification) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(classification);
mRemote.transact(Stub.TRANSACTION_submitClassification, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public java.util.List<activity.classifier.rpc.Classification> getClassifications() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<activity.classifier.rpc.Classification> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getClassifications, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(activity.classifier.rpc.Classification.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void SetWakeLock(boolean wakelock) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((wakelock)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_SetWakeLock, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_isRunning = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_submitClassification = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getClassifications = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_SetWakeLock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public boolean isRunning() throws android.os.RemoteException;
public void submitClassification(java.lang.String classification) throws android.os.RemoteException;
public java.util.List<activity.classifier.rpc.Classification> getClassifications() throws android.os.RemoteException;
public void SetWakeLock(boolean wakelock) throws android.os.RemoteException;
}
