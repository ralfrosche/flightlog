package com.rosche.flightlog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.microedition.khronos.opengles.GL;

import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static String DB_PATH = "/data/data/com.rosche.flightlog/databases/";

	public static String DB_NAME = "flightlog20130527";

	private SQLiteDatabase myDataBase;

	public static String separation = ",";

	private final Context myContext;

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DatabaseHelper(Context context) {

		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
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

	public String[] ReadNRFromDB(String filter) {

		ArrayList<String> temp_array = new ArrayList<String>();
		String[] number_array = new String[0];

		String constraint = "";
		if (filter != "") {
			constraint = " WHERE name like '%" + filter + "%'";

		} else {
			constraint = " WHERE 1";

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

			Log.e("MLISTE", "+++ readData +++" + e);

		}

		number_array = temp_array.toArray(number_array);

		return number_array;

	}

	public Integer updateFlight(String[] params, String id) {

		String sqlQuery = "UPDATE flights SET ";

		sqlQuery += "description='" + params[1] + "',";
		sqlQuery += "date='" + params[2] + "'";
		sqlQuery += " WHERE id ='" + id + "'";
		Log.e("MLISTE", "+++ updateFlightdata +++" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			db.execSQL(sqlQuery);

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ updateFlightdata error +++" + e);

		}

		return Integer.parseInt(id);
	}

	public String[] ReadFlightFromDB(String id) {

		String[] flight_array = new String[4];
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

				} while (c.moveToNext());
			}

			c.close();

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ readDataflight +++" + e);

		}
		return flight_array;
	}

	public void deleteFlightFromDB(Integer flightId) {

		String sqlQuery = "DELETE FROM flights WHERE id='"
				+ String.valueOf(flightId) + "'";

		Log.e("MLISTE", "+++ deleteflight +++" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			db.execSQL(sqlQuery);

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ deleteflight +++" + e);

		}

	}

	public ArrayList<ArrayList<String>> ReadFlightsFromDB(String Selecteditem) {
		String[] result_array;
		ArrayList<ArrayList<String>> number_array = new ArrayList<ArrayList<String>>();

		result_array = Selecteditem.split(":");
		// String[]flight_array = new String[4];
		String sqlQuery = "SELECT id, model_id, description, date FROM flights where model_id = '"
				+ result_array[0] + "' ORDER BY id DESC";

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
			Log.e("MLISTE", "+++ readFlightData +++" + e);

		}

		return number_array;
	}

	public String[] ReadFromDB(String Selecteditem) {
		String[] result_array;
		result_array = Selecteditem.split(":");
		String[] member_array = new String[11];
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
					member_array[5] = c.getString(c.getColumnIndex("l�nge"));
					member_array[6] = c.getString(c.getColumnIndex("gewicht"));
					member_array[7] = c.getString(c.getColumnIndex("rcdata"));
					member_array[8] = c.getString(c
							.getColumnIndex("ausstattung"));
					member_array[9] = c.getString(c.getColumnIndex("status"));
					member_array[10] = c.getString(c.getColumnIndex("id"));
				} while (c.moveToNext());
			}

			c.close();

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ readData +++" + e);

		}
		return member_array;
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	public boolean checkDataBase() {
		Log.e("MLISTE", "+++ checkDataBase +++");
		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);

		} catch (SQLiteException e) {

		}

		if (checkDB != null) {

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	private void copyDataBase() throws IOException {
		Log.e("MLISTE", "+++ copyDataBase +++");

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

	public boolean importDataBaseComplete(Context context) throws IOException {

		try {

			File sdCard = Environment.getExternalStorageDirectory();
			String dir = sdCard.getAbsolutePath() + "/flight_log/import/";
			String inFilename = "model_data.csv";
			
			// testing read from asset
			//InputStream myInput = myContext.getAssets().open(inFilename);
			//InputStreamReader r = new InputStreamReader(myInput);
			//BufferedReader br = new BufferedReader(r);
			// testing
			
			FileReader fr = new FileReader(dir+inFilename);
			BufferedReader br = new BufferedReader(fr);

			String data = "";
			String tableName = "flightlog";
			String columns = "id,name,typ,beschreibung,datum,spannweite,l�nge,gewicht,rcdata,ausstattung,status";
			String InsertString1 = "INSERT INTO " + tableName + " (" + columns
					+ ") values (";
			String InsertString2 = ")";

			while ((data = br.readLine()) != null) {

				Integer nextval = getId("flightlog");

				String[] sarray = data.split(",");
				String sqlQuery = "";
				if (sarray.length == 12) {
					StringBuilder sb = new StringBuilder(InsertString1);
					String nameModel = sarray[1].replaceAll("\"", "").trim()
							.toLowerCase(Locale.GERMANY);
					if (!nameModel.equals("name")) {
						sb.append("\"" + String.valueOf(nextval) + "\",");
						sb.append("\"" + sarray[1].replaceAll("\"", "") + "\",");
						sb.append("\"" + sarray[2].replaceAll("\"", "") + "\",");
						sb.append("\"" + sarray[3].replaceAll("\"", "") + "\",");
						sb.append("\"" + sarray[4].replaceAll("\"", "") + "\",");
						sb.append("\"" + sarray[5].replaceAll("\"", "") + "\",");
						sb.append("\"" + sarray[6].replaceAll("\"", "") + "\",");
						sb.append("\"" + sarray[7].replaceAll("\"", "") + "\",");
						sb.append("\"" + sarray[8].replaceAll("\"", "") + "\",");
						sb.append("\"" + sarray[9].replaceAll("\"", "") + "\",");
						sb.append("\"" + sarray[10].replaceAll("\"", "") + "\"");
						sb.append(InsertString2);

						Log.e("MLISTE", "+++ SQL importCSV:" + sb.toString());
						sqlQuery = sb.toString();

						try {
							SQLiteDatabase db = this.getWritableDatabase();

							db.execSQL(sqlQuery);

							db.close();

						} catch (SQLiteException e) {
							Log.e("MLISTE", "+++ get id +++" + e);

						}

						Toast.makeText(
								context,
								"Modell " + sarray[1].replaceAll("\"", "")
										+ " importiert.", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					Toast.makeText(
							context,
							"Error: Datensatzl�nge nicht korrekt! L:"
									+ String.valueOf(sarray.length),
							Toast.LENGTH_SHORT).show();
				}

			}

			br.close();

			return true;

		} catch (IOException e) {

			Toast.makeText(context,
					"ERROR:Import Model der Datenbank fehlgeschlagen!" + e,
					Toast.LENGTH_SHORT).show();
			Log.e("MLISTE", "+++ ERROR exportDataBaseComplete +++" + e);
			return false;
		}

	}

	public boolean exportDataBaseComplete() throws IOException {
		Log.e("MLISTE", "+++ exportDataBase +++");

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
			header += '"' + "BESCHREIBUNG" + '"' + separation;
			header += '"' + "DATUM" + '"' + separation;
			header += '"' + "SPANNWEITE" + '"' + separation;
			header += '"' + "LAENGE" + '"' + separation;
			header += '"' + "GEWICHT" + '"' + separation;
			header += '"' + "RCEINSTELLUNG" + '"' + separation;
			header += '"' + "AUSSTATTUNG" + '"' + separation;
			header += '"' + "STATUS" + '"' + separation;
			header += '"' + "BILD_PFAD" + '"' + "\n";

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
								+ c.getString(c.getColumnIndex("beschreibung"))
								+ '"';
						record += separation
								+ c.getString(c.getColumnIndex("datum"));
						record += separation + '"'
								+ c.getString(c.getColumnIndex("spannweite"))
								+ '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("l�nge")) + '"';
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
						record += '\n';
						printStream.print(record);
						Integer model_id = Integer.parseInt(c.getString(c
								.getColumnIndex("id")));
						ArrayList<String> flights = new ArrayList<String>();

						// *********************
						// insert flights
						flights = exportDataBaseFlightsById(model_id);
						int length = flights.size();
						if (length > 2) {
							String prefix = "\"\"" + separation + "\"\""
									+ separation;
							String postfix = separation + prefix + prefix
									+ prefix + "\n";
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
				Log.e("MLISTE", "+++ readData +++" + e);

			}

			printStream.flush();
			printStream.close();
			myOutput.flush();
			myOutput.close();

			return true;
		} catch (IOException e) {
			Log.e("MLISTE", "+++ ERROR exportDataBaseComplete +++" + e);
			return false;
		}

	}

	public boolean exportDataBase() throws IOException {
		Log.e("MLISTE", "+++ exportDataBase +++");

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
			header += '"' + "BESCHREIBUNG" + '"' + separation;
			header += '"' + "DATUM" + '"' + separation;
			header += '"' + "SPANNWEITE" + '"' + separation;
			header += '"' + "LAENGE" + '"' + separation;
			header += '"' + "GEWICHT" + '"' + separation;
			header += '"' + "RCEINSTELLUNG" + '"' + separation;
			header += '"' + "AUSSTATTUNG" + '"' + separation;
			header += '"' + "STATUS" + '"' + separation;
			header += '"' + "BILD_PFAD" + '"' + "\n";

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
								+ c.getString(c.getColumnIndex("beschreibung"))
								+ '"';
						record += separation
								+ c.getString(c.getColumnIndex("datum"));
						record += separation + '"'
								+ c.getString(c.getColumnIndex("spannweite"))
								+ '"';
						record += separation + '"'
								+ c.getString(c.getColumnIndex("l�nge")) + '"';
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
						record += "\n";

						printStream.print(record);

					} while (c.moveToNext());
				}

				c.close();

				db.close();

			} catch (SQLiteException e) {
				Log.e("MLISTE", "+++ readData +++" + e);

			}

			printStream.flush();
			printStream.close();
			myOutput.flush();
			myOutput.close();

			return true;
		} catch (IOException e) {
			Log.e("MLISTE", "+++ ERROR exportDataBase +++" + e);
			return false;
		}

	}

	public ArrayList<String> exportDataBaseFlightsById(Integer model_id) {
		Log.e("MLISTE", "+++ exportDataBaseflightsbyid +++");
		ArrayList<String> flights = new ArrayList<String>();
		String constraint = "";
		if (model_id > 0) {
			constraint = " AND f.model_id=" + String.valueOf(model_id)
					+ " ORDER BY f.id ASC";
		} else {
			constraint = " ORDER BY f.id ASC";
		}
		String sqlQuery = "SELECT f.model_id,m.name,f.date,f.description FROM flights f join flightlog m on f.model_id=m.id "
				+ constraint;
		String record = "";
		String header = "";

		header = "\"Fluege\"" + separation;
		header += "\"\"" + separation;
		header += "\"\"";
		flights.add(header);

		header = "\"Nr.\"" + separation;
		header += "\"Datum\"" + separation;
		header += "\"Bericht\"";

		flights.add(header);

		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);
			if (c.moveToFirst()) {
				do {
					record = c.getString(c.getColumnIndex("date"));
					record += separation + "\""
							+ c.getString(c.getColumnIndex("description"))
							+ "\"";

					flights.add(record);

				} while (c.moveToNext());
			}

			c.close();

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ readData +++" + e);

		}
		return flights;

	}

	public boolean exportDataBaseFlights(Integer model_id) throws IOException {
		Log.e("MLISTE", "+++ exportDataBase +++");

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
			String sqlQuery = "SELECT f.model_id,m.name,f.date,f.description FROM flights f join flightlog m on f.model_id=m.id "
					+ constraint;
			String record = "";
			String header = "";

			header = '"' + "MODELL_ID" + '"' + separation;
			header += '"' + "NAME" + '"' + separation;
			header += '"' + "DATUM" + '"' + separation;
			header += '"' + "EINTRAG" + '"' + "\n";

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
						record += separation + '"'
								+ c.getString(c.getColumnIndex("description"))
								+ '"';
						record += "\n";

						printStream.print(record);

					} while (c.moveToNext());
				}

				c.close();

				db.close();

			} catch (SQLiteException e) {
				Log.e("MLISTE", "+++ readData +++" + e);

			}

			printStream.flush();
			printStream.close();
			myOutput.flush();
			myOutput.close();

			return true;
		} catch (IOException e) {
			Log.e("MLISTE", "+++ ERROR exportDataBase +++" + e);
			return false;
		}

	}

	public boolean backupDataBase() throws IOException {
		Log.e("MLISTE", "+++ backupDataBase +++");

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
			Log.e("MLISTE", "+++ ERROR backupDataBase +++" + e);
			return false;
		}

	}

	public Integer insertFlight(String[] params) {
		Integer nextval = getId("flights");
		String sqlQuery = "INSERT INTO flights (id,model_id,description,date) VALUES ('"
				+ nextval + "'";
		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {

				sqlQuery += ",'" + params[i] + "'";
			} else {
				sqlQuery += ",''";
			}

		}
		sqlQuery += ")";
		Log.e("MLISTE", "+++ insertdata +++" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			db.execSQL(sqlQuery);

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ get id +++" + e);

		}

		return nextval;
	}

	public Integer insert(String[] params) {
		Integer nextval = getId("flightlog");
		String sqlQuery = "INSERT INTO flightlog (id,name,typ,beschreibung,"
				+ "datum,spannweite,l�nge,gewicht,rcdata,ausstattung,status) VALUES ('"
				+ nextval + "'";
		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {

				sqlQuery += ",'" + params[i] + "'";
			} else {
				sqlQuery += ",''";
			}

		}
		sqlQuery += ")";
		Log.e("MLISTE", "+++ insertdata +++" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			db.execSQL(sqlQuery);

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ get id +++" + e);

		}

		return nextval;
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

		Log.e("MLISTE", "+++ insertdata +++" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			db.execSQL(sqlQueryDelete);
			db.execSQL(sqlQuery);

			db.close();
			return nextval;

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ get id +++" + e);
			return null;

		}

	}

	public String getImage(Integer model_id) {
		String sqlQuery = "SELECT image_path FROM images WHERE model_id ='"
				+ model_id + "' ORDER BY id DESC";
		String image_path = "";

		Log.e("MLISTE", "+++ selct image +++" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = db.rawQuery(sqlQuery, null);

			if (c.moveToFirst()) {
				do {

					image_path = c.getString(c.getColumnIndex("image_path"));

				} while (c.moveToNext());
			}

			c.close();

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ get image path +++" + e);

		}
		return image_path;
	}

	public void setSeparation(String character) {
		separation = character;
		Log.e("MListe", "SeparationChar:" + separation);
	}

	public void resetImageDB() {

		String sqlQueryDelete = "DELETE FROM images WHERE 1";

		Log.e("MLISTE", "+++ reset ImageDB +++" + sqlQueryDelete);
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			db.execSQL(sqlQueryDelete);

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++reset error of imagedb +++" + e);

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

		Log.e("MLISTE", "+++ updatedata +++" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			Cursor c = db.rawQuery(sqlSearchImage, null);
			File sdCard = Environment.getExternalStorageDirectory();

			if (c.moveToFirst()) {
				do {
					String oldImagePath = c.getString(c
							.getColumnIndex("image_path"));
					;
					String f = sdCard.getAbsolutePath() + "/" + oldImagePath;
					Log.e("MLISTE", "+++ delete_oldimage: +++" + oldImagePath);
					Log.e("MLISTE", "+++ iamge_path +++" + image_path);
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
			Log.e("MLISTE", "+++ update id wrong +++" + e);
			return null;
		}
	}

	public String delete(String id) {
		String[] result_array;
		result_array = id.split(":");
		id = result_array[0].trim();

		String sqlQuery = "DELETE FROM flightlog WHERE id='" + id + "'";

		Log.e("MLISTE", "+++ deletedata +++" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			db.execSQL(sqlQuery);

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ deletedata +++" + e);

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
		sqlQuery += "l�nge='" + params[5] + "',";
		sqlQuery += "gewicht='" + params[6] + "',";
		sqlQuery += "rcdata='" + params[7] + "',";
		sqlQuery += "ausstattung='" + params[8] + "',";
		sqlQuery += "status='" + params[9] + "'";
		sqlQuery += " WHERE id ='" + id + "'";
		Log.e("MLISTE", "+++ updatedata +++" + sqlQuery);
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			db.execSQL(sqlQuery);

			db.close();

		} catch (SQLiteException e) {
			Log.e("MLISTE", "+++ get id +++" + e);

		}

		return Integer.parseInt(id);
	}

	private int getId(String table) {
		String sqlQuery = "SELECT max(id) as nextval, count(*) as counts FROM "
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
			Log.e("MLISTE", "+++ get id +++" + e);

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