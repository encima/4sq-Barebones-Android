package com.hwwo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class EstimateActivity extends Activity {
	private static final int TAKE_PICTURE = 0;
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	static TextView _textview;
	private ImageButton _imgbutton;
    String client_id = "JYS2PEKF3JADJL2KUE5VKN1U24UTVLREEYPMY3MZJBYPQPVM";
	String client_secret = "40ES50ZFQZ0J5ASDIVPDBUFZU54JS1ZN4PUUWO023Q1UGKSD";
	private SharedPreferences hPrefs;
	private boolean updateTwitter = true;
	private boolean updateFacebook = true;
	String checkinResult = null;
	ProgressDialog mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homelayout);
		
		hPrefs = getSharedPreferences("h_prefs", MODE_PRIVATE); 
		
        _textview = (TextView)findViewById(R.id.TextView01);
        _textview.setText("Location here.");
        
        _imgbutton = (ImageButton) findViewById(R.id.ImageButton01);
        _imgbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				takePhoto(v);
			}
		});
        
        final Button facebookButton = (Button) findViewById(R.id.btnFacebookPost);
        facebookButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(updateFacebook){
					facebookButton.setBackgroundColor(Color.TRANSPARENT);
					updateFacebook = false;
				}else {
					facebookButton.setBackgroundColor(Color.parseColor("#50524E"));
					updateFacebook = true;
				}
			}
		});
        
        final Button twitterButton = (Button) findViewById(R.id.btnTwitterPost);
        twitterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(updateTwitter){
					twitterButton.setBackgroundColor(Color.TRANSPARENT);
					updateTwitter = false;
				}else {
					twitterButton.setBackgroundColor(Color.parseColor("#50524E"));
					updateTwitter = true;
				}
			}
		});
        
        final Button checkinButton = (Button) findViewById(R.id.btnCheckIn);
        checkinButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkinButton.setBackgroundColor(Color.parseColor("#ffffff"));
				if(!_textview.getText().equals("Location here.")) {
					SQLiteDatabase hDB = getApplication().openOrCreateDatabase("HwwoDB", 1, null);
					final Checkin c = GeneralMethods.queryPlace(hDB, _textview.getText().toString());
					if(c != null) {
						final ProgressDialog progD = ProgressDialog.show(EstimateActivity.this, "Checking in to... ", _textview.getText().toString(), true);
						final Handler handler = new Handler() {
						   public void handleMessage(Message msg) {
			                	String id = c.getID();
								String loc = c.getLat() + "," + c.getLong();
								String result = GeneralMethods.checkin(hPrefs, id, loc);
								JSONObject checkin = null;
								int code = 0;
									try {
										checkin = new JSONObject(result);
										code = Integer.parseInt(checkin.getJSONObject("meta").getString("code"));
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									if(code != 200) {
										Toast.makeText(EstimateActivity.this, "Something went wrong here, hmmmm...", Toast.LENGTH_LONG).show();
									}else{
										System.out.println("****" + checkin.toString());
										try {
											JSONArray arr = checkin.getJSONArray("notifications");
												for(int i = 0; i<arr.length(); i++) {
													JSONObject obj = arr.getJSONObject(i);
													if(obj.getString("type").equals("message")) {
														_textview.setText(obj.getJSONObject("item").getString("message"));
													}
												}
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								checkinButton.setBackgroundColor(Color.parseColor("#50524E"));
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
				        hDB.close();
					}else{
						Toast.makeText(getApplicationContext(), "Wow, something messed up", 
								Toast.LENGTH_LONG).show();
					}
				}else{
					Toast.makeText(getApplicationContext(), "Choose a location first, really?", 
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	public static void setTheTextViewStuff(String str){
		_textview.setText(str);
	}
	
	 public void testUser() {
	        // extract the OAUTH access token if it exists
	    	String code;
	    	if(hPrefs.getString("4sqAccessToken", null) != null) {
	    		code = hPrefs.getString("4sqAccessToken", null);
	    		try {
	    				JSONObject userJson = executeHttpGet(
	    						"https://api.foursquare.com/v2/users/self/friends?oauth_token=" + code);
	    				// Get return code
	    				int returnCode = Integer.parseInt(userJson.getJSONObject("meta").getString("code"));
	    				// 200 = OK
	    				if(returnCode == 200){
	    					// output data
	    					JSONArray checkins = userJson.getJSONObject("response").getJSONObject("friends").getJSONArray("items");
	    						for(int i = 0; i<checkins.length(); i++) {
	    							JSONObject test = checkins.getJSONObject(i);
	    							System.out.println(test.get("firstName"));
	    						}	
	    				}else{
	    					Toast.makeText(this, "Wrong return code: " +code, Toast.LENGTH_SHORT).show();
	    				}
	    			} catch (Exception exp) {
	    				Log.e("LoginTest", "Login to Foursquare failed", exp);
	    			}
	        	}else{
	        		Toast.makeText(this, "Unknown login error", Toast.LENGTH_SHORT).show();
	        	}
	    }

	    // Calls a URI and returns the answer as a JSON object
	    private JSONObject executeHttpGet(String uri) throws Exception{
	    	HttpGet req = new HttpGet(uri);

	    	HttpClient client = new DefaultHttpClient();
	    	HttpResponse resLogin = client.execute(req);
	    	BufferedReader r = new BufferedReader(
	    			new InputStreamReader(resLogin.getEntity()
	    					.getContent()));
	    	StringBuilder sb = new StringBuilder();
	    	String s = null;
	    	while ((s = r.readLine()) != null) {
	    		sb.append(s);
	    	}

	    	return new JSONObject(sb.toString());
	    }
	    
	    private Uri imageUri;

	    public void takePhoto(View view) {
	        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
	        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
	        intent.putExtra(MediaStore.EXTRA_OUTPUT,
	                Uri.fromFile(photo));
	        imageUri = Uri.fromFile(photo);
	        startActivityForResult(intent, TAKE_PICTURE);
	    }

	    @Override
	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        switch (requestCode) {
	        case TAKE_PICTURE:
	            if (resultCode == Activity.RESULT_OK) {
	                Uri selectedImage = imageUri;
	                getContentResolver().notifyChange(selectedImage, null);
	                ContentResolver cr = getContentResolver();
	                Bitmap bitmap;
	                try {
	                     bitmap = android.provider.MediaStore.Images.Media
	                     .getBitmap(cr, selectedImage);
	                     
	                     int width = bitmap.getWidth();
	                     int height = bitmap.getHeight();
	                     int newWidth = _imgbutton.getWidth();
	                     int newHeight = _imgbutton.getHeight();
	                     
	                     //calculate the scale - in this case = 0.4f
	                     float scaleWidth = ((float) newWidth) / width;
	                     float scaleHeight = ((float) newHeight) / height;
	                     
	                     //create a matrix for the manipulation
	                     Matrix matrix = new Matrix();
	                     // resize the bit map
	                     matrix.postScale(scaleWidth, scaleHeight);
	              
	                     // recreate the new Bitmap
	                     Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
	                                       width, height, matrix, true);
	           
	                     
	                     _imgbutton.setImageBitmap(resizedBitmap);                    
	                     Toast.makeText(this, selectedImage.toString(),
	                            Toast.LENGTH_LONG).show();
	                } catch (Exception e) {
	                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
	                            .show();
	                    Log.e("Camera", e.toString());
	                }
	            }
	        }
	    }
	    
	    public void checkResult(String result) {
	    	_textview.setText(result);
	    }
}
