/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2014 PubMatic, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained
 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed 
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes  
 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, 
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE 
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS  
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.                
 */

package com.moceanmobile.mast;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public final class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
	private interface ImageDownloaderListener {
		public void onSuccess(Bitmap bitmap);

		public void onError();
	}

	private ImageDownloaderListener listener = null;

	/**
	 * @param listener
	 */
	public ImageDownloader(ImageDownloaderListener listener) {
		super();
		this.listener = listener;
	}

	/**
	 * Fetch the bitmap from given url and load the image inside the imageView.
	 * 
	 * @param imageView
	 * @param url
	 */
	public static void loadImage(final android.widget.ImageView imageView,
			final String url) {

		if (imageView == null) {
			return;
		}

		// Remove all previous images while the new image is loaded
		imageView.setImageBitmap(null);

		// Request new url
		new ImageDownloader(new ImageDownloaderListener() {

			@Override
			public void onSuccess(Bitmap bitmap) {
				imageView.setImageBitmap(bitmap);
			}

			@Override
			public void onError() {

			}
		}).execute(url);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (listener != null) {
			if (bitmap != null) {
				listener.onSuccess(bitmap);
			} else {
				listener.onError();
			}
		}
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		InputStream inputStream = null;
		Bitmap bitmap = null;
		String url = null;

		try {
			if (params != null && params.length > 0
					&& (url = params[0]) != null) {

				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams,
						Defaults.NETWORK_TIMEOUT_SECONDS * 1000);

				HttpClient httpClient = new DefaultHttpClient(httpParams);

				HttpGet httpGet = new HttpGet(url);
				httpGet.setHeader("Connection", "close");

				HttpResponse httpResponse = httpClient.execute(httpGet);

				if (httpResponse.getStatusLine()
								.getStatusCode() == 200) {
					inputStream = httpResponse.getEntity()
												.getContent();
					if (!isCancelled()) {
						bitmap = BitmapFactory.decodeStream(inputStream);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return bitmap;
	}
}
