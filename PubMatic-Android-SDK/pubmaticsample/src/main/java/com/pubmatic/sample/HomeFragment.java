package com.pubmatic.sample;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedHashMap;

/**
 * Created by Sagar on 12/20/2016.
 */

public class HomeFragment extends Fragment {

    private Spinner mSpinnerPlatform;
    private Spinner mSpinnerAdType;

    private String[] platforms;
    private String[] adTypes;

    private ArrayAdapter<String> mPlatformAdapter;
    private ArrayAdapter<String> mAdTypeAdapter;

    private ConfigurationManager.PLATFORM mPlatform;
    private ConfigurationManager.AD_TYPE mAdType;

    private View mLoadAd;

    LinkedHashMap<String, LinkedHashMap<String, String>> mSettings;

    private LinearLayout mSettingsParent;

    public HomeFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mSpinnerPlatform = (Spinner) rootView.findViewById(R.id.home_spinner_platform);
        mSpinnerAdType = (Spinner) rootView.findViewById(R.id.home_spinner_ad_type);

        platforms = new String[] {"Mocean", "PubMatic", "Phoenix"};
        adTypes = new String[] {"Banner", "Native"};

        mPlatformAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, platforms);
        mSpinnerPlatform.setAdapter(mPlatformAdapter);
        mSpinnerPlatform.setSelection(0);
        mSpinnerPlatform.setOnItemSelectedListener(onPlatformSelected);
        mPlatform = ConfigurationManager.PLATFORM.MOCEAN;

        mAdTypeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, adTypes);
        mSpinnerAdType.setAdapter(mAdTypeAdapter);
        mSpinnerAdType.setSelection(0);
        mSpinnerAdType.setOnItemSelectedListener(onAdTypeSelected);
        mAdType = ConfigurationManager.AD_TYPE.BANNER;

        mSettingsParent = (LinearLayout) rootView.findViewById(R.id.home_settings_parent);

        mLoadAd = rootView.findViewById(R.id.home_load_ad);
        mLoadAd.setOnClickListener(onLoadAd);

        refreshSettings();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mSpinnerPlatform.setSelection(0);
        mSpinnerAdType.setSelection(0);

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

        return v;
    }

    private AdapterView.OnItemSelectedListener onPlatformSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {

            TextView selectedText = (TextView) parent.getChildAt(0);
            if (selectedText != null) {
                selectedText.setTextColor(Color.BLACK);
            }

            String platform = "";

            if(selectedText != null)
                platform = selectedText.getText().toString();

            if(platform.equals("Mocean"))
                mPlatform = ConfigurationManager.PLATFORM.MOCEAN;
            else if(platform.equals("PubMatic"))
                mPlatform = ConfigurationManager.PLATFORM.PUBMATIC;
            else if(platform.equals("Phoenix"))
                mPlatform = ConfigurationManager.PLATFORM.PHEONIX;
            else
                mPlatform = ConfigurationManager.PLATFORM.MOCEAN;

            refreshSettings();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private AdapterView.OnItemSelectedListener onAdTypeSelected = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {

            TextView selectedText = (TextView) parent.getChildAt(0);
            if (selectedText != null) {
                selectedText.setTextColor(Color.BLACK);
            }

            String adType = "";

            if(selectedText != null)
                adType = selectedText.getText().toString();

            if(adType.equals("Banner"))
                mAdType = ConfigurationManager.AD_TYPE.BANNER;
            else if(adType.equals("Native"))
                mAdType = ConfigurationManager.AD_TYPE.NATIVE;
            else
                mAdType = ConfigurationManager.AD_TYPE.BANNER;

            refreshSettings();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private void refreshSettings()
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

            /*InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);*/

            if(mAdType == ConfigurationManager.AD_TYPE.BANNER)
            {
                if(mPlatform == ConfigurationManager.PLATFORM.MOCEAN)
                {
                    EditText et = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_AD_TAG + ":" + PMConstants.SETTINGS_AD_TAG_ZONE);
                    String zone = et.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).put(PMConstants.SETTINGS_AD_TAG_ZONE, zone);

                    EditText etCity = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_CITY);
                    String city = etCity.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_CITY, city);

                    EditText etZip = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_ZIP);
                    String zip = etZip.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ZIP, zip);

                    if(zone != null && !zone.equals(""))
                    {
                        BannerAdFragment bannerAdDialogFragment = new BannerAdFragment(mPlatform, mSettings);
                        bannerAdDialogFragment.show(getActivity().getFragmentManager(), "BannerAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter a zone", Toast.LENGTH_LONG).show();
                }
                else if(mPlatform == ConfigurationManager.PLATFORM.PUBMATIC)
                {
                    EditText pubEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_AD_TAG + ":" + PMConstants.SETTINGS_AD_TAG_PUB_ID);
                    String pubId = pubEt.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).put(PMConstants.SETTINGS_AD_TAG_PUB_ID, pubId);

                    EditText siteEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_AD_TAG + ":" + PMConstants.SETTINGS_AD_TAG_SITE_ID);
                    String siteId = siteEt.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).put(PMConstants.SETTINGS_AD_TAG_SITE_ID, siteId);

                    EditText adEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_AD_TAG + ":" + PMConstants.SETTINGS_AD_TAG_AD_ID);
                    String adId = adEt.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).put(PMConstants.SETTINGS_AD_TAG_AD_ID, adId);

                    EditText etCity = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_CITY);
                    String city = etCity.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_CITY, city);

                    EditText etZip = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_ZIP);
                    String zip = etZip.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ZIP, zip);

                    if(pubId != null && !pubId.equals("") && siteId != null && !siteId.equals("") && adId != null & !adId.equals(""))
                    {
                        BannerAdFragment bannerAdDialogFragment = new BannerAdFragment(mPlatform, mSettings);
                        bannerAdDialogFragment.show(getActivity().getFragmentManager(), "BannerAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter pubId, siteId and adId", Toast.LENGTH_LONG).show();
                }
                else if(mPlatform == ConfigurationManager.PLATFORM.PHEONIX)
                {
                    EditText adUnitIdEt = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_AD_TAG + ":" + PMConstants.SETTINGS_AD_TAG_AD_UNIT_ID);
                    String adUnitId = adUnitIdEt.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).put(PMConstants.SETTINGS_AD_TAG_AD_UNIT_ID, adUnitId);

                    EditText etCity = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_CITY);
                    String city = etCity.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_CITY, city);

                    EditText etZip = (EditText) getView().findViewWithTag(PMConstants.SETTINGS_HEADING_TARGETTING + ":" + PMConstants.SETTINGS_TARGETTING_ZIP);
                    String zip = etZip.getText().toString();
                    mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).put(PMConstants.SETTINGS_TARGETTING_ZIP, zip);

                    if(adUnitId != null && !adUnitId.equals(""))
                    {
                        BannerAdFragment bannerAdDialogFragment = new BannerAdFragment(mPlatform, mSettings);
                        bannerAdDialogFragment.show(getActivity().getFragmentManager(), "BannerAdFragment");
                    }
                    else
                        Toast.makeText(getActivity(), "Please enter adUnitId", Toast.LENGTH_LONG).show();

                }
            }
            else if(mAdType == ConfigurationManager.AD_TYPE.NATIVE)
            {

            }
        }
    };
}
