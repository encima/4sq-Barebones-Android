package com.hwwo;

import java.util.Vector;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class NearbyActivity extends ListActivity {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	
	SQLiteDatabase hDB; 
	SharedPreferences hPrefs;
	ProgressDialog mProgress;
	Vector<String> nearby = new Vector<String>();
	Vector<String> search = new Vector<String>();
	Context context;
	Location c = null;
	static ListView lv;
	static ArrayAdapter<String> la;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.placeslayout);

		lv = getListView();
		context = getApplicationContext();
		hDB = getApplicationContext().openOrCreateDatabase("HwwoDB", 1, null);
		hPrefs = getSharedPreferences("h_prefs", MODE_PRIVATE);
		if(hPrefs.getString("4sqAccessToken", null) != null){
			refresh(GeneralMethods.queryDB(hDB, "SELECT name FROM hCheckins"));
		}else{
			la = new ArrayAdapter<String>(getApplicationContext(), R.layout.places_item, COUNTRIES);
			setListAdapter(la);
		}
		lv.setCacheColorHint(0);
		lv.setTextFilterEnabled(true);
		lv.setOnTouchListener(MainActivity.gestureListener);
		
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	String place = ((TextView) view).getText().toString();
		    	Checkin c = GeneralMethods.queryPlace(hDB, place);
		    	//String ID = c.getID();
		    	//c = GeneralMethods.queryPlaceById(hDB, ID);
		    	String description  = null;
		    	if(c != null) description = "You are at " + c.getName() + " which is a " + c.getCategory() + " and is at " + c.getAddress();
		    	EstimateActivity.setTheTextViewStuff(place, description);
		    	MainActivity.setTab(1);
		    }
		});
	}
	
	static final String[] COUNTRIES = new String[] {
	    "You are not authenticated!", "So...", "You cannot check-in" , "How about logging in?"
	  };
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Near You");
		menu.add("Search");
		menu.add("Exit");
		return true;
	}
	
	public void refresh(Vector<String> listVector) {
		la = new ArrayAdapter<String>(getApplicationContext(), R.layout.places_item, listVector);
		lv.setAdapter(la);
		la.notifyDataSetChanged();
	}
	
	public void search() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("SEARCH!");
		alert.setMessage("Enter a place name or whatever into here...");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			final String value = input.getText().toString().trim();
			final ProgressDialog progD = ProgressDialog.show(NearbyActivity.this, "Searching For: ", value, true);
				final Handler handler = new Handler() {
				   public void handleMessage(Message msg) {
					  search.clear();
					  if(hPrefs.getString("loc", null) != null) {
						  if(MainActivity.getPlaces.isAlive()) MainActivity.getPlaces.interrupt();
						  MainActivity.getPlaces = new GetPlaces(getApplicationContext(), value, hPrefs.getString("loc", null), hDB, MainActivity.handler);
						  MainActivity.getPlaces.start();
					  }
					  //refresh(search);
				      progD.dismiss();
				      }
				   };
				Thread checkUpdate = new Thread() {  
				   public void run() {
					  try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				      handler.sendEmptyMessage(0);
				      }
				   };
				checkUpdate.start();
		}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals("Near You")) {
			final ProgressDialog progD = ProgressDialog.show(NearbyActivity.this, "Searching For: ", "Places Near You", true);
			final Handler handler = new Handler() {
			   public void handleMessage(Message msg) {
				  nearby.clear();
				  //nearby = GeneralMethods.searchPlaces(context, hPrefs, hDB, null, c);
				  if(hPrefs.getString("loc", null) != null) {
					  Thread getPlaces = new GetPlaces(getApplicationContext(), null, hPrefs.getString("loc", null), hDB, MainActivity.handler);
					  getPlaces.start();
				  }
				  //refresh(nearby);
			      progD.dismiss();
			      }
			   };
			Thread checkUpdate = new Thread() {  
			   public void run() {
				  try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			      handler.sendEmptyMessage(0);
			      }
			   };
			checkUpdate.start();
		}else if(item.getTitle().equals("Search")) {
			search();
		}else{
			finish();
		}
		return true;
	}
	
	public void near() {
		
	}
}
