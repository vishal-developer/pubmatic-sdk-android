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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pubmatic.sdk.banner.mocean.MoceanBannerAdRequest;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.mocean.MoceanAdRequest;
import com.pubmatic.sdk.common.pubmatic.PubMaticAdRequest;
import com.pubmatic.sdk.nativead.PMNativeAd;
import com.pubmatic.sdk.nativead.bean.PMAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMDataAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMDataAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMDataAssetTypes;
import com.pubmatic.sdk.nativead.bean.PMImageAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMImageAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMImageAssetTypes;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetResponse;
import com.pubmatic.sdk.nativead.mocean.MoceanNativeAdRequest;
import com.pubmatic.sdk.nativead.pubmatic.PubMaticNativeAdRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class NativeAdFragment extends DialogFragment {

    private AlertDialog.Builder mBuilder;
    private LayoutInflater mInflater;

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

    public NativeAdFragment() {}

    public NativeAdFragment(ConfigurationManager.PLATFORM platform, LinkedHashMap<String, LinkedHashMap<String, String>> settings)
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
        view = mInflater.inflate(R.layout.native_template, null);

        initLayout(view);

        loadAd(view);

        mBuilder.setView(view);

        Dialog dialog = mBuilder.create();

        Drawable drawable = new ColorDrawable(Color.BLACK);
        drawable.setAlpha(220);

        dialog.getWindow().setBackgroundDrawable(drawable);

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

    private void loadAd(View rootView)
    {
        // Initialize the adview
        ad = new PMNativeAd(getActivity());
        ad.setRequestListener(new AdRequestListener());

		/*
		 * Uncomment following line to use internal browser instead system
		 * default browser, to open ads when clicked
		 */
        ad.setUseInternalBrowser(true);

        // Enable device id detection
        ad.setAndroidaidEnabled(true);

        AdRequest adRequest;

        if(mPlatform == ConfigurationManager.PLATFORM.MOCEAN) {

            String zone = mSettings.get(PMConstants.SETTINGS_HEADING_AD_TAG).get(PMConstants.SETTINGS_AD_TAG_ZONE);

            if(zone == null || zone.equals(""))
            {
                Toast.makeText(getActivity(), "Please enter a zone", Toast.LENGTH_LONG).show();
                return;
            }

            adRequest = MoceanNativeAdRequest.createMoceanNativeAdRequest(getActivity(), zone, getAssetRequests());

            String test = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_TEST);
            ((MoceanNativeAdRequest)adRequest).setTest(Boolean.parseBoolean(test));

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
                    ((MoceanNativeAdRequest)adRequest).setCity(city);

                String zip = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ZIP);

                if(!zip.equals("") && !zip.equals(""))
                    ((MoceanNativeAdRequest)adRequest).setZip(zip);

                String dma = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_DMA);

                if(!dma.equals("") && !dma.equals(""))
                    ((MoceanNativeAdRequest)adRequest).setDMA(dma);

                String area = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_AREA);

                if(!area.equals("") && area != null)
                    ((MoceanNativeAdRequest)adRequest).setAreaCode(area);

                String age = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_AGE);

                if(!age.equals("") && !age.equals(""))
                    ((MoceanNativeAdRequest)adRequest).setAge(age);

                String birthday = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_BIRTHDAY);

                if(!birthday.equals("") && birthday != null)
                    ((MoceanNativeAdRequest)adRequest).setBirthDay(birthday);

                String gender = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_GENDER);

                if(!gender.equals("") && !gender.equals(""))
                {
                    if(gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("M"))
                        ((MoceanBannerAdRequest)adRequest).setGender(MoceanAdRequest.GENDER.MALE);
                    else if(gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase("F"))
                        ((MoceanBannerAdRequest)adRequest).setGender(MoceanAdRequest.GENDER.FEMALE);
                }

                String over18 = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_OVER_18);

                if(!over18.equals("") && over18 != null)
                {
                    if(over18.equalsIgnoreCase("0"))
                        ((MoceanBannerAdRequest)adRequest).setOver18(MoceanAdRequest.OVER_18.DENY);
                    else if(over18.equalsIgnoreCase("2"))
                        ((MoceanBannerAdRequest)adRequest).setOver18(MoceanAdRequest.OVER_18.ONLY_OVER_18);
                    else if(over18.equalsIgnoreCase("3"))
                        ((MoceanBannerAdRequest)adRequest).setOver18(MoceanAdRequest.OVER_18.ALLOW_ALL);
                }

                String ethnicity = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ETHNICITY);

                if(!ethnicity.equals("") && ethnicity != null)
                {
                    if(ethnicity.equals("0"))
                        ((MoceanBannerAdRequest)adRequest).setEthnicity(MoceanAdRequest.ETHNICITY.BLACK);
                    else if(ethnicity.equals("1"))
                        ((MoceanBannerAdRequest)adRequest).setEthnicity(MoceanAdRequest.ETHNICITY.ASIAN);
                    else if(ethnicity.equals("2"))
                        ((MoceanBannerAdRequest)adRequest).setEthnicity(MoceanAdRequest.ETHNICITY.LATINO);
                    else if(ethnicity.equals("3"))
                        ((MoceanBannerAdRequest)adRequest).setEthnicity(MoceanAdRequest.ETHNICITY.WHITE);
                    else if(ethnicity.equals("4"))
                        ((MoceanBannerAdRequest)adRequest).setEthnicity(MoceanAdRequest.ETHNICITY.EAST_INDIAN);
                }

                String language = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_LANGUAGE);

                if(!language.equals("") && language != null)
                    ((MoceanBannerAdRequest)adRequest).setLanguage(language);

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

            adRequest = PubMaticNativeAdRequest.createPubMaticNativeAdRequest(getActivity(), pubId, siteId, adId, getAssetRequests());

            // Configuration Parameters
            String androidAidEnabled = mSettings.get(PMConstants.SETTINGS_HEADING_CONFIGURATION).get(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED);
            ((PubMaticNativeAdRequest)adRequest).setAndroidAidEnabled(Boolean.parseBoolean(androidAidEnabled));

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
                    ((PubMaticNativeAdRequest)adRequest).setCity(city);

                String state = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STATE);

                if(!state.equals("") && !state.equals(""))
                    ((PubMaticNativeAdRequest)adRequest).setState(state);

                String zip = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ZIP);

                if(!zip.equals("") && zip != null)
                    ((PubMaticNativeAdRequest)adRequest).setZip(zip);

                String appDomain = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_DOMAIN);

                if(!appDomain.equals("") && appDomain != null)
                    ((PubMaticNativeAdRequest)adRequest).setAppDomain(appDomain);

                String appCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_CATEGORY);

                if(!appCategory.equals("") && appCategory != null)
                    ((PubMaticNativeAdRequest)adRequest).setAppCategory(appCategory);

                String iabCategory = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_IAB_CATEGORY);

                if(!iabCategory.equals("") && iabCategory != null)
                    ((PubMaticNativeAdRequest)adRequest).setIABCategory(iabCategory);

                String storeUrl = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_STORE_URL);

                if(!storeUrl.equals("") && storeUrl != null)
                    ((PubMaticNativeAdRequest)adRequest).setStoreURL(storeUrl);

                String appName = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_APP_NAME);

                if(!appName.equals("") && appName != null)
                    ((PubMaticNativeAdRequest)adRequest).setAppName(appName);

                String yearOfBirth = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH);

                if(!yearOfBirth.equals("") && yearOfBirth != null)
                    ((PubMaticNativeAdRequest)adRequest).setYearOfBirth(yearOfBirth);

                String income = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_INCOME);

                if(!income.equals("") && income != null)
                    ((PubMaticNativeAdRequest)adRequest).setIncome(income);

                String ethnicity = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ETHNICITY);

                if(!ethnicity.equals("") && ethnicity != null)
                {
                    if(ethnicity.equalsIgnoreCase("HISPANIC"))
                        ((PubMaticNativeAdRequest)adRequest).setEthnicity(PubMaticAdRequest.ETHNICITY.HISPANIC);
                    else if(ethnicity.equalsIgnoreCase("AFRICAN_AMERICAN"))
                        ((PubMaticNativeAdRequest)adRequest).setEthnicity(PubMaticAdRequest.ETHNICITY.AFRICAN_AMERICAN);
                    else if(ethnicity.equalsIgnoreCase("CAUCASIAN"))
                        ((PubMaticNativeAdRequest)adRequest).setEthnicity(PubMaticAdRequest.ETHNICITY.CAUCASIAN);
                    else if(ethnicity.equalsIgnoreCase("ASIAN_AMERICAN"))
                        ((PubMaticNativeAdRequest)adRequest).setEthnicity(PubMaticAdRequest.ETHNICITY.ASIAN_AMERICAN);
                }

                String gender = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_GENDER);

                if(gender != null && !gender.equals(""))
                {
                    if(gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("M"))
                        ((PubMaticNativeAdRequest)adRequest).setGender(PubMaticAdRequest.GENDER.MALE);
                    else if(gender.equalsIgnoreCase("Female") || gender.equalsIgnoreCase("F"))
                        ((PubMaticNativeAdRequest)adRequest).setGender(PubMaticAdRequest.GENDER.FEMALE);
                    else if(gender.equalsIgnoreCase("Others") || gender.equalsIgnoreCase("O"))
                        ((PubMaticNativeAdRequest)adRequest).setGender(PubMaticAdRequest.GENDER.OTHER);
                }

                String dma = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_DMA);

                if(!dma.equals("") && dma != null)
                    ((PubMaticNativeAdRequest)adRequest).setDMA(dma);

                String paid = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_PAID);

                if(!paid.equals("") && paid != null)
                    ((PubMaticNativeAdRequest)adRequest).setApplicationPaid(Boolean.parseBoolean(paid));

                String awt = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_AWT);

                if(!awt.equals("") && awt != null)
                {
                    int awtOption = Integer.parseInt(awt);

                    if(awtOption == 0)
                        ((PubMaticNativeAdRequest)adRequest).setAWT(PubMaticAdRequest.AWT_OPTION.DEFAULT);
                    else if(awtOption == 1)
                        ((PubMaticNativeAdRequest)adRequest).setAWT(PubMaticAdRequest.AWT_OPTION.WRAPPED_IN_IFRAME);
                    else if(awtOption == 2)
                        ((PubMaticNativeAdRequest)adRequest).setAWT(PubMaticAdRequest.AWT_OPTION.WRAPPED_IN_JS);
                }

                String coppa = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_COPPA);
                ((PubMaticBannerAdRequest)adRequest).setCoppa(Boolean.parseBoolean(coppa));

                String ormaCompliance = mSettings.get(PMConstants.SETTINGS_HEADING_TARGETTING).get(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE);

                if(!ormaCompliance.equals("") && ormaCompliance != null)
                    ((PubMaticNativeAdRequest)adRequest).setOrmmaComplianceLevel(Integer.parseInt(ormaCompliance));

                boolean isDoNotTrackChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_DO_NOT_TRACK);
                ((PubMaticAdRequest)adRequest).setDoNotTrack(isDoNotTrackChecked);
            }
            catch (Exception exception)
            {
                Log.e("Parse Error", exception.toString());
            }

        }
        else
            adRequest = MoceanNativeAdRequest.createMoceanNativeAdRequest(getActivity(), "88269", getAssetRequests());

        boolean isUseInternalBrowserChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_USE_INTERNAL_BROWSER);
        ad.setUseInternalBrowser(isUseInternalBrowserChecked);

        boolean isAutoLocationDetectionChecked = PubMaticPreferences.getBooleanPreference(getActivity(), PubMaticPreferences.PREFERENCE_KEY_AUTO_LOCATION_DETECTION);
        ad.setLocationDetectionEnabled(isAutoLocationDetectionChecked);

        // Request for ads
        ad.execute(adRequest);
    }

    private List<PMAssetRequest> getAssetRequests() {
        List<PMAssetRequest> assets = new ArrayList<PMAssetRequest>();

        PMTitleAssetRequest titleAsset = new PMTitleAssetRequest(1);// Unique assetId is mandatory for each asset
        titleAsset.setLength(50);
        titleAsset.setRequired(true); // Optional (Default: false)
        assets.add(titleAsset);

        PMImageAssetRequest imageAssetIcon = new PMImageAssetRequest(2);
        imageAssetIcon.setImageType(PMImageAssetTypes.icon);
        assets.add(imageAssetIcon);

        PMImageAssetRequest imageAssetMainImage = new PMImageAssetRequest(3);
        imageAssetMainImage.setImageType(PMImageAssetTypes.main);
        assets.add(imageAssetMainImage);

        PMDataAssetRequest dataAssetDesc = new PMDataAssetRequest(5);
        dataAssetDesc.setDataAssetType(PMDataAssetTypes.desc);
        dataAssetDesc.setLength(25);
        assets.add(dataAssetDesc);

        PMDataAssetRequest dataAssetRating = new PMDataAssetRequest(6);
        dataAssetRating.setDataAssetType(PMDataAssetTypes.rating);
        assets.add(dataAssetRating);

        PMDataAssetRequest dataAssetCta = new PMDataAssetRequest(7);
        dataAssetCta.setDataAssetType(PMDataAssetTypes.ctatext);
        assets.add(dataAssetCta);

        return assets;
    }

    private class AdRequestListener implements PMNativeAd.NativeRequestListener {

        @Override
        public void onNativeAdFailed(PMNativeAd ad, final Exception ex) {

            if(ex!=null) {
                ex.printStackTrace();
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    dismiss();
                }
            });
        }

        @Override
        public void onNativeAdReceived(final PMNativeAd ad) {

            if (ad != null) {

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        List<PMAssetResponse> nativeAssets = ad.getNativeAssets();
                        for (PMAssetResponse asset : nativeAssets) {
                            try {
								/*
								 * As per openRTB standard, assetId in response
								 * must match that of in request.
								 */
                                switch (asset.getAssetId()) {
                                    case 1:
                                        txtTitle.setText(((PMTitleAssetResponse) asset)
                                                .getTitleText());
                                        break;
                                    case 2:
                                        PMNativeAd.Image iconImage = ((PMImageAssetResponse) asset)
                                                .getImage();
                                        if (iconImage != null) {
                                            imgLogo.setImageBitmap(null);
                                            ad.loadImage(imgLogo,
                                                    iconImage.getUrl());
                                        }
                                        break;
                                    case 3:
                                        PMNativeAd.Image mainImage = ((PMImageAssetResponse) asset)
                                                .getImage();
                                        if (mainImage != null) {
                                            imgMain.setImageBitmap(null);
                                            ad.loadImage(imgMain,
                                                    mainImage.getUrl());
                                        }
                                        break;
                                    case 5:
                                        txtDescription
                                                .setText(((PMDataAssetResponse) asset)
                                                        .getValue());
                                        break;
                                    case 7:
                                        ctaText
                                                .setText(((PMDataAssetResponse) asset).getValue());
                                        break;
                                    case 6:
                                        String ratingStr = ((PMDataAssetResponse) asset)
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
                                            Log.e("NativeActivity",
                                                    "Error parsing 'rating'");
                                        }
                                        break;

                                    default: // NOOP
                                        break;
                                }
                            } catch (Exception ex) {
                                Log.i("NativeAdFragment", "ERROR in rendering asset. Skipping asset.");
                            }
                        }
                    }
                });

                if (ad.getJsTracker() != null) {
                    Log.i("NativeAdFragment", ad.getJsTracker());
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
        public void onReceivedThirdPartyRequest(PMNativeAd mastNativeAd,
                                                Map<String, String> properties, Map<String, String> parameters) {
        }

        @Override
        public void onNativeAdClicked(PMNativeAd ad) {
        }
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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        ad.destroy();
        ad = null;
    }
}
