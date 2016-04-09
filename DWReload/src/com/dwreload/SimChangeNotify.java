package com.dwreload;

import android.content.Context;
import android.telephony.TelephonyManager;

public class SimChangeNotify extends Thread{
	private Context context;
	
	public SimChangeNotify(Context context){
		this.context = context;
	}
	
	@Override
	public void run() {
		try {
			SettingsManager mgr = new SettingsManager(context);
			
			String IMSI = mgr.imsi();
			
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			String currentIMSI = null;
			if (tm != null){
				currentIMSI = tm.getSubscriberId();
			}
			
			if (currentIMSI == null || currentIMSI.length() == 0){
				currentIMSI = "0";
			}
			mgr.imsi(currentIMSI);
			if (IMSI.equals(currentIMSI)){
				return;
			}
			/*
			 * Notification
			 */
			if (!mgr.isSimChangeNotificationEnabled()){
				return;
			}
			
			String number = mgr.notifyNumber();			
			String text = "SIM-card changed! IMEI: " + mgr.imei() + " IMSI: " + currentIMSI;
			
			SmsNotification.sendSms(context, text, number);
			} catch (Exception e) {
		}
	}
	
}
