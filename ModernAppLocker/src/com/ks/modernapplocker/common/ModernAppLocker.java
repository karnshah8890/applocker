package com.ks.modernapplocker.common;

import java.io.File;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.ks.modernapplocker.R;

public class ModernAppLocker extends Application {

//	public ImageLoader universalImageLoader = ImageLoader.getInstance();

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			PackageManager p = getPackageManager();
			ComponentName componentName = new ComponentName(getPackageName(), "com.ks.modernapplocker.SplashActivity");
			p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
//				getApplicationContext())
//				.threadPoolSize(3)
//				.threadPriority(Thread.MIN_PRIORITY + 2)
//				.memoryCacheSize(2500000)
//				// .memoryCache(new FIFOLimitedMemoryCache(2400000))
//				.denyCacheImageMultipleSizesInMemory()
//				.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
//				.memoryCacheSize(2 * 1024 * 1024)
//				.discCacheFileNameGenerator(new HashCodeFileNameGenerator())
//				// .enableLogging()
//				.imageDownloader(new BaseImageDownloader(this)).build();
//		universalImageLoader.init(config);
		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + getString(R.string.app_name));
		if (!file.exists()) {
			file.mkdir();
		}
	}

//	public ImageLoader getUniversalImageLoader() {
//		return universalImageLoader;
//	}

}
