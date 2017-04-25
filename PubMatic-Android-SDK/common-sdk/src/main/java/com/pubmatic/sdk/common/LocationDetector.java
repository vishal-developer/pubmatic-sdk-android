/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2017
 * PubMatic, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * PubMatic. The intellectual and technical concepts contained herein are
 * proprietary to PubMatic and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from PubMatic.
 * Access to the source code contained herein is hereby forbidden to anyone
 * except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such
 * access.
 *
 * The copyright notice above does not evidence any actual or intended
 * publication or disclosure of this source code, which includes information
 * that is confidential and/or proprietary, and is a trade secret, of PubMatic.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC
 * DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN
 * CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE
 * CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO
 * REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR
 * SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.pubmatic.sdk.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

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
            //Add the observer if not already added
        	if(!observers.contains(observer)) observers.add(observer);

            //Check if device location is enabled then start the update request
        	if(isDeviceLocationEnabled() && locationListener == null) requestLocationUpdate();
        }
    }

    @Override
    public synchronized void deleteObserver(Observer observer) {
    	super.deleteObserver(observer);
        synchronized (MUTEX) {
        	observers.remove(observer);
        	if(observers.isEmpty()) removeLocationUpdate();
        }
    }

    @Override
    public synchronized void deleteObservers() {
    	super.deleteObservers();
    	synchronized (MUTEX) {
    		observers.clear();
        	removeLocationUpdate();;
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
	public boolean isDeviceLocationEnabled() {
		if (context!=null && context.getSystemService(Context.LOCATION_SERVICE) != null)
			return true;
        else
		    return false;
	}

    /**
     *
     */
    private void removeLocationUpdate()
    {
        if (locationManager != null && locationListener != null) {
            try{
                locationManager.removeUpdates(locationListener);
            }
            catch (SecurityException se){

            }

            locationManager = null;
            locationListener = null;
        }
    }

    /**
     * Enables location detection with specified criteria. To disable location
     * detection use setLocationDetectionEnabled(false).
     *
     * @return
     */
    public Location getLocation() {
        try {
            locationManager = (LocationManager) this.context
                    .getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {

                // First get location from Network Provider
                if (isNetworkEnabled) {

                    if (locationManager != null)
                    {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        {

                            int permissionCheck = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

                            if(permissionCheck == PackageManager.PERMISSION_GRANTED)
                            {
                                this.location = locationManager
                                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            }
                        }
                        else
                        {
                            this.location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                else if (isGPSEnabled) {
                    if (location == null) {

                        if (locationManager != null) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                int permissionCheck = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

                                if(permissionCheck == PackageManager.PERMISSION_GRANTED)
                                {
                                    this.location = locationManager
                                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                }
                            }
                            else
                            {
                                this.location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Enables location detection with specified criteria. To disable location
     * detection use setLocationDetectionEnabled(false).
     * @return
     */
    public Location requestLocationUpdate() {

        try {
            locationManager = (LocationManager) this.context
                    .getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                locationListener = new LocationListener();

                // getting GPS status
                boolean isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                boolean isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                } else
                {
                    // First get location from Network Provider
                    if (isNetworkEnabled)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        {
                            int permissionCheck = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

                            if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                            {
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, CommonConstants.LOCATION_DETECTION_MINTIME,
                                        CommonConstants.LOCATION_DETECTION_MINDISTANCE, locationListener);
                            }
                        }
                        else
                        {
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, CommonConstants.LOCATION_DETECTION_MINTIME,
                                    CommonConstants.LOCATION_DETECTION_MINDISTANCE, locationListener);
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    else if (isGPSEnabled)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        {
                            int permissionCheck = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

                            if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                            {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CommonConstants.LOCATION_DETECTION_MINTIME,
                                        CommonConstants.LOCATION_DETECTION_MINDISTANCE, locationListener);
                            }
                        }
                        else
                        {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CommonConstants.LOCATION_DETECTION_MINTIME,
                                    CommonConstants.LOCATION_DETECTION_MINDISTANCE, locationListener);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
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
            PMLogger.logEvent("LocationListener.onLocationChanged location:" + loc.toString(),
                              PMLogger.LogLevel.Debug);

			location = loc;
			changed	 =true;
	        notifyObservers();
		}

		@Override
		public void onProviderDisabled(String provider) {
            PMLogger.logEvent("LocationListener.onProviderDisabled provider:" + provider,
                              PMLogger.LogLevel.Debug);
		}

		@Override
		public void onProviderEnabled(String provider) {
            PMLogger.logEvent("LocationListener.onProviderEnabled provider:" + provider,
                              PMLogger.LogLevel.Debug);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
            PMLogger.logEvent("LocationListener.onStatusChanged provider:" + provider + " status:" + String
                    .valueOf(status), PMLogger.LogLevel.Debug);

			if (status == LocationProvider.AVAILABLE)
				return;

			location = null;

		}
	}

}
