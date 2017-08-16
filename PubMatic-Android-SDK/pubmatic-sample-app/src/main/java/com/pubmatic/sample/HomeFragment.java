package com.pubmatic.sample;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedHashMap;

/**
 * This fragment loads 'mSettings' object in memory from Settings.json file in asset folder.
 * Any change in the UI values updates the 'mSettings' object in onFocusChangedListener.
 * 'mSettings' object gets passed to respective ad fragment when LoadAd button clicked.
 */
public class HomeFragment extends Fragment {

    private TextView mAdTypeSelector;

    private ConfigurationManager.PLATFORM mPlatform;
    private ConfigurationManager.AD_TYPE mAdType;

    private LinearLayout mSettingsParent;
    private Dialog mDialog;
    private View mLoadAd;

    private final Handler handler = new Handler();
    private LinkedHashMap<String, LinkedHashMap<String, String>> mSettings;

    private static final int MULTIPLE_PERMISSIONS_REQUEST_CODE = 12355;
    private static String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mPlatform = ConfigurationManager.PLATFORM.PUBMATIC;

        mAdTypeSelector = (TextView) rootView.findViewById(R.id.home_ad_type);
        mAdTypeSelector.setOnClickListener(onAdTypeChooserSelected);

        //Default type would be banner
        mAdType = ConfigurationManager.AD_TYPE.BANNER;

        mSettingsParent = (LinearLayout) rootView.findViewById(R.id.home_settings_parent);

        mLoadAd = rootView.findViewById(R.id.home_load_ad);
        mLoadAd.setOnClickListener(onLoadAd);

        //Invoke permission check after 500 millisec. It is required for CI invoke
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!hasPermissions(getActivity(), PERMISSIONS)){
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, MULTIPLE_PERMISSIONS_REQUEST_CODE);
                }
            }
        }, 500);

        //Load the default values of the parameters from Settings file inside asset folder
        refreshSettings();

        return rootView;
    }

    /**
     * It reads the settings.json file from asset folder and populates them in the screen UI.
     * Internally, it inflates the runtime UI and assign the id/properties before UI loading.
     */
    public void refreshSettings()
    {
        mSettingsParent.removeAllViews();

        mSettings = ConfigurationManager.getInstance(getContext()).getSettings(mPlatform, mAdType);

        //Add Header section in screen UI
        for(String settingHeaderkey : mSettings.keySet())
        {
            Log.i("Setting Header", settingHeaderkey);
            Log.i("Setting header", "-------------------------------------------------------");

            View settingsHeaderView = getSettingsHeaderView(settingHeaderkey);

            if(settingHeaderkey != null)
                mSettingsParent.addView(settingsHeaderView);

            LinkedHashMap<String, String> setting = mSettings.get(settingHeaderkey);

            //Add elements/parameters in screen UI
            for(String settingKey : setting.keySet())
            {
                Log.i("Setting Key", settingKey);
                Log.i("Setting Value", setting.get(settingKey).toString());

                //Get the element UI view for individual parameter
                View settingView = getSettingItemsView(settingHeaderkey, settingKey, setting.get(settingKey).toString());

                if(settingView != null)
                    mSettingsParent.addView(settingView);
            }
        }

    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Resturns the Header view
     * @param headerText
     * @return
     */
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

    /**
     *
     * @param settingsHeader
     * @param setting
     * @param defaultValue
     * @return
     */
    private View getSettingItemsView(String settingsHeader, String setting, String defaultValue)
    {
        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.setting, null);

        TextView textView = (TextView) v.findViewById(R.id.home_setting_property);
        textView.setText(setting);

        final EditText editText = (EditText) v.findViewById(R.id.home_setting_value);

        //Need to assign the id from xml file, as it is inflated runtime
        int id = PMUtils.getId(settingsHeader, setting);
        if(id != 0)
            editText.setId(id);

        //assign the basic properties of the edit text
        editText.setText(defaultValue);
        editText.setSelection(defaultValue.length());
        editText.setTag(settingsHeader + ":" + setting);
        //editText.setOnFocusChangeListener(onSettingFocusChangedListener);
        editText.getBackground().setColorFilter(getResources().getColor(android.R.color.holo_blue_dark), PorterDuff.Mode.SRC_IN);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String tag = (String) editText.getTag();
                String value = editText.getText().toString();

                String headerKey = tag.split(":")[0];
                String settingKey = tag.split(":")[1];

                mSettings.get(headerKey).put(settingKey, value);
            }
            @Override
            public void afterTextChanged(Editable newText) {

            }});

        //Set the keyboard type for respective parameters
        if(settingsHeader.equals(PMConstants.SETTINGS_HEADING_AD_TAG))
        {
            if(mPlatform == ConfigurationManager.PLATFORM.PUBMATIC)
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if(settingsHeader.equals((PMConstants.SETTINGS_HEADING_CONFIGURATION))) {
            if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_WIDTH) || setting.equals(PMConstants.SETTINGS_CONFIGURATION_HEIGHT ) || setting.equals(PMConstants.SETTINGS_CONFIGURATION_AD_REFRESH_RATE))
            {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        } else if(settingsHeader.equals(PMConstants.SETTINGS_HEADING_TARGETTING)) {
            if (setting.equals(PMConstants.SETTINGS_TARGETTING_ZIP)
                    || setting.equals(PMConstants.SETTINGS_TARGETTING_DMA)
                    || setting.equals(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE)
                    || setting.equals(PMConstants.SETTINGS_TARGETTING_AGE)
                    || setting.equals(PMConstants.SETTINGS_TARGETTING_INCOME)
                    || setting.equals(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH))
            {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_LATITUDE)
                || setting.equals(PMConstants.SETTINGS_TARGETTING_LONGITUDE))
            {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        }

        return v;
    }

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

    private View.OnClickListener onLoadAd = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if(mAdType == ConfigurationManager.AD_TYPE.BANNER)
            {
                if(mPlatform == ConfigurationManager.PLATFORM.PUBMATIC)
                {
                    if(checkPubMaticAdTag())
                    {
                        BannerAdFragment bannerAdDialogFragment = new BannerAdFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Settings",mSettings);
                        bundle.putSerializable("Platform", mPlatform);
                        bannerAdDialogFragment.setArguments(bundle);

                        bannerAdDialogFragment.show(getActivity().getFragmentManager(), "BannerAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
                }
            }
            else if(mAdType == ConfigurationManager.AD_TYPE.INTERSTITIAL)
            {
                if(mPlatform == ConfigurationManager.PLATFORM.PUBMATIC)
                {
                    if(checkPubMaticAdTag())
                    {
                        InterstitialAdFragment interstitialAdFragment = new InterstitialAdFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Settings",mSettings);
                        bundle.putSerializable("Platform", mPlatform);
                        interstitialAdFragment.setArguments(bundle);

                        interstitialAdFragment.show(getActivity().getFragmentManager(), "interstitialAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
                }
            }
            else if(mAdType == ConfigurationManager.AD_TYPE.NATIVE)
            {
                if(mPlatform == ConfigurationManager.PLATFORM.PUBMATIC)
                {
                    if(checkPubMaticAdTag())
                    {
                        NativeAdFragment nativeAdFragment = new NativeAdFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Settings",mSettings);
                        bundle.putSerializable("Platform", mPlatform);
                        nativeAdFragment.setArguments(bundle);

                        nativeAdFragment.show(getActivity().getFragmentManager(), "NativeAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    /**
     * It returns true if all mandatory parameters are set else returns false
     * @return
     */
    private boolean checkPubMaticAdTag()
    {
        String pubId = null;
        String siteId = null;
        String adId = null;

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

        if(TextUtils.isEmpty(pubId) || TextUtils.isEmpty(siteId) || TextUtils.isEmpty(adId))
            return false;
        else
            return true;
    }

    //------------------- Below methods are not in use, can be delted -------------------

    /**
     * Update 'mSetings' object in memory with the updated value, when focus
     * change happen to any UI element view
     */
    private View.OnFocusChangeListener onSettingFocusChangedListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasGainedFocus) {
            if(!hasGainedFocus)
            {
                String tag = (String) view.getTag();
                String value = ((EditText) view).getText().toString();

                String headerKey = tag.split(":")[0];
                String settingKey = tag.split(":")[1];

                mSettings.get(headerKey).put(settingKey, value);
            }
        }
    };

    /**
     * It loads the 'mSettings' memory object with the current values of the Configutaion UI section
     */
    private void getPubMaticConfigurationParameters()
    {
        if(mAdType == ConfigurationManager.AD_TYPE.BANNER) {
            EditText widthEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_CONFIGURATION + ":" + PMConstants.SETTINGS_CONFIGURATION_WIDTH);
            String width = widthEt.getText().toString();
            mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).put(PMConstants.SETTINGS_CONFIGURATION_WIDTH, width);

            EditText heightEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_CONFIGURATION + ":" + PMConstants.SETTINGS_CONFIGURATION_HEIGHT);
            String height = heightEt.getText().toString();
            mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).put(PMConstants.SETTINGS_CONFIGURATION_HEIGHT, height);

            EditText adRefreshRateEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_CONFIGURATION + ":" + PMConstants.SETTINGS_CONFIGURATION_AD_REFRESH_RATE);
            String adRefreshRate = adRefreshRateEt.getText().toString();
            mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).put(PMConstants.SETTINGS_CONFIGURATION_AD_REFRESH_RATE, adRefreshRate);
        }

        EditText androidAidEnabledEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_CONFIGURATION + ":" + PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED);
        String androidAidEnabled = androidAidEnabledEt.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).put(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED, androidAidEnabled);
    }

    /**
     * It loads the 'mSettings' memory object with the current values of the Targetting UI section
     */
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

        EditText storeUrlEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_STORE_URL);
        String storeUrl = storeUrlEt.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_STORE_URL, storeUrl);

        EditText appDomainEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_APP_DOMAIN);
        String appDomain = appDomainEt.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_APP_DOMAIN, appDomain);

        EditText etCity = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_CITY);
        String city = etCity.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_CITY, city);

        EditText etZip = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_ZIP);
        String zip = etZip.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ZIP, zip);

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
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_GENDER, gender);

        EditText etDMA = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_DMA);
        String dma = etDMA.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_DMA, dma);

        EditText etOrmaCompliance = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE);
        String ormaCompliance = etOrmaCompliance.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE, ormaCompliance);

        EditText etPaid = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_PAID);
        String paid = etPaid.getText().toString();
        mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_PAID, paid);
    }

}
