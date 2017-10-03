package com.pubmatic.sdk.nativead.bean;

public class PMNativeTitleAssetRequest extends PMNativeAssetRequest {

	/** Character length of title asset */
	public int length;

	public PMNativeTitleAssetRequest(int assetId) { this.assetId = assetId; }

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

}
