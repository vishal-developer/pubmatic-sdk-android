package com.pubmatic.sdk.common;

import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;

import org.json.JSONObject;

public interface RRFormatter {

    HttpRequest formatRequest(AdRequest request);

    AdResponse formatResponse(HttpResponse response);
}
