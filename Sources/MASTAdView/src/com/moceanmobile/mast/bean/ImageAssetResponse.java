package com.moceanmobile.mast.bean;

import com.moceanmobile.mast.MASTNativeAd.Image;

public class ImageAssetResponse extends AssetResponse {

    /** Image Asset */
    public Image image;

    /** Image Type (Optional in response) */
    public ImageAssetTypes imageType;

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
    public ImageAssetTypes getImageType() {
        return imageType;
    }

    public void setImageType(ImageAssetTypes imageType) {
        this.imageType = imageType;
    }
}
