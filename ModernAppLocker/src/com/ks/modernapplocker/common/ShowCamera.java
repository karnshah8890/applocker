package com.ks.modernapplocker.common;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {

	private final String TAG = getClass().getSimpleName();
	private SurfaceHolder holdMe;
	private Camera theCamera;

	public ShowCamera(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ShowCamera(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ShowCamera(Context context) {
		super(context);
	}

	@SuppressWarnings("deprecation")
	public ShowCamera(Context context, Camera camera) {
		super(context);
		theCamera = camera;
		holdMe = getHolder();
		holdMe.addCallback(this);
		if (Build.VERSION.SDK_INT < 11) {
			holdMe.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int width, int height) {
		if (theCamera != null) {
			final Camera.Parameters myParameters = theCamera.getParameters();
			// final Camera.Size myBestSize = getOptimalPreviewSize(width, height, myParameters);
			final Camera.Size myBestSize = getOptimalPreviewSize(myParameters.getSupportedPreviewSizes(), width, height);

			requestLayout();
			if (myBestSize != null) {
				myParameters.setPreviewSize(myBestSize.width, myBestSize.height);
				theCamera.setParameters(myParameters);
			}
//			theCamera.startPreview();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			theCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
	}

	@SuppressWarnings("deprecation")
	public void addinit(Camera cameraObject) {
		theCamera = cameraObject;
		holdMe = getHolder();
		holdMe.addCallback(this);
		holdMe.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) h / w;

		if (sizes == null)
			return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		Log.e(TAG, "optimalSize === Width : " + optimalSize.width + ", Height : " + optimalSize.height);
		return optimalSize;
	}
}