package com.gamejam.crashrun.game;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public class RandomPointProvider
{
	
	private static final String TAG = "CRASHRUN";
	double maxRange;
	public LatLng user;
	double nodeX = 0;
	double nodeY = 0;
    double toAdd;
	
	Geocoder gc;
	
	public enum Range
	{
		SHORT, MEDIUM, LONG
	}

    //increase range to a certain extent
    //decrease time avaliable simutaneously
    //generate orbs x distance away from each other
	
	public RandomPointProvider(LatLng location, Range range, Context c, Game game)
    {
        user = location;
        toAdd = (game.levelAdd(0)*1.0)/10000;
        if (toAdd >= 0.010) {
            toAdd = 0.01;
        }
        if (range == RandomPointProvider.Range.SHORT)
        {
            //add 1 per round until max
            maxRange = 0.002;
            maxRange += toAdd;
        }
        else if(range == RandomPointProvider.Range.MEDIUM)
        {
            //add 1 per round until max
            maxRange = 0.003;
            maxRange += toAdd;
        }
        else if(range == RandomPointProvider.Range.LONG)
        {
            //add 1 per round until max
            maxRange = 0.004;
            maxRange += toAdd;
        }
        Log.d("game max range", String.valueOf(maxRange));
        gc  = new Geocoder(c);
    }

//	public LatLng getRandomPoint(double maxRange){
//		double t = 2*Math.PI*Math.random();
//		double	u = (Math.random()+Math.random());
//		double	r;
//		if (u>1){
//			r = 2-u;
//		}else{
//			r = u;
//		}
//		double x = r*Math.cos(t);
//		double y = r*Math.sin(t);
//		
//		return new LatLng(x,y);

	public LatLng getRandomPoint()
	{
		if((int)(Math.random()*100) >=50)
		{
			nodeX = Math.random()*maxRange;
		}
		else
		{
			nodeX = Math.random()*-maxRange;
		}
		
		if((int)(Math.random()*100) >=50)
		{
			nodeY = Math.random()*maxRange*1/Math.cos(nodeX);
		}
		else
		{
			nodeY = Math.random()*-maxRange*1/Math.cos(nodeX);
		}
		
		return geocode(new LatLng(user.latitude + nodeX, user.longitude + nodeY));
	}
	
	public LatLng getTR()
	{
		return new LatLng(user.latitude + maxRange, user.longitude + maxRange);
	}
	
	public LatLng getBR()
	{
		return new LatLng(user.latitude + maxRange, user.longitude - maxRange);
	}
	
	public LatLng getTL()
	{
		return new LatLng(user.latitude - maxRange, user.longitude + maxRange);
	}
	
	public LatLng getBL()
	{
		return new LatLng(user.latitude - maxRange, user.longitude - maxRange);
	}
	
	public LatLng geocode(LatLng location)
	{
		List<Address> address;
		try
		{
			address = gc.getFromLocation(location.latitude, location.longitude, 1);
			return new LatLng(address.get(0).getLatitude(), address.get(0).getLongitude());
		}
		catch(Exception e)
		{
			return null;
			

		}	
	}
}
