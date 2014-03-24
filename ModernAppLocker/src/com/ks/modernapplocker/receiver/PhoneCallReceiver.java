package com.ks.modernapplocker.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.ks.modernapplocker.AppListActivity;
import com.ks.modernapplocker.R;
import com.ks.modernapplocker.SplashActivity;
import com.ks.modernapplocker.common.Util;

/**
 * This receiver activates when call is coming or call is placed.
 * 
 * @author indianic
 * 
 */
public class PhoneCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// We listen to two intents. The new outgoing call only tells us of an outgoing call. We use it to get the number.
		if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
			String number = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
			Log.e(getClass().getSimpleName(), "number : "+number);
			final SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
			if (number.equalsIgnoreCase(preferences.getString(Util.NUMBER, "1234"))) {
				Intent i = new Intent(context, AppListActivity.class);
				// i.putExtra("state", state);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
				setResultData(null);
				abortBroadcast();
			}
		}
		abortBroadcast();
	}
}
