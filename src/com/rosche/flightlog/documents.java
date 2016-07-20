package com.rosche.flightlog;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;


import java.util.ArrayList;

import net.sf.andpdf.nio.ByteBuffer;
import net.sf.andpdf.refs.HardReference;



import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFImage;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFPaint;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Point;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class documents extends Activity {
	ArrayList<ArrayList<String>> DataToDB;
	String modelID = "";
	String editID = "";
	boolean editMode = false;
	protected static final int REQUEST_PICK_DOCUMENT = 123;
	View layout;
	private float padding;
	TextView tv;
	private int ViewSize = 0;
	TextView tvdate;
	String query;
	public static SharedPreferences mPrefs;
	public String document_path = "Documents";
	String DocumentString = "";
	Integer flightId = 0;
	boolean lockIntenet = false;
	DatabaseHelper myDbHelper = new DatabaseHelper(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.documents);
		query = getIntent().getStringExtra("query");
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		readPrefs();
		getDocs(query);

	}

	public void readPrefs() {
		document_path = mPrefs.getString("document_path", document_path);

	}

	final Handler handler = new Handler();
	Runnable mLongPressed = new Runnable() {
		public void run() {
			lockIntenet = true;
			final AlertDialog.Builder b = new AlertDialog.Builder(
					documents.this);
			b.setIcon(android.R.drawable.ic_dialog_alert);
			b.setMessage("Are you sure to delete this document?");
			b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					myDbHelper.deleteDocument(DocumentString, modelID);
					lockIntenet = false;
					getDocs(query);
					
				}
			});
			b.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					lockIntenet = false;
				}
			});

			b.show();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.documents, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.pic_document:
			pickDocument();
			return true;
		}
		return false;
	}

	private void pickDocument() {
		File root = new File(Environment.getExternalStorageDirectory()
				.getPath() + document_path);
		Uri uri = Uri.fromFile(root);
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
		intent.setData(uri);
		intent.setType("application/pdf");
		startActivityForResult(
				Intent.createChooser(intent, "Complete action using"),
				REQUEST_PICK_DOCUMENT);
	}
	public static String getFileNameByUri(Context context, Uri uri)
	{
	    String fileName="unknown";
	    Uri filePathUri = uri;
	    if (uri.getScheme().toString().compareTo("content") == 0)
	    {      
	        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
	        if (cursor.moveToFirst())
	        {
	            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
	            filePathUri = Uri.parse(cursor.getString(column_index));
	            fileName = filePathUri.getLastPathSegment().toString();
	        }
	    }
	    else if (uri.getScheme().compareTo("file")==0)
	    {
	        fileName = filePathUri.getLastPathSegment().toString();
	    }
	    else
	    {
	        fileName = fileName+"_"+filePathUri.getLastPathSegment();
	    }
	    return fileName;
	}
	public String getPath(Uri uri) {
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

		if (requestCode == REQUEST_PICK_DOCUMENT) {
			if (resultCode == -1) {
				Uri documentUri = data.getData();
				String path = getPath(documentUri);
				myDbHelper.addDocument(Integer.parseInt(modelID), path);
				
				String newImagePath ="";
				Resources r = getResources();
				padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
						r.getDisplayMetrics());
				if (documentUri != null) {
					Log.e("documentUri",""+documentUri);
					File sdCard = Environment.getExternalStorageDirectory();
					File dir = new File(sdCard.getAbsolutePath() + "/flight_log/previews");
					dir.mkdir();
					newImagePath = "flight_log/previews/";
					String fileName = documentUri.getLastPathSegment();
					Bitmap bitmap = null;
					
					int imgageSize = (int) ((getScreenWidth(this) - ((3 + 1) * padding)) / 3);
					if (fileName.toLowerCase().contains(".pdf")){
						bitmap  = renderPdf(imgageSize, path);
					}
					else if(fileName.toLowerCase().contains(".jpg") || fileName.toLowerCase().contains(".png") || fileName.toLowerCase().contains(".gif")) {
						bitmap = ImageGallery.decodeSampledBitmapFromFile(
								getPath(documentUri), imgageSize, imgageSize);
					}
					
					File fileThumb = new File(dir, fileName+".jpg");
					FileOutputStream out;
					try {
						out = new FileOutputStream(fileThumb);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
						out.flush();
						out.close();
					} catch (Exception e) {
					}

					fileThumb = null;
					Log.e("FL","bitmap:"+bitmap);
				}
				
				
				
				
				
			}
		}
	}
	public int getScreenWidth(Context context) {
		int columnWidth;
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (java.lang.NoSuchMethodError ignore) {
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		columnWidth = point.x;
		return columnWidth;
	}
	@Override
	public void onResume() {
		super.onResume();
		getDocs(query);

	}
	private void showPDFDialog(){
		 AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);                      
		    dlgAlert.setMessage("No Viewer Found");
		    dlgAlert.setTitle("Error");              
		    dlgAlert.setPositiveButton("OK", null);
		    dlgAlert.setCancelable(true);
		    dlgAlert.create().show();
		    finish(); 
	}
	private void getDocs(String query) {
		String[] result_array;
		result_array = query.split(":");
		modelID = result_array[0].trim();

		try {

			myDbHelper.createDataBase();
			ArrayList<String> documentPaths = new ArrayList<String>();

			documentPaths = myDbHelper.getDocuments(Integer.parseInt(modelID));
			if (documentPaths.size() > 0) {

				TableLayout tbl = (TableLayout) findViewById(R.id.tableLayout2);
				tbl.removeAllViews();
				TableRow.LayoutParams params1 = new TableRow.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
						1.0f);
				TableRow.LayoutParams params4 = new TableRow.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
						1.0f);
				TableRow.LayoutParams params2 = new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

				TableRow.LayoutParams params3 = new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, 1);
				View vH = new View(this);
				vH.setLayoutParams(params3);
				vH.setBackgroundColor(getResources().getColor(
						R.color.errorColor));
				tbl.addView(vH);

				for (int i = 0; i < documentPaths.size(); i++) {
					
					
					
					
					
					
					final TableRow row = new TableRow(this);
					final String docPath = documentPaths.get(i);
					row.setPadding(10, 2, 0, 0);
					TextView txt1 = new TextView(this);

					
					File pdfFile = new File(docPath);
					Uri path = Uri.fromFile(pdfFile);
					txt1.setText(path.getLastPathSegment());
					pdfFile = null;
					//params4.gravity = Gravity.CENTER;
					params4.topMargin = 10;
					params4.leftMargin = 5;
					txt1.setLayoutParams(params4);
				
				
					txt1.setId(i);
					txt1.setTextSize(18);
					View v = new View(this);
					v.setLayoutParams(params3);
					v.setBackgroundColor(getResources().getColor(
							R.color.errorColor));

					
					String fileName = path.getLastPathSegment();
					Bitmap bMap = null;
					File sdCard = Environment.getExternalStorageDirectory();
					String previewPath = sdCard.getAbsolutePath() + "/flight_log/previews/"+fileName+".jpg";
							
					bMap = BitmapFactory.decodeFile(previewPath);

			        ImageView imageView = new ImageView(this);

			        if (bMap != null) {
			        	  imageView.setImageBitmap(bMap);
			        } else {
			        	imageView.setImageResource(R.drawable.dokument);
			        	
			        }
			      
			        imageView.setLayoutParams(params1);
			        row.addView(imageView);
			        row.addView(txt1);
					
					row.setLayoutParams(params2);
					row.setFocusableInTouchMode(true);
					row.setClickable(true);
					row.setFocusable(true);
					row.setBackgroundResource(R.drawable.selector);

					row.setOnTouchListener(new View.OnTouchListener() {
						boolean returncode = false;

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							final int action = event.getAction();
							switch (action & MotionEvent.ACTION_MASK) {
							case MotionEvent.ACTION_DOWN: {
								handler.postDelayed(mLongPressed, 600);
								returncode = false;
								DocumentString = docPath;

								break;
							}
							case MotionEvent.ACTION_UP:
								handler.removeCallbacks(mLongPressed);
								if (lockIntenet == false) {
									Intent target = new Intent();
									File pdfFile = new File(docPath);
									Uri path = Uri.fromFile(pdfFile);
									target.setAction(Intent.ACTION_VIEW);
									if(docPath.toLowerCase().contains(".pdf")) {
										target.setDataAndType(path,"application/pdf");
										target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
									} else {
										target.setDataAndType(path,"image/*");
									}
		
										Intent intent = Intent.createChooser(target, "Open File");
									try {
									    startActivity(intent);
									} catch (ActivityNotFoundException e) {
										showPDFDialog();
										
										} 
									}
								break;
							case MotionEvent.ACTION_CANCEL:
							case MotionEvent.ACTION_OUTSIDE:
								handler.removeCallbacks(mLongPressed);
								DocumentString = docPath;
								returncode = false;
								break;
							default:
								returncode = false;
							}
							return returncode;
						}

					});
					row.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
						}
					});

					tbl.addView(row);
					tbl.addView(v);

				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private Bitmap renderPdf( int imgageSize, String fileName) {
		
		PDFImage.sShowImages = true; // show images
	    PDFPaint.s_doAntiAlias = true; // make text smooth
	    HardReference.sKeepCaches = true; // save images in cache
		 Bitmap page = null;
		 // Environment.getExternalStorageDirectory().getPath()+"/randompdf.pdf
		 File file = new File(fileName);
         RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			
			Log.e("FL","raf:"+ raf);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  try {
			  PDFFile pdf;
         FileChannel channel = raf.getChannel();
     	Log.e("FL","channel:"+ channel);
         ByteBuffer bb = ByteBuffer.NEW(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
         pdf = new PDFFile(bb);
			raf.close();
			ViewSize = 200;
			  //Get the first page from the pdf doc
	          PDFPage PDFpage = pdf.getPage(1, true);
			  final float scale = imgageSize * 0.99f;
	         page = PDFpage.getImage((int)(scale), (int)( scale), null, true, true);

	      	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  return page;
	}

}
