package com.gamejam.crashrun;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gamejam.crashrun.game.Game;
import com.gamejam.crashrun.game.RandomPointProvider;
import com.gamejam.crashrun.rest.StepCounter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

@EFragment
public class ViewMapFragment extends Fragment implements GoogleMap.OnCameraChangeListener{
    Game game;
    private View roundUp;
    private WatchSync watchSync;
    int closestIndex;
    int closestIndex2;
    private double closestLat;
    private double closestLong;
    private String provider;


    public void make(Game game) {
        this.game = game;

    }
	public static final String TAG = "CRASHRUN";
	public Location userLocation;
	SupportMapFragment mMapFragment;
    private onCameraListener mCallback;
    GoogleMap mMap;
    LatLng last_location;
    //Location theTrueLastLocation;
    FragmentTransaction fragmentTransaction;
    CustomLocationSource customLocationSource;
    LatLng currentLocation;
    List<LatLng> orbs;
    List<LatLng> specialOrbs;
    boolean GENERATED = false;
    RelativeLayout RelativeLayout;





    /**
     * An interface to pass data to the host Activity.
     * @author Icechen1
     */
    public interface onCameraListener {
        public void onCameraLocationChange(LatLng loc);
        public void onMyLocationChange(Location location);
        public void onOrbGet(int i);
		public void onNewRound();
    }
    
    public static enum MapType{
    	Satellite, Hybrid, Map, Terrain
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	if (savedInstanceState != null){
    		try{
    			Log.d(MainActivity.TAG, "restoring last location");
    			last_location = savedInstanceState.getParcelable("last_location");
    		}catch(Exception e){

    		}
    	}
    	//Load Map Type
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String type;
        int maptype = GoogleMap.MAP_TYPE_NORMAL;
        try{
        	type = settings.getString("map_type", "normal");
        }catch (Exception e){
        	type = "normal";
        }
	    
        if(type.equals("satellite")){
        	maptype = GoogleMap.MAP_TYPE_SATELLITE;
        }else if (type.equals("normal")){
        	maptype = GoogleMap.MAP_TYPE_NORMAL;
        }else if (type.equals("terrain")){
        	maptype = GoogleMap.MAP_TYPE_TERRAIN;
        }else if (type.equals("hybrid")){
        	maptype = GoogleMap.MAP_TYPE_HYBRID;
        }
	    	    
    	GoogleMapOptions options = new GoogleMapOptions();
    	options.mapType(maptype) //TODO OPTIONS
    	.compassEnabled(true)
    	.rotateGesturesEnabled(true)
    	.scrollGesturesEnabled(true)
    	.tiltGesturesEnabled(true);
    	RelativeLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_map, container, false);
    	mMapFragment = (SupportMapFragment) getFragmentManager().findFragmentByTag("mapfragment");
    	//create the fragment
    	if (mMapFragment == null){
    		mMapFragment = SupportMapFragment.newInstance(options);
    		fragmentTransaction =
    				getFragmentManager().beginTransaction();
    		fragmentTransaction.add(R.id.map_container, mMapFragment,"mapfragment");
    		fragmentTransaction.commit();
    	}
    	
        // creates our custom LocationSource and initializes some of its members
    	customLocationSource = new CustomLocationSource(getActivity().getApplicationContext());
    	orbs = new ArrayList<LatLng>();
        specialOrbs = new ArrayList<LatLng>();
        roundUp = RelativeLayout.findViewById(R.id.roundup);
        roundUp.setVisibility(View.GONE);
        watchSync = WatchSync.newInstance(getActivity());
        watchSync.onStart();

    	return RelativeLayout; 
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	if (mMap != null) {
    		Log.d(MainActivity.TAG, "Saving current location");
    		savedInstanceState.putParcelable("last_location", mMap.getCameraPosition().target);
		 }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (onCameraListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onCameraListener");
        }
    }
	@Override
	public void onDetach(){
		super.onDetach();
		mMapFragment = (SupportMapFragment) getFragmentManager().findFragmentByTag("mapfragment");
        /* Disable the my-location layer (this causes our LocationSource to be automatically deactivated.) */
        mMap.setMyLocationEnabled(false);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.d(MainActivity.TAG, "onResume()");
        /* We query for the best Location Provider everytime this fragment is displayed
         * just in case a better provider might have become available since we last displayed it */
		customLocationSource.getBestAvailableProvider();
        
		mMap = ((SupportMapFragment) getFragmentManager().findFragmentByTag("mapfragment")).getMap();
		// Check if we were successful in obtaining the map.
		if (mMap != null) {
			// The Map is verified. It is now safe to manipulate the map.
			mMap.setOnCameraChangeListener(this);
			
			//mMap.setInfoWindowAdapter(new PopupAdapter(getActivity().getLayoutInflater()));
			
            // Replace the (default) location source of the my-location layer with our custom LocationSource
            mMap.setLocationSource(customLocationSource);
           // mMap.
            // Set default zoom
            // mMap.moveCamera(CameraUpdateFactory.zoomTo(15f));
            
		//	mMap.setOnInfoWindowClickListener(this);
			
			Log.d(MainActivity.TAG, "Setting mMap Options");

			//RESTORE ALL ITEMS
			if(last_location!= null){
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(last_location, 14));
			}
		
			
		 }
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		LatLng location = arg0.target;
		mCallback.onCameraLocationChange(location);
		//TODO DELETE NOW
		if(MainActivity.DEMO){
		currentLocation=location;
		}
			//TODO DELETE THIS
		//	mMap.clear();
		if (MainActivity.DEMO){
		addMarker(location,1);
		}	
		if(!MainActivity.paused && MainActivity.DEMO){
			checkForNearbyItems(location);

		}
		
	}
	public void checkForNearbyItems(){
		checkForNearbyItems(currentLocation);
	}
	
	public void checkForNearbyItems(LatLng location) {

        if(orbs.size() > 0){
			//List<LatLng> orbToRemove = new ArrayList<LatLng>();
			Log.d(TAG, "" + orbs.size());


            Location LocationUser = new Location("User");
            LocationUser.setLatitude(location.latitude);
            LocationUser.setLongitude(location.longitude);

            Location LocationOrb = new Location("Orb");

            LatLng orb = orbs.get(0);
            LocationOrb.setLatitude(orb.latitude);
            LocationOrb.setLongitude(orb.longitude);

            Double previousDistance = (double) LocationOrb.distanceTo(LocationUser);

            for (int i  = 0; i < orbs.size(); i++) {
                orb = orbs.get(i);

                LocationOrb.setLatitude(orb.latitude);
                LocationOrb.setLongitude(orb.longitude);

                Double distance = (double) LocationOrb.distanceTo(LocationUser);
                if (distance <= previousDistance) {
                    previousDistance = distance;
                    closestIndex = i;
                }
            }
            Double previousDistance2 = 1000000.0;
            if (specialOrbs.size() != 0) {
                orb = specialOrbs.get(0);
                LocationOrb.setLatitude(orb.latitude);
                LocationOrb.setLongitude(orb.longitude);
                previousDistance2 = (double) LocationOrb.distanceTo(LocationUser);

            }


            for (int i  = 0; i < specialOrbs.size(); i++) {
                orb = specialOrbs.get(i);
                LocationOrb.setLatitude(orb.latitude);
                LocationOrb.setLongitude(orb.longitude);

                Double distance = (double) LocationOrb.distanceTo(LocationUser);
                if (distance <= previousDistance2) {
                    previousDistance2 = distance;
                    closestIndex2 = i;
                }
            }

            Double closestLat;
            Double closestLong;
            if (previousDistance <= previousDistance2) {
                closestLat = orbs.get(closestIndex).latitude;
                closestLong = orbs.get(closestIndex).longitude;
            }
            else {
                closestLat = specialOrbs.get(closestIndex2).latitude;
                closestLong = specialOrbs.get(closestIndex2).longitude;
            }
            Log.d("SOME FKIN BUG",closestLat + " " + closestLong);
            watchSync.sendUpdate(location,new LatLng(closestLat,closestLong),null,null,(byte)0);
            Log.d("Closest Lat:", String.valueOf(closestLat));
            Log.d("Closest Long:", String.valueOf(closestLong));



        Iterator<LatLng> iter = orbs.iterator();
        Iterator<LatLng> iter2 = specialOrbs.iterator();
		while (iter.hasNext()){
			try{
			//TODO FIX TRY 	thing
			orb = iter.next();
			LocationOrb.setLatitude(orb.latitude);
			LocationOrb.setLongitude(orb.longitude);

			LocationUser.setLatitude(location.latitude);
			LocationUser.setLongitude(location.longitude);

			Double distance = (double) LocationOrb.distanceTo(LocationUser);

			if(distance < 30){
                watchSync.sendUpdate(location,new LatLng(closestLat,closestLong),null,null,(byte)1);
				if(MainActivity.DEMO){
				int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(getActivity().getApplicationContext(), "Close enough to orb", duration).show();

                }
                iter.remove();
				mCallback.onOrbGet(0);
				if(orbs.size() < 1){
                    game.levelAdd(1);
					game.newRound();
                    showRoundScreen();
                    mCallback.onNewRound();
                    orbs.clear();
                    specialOrbs.clear();
                    mMap.clear();
                    addOrbs();
                    Log.d(TAG, "Done!!");
					return;
				}
				//TODO VIBRATE
			}
			}catch(Exception e){
				
			}

		}

        while (iter2.hasNext()){
            try{
                //TODO FIX TRY 	thing
                LatLng orb2 = iter2.next();
                Location LocationOrb2 = new Location("Orb");
                LocationOrb2.setLatitude(orb2.latitude);
                LocationOrb2.setLongitude(orb2.longitude);

                LocationUser.setLatitude(location.latitude);
                LocationUser.setLongitude(location.longitude);

                Double distance = (double) LocationOrb2.distanceTo(LocationUser);

                if(distance < 15){

                    if(MainActivity.DEMO){
                        int duration = Toast.LENGTH_SHORT;
                        Toast.makeText(getActivity().getApplicationContext(), "Close enough to orb", duration).show();
                    }

                    iter2.remove();
                    mCallback.onOrbGet(2);
                    if(orbs.size() < 1){
                        game.levelAdd(1);
                        game.newRound();
                        showRoundScreen();
                        mCallback.onNewRound();
                        orbs.clear();
                        specialOrbs.clear();
                        mMap.clear();
                        addOrbs();

                        Log.d(TAG, "Done!!");
                        return;
                    }
                    //TODO VIBRATE
                }
            }catch(Exception e){

            }

        }
		//Re-add other pins
		mMap.clear();
		for(int i = 0; i < orbs.size(); i++)
		{
			addMarker(orbs.get(i),0);
			
		}
        for(int i = 0; i < specialOrbs.size(); i++)
        {
            addMarker(specialOrbs.get(i),2);

        }
	}
	}
	@Background
	public void generatePoint(LatLng location){
        if(provider == null || !provider.equals("gps")) return;
        int addMoreOrbs;
		orbs.clear();
        specialOrbs.clear();
		RandomPointProvider mRPP = new RandomPointProvider(location, RandomPointProvider.Range.SHORT,getActivity().getApplicationContext(),game);
		//addPoly(mRPP);
		//orbs = new ArrayList<LatLng>();
        addMoreOrbs = ((game.levelAdd(0)-1)/2);
        Log.d("game add more orbs", String.valueOf(4+ addMoreOrbs));
        if (addMoreOrbs > 10) {
            addMoreOrbs =10;
        }

        for(int i = 0; i < (4 + addMoreOrbs); i++)
        {
            LatLng point = mRPP.getRandomPoint();
            if(point != null){
                orbs.add(point);
                addMarker(orbs.get((orbs.size()-1)),0);
            }else{
                Log.d(TAG, "Error generating points");
            }
        }
        int specialOrbsAdded = 1+ game.levelAdd(0)/4;
        for(int i = 0; i < specialOrbsAdded; i++)
        {
            LatLng point = mRPP.getRandomPoint();
            if(point != null){
                specialOrbs.add(point);
                addMarker(specialOrbs.get((specialOrbs.size()-1)),2);
            }else{
                Log.d(TAG, "Error generating points");
            }
        }


    }
	
	@UiThread //later: make apparent boundary bigger than real boundary
	public void addPoly(RandomPointProvider mRPP){
		mMap.clear();
//		Polyline line = mMap.addPolyline(new PolylineOptions()
//	       .add(mRPP.getTR(), mRPP.getBR(), mRPP.getBL(), mRPP.getTL(), mRPP.getTR())
//	       .width(5)
//	       .color(Color.RED));
	}
	
	
	
	@Background
	public void addMarker(LatLng Node,int type){
		/*
		 * int 0 = orb
		 * int 1 = center
		 */
		mMap = ((SupportMapFragment) getFragmentManager().findFragmentByTag("mapfragment")).getMap();
		// Check if we were successful in obtaining the map.
		if (mMap != null) {
			if(type == 0){
			 MarkerOptions MarkerOptions = new MarkerOptions()
		       .position(Node)
		       .title("Orb")
		       .snippet("Go get it!")
		       .icon(BitmapDescriptorFactory.fromResource(R.drawable.heart));
			 UiAddMarker(MarkerOptions);
			    
			}

            if(type == 2){
                MarkerOptions MarkerOptions = new MarkerOptions()
                        .position(Node)
                        .title("Time Orb")
                        .snippet("+1 Minute")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.clock2));
                UiAddMarker(MarkerOptions);

            }
			if(type == 1){
				 MarkerOptions MarkerOptions = new MarkerOptions()
			       .position(Node)
			       .title("test")
			       .snippet("center")
			       .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
				 UiAddMarker(MarkerOptions);
				}
		 }
		 
	}
	@UiThread
	public void UiAddMarker(MarkerOptions MarkerOptions){
	mMap.addMarker(MarkerOptions);
	}
	@UiThread
	public void UiAddOverlay(GroundOverlayOptions GroundOverlayOptions){
	    mMap.addGroundOverlay(GroundOverlayOptions);
	}
	public void changeView(MapType type) {
		mMap = ((SupportMapFragment) getFragmentManager().findFragmentByTag("mapfragment")).getMap();
		// Check if we were successful in obtaining the map.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
    	SharedPreferences.Editor edit = settings.edit();
		if (mMap != null) {
			if (type == MapType.Satellite) {
				mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				//Save it
	    	    edit.putString("map_type", "satellite");
	       	    edit.commit();
			}
			if (type == MapType.Hybrid) {
				mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				//Save it
	    	    edit.putString("map_type", "hybrid");
	       	    edit.commit();
			}
			if (type == MapType.Map) {
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				//Save it
	    	    edit.putString("map_type", "normal");
	       	    edit.commit();
			}
			if (type == MapType.Terrain) {
				mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				//Save it
	    	    edit.putString("map_type", "terrain");
	       	    edit.commit();
			}
		 }
	}
/*
TODO Fix this

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG,"LocationChanged");
		mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(arg0.getLatitude(), arg0.getLongitude())));
		
	}
	*/
	@Background
	public void updateLocation(LatLng location)
	{
		currentLocation = location;
	}
	/* Our custom LocationSource. 
	 * We register this class to receive location updates from the Location Manager
	 * and for that reason we need to also implement the LocationListener interface. */
	public class CustomLocationSource implements LocationSource, LocationListener {

	    private OnLocationChangedListener mListener;
	    private LocationManager locationManager;
	    private final Criteria criteria = new Criteria();
	    private String bestAvailableProvider;
	    /* Updates are restricted to one every 10 seconds, and only when
	     * movement of more than 10 meters has been detected.*/
	    private final int minTime = 10000;     // minimum time interval between location updates, in milliseconds
	    private final int minDistance = 10;    // minimum distance between location updates, in meters

	    CustomLocationSource(Context mContext) {
	        // Get reference to Location Manager
	        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

	        // Specify Location Provider criteria
	        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
	        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
	        //criteria.setAltitudeRequired(true);
	        criteria.setBearingRequired(true);
	        //criteria.setSpeedRequired(true);
	        //criteria.setCostAllowed(true);
	    }

	    void getBestAvailableProvider() {
	        /* The preffered way of specifying the location provider (e.g. GPS, NETWORK) to use 
	         * is to ask the Location Manager for the one that best satisfies our criteria.
	         * By passing the 'true' boolean we ask for the best available (enabled) provider. */
	        bestAvailableProvider = locationManager.getBestProvider(criteria, true);
	    }

	    /* Activates this provider. This provider will notify the supplied listener
	     * periodically, until you call deactivate().
	     * This method is automatically invoked by enabling my-location layer. */
	    @Override
	    public void activate(OnLocationChangedListener listener) {
	        // We need to keep a reference to my-location layer's listener so we can push forward
	        // location updates to it when we receive them from Location Manager.
	        mListener = listener;

	        // Request location updates from Location Manager
	        if (bestAvailableProvider != null) {
	            locationManager.requestLocationUpdates(bestAvailableProvider, minTime, minDistance, this);
	        } else {
	            // (Display a message/dialog) No Location Providers currently available.
	        }
	    }

	    /* Deactivates this provider.
	     * This method is automatically invoked by disabling my-location layer. */
	    @Override
	    public void deactivate() {
	        // Remove location updates from Location Manager
	        locationManager.removeUpdates(this);

	        mListener = null;
	    }

	    @Override
	    public void onLocationChanged(Location location) {
	        /* Push location updates to the registered listener..
	         * (this ensures that my-location layer will set the blue dot at the new/received location) */
	    	Log.d(TAG, "Got "+ location.getProvider());
			last_location = new LatLng(location.getLatitude(), location.getLongitude());
            provider = location.getProvider();
	    	//Remove the loading splash screen
	    	if(location.getProvider().equals("gps")){
                LinearLayout waitingLayout = (LinearLayout) RelativeLayout.findViewById(R.id.waiting);
                waitingLayout.setVisibility(View.GONE);
            }

	        if (mListener != null) {
	            mListener.onLocationChanged(location);
	        }
			//mCallback.onUserLocationChange(new LatLng(location.getLatitude(), location.getLongitude()));
	        /* ..and Animate camera to center on that location !
	         * (the reason for we created this custom Location Source !) */
	        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),15.0f));
	        CameraPosition cameraPosition = new CameraPosition.Builder()
	        .target(last_location)      // Sets the center of the map to Mountain View
	        .zoom(17)                   // Sets the zoom
	      //  .bearing(90)                // Sets the orientation of the camera to east
	        .tilt(45)                   // Sets the tilt of the camera to 30 degrees
	        .build();                   // Creates a CameraPosition from the builder
	        //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			//mCallback.onCameraLocationChange(location);
			//last_location = location;
				
				//TODO DELETE THIS
			//	mMap.clear();
				//addMarker(arg0.target,1);		
				updateLocation(last_location);
				if(!MainActivity.paused){
				checkForNearbyItems(last_location);
				}
            //watchSync.sendUpdate(last_location,null,null,null,(byte)0);

	    }


		@Override
	    public void onStatusChanged(String s, int i, Bundle bundle) {
    		LinearLayout waitingLayout = (LinearLayout) RelativeLayout.findViewById(R.id.waiting);
	    	if (i == LocationProvider.AVAILABLE){
	    		waitingLayout.setVisibility(View.GONE);	
	    	}else{
	    		waitingLayout.setVisibility(View.VISIBLE);	
	    	}
	    }

	    @Override
	    public void onProviderEnabled(String s) {
    	//	LinearLayout waitingLayout = (LinearLayout) RelativeLayout.findViewById(R.id.waiting);
    	//	waitingLayout.setVisibility(View.GONE);
	    }

	    @Override
	    public void onProviderDisabled(String s) {
    		LinearLayout waitingLayout = (LinearLayout) RelativeLayout.findViewById(R.id.waiting);
    		waitingLayout.setVisibility(View.VISIBLE);	
	    }
	}
	public void addOrbs() {
		// TODO Auto-generated method stub
		generatePoint(last_location);
	}
	

    public void startGame() {
		// TODO Auto-generated method stub
        if(provider != null && provider.equals("gps")){
            game.newGame();
        }else{
            LinearLayout waitingLayout = (LinearLayout) RelativeLayout.findViewById(R.id.waiting);
            waitingLayout.setVisibility(View.VISIBLE);

            //retry
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startGame();
                }
            },1000);

        }
		if(orbs.size() < 1){
            game.newRound();
            showRoundScreen();
            mCallback.onNewRound();
            orbs.clear();
            specialOrbs.clear();
            mMap.clear();
            addOrbs();
        }
	}

    private void showRoundScreen() {
        ((TextView)roundUp.findViewById(R.id.roundup_text)).setText("Round " + game.levelAdd(0));

        if (game.levelAdd(0) != 1) {

            double[] stats = game.stats(0,  0, 0);
            double dist = stats[0];
            double steps = stats[1];
            double speed = stats[2];


            ((TextView)roundUp.findViewById(R.id.stats)).setText("Distance:   " + dist + " meters\n"
                    + "Steps:   " + steps + " steps\n" + "Speed:   " + (float)(speed)+ " m/s\n" +"Score:   " +String.valueOf(game.scoreAdd(0)));



        }


        roundUp.setVisibility(View.VISIBLE);
        //hide again
        Handler h = new Handler();
        h.postDelayed(new Runnable(){
            public void run() {
                roundUp.setVisibility(View.GONE);
            }}, 4000);
    }

    public void stopGame() {
        orbs.clear();
        specialOrbs.clear();
        mMap.clear();
    }
}
