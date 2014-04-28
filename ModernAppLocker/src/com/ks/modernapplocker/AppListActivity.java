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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.ks.modernapplocker.adapter.ApplistAdapter;
import com.ks.modernapplocker.common.AnimationFactory;
import com.ks.modernapplocker.common.AnimationFactory.FlipDirection;
import com.ks.modernapplocker.common.Util;
import com.ks.modernapplocker.model.AppInfo;
import com.ks.modernapplocker.service.AppStartListenerService;

public class AppListActivity extends ActionBarActivity implements OnClickListener {

	private final String TAG = getClass().getSimpleName();
	private ListView listView;
	private ArrayList<AppInfo> arrayList;
	private SharedPreferences preferences;
	private String pw_stroed;
	private EditText edtPassword, edtConfirmPass;
	private ViewAnimator viewAnimator;
	private int position = -1;
	private RadioGroup rgLock;
	private String lockType = Util.PW;
	private SeekBar seekBar;
	private RelativeLayout relPin, relPattern, relDialog;

	private LockPatternView mLockPatternView;
	private IEncrypter mEncrypter;
	private static final long DELAY_TIME_TO_RELOAD_LOCK_PATTERN_VIEW = DateUtils.SECOND_IN_MILLIS;
	private int mMinWiredDots = 4;
	public char[] pattern;
	private View listItemView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (isTablet()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		// Now set the content view
		setContentView(R.layout.activity_app_list);

		initComp();
	}

	private boolean isTablet() {
		return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	private void initComp() {
		listView = (ListView) findViewById(R.id.activity_app_list);
		rgLock = (RadioGroup) findViewById(R.id.settings_rg_type);
		seekBar = (SeekBar) findViewById(R.id.settings_rel_dialog_seekBar);
		relPin = (RelativeLayout) findViewById(R.id.settings_rel_pin);
		relPin.setVisibility(View.VISIBLE);
		relPattern = (RelativeLayout) findViewById(R.id.settings_rel_pattern);
		relDialog = (RelativeLayout) findViewById(R.id.settings_rel_dialog);
		viewAnimator = (ViewAnimator) findViewById(R.id.app_list_viewFlipper);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				((TextView) findViewById(R.id.settings_rel_dialog_txt_seek)).setText(progress + "/10");
			}
		});
		arrayList = new ArrayList<AppInfo>();

		preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
		edtPassword = (EditText) findViewById(R.id.activity_applist_edt_pass);
		edtConfirmPass = (EditText) findViewById(R.id.activity_applist_edt_confirm_pass);
		pw_stroed = preferences.getString(Util.PW, "");
		((TextView) findViewById(R.id.activity_applist_txt_label)).setVisibility(View.VISIBLE);
		((Button) findViewById(R.id.activity_applist_btn_confirm)).setVisibility(View.VISIBLE);
		edtPassword.setVisibility(View.VISIBLE);

		if (!pw_stroed.equals("")) {
			((TextView) findViewById(R.id.activity_applist_txt_label)).setText("Enter password");
		} else {
			((TextView) findViewById(R.id.activity_applist_txt_label_note)).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.activity_applist_txt_label_username)).setVisibility(View.VISIBLE);
			((EditText) findViewById(R.id.activity_applist_edt_username)).setVisibility(View.VISIBLE);
			edtConfirmPass.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.activity_applist_txt_label_confirm)).setVisibility(View.VISIBLE);
		}
		listView.setVisibility(View.GONE);
		// }

		rgLock.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.settings_rb_pin) {
					lockType = Util.PW;
					expandCollapse(relPin);
				} else if (checkedId == R.id.settings_rb_pattern) {
					lockType = Util.PATTERN;
					expandCollapse(relPattern);
					initContentView();
				} else if (checkedId == R.id.settings_rb_dialog) {
					lockType = Util.DIALOG;
					expandCollapse(relDialog);
				}

			}
		});

	}

	private void setValues() {
		ApplistAdapter adapter = new ApplistAdapter(AppListActivity.this, arrayList, preferences);
		listView.setAdapter(adapter);
	}

	@Override
	public void onBackPressed() {
		if (listView.getVisibility() == View.VISIBLE || edtPassword.getVisibility() == View.VISIBLE) {
			super.onBackPressed();
		} else {
			AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
		}
	}

	private void expandCollapse(View expand) {
		if (expand == relPin) {
			if (relPattern.getVisibility() == View.VISIBLE) {
				relPattern.setVisibility(View.INVISIBLE);
			} else if (relDialog.getVisibility() == View.VISIBLE) {
				relDialog.setVisibility(View.INVISIBLE);
			}
			relPin.setVisibility(View.VISIBLE);
		} else if (expand == relPattern) {
			if (relPin.getVisibility() == View.VISIBLE) {
				relPin.setVisibility(View.INVISIBLE);
			} else if (relDialog.getVisibility() == View.VISIBLE) {
				relDialog.setVisibility(View.INVISIBLE);
			}
			relPattern.setVisibility(View.VISIBLE);

		} else if (expand == relDialog) {
			if (relPin.getVisibility() == View.VISIBLE) {
				relPin.setVisibility(View.INVISIBLE);
			} else if (relPattern.getVisibility() == View.VISIBLE) {
				relPattern.setVisibility(View.INVISIBLE);
			}
			relDialog.setVisibility(View.VISIBLE);
		}
	}

	private boolean isValidEmail(CharSequence target) {
		if (target == null) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
	}

	public void showDetails(int position, View v) {
		this.position = position;
		listItemView = v;
		AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
		if (preferences.getString(arrayList.get(position).getPkgName() + Util.TYPE, "").equalsIgnoreCase(Util.PW)) {
			expandCollapse(relPin);
		} else if (preferences.getString(arrayList.get(position).getPkgName() + Util.TYPE, "").equalsIgnoreCase(Util.PATTERN)) {
			expandCollapse(relPattern);
		} else if (preferences.getString(arrayList.get(position).getPkgName() + Util.TYPE, "").equalsIgnoreCase(Util.DIALOG)) {
			seekBar.setProgress(Integer.parseInt(preferences.getString(arrayList.get(position).getPkgName() + Util.DIALOG, "0")));
			expandCollapse(relDialog);
		}
	}

	private class GetAllAppsTask extends AsyncTask<Void, Void, Void> {

		private ProgressBar bar;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			bar = (ProgressBar) findViewById(R.id.activity_applist_progressbar);
			bar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				getAllApps();
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			bar.setVisibility(View.GONE);
			if (arrayList.size() != 0) {
				setValues();
			}
		}

	}

	private void getAllApps() throws NameNotFoundException {

		PackageManager pmManager = this.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = pmManager.queryIntentActivities(mainIntent, PackageManager.SIGNATURE_MATCH);
		Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pmManager));
		if (arrayList != null) {
			arrayList.clear();
			StringBuilder strPremisssions;
			PackageInfo pInfo;
			Drawable iconDrawable;
			for (ResolveInfo info : resolveInfos) {
				strPremisssions = new StringBuilder();
				String packagename = info.activityInfo.packageName;
				if (packagename.equals(getPackageName()))
					continue;
				String activityname = info.activityInfo.name;
				String lable = (String) info.loadLabel(pmManager);
				iconDrawable = info.loadIcon(pmManager);

				pInfo = pmManager.getPackageInfo(packagename, PackageManager.GET_PERMISSIONS);
				if (pInfo.requestedPermissions != null && pInfo.requestedPermissions.length != 0) {
					for (int i = 0; i < pInfo.requestedPermissions.length; i++) {
						strPremisssions.append(pInfo.requestedPermissions[i] + "\n");
					}
				}

				AppInfo appInfo = new AppInfo();
				appInfo.setAppIcon(iconDrawable);
				appInfo.setAppLabel(lable);
				appInfo.setAppName(activityname);
				appInfo.setPkgName(packagename);
				appInfo.setPermissions(strPremisssions.toString());
				appInfo.setVersion(pInfo.versionName);
				boolean locked = preferences.getBoolean(packagename + Util.LOKCED, false);
				if (activityname.contains("Settings")) {
					appInfo.setChecked(true);
					appInfo.setLockeIcon(getResources().getDrawable(R.drawable.lock));
					Editor editor1 = preferences.edit();
					editor1.putString(packagename + Util.TYPE, Util.PW);
					editor1.putString(packagename + Util.PW, pw_stroed);
					editor1.putBoolean(appInfo.getPkgName() + Util.LOKCED, true);
					editor1.commit();
				} else {
					appInfo.setChecked(locked);
					if (locked) {
						appInfo.setLockeIcon(getResources().getDrawable(R.drawable.lock));
					} else {
						appInfo.setLockeIcon(getResources().getDrawable(R.drawable.unlock));
					}
				}

				Intent luanchIntent = new Intent();
				luanchIntent.setComponent(new ComponentName(packagename, activityname));
				appInfo.setIntent(luanchIntent);
				arrayList.add(appInfo);
			}
		}
	}

	public void setDatas(ArrayList<AppInfo> arrayList) {
		this.arrayList = arrayList;
	}

	private void setupService() {
		Intent intent = new Intent(AppStartListenerService.ACTION_FOREGROUND);
		intent.setClass(this, AppStartListenerService.class);
		this.startService(intent);
	}

	@Override
	protected void onStop() {
		super.onStop();
		setupService();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_applist_btn_confirm:
			if (!pw_stroed.equals("")) {
				if (!pw_stroed.equalsIgnoreCase(edtPassword.getText().toString())) {
					Toast.makeText(this, getString(R.string.password_error), Toast.LENGTH_LONG).show();
					return;
				}
			} else {
				String pwString = edtPassword.getText().toString();
				String confirmString = edtConfirmPass.getText().toString();
				EditText edtUserName = (EditText) findViewById(R.id.activity_applist_edt_username);
				if (!isValidEmail(edtUserName.getText())) {
					Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_LONG).show();
					return;
				}
				if (pwString.length() < 1 && confirmString.length() < 1) {
					Toast.makeText(this, getString(R.string.no_empty), Toast.LENGTH_LONG).show();
					return;
				}
				if (!pwString.equals(confirmString)) {
					Toast.makeText(this, getString(R.string.twice_diff), Toast.LENGTH_LONG).show();
					return;
				}

				pw_stroed = pwString;
				Editor editor = preferences.edit();
				editor.putString(Util.PW, pwString);
				editor.putString(Util.USERNAME, edtUserName.getText().toString());
				editor.commit();
			}

			listView.setVisibility(View.VISIBLE);
			edtPassword.setVisibility(View.GONE);
			edtConfirmPass.setVisibility(View.GONE);
			((Button) findViewById(R.id.activity_applist_btn_confirm)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.activity_applist_txt_label)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.activity_applist_txt_label_confirm)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.activity_applist_txt_label_note)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.activity_applist_txt_label_username)).setVisibility(View.GONE);
			((EditText) findViewById(R.id.activity_applist_edt_username)).setVisibility(View.GONE);

			new GetAllAppsTask().execute();

			break;

		case R.id.settings_btn_save:
			if (position >= 0) {
				Editor editor1 = preferences.edit();
				editor1.putString(arrayList.get(position).getPkgName() + Util.TYPE, lockType);
				if (rgLock.getCheckedRadioButtonId() == R.id.settings_rb_pin) {
					editor1.putString(arrayList.get(position).getPkgName() + lockType, ((EditText) findViewById(R.id.settings_rel_pin_edt_password)).getText().toString());
				} else if (rgLock.getCheckedRadioButtonId() == R.id.settings_rb_pattern) {
					editor1.putString(arrayList.get(position).getPkgName() + lockType, String.valueOf(pattern));
					Log.e(TAG, "pattern : " + String.valueOf(pattern));
				} else if (rgLock.getCheckedRadioButtonId() == R.id.settings_rb_dialog) {
					editor1.putString(arrayList.get(position).getPkgName() + lockType, String.valueOf(seekBar.getProgress()));
					Log.e(getClass().getSimpleName(), "count : " + seekBar.getProgress());
				}

				boolean checked = arrayList.get(position).isChecked();
				AppInfo appInfo = (AppInfo) listView.getAdapter().getItem(position);
				appInfo.setChecked(!checked);
				if (!checked) {
					appInfo.setLockeIcon(getResources().getDrawable(R.drawable.lock));
				} else {
					appInfo.setLockeIcon(getResources().getDrawable(R.drawable.unlock));
				}
				editor1.putBoolean(appInfo.getPkgName() + Util.LOKCED, !checked);
				editor1.commit();
				arrayList.set(position, appInfo);
				((ApplistAdapter) listView.getAdapter()).setArrayList(arrayList);
				if (listItemView != null) {
					((ImageView) listItemView.findViewById(R.id.imageView2)).setImageDrawable(arrayList.get(position).getLockeIcon());
					listItemView = null;
				}
				AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
				seekBar.setProgress(1);
				((EditText) findViewById(R.id.settings_rel_pin_edt_password)).setText("");
				if (mLockPatternView != null) {
					mLockPatternView.clearPattern();
				}
			}
			break;

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

		// setContentView(R.layout.alp_lock_pattern_activity);
		UI.adjustDialogSizeForLargeScreens(getWindow());

		mLockPatternView = (LockPatternView) findViewById(R.id.settings_app_lock_pattern);

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
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		initContentView();
	}// onConfigurationChanged()

	private final LockPatternView.OnPatternListener mLockPatternViewListener = new LockPatternView.OnPatternListener() {

		@Override
		public void onPatternStart() {
			mLockPatternView.removeCallbacks(mLockPatternViewReloader);
			mLockPatternView.setDisplayMode(DisplayMode.Correct);

		}// onPatternStart()

		@Override
		public void onPatternDetected(List<Cell> pattern) {
			doCheckAndCreatePattern(pattern);
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

	/**
	 * Checks and creates the pattern.
	 * 
	 * @param pattern
	 *            the current pattern of lock pattern view.
	 */
	private void doCheckAndCreatePattern(final List<Cell> pattern1) {
		if (pattern1.size() < mMinWiredDots) {
			mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			mLockPatternView.postDelayed(mLockPatternViewReloader, DELAY_TIME_TO_RELOAD_LOCK_PATTERN_VIEW);
			return;
		}

		/*
		 * Use a LoadingDialog because encrypting pattern might take time...
		 */
		new LoadingDialog<Void, Void, char[]>(this, false) {

			@Override
			protected char[] doInBackground(Void... params) {
				return mEncrypter != null ? mEncrypter.encrypt(AppListActivity.this, pattern1) : LockPatternUtils.patternToSha1(pattern1).toCharArray();
			}// onCancel()

			@Override
			protected void onPostExecute(char[] result) {
				super.onPostExecute(result);
				pattern = result;
			}// onPostExecute()

		}.execute();
	}// doCheckAndCreatePattern()
}
