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

public final class NativeAdSize {

	private int width = 0;
	private int height = 0;

	// @formatter:off
	// Icon image sizes
	public static NativeAdSize ICON_IMAGE_75X75 = new NativeAdSize(75, 75);
	public static NativeAdSize ICON_IMAGE_150X150 = new NativeAdSize(150, 150);
	public static NativeAdSize ICON_IMAGE_300X300 = new NativeAdSize(300, 300);
	
	// Logo image sizes
	public static NativeAdSize LOGO_IMAGE_75X75 = new NativeAdSize(75, 75);
	public static NativeAdSize LOGO_IMAGE_80X80 = new NativeAdSize(80, 80);
	public static NativeAdSize LOGO_IMAGE_150X150 = new NativeAdSize(150, 150);
	public static NativeAdSize LOGO_IMAGE_300X300 = new NativeAdSize(300, 300);

	// Main image sizes
	public static NativeAdSize MAIN_IMAGE_480X320 = new NativeAdSize(480, 320);
	public static NativeAdSize MAIN_IMAGE_960X640 = new NativeAdSize(960, 640);
	public static NativeAdSize MAIN_IMAGE_1200X800 = new NativeAdSize(1200, 800);
	public static NativeAdSize MAIN_IMAGE_300X250 = new NativeAdSize(300, 250);
	public static NativeAdSize MAIN_IMAGE_320X568 = new NativeAdSize(320, 568);
	public static NativeAdSize MAIN_IMAGE_640X1136 = new NativeAdSize(640, 1136);
	public static NativeAdSize MAIN_IMAGE_720X1280 = new NativeAdSize(720, 1280);
	public static NativeAdSize MAIN_IMAGE_250X300 = new NativeAdSize(250, 300);
	public static NativeAdSize MAIN_IMAGE_568X320 = new NativeAdSize(568, 320);
	public static NativeAdSize MAIN_IMAGE_1136X640 = new NativeAdSize(1136, 640);
	public static NativeAdSize MAIN_IMAGE_728X90 = new NativeAdSize(728, 90);
	public static NativeAdSize MAIN_IMAGE_256X135 = new NativeAdSize(256, 135);
	public static NativeAdSize MAIN_IMAGE_600X313 = new NativeAdSize(600, 313);
	public static NativeAdSize MAIN_IMAGE_1200X627 = new NativeAdSize(1200, 627);
	public static NativeAdSize MAIN_IMAGE_320X50 = new NativeAdSize(320, 50);
	public static NativeAdSize MAIN_IMAGE_320X480 = new NativeAdSize(320, 480);
	public static NativeAdSize MAIN_IMAGE_640X960 = new NativeAdSize(640, 960);
	public static NativeAdSize MAIN_IMAGE_800X1200 = new NativeAdSize(800, 1200);

	// @formatter:on

	public NativeAdSize(int width, int height) {
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException(
					"Width and Height should be greater than zero");
		}

		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	String getSizeInString() {
		return width + "x" + height;
	}

	String getAspectRatio() {
		String ratio = "";
		try {
			ratio = String.valueOf((width / height));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ratio;
	}
}
