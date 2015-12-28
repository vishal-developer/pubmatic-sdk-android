/**
 *
 */
package com.pubmatic.sdk.common.network;

import android.content.Context;

import com.pubmatic.sdk.common.utils.CommonConstants;
import com.pubmatic.sdk.common.utils.CommonConstants.AD_REQUEST_TYPE;

import java.io.IOException;

import static com.pubmatic.sdk.common.utils.CommonConstants.CHANNEL;

/**
 * @author shrawangupta
 */
public abstract class RequestFormatter {

/*
    public abstract HttpRequest getTrackingRequest(final String requestUrl);
	
	public abstract HttpRequest getWrapperAdRequest(final Context appContext,
			final String requestUrl) throws IOException;
*/

    /**
     * Returns the {@link PubRequest} object which contains the information required to make the
     * HTTP request for getting the Ad
     *
     * @param appContext application context
     * @param requestParameters {@link RequestParameters} object
     *
     * @return Returns the {@link PubRequest} object which contains the information required to make
     * the HTTP request for getting the Ad
     *
     * @throws IOException
     */
    @SuppressWarnings("JavadocReference")
    public abstract HttpRequest getAdRequest(final Context appContext,
            final RequestParameters requestParameters) throws IOException;

    /**
     * Returns the POSTData string
     *
     * @param context Context of the application
     * @param requestParameters Ad request parameters provided by Publisher that will be send with
     * the HTTP request.
     */
    public abstract StringBuffer getAdRequestPOSTData(final Context context,
            RequestParameters requestParameters);

    public static HttpRequest getTrackingRequest(final String requestUrl,
            final CommonConstants.CHANNEL channel) {

        HttpRequest request = new HttpRequest();
        request.setRequestUrl(requestUrl);
        request.setRequestType(channel == CHANNEL.PUBMATIC ? AD_REQUEST_TYPE.PUB_TRACKER : channel == CHANNEL.MOCEAN ? AD_REQUEST_TYPE.MOCEAN_TRACKER : AD_REQUEST_TYPE.PHOENIX_TRACKER);
        request.setRequestMethod(ProtocolConstants.HTTPMETHODGET);
        return request;
    }
}
