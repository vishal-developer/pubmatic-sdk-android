package com.pubmatic.sample;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Permission;
import java.util.LinkedHashMap;

public class HomeFragment extends Fragment {

    private TextView mPlatformSelector;
    private TextView mAdTypeSelector;

    private ConfigurationManager.PLATFORM mPlatform;
    private ConfigurationManager.AD_TYPE mAdType;

    private Dialog mDialog;

    private View mLoadAd;

    private final Handler handler = new Handler();

    public static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 112;
    public static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 113;

    LinkedHashMap<String, LinkedHashMap<String, String>> mSettings;

    private LinearLayout mSettingsParent;

    public HomeFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mPlatformSelector = (TextView) rootView.findViewById(R.id.home_platform);
        mAdTypeSelector = (TextView) rootView.findViewById(R.id.home_ad_type);

        mPlatformSelector.setOnClickListener(onPlatformChooserSelected);
        mPlatform = ConfigurationManager.PLATFORM.PUBMATIC;

        mAdTypeSelector.setOnClickListener(onAdTypeChooserSelected);
        mAdType = ConfigurationManager.AD_TYPE.BANNER;

        mSettingsParent = (LinearLayout) rootView.findViewById(R.id.home_settings_parent);

        mLoadAd = rootView.findViewById(R.id.home_load_ad);
        mLoadAd.setOnClickListener(onLoadAd);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getActivity() != null)
                {
                    int readExternalStoragePermissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);

                    if(readExternalStoragePermissionCheck != PackageManager.PERMISSION_GRANTED)
                        requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_READ_EXTERNAL_STORAGE);

                    int accessFineLocationPermissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

                    if(accessFineLocationPermissionCheck != PackageManager.PERMISSION_GRANTED)
                        requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
                }
            }
        }, 500);

        refreshSettings();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private TextView getSettingsHeaderView(String headerText)
    {
        TextView textView = new TextView(getActivity());
        textView.setText(headerText);

        textView.setTextSize(16);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setPadding(10, 10, 10, 10);

        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setBackgroundColor(getResources().getColor(R.color.grey_200));

        return textView;
    }

    private View getSettingItemsView(String settingsHeader, String setting, String defaultValue)
    {
        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.setting, null);

        TextView textView = (TextView) v.findViewById(R.id.home_setting_property);
        textView.setText(setting);

        EditText editText = (EditText) v.findViewById(R.id.home_setting_value);

        int id = PMUtils.getId(settingsHeader, setting);
        if(id != 0)
            editText.setId(id);

        editText.setText(defaultValue);
        editText.setSelection(defaultValue.length());
        editText.setTag(settingsHeader + ":" + setting);
        editText.setOnFocusChangeListener(onSettingFocusChangedListener);
        editText.getBackground().setColorFilter(getResources().getColor(android.R.color.holo_blue_dark), PorterDuff.Mode.SRC_IN);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        if(settingsHeader.equals(PMConstants.SETTINGS_HEADING_AD_TAG))
        {
            if(mPlatform == ConfigurationManager.PLATFORM.MOCEAN || mPlatform == ConfigurationManager.PLATFORM.PUBMATIC)
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        if(settingsHeader.equals((PMConstants.SETTINGS_HEADING_CONFIGURATION)))
        {
            if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_WIDTH) || setting.equals(PMConstants.SETTINGS_CONFIGURATION_HEIGHT ))
            {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        }

        if(settingsHeader.equals(PMConstants.SETTINGS_HEADING_TARGETTING))
        {
            if (setting.equals(PMConstants.SETTINGS_TARGETTING_LATITUDE)
                    || setting.equals(PMConstants.SETTINGS_TARGETTING_LONGITUDE)
                    || setting.equals(PMConstants.SETTINGS_TARGETTING_ZIP)
                    || setting.equals(PMConstants.SETTINGS_TARGETTING_AGE)
                    || setting.equals(PMConstants.SETTINGS_TARGETTING_INCOME)
                    || setting.equals(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH))
            {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        }

        return v;
    }

    private View.OnClickListener onPlatformChooserSelected = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            mDialog = new Dialog(getActivity());
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            mDialog.setContentView(R.layout.dialog_platform);

            View moceanPlatform = mDialog.findViewById(R.id.dialog_platform_mocean);
            moceanPlatform.setOnClickListener(onPlatformSelected);

            View pubmaticPlatform = mDialog.findViewById(R.id.dialog_platform_pubmatic);
            pubmaticPlatform.setOnClickListener(onPlatformSelected);

            mDialog.show();

        }
    };

    private View.OnClickListener onPlatformSelected = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            String platform = ((TextView)view).getText().toString();
            mPlatformSelector.setText(platform);

            if(platform.equals("Mocean"))
                mPlatform = ConfigurationManager.PLATFORM.MOCEAN;
            else if(platform.equals("PubMatic"))
                mPlatform = ConfigurationManager.PLATFORM.PUBMATIC;
            else if(platform.equals("Phoenix"))
                mPlatform = ConfigurationManager.PLATFORM.PHEONIX;
            else
                mPlatform = ConfigurationManager.PLATFORM.MOCEAN;

            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                refreshSettings();

            if(mDialog != null)
                mDialog.dismiss();
        }
    };

    private View.OnClickListener onAdTypeChooserSelected = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            mDialog = new Dialog(getActivity());
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            mDialog.setContentView(R.layout.dialog_ad_type);

            View bannerAdType = mDialog.findViewById(R.id.dialog_ad_type_banner);
            bannerAdType.setOnClickListener(onAdTypeSelected);

            View interstitialAdType = mDialog.findViewById(R.id.dialog_ad_type_interstitial);
            interstitialAdType.setOnClickListener(onAdTypeSelected);

            View nativeAdType = mDialog.findViewById(R.id.dialog_ad_type_native);
            nativeAdType.setOnClickListener(onAdTypeSelected);

            mDialog.show();

        }
    };

    private View.OnClickListener onAdTypeSelected = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            String adType = ((TextView)view).getText().toString();
            mAdTypeSelector.setText(adType);

            if(adType.equals("Banner"))
                mAdType = ConfigurationManager.AD_TYPE.BANNER;
            else if(adType.equals("Interstitial"))
                mAdType = ConfigurationManager.AD_TYPE.INTERSTITIAL;
            else if(adType.equals("Native"))
                mAdType = ConfigurationManager.AD_TYPE.NATIVE;
            else
                mAdType = ConfigurationManager.AD_TYPE.BANNER;

            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                refreshSettings();

            if(mDialog != null)
                mDialog.dismiss();

        }
    };

    public void refreshSettings()
    {
        mSettingsParent.removeAllViews();

        mSettings = ConfigurationManager.getInstance(getActivity()).getSettings(mPlatform, mAdType);

        for(String settingHeaderkey : mSettings.keySet())
        {
            Log.i("Setting Header", settingHeaderkey);
            Log.i("Setting header", "-------------------------------------------------------");

            View settingsHeaderView = getSettingsHeaderView(settingHeaderkey);

            if(settingHeaderkey != null)
                mSettingsParent.addView(settingsHeaderView);

            LinkedHashMap<String, String> setting = mSettings.get(settingHeaderkey);

            for(String settingKey : setting.keySet())
            {
                Log.i("Setting Key", settingKey);
                Log.i("Setting Value", setting.get(settingKey).toString());

                View settingView = getSettingItemsView(settingHeaderkey, settingKey, setting.get(settingKey).toString());

                if(settingView != null)
                    mSettingsParent.addView(settingView);
            }
        }

    }

    private View.OnFocusChangeListener onSettingFocusChangedListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if(!b)
            {
                String tag = (String) view.getTag();
                String value = ((EditText) view).getText().toString();

                String headerKey = tag.split(":")[0];
                String settingKey = tag.split(":")[1];

                mSettings.get(headerKey).put(settingKey, value);

                // Toast.makeText(getActivity(), tag + " : " + value, Toast.LENGTH_LONG).show();
            }
        }
    };

    private View.OnClickListener onLoadAd = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if(mAdType == ConfigurationManager.AD_TYPE.BANNER)
            {
                if(mPlatform == ConfigurationManager.PLATFORM.MOCEAN)
                {
                    if(checkMoceanAdTag())
                    {
                        getMoceanTargettingParameters();

                        BannerAdFragment bannerAdDialogFragment = new BannerAdFragment(mPlatform, mSettings);
                        bannerAdDialogFragment.show(getActivity().getFragmentManager(), "BannerAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter a zone", Toast.LENGTH_LONG).show();
                }
                else if(mPlatform == ConfigurationManager.PLATFORM.PUBMATIC)
                {
                    if(checkPubMaticAdTag())
                    {
                        getPubMaticConfigurationParameters();
                        getPubmaticTargettingParameters();

                        BannerAdFragment bannerAdDialogFragment = new BannerAdFragment(mPlatform, mSettings);
                        bannerAdDialogFragment.show(getActivity().getFragmentManager(), "BannerAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
                }
                /*else if(mPlatform == ConfigurationManager.PLATFORM.PHEONIX)
                {
                    EditText adUnitIdEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_AD_TAG + ":" + PMConstants.SETTINGS_AD_TAG_AD_UNIT_ID);
                    String adUnitId = adUnitIdEt.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).put(PMConstants.SETTINGS_AD_TAG_AD_UNIT_ID, adUnitId);

                    if(adUnitId != null && !adUnitId.equals(""))
                    {
                        BannerAdFragment bannerAdDialogFragment = new BannerAdFragment(mPlatform, mSettings);
                        bannerAdDialogFragment.show(getActivity().getFragmentManager(), "BannerAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter adUnitId", Toast.LENGTH_LONG).show();
                }*/
            }
            else if(mAdType == ConfigurationManager.AD_TYPE.INTERSTITIAL)
            {
                if(mPlatform == ConfigurationManager.PLATFORM.MOCEAN)
                {
                    if(checkMoceanAdTag())
                    {
                        getMoceanTargettingParameters();

                        InterstitialAdFragment interstitialAdFragment = new InterstitialAdFragment(mPlatform, mSettings);
                        interstitialAdFragment.show(getActivity().getFragmentManager(), "interstitialAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter a zone", Toast.LENGTH_LONG).show();
                }
                else if(mPlatform == ConfigurationManager.PLATFORM.PUBMATIC)
                {
                    if(checkPubMaticAdTag())
                    {
                        getPubMaticConfigurationParameters();
                        getPubmaticTargettingParameters();

                        InterstitialAdFragment interstitialAdFragment = new InterstitialAdFragment(mPlatform, mSettings);
                        interstitialAdFragment.show(getActivity().getFragmentManager(), "interstitialAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
                }
            }
            else if(mAdType == ConfigurationManager.AD_TYPE.NATIVE)
            {
                if(mPlatform == ConfigurationManager.PLATFORM.MOCEAN)
                {
                    if(checkMoceanAdTag())
                    {
                        getMoceanTargettingParameters();

                        NativeAdFragment nativeAdFragment = new NativeAdFragment(mPlatform, mSettings);
                        nativeAdFragment.show(getActivity().getFragmentManager(), "NativeAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter a zone", Toast.LENGTH_LONG).show();
                }
                else if(mPlatform == ConfigurationManager.PLATFORM.PUBMATIC)
                {
                    if(checkPubMaticAdTag())
                    {
                        getPubMaticConfigurationParameters();
                        getPubmaticTargettingParameters();

                        NativeAdFragment nativeAdFragment = new NativeAdFragment(mPlatform, mSettings);
                        nativeAdFragment.show(getActivity().getFragmentManager(), "NativeAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_READ_EXTERNAL_STORAGE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    refreshSettings();
                 else
                    Toast.makeText(getActivity(), "Permissions denied", Toast.LENGTH_SHORT).show();

                return;
            }
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    refreshSettings();
                else
                    Toast.makeText(getActivity(), "Permissions denied", Toast.LENGTH_SHORT).show();

                return;
            }
        }
    }

    private void getMoceanTargettingParameters()
    {
        EditText etLatitude = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_LATITUDE);
        String latitude = etLatitude.getText().toString();

        if(!latitude.equals(""))
            mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_LATITUDE, latitude);

        EditText etLongitude = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_LONGITUDE);
        String longitude = etLongitude.getText().toString();

        if(!longitude.equals(""))
            mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_LONGITUDE, longitude);

        EditText etCity = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_CITY);
        String city = etCity.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_CITY, city);

        EditText etZip = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_ZIP);
        String zip = etZip.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ZIP, zip);

        EditText etDMA = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_DMA);
        String dma = etDMA.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_DMA, dma);

        EditText etArea = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_AREA);
        String area = etArea.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_AREA, area);

        EditText etAge = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_AGE);
        String age = etAge.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_AGE, age);

        EditText etBirthday = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_BIRTHDAY);
        String birthday = etBirthday.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_BIRTHDAY, birthday);

        EditText etGender = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_GENDER);
        String gender = etGender.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_GENDER, gender);

        EditText etEthnicity = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_ETHNICITY);
        String ethnicity = etEthnicity.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ETHNICITY, ethnicity);

        EditText etLanguage = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_LANGUAGE);
        String language = etLanguage.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_LANGUAGE, language);

        EditText etOver18 = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_OVER_18);
        String over18 = etOver18.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_OVER_18, over18);

        EditText etTimeout = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_TIMEOUT);
        String timeout = etTimeout.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_TIMEOUT, timeout);

        EditText etKeywords = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_KEYWORDS);
        String keywords = etKeywords.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_KEYWORDS, keywords);
    }

    private void getPubmaticTargettingParameters()
    {
        EditText etLatitude = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_LATITUDE);
        String latitude = etLatitude.getText().toString();

        if(!latitude.equals(""))
            mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_LATITUDE, latitude);

        EditText etLongitude = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_LONGITUDE);
        String longitude = etLongitude.getText().toString();

        if(!longitude.equals(""))
            mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_LONGITUDE, longitude);

        EditText appCategoryEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_APP_CATEGORY);
        String appCategoryId = appCategoryEt.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_APP_CATEGORY, appCategoryId);

        EditText iabCategoryEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_IAB_CATEGORY);
        String iabCategoryId = iabCategoryEt.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_IAB_CATEGORY, iabCategoryId);

        EditText awtEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_AWT);
        String awt = awtEt.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_AWT, awt);

        EditText storeUrlEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_STORE_URL);
        String storeUrl = storeUrlEt.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_STORE_URL, storeUrl);

        EditText appNameEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_APP_NAME);
        String appName = appNameEt.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_APP_NAME, appName);

        EditText appDomainEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_APP_DOMAIN);
        String appDomain = appDomainEt.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_APP_DOMAIN, appDomain);

        EditText etCity = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_CITY);
        String city = etCity.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_CITY, city);

        EditText etZip = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_ZIP);
        String zip = etZip.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ZIP, zip);

        EditText etCountry = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_COUNTRY);
        String country = etCountry.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_COUNTRY, country);

        EditText etState = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_STATE);
        String state = etState.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_STATE, state);

        EditText etYearOfBirth = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH);
        String yearOfBirth = etYearOfBirth.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH, yearOfBirth);

        EditText etIncome = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_INCOME);
        String income = etIncome.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_INCOME, income);

        EditText etEthnicity = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_ETHNICITY);
        String ethnicity = etEthnicity.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ETHNICITY, ethnicity);

        EditText etGender = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_GENDER);
        String gender = etGender.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ETHNICITY, gender);

        EditText etDMA = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_DMA);
        String dma = etDMA.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_DMA, dma);

        EditText etLanguage = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_LANGUAGE);
        String language = etLanguage.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_LANGUAGE, language);

        EditText etOrmaCompliance = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE);
        String ormaCompliance = etOrmaCompliance.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE, ormaCompliance);

        EditText etPaid = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_PAID);
        String paid = etPaid.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_PAID, paid);
    }

    private boolean checkMoceanAdTag()
    {
        String zone = "";

        try
        {
            EditText et = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_AD_TAG + ":" + PMConstants.SETTINGS_AD_TAG_ZONE);
            zone = et.getText().toString();
            mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).put(PMConstants.SETTINGS_AD_TAG_ZONE, zone);
        }
        catch(Exception exception)
        {
            Log.i("CheckMoceanAdTag : ", exception.toString());
        }

        if(zone != null && !zone.equals(""))
            return true;
        else
            return false;
    }

    private boolean checkPubMaticAdTag()
    {
        String pubId = "";
        String siteId = "";
        String adId = "";

        try
        {
            EditText pubEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_AD_TAG + ":" + PMConstants.SETTINGS_AD_TAG_PUB_ID);
            pubId = pubEt.getText().toString();
            mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).put(PMConstants.SETTINGS_AD_TAG_PUB_ID, pubId);

            EditText siteEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_AD_TAG + ":" + PMConstants.SETTINGS_AD_TAG_SITE_ID);
            siteId = siteEt.getText().toString();
            mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).put(PMConstants.SETTINGS_AD_TAG_SITE_ID, siteId);

            EditText adEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_AD_TAG + ":" + PMConstants.SETTINGS_AD_TAG_AD_ID);
            adId = adEt.getText().toString();
            mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).put(PMConstants.SETTINGS_AD_TAG_AD_ID, adId);
        }
        catch(Exception exception)
        {
            Log.i("CheckPubMaticAdTag : ", exception.toString());
        }

        if(pubId != null && !pubId.equals("") && siteId != null && !siteId.equals("") && adId != null & !adId.equals(""))
            return true;
        else
            return false;
    }

    private void getPubMaticConfigurationParameters()
    {
        EditText androidAidEnabledEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_CONFIGURATION + ":" + PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED);
        String androidAidEnabled = androidAidEnabledEt.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).put(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED, androidAidEnabled);
    }
}
