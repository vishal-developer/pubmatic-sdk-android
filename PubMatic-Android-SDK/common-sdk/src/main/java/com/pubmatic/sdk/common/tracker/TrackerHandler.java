package com.pubmatic.sdk.common.tracker;

import android.text.TextUtils;
import android.util.Log;

import com.pubmatic.sdk.common.network.HttpHandler;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.network.RequestFormatter;
import com.pubmatic.sdk.common.utils.CommonConstants;

import java.util.ArrayList;

public class TrackerHandler {

    /*
     * Maximum number of Impression URLs that can be grouped in a thread , to be
     * submitted to TrackingThreadPoolExecutor. One thread will be created with
     * MAX_URL_PER_THREAD number of Impression URLs in it and given to
     * ThreadPoolExecutor
     */
    private static int MAX_URL_PER_THREAD = TrackingthreadPoolConstants.MAX_URL_PER_THREAD;

    private static final String TAG = "TrackerHandler";

    private static HttpHandler.HttpRequestListener httpRequestListener = new HttpHandler.HttpRequestListener() {

        @Override
        public void onRequestComplete(HttpResponse result, final String requestURL) {
            Log.d(TAG, "Tracking request completed for [" + requestURL + "]");
        }

        @Override
        public void onErrorOccured(final int errorType, int errorCode, final String requestURL) {
            Log.d(TAG, "Tracking request failed for [" + requestURL + "]");
        }

        @Override
        public boolean overrideRedirection() {
            return false;
        }
    };

    public static synchronized void sendTrackingRequest(final String url,
            final CommonConstants.CHANNEL channel) {
        if (url != null && !TextUtils.isEmpty(url)) {
            Runnable runnable = new HttpHandler(httpRequestListener,
                                                RequestFormatter.getTrackingRequest(url, channel));
            TrackingThreadPoolExecutor.getInstance().execute(runnable);
        }
    }

    public static void sendTrackingRequest(final String[] requestUrlArray,
            final CommonConstants.CHANNEL channel) {

        int leftSize = requestUrlArray.length;
        int i = 0;

        while (leftSize > 0) {
            ArrayList<HttpRequest> pubRequestList;

            if (leftSize > MAX_URL_PER_THREAD) {
                pubRequestList = new ArrayList<HttpRequest>(MAX_URL_PER_THREAD);

                for (int j = i * MAX_URL_PER_THREAD; j < ((i + 1) * MAX_URL_PER_THREAD); j++) {
                    pubRequestList.add(RequestFormatter.getTrackingRequest(requestUrlArray[j],
                                                                           channel));
                }
            } else {
                pubRequestList = new ArrayList<HttpRequest>(leftSize);

                for (int j = i * MAX_URL_PER_THREAD; j < (i * MAX_URL_PER_THREAD) + leftSize; j++) {
                    pubRequestList.add(RequestFormatter.getTrackingRequest(requestUrlArray[j],
                                                                           channel));
                }
            }

            Runnable runnable = new HttpHandler(httpRequestListener, pubRequestList);
            TrackingThreadPoolExecutor.getInstance().execute(runnable);

            i++;
            leftSize = leftSize - MAX_URL_PER_THREAD;

        }
    }
}
