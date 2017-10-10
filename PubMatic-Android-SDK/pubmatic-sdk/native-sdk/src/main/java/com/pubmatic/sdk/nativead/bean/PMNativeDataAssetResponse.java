package com.pubmatic.sdk.nativead.bean;

public class PMNativeDataAssetResponse extends PMNativeAssetResponse {

	/** Data Asset Type */
	public PMNativeDataAssetTypes dataAssetType = null;

	/** Value of data asset received */
	public String value;

	public PMNativeDataAssetTypes getDataAssetType() {
		return dataAssetType;
	}

	public void setDataAssetType(PMNativeDataAssetTypes dataAssetType) {
		this.dataAssetType = dataAssetType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
