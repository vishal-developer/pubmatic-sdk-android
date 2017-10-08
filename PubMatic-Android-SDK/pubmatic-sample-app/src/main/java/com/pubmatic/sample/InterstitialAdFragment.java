package com.pubmatic.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.webkit.WebView;

import com.pubmatic.sdk.banner.PMInterstitialAd;
import com.pubmatic.sdk.banner.pubmatic.PMInterstitialAdRequest;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.PMError;
import com.pubmatic.sdk.common.PubMaticSDK;
import com.pubmatic.sdk.common.pubmatic.PMAdRequest;

import java.util.LinkedHashMap;

import static com.pubmatic.sample.R.id.showBtn;

public class InterstitialAdFragment extends DialogFragment {

    private boolean isFragmentActive = true;

    private WebView webView =null;

    private ConfigurationManager.PLATFORM mPlatform;

    private LinkedHashMap<String, LinkedHashMap<String, String>> mSettings;

    private PMInterstitialAd mInterstitialAd;

    public InterstitialAdFragment()
    {
        boolean isAutoLocationDetectionChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_AUTO_LOCATION_DETECTION);
        PubMaticSDK.setLocationDetectionEnabled(isAutoLocationDetectionChecked);
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
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar);
        dialog.setContentView(R.layout.interstitial_template);


        Button loadAdBtn = (Button) dialog.findViewById(R.id.loadAdBtn);
        if(loadAdBtn!=null)
            loadAdBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadAd();
                }
            });

        Button showBtn = (Button) dialog.findViewById(R.id.showBtn);
        if(showBtn!=null)
            showBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAd();
                }
            });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void showAd() {
        if(mInterstitialAd!=null && mInterstitialAd.isReady()) {
            mInterstitialAd.showCloseButtonAfterDelay(3);
            mInterstitialAd.showForDuration(10);
        }
        else
            Toast.makeText(getActivity(), "Interstitial ad is not ready, please try after some time.", Toast.LENGTH_LONG).show();
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

            adRequest = PMInterstitialAdRequest.createPMInterstitialAdRequest(pubId, siteId, adId);

            // Configuration Parameters
            String androidAidEnabled = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED);
            ((PMInterstitialAdRequest)adRequest).setAndroidAidEnabled(Boolean.parseBoolean(androidAidEnabled));

            String coppa = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_TARGETTING_COPPA);
            if(!TextUtils.isEmpty(coppa))
                ((PMInterstitialAdRequest)adRequest).setCoppa(Boolean.parseBoolean(coppa));

            try
            {
                // Targeting Parameters
                String latitude = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LATITUDE);
                String longitude = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LONGITUDE);

                Location location = new Location("user");

                if(!TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(latitude)) {
                    location.setLatitude(Double.parseDouble(latitude));
                    location.setLongitude(Double.parseDouble(longitude));

                    adRequest.setLocation(location);
                }

                String city = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_CITY);
                if(!TextUtils.isEmpty(city))
                    ((PMInterstitialAdRequest)adRequest).setCity(city);

                String state = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STATE);
                if(!TextUtils.isEmpty(state))
                    ((PMInterstitialAdRequest)adRequest).setState(state);

                String zip = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ZIP);
                if(!TextUtils.isEmpty(zip))
                    ((PMInterstitialAdRequest)adRequest).setZip(zip);

                String appDomain = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_DOMAIN);
                if(!TextUtils.isEmpty(appDomain))
                    ((PMInterstitialAdRequest)adRequest).setAppDomain(appDomain);

                String appCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_CATEGORY);
                if(!TextUtils.isEmpty(appCategory))
                    ((PMInterstitialAdRequest)adRequest).setAppCategory(appCategory);

                String iabCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_IAB_CATEGORY);
                if(!TextUtils.isEmpty(iabCategory))
                    ((PMInterstitialAdRequest)adRequest).setIABCategory(iabCategory);

                String storeUrl = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STORE_URL);
                if(!TextUtils.isEmpty(storeUrl))
                    ((PMInterstitialAdRequest)adRequest).setStoreURL(storeUrl);

                String yearOfBirth = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH);
                if(!TextUtils.isEmpty(yearOfBirth))
                    ((PMInterstitialAdRequest)adRequest).setYearOfBirth(yearOfBirth);

                String income = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_INCOME);
                if(!TextUtils.isEmpty(income))
                    ((PMInterstitialAdRequest)adRequest).setIncome(income);

                String ethnicity = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ETHNICITY);
                if(!TextUtils.isEmpty(ethnicity))
                {
                    if(ethnicity.equalsIgnoreCase("HISPANIC"))
                        ((PMInterstitialAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.HISPANIC);
                    else if(ethnicity.equalsIgnoreCase("AFRICAN_AMERICAN"))
                        ((PMInterstitialAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.AFRICAN_AMERICAN);
                    else if(ethnicity.equalsIgnoreCase("CAUCASIAN"))
                        ((PMInterstitialAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.CAUCASIAN);
                    else if(ethnicity.equalsIgnoreCase("ASIAN_AMERICAN"))
                        ((PMInterstitialAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.ASIAN_AMERICAN);
                }

                String gender = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_GENDER);
                if(!TextUtils.isEmpty(gender))
                {
                    if(gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("M"))
                        ((PMInterstitialAdRequest)adRequest).setGender(PMAdRequest.GENDER.MALE);
                    else if(gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase("F"))
                        ((PMInterstitialAdRequest)adRequest).setGender(PMAdRequest.GENDER.FEMALE);
                    else if(gender.equalsIgnoreCase("Others") || gender.equalsIgnoreCase("O"))
                        ((PMInterstitialAdRequest)adRequest).setGender(PMAdRequest.GENDER.OTHER);
                }

                String dma = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_DMA);
                if(!TextUtils.isEmpty(dma))
                    ((PMInterstitialAdRequest)adRequest).setDMA(dma);

                String paid = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_PAID);
                if(!TextUtils.isEmpty(paid))
                    ((PMInterstitialAdRequest)adRequest).setApplicationPaid(Boolean.parseBoolean(paid));

                String ormaCompliance = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE);
                if(!TextUtils.isEmpty(ormaCompliance))
                    ((PMInterstitialAdRequest)adRequest).setOrmmaComplianceLevel(Integer.parseInt(ormaCompliance));
            }
            catch (Exception exception)
            {
                Log.e("Parse Error", exception.toString());
            }
        }

        return adRequest;
    }

    private void loadAd()
    {
        mInterstitialAd = new PMInterstitialAd(getActivity());

        mInterstitialAd.setRequestListener(new PMInterstitialAd.InterstitialAdListener.RequestListener() {

            @Override
            public void onFailedToReceiveAd(PMInterstitialAd ad, final PMError error) {
                try {
                    if(error!=null)
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
                    //dismiss();
                }
                catch(IllegalStateException e) {
                    return;
                }
            }

            @Override
            public void onReceivedAd(PMInterstitialAd adView) {
                Toast.makeText(getActivity(), "Interstitial ad is loaded. Press 'Show Ad' button now", Toast.LENGTH_LONG).show();
            }

        });

        mInterstitialAd.setActivityListener(new PMInterstitialAd.InterstitialAdListener.ActivityListener() {
            @Override
            public boolean onOpenUrl(PMInterstitialAd adView, String url) {
                return false;
            }

            @Override
            public void onLeavingApplication(PMInterstitialAd adView) {

            }

            @Override
            public boolean onCloseButtonClick(PMInterstitialAd adView) {
//                if(isFragmentActive) {
//                    dismiss();
                    return false;
//                } else
//                    return false;
            }
        });

        boolean isUseInternalBrowserChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_USE_INTERNAL_BROWSER);
        mInterstitialAd.setUseInternalBrowser(isUseInternalBrowserChecked);

        // Make the ad request to Server banner.loadRequest(adRequest);
        AdRequest adRequest = buildAdRequest();
        mInterstitialAd.loadRequest(adRequest);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(mInterstitialAd!=null)
            mInterstitialAd.destroy();
        dismiss();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        isFragmentActive = false;
        if(mInterstitialAd!=null)
            mInterstitialAd.destroy();
        mInterstitialAd = null;
    }
}
