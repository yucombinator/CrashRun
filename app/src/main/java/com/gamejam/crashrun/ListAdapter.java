package com.gamejam.crashrun;

import java.util.ArrayList;

import com.gamejam.crashrun.api.*;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<OSMNode> {

	// declaring our ArrayList of items
	private ArrayList<OSMNode> objects;
	LatLng currentLocation;

	/* here we must override the constructor for ArrayAdapter
	* the only variable we care about now is ArrayList<Item> objects,
	* because it is the list of objects we want to display.
	*/
	public ListAdapter(Context context, int textViewResourceId, ArrayList<OSMNode> objects, LatLng currentLocation) {
		super(context, textViewResourceId, objects);
		this.objects = objects;
		this.currentLocation = currentLocation;
	}

	/*
	 * we are overriding the getView method here - this is what defines how each
	 * list item will look.
	 */
	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.listview_row, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		OSMNode i = objects.get(position);

		if (i != null) {

			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.

			TextView title = (TextView) v.findViewById(R.id.title);
			TextView distance = (TextView) v.findViewById(R.id.distance);
			ImageView arrow = (ImageView) v.findViewById(R.id.arrow);


			// check to see if each individual textview is null.
			// if not, assign some text!
			if (title != null){
				title.setText(i.getTags().get("type"));
			}
			if (distance != null){
				
				//Finds the distance to point
				Location myLocation = new Location("My Location");

				myLocation.setLatitude(currentLocation.latitude);
				myLocation.setLongitude(currentLocation.longitude);
				
				
				Location LocationA = new Location("point A");
				LocationA.setLatitude(i.getLat());
				LocationA.setLongitude(i.getLon());
				
				Double distanceA = (double) LocationA.distanceTo(myLocation);
				Double distanceB = (double) (Math.round(distanceA))/1000;
				
				//Write distance
				distance.setText(distanceB.toString() + " km"); //TODO Different units like miles
				//Write address
				
				float angle = LocationA.bearingTo(myLocation);
				BitmapDrawable arrowDrawable = rotateDrawable(angle);
				
				arrow.setImageBitmap(arrowDrawable.getBitmap());
				
				
			}
		}

		// the view must be returned to our activity
		return v;

	}
	public BitmapDrawable rotateDrawable(float angle)
	{
		//rotate image to the right bearing
	  Bitmap arrowBitmap = BitmapFactory.decodeResource(getContext().getResources(), 
	                                                    android.R.drawable.arrow_down_float);
	  // Create blank bitmap of equal size
	  Bitmap canvasBitmap = arrowBitmap.copy(Bitmap.Config.ARGB_8888, true);
	  canvasBitmap.eraseColor(0x00000000);

	  // Create canvas
	  Canvas canvas = new Canvas(canvasBitmap);

	  // Create rotation matrix
	  Matrix rotateMatrix = new Matrix();
	  rotateMatrix.setRotate(angle, canvas.getWidth()/2, canvas.getHeight()/2);

	  // Draw bitmap onto canvas using matrix
	  canvas.drawBitmap(arrowBitmap, rotateMatrix, null);

	  return new BitmapDrawable(getContext().getResources(),canvasBitmap); 
	  //return canvas;
	  //TODO update arrows when phone turns
	}

	
} 