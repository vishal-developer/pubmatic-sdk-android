package com.pubmatic.sdk.common.network;

import java.util.ArrayList;

import com.pubmatic.sdk.common.network.HttpWorker.HttpRedirectListener;
import com.pubmatic.sdk.common.utils.PubError;

public class HttpHandler implements Runnable, HttpRedirectListener {

    private ArrayList<HttpRequest> mHttpRequestList = null;
    private HttpRequest mHttpRequest;
    private HttpRequestListener mListener;

    public interface HttpRequestListener {

        void onRequestComplete(HttpResponse result, final String requestURL);

        void onErrorOccured(int errorType, int errorCode, final String requestURL);

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

                if (response.errorType != PubError.SUCCESS_CODE) {
                    mListener.onErrorOccured(response.errorType,
                                             response.errorCode,
                                             pubRequest.getRequestUrl());
                } else {
                    mListener.onRequestComplete(response, pubRequest.getRequestUrl());
                }
            } else {
                mListener.onErrorOccured(PubError.UNDEFINED_ERROR, -1, pubRequest.getRequestUrl());
            }
        }
    }

    @Override
    public boolean overrideRedirection() {
        return mListener == null ? false : mListener.overrideRedirection();
    }
}
