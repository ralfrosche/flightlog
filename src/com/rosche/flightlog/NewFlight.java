package com.rosche.flightlog;

import java.io.IOException;
import java.util.Calendar;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class NewFlight extends Activity {
	String[] DataToDB;
	String modelID = "";
	String editID = "";
	boolean editMode = false;

	private TextView mDateDisplay;
	private ImageButton mPickDate;
	private int mYear;
	private int mMonth;
	private int mDay;
	ImageView imageViewChoose;
	static final int DATE_DIALOG_ID = 0;
	int column_index;

	DatabaseHelper myDbHelper = new DatabaseHelper(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newflight);
		final String id = getIntent().getStringExtra("id");
		modelID = getIntent().getStringExtra("model_id");

		Log.e("MListe", "- updated flight ID:" + id);

		if (!id.equals("")) {
			editMode = true;
			EditText sbeschreibung = (EditText) findViewById(R.id.beschreibungedit);
			EditText sdatum = (EditText) findViewById(R.id.dateedit);

			try {
				myDbHelper.createDataBase();
				DataToDB = myDbHelper.ReadFlightFromDB(id);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sdatum.setText(DataToDB[3]);
			sbeschreibung.setText(DataToDB[2]);

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
			String[] params = new String[3];
			Integer insertedID = 0;

			@Override
			public void onClick(View arg0) {
				EditText sbeschreibung = (EditText) findViewById(R.id.beschreibungedit);
				EditText sdatum = (EditText) findViewById(R.id.dateedit);

				params[0] = modelID;
				params[1] = sbeschreibung.getText().toString();
				params[2] = sdatum.getText().toString().trim();
				
				StringTokenizer date = new StringTokenizer(params[2], ".");
				String day = date.nextToken();
				String month = date.nextToken();
				String year = date.nextToken();
				params[2] = String.format("%02d", Integer.parseInt(day))+"."+String.format("%02d", Integer.parseInt(month))+"."+year;
				
				if (!params[0].equals("")) {

					try {

						myDbHelper.createDataBase();
						if (editMode == true) {
							insertedID = myDbHelper.updateFlight(params, id);
							Log.e("MListe", "- updated flight ID:" + insertedID);

						} else {
							insertedID = myDbHelper.insertFlight(params);

							Log.e("MListe", "- inserted flight ID:"
									+ insertedID);

						}

						myDbHelper.close();
						finish();

					} catch (IOException ioe) {

						throw new Error("Unable to open database");

					}
				} else {
					// show error
					Log.e("MListe", "- empty modelid:");
				}

			}

		});

		mDateDisplay = (TextView) findViewById(R.id.dateedit);
		mPickDate = (ImageButton) findViewById(R.id.dateButton);

		mPickDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

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
				new StringBuilder()

				.append(mDay).append(".").append(mMonth + 1).append(".")
						.append(mYear)));
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