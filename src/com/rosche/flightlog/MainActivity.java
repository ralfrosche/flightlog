package com.rosche.flightlog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;

import java.util.List;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

import android.app.ActivityManager;
import android.app.SearchManager;
import android.app.SearchManager.OnCancelListener;
import android.app.SearchManager.OnDismissListener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import android.widget.Spinner;

import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity implements OnItemSelectedListener {
	private static final int REQUEST_PICK_DB = 666;
	private Spinner customListSpinner;
	String[] DataToDB;
	public static String separation = ",";
	public static boolean show_image_on_start = false;
	public int image_height = 200;
	public int image_width = 300;
	String[] result_array;
	public static SharedPreferences mPrefs;
	String Selecteditem;
	Integer SelectedPosition = 0;
	Integer SelectedPositionAtStartup = 0;
	public String filter = "";
	boolean searchInvolved = false;
	private float padding;
	private boolean darkTheme = false;
	public int dbVersion = 0;
	private MenuItem mMenuItemSearch;
	private boolean hide_empty_fields = false;
	DatabaseHelper myDbHelper = new DatabaseHelper(this);
	private boolean startup = true;
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
	public String labelNumberOfFlights = "Anz.Flüge:";
	public static int REQUEST_CODE_CROP_IMAGE = 123;
	public boolean hide_beschreibung = false;
	public boolean hide_type = false;
	public boolean hide_art = false;
	public boolean hide_datum = false;
	public boolean hide_status = false;
	public boolean hide_hersteller = false;
	public boolean hide_spannweite = false;
	public boolean hide_laenge = false;
	public boolean hide_gewicht = false;
	public boolean hide_rcdata = false;
	public boolean hide_ausstattung = false;
	public boolean hide_numberOfFlights = false;
	public boolean select_acticve_only = false;
	public static boolean db_version_checked = false;
	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern
			.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		readPrefs();
		if (darkTheme) {
			setTheme(R.style.AppBaseThemeDark);
		} else {
			setTheme(R.style.AppTheme);
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		/*
		try {
			myDbHelper.copyDataBaseEmergency();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		if (db_version_checked == false) {
			try {
				myDbHelper.createDataBase();

				boolean upgrade = myDbHelper.checkDatabaseVersion();
				if (upgrade == true) {

					myDbHelper.upgradeDatabaseVersion(getBaseContext());
					Toast.makeText(
							getBaseContext(),
							"Datenbank  auf Version: "
									+ DatabaseHelper.PROGRAMM_VERSION
									+ " gepatched", Toast.LENGTH_SHORT).show();

				} else {
					/*Toast.makeText(getBaseContext(),
							"Datenbank hat aktuelle Version",
							Toast.LENGTH_SHORT).show();
							*/

				}

				dbVersion = myDbHelper.getDatabaseVersion();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			db_version_checked = true;
		}
		// myDbHelper.resetDB();

		getActionBar().setDisplayShowHomeEnabled(false);
		Resources r = getResources();
		padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
				r.getDisplayMetrics());
		handleIntent(getIntent());
		ImageView imageViewList = null;
		if (show_image_on_start == true) {
			imageViewList = (ImageView) findViewById(R.id.imageViewList);
			imageViewList.setVisibility(View.VISIBLE);
		} else {
			imageViewList = (ImageView) findViewById(R.id.imageViewList);
			imageViewList.setVisibility(View.GONE);
		}

		initCustomListSpinner(filter);

	}

	private void doDelete() {

		try {

			myDbHelper.createDataBase();

			myDbHelper.delete(Selecteditem);

			myDbHelper.close();
			initCustomListSpinner(filter);

		} catch (IOException ioe) {

			throw new Error("Unable to open database");

		}

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
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMySearch(query);
		}

	}

	private void doMySearch(String query) {
		// TODO Auto-generated method stub
		filter = query;
		initCustomListSpinner(query);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		String FILENAME = "flight_log_sav";
		String string = String.valueOf(SelectedPosition);
		Log.e("flight_log",
				"String.valueOf(SelectedPosition):"
						+ String.valueOf(SelectedPosition));
		FileOutputStream fos = null;
		try {
			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			fos.write(string.getBytes());
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		myDbHelper.close();
	}

	@Override
	public void onStart() {
		super.onStart();

		String FILENAME = "flight_log_sav";
		byte[] bytes = new byte[] { 32, 32, 32, 32, 32, 32, 32 };
		;
		String str = "";
		FileInputStream fis = null;
		try {
			fis = openFileInput(FILENAME);

			try {
				fis.read(bytes, 0, 7);
				str = new String(bytes);
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!str.equals("") && Selecteditem == null) {
			try {
				SelectedPosition = Integer.parseInt(str.trim());
			} catch (NumberFormatException e) {
				SelectedPosition = 1;
			}
			SelectedPositionAtStartup = SelectedPosition;
			Selecteditem = "new";
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		if (Selecteditem != null) {
			if (!Selecteditem.equals("")) {
				Integer tmpPosition = SelectedPosition;
				initCustomListSpinner("");
				customListSpinner.setSelection(tmpPosition);
			}

		}

	}

	private void initCustomListSpinner(String filter) {

		customListSpinner = (Spinner) findViewById(R.id.custom_list_spinner);
		List<CharSequence> choices = new ArrayList<CharSequence>();

		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		try {

			myDbHelper.createDataBase();
			int dlength = 0;
			if (startup == false) {
				DataToDB = myDbHelper.ReadNRFromDB(filter, select_acticve_only);
				if (DataToDB.length == 0) {
					Toast.makeText(getBaseContext(), "Keine Daten gefunden",
							Toast.LENGTH_SHORT).show();

				}
				dlength = 99;
			}
			while (dlength == 0) {
				DataToDB = myDbHelper.ReadNRFromDB(filter, select_acticve_only);
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

		// DataToDB = myDbHelper.ReadFieldsFromDB(query.trim());
		// int length = DataToDB.size();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		mMenuItemSearch = menu.getItem(6);

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
				SelectedPosition = SelectedPositionAtStartup;
				customListSpinner.setSelection(SelectedPosition);

			} else {
				mMenuItemSearch.setTitle("Filter entfernen");
				onSearchRequested();
			}

			return true;
		case R.id.version:
			doVersion();
			return true;
		case R.id.mEdit:
			launchEdit(true);
			return true;
		case R.id.mNew:
			launchEdit(false);
			return true;
		case R.id.mDelete:
			doDelete();
			return true;
		case R.id.mFligtData:
			launchFlights();
			return true;
		case R.id.backup:
			doBackup();
			return true;
		case R.id.restore:
			doRestore();
			return true;
		case R.id.export:
			readPrefs();
			doExport();
			return true;
		case R.id.help:
			launchHelp();
			return true;
		case R.id.pics:
			readPrefs();
			doPics();
			return true;
		case R.id.options:
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			return true;
		case R.id.importMod:
			readPrefs();
			doImport();
			return true;
		case R.id.docs:
			doDocs();
			return true;
		case R.id.customFields:
			doCustomFields();
			return true;
		}

		return false;
	}

	private void launchHelp() {
		Intent intent = new Intent(this, help.class);
		startActivity(intent);
	}

	public void readPrefs() {
		String valueTmp = mPrefs.getString("separation", separation);

		if (!valueTmp.equals("")) {
			separation = valueTmp;
		}
		darkTheme = mPrefs.getBoolean("darkTheme", false);
		show_image_on_start = mPrefs.getBoolean("show_image_on_start", false);
		hide_empty_fields = mPrefs.getBoolean("hide_empty_fields", false);

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
		labelNumberOfFlights = mPrefs.getString("labelNumberOfFlights",
				labelNumberOfFlights);

		hide_beschreibung = mPrefs.getBoolean("hide_beschreibung",
				hide_beschreibung);
		hide_type = mPrefs.getBoolean("hide_type", hide_type);
		hide_art = mPrefs.getBoolean("hide_art", hide_art);
		hide_datum = mPrefs.getBoolean("hide_datum", hide_datum);
		hide_status = mPrefs.getBoolean("hide_status", hide_status);
		hide_hersteller = mPrefs.getBoolean("hide_hersteller", hide_hersteller);
		hide_spannweite = mPrefs.getBoolean("hide_spannweite", hide_spannweite);
		hide_laenge = mPrefs.getBoolean("hide_laenge", hide_laenge);
		hide_gewicht = mPrefs.getBoolean("hide_gewicht", hide_gewicht);
		hide_rcdata = mPrefs.getBoolean("hide_rcdata", hide_rcdata);
		hide_ausstattung = mPrefs.getBoolean("hide_ausstattung",
				hide_ausstattung);
		hide_numberOfFlights = mPrefs.getBoolean("hide_numberOfFlights",
				hide_numberOfFlights);
		select_acticve_only = mPrefs.getBoolean("select_acticve_only",
				select_acticve_only);

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

	private void doPics() {

		Intent intent = new Intent(this, ImageGallery.class);
		intent.putExtra("query", customListSpinner.getSelectedItem().toString());
		startActivity(intent);

	}

	private void doDocs() {
		Intent intent = new Intent(this, documents.class);
		intent.putExtra("query", customListSpinner.getSelectedItem().toString());
		startActivity(intent);
	}

	private void launchFlights() {
		Intent intent = new Intent(this, Flights.class);
		intent.putExtra("query", customListSpinner.getSelectedItem().toString());

		startActivity(intent);
	}

	private void doCustomFields() {
		Intent intent = new Intent(this, customFields.class);
		intent.putExtra("query", customListSpinner.getSelectedItem().toString());
		startActivity(intent);
	}

	private void doVersion() {

		String freeMemory = getMemoryInfo(this);

		Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
		Debug.getMemoryInfo(memoryInfo);
		String memMessage = String.format(
				"Pss=%.2f MB, Private=%.2f MB, Shared=%.2f MB",
				memoryInfo.getTotalPss() / 1024.0,
				memoryInfo.getTotalPrivateDirty() / 1024.0,
				memoryInfo.getTotalSharedDirty() / 1024.0);
		String androidVersion = Build.VERSION.CODENAME + "("
				+ Build.VERSION.RELEASE + ")";

		new AlertDialog.Builder(this)
				.setTitle("Versionshinweis")
				.setInverseBackgroundForced(false)
				.setMessage(

						"Flightlog V1.13\n" + "(c) 2013,2014 Ralf Rosche\n"
								+ "Database Version: " + DatabaseHelper.DB_NAME
								+ "V" + DatabaseHelper.PROGRAMM_VERSION
								+ "Patch" + String.valueOf(dbVersion)
								+ "\nKommerzielle Nutzung der Daten verboten!"
								+ "\nVerbesserungsvorschläge willkommen!"
								+ "\nmailto:r.rosche@gmx.net\n\n"
								+ "Memory Information:\n" + freeMemory
								+ "\nAndroid Version: " + androidVersion
								+ "\n\n" ).show();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		Selecteditem = customListSpinner.getSelectedItem().toString();
		SelectedPosition = customListSpinner.getSelectedItemPosition();
		RunDatabase();
	}

	public void doRestore() {
		boolean backupSuccess = false;
		try {
			myDbHelper.createDataBase();
			backupSuccess = myDbHelper.backupDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (backupSuccess) {
			Toast.makeText(getBaseContext(), "Backup db successful!",
					Toast.LENGTH_SHORT).show();
			doDbRestore();
		} else {
			Toast.makeText(getBaseContext(), "ERROR:Backup failed",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void doDbRestore() {

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		intent.putExtra("return-data", true);
		startActivityForResult(
				Intent.createChooser(intent, "Complete action using"),
				REQUEST_PICK_DB);

		return;
	}
	public String getPath2(Uri uri) {
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_PICK_DB) {
			if (resultCode == -1) {
				Log.e("Flightlog","resultCode:"+resultCode);
				Uri imageUri = data.getData();
				Log.e("Flightlog","imageUri:"+getPath(imageUri));
				String path = getPath(imageUri);
				try {
					myDbHelper.copyDataBase(path);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}
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
					"Export der kompletten Flight Tabelle erfolgreich!",
					Toast.LENGTH_SHORT).show();

		} else {
			Toast.makeText(
					getBaseContext(),
					"ERROR:Export der kompletten Flight Tabelle fehlgeschlagen!",
					Toast.LENGTH_SHORT).show();

		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	public static String getMemoryInfo(Context context) {
		StringBuffer memoryInfo = new StringBuffer();
		final ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(outInfo);
		memoryInfo.append("\nTotal Available Memory :")
				.append(outInfo.availMem >> 10).append("k");
		memoryInfo.append("\nTotal Available Memory :")
				.append(outInfo.availMem >> 20).append("M");
		memoryInfo.append("\nIn low memory situation:").append(
				outInfo.lowMemory);

		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/cat", "/proc/meminfo" };
			result = cmdexe.run(args, "/system/bin/");
		} catch (IOException ex) {
			Log.i("fetch_process_info", "ex=" + ex.toString());
		}

		return memoryInfo.toString() + "\n\n" + result;
	}

	public String getPath(Uri uri) {
		String selectedImagePath;
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				null);
		if (cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
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

	public void RunDatabase() {
		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		String image_path = "";
		ImageView imageViewList;
		String editID = "";

		try {

			myDbHelper.createDataBase();
			DataToDB = myDbHelper.ReadFromDB(Selecteditem.trim());

			// handle custom fields
			ArrayList<ArrayList<String>> fieldArray;
			fieldArray = myDbHelper.ReadFieldsFromDB(Selecteditem.trim(), true);
			int length = fieldArray.size();
			TableLayout table = (TableLayout) findViewById(R.id.tableLayoutMain);
			View lastRuler = findViewById(R.id.lastRuler);
			int index = ((ViewGroup) lastRuler.getParent())
					.indexOfChild(lastRuler);
			index = table.indexOfChild(lastRuler);
			int childCount = table.getChildCount();

			int test = 0;

			while (index < childCount) {
				View row = table.getChildAt(childCount);
				table.removeView(row);
				table.postInvalidate();
				childCount--;
				test++;
				if (test > 20)
					break;
			}

			TextView sname = (TextView) findViewById(R.id.name);
			final TextView styp = (TextView) findViewById(R.id.typ);
			final TextView numberFlights = (TextView) findViewById(R.id.numberFlights);
			final TextView sart = (TextView) findViewById(R.id.art);
			final TextView sdatum = (TextView) findViewById(R.id.datum);
			TextView sstatus = (TextView) findViewById(R.id.status);

			final TextView sbeschreibung = (TextView) findViewById(R.id.beschreibung);
			final TextView shersteller = (TextView) findViewById(R.id.hersteller);
			final TextView sspannweite = (TextView) findViewById(R.id.spannweite);
			final TextView slaenge = (TextView) findViewById(R.id.laenge);
			final TextView sgewicht = (TextView) findViewById(R.id.gewicht);
			final TextView src_data = (TextView) findViewById(R.id.rc_data);
			final TextView sausstattung = (TextView) findViewById(R.id.ausstattung);

			final TextView textViewlabelBeschreibung = (TextView) findViewById(R.id.labelBeschreibung);
			final TextView textViewlabelTyp = (TextView) findViewById(R.id.typLabel);
			final TextView textViewlabelArt = (TextView) findViewById(R.id.fmain);
			final TextView textViewlabelDatum = (TextView) findViewById(R.id.labelDatum);
			final TextView textViewlabelStatus = (TextView) findViewById(R.id.labelStatus);
			final TextView textViewlabelHersteller = (TextView) findViewById(R.id.labelHersteller);
			final TextView textViewlabelSpannweite = (TextView) findViewById(R.id.labelSpannweite);
			final TextView textViewlabelLaenge = (TextView) findViewById(R.id.labelLaenge);
			final TextView textViewlabelGewicht = (TextView) findViewById(R.id.labelGewicht);
			final TextView textViewlabelRCdata = (TextView) findViewById(R.id.labelRCdata);
			final TextView textViewlabelAusstattung = (TextView) findViewById(R.id.labelAusstattung);
			final TextView textViewlabelNumberOfFlights = (TextView) findViewById(R.id.labelNumberOfFlights);

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
			textViewlabelNumberOfFlights.setText(labelNumberOfFlights);

			numberFlights.setText(" "
					+ String.valueOf(myDbHelper.countFlights(DataToDB[10])));

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
			View ruler = null;
			if (hide_empty_fields) {
				if (sbeschreibung.getText().equals("") || hide_beschreibung) {
					final TableRow rowBeschreinung = (TableRow) findViewById(R.id.rowBeschreinung);
					rowBeschreinung.setVisibility(View.GONE);
					ruler = findViewById(R.id.rBeschreibung);
					ruler.setVisibility(View.GONE);

				}

				if (shersteller.getText().equals("") || hide_hersteller) {
					final TableRow rowHersteller = (TableRow) findViewById(R.id.rowHersteller);
					rowHersteller.setVisibility(View.GONE);
					ruler = findViewById(R.id.rHersteller);
					ruler.setVisibility(View.GONE);

				}
				if (sspannweite.getText().equals("") || hide_spannweite) {
					final TableRow rowSpannweite = (TableRow) findViewById(R.id.rowSpannweite);
					rowSpannweite.setVisibility(View.GONE);
					ruler = findViewById(R.id.rSpannweite);
					ruler.setVisibility(View.GONE);

				}
				if (slaenge.getText().equals("") || hide_laenge) {
					final TableRow rowLaenge = (TableRow) findViewById(R.id.rowLaenge);
					rowLaenge.setVisibility(View.GONE);
					ruler = findViewById(R.id.rLaenge);
					ruler.setVisibility(View.GONE);

				}
				if (sgewicht.getText().equals("") || hide_gewicht) {
					final TableRow rowGewicht = (TableRow) findViewById(R.id.rowGewicht);
					rowGewicht.setVisibility(View.GONE);
					ruler = findViewById(R.id.rGewicht);
					ruler.setVisibility(View.GONE);

				}
				if (sausstattung.getText().equals("") || hide_ausstattung) {
					final TableRow rowAusstattung = (TableRow) findViewById(R.id.rowAusstattung);
					rowAusstattung.setVisibility(View.GONE);
					ruler = findViewById(R.id.rAusstattung);
					ruler.setVisibility(View.GONE);

				}

				if (src_data.getText().equals("") || hide_rcdata) {
					final TableRow rowRCdata = (TableRow) findViewById(R.id.rowRCdata);
					rowRCdata.setVisibility(View.GONE);
					ruler = findViewById(R.id.rRCdata);
					ruler.setVisibility(View.GONE);

				}

				if (src_data.getText().equals("") || hide_rcdata) {
					final TableRow rowRCdata = (TableRow) findViewById(R.id.rowRCdata);
					rowRCdata.setVisibility(View.GONE);
					ruler = findViewById(R.id.rRCdata);
					ruler.setVisibility(View.GONE);

				}
			}

			if (hide_beschreibung) {
				final TableRow rowBeschreinung = (TableRow) findViewById(R.id.rowBeschreinung);
				rowBeschreinung.setVisibility(View.GONE);
				ruler = findViewById(R.id.rBeschreibung);
				ruler.setVisibility(View.GONE);

			}

			if (hide_hersteller) {
				final TableRow rowHersteller = (TableRow) findViewById(R.id.rowHersteller);
				rowHersteller.setVisibility(View.GONE);
				ruler = findViewById(R.id.rHersteller);
				ruler.setVisibility(View.GONE);

			}
			if (hide_spannweite) {
				final TableRow rowSpannweite = (TableRow) findViewById(R.id.rowSpannweite);
				rowSpannweite.setVisibility(View.GONE);
				ruler = findViewById(R.id.rSpannweite);
				ruler.setVisibility(View.GONE);

			}
			if (hide_laenge) {
				final TableRow rowLaenge = (TableRow) findViewById(R.id.rowLaenge);
				rowLaenge.setVisibility(View.GONE);
				ruler = findViewById(R.id.rLaenge);
				ruler.setVisibility(View.GONE);

			}
			if (hide_gewicht) {
				final TableRow rowGewicht = (TableRow) findViewById(R.id.rowGewicht);
				rowGewicht.setVisibility(View.GONE);
				ruler = findViewById(R.id.rGewicht);
				ruler.setVisibility(View.GONE);

			}
			if (hide_ausstattung) {
				final TableRow rowAusstattung = (TableRow) findViewById(R.id.rowAusstattung);
				rowAusstattung.setVisibility(View.GONE);
				ruler = findViewById(R.id.rAusstattung);
				ruler.setVisibility(View.GONE);

			}

			if (hide_rcdata) {
				final TableRow rowRCdata = (TableRow) findViewById(R.id.rowRCdata);
				rowRCdata.setVisibility(View.GONE);
				ruler = findViewById(R.id.rRCdata);
				ruler.setVisibility(View.GONE);

			}

			if (hide_type) {
				final TableRow rowType = (TableRow) findViewById(R.id.rowType);
				rowType.setVisibility(View.GONE);
				ruler = findViewById(R.id.rType);
				ruler.setVisibility(View.GONE);

			}

			if (hide_art) {
				final TableRow rowArt = (TableRow) findViewById(R.id.rowArt);
				rowArt.setVisibility(View.GONE);
				ruler = findViewById(R.id.rArt);
				ruler.setVisibility(View.GONE);

			}

			if (hide_datum) {
				final TableRow rowDatum = (TableRow) findViewById(R.id.rowDatum);
				rowDatum.setVisibility(View.GONE);
				ruler = findViewById(R.id.rDatum);
				ruler.setVisibility(View.GONE);

			}

			if (hide_status) {
				final TableRow rowStatus = (TableRow) findViewById(R.id.rowStatus);
				rowStatus.setVisibility(View.GONE);
				ruler = findViewById(R.id.rStatus);
				ruler.setVisibility(View.GONE);

			}
			if (hide_numberOfFlights) {
				final TableRow rowNumberOfFlights = (TableRow) findViewById(R.id.rowNumberOfFlights);
				rowNumberOfFlights.setVisibility(View.GONE);
				ruler = findViewById(R.id.rNumberOfFlights);
				ruler.setVisibility(View.GONE);

			}

			for (int i = 0; i < length; i++) {
				ArrayList<String> fieldrow = new ArrayList<String>();
				fieldrow = fieldArray.get(i);
				String label = fieldrow.get(3) + ":";
				String data = fieldrow.get(2);
				String type = fieldrow.get(5);
				Log.e("type", "type" + type);
				LayoutInflater inflater = (LayoutInflater) this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				TableRow rowViewNew = null;
				TextView customLabel = null;
				TextView customField = null;
				CheckBox CustomCheckBox = null;

				if (type.equals("TEXT")) {
					rowViewNew = (TableRow) inflater.inflate(R.layout.text_row,
							null);
					customLabel = (TextView) rowViewNew.getChildAt(0);
					customField = (TextView) rowViewNew.getChildAt(1);
					customField.setText(data);
				} else if (type.equals("TEXT_AREA")) {
					rowViewNew = (TableRow) inflater.inflate(
							R.layout.text_area_row, null);
					customLabel = (TextView) rowViewNew.getChildAt(0);
					ScrollView customScroller = (ScrollView) rowViewNew
							.getChildAt(1);
					customField = (TextView) customScroller.getChildAt(0);
					customField.setText(data);
				} else if (type.equals("CHECKBOX")) {
					rowViewNew = (TableRow) inflater.inflate(R.layout.checkbox_row,
							null);
					customLabel = (TextView) rowViewNew.getChildAt(0);
					CustomCheckBox = (CheckBox) rowViewNew.getChildAt(1);
					if (data.equals("1")) {
						CustomCheckBox.setChecked(true);
					} else {
						CustomCheckBox.setChecked(false);
					}
					CustomCheckBox.setEnabled(false);
				} else {
					rowViewNew = (TableRow) inflater.inflate(R.layout.text_row,
							null);
					customLabel = (TextView) rowViewNew.getChildAt(0);
					customField = (TextView) rowViewNew.getChildAt(1);
					customField.setText(data);
				}

				View v = new View(this);
				v.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.FILL_PARENT, 1));
				v.setBackgroundColor(Color.rgb(255, 0, 0));

				customLabel.setText(label);

				table.addView(rowViewNew);
				table.addView(v);
			}

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			int height = metrics.heightPixels;
			int width = metrics.widthPixels;
			image_width = width;
			image_height = height;
			image_path = myDbHelper.getImage(Integer.parseInt(editID));
			myDbHelper.close();
			imageViewList = (ImageView) findViewById(R.id.imageViewList);
			imageViewList.setScaleType(ImageView.ScaleType.FIT_CENTER);

			if (!image_path.equals("")) {
				Uri mUri;

				BitmapFactory.Options options = new BitmapFactory.Options();
				boolean sizeDown = true;
				if (sizeDown) {
					options.inSampleSize = 4;
				}

				File sdCard = Environment.getExternalStorageDirectory();
				String f = sdCard.getAbsolutePath() + "/" + image_path;
				File file = new File(f);
				mUri = Uri.parse(f);
				Bitmap bitmap = BitmapFactory
						.decodeFile(getPath(mUri), options);

				if (file.exists()) {

					// imageViewList.setImageURI(mUri);
					imageViewList.setImageBitmap(bitmap);
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
