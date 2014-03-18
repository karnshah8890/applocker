package com.ks.modernapplocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ks.modernapplocker.service.AppStartListenerService;

public class KeepAliveReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("KeepAliveReceiver", "KeepAliveReceiver");
		Intent i = new Intent(AppStartListenerService.ACTION_FOREGROUND);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setClass(context, AppStartListenerService.class);
		context.startService(i);
	}

}
