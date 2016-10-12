package com.pubmatic.sdk.headerbidding;

import android.content.Context;

import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.pubmatic.PUBDeviceInformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PubMaticHBBannerRequest extends PubMaticBannerAdRequest {

    private List<String> appIabCategory;
    private List<String> sectionIabCategory;
    private List<String> pageIabCategory;

    private boolean hasPrivacyPolicy;
    private boolean paid;

    private List<PMBannerImpression> impressions;
    private Set<String> adSlotIdsHB;

    private PubMaticHBBannerRequest(Context context) {
        super(context);
        setAdServerURL(CommonConstants.HEADER_BIDDING_HASO_URL);
        impressions = new ArrayList<>();
        adSlotIdsHB = new HashSet<>();
    }

    private PubMaticHBBannerRequest(Context context, String pubId, PMBannerImpression impression) {
        this(context);
        this.mPubId = pubId;
        impressions.add(impression);
    }

    private PubMaticHBBannerRequest(Context context, String pubId, List<PMBannerImpression> impressions) {
        this(context);
        this.mPubId = pubId;
        this.impressions.addAll(impressions);
    }

    public static PubMaticHBBannerRequest initHBRequestForImpression(Context context, String pubId, PMBannerImpression impression) {
        return new PubMaticHBBannerRequest(context, pubId, impression);
    }

    public static PubMaticHBBannerRequest initHBRequestForImpression(Context context, String pubId, List<PMBannerImpression> impressions) {
        return new PubMaticHBBannerRequest(context, pubId, impressions);
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
        setAdServerURL("http://172.16.4.36:8000/openrtb/24");
        setUpUrlParams();
        setupPostData();
    }

    public List<String> getAppIABCategory() {
        return appIabCategory;
    }

    public void setAppIABCategory(List<String> appIabCategory) {
        this.appIabCategory = appIabCategory;
    }

    public List<String> getSectionIabCategory() {
        return sectionIabCategory;
    }

    public void setSectionIabCategory(List<String> sectionIabCategory) {
        this.sectionIabCategory = sectionIabCategory;
    }

    public List<String> getPageIabCategory() {
        return pageIabCategory;
    }

    public void setPageIabCategory(List<String> pageIabCategory) {
        this.pageIabCategory = pageIabCategory;
    }

    public boolean isHasPrivacyPolicy() {
        return hasPrivacyPolicy;
    }

    public void setHasPrivacyPolicy(boolean hasPrivacyPolicy) {
        this.hasPrivacyPolicy = hasPrivacyPolicy;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
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
            parentJsonObject.put("site", getSiteJson());
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

                bannerJsonObject.put("pos", 0);

                JSONArray formatJsonArray = new JSONArray();

                for(AdSize adSize : impression.getAdSizes())
                {
                    JSONObject adSizeJsonObject = new JSONObject();

                    adSizeJsonObject.put("w", adSize.getWidth());
                    adSizeJsonObject.put("h", adSize.getHeight());

                    formatJsonArray.put(adSizeJsonObject);
                }

                bannerJsonObject.put("format", formatJsonArray);

                // impression - banner
                impressionJsonObject.put("banner", bannerJsonObject);

                if(impression.isInterstitial())
                    impressionJsonObject.put("instl", true);

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
            appJsonObject.put("id", "");

            if(getAppName() != null && !getAppName().equals(""))
                appJsonObject.put("name", getAppName());
            else
                appJsonObject.put("name", pubDeviceInformation.mApplicationName);

            appJsonObject.put("bundle", pubDeviceInformation.mPackageName);
            //appJsonObject.put("domain", pubDeviceInformation.mDeviceIpAddress);
            //appJsonObject.put("domain", pubDeviceInformation.mDeviceIpAddress);
            appJsonObject.put("storeurl", getStoreURL());

            JSONArray catJsonArray = new JSONArray();
            if(appIabCategory != null) {
                for (int i = 0; i < appIabCategory.size(); i++)
                    catJsonArray.put(appIabCategory.get(i));
            }
            appJsonObject.put("cat", catJsonArray);

            JSONArray sectionCatJsonArray = new JSONArray();
            if(sectionIabCategory != null) {
                for (int i = 0; i < sectionIabCategory.size(); i++)
                    sectionCatJsonArray.put(sectionIabCategory.get(i));
            }

            appJsonObject.put("sectioncat", sectionCatJsonArray);

            JSONArray pageCatJsonArray = new JSONArray();
            if(pageIabCategory != null) {
                for (int i = 0; i < pageIabCategory.size(); i++)
                    pageCatJsonArray.put(pageIabCategory.get(i));
            }

            appJsonObject.put("pagecat", pageCatJsonArray);

            appJsonObject.put("ver", pubDeviceInformation.mApplicationVersion);


            if(isHasPrivacyPolicy())
                appJsonObject.put("privacypolicy", 1);
            else
                appJsonObject.put("privacypolicy", 0);

            if(isPaid())
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

    private JSONObject getSiteJson()
    {
        JSONObject siteJsonObject = new JSONObject();

        PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation.getInstance(mContext);

        try
        {
            //siteJsonObject.put("domain", pubDeviceInformation.mDeviceIpAddress);
            siteJsonObject.put("domain", "182.74.39.250");
            siteJsonObject.put("page", "http://172.16.4.36/ssWrapperTest.html");

            JSONObject publisherJsonObject = new JSONObject();
            publisherJsonObject.put("id", getPubId());

            siteJsonObject.put("publisher", publisherJsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return siteJsonObject;
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

            asJsonObject.put("SAVersion", pubDeviceInformation.mApplicationVersion);
            asJsonObject.put("kltstamp", pubDeviceInformation.mDeviceTimeStamp);
            asJsonObject.put("timezone", pubDeviceInformation.mDeviceTimeZone);
            asJsonObject.put("screenResolution", pubDeviceInformation.mDeviceScreenResolution);

            double ranreq = Math.random();
            asJsonObject.put("ranreq", ranreq);
            asJsonObject.put("pageURL", pubDeviceInformation.mPageURL);
            asJsonObject.put("refurl", "");
            asJsonObject.put("inIframe", String.valueOf(pubDeviceInformation.mInIframe));
            asJsonObject.put("kadpageurl", pubDeviceInformation.mPageURL);

            extensionJsonObject.put("as", asJsonObject);

            extJsonObject.put("extension", extensionJsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  extJsonObject;
    }

}
