package com.hwwo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EstimateActivity extends Activity {
	private static final int TAKE_PICTURE = 0;
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	static TextView _textview, tv2, tipview
	;
	private ImageButton _imgbutton;
    String client_id = "JYS2PEKF3JADJL2KUE5VKN1U24UTVLREEYPMY3MZJBYPQPVM";
	String client_secret = "40ES50ZFQZ0J5ASDIVPDBUFZU54JS1ZN4PUUWO023Q1UGKSD";
	private SharedPreferences hPrefs;
	static ImageView imgView;
	String checkinResult = null;
	ProgressDialog mProgress;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homelayout);
		
		hPrefs = getSharedPreferences("h_prefs", MODE_PRIVATE); 
		
        _textview = (TextView)findViewById(R.id.TextView01);
        _textview.setText("Location here.");
        tv2 = (TextView) findViewById(R.id.TextView02);
        tipview = (TextView) findViewById(R.id.TipView);
        imgView = (ImageView)findViewById(R.id.fourPic);
        SQLiteDatabase hDB = getApplication().openOrCreateDatabase("HwwoDB", 1, null);
        //Vector<String> place = GeneralMethods.queryDB(hDB, "SELECT * FROM hCheckins");
        if(hPrefs.getString("4sqAccessToken", null) != null) {
        	String name =  NearbyActivity.lv.getAdapter().getItem(0).toString();
        	_textview.setText(name);
        	Checkin c = GeneralMethods.queryPlace(hDB, name);
        	String description = "You are at " + c.getName() + " which is a " + c.getCategory() + " and is at " + c.getAddress() + "\n You are heading in direction: " + hPrefs.getFloat("direction", 0);
        	Vector<String> tips = GeneralMethods.getPlaceInfo(c.getID(), hDB, hPrefs);
        	StringBuilder builder = new StringBuilder();
        	String tipString = "No Tips Found";
        	if(!tips.isEmpty()) {	        	
	        	for(int i = 0; i < tips.size(); i++) {
	        		builder.append("-" + tips.get(i) + "\n");
	        	}
	        	tipString = builder.toString();
        	}
        	Vector<String> image = GeneralMethods.getPlaceImages(c.getID(), hDB, hPrefs);
        	if(!image.isEmpty()) {
        		EstimateActivity.setImage(image.firstElement(), getApplicationContext());
        	}
        	
        	EstimateActivity.setTheTextViewStuff(name, description, tipString);
        }

        _imgbutton = (ImageButton)findViewById(R.id.imgBtn);
        _imgbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				takePhoto(v);
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
	
	public static void setImage(String firstElement, Context context) {
		//Drawable image = ImageOperations(context , firstElement,"image.jpg");
		//Bitmap img = loadBitmap(firstElement);
		URL newurl;
		try {
			newurl = new URL(firstElement);
			Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
			imgView.setImageBitmap(mIcon_val);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	}
		
	public static void setTheTextViewStuff(String place, String description, String tip){
		_textview.setText(place);
		tv2.setText(description);
		tipview.setText(tip);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Get Tips");
		menu.add("Exit");
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals("Get Tips")) {
			SQLiteDatabase hDB = getApplication().openOrCreateDatabase("HwwoDB", 1, null);
			Checkin ID = GeneralMethods.queryPlace(hDB, _textview.getText().toString()); 
			//Vector<String> tips = GeneralMethods.getPlaceInfo(ID.getID(), hDB);
			//if(!tips.isEmpty()){
			if(ID.getID() != null && !GeneralMethods.getPlaceInfo(ID.getID(), hDB, hPrefs).isEmpty()) {
				tipview.setText(GeneralMethods.getPlaceInfo(ID.getID(), hDB, hPrefs).firstElement());
			}else{
				tipview.setText("No Tips found");
			}
				//Toast.makeText(getApplicationContext(), tips.firstElement(), Toast.LENGTH_SHORT).show();
			//}else{
				//Toast.makeText(getApplicationContext(), "No Tips for " + ID.getID(), Toast.LENGTH_SHORT).show();
			//}
		}else{
			finish();
		}
		return true;
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

/* public void testUser() {
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
	    */
