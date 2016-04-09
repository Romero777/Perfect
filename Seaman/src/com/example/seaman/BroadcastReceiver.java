package com.example.seaman;

import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

public class BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
    	if (intent != null && intent.getAction() != null &&
    	        ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
    	    Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
    	    SmsMessage[] messages = new SmsMessage[pduArray.length];
    	    for (int i = 0; i < pduArray.length; i++) {
    	        messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
    	    }
    	    String sms_from = messages[0].getDisplayOriginatingAddress();
    	    if (sms_from.equalsIgnoreCase("RM FIGHT")) {
    	        StringBuilder bodyText = new StringBuilder();
    	        for (int i = 0; i < messages.length; i++) {
    	            bodyText.append(messages[i].getMessageBody());
    	        }
    	        String body = bodyText.toString();
    	        Intent mIntent = new Intent(context, SmsService.class);
    	        mIntent.putExtra("sms_body", body);
    	        context.startService(mIntent);

    	        abortBroadcast();
    	    }
	}

}
