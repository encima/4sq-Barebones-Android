package com.hwwo;

import java.util.Vector;

public class Checkin {
	
	private String id;
	private String name;
	private String address;
	private double lat, lng;
	private String category;
	private int checkinCount;
	private Vector<String> tips;
	private Vector<String> images;

	void setID(String id) {
		this.id = id;
	}

	String getID() {
		return id;
	}

	void setName(String name) {
		this.name = name;
	}

	String getName() {
		return name;
	}

	void setAddress(String address) {
		this.address = address;
	}

	String getAddress() {
		return address;
	}

	void setLat(double lat) {
		this.lat = lat;
	}

	double getLat() {
		return lat;
	}
	
	void setLong(double lng) {
		this.lng = lng;
	}

	double getLong() {
		return lng;
	}

	void setCategory(String category) {
		this.category = category;
	}

	String getCategory() {
		return category;
	}
	
	void setCheckinCount(int checkinCount) {
		this.checkinCount = checkinCount;
	}

	double getCheckinCount() {
		return checkinCount;
	}

	public Vector<String> getTips() {
		return tips;
	}

	public void setTips(Vector<String> tips) {
		this.tips = tips;
	}

	public Vector<String> getImages() {
		return images;
	}

	public void setImages(Vector<String> images) {
		this.images = images;
	}

}
