package com.dwreload.modules.mail;


import java.io.File;
import java.util.ArrayList;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import com.dwreload.Debug;
import com.dwreload.SettingsManager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class SendMail extends AsyncTask<String, Void, Boolean>
	{
	private Context mContext;
	private String  mServerprt;
	private String  mSMTPServer;
	private String  mUsername;
	private String  mUserpass;
	private String  mEmailAdress;
	private String  mEmailTo;
	private String  mSecurity;
	private ArrayList<File> mFiles;
	
	public SendMail(Context context, ArrayList<File> f) 
		{
		this.mContext = context;
		this.mFiles = f;
		}
	
	@Override
    protected void onPreExecute()
		{
		super.onPreExecute();
		
		SettingsManager settings = new SettingsManager(mContext);
		this.mEmailAdress = settings.email();
		this.mSMTPServer = settings.email_server();
		this.mServerprt = Integer.toString(settings.servport());
		this.mUsername = settings.email_username();
		this.mUserpass = settings.email_userpass();
		this.mSecurity = settings.securityType();
		this.mEmailTo = settings.email_acceptor();
		}
	
	
	@Override
	protected Boolean doInBackground(String... args)
		{
		String subject = args[0];
		String body = args[1];

		SmtpMessageSender messageSender = new SmtpMessageSender();
		try
			{
			Session session = messageSender.createSession(mSMTPServer, mServerprt, mUsername, mUserpass, mSecurity);
			MimeMessage message = messageSender.createMimeMessage(session, subject, mEmailAdress, mEmailTo, Message.RecipientType.TO);
			messageSender.addText(message, body, "UTF-8", "plain");
			if (!(mFiles == null))
				{
				for (File file : mFiles)
					{
					messageSender.addAttachment(message, file); 
					}
				}
			Transport transport = session.getTransport("smtp");
			transport.connect(mSMTPServer, mUsername, mUserpass);

			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			}
		catch (Exception e)
			{
			Log.e("SendMail", "error: " + e);
			Debug.exception(e);
			return false;
			}
		return true;
	}
	
	@Override
    protected void onPostExecute(Boolean result)
		{
		super.onPostExecute(result);
		}
}
