package com.pubmatic.sdk.nativead.bean;

public class PMNativeDataAssetRequest extends PMNativeAssetRequest {

	/** Length for DataAsset */
	public int length;

	/** Data Asset Type */
	public PMNativeDataAssetTypes dataAssetType = null;

	public PMNativeDataAssetRequest(int assetId) { this.assetId = assetId; }

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public PMNativeDataAssetTypes getDataAssetType() {
		return dataAssetType;
	}

	public void setDataAssetType(PMNativeDataAssetTypes dataAssetType) {
		this.dataAssetType = dataAssetType;
	}

}
