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

/**
 * {@link MASTBaseAdapterListener} interface is implemented by {@link MASTNativeAd}.
 * This interface provides all callback methods used in the Adapters.
 */
public interface MASTBaseAdapterListener {

	/**
	 * Function will get call when Ad request loaded an Ad successfully.
	 * 
	 * @param adapter
	 *            Instance of the adapter class
	 */
	 void onReceiveAd(MASTBaseAdapter adapter);

	/**
	 * Function will get called when Ad request has failed due to no network
	 * connection or if no Ad's found to display. Using PUBRequestError
	 * parameter, you can find out what is the error cause.
	 * 
	 * @param adapter
	 *            Instance of the adapter class
	 * @param pubError
	 *            The {@link Exception} object which has the cause and the error
	 *            code
	 */
	void onReceiveError(MASTBaseAdapter adapter, Exception exception);

	/**
	 * Function will get call when Ad is clicked.
	 * 
	 * @param adapter
	 *            Instance of the adapter class
	 */
	void onAdClicked(MASTBaseAdapter adapter);
}
