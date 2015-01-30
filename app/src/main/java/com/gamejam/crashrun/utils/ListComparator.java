package com.gamejam.crashrun.utils;

import java.util.Comparator;

import com.gamejam.crashrun.api.*;
import com.google.android.gms.maps.model.LatLng;

import android.location.Location;

public class ListComparator implements Comparator<OSMNode> {
	LatLng currentLocation;
	ListComparator(LatLng currentLocation){
		this.currentLocation = currentLocation;
	}
	public int compare(OSMNode obj1, OSMNode obj2) {

		Location myLocation = new Location("My Location");

		myLocation.setLatitude(currentLocation.latitude);
		myLocation.setLongitude(currentLocation.longitude);


		Location LocationA = new Location("point A");
		LocationA.setLatitude(obj1.getLat());
		LocationA.setLongitude(obj1.getLon());

		Double distanceA = (double) LocationA.distanceTo(myLocation);


		Location LocationB = new Location("point B");

		LocationB.setLatitude(obj2.getLat());
		LocationB.setLongitude(obj2.getLon());

		Double distanceB = (double) LocationB.distanceTo(myLocation);

		//int compare = distanceA.compareTo(distanceB);
		int compare = (int) Math.round(distanceA - distanceB);
		//Log.d("Amenities", "Compare result: "+ compare);
		return compare;
	}
}
