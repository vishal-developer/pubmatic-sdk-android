package com.pubmatic.sdk.common.phoenix;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdvertisingIdClient;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.pubmatic.PubMaticUtils;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by shrawangupta on 11/02/16.
 */
public abstract class PhoenixAdRequest extends AdRequest {

    protected Context         mContext;
    protected String          mImpressionId;
    protected String          mAdUnitId;
    private   int             mRequestType = -1;
    private   boolean         mDebugEnable = false;
    private PM_AD_POSITION    mAdPosition = PM_AD_POSITION.UNKNOWN;

    public PM_AD_POSITION getAdPosition() {
        return mAdPosition;
    }

    public void setAdPosition(PM_AD_POSITION adPosition) {
        this.mAdPosition = adPosition;
    }

    public boolean isDebugEnable() {
        return mDebugEnable;
    }

    public void setDebugEnable(boolean debugEnable) {
        this.mDebugEnable = debugEnable;
    }

    public int getRequestType() {
        return mRequestType;
    }

    public String getAdUnitId() {
        return mAdUnitId;
    }

    public void setAdUnitId(String mAdUnitId) {
        this.mAdUnitId = mAdUnitId;
    }


    public String getImpressionId() {
        return mImpressionId;
    }

    public void setImpressionId(String mImpressionId) {
        this.mImpressionId = mImpressionId;
    }

    public enum PM_AD_POSITION {
        UNKNOWN, ABOVE_FOLD, BELOW_FOLD, PARTIALLY_ABOVE_FOLD
    }

    /**
     * Ad request type. Can be set multiple values using pipe/OR opertaor (|).
     * Example: setRequestType(REQUEST_TYPE.IMAGE|REQUEST_TYPE.TEXT);
     * @param requestType
     */
    protected void setRequestType(int requestType) {
        this.mRequestType = requestType;
    }

    public static class REQUEST_TYPE {
        public static final int FLASH       = 1;
        public static final int IMAGE       = 2;
        public static final int NATIVE      = 4;
        public static final int TEXT        = 8;
        public static final int THIRD_PARTY = 16;
        public static final int VIDEO       = 32;
        public static final int RICH_MEDIA  = 64;
    }

    public abstract void setAttributes(AttributeSet attr);

    protected PhoenixAdRequest(Context context) {
        super(CommonConstants.CHANNEL.PHOENIX, context);
        mContext = context;
    }

    @Override
    public String getAdServerURL() {
        return TextUtils.isEmpty(mBaseUrl) ? CommonConstants.PHOENIX_AD_NETWORK_URL : mBaseUrl;
    }

    @Override
    public boolean checkMandatoryParams() {
        return false;
    }

    @Override
    protected void initializeDefaultParams(Context context) {

    }

    @Override
    public void copyRequestParams(AdRequest adRequest) {
        if (adRequest != null && adRequest instanceof PhoenixAdRequest) {
            if (TextUtils.isEmpty(mAdUnitId))
                this.mAdUnitId = ((PhoenixAdRequest) adRequest).mAdUnitId;
            if (TextUtils.isEmpty(mImpressionId))
                this.mImpressionId = ((PhoenixAdRequest) adRequest).mImpressionId;
            if (getWidth() <= 0)
                setWidth(adRequest.getWidth());
            if (getHeight() <= 0)
                setHeight(adRequest.getHeight());
        }
    }

    @Override
    protected void setupPostData() {

        super.setupPostData();
        //Append custom parameters. It's representation is different from PubMatic channel.
        if(mCustomParams!=null && !mCustomParams.isEmpty()) {
            Set<String> set = mCustomParams.keySet();
            Iterator<String> iterator = set.iterator();
            StringBuffer sb = null;
            while(iterator.hasNext()) {
                String key = iterator.next();
                List<String> valueList = mCustomParams.get(key);
                if(valueList!=null && !valueList.isEmpty()) {
                    if(sb==null)
                        sb = new StringBuffer(key+"=");
                    else
                        sb.append("&"+key+"=");

                    for(String s : valueList) {
                        if(sb.toString().endsWith("="))
                            sb.append(s);
                        else
                            sb.append(","+s);
                    }

                }
            }
            putPostData(PhoenixConstants.GLOBAL_KEYWORD_PARAM,sb.toString());
        }

        switch (mAdPosition) {
            case ABOVE_FOLD:
                putPostData(PhoenixConstants.VISIBLE_AD_POSITION_PARAM,         String.valueOf(1));
                break;
            case BELOW_FOLD:
                putPostData(PhoenixConstants.VISIBLE_AD_POSITION_PARAM,         String.valueOf(2));
                break;
            case PARTIALLY_ABOVE_FOLD:
                putPostData(PhoenixConstants.VISIBLE_AD_POSITION_PARAM,         String.valueOf(3));
                break;
        }

        if(isDebugEnable())
            putPostData(PhoenixConstants.DEBUG_PARAM,         String.valueOf(1));

        putPostData(PhoenixConstants.SOURCE_PARAM,         String.valueOf(3));
        PhoenixDeviceInformation deviceInfo = PhoenixDeviceInformation.getInstance(mContext);

        try {

            putPostData(PhoenixConstants.AD_UNIT_PARAM,         mAdUnitId);
            putPostData(PhoenixConstants.IMPRESSION_ID_PARAM,   mImpressionId);

            putPostData(PhoenixConstants.RANDOM_NUMBER_PARAM,   String.valueOf(PhoenixDeviceInformation.getRandomNumber()));
            putPostData(PhoenixConstants.TIME_STAMP_PARAM,      String.valueOf(PhoenixDeviceInformation.getCurrentTime()));

            if (deviceInfo.mPageURL != null) {
                putPostData(PhoenixConstants.PAGE_URL_PARAM,    URLEncoder.encode(
                                                                deviceInfo.mPageURL,
                                                                PhoenixConstants.URL_ENCODING));
                putPostData(PhoenixConstants.SCREEN_PARAM,      deviceInfo.mDeviceScreenResolution);
                putPostData(PhoenixConstants.TIME_ZONE_PARAM,   PhoenixDeviceInformation.getTimeZoneOffset());

            }
            // Setting js
            putPostData(PhoenixConstants.IN_IFRAME_PARAM,       String.valueOf(PhoenixDeviceInformation.mInIframe));


            // Set the location
            if (mLocation != null) {
                putPostData(PhoenixConstants.LATITUDE_PARAM,
                        String.valueOf(mLocation.getLatitude()));
                putPostData(PhoenixConstants.LONGITUDE_PARAM,
                        String.valueOf(mLocation.getLongitude()));
                putPostData(PhoenixConstants.LOCATION_SOURCE_PARAM,
                        String.valueOf(mLocation.getProvider()));
            }

            // --------------- SSP related parameters ---------------
            if(!TextUtils.isEmpty(mIABCategory))
                putPostData(PhoenixConstants.IAP_CATEGORY_PARAM, mIABCategory);

            if(!TextUtils.isEmpty(mAid))
                putPostData(PhoenixConstants.APP_ID_PARAM, mAid);

            // Setting carrier
            if (deviceInfo.mCarrierName != null) {
                putPostData(PhoenixConstants.CARRIER_PARAM, URLEncoder.encode(
                        deviceInfo.mCarrierName,
                        PhoenixConstants.URL_ENCODING));
            }

            if (deviceInfo.mApplicationName != null) {
                putPostData(PhoenixConstants.APP_NAME_PARAM, URLEncoder.encode(
                        deviceInfo.mApplicationName,
                        PhoenixConstants.URL_ENCODING));
            }

            if (deviceInfo.mPackageName != null) {
                putPostData(PhoenixConstants.BUNDLE_PARAM, URLEncoder.encode(
                        deviceInfo.mPackageName,
                        PhoenixConstants.URL_ENCODING));
            }

            if (deviceInfo.mApplicationVersion != null) {
                putPostData(PhoenixConstants.APP_VERSION_PARAM, URLEncoder.encode(
                        deviceInfo.mApplicationVersion,
                        PhoenixConstants.URL_ENCODING));
            }

            //
            //Send Advertisement ID
            AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.refreshAdvertisingInfo(mContext);
            if(adInfo!=null) {

                if (isAndoridAidEnabled() && !TextUtils.isEmpty(adInfo.getId())) {
                    putPostData(PhoenixConstants.UDID_PARAM, PubMaticUtils.sha1(adInfo.getId()));
                    putPostData(PhoenixConstants.UDID_TYPE_PARAM, String.valueOf(9));//9 - Android Advertising ID
                    putPostData(PhoenixConstants.UDID_HASH_PARAM, String.valueOf(0));//0 - raw udid
                }
			/*
			 * Pass dnt=1 if user have enabled Opt-Out of interest based ads in
			 * Google settings in Android device
			 */
                putPostData(PhoenixConstants.DNT_PARAM, String.valueOf(adInfo.isLimitAdTrackingEnabled() == true ? 1 : 0));
            } else if(mContext!=null){
                //Send Android ID
                putPostData(PhoenixConstants.UDID_PARAM, PhoenixUtils.getUdidFromContext(mContext));
                putPostData(PhoenixConstants.UDID_TYPE_PARAM, String.valueOf(3));//9 - Android ID
                putPostData(PhoenixConstants.UDID_HASH_PARAM, String.valueOf(2));//0 - SHA1
            }

            // Setting js
            putPostData(PhoenixConstants.JS_PARAM, String.valueOf(1));
            putPostData(PhoenixConstants.APP_API_PARAM, "3::4::5");
            putPostData(PhoenixConstants.NETWORK_TYPE_PARAM, PubMaticUtils.getNetworkType(mContext));

            if(!TextUtils.isEmpty(mStoreURL))
                putPostData(PhoenixConstants.STORE_URL_PARAM, mStoreURL);


            //Set the awt parameter
            switch (mAWT) {
                case WRAPPED_IN_IFRAME:
                    putPostData(PhoenixConstants.AWT_PARAM, String.valueOf(1));
                    break;
                case WRAPPED_IN_JS:
                    putPostData(PhoenixConstants.AWT_PARAM, String.valueOf(2));
                    break;
            }

            switch (mSecureFlag) {
                case SECURE:
                    putPostData(PhoenixConstants.SECURE_FLAG_PARAM, String.valueOf(1));
                    break;
                case NON_SECURE:
                    putPostData(PhoenixConstants.SECURE_FLAG_PARAM, String.valueOf(0));
                    break;
            }

            putPostData(PhoenixConstants.NETWORK_TYPE_PARAM, PhoenixUtils.getNetworkType(mContext));

            // Setting adOrientation
            if(!TextUtils.isEmpty(mAdOrientation))
                putPostData(PhoenixConstants.AD_ORIENTATION_PARAM, mAdOrientation);

            // Setting deviceOrientation
            putPostData(PhoenixConstants.DEVICE_ORIENTATION_PARAM, String.valueOf(getDeviceOrientation(mContext)));

            if(!TextUtils.isEmpty(mPMZoneId))
                putPostData(PhoenixConstants.PM_ZONE_ID_PARAM, mPMZoneId);

            if(!TextUtils.isEmpty(mAppCategory))
                putPostData(PhoenixConstants.APP_CATEGORY_PARAM, mAppCategory);

            if(!TextUtils.isEmpty(mAppDomain))
                putPostData(PhoenixConstants.APP_DOMAIN_PARAM, mAppDomain);

            putPostData(PhoenixConstants.APP_PAID_PARAM, String.valueOf(mPaid ? 1 : 0));

            if(!TextUtils.isEmpty(mSiteCode))
                putPostData(PhoenixConstants.SITE_CODE_PARAM, mSiteCode);

            if(!TextUtils.isEmpty(mAdTruth))
                putPostData(PhoenixConstants.AD_TRUTH_PARAM, mAdTruth);

            if(!TextUtils.isEmpty(mAdFloor))
                putPostData(PhoenixConstants.AD_FLOOR_PARAM, mAdFloor);

            if(!TextUtils.isEmpty(mBlockCreativeAttr))
                putPostData(PhoenixConstants.BATTR_PARAM, mBlockCreativeAttr);

            if(!TextUtils.isEmpty(mBlockAdDomain))
                putPostData(PhoenixConstants.BLK_ADV_DOMAIN_PARAM, mBlockAdDomain);


            if(!TextUtils.isEmpty(mBlockIabCategory))
                putPostData(PhoenixConstants.BLK_IAB_CATEG_PARAM, mBlockIabCategory);


            if(!TextUtils.isEmpty(mBlockAdIds))
                putPostData(PhoenixConstants.BLK_ADV_IDS_PARAM, mBlockAdIds);


            if(!TextUtils.isEmpty(mBlockDomainIds))
                putPostData(PhoenixConstants.BLK_DOMAIN_IDS_PARAM, mBlockDomainIds);

            putPostData(PhoenixConstants.COPPA_PARAM, String.valueOf(mCoppa==true? 1 : 0));

        } catch (Exception e) {

        }
    }


    @Override
    public void createRequest(Context context) {
        mPostData		= null;
        initializeDefaultParams(context);
        setupPostData();
    }


    public int getDeviceOrientation(Context context) {
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
            return 0;
        return 1;
    }

    //SSP related parameters
    protected String          mIABCategory = null;
    protected String          mPMZoneId;
    protected String          mStoreURL;
    protected String          mAid;
    protected String          mAppCategory;
    protected String          mAppDomain;
    protected boolean         mPaid;
    protected int             mAdRefreshRate;
    protected String          mAdOrientation;
    private boolean			  mCoppa;
    protected AWT_OPTION      mAWT = AWT_OPTION.DEFAULT;
    protected SECURE_FLAG     mSecureFlag = SECURE_FLAG.DEFAULT;
    protected String          mBlockAdDomain;
    protected String          mBlockIabCategory;
    protected String          mBlockAdIds;
    protected String          mBlockDomainIds;
    protected String          mAdTruth;
    protected String          mAdFloor;

    public String getSiteCode() {
        return mSiteCode;
    }

    public void setSiteCode(String mSiteCode) {
        this.mSiteCode = mSiteCode;
    }

    protected String          mSiteCode;

    public SECURE_FLAG getSecureFlag() {
        return mSecureFlag;
    }

    public void setSecureFlag(SECURE_FLAG secureFlag) {
        this.mSecureFlag = secureFlag;
    }

    public String getAdFloor() {
        return mAdFloor;
    }

    public void setAdFloor(String mAdFloor) {
        this.mAdFloor = mAdFloor;
    }

    public String getAdTruth() {
        return mAdTruth;
    }

    public void setAdTruth(String mAdTruth) {
        this.mAdTruth = mAdTruth;
    }

    public String getBlockCreativeAttr() {
        return mBlockCreativeAttr;
    }

    public void setBlockCreativeAttr(String mBlockCreativeAttr) {
        this.mBlockCreativeAttr = mBlockCreativeAttr;
    }

    protected String          mBlockCreativeAttr;


    public String getBlockDomainIds() {
        return mBlockDomainIds;
    }

    public void setBlockDomainIds(String mBlockDomainIds) {
        this.mBlockDomainIds = mBlockDomainIds;
    }

    public String getBlockAdDomain() {
        return mBlockAdDomain;
    }

    public void setBlockAdDomain(String mBlockAdDomain) {
        this.mBlockAdDomain = mBlockAdDomain;
    }


    public String getBlockIabCategory() {
        return mBlockIabCategory;
    }

    public void setBlockIabCategory(String mBlockIabCategory) {
        this.mBlockIabCategory = mBlockIabCategory;
    }

    public String getBlockAdIds() {
        return mBlockAdIds;
    }

    public void setBlockAdIds(String mBlockAdIds) {
        this.mBlockAdIds = mBlockAdIds;
    }

    public enum SECURE_FLAG { DEFAULT, SECURE, NON_SECURE }

    public enum AWT_OPTION { DEFAULT, WRAPPED_IN_IFRAME, WRAPPED_IN_JS }

    public String getIABCategory() {
        return mIABCategory;
    }

    public void setIABCategory(String iABCategory) {
        this.mIABCategory = iABCategory;
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

    public String getAdOrientation() {
        return mAdOrientation;
    }

    public void setAdOrientation(String mAdOrientation) {
        this.mAdOrientation = mAdOrientation;
    }


}
