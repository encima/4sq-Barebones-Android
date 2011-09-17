package com.hwwo;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class FriendsActivity extends ListActivity {
	
	static ListView lv;
	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		lv = getListView();//(ListView)findViewById(R.id.listView1);
		SharedPreferences hPrefs = getSharedPreferences("h_prefs", MODE_PRIVATE);
		if(hPrefs.getString("4sqAccessToken", null) != null) {
			SQLiteDatabase hDB = getApplicationContext().openOrCreateDatabase("HwwoDB", 1, null);
			setListAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.places_item, GeneralMethods.queryDB(hDB, "SELECT name FROM hFriends")));
			hDB.close();
		}else{
			setListAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.places_item, COUNTRIES));
		}
		lv.setCacheColorHint(0);
		lv.setTextFilterEnabled(true);
		lv.setOnTouchListener(MainActivity.gestureListener);
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	//set the selected friend to be listed as being with
		    	if(view.getTag() != "SELECTED"){
		    		view.setBackgroundColor(Color.parseColor("#50524E"));
		    		view.setTag("SELECTED");
		    		view.setSelected(true);
		    	}else{
		    		view.setBackgroundColor(Color.TRANSPARENT);
		    		view.setTag("");
		    		view.setSelected(false);
		    	}
		    }
		});
	}
	
	public static String getListOfSelectedFriends(){
		StringBuilder sb = new StringBuilder();
		
		long[] l = lv.getCheckItemIds();
		
		for(int i = 0; i < l.length ; i++){
			sb.append(lv.getItemAtPosition(i).toString());
			sb.append(", ");
		}
		
		return sb.toString();
	}
	
	static final String[] COUNTRIES = new String[] {
	    "Chris Gwilliams", "Chris Fellows", "Geoff Leopard" , "Brian Maiden"
	  };
}
