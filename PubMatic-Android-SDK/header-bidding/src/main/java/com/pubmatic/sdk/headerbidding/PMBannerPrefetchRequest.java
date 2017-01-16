/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2016
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

import android.content.Context;
import android.text.TextUtils;

import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.AdvertisingIdClient;
import com.pubmatic.sdk.common.pubmatic.PUBDeviceInformation;
import com.pubmatic.sdk.common.pubmatic.PubMaticConstants;
import com.pubmatic.sdk.common.pubmatic.PubMaticUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PMBannerPrefetchRequest extends PubMaticBannerAdRequest {

    private List<PMBannerImpression> impressions;
    private Set<String> adSlotIdsHB;

    private PMBannerPrefetchRequest(Context context) {
        super(context);
        impressions = new ArrayList<>();
        adSlotIdsHB = new HashSet<>();
    }

    private PMBannerPrefetchRequest(Context context, String pubId, PMBannerImpression impression) {
        this(context);
        this.mPubId = pubId;

        if(impression.validate())
            impressions.add(impression);
    }

    private PMBannerPrefetchRequest(Context context, String pubId, List<PMBannerImpression> impressions) {
        this(context);
        this.mPubId = pubId;

        for(PMBannerImpression impression : impressions)
        {
            if(impression.validate())
                this.impressions.add(impression);
        }
    }

    public static PMBannerPrefetchRequest initHBRequestForImpression(Context context, String pubId, PMBannerImpression impression) {
        return new PMBannerPrefetchRequest(context, pubId, impression);
    }

    public static PMBannerPrefetchRequest initHBRequestForImpression(Context context, String pubId, List<PMBannerImpression> impressions) {
        return new PMBannerPrefetchRequest(context, pubId, impressions);
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

    public void createRequest(Context context) {
        setAdType(AD_TYPE.RICHMEDIA);
        setAdServerURL("http://hb.pubmatic.com/openrtb/24");
        setUpUrlParams();
        setupPostData();
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

        AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.refreshAdvertisingInfo(mContext);

        try
        {
            long randomNumber = (long) (Math.random() * 10000000000l);

            parentJsonObject.put("id", String.valueOf(randomNumber));
            parentJsonObject.put("at", 2);

            parentJsonObject.put("cur", getCurrencyJson());
            parentJsonObject.put("imp", getImpressionJson());
            parentJsonObject.put("app", getAppJson());
            parentJsonObject.put("device", getDeviceObject());

            if(!isDoNotTrack())
            {
                if(!AdvertisingIdClient.getLimitedAdTrackingState(mContext, false))
                    parentJsonObject.put("user", getUserJson());
            }

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

            if(mPaid)
                appJsonObject.put("paid", 1);
            else
                appJsonObject.put("paid", 0);

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

            if(isDoNotTrack()) {
                deviceJsonObject.put("dnt", 1);
            }
            else {

                AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.refreshAdvertisingInfo(mContext);

                if(!AdvertisingIdClient.getLimitedAdTrackingState(mContext, false))
                {
                    deviceJsonObject.put("dnt", 0);

                    if (isAndoridAidEnabled() && !TextUtils.isEmpty(adInfo.getId())) {
                        deviceJsonObject.put("ifa", adInfo.getId());
                    }
                }
                else
                    deviceJsonObject.put("dnt", 1);
            }

            String networkType = PubMaticUtils.getNetworkType(mContext);

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
            deviceJsonObject.put("ip", pubDeviceInformation.mDeviceIpAddress);
            deviceJsonObject.put("make", pubDeviceInformation.mDeviceMake);
            deviceJsonObject.put("model", pubDeviceInformation.mDeviceModel);
            deviceJsonObject.put("os", pubDeviceInformation.mDeviceOSName);
            deviceJsonObject.put("osv", pubDeviceInformation.mDeviceOSVersion);
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
            if(pubDeviceInformation.mDeviceCountryCode != null && !pubDeviceInformation.mDeviceCountryCode.equals(""))
                geoJsonObject.put("country", pubDeviceInformation.mDeviceCountryCode);

            if(getState() != null && !getState().equals(""))
                geoJsonObject.put("region", getState());

            if(getCity() != null && !getCity().equals(""))
                geoJsonObject.put("city", getCity());

            if(getDMA() != null && !getDMA().equals(""))
                geoJsonObject.put("metro", getDMA());

            if(getmZip() != null && !getmZip().equals(""))
                geoJsonObject.put("zip", getmZip());

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

        try
        {
            JSONObject extensionJsonObject = new JSONObject();

            JSONObject dmJsonObject = new JSONObject();

            dmJsonObject.put("rs", 1);
            dmJsonObject.put("a", "1");

            extensionJsonObject.put("dm", dmJsonObject);

            JSONObject asJsonObject = new JSONObject();

            PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation.getInstance(mContext);

            if(mAdType == AD_TYPE.TEXT)
                asJsonObject.put("adtype", String.valueOf(1));
            else if(mAdType == AD_TYPE.IMAGE)
                asJsonObject.put("adtype", String.valueOf(2));
            else if(mAdType == AD_TYPE.IMAGE_TEXT)
                asJsonObject.put("adtype", String.valueOf(3));
            else if(mAdType == AD_TYPE.RICHMEDIA)
                asJsonObject.put("adtype", String.valueOf(11));
            else if(mAdType == AD_TYPE.NATIVE)
                asJsonObject.put("adtype", String.valueOf(12));

            asJsonObject.put("pageURL", pubDeviceInformation.mPageURL);
            asJsonObject.put("kltstamp", pubDeviceInformation.mDeviceTimeStamp);

            double ranreq = Math.random();
            asJsonObject.put("ranreq", ranreq);

            asJsonObject.put("timezone", pubDeviceInformation.mDeviceTimeZone);
            asJsonObject.put("screenResolution", pubDeviceInformation.mDeviceScreenResolution);
            asJsonObject.put("adPosition", pubDeviceInformation.mAdPosition);
            asJsonObject.put("inIframe", String.valueOf(pubDeviceInformation.mInIframe));
            asJsonObject.put("adVisibility", String.valueOf(pubDeviceInformation.mAdVisibility));

            if(getAWT() == AWT_OPTION.DEFAULT)
                asJsonObject.put("awt", "0");
            else if(getAWT() == AWT_OPTION.WRAPPED_IN_IFRAME)
                asJsonObject.put("awt", "1");
            else if(getAWT() == AWT_OPTION.WRAPPED_IN_JS)
                asJsonObject.put("awt", "2");

            if(getPMZoneId() != null)
                asJsonObject.put("pmZoneId", getPMZoneId());

            if(!isDoNotTrack()) {
                AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.refreshAdvertisingInfo(mContext);

                if(!AdvertisingIdClient.getLimitedAdTrackingState(mContext, false)) {
                    if (isAndoridAidEnabled() && !TextUtils.isEmpty(adInfo.getId())) {
                        asJsonObject.put(PubMaticConstants.UDID_PARAM, adInfo.getId());
                        asJsonObject.put(PubMaticConstants.UDID_TYPE_PARAM, String.valueOf(9)); //9 - Android Advertising ID
                        asJsonObject.put(PubMaticConstants.UDID_HASH_PARAM, String.valueOf(0)); //0 - raw udid
                    } else if (mContext != null) {
                        //Send Android ID
                        asJsonObject.put(PubMaticConstants.UDID_PARAM, PubMaticUtils.sha1(PubMaticUtils.getUdidFromContext(mContext)));
                        asJsonObject.put(PubMaticConstants.UDID_TYPE_PARAM, String.valueOf(3)); //3 - Android ID
                        asJsonObject.put(PubMaticConstants.UDID_HASH_PARAM, String.valueOf(2)); //2 - SHA1
                    }

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
                }
            }

            if(getOrmmaComplianceLevel() >= 0)
                asJsonObject.put("ormma", String.valueOf(getOrmmaComplianceLevel()));

            if(mKeywordsList != null && !mKeywordsList.isEmpty())
                asJsonObject.put("keywords", getKeywordString());

            if(getIABCategory() != null && !getIABCategory().equals(""))
                asJsonObject.put("iabcat", getIABCategory());

            if(getAppCategory() != null && !getAppCategory().equals(""))
                asJsonObject.put("cat", getAppCategory());

            asJsonObject.put("api", URLEncoder.encode("3::4::5"));

            String networkType = PubMaticUtils.getNetworkType(mContext);
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
            if(getGender() != null && !getGender().equals(""))
                userJsonObject.put("gender", getGender());

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
            if(isCoppa())
                regsJsonObject.put("coppa", 1);
            else
                regsJsonObject.put("coppa", 0);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  regsJsonObject;
    }
}
