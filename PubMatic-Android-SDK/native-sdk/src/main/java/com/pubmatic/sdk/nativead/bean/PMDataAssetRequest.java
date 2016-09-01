package com.pubmatic.sdk.nativead.bean;

public class PMDataAssetRequest extends PMAssetRequest {

	/** Length for DataAsset */
	public int length;

	/** Data Asset Type */
	public PMDataAssetTypes dataAssetType = null;

	public PMDataAssetRequest(int assetId) { this.assetId = assetId; }

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public PMDataAssetTypes getDataAssetType() {
		return dataAssetType;
	}

	public void setDataAssetType(PMDataAssetTypes dataAssetType) {
		this.dataAssetType = dataAssetType;
	}

}
