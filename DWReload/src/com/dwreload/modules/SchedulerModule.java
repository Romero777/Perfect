package com.dwreload.modules;

import com.dwreload.ThreadManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SchedulerModule extends BroadcastReceiver {
	public static int TIMER_REQUEST_CODE = 33;

	@Override
	public void onReceive(Context context, Intent intent)
		{
		ThreadManager Th = new ThreadManager(context);
		//Th.sendFiles();
		Th.sendLogs();
		}

	public void setPeriod(Context context, Long time, Long Period)
		{
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, SchedulerModule.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, SchedulerModule.TIMER_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT );
		am.cancel(pendingIntent);
		// ”станавливаем регул€рную пиналку
		am.setRepeating(AlarmManager.RTC_WAKEUP, time, Period, pendingIntent);
		}
}
