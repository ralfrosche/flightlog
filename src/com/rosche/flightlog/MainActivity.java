package com.rosche.flightlog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.SearchManager;
import android.app.SearchManager.OnCancelListener;
import android.app.SearchManager.OnDismissListener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import android.widget.Spinner;

import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity implements OnItemSelectedListener {

	private Spinner customListSpinner;
	String[] DataToDB;
	public static String separation = ",";
	String[] result_array;
	public static SharedPreferences mPrefs;
	String Selecteditem;
	Integer SelectedPosition = 0;
	public String filter = "";
	boolean searchInvolved = false;
	private MenuItem mMenuItemSearch;

	DatabaseHelper myDbHelper = new DatabaseHelper(this);

	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern
			.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		try {
			myDbHelper.createDataBase();
			boolean upgrade = myDbHelper.checkDatabaseVersion();
			if (upgrade == true) {
				
				myDbHelper.upgradeDatabaseVersion(getBaseContext());
				Toast.makeText(getBaseContext(),
						"Datenbank erfolgreich auf Version: "+myDbHelper.PROGRAMM_VERSION+ " gepatched", Toast.LENGTH_SHORT)
						.show();
				
				
			} else {
				Toast.makeText(getBaseContext(),
						"Datenbank hat aktuelle Version", Toast.LENGTH_SHORT)
						.show();
				
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		
		

		handleIntent(getIntent());

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		readPrefs();

		Button editButton = (Button) findViewById(R.id.editbutton);
		Button newButton = (Button) findViewById(R.id.newbutton);
		Button delButton = (Button) findViewById(R.id.delbutton);

		Button fButton = (Button) findViewById(R.id.flbutton);

		editButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				launchEdit(true);
			}

		});

		fButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				launchFlights();
			}

		});

		newButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				launchEdit(false);
			}

		});

		delButton.setOnClickListener(new View.OnClickListener() {
			String id = "";

			@Override
			public void onClick(View arg0) {

				try {

					myDbHelper.createDataBase();

					id = myDbHelper.delete(Selecteditem);

					Log.e("MListe", "- deleted ID:" + id);

					myDbHelper.close();
					initCustomListSpinner(filter);

				} catch (IOException ioe) {

					throw new Error("Unable to open database");

				}

			}

		});
		initCustomListSpinner(filter);

	}

	@Override
	public boolean onSearchRequested() {

		final SearchManager searchManager = (SearchManager) this
				.getSystemService(Context.SEARCH_SERVICE);
		searchManager.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {

				if (filter == "")
					initCustomListSpinner("");

			}
		});

		searchManager.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {

				Log.e("MListe", "- Cancel-");

			}
		});

		return super.onSearchRequested();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		Log.e("MListe", "- HandleIntent-" + intent.getAction());
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMySearch(query);
		}

	}

	private void doMySearch(String query) {
		// TODO Auto-generated method stub
		filter = query;
		initCustomListSpinner(query);

		Log.e("MListe", "- Query:-" + query);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		myDbHelper.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.e("MListe", "- onresume-");
		if (Selecteditem != null) {
			if (!Selecteditem.equals("")) {
				Integer tmpPosition = SelectedPosition;
				initCustomListSpinner("");
				customListSpinner.setSelection(tmpPosition);
			}

		}

		// initCustomListSpinner(filter);

	}

	private void initCustomListSpinner(String filter) {

		customListSpinner = (Spinner) findViewById(R.id.custom_list_spinner);
		List<CharSequence> choices = new ArrayList<CharSequence>();

		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		try {

			myDbHelper.createDataBase();
			int dlength = 0;
			while (dlength == 0) {
				DataToDB = myDbHelper.ReadNRFromDB(filter);
				dlength = DataToDB.length;
			}

			for (int i = 0; i < DataToDB.length; i++) {
				choices.add(DataToDB[i]);
			}

			myDbHelper.close();

		} catch (IOException ioe) {

			throw new Error("Unable to create database");

		}

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item, choices);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		customListSpinner.setAdapter(adapter);
		customListSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		mMenuItemSearch = menu.getItem(0);

		Log.e("MListe", "- OPtions menu created -");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String searchS;
		switch (item.getItemId()) {

		case R.id.search:
			filter = "";

			searchS = (String) mMenuItemSearch.getTitle();
			if (searchS.equals("Filter entfernen")) {
				mMenuItemSearch.setTitle("Suche / Filter");
				initCustomListSpinner(filter);

			} else {
				mMenuItemSearch.setTitle("Filter entfernen");
				onSearchRequested();
			}

			return true;
		case R.id.version:
			doVersion();
			return true;
		case R.id.backup:
			doBackup();
			return true;
		case R.id.export:
			readPrefs();
			doExport();
			return true;
		case R.id.options:
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			return true;
		case R.id.importMod:
			readPrefs();
			doImport();
			return true;
		}

		return false;
	}

	public void readPrefs() {
		String valueTmp = mPrefs.getString("separation", separation);

		Log.e("MListe", "SeparationOld:" + separation);
		if (!valueTmp.equals("")) {
			separation = valueTmp;
		}

		Log.e("MListe", "SeparationNew:" + valueTmp);

	}

	private void launchEdit(boolean edit) {
		Intent intent = new Intent(this, EditActivity.class);
		if (edit == true) {
			intent.putExtra("query", customListSpinner.getSelectedItem()
					.toString());
		} else {
			intent.putExtra("query", "");
		}
		startActivity(intent);
	}

	private void launchFlights() {
		Intent intent = new Intent(this, Flights.class);
		intent.putExtra("query", customListSpinner.getSelectedItem().toString());

		startActivity(intent);
	}

	private void doVersion() {

		new AlertDialog.Builder(this)
				.setTitle("Versionshinweis")
				.setInverseBackgroundForced(false)
				.setMessage(

						"Flightlog V1.04\n" + "(c) 2013 Ralf Rosche\n"
								+ "Database Version: " + DatabaseHelper.DB_NAME
								+ "\nKommerzielle Nutzung der Daten verboten!"
								+ "\nVerbesserungsvorschläge willkommen!"
								+ "\nmailto:aeroclub@gmx.net")
				.show();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		Selecteditem = customListSpinner.getSelectedItem().toString();
		SelectedPosition = customListSpinner.getSelectedItemPosition();
		RunDatabase();
	}

	public void doBackup() {
		boolean backupSuccess = false;
		try {
			myDbHelper.createDataBase();
			backupSuccess = myDbHelper.backupDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (backupSuccess) {
			Toast.makeText(getBaseContext(),
					"Backup der Datenbank erfolgreich!", Toast.LENGTH_SHORT)
					.show();

		} else {
			Toast.makeText(getBaseContext(),
					"ERROR:Backup der Datenbank fehlgeschlagen!",
					Toast.LENGTH_SHORT).show();

		}

	}

	public void doImport() {

		boolean Success = false;
		try {
			myDbHelper.createDataBase();
			myDbHelper.setSeparation(separation);
			Success = myDbHelper.importDataBaseComplete(getBaseContext());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Success) {
			Toast.makeText(getBaseContext(),
					"Import der Model Datenbank erfolgreich!",
					Toast.LENGTH_SHORT).show();
			initCustomListSpinner(filter);

		} else {
			Toast.makeText(getBaseContext(),
					"ERROR:Import Model der Datenbank fehlgeschlagen!",
					Toast.LENGTH_SHORT).show();

		}
	}

	public void doExport() {

		boolean backupSuccess = false;
		try {
			myDbHelper.createDataBase();
			myDbHelper.setSeparation(separation);
			backupSuccess = myDbHelper.exportDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (backupSuccess) {
			Toast.makeText(getBaseContext(),
					"Export der Model Datenbank erfolgreich!",
					Toast.LENGTH_SHORT).show();

		} else {
			Toast.makeText(getBaseContext(),
					"ERROR:Export Model der Datenbank fehlgeschlagen!",
					Toast.LENGTH_SHORT).show();

		}

		try {
			myDbHelper.createDataBase();
			myDbHelper.setSeparation(separation);
			backupSuccess = myDbHelper.exportDataBaseFlights(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (backupSuccess) {
			Toast.makeText(getBaseContext(),
					"Export der Flight Datenbank erfolgreich!",
					Toast.LENGTH_SHORT).show();

		} else {
			Toast.makeText(getBaseContext(),
					"ERROR:Export Flight der Datenbank fehlgeschlagen!",
					Toast.LENGTH_SHORT).show();

		}

		try {
			myDbHelper.createDataBase();
			myDbHelper.setSeparation(separation);
			backupSuccess = myDbHelper.exportDataBaseComplete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (backupSuccess) {
			Toast.makeText(getBaseContext(),
					"Export der kompoletten Flight Tabelle erfolgreich!",
					Toast.LENGTH_SHORT).show();

		} else {
			Toast.makeText(
					getBaseContext(),
					"ERROR:Export der kompoletten Flight Tabelle fehlgeschlagen!",
					Toast.LENGTH_SHORT).show();

		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	public void RunDatabase() {
		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		String image_path = "";
		ImageView imageViewList;
		String editID = "";

		try {

			myDbHelper.createDataBase();
			DataToDB = myDbHelper.ReadFromDB(Selecteditem.trim());
			TextView sname = (TextView) findViewById(R.id.name);
			final TextView styp = (TextView) findViewById(R.id.typ);
			final TextView sart = (TextView) findViewById(R.id.art);
			final TextView sbeschreibung = (TextView) findViewById(R.id.beschreibung);
			final TextView shersteller = (TextView) findViewById(R.id.hersteller);
			final TextView sdatum = (TextView) findViewById(R.id.datum);
			final TextView sspannweite = (TextView) findViewById(R.id.spannweite);
			TextView slaenge = (TextView) findViewById(R.id.laenge);
			TextView sgewicht = (TextView) findViewById(R.id.gewicht);
			TextView src_data = (TextView) findViewById(R.id.rc_data);
			TextView sausstattung = (TextView) findViewById(R.id.ausstattung);
			TextView sstatus = (TextView) findViewById(R.id.status);

			sname.setText(DataToDB[0]);
			styp.setText(DataToDB[1]);
			sbeschreibung.setText(DataToDB[2]);
			sdatum.setText(DataToDB[3]);
			sspannweite.setText(DataToDB[4]);
			slaenge.setText(DataToDB[5]);
			sgewicht.setText(DataToDB[6]);
			src_data.setText(DataToDB[7]);
			sausstattung.setText(DataToDB[8]);
			sstatus.setText(DataToDB[9]);
			editID = DataToDB[10];
			shersteller.setText(DataToDB[12]);
			sart.setText(DataToDB[11]);

			image_path = myDbHelper.getImage(Integer.parseInt(editID));
			myDbHelper.close();
			imageViewList = (ImageView) findViewById(R.id.imageViewList);
			Log.e("MListe", "- image:" + image_path);
			if (!image_path.equals("")) {
				Uri mUri;

				File sdCard = Environment.getExternalStorageDirectory();
				String f = sdCard.getAbsolutePath() + "/" + image_path;
				File file = new File(f);
				mUri = Uri.parse(f);
				if (file.exists()) {
					Log.e("MListe", "- updated model ID:" + f);
					imageViewList.setImageURI(mUri);
				} else {
					imageViewList.setImageResource(R.drawable.no_photo);
				}
			} else {
				imageViewList.setImageResource(R.drawable.no_photo);
			}

		} catch (IOException ioe) {

			throw new Error("Unable to create database");

		}

		try {

		} catch (SQLException sqle) {

			throw sqle;

		}
	}

}
