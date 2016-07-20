package com.rosche.flightlog;

import java.io.File;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import eu.janmuller.android.simplecropimage.CropImage;




import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;


import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.GridView;

import android.widget.AdapterView.OnItemClickListener;

public class ImageGallery extends Activity {

	private GridView gridView;
	private String editID = "";
	private int InsertId = 0;
	private static ImageItem item = null;
	private static String newImagePath = "";
	private ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
	protected static final int CAPTURE_IMAGE_THUMBNAIL_ACTIVITY_REQUEST_CODE = 1888;
	protected static final int SOME_RANDOM_REQUEST_CODE = 1002;
	protected static final int REQUEST_PICK_IMAGE = 999;
	protected static final int REQUEST_CODE_CROP_IMAGE = 2000;
	protected static final int CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE = 1777;
	private GridViewAdapter customGridAdapter;
	private DatabaseHelper myDbHelper = new DatabaseHelper(this);
	private Activity _activity = this;
	private int columnWidth;
	public static SharedPreferences mPrefs;
	private float padding;
	public String picture_path = "Pictures";
	public boolean crop = false;
	public boolean sizeDown = false;
	public boolean use_internal_cropping = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] result_array;
		String query = getIntent().getStringExtra("query");
		result_array = query.split(":");
		editID = result_array[0].trim();
		setContentView(R.layout.activity_image_view);
		setGrid();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		readPrefs();
	}

	public void readPrefs() {
		picture_path = mPrefs.getString("picture_path", picture_path);
		crop = mPrefs.getBoolean("crop", crop);
		sizeDown = mPrefs.getBoolean("sizeDown", sizeDown);
		use_internal_cropping = mPrefs.getBoolean("use_internal_cropping", use_internal_cropping);
		
	}

	private void InitilizeGridLayout() {
		Resources r = getResources();
		padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
				r.getDisplayMetrics());
		columnWidth = (int) ((getScreenWidth(this) - ((3 + 1) * padding)) / 3);
		gridView.setNumColumns(3);
		gridView.setColumnWidth(columnWidth);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setPadding((int) padding, (int) padding, (int) padding,
				(int) padding);
		gridView.setHorizontalSpacing((int) padding);
		gridView.setVerticalSpacing((int) padding);
	}

	private void setGrid() {
		imageItems.removeAll(imageItems);
		gridView = (GridView) findViewById(R.id.gridView);
		ArrayList<String> imagePaths = new ArrayList<String>();
		imagePaths = myDbHelper.getImages(Integer.parseInt(editID));
		File sdCard = Environment.getExternalStorageDirectory();
		Bitmap bMap = null;
		if (imagePaths.size() > 0) {
			for (int i = 0; i < imagePaths.size(); i++) {
				String path = sdCard.getAbsolutePath() + "/"
						+ imagePaths.get(i).replace(".jpg", "_thumb.jpg");
				bMap = BitmapFactory.decodeFile(path);
				imageItems.add(new ImageItem(bMap, "Picture #" + i, imagePaths
						.get(i)));
			}
		}
		bMap = BitmapFactory.decodeResource(getResources(),
				R.drawable.exposureicon);
		// imageItems.add(new ImageItem(bMap, "New", "nothing"));
		InsertId = imageItems.size() - 199;
		imagePaths = null;
		InitilizeGridLayout();
		int imgageSize = (int) ((getScreenWidth(this) - ((3 + 1) * padding)) / 3);
		customGridAdapter = new GridViewAdapter(this, R.layout.row_grid,
				imageItems, imgageSize);
		gridView.setAdapter(customGridAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				if (position == InsertId) {
					takePicture();

				} else {
					Intent i = new Intent(_activity,
							FullScreenViewActivity.class);
					i.putExtra("position", position);
					i.putExtra("editID", Integer.parseInt(editID));
					startActivity(i);
				}

			}

		});

		gridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				item = imageItems.get(arg2);

				final AlertDialog.Builder b = new AlertDialog.Builder(
						ImageGallery.this);
				b.setIcon(android.R.drawable.ic_dialog_alert);
				b.setMessage("Are you sure to delete this picture?");
				b.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								myDbHelper.deleteImage(item.getimagePath());

								setGrid();
							}
						});
				b.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						});

				b.show();

				return false;
			}

		});
	}

	private void takePicture() {

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss",
				Locale.GERMANY);
		newImagePath = "flight_log/" + dateFormat.format(new Date());
		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + newImagePath + ".jpg");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		//intent.putExtra("crop", "true");
		startActivityForResult(intent,
				CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);

	}

	private void pickMultipleImage() {
		Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
		i.putExtra("picture_path", picture_path);
		startActivityForResult(i, 200);

	}

	public String getPath(Uri uri) {
		String selectedImagePath;
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				null);
		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			selectedImagePath = cursor.getString(column_index);
		} else {
			selectedImagePath = null;
		}

		if (selectedImagePath == null) {
			selectedImagePath = uri.getPath();
		}
		return selectedImagePath;
	}

	private void pickImage() {
		/*
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		File file = new File(Environment.getExternalStorageDirectory(),
				picture_path);
		intent.setDataAndType(Uri.fromFile(file), "image/*");
		// intent.setType("image/*");
		String cropString = "false";
		if (crop == true) {
			cropString = "true";
		} else {
			cropString = "false";
		}
		intent.putExtra("crop", cropString);
		intent.putExtra("return-data", true);
		startActivityForResult(
				Intent.createChooser(intent, "Complete action using"),
				REQUEST_PICK_IMAGE);
		
		*/

		 File root = new File(Environment.getExternalStorageDirectory().getPath()
				 + picture_path);
				         Uri uri = Uri.fromFile(root);
				         Intent intent = new Intent();
				         intent.setAction(Intent.ACTION_GET_CONTENT);
				         intent.setData(uri);
				         intent.setType("image/*");
						if (crop == true) {
							intent.putExtra("crop", "true");
							
						}
						intent.setType("image/*");
						intent.putExtra("return-data", true);
				     	startActivityForResult(
								Intent.createChooser(intent, "Complete action using"),
								REQUEST_PICK_IMAGE);
		
		
	}
	  private void runCropImage(File filepath) {

	      if (use_internal_cropping) {  
	    	  Intent intent = new Intent(this, CropImage.class);
	        intent.putExtra(CropImage.IMAGE_PATH, filepath.getPath());
	        intent.putExtra(CropImage.SCALE, true);

	        intent.putExtra(CropImage.ASPECT_X, 3);
	        intent.putExtra(CropImage.ASPECT_Y, 2);

	        startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
	      }
	    }
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SOME_RANDOM_REQUEST_CODE) {

		}

		else if (requestCode == REQUEST_PICK_IMAGE) {
			Log.e("Flightlog", "resultCode"+resultCode);
			if (resultCode == -1) {
				Bundle extras = data.getExtras();
				Uri imageUri = data.getData();
				
				
				
				if (imageUri != null) {
					Log.e("Flightlog", "filepath"+getPath(imageUri));
					
					String iFile = Environment.getExternalStorageDirectory()
							+ File.separator + newImagePath + ".jpg";
					Log.e("Flightlog", "filepath:"+getPath(imageUri));
					
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"ddMMyyyyHHmmss", Locale.GERMANY);
					newImagePath = "flight_log/"
							+ dateFormat.format(new Date());
					File fileThumb = new File(
							Environment.getExternalStorageDirectory()
									+ File.separator + newImagePath + "_thumb"
									+ ".jpg");
					File fileFull = new File(
							Environment.getExternalStorageDirectory()
									+ File.separator + newImagePath + ".jpg");
					int imgageSize = (int) ((getScreenWidth(this) - ((3 + 1) * padding)) / 3);
					Bitmap bitmap = decodeSampledBitmapFromFile(
							getPath(imageUri), imgageSize, imgageSize);
					FileOutputStream out;
					try {
						out = new FileOutputStream(fileThumb);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
						out.flush();
						out.close();
					} catch (Exception e) {
					}
					BitmapFactory.Options options = new BitmapFactory.Options();
					if (sizeDown) {
						options.inSampleSize = 3;
					}
					bitmap = BitmapFactory.decodeFile(getPath(imageUri),
							options);
					try {
						out = new FileOutputStream(fileFull);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
						out.flush();
						out.close();
					} catch (Exception e) {
					}
					
					runCropImage(fileFull);
					fileFull = null;
					fileThumb = null;
					
					
					myDbHelper.addImage(Integer.parseInt(editID), newImagePath
							+ ".jpg");

				} else if (extras != null) {
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"ddMMyyyyHHmmss", Locale.GERMANY);
					newImagePath = "flight_log/"
							+ dateFormat.format(new Date());
					File fileThumb = new File(
							Environment.getExternalStorageDirectory()
									+ File.separator + newImagePath + "_thumb"
									+ ".jpg");
					File fileFull = new File(
							Environment.getExternalStorageDirectory()
									+ File.separator + newImagePath + ".jpg");

					int imgageSize = (int) ((getScreenWidth(this) - ((3 + 1) * padding)) / 3);

					Bitmap bitmap = extras.getParcelable("data");
					FileOutputStream out;
					try {
						out = new FileOutputStream(fileFull);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
						out.flush();
						out.close();
					} catch (Exception e) {
					}
					String iFile = Environment.getExternalStorageDirectory()
							+ File.separator + newImagePath + ".jpg";
					Log.e("Flightlog", "filepath:"+iFile);
					
					bitmap = decodeSampledBitmapFromFile(
							getPath(Uri.fromFile(fileFull)), imgageSize,
							imgageSize);

					try {
						out = new FileOutputStream(fileThumb);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
						out.flush();
						out.close();
					} catch (Exception e) {
					}
					
					myDbHelper.addImage(Integer.parseInt(editID), newImagePath
							+ ".jpg");
					runCropImage(fileThumb);

				}

			}
			gridView.removeAllViewsInLayout();
			setGrid();
		} else if (requestCode == REQUEST_CODE_CROP_IMAGE) {
		     String path = data.getStringExtra(CropImage.IMAGE_PATH);
		     Log.e("Flightlog","pathCroped:"+path);
          
		} else if (requestCode == CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE) {
			Log.e("resultCode",""+resultCode);
			if (resultCode == -1) {
				String imagePath = Environment.getExternalStorageDirectory()
						+ File.separator + newImagePath + ".jpg";
				File fileThumb = new File(
						Environment.getExternalStorageDirectory()
								+ File.separator + newImagePath + "_thumb"
								+ ".jpg");
				File fileFull = new File(
						Environment.getExternalStorageDirectory()
								+ File.separator + newImagePath + ".jpg");

				int imgageSize = (int) ((getScreenWidth(this) - ((3 + 1) * padding)) / 3);
				Bitmap bitmap = decodeSampledBitmapFromFile(imagePath,
						imgageSize, imgageSize);
				FileOutputStream out;
				try {
					out = new FileOutputStream(fileThumb);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
				} catch (Exception e) {
				}

				BitmapFactory.Options options = new BitmapFactory.Options();
				if (sizeDown) {
					options.inSampleSize = 3;
				}


				bitmap = BitmapFactory.decodeFile(imagePath, options);

				try {
					out = new FileOutputStream(fileFull);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
				} catch (Exception e) {
				}

				myDbHelper.addImage(Integer.parseInt(editID), newImagePath
						+ ".jpg");
				runCropImage(fileFull);
				gridView.removeAllViewsInLayout();
				setGrid();
			}

		} else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {

			String[] all_path = data.getStringArrayExtra("all_path");
			int i = 0;
			for (String imageUri : all_path) {
				boolean convertThumb = false;
				boolean convertFull = false;
				i++;
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"ddMMyyyyHHmmss", Locale.GERMANY);
				newImagePath = "flight_log/" + dateFormat.format(new Date())
						+ '_' + i;
				
				File sdCard = Environment.getExternalStorageDirectory();
				File dir = new File(sdCard.getAbsolutePath() + "/flight_log");
				dir.mkdir();
			
				
				File fileThumb = new File(
						Environment.getExternalStorageDirectory()
								+ File.separator + newImagePath + "_thumb"
								+ ".jpg");
				File fileFull = new File(
						Environment.getExternalStorageDirectory()
								+ File.separator + newImagePath + ".jpg");

				int imgageSize = (int) ((getScreenWidth(this) - ((3 + 1) * padding)) / 3);
				Bitmap bitmap = decodeSampledBitmapFromFile(imageUri,
						imgageSize, imgageSize);
				FileOutputStream out;
				try {
					out = new FileOutputStream(fileThumb);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
					convertThumb = true;
					convertFull = true;
				} catch (Exception e) {
					convertThumb = false;
					convertFull = false;

				}

				BitmapFactory.Options options = new BitmapFactory.Options();
				if (sizeDown) {
					options.inSampleSize = 3;
				}

				bitmap = BitmapFactory.decodeFile(imageUri, options);
				try {
					out = new FileOutputStream(fileFull);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
					convertThumb = true;
					convertFull = true;
				} catch (Exception e) {
					convertThumb = false;
					convertFull = false;
				}
				fileFull = null;
				fileThumb = null;
				if (convertThumb && convertFull) {
					myDbHelper.addImage(Integer.parseInt(editID), newImagePath
							+ ".jpg");
				}

			}

			gridView.removeAllViewsInLayout();
			setGrid();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.image_gallery, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.take_picture:
			takePicture();
			return true;
		case R.id.import_picture:
			pickImage();
			return true;
			// @drawable/edit_query
		case R.id.import_multiple_pictures:
			pickMultipleImage();
			return true;
		}
		return false;
	}

	public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth,
			int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		final int height = options.outHeight;
		final int width = options.outWidth;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		int inSampleSize = 1;

		if (height > reqHeight) {
			inSampleSize = Math.round((float) height / (float) reqHeight);
		}
		int expectedWidth = width / inSampleSize;

		if (expectedWidth > reqWidth) {
			inSampleSize = Math.round((float) width / (float) reqWidth);
		}

		options.inSampleSize = inSampleSize;
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	public int getScreenWidth(Context context) {
		int columnWidth;
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (java.lang.NoSuchMethodError ignore) {
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		columnWidth = point.x;
		return columnWidth;
	}

}
