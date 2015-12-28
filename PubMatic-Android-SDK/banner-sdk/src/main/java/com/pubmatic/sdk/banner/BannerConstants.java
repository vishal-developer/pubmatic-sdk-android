package com.pubmatic.sdk.banner;

public class BannerConstants {

    // 10 mins in ms
    public static final int LOCATION_DETECTION_MINTIME = 10 * 60 * 1000;
    public static final int LOCATION_DETECTION_MINDISTANCE = 20; // Meters

    // How much content is allowed after parsing out click url and image or text
    // content before
    // falling through and rendering as html vs. native rendering.
    public static final int DESCRIPTOR_THIRD_PARTY_VALIDATOR_LENGTH = 20;

    // Default injection HTML rich media ads.
    // IMPORTANT: This string have specific format specifiers (%s).
    // Improper modification to this string can cause ad rendering failures.
    public static final String RICHMEDIA_FORMAT = "<html><head><meta name=\"viewport\" content=\"user-scalable=0\"/><style>body{margin:0;padding:0;}</style><script type=\"text/javascript\">%s</script></head><body><div align=\"center\">%s</div></body></html>";

    public static final String ENCODING_UTF_8 = "UTF-8";
}

