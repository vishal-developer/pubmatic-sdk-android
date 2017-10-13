package com.pubmatic.sdk.nativead.bean;

import java.util.ArrayList;
import java.util.List;

public class PMNativeImageAssetRequest extends PMNativeAssetRequest {

	/** Image Type */
	public PMNativeImageAssetTypes imageType = null;

	/** Image width */
	public int width;

	/** Image height */
	public int height;

	/** Image mime types */
	private List<String> mimeTypes;

	public PMNativeImageAssetRequest(int assetId) {
		this.assetId = assetId;
		this.mimeTypes = new ArrayList<>(0);
	}

	public PMNativeImageAssetTypes getImageType() {
		return imageType;
	}

	public void setImageType(PMNativeImageAssetTypes imageType) {
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

	public void setMimeTypes(List<String> mimeTypes) { this.mimeTypes = mimeTypes; }

	public List<String> getMimeTypes() { return this.mimeTypes; }
}
