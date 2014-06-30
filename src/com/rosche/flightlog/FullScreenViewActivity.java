package com.rosche.flightlog;


import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;

import android.util.Log;

public class FullScreenViewActivity extends Activity{

	private FullScreenImageAdapter adapter;
	private ViewPager viewPager;
	private DatabaseHelper myDbHelper = new DatabaseHelper(this);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen_view);

		viewPager = (ViewPager) findViewById(R.id.pager);

		ArrayList<String> imagePaths = new ArrayList<String>();
		Intent intent = getIntent();
		int position = intent.getIntExtra("position", 0);
		int editID = intent.getIntExtra("editID", 0);
 		imagePaths = myDbHelper.getImages(editID);
		ArrayList<String> imageItems = new ArrayList<String>();
 		File sdCard = Environment.getExternalStorageDirectory();
		if (imagePaths.size() > 0) {
 		    for (int i = 0; i < imagePaths.size(); i++) {
 		    	String path = sdCard.getAbsolutePath() + "/" + imagePaths.get(i).replace(".jpg",".jpg");
 		    	imageItems.add(path);
 	        }
 		}
		Log.e("imageItems",""+imageItems);
 		adapter = new FullScreenImageAdapter(FullScreenViewActivity.this,
 				imageItems);
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(position);
 

	}
}
