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
package com.pubmatic.sdk.common.network;

import java.util.ArrayList;

import com.pubmatic.sdk.common.PMError;
import com.pubmatic.sdk.common.network.HttpWorker.HttpRedirectListener;

public class HttpHandler implements Runnable, HttpRedirectListener {

    private ArrayList<HttpRequest> mHttpRequestList = null;
    private HttpRequest mHttpRequest;
    private HttpRequestListener mListener;

    public interface HttpRequestListener {

        void onRequestComplete(HttpResponse result, final String requestURL);

        void onErrorOccured(PMError error, final String requestURL);

        boolean overrideRedirection();
    }

    public HttpHandler(HttpRequestListener httpRequestListener,
            ArrayList<HttpRequest> pubRequestList) {
        mListener = httpRequestListener;
        mHttpRequestList = pubRequestList;
    }

    public HttpHandler(HttpRequestListener httpRequestListener, HttpRequest pubRequest) {
        mListener = httpRequestListener;
        mHttpRequest = pubRequest;
    }

    @Override
    public void run() {

        if (mHttpRequestList != null) {
            for (HttpRequest pubRequest : mHttpRequestList) {
                execute(pubRequest);
            }

        } else if (mHttpRequest != null) {
            execute(mHttpRequest);
        }
    }

    private void execute(HttpRequest pubRequest) {
        HttpResponse response = new HttpWorker().execute(pubRequest, this);

        if (mListener != null) {

            if (response != null) {

                PMError error = response.getError();

                if(error != null) {
                    mListener.onErrorOccured(error,pubRequest.getRequestUrl());
                } else {
                    mListener.onRequestComplete(response, pubRequest.getRequestUrl());
                }

            } else {
                mListener.onErrorOccured(new PMError(PMError.INTERNAL_ERROR, "Invalid error"), pubRequest.getRequestUrl());
            }
        }
    }

    @Override
    public boolean overrideRedirection() {
        return mListener == null ? false : mListener.overrideRedirection();
    }
}
