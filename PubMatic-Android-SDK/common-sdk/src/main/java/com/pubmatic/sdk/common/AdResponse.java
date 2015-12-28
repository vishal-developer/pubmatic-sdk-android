package com.pubmatic.sdk.common;

import com.pubmatic.sdk.common.AdRequest;

public class AdResponse {

	private AdRequest mRequest;
	//private Map<String, String> mHeaderList;
	private int mStatusCode;
	private String errorCode;
	private String errorMessage;
	private Exception exception;
	private Renderable mRenderable;

	public AdRequest getRequest() {
		return mRequest;
	}

	public void setRequest(AdRequest mRequest) {
		this.mRequest = mRequest;
	}
//
//	public Map<String, String> getHeaderList() {
//		return mHeaderList;
//	}
//
//	public void setHeaderList(Map<String, String> mHeaderList) {
//		this.mHeaderList = mHeaderList;
//	}

	public int getStatusCode() {
		return mStatusCode;
	}

	public void setStatusCode(int mStatusCode) {
		this.mStatusCode = mStatusCode;
	}

	public Renderable getRenderable() {
		return mRenderable;
	}

	public void setRenderable(Renderable renderable) {
		this.mRenderable = renderable;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorcode) {
		this.errorCode = errorcode;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

}
