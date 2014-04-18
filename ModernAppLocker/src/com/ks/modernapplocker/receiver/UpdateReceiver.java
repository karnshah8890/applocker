package com.ks.modernapplocker.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * This receiver activates when call is coming or call is placed.
 * 
 * @author indianic
 * 
 */
public class UpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(getClass().getSimpleName(), "Called @@@@@@@");
		try {
			// PackageManager p = context.getPackageManager();
			// ComponentName componentName = new ComponentName(context.getPackageName(), "com.ks.modernapplocker.SplashActivity");
			// p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			ComponentName componentToDisable = new ComponentName(context, com.ks.modernapplocker.SplashActivity.class);
			context.getPackageManager().setComponentEnabledSetting(componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
