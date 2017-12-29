package com.pubmatic.sample;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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


import java.io.Serializable;
import java.lang.ref.WeakReference;
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

    private LinkedHashMap<String, LinkedHashMap<String, String>> mSettings;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mPlatform = ConfigurationManager.PLATFORM.PUBMATIC;

        mAdTypeSelector = (TextView) rootView.findViewById(R.id.home_ad_type);
        mAdTypeSelector.setOnClickListener(onAdTypeChooserSelected);

        //Default type would be banner
        if (savedInstanceState != null) {
            //probably orientation change
            mAdType = (ConfigurationManager.AD_TYPE) savedInstanceState.getSerializable("adtype");

            if(mAdType == ConfigurationManager.AD_TYPE.BANNER)
                mAdTypeSelector.setText("Banner");
            else if(mAdType == ConfigurationManager.AD_TYPE.INTERSTITIAL)
                mAdTypeSelector.setText("Interstitial");
            else if(mAdType == ConfigurationManager.AD_TYPE.NATIVE)
                mAdTypeSelector.setText("Native");
            else
                mAdTypeSelector.setText("Banner");
        } else
            mAdType = ConfigurationManager.AD_TYPE.BANNER;

        mSettingsParent = (LinearLayout) rootView.findViewById(R.id.home_settings_parent);

        mLoadAd = rootView.findViewById(R.id.home_load_ad);
        mLoadAd.setOnClickListener(onLoadAd);

        //Load the default values of the parameters from Settings file inside asset folder
        refreshSettings();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("adtype", (Serializable) mAdType);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    /**
     * It reads the settings.json file from asset folder and populates them in the screen UI.
     * Internally, it inflates the runtime UI and assign the id/properties before UI loading.
     */
    public void refreshSettings()
    {
        mSettingsParent.removeAllViews();

        mSettings = ConfigurationManager.getInstance(getContext()).getSettings(mPlatform, mAdType);

        if(mSettings==null)
            return;

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
        editText.getBackground().setColorFilter(getResources().getColor(android.R.color.holo_blue_dark), PorterDuff.Mode.SRC_IN);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        final WeakReference<LinkedHashMap<String, LinkedHashMap<String, String>>> weakSettings = new WeakReference<LinkedHashMap<String, LinkedHashMap<String, String>>>(mSettings);
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

                weakSettings.get().get(headerKey).put(settingKey, value);
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

            refreshSettings();

            if(mDialog != null)
                mDialog.dismiss();

        }
    };

    private View.OnClickListener onLoadAd = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            ((HomeActivity)getActivity()).resetLogs();

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
                        Toast.makeText(getActivity().getApplicationContext(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getActivity().getApplicationContext(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getActivity().getApplicationContext(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
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
        return true;
    }
}
