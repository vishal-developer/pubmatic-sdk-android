package com.pubmatic.sdk.nativead.bean;

public class PMImageAssetRequest extends PMAssetRequest {

	/** Image Type */
	public PMImageAssetTypes imageType = null;

	/** Image width */
	public int width;

	/** Image height */
	public int height;

	public PMImageAssetTypes getImageType() {
		return imageType;
	}

	public void setImageType(PMImageAssetTypes imageType) {
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
