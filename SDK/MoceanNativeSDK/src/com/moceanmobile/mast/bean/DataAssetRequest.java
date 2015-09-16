package com.moceanmobile.mast.bean;

public class DataAssetRequest extends AssetRequest {

	/** Length for DataAsset */
	public int length;

	/** Data Asset Type */
	public DataAssetTypes dataAssetType = null;

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public DataAssetTypes getDataAssetType() {
		return dataAssetType;
	}

	public void setDataAssetType(DataAssetTypes dataAssetType) {
		this.dataAssetType = dataAssetType;
	}

}
