/*
 * PubMatic Inc. (�PubMatic�) CONFIDENTIAL
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
package com.pubmatic.sampleapp.interstitial;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.PMInterstitialAdView;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.PMLogger;

import java.util.Map;

public class PubMaticInterstitialActivity extends Activity {

    PMInterstitialAdView interstitialAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pubmatic_interstitial);
    }

    public void loadInterstitialButtonClicked(View view) {
        PMLogger.setLogLevel(PMLogger.LogLevel.Debug);

        if (interstitialAdView == null) {
            interstitialAdView = new PMInterstitialAdView(this);

            RelativeLayout layout = (RelativeLayout) findViewById(R.id.parent);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                                                 RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.setLayoutDirection(RelativeLayout.ALIGN_PARENT_TOP);
            }
            layout.addView(interstitialAdView, params);

            interstitialAdView.setUseInternalBrowser(true);
        }

        interstitialAdView.setRequestListener(new PMBannerAdView.BannerAdViewDelegate.RequestListener() {
            @Override
            public void onFailedToReceiveAd(PMBannerAdView adView, int errorcode, String errorMessage) {
                Toast.makeText(PubMaticInterstitialActivity.this,
                               "Ad failed to load!",
                               Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedAd(PMBannerAdView adView) {
                Toast.makeText(PubMaticInterstitialActivity.this,
                        "Ad loaded!",
                        Toast.LENGTH_SHORT).show();
                interstitialAdView.showInterstitial();
            }

            @Override
            public void onReceivedThirdPartyRequest(PMBannerAdView adView,
                    Map<String, String> properties,
                    Map<String, String> parameters) {
                // No operation
            }
        });

        PubMaticBannerAdRequest adRequest = PubMaticBannerAdRequest.createPubMaticBannerAdRequest(
                this, "31400", "32504", "884567");
        interstitialAdView.execute(adRequest);
    }
}
