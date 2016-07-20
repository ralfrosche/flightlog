package com.rosche.flightlog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.StringTokenizer;

import android.app.Activity;

import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class newField extends Activity implements OnItemSelectedListener{
	ArrayList<ArrayList<String>> DataToDB;
	String[] fieldRecord;
	String modelID = "";
	String editID = "";
	boolean editMode = false;
	public static SharedPreferences mPrefs;


	ImageView imageViewChoose;
	static final int DATE_DIALOG_ID = 0;
	int column_index;
	DatabaseHelper myDbHelper = new DatabaseHelper(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newfield);
		getActionBar().setDisplayShowHomeEnabled(false);
		final String id = getIntent().getStringExtra("id");
				
		modelID = getIntent().getStringExtra("model_id");
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	

		try {
			myDbHelper.createDataBase();
			DataToDB = myDbHelper.getCustomFields();
			Log.e("DataToDB","DataToDB:"+DataToDB);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Spinner fieldTyp = (Spinner) findViewById(R.id.fieldTyp);
		List<CharSequence> choices = new ArrayList<CharSequence>();
		int length = DataToDB.size();
		for (int i = 0; i < length; i++) {
			ArrayList<String> fieldArray = new ArrayList<String>();
			fieldArray = DataToDB.get(i);
			String field = fieldArray.get(1);
			choices.add(field);
		}
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item, choices);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		fieldTyp.setAdapter(adapter);
		fieldTyp.setOnItemSelectedListener(this);
		
		if (!id.equals("")) {
			
			editMode = true;
			
			EditText fieldlabel = (EditText) findViewById(R.id.fieldlabel);
			EditText fieldHint = (EditText) findViewById(R.id.fieldHint);
			CheckBox fieldVisible = (CheckBox) findViewById(R.id.fieldVisible);
			// CheckBox fieldGlobal = (CheckBox) findViewById(R.id.fieldGlobal);
		
			try {
				myDbHelper.createDataBase();
				fieldRecord = myDbHelper.ReadFieldFromDB(id);
				Log.e("Flightlog","fieldRecord:"+fieldRecord);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			final TableRow tableRowGlobal = (TableRow) findViewById(R.id.tableRowGlobal);
			tableRowGlobal.setVisibility(View.GONE);
			View ruler = (View) findViewById(R.id.viewGlobal);
			ruler.setVisibility(View.GONE);
			
			if (fieldRecord[4].equals("1")) {
				fieldVisible.setChecked(true);
			} else {
				fieldVisible.setChecked(false);
			}
			
			fieldlabel.setText(fieldRecord[2]);
			fieldHint.setText(fieldRecord[3]);
			String myString = fieldRecord[5];

			@SuppressWarnings("rawtypes")
		
			ArrayAdapter myAdap = (ArrayAdapter) fieldTyp.getAdapter();
			for (int index = 0, count = myAdap.getCount(); index < count; ++index) {
				if (myAdap.getItem(index).equals(myString)) {
					fieldTyp.setSelection(index);
					break;
				}
			}
			

		}

		Button cancelButton = (Button) findViewById(R.id.fieldcancelbutton);
		Button saveButton = (Button) findViewById(R.id.fieldsavebutton);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}

		});
		saveButton.setOnClickListener(new View.OnClickListener() {
			String[] params = new String[6];
			@Override
			public void onClick(View arg0) {
				EditText fieldlabel = (EditText) findViewById(R.id.fieldlabel);
				EditText fieldHint = (EditText) findViewById(R.id.fieldHint);
				CheckBox fieldVisible = (CheckBox) findViewById(R.id.fieldVisible);
				if (fieldVisible.isChecked()) {
					params[5] = "1";
				}else {
					params[5] = "0";
				}
				CheckBox fieldGlobal = (CheckBox) findViewById(R.id.fieldGlobal);
				boolean Global = false;
				if (fieldGlobal.isChecked()) {
					 Global = true;
				} else {
					 Global = false;
				}
				Spinner fieldTyp = (Spinner) findViewById(R.id.fieldTyp);
				if (fieldTyp.getSelectedItem().toString().equals("TEXT")) {
					params[1] = "1";
				}
				if (fieldTyp.getSelectedItem().toString().equals("TEXT_AREA")) {
					params[1] = "2";
				}
				if (fieldTyp.getSelectedItem().toString().equals("CHECKBOX")) {
					params[1] = "3";
				}
				// id,model_id,field_id,data,label,hint,visible
				params[0] = modelID;
				
				params[2] = "";
				params[3] = fieldlabel.getText().toString().trim();
			
				params[4] = fieldHint.getText().toString().trim();

				

				if (!params[0].equals("")) {

					try {

						myDbHelper.createDataBase();
						Integer insertedID;

						if (editMode == false) {
							insertedID = myDbHelper.insertField(params, Global);
						} else {
							insertedID = myDbHelper.updateField(params, id);
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

	
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}





}