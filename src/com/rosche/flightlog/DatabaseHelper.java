package com.rosche.flightlog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String TABLE_NAME_VERSION = "version";
	public static final String PROGRAMM_VERSION = "9";
	//public Context context;
	//public String test = context.getFilesDir().getPath();
	private static String DB_PATH = "/data/data/com.rosche.flightlog/databases/";
	public static String DB_NAME = "flightlog20130527";
	public static String DB_RESTORE = "flightlogRESTORE";
	private SQLiteDatabase myDataBase;
	public static String separation = ",";
	private final Context myContext;
	public int actualVersion = 0;
	public ArrayList<String> upgrades = new ArrayList<String>();

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
		//String test = this.myContext .getFilesDir().getPath();
		upgrades.clear();
		upgrades.add("ALTER TABLE flightlog ADD COLUMN art text");
		upgrades.add("ALTER TABLE flightlog ADD COLUMN hersteller text");
		upgrades.add("CREATE TABLE documents (id INTEGER PRIMARY KEY, model_id NUMERIC, document BLOB, document_path TEXT)");
		upgrades.add("ALTER TABLE flights ADD COLUMN flights text");
		upgrades.add("CREATE TABLE field_description (id INTEGER PRIMARY KEY, type TEXT, options TEXT, default_value TEXT, default_label TEXT)");
		upgrades.add("CREATE TABLE customfields (id INTEGER PRIMARY KEY, model_id NUMERIC, field_id NUMERIC, data TEXT, label TEXT, hint TEXT, visible TEXT)");
		upgrades.add("INSERT INTO field_description (id,type,options,default_value,default_label) VALUES(1,'TEXT','','TEXT','TEXT')");
		upgrades.add("INSERT INTO field_description (id,type,options,default_value,default_label) VALUES(2,'TEXT_AREA','15','TEXT_AREA','TEXT_AREA')");
		upgrades.add("INSERT INTO field_description (id,type,options,default_value,default_label) VALUES(3,'CHECKBOX','1','CHECKBOX','CHECKBOX')");

	}

	public boolean upgradeDatabaseVersion(Context context) {
		for (int i = actualVersion; i < upgrades.size(); i++) {
			String sqlQuery = upgrades.get(i);
			try {
				SQLiteDatabase db = this.getWritableDatabase();
				db.execSQL(sqlQuery);
				db.close();
			} catch (SQLiteException e) {

				return false;
			}
		}
		return true;
	}

	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		if (dbExist) {

			// do nothing - database already exist
		} else {

			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}

	public boolean checkDatabaseVersion() {

		String sqlDataStore = "create table if not exists "
				+ TABLE_NAME_VERSION
				+ " (id integer primary key autoincrement,"
				+ " version text not null default '0')";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlDataStore);
			db.close();
		} catch (SQLiteException e) {

			return false;
		}

		String sqlQuery = "SELECT version from version WHERE 1 ORDER BY ID DESC LIMIT 1";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			String version = "0";
			if (c.moveToFirst()) {
				do {
					version = c.getString(c.getColumnIndex("version"));
				} while (c.moveToNext());
			}
			c.close();
			db.close();
			actualVersion = Integer.parseInt(version);
			if (!version.equals(PROGRAMM_VERSION)) {
				try {
					db = this.getWritableDatabase();
					String sqlQueryDelete = "DELETE from version WHERE 1";
					sqlQuery = "INSERT INTO version  (version) VALUES ('"
							+ PROGRAMM_VERSION + "')";
					db.execSQL(sqlQueryDelete);
					db.execSQL(sqlQuery);
					db.close();
					return true;
				} catch (SQLiteException e) {

					return false;
				}
			} else {
				return false;
			}
		} catch (SQLiteException e) {

			return false;
		}
	}

	public int getDatabaseVersion() {

		String sqlDataStore = "create table if not exists "
				+ TABLE_NAME_VERSION
				+ " (id integer primary key autoincrement,"
				+ " version text not null default '0')";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlDataStore);
			db.close();
		} catch (SQLiteException e) {

			return 0;
		}

		String sqlQuery = "SELECT version from version WHERE 1 ORDER BY ID DESC LIMIT 1";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			String version = "0";
			if (c.moveToFirst()) {
				do {
					version = c.getString(c.getColumnIndex("version"));
				} while (c.moveToNext());
			}
			c.close();
			db.close();
			actualVersion = Integer.parseInt(version);
			return actualVersion;
				} catch (SQLiteException e) {

			return 0;
		}
	}

	public String[] ReadNRFromDB(String filter, boolean select_acticve_only) {
		ArrayList<String> temp_array = new ArrayList<String>();
		String[] number_array = new String[0];
		String constraint = "";
		if (filter != "") {
			constraint = " WHERE name like '%" + filter + "%'";
		} else {
			constraint = " WHERE 1";
		}
		
		if (select_acticve_only) {
			constraint += " AND STATUS IN ('aktiv', 'aktice', 'aktuell')";
		}
		String sqlQuery = "SELECT id,name,typ FROM flightlog " + constraint
				+ " ORDER BY name";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					temp_array.add(c.getString(c.getColumnIndex("id")) + ": "
							+ c.getString(c.getColumnIndex("name")) + ", "
							+ c.getString(c.getColumnIndex("typ")));

				} while (c.moveToNext());
			}
			c.close();
			db.close();
		} catch (SQLiteException e) {
		}
		number_array = temp_array.toArray(number_array);
		return number_array;
	}
	public Integer updateField(String[] params, String id) {
		String sqlQuery = "UPDATE customfields SET ";
		sqlQuery += "label='" + params[3] + "',";
		sqlQuery += "hint='" + params[4] + "',";
		sqlQuery += "visible='" + params[5] + "',";
		sqlQuery += "field_id='" + params[1] + "'";
		sqlQuery += " WHERE id ='" + id + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {

		}
		return Integer.parseInt(id);
	}
	public Integer updateFlight(String[] params, String id) {
		String sqlQuery = "UPDATE flights SET ";
		sqlQuery += "description='" + params[1] + "',";
		sqlQuery += "date='" + params[2] + "',";
		sqlQuery += "flights='" + params[3] + "'";
		sqlQuery += " WHERE id ='" + id + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {

		}
		return Integer.parseInt(id);
	}
	public int numberFlights(String id) {
		int numberFlights = 0;
		String sqlQuery = "SELECT count(*) as count FROM flights where model_id='" + id + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					numberFlights = c.getInt(c.getColumnIndex("count"));

				} while (c.moveToNext());
			}
			c.close();
			db.close();

		} catch (SQLiteException e) {

		}
		return numberFlights;
	}
	
	
	public int countFlights(String id) {
		int countFlights = 0;
		String sqlQuery = "SELECT sum(flights) as count FROM flights where model_id='" + id + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					countFlights = c.getInt(c.getColumnIndex("count"));

				} while (c.moveToNext());
			}
			c.close();
			db.close();

		} catch (SQLiteException e) {

		}
		return countFlights;
	}
	
	public String[] ReadFieldFromDB(String id) {
		String[] field_array = new String[6];
		
		String sqlQuery = "SELECT a.id, a.model_id, a.field_id, a.data, a.label, a.hint, a.visible, b.type FROM customfields a, field_description b where a.id = '"
				+ id
				+ "' AND a.field_id = b.id ORDER by a.label DESC";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					field_array[0] = c.getString(c.getColumnIndex("id"));
					field_array[1] = c.getString(c.getColumnIndex("model_id"));
					field_array[2] = c.getString(c
							.getColumnIndex("label"));
					field_array[3] = c.getString(c.getColumnIndex("hint"));
					field_array[4] = c.getString(c.getColumnIndex("visible"));
					field_array[5] = c.getString(c.getColumnIndex("type"));
				} while (c.moveToNext());
			}
			c.close();
			db.close();

		} catch (SQLiteException e) {

		}
		return field_array;
	}
	
	public String[] ReadFlightFromDB(String id) {
		String[] flight_array = new String[5];
		String sqlQuery = "SELECT * FROM flights where id = '" + id + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					flight_array[0] = c.getString(c.getColumnIndex("id"));
					flight_array[1] = c.getString(c.getColumnIndex("model_id"));
					flight_array[2] = c.getString(c
							.getColumnIndex("description"));
					flight_array[3] = c.getString(c.getColumnIndex("date"));
					flight_array[4] = c.getString(c.getColumnIndex("flights"));
				} while (c.moveToNext());
			}
			c.close();
			db.close();

		} catch (SQLiteException e) {

		}
		return flight_array;
	}
	public void deleteFieldFromDB(Integer flightId) {
		String sqlQuery = "DELETE FROM customfields WHERE id='"
				+ String.valueOf(flightId) + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {

		}

	}
	public void deleteFlightFromDB(Integer flightId) {
		String sqlQuery = "DELETE FROM flights WHERE id='"
				+ String.valueOf(flightId) + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {

		}

	}
	public ArrayList<ArrayList<String>> getCustomFields() {
		ArrayList<ArrayList<String>> number_array = new ArrayList<ArrayList<String>>();
		String sqlQuery = "SELECT id,type,options,default_value,default_label FROM field_description ORDER by type DESC";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			number_array.clear();
			if (c.moveToFirst()) {
				do {
					ArrayList<String> flight_array = new ArrayList<String>();
					flight_array.add(c.getString(c.getColumnIndex("id")));
					flight_array.add(c.getString(c.getColumnIndex("type")));
					flight_array.add(c.getString(c
							.getColumnIndex("options")));
					flight_array.add(c.getString(c.getColumnIndex("default_value")));
					flight_array.add(c.getString(c.getColumnIndex("default_label")));
					number_array.add(flight_array);
				} while (c.moveToNext());
			}
			c.close();
			db.close();
		} catch (SQLiteException e) {

		}
		return number_array;
	}
	public ArrayList<ArrayList<String>> ReadFieldsFromDB(String Selecteditem, boolean visible) {
		String[] result_array;
		ArrayList<ArrayList<String>> number_array = new ArrayList<ArrayList<String>>();
		result_array = Selecteditem.split(":");
		String constraint = "";
		if (visible == true) {
			constraint = " AND a.visible='1' ";
		}
		// String[]flight_array = new String[4];
		String sqlQuery = "SELECT a.id, a.model_id, a.field_id, a.data, a.label, a.hint, a.visible, b.type FROM customfields a, field_description b where a.model_id = '"
				+ result_array[0]
				+ "' AND a.field_id = b.id "+constraint+" ORDER by a.label DESC";

		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			number_array.clear();
			if (c.moveToFirst()) {
				do {
					ArrayList<String> flight_array = new ArrayList<String>();
					flight_array.add(c.getString(c.getColumnIndex("id")));
					flight_array.add(c.getString(c.getColumnIndex("model_id")));
					flight_array.add(c.getString(c
							.getColumnIndex("data")));
					flight_array.add(c.getString(c.getColumnIndex("label")));
					flight_array.add(c.getString(c.getColumnIndex("hint")));
					flight_array.add(c.getString(c.getColumnIndex("type")));
					number_array.add(flight_array);
				} while (c.moveToNext());
			}
			c.close();
			db.close();
		} catch (SQLiteException e) {

		}
		return number_array;
	}
	public ArrayList<ArrayList<String>> ReadFlightsFromDB(String Selecteditem) {
		String[] result_array;
		ArrayList<ArrayList<String>> number_array = new ArrayList<ArrayList<String>>();
		result_array = Selecteditem.split(":");
		// String[]flight_array = new String[4];
		String sqlQuery = "SELECT id, model_id, description, date FROM flights where model_id = '"
				+ result_array[0]
				+ "' ORDER by CAST (SUBSTR(date,7,4)||SUBSTR(date,4,2)||SUBSTR(date,1,2) as signed) DESC,id DESC";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			number_array.clear();
			if (c.moveToFirst()) {
				do {
					ArrayList<String> flight_array = new ArrayList<String>();
					flight_array.add(c.getString(c.getColumnIndex("id")));
					flight_array.add(c.getString(c.getColumnIndex("model_id")));
					flight_array.add(c.getString(c
							.getColumnIndex("description")));
					flight_array.add(c.getString(c.getColumnIndex("date")));
					number_array.add(flight_array);
				} while (c.moveToNext());
			}
			c.close();
			db.close();
		} catch (SQLiteException e) {

		}
		return number_array;
	}

	public String[] ReadFromDB(String Selecteditem) {
		String[] result_array;
		result_array = Selecteditem.split(":");
		String[] member_array = new String[13];
		String sqlQuery = "SELECT * FROM flightlog where id = '"
				+ result_array[0] + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					member_array[0] = c.getString(c.getColumnIndex("name"));
					member_array[1] = c.getString(c.getColumnIndex("typ"));
					member_array[2] = c.getString(c
							.getColumnIndex("beschreibung"));
					member_array[3] = c.getString(c.getColumnIndex("datum"));
					member_array[4] = c.getString(c
							.getColumnIndex("spannweite"));
					member_array[5] = c.getString(c.getColumnIndex("länge"));
					member_array[6] = c.getString(c.getColumnIndex("gewicht"));
					member_array[7] = c.getString(c.getColumnIndex("rcdata"));
					member_array[8] = c.getString(c
							.getColumnIndex("ausstattung"));
					member_array[9] = c.getString(c.getColumnIndex("status"));
					member_array[10] = c.getString(c.getColumnIndex("id"));
					member_array[11] = c.getString(c.getColumnIndex("art"));
					member_array[12] = c.getString(c
							.getColumnIndex("hersteller"));
				} while (c.moveToNext());
			}
			c.close();
			db.close();
		} catch (SQLiteException e) {

		}
		return member_array;
	}

	public boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);

		} catch (SQLiteException e) {

		}

		if (checkDB != null) {

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	public boolean checkDataBase2() {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);
			// checkDB.execSQL("SELECT * FROM flightlog WHERE id=1");

		} catch (Exception e) {

			checkDB.close();
			return false;
		}
		checkDB.close();
		return true;
	}
	
	
	public void copyDataBaseEmergency() throws IOException {
		InputStream myInput = myContext.getAssets().open(DB_RESTORE);
		String outFileName = DB_PATH + DB_NAME;
		Log.e("FL","outFileName:"+outFileName);
		OutputStream myOutput = new FileOutputStream(outFileName);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	public void copyDataBase(String path) throws IOException {
		String outFileName = DB_PATH + DB_NAME;
		File f_path = new File(path);
		InputStream  myInput = null;
		myInput = new BufferedInputStream(new FileInputStream(f_path));
		OutputStream myOutput = new FileOutputStream(outFileName);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	private void copyDataBase() throws IOException {
		InputStream myInput = myContext.getAssets().open(DB_NAME);
		String outFileName = DB_PATH + DB_NAME;
		OutputStream myOutput = new FileOutputStream(outFileName);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	public String readLine(BufferedReader br) throws IOException {
		StringBuilder sb = new StringBuilder();
		int i;
		while (0 <= (i = br.read())) {
			if (i == '\r') {
				br.read();
				break;
			} else {
				sb.append((char) i);
			}
		}
		if (sb.length() == 0) {
			return null;
		} else {
			return sb.toString();
		}

	}

	public boolean importDataBaseComplete(Context context) throws IOException {
		try {
			File sdCard = Environment.getExternalStorageDirectory();
			String dir = sdCard.getAbsolutePath() + "/flight_log/import/";
			String inFilename = "model_data.csv";
			FileReader fr = new FileReader(dir + inFilename);
			BufferedReader br = new BufferedReader(fr);
			String data = "";
			String tableName = "flightlog";
			String columns = "id,name,typ,art,beschreibung,hersteller,datum,spannweite,länge,gewicht,rcdata,ausstattung,status";
			String InsertString1 = "INSERT INTO " + tableName + " (" + columns
					+ ") values (";
			String InsertString2 = ")";
			int i = 0;
			while ((data = readLine(br)) != null) {
				Integer nextval = getId("flightlog");
				String[] sarray = data.split(separation);
				String sqlQuery = "";
				if (i > 0) {

					if (sarray.length == 13 || sarray.length == 14) {
						StringBuilder sb = new StringBuilder(InsertString1);
						String nameModel = sarray[1].replaceAll("\"", "")
								.trim().toLowerCase(Locale.GERMANY);
						if (!nameModel.equals("name")) {
							sb.append("\"" + String.valueOf(nextval) + "\",");
							sb.append("\"" + sarray[1].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[2].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[3].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[4].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[5].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[6].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[7].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[8].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[9].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[10].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[11].replaceAll("\"", "")
									+ "\",");
							sb.append("\"" + sarray[12].replaceAll("\"", "")
									+ "\"");
							sb.append(InsertString2);
							sqlQuery = sb.toString();
							try {
								SQLiteDatabase db = this.getWritableDatabase();

								db.execSQL(sqlQuery);
								db.close();
							} catch (SQLiteException e) {

							}
							Toast.makeText(
									context,
									"Modell " + sarray[1].replaceAll("\"", "")
											+ " importiert.",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(
								context,
								"Error: Datensatzlänge: "
										+ String.valueOf(sarray.length),
								Toast.LENGTH_SHORT).show();
					}
				}
				i++;
			}
			br.close();
			fr.close();
			File file = new File(dir + inFilename);
			file.delete();
			return true;
		} catch (IOException e) {
			Toast.makeText(context,
					"ERROR:Import Model der Datenbank fehlgeschlagen!" + e,
					Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public boolean exportDataBaseComplete() throws IOException {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"ddMMyyyyHHmmss", Locale.GERMANY);
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/flight_log/export");
			dir.mkdir();
			String outFilename = dateFormat.format(new Date()) + "_"
					+ "model_data_complete.csv";

			File f = new File(dir, outFilename);
			f.createNewFile();
			OutputStream myOutput = new FileOutputStream(f);
			PrintStream printStream = new PrintStream(myOutput);
			String sqlQuery = "SELECT f.*,i.image_path FROM flightlog f join images i on i.model_id=f.id ORDER BY f.id ASC";
			String record = "";
			String header = "";
			header = '"' + "MODELL_ID" + '"' + separation;
			header += '"' + "NAME" + '"' + separation;
			header += '"' + "TYP" + '"' + separation;
			header += '"' + "ART" + '"' + separation;
			header += '"' + "BESCHREIBUNG" + '"' + separation;
			header += '"' + "HERSTELLER" + '"' + separation;
			header += '"' + "DATUM" + '"' + separation;
			header += '"' + "SPANNWEITE" + '"' + separation;
			header += '"' + "LAENGE" + '"' + separation;
			header += '"' + "GEWICHT" + '"' + separation;
			header += '"' + "RCEINSTELLUNG" + '"' + separation;
			header += '"' + "AUSSTATTUNG" + '"' + separation;
			header += '"' + "STATUS" + '"' + separation;
			header += '"' + "BILD_PFAD" + '"' + "\r\n";
			printStream.print(header);
			try {
				SQLiteDatabase db = this.getWritableDatabase();
				Cursor c = db.rawQuery(sqlQuery, null);
				if (c.moveToFirst()) {
					do {
						record = c.getString(c.getColumnIndex("id"));
						record += separation + '"'
								+ c.getString(c.getColumnIndex("name")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("typ")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("art")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("beschreibung"))
								+ '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("hersteller"))
								+ '"';
						record += separation
								+ c.getString(c.getColumnIndex("datum"));
						record += separation + '"'
								+ c.getString(c.getColumnIndex("spannweite"))
								+ '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("länge")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("gewicht"))
								+ '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("rcdata")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("ausstattung"))
								+ '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("status")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("image_path"))
								+ '"';
						record += '\r';
						record += '\n';
						printStream.print(record);
						Integer model_id = Integer.parseInt(c.getString(c
								.getColumnIndex("id")));
						ArrayList<String> flights = new ArrayList<String>();
						// *********************
						// flights
						flights = exportDataBaseFlightsById(model_id);
						int length = flights.size();
						if (length > 2) {
							String prefix = "\"\"" + separation + "\"\""
									+ separation;
							String postfix = separation + prefix + prefix
									+ prefix + "\r\n";
							for (int i = 0; i < length; i++) {
								String flight = "";
								if (i == 0 || i == 1) {
									flight = flights.get(i);
									flight = prefix + flight + postfix;

								} else {
									flight = flights.get(i);
									flight = prefix + String.valueOf(i)
											+ separation + flight + postfix;
								}
								printStream.print(flight);
							}
						}
						// ********************
					} while (c.moveToNext());
				}
				c.close();
				db.close();
			} catch (SQLiteException e) {

			}
			printStream.flush();
			printStream.close();
			myOutput.flush();
			myOutput.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean exportDataBase() throws IOException {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"ddMMyyyyHHmmss", Locale.GERMANY);
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/flight_log/export");
			dir.mkdir();

			String outFilename = dateFormat.format(new Date()) + "_"
					+ "model_data.csv";
			File f = new File(dir, outFilename);
			f.createNewFile();
			OutputStream myOutput = new FileOutputStream(f);
			PrintStream printStream = new PrintStream(myOutput);
			String sqlQuery = "SELECT f.*,i.image_path FROM flightlog f join images i on i.model_id=f.id";
			String record = "";
			String header = "";
			header = '"' + "MODELL_ID" + '"' + separation;
			header += '"' + "NAME" + '"' + separation;
			header += '"' + "TYP" + '"' + separation;
			header += '"' + "ART" + '"' + separation;
			header += '"' + "BESCHREIBUNG" + '"' + separation;
			header += '"' + "HERSTELLER" + '"' + separation;
			header += '"' + "DATUM" + '"' + separation;
			header += '"' + "SPANNWEITE" + '"' + separation;
			header += '"' + "LAENGE" + '"' + separation;
			header += '"' + "GEWICHT" + '"' + separation;
			header += '"' + "RCEINSTELLUNG" + '"' + separation;
			header += '"' + "AUSSTATTUNG" + '"' + separation;
			header += '"' + "STATUS" + '"' + separation;
			header += '"' + "BILD_PFAD" + '"' + "\r\n";
			printStream.print(header);
			try {
				SQLiteDatabase db = this.getWritableDatabase();
				Cursor c = db.rawQuery(sqlQuery, null);
				if (c.moveToFirst()) {
					do {
						record = c.getString(c.getColumnIndex("id"));
						record += separation + '"'
								+ c.getString(c.getColumnIndex("name")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("typ")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("art")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("beschreibung"))
								+ '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("hersteller"))
								+ '"';
						record += separation
								+ c.getString(c.getColumnIndex("datum"));
						record += separation + '"'
								+ c.getString(c.getColumnIndex("spannweite"))
								+ '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("länge")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("gewicht"))
								+ '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("rcdata")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("ausstattung"))
								+ '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("status")) + '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("image_path"))
								+ '"';
						record += "\r\n";
						printStream.print(record);
					} while (c.moveToNext());
				}
				c.close();
				db.close();
			} catch (SQLiteException e) {

			}
			printStream.flush();
			printStream.close();
			myOutput.flush();
			myOutput.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public ArrayList<String> exportDataBaseFlightsById(Integer model_id) {
		ArrayList<String> flights = new ArrayList<String>();
		String constraint = "";
		if (model_id > 0) {
			constraint = " AND f.model_id=" + String.valueOf(model_id)
					+ " ORDER BY f.id ASC";
		} else {
			constraint = " ORDER BY f.id ASC";
		}
		String sqlQuery = "SELECT f.model_id,m.name,f.date,f.description,f.flights FROM flights f join flightlog m on f.model_id=m.id "
				+ constraint;
		String record = "";
		String header = "";
		header = "\"Eintrag\"" + separation;
		header += "\"\"" + separation;
		header += "\"\"";
		flights.add(header);
		header = "\"Nr.\"" + separation;
		header += "\"Datum\"" + separation;
		header += "\"Flüge\"" + separation;
		header += "\"Bericht\"";
		flights.add(header);
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					record = c.getString(c.getColumnIndex("date"));
					record += separation + "\""+ c.getString(c.getColumnIndex("flights"))+ "\"";
					record += separation + "\""+ c.getString(c.getColumnIndex("description"))+ "\"";
					flights.add(record);
				} while (c.moveToNext());
			}
			c.close();
			db.close();
		} catch (SQLiteException e) {

		}
		return flights;
	}

	public boolean exportDataBaseFlights(Integer model_id) throws IOException {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"ddMMyyyyHHmmss", Locale.GERMANY);
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/flight_log/export");
			dir.mkdir();
			String outFilename = dateFormat.format(new Date()) + "_"
					+ "flight_data.csv";

			File f = new File(dir, outFilename);
			f.createNewFile();
			OutputStream myOutput = new FileOutputStream(f);
			PrintStream printStream = new PrintStream(myOutput);
			String constraint = "";
			if (model_id > 0) {
				constraint = " AND f.model_id=" + String.valueOf(model_id);
			} else {
				constraint = "";
			}
			String sqlQuery = "SELECT f.model_id,m.name,f.date,f.description,f.flights FROM flights f join flightlog m on f.model_id=m.id "
					+ constraint;
			String record = "";
			String header = "";
			header = '"' + "MODELL_ID" + '"' + separation;
			header += '"' + "NAME" + '"' + separation;
			header += '"' + "DATUM" + '"' + separation;
			header += '"' + "FLUEGE" + '"' + separation;
			header += '"' + "EINTRAG" + '"' + "\r\n";
			printStream.print(header);
			try {
				SQLiteDatabase db = this.getWritableDatabase();
				Cursor c = db.rawQuery(sqlQuery, null);
				if (c.moveToFirst()) {
					do {
						record = c.getString(c.getColumnIndex("model_id"));
						record += separation + '"'
								+ c.getString(c.getColumnIndex("name")) + '"';
						record += separation
								+ c.getString(c.getColumnIndex("date"));
						record += separation + "\""+ c.getString(c.getColumnIndex("flights"))+ "\"";
						record += separation + '"'
								+ c.getString(c.getColumnIndex("description"))
								+ '"';
						record += "\r\n";
						printStream.print(record);
					} while (c.moveToNext());
				}
				c.close();
				db.close();
			} catch (SQLiteException e) {

			}
			printStream.flush();
			printStream.close();
			myOutput.flush();
			myOutput.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean backupDataBase() throws IOException {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"ddMMyyyyHHmmss", Locale.GERMANY);
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/flight_log/backup");
			dir.mkdir();
			String inFileName = DB_PATH + DB_NAME;
			String outFilename = dateFormat.format(new Date()) + "_" + DB_NAME;
			File f = new File(dir, outFilename);
			f.createNewFile();
			InputStream myInput = new FileInputStream(inFileName);
			OutputStream myOutput = new FileOutputStream(f);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			myOutput.flush();
			myOutput.close();
			myInput.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	public Integer insertField(String[] params, boolean Global) {
		Integer nextval = getId("customfields");
		if (Global == true) {
			ArrayList<String> IDS = new ArrayList<String>();
			String sqlQuery = "SELECT id FROM flightlog where 1";
								try {
				SQLiteDatabase db = this.getWritableDatabase();
				Cursor c = db.rawQuery(sqlQuery, null);
				int i = 0;
				if (c.moveToFirst()) {
					do {
						IDS.add(c.getString(c.getColumnIndex("id")));
						i++;
					} while (c.moveToNext());
				}
				c.close();
				db.close();
			} catch (SQLiteException e) {

			}
			int length = IDS.size();
			for (int i = 0;i<length ; i++) {
				params[0] = IDS.get(i);
				nextval = getId("customfields");
				sqlQuery = "INSERT INTO customfields (id,model_id,field_id,data,label,hint,visible) VALUES ('"
						+ nextval + "'";
				for (int n = 0; n < params.length; n++) {
					if (params[n] != null) {
		
						sqlQuery += ",'" + params[n] + "'";
					} else {
						sqlQuery += ",''";
					}
				}
				
				sqlQuery += ")";
			try {
				SQLiteDatabase db = this.getWritableDatabase();
				db.execSQL(sqlQuery);
				db.close();
			} catch (SQLiteException e) {
			
			}
			}
	
		
		} else {
			String sqlQuery = "INSERT INTO customfields (id,model_id,field_id,data,label,hint,visible) VALUES ('"
					+ nextval + "'";
			for (int n = 0; n < params.length; n++) {
				if (params[n] != null) {
	
					sqlQuery += ",'" + params[n] + "'";
				} else {
					sqlQuery += ",''";
				}
			}
			
			sqlQuery += ")";
		
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {
		
		}
		
		}
		
		return nextval;
	}
	public Integer insertFlight(String[] params) {
		Integer nextval = getId("flights");
		String sqlQuery = "INSERT INTO flights (id,model_id,description,date,flights) VALUES ('"
				+ nextval + "'";
		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {

				sqlQuery += ",'" + params[i] + "'";
			} else {
				sqlQuery += ",''";
			}
		}
		sqlQuery += ")";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {

		}
		return nextval;
	}

	public Integer insert(String[] params) {
		Integer nextval = getId("flightlog");
		String sqlQuery = "INSERT INTO flightlog (id,name,typ,beschreibung,"
				+ "datum,spannweite,länge,gewicht,rcdata,ausstattung,status,art,hersteller) VALUES ('"
				+ nextval + "'";
		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {

				sqlQuery += ",'" + params[i] + "'";
			} else {
				sqlQuery += ",''";
			}
		}
		sqlQuery += ")";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {

		}
		return nextval;
	}
	public Integer addDocument(Integer id, String image_path) {
		Integer nextval = getId("documents");
		String sqlQuery = "INSERT INTO documents (id,model_id,document_path) VALUES ('"
				+ nextval
				+ "'"
				+ ",'"
				+ String.valueOf(id)
				+ "',"
				+ "'"
				+ image_path + "'" + ")";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
			return nextval;
		} catch (SQLiteException e) {

			return null;
		}
	}
	public Integer addImage(Integer id, String image_path) {
		Integer nextval = getId("images");
		String sqlQuery = "INSERT INTO images (id,model_id,image_path) VALUES ('"
				+ nextval
				+ "'"
				+ ",'"
				+ String.valueOf(id)
				+ "',"
				+ "'"
				+ image_path + "'" + ")";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
			return nextval;
		} catch (SQLiteException e) {

			return null;
		}
	}

	public Integer insertImage(Integer id, String image_path) {
		Integer nextval = getId("images");
		String sqlQueryDelete = "DELETE FROM images WHERE model_id ='" + id
				+ "'";
		String sqlQuery = "INSERT INTO images (id,model_id,image_path) VALUES ('"
				+ nextval
				+ "'"
				+ ",'"
				+ String.valueOf(id)
				+ "',"
				+ "'"
				+ image_path + "'" + ")";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQueryDelete);
			db.execSQL(sqlQuery);
			db.close();
			return nextval;
		} catch (SQLiteException e) {

			return null;
		}
	}
	public ArrayList<String> getDocuments(Integer model_id) {
		String sqlQuery = "SELECT document_path FROM documents WHERE model_id ='"
				+ model_id + "' ORDER BY id DESC";
		ArrayList<String> documentPaths = new ArrayList<String>();
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					documentPaths.add(c.getString(c.getColumnIndex("document_path")));
				} while (c.moveToNext());
			}
			c.close();
			db.close();

		} catch (SQLiteException e) {

		}
		return documentPaths;
	}
	public ArrayList<String> getImages(Integer model_id) {
		String sqlQuery = "SELECT image_path FROM images WHERE model_id ='"
				+ model_id + "' ORDER BY id DESC";
		ArrayList<String> imagePaths = new ArrayList<String>();
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					imagePaths.add(c.getString(c.getColumnIndex("image_path")));
				} while (c.moveToNext());
			}
			c.close();
			db.close();

		} catch (SQLiteException e) {

		}
		return imagePaths;
	}

	public String getImage(Integer model_id) {
		String sqlQuery = "SELECT image_path FROM images WHERE model_id ='"
				+ model_id + "' ORDER BY id DESC";
		String image_path = "";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					image_path = c.getString(c.getColumnIndex("image_path"));
					File sdCard = Environment.getExternalStorageDirectory();
					String f = sdCard.getAbsolutePath() + "/" + image_path;
					File file = new File(f);
					if (file.exists()) {
						return image_path;
					}
				} while (c.moveToNext());
			}
			c.close();
			db.close();

		} catch (SQLiteException e) {

		}
		return image_path;
	}

	public void setSeparation(String character) {
		separation = character;
	}

	public void deleteImage(String imagePath) {
		String sqlQueryDelete = "DELETE FROM images WHERE image_path='"
				+ imagePath + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQueryDelete);
			db.close();
		} catch (SQLiteException e) {

		}
	}
	public void deleteDocument(String imagePath, String model_id) {
		String sqlQueryDelete = "DELETE FROM documents WHERE document_path='"
				+ imagePath + "' and model_id='"+model_id+"'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQueryDelete);
			db.close();
		} catch (SQLiteException e) {

		}
	}
	public void resetImageDB() {
		String sqlQueryDelete = "DELETE FROM images WHERE 1";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQueryDelete);
			db.close();
		} catch (SQLiteException e) {

		}
	}

	public void resetDB() {
		String sqlQuery = "ALTER TABLE customfields ADD COLUMN visible text";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {

		}
	}

	public Integer updateImage(Integer model_id, String image_path) {
		Integer nextval = getId("images");
		String sqlQueryDelete = "DELETE FROM images WHERE model_id ='"
				+ model_id + "'";
		String sqlQuery = "INSERT INTO images (id,model_id,image_path) VALUES ('"
				+ nextval
				+ "'"
				+ ",'"
				+ String.valueOf(model_id)
				+ "',"
				+ "'"
				+ image_path + "'" + ")";
		String sqlSearchImage = "SELECT * FROM images WHERE model_id ='"
				+ model_id + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlSearchImage, null);
			File sdCard = Environment.getExternalStorageDirectory();
			if (c.moveToFirst()) {
				do {
					String oldImagePath = c.getString(c
							.getColumnIndex("image_path"));
					String f = sdCard.getAbsolutePath() + "/" + oldImagePath;
					File file = new File(f);
					if (file.exists()) {
						if (!oldImagePath.equals(image_path))
							file.delete();
					}
				} while (c.moveToNext());
			}
			c.close();
			db.execSQL(sqlQueryDelete);
			db.execSQL(sqlQuery);
			db.close();
			return model_id;
		} catch (SQLiteException e) {
			return null;
		}
	}

	public String delete(String id) {
		String[] result_array;
		result_array = id.split(":");
		id = result_array[0].trim();
		String sqlQuery = "DELETE FROM flightlog WHERE id='" + id + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {

		}
		return id;
	}

	public Integer update(String[] params, String id) {
		String sqlQuery = "UPDATE flightlog SET ";
		sqlQuery += "name='" + params[0] + "',";
		sqlQuery += "typ='" + params[1] + "',";
		sqlQuery += "beschreibung='" + params[2] + "',";
		sqlQuery += "datum='" + params[3] + "',";
		sqlQuery += "spannweite='" + params[4] + "',";
		sqlQuery += "länge='" + params[5] + "',";
		sqlQuery += "gewicht='" + params[6] + "',";
		sqlQuery += "rcdata='" + params[7] + "',";
		sqlQuery += "ausstattung='" + params[8] + "',";
		sqlQuery += "status='" + params[9] + "',";
		sqlQuery += "art='" + params[10] + "',";
		sqlQuery += "hersteller='" + params[11] + "'";
		sqlQuery += " WHERE id ='" + id + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {

		}
		return Integer.parseInt(id);
	}
	public Integer updateFieldData(String id, String data) {
		data = data.replace("'", "\'");
		data = data.replace(""+'"', "\"");
		String sqlQuery = "UPDATE customfields SET ";
		sqlQuery += "data='" + data + "'";
			sqlQuery += " WHERE id ='" + id + "'";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL(sqlQuery);
			db.close();
		} catch (SQLiteException e) {

		}
		return Integer.parseInt(id);
	}
	
	private int getId(String table) {
		String sqlQuery = "SELECT max(CAST(id AS INTEGER)) as nextval, count(*) as counts FROM "
				+ table;
		Integer id = 0;
		Integer counts = 0;
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					counts = Integer.parseInt(c.getString(c
							.getColumnIndex("counts")));
					if (counts > 0) {
						id = Integer.parseInt(c.getString(c
								.getColumnIndex("nextval")));
					} else {
						id = 0;
					}

				} while (c.moveToNext());
			}
			c.close();
			db.close();
		} catch (SQLiteException e) {

		}
		return id + 1;
	}

	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}