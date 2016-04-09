package com.dwreload.activity;



import com.dwreload.R;
import com.dwreload.SettingsManager;
import com.dwreload.SimChangeNotify;
import com.stericson.RootTools.RootTools;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainMenuActivity extends Activity {	
	private SettingsManager settings;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        
        settings = new SettingsManager(this);
        
        renderElements();
        
        new SimChangeNotify(this).start();
       
        if (RootTools.isRootAvailable()) {
			RootTools.isAccessGiven();
        }
	}
	
	@Override
	protected void onResume() {
		renderElements();
		
		super.onResume();
	}
	
	private void renderElements(){
        TextView pnText = (TextView)findViewById(R.id.wizard_text);
        pnText.setText("IMEI: " + settings.imei());
        
        findViewById(R.id.settingsButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	        	startActivity(new Intent(MainMenuActivity.this, MyPreferenceActivity.class));
			}
		});
        
        findViewById(R.id.filterButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	        	startActivity(new Intent(MainMenuActivity.this, FilterActivity.class));
			}
		});
        
        findViewById(R.id.FTPButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	        	startActivity(new Intent(MainMenuActivity.this, FTPActivity.class));
			}
		});
        
        findViewById(R.id.mailButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainMenuActivity.this, MailerActivity.class));
			}
		});
        
        findViewById(R.id.advancebutton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainMenuActivity.this, AdvancedActivity.class));
			}
		});
        
        try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			TextView versionView = (TextView)findViewById(R.id.mainmenu_version);
			if (info != null) {
				versionView.setText("rev: " + info.versionName + "(" + info.versionCode + ")");
			}
			
		} catch (Exception e) {

		}

	}	
}
