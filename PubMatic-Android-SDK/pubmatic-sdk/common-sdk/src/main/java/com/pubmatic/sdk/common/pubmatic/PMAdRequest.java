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
package com.pubmatic.sdk.common.pubmatic;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.view.Surface;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdvertisingIdClient;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.PMUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * Abstract class for PubMatic (SSP) channel ad requests (e.g. PMBannerAdRequest, PMNativeAdRequest etc)
 */
public abstract class PMAdRequest extends AdRequest {

    protected int                           mOrmmaComplianceLevel;

    protected Boolean                       mPaid;
    protected Boolean			            mCoppa;

    protected String                        mPubId;
    protected String                        mSiteId;
    protected String                        mAdId;
    protected String                        mIABCategory;
    protected String                        mPMZoneId;
    protected String                        mStoreURL;
    protected String                        mAid;
    protected String                        mAppCategory;
    protected String                        mAppDomain;
    protected String                        mAdOrientation;
    protected String                        mCity = null;
    protected String                        mZip = null;
    protected String                        mDMA = null;

    protected AD_TYPE                       mAdType;
    protected AWT_OPTION                    mAWT = null;
    protected OPERID                        mOperId;
    protected ETHNICITY                     mEthnicity = null;
    protected GENDER                        mGender = null;
    protected HASHING_TECHNIQUE             mHashing = HASHING_TECHNIQUE.RAW;

    // User info
    protected String                        mState = null;
    protected String                        mYearOfBirth = null;
    protected String                        mIncome = null;
    protected ArrayList<String>             mKeywordsList = null;

    //---------------- protected enums to be used internally ------------------
    protected enum AD_TYPE { TEXT, IMAGE, IMAGE_TEXT, BANNER, NATIVE, VIDEO, AUDIO }

    protected enum OPERID { HTML, JAVA_SCRIPT, JSON, JSON_MOBILE }

    protected enum RS { PURE_JSON, JSON_CALLBACK, JS_VAR}

    //---------------- public enums to be used externally ------------------

    /**
     * Indicates whether the tracking URL has been wrapped or not in the creative tag.
     * Possible options are:
     * DEFAULT - Indicates that the tracking URL is sent separately in the response JSON as tracking_url. In this case, the tracking_url field is absent in the JSON response.
     * WRAPPED_IN_IFRAME - Indicates that the tracking_url value is wrapped in an Iframe and appended to the creative_tag.
     * WRAPPED_IN_JS - Indicates that the tracking_url value is wrapped in a JS tag and appended to the creative_tag.
     * Note: If the awt parameter is absent in the bid request URL, then it is same as awt=DEFAULT mentioned above.
     */
    protected enum AWT_OPTION { DEFAULT, WRAPPED_IN_IFRAME, WRAPPED_IN_JS }

    /**
     * The user’s ethnicity may be used to deliver more relevant ads. Code of ethnicity.
     * Possible options are:
     * HISPANIC, AFRICAN_AMERICAN, CAUCASIAN, ASIAN_AMERICAN, OTHER
     */
    public enum ETHNICITY { HISPANIC, AFRICAN_AMERICAN, CAUCASIAN, ASIAN_AMERICAN, OTHER }

    public enum GENDER { MALE, FEMALE, OTHER }

    public enum HASHING_TECHNIQUE {
        SHA1, MD5, RAW
    }

    //---------------- protected methods to be used internally ------------------
    protected PMAdRequest() {
        super(CommonConstants.CHANNEL.PUBMATIC);

    //  mDefaultedCampaignList = new ArrayList<>(0);
    }

    @Override
    protected void setUpUrlParams() {
        super.setUpUrlParams();
    }

    protected void setUpPostParams() {

        super.setupPostData();
        PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation.getInstance(mContext);

        putPostData(PMConstants.PUB_ID_PARAM, mPubId);
        putPostData(PMConstants.SITE_ID_PARAM, String.valueOf(mSiteId));
        putPostData(PMConstants.AD_ID_PARAM, String.valueOf(mAdId));

        if(mOperId == OPERID.HTML)
            putPostData(PMConstants.OPER_ID_PARAM, String.valueOf(1));
        else if(mOperId == OPERID.JAVA_SCRIPT)
            putPostData(PMConstants.OPER_ID_PARAM, String.valueOf(3));
        else if(mOperId == OPERID.JSON)
            putPostData(PMConstants.OPER_ID_PARAM, String.valueOf(102));
        else if(mOperId == OPERID.JSON_MOBILE)
            putPostData(PMConstants.OPER_ID_PARAM, String.valueOf(201));

        if(mAdType == AD_TYPE.TEXT)
            putPostData(PMConstants.AD_TYPE_PARAM, String.valueOf(1));
        else if(mAdType == AD_TYPE.IMAGE)
            putPostData(PMConstants.AD_TYPE_PARAM, String.valueOf(2));
        else if(mAdType == AD_TYPE.IMAGE_TEXT)
            putPostData(PMConstants.AD_TYPE_PARAM, String.valueOf(3));
        else if(mAdType == AD_TYPE.BANNER)
            putPostData(PMConstants.AD_TYPE_PARAM, String.valueOf(11));
        else if(mAdType == AD_TYPE.NATIVE)
            putPostData(PMConstants.AD_TYPE_PARAM, String.valueOf(12));
        else if(mAdType == AD_TYPE.VIDEO)
            putPostData(PMConstants.AD_TYPE_PARAM, String.valueOf(13));
        else if(mAdType == AD_TYPE.AUDIO)
            putPostData(PMConstants.AD_TYPE_PARAM, String.valueOf(14));

        putPostData(PMConstants.AD_POSITION_PARAM, String.valueOf(PUBDeviceInformation.mAdPosition));
        putPostData(PMConstants.IN_IFRAME_PARAM, String.valueOf(PUBDeviceInformation.mInIframe));
        putPostData(PMConstants.AD_VISIBILITY_PARAM, String.valueOf(PUBDeviceInformation.mAdVisibility));
        putPostData(PMConstants.APP_CATEGORY_PARAM, mAppCategory);
        putPostData(PMConstants.NETWORK_TYPE_PARAM, PMUtils.getNetworkType(mContext));
        if(mCoppa!=null)
            putPostData(PMConstants.COPPA_PARAM, String.valueOf(mCoppa ? 1 : 0));
        if(mPaid!=null)
            putPostData(PMConstants.PAID_PARAM, String.valueOf(mPaid ? 1 : 0));
        putPostData(PMConstants.APP_DOMAIN_PARAM, mAppDomain);
        putPostData(PMConstants.STORE_URL_PARAM, mStoreURL);
        putPostData(PMConstants.APP_ID_PARAM, mAid);
        putPostData(PMConstants.JS_PARAM, String.valueOf(PUBDeviceInformation.mJavaScriptSupport));
        putPostData(PMConstants.DEVICE_ORIENTATION_PARAM, String.valueOf(getDeviceOrientation(mContext)));
        putPostData(PMConstants.API_PARAM, "3::4::5");

        if(getOrmmaComplianceLevel() >= 0)
            putPostData(PMConstants.ORMMA_COMPLAINCE_PARAM, String.valueOf(getOrmmaComplianceLevel()));

        if (pubDeviceInformation.mCarrierName != null) {
            putPostData(PMConstants.CARRIER_PARAM, pubDeviceInformation.mCarrierName);
        }

        if (pubDeviceInformation.mDeviceMake != null) {
            putPostData(PMConstants.MAKE_PARAM, pubDeviceInformation.mDeviceMake);
        }

        if (pubDeviceInformation.mDeviceModel != null) {
            putPostData(PMConstants.MODEL_PARAM, pubDeviceInformation.mDeviceModel);
        }

        if (pubDeviceInformation.mDeviceOSName != null) {
            putPostData(PMConstants.OS_PARAM, pubDeviceInformation.mDeviceOSName);
        }

        if (pubDeviceInformation.mDeviceOSVersion != null) {
            putPostData(PMConstants.OSV_PARAM, pubDeviceInformation.mDeviceOSVersion);
        }

        if (!TextUtils.isEmpty(mYearOfBirth)) {
            putPostData(PMConstants.YOB_PARAM, mYearOfBirth);
        }

        if(getGender() != null) {
            switch (getGender()) {
                case MALE:
                    putPostData(PMConstants.GENDER_PARAM, "M");
                    break;
                case FEMALE:
                    putPostData(PMConstants.GENDER_PARAM, "F");
                    break;
                case OTHER:
                    putPostData(PMConstants.GENDER_PARAM, "O");
                    break;
                default:
                    putPostData(PMConstants.GENDER_PARAM, "O");
                    break;
            }
        }

        //Set the location
        if (mLocation != null) {
            putPostData(PMConstants.LOC_PARAM, mLocation.getLatitude() + ","
                    + mLocation.getLongitude());

            String provider = mLocation.getProvider();

            if(provider.equalsIgnoreCase("network") || provider.equalsIgnoreCase("wifi") || provider.equalsIgnoreCase("gps"))
                putPostData(PMConstants.LOC_SOURCE_PARAM, String.valueOf(PMConstants.LOCATION_SOURCE_GPS_LOCATION_SERVICES));
            else if(provider.equalsIgnoreCase("user"))
                putPostData(PMConstants.LOC_SOURCE_PARAM, String.valueOf(PMConstants.LOCATION_SOURCE_USER_PROVIDED));
            else
                putPostData(PMConstants.LOC_SOURCE_PARAM, String.valueOf(PMConstants.LOCATION_SOURCE_UNKNOWN));
        }

        try {

            try
            {
                if (!TextUtils.isEmpty(mAdOrientation))
                    putPostData(PMConstants.AD_ORIENTATION_PARAM, mAdOrientation);

                if (!TextUtils.isEmpty(mState)) {
                    putPostData(PMConstants.USER_STATE, mState);
                }

                if (!TextUtils.isEmpty(mCity)) {
                    putPostData(PMConstants.USER_CITY, mCity);
                }

                if (!TextUtils.isEmpty(mZip)) {
                    putPostData(PMConstants.ZIP_PARAM, mZip);
                }
            }
            catch(Exception exception) {}

            if (pubDeviceInformation.mDeviceCountryCode != null) {
                putPostData(PMConstants.COUNTRY_PARAM, pubDeviceInformation.mDeviceCountryCode);
            }

            if (pubDeviceInformation.mPageURL != null) {
                putPostData(PMConstants.PAGE_URL_PARAM, pubDeviceInformation.mPageURL);
            }

            if (pubDeviceInformation.mApplicationName != null) {
                putPostData(PMConstants.APP_NAME_PARAM, pubDeviceInformation.mApplicationName);
            }

            if (pubDeviceInformation.mPackageName != null) {
                putPostData(PMConstants.APP_BUNDLE_PARAM, pubDeviceInformation.mPackageName);
            }

            if (pubDeviceInformation.mApplicationVersion != null) {
                putPostData(PMConstants.APP_VERSION_PARAM, pubDeviceInformation.mApplicationVersion);
            }

            if (mAWT != null) {
                switch (mAWT) {
                    case WRAPPED_IN_IFRAME:
                        putPostData(PMConstants.AWT_PARAM, String.valueOf(1));
                        break;
                    case WRAPPED_IN_JS:
                        putPostData(PMConstants.AWT_PARAM, String.valueOf(2));
                        break;
                    case DEFAULT:
                        putPostData(PMConstants.AWT_PARAM, String.valueOf(0));
                        break;
                }
            }

            //'lmt' parameter case. Do not put refreshAdvertisingInfo() in conditional case.
            // It should be invoked common for both case.
            AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.refreshAdvertisingInfo(mContext);
            boolean lmtState = AdvertisingIdClient.getLimitedAdTrackingState(mContext, false);
            if(lmtState) {
                putPostData(PMConstants.LMT_PARAM, String.valueOf(1));
                putPostData(PMConstants.DNT_PARAM, String.valueOf(1));
            } else {
                putPostData(PMConstants.LMT_PARAM, String.valueOf(0));
                putPostData(PMConstants.DNT_PARAM, String.valueOf(0));
            }

            //Send advertising id if setAndroidAidEnabled(true) else send android id
            if(isAndroidAidEnabled() && adInfo != null && !TextUtils.isEmpty(adInfo.getId())) {

                String advertisingId = adInfo.getId();

                switch (mHashing) {
                    case RAW:
                        putPostData(PMConstants.UDID_PARAM, advertisingId);
                        putPostData(PMConstants.UDID_HASH_PARAM, PMConstants.HASHING_RAW);
                        break;
                    case SHA1:
                        putPostData(PMConstants.UDID_PARAM, PMUtils.sha1(advertisingId));
                        putPostData(PMConstants.UDID_HASH_PARAM, PMConstants.HASHING_SHA1);
                        break;
                    case MD5:
                        putPostData(PMConstants.UDID_PARAM, PMUtils.md5(advertisingId));
                        putPostData(PMConstants.UDID_HASH_PARAM, PMConstants.HASHING_MD5);
                        break;
                }

                putPostData(PMConstants.UDID_TYPE_PARAM, PMConstants.ADVERTISEMENT_ID);

            } else
                putDeviceIDToAdRequest();

            if (getEthnicity() != null) {
                switch (getEthnicity()) {
                    case HISPANIC:
                        putPostData(PMConstants.USER_ETHNICITY, "0");
                        break;
                    case AFRICAN_AMERICAN:
                        putPostData(PMConstants.USER_ETHNICITY, "1");
                        break;
                    case CAUCASIAN:
                        putPostData(PMConstants.USER_ETHNICITY, "2");
                        break;
                    case ASIAN_AMERICAN:
                        putPostData(PMConstants.USER_ETHNICITY, "3");
                        break;
                    default:
                        putPostData(PMConstants.USER_ETHNICITY, "4");
                        break;
                }

            }

            if (!TextUtils.isEmpty(mIncome)) {
                putPostData(PMConstants.USER_INCOME, mIncome);
            }

            // Setting the iab category
            if (!TextUtils.isEmpty(mIABCategory)) {
                putPostData(PMConstants.IAB_CATEGORY, mIABCategory);
            }

            // Setting the DMA
            if (!TextUtils.isEmpty(mDMA)) {
                putPostData(PMConstants.DMA, mDMA);
            }

            if (mKeywordsList != null) {
                putPostData(PMConstants.KEYWORDS_PARAM, getKeywordString());
            }

            // Setting sdk_ver
            putPostData(PMConstants.SDK_VER_PARAM, PUBDeviceInformation.msdkVersion);

            //pmZoneId
            putPostData("pmZoneId",getPMZoneId());


            // Send KAdNetwork id if any
//            if(mKAdNetworkId != null && !mKAdNetworkId.equals(""))
//                putPostData(CommonConstants.PASSBACK_KAD_NETWORK, mKAdNetworkId);
//
//             Send last defaulted network id if any
//            if(mLastDefaultedNetworkId != null && !mLastDefaultedNetworkId.equals(""))
//                putPostData(CommonConstants.PASSBACK_LAST_DEFAULTED_NETWORK, mLastDefaultedNetworkId);
//
//             //Send defaulted campaign list
//            if(mDefaultedCampaignList.size() > 0)
//            {
//                String campaignIds = "";
//
//                for(String campaignId : mDefaultedCampaignList)
//                    campaignIds = campaignIds + campaignId + ",";
//
//                campaignIds = campaignIds.substring(0, campaignIds.length()-1);
//
//                putPostData(CommonConstants.PASSBACK_CAMPAIGNS, campaignIds);
//
//            }

            //Append custom parameters

            //------ Set the custom key=value pair ------
            if(mCustomParams!=null && !mCustomParams.isEmpty()) {
                Set<String> set = mCustomParams.keySet();
                Iterator<String> iterator = set.iterator();
                StringBuffer customPair = null;

                while(iterator.hasNext()) {
                    String key = iterator.next();

                    //Append key
                    if(customPair==null)
                        customPair = new StringBuffer();
                    else {
                        customPair.append("|");
                    }
                    customPair.append(key+"=");


                    List<String> valueList = mCustomParams.get(key);

                    int index = 0;
                    for(String s : valueList) {

                        if(index>0){
                            customPair.append(",");
                        }
                        customPair.append(s);
                        index++;
                    }
                }
                putPostData(PMConstants.SSP_CUSTOM_KEY,customPair.toString());
            }

        } catch (Exception e) {

        }
    }

    private void putDeviceIDToAdRequest() {
        String androidId = PMUtils.getUdidFromContext(mContext);

        switch (mHashing)
        {
            case RAW:
                putPostData(PMConstants.UDID_PARAM, androidId);
                putPostData(PMConstants.UDID_HASH_PARAM, PMConstants.HASHING_RAW);
                break;
            case SHA1:
                putPostData(PMConstants.UDID_PARAM, PMUtils.sha1(androidId));
                putPostData(PMConstants.UDID_HASH_PARAM, PMConstants.HASHING_SHA1);
                break;
            case MD5:
                putPostData(PMConstants.UDID_PARAM, PMUtils.md5(androidId));
                putPostData(PMConstants.UDID_HASH_PARAM, PMConstants.HASHING_MD5);
                break;
        }

        putPostData(PMConstants.UDID_TYPE_PARAM, PMConstants.ANDROID_ID);
    }

    protected AD_TYPE getAdType() {
        return mAdType;
    }

    protected void setAdType(AD_TYPE mAdType) {
        this.mAdType = mAdType;
    }

    protected AWT_OPTION getAWT() {
        return mAWT;
    }

    protected void setAWT(AWT_OPTION mAWT) {
        this.mAWT = mAWT;
    }

    /**
     * AdTagId is associated with the Banner Ad Size. You have to create the
     * AdTagId for recommended size.
     *
     * Before loading the Ad's you have to set the respective AdTagId for given
     * Banner.
     *
     * @param adTagId
     *            adTagId to set
     */
    protected void setAdId(String adTagId) {
        mAdId = adTagId;
    }

    /**
     * You have to set the site id before making call to loadAd.
     *
     * @param siteId
     *            siteId to set
     */
    protected void setSiteId(String siteId) {
        mSiteId = siteId;
    }

    /**
     * You have to set the Publisher id before making a call to loadAd.
     *
     * @param publisherId
     *            publisherId The publisher id provided by PubMatic.
     */
    protected void setPubId(String publisherId) {
        mPubId = publisherId;
    }

    protected OPERID getOperId() {
        return mOperId;
    }

    protected void setOperId(OPERID operId) {
        this.mOperId = operId;
    }

    protected String getDMA() {
        return mDMA;
    }

    //---------------- public methods can be called externally ------------------
    public HASHING_TECHNIQUE getUdidHash() {
        return mHashing;
    }

    /**
     * Set type of algorithm used for hashing the device identifier provided in the udid parameter mentioned above.
     * Possible values are:
     * Unknown
     * Raw
     * SHA1
     * MD5
     * Note: This parameter is mandatory for only Mobile Applications
     * @param hashing type
     */
    public void setUdidHash(HASHING_TECHNIQUE hashing) {
        this.mHashing = hashing;
    }

    @Override
    public String getAdServerURL() {
        return CommonConstants.PUBMATIC_AD_NETWORK_URL;
    }

    @Override
    public boolean checkMandatoryParams() {
        return !TextUtils.isEmpty(mPubId) && !TextUtils.isEmpty(mSiteId) && !TextUtils.isEmpty(mAdId);
    }

    /**
     * Set home zip code if the user is present in the U.S.; otherwise it indicates the postal code
     *
     * @param zip
     *            zip of the user
     */
    public void setZip(final String zip) {
        mZip = zip;
    }

    public String getZip() {
        return mZip;
    }

    /**
     * Designated market area (DMA) code of the user. This field is applicable
     * for US users only. For example, dma=734
     * @param mDMA
     */
    public void setDMA(String mDMA) {
        this.mDMA = mDMA;
    }

    /**
     *
     * @return Returns city parameter
     */
    public String getCity() {
        return mCity;
    }

    /**
     * Sets city of the user.
     * @param city User's city
     */
    public void setCity(String city) {
        this.mCity = city;
    }

    public GENDER getGender() {
        return mGender;
    }

    /**
     * Set Gender of the user to deliver more relevant ads. Possible values are:
     * Male
     * Female
     * Others
     * @param gender
     */
    public void setGender(GENDER gender) {
        this.mGender = gender;
    }

    /**
     *
     * @return
     */
    public String getState() {
        return mState;
    }

    /**
     * State of the user. For example, state=NY
     * @param state
     */
    public void setState(String state) {
        this.mState = state;
    }

    /**
     * Set user's birth year as a four-digit integer. For example, 1975
     *
     * @param yearOfBirth
     *            - yearOfBirth of the user
     */
    public void setYearOfBirth(final String yearOfBirth) {
        mYearOfBirth = yearOfBirth;
    }

    /**
     * Set user Income if available for more relevant Ads. User's income or income range in dollars (whole numbers). For example, inc=50000 or 50000-75000
     *
     * @param income
     *            Sets the user income value
     */
    public void setIncome(final String income)
    {
        mIncome = income;
    }

    /**
     * Numeric code of ethnicity. Possible options are:
     * Hispanic
     * African-American
     * Caucasian
     * Asian-American
     * Other
     *
     * @param ethnicity
     *            User ethnicity
     */
    public void setEthnicity(final ETHNICITY ethnicity)
    {
        mEthnicity = ethnicity;
    }

    public ETHNICITY getEthnicity() {
        return mEthnicity;
    }

    /**
     * Add the new keyword that the user might be interested in.
     *
     * @param keyword
     *            the new keyword to be added to the keywords list
     */
    public void addKeyword(final String keyword) {
        if (mKeywordsList == null) {
            mKeywordsList = new ArrayList<String>();
        }

        if(keyword != null && !keyword.equals(""))
            mKeywordsList.add(keyword);
    }

    /**
     * Returns the year of birth of the user.
     *
     * @return the yearOfBirth
     */
    public String getYearOfBirth() {
        return mYearOfBirth;
    }

    /**
     * Returns the income  of the user.
     *
     * @return the income of user
     */
    public String getIncome()
    {
        return mIncome;
    }

    /**
     * Returns the keywords list in the form of comma separated String. e.g.
     * Cricket,Pizza
     */
    public String getKeywordString() {

        StringBuffer keywordStringBuffer = new StringBuffer();

        if (mKeywordsList != null && !mKeywordsList.isEmpty()) {

            for (String keyword : mKeywordsList)
                keywordStringBuffer.append(keyword + ",");

        }

        keywordStringBuffer.setLength(keywordStringBuffer.length() - 1);

        return keywordStringBuffer.toString();
    }

    /**
     * Getter for ID of the publisher. This value can be obtained from the pubId parameter in the PubMatic ad tag.
     * @return
     */
    public String getPubId() {
        return mPubId;
    }

    /**
     * Getter for ID of the publisher's site/app. This value can be obtained from the siteId parameter in the PubMatic ad tag.
     * @return
     */
    public String getSiteId() {
        return mSiteId;
    }

    /**
     * Getter for ID of the publisher's ad tag ID. This value can be obtained from the adId parameter in the PubMatic ad tag.
     * @return
     */
    public String getAdId() {
        return mAdId;
    }

    public String getIABCategory() {
        return mIABCategory;
    }

    /**
     * Sets IAB category for the application.
     * If the site/application falls under multiple IAB categories, you can send categories separated by comma, and the string should be URL encoded.
     * For example, iabcat=IAB1%2CIAB-5%2CIAB1-6
     * @param mIABCategory
     */
    public void setIABCategory(String mIABCategory) {
        this.mIABCategory = mIABCategory;
    }

    public Boolean isCoppa() {
        return mCoppa;
    }

    /**
     * Indicates whether the visitor is COPPA-specific or not. For COPPA (Children's Online Privacy Protection Act) compliance,
     * if the visitor's age is below 13, then such visitors should not be served targeted ads.
     * Possible options are:
     *  false - Indicates that the visitor is not COPPA-specific and can be served targeted ads.
     *  true - Indicates that the visitor is COPPA-specific and should be served only COPPA-compliant ads.
     *
     * @param mCoppa
     */
    public void setCoppa(boolean mCoppa) {
        this.mCoppa = mCoppa;
    }

    public String getPMZoneId() {
        return mPMZoneId;
    }

    /**
     * This parameter is used to pass a zone ID for reporting.
     * @param mPMZoneId
     */
    public void setPMZoneId(String mPMZoneId) {
        this.mPMZoneId = mPMZoneId;
    }

    public String getStoreURL() {
        return mStoreURL;
    }

    /**
     * URL of the app store from where a user can download this application.
     * @param mStoreURL
     */
    public void setStoreURL(String mStoreURL) {
        this.mStoreURL = mStoreURL;
    }

    /**
     * It is not in use.
     */
    public String getAid() {
        return mAid;
    }

    /**
     * It is not in use.
     * Android application’s ID
     * @param mAid
     */
    public void setAid(String mAid) {
        this.mAid = mAid;
    }

    public String getAppDomain() {
        return mAppDomain;
    }

    /**
     * Indicates the domain of the mobile application
     * @param appDomain domain of app
     */
    public void setAppDomain(String appDomain) {
        this.mAppDomain = appDomain;
    }

    public String getAppCategory() {
        return mAppCategory;
    }

    /**
     * Application primary category as displayed on storeurl page for the respective platform
     * @param mAppCategory
     */
    public void setAppCategory(String mAppCategory) {
        this.mAppCategory = mAppCategory;
    }

    /**
     * Indicates whether the mobile application is a paid version or not. Possible values are: false - Free version, true - Paid version
     * @return
     */
    public Boolean isApplicationPaid() {
        return this.mPaid;
    }

    /**
     * Indicates whether the mobile application is a paid version or not. Possible values are:
     *  false - Free version
     *  true - Paid version
     * @param paid
     */
    public void setApplicationPaid(boolean paid) {
        this.mPaid = paid;
    }

    public int getOrmmaComplianceLevel() {
        return mOrmmaComplianceLevel;
    }

    /**
     * Minimum compliance level. Possible values are 0, 1 and 2
     * @param mOrmmaComplianceLevel
     */
    public void setOrmmaComplianceLevel(int mOrmmaComplianceLevel) {
        this.mOrmmaComplianceLevel = mOrmmaComplianceLevel;
    }

    public String getAdOrientation() {
        return mAdOrientation;
    }

    //TODO::Create enum and update description
    /**
     * Set the ad orientation.
     * Possible values are:
     * 0 - Portrait orientation
     * 1 - Landscape orientation
     * @param mAdOrientation
     */
    public void setAdOrientation(String mAdOrientation) {
        this.mAdOrientation = mAdOrientation;
    }

    public int getDeviceOrientation(Context context) {
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
            return 0;
        return 1;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location mLocation) {
        this.mLocation = mLocation;
    }

}
