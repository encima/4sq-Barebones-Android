package com.hwwo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.R.color;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends TabActivity implements OnClickListener{
    /** Called when the activity is first created. */
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    public static View.OnTouchListener gestureListener;
    private static TabHost tabHost;
    private static int tabWeAreOn;
    private SharedPreferences hPrefs;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        hPrefs = getSharedPreferences("h_prefs", MODE_PRIVATE); 	
        
        if(hPrefs.getString("4sqAccessToken", null) == null) {
			Intent first = new Intent(getApplicationContext(), FoursqLogin.class);
			first.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(first);
		}

     // Gesture detection
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };

        Resources res = getResources(); // Resource object to get Drawables
        tabHost = getTabHost();  // The activity TabHost
        TextView tview;
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        tabHost.setOnClickListener(MainActivity.this);
        tabHost.setOnTouchListener(gestureListener);
        
        intent = new Intent().setClass(this, NearbyActivity.class);
        tview=new TextView(this); tview.setText("nearby."); tview.setGravity(1);
        tview.setTextSize(20.0f); tview.setTextColor(Color.parseColor("#ffffff")); 
        tview.setTypeface(Typeface.DEFAULT_BOLD);
        spec = tabHost.newTabSpec("nearby").setIndicator(tview).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, EstimateActivity.class);
        tview=new TextView(this); tview.setText("hwwo."); tview.setGravity(1);
        tview.setTextSize(20.0f); tview.setTextColor(Color.parseColor("#ffffff")); 
        tview.setTypeface(Typeface.DEFAULT_BOLD);
        spec = tabHost.newTabSpec("home").setIndicator(tview).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, FriendsActivity.class);
        tview=new TextView(this); tview.setText("friends."); tview.setGravity(1);
        tview.setTextSize(20.0f); tview.setTextColor(Color.parseColor("#ffffff")); 
        tview.setTypeface(Typeface.DEFAULT_BOLD);
        //tview = (TextView)findViewById(R.id.friendsTabView);
        spec = tabHost.newTabSpec("friends").setIndicator(tview).setContent(intent);
        tabHost.addTab(spec);
        
        tabWeAreOn = 1;
        tabHost.setCurrentTab(tabWeAreOn);
        ((TextView)tabHost.getCurrentTabView()).setTextColor(Color.parseColor("#50524E"));
    }
    
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	if(tabWeAreOn != 2)
                    	tabWeAreOn++;
                    tabHost.setCurrentTab(tabWeAreOn);
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	if(tabWeAreOn != 0)
                    	tabWeAreOn--;
                    tabHost.setCurrentTab(tabWeAreOn);
                }
                for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) 
                { 
                	//((TextView)tabHost.getChildAt(i)).setTextColor(Color.parseColor("#000000"));
                	((TextView)tabHost.getTabWidget().getChildAt(i)).setTextColor(Color.parseColor("#ffffff"));
                } 
                ((TextView)tabHost.getCurrentTabView()).setTextColor(Color.parseColor("#50524E"));
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }
    
    public static void setTab(int index) {
    	tabHost.setCurrentTab(index);
    	tabWeAreOn = index;
    	for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) 
        {
        	((TextView)tabHost.getTabWidget().getChildAt(i)).setTextColor(Color.parseColor("#ffffff"));
        } 
    	((TextView)tabHost.getCurrentTabView()).setTextColor(Color.parseColor("#50524E"));
    }
	@Override
	public void onClick(View v) {
        //Filter f = (Filter) v.getTag();
        //FilterFullscreenActivity.show(this, input, f);		
	}
}