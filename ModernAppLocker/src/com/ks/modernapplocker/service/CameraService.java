package com.ks.modernapplocker.service;

import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraService extends Service {
	// Camera variables
	// a surface holder
	private SurfaceHolder sHolder;
	// a variable to control the camera
	private Camera mCamera;
	// the camera parameters
	private Parameters parameters;

	/** Called when the activity is first created. */
	@Override
	public void onCreate() {
		super.onCreate();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		try {
			mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
			SurfaceView sv = new SurfaceView(getApplicationContext());
			mCamera.setPreviewDisplay(sv.getHolder());
			parameters = mCamera.getParameters();

			// set camera parameters
			mCamera.setParameters(parameters);
			mCamera.startPreview();
			mCamera.takePicture(null, null, mCall);

			// Get a surface
			sHolder = sv.getHolder();
			// tells Android that this surface will have its data constantly replaced
			sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	Camera.PictureCallback mCall = new Camera.PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			// decode the data obtained by the camera into a Bitmap

			FileOutputStream outStream = null;
			try {
				outStream = new FileOutputStream("/sdcard/Image.jpg");
				outStream.write(data);
				outStream.close();
			} catch (Exception e) {
				Log.d("CAMERA", e.getMessage());
			}
			mCamera.stopPreview();
			stopSelf();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}