/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2014
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

package com.pubmatic.sdk.banner;

import android.text.TextUtils;


import com.pubmatic.sdk.common.AdResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used for parsing ad response JSON/XML from Mocean ad server.
 */
public class BannerAdDescriptor implements AdResponse.Renderable {


    /***
     * Parses an ad descriptor from a pull parser that is parked on the "ad"
     * start element. Returns the parser on the "ad" end element.
     *
     * If the result is null or an exception is thrown the parser may be parked
     * nested in the ad tag.
     *
     * @param parser
     * @return Parsed AdDescriptor or null if an error was encountered.
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static BannerAdDescriptor parseDescriptor(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        Map<String, String> adInfo = new HashMap<String, String>();
        ArrayList<String> impressionTrackers = new ArrayList<String>();
        ArrayList<String> clickTrackers = new ArrayList<String>();

        String adType = parser.getAttributeValue(null, "type");
        adInfo.put("type", adType);

        String subAdType = parser.getAttributeValue(null, "subtype");
        adInfo.put("subtype", subAdType);

        // read past start tag
        parser.next();

        // read and populate ad info
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = parser.getName();
            String value = null;

            if ((eventType == XmlPullParser.END_TAG) && ("ad".equals(name))) {
                // done with the ad descriptor
                break;
            } else if (eventType == XmlPullParser.START_TAG) {
                String subType = parser.getAttributeValue(null, "type");
                String width = parser.getAttributeValue(null, "width");
                String height = parser.getAttributeValue(null, "height");
                if (TextUtils.isEmpty(subType) == false) {
                    adInfo.put(name + "Type", subType);
                }
                if (TextUtils.isEmpty(width) == false && width != null) {
                    adInfo.put("width", width);
                }
                if (TextUtils.isEmpty(height) == false && height != null) {
                    adInfo.put("height", height);
                }

                parser.next();
                XmlPullParser mParser = parser;
                int newEventType = mParser.getEventType();
                if (name.equals("impressiontrackers")) {
                    String impT = mParser.getName();
                    if (impT.equalsIgnoreCase("impressiontracker")) {
                        while (newEventType != XmlPullParser.END_DOCUMENT) {
                            String valueIT = "";
                            String newName = mParser.getName();
                            if (newEventType == XmlPullParser.END_TAG
                                    && "impressiontrackers".equals(newName)) {
                                break;
                            } else if (eventType == XmlPullParser.START_TAG) {
                                if (mParser.getEventType() == XmlPullParser.TEXT) {
                                    valueIT = mParser.getText();
                                }
                                if (TextUtils.isEmpty(valueIT) == false) {
                                    impressionTrackers.add(valueIT);
                                }
                                mParser.next();
                                newEventType = mParser.getEventType();
                            }
                        }
                    }
                } else if (name.equals("clicktrackers")) {
                    String clkT = mParser.getName();
                    if (("clicktracker").equalsIgnoreCase(clkT)) {
                        newEventType = mParser.getEventType();
                        while (newEventType != XmlPullParser.END_DOCUMENT) {
                            String valueCT = "";
                            String newName = parser.getName();
                            if (newEventType == XmlPullParser.END_TAG
                                    && "clicktrackers".equals(newName)) {
                                break;
                            } else if (eventType == XmlPullParser.START_TAG) {
                                if (mParser.getEventType() == XmlPullParser.TEXT) {
                                    valueCT = mParser.getText();
                                }
                                if (TextUtils.isEmpty(valueCT) == false) {
                                    clickTrackers.add(valueCT);
                                }
                                mParser.next();
                                newEventType = mParser.getEventType();
                            }
                        }
                    }
                } else if (name.equals("mediation")) {
                    //TODO :: Need to implement in next phase.
                    //mediationData = parseMediation(mParser);
                } else {
                    if (parser.getEventType() == XmlPullParser.TEXT) {
                        value = parser.getText();
                    }
                    if (TextUtils.isEmpty(value) == false) {
                        adInfo.put(name, value);
                    }
                }
            }

            parser.next();
            eventType = parser.getEventType();
        }

        BannerAdDescriptor adDescriptor = new BannerAdDescriptor(adInfo);
        adDescriptor.setImpressionTrackers(impressionTrackers);
        adDescriptor.setClickTrackers(clickTrackers);
        // Mediation data will be available only in case of third-party
        // mediation response
        // adDescriptor.setMediationData(mediationData);
        return adDescriptor;
    }

    /**
     * Returns the value of the current XML tag.
     *
     * @param parser
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static String getXmlValue(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        if (parser.next() == XmlPullParser.TEXT) {
            String result = parser.getText();
            parser.nextTag();
            return result != null ? result.trim() : null;
        }
        return null;
    }

    /**
     * Caller should only call this method to point to the end tag of current
     * level of START_TAG and ignoring the next upcoming xml tags. It throws
     * IllegalStateException if the current event type is not START_TAG
     *
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static void seekToCurrentEndTag(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException(
                    "Current event of parser is not pointing to XmlPullParser.START_TAG");
        }
        int remainingTag = 1;
        while (remainingTag != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    remainingTag--;
                    break;
                case XmlPullParser.START_TAG:
                    remainingTag++;
                    break;
            }
        }

    }

    private final Map<String, String> adInfo;
    private ArrayList<String> mImpressionTrackers = new ArrayList<String>();
    private ArrayList<String> mClickTrackers = new ArrayList<String>();

    /*
     * This parameter will be used for Native Ads. It will be set when error
     * occurs while serving the Native ad and server returns a json with error.
     */
    private String errorMessage = null;

    BannerAdDescriptor() {
        this.adInfo = null;
    }

    public BannerAdDescriptor(Map<String, String> adInfo) {
        this.adInfo = adInfo;
    }

    /**
     * Set the ecpm of the Ad
     *
     * @param ecpm
     *            the ecpm to set
     */
    public void setEcpm(final String ecpm) {
        if(adInfo!=null)
            adInfo.put("ecpm", ecpm);
    }

    /**
     * Returns the ecpm of the Ad
     *
     * @return ecpm of the Ad
     */
    public String getEcpm() {
        String value = adInfo.get("ecpm");
        return value;
    }

    public String getType() {
        String value = adInfo.get("type");
        return value;
    }

    public String getWidth() {
        String value = adInfo.get("width");
        return value;
    }

    public String getHeight() {
        String value = adInfo.get("height");
        return value;
    }

    public String getSubType() {
        String value = adInfo.get("subtype");
        return value;
    }

    public String getURL() {
        String value = adInfo.get("url");
        return value;
    }

    public String getTrack() {
        String value = adInfo.get("track");
        return value;
    }

    public String getImage() {
        String value = adInfo.get("img");
        return value;
    }

    public String getImageType() {
        String value = adInfo.get("imgType");
        return value;
    }

    public String getText() {
        String value = adInfo.get("text");
        return value;
    }

    public String getContent() {
        String value = adInfo.get("content");
        return value;
    }

    public String getAdCreativeId() {
        String value = adInfo.get("creativeid");
        return value;
    }

    public ArrayList<String> getImpressionTrackers() {
        return mImpressionTrackers;
    }

    public void setImpressionTrackers(ArrayList<String> mImpressionTrackers) {
        this.mImpressionTrackers.clear();
        this.mImpressionTrackers = mImpressionTrackers;
    }

    /**
     * Get click trackers list is received from server
     *
     * @return List of click tracker URL's
     */
    public ArrayList<String> getClickTrackers() {
        return mClickTrackers;
    }

    /**
     * Set the list of click tracker url's
     *
     * @param clickTrackers
     */
    public void setClickTrackers(ArrayList<String> clickTrackers) {
        if (this.mClickTrackers != null) {
            this.mClickTrackers.clear();
        }
        this.mClickTrackers = clickTrackers;
    }

    /**
     * Returns the error message if an error occurs in case of Native Ad.
     *
     * @return - errorMessage
     */
    String getErrroMessage() {
        return errorMessage;
    }

    @Override
    public Object getRenderable() {
        return null;
    }
}