package com.hwwo;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
    
    LocationManager mLocManager;
    LocationListener mLocListener;
    SensorManager mSenManager;
    SensorEventListener mSenListener;
    
    public static Handler handler;
    
    public static Thread getPlaces = null;
    
    float direction;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handler = new Handler();
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
        
		mLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mLocListener = new LocListen();
		mLocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 15000, 0, mLocListener);
		mLocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 15000, 0, mLocListener);
		Toast.makeText(getApplicationContext(), "Getting Location", Toast.LENGTH_SHORT).show();
		
		mSenManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSenListener = new mSenListen();
		mSenManager.registerListener(mSenListener, mSenManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }
    
    @Override
   protected void onPause() {
    	super.onPause();
    	mLocManager.removeUpdates(mLocListener);
    	mSenManager.unregisterListener(mSenListener);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mLocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 10, mLocListener);
    	mSenManager.registerListener(mSenListener, mSenManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
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
	
	public class LocListen implements LocationListener
	{
		@Override
		public void onLocationChanged(Location loc)
		{
			Log.i("4sqVenue", "Location Changed");
			String l = loc.getLatitude() + "," + loc.getLongitude();
			hPrefs.edit().putString("loc", l).commit();
			String Text = "My current location is: " + "Latitude = " + loc.getLatitude() + "Longitude = " + loc.getLongitude() + " Heading: " + direction;
			Toast.makeText( getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
			SQLiteDatabase hDB = getApplication().openOrCreateDatabase("HwwoDB", 1, null);
			getPlaces = new GetPlaces(getApplicationContext(), null, l, hDB, handler);
			getPlaces.start();
			hDB.close();
		}
		 
		@Override
		public void onProviderDisabled(String provider)
		{
			Toast.makeText( getApplicationContext(),
			"Gps Disabled",
			Toast.LENGTH_SHORT ).show();
		}
		 
		@Override
		public void onProviderEnabled(String provider)
		{
			Toast.makeText( getApplicationContext(),
			"Gps Enabled",
			Toast.LENGTH_SHORT).show();
		}
		 
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		 
		}
	}
	
	public class mSenListen implements SensorEventListener {

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent e) {
			if (e.sensor.getType()==Sensor.TYPE_ORIENTATION) {
				direction = e.values[0];
				direction = trueNorthDirectionConversion(direction, true);
				hPrefs.edit().putFloat("direction", direction);
			}
		}
		
		public float trueNorthDirectionConversion(float inDirection, boolean toTrueNorth) {
			if (toTrueNorth) {
				// Convert from SensorManager to EXIF direction
				inDirection = inDirection + 90;
				if (inDirection >= 360) {
					inDirection = inDirection - 360;
				}
				
			} else {
				// Convert from EXIF direction to SensorManager
				inDirection = inDirection - 90;
				if (inDirection < 0) {
					inDirection = inDirection + 360;
				}
			}
			return inDirection;

		}
		
	}
}