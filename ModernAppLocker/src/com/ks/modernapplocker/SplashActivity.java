package com.ks.modernapplocker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class SplashActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			PackageManager p = getPackageManager();
//			ComponentName componentName = new ComponentName(getPackageName(),SplashActivity.class.getSimpleName());
			p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Intent intent = new Intent(getApplicationContext(), AppListActivity.class);
		startActivity(intent);
		finish();
	}
}
