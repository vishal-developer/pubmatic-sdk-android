package com.pubmatic.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by Sagar on 12/20/2016.
 */

public class SettingsFragment extends Fragment {

    Switch useInternalBrowser;
    Switch autoLocationDetection;
    Switch doNotTrack;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        useInternalBrowser = (Switch) rootView.findViewById(R.id.settings_switch_use_internal_browser);
        autoLocationDetection = (Switch) rootView.findViewById(R.id.settings_switch_auto_location_detection);
        doNotTrack = (Switch) rootView.findViewById(R.id.settings_switch_do_not_track);

        useInternalBrowser.setOnCheckedChangeListener(onUseInternalBrowserToggled);
        autoLocationDetection.setOnCheckedChangeListener(onAutoLocationDetectionToggled);
        doNotTrack.setOnCheckedChangeListener(onDoNotTrackToggled);

        boolean isUseInternalBrowserChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_USE_INTERNAL_BROWSER);
        useInternalBrowser.setChecked(isUseInternalBrowserChecked);

        boolean isAutoLocationDetectionChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_AUTO_LOCATION_DETECTION);
        autoLocationDetection.setChecked(isAutoLocationDetectionChecked);

        boolean isDoNotTrackChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_DO_NOT_TRACK);
        doNotTrack.setChecked(isDoNotTrackChecked);

        return rootView;
    }

    private CompoundButton.OnCheckedChangeListener onUseInternalBrowserToggled = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            PubMaticPreferences.saveBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_USE_INTERNAL_BROWSER, isChecked);
        }
    };

    private CompoundButton.OnCheckedChangeListener onAutoLocationDetectionToggled = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            PubMaticPreferences.saveBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_AUTO_LOCATION_DETECTION, isChecked);
        }
    };

    private CompoundButton.OnCheckedChangeListener onDoNotTrackToggled = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            PubMaticPreferences.saveBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_DO_NOT_TRACK, isChecked);
        }
    };
}
