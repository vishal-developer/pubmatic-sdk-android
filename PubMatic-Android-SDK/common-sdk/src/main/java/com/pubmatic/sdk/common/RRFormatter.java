package com.pubmatic.sdk.common;

import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;

public interface RRFormatter {

    public abstract HttpRequest formatRequest(AdRequest request);

    public abstract AdResponse formatResponse(HttpResponse response);

}
