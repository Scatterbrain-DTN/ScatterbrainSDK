/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package net.ballmerlabs.scatterbrainsdk;
public interface ScatterbrainAPI extends android.os.IInterface
{
  /** Default implementation for ScatterbrainAPI. */
  public static class Default implements net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI
  {
    @Override public java.util.List<net.ballmerlabs.scatterbrainsdk.ScatterMessage> getByApplication(java.lang.String application) throws android.os.RemoteException
    {
      return null;
    }
    @Override public net.ballmerlabs.scatterbrainsdk.ScatterMessage getById(long id) throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.util.List<net.ballmerlabs.scatterbrainsdk.Identity> getIdentities() throws android.os.RemoteException
    {
      return null;
    }
    @Override public net.ballmerlabs.scatterbrainsdk.Identity getIdentityByFingerprint(java.lang.String fingerprint) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void sendMessage(net.ballmerlabs.scatterbrainsdk.ScatterMessage message) throws android.os.RemoteException
    {
    }
    @Override public void sendAndSignMessage(net.ballmerlabs.scatterbrainsdk.ScatterMessage message, java.lang.String identity) throws android.os.RemoteException
    {
    }
    @Override public void sendMessages(java.util.List<net.ballmerlabs.scatterbrainsdk.ScatterMessage> messages) throws android.os.RemoteException
    {
    }
    @Override public void startDiscovery() throws android.os.RemoteException
    {
    }
    @Override public void stopDiscovery() throws android.os.RemoteException
    {
    }
    @Override public void startPassive() throws android.os.RemoteException
    {
    }
    @Override public void stopPassive() throws android.os.RemoteException
    {
    }
    @Override public net.ballmerlabs.scatterbrainsdk.Identity generateIdentity(java.lang.String name) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void authorizeApp(java.lang.String identity, java.lang.String packagename) throws android.os.RemoteException
    {
    }
    @Override public void deauthorizeApp(java.lang.String identity, java.lang.String packagename) throws android.os.RemoteException
    {
    }
    @Override public boolean isDiscovering() throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean isPassive() throws android.os.RemoteException
    {
      return false;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI
  {
    private static final java.lang.String DESCRIPTOR = "net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI interface,
     * generating a proxy if needed.
     */
    public static net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI))) {
        return ((net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI)iin);
      }
      return new net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_getByApplication:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.util.List<net.ballmerlabs.scatterbrainsdk.ScatterMessage> _result = this.getByApplication(_arg0);
          reply.writeNoException();
          reply.writeTypedList(_result);
          return true;
        }
        case TRANSACTION_getById:
        {
          data.enforceInterface(descriptor);
          long _arg0;
          _arg0 = data.readLong();
          net.ballmerlabs.scatterbrainsdk.ScatterMessage _result = this.getById(_arg0);
          reply.writeNoException();
          if ((_result!=null)) {
            reply.writeInt(1);
            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          }
          else {
            reply.writeInt(0);
          }
          return true;
        }
        case TRANSACTION_getIdentities:
        {
          data.enforceInterface(descriptor);
          java.util.List<net.ballmerlabs.scatterbrainsdk.Identity> _result = this.getIdentities();
          reply.writeNoException();
          reply.writeTypedList(_result);
          return true;
        }
        case TRANSACTION_getIdentityByFingerprint:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          net.ballmerlabs.scatterbrainsdk.Identity _result = this.getIdentityByFingerprint(_arg0);
          reply.writeNoException();
          if ((_result!=null)) {
            reply.writeInt(1);
            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          }
          else {
            reply.writeInt(0);
          }
          return true;
        }
        case TRANSACTION_sendMessage:
        {
          data.enforceInterface(descriptor);
          net.ballmerlabs.scatterbrainsdk.ScatterMessage _arg0;
          if ((0!=data.readInt())) {
            _arg0 = net.ballmerlabs.scatterbrainsdk.ScatterMessage.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          this.sendMessage(_arg0);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_sendAndSignMessage:
        {
          data.enforceInterface(descriptor);
          net.ballmerlabs.scatterbrainsdk.ScatterMessage _arg0;
          if ((0!=data.readInt())) {
            _arg0 = net.ballmerlabs.scatterbrainsdk.ScatterMessage.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          java.lang.String _arg1;
          _arg1 = data.readString();
          this.sendAndSignMessage(_arg0, _arg1);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_sendMessages:
        {
          data.enforceInterface(descriptor);
          java.util.List<net.ballmerlabs.scatterbrainsdk.ScatterMessage> _arg0;
          _arg0 = data.createTypedArrayList(net.ballmerlabs.scatterbrainsdk.ScatterMessage.CREATOR);
          this.sendMessages(_arg0);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_startDiscovery:
        {
          data.enforceInterface(descriptor);
          this.startDiscovery();
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_stopDiscovery:
        {
          data.enforceInterface(descriptor);
          this.stopDiscovery();
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_startPassive:
        {
          data.enforceInterface(descriptor);
          this.startPassive();
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_stopPassive:
        {
          data.enforceInterface(descriptor);
          this.stopPassive();
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_generateIdentity:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          net.ballmerlabs.scatterbrainsdk.Identity _result = this.generateIdentity(_arg0);
          reply.writeNoException();
          if ((_result!=null)) {
            reply.writeInt(1);
            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          }
          else {
            reply.writeInt(0);
          }
          return true;
        }
        case TRANSACTION_authorizeApp:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          this.authorizeApp(_arg0, _arg1);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_deauthorizeApp:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          this.deauthorizeApp(_arg0, _arg1);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_isDiscovering:
        {
          data.enforceInterface(descriptor);
          boolean _result = this.isDiscovering();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          return true;
        }
        case TRANSACTION_isPassive:
        {
          data.enforceInterface(descriptor);
          boolean _result = this.isPassive();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public java.util.List<net.ballmerlabs.scatterbrainsdk.ScatterMessage> getByApplication(java.lang.String application) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.util.List<net.ballmerlabs.scatterbrainsdk.ScatterMessage> _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(application);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getByApplication, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getByApplication(application);
          }
          _reply.readException();
          _result = _reply.createTypedArrayList(net.ballmerlabs.scatterbrainsdk.ScatterMessage.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public net.ballmerlabs.scatterbrainsdk.ScatterMessage getById(long id) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        net.ballmerlabs.scatterbrainsdk.ScatterMessage _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(id);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getById, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getById(id);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = net.ballmerlabs.scatterbrainsdk.ScatterMessage.CREATOR.createFromParcel(_reply);
          }
          else {
            _result = null;
          }
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public java.util.List<net.ballmerlabs.scatterbrainsdk.Identity> getIdentities() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.util.List<net.ballmerlabs.scatterbrainsdk.Identity> _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getIdentities, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getIdentities();
          }
          _reply.readException();
          _result = _reply.createTypedArrayList(net.ballmerlabs.scatterbrainsdk.Identity.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public net.ballmerlabs.scatterbrainsdk.Identity getIdentityByFingerprint(java.lang.String fingerprint) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        net.ballmerlabs.scatterbrainsdk.Identity _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(fingerprint);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getIdentityByFingerprint, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getIdentityByFingerprint(fingerprint);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = net.ballmerlabs.scatterbrainsdk.Identity.CREATOR.createFromParcel(_reply);
          }
          else {
            _result = null;
          }
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void sendMessage(net.ballmerlabs.scatterbrainsdk.ScatterMessage message) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((message!=null)) {
            _data.writeInt(1);
            message.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_sendMessage, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().sendMessage(message);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void sendAndSignMessage(net.ballmerlabs.scatterbrainsdk.ScatterMessage message, java.lang.String identity) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((message!=null)) {
            _data.writeInt(1);
            message.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeString(identity);
          boolean _status = mRemote.transact(Stub.TRANSACTION_sendAndSignMessage, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().sendAndSignMessage(message, identity);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void sendMessages(java.util.List<net.ballmerlabs.scatterbrainsdk.ScatterMessage> messages) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeTypedList(messages);
          boolean _status = mRemote.transact(Stub.TRANSACTION_sendMessages, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().sendMessages(messages);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void startDiscovery() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startDiscovery, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().startDiscovery();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void stopDiscovery() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopDiscovery, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().stopDiscovery();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void startPassive() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startPassive, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().startPassive();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void stopPassive() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopPassive, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().stopPassive();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public net.ballmerlabs.scatterbrainsdk.Identity generateIdentity(java.lang.String name) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        net.ballmerlabs.scatterbrainsdk.Identity _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(name);
          boolean _status = mRemote.transact(Stub.TRANSACTION_generateIdentity, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().generateIdentity(name);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = net.ballmerlabs.scatterbrainsdk.Identity.CREATOR.createFromParcel(_reply);
          }
          else {
            _result = null;
          }
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void authorizeApp(java.lang.String identity, java.lang.String packagename) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(identity);
          _data.writeString(packagename);
          boolean _status = mRemote.transact(Stub.TRANSACTION_authorizeApp, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().authorizeApp(identity, packagename);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void deauthorizeApp(java.lang.String identity, java.lang.String packagename) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(identity);
          _data.writeString(packagename);
          boolean _status = mRemote.transact(Stub.TRANSACTION_deauthorizeApp, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().deauthorizeApp(identity, packagename);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public boolean isDiscovering() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isDiscovering, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().isDiscovering();
          }
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public boolean isPassive() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isPassive, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().isPassive();
          }
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public static net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI sDefaultImpl;
    }
    static final int TRANSACTION_getByApplication = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getById = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getIdentities = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_getIdentityByFingerprint = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_sendMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_sendAndSignMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_sendMessages = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_startDiscovery = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_stopDiscovery = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_startPassive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_stopPassive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
    static final int TRANSACTION_generateIdentity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
    static final int TRANSACTION_authorizeApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    static final int TRANSACTION_deauthorizeApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
    static final int TRANSACTION_isDiscovering = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
    static final int TRANSACTION_isPassive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
    public static boolean setDefaultImpl(net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static net.ballmerlabs.scatterbrainsdk.ScatterbrainAPI getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public java.util.List<net.ballmerlabs.scatterbrainsdk.ScatterMessage> getByApplication(java.lang.String application) throws android.os.RemoteException;
  public net.ballmerlabs.scatterbrainsdk.ScatterMessage getById(long id) throws android.os.RemoteException;
  public java.util.List<net.ballmerlabs.scatterbrainsdk.Identity> getIdentities() throws android.os.RemoteException;
  public net.ballmerlabs.scatterbrainsdk.Identity getIdentityByFingerprint(java.lang.String fingerprint) throws android.os.RemoteException;
  public void sendMessage(net.ballmerlabs.scatterbrainsdk.ScatterMessage message) throws android.os.RemoteException;
  public void sendAndSignMessage(net.ballmerlabs.scatterbrainsdk.ScatterMessage message, java.lang.String identity) throws android.os.RemoteException;
  public void sendMessages(java.util.List<net.ballmerlabs.scatterbrainsdk.ScatterMessage> messages) throws android.os.RemoteException;
  public void startDiscovery() throws android.os.RemoteException;
  public void stopDiscovery() throws android.os.RemoteException;
  public void startPassive() throws android.os.RemoteException;
  public void stopPassive() throws android.os.RemoteException;
  public net.ballmerlabs.scatterbrainsdk.Identity generateIdentity(java.lang.String name) throws android.os.RemoteException;
  public void authorizeApp(java.lang.String identity, java.lang.String packagename) throws android.os.RemoteException;
  public void deauthorizeApp(java.lang.String identity, java.lang.String packagename) throws android.os.RemoteException;
  public boolean isDiscovering() throws android.os.RemoteException;
  public boolean isPassive() throws android.os.RemoteException;
}
