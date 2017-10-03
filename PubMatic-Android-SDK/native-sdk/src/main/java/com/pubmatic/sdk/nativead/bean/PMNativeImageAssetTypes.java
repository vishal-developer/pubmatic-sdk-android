package com.pubmatic.sdk.nativead.bean;

/** Image Asset types as per OpenRTB Native standard */
public enum PMNativeImageAssetTypes {

	icon(1), logo(2), main(3);

	private int type;

	private PMNativeImageAssetTypes(int type) {
		this.type = type;
	}

	public int getTypeId() {
		return this.type;
	}

}
