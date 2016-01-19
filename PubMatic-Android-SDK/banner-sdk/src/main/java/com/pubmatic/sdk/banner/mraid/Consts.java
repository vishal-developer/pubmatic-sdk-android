/*
 * PubMatic Inc. (PubMatic) CONFIDENTIAL
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

package com.pubmatic.sdk.banner.mraid;

public interface Consts
{
    enum State
    {
        Loading,
        Default,
        Expanded,
        Resized,
        Hidden,
    }
    
    enum PlacementType
    {
        Inline,
        Interstitial,
    }

    enum Feature
    {
        SMS,
        Tel,
        Calendar,
        StorePicture,
        InlineVideo,
    }

    enum ForceOrientation
    {
        Portrait,
        Landscape,
        None,
    }

    enum CustomClosePosition
    {
        TopLeft,
        TopCenter,
        TopRight,
        Center,
        BottomLeft,
        BottomCenter,
        BottomRight,
    }
    
	String StateLoading = "loading";
    String StateDefault = "default";
    String StateExpanded = "expanded";
    String StateResized = "resized";
    String StateHidden = "hidden";

    String PlacementTypeInline = "inline";
    String PlacementTypeInterstitial = "interstitial";

	String FeatureSMS = "sms";
	String FeatureTel = "tel";
	String FeatureCalendar = "calendar";
	String FeatureStorePicture = "storePicture";
	String FeatureInlineVideo = "inlineVideo";

	String EventReady = "ready";

	String True = "true";
	String False = "false";

	String Scheme = "mraid";
	String CommandInit = "init";
	String CommandClose = "close";
	String CommandOpen = "open";
	String CommandUpdateCurrentPosition = "updatecurrentposition";
	String CommandExpand = "expand";
	String CommandSetExpandProperties = "setexpandproperties";
	String CommandResize = "resize";
	String CommandSetResizeProperties = "setresizeproperties";
	String CommandSetOrientationProperties = "setorientationproperties";
	String CommandPlayVideo = "playvideo";
	String CommandCreateCalendarEvent = "createcalendarevent";
	String CommandStorePicture = "storepicture";

	String CommandArgUrl = "url";
	String CommandArgEvent = "event";

	String PropertiesWidth = "width";
	String PropertiesHeight = "height";

	String ExpandPropertiesUseCustomClose = "useCustomClose";

	String ResizePropertiesCustomClosePosition = "customClosePosition";
	String ResizePropertiesOffsetX = "offsetX";
	String ResizePropertiesOffsetY = "offsetY";
	String ResizePropertiesAllowOffscreen = "allowOffscreen";

	String ResizePropertiesCCPositionTopLeft = "top-left";
	String ResizePropertiesCCPositionTopCenter = "top-center";
	String ResizePropertiesCCPositionTopRight = "top-right";
	String ResizePropertiesCCPositionCenter = "center";
	String ResizePropertiesCCPositionBottomLeft = "bottom-left";
	String ResizePropertiesCCPositionBottomCenter = "bottom-center";
	String ResizePropertiesCCPositionBottomRight = "bottom-right";

	String OrientationPpropertiesAllowOrientationChange = "allowOrientationChange";
	String OrientationPpropertiesForceOrientation = "forceOrientation";
	String OrientationPropertiesForceOrientationNone = "none";
	String OrientationPropertiesForceOrientationPortrait = "portrait";
	String OrientationPropertiesForceOrientationLandscape = "landscape";
}
