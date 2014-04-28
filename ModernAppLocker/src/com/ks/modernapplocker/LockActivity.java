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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ks.modernapplocker.common.ShowCamera;
import com.ks.modernapplocker.common.Util;
import com.ks.modernapplocker.service.AppStartListenerService;

public class LockActivity extends ActionBarActivity implements OnClickListener {

	private final String TAG = getClass().getSimpleName();
	private EditText pwdEditText;
	private Button confirmButton;
	private ImageView appIcon;
	private TextView appLable;
	private String packename;
	// private int id;
	private SharedPreferences sPreferences;
	boolean haveChecked;

	private LockPatternView mLockPatternView;
	private IEncrypter mEncrypter;
	private static final long DELAY_TIME_TO_RELOAD_LOCK_PATTERN_VIEW = DateUtils.SECOND_IN_MILLIS;
	private int mRetryCount = 1;

	private int mOrientation = -1;
	private OrientationEventListener eventListener;
	private Camera cameraObject;
	private ShowCamera showCamera;
	private RelativeLayout relPreview;
	private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
	private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
	private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
	private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock);
		packename = getIntent().getStringExtra("name");
		initCompo();
		// id = getIntent().getIntExtra("id", 0);
		initAppInfo();
		openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (eventListener == null) {
			eventListener = new OrientationEventListener(this) {

				@Override
				public void onOrientationChanged(int orientation) {
					// Log.e(getClass().getSimpleName(),
					// "orientation : "+orientation );
					if (orientation >= 315 || orientation < 45) {
						if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
							mOrientation = ORIENTATION_PORTRAIT_NORMAL;
						}
					} else if (orientation < 315 && orientation >= 225) {
						if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
							mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
						}
					} else if (orientation < 225 && orientation >= 135) {
						if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
							mOrientation = ORIENTATION_PORTRAIT_INVERTED;
						}
					} else { // orientation <135 && orientation > 45
						if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
							mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
						}
					}
				}
			};
		}
		if (eventListener.canDetectOrientation()) {
			eventListener.enable();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		eventListener.disable();
		try {
			if (cameraObject != null) {
				cameraObject.stopPreview();
				cameraObject.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openCamera(int cameraId) {

		cameraObject = isCameraAvailiable(cameraId);
		if (cameraObject != null) {
			showCamera = new ShowCamera(this, cameraObject);
			setCameraDisplayOrientation(this, cameraId, cameraObject);
		}
	}

	private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}

		final Camera.Parameters parameters = camera.getParameters();
		parameters.setRotation(degrees);

		camera.setParameters(parameters);
		camera.setDisplayOrientation(result);
	}

	public static Camera isCameraAvailiable(int cameraId) {
		Camera object = null;
		try {
			object = Camera.open(cameraId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	private void initCompo() {
		relPreview = (RelativeLayout) findViewById(R.id.activity_lock_rel_camera);
		pwdEditText = (EditText) findViewById(R.id.pw);
		confirmButton = (Button) findViewById(R.id.btn_confirm);
		appIcon = (ImageView) findViewById(R.id.appIcon);
		appLable = (TextView) findViewById(R.id.appLable);
		confirmButton.setOnClickListener(this);
		sPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

		if (!sPreferences.getString(packename + Util.TYPE, "").equalsIgnoreCase(Util.PW)) {
			// ((RelativeLayout)findViewById(R.id.rel_pin)).setVisibility(View.GONE);
			// ((RelativeLayout)findViewById(R.id.rel_lockpattern)).setVisibility(View.VISIBLE);
			Log.e(TAG, " packename : " + packename);
			initContentView();
		} else {
			((RelativeLayout) findViewById(R.id.rel_pin)).setVisibility(View.VISIBLE);
			((LockPatternView) findViewById(R.id.app_lock_pattern)).setVisibility(View.GONE);
		}
	}

	private void initAppInfo() {
		PackageManager pManager = getPackageManager();
		try {
			ApplicationInfo info = pManager.getApplicationInfo(packename, PackageManager.GET_META_DATA);
			// PackageInfo pInfo = pManager.getPackageInfo(packename,
			// PackageManager.GET_PERMISSIONS);
			// for (int i = 0; i < pInfo.requestedPermissions.length; i++) {
			// Log.e(TAG, " permission : " + pInfo.requestedPermissions[i]);
			// }
			// Log.e(TAG, " versionName : " + pInfo.versionName);
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

		// setContentView(R.layout.alp_lock_pattern_activity);
		UI.adjustDialogSizeForLargeScreens(getWindow());

		mLockPatternView = (LockPatternView) findViewById(R.id.app_lock_pattern);
		Log.e(TAG, "m lock pattern : " + mLockPatternView);
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
			if (!pw.equals(sPreferences.getString(packename + Util.PW, ""))) {
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
		if (sPreferences.getString(packename + Util.TYPE, "").equalsIgnoreCase(Util.PATTERN)) {
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
				char[] currentPattern = sPreferences.getString(packename + Util.PATTERN, "").toCharArray();
				if (currentPattern == null)
					currentPattern = Settings.Security.getPattern(LockActivity.this);
				if (currentPattern != null) {
					Log.e(TAG, "pattern : " + String.valueOf(currentPattern));
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
				Log.e("APp list", "result : " + result);
				if (result) {
					haveChecked = true;
					setupService();
					finish();
				} else {
					if (mRetryCount == 3) {
						try {
							if (showCamera != null) {
								relPreview.setVisibility(View.VISIBLE);
								relPreview.addView(showCamera);
								cameraObject.startPreview();
								cameraObject.setPreviewCallback(new PreviewCallback() {

									@Override
									public void onPreviewFrame(byte[] data, Camera camera) {
										cameraObject.takePicture(null, null, mCall);
									}
								});
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						mRetryCount++;
						mLockPatternView.setDisplayMode(DisplayMode.Wrong);
						mLockPatternView.postDelayed(mLockPatternViewReloader, DELAY_TIME_TO_RELOAD_LOCK_PATTERN_VIEW);
					}
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

	Camera.PictureCallback mCall = new Camera.PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			// decode the data obtained by the camera into a Bitmap
			new ImageCaptureTask().execute(data);
		}
	};

	private class ImageCaptureTask extends AsyncTask<byte[], Void, Boolean> {

		private String fileName;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			cameraObject.stopPreview();
			cameraObject.release();
			relPreview.removeView(showCamera);
		}

		@Override
		protected Boolean doInBackground(byte[]... params) {
			boolean success = false;
			try {
				fileName = getCacheDir().getAbsolutePath() + File.separator + "intruderImage.jpg";
				final Matrix matrix = new Matrix();
				final Camera.CameraInfo info = new Camera.CameraInfo();
				Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					Log.e(TAG, "info : " + Camera.CameraInfo.CAMERA_FACING_FRONT);
					switch (mOrientation) {
					case ORIENTATION_PORTRAIT_NORMAL:
						// image.put(Media.ORIENTATION, 90);
						matrix.postRotate(-90);
						break;
					case ORIENTATION_LANDSCAPE_NORMAL:
						// image.put(Media.ORIENTATION, 0);
						matrix.postRotate(90);
						break;
					case ORIENTATION_PORTRAIT_INVERTED:
						// image.put(Media.ORIENTATION, 270);
						matrix.postRotate(270);
						break;
					case ORIENTATION_LANDSCAPE_INVERTED:
						// image.put(Media.ORIENTATION, 180);
						matrix.postRotate(90);
						break;
					}
				}

				Bitmap bitmap = BitmapFactory.decodeByteArray(params[0], 0, params[0].length);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

				FileOutputStream outStream = new FileOutputStream(fileName);
				success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
				outStream.flush();
				outStream.close();
				if (bitmap != null) {
					bitmap.recycle();
				}
				if (success) {
					Log.e(getClass().getSimpleName(), "file saved >>>>>>>>>>>>>");
				}
			} catch (Exception e) {
				Log.e("CAMERA", e.getMessage());
			}
			return success;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				final ImageView imageView = new ImageView(LockActivity.this);
				imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				imageView.setImageURI(Uri.fromFile(new File(fileName)));
				imageView.setBackgroundColor(Color.WHITE);
				relPreview.setVisibility(View.VISIBLE);
				relPreview.addView(imageView);
				Animation hyperspaceJump = AnimationUtils.loadAnimation(LockActivity.this, R.anim.rotate_scale_anim);
				hyperspaceJump.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						imageView.setVisibility(View.GONE);
						onBackPressed();
					}
				});
				imageView.startAnimation(hyperspaceJump);
				new SendEmail().execute(fileName);
			}
		}

	}

	public class SendEmail extends AsyncTask<String, Void, Boolean> {

		private String fileName;

		@Override
		protected Boolean doInBackground(String... params) {
			fileName = params[0];
			boolean isSended = false;
			String host = "smtp.gmail.com";
			String port = "587";
			String mailFrom = "karn.shah@indianic.com";
			String password = "bhumika83";

			// message info
			String mailTo = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).getString(Util.USERNAME, "");// "karn.shah@indianic.com";
			String subject = "ModernApp Locker finds one intruder.";
			String message = "Someone is tryig to open " + appLable.getText().toString() + " at " + new Date().toString() + " but couldn't able to open it.";

			// attachments
			String[] attachFiles = new String[1];
			attachFiles[0] = fileName;

			try {
				Util.sendEmailWithAttachments(host, port, mailFrom, password, mailTo, subject, message, attachFiles);
				isSended = true;
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}

			return isSended;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				File file = new File(fileName);
				file.delete();
			}
		}

	}

}
