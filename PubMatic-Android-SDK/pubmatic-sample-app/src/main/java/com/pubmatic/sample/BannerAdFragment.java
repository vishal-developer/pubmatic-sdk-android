package com.pubmatic.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.pubmatic.PMBannerAdRequest;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.PMAdSize;
import com.pubmatic.sdk.common.PMError;
import com.pubmatic.sdk.common.pubmatic.PMAdRequest;

import java.util.LinkedHashMap;


public class BannerAdFragment extends DialogFragment implements PMBannerAdView.BannerAdViewDelegate.RequestListener {

    private ConfigurationManager.PLATFORM mPlatform;

    private LinkedHashMap<String, LinkedHashMap<String, String>> mSettings;

    private PMBannerAdView mBanner;


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

        //Get Dialog from Builder & Set Dialog as a full screen
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            //this line is to add the softkeyboard
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        //Create view for dialog and attach it to Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.banner_template, null);
        builder.setView(view);

        //Create Dialog and Set the transparent black opacity for dialog
        Drawable drawable = new ColorDrawable(Color.WHITE);
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(drawable);

        //Load an Ad
        loadAd(view);

        return dialog;
    }

    /**
     *
     * @param rootView
     */
    private void loadAd(View rootView) {

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(PMUtils.dpToPx(320), PMUtils.dpToPx(50));
        params.setLayoutDirection(RelativeLayout.CENTER_IN_PARENT);

        mBanner = new PMBannerAdView(getActivity());
        mBanner.setRequestListener(this);

        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.banner_parent);
        layout.addView(mBanner, params);

        //Optionally, Set banner properties
        boolean isUseInternalBrowserChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_USE_INTERNAL_BROWSER);
        mBanner.setUseInternalBrowser(isUseInternalBrowserChecked);


        //Set Ad Refresh parameter on Banner ad view object with time provided in UI
        try
        {
            String adRefreshRate = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_AD_REFRESH_RATE);
            if(!TextUtils.isEmpty(adRefreshRate))
                mBanner.setUpdateInterval(Integer.parseInt(adRefreshRate));
        } catch(Exception exception) {
            Toast.makeText(getActivity(), "Ad Refresh not set. Please verify ad refresh parameter.", Toast.LENGTH_SHORT).show();
        }

        //Create AdRequest object and sets targeting parameters
        AdRequest adRequest = buildAdRequest();
        try
        {
            // Make the ad request to Server
            if(adRequest!=null)
                mBanner.loadRequest(adRequest);
            else {
                dismiss();
            }
        } catch(IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getActivity(), "Please verify the mandatory ad request parameters", Toast.LENGTH_LONG).show();
            dismiss();
            illegalArgumentException.printStackTrace();
        }
    }

    /**
     * This method creates an AdRequest object based on platform selection in UI. And it also sets
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

            adRequest = PMBannerAdRequest.createPMBannerAdRequest( pubId, siteId, adId);

            try
            {
                String width = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_WIDTH);
                int widthInt = Integer.parseInt(width);

                String height = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_HEIGHT);
                int heightInt = Integer.parseInt(height);

                ((PMBannerAdRequest)adRequest).setAdSize(new PMAdSize(widthInt, heightInt));

                // Configuration Parameters
                String androidAidEnabled = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED);
                adRequest.setAndroidAidEnabled(Boolean.parseBoolean(androidAidEnabled));

                // Targetting Parameters
                String latitude = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LATITUDE);
                String longitude = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LONGITUDE);

                Location location = new Location("user");

                if(!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude))
                {
                    location.setLatitude(Double.parseDouble(latitude));
                    location.setLongitude(Double.parseDouble(longitude));

                    adRequest.setLocation(location);
                }

                String city = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_CITY);

                if(!TextUtils.isEmpty(city))
                    ((PMBannerAdRequest)adRequest).setCity(city);

                String state = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STATE);

                if(!TextUtils.isEmpty(state))
                    ((PMBannerAdRequest)adRequest).setState(state);

                String zip = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ZIP);

                if(!TextUtils.isEmpty(zip))
                    ((PMBannerAdRequest)adRequest).setZip(zip);

                String appDomain = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_DOMAIN);

                if(!TextUtils.isEmpty(appDomain))
                    ((PMBannerAdRequest)adRequest).setAppDomain(appDomain);

                String appCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_CATEGORY);

                if(!TextUtils.isEmpty(appCategory))
                    ((PMBannerAdRequest)adRequest).setAppCategory(appCategory);

                String iabCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_IAB_CATEGORY);

                if(!TextUtils.isEmpty(iabCategory))
                    ((PMBannerAdRequest)adRequest).setIABCategory(iabCategory);

                String storeUrl = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STORE_URL);

                if(!TextUtils.isEmpty(storeUrl))
                    ((PMBannerAdRequest)adRequest).setStoreURL(storeUrl);

                String yearOfBirth = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH);

                if(!TextUtils.isEmpty(yearOfBirth))
                    ((PMBannerAdRequest)adRequest).setYearOfBirth(yearOfBirth);

                String income = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_INCOME);

                if(!TextUtils.isEmpty(income))
                    ((PMBannerAdRequest)adRequest).setIncome(income);

                String ethnicity = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ETHNICITY);

                if(!TextUtils.isEmpty(ethnicity))
                {
                    if(ethnicity.equalsIgnoreCase("HISPANIC"))
                        ((PMBannerAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.HISPANIC);
                    else if(ethnicity.equalsIgnoreCase("AFRICAN_AMERICAN"))
                        ((PMBannerAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.AFRICAN_AMERICAN);
                    else if(ethnicity.equalsIgnoreCase("CAUCASIAN"))
                        ((PMBannerAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.CAUCASIAN);
                    else if(ethnicity.equalsIgnoreCase("ASIAN_AMERICAN"))
                        ((PMBannerAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.ASIAN_AMERICAN);
                }

                String gender = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_GENDER);

                if(!TextUtils.isEmpty(gender))
                {
                    if(gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("M"))
                        ((PMBannerAdRequest)adRequest).setGender(PMAdRequest.GENDER.MALE);
                    else if(gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase("F"))
                        ((PMBannerAdRequest)adRequest).setGender(PMAdRequest.GENDER.FEMALE);
                    else if(gender.equalsIgnoreCase("Others") || gender.equalsIgnoreCase("O"))
                        ((PMBannerAdRequest)adRequest).setGender(PMAdRequest.GENDER.OTHER);
                }

                String dma = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_DMA);

                if(!TextUtils.isEmpty(dma))
                    ((PMBannerAdRequest)adRequest).setDMA(dma);

                String paid = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_PAID);

                if(!TextUtils.isEmpty(paid))
                    ((PMBannerAdRequest)adRequest).setApplicationPaid(Boolean.parseBoolean(paid));

                String coppa = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_COPPA);
                if(!TextUtils.isEmpty(coppa))
                    ((PMBannerAdRequest)adRequest).setCoppa(Boolean.parseBoolean(coppa));

                String ormaCompliance = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE);
                if(!TextUtils.isEmpty(ormaCompliance))
                    ((PMBannerAdRequest)adRequest).setOrmmaComplianceLevel(Integer.parseInt(ormaCompliance));

            }
            catch (Exception exception)
            {
                Log.e("Parse Error", exception.toString());
            }

        }

        return adRequest;

    }

    @Override
    public void onFailedToReceiveAd(PMBannerAdView adView, final PMError error) {

        try {
            if(error!=null)
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
            dismiss();
        }
        catch(IllegalStateException e) {
            return;
        }
    }

    @Override
    public void onReceivedAd(PMBannerAdView adView) {

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(mBanner!=null)
            mBanner.destroy();
        mBanner = null;
    }
}
