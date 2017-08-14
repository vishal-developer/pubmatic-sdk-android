package com.pubmatic.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.PMAdSize;
import com.pubmatic.sdk.common.pubmatic.PubMaticAdRequest;

import java.util.LinkedHashMap;
import java.util.Map;


public class BannerAdFragment extends DialogFragment implements PMBannerAdView.BannerAdViewDelegate.RequestListener {

    private AlertDialog.Builder mBuilder;
    private LayoutInflater mInflater;

    private ConfigurationManager.PLATFORM mPlatform;

    private LinkedHashMap<String, LinkedHashMap<String, String>> mSettings;

    PMBannerAdView mBanner;

    public BannerAdFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = this.getArguments();
        if(b.getSerializable("Settings") != null)
            mSettings = (LinkedHashMap<String, LinkedHashMap<String, String>>)b.getSerializable("Settings");

        if(b.getSerializable("Platform") != null)
            mPlatform = (ConfigurationManager.PLATFORM)b.getSerializable("Platform");
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

    private void loadAd(View rootView) {

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dpToPx(320), dpToPx(50));
        params.setLayoutDirection(RelativeLayout.CENTER_IN_PARENT);

        mBanner = new PMBannerAdView(getActivity());
        mBanner.setRequestListener(this);

        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.banner_parent);
        layout.addView(mBanner, params);

        //Optionally, Set banner properties
        setProperties();

        //Create Adrequest object and sets targeting parameters
        AdRequest adRequest = buildAdRequest();

        try
        {
            // Make the ad request to Server
            if(adRequest!=null)
                mBanner.execute(adRequest);
        } catch(IllegalArgumentException illegalArgumentException)
        {
            dismiss();
            illegalArgumentException.printStackTrace();
            Toast.makeText(getActivity(), "Please verify the mandatory ad request parameters", Toast.LENGTH_LONG).show();
        }
    }

    private void setProperties()
    {

        boolean isUseInternalBrowserChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_USE_INTERNAL_BROWSER);
        mBanner.setUseInternalBrowser(isUseInternalBrowserChecked);

        boolean isAutoLocationDetectionChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_AUTO_LOCATION_DETECTION);
        mBanner.setLocationDetectionEnabled(isAutoLocationDetectionChecked);

    }

    /**
     * This method creates an Adrequest object based on platform selection in UI. And it also sets
     * targeting parameters (provided from UI) in ad request object
     * @return
     */
    private AdRequest buildAdRequest()
    {
        AdRequest adRequest = null;
        if(mPlatform == ConfigurationManager.PLATFORM.PUBMATIC) {

            String pubId = mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).get(PMConstants.SETTINGS_AD_TAG_PUB_ID);
            String siteId = mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).get(PMConstants.SETTINGS_AD_TAG_SITE_ID);
            String adId = mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).get(PMConstants.SETTINGS_AD_TAG_AD_ID);

            if(pubId == null || pubId.equals("") || siteId == null || siteId.equals("") || adId == null || adId.equals(""))
            {
                Toast.makeText(getActivity(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
                return null;
            }

            adRequest = PubMaticBannerAdRequest.createPubMaticBannerAdRequest(getActivity(), pubId, siteId, adId);

            try
            {
                String width = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_WIDTH);
                int widthInt = Integer.parseInt(width);

                String height = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_HEIGHT);
                int heightInt = Integer.parseInt(height);

                ((PubMaticBannerAdRequest)adRequest).setAdSize(new PMAdSize(widthInt, heightInt));

                // Configuration Parameters
                String androidAidEnabled = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED);
                adRequest.setAndroidAidEnabled(Boolean.parseBoolean(androidAidEnabled));

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

                String yearOfBirth = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH);

                if(!yearOfBirth.equals("") && yearOfBirth != null)
                    ((PubMaticBannerAdRequest)adRequest).setYearOfBirth(yearOfBirth);

                String income = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_INCOME);

                if(!income.equals("") && income != null)
                    ((PubMaticBannerAdRequest)adRequest).setIncome(income);

                String ethnicity = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ETHNICITY);

                if(ethnicity != null && !ethnicity.equals(""))
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

                if(gender != null && !gender.equals(""))
                {
                    if(gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("M"))
                        ((PubMaticBannerAdRequest)adRequest).setGender(PubMaticAdRequest.GENDER.MALE);
                    else if(gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase("F"))
                        ((PubMaticBannerAdRequest)adRequest).setGender(PubMaticAdRequest.GENDER.FEMALE);
                    else if(gender.equalsIgnoreCase("Others") || gender.equalsIgnoreCase("O"))
                        ((PubMaticBannerAdRequest)adRequest).setGender(PubMaticAdRequest.GENDER.OTHER);
                }

                String dma = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_DMA);

                if(!dma.equals("") && dma != null)
                    ((PubMaticBannerAdRequest)adRequest).setDMA(dma);

                String paid = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_PAID);

                if(!paid.equals("") && paid != null)
                    ((PubMaticBannerAdRequest)adRequest).setApplicationPaid(Boolean.parseBoolean(paid));

                String coppa = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_COPPA);
                ((PubMaticBannerAdRequest)adRequest).setCoppa(Boolean.parseBoolean(coppa));

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

        }/*
        else
            adRequest = PubMaticBannerAdRequest.createPubMaticBannerAdRequest(getActivity(), pubId, siteId, adId);*/

        try
        {
            String adRefreshRate = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_AD_REFRESH_RATE);

            if(!TextUtils.isEmpty(adRefreshRate))
                mBanner.setUpdateInterval(Integer.parseInt(adRefreshRate));
        } catch(Exception exception)
        {

        }

        return adRequest;

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
    public void onFailedToReceiveAd(PMBannerAdView adView, int errorCode, final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
    }

    @Override
    public void onReceivedAd(PMBannerAdView adView) {

    }

    @Override
    public void onReceivedThirdPartyRequest(PMBannerAdView adView, Map<String, String> properties, Map<String, String> parameters) {

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        mBanner.destroy();
        mBanner = null;
    }
}
