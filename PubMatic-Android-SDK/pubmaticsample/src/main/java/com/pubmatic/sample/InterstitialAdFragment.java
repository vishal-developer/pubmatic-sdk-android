package com.pubmatic.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.PMInterstitialAdView;
import com.pubmatic.sdk.banner.mocean.MoceanBannerAdRequest;
import com.pubmatic.sdk.banner.phoenix.PhoenixBannerAdRequest;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.pubmatic.PubMaticAdRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sagar on 3/3/2017.
 */

public class InterstitialAdFragment extends DialogFragment {

    private AlertDialog.Builder mBuilder;
    private LayoutInflater mInflater;

    private ConfigurationManager.PLATFORM mPlatform;

    private LinkedHashMap<String, LinkedHashMap<String, String>> mSettings;

    private AdRequest adRequest;

    public InterstitialAdFragment() {}

    public InterstitialAdFragment(ConfigurationManager.PLATFORM platform, LinkedHashMap<String, LinkedHashMap<String, String>> settings)
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
        view = mInflater.inflate(R.layout.banner_template, null);

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

            try
            {
                // Targetting Parameters
                String latitude = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LATITUDE);
                String longitude = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LONGITUDE);

                Location location = new Location("");

                if(!latitude.equals("") && !longitude.equals(""))
                {
                    location.setLatitude(Double.parseDouble(latitude));
                    location.setLongitude(Double.parseDouble(longitude));

                    ((MoceanBannerAdRequest)adRequest).setLocation(location);
                }

                String city = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_CITY);

                if(!city.equals("") && !city.equals(""))
                    ((MoceanBannerAdRequest)adRequest).setCity(city);

                String zip = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ZIP);

                if(!zip.equals("") && !zip.equals(""))
                    ((MoceanBannerAdRequest)adRequest).setZip(zip);

                String dma = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_DMA);

                if(!dma.equals("") && !dma.equals(""))
                    ((MoceanBannerAdRequest)adRequest).setDMA(dma);

                String area = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_AREA);

                if(!area.equals("") && area != null)
                    ((MoceanBannerAdRequest)adRequest).setAreaCode(area);

                String age = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_AGE);

                if(!age.equals("") && !age.equals(""))
                    ((MoceanBannerAdRequest)adRequest).setAge(age);

                String birthday = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_BIRTHDAY);

                if(!birthday.equals("") && birthday != null)
                    ((MoceanBannerAdRequest)adRequest).setBirthDay(birthday);

                String gender = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_GENDER);

                if(!gender.equals("") && !gender.equals(""))
                    ((MoceanBannerAdRequest)adRequest).setGender(gender);

                /*String ethnicity = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ETHNICITY);

                if(!ethnicity.equals("") && ethnicity != null)
                    ((MoceanBannerAdRequest)adRequest).setEthnicity(ethnicity);*/

                /*String language = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LANGUAGE);

                if(!language.equals("") && language != null)
                    ((MoceanBannerAdRequest)adRequest).setLanguage(language);*/

                /*String over18 = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_OVER_18);

                if(!over18.equals("") && over18 != null)
                    ((MoceanBannerAdRequest)adRequest).setOver18(over18);*/

                /*String timeout = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_TIMEOUT);

                if(!timeout.equals("") && timeout != null)
                    ((MoceanBannerAdRequest)adRequest).setTimeout(Integer.parseInt(timeout));*/

                /*String keywords = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_KEYWORDS);

                if(!keywords.equals("") && keywords != null)
                    ((MoceanBannerAdRequest)adRequest).setKeywords(keywords);*/

            }
            catch (Exception exception)
            {
                Log.e("Parse Error", exception.toString());
            }
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

            adRequest = PubMaticBannerAdRequest.createPubMaticBannerAdRequest(getActivity(), pubId, siteId, adId);

            // Configuration Parameters
            String doNotTrack = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_DO_NOT_TRACK);
            ((PubMaticBannerAdRequest)adRequest).setDoNotTrack(Boolean.parseBoolean(doNotTrack));

            String androidAidEnabled = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED);
            ((PubMaticBannerAdRequest)adRequest).setAndroidAidEnabled(Boolean.parseBoolean(androidAidEnabled));

            String coppa = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_COPPA);
            ((PubMaticBannerAdRequest)adRequest).setCoppa(Boolean.parseBoolean(coppa));

            try
            {
                // Targetting Parameters
                String latitude = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LATITUDE);
                String longitude = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LONGITUDE);

                Location location = new Location("");

                if(!latitude.equals("") && !longitude.equals(""))
                {
                    location.setLatitude(Double.parseDouble(latitude));
                    location.setLongitude(Double.parseDouble(longitude));

                    ((PubMaticBannerAdRequest)adRequest).setLocation(location);
                }

                String city = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_CITY);

                if(!city.equals("") && city != null)
                    ((PubMaticBannerAdRequest)adRequest).setCity(city);

                String zip = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ZIP);

                if(!zip.equals("") && zip != null)
                    ((PubMaticBannerAdRequest)adRequest).setZip(zip);

                String appDomain = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_DOMAIN);

                if(!appDomain.equals("") && appDomain != null)
                    ((PubMaticBannerAdRequest)adRequest).setAppDomain(appDomain);

                String appCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_CATEGORY);

                if(!appCategory.equals("") && appCategory != null)
                    ((PubMaticBannerAdRequest)adRequest).setAppCategory(appCategory);

                String iabCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_IAB_CATEGORY);

                if(!iabCategory.equals("") && iabCategory != null)
                    ((PubMaticBannerAdRequest)adRequest).setIABCategory(iabCategory);

                String storeUrl = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STORE_URL);

                if(!storeUrl.equals("") && storeUrl != null)
                    ((PubMaticBannerAdRequest)adRequest).setStoreURL(storeUrl);

                String appName = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_NAME);

                if(!appName.equals("") && appName != null)
                    ((PubMaticBannerAdRequest)adRequest).setAppName(appName);

                String yearOfBirth = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH);

                if(!yearOfBirth.equals("") && yearOfBirth != null)
                    ((PubMaticBannerAdRequest)adRequest).setYearOfBirth(yearOfBirth);

                String income = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_INCOME);

                if(!income.equals("") && income != null)
                    ((PubMaticBannerAdRequest)adRequest).setIncome(income);

                String ethnicity = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ETHNICITY);

                if(!ethnicity.equals("") && ethnicity != null)
                {
                    if(ethnicity.equalsIgnoreCase("HISPANIC"))
                        ((PubMaticBannerAdRequest)adRequest).setEthnicity(PubMaticAdRequest.ETHNICITY.HISPANIC);
                    else if(ethnicity.equalsIgnoreCase("AFRICAN_AMERICAN"))
                        ((PubMaticBannerAdRequest)adRequest).setEthnicity(PubMaticAdRequest.ETHNICITY.AFRICAN_AMERICAN);
                    else if(ethnicity.equalsIgnoreCase("CAUCASIAN"))
                        ((PubMaticBannerAdRequest)adRequest).setEthnicity(PubMaticAdRequest.ETHNICITY.CAUCASIAN);
                    else if(ethnicity.equalsIgnoreCase("ASIAN_AMERICAN"))
                        ((PubMaticBannerAdRequest)adRequest).setEthnicity(PubMaticAdRequest.ETHNICITY.ASIAN_AMERICAN);
                }

                String gender = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_GENDER);

                if(!gender.equals("") && gender != null)
                    ((PubMaticBannerAdRequest)adRequest).setGender(gender);

                String dma = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_DMA);

                if(!dma.equals("") && dma != null)
                    ((PubMaticBannerAdRequest)adRequest).setDMA(dma);

                String paid = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_PAID);

                if(!paid.equals("") && paid != null)
                    ((PubMaticBannerAdRequest)adRequest).isApplicationPaid(Boolean.parseBoolean(paid));

                String country = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_COUNTRY);

                if(!country.equals("") && country != null)
                    ((PubMaticBannerAdRequest)adRequest).setCountry(country);

                String awt = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_COUNTRY);

                if(!awt.equals("") && awt != null)
                {
                    int awtOption = Integer.parseInt(awt);

                    if(awtOption == 0)
                        ((PubMaticBannerAdRequest)adRequest).setAWT(PubMaticAdRequest.AWT_OPTION.DEFAULT);
                    else if(awtOption == 1)
                        ((PubMaticBannerAdRequest)adRequest).setAWT(PubMaticAdRequest.AWT_OPTION.WRAPPED_IN_IFRAME);
                    else if(awtOption == 2)
                        ((PubMaticBannerAdRequest)adRequest).setAWT(PubMaticAdRequest.AWT_OPTION.WRAPPED_IN_JS);
                }

                String ormaCompliance = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE);

                if(!ormaCompliance.equals("") && ormaCompliance != null)
                    ((PubMaticBannerAdRequest)adRequest).setOrmmaComplianceLevel(Integer.parseInt(ormaCompliance));
            }
            catch (Exception exception)
            {
                Log.e("Parse Error", exception.toString());
            }
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

        final PMInterstitialAdView interstitialAdView = new PMInterstitialAdView(getActivity());

        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.banner_parent);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.setLayoutDirection(RelativeLayout.ALIGN_PARENT_TOP);
        }

        layout.addView(interstitialAdView, params);

        interstitialAdView.setUseInternalBrowser(true);

        interstitialAdView.setRequestListener(new PMBannerAdView.BannerAdViewDelegate.RequestListener() {
            @Override
            public void onFailedToReceiveAd(PMBannerAdView adView, Exception ex) {
                Toast.makeText(getActivity(), "Ad failed to load!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedAd(PMBannerAdView adView) {
                interstitialAdView.showInterstitial();
            }

            @Override
            public void onReceivedThirdPartyRequest(PMBannerAdView adView,
                                                    Map<String, String> properties,
                                                    Map<String, String> parameters) {
                // No operation
            }
        });

        boolean isUseInternalBrowserChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_USE_INTERNAL_BROWSER);
        interstitialAdView.setUseInternalBrowser(isUseInternalBrowserChecked);

        boolean isAutoLocationDetectionChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_AUTO_LOCATION_DETECTION);
        interstitialAdView.setLocationDetectionEnabled(isAutoLocationDetectionChecked);

        // Make the ad request to Server banner.execute(adRequest);
        interstitialAdView.execute(adRequest);
    }
}
