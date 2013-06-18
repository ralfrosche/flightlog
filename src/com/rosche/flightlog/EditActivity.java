package com.rosche.flightlog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class EditActivity extends Activity {

	String[] DataToDB;
	DatabaseHelper myDbHelper = new DatabaseHelper(this);
	boolean editMode = false;
	String editID = "";
	private TextView mDateDisplay;
	private ImageButton mPickDate;
	private int mYear;
	private int mMonth;
	private int mDay;
	ImageView imageViewChoose;
	static final int DATE_DIALOG_ID = 0;
	int column_index;
	String image_path = "";
	Intent intent = null;
	// Declare our Views, so we can access them later
	String logo, imagePath, Logo;
	Cursor cursor;
	// YOU CAN EDIT THIS TO WHATEVER YOU WANT
	private static final int SELECT_PICTURE = 1;

	String selectedImagePath;
	// ADDED
	String filemanagerstring;

	@SuppressWarnings("rawtypes")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		String query = getIntent().getStringExtra("query");

		if (!query.equals("")) {
			String[] result_array;
			result_array = query.split(":");
			editID = result_array[0].trim();
			editMode = true;
			try {

				EditText sname = (EditText) findViewById(R.id.nameedit);
				EditText sbeschreibung = (EditText) findViewById(R.id.beschreibungedit);
				EditText sdatum = (EditText) findViewById(R.id.dateedit);
				EditText sspannweite = (EditText) findViewById(R.id.spannweiteedit);
				EditText slaenge = (EditText) findViewById(R.id.laengeedit);
				EditText sgewicht = (EditText) findViewById(R.id.gewichtedit);
				EditText src_data = (EditText) findViewById(R.id.rc_dataedit);
				EditText sausstattung = (EditText) findViewById(R.id.aussttungedit);
				Spinner styp = (Spinner) findViewById(R.id.typspinner);
				Spinner sstatus = (Spinner) findViewById(R.id.statusspinner);

				myDbHelper.createDataBase();
				DataToDB = myDbHelper.ReadFromDB(query.trim());

				String myString = DataToDB[1];

				@SuppressWarnings("rawtypes")
				ArrayAdapter myAdap = (ArrayAdapter) styp.getAdapter(); // cast
																		// to an
																		// ArrayAdapter

				for (int index = 0, count = myAdap.getCount(); index < count; ++index) {
					if (myAdap.getItem(index).equals(myString)) {
						styp.setSelection(index);
						break;
					}
				}

				myString = DataToDB[9];

				myAdap = (ArrayAdapter) sstatus.getAdapter(); // cast to an
																// ArrayAdapter

				for (int index = 0, count = myAdap.getCount(); index < count; ++index) {
					if (myAdap.getItem(index).equals(myString)) {
						sstatus.setSelection(index);
						break;
					}
				}

				sname.setText(DataToDB[0]);

				sbeschreibung.setText(DataToDB[2]);
				sdatum.setText(DataToDB[3]);
				sspannweite.setText(DataToDB[4]);
				slaenge.setText(DataToDB[5]);
				sgewicht.setText(DataToDB[6]);
				src_data.setText(DataToDB[7]);
				sausstattung.setText(DataToDB[8]);
				image_path = myDbHelper.getImage(Integer.parseInt(editID));
				myDbHelper.close();
				Log.e("MListe", "- inmage:" + image_path);
				imageViewChoose = (ImageView) findViewById(R.id.imageViewChoose);

				if (!image_path.equals("")) {
					Uri mUri;
					File sdCard = Environment.getExternalStorageDirectory();
					String f = sdCard.getAbsolutePath() + "/" + image_path;

					mUri = Uri.parse(f);
					Log.e("MListe", "- updated model ID:" + f);
					Log.e("MListe", "- updated model ID:" + mUri);
					File file = new File(f);
					if (file.exists()) {
						imageViewChoose.setImageURI(mUri);
					} else {
						imageViewChoose.setImageResource(R.drawable.spitfire33);
					}

				} else {
					imageViewChoose.setImageResource(R.drawable.spitfire33);
				}

			} catch (IOException ioe) {

				throw new Error("Unable to open database");

			}

		} else {
			editMode = false;
		}

		Button cancelButton = (Button) findViewById(R.id.cancelbutton);
		Button saveButton = (Button) findViewById(R.id.savebutton);
		imageViewChoose = (ImageView) findViewById(R.id.imageViewChoose);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}

		});
		imageViewChoose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						SELECT_PICTURE);

			}
		});

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String[] params = new String[10];
				Integer id;
				Integer idImage;
				EditText sname = (EditText) findViewById(R.id.nameedit);
				EditText sbeschreibung = (EditText) findViewById(R.id.beschreibungedit);
				EditText sdatum = (EditText) findViewById(R.id.dateedit);
				EditText sspannweite = (EditText) findViewById(R.id.spannweiteedit);
				EditText slaenge = (EditText) findViewById(R.id.laengeedit);
				EditText sgewicht = (EditText) findViewById(R.id.gewichtedit);
				EditText src_data = (EditText) findViewById(R.id.rc_dataedit);
				EditText sausstattung = (EditText) findViewById(R.id.aussttungedit);
				Spinner styp = (Spinner) findViewById(R.id.typspinner);
				Spinner sstatus = (Spinner) findViewById(R.id.statusspinner);

				params[0] = sname.getText().toString();
				params[1] = styp.getSelectedItem().toString();
				params[2] = sbeschreibung.getText().toString();
				params[3] = sdatum.getText().toString();
				params[4] = sspannweite.getText().toString();
				params[5] = slaenge.getText().toString();
				params[6] = sgewicht.getText().toString();
				params[7] = src_data.getText().toString();
				params[8] = sausstattung.getText().toString();
				params[9] = sstatus.getSelectedItem().toString();

				if (!params[0].equals("")) {

					try {

						myDbHelper.createDataBase();
						if (editMode == true) {
							id = myDbHelper.update(params, editID);
							idImage = myDbHelper.updateImage(id, image_path);
							Log.e("MListe", "- updated model ID:" + id
									+ " Image:" + idImage);
						} else {
							id = myDbHelper.insert(params);
							myDbHelper.insertImage(id, image_path);
							Log.e("MListe", "- inserted new ID:" + id);

						}

						myDbHelper.close();
						finish();

					} catch (IOException ioe) {

						throw new Error("Unable to open database");

					}
				} else {
					// show error
					Log.e("MListe", "- empty name:");
				}
			}

		});

		mDateDisplay = (TextView) findViewById(R.id.dateedit);
		mPickDate = (ImageButton) findViewById(R.id.dateButton);

		// add a click listener to the button
		mPickDate.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		// display the current date (this method is below)
		if (editMode != true)
			updateDisplay();
	}

	// updates the date in the TextView
	private void updateDisplay() {
		mDateDisplay = (TextView) findViewById(R.id.dateedit);
		mDateDisplay.setText(getString(R.string.strSelectedDate,
				new StringBuilder().append(mDay).append(".").append(mMonth + 1)
						.append(".").append(mYear)));
	}

	// the callback received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDisplay();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		}
		return null;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Bitmap newImage = null;
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"ddMMyyyyHHmmss", Locale.GERMANY);
			image_path = "flight_log/" + dateFormat.format(new Date()) + ".jpg";
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/flight_log");
			dir.mkdir();

			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				try {
					newImage = decodeUri(selectedImageUri);

					saveBitmap(newImage, image_path);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				imageViewChoose = (ImageView) findViewById(R.id.imageViewChoose);

				imageViewChoose.setImageBitmap(newImage);
			}

		}

	}

	private void saveBitmap(Bitmap newImage, String image_path) {
		imageViewChoose = (ImageView) findViewById(R.id.imageViewChoose);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		newImage.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

		File f = new File(Environment.getExternalStorageDirectory()
				+ File.separator + image_path);
		FileOutputStream fo = null;
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// write the bytes in file

		try {
			fo = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fo.write(bytes.toByteArray());
			fo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// remember close de FileOutput

	}

	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o);
		final int REQUIRED_SIZE = 200;
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o2);

	}
}
