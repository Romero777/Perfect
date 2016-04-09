package com.dwreload.activity;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import com.dwreload.R;
import com.dwreload.SettingsManager;
import com.dwreload.modules.SchedulerModule;

import android.app.Activity;
//import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;


public class AdvancedActivity extends FragmentActivity {
	private SettingsManager settings;
	private Activity activity = this;
	int mHour = 0;
    int mMinute = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced);
		
		settings = new SettingsManager(this);
		Refresh();
		
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
		CheckBox backcam   = (CheckBox) findViewById(R.id.cb_usebackcaminstdfront);
		CheckBox numinname = (CheckBox) findViewById(R.id.cb_numberinthename);
		CheckBox dayreport   = (CheckBox) findViewById(R.id.cb_dayreport);
		EditText cw1   = (EditText) findViewById(R.id.editcw1);
		EditText cmd1   = (EditText) findViewById(R.id.editcmd1);
		EditText cw2  = (EditText) findViewById(R.id.editcw2);
		EditText cmd2   = (EditText) findViewById(R.id.editcmd2);
		EditText cw3   = (EditText) findViewById(R.id.editcw3);
		EditText cmd3  = (EditText) findViewById(R.id.editcmd3);
		
		settings.UseBackCameraInsteadFront(backcam.isChecked());
		settings.NumberInName(numinname.isChecked());
		settings.DayReports(dayreport.isChecked());
		settings.CodeWord1(cw1.getText().toString());
		settings.Cmd1(cmd1.getText().toString());
		settings.CodeWord2(cw2.getText().toString());
		settings.Cmd2(cmd2.getText().toString());
		settings.CodeWord3(cw3.getText().toString());
		settings.Cmd3(cmd3.getText().toString());
		settings.RepHour(mHour);
		settings.RepMinute(mMinute);
		}
	
	private void Refresh()
		{
		CheckBox backcam   = (CheckBox) findViewById(R.id.cb_usebackcaminstdfront);
		backcam.setChecked(settings.UseBackCameraInsteadFront());
		backcam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.UseBackCameraInsteadFront(isChecked);
				}
			});
		
		CheckBox numinname   = (CheckBox) findViewById(R.id.cb_numberinthename);
		numinname.setChecked(settings.NumberInName());
		numinname.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.NumberInName(isChecked);
				}
			});
		
		
		
		EditText cw1   = (EditText) findViewById(R.id.editcw1);
	    cw1.setText(settings.CodeWord1());
	    EditText cw2   = (EditText) findViewById(R.id.editcw2);
	    cw2.setText(settings.CodeWord2());
	    EditText cw3   = (EditText) findViewById(R.id.editcw3);
	    cw3.setText(settings.CodeWord3());
	    EditText cmd1   = (EditText) findViewById(R.id.editcmd1);
	    cmd1.setText(settings.Cmd1());
	    EditText cmd2   = (EditText) findViewById(R.id.editcmd2);
	    cmd2.setText(settings.Cmd2());
	    EditText cmd3   = (EditText) findViewById(R.id.editcmd3);
	    cmd3.setText(settings.Cmd3());
	    
	    final TextView path = (TextView) findViewById(R.id.pathtophoto);
	    path.setText(settings.PhotoPath());
	    
	    mHour = settings.RepHour();
	    mMinute = settings.RepMinute();
	    final TextView reporttime = (TextView) findViewById(R.id.timerep);
	    reporttime.setText(Integer.toString(mHour) + ":" + (mMinute <10 ? "0":"") + Integer.toString(mMinute));
	    reporttime.setVisibility(settings.DayReports()? View.VISIBLE:View.INVISIBLE);
	    
	    final Button filebutton = (Button) findViewById(R.id.filebutton);
		filebutton.setOnClickListener(new View.OnClickListener()
			{
			public void onClick(View v)
				{
				File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
				FileDialog fileDialog = new FileDialog(activity, mPath);
				fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener()
					{
					public void directorySelected(File directory)
						{
						path.setText(directory.toString());
						settings.PhotoPath(directory.toString());
						}
					});
				fileDialog.setSelectDirectoryOption(true);
				fileDialog.showDialog();
				}
			
			});
		
	    final Handler mHandler = new Handler()
	    	{
	        @Override
	        public void handleMessage(Message m)
	        	{
	            Bundle b = m.getData();
	            mHour = b.getInt("set_hour");
	            mMinute = b.getInt("set_minute");
	            final TextView reporttime = (TextView) findViewById(R.id.timerep);
	            reporttime.setText(Integer.toString(mHour) + ":" + (mMinute <10 ? "0":"") + Integer.toString(mMinute));
	            SetAlm(true);
	        	}
	    	};
	    
		CheckBox dayreport   = (CheckBox) findViewById(R.id.cb_dayreport);
		dayreport.setChecked(settings.DayReports());
		dayreport.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				settings.DayReports(isChecked);
				reporttime.setVisibility(isChecked? View.VISIBLE:View.INVISIBLE);
				if (isChecked)
					{
		             Bundle b = new Bundle();                          // Creating a bundle object to pass currently set time to the fragment
		             b.putInt("set_hour", mHour);                      // Adding currently set hour to bundle object
		             b.putInt("set_minute", mMinute);                  // Adding currently set minute to bundle object
		             TimeDialog timePicker = new TimeDialog(mHandler); // Instantiating TimePickerDialogFragment
		             timePicker.setArguments(b);                       // Setting the bundle object on timepicker fragment
		             FragmentManager fm = getSupportFragmentManager(); // Getting fragment manger for this activity
		             FragmentTransaction ft = fm.beginTransaction();   // Starting a fragment transaction
		             ft.add(timePicker, "time_picker");                // Adding the fragment object to the fragment transaction
		             ft.commit();                                      // Opening the TimePicker fragment
					}
				else
					{
					SetAlm(false);	
					}
				}
			});
		
	    
	}	
	private void SetAlm(boolean day)
		{
		final long DAY_INTERVAL = 24 * 60 * 60 * 1000L;
		final long UPDATE_TIMEOUT = 30 * 60 * 1000L;
		SchedulerModule scheduler = new SchedulerModule();
        Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, mHour);
		calendar.set(Calendar.MINUTE, mMinute);
		calendar.set(Calendar.SECOND, 0);
		if (day)
			{
			scheduler.setPeriod(this, calendar.getTimeInMillis(), DAY_INTERVAL);
			}
		else
			{
			scheduler.setPeriod(this, new Date().getTime() + UPDATE_TIMEOUT, UPDATE_TIMEOUT);
			}
		}

}
	
