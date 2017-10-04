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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.PMError;
import com.pubmatic.sdk.common.pubmatic.PMAdRequest;
import com.pubmatic.sdk.nativead.PMNativeAd;
import com.pubmatic.sdk.nativead.bean.PMNativeAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMNativeAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMNativeDataAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMNativeDataAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMNativeDataAssetTypes;
import com.pubmatic.sdk.nativead.bean.PMNativeImageAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMNativeImageAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMNativeImageAssetTypes;
import com.pubmatic.sdk.nativead.bean.PMNativeTitleAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMNativeTitleAssetResponse;
import com.pubmatic.sdk.nativead.pubmatic.PMNativeAdRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 */
public class NativeAdFragment extends DialogFragment {

    private static final String TAG = "NativeAdFragment";

    private ConfigurationManager.PLATFORM mPlatform;

    private LinkedHashMap<String, LinkedHashMap<String, String>> mSettings;

    private ImageView imgLogo = null;
    private ImageView imgMain = null;
    private TextView txtTitle = null;
    private TextView ctaText = null;
    private TextView txtDescription = null;
    private RatingBar ratingBar = null;
    private RelativeLayout mLayout = null;

    private PMNativeAd ad = null;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.native_template, null);
        builder.setView(view);

        initLayout(view);

        Drawable drawable = new ColorDrawable(Color.BLACK);
        drawable.setAlpha(220);
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(drawable);

        loadAd();

        return dialog;
    }

    private void initLayout(View view)
    {
        mLayout = (RelativeLayout) view.findViewById(R.id.native_parent);
        imgLogo = (ImageView) view.findViewById(R.id.imgLogo);
        imgMain = (ImageView) view.findViewById(R.id.imgMain);
        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        ctaText = (TextView) view.findViewById(R.id.ctaText);
        txtDescription = (TextView) view.findViewById(R.id.txtDescription);
        ratingBar = (RatingBar) view.findViewById(R.id.ratingbar);
    }

    private void loadAd()
    {
        // Initialize the adview
        ad = new PMNativeAd(getActivity());
        ad.setRequestListener(new AdRequestListener());

		/*
		 * Uncomment following line to use internal browser instead system
		 * default browser, to open ads when clicked
		 */
        boolean isUseInternalBrowserChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_USE_INTERNAL_BROWSER);
        ad.setUseInternalBrowser(isUseInternalBrowserChecked);

        // Request for ads
        AdRequest adRequest = buildAdRequest();
        if(adRequest!=null)
            ad.loadRequest(adRequest);
    }

    /**
     * Build AdRequest object and assign parameter values.
     * @return
     */
    private AdRequest buildAdRequest() {

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

            adRequest = PMNativeAdRequest.createPMNativeAdRequest(pubId, siteId, adId, getAssetRequests());

            // Configuration Parameters
            String androidAidEnabled = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED);
            ((PMNativeAdRequest)adRequest).setAndroidAidEnabled(Boolean.parseBoolean(androidAidEnabled));

            try
            {
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

                if(!city.equals("") && city != null)
                    ((PMNativeAdRequest)adRequest).setCity(city);

                String state = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STATE);

                if(!state.equals("") && !state.equals(""))
                    ((PMNativeAdRequest)adRequest).setState(state);

                String zip = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ZIP);

                if(!zip.equals("") && zip != null)
                    ((PMNativeAdRequest)adRequest).setZip(zip);

                String appDomain = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_DOMAIN);

                if(!appDomain.equals("") && appDomain != null)
                    ((PMNativeAdRequest)adRequest).setAppDomain(appDomain);

                String appCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_CATEGORY);

                if(!appCategory.equals("") && appCategory != null)
                    ((PMNativeAdRequest)adRequest).setAppCategory(appCategory);

                String iabCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_IAB_CATEGORY);

                if(!iabCategory.equals("") && iabCategory != null)
                    ((PMNativeAdRequest)adRequest).setIABCategory(iabCategory);

                String storeUrl = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STORE_URL);

                if(!storeUrl.equals("") && storeUrl != null)
                    ((PMNativeAdRequest)adRequest).setStoreURL(storeUrl);

                String yearOfBirth = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH);

                if(!yearOfBirth.equals("") && yearOfBirth != null)
                    ((PMNativeAdRequest)adRequest).setYearOfBirth(yearOfBirth);

                String income = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_INCOME);

                if(!income.equals("") && income != null)
                    ((PMNativeAdRequest)adRequest).setIncome(income);

                String ethnicity = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ETHNICITY);

                if(!ethnicity.equals("") && ethnicity != null)
                {
                    if(ethnicity.equalsIgnoreCase("HISPANIC"))
                        ((PMNativeAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.HISPANIC);
                    else if(ethnicity.equalsIgnoreCase("AFRICAN_AMERICAN"))
                        ((PMNativeAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.AFRICAN_AMERICAN);
                    else if(ethnicity.equalsIgnoreCase("CAUCASIAN"))
                        ((PMNativeAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.CAUCASIAN);
                    else if(ethnicity.equalsIgnoreCase("ASIAN_AMERICAN"))
                        ((PMNativeAdRequest)adRequest).setEthnicity(PMAdRequest.ETHNICITY.ASIAN_AMERICAN);
                }

                String gender = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_GENDER);

                if(gender != null && !gender.equals(""))
                {
                    if(gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("M"))
                        ((PMNativeAdRequest)adRequest).setGender(PMAdRequest.GENDER.MALE);
                    else if(gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase("F"))
                        ((PMNativeAdRequest)adRequest).setGender(PMAdRequest.GENDER.FEMALE);
                    else if(gender.equalsIgnoreCase("Others") || gender.equalsIgnoreCase("O"))
                        ((PMNativeAdRequest)adRequest).setGender(PMAdRequest.GENDER.OTHER);
                }

                String dma = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_DMA);

                if(!dma.equals("") && dma != null)
                    ((PMNativeAdRequest)adRequest).setDMA(dma);

                String paid = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_PAID);

                if(!paid.equals("") && paid != null)
                    ((PMNativeAdRequest)adRequest).setApplicationPaid(Boolean.parseBoolean(paid));

                String coppa = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_COPPA);
                ((PMNativeAdRequest)adRequest).setCoppa(Boolean.parseBoolean(coppa));

                String ormaCompliance = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE);

                if(!ormaCompliance.equals("") && ormaCompliance != null)
                    ((PMNativeAdRequest)adRequest).setOrmmaComplianceLevel(Integer.parseInt(ormaCompliance));
            }
            catch (Exception exception)
            {
                Log.e("Parse Error", exception.toString());
            }

        }
        return  adRequest;
    }

    private List<PMNativeAssetRequest> getAssetRequests() {
        // First create some assets to add in the request
        List<PMNativeAssetRequest> assets = new ArrayList<PMNativeAssetRequest>();

        // Unique assetId is mandatory for each asset
        PMNativeTitleAssetRequest titleAsset = new PMNativeTitleAssetRequest(1);
        titleAsset.setLength(50);
        titleAsset.setRequired(true); // Optional (Default: false)
        assets.add(titleAsset);

        PMNativeImageAssetRequest imageAssetIcon = new PMNativeImageAssetRequest(2);
        imageAssetIcon.setImageType(PMNativeImageAssetTypes.icon);
        assets.add(imageAssetIcon);

        PMNativeImageAssetRequest imageAssetMainImage = new PMNativeImageAssetRequest(3);
        imageAssetMainImage.setImageType(PMNativeImageAssetTypes.main);
        assets.add(imageAssetMainImage);

        PMNativeDataAssetRequest dataAssetDesc = new PMNativeDataAssetRequest(5);
        dataAssetDesc.setDataAssetType(PMNativeDataAssetTypes.desc);
        dataAssetDesc.setLength(25);
        assets.add(dataAssetDesc);

        PMNativeDataAssetRequest dataAssetRating = new PMNativeDataAssetRequest(6);
        dataAssetRating.setDataAssetType(PMNativeDataAssetTypes.rating);
        assets.add(dataAssetRating);

        PMNativeDataAssetRequest dataAssetCta = new PMNativeDataAssetRequest(4);
        dataAssetCta.setDataAssetType(PMNativeDataAssetTypes.ctatext);
        assets.add(dataAssetCta);

        return assets;
    }

    private class AdRequestListener implements PMNativeAd.NativeRequestListener {

        @Override
        public void onNativeAdReceived(final PMNativeAd ad) {

            if (ad != null) {

                List<PMNativeAssetResponse> nativeAssets = ad.getNativeAssets();
                for (PMNativeAssetResponse asset : nativeAssets) {
                    try {
                        /*
                         * As per openRTB standard, assetId in response
                         * must match that of in request.
                         */
                        switch (asset.getAssetId()) {
                            case 1:
                                txtTitle.setText(((PMNativeTitleAssetResponse) asset)
                                        .getTitleText());
                                break;
                            case 2:
                                PMNativeAd.Image iconImage = ((PMNativeImageAssetResponse) asset)
                                        .getImage();
                                if (iconImage != null) {
                                    imgLogo.setImageBitmap(null);
                                    ad.loadImage(imgLogo,
                                            iconImage.getUrl());
                                }
                                break;
                            case 3:
                                PMNativeAd.Image mainImage = ((PMNativeImageAssetResponse) asset)
                                        .getImage();
                                if (mainImage != null) {
                                    imgMain.setImageBitmap(null);
                                    ad.loadImage(imgMain,
                                            mainImage.getUrl());
                                }
                                break;
                            case 5:
                                txtDescription
                                        .setText(((PMNativeDataAssetResponse) asset)
                                                .getValue());
                                break;
                            case 4:
                                ctaText
                                        .setText(((PMNativeDataAssetResponse) asset).getValue());
                                break;
                            case 6:
                                String ratingStr = ((PMNativeDataAssetResponse) asset)
                                        .getValue();
                                try {
                                    float rating = Float
                                            .parseFloat(ratingStr);
                                    if (rating > 0f) {
                                        ratingBar.setRating(rating);
                                        ratingBar
                                                .setVisibility(View.VISIBLE);
                                    } else {
                                        ratingBar.setRating(rating);
                                        ratingBar.setVisibility(View.GONE);
                                    }
                                } catch (Exception e) {
                                    // Invalid rating string
                                    Log.e(TAG,
                                            "Error parsing 'rating'");
                                }
                                break;

                            default: // NOOP
                                break;
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "ERROR in rendering asset. Skipping asset.");
                        ex.printStackTrace();
                    }
                }


                if (ad.getJsTracker() != null) {
                    Log.i(TAG, ad.getJsTracker());
					/*
					 * Note: Publisher should execute the javascript tracker
					 * whenever possible.
					 */
                }

				/*
				 * IMPORTANT : Must call this method when response rendering is
				 * complete. This method sets click listener on the ad container
				 * layout. This is required for firing click tracker when ad is
				 * clicked by the user.
				 */
                ad.trackViewForInteractions(mLayout);
            }

        }

        @Override
        public void onNativeAdFailed(PMNativeAd ad, final PMError error) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
                    dismiss();
                }
            });
        }

        @Override
        public void onNativeAdClicked(PMNativeAd ad) {
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(ad!=null)
            ad.destroy();
        ad = null;
    }
}
