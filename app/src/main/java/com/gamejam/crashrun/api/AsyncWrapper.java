package com.gamejam.crashrun.api;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.android.maps.GeoPoint;

import android.os.AsyncTask;
import android.os.Handler;

public class AsyncWrapper extends AsyncTask<GeoPoint, Integer, List<OSMNode>>{
	List<OSMNode> Geolist;
	@Override
	protected List<OSMNode> doInBackground(GeoPoint... center) {
		// Looper.prepare();
		// TODO Auto-generated method stub
		// Fix error handling
		// Get list of nodes from API
			double lat = center[0].getLatitudeE6()/ 1E6;
			double lon = center[0].getLongitudeE6()/ 1E6;
	//		Log.d(MapActivity.tag,"Center: Lat: " + lat + " Long: " + lon);
			List<OSMNode> NodesList = null;
			// List<GeoPoint> GeoPointList = new ArrayList<GeoPoint>();
			//List <ManagedOverlayItem> allItem = new ArrayList<ManagedOverlayItem>();

			try {
				NodesList = OSMWrapperAPI.fetch(lon,lat,0.005);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return NodesList;

	}
	@Override
	protected void onPostExecute(List<OSMNode> list) {
		// async task finished
		
	//	Log.v(MapActivity.tag, "Progress Finished.");
		Geolist = list;
		Handler handler=new Handler();
		handler.postDelayed(runOnResult, 100);
	}
	private Runnable runOnResult = new Runnable() {
		   public void run() {
	//		Log.v(MapActivity.tag, "Adding to overlay");
	//		  MapFragment.addToOverlay(Geolist);
			  
		   }
		};

}
