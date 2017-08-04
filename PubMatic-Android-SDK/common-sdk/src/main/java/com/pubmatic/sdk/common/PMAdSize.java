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
package com.pubmatic.sdk.common;

/**
 * Class provides the different Ad sizes for Banner Ads.
 */
public final class PMAdSize {

	private int adWidth;
	private int adHeight;

	// Supported Banner Ad Size for both android phone and tablet
	public static final PMAdSize PUBBANNER_SIZE_320x50 = new PMAdSize(320, 50);
	public static final PMAdSize PUBBANNER_SIZE_300x50 = new PMAdSize(300, 50);
	public static final PMAdSize PUBBANNER_SIZE_300x250 = new PMAdSize(300,250);
	public static final PMAdSize PUBBANNER_SIZE_38x38 = new PMAdSize(38, 38);

	// Banner Ad Size for android phone
	public static final PMAdSize PUBBANNER_SIZE_320x416 = new PMAdSize(320, 416);
	public static final PMAdSize PUBBANNER_SIZE_320x100 = new PMAdSize(320, 100);
	public static final PMAdSize PUBBANNER_SIZE_320x53 = new PMAdSize(320, 53);
	public static final PMAdSize PUBBANNER_SIZE_480x32 = new PMAdSize(480, 32);

	// Banner Ad Size for android tablet
	public static final PMAdSize PUBBANNER_SIZE_768x66 = new PMAdSize(768, 66);
	public static final PMAdSize PUBBANNER_SIZE_768x90 = new PMAdSize(768, 90);
	public static final PMAdSize PUBBANNER_SIZE_728x90 = new PMAdSize(728, 90);
	public static final PMAdSize PUBBANNER_SIZE_1024x90 = new PMAdSize(1024, 90);
	public static final PMAdSize PUBBANNER_SIZE_1024x66 = new PMAdSize(1024, 66);
	public static final PMAdSize PUBBANNER_SIZE_160x600 = new PMAdSize(160, 600);
	public static final PMAdSize PUBBANNER_SIZE_120x60 = new PMAdSize(120, 60);
	public static final PMAdSize PUBBANNER_SIZE_555x206 = new PMAdSize(555, 206);
	public static final PMAdSize PUBBANNER_SIZE_500x500 = new PMAdSize(500, 500);
	public static final PMAdSize PUBBANNER_SIZE_250x250 = new PMAdSize(250, 250);
	public static final PMAdSize PUBBANNER_SIZE_216x36 = new PMAdSize(216, 36);
	public static final PMAdSize PUBBANNER_SIZE_210x175 = new PMAdSize(210, 175);
	public static final PMAdSize PUBBANNER_SIZE_200x120 = new PMAdSize(200, 120);
	public static final PMAdSize PUBBANNER_SIZE_185x30 = new PMAdSize(185, 30);
	public static final PMAdSize PUBBANNER_SIZE_168x28 = new PMAdSize(168, 28);
	public static final PMAdSize PUBBANNER_SIZE_120x20 = new PMAdSize(120, 20);

	/*
	* Making the constructors private to avoid the creation
	* of the PUBAdSize object without width and height
	 */
	private PMAdSize() {}

	/**
	 * @param adWidth
	 * @param adHeight
	 */
	public PMAdSize(int adWidth, int adHeight) {
		this.adWidth = adWidth;
		this.adHeight = adHeight;
	}

	/**
	 * @return the adWidth
	 */
	public int getAdWidth() {
		return adWidth;
	}

	/**
	 * @return the adHeight
	 */
	public int getAdHeight() {
		return adHeight;
	}
}