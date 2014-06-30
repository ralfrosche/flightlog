package com.rosche.flightlog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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
	public static SharedPreferences mPrefs;
	static final int DATE_DIALOG_ID = 0;
	int column_index;
	String image_path = "";
	public String art_array = "Verbennerflugmodell,Elektroflugmodell,Scaleflugmodell,Hotliner,Warbird,Trainer,F3K Segler,Speedflieger,Combatmodell";
	public String typ_array = "Fesselflugmodell,Motorflugmodell,Segelflugmodell,Hubschrauber,Quadrocopter";
	public String status_array = "aktiv,verkauft,defekt,zerstört,unfertig";
	Intent intent = null;
	String logo, imagePath, Logo;
	Cursor cursor;
	String selectedImagePath;
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
				mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
				readPrefs();
				EditText sname = (EditText) findViewById(R.id.nameedit);
				EditText sbeschreibung = (EditText) findViewById(R.id.beschreibungedit);
				EditText shersteller = (EditText) findViewById(R.id.hesrtelleredit);
				EditText sdatum = (EditText) findViewById(R.id.dateedit);
				EditText sspannweite = (EditText) findViewById(R.id.spannweiteedit);
				EditText slaenge = (EditText) findViewById(R.id.laengeedit);
				EditText sgewicht = (EditText) findViewById(R.id.gewichtedit);
				EditText src_data = (EditText) findViewById(R.id.rc_dataedit);
				EditText sausstattung = (EditText) findViewById(R.id.aussttungedit);

				Spinner styp = (Spinner) findViewById(R.id.typspinner);
				Spinner sart = (Spinner) findViewById(R.id.artspinner);
				Spinner sstatus = (Spinner) findViewById(R.id.statusspinner);

				ArrayList<String> slist = new ArrayList<String>();
				ArrayList<String> tlist = new ArrayList<String>();
				ArrayList<String> alist = new ArrayList<String>();
				ArrayAdapter<String> sadapter;
				ArrayAdapter<String> tadapter;
				ArrayAdapter<String> aadapter;

				slist.addAll(Arrays.asList(status_array.split("\\s*,\\s*")));
				sadapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_spinner_item, slist);
				sstatus.setAdapter(sadapter);

				tlist.addAll(Arrays.asList(typ_array.split("\\s*,\\s*")));
				tadapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_spinner_item, tlist);
				styp.setAdapter(tadapter);

				alist.addAll(Arrays.asList(art_array.split("\\s*,\\s*")));
				aadapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_spinner_item, alist);
				sart.setAdapter(aadapter);

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

				myString = DataToDB[11];
				myAdap = (ArrayAdapter) sart.getAdapter(); // cast
				for (int index = 0, count = myAdap.getCount(); index < count; ++index) {
					if (myAdap.getItem(index).equals(myString)) {
						sart.setSelection(index);
						break;
					}
				}

				myString = DataToDB[9];

				myAdap = (ArrayAdapter) sstatus.getAdapter();

				for (int index = 0, count = myAdap.getCount(); index < count; ++index) {
					if (myAdap.getItem(index).equals(myString)) {
						sstatus.setSelection(index);
						break;
					}
				}

				sname.setText(DataToDB[0]);

				sbeschreibung.setText(DataToDB[2]);
				shersteller.setText(DataToDB[12]);
				sdatum.setText(DataToDB[3]);
				sspannweite.setText(DataToDB[4]);
				slaenge.setText(DataToDB[5]);
				sgewicht.setText(DataToDB[6]);
				src_data.setText(DataToDB[7]);
				sausstattung.setText(DataToDB[8]);
				image_path = myDbHelper.getImage(Integer.parseInt(editID));
				myDbHelper.close();
						
			} catch (IOException ioe) {

				throw new Error("Unable to open database");

			}

		} else {
			editMode = false;
			Spinner styp = (Spinner) findViewById(R.id.typspinner);
			Spinner sart = (Spinner) findViewById(R.id.artspinner);
			Spinner sstatus = (Spinner) findViewById(R.id.statusspinner);
			ArrayList<String> slist = new ArrayList<String>();
			ArrayList<String> tlist = new ArrayList<String>();
			ArrayList<String> alist = new ArrayList<String>();
			ArrayAdapter<String> sadapter;
			ArrayAdapter<String> tadapter;
			ArrayAdapter<String> aadapter;

			slist.addAll(Arrays.asList(status_array.split("\\s*,\\s*")));
			sadapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, slist);
			sstatus.setAdapter(sadapter);

			tlist.addAll(Arrays.asList(typ_array.split("\\s*,\\s*")));
			tadapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, tlist);
			styp.setAdapter(tadapter);

			alist.addAll(Arrays.asList(art_array.split("\\s*,\\s*")));
			aadapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, alist);
			sart.setAdapter(aadapter);
		}

		Button cancelButton = (Button) findViewById(R.id.cancelbutton);
		Button saveButton = (Button) findViewById(R.id.savebutton);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}

		});

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String[] params = new String[12];
				Integer id;
				Integer idImage;
				EditText sname = (EditText) findViewById(R.id.nameedit);
				EditText sbeschreibung = (EditText) findViewById(R.id.beschreibungedit);
				EditText shersteller = (EditText) findViewById(R.id.hesrtelleredit);
				EditText sdatum = (EditText) findViewById(R.id.dateedit);
				EditText sspannweite = (EditText) findViewById(R.id.spannweiteedit);
				EditText slaenge = (EditText) findViewById(R.id.laengeedit);
				EditText sgewicht = (EditText) findViewById(R.id.gewichtedit);
				EditText src_data = (EditText) findViewById(R.id.rc_dataedit);
				EditText sausstattung = (EditText) findViewById(R.id.aussttungedit);
				Spinner styp = (Spinner) findViewById(R.id.typspinner);
				Spinner sart = (Spinner) findViewById(R.id.artspinner);
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
				params[10] = sart.getSelectedItem().toString();
				params[11] = shersteller.getText().toString();

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


		if (editMode != true)
			updateDisplay();
	}


	private void updateDisplay() {
		mDateDisplay = (TextView) findViewById(R.id.dateedit);
		mDateDisplay.setText(getString(R.string.strSelectedDate,
				new StringBuilder().append(mDay).append(".").append(mMonth + 1)
						.append(".").append(mYear)));
	}

	public void readPrefs() {
		String valueTmp = mPrefs.getString("typ_array", typ_array);
		if (!valueTmp.equals("")) {
			typ_array = valueTmp;
		}

		valueTmp = mPrefs.getString("status_array", status_array);
		if (!valueTmp.equals("")) {
			status_array = valueTmp;
		}
		valueTmp = mPrefs.getString("art_array", art_array);
		if (!valueTmp.equals("")) {
			art_array = valueTmp;
		}
	}

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

}
