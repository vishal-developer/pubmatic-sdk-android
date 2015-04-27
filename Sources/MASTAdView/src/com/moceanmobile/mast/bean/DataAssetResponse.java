package com.moceanmobile.mast.bean;

public class DataAssetResponse extends AssetResponse {

	/** Data Asset Type */
	public DataAssetTypes dataAssetType = null;

	/** Value of data asset received */
	public String value;

	public DataAssetTypes getDataAssetType() {
		return dataAssetType;
	}

	public void setDataAssetType(DataAssetTypes dataAssetType) {
		this.dataAssetType = dataAssetType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
