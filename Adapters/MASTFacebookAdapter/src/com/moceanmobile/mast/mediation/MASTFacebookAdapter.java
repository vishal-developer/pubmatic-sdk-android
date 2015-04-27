/*
 * 
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * 
 * Unpublished Copyright (c) 2006-2014 PubMatic, All Rights Reserved.
 * 
 * 
 * 
 * NOTICE: All information contained herein is, and remains the property of
 * PubMatic. The intellectual and technical concepts contained
 * 
 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign
 * Patents, patents in process, and are protected by trade secret or copyright
 * law.
 * 
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained
 * 
 * from PubMatic. Access to the source code contained herein is hereby forbidden
 * to anyone except current PubMatic employees, managers or contractors who have
 * executed
 * 
 * Confidentiality and Non-disclosure agreements explicitly covering such
 * access.
 * 
 * 
 * 
 * The copyright notice above does not evidence any actual or intended
 * publication or disclosure of this source code, which includes
 * 
 * information that is confidential and/or proprietary, and is a trade secret,
 * of PubMatic. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC
 * PERFORMANCE,
 * 
 * OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS
 * WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF
 * APPLICABLE
 * 
 * LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE
 * CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * 
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR
 * SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.moceanmobile.mast.mediation;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAd.Image;
import com.facebook.ads.NativeAd.Rating;
import com.moceanmobile.mast.MASTBaseAdapter;
import com.moceanmobile.mast.bean.AssetResponse;
import com.moceanmobile.mast.bean.DataAssetResponse;
import com.moceanmobile.mast.bean.DataAssetTypes;
import com.moceanmobile.mast.bean.ImageAssetResponse;
import com.moceanmobile.mast.bean.ImageAssetTypes;
import com.moceanmobile.mast.bean.TitleAssetResponse;

public class MASTFacebookAdapter extends MASTBaseAdapter implements AdListener {

    // Facebook native ad class.
    private NativeAd mNativeAd = null;

    @Override
    public void loadAd() {

        try {
            /*
             * Since it is required to create new object of Facebook
             * MASTNativeAd on UI thread, we are creating a running these
             * statements on UI thread using.
             * 
             * Also since we are catching the exception for any error occurs, we
             * are not checking whether context is instance of activity before
             * type casting it.
             */
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Initialize the Facebook native ad
                    mNativeAd = new NativeAd(mContext, mAdUnitId);

                    // Set a listener to get notified when the ad was loaded.
                    mNativeAd.setAdListener(MASTFacebookAdapter.this);

                    if (mMapMediationNetworkTestDeviceIds != null
                            && mMapMediationNetworkTestDeviceIds
                                    .containsKey(MediationNetwork.FACEBOOK_AUDIENCE_NETWORK)) {
                        AdSettings.addTestDevice(mMapMediationNetworkTestDeviceIds
                                .get(MediationNetwork.FACEBOOK_AUDIENCE_NETWORK));
                    }

                    mNativeAd.loadAd();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

            adFailed(this, e);
        }
    }

    @Override
    public void trackViewForInteractions(View view) {

        if (mNativeAd != null && view != null) {
            mNativeAd.registerViewForInteraction(view);
        }
    }

    @Override
    public void destroy() {
        if (mNativeAd != null) {
            mNativeAd.unregisterView();
            mNativeAd.destroy();
            mNativeAd = null;
        }

        super.destroy();
    }

    @Override
    public void onAdClicked(Ad ad) {
        if (ad != mNativeAd) {
            adFailed(this, new Exception("Unable to load ad. Invalid state received."));
            return;
        }

        adClicked(this);
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (ad != mNativeAd) {
            adFailed(this, new Exception("Unable to load ad. Invalid state received."));
            return;
        }

        if (mNativeAd != null && mAdDescriptor != null) {
            // Map FAN native asset with Mocean Asset response

            List<AssetResponse> assetResponses = new ArrayList<AssetResponse>();

            TitleAssetResponse titleAsset = new TitleAssetResponse();
            titleAsset.setTitleText(mNativeAd.getAdTitle());
            assetResponses.add(titleAsset);

            ImageAssetResponse coverImageAsset = new ImageAssetResponse();
            coverImageAsset.setImageType(ImageAssetTypes.main);
            Image coverImage = mNativeAd.getAdCoverImage();
            if (coverImage != null) {
                coverImageAsset.setImage(new com.moceanmobile.mast.MASTNativeAd.Image(coverImage.getUrl()));
                assetResponses.add(coverImageAsset);
            }

            ImageAssetResponse iconImageAsset = new ImageAssetResponse();
            iconImageAsset.setImageType(ImageAssetTypes.icon);
            Image iconImage = mNativeAd.getAdIcon();
            if (coverImage != null) {
                iconImageAsset.setImage(new com.moceanmobile.mast.MASTNativeAd.Image(iconImage.getUrl()));
                assetResponses.add(iconImageAsset);
            }

            DataAssetResponse descriptionAsset = new DataAssetResponse();
            descriptionAsset.setDataAssetType(DataAssetTypes.desc);
            descriptionAsset.setValue(mNativeAd.getAdBody());
            assetResponses.add(descriptionAsset);

            DataAssetResponse ctaAsset = new DataAssetResponse();
            ctaAsset.setDataAssetType(DataAssetTypes.ctatext);
            ctaAsset.setValue(mNativeAd.getAdCallToAction());
            assetResponses.add(ctaAsset);

            DataAssetResponse ratingAsset = new DataAssetResponse();
            ratingAsset.setDataAssetType(DataAssetTypes.rating);
            Rating rating = mNativeAd.getAdStarRating();
            if (rating != null) {
                ratingAsset.setValue(String.valueOf(rating.getValue()));
                assetResponses.add(ratingAsset);
            }

            mAdDescriptor.setNativeAssetList(assetResponses);
        }

        adReceived(this);
    }

    @Override
    public void onError(Ad ad, AdError error) {
        adFailed(this, new Exception(error.getErrorMessage()));
    }
}
