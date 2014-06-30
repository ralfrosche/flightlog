package com.rosche.flightlog;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GridViewAdapter extends ArrayAdapter<ImageItem> {
	private Context context;
	private int imageWidth;
	private int layoutResourceId;
	private ArrayList<ImageItem> data = new ArrayList<ImageItem>();

	public GridViewAdapter(Context context, int layoutResourceId,
			ArrayList<ImageItem> data, int imagewidth) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
		this.imageWidth = imagewidth;
	
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new ViewHolder();
			ImageView iV= (ImageView) row.findViewById(R.id.image);
			int height = iV.getLayoutParams().height;
			iV.setScaleType(ImageView.ScaleType.FIT_CENTER);
			LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(imageWidth,height);
			iV.setLayoutParams(parms);
			holder.image = iV;
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}
		ImageItem item = data.get(position);
		holder.image.setImageBitmap(item.getImage());
		return row;
	}

	static class ViewHolder {
		ImageView image;
	}
}