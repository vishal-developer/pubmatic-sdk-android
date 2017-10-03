package com.pubmatic.sdk.nativead.bean;

import com.pubmatic.sdk.nativead.PMNativeAd.Image;

public class PMNativeImageAssetResponse extends PMNativeAssetResponse {

    /** Image Asset */
    public Image image;

    /** Image Type (Optional in response) */
    public PMNativeImageAssetTypes imageType;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * Image type is only available in case of mediation response
     * 
     * @return Type of image asset
     */
    public PMNativeImageAssetTypes getImageType() {
        return imageType;
    }

    public void setImageType(PMNativeImageAssetTypes imageType) {
        this.imageType = imageType;
    }
}
