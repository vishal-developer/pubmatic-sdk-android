package com.pubmatic.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.mocean.MoceanBannerAdRequest;
import com.pubmatic.sdk.banner.phoenix.PhoenixBannerAdRequest;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.AdRequest;

import java.util.LinkedHashMap;

/**
 * Created by Sagar on 12/29/2016.
 */
public class BannerAdFragment extends DialogFragment {

    private AlertDialog.Builder mBuilder;
    private LayoutInflater mInflater;

    private ConfigurationManager.PLATFORM mPlatform;

    private LinkedHashMap<String, LinkedHashMap<String, String>> mSettings;

    public BannerAdFragment() {}

    public BannerAdFragment(ConfigurationManager.PLATFORM platform, LinkedHashMap<String, LinkedHashMap<String, String>> settings)
    {
        mPlatform = platform;
        mSettings = settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final View view;

        mBuilder = new AlertDialog.Builder(getActivity());

        mInflater = getActivity().getLayoutInflater();
        view = mInflater.inflate(R.layout.mocean_banner, null);

        loadAd(view);

        mBuilder.setView(view);

        Dialog dialog = mBuilder.create();

        Drawable drawable = new ColorDrawable(Color.BLACK);
        drawable.setAlpha(120);

        dialog.getWindow().setBackgroundDrawable(drawable);

        return dialog;
    }

    private void loadAd(View rootView)
    {
        PMBannerAdView banner = new PMBannerAdView(getActivity());
        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.banner_parent);

        // Set layout height & width in pixels for banner ad view as per requirement
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dpToPx(320), dpToPx(50));
        params.setLayoutDirection(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(banner, params);

        AdRequest adRequest;

        if(mPlatform == ConfigurationManager.PLATFORM.MOCEAN) {

            String zone = mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).get(PMConstants.SETTINGS_AD_TAG_ZONE);

            if(zone == null || zone.equals(""))
            {
                Toast.makeText(getActivity(), "Please enter a zone", Toast.LENGTH_LONG).show();
                return;
            }

            adRequest = MoceanBannerAdRequest.createMoceanBannerAdRequest(getActivity(), zone);

            String test = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_TEST);
            ((MoceanBannerAdRequest)adRequest).setTest(Boolean.parseBoolean(test));

            String ip = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_IP);
            ((MoceanBannerAdRequest)adRequest).setIp(ip);

            String city = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_CITY);
            ((MoceanBannerAdRequest)adRequest).setCity(city);

            String age = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_AGE);
            ((MoceanBannerAdRequest)adRequest).setAge(age);

            String gender = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_GENDER);
            ((MoceanBannerAdRequest)adRequest).setGender(gender);

            String isoRegion = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ISO_REGION);
            ((MoceanBannerAdRequest)adRequest).setIsoRegion(isoRegion);

            String zip = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ZIP);
            ((MoceanBannerAdRequest)adRequest).setZip(zip);

            String dma = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_DMA);
            ((MoceanBannerAdRequest)adRequest).setDMA(dma);
        }
        else if(mPlatform == ConfigurationManager.PLATFORM.PUBMATIC) {

            String pubId = mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).get(PMConstants.SETTINGS_AD_TAG_PUB_ID);
            String siteId = mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).get(PMConstants.SETTINGS_AD_TAG_SITE_ID);
            String adId = mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).get(PMConstants.SETTINGS_AD_TAG_AD_ID);

            if(pubId == null || pubId.equals("") || siteId == null || siteId.equals("") || adId == null || adId.equals(""))
            {
                Toast.makeText(getActivity(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
                return;
            }

            //adRequest = PubMaticBannerAdRequest.createPubMaticBannerAdRequest(getActivity(), "31400", "32504", "439662");
            adRequest = PubMaticBannerAdRequest.createPubMaticBannerAdRequest(getActivity(), pubId, siteId, adId);

            // Configuration Parameters
            String doNotTrack = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_DO_NOT_TRACK);
            ((PubMaticBannerAdRequest)adRequest).setDoNotTrack(Boolean.parseBoolean(doNotTrack));

            String androidAidEnabled = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED);
            ((PubMaticBannerAdRequest)adRequest).setAndroidAidEnabled(Boolean.parseBoolean(androidAidEnabled));

            String coppa = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_COPPA);
            ((PubMaticBannerAdRequest)adRequest).setCoppa(Boolean.parseBoolean(coppa));

            // Targetting Parameters
            String userAgent = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_USER_AGENT);
            ((PubMaticBannerAdRequest)adRequest).setUserAgent(userAgent);

            /*String location = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LOCATION);
            ((PubMaticBannerAdRequest)adRequest).setLocation(location);*/

            String appCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_CATEGORY);
            ((PubMaticBannerAdRequest)adRequest).setAppCategory(appCategory);

            String iabCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_IAB_CATEGORY);
            ((PubMaticBannerAdRequest)adRequest).setIABCategory(iabCategory);

            String storeUrl = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STORE_URL);
            ((PubMaticBannerAdRequest)adRequest).setStoreURL(storeUrl);

            String appName = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_NAME);
            ((PubMaticBannerAdRequest)adRequest).setAppName(appName);

            String appDomain = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_DOMAIN);
            ((PubMaticBannerAdRequest)adRequest).setAppDomain(appDomain);

            String city = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_CITY);
            ((PubMaticBannerAdRequest)adRequest).setCity(city);

            String zip = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ZIP);
            ((PubMaticBannerAdRequest)adRequest).setZip(zip);

            String country = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_COUNTRY);
            ((PubMaticBannerAdRequest)adRequest).setCountry(country);

            String yearOfBirth = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH);
            ((PubMaticBannerAdRequest)adRequest).setYearOfBirth(yearOfBirth);

            String income = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_INCOME);
            ((PubMaticBannerAdRequest)adRequest).setIncome(income);

            String dma = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_DMA);
            ((PubMaticBannerAdRequest)adRequest).setDMA(dma);

            String language = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LANGUAGE);
            ((PubMaticBannerAdRequest)adRequest).setLanguage(language);

            /*String ormaCompliance = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE);
            ((PubMaticBannerAdRequest)adRequest).setOrmmaComplianceLevel(Integer.parseInt(ormaCompliance));*/
        }
        else if(mPlatform == ConfigurationManager.PLATFORM.PHEONIX)
        {
            String adUnitId = mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).get(PMConstants.SETTINGS_AD_TAG_AD_UNIT_ID);

            if(adUnitId == null || adUnitId.equals(""))
            {
                Toast.makeText(getActivity(), "Please enter an ad unit id", Toast.LENGTH_LONG).show();
                return;
            }

            adRequest = PhoenixBannerAdRequest.createPhoenixBannerAdRequest(getActivity(), adUnitId, "DIV1");
        }
        else
            adRequest = MoceanBannerAdRequest.createMoceanBannerAdRequest(getActivity(), "88269");

        try
        {
            String width = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_WIDTH);
            adRequest.setWidth(Integer.parseInt(width));

            String height = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_HEIGHT);
            adRequest.setHeight(Integer.parseInt(height));
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        boolean isUseInternalBrowserChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_USE_INTERNAL_BROWSER);
        banner.setUseInternalBrowser(isUseInternalBrowserChecked);

        boolean isAutoLocationDetectionChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_AUTO_LOCATION_DETECTION);
        banner.setLocationDetectionEnabled(isAutoLocationDetectionChecked);

        // Make the ad request to Server banner.execute(adRequest);
        banner.execute(adRequest);
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density) * 3;
    }
}
