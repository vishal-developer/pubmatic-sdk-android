/*
 * PubMatic Inc. (�PubMatic�) CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2017 PubMatic, All Rights Reserved.
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.banner.PMInterstitialAd;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.PMLogger;

public class InterstitialDemoActivity extends Activity {

    PMInterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pubmatic_interstitial);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        PMLogger.setLogLevel(PMLogger.LogLevel.Debug);

        setPrefetchIds("31400", "32504", "1059651");
    }

    private void setPrefetchIds(String pubId, String siteId, String adId) {
        final EditText pubIdET = (EditText)findViewById(R.id.pubIdET);
        if(pubIdET!=null) {
            pubIdET.setText(pubId);
        }
        final EditText siteIdET = (EditText)findViewById(R.id.siteIdET);
        if(siteIdET!=null) {
            siteIdET.setText(siteId);
        }
        final EditText adIdET = (EditText)findViewById(R.id.adIdET);
        if(adIdET!=null) {
            adIdET.setText(adId);
        }

        //Handle click of load ad button
        Button loadAdBtn = (Button)findViewById(R.id.loadAdBtn);
        loadAdBtn.setText("Load Interstitial Ad");
        loadAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pubId = null, siteId = null, adId = null;
                if(pubIdET!=null) {
                    pubId = pubIdET.getText().toString();
                }
                if(siteIdET!=null) {
                    siteId = siteIdET.getText().toString();
                }
                if(adIdET!=null) {
                    adId = adIdET.getText().toString();
                }

                loadAd(pubId, siteId, adId, v);
            }
        });
    }

    private void loadAd(String pubId, String siteId, String adId, View view) {

        if (interstitialAd == null) {
            interstitialAd = new PMInterstitialAd(this);
            interstitialAd.setUseInternalBrowser(true);
        }

        interstitialAd.setRequestListener(new PMInterstitialAd.InterstitialAdListener.RequestListener() {
            @Override
            public void onFailedToReceiveAd(PMInterstitialAd adView, int errorcode, String errorMessage) {
                Toast.makeText(InterstitialDemoActivity.this,
                               "Ad failed to load!",
                               Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedAd(PMInterstitialAd adView) {
                Toast.makeText(InterstitialDemoActivity.this,
                        "Ad loaded!",
                        Toast.LENGTH_SHORT).show();
                interstitialAd.showInterstitial();
            }
        });

        PubMaticBannerAdRequest adRequest = PubMaticBannerAdRequest.createPubMaticBannerAdRequest(
                this, pubId, siteId, adId);
        interstitialAd.execute(adRequest);
    }

    private void destroyAd() {
        if(interstitialAd!=null)
            interstitialAd.destroy();
        interstitialAd = null;
    }

    @Override
    protected void onDestroy() {
        destroyAd();
        super.onDestroy();
    }
}
