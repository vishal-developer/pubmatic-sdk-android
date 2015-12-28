package com.pubmatic.sdk.nativead.bean;

import com.pubmatic.sdk.nativead.PMNativeAd.Image;

public class PMImageAssetResponse extends PMAssetResponse {

    /** Image Asset */
    public Image image;

    /** Image Type (Optional in response) */
    public PMImageAssetTypes imageType;

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
    public PMImageAssetTypes getImageType() {
        return imageType;
    }

    public void setImageType(PMImageAssetTypes imageType) {
        this.imageType = imageType;
    }
}
