/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2017
 * PubMatic, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * PubMatic. The intellectual and technical concepts contained herein are
 * proprietary to PubMatic and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from PubMatic.
 * Access to the source code contained herein is hereby forbidden to anyone
 * except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such
 * access.
 *
 * The copyright notice above does not evidence any actual or intended
 * publication or disclosure of this source code, which includes information
 * that is confidential and/or proprietary, and is a trade secret, of PubMatic.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC
 * DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN
 * CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE
 * CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO
 * REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR
 * SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.pubmatic.sdk.banner.mocean;

import com.pubmatic.sdk.banner.BannerAdDescriptor;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

public class MoceanBannerRRFormatter implements RRFormatter {


	private AdRequest mRequest;

	@Override
	public HttpRequest formatRequest(AdRequest request) {
		mRequest = request;
		MoceanBannerAdRequest adRequest = (MoceanBannerAdRequest) request;
		HttpRequest httpRequest = new HttpRequest(CONTENT_TYPE.URL_ENCODED);
		httpRequest.setUserAgent(adRequest.getUserAgent());
		httpRequest.setConnection("close");
		httpRequest.setRequestUrl(request.getAdServerURL());
		httpRequest.setPostData(adRequest.getPostData());
		httpRequest.setRequestMethod(CommonConstants.HTTPMETHODPOST);
		return httpRequest;
	}

	@Override
	public AdResponse formatResponse(HttpResponse response) {

		AdResponse adResponse = new AdResponse();
		// adResponse.setStatusCode(response.getStatusCode());
		adResponse.setRequest(mRequest);
		// adResponse.setHeaderList(response.getHeaders());

		// Below code is to parse only xml data from a server response for now
		try {
			XmlPullParserFactory parserFactory = XmlPullParserFactory
					.newInstance();
			parserFactory.setNamespaceAware(false);
			parserFactory.setValidating(false);

			XmlPullParser parser = parserFactory.newPullParser();
			String strResponse = response.getResponseData();
			parser.setInput(new StringReader(strResponse));

			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
					if ("error".equals(name)) {
						String errorCode = parser.getAttributeValue(null,
								"code");
						String errorMessage = null;

						// read past the name
						parser.next();

						// read the contents
						if (parser.getEventType() == XmlPullParser.TEXT) {
							errorMessage = parser.getText();
						}

						adResponse.setErrorCode(errorCode);
						adResponse.setErrorMessage(errorMessage);

						// stop parsing
						break;
					} else if ("ad".equals(name)) {
						BannerAdDescriptor adDescriptor = BannerAdDescriptor
								.parseDescriptor(parser);
						adResponse.setRenderable(adDescriptor);

						/*
						 * The stream may contain more descriptors but only the
						 * first one matters (and really, should be the only
						 * one)
						 */
						break;
					}
				}

				parser.next();
				eventType = parser.getEventType();
			} // while ends

		} catch (Exception ex) {
			// maybe set errorcode constants for various events like failing/parsing-error/exception
			adResponse.setException(ex);
		} finally {

		}
		return adResponse;

	}

    public AdResponse formatHeaderBiddingResponse(JSONObject response) {
        return null;
    }

	public AdRequest getAdRequest() {
		return mRequest;
	}

	public void setAdRequest(AdRequest mRequest) {
		this.mRequest = mRequest;
	}

	public AdRequest getRequest() {
		return mRequest;
	}

	public void setRequest(AdRequest mRequest) {
		this.mRequest = mRequest;
	}

}
