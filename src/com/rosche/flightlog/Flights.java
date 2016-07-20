package com.rosche.flightlog;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import android.widget.PopupWindow;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Flights extends Activity {
	ArrayList<ArrayList<String>> DataToDB;
	String modelID = "";
	String editID = "";
	boolean editMode = false;
	View layout;
	TextView tv;
	TextView tvdate;
	String query;
	PopupWindow pw;

	Integer flightId = 0;
	DatabaseHelper myDbHelper = new DatabaseHelper(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flights);
		  getActionBar().setDisplayShowHomeEnabled(false);

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.flightpopup, null);

		pw = new PopupWindow(layout, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);
		pw.setOutsideTouchable(true);

		tv = (TextView) pw.getContentView().findViewById(R.id.flightTextView);
		tvdate = (TextView) pw.getContentView().findViewById(R.id.dateTextView);
		query = getIntent().getStringExtra("query");

		if (!query.equals("")) {
			editMode = true;
			getFlights(query);

		}

		Button cancelButton = (Button) findViewById(R.id.cancelbutton);
		Button newButton = (Button) findViewById(R.id.newbutton);
		Button delButton = (Button) findViewById(R.id.delbutton);
		Button editflButton = (Button) findViewById(R.id.editflbutton);

		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}

		});

		delButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				deleteflight();
				getFlights(query);
			}

		});

		newButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				launchEdit(false);
			}

		});
		editflButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				launchEdit(true);
			}

		});

	}

	private void deleteflight() {
		try {
			if (flightId != 0) {
				myDbHelper.createDataBase();
				myDbHelper.deleteFlightFromDB(flightId);
				getFlights(query);
			} else {
				Log.e("MLISTE", "+++ flightID missing++++");
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	final Handler handler = new Handler();
	Runnable mLongPressed = new Runnable() {
		public void run() {
			Log.i("", "Long press!");
			pw.showAsDropDown(layout);
		}
	};

	private void getFlights(String query) {
		String[] result_array;
		result_array = query.split(":");
		modelID = result_array[0].trim();

		try {
			myDbHelper.createDataBase();
			DataToDB = myDbHelper.ReadFlightsFromDB(query.trim());
			Log.e("MLISTE", "+++ flightsCountInit++++");
			int length = DataToDB.size();
			Log.e("MLISTE", "+++ flightsCount:" + length);
			TableLayout tbl = (TableLayout) findViewById(R.id.tableLayout1);
			tbl.removeAllViews();
			TableRow.LayoutParams params1 = new TableRow.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f);

			TableRow.LayoutParams params2 = new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			TableRow.LayoutParams params3 = new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, 1);
			View vH = new View(this);
			vH.setLayoutParams(params3);
			vH.setBackgroundColor(getResources().getColor(R.color.errorColor));
			tbl.addView(vH);

			for (int i = 0; i < length; i++) {

				final TableRow row = new TableRow(this);
				row.setPadding(10, 2, 0, 0);
				TextView txt1 = new TextView(this);
				TextView txt2 = new TextView(this);
				ArrayList<String> flight_array = new ArrayList<String>();

				flight_array = DataToDB.get(i);
				Log.e("MLISTE", "+++ logliste++++" + flight_array);
				txt1.setText(flight_array.get(3));

				if (flight_array.get(2).length() > 15) {
					txt2.setText(flight_array.get(2).substring(0, 15) + "...");
				} else {
					txt2.setText(flight_array.get(2));
				}
				final String flight_text = flight_array.get(2);
				final String flight_date = flight_array.get(3);
				final int fid = Integer.parseInt(flight_array.get(0));

				if (i == 0) {
					flightId = fid;
				}

				txt1.setLayoutParams(params1);
				txt2.setLayoutParams(params1);
				txt1.setTextSize(18);
				txt2.setTextSize(18);
				View v = new View(this);
				v.setLayoutParams(params3);
				v.setBackgroundColor(getResources()
						.getColor(R.color.errorColor));

				row.addView(txt1);
				row.addView(txt2);
				row.setLayoutParams(params2);
				row.setFocusableInTouchMode(true);
				row.setClickable(true);
				row.setFocusable(true);
				// android:drawable="@drawable/semitransparent_white" />
				row.setBackgroundResource(R.drawable.selector);

				row.setOnTouchListener(new View.OnTouchListener() {
					boolean returncode = false;

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						final int action = event.getAction();

						switch (action & MotionEvent.ACTION_MASK) {
						case MotionEvent.ACTION_DOWN: {
							handler.postDelayed(mLongPressed, 600);
							Log.e("MLISTE", "toucheventdown:");
							flightId = fid;
							tv.setText(flight_text);
							tvdate.setText(flight_date);
							returncode = false;

							break;
						}
						case MotionEvent.ACTION_UP:
						case MotionEvent.ACTION_CANCEL:
						case MotionEvent.ACTION_OUTSIDE:
							// case MotionEvent.ACTION_MOVE:
							handler.removeCallbacks(mLongPressed);
							flightId = fid;
							if (pw != null) {
								pw.dismiss();
							}
							Log.e("MLISTE", "flightID_up_MOVE:" + flightId);
							returncode = false;
							break;
						default:
							returncode = false;
						}
						return returncode;
					}

				});
				// doubleclick on xperia
				row.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {

						flightId = fid;
						Log.e("MLISTE", "flightIDperia:" + flightId);
					}
				});

				tbl.addView(row);
				tbl.addView(v);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.e("MListe", "- onresume flights-");
		getFlights(query);

	}

	private void launchEdit(boolean edit) {
		Intent intent = new Intent(this, NewFlight.class);
		if (edit == true) {
			Log.e("MLISTE", "flightID:" + flightId);
			intent.putExtra("id", String.valueOf(flightId));
			intent.putExtra("model_id", modelID);
		} else {
			intent.putExtra("id", "");
			intent.putExtra("model_id", modelID);
		}
		startActivity(intent);
	}

}
