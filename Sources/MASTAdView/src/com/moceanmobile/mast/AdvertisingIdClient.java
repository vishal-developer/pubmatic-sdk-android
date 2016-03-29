package com.moceanmobile.mast;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;

public final class AdvertisingIdClient {

	private final static String PM_LIMITED_TRACKING_AD_KEY 	= "limited_tracking_ad_key";
	private final static String PM_AID_STORAGE 				= "aid_shared_preference";
	private final static String PM_AID_KEY 					= "aid_key";
	
public static final class AdInfo {
	
    private final String advertisingId;
    private final boolean limitAdTrackingEnabled;

    AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {
        this.advertisingId = advertisingId;
        this.limitAdTrackingEnabled = limitAdTrackingEnabled;
    }

    public String getId() {
        return this.advertisingId;
    }

    public boolean isLimitAdTrackingEnabled() {
        return this.limitAdTrackingEnabled;
    }
}

/**
 * Refresh the advertising info saved in local storage asynchronously.
 * @param context
 * @return Returns the advertising info saved in local storage before refresh.
 */
public static AdInfo refreshAdvertisingInfo(final Context context) {
	
	new Thread(new Runnable() {
		public void run() {

		    if(Looper.myLooper() == Looper.getMainLooper()) throw new IllegalStateException("Cannot be called from the main thread");

		    AdvertisingConnection connection = new AdvertisingConnection();
		    Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
		    intent.setPackage("com.google.android.gms");
		    if(context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
		        try {
		            AdvertisingInterface adInterface = new AdvertisingInterface(connection.getBinder());
		            
		            //Save the Advertisement id & opt-out plag in local storage.
		            saveAndroidAid(context, adInterface.getId());
		            saveLimitedAdTrackingState(context, adInterface.isLimitAdTrackingEnabled(true));
		            
		        } catch (Exception exception) {
		            
		        } finally {
		            context.unbindService(connection);
		        }
		    } 
		}}).start(); 
	
	AdInfo adInfo = new AdInfo(AdvertisingIdClient.getAndroidAid(context, null), 
			AdvertisingIdClient.getLimitedAdTrackingState(context, true));
	return adInfo;
}

public static AdInfo getAdvertisingIdInfo(Context context) throws Exception {
    if(Looper.myLooper() == Looper.getMainLooper()) throw new IllegalStateException("Cannot be called from the main thread");

    try { PackageManager pm = context.getPackageManager(); pm.getPackageInfo("com.android.vending", 0); }  
    catch (Exception e) { throw e; }

    AdvertisingConnection connection = new AdvertisingConnection();
    Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
    intent.setPackage("com.google.android.gms");
    if(context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
        try {
            AdvertisingInterface adInterface = new AdvertisingInterface(connection.getBinder());
            
            //Save the Advertisement id & opt-out plag in local storage.
            saveAndroidAid(context, adInterface.getId());
            saveLimitedAdTrackingState(context, adInterface.isLimitAdTrackingEnabled(true));
            
            AdInfo adInfo = new AdInfo(adInterface.getId(), adInterface.isLimitAdTrackingEnabled(true));
            return adInfo;
        } catch (Exception exception) {
            throw exception;
        } finally {
            context.unbindService(connection);
        }
    }       
    throw new IOException("Google Play connection failed");     
}

/**
 * Save the Android advertisement id in local storage for further use.
 */
public static void saveAndroidAid(final Context context, String androidAid) {
	//Save the android_aid in local storage & use for next ad request
	SharedPreferences storage = context.getSharedPreferences(PM_AID_STORAGE, Context.MODE_PRIVATE);
	SharedPreferences.Editor editor = storage.edit();
	if(editor!=null) {
		editor.putString(PM_AID_KEY, androidAid);
		editor.commit();
	}
}

/**
 * Returns the Android advertisement id if saved in local storage else returns given androidAid.
 */
public static String getAndroidAid(Context context, String androidAid) {

	if(context!=null) {
		//Save the android_aid in local storage & use for next ad request
		SharedPreferences storage = context.getSharedPreferences(PM_AID_STORAGE, Context.MODE_PRIVATE);
		return storage.getString(PM_AID_KEY, androidAid);
	}
	return androidAid;
}


/**
 * Save the Android advertisement id in local storage for further use.
 */
public static void saveLimitedAdTrackingState(final Context context, boolean state) {
	//Save the android_aid in local storage & use for next ad request
	SharedPreferences storage = context.getSharedPreferences(PM_AID_STORAGE, Context.MODE_PRIVATE);
	SharedPreferences.Editor editor = storage.edit();
	if(editor!=null) {
		editor.putBoolean(PM_LIMITED_TRACKING_AD_KEY, state);
		editor.commit();
	}
}

/**
 * Returns the Android advertisement id if saved in local storage else returns given state.
 */
public static boolean getLimitedAdTrackingState(Context context, boolean state) {

	if(context!=null) {
		//Save the android_aid in local storage & use for next ad request
		SharedPreferences storage = context.getSharedPreferences(PM_AID_STORAGE, Context.MODE_PRIVATE);
		return storage.getBoolean(PM_LIMITED_TRACKING_AD_KEY, state);
	}
	return state;
}

private static final class AdvertisingConnection implements ServiceConnection {
    boolean retrieved = false;
    private final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue<IBinder>(1);

    public void onServiceConnected(ComponentName name, IBinder service) {
        try { this.queue.put(service); }
        catch (InterruptedException localInterruptedException){}
    }

    public void onServiceDisconnected(ComponentName name){}

    public IBinder getBinder() throws InterruptedException {
        if (this.retrieved) throw new IllegalStateException();
        this.retrieved = true;
        return (IBinder)this.queue.take();
    }
}

private static final class AdvertisingInterface implements IInterface {
    private IBinder binder;

    public AdvertisingInterface(IBinder pBinder) {
        binder = pBinder;
    }

    public IBinder asBinder() {
        return binder;
    }

    public String getId() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String id;
        try {
            data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
            binder.transact(1, data, reply, 0);
            reply.readException();
            id = reply.readString();
        } finally {
            reply.recycle();
            data.recycle();
        }
        return id;
    }

    public boolean isLimitAdTrackingEnabled(boolean paramBoolean) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean limitAdTracking;
        try {
            data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
            data.writeInt(paramBoolean ? 1 : 0);
            binder.transact(2, data, reply, 0);
            reply.readException();
            limitAdTracking = 0 != reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
        return limitAdTracking;
    }
}
}
