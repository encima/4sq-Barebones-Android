package com.hwwo;

import java.util.Vector;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SearchActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onDestroy();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        final EditText searchTerm = (EditText) findViewById(R.id.txtSearch);
        searchTerm.setHint("Enter Places/Search Term Here!");
        Button search = (Button) findViewById(R.id.btnSearch);
        
        search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(!searchTerm.getText().toString().equals("")) {
					SharedPreferences hPrefs = getSharedPreferences("h_prefs", MODE_PRIVATE);
					SQLiteDatabase hDB = getApplicationContext().openOrCreateDatabase("HwwoDB", 1, null);
					GeneralMethods.searchPlaces(getApplicationContext(), hPrefs, hDB, searchTerm.getText().toString());
					finish();
				}else{
					Toast.makeText(getApplicationContext(), "Come on, I think we both know this is an empty box", Toast.LENGTH_LONG).show();
				}
			}
		});
    }
}