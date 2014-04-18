package com.ks.modernapplocker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.TextView;

import com.ks.modernapplocker.common.Util;
import com.ks.modernapplocker.service.AppStartListenerService;

public class DialogActivity extends ActionBarActivity {

	private String packename;
	private String appName = "Application";
	private int count = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(null);
		getSupportActionBar().hide();
		packename = getIntent().getStringExtra("name");
		PackageManager pManager = getPackageManager();
		try {
			ApplicationInfo info = pManager.getApplicationInfo(packename, PackageManager.GET_META_DATA);
			appName = info.loadLabel(pManager).toString();

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		showAlert();
	}

	private void showAlert() {
		SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
		final int finalCount = Integer.parseInt(preferences.getString(packename + Util.DIALOG, "5"));
		Log.e(getClass().getSimpleName(), "count : " + finalCount);
		final AlertDialog.Builder dialog = new AlertDialog.Builder(DialogActivity.this);
		dialog.setCancelable(false);
		Dialog dialog2 = dialog.create();
		dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
		TextView view = new TextView(this);
		view.setPadding(5, 5, 5, 5);
		view.setTextSize(16);
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			view.setTextColor(Color.WHITE);
		}
		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (count == finalCount) {
					setupService();
					finish();
				} else {
					count++;
				}
			}
		});
		view.setText("Unfortunately,\n" + appName + " has stopped.");
		dialog.setView(view);
		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				onBackPressed();
			}
		});
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				onBackPressed();
			}
		});
		dialog.show();
	}

	@Override
	public void onBackPressed() {
		// restart this activity
		// startActivity(getIntent());
		// killAppProcess();

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
		setupService();
		finish();
	}

	private void setupService() {
		Intent intent = new Intent();
		intent.putExtra("passApp", packename);
		intent.setClass(this, AppStartListenerService.class);
		this.startService(intent);
	}
}
