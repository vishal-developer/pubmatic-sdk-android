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

package com.pubmatic.sdk.headerbidding;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.pubmatic.sdk.banner.pubmatic.PMBannerAdRequest;
import com.pubmatic.sdk.common.AdvertisingIdClient;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.pubmatic.PUBDeviceInformation;
import com.pubmatic.sdk.common.pubmatic.PMConstants;
import com.pubmatic.sdk.common.PMUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.R.attr.width;
import static java.security.AccessController.getContext;

public class PMPrefetchRequest extends PMBannerAdRequest {

    private List<PMBannerImpression> impressions;
    private Set<String> adSlotIdsHB;


    private PMPrefetchRequest() {
        super();
        impressions = new ArrayList<>();
        adSlotIdsHB = new HashSet<>();
    }

    private PMPrefetchRequest(String pubId, PMBannerImpression impression) {
        this();
        this.mPubId = pubId;

        if(impression.validate())
            impressions.add(impression);
    }

    private PMPrefetchRequest(String pubId, List<PMBannerImpression> impressions) {
        this();
        this.mPubId = pubId;

        for(PMBannerImpression impression : impressions)
        {
            if(impression.validate())
                this.impressions.add(impression);
            else {
                PMLogger.logEvent("Invalid Impression found for this request, hence ignoring this impression.", PMLogger.PMLogLevel.Debug);
            }
        }
    }

    public static PMPrefetchRequest initHBRequestForImpression(String pubId, PMBannerImpression impression) {
        return new PMPrefetchRequest(pubId, impression);
    }

    public static PMPrefetchRequest initHBRequestForImpression(String pubId, List<PMBannerImpression> impressions) {
        return new PMPrefetchRequest(pubId, impressions);
    }

    public List<PMBannerImpression> getImpressions()
    {
        return impressions;
    }

    /**
     * Returns a new Set containing all the current adSlotIds participating in header bidding.
     */
    public Set<String> getAdSlotIdsHB() {
        HashSet<String> adSlotIdsCopySet = new HashSet<String>();
        if (adSlotIdsHB == null)
            adSlotIdsHB = new HashSet<String>();

        adSlotIdsCopySet.addAll(adSlotIdsHB);
        return adSlotIdsCopySet;
    }

    /**
     * Add a new adSlotId to compete for header bidding via PubMatic.
     *
     * @param adSlotId
     */
    public void addAdSlotIdsForHeaderBidding(String adSlotId) {
        adSlotIdsHB.add(adSlotId);
    }

    /**
     * Update the adSlotIds participating in Header bidding.
     * Note : This will remove all previous adSlotIds registered on this AdRequest instance.
     *
     * @param adSlotIds
     */
    private void setAdSlotIdsForHeaderBidding(Set<String> adSlotIds) {
        adSlotIdsHB.clear();
        adSlotIdsHB.addAll(adSlotIds);
    }

    /**
     * Unregister all adSlotIds participating in Header bidding.
     */
    public void clearAdSlotIdsForHeaderBidding() {
        adSlotIdsHB.clear();
    }

    void createRequest(Context context) {
        setContext(context);
        setAdType(AD_TYPE.BANNER);
        setAWT(AWT_OPTION.DEFAULT);
        setUpUrlParams();
        setupPostData();
    }

    @Override
    public String getAdServerURL() {
        return com.pubmatic.sdk.headerbidding.PMConstants.PUBMATIC_DM_SERVER_URL_PRODUCTION;
    }

    @Override
    protected void setUpUrlParams() {
        mUrlParams.clear();
    }

    @Override
    protected void setupPostData() {
        mPostData = new StringBuffer(getBody().toString());
    }

    private JSONObject getBody()
    {
        JSONObject parentJsonObject = new JSONObject();

        try
        {
            long randomNumber = (long) (Math.random() * 10000000000l);

            parentJsonObject.put("id", String.valueOf(randomNumber));
            parentJsonObject.put("at", 2);

            parentJsonObject.put("cur", getCurrencyJson());
            parentJsonObject.put("imp", getImpressionJson());
            parentJsonObject.put("app", getAppJson());
            parentJsonObject.put("device", getDeviceObject());
            parentJsonObject.put("user", getUserJson());

            parentJsonObject.put("regs", getRegsJson());
            parentJsonObject.put("ext", getExtJson());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return  parentJsonObject;
    }

    private JSONArray getCurrencyJson()
    {
        JSONArray currencyJsonArray = new JSONArray();

        currencyJsonArray.put("USD");

        return  currencyJsonArray;
    }

    private JSONArray getImpressionJson()
    {
        JSONArray impressionJsonArray = new JSONArray();

        for(PMBannerImpression impression : impressions)
        {
            JSONObject impressionJsonObject = new JSONObject();

            try
            {
                // impression - id
                impressionJsonObject.put("id", impression.getId());

                JSONObject bannerJsonObject = new JSONObject();

                if(impression.isInterstitial())
                    bannerJsonObject.put("pos", 7);
                else
                    bannerJsonObject.put("pos", 0);

                JSONArray formatJsonArray = new JSONArray();

                for(PMAdSize adSize : impression.getAdSizes())
                {
                    JSONObject adSizeJsonObject = new JSONObject();

                    adSizeJsonObject.put("w", adSize.getWidth());
                    adSizeJsonObject.put("h", adSize.getHeight());

                    formatJsonArray.put(adSizeJsonObject);
                }

                bannerJsonObject.put("format", formatJsonArray);

                JSONArray apiJsonArray = new JSONArray();
                apiJsonArray.put(3);
                apiJsonArray.put(4);
                apiJsonArray.put(5);

                bannerJsonObject.put("api", apiJsonArray);

                // impression - banner
                impressionJsonObject.put("banner", bannerJsonObject);

                if(impression.isInterstitial())
                    impressionJsonObject.put("instl", 1);

                JSONObject extJsonObject = new JSONObject();

                JSONObject extensionJsonObject = new JSONObject();

                extensionJsonObject.put("adunit", impression.getAdSlotId());
                extensionJsonObject.put("slotIndex", String.valueOf(impression.getAdSlotIndex()));

                extJsonObject.put("extension", extensionJsonObject);

                // impression - ext
                impressionJsonObject.put("ext", extJsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            impressionJsonArray.put(impressionJsonObject);
        }

        return impressionJsonArray;
    }

    private JSONObject getAppJson()
    {
        JSONObject appJsonObject = new JSONObject();

        PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation.getInstance(mContext);

        try
        {
            if(pubDeviceInformation.mApplicationName != null && !pubDeviceInformation.mApplicationName.equals(""))
                appJsonObject.put("name", pubDeviceInformation.mApplicationName);

            if(pubDeviceInformation.mPackageName != null && !pubDeviceInformation.mPackageName.equals(""))
                appJsonObject.put("bundle", pubDeviceInformation.mPackageName);

            if(getAppDomain() != null && !getAppDomain().equals(""))
                appJsonObject.put("domain", getAppDomain());

            if(getStoreURL() != null && !getStoreURL().equals(""))
                appJsonObject.put("storeurl", getStoreURL());

            if(getIABCategory() != null)
            {
                JSONArray iabCatJsonArray = new JSONArray();

                String[] iabCategories = getIABCategory().split(",");
                if(iabCategories != null && iabCategories.length > 0) {
                    for (int i = 0; i < iabCategories.length; i++)
                        iabCatJsonArray.put(iabCategories[i]);
                }

                appJsonObject.put("cat", iabCatJsonArray);
            }

            appJsonObject.put("ver", pubDeviceInformation.mApplicationVersion);

            if(mPaid!=null)
                appJsonObject.put("paid", mPaid ? 1 : 0);

            JSONObject publisherJsonObject = new JSONObject();
            publisherJsonObject.put("id", getPubId());

            appJsonObject.put("publisher", publisherJsonObject);
        }
        catch(JSONException jsonExcAppJson) {
            jsonExcAppJson.printStackTrace();
        }

        return appJsonObject;
    }

    private JSONObject getDeviceObject()
    {
        JSONObject deviceJsonObject = new JSONObject();

        PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation.getInstance(mContext);

        try
        {
            deviceJsonObject.put("geo", getGeoObject());

            //'lmt' specific parameter
            AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.refreshAdvertisingInfo(mContext);
            boolean lmtState = AdvertisingIdClient.getLimitedAdTrackingState(mContext, false);

            if(lmtState) {
                deviceJsonObject.put("lmt", 1);
                deviceJsonObject.put("dnt", 1);
            } else {
                deviceJsonObject.put("lmt", 0);
                deviceJsonObject.put("dnt", 0);
            }

            if (isAndroidAidEnabled() && adInfo!=null && !TextUtils.isEmpty(adInfo.getId())) {
                String advertisingId = adInfo.getId();
                switch (mHashing)
                {
                    case RAW:
                        deviceJsonObject.put("ifa", advertisingId);
                        break;
                    case SHA1:
                        deviceJsonObject.put("dpidsha1", PMUtils.sha1(advertisingId));
                        break;
                    case MD5:
                        deviceJsonObject.put("dpidmd5", PMUtils.md5(advertisingId));
                        break;
                }
            } else {
                String androidId = PMUtils.getUdidFromContext(mContext);

                switch (mHashing)
                {
                    case SHA1:
                        deviceJsonObject.put("dpidsha1", PMUtils.sha1(androidId));
                        break;
                    case MD5:
                        deviceJsonObject.put("dpidmd5", PMUtils.md5(androidId));
                        break;
                }
            }


            String networkType = PMUtils.getNetworkType(mContext);

            if(networkType != null && !networkType.equals(""))
            {
                if(networkType.equalsIgnoreCase("wifi"))
                    deviceJsonObject.put("connectiontype", 2);
                else if(networkType.equalsIgnoreCase("cellular"))
                    deviceJsonObject.put("connectiontype", 3);
            }

            if(pubDeviceInformation.mCarrierName != null && !pubDeviceInformation.mCarrierName.equals(""))
                deviceJsonObject.put("carrier", pubDeviceInformation.mCarrierName);

            deviceJsonObject.put("js", pubDeviceInformation.mJavaScriptSupport);

            deviceJsonObject.put("ua", getUserAgent());
            //DeviceIpAddress is deprecated.
            //deviceJsonObject.put("ip", pubDeviceInformation.mDeviceIpAddress);
            deviceJsonObject.put("make", pubDeviceInformation.mDeviceMake);
            deviceJsonObject.put("model", pubDeviceInformation.mDeviceModel);
            deviceJsonObject.put("os", pubDeviceInformation.mDeviceOSName);
            deviceJsonObject.put("osv", pubDeviceInformation.mDeviceOSVersion);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            (((Activity) mContext).getWindowManager()).getDefaultDisplay().getMetrics(displayMetrics);
            deviceJsonObject.put("w", displayMetrics.widthPixels);
            deviceJsonObject.put("h", displayMetrics.heightPixels);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return  deviceJsonObject;
    }

    private JSONObject getGeoObject()
    {
        JSONObject geoJsonObject = new JSONObject();

        PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation.getInstance(mContext);

        try
        {
            if(mLocation != null) {
                geoJsonObject.put("lat", mLocation.getLatitude());
                geoJsonObject.put("lon", mLocation.getLongitude());


                String provider = mLocation.getProvider();
                if(!TextUtils.isEmpty(provider) ) {
                    if (provider.equalsIgnoreCase("network") || provider.equalsIgnoreCase("wifi") || provider.equalsIgnoreCase("gps"))
                        geoJsonObject.put(PMConstants.LOCATION_TYPE, PMConstants.LOCATION_SOURCE_GPS_LOCATION_SERVICES);
                    else if (provider.equalsIgnoreCase("user"))
                        geoJsonObject.put(PMConstants.LOCATION_TYPE, PMConstants.LOCATION_SOURCE_USER_PROVIDED);
                    else
                        geoJsonObject.put(PMConstants.LOCATION_TYPE, PMConstants.LOCATION_SOURCE_UNKNOWN);
                }
            }

            if(pubDeviceInformation.mDeviceCountryCode != null && !pubDeviceInformation.mDeviceCountryCode.equals(""))
                geoJsonObject.put("country", pubDeviceInformation.mDeviceCountryCode);

            if(getState() != null && !getState().equals(""))
                geoJsonObject.put("region", getState());

            if(getCity() != null && !getCity().equals(""))
                geoJsonObject.put("city", getCity());

            if(getDMA() != null && !getDMA().equals(""))
                geoJsonObject.put("metro", getDMA());

            if(getZip() != null && !getZip().equals(""))
                geoJsonObject.put("zip", getZip());

        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return  geoJsonObject;
    }

    private JSONObject getExtJson()
    {
        JSONObject extJsonObject = new JSONObject();

        try {
            JSONObject extensionJsonObject = new JSONObject();

            JSONObject dmJsonObject = new JSONObject();

            dmJsonObject.put("rs", 1);
            dmJsonObject.put("a", "1");

            extensionJsonObject.put("dm", dmJsonObject);

            JSONObject asJsonObject = new JSONObject();

            PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation.getInstance(mContext);

            if(pubDeviceInformation!=null) {

                if (mAdType == AD_TYPE.TEXT)
                    asJsonObject.put("adtype", String.valueOf(1));
                else if (mAdType == AD_TYPE.IMAGE)
                    asJsonObject.put("adtype", String.valueOf(2));
                else if (mAdType == AD_TYPE.IMAGE_TEXT)
                    asJsonObject.put("adtype", String.valueOf(3));
                else if (mAdType == AD_TYPE.BANNER)
                    asJsonObject.put("adtype", String.valueOf(11));
                else if (mAdType == AD_TYPE.NATIVE)
                    asJsonObject.put("adtype", String.valueOf(12));

                asJsonObject.put("pageURL", pubDeviceInformation.mPageURL);
                asJsonObject.put("kltstamp", PMUtils.getCurrentTimeStamp());

                if (mLocation != null) {
                    String loc = mLocation.getLatitude() + "," + mLocation.getLongitude();
                    asJsonObject.put("loc", loc);
                }

                double ranreq = Math.random();
                asJsonObject.put("ranreq", ranreq);

                asJsonObject.put("msdkVersion", CommonConstants.SDK_VERSION);
                asJsonObject.put("timezone", pubDeviceInformation.mDeviceTimeZone);
                asJsonObject.put("screenResolution", pubDeviceInformation.mDeviceScreenResolution);
                asJsonObject.put("adPosition", pubDeviceInformation.mAdPosition);
                asJsonObject.put("inIframe", String.valueOf(pubDeviceInformation.mInIframe));
                asJsonObject.put("adVisibility", String.valueOf(pubDeviceInformation.mAdVisibility));
            }

            if(getAWT() == AWT_OPTION.DEFAULT)
                asJsonObject.put("awt", "0");
            else if(getAWT() == AWT_OPTION.WRAPPED_IN_IFRAME)
                asJsonObject.put("awt", "1");
            else if(getAWT() == AWT_OPTION.WRAPPED_IN_JS)
                asJsonObject.put("awt", "2");

            if(getPMZoneId() != null)
                asJsonObject.put("pmZoneId", getPMZoneId());

            // 'lmt' parameter specific
            AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.refreshAdvertisingInfo(mContext);
            if (isAndroidAidEnabled() && adInfo!=null && !TextUtils.isEmpty(adInfo.getId())) {

                String advertisingId = adInfo.getId();

                switch (mHashing)
                {
                    case RAW:
                        asJsonObject.put(PMConstants.UDID_PARAM, advertisingId);
                        asJsonObject.put(PMConstants.UDID_HASH_PARAM, com.pubmatic.sdk.headerbidding.PMConstants.HASHING_RAW);
                        break;
                    case SHA1:
                        asJsonObject.put(PMConstants.UDID_PARAM, PMUtils.sha1(advertisingId));
                        asJsonObject.put(PMConstants.UDID_HASH_PARAM, com.pubmatic.sdk.headerbidding.PMConstants.HASHING_SHA1);
                        break;
                    case MD5:
                        asJsonObject.put(PMConstants.UDID_PARAM, PMUtils.md5(advertisingId));
                        asJsonObject.put(PMConstants.UDID_HASH_PARAM, com.pubmatic.sdk.headerbidding.PMConstants.HASHING_MD5);
                        break;
                }

                asJsonObject.put(PMConstants.UDID_TYPE_PARAM, String.valueOf(com.pubmatic.sdk.headerbidding.PMConstants.ADVERTISEMENT_ID));
            } else {
                String androidId = PMUtils.getUdidFromContext(mContext);

                switch (mHashing) {
                    case RAW:
                        asJsonObject.put(PMConstants.UDID_PARAM, androidId);
                        asJsonObject.put(PMConstants.UDID_HASH_PARAM, com.pubmatic.sdk.headerbidding.PMConstants.HASHING_RAW);
                        break;
                    case SHA1:
                        asJsonObject.put(PMConstants.UDID_PARAM, PMUtils.sha1(androidId));
                        asJsonObject.put(PMConstants.UDID_HASH_PARAM, com.pubmatic.sdk.headerbidding.PMConstants.HASHING_SHA1);
                        break;
                    case MD5:
                        asJsonObject.put(PMConstants.UDID_PARAM, PMUtils.md5(androidId));
                        asJsonObject.put(PMConstants.UDID_HASH_PARAM, com.pubmatic.sdk.headerbidding.PMConstants.HASHING_MD5);
                        break;
                }
                asJsonObject.put(PMConstants.UDID_TYPE_PARAM, String.valueOf(com.pubmatic.sdk.headerbidding.PMConstants.ANDROID_ID));
            }
            //End of 'lmt' specific parameter

            if(getEthnicity() != null) {
                switch (getEthnicity()) {
                    case HISPANIC:
                        asJsonObject.put("ethn", "0");
                        break;
                    case AFRICAN_AMERICAN:
                        asJsonObject.put("ethn", "1");
                        break;
                    case CAUCASIAN:
                        asJsonObject.put("ethn", "2");
                        break;
                    case ASIAN_AMERICAN:
                        asJsonObject.put("ethn", "3");
                        break;
                    default:
                        asJsonObject.put("ethn", "4");
                        break;
                }
            }

            if(getIncome() != null && !getIncome().equals(""))
                asJsonObject.put("inc", getIncome());

            if(getOrmmaComplianceLevel() >= 0)
                asJsonObject.put("ormma", String.valueOf(getOrmmaComplianceLevel()));

            if(mKeywordsList != null && !mKeywordsList.isEmpty())
                asJsonObject.put("keywords", getKeywordString());

            if(getIABCategory() != null && !getIABCategory().equals(""))
                asJsonObject.put("iabcat", getIABCategory());

            if(getAppCategory() != null && !getAppCategory().equals(""))
                asJsonObject.put("cat", getAppCategory());

            asJsonObject.put("api", "3::4::5");

            String networkType = PMUtils.getNetworkType(mContext);
            asJsonObject.put("nettype", networkType);

            extensionJsonObject.put("as", asJsonObject);

            extJsonObject.put("extension", extensionJsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  extJsonObject;
    }

    private JSONObject getUserJson()
    {
        JSONObject userJsonObject = new JSONObject();

        try
        {
            if(getGender() != null) {
                switch (getGender()) {
                    case MALE:
                        userJsonObject.put(PMConstants.GENDER_PARAM, "M");
                        break;
                    case FEMALE:
                        userJsonObject.put(PMConstants.GENDER_PARAM, "F");
                        break;
                    case OTHER:
                        userJsonObject.put(PMConstants.GENDER_PARAM, "O");
                        break;
                    default:
                        break;
                }
            }

            if (getYearOfBirth() != null && !getYearOfBirth().equals(""))
                userJsonObject.put("yob", Integer.parseInt(getYearOfBirth()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  userJsonObject;
    }

    private JSONObject getRegsJson()
    {
        JSONObject regsJsonObject = new JSONObject();

        try
        {
            if(mCoppa!=null)
                regsJsonObject.put("coppa", mCoppa?1:0);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  regsJsonObject;
    }
}
