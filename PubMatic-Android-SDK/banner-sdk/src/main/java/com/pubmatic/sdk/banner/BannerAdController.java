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
package com.pubmatic.sdk.banner;

import android.content.Context;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.CommonConstants.CHANNEL;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BannerAdController {

	protected CHANNEL			mChannel;
	protected Context 			mContext 		= null;
	protected AdRequest 		mAdRequest 		= null;
	protected RRFormatter 		mRRFormatter 	= null;
	
	
	public BannerAdController(CHANNEL channel, Context cnt, AttributeSet attr) {
		mChannel = channel;
		mContext = cnt;
		createDefaultAdRequest(attr);
	}
	public AdRequest getAdRequest() {
		return mAdRequest;
	}

	//TODO :: Need to verify this method
	private void createDefaultAdRequest(AttributeSet attr) {

		String adRequestName = null;
		Class  className     = null;
		Method m			 = null;
		try {

			switch (mChannel) {
				case MOCEAN:
					adRequestName = "com.pubmatic.sdk.banner.mocean.MoceanBannerAdRequest";
					className = Class.forName(adRequestName);
					m = className.getMethod("createMoceanBannerAdRequest", Context.class, String.class);
					mAdRequest = (AdRequest)m.invoke(null, mContext, null);
					//Call setAttributes()
					m = className.getMethod("setAttributes", AttributeSet.class);
					m.invoke(mAdRequest, attr);

					break;
				case PUBMATIC:
					adRequestName = "com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest";
					className = Class.forName(adRequestName);
					m = className.getMethod("createPubMaticBannerAdRequest",
											Context.class, String.class, String.class, String.class);
					mAdRequest = (AdRequest)m.invoke(null, mContext, null, null, null);
					//Call setAttributes()
					m = className.getMethod("setAttributes", AttributeSet.class);
					m.invoke(mAdRequest, attr);
					break;
				case PHOENIX:
					adRequestName = "com.pubmatic.sdk.banner.phoenix.PhoenixBannerAdRequest";
					className = Class.forName(adRequestName);
					m = className.getMethod("createPhoenixBannerAdRequest", Context.class, String.class, String.class);
					mAdRequest = (AdRequest)m.invoke(null, mContext, null, null);
					//Call setAttributes()
					m = className.getMethod("setAttributes", AttributeSet.class);
					m.invoke(mAdRequest, attr);

					break;

				default:
					break;
			}

			createRRFormatter();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassCastException ex) {

		}
	}

	public void setAdRequest(AdRequest adRequest) {

		if (adRequest == null)
			throw new IllegalArgumentException("AdRequest object is null");

		mAdRequest = adRequest;
		//Create RRFormater
		createRRFormatter();
	}

	private void createRRFormatter() {
		if(mAdRequest != null)
		{
			//Create RRFormater
			String rrFormaterName = mAdRequest.getFormatter();

			try {
				Class className = Class.forName(rrFormaterName);
				mRRFormatter = (RRFormatter) className.newInstance();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (ClassCastException ex) {

			}
		}
	}

	public RRFormatter getRRFormatter() {
		return mRRFormatter;
	}

	public boolean checkMandatoryParams() {
		return mAdRequest==null ? false : mAdRequest.checkMandatoryParams();
	}

	public void applyAttributeSet(AttributeSet attrs) {
		// TODO Auto-generated method stub
		
	}
}
