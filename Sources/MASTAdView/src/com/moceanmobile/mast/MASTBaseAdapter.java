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

// SYSTEM IMPORTS
import java.lang.reflect.Constructor;
import java.util.Map;

import android.content.Context;
import android.view.View;

/**
 * This class will be used as base class for integrating any third-party SDK.
 * Adapter for every Ad-network SDK must inherit this class.
 */
public abstract class MASTBaseAdapter {

	public static enum MediationNetwork {
		FACEBOOK_AUDIENCE_NETWORK, MOPUB
	};

	protected MASTBaseAdapterListener mBaseAdapterListener;
	protected Context mContext = null;
	protected String mAdUnitId = null;
	protected NativeAdDescriptor mAdDescriptor = null;
	protected Map<String, String> mKeywords = null;
	protected Map<MediationNetwork, String> mMapMediationNetworkTestDeviceIds = null;
	/**
	 * List of supported asset elements of native ad. <br />
	 * Possible values: <br />
	 * 0 - all fields; <br />
	 * 1 - icon image; <br />
	 * 2 - main image; <br />
	 * 3 - title; <br />
	 * 4 - description; <br />
	 * 5 - call to action; <br />
	 * 6 - rating. <br />
	 * <br />
	 * You can set different combinations with these values. <br />
	 * For example, native_content=1,3,4 (icon + title + description).
	 * 
	 * @param nativeContent
	 *            Any of the values between 0 to 6. Or combintation of values
	 *            comma separated e.g. 1,4,5.
	 */
	protected String mNativeContent = null;

	/**
	 * This method will initialize the adapter. This method will be called from
	 * MASTNativeAd.java and it will also set listeners to listen the events from
	 * the thirdparty SDK events.
	 * 
	 * @param context
	 * @param adDescriptor
	 * @param adapterListener
	 */
	protected void init(Context context, NativeAdDescriptor adDescriptor,
			MASTBaseAdapterListener adapterListener) {

		mContext = context;
		mAdDescriptor = adDescriptor;
		mBaseAdapterListener = adapterListener;
		if (mAdDescriptor != null) {
			mAdUnitId = mAdDescriptor.getAdUnitId();
		}
	}

	/**
	 * This function create new instance of given class name.
	 * 
	 * @param className
	 *            - class name string.
	 * @return Instance of the Adapter
	 */
	protected static final MASTBaseAdapter getAdapterForClass(String className,
			Context context) {
		MASTBaseAdapter adapterInstance = null;

		try {
			Class<?> adapterClass = getClass(className);
			if (adapterClass != null) {
				Constructor<?> constructor = adapterClass.getConstructor();
				adapterInstance = (MASTBaseAdapter) constructor.newInstance();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return adapterInstance;
	}

	/**
	 * This function check if the given class is present in the code or not.
	 * 
	 * @param className
	 * @throws ClassNotFoundException
	 */
	protected static Class<?> getClass(String className)
			throws ClassNotFoundException {
		return (Class<?>) Class.forName(className);
	}

	/**
	 * This method is need to be implemented by each Adapter. This will create
	 * the adView object set the relative information and then gives call for
	 * getAd to respective SDK.
	 */
	public abstract void loadAd();

	/**
	 * Tracks the given for interactions, this method is required for some
	 * thirdparty SDKs/adapters to tell the adapter to track the interactions
	 * happening on this view.
	 * 
	 * The thirdparty SDK/adapter will track all the events on this view such as
	 * click etc. and inform the caller using {@link MASTBaseAdapterListener}.
	 * 
	 * @param view
	 *            view on which interactions to be tracked.
	 */
	public abstract void trackViewForInteractions(View view);

	/**
	 * Destroy the resources of the adapter.
	 */
	public void destroy() {
		mContext = null;
		mBaseAdapterListener = null;
		mAdDescriptor = null;
	}

	protected final void adReceived(MASTBaseAdapter adapter) {
		if (mBaseAdapterListener != null) {
			mBaseAdapterListener.onReceiveAd(adapter);
		}
	}

	protected final void adFailed(MASTBaseAdapter adapter, Exception exception) {
		if (mBaseAdapterListener != null) {
			mBaseAdapterListener.onReceiveError(adapter, exception);
		}
	}

	protected final void adClicked(MASTBaseAdapter adapter) {
		if (mBaseAdapterListener != null) {
			mBaseAdapterListener.onAdClicked(adapter);
		}
	}

}
