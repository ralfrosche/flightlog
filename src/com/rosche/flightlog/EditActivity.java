package com.rosche.flightlog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
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
	public String labelBeschreibung = "Beschr.:";
	public String labelTyp = "Typ:";
	public String labelArt = "Art:";
	public String labelDatum = "Datum:";
	public String labelStatus = "Status:";
	public String labelHersteller = "Herst.:";
	public String labelSpannweite = "Spannw.:";
	public String labelLaenge = "Länge:";
	public String labelGewicht = "Gewicht:";
	public String labelRCdata = "RC Daten:";
	public String labelAusstattung = "Ausst.:";
	String query = "";


	@SuppressWarnings("rawtypes")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		  getActionBar().setDisplayShowHomeEnabled(false);
		 query = getIntent().getStringExtra("query");
		 getWindow().setSoftInputMode(
				    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
				
				
				final TextView textViewlabelBeschreibung=                       (TextView) findViewById(R.id.editLabelBeschreibung);
				final TextView textViewlabelTyp=                                (TextView) findViewById(R.id.edittypLabel);
				final TextView textViewlabelArt=                                (TextView) findViewById(R.id.editlabelfmain);
				final TextView textViewlabelDatum=                              (TextView) findViewById(R.id.editlabelDatum);
				final TextView textViewlabelStatus=                             (TextView) findViewById(R.id.editlabelStatus);
				final TextView textViewlabelHersteller=                         (TextView) findViewById(R.id.editlabelHersteller);
				final TextView textViewlabelSpannweite=		                (TextView) findViewById(R.id.editlabelSpannweite);
				final TextView textViewlabelLaenge=                             (TextView) findViewById(R.id.editlabellaenge);
				final TextView textViewlabelGewicht=                            (TextView) findViewById(R.id.editlabelGewicht);
				final TextView textViewlabelRCdata=                             (TextView) findViewById(R.id.editlabelRCdata);
				final TextView textViewlabelAusstattung=                        (TextView) findViewById(R.id.editlabelAusstattung);
				
				
				textViewlabelBeschreibung.setText(labelBeschreibung);                
				textViewlabelTyp.setText(labelTyp);                         
				textViewlabelArt.setText(labelArt);                         
				textViewlabelDatum.setText(labelDatum);                       
				textViewlabelStatus.setText(labelStatus);                      
				textViewlabelHersteller.setText(labelHersteller);                  
				textViewlabelSpannweite.setText(labelSpannweite);		  
				textViewlabelLaenge.setText(labelLaenge);                      
				textViewlabelGewicht.setText(labelGewicht);                     
				textViewlabelRCdata.setText(labelRCdata);                      
				textViewlabelAusstattung.setText(labelAusstattung);                 
				
				
				
				

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
				
				
				// handle custom fields
				ArrayList<ArrayList<String>> fieldArray;
				fieldArray = myDbHelper.ReadFieldsFromDB(query.trim(), false);
				int length = fieldArray.size();
				// Log.e("FL","length"+length);
				TableLayout table = (TableLayout) findViewById(R.id.tableLayoutEdit);
				for (int i = 0; i < length; i++) {
					ArrayList<String>  fieldrow = new ArrayList<String>();
					fieldrow = fieldArray.get(i);
					String label = fieldrow.get(3)+ ":";
					String hint = fieldrow.get(4);
					String type = fieldrow.get(5);
					String data = fieldrow.get(2);
					
					LayoutInflater inflaterRow = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					TableRow rowViewNew = null;
					TextView customLabel = null;
					EditText customField = null;
					CheckBox CustomCheckBox = null;
							
					if (type.equals("TEXT")) {
						rowViewNew = (TableRow)inflaterRow.inflate(R.layout.text_row_edit, null);
						 customLabel = (TextView) rowViewNew.getChildAt(0);
						 customField = (EditText) rowViewNew.getChildAt(1);
						 customField.setHint(hint);
						customField.setText(data);
					} else if(type.equals("TEXT_AREA")) {
						rowViewNew = (TableRow)inflaterRow.inflate(R.layout.text_area_row_edit, null);
						 customLabel = (TextView) rowViewNew.getChildAt(0);
						 customField = (EditText) rowViewNew.getChildAt(1);
						 customField.setHint(hint);
						 customField.setText(data);
					}  else if(type.equals("CHECKBOX")) {
						rowViewNew = (TableRow)inflaterRow.inflate(R.layout.checkbox_row, null);
						 customLabel = (TextView) rowViewNew.getChildAt(0);
						 CustomCheckBox = (CheckBox) rowViewNew.getChildAt(1);
						 CustomCheckBox.setHint(hint);
						 if (data.equals("1")) {
							 CustomCheckBox.setChecked(true);
						 } else {
							 CustomCheckBox.setChecked(false);
						 }
						
					} else {
						rowViewNew = (TableRow)inflaterRow.inflate(R.layout.text_row_edit, null);
						 customLabel = (TextView) rowViewNew.getChildAt(0);
						 customField = (EditText) rowViewNew.getChildAt(1);
						 customField.setHint(hint);
						customField.setText(data);
					}
					
					customLabel.setText(label);
					rowViewNew.setTag(fieldrow.get(0));
					View v = new View(this);
					v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
					v.setBackgroundColor(Color.rgb(255, 0, 0));
					table.addView(rowViewNew);
					table.addView(v);
				}
				myDbHelper.close();

			} catch (IOException ioe) {

				throw new Error("Unable to open database");

			}

		} else {
			
			
			final TextView textViewlabelBeschreibung=                       (TextView) findViewById(R.id.editLabelBeschreibung);
			final TextView textViewlabelTyp=                                (TextView) findViewById(R.id.edittypLabel);
			final TextView textViewlabelArt=                                (TextView) findViewById(R.id.editlabelfmain);
			final TextView textViewlabelDatum=                              (TextView) findViewById(R.id.editlabelDatum);
			final TextView textViewlabelStatus=                             (TextView) findViewById(R.id.editlabelStatus);
			final TextView textViewlabelHersteller=                         (TextView) findViewById(R.id.editlabelHersteller);
			final TextView textViewlabelSpannweite=		                (TextView) findViewById(R.id.editlabelSpannweite);
			final TextView textViewlabelLaenge=                             (TextView) findViewById(R.id.editlabellaenge);
			final TextView textViewlabelGewicht=                            (TextView) findViewById(R.id.editlabelGewicht);
			final TextView textViewlabelRCdata=                             (TextView) findViewById(R.id.editlabelRCdata);
			final TextView textViewlabelAusstattung=                        (TextView) findViewById(R.id.editlabelAusstattung);
			
			
			textViewlabelBeschreibung.setText(labelBeschreibung);                
			textViewlabelTyp.setText(labelTyp);                         
			textViewlabelArt.setText(labelArt);                         
			textViewlabelDatum.setText(labelDatum);                       
			textViewlabelStatus.setText(labelStatus);                      
			textViewlabelHersteller.setText(labelHersteller);                  
			textViewlabelSpannweite.setText(labelSpannweite);		  
			textViewlabelLaenge.setText(labelLaenge);                      
			textViewlabelGewicht.setText(labelGewicht);                     
			textViewlabelRCdata.setText(labelRCdata);                      
			textViewlabelAusstattung.setText(labelAusstattung);                 
			
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
							updateCustomFields();
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
	private void updateCustomFields(){
		ArrayList<ArrayList<String>> fieldArray;
		fieldArray = myDbHelper.ReadFieldsFromDB(query.trim(), false);
		TableLayout table = (TableLayout) findViewById(R.id.tableLayoutEdit);
		
		final int childCount = table.getChildCount();
		for (int i = 0; i < childCount; i++) {
	        final View child = table.getChildAt(i);
	        if (child instanceof TableRow) {
	        	TableRow rowViewNew = (TableRow) table.getChildAt(i);
	        	String tag = (String) child.getTag();
	        	if (tag != null && (Integer.parseInt(tag) != 0)) {
	        		Log.e("FL","tag:"+tag);
	        		EditText customField = null;
	        		CheckBox customCheckBox = null;
	        		String data = "";
	        		final View chieldField = rowViewNew.getChildAt(1);
	        		  if (chieldField instanceof EditText) {
	        			 customField = (EditText) rowViewNew.getChildAt(1);
	  					 data = customField.getText().toString().trim();
	        			  
	        		  }
	        		  if (chieldField instanceof CheckBox) {
	        			  customCheckBox = (CheckBox) rowViewNew.getChildAt(1);
		        			  if (customCheckBox.isChecked()) {
		        				  data = "1";
		        			  } else {
		        				  data = "0";
		        			  }
     			  
		        		  }
					
					Log.e("FL","data:"+data);
	        		myDbHelper.updateFieldData(tag, data);
	        		
	        	}
	        		            
	        } 
	      }
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
		
		labelBeschreibung = mPrefs.getString("labelBeschreibung",
				labelBeschreibung);
		labelTyp = mPrefs.getString("labelTyp", labelTyp);
		labelArt = mPrefs.getString("labelArt", labelArt);
		labelDatum = mPrefs.getString("labelDatum", labelDatum);
		labelStatus = mPrefs.getString("labelStatus", labelStatus);
		labelHersteller = mPrefs.getString("labelHersteller", labelHersteller);
		labelSpannweite = mPrefs.getString("labelSpannweite", labelSpannweite);
		labelLaenge = mPrefs.getString("labelLaenge", labelLaenge);
		labelGewicht = mPrefs.getString("labelGewicht", labelGewicht);
		labelRCdata = mPrefs.getString("labelRCdata", labelRCdata);
		labelAusstattung = mPrefs.getString("labelAusstattung",
				labelAusstattung);
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
