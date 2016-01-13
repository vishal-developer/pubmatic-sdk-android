package com.pubmatic.sdk.common.network;

import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.common.CommonConstants.PubError;

public class HttpResponse {

    public int errorCode = 0;
    public int errorType = PubError.UNDEFINED_ERROR;
    private CONTENT_TYPE contentType = CONTENT_TYPE.INVALID;
    private StringBuffer stringResponse = null;
    private HttpRequest httpRequest = null;

    public HttpResponse() {

    }

    public HttpResponse(String response) {
        stringResponse = new StringBuffer(response);
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public String getResponseData() {
        return null == stringResponse ? null : stringResponse.toString();
    }

    public void setResponse(String data) {
        if (stringResponse == null) {
            stringResponse = new StringBuffer(data);
        } else {
            stringResponse.append(data);
        }
    }

    public void resetResponse() {
        stringResponse = null;
    }

    public CONTENT_TYPE getContentType() {
        return contentType;
    }

    public void setContentType(CONTENT_TYPE type) {
        contentType = type;
    }

}
