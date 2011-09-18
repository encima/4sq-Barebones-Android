package com.hwwo;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

class GetPlaces extends Thread {

	Context context;
	String query;
	String l;
	SQLiteDatabase hDB;
	Handler handler;
	
	public GetPlaces(Context context, String q, String l, SQLiteDatabase hDB, Handler handler) {
		this.context = context;
		this.query = q;
		this.l = l;
		this.hDB = hDB;
		this.handler = handler;
	}
	
	public void run() {
		final Vector<String> locations = new Vector<String>();
    	Vector<Checkin> search = new Vector<Checkin>();
    	SharedPreferences hPrefs = context.getSharedPreferences("h_prefs", 0); 	
    	if(hPrefs.getString("4sqAccessToken", null) != null) {
    		String code = hPrefs.getString("4sqAccessToken", null);
    		String locURL = "https://api.foursquare.com/v2/venues/search?oauth_token=" + code + "&ll=" + l;
    		String searchURL = "https://api.foursquare.com/v2/venues/search?oauth_token=" + code + "&ll=" + l + "&query=" + query;
    		JSONObject places = null;
    		Log.i("4sqVenue", "Searching for: " + query);
    		try{  
    			if(query == null) {
    				places = GeneralMethods.executeHttpGet(locURL);
    			}else{
    				places = GeneralMethods.executeHttpGet(searchURL);
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
								//Log.i("4sqVenue", obj.optJSONObject("location").optString("address", null));
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
						Log.i("4sqVenue", "Updated places");
						//NearbyActivity.update(locations, context);
			            handler.postAtFrontOfQueue(new Runnable() {
							@Override
							public void run() {
								NearbyActivity.la.clear();
								NearbyActivity.la.notifyDataSetChanged();
								NearbyActivity.la = new ArrayAdapter<String>(context, R.layout.places_item, locations);
								NearbyActivity.la.notifyDataSetChanged();
								NearbyActivity.lv.setAdapter(NearbyActivity.la);
							}
			            });
						Checkin c = GeneralMethods.queryPlaceById(hDB, search.firstElement().getID());
			    		String description = "You are at " + c.getName() + " which is a " + c.getCategory() + " and is at " + c.getAddress();
						EstimateActivity.setTheTextViewStuff(locations.firstElement(), description);
						Log.i("4sqVenue", "Updated places");
						GeneralMethods.writeCheckins(hDB, search, false, true);
				}
    		}catch(Exception e){
    			Log.e("Places Search", "Error searching places:" + e);
    			e.printStackTrace();
    		}
    	}
	}
}
