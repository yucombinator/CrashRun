package com.gamejam.crashrun.api;

import java.util.HashMap;
import java.util.Map;

import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * OSMNode: Object to contain information about an OpenStreetMap node.
 * 
 * @author Icechen1
 */
public class OSMNode implements Parcelable {
	
	private String id;
	
	private double lat;
	
	private double lon;
	
	private Location location;
	
	private final HashMap<String, String> tags;

	private String version;
	
	private String name;
	
	public OSMNode(String id, String latitude, String longitude, String version, HashMap<String, String> tags, String name){
		this.id = id;
		this.lat = Double.parseDouble(latitude);
		this.lon = Double.parseDouble(longitude);
		this.version = version;
		this.tags = tags;
		this.name = name;
		
		this.location = new Location(id);
		location.setLatitude(lat);
		location.setLongitude(lon);
	}
	
    private OSMNode(Parcel in) {
    	location = in.readParcelable(Location.class.getClassLoader());
    	id = in.readString();
        lat = in.readDouble();
        lon = in.readDouble(); 	
        Bundle map = in.readBundle(); 
        tags = (HashMap<String, String>) map.getSerializable("tags");
    	version = in.readString();
    	name = in.readString();
    }
    
	public String getId(){
		return id;
	}
	public double getLat(){
		return lat;
	}
	public double getLon(){
		return lon;
	}
	public double getDist(OSMNode Location){
		Location currentLocation = new Location("point B");

		currentLocation.setLatitude(Location.getLat());
		currentLocation.setLongitude(Location.getLon());
		
		double distance = location.distanceTo(currentLocation);

		return distance;
	}
	public Map<String, String> getTags(){
		return tags;
	}
	
	public String getTagsMatching(String key){
		if(tags.containsKey(key)){
			return tags.get(key);
		}
		return "N/A";
	}
	
	public String getVersion(){
		return version;
	}
	
	public String getName(){
		return name;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(location, flags);
    	out.writeString(id);
        out.writeDouble(lat);
        out.writeDouble(lon);    	
        Bundle map = new Bundle();
        map.putSerializable("tags",tags);
        out.writeBundle(map);    	
    	out.writeString(version);
    	out.writeString(name);
	}

	public static final Parcelable.Creator<OSMNode> CREATOR
	= new Parcelable.Creator<OSMNode>() {
		public OSMNode createFromParcel(Parcel in) {
			return new OSMNode(in);
		}

		public OSMNode[] newArray(int size) {
			return new OSMNode[size];
		}
	};

}
