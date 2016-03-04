package com.pubmatic.sdk.common.phoenix;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.pubmatic.PubMaticConstants;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by shrawangupta on 11/02/16.
 */
public abstract class PhoenixAdRequest extends AdRequest {

    protected Context         mContext;
    protected String          mImpressionId;
    protected String          mAdUnitId;
    private   int             mRequestType = -1;

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

    /**
     * Ad request type. Can be set multiple values using pipe/OR opertaor (|).
     * Example: setRequestType(REQUEST_TYPE.IMAGE|REQUEST_TYPE.TEXT);
     * @param requestType
     */
    public void setRequestType(int requestType) {
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
            while(iterator.hasNext()) {
                String key = iterator.next();
                List<String> valueList = mCustomParams.get(key);
                if(valueList!=null && !valueList.isEmpty()) {
                    StringBuffer sb = null;
                    for(String s : valueList) {
                        if(sb==null)
                            sb = new StringBuffer(s);
                        else
                            sb.append(","+s);
                    }
                    putPostData(key,sb.toString());
                }
            }
        }

        PhoenixDeviceInformation deviceInfo = PhoenixDeviceInformation.getInstance(mContext);

        try {

            putPostData(PhoenixConstants.AD_UNIT_PARAM,         mAdUnitId);
            putPostData(PhoenixConstants.IMPRESSION_ID_PARAM,   mImpressionId);
            if(mRequestType>0)
            putPostData(PhoenixConstants.REQUEST_TYPE_PARAM,    String.valueOf(mRequestType));

            putPostData(PhoenixConstants.RANDOM_NUMBER_PARAM,   String.valueOf(PhoenixDeviceInformation.getRandomNumber()));
            putPostData(PhoenixConstants.TIME_STAMP_PARAM,      String.valueOf(PhoenixDeviceInformation.getCurrentTime()));

            if (deviceInfo.mPageURL != null) {
                putPostData(PhoenixConstants.PAGE_URL_PARAM,    URLEncoder.encode(
                                                                deviceInfo.mPageURL,
                                                                PubMaticConstants.URL_ENCODING));
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
            }
        } catch (Exception e) {

        }
    }


    @Override
    public void createRequest(Context context) {
        mPostData		= null;
        initializeDefaultParams(context);
        setupPostData();
    }
}
