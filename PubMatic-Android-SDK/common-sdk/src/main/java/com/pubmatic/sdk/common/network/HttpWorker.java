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

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.common.CommonConstants.PubError;
import com.pubmatic.sdk.common.PMLogger;

public class HttpWorker {

	private static String TAG 	 					= "HttpWorker";
	private boolean mIsCancelled 					= false;
	
	public interface HttpRedirectListener {
		public abstract boolean overrideRedirection();
	}
	
	
	private void setAdHeaders(HttpRequest httpRequest, HttpURLConnection httpUrlConnection) throws IOException {

		if(httpRequest==null || httpUrlConnection==null)
			return;

        if(CommonConstants.HTTPMETHODPOST.equals(httpRequest.getRequestMethod()))
		    httpUrlConnection.setDoOutput(true);

		// Setting headers
		if (httpRequest.mContentTypeHeader != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.CONTENT_TYPE,
					httpRequest.mContentTypeHeader);
		}

		if (httpRequest.mContentLength != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.CONTENT_LENGTH,
					httpRequest.mContentLength);
		}

		if (httpRequest.mContentMd5 != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.CONTENT_MD5,
					httpRequest.mContentMd5);
		}

		if (httpRequest.mHost != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.HOST, httpRequest.mHost);
		}

		if (httpRequest.mContentLanguage != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.CONTENT_LANGUAGE,
					httpRequest.mContentLanguage);
		}

		if (httpRequest.mAcceptLangauge != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.ACCEPT_LANGUAGE,
					httpRequest.mAcceptLangauge);
		}

		if (httpRequest.getUserAgent() != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.USER_AGENT,
					httpRequest.getUserAgent());
		}

		if (httpRequest.mAccept != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.ACCEPT,
					httpRequest.mAccept);
		}

		if (httpRequest.mAcceptCharset != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.ACCEPT_CHARSET,
					httpRequest.mAcceptCharset);
		}

		if (httpRequest.mAcceptDateTime != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.ACCEPT_DATETIME,
					httpRequest.mAcceptDateTime);
		}

		if (httpRequest.mCacheControl != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.CACHE_CONTROL,
					httpRequest.mCacheControl);
		}

		if (httpRequest.mDate != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.DATE, httpRequest.mDate);
		}

		if (httpRequest.mConnection != null) {
			httpUrlConnection.setRequestProperty(
					CommonConstants.CONNECTION, httpRequest.mConnection);
		}

		switch (httpRequest.getContentType()) {
			case URL_ENCODED:
				httpUrlConnection.setRequestProperty(
						CommonConstants.CONTENT_TYPE,
						"application/x-www-form-urlencoded");
				break;
	
			case JSON:
				httpUrlConnection.setRequestProperty(
						CommonConstants.CONTENT_TYPE,
						"application/json");
				break;
				
			default:
				break;
		}
	}

	public HttpResponse execute(HttpRequest httpRequest, HttpRedirectListener redirectListener) {
		
		InputStreamReader inputStreamReader = null;
		InputStream 	  inputStream 		= null;
		BufferedReader    reader 			= null;
		HttpURLConnection httpUrlConnection = null;
		HttpResponse 	  httpResponse 		= new HttpResponse();
		URL 			  url 				= null;
		String 			  newUrl 			= null;
		int 			  responseCode 		= -1;
		boolean 		  redirect 			= false;

		try {

			httpResponse.setHttpRequest(httpRequest);
			
			// Check whether the param is null or not
			if (httpRequest == null || httpRequest.getRequestUrl() == null) {
				httpResponse.errorType = PubError.REQUEST_ERROR;
				return httpResponse;
			}

			String requestMethod = httpRequest.getRequestMethod();

			if(TextUtils.isEmpty(requestMethod)) {
				httpResponse.errorType = PubError.REQUEST_ERROR;
				return httpResponse;
			}

			// Create url for GET/POST method
			if(requestMethod.equalsIgnoreCase(CommonConstants.HTTPMETHODGET) && httpRequest.getPostData() != null) {
				StringBuffer getUrl = new StringBuffer(httpRequest.getRequestUrl());
				if(!getUrl.toString().endsWith("?"))
					getUrl.append("?");
				getUrl.append(httpRequest.getPostData());
				url = new URL(getUrl.toString());

				PMLogger.logEvent(TAG + ": Http GET request  = " + getUrl.toString(),
						PMLogger.LogLevel.Debug);
				getUrl = null;
			} else {
				url = new URL(httpRequest.getRequestUrl());

				PMLogger.logEvent(TAG + ": Http request  = " + httpRequest.getRequestUrl(),
						PMLogger.LogLevel.Debug);
			}

			// Get connection object
			httpUrlConnection = (HttpURLConnection) url.openConnection();

			if (httpUrlConnection != null) {

				// Setting request method, headers and body
				setAdHeaders(httpRequest, httpUrlConnection);

				// Set the properties of HttpUrlConnection
		        HttpURLConnection.setFollowRedirects(false);
				httpUrlConnection.setInstanceFollowRedirects(false);
				httpUrlConnection.setRequestProperty("User-Agent", httpRequest.getUserAgent());
				httpUrlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				httpUrlConnection.setConnectTimeout(CommonConstants.MAX_SOCKET_TIME);

                if(httpRequest.getRequestMethod() != null)
				    httpUrlConnection.setRequestMethod(httpRequest.getRequestMethod());

				// Uploading the body of POST request
				if(requestMethod.equalsIgnoreCase(CommonConstants.HTTPMETHODPOST) && httpRequest.getPostData() != null) {
					String postData = httpRequest.getPostData();
					httpUrlConnection
							.setFixedLengthStreamingMode(postData.toString()
									.getBytes().length);

					DataOutputStream dataOutputStream = new DataOutputStream(
							httpUrlConnection.getOutputStream());
					if (dataOutputStream != null) {
						// Write the content
						dataOutputStream
								.writeBytes(postData.toString());
						dataOutputStream.flush();
						dataOutputStream.close();

                        PMLogger.logEvent(TAG + ": Http request body = " + postData.toString(),
                                          PMLogger.LogLevel.Debug);
					}
				}
				
				responseCode = httpUrlConnection.getResponseCode();

				//Check for Redirection
				//Case 1 of redirect :: Check HTTP response code for 301, 302
				if (responseCode != HttpURLConnection.HTTP_OK) {
					if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
						|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
							|| responseCode == HttpURLConnection.HTTP_SEE_OTHER)
					redirect = true;
					newUrl = httpUrlConnection.getHeaderField("Location");
				}

				//Case 2 of redirect :: check if host changes
				if (!url.getHost().equals(httpUrlConnection.getURL().getHost())) {
					redirect = true;
					newUrl = httpUrlConnection.getURL().toString();
				}
				
				if(redirect) { //Handle redirect
					
					if(redirectListener!=null && redirectListener.overrideRedirection()) {

			        	httpResponse.errorCode 		= responseCode;

						if (isCancelled()) 
							httpResponse.errorType = PubError.REQUEST_CANCLE;
						else
							httpResponse.errorType = PubError.REDIRECT_ERROR;
						
						if(httpResponse!=null)
						{
                            PMLogger.logEvent(TAG + ": Http redirect response  = " + httpResponse
                                                                      .getResponseData(),
                                              PMLogger.LogLevel.Debug);
						}
						return httpResponse;
						
					} else {
						
						// open the new connection again
						httpUrlConnection = (HttpURLConnection) new URL(newUrl).openConnection();
						httpUrlConnection.setRequestProperty("User-Agent", httpRequest.getUserAgent());
						httpUrlConnection.setRequestProperty("Accept", httpRequest.mAccept);
				 
						responseCode = httpUrlConnection.getResponseCode();
					}
				} // End of Redirection
				
				if (responseCode == HttpURLConnection.HTTP_OK) {

		        	String contentTypeHeader = httpUrlConnection.getHeaderField("Content-Type");
		        	httpResponse.setContentType(contentTypeHeader.contains("json") ? 
							        			CONTENT_TYPE.JSON : contentTypeHeader.contains("xml") ? 
							        			CONTENT_TYPE.XML : CONTENT_TYPE.INVALID);
		        	
					// Since the connection is successful, read the response
					inputStream = httpUrlConnection.getInputStream();
					if (inputStream == null) {
						httpResponse.errorType = PubError.CONNECTION_ERROR;
						return httpResponse;
					}
					
					inputStreamReader = new InputStreamReader(inputStream);
					reader = new BufferedReader(inputStreamReader);

					String inputLine = null;
					while ((inputLine = reader.readLine()) != null) {
						httpResponse.setResponse(inputLine);
					}
					httpResponse.errorType = PubError.SUCCESS_CODE;
		        	
				} // if (responseCode == HttpURLConnection.HTTP_OK) ends
				else {
					httpResponse.errorType = PubError.SERVER_ERROR;
		        	if(httpUrlConnection!=null)
		        		httpResponse.errorCode = httpUrlConnection.getResponseCode();
				}
			}

			//TODO :: Need to get the Response header as well.
			
			
			// Return the data only if the current request is not cancelled
			if (isCancelled()) {
				httpResponse.errorType = PubError.REQUEST_CANCLE;
				return httpResponse;
			}
			if(httpResponse!=null)
                PMLogger.logEvent(TAG + ": Http response  = " + httpResponse.getResponseData(),
                                  PMLogger.LogLevel.Debug);
			return httpResponse;
		} catch (SocketTimeoutException e) {
			//e.printStackTrace();
			httpResponse.errorType = PubError.TIMEOUT_ERROR;
			return httpResponse;
		} catch (IOException e) {
			//e.printStackTrace();
			httpResponse.errorType = PubError.CONNECTION_ERROR;
			return httpResponse;
		} catch (Exception e) {
			e.printStackTrace();
			httpResponse.errorType = PubError.INVALID_AD_ERROR;
			return httpResponse;
		} finally {
			try {
				// Release the resources
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}

				if (inputStreamReader != null) {
					inputStreamReader.close();
					inputStreamReader = null;
				}

				if (reader != null) {
					reader.close();
					reader = null;
				}

				if (httpUrlConnection != null) {
					httpUrlConnection.disconnect();
					httpUrlConnection = null;
				}
			} catch (IOException e) {

			}
		}
	
	}


	public void cancelRequest() {
		mIsCancelled = true;
	}

	private boolean isCancelled() {
		return mIsCancelled;
	}

}
