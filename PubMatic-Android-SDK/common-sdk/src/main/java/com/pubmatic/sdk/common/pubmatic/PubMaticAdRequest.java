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


public abstract class PubMaticAdRequest extends AdRequest {

//    private int                             mAdHeight;
//    private int                             mAdWidth;
//    private boolean                         mInIFrame;
//    private String                          mAdNetwork;
//    private AD_VISIBILITY                   mAdVisibility;
//    private String                          mAppName;
//    private String 		                    mNetworkType;

    // Passback params
//    private String mKAdNetworkId;
//    private String mLastDefaultedNetworkId;
//    private List<String> mDefaultedCampaignList;

    protected int                           mAdRefreshRate;
    protected int                           mOrmmaComplianceLevel;

    protected boolean                       mPaid;
    protected boolean			            mDoNotTrack;
    protected boolean			            mCoppa;

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

    protected Context                       mContext;
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
    public enum AWT_OPTION { DEFAULT, WRAPPED_IN_IFRAME, WRAPPED_IN_JS }

    public enum ETHNICITY { HISPANIC, AFRICAN_AMERICAN, CAUCASIAN, ASIAN_AMERICAN, OTHER }

    public enum GENDER { MALE, FEMALE, OTHER }

    public enum HASHING_TECHNIQUE {
        SHA1, MD5, RAW
    }

    //---------------- protected methods to be used internally ------------------
    protected PubMaticAdRequest(Context context) {
        super(CommonConstants.CHANNEL.PUBMATIC, context);
        mContext = context;

//        mDefaultedCampaignList = new ArrayList<>(0);
    }

    @Override
    protected void setUpUrlParams() {
        super.setUpUrlParams();
    }

    protected void setUpPostParams() {

        super.setupPostData();
        boolean optedOut = AdvertisingIdClient.getLimitedAdTrackingState(mContext, false);
        PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation.getInstance(mContext);

        putPostData(PubMaticConstants.PUB_ID_PARAM, mPubId);
        putPostData(PubMaticConstants.SITE_ID_PARAM, String.valueOf(mSiteId));
        putPostData(PubMaticConstants.AD_ID_PARAM, String.valueOf(mAdId));

        if(mOperId == OPERID.HTML)
            putPostData(PubMaticConstants.OPER_ID_PARAM, String.valueOf(1));
        else if(mOperId == OPERID.JAVA_SCRIPT)
            putPostData(PubMaticConstants.OPER_ID_PARAM, String.valueOf(3));
        else if(mOperId == OPERID.JSON)
            putPostData(PubMaticConstants.OPER_ID_PARAM, String.valueOf(102));
        else if(mOperId == OPERID.JSON_MOBILE)
            putPostData(PubMaticConstants.OPER_ID_PARAM, String.valueOf(201));

        if(mAdType == AD_TYPE.TEXT)
            putPostData(PubMaticConstants.AD_TYPE_PARAM, String.valueOf(1));
        else if(mAdType == AD_TYPE.IMAGE)
            putPostData(PubMaticConstants.AD_TYPE_PARAM, String.valueOf(2));
        else if(mAdType == AD_TYPE.IMAGE_TEXT)
            putPostData(PubMaticConstants.AD_TYPE_PARAM, String.valueOf(3));
        else if(mAdType == AD_TYPE.BANNER)
            putPostData(PubMaticConstants.AD_TYPE_PARAM, String.valueOf(11));
        else if(mAdType == AD_TYPE.NATIVE)
            putPostData(PubMaticConstants.AD_TYPE_PARAM, String.valueOf(12));
        else if(mAdType == AD_TYPE.VIDEO)
            putPostData(PubMaticConstants.AD_TYPE_PARAM, String.valueOf(13));
        else if(mAdType == AD_TYPE.AUDIO)
            putPostData(PubMaticConstants.AD_TYPE_PARAM, String.valueOf(14));

        putPostData(PubMaticConstants.AD_POSITION_PARAM, String.valueOf(PUBDeviceInformation.mAdPosition));
        putPostData(PubMaticConstants.IN_IFRAME_PARAM, String.valueOf(PUBDeviceInformation.mInIframe));
        putPostData(PubMaticConstants.AD_VISIBILITY_PARAM, String.valueOf(PUBDeviceInformation.mAdVisibility));
        putPostData(PubMaticConstants.APP_CATEGORY_PARAM, mAppCategory);
        putPostData(PubMaticConstants.COPPA_PARAM, String.valueOf(mCoppa ? 1 : 0));
        putPostData(PubMaticConstants.NETWORK_TYPE_PARAM, PMUtils.getNetworkType(mContext));
        putPostData(PubMaticConstants.PAID_PARAM, String.valueOf(mPaid ? 1 : 0));
        putPostData(PubMaticConstants.APP_DOMAIN_PARAM, mAppDomain);
        putPostData(PubMaticConstants.STORE_URL_PARAM, mStoreURL);
        putPostData(PubMaticConstants.APP_ID_PARAM, mAid);
        putPostData(PubMaticConstants.AD_REFRESH_RATE_PARAM, String.valueOf(mAdRefreshRate));
        putPostData(PubMaticConstants.JS_PARAM, String.valueOf(PUBDeviceInformation.mJavaScriptSupport));
        putPostData(PubMaticConstants.DEVICE_ORIENTATION_PARAM, String.valueOf(getDeviceOrientation(mContext)));

        try {

            if(!isDoNotTrack() && !optedOut)
            {
                if (pubDeviceInformation.mCarrierName != null) {
                    putPostData(PubMaticConstants.CARRIER_PARAM, pubDeviceInformation.mCarrierName);
                }

                if (pubDeviceInformation.mDeviceMake != null) {
                    putPostData(PubMaticConstants.MAKE_PARAM, pubDeviceInformation.mDeviceMake);
                }

                if (pubDeviceInformation.mDeviceModel != null) {
                    putPostData(PubMaticConstants.MODEL_PARAM, pubDeviceInformation.mDeviceModel);
                }

                if (pubDeviceInformation.mDeviceOSName != null) {
                    putPostData(PubMaticConstants.OS_PARAM, pubDeviceInformation.mDeviceOSName);
                }

                if (pubDeviceInformation.mDeviceOSVersion != null) {
                    putPostData(PubMaticConstants.OSV_PARAM, pubDeviceInformation.mDeviceOSVersion);
                }

                if (!TextUtils.isEmpty(mYearOfBirth)) {
                    putPostData(PubMaticConstants.YOB_PARAM, mYearOfBirth);
                }

                if(getGender() != null) {
                    switch (getGender()) {
                        case MALE:
                            putPostData(PubMaticConstants.GENDER_PARAM, "M");
                            break;
                        case FEMALE:
                            putPostData(PubMaticConstants.GENDER_PARAM, "F");
                            break;
                        case OTHER:
                            putPostData(PubMaticConstants.GENDER_PARAM, "O");
                            break;
                        default:
                            putPostData(PubMaticConstants.GENDER_PARAM, "O");
                            break;
                    }
                }

                //Set the location
                if (mLocation != null) {
                    putPostData(PubMaticConstants.LOC_PARAM, mLocation.getLatitude() + ","
                            + mLocation.getLongitude());

                    String provider = mLocation.getProvider();

                    if(provider.equalsIgnoreCase("network") || provider.equalsIgnoreCase("wifi") || provider.equalsIgnoreCase("gps"))
                        putPostData(PubMaticConstants.LOC_SOURCE_PARAM, PubMaticConstants.LOCATION_SOURCE_GPS_LOCATION_SERVICES);
                    else if(provider.equalsIgnoreCase("user"))
                        putPostData(PubMaticConstants.LOC_SOURCE_PARAM, PubMaticConstants.LOCATION_SOURCE_USER_PROVIDED);
                    else
                        putPostData(PubMaticConstants.LOC_SOURCE_PARAM, PubMaticConstants.LOCATION_SOURCE_UNKNOWN);
                }
            }

            try
            {
                if (!TextUtils.isEmpty(mAdOrientation))
                    putPostData(PubMaticConstants.AD_ORIENTATION_PARAM, mAdOrientation);

                if (!TextUtils.isEmpty(mState)) {
                    putPostData(PubMaticConstants.USER_STATE, mState);
                }

                if (!TextUtils.isEmpty(mCity)) {
                    putPostData(PubMaticConstants.USER_CITY, mCity);
                }

                if (!TextUtils.isEmpty(mZip)) {
                    putPostData(PubMaticConstants.ZIP_PARAM, mZip);
                }
            }
            catch(Exception exception) {}

            if (pubDeviceInformation.mDeviceCountryCode != null) {
                putPostData(PubMaticConstants.COUNTRY_PARAM, pubDeviceInformation.mDeviceCountryCode);
            }

            if (pubDeviceInformation.mPageURL != null) {
                putPostData(PubMaticConstants.PAGE_URL_PARAM, pubDeviceInformation.mPageURL);
            }

            if (pubDeviceInformation.mApplicationName != null) {
                putPostData(PubMaticConstants.APP_NAME_PARAM, pubDeviceInformation.mApplicationName);
            }

            if (pubDeviceInformation.mPackageName != null) {
                putPostData(PubMaticConstants.APP_BUNDLE_PARAM, pubDeviceInformation.mPackageName);
            }

            if (pubDeviceInformation.mApplicationVersion != null) {
                putPostData(PubMaticConstants.APP_VERSION_PARAM, pubDeviceInformation.mApplicationVersion);
            }

            if (mAWT != null) {
                switch (mAWT) {
                    case WRAPPED_IN_IFRAME:
                        putPostData(PubMaticConstants.AWT_PARAM, String.valueOf(1));
                        break;
                    case WRAPPED_IN_JS:
                        putPostData(PubMaticConstants.AWT_PARAM, String.valueOf(2));
                        break;
                    case DEFAULT:
                        putPostData(PubMaticConstants.AWT_PARAM, String.valueOf(0));
                        break;
                }
            }

            if(isDoNotTrack() || optedOut)
                putPostData(PubMaticConstants.DNT_PARAM, String.valueOf(1));
            else
                putPostData(PubMaticConstants.DNT_PARAM, String.valueOf(0));

            if(!isDoNotTrack()) {
                AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.refreshAdvertisingInfo(mContext);

                if(!AdvertisingIdClient.getLimitedAdTrackingState(mContext, false)) {
                    if (isAndoridAidEnabled() && adInfo!=null && !TextUtils.isEmpty(adInfo.getId())) {

                        String advertisingId = adInfo.getId();

                        switch (mHashing)
                        {
                            case RAW:
                                putPostData(PubMaticConstants.UDID_PARAM, advertisingId);
                                putPostData(PubMaticConstants.UDID_HASH_PARAM, PubMaticConstants.HASHING_RAW);
                                break;
                            case SHA1:
                                putPostData(PubMaticConstants.UDID_PARAM, PMUtils.sha1(advertisingId));
                                putPostData(PubMaticConstants.UDID_HASH_PARAM, PubMaticConstants.HASHING_SHA1);
                                break;
                            case MD5:
                                putPostData(PubMaticConstants.UDID_PARAM, PMUtils.md5(advertisingId));
                                putPostData(PubMaticConstants.UDID_HASH_PARAM, PubMaticConstants.HASHING_MD5);
                                break;
                        }

                        putPostData(PubMaticConstants.UDID_TYPE_PARAM, PubMaticConstants.ADVERTISEMENT_ID);

                    }
                    else {

                        String androidId = PMUtils.getUdidFromContext(mContext);

                        switch (mHashing)
                        {
                            case RAW:
                                putPostData(PubMaticConstants.UDID_PARAM, androidId);
                                putPostData(PubMaticConstants.UDID_HASH_PARAM, PubMaticConstants.HASHING_RAW);
                                break;
                            case SHA1:
                                putPostData(PubMaticConstants.UDID_PARAM, PMUtils.sha1(androidId));
                                putPostData(PubMaticConstants.UDID_HASH_PARAM, PubMaticConstants.HASHING_SHA1);
                                break;
                            case MD5:
                                putPostData(PubMaticConstants.UDID_PARAM, PMUtils.md5(androidId));
                                putPostData(PubMaticConstants.UDID_HASH_PARAM, PubMaticConstants.HASHING_MD5);
                                break;
                        }

                        putPostData(PubMaticConstants.UDID_TYPE_PARAM, PubMaticConstants.ANDROID_ID);
                    }
                }
            }

            if (getEthnicity() != null) {
                switch (getEthnicity()) {
                    case HISPANIC:
                        putPostData(PubMaticConstants.USER_ETHNICITY, "0");
                        break;
                    case AFRICAN_AMERICAN:
                        putPostData(PubMaticConstants.USER_ETHNICITY, "1");
                        break;
                    case CAUCASIAN:
                        putPostData(PubMaticConstants.USER_ETHNICITY, "2");
                        break;
                    case ASIAN_AMERICAN:
                        putPostData(PubMaticConstants.USER_ETHNICITY, "3");
                        break;
                    default:
                        putPostData(PubMaticConstants.USER_ETHNICITY, "4");
                        break;
                }

            }

            if (!TextUtils.isEmpty(mIncome)) {
                putPostData(PubMaticConstants.USER_INCOME, mIncome);
            }

            // Setting the iab category
            if (!TextUtils.isEmpty(mIABCategory)) {
                putPostData(PubMaticConstants.IAB_CATEGORY, mIABCategory);
            }

            // Setting the DMA
            if (!TextUtils.isEmpty(mDMA)) {
                putPostData(PubMaticConstants.DMA, mDMA);
            }

            if (mKeywordsList != null) {
                putPostData(PubMaticConstants.KEYWORDS_PARAM, getKeywordString());
            }

            // Setting sdk_ver
            putPostData(PubMaticConstants.SDK_VER_PARAM, PUBDeviceInformation.msdkVersion);

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
                putPostData(PubMaticConstants.SSP_CUSTOM_KEY,customPair.toString());
            }

        } catch (Exception e) {

        }
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
     * 0 - Unknown
     * 1 - Raw
     * 2 - SHA1
     * 3 - MD5
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
     * Sets city of the user. For example, setCity("New York");
     * @param city User's city
     */
    public void setCity(String city) {
        this.mCity = city;
    }

    public GENDER getGender() {
        return mGender;
    }

    /**
     * Gender of the user. Possible values are:
     * M - Male
     * F - Female
     * O - Others
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
     * User's income or income range in dollars (whole numbers). For example, inc=50000 or 50000-75000
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
     *  0 - Hispanic
     *  1 - African-American
     *  2 - Caucasian
     *  3 - Asian-American
     *  4 –Other
     *  For example, ethn=1.
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
     *
     * @return
     */
    public String getPubId() {
        return mPubId;
    }

    /**
     *
     * @return
     */
    public String getSiteId() {
        return mSiteId;
    }

    /**
     *
     * @return
     */
    public String getAdId() {
        return mAdId;
    }

//    public int getAdHeight() {
//        return mAdHeight;
//    }
//
//    public void setAdHeight(int mAdHeight) {
//        this.mAdHeight = mAdHeight;
//    }
//
//    public int getAdWidth() {
//        return mAdWidth;
//    }
//
//    public void setAdWidth(int mAdWidth) {
//        this.mAdWidth = mAdWidth;
//    }
//
//    public boolean isInIFrame() {
//        return mInIFrame;
//    }
//
//    public void setInIFrame(boolean mInIFrame) {
//        this.mInIFrame = mInIFrame;
//    }
//
//    public String getAdNetwork() {
//        return mAdNetwork;
//    }
//
//    public void setAdNetwork(String mAdNetwork) {
//        this.mAdNetwork = mAdNetwork;
//    }
//
//    public AD_VISIBILITY getAdVisibility() {
//        return mAdVisibility;
//    }
//
//    public void setAdVisibility(AD_VISIBILITY mAdVisibility) {
//        this.mAdVisibility = mAdVisibility;
//    }

    public String getIABCategory() {
        return mIABCategory;
    }

    /**
     * List of IAB content categories for the overall site/application. Refer the "Table 6.1 Content Categories" in the Open RTB 2.1 / 2.2 specifications document.
     * If the site/application falls under multiple IAB categories, you can send categories separated by comma, and the string should be URL encoded.
     * For example, iabcat=IAB1%2CIAB-5%2CIAB1-6
     * @param mIABCategory
     */
    public void setIABCategory(String mIABCategory) {
        this.mIABCategory = mIABCategory;
    }

    public boolean isDoNotTrack() {
        return mDoNotTrack;
    }

    /**
     * Indicates whether the user has opted out of the publisher or not, or whether HTTP_DNT is set or not. Possible values are:
     *  0 - Either the user has not opted out of the publisher or HTTP_DNT is not set.
     *  1 - Either the user has opted out of the publisher or HTTP_DNT is set; in this case, PubMatic will not target such users.
     *  Note: The default value for this parameter is 0
     * @param mDoNotTrack flag for do-not-track
     */
    public void setDoNotTrack(boolean mDoNotTrack) {
        this.mDoNotTrack = mDoNotTrack;
    }

    public boolean isCoppa() {
        return mCoppa;
    }

    /**
     * Indicates whether the visitor is COPPA-specific or not. For COPPA (Children's Online Privacy Protection Act) compliance,
     * if the visitor's age is below 13, then such visitors should not be served targeted ads.
     * Possible options are:
     *  0 - Indicates that the visitor is not COPPA-specific and can be served targeted ads.
     *  1 - Indicates that the visitor is COPPA-specific and should be served only COPPA-compliant ads.
     * The United States Federal Trade Commission has written a comprehensive FAQ on complying with COPPA at http://business.ftc.gov/documents/Complying-with-COPPA-Frequently-Asked-Questions.
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
//
//    public String getAppName() {
//        return mAppName;
//    }
//
//    public void setAppName(String appName) {
//        mAppName = appName;
//    }

    public String getStoreURL() {
        return mStoreURL;
    }

    /**
     * URL of the app store from where a user can download this application. This URL must match the storeurl that is whitelisted on UI.
     * @param mStoreURL
     */
    public void setStoreURL(String mStoreURL) {
        this.mStoreURL = mStoreURL;
    }

    /**
     * It is not in use.
     */
    @Deprecated
    public String getAid() {
        return mAid;
    }

    /**
     * It is not in use.
     * @param mAid
     */
    @Deprecated
    public void setAid(String mAid) {
        this.mAid = mAid;
    }

    public String getAppDomain() {
        return mAppDomain;
    }

    /**
     * Indicates the domain of the mobile application
     * @param mAppDomain domain of app
     */
    public void setAppDomain(String mAppDomain) {
        this.mAppDomain = mAppDomain;
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

    public boolean isApplicationPaid() {
        return this.mPaid;
    }

    /**
     * Indicates whether the mobile application is a paid version or not. Possible values are:
     *  0 - Free version
     *  1 - Paid version
     * @param paid
     */
    public void setApplicationPaid(boolean paid) {
        this.mPaid = paid;
    }

    public int getAdRefreshRate() {
        return mAdRefreshRate;
    }

    /**
     * Ad will be refreshed by given time interval in seconds. By default,
     * Library will set to 0 second and Ad will be not refresh automatically.
     * <p/>
     * <p/>
     * The adRefreshRa te should be in range on 12 to 120 second. If you set the
     * ad refresh time interval value < 0 or >=1 && < 12, library will set it to
     * 12 seconds If you set the ad refresh time interval >120 seconds, library
     * will set it to 120 seconds
     *
     * @param adRefreshRate adRefreshRate to set.
     */
    public void setAdRefreshRate(int adRefreshRate) {
        this.mAdRefreshRate = adRefreshRate;
    }

//    public String getNetworkType() {
//        return mNetworkType;
//    }
//
//    public void setNetworkType(String networkType) {
//        this.mNetworkType = networkType;
//    }

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

//    public String getKAdNetworkId() {
//        return mKAdNetworkId;
//    }
//
//    public void setKAdNetworkId(String kAdNetworkId) {
//        this.mKAdNetworkId = kAdNetworkId;
//    }
//
//    public String getLastDefaultedNetworkId() {
//        return mLastDefaultedNetworkId;
//    }
//
//    protected void setLastDefaultedNetworkId(String lastDefaultedNetworkId) {
//        this.mLastDefaultedNetworkId = lastDefaultedNetworkId;
//    }
//
//    public List<String> getDefaultedCampaignList() {
//        return mDefaultedCampaignList;
//    }
//
//    public void addDefaultedCampaign(String campaign)
//    {
//        this.mDefaultedCampaignList.add(campaign);
//    }
//
//    public void clearDefaultedCampaignList()
//    {
//        this.mDefaultedCampaignList.clear();
//    }

//    public void setDefaultedCampaignList(List<String> defaultedCampaignList) {
//        this.mDefaultedCampaignList = defaultedCampaignList;
//    }

    public String getAdOrientation() {
        return mAdOrientation;
    }

    /**
     * Set ID of the ad orientation for the given ad request.
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

//    public void resetPassbackParameters()
//    {
//        setKAdNetworkId("");
//        setLastDefaultedNetworkId("");
//        clearDefaultedCampaignList();
//    }
}
