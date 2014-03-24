package com.ks.modernapplocker;

import group.pals.android.lib.ui.lockpattern.util.IEncrypter;
import group.pals.android.lib.ui.lockpattern.util.InvalidEncrypterException;
import group.pals.android.lib.ui.lockpattern.util.LoadingDialog;
import group.pals.android.lib.ui.lockpattern.util.Settings;
import group.pals.android.lib.ui.lockpattern.util.UI;
import group.pals.android.lib.ui.lockpattern.widget.LockPatternUtils;
import group.pals.android.lib.ui.lockpattern.widget.LockPatternView;
import group.pals.android.lib.ui.lockpattern.widget.LockPatternView.Cell;
import group.pals.android.lib.ui.lockpattern.widget.LockPatternView.DisplayMode;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ks.modernapplocker.common.Util;
import com.ks.modernapplocker.service.AppStartListenerService;

public class LockActivity extends ActionBarActivity implements OnClickListener {

	private final String TAG = getClass().getSimpleName();
	private EditText pwdEditText;
	private Button confirmButton;
	private ImageView appIcon;
	private TextView appLable;
	private String packename;
	private int id;
	private SharedPreferences sPreferences;
	boolean haveChecked;

	private LockPatternView mLockPatternView;
	private IEncrypter mEncrypter;
	private static final long DELAY_TIME_TO_RELOAD_LOCK_PATTERN_VIEW = DateUtils.SECOND_IN_MILLIS;
	private int mRetryCount = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock);
		packename = getIntent().getStringExtra("name");
		initCompo();
		id = getIntent().getIntExtra("id", 0);
		initAppInfo();
	}

	private void initCompo() {
		pwdEditText = (EditText) findViewById(R.id.pw);
		confirmButton = (Button) findViewById(R.id.btn_confirm);
		appIcon = (ImageView) findViewById(R.id.appIcon);
		appLable = (TextView) findViewById(R.id.appLable);
		confirmButton.setOnClickListener(this);
		sPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

		if (!sPreferences.getString(packename+Util.TYPE, "").equalsIgnoreCase(Util.PW)) {
//			((RelativeLayout)findViewById(R.id.rel_pin)).setVisibility(View.GONE);
//			((RelativeLayout)findViewById(R.id.rel_lockpattern)).setVisibility(View.VISIBLE);
			Log.e(TAG, " packename : " + packename);
			initContentView();
		}else {
			((RelativeLayout)findViewById(R.id.rel_pin)).setVisibility(View.VISIBLE);
			((LockPatternView)findViewById(R.id.settings_app_lock_pattern)).setVisibility(View.GONE);
		}
	}

	private void initAppInfo() {
		PackageManager pManager = getPackageManager();
		try {
			ApplicationInfo info = pManager.getApplicationInfo(packename, PackageManager.GET_META_DATA);
			PackageInfo pInfo = pManager.getPackageInfo(packename, PackageManager.GET_PERMISSIONS);
//			for (int i = 0; i < pInfo.requestedPermissions.length; i++) {
//				Log.e(TAG, " permission : " + pInfo.requestedPermissions[i]);
//			}
//			Log.e(TAG, " versionName : " + pInfo.versionName);
			appIcon.setImageDrawable(info.loadIcon(pManager));
			appLable.setText(info.loadLabel(pManager));

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Initializes UI...
	 */
	private void initContentView() {
		/*
		 * Encrypter.
		 */
		char[] encrypterClass = Settings.Security.getEncrypterClass(this);
		if (encrypterClass != null) {
			try {
				mEncrypter = (IEncrypter) Class.forName(new String(encrypterClass), false, getClassLoader()).newInstance();
			} catch (Throwable t) {
				throw new InvalidEncrypterException();
			}
		}

		/*
		 * Save all controls' state to restore later.
		 */
		LockPatternView.DisplayMode lastDisplayMode = mLockPatternView != null ? mLockPatternView.getDisplayMode() : null;
		List<Cell> lastPattern = mLockPatternView != null ? mLockPatternView.getPattern() : null;

//		setContentView(R.layout.alp_lock_pattern_activity);
		UI.adjustDialogSizeForLargeScreens(getWindow());

		mLockPatternView = (LockPatternView) findViewById(R.id.app_lock_pattern);
		Log.e(TAG, "m lock pattern : "+mLockPatternView);
		/*
		 * LOCK PATTERN VIEW
		 */

		switch (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
		case Configuration.SCREENLAYOUT_SIZE_LARGE:
		case Configuration.SCREENLAYOUT_SIZE_XLARGE: {
			final int size = getResources().getDimensionPixelSize(R.dimen.alp_lockpatternview_size);
			LayoutParams lp = mLockPatternView.getLayoutParams();
			lp.width = size;
			lp.height = size;
			mLockPatternView.setLayoutParams(lp);

			break;
		}// LARGE / XLARGE
		}

		/*
		 * Haptic feedback.
		 */
		boolean hapticFeedbackEnabled = false;
		try {
			hapticFeedbackEnabled = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0;
		} catch (Throwable t) {
			/*
			 * Ignore it.
			 */
		}
		mLockPatternView.setTactileFeedbackEnabled(hapticFeedbackEnabled);

		mLockPatternView.setInStealthMode(Settings.Display.isStealthMode(this));
		mLockPatternView.setOnPatternListener(mLockPatternViewListener);
		if (lastPattern != null && lastDisplayMode != null)
			mLockPatternView.setPattern(lastDisplayMode, lastPattern);

	}// initContentView()

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_confirm) {
			String pw = pwdEditText.getText().toString();
			if (!pw.equals(sPreferences.getString(packename+Util.PW, ""))) {
				// password error
				Toast.makeText(this, getString(R.string.password_error), Toast.LENGTH_LONG).show();
				return;
			}
			haveChecked = true;
			setupService();
			this.finish();
		}
	}

	@Override
	public void onBackPressed() {
		if (!haveChecked) {
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
	}

	private void setupService() {
		Intent intent = new Intent();
		intent.putExtra("passApp", packename);
		intent.setClass(this, AppStartListenerService.class);
		this.startService(intent);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.e(TAG, " packename : " + packename);
		if (sPreferences.getString(packename+Util.TYPE, "").equalsIgnoreCase(Util.PATTERN)) {
			initContentView();
		}
	}// onConfigurationChanged()

	private final LockPatternView.OnPatternListener mLockPatternViewListener = new LockPatternView.OnPatternListener() {

		@Override
		public void onPatternStart() {
			mLockPatternView.removeCallbacks(mLockPatternViewReloader);
			mLockPatternView.setDisplayMode(DisplayMode.Correct);

		}// onPatternStart()

		@Override
		public void onPatternDetected(List<Cell> pattern) {
			doComparePattern(pattern);
		}// onPatternDetected()

		@Override
		public void onPatternCleared() {
			mLockPatternView.removeCallbacks(mLockPatternViewReloader);

			mLockPatternView.setDisplayMode(DisplayMode.Correct);
		}// onPatternCleared()

		@Override
		public void onPatternCellAdded(List<Cell> pattern) {
		}// onPatternCellAdded()
	};// mLockPatternViewListener

	private void doComparePattern(final List<Cell> pattern) {
		if (pattern == null)
			return;

		/*
		 * Use a LoadingDialog because decrypting pattern might take time...
		 */

		new LoadingDialog<Void, Void, Boolean>(this, false) {

			@Override
			protected Boolean doInBackground(Void... params) {
				char[] currentPattern = sPreferences.getString(packename+Util.PATTERN, "").toCharArray();
				if (currentPattern == null)
					currentPattern = Settings.Security.getPattern(LockActivity.this);
				if (currentPattern != null) {
					Log.e(TAG, "pattern : "+String.valueOf(currentPattern));
					if (mEncrypter != null)
						return pattern.equals(mEncrypter.decrypt(LockActivity.this, currentPattern));
					else
						return Arrays.equals(currentPattern, LockPatternUtils.patternToSha1(pattern).toCharArray());
				}

				return false;
			}// doInBackground()

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				Log.e("APp list", "result : "+result);
				if (result) {
					haveChecked = true;
					setupService();
					finish();
				} else {
					mRetryCount++;
					mLockPatternView.setDisplayMode(DisplayMode.Wrong);
					mLockPatternView.postDelayed(mLockPatternViewReloader, DELAY_TIME_TO_RELOAD_LOCK_PATTERN_VIEW);
				}
			}// onPostExecute()

		}.execute();
	}// doComparePattern()

	/**
	 * This reloads the {@link #mLockPatternView} after a wrong pattern.
	 */
	private final Runnable mLockPatternViewReloader = new Runnable() {

		@Override
		public void run() {
			mLockPatternView.clearPattern();
			mLockPatternViewListener.onPatternCleared();
		}// run()
	};// mLockPatternViewReloader

}
