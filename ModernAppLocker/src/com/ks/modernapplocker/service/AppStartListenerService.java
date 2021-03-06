package com.ks.modernapplocker.service;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ks.modernapplocker.common.Util;

public class AppStartListenerService extends ForegroundService {
	/**
	 * 
	 * @author lance.liang 这是一个一直在后台运行的服务， 启用了一个线程，每个一� 氩檠幌挛挥诙ザ擞τ贸绦蚴鞘裁矗绻⑸吮浠� 说明新的应用被启动
	 *         了，此时发送一个广播通知广播接收器，在广播接收器里面 判断应用是否加密
	 */
	final String TAG = getClass().getSimpleName();
	boolean isRunning = false;
	ActivityManager am;
	List<RunningTaskInfo> rtinfos;
	List<RunningAppProcessInfo> rapinfos;
	MyThread myThread;
	String passApp;
	boolean create = false;
	static Object syncObject = new Object();

	@Override
	public void onCreate() {

		am = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
		myThread = new MyThread();
		Log.i(TAG, "onCreate");
		create = true;
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (intent != null) {
			passApp = intent.getStringExtra("passApp");
		}
		if (passApp == null) {
			passApp = "";
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");

		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	void startForegroundCompat(int id) {

		if (!isRunning && create) {
			isRunning = true;
			create = false;
			myThread.start();
			Log.i(TAG, "startForegroundCompat");
		}
		super.startForegroundCompat(id);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		isRunning = false;
		super.onDestroy();
	}

	class MyThread extends Thread {
		String name;
		String name1;
		String name2;
		int count;

		@Override
		public void run() {

			synchronized (syncObject) {
				while (isRunning) {

					try {

						MyThread.sleep(200);

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (passApp == null)
						continue;
					rtinfos = am.getRunningTasks(1);
					// Log.e(TAG,"name:"+am);
					if (rtinfos.size() > 0) {
						RunningTaskInfo info = rtinfos.get(0);
						String tempName = info.baseActivity.getPackageName();
						if (!tempName.equals(getApplicationContext().getPackageName()) && name != null && !name.equals(tempName) && !tempName.equals(passApp)) {
							// isRunning = false;
							// catch a new process on the top,send a broadcast
							Log.i(TAG, "send a broadcast");
							Intent intent = new Intent();
							intent.putExtra("name", tempName);
							intent.putExtra("id", info.id);
							intent.setAction(Util.APP_START_RECEIVER);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							AppStartListenerService.this.sendBroadcast(intent);
							// break;
						}
						name = tempName;
					}

				}

				// isRunning = false;
				AppStartListenerService.this.stopSelf();
			}

		}

	}
}
