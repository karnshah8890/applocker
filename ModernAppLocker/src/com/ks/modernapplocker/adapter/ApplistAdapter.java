package com.ks.modernapplocker.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ks.modernapplocker.AppListActivity;
import com.ks.modernapplocker.R;
import com.ks.modernapplocker.common.Util;
import com.ks.modernapplocker.model.AppInfo;

public class ApplistAdapter extends BaseAdapter {

	private final String TAG = getClass().getSimpleName();
	private Context context;
	// private ModernAppLocker appLocker;
	// private DisplayImageOptions options;
	private ArrayList<AppInfo> arrayList;
	private SharedPreferences.Editor editor;
	private Animation animation;

	public ApplistAdapter(Context context, ArrayList<AppInfo> arrayList, SharedPreferences preferences) {
		this.context = context;
		this.arrayList = arrayList;
		// this.options = getImageOptions();
		editor = preferences.edit();

		// appLocker = (ModernAppLocker) context.getApplicationContext();
	}

	// private DisplayImageOptions getImageOptions() {
	// final DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
	// .showStubImage(R.drawable.ic_launcher).cacheInMemory()
	// .showImageForEmptyUri(R.drawable.ic_launcher).cacheOnDisc()
	// .imageScaleType(ImageScaleType.EXACTLY).build();
	//
	// return imageOptions;
	// }

	@Override
	public int getCount() {
		return arrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	private class ViewHolder {
		private ImageView imgIcon, imgLock;
		private TextView txtName, txtVersion, txtPermission;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.row_app_list, null);
			holder.imgIcon = (ImageView) convertView.findViewById(R.id.imageView1);
			holder.imgLock = (ImageView) convertView.findViewById(R.id.imageView2);
			holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
			holder.txtVersion = (TextView) convertView.findViewById(R.id.txtVersion);
			// holder.txtPermission = (TextView) convertView
			// .findViewById(R.id.txtPermission);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.imgIcon.setImageDrawable(arrayList.get(position).getAppIcon());
		holder.txtName.setText(arrayList.get(position).getAppLabel());
		holder.txtVersion.setText(arrayList.get(position).getVersion());
		// holder.txtPermission.setText(arrayList.get(position).getPermissions());
		holder.imgLock.setImageDrawable(arrayList.get(position).getLockeIcon());
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean checked = arrayList.get(position).isChecked();
				if (!checked) {
					((AppListActivity) context).showDetails(position, v);
				} else {
					AppInfo appInfo = (AppInfo) arrayList.get(position);
					appInfo.setChecked(false);
					appInfo.setLockeIcon(context.getResources().getDrawable(R.drawable.unlock));
					((ImageView) v.findViewById(R.id.imageView2)).setImageDrawable(context.getResources().getDrawable(R.drawable.unlock));
					editor.putBoolean(appInfo.getPkgName() + Util.LOKCED, false);
					editor.commit();
					arrayList.set(position, appInfo);
				}
			}
		});
		animation = AnimationUtils.loadAnimation(context, R.anim.push_left_in);
		animation.setDuration(500);
		convertView.startAnimation(animation);
		animation = null;

		return convertView;
	}

	public void setArrayList(ArrayList<AppInfo> arrayList) {
		this.arrayList = arrayList;
	}

}
