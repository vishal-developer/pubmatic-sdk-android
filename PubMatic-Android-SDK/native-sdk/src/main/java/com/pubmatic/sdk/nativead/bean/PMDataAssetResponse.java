package com.pubmatic.sdk.nativead.bean;

public class PMDataAssetResponse extends PMAssetResponse {

	/** Data Asset Type */
	public PMDataAssetTypes dataAssetType = null;

	/** Value of data asset received */
	public String value;

	public PMDataAssetTypes getDataAssetType() {
		return dataAssetType;
	}

	public void setDataAssetType(PMDataAssetTypes dataAssetType) {
		this.dataAssetType = dataAssetType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
