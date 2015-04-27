package com.moceanmobile.mast.bean;

public class ImageAssetRequest extends AssetRequest {

	/** Image Type */
	public ImageAssetTypes imageType = null;

	/** Image width */
	public int width;

	/** Image height */
	public int height;

	public ImageAssetTypes getImageType() {
		return imageType;
	}

	public void setImageType(ImageAssetTypes imageType) {
		this.imageType = imageType;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
