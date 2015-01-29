/*

 * PubMatic Inc. ("PubMatic") CONFIDENTIAL

 * Unpublished Copyright (c) 2006-2014 PubMatic, All Rights Reserved.

 *

 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained

 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.

 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained

 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed 

 * Confidentiality and Non-disclosure agreements explicitly covering such access.

 *

 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes  

 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, 

 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE 

 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS  

 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.                

 */
package com.moceanmobile.mast.mediation;

import java.util.EnumSet;
import java.util.Map;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.moceanmobile.mast.MASTBaseAdapter;
import com.moceanmobile.mast.MASTNativeAd;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubNative.MoPubNativeEventListener;
import com.mopub.nativeads.MoPubNative.MoPubNativeNetworkListener;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.NativeResponse;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.RequestParameters.NativeAdAsset;

public class MASTMoPubAdapter extends MASTBaseAdapter implements
		MoPubNativeNetworkListener, MoPubNativeEventListener {
	private RequestParameters mRequestParameters;
	private MoPubNative mMopubNative = null;
	private NativeResponse mNativeResponse = null;

	@Override
	public void loadAd() {
		((Activity) mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// @formatter:off
				mRequestParameters = new RequestParameters.Builder()
											.desiredAssets(getDesiredAssets())
											.keywords(getKeywords())
											.build();
				// @formatter:on

				mMopubNative = new MoPubNative(mContext, mAdUnitId,
						MASTMoPubAdapter.this);
				mMopubNative.setNativeEventListener(MASTMoPubAdapter.this);

				mMopubNative.makeRequest(mRequestParameters);
			}
		});
	}

	@Override
	public void trackViewForInteractions(final View view) {
		if (mNativeResponse != null) {
			// Tell MoPub SDK to record the impression
			mNativeResponse.recordImpression(view);

			// Set listener on view to track click events
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mNativeResponse.handleClick(view);
				}
			});
		}
	}

	@Override
	public void destroy() {
		if (mMopubNative != null) {
			mMopubNative.destroy();
			mMopubNative = null;
		}
	}

	// Private Method
	private EnumSet<NativeAdAsset> getDesiredAssets() {
		EnumSet<NativeAdAsset> desiredAssets = EnumSet.noneOf(NativeAdAsset.class);

		if (!TextUtils.isEmpty(mNativeContent)) {
			String[] assets = mNativeContent.split(",");
			for (String asset : assets) {
				int assetInt = Integer.parseInt(asset);
				switch (assetInt) {
				case 0:
				default:
					desiredAssets = EnumSet.allOf(NativeAdAsset.class);
					break;
				case 1:
					desiredAssets.add(NativeAdAsset.ICON_IMAGE);
					break;
				case 2:
					desiredAssets.add(NativeAdAsset.MAIN_IMAGE);
					break;
				case 3:
					desiredAssets.add(NativeAdAsset.TITLE);
					break;
				case 4:
					desiredAssets.add(NativeAdAsset.TEXT);
					break;
				case 5:
					desiredAssets.add(NativeAdAsset.CALL_TO_ACTION_TEXT);
					break;
				case 6:
					desiredAssets.add(NativeAdAsset.STAR_RATING);
					break;
				}
			}
		}
		return desiredAssets;
	}

	private String getKeywords() {
		// MoPub SDK accepts the targeting parameters in comma separated
		// format. e.g. "m_age:24,m_gender:m,m_marital:single"
		StringBuilder sb = new StringBuilder("");

		if (mKeywords != null && mKeywords.size() > 0) {
			for (Map.Entry<String, String> entry : mKeywords.entrySet()) {
				sb.append(entry.getKey());
				sb.append('=');
				sb.append(entry.getValue());
				sb.append('&');
			}
			sb.setLength(sb.length() - 1);
		}

		return sb.toString();
	}

	// MoPub Native Relative Listener Methods
	@Override
	public void onNativeLoad(NativeResponse response) {
		mNativeResponse = response;
		if (mAdDescriptor != null) {
			// Set native title
			mAdDescriptor.setNativeAdTitle(response.getTitle());

			// Set native text
			mAdDescriptor.setNativeAdText(response.getText());

			// Set icon image
			if (response.getIconImageUrl() != null) {
				MASTNativeAd.Image iconImage = new MASTNativeAd.Image(
						response.getIconImageUrl());
				mAdDescriptor.setIconImage(iconImage);
			}

			// Set main image
			if (response.getMainImageUrl() != null) {
				MASTNativeAd.Image mainImage = new MASTNativeAd.Image(
						response.getMainImageUrl());
				mAdDescriptor.setMainImage(mainImage);
			}

			// Click url
			mAdDescriptor.setClick(response.getClickDestinationUrl());

			// Set rating
			Double starRating = response.getStarRating();
			if (starRating != null) {
				mAdDescriptor.setNativeAdRating(starRating.floatValue());
			}

			// Call to action text
			mAdDescriptor.setNativeAdCallToActionText(response.getCallToAction());

			adReceived(this);
		} else {
			adFailed(this, new Exception("Invalid response received!"));
		}

	}

	@Override
	public void onNativeFail(NativeErrorCode errorCode) {
		adFailed(this, new Exception(errorCode.name()));
	}

	@Override
	public void onNativeClick(View view) {
		Log.i("MoPubAdapter", "ad clicked");
		adClicked(this);
	}

	@Override
	public void onNativeImpression(View view) {
		// NOOP
	}
}
