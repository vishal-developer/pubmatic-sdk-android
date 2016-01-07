package com.pubmatic.sdk.common.pubmatic;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.PMUserInfo;
import com.pubmatic.sdk.common.utils.CommonConstants;
import com.pubmatic.sdk.common.utils.PubUtils;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by shrawangupta on 29/12/15.
 */
public abstract class PubMaticAdRequest extends AdRequest {

    public abstract void setAttributes(AttributeSet attr);

    public abstract void copyRequestParams(AdRequest adRequestParams);

    protected PubMaticAdRequest(Context context) {
        super(CommonConstants.CHANNEL.PUBMATIC, context);
        mContext = context;

    }

    @Override
    public String getAdServerURL() {
        return TextUtils.isEmpty(mBaseUrl) ? CommonConstants.PUBMATIC_AD_NETWORK_URL : mBaseUrl;
    }

    @Override
    public boolean checkMandatoryParams() {
        return false;
    }

    @Override
    protected void initializeDefaultParams(Context context) {

    }

    @Override
    protected void setupPostData() {

        putPostData(CommonConstants.PUB_ID_PARAM, mPubId);
        putPostData(CommonConstants.SITE_ID_PARAM, String.valueOf(this.mSiteId));
        putPostData(CommonConstants.AD_ID_PARAM, String.valueOf(this.mAdId));

        PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation
                .getInstance(mContext);

        try {

            if (pubDeviceInformation.mDeviceAcceptLanguage != null) {
                // Appending did
                putPostData(CommonConstants.LANGUAGE, URLEncoder.encode(
                        pubDeviceInformation.mDeviceAcceptLanguage,
                        CommonConstants.URL_ENCODING));
            }
            // Setting country
            if (pubDeviceInformation.mDeviceCountryCode != null) {
                putPostData(CommonConstants.COUNTRY_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mDeviceCountryCode,
                        CommonConstants.URL_ENCODING));
            }

            // Setting carrier
            if (pubDeviceInformation.mCarrierName != null) {
                putPostData(CommonConstants.CARRIER_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mCarrierName,
                        CommonConstants.URL_ENCODING));
            }

            // Setting make
            if (pubDeviceInformation.mDeviceMake != null) {
                putPostData(CommonConstants.MAKE_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mDeviceMake,
                        CommonConstants.URL_ENCODING));
            }

            // Setting model
            if (pubDeviceInformation.mDeviceModel != null) {
                putPostData(CommonConstants.MODEL_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mDeviceModel,
                        CommonConstants.URL_ENCODING));
            }

            // Setting os
            if (pubDeviceInformation.mDeviceOSName != null) {
                putPostData(CommonConstants.OS_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mDeviceOSName,
                        CommonConstants.URL_ENCODING));
            }

            // Setting osv
            if (pubDeviceInformation.mDeviceOSVersion != null) {
                putPostData(CommonConstants.OSV_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mDeviceOSVersion,
                        CommonConstants.URL_ENCODING));
            }

            if (pubDeviceInformation.mApplicationName != null) {
                putPostData(CommonConstants.APP_NAME_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mApplicationName,
                        CommonConstants.URL_ENCODING));
            }

            if (pubDeviceInformation.mPackageName != null) {
                putPostData(CommonConstants.APP_BUNDLE_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mPackageName,
                        CommonConstants.URL_ENCODING));
            }

            if (pubDeviceInformation.mApplicationVersion != null) {
                putPostData(CommonConstants.APP_VERSION_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mApplicationVersion,
                        CommonConstants.URL_ENCODING));
            }

            if (pubDeviceInformation.mPageURL != null) {
                putPostData(CommonConstants.PAGE_URL_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mPageURL,
                        CommonConstants.URL_ENCODING));
            }

            // Setting js
            putPostData(CommonConstants.JS_PARAM, String.valueOf(PUBDeviceInformation.mJavaScriptSupport));
            putPostData(CommonConstants.IN_IFRAME_PARAM, String.valueOf(PUBDeviceInformation.mInIframe));
            putPostData(CommonConstants.AD_VISIBILITY_PARAM, String.valueOf(PUBDeviceInformation.mAdVisibility));
            putPostData(CommonConstants.AD_POSITION_PARAM, String.valueOf(PUBDeviceInformation.mAdPosition));
            putPostData(CommonConstants.APP_ID_PARAM, mAid);
            putPostData(CommonConstants.APP_CATEGORY_PARAM, mAppCategory);
            putPostData(CommonConstants.NETWORK_TYPE_PARAM, PubUtils.getNetworkType(mContext));

            //Send Advertisement ID
            if(!TextUtils.isEmpty(mUDID)) {
                putPostData(CommonConstants.UDID_PARAM, mUDID);
                putPostData(CommonConstants.UDID_TYPE_PARAM, String.valueOf(9));//9 - Android Advertising ID
                putPostData(CommonConstants.UDID_HASH_PARAM, String.valueOf(0));//0 - raw udid
            } else if(mContext!=null){
                //Send Android ID
                putPostData(CommonConstants.UDID_PARAM, PubUtils.getUdidFromContext(mContext));
                putPostData(CommonConstants.UDID_TYPE_PARAM, String.valueOf(3));//9 - Android ID
                putPostData(CommonConstants.UDID_HASH_PARAM, String.valueOf(2));//0 - SHA1
            } else {
            }
            // Setting ver
            if (pubDeviceInformation.mApplicationVersion != null) {
                putPostData(CommonConstants.VER_PARAM, URLEncoder.encode(
                        pubDeviceInformation.mApplicationVersion,
                        CommonConstants.URL_ENCODING));
            }

            // Setting user specific information
            if (mPMUserInfo != null) {
                // Setting year of birth
                if (mPMUserInfo.getYearOfBirth() != null) {
                    putPostData(CommonConstants.YOB_PARAM, URLEncoder.encode(
                            mPMUserInfo.getYearOfBirth(),
                            CommonConstants.URL_ENCODING));
                }

                // Setting Gender of user
                putPostData(CommonConstants.GENDER_PARAM, URLEncoder.encode(
                        mPMUserInfo.getGender(), CommonConstants.URL_ENCODING));

                // Setting zip code of user
                if (mPMUserInfo.getZip() != null) {
                    putPostData(CommonConstants.ZIP_PARAM, URLEncoder.encode(
                            mPMUserInfo.getZip(), CommonConstants.URL_ENCODING));
                }

                // Setting area code of user
                if (mPMUserInfo.getAreaCode() != null) {
                    putPostData(CommonConstants.AREACODE, URLEncoder.encode(
                            mPMUserInfo.getAreaCode(),
                            CommonConstants.URL_ENCODING));
                }

                // Setting the income
                if (mPMUserInfo.getIncome() != null) {
                    putPostData(CommonConstants.USER_INCOME, URLEncoder.encode(
                            mPMUserInfo.getIncome(),
                            CommonConstants.URL_ENCODING));
                }

                // Setting the user entnicity
                if (mPMUserInfo.getEthnicity() != null) {
                    putPostData(CommonConstants.USER_ETHNICITY, URLEncoder.encode(
                            mPMUserInfo.getEthnicity(),
                            CommonConstants.URL_ENCODING));
                }

                if (mPMUserInfo.getKeywordString() != null) {
                    putPostData(CommonConstants.KEYWORDS_PARAM, URLEncoder.encode(
                            mPMUserInfo.getKeywordString(),
                            CommonConstants.URL_ENCODING));
                }


                // Setting user city
                if (mPMUserInfo.getCity() != null) {
                    putPostData(CommonConstants.USER_CITY, URLEncoder.encode(
                            mPMUserInfo.getCity(),
                            CommonConstants.URL_ENCODING));
                }

                // Setting the state
                if (mPMUserInfo.getState() != null) {
                    putPostData(CommonConstants.USER_STATE, URLEncoder.encode(
                            mPMUserInfo.getState(),
                            CommonConstants.URL_ENCODING));
                }
            }

            // Setting adOrientation
            if(!TextUtils.isEmpty(mAdOrientation))
             putPostData(CommonConstants.AD_ORIENTATION_PARAM, mAdOrientation);

            // Setting deviceOrientation
            putPostData(CommonConstants.DEVICE_ORIENTATION_PARAM, String.valueOf(mDeviceOrientation));

            // Setting adRefreshRate
            putPostData(CommonConstants.AD_REFRESH_RATE_PARAM, String.valueOf(mAdRefreshRate));

            // Setting sdk_id
            putPostData(CommonConstants.SDK_ID_PARAM, URLEncoder
                    .encode(PUBDeviceInformation.msdkId,
                            CommonConstants.URL_ENCODING));

            // Setting sdk_ver
            putPostData(CommonConstants.SDK_VER_PARAM, URLEncoder.encode(
                    PUBDeviceInformation.msdkVersion,
                    CommonConstants.URL_ENCODING));

            if (mNetworkType != null) {
                putPostData(CommonConstants.NETWORK_TYPE_PARAM, URLEncoder.encode(mNetworkType,
                        CommonConstants.URL_ENCODING));
            }

            putPostData(CommonConstants.AD_TYPE_PARAM, String.valueOf(11));//For Text and Image and Rich Media ads
            putPostData(CommonConstants.DNT_PARAM, String.valueOf(mDoNotTrack==true ? 1 : 0));
            putPostData(CommonConstants.COPPA_PARAM, String.valueOf(mCoppa==true ? 1 : 0));
            putPostData(CommonConstants.STORE_URL_PARAM, mStoreURL);
            putPostData(CommonConstants.PAID_PARAM, String.valueOf(mPaid==true ? 1 : 0));
            putPostData(CommonConstants.APP_DOMAIN_PARAM, mAppDomain);

            //Set the awt parameter
            switch (mAWT) {
                case WRAPPED_IN_IFRAME:
                    putPostData(CommonConstants.AWT_PARAM, String.valueOf(1));
                    break;
                case WRAPPED_IN_JS:
                    putPostData(CommonConstants.AWT_PARAM, String.valueOf(2));
                    break;
            }

            //Set the location
            if (mLocation != null) {
                putPostData(CommonConstants.LOC_PARAM, URLEncoder.encode(
                        mLocation.getLatitude() + ","
                                + mLocation.getLongitude(),
                        CommonConstants.URL_ENCODING));

                putPostData(CommonConstants.LOC_SOURCE_PARAM, URLEncoder.encode(
                        mLocation.getProvider()));
            }


        } catch (Exception e) {

        }
    }

    @Override
    public void setCustomParams(Map<String, List<String>> customParams) {

    }

    @Override
    public void createRequest(Context context) {
        mPostData		= null;
        initializeDefaultParams(context);
        setupPostData();
    }

    //PubMatic specific enums
    public enum AD_TYPE { TEXT, IMAGE, IMAGE_TEXT, RICHMEDIA, NATIVE, VIDEO, AUDIO }

    public enum OVER18 { DEFAULT_DENY, DENY, ONLY_OVER18, ALLOW_ALL }

    public enum OPERID { HTML, JAVA_SCRIPT, JSON, JSON_MOBILE }

    public enum RS { PURE_JSON, JSON_CALLBACK, JS_VAR}

    public enum AWT_OPTION { DEFAULT, WRAPPED_IN_IFRAME, WRAPPED_IN_JS }

    public enum AD_VISIBILITY { CAN_NOT_DETERMINE, ABOVE_FOLD, BELOW_FOLD, PARTIAL }

    public enum LOCATION_SOURCE { UNKOWN, GPS_OR_SERVICES, IP, UCER_PROVIDED }

    public enum ETHNICITY { HISPANIC, AFRICAN_AMERICAN, CAUCASIAN, ASIAN_AMERICAN, OTHER }

    //Mocean specific enums
    public enum FORMAT_KEY { HTML, XML, JSON, JSONP, GENERIC, VAST, DAAST, OFFLINE_XML }

    protected Context         mContext;
    protected String          mPubId;
    protected String          mSiteId;
    protected String          mAdId;
    protected RS              mRs;
    protected AD_TYPE         mAdType;
    protected int             mAdHeight;
    protected int             mAdWidth;
    protected boolean         mInIFrame;
    protected String          mAdNetwork;
    protected AD_VISIBILITY   mAdVisibility;
    protected String          mIABCategory;
    protected String          mPMZoneId;
    protected String          mAppName;
    protected String          mStoreURL;
    protected String          mAid;
    protected String          mAppCategory;
    protected String          mAppDomain;
    protected boolean         mPaid;
    protected int             mAdRefreshRate;
    protected int             mOrmmaComplianceLevel;
    protected String          mAdOrientation;
    protected int             mDeviceOrientation;
    protected String          mState;
    protected String          mBirthYear;
    protected ETHNICITY       mEthnicity;
    protected String          mIncome;
    protected String          mLanguage;
    protected String 		  mNetworkType;
    private boolean			  mDoNotTrack;
    private boolean			  mCoppa;
    private PubMaticAdRequest.AWT_OPTION mAWT;


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
    public void setAdId(String adTagId) {
        mAdId = adTagId;
    }

    /**
     * You have to set the site id before making call to loadAd.
     *
     * @param siteId
     *            siteId to set
     */
    public void setSiteId(String siteId) {
        mSiteId = siteId;
    }

    /**
     * You have to set the Publisher id before making a call to loadAd.
     *
     * @param publisherId
     *            publisherId The publisher id provided by PubMatic.
     */
    public void setPubId(String publisherId) {
        mPubId = publisherId;
    }

    public RS getRs() {
        return mRs;
    }

    public void setRs(RS mRs) {
        this.mRs = mRs;
    }

    public AD_TYPE getAdType() {
        return mAdType;
    }

    public void setAdType(AD_TYPE mAdType) {
        this.mAdType = mAdType;
    }

    public int getAdHeight() {
        return mAdHeight;
    }

    public void setAdHeight(int mAdHeight) {
        this.mAdHeight = mAdHeight;
    }

    public int getAdWidth() {
        return mAdWidth;
    }

    public void setAdWidth(int mAdWidth) {
        this.mAdWidth = mAdWidth;
    }

    public boolean isInIFrame() {
        return mInIFrame;
    }

    public void setInIFrame(boolean mInIFrame) {
        this.mInIFrame = mInIFrame;
    }

    public String getAdNetwork() {
        return mAdNetwork;
    }

    public void setAdNetwork(String mAdNetwork) {
        this.mAdNetwork = mAdNetwork;
    }

    public AD_VISIBILITY getAdVisibility() {
        return mAdVisibility;
    }

    public void setAdVisibility(AD_VISIBILITY mAdVisibility) {
        this.mAdVisibility = mAdVisibility;
    }

    public String getIABCategory() {
        return mIABCategory;
    }

    public void setIABCategory(String mIABCategory) {
        this.mIABCategory = mIABCategory;
    }

    public boolean isDoNotTrack() {
        return mDoNotTrack;
    }

    public void setDoNotTrack(boolean mDoNotTrack) {
        this.mDoNotTrack = mDoNotTrack;
    }

    public boolean isCoppa() {
        return mCoppa;
    }

    public void setCoppa(boolean mCoppa) {
        this.mCoppa = mCoppa;
    }

    public AWT_OPTION getAWT() {
        return mAWT;
    }

    public void setAWT(AWT_OPTION mAWT) {
        this.mAWT = mAWT;
    }

    public String getPMZoneId() {
        return mPMZoneId;
    }

    public void setPMZoneId(String mPMZoneId) {
        this.mPMZoneId = mPMZoneId;
    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String mAppName) {
        this.mAppName = mAppName;
    }

    public String getStoreURL() {
        return mStoreURL;
    }

    public void setStoreURL(String mStoreURL) {
        this.mStoreURL = mStoreURL;
    }

    public String getAid() {
        return mAid;
    }

    public void setAid(String mAid) {
        this.mAid = mAid;
    }

    public String getAppDomain() {
        return mAppDomain;
    }

    public void setAppDomain(String mAppDomain) {
        this.mAppDomain = mAppDomain;
    }

    public String getAppCategory() {
        return mAppCategory;
    }

    public void setAppCategory(String mAppCategory) {
        this.mAppCategory = mAppCategory;
    }

    public void isApplicationPaid(boolean mPaid) {
        this.mPaid = mPaid;
    }

    public int getAdRefreshRate() {
        return mAdRefreshRate;
    }

    public void setAdRefreshRate(int mAdRefreshRate) {
        this.mAdRefreshRate = mAdRefreshRate;
    }

    public int getOrmmaComplianceLevel() {
        return mOrmmaComplianceLevel;
    }

    public void setOrmmaComplianceLevel(int mOrmmaComplianceLevel) {
        this.mOrmmaComplianceLevel = mOrmmaComplianceLevel;
    }

    public String getAdOrientation() {
        return mAdOrientation;
    }

    public void setAdOrientation(String mAdOrientation) {
        this.mAdOrientation = mAdOrientation;
    }

    public int getDeviceOrientation() {
        return mDeviceOrientation;
    }

    public void setDeviceOrientation(int mDeviceOrientation) {
        this.mDeviceOrientation = mDeviceOrientation;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location mLocation) {
        this.mLocation = mLocation;
    }

    public String getState() {
        return mState;
    }

    public void setState(String mState) {
        this.mState = mState;
    }

    public String getBirthYear() {
        return mBirthYear;
    }

    public void setBirthYear(String mBirthYear) {
        this.mBirthYear = mBirthYear;
    }

    public ETHNICITY getEthnicity() {
        return mEthnicity;
    }

    public void setEthnicity(ETHNICITY mEthnicity) {
        this.mEthnicity = mEthnicity;
    }

    public String getIncome() {
        return mIncome;
    }

    public void setIncome(String mIncome) {
        this.mIncome = mIncome;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String mLanguage) {
        this.mLanguage = mLanguage;
    }

}
