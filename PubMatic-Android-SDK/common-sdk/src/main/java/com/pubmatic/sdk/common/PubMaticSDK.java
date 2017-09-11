package com.pubmatic.sdk.common;

/**
 * Class to provide configuration properties for PubMatic SDK
 */
public class PubMaticSDK {

    static boolean locationUpdateState = true;

    /**
     * Sets the log level of the instance. Logging is done through console logging.
     *
     * @param level LogLevel
     */
    public static void setLogLevel(PMLogger.LogLevel level) {
        PMLogger.setLogLevel(level);
    }

    /**
     * Returns the SDK version
     * @return
     */
    public static String getSDKVersion() {
        return CommonConstants.SDK_VERSION;
    }

    /**
     * Enables or disable SDK location detection. If enabled with this method the most battery
     * optimized settings are used. This method is used to disable location detection for either
     * method of enabling location detection. Default value is true.
     * <p/>
     * Permissions for coarse or fine location detection may be required.
     *
     * @param state
     */
    public static void setLocationDetectionEnabled(boolean state) {
        locationUpdateState = state;
    }

    /**
     * Determines if location detection is enabled. If enabled, the SDK will use the location
     * services of the device to determine the device's location ad add ad request parameters
     * (lat/long) to the ad request. Location detection can be enabled with
     * setLocationDetectionEnabled() or enableLocationDetection().
     *
     * @return true if location detection is enabled, false if not
     */
    public static boolean isLocationDetectionEnabled() {
        return locationUpdateState;
    }
}
