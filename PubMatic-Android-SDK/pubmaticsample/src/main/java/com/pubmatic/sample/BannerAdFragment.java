package com.pubmatic.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
import com.pubmatic.sdk.common.pubmatic.PubMaticAdRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class BannerAdFragment extends DialogFragment implements PMBannerAdView.BannerAdViewDelegate.RequestListener {

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
        view = mInflater.inflate(R.layout.banner_template, null);

        loadAd(view);

        mBuilder.setView(view);

        Dialog dialog = mBuilder.create();

        Drawable drawable = new ColorDrawable(Color.BLACK);
        drawable.setAlpha(220);

        dialog.getWindow().setBackgroundDrawable(drawable);

        return dialog;
    }

    private void loadAd(View rootView)
    {
        PMBannerAdView banner = new PMBannerAdView(getActivity());
        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.banner_parent);

        AdRequest adRequest = null;
        int widthInt = 0;
        int heightInt = 0;

        try
        {
            String width = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_WIDTH);
            widthInt = Integer.parseInt(width);
            adRequest.setWidth(widthInt);

            String height = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_HEIGHT);
            heightInt = Integer.parseInt(height);
            adRequest.setHeight(heightInt);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        RelativeLayout.LayoutParams params;

        // Set layout height & width in pixels for banner ad view as per requirement
        if(widthInt != 0 && heightInt != 0)
            params = new RelativeLayout.LayoutParams(dpToPx(widthInt), dpToPx(heightInt));
        else
            params = new RelativeLayout.LayoutParams(dpToPx(320), dpToPx(50));

        params.setLayoutDirection(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(banner, params);

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

                    adRequest.setLocation(location);
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
            String androidAidEnabled = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED);
            adRequest.setAndroidAidEnabled(Boolean.parseBoolean(androidAidEnabled));

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

                    adRequest.setLocation(location);
                }

                String city = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_CITY);

                if(!city.equals("") && city != null)
                    ((PubMaticBannerAdRequest)adRequest).setCity(city);

                String state = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STATE);

                if(!state.equals("") && !state.equals(""))
                    ((PubMaticBannerAdRequest)adRequest).setState(state);

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
                    ((PubMaticBannerAdRequest)adRequest).setApplicationPaid(Boolean.parseBoolean(paid));

                String country = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_COUNTRY);

                if(!country.equals("") && country != null)
                    ((PubMaticBannerAdRequest)adRequest).setCountry(country);

                String awt = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_AWT);

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

                boolean isDoNotTrackChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_DO_NOT_TRACK);
                ((PubMaticAdRequest)adRequest).setDoNotTrack(isDoNotTrackChecked);

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

    @Override
    public void onFailedToReceiveAd(PMBannerAdView adView, Exception ex) {

    }

    @Override
    public void onReceivedAd(PMBannerAdView adView) {

    }

    @Override
    public void onReceivedThirdPartyRequest(PMBannerAdView adView, Map<String, String> properties, Map<String, String> parameters) {

    }
}
