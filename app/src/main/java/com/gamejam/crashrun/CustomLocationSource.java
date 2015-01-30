package com.gamejam.crashrun;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;


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
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
    }
    
    private onUpdateListener mCallback;
    public interface onUpdateListener {
        public void onUserLocationChange(LatLng loc);
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
        if (mListener != null) {
            mListener.onLocationChanged(location);
        }
		//mCallback.onUserLocationChange(new LatLng(location.getLatitude(), location.getLongitude()));
        /* ..and Animate camera to center on that location !
         * (the reason for we created this custom Location Source !) */
       // mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
