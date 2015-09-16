package com.moceanmobile.mast.bean;

/** Image Asset types as per OpenRTB Native standard */
public enum ImageAssetTypes {

	icon(1), logo(2), main(3);

	private int type;

	private ImageAssetTypes(int type) {
		this.type = type;
	}

	public int getTypeId() {
		return this.type;
	}

}
