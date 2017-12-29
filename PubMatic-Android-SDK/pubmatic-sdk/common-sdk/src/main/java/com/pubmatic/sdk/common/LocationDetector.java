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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;


public class LocationDetector {

	// Location support
	private LocationListener locationListener = null;
	private static LocationDetector self;
	private Context context;
    private int observerCount = 0;
    private Location location;
    private final Object MUTEX= new Object();

    private LocationDetector(Context context){
        this.context = context;
    }

    public static LocationDetector getInstance(Context context) {
    	if(self==null)
    		self = new LocationDetector(context);
    	return self;
    }

    /**
     * This method will start tal=king location updates from LocationManager when first caller
     * invoke this method. It does not maintain the object/instance of the caller.
     */
    public void registerForLocationUpdates() {

        synchronized (MUTEX) {

            // Check the count of the observer. It is required to stop the update if all observers
            // decativated.
            observerCount++;

            //Check if device location is enabled then start the update request
        	if(locationListener == null)
        	    requestLocationUpdate();

        }
    }

    /**
     * This method stops taking location updates from LocationManager when the number this method
     * invocation is equal to the number of registerForLocationUpdates() invokes.
     */
    public synchronized void unregisterForLocationUpdates() {

        synchronized (MUTEX) {
            if(observerCount>0) {
                --observerCount;
                if (observerCount == 0) {
                    removeLocationUpdate();
                }
            }
        }
    }

    /**
     * This method stops taking location updates from LocationManager irrespective of the number of
     * registerForLocationUpdates() invoked.
     */
    public synchronized void stopLocationUpdates() {

        observerCount = 0;
        removeLocationUpdate();
    }

    /**
     * Enables location detection with specified criteria. To disable location
     * detection use setLocationDetectionEnabled(false).
     *
     * @return
     */
    @SuppressWarnings({"ResourceType"})
    public Location getLocation() {

        if(this.location!=null)
            return this.location;

        if(context!=null) {
            try {

                LocationManager locationManager = (LocationManager) context
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

                        if (locationManager != null && hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
                        {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            {

                                int permissionCheck = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

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

                            if (locationManager != null && hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                    int permissionCheck = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

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
        } else {
            PMLogger.logEvent("Context is null, not fetching location.", PMLogger.PMLogLevel.Debug);
        }

        return location;
    }

    /**
     * Enables location detection with specified criteria. To disable location
     * detection use setLocationDetectionEnabled(false).
     * @return
     */
    @SuppressWarnings({"ResourceType"})
    private void requestLocationUpdate() {

        if(context!=null) {

            try {
                LocationManager locationManager = (LocationManager) context
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
                    } else if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // First get location from Network Provider
                        if (isNetworkEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                int permissionCheck = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, CommonConstants.LOCATION_DETECTION_MINTIME,
                                            CommonConstants.LOCATION_DETECTION_MINDISTANCE, locationListener);
                                }
                            } else {
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, CommonConstants.LOCATION_DETECTION_MINTIME,
                                        CommonConstants.LOCATION_DETECTION_MINDISTANCE, locationListener);
                            }
                        }
                        // if GPS Enabled get lat/long using GPS Services
                        else if (isGPSEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                int permissionCheck = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CommonConstants.LOCATION_DETECTION_MINTIME,
                                            CommonConstants.LOCATION_DETECTION_MINDISTANCE, locationListener);
                                }
                            } else {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CommonConstants.LOCATION_DETECTION_MINTIME,
                                        CommonConstants.LOCATION_DETECTION_MINDISTANCE, locationListener);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            PMLogger.logEvent("Context is null, not able to start location updates.", PMLogger.PMLogLevel.Debug);
        }
    }

    /**
     * returns true if app has Location permission
     * @param permission
     * @return
     */
    private boolean hasPermission(String permission)
    {
        if(context!=null) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
                if (info.requestedPermissions != null) {
                    for (String p : info.requestedPermissions) {
                        if (p.equals(permission)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            PMLogger.logEvent("Context is null, not able to check location permission.", PMLogger.PMLogLevel.Debug);
        }
        return false;
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
    private boolean isDeviceLocationEnabled() {
        if (context!=null && context.getSystemService(Context.LOCATION_SERVICE) != null)
            return true;
        else
            return false;
    }

    /**
     *
     */
    @SuppressWarnings({"ResourceType"})
    private void removeLocationUpdate()
    {
        if(context!=null) {
            LocationManager locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null && locationListener != null && hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                try{
                    locationManager.removeUpdates(locationListener);
                }
                catch (SecurityException se){

                }
                locationListener = null;
            }

        } else {
            PMLogger.logEvent("Context is null, not able to remove location updates.", PMLogger.PMLogLevel.Debug);
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
            PMLogger.logEvent("LocationListener.onLocationChanged location:" + loc.toString(),
                    PMLogger.PMLogLevel.Debug);

            location = loc;
        }

        @Override
        public void onProviderDisabled(String provider) {
            PMLogger.logEvent("LocationListener.onProviderDisabled provider:" + provider,
                    PMLogger.PMLogLevel.Debug);
        }

        @Override
        public void onProviderEnabled(String provider) {
            PMLogger.logEvent("LocationListener.onProviderEnabled provider:" + provider,
                    PMLogger.PMLogLevel.Debug);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            PMLogger.logEvent("LocationListener.onStatusChanged provider:" + provider + " status:" + String
                    .valueOf(status), PMLogger.PMLogLevel.Debug);

            if (status == LocationProvider.AVAILABLE)
                return;

            location = null;

        }
    }

}
