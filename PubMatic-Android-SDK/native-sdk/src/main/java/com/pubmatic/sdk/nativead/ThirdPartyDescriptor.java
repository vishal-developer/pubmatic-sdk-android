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

package com.pubmatic.sdk.nativead;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

public class ThirdPartyDescriptor {
	/**
	 * This method will return a parsed thirdparty descriptor
	 * 
	 * @param nativeAdDescriptor
	 * @return
	 * @throws JSONException
	 */
	public static ThirdPartyDescriptor getDescriptor(
			NativeAdDescriptor nativeAdDescriptor) throws JSONException {

		ThirdPartyDescriptor thirdPartyDescriptor = null;
		Map<String, String> params = null;
		Map<String, String> properties = null;

		if (nativeAdDescriptor != null) {
			thirdPartyDescriptor = new ThirdPartyDescriptor();
			params = new HashMap<String, String>();
			properties = new HashMap<String, String>();

			String adUnitId = nativeAdDescriptor.getAdUnitId();
			params.put("adid", adUnitId);

			thirdPartyDescriptor.params = params;
			thirdPartyDescriptor.properties = properties;
		}

		return thirdPartyDescriptor;
	}

	private Map<String, String> properties = new HashMap<String, String>();
	private Map<String, String> params = new HashMap<String, String>();

	private ThirdPartyDescriptor() {

	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public Map<String, String> getParams() {
		return params;
	}
}
