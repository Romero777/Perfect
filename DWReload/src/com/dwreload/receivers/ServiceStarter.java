package com.dwreload.receivers;

import com.dwreload.DBManager;
//import com.dwreload.SettingsManager;
import com.dwreload.SimChangeNotify;
import com.dwreload.services.AppService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			DBManager dbManager = new DBManager(context);
			dbManager.deleteOdlRecords();
			dbManager.close();

			context.startService(new Intent(context, AppService.class));
			
			new SimChangeNotify(context).start();
			
		} catch (Exception e) {
			
		}
	}
}