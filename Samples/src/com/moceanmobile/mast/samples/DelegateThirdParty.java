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

package com.moceanmobile.mast.samples;

import java.util.Map;

import android.os.Bundle;

import com.moceanmobile.mast.MASTAdView;
import com.moceanmobile.mast.MASTAdView.LogLevel;
import com.moceanmobile.mast.MASTAdViewDelegate.RequestListener;

public class DelegateThirdParty extends DelegateGeneric {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final MASTAdView adView = (MASTAdView) findViewById(R.id.adView);

		// adView.setZone(90038);
		adView.setZone(179492); // Test tag

		// Set custom Base URL for mocean adserver if required
		adView.setAdNetworkURL("http://ads.test.mocean.mobi/ad?creatives=559665");

		// Disable logging in production to prevent output spam.
		adView.setLogLevel(LogLevel.Debug);

		adView.setRequestListener(new RequestListener() {

			@Override
			public void onReceivedThirdPartyRequest(MASTAdView mastAdView,
					Map<String, String> properties, Map<String, String> params) {
				appendOutput("Properties: " + properties);
				appendOutput("Parameters: " + params);
				if (mastAdView.getMediationData() != null) {
					appendOutput(mastAdView.getMediationData().toString());
				}

				// ---------------------------------------------------------
				// Write Code to initialize third party SDK and request ads.
				// ---------------------------------------------------------

				// Test sending impression tracker and click trackers.

				// Note: This method should be called only when ad from third
				// party SDK is rendered.
				mastAdView.sendImpression(); // Method added here only for
												// testing purpose

				// Note: This method should be called only when ad clicked
				// callback is received from third party SDK.
				mastAdView.sendClickTracker(); // Method added here only for
												// testing purpose
			}

			@Override
			public void onReceivedAd(MASTAdView mastAdView) {
				appendOutput("DelegateThirdParty.onReceivedAd");
			}

			@Override
			public void onFailedToReceiveAd(MASTAdView mastAdView, Exception ex) {
				appendOutput("DelegateThirdParty.onFailedToReceiveAd. Exception: "
						+ ex.getMessage());
			}
		});
	}
}
