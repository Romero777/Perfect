package com.dwreload.modules.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationTimerBroadcastReceiver extends BroadcastReceiver {
	public static int TIMER_REQUEST_CODE = 100;

	@Override
	public void onReceive(Context context, Intent intent) {

		//Log.w("LocationTimer", "It work!");
		LocationModule.message(LocationModule.REQUEST_SCHEDULED_LOCATION);
	}

}
