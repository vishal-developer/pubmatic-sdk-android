package com.pubmatic.sdk.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.pubmatic.sdk.common.utils.CommonConstants;

public class LocationDetector extends Observable {

	// Location support
	private LocationManager locationManager = null;
	private LocationListener locationListener = null;
	private static LocationDetector self;
	private List<Observer> observers;
    private boolean changed;
    private Context context;
    private Location location;
    private final Object MUTEX= new Object();

    private static String TAG = LocationDetector.class.getName();

    private LocationDetector(Context context){
    	this.context = context;
        this.observers=new ArrayList<Observer>();
    }

    public static LocationDetector getInstance(Context context) {
    	if(self==null)
    		self = new LocationDetector(context);
    	return self;
    }

    @Override
    public void addObserver(Observer observer) {
    	super.addObserver(observer);

        if(observer == null) throw new NullPointerException("Null Observer");
        synchronized (MUTEX) {
        	if(!observers.contains(observer)) observers.add(observer);
        	if(!isLocationDetectionEnabled()) setLocationDetectionEnabled(true);
        }
    }

    @Override
    public synchronized void deleteObserver(Observer observer) {
    	super.deleteObserver(observer);
        synchronized (MUTEX) {
        	observers.remove(observer);
        	if(observers.isEmpty()) setLocationDetectionEnabled(false);
        }
    }

    @Override
    public synchronized void deleteObservers() {
    	super.deleteObservers();
    	synchronized (MUTEX) {
    		observers.clear();
        	setLocationDetectionEnabled(false);
        }
    }

    public void notifyObservers() {
        List<Observer> observersLocal = null;
        //synchronization is used to make sure any observer registered after message is received is not notified
        synchronized (MUTEX) {
            if (!changed)
                return;
            observersLocal = new ArrayList<Observer>(this.observers);
            this.changed=false;
        }
        for (Observer obj : observersLocal) {
            obj.update(this, location);///update();
        }

    }

    public Location getUpdate(Observer obj) {
    	if(observers.contains(obj))
    		return this.location;
    	else
    		return null;
    }


	/**
	 * Determines if location detection is enabled. If enabled, the SDK will use
	 * the location services of the device to determine the device's location ad
	 * add ad request parameters (lat/long) to the ad request. Location
	 * detection can be enabled with setLocationDetectionEnabled() or
	 * enableLocationDetection().
	 *
	 * @return true if location detection is enabled, false if not
	 */
	public boolean isLocationDetectionEnabled() {
		if (locationManager != null) {
			return true;
		}

		return false;
	}

	/**
	 * Enables or disable SDK location detection. If enabled with this method
	 * the most battery optimized settings are used. For more fine tuned control
	 * over location detection settings use enableLocationDetection(). This
	 * method is used to disable location detection for either method of
	 * enabling location detection.
	 * <p>
	 * Permissions for coarse or fine location detection may be required.
	 *
	 * @param locationDetectionEnabled
	 */
	@SuppressWarnings("ResourceType")
	public void setLocationDetectionEnabled(boolean locationDetectionEnabled) {
		if (locationDetectionEnabled == false) {
			if (locationManager != null && locationListener != null) {
				locationManager.removeUpdates(locationListener);
				locationManager = null;
				locationListener = null;
			}

			return;
		}

		Criteria criteria = new Criteria();
		criteria.setCostAllowed(false);

		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setAltitudeRequired(false);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);

		enableLocationDetection(CommonConstants.LOCATION_DETECTION_MINTIME,
				CommonConstants.LOCATION_DETECTION_MINDISTANCE, criteria, null);
	}

	/**
	 * Enables location detection with specified criteria. To disable location
	 * detection use setLocationDetectionEnabled(false).
	 *
	 * @param minTime
	 *            LocationManager.requestLocationUpdates minTime
	 * @param minDistance
	 *            LocationManager.requestLocationUpdates minDistance
	 * @param criteria
	 *            Criteria used to find an available provider. Ignored if
	 *            provider is non-null.
	 * @param provider
	 *            Named provider used by the LocationManager to obtain location
	 *            updates.
	 */
	@SuppressWarnings("ResourceType")
	public void enableLocationDetection(long minTime, float minDistance,
										Criteria criteria, String provider) {
		if ((provider == null) && (criteria == null))
			throw new IllegalArgumentException("criteria or provider required");

		locationManager = (LocationManager) this.context
				.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null) {
			try {
				if (provider == null) {
					provider = locationManager.getBestProvider(
							criteria, true);
				}

				if (provider != null) {
					locationListener = new LocationListener();
					locationManager.requestLocationUpdates(provider, minTime,
							minDistance, locationListener);
				}
			} catch (Exception ex) {
				Log.d(TAG, "Error requesting location updates.  Exception:" + ex);

				locationManager.removeUpdates(locationListener);
				locationManager = null;
				locationListener = null;
			}
		}
	}

	/**
	 * This class listens for the location update from android system
	 *
	 */
	private class LocationListener implements android.location.LocationListener {
		public LocationListener() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onLocationChanged(Location loc) {
			Log.d(TAG, "LocationListener.onLocationChanged location:"
							+ loc.toString());

			location = loc;
			changed	 =true;
	        notifyObservers();
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG,
					"LocationListener.onProviderDisabled provider:" + provider);
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(TAG, "LocationListener.onProviderEnabled provider:" + provider);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(TAG, "LocationListener.onStatusChanged provider:" + provider
					+ " status:" + String.valueOf(status));

			if (status == LocationProvider.AVAILABLE)
				return;

			location = null;

		}
	}

}
