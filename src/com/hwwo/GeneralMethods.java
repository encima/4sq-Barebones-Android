package com.hwwo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class GeneralMethods {
	
	public static SQLiteDatabase createDB(Context context) {
		SQLiteDatabase hDB = context.openOrCreateDatabase("HwwoDB", 1, null);
		String createFriendsTable = "CREATE TABLE IF NOT EXISTS hFriends (id VARCHAR, name VARCHAR, photo VARCHAR, gender VARCHAR, city VARCHAR);";
		String createCheckinTable = "CREATE TABLE IF NOT EXISTS hCheckins (id VARCHAR, name VARCHAR, address VARCHAR, lat FLOAT, long FLOAT, category VARCHAR, checkinCount NUMBER, userCheckin NUMBER, tips VARCHAR, images VARCHAR);";
		String createTipsTable = "CREATE TABLE IF NOT EXISTS hTips(id VARCHAR, tip VARCHAR);";
		String createImageTable = "CREATE TABLE IF NOT EXISTS hTips(id VARCHAR, image VARCHAR);";
		hDB.execSQL(createFriendsTable);
		hDB.execSQL(createCheckinTable);
		hDB.execSQL(createTipsTable);
		hDB.execSQL(createImageTable);
		return hDB;
	}
	
	 public static void getFriends(Context context, SharedPreferences hPrefs, SQLiteDatabase hDB) {
	        // extract the OAUTH access token if it exists
	    	String code;
	    	
	    	Vector<Friend> friends = new Vector<Friend>();
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
	    							JSONObject obj = checkins.getJSONObject(i);
	    							Friend f = new Friend();
	    							f.setID(obj.optString("id", null));
	    							String name = obj.optString("firstName", null) + " " + obj.optString("lastName", null);
	    							System.out.println(name);
	    							f.setName(name);
	    							f.setPhoto(obj.optString("photo", null));
	    							f.setGender(obj.optString("gender", null));
	    							f.setCity(obj.optString("homeCity", null));
	    							
	    							friends.add(f);
	    						}
	    						
	    					writeFriends(hDB, friends, true);
	    				}else{
	    					Toast.makeText(context, "Wrong return code: " +code, Toast.LENGTH_SHORT).show();
	    				}
	    			} catch (Exception exp) {
	    				Log.e("LoginTest", "Issue retrieving friends", exp);
	    			}
	        	}else{
	        		Toast.makeText(context, "Unknown login error", Toast.LENGTH_SHORT).show();
	        	}
	    }

		public static void writeFriends(SQLiteDatabase hDB, Vector<Friend> friends, boolean delete) {
			if (delete) {
				hDB.execSQL("DELETE FROM hFriends");
				System.out.println("Deleting from hFriends");
			}
				for(int i = 0; i < friends.size(); i++) { 
					ContentValues insertValues = new ContentValues();
						insertValues.put("id", friends.get(i).getID());				
						insertValues.put("name", friends.get(i).getName());
						insertValues.put("photo", friends.get(i).getPhoto());				
						insertValues.put("gender", friends.get(i).getGender());
						insertValues.put("city", friends.get(i).getCity());
					hDB.insertOrThrow("hFriends", null, insertValues);
				}	
			System.out.println("Inserted " + friends.size() + " 4sq Friends into the hwwo. Databaase");
		}
		
	 public static void getCheckins(Context context, SharedPreferences hPrefs, SQLiteDatabase hDB) {
	        // extract the OAUTH access token if it exists
	    	String code;
	    	
	    	Vector<Checkin> checkins = new Vector<Checkin>();
	    	if(hPrefs.getString("4sqAccessToken", null) != null) {
	    		code = hPrefs.getString("4sqAccessToken", null);
	    		try {
	    				JSONObject userJson = executeHttpGet(
	    						"https://api.foursquare.com/v2/users/self/checkins?oauth_token=" + code);
	    				// Get return code
	    				int returnCode = Integer.parseInt(userJson.getJSONObject("meta").getString("code"));
	    				// 200 = OK
	    				if(returnCode == 200){
	    					// output data
	    					JSONArray checkin = userJson.getJSONObject("response").getJSONObject("checkins").getJSONArray("items");
	    						for(int i = 0; i<checkin.length(); i++) {
	    							JSONObject obj = checkin.getJSONObject(i);
	    							Checkin c = new Checkin();
	    							c.setID(obj.getJSONObject("venue").optString("id", null));
	    							c.setName(obj.getJSONObject("venue").optString("name", null));
	    							c.setAddress(obj.getJSONObject("venue").getJSONObject("location").optString("address", null));
	    							c.setLat(obj.getJSONObject("venue").getJSONObject("location").optDouble("lat"));
	    							c.setLong(obj.getJSONObject("venue").getJSONObject("location").optDouble("lng"));
	    							JSONArray cat = obj.getJSONObject("venue").getJSONArray("categories");
		    							if(cat.length() != 0){ 
		    								c.setCategory(cat.getJSONObject(0).optString("name", null));
		    							}else{
		    								c.setCategory(null);
		    							}
	    							c.setCheckinCount(obj.getJSONObject("venue").getJSONObject("stats").getInt("checkinsCount"));
	    							if(!checkins.contains(c)){
	    								checkins.add(c);
	    							}
	    						}
	    						
	    					writeCheckins(hDB, checkins, true, true);
	    				}else{
	    					Toast.makeText(context, "Wrong return code: " +code, Toast.LENGTH_SHORT).show();
	    				}
	    			} catch (Exception exp) {
	    				Log.e("LoginTest", "Issue retrieving checkins", exp);
	    			}
	        	}else{
	        		Toast.makeText(context, "Unknown login error", Toast.LENGTH_SHORT).show();
	        	}
	    }
	 
	 public static Vector<String> getPlaceInfo(String name, SQLiteDatabase hDB) {
		 Vector<String> tipVector = new Vector<String>();
	    		try {
	    				JSONObject userJson = executeHttpGet(
	    						"https://api.foursquare.com/v2/venues/" + name);
	    				// Get return code
	    				int returnCode = Integer.parseInt(userJson.getJSONObject("meta").getString("code"));
	    				// 200 = OK
	    				if(returnCode == 200){
	    					JSONObject place = userJson.getJSONObject("response").getJSONObject("venue");
		    					Checkin c = new Checkin();
								JSONArray tip = place.getJSONObject("venue").getJSONObject("tips").getJSONArray("groups");
								for(int i  = 0; i < tip.length(); i++) {
									JSONArray tips = tip.getJSONObject(i).getJSONArray("items");
										if(tips.length() != 0) {
											for(int j = 0; j<tip.length(); j++) {
												tipVector.add(tips.getJSONObject(j).optString("text", null));
												Log.i("4sqVenue", tips.getJSONObject(j).optString("text", null));
											}
											//c.setTips(tips);
										}else{
											//c.setTips(null);
										}
								}
	    				}
	    		}catch(Exception e){
	    			
	    		}
		return tipVector;
	 }
		 
		public static void writeCheckins(SQLiteDatabase hDB, Vector<Checkin> checkins, boolean userCheckin, boolean delete) {
			if (delete) {
				hDB.execSQL("DELETE FROM hCheckins");
				System.out.println("Deleting from hCheckins");
			}
				for(int i = 0; i < checkins.size(); i++) { 
					ContentValues insertValues = new ContentValues();
						insertValues.put("id", checkins.get(i).getID());				
						insertValues.put("name", checkins.get(i).getName());
						insertValues.put("address", checkins.get(i).getAddress());
						insertValues.put("lat", checkins.get(i).getLat());
						insertValues.put("long", checkins.get(i).getLong());
						insertValues.put("category", checkins.get(i).getCategory());
						insertValues.put("checkinCount", checkins.get(i).getCheckinCount());
					hDB.insertOrThrow("hCheckins", null, insertValues);
				}	
			System.out.println("Inserted " + checkins.size() + " 4sq Checkins into the hwwo. Databaase");
		}
		
		public static Vector<String> queryDB(SQLiteDatabase hDB, String query) {
			String cID  = null;
			Cursor c = null;
			Vector<String> results = new Vector<String>();
				try {
					c = hDB.rawQuery(query, null);
				}catch(Exception e){
					e.printStackTrace();
				}
					for (boolean hasData = c.moveToFirst(); hasData; hasData = c.moveToNext()) {
						results.add(c.getString(c.getColumnIndexOrThrow("name")));
					}
			c.close();
			return results;
		}
		
		public static Checkin queryPlace(SQLiteDatabase hDB, String name) {
			Cursor c = null;
				try{
					c = hDB.rawQuery("SELECT * FROM hCheckins WHERE name='" + name + "';", null);
				}catch(Exception e){
					Log.e("Query Checkins", "Error querying Database" + e);
				}
			
			Checkin checkin = new Checkin();
			if(c != null) {
				c.moveToFirst();
				System.out.println(c.toString());
				for(boolean hasData = c.moveToFirst(); hasData; hasData = c.moveToNext()){
					checkin.setName(c.getString(c.getColumnIndexOrThrow("name")));
					checkin.setID(c.getString(c.getColumnIndexOrThrow("id")));
					checkin.setLat(c.getDouble(c.getColumnIndexOrThrow("lat")));
					checkin.setLong(c.getDouble(c.getColumnIndexOrThrow("long")));
					checkin.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
					checkin.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
				}
				c.close();
				return checkin;
			}else{
				return null;
			}
			
		}
		
		public static Checkin queryPlaceById(SQLiteDatabase hDB, String name) {
			Cursor c = null;
				try{
					c = hDB.rawQuery("SELECT * FROM hCheckins WHERE id='" + name + "';", null);
				}catch(Exception e){
					Log.e("Query Checkins", "Error querying Database" + e);
				}
			
			Checkin checkin = new Checkin();
			if(c != null) {
				c.moveToFirst();
				System.out.println(c.toString());
				for(boolean hasData = c.moveToFirst(); hasData; hasData = c.moveToNext()){
					checkin.setName(c.getString(c.getColumnIndexOrThrow("name")));
					checkin.setID(c.getString(c.getColumnIndexOrThrow("id")));
					checkin.setLat(c.getDouble(c.getColumnIndexOrThrow("lat")));
					checkin.setLong(c.getDouble(c.getColumnIndexOrThrow("long")));
					checkin.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
					checkin.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
				}
				c.close();
				return checkin;
			}else{
				return null;
			}
			
		}

		// Calls a URI and returns the answer as a JSON object
	    static JSONObject executeHttpGet(String uri) throws Exception{
	    	String result = null;
	    	HttpGet req = new HttpGet(uri);
	    	HttpClient client = new DefaultHttpClient();
	    	HttpResponse resLogin = client.execute(req);
	    	HttpEntity entity = resLogin.getEntity();
	    	InputStream is = entity.getContent();
		    	try{
		    		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
		    		StringBuilder sb = new StringBuilder();
		    		String line = null;
		    		while ((line = reader.readLine()) != null) {
		    			sb.append(line + "\n");
		    			System.out.println(line);
		    		}
		    		is.close();
		    		result=sb.toString();
		    		System.out.println(result);
		    	}catch(Exception e){
		    		Log.e("log_tag", "Error converting result "+e.toString());
		    	}
	    	return new JSONObject(result);
	    }
	    
	    public static JSONObject getJSONfromURL(String url){

	    	//initialize
	    	InputStream is = null;
	    	String result = "";
	    	JSONObject jArray = null;

	    	//http post
	    	try{
	    		HttpClient httpclient = new DefaultHttpClient();
	    		HttpPost httppost = new HttpPost(url);
	    		HttpResponse response = httpclient.execute(httppost);
	    		HttpEntity entity = response.getEntity();
	    		is = entity.getContent();

	    	}catch(Exception e){
	    		Log.e("log_tag", "Error in http connection "+e.toString());
	    	}

	    	//convert response to string
	    	try{
	    		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
	    		StringBuilder sb = new StringBuilder();
	    		String line = null;
	    		while ((line = reader.readLine()) != null) {
	    			sb.append(line + "\n");
	    			//System.out.println(line);
	    		}
	    		is.close();
	    		result=sb.toString();
	    	}catch(Exception e){
	    		Log.e("log_tag", "Error converting result "+e.toString());
	    	}

	    	//try parse the string to a JSON object
	    	try{
	            jArray = new JSONObject(result);
	    	}catch(JSONException e){
	    		Log.e("log_tag", "Error parsing data "+e.toString());
	    	}

	    	return jArray;
	    }
	    
	    static String checkin(SharedPreferences hPrefs, String id, String loc){
	    	String code = null;
		    	if(hPrefs.getString("4sqAccessToken", null) != null) {
		    		code = hPrefs.getString("4sqAccessToken", null);
					JSONObject j = getJSONfromURL("https://api.foursquare.com/v2/checkins/add?oauth_token=" + code + "&venueId=" + id + "&ll=" + loc);
					int returnCode = 0;
						try {
							returnCode = Integer.parseInt(j.getJSONObject("meta").getString("code"));
						} catch (Exception e) {
							e.printStackTrace();
						}
					if(returnCode != 200) {
						return null;
					}else{
						return j.toString();
					}
		    	}
				return null;
	    }
	    
	    public static Vector<String> searchPlaces(Context context, SharedPreferences hPrefs, SQLiteDatabase hDB, String query, Location l) {
	    	Vector<String> locations = new Vector<String>();
	    	Vector<Checkin> search = new Vector<Checkin>();
	    	if(hPrefs.getString("4sqAccessToken", null) != null) {
	    		String code = hPrefs.getString("4sqAccessToken", null);
	    		String loc = l.getLatitude() + "," + l.getLongitude(); 
	    		String locURL = "https://api.foursquare.com/v2/venues/search?oauth_token=" + code + "&ll=" + loc;
	    		String searchURL = "https://api.foursquare.com/v2/venues/search?oauth_token=" + code + "&ll=" + loc + "&query=" + query;
	    		JSONObject places = null;
	    		
	    		try{  
	    			if(query == null) {
	    				places = executeHttpGet(locURL);
	    			}else{
	    				places = executeHttpGet(searchURL);
	    			}
	    			int returnCode = Integer.parseInt(places.getJSONObject("meta").getString("code"));
    				// 200 = OK
    				if(returnCode == 200){
    					// output data
    					JSONArray check = places.getJSONObject("response").getJSONArray("groups");
    					String checkin = check.getJSONObject(0).getString("items");
    					JSONArray checkins = new JSONArray(checkin);
    						for(int i = 0; i<checkins.length(); i++) {
    							JSONObject obj = checkins.getJSONObject(i);
    							Checkin c = new Checkin();
    							c.setID(obj.optString("id", null));
    							c.setName(obj.optString("name", null));
    							Log.i("4sqVenue", obj.optString("name", null));
    							if(obj.optJSONObject("location") != null) {
    								c.setAddress(obj.optJSONObject("location").optString("address", null));
    								Log.i("4sqVenue", obj.optJSONObject("location").optString("address", null));
        							c.setLat(obj.optJSONObject("location").optDouble("lat"));
        							c.setLong(obj.optJSONObject("location").optDouble("lng"));
    							}else{
    								c.setAddress(null);
    								c.setLat(0.0);
    								c.setLat(0.0);
    							}
    							
    							JSONArray cat = obj.getJSONArray("categories");
	    							if(cat.length() != 0){ 
	    								c.setCategory(cat.getJSONObject(0).optString("name", null));
	    							}else{
	    								c.setCategory(null);
	    							}
    							c.setCheckinCount(obj.getJSONObject("stats").getInt("checkinsCount"));
	    							if(!locations.contains(c)){
	    								locations.add(c.getName());
	    								System.out.println("@@@@@@@@@@@@@@@@" + c.getName());
	    							}
	    							if(!search.contains(c)) {
	    								search.add(c);
	    							}
    						}
    						NearbyActivity.update(locations, context);
    						Checkin c = GeneralMethods.queryPlace(hDB, locations.firstElement());
    			    		String description = "You are at " + c.getName() + " which is a " + c.getCategory() + " and is at " + c.getAddress();
    						EstimateActivity.setTheTextViewStuff(locations.firstElement(), description);
    						Log.i("4sqVenue", "Updated places");
    						writeCheckins(hDB, search, false, true);
    				}
	    		}catch(Exception e){
	    			Log.e("Places Search", "Error searching places:" + e);
	    			e.printStackTrace();
	    		}
	    		return locations;
	    	}else{
	    		return null;//writeCheckins(hDB, checkins, true);
	    	}
	    }

	    public static Location getLocation(Context context)
	    {
	        /*LocationManager lm;
	        boolean gps_enabled=false;
	        boolean network_enabled=false;
	    	Toast.makeText(context, "Getting Location, please Wait.....", Toast.LENGTH_LONG).show();
	        
	    	lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	    	//LocListen ll = new LocListen();
	        try{
	        	gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	        	network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	        }catch(Exception ex){
	        	Log.e("Retrieve Location", "Error retrieving gps and network settings" + ex);
	        }

	        if(!gps_enabled && !network_enabled)
	        {
	            return null;
	        }
	        Location c = null;
		        if(gps_enabled)
		        	lm.requestLocationUpdates(lm.GPS_PROVIDER, 100, 2, ll);
		            c = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		            lm.removeUpdates(ll);
		        if(network_enabled)
		        	lm.requestLocationUpdates(lm.NETWORK_PROVIDER, 100, 2, ll);
		            c = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		            lm.removeUpdates(ll);
		    Toast.makeText(context, "Got Location!", Toast.LENGTH_LONG);*/
	        return null;
	    }

}
