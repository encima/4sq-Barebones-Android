package com.hwwo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class FoursqLogin extends Activity {
    /** Called when the activity is first created. */
	
	private SharedPreferences hPrefs;
	String client_id = "JYS2PEKF3JADJL2KUE5VKN1U24UTVLREEYPMY3MZJBYPQPVM";
	String client_secret = "40ES50ZFQZ0J5ASDIVPDBUFZU54JS1ZN4PUUWO023Q1UGKSD";
	WebView wv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foursqlogin);    
        
        hPrefs = getSharedPreferences("h_prefs", MODE_PRIVATE);
        
        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View arg0) {
				findViewById(R.id.webview).setVisibility(1);
				loginToFoursquare();
			}
		});
        wv = (WebView) findViewById(R.id.webview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                String fragment = "#access_token=";
                int start = url.indexOf(fragment);
                if (start > -1) {
                    // You can use the accessToken for api calls now.
                    String accessToken = url.substring(start + fragment.length(), url.length());             	
                    Toast.makeText(FoursqLogin.this, "Token: " + accessToken, Toast.LENGTH_SHORT).show();
                    hPrefs.edit().putString("4sqAccessToken", accessToken).commit();
                    
                    SQLiteDatabase hDB = GeneralMethods.createDB(getApplicationContext());
                    GeneralMethods.getFriends(getApplicationContext(), hPrefs, hDB);
                    GeneralMethods.getCheckins(getApplicationContext(), hPrefs, hDB);
                    hDB.close();
                    
                    Intent first = new Intent(getApplicationContext(), MainActivity.class);
        			first.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			getApplicationContext().startActivity(first);
                    
                }
            }
        });
        final Activity ma = this;
        wv.setWebChromeClient(new WebChromeClient() {
        	 public void onProgressChanged(WebView view, int progress)   
        	 {
        	  //Make the bar disappear after URL is loaded, and changes string to Loading...
        	  ma.setTitle("Loading...");
        	  ma.setProgress(progress * 100); //Make the bar disappear after URL is loaded
        	 
        	  // Return the app name after finish loading
        	     if(progress == 100)
        	        ma.setTitle(R.string.app_name);
        	   }
        	 });
    }
    
    @Override
    public void onBackPressed() {
    	Toast.makeText(getApplicationContext(), "Sorry, we need some authorisation!", Toast.LENGTH_LONG).show();
       return;
    }
    
    private void loginToFoursquare() { 	
    	String url =
            "https://foursquare.com/oauth2/authenticate" + 
                "?client_id=" + client_id + 
                "&response_type=token" + 
                "&redirect_uri= foursqlogin://";
        wv.loadUrl(url);
        wv.requestFocus(View.FOCUS_DOWN);
    }

}