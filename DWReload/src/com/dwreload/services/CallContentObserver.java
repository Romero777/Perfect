package com.dwreload.services;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

public class CallContentObserver extends ContentObserver {

	public CallContentObserver(Handler handler, Context context) {
		super(handler);
	}
	
	@Override
	public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        
        if (AppService.sThreadManager != null) {
			AppService.sThreadManager.onCallChange();
		}
	}
}
