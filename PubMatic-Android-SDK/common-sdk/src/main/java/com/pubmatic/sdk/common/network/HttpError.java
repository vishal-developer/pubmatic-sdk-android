package com.pubmatic.sdk.common.network;

public class HttpError {
	
	String message;
	int statusCode;
	StackTraceElement[] stackTraceElements;
	
	public static class ErrorCodes
	{
		 public static final int INVALID_AD_ERROR        = -1;
		 
		 public static final int UNDEFINED_ERROR        = -999;

		 public static final int REQUEST_ERROR         = -1000;
		 
		 public static final int CONNECTION_ERROR        = -1001;

		 public static final int SERVER_ERROR         = -1002;
		 
		 public static final int REQUEST_CANCLE         = -1003;
		 
		 public static final int TIMEOUT_ERROR         = -1004;
		 
		 public static final int GENERIC_IO_ERROR        = -1005;
		 
		 public static final int INVALID_RESPONSE_ERROR       = -1006;
		 
		 public static final int JSON_ERROR          = -1007;
		 
		 public static final int REDIRECT_ERROR         = -1302;
		 
		 public static final int SUCCESS_CODE         = 0;
		 
		 public static final int PARSE_ERROR          = -1009;
		 
		 public static final int AUTH_FALIURE_ERROR          = -1009;
	}
	
	public HttpError(String message, int statusCode,
			StackTraceElement[] stackTraceElements) {
		super();
		this.message = message;
		this.statusCode = statusCode;
		this.stackTraceElements = stackTraceElements;
	}

	public String getMessage() {
		return message;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void printStackTrace() {
		if(stackTraceElements != null)
			System.out.println(stackTraceElements);
	}
}
