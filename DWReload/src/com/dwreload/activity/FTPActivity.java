package com.dwreload.activity;


import com.dwreload.R;
import com.dwreload.SettingsManager;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;


public class FTPActivity extends Activity {
	private SettingsManager settings;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ftp);
		
		settings = new SettingsManager(this);
		RenderElements();
	}
	
	@Override
	protected void onResume() {
		RenderElements();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		CheckBox sms   = (CheckBox) findViewById(R.id.duplicatesms);
		CheckBox calls = (CheckBox) findViewById(R.id.duplicatecalls);
		EditText adr   = (EditText) findViewById(R.id.ftpservaddr);
		EditText prt   = (EditText) findViewById(R.id.ftpport);
		EditText usr   = (EditText) findViewById(R.id.ftpusername);
		EditText pwd   = (EditText) findViewById(R.id.ftpuserpass);
		EditText path  = (EditText) findViewById(R.id.ftpworkpath);
		
		settings.sendSMStoFTP(sms.isChecked());
		settings.sendCallToFTP(calls.isChecked());
		settings.ftp_adress(adr.getText().toString());
		settings.ftp_port(prt.getText().toString());
		settings.ftp_username(usr.getText().toString());
		settings.ftp_userpass(pwd.getText().toString());
		settings.ftp_path(path.getText().toString());
		
		super.onPause();
	}
	
	
	private void RenderElements()
		{		
		CheckBox sms   = (CheckBox) findViewById(R.id.duplicatesms);
		sms.setChecked(settings.sendSMStoFTP());
		sms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendSMStoFTP(isChecked);
				}
			});
		
		CheckBox cll   = (CheckBox) findViewById(R.id.duplicatecalls);
		cll.setChecked(settings.sendCallToFTP());
		cll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendCallToFTP(isChecked);
				}
			});
		
		CheckBox gps   = (CheckBox) findViewById(R.id.GPStoFTP);
		gps.setChecked(settings.sendGpsToFTP());
		gps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendGpsToFTP(isChecked);
				}
			});
		
		CheckBox his   = (CheckBox) findViewById(R.id.HistoryToFTP);
		his.setChecked(settings.sendHistoryToFTP());
		his.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendHistoryToFTP(isChecked);
				}
			});
		
		CheckBox social   = (CheckBox) findViewById(R.id.CBSocial);
		social.setChecked(settings.sendSocialToFTP());
		social.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendSocialToFTP(isChecked);
				}
			});
		
		CheckBox other   = (CheckBox) findViewById(R.id.OtherToFTP);
		other.setChecked(settings.sendAllOtherToFTP());
		other.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendAllOtherToFTP(isChecked);
				}
			});
		
		CheckBox files   = (CheckBox) findViewById(R.id.FilesToFTP);
		files.setChecked(settings.SendFilesToFTP());
		files.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.SendFilesToFTP(isChecked);
				}
			});
		
		EditText adr   = (EditText) findViewById(R.id.ftpservaddr);
	    adr.setText(settings.ftp_adress());
	    EditText prt   = (EditText) findViewById(R.id.ftpport);
	    prt.setText(Integer.toString(settings.ftp_port()));
		EditText usr   = (EditText) findViewById(R.id.ftpusername);
		usr.setText(settings.ftp_username());
		EditText pwd   = (EditText) findViewById(R.id.ftpuserpass);
		pwd.setText(settings.ftp_userpass());
		EditText path  = (EditText) findViewById(R.id.ftpworkpath);		
		path.setText(settings.ftp_path());
		
		
		prt.addTextChangedListener(new TextWatcher()
	    	{
	        public void afterTextChanged(Editable s)
	        	{
	        	String nv = s.toString();
				if (!onlyDigits(nv))
					{
					Toast.makeText(FTPActivity.this, R.string.settings_numberError, Toast.LENGTH_LONG).show();
					}
	        	}
	        @Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after){}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count){}    
	    	});
		
		}
	

	
	
	private Boolean onlyDigits(String s)
		{
		for (Integer i = 0; i < s.length(); i++)
			{
			if(!Character.isDigit(s.charAt(i)))
				{
				return false;
				}
			}
		return true;
		}
}