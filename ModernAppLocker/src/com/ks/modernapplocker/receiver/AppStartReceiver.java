package com.ks.modernapplocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.ks.modernapplocker.LockActivity;
import com.ks.modernapplocker.R;
import com.ks.modernapplocker.common.Util;
import com.ks.modernapplocker.service.AppStartListenerService;

public class AppStartReceiver extends BroadcastReceiver {
	SharedPreferences sharedPreferences;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("AppStartReceiver", "Receive a broadcast");
		sharedPreferences = context.getSharedPreferences(
				context.getString(R.string.app_name), Context.MODE_PRIVATE);
		// check pw
		String name = intent.getStringExtra("name");
		Log.i("AppStartReceiver", name + Util.LOKCED);
		if (sharedPreferences.getBoolean(name + Util.LOKCED, false)) {
			// to input password activity
			Log.i("AppStartReceiver", "to activity");
			intent.setClass(context, LockActivity.class);
			context.startActivity(intent);
			return;
		}

		Intent i = new Intent();
		i.setClass(context, AppStartListenerService.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(i);
	}

}
