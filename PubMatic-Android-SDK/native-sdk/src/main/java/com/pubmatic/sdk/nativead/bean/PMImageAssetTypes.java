package com.pubmatic.sdk.nativead.bean;

/** Image Asset types as per OpenRTB Native standard */
public enum PMImageAssetTypes {

	icon(1), logo(2), main(3);

	private int type;

	private PMImageAssetTypes(int type) {
		this.type = type;
	}

	public int getTypeId() {
		return this.type;
	}

}
