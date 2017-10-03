package com.pubmatic.sdk.nativead.bean;

/** Data Asset types as per OpenRTB Native standard */
public enum PMNativeDataAssetTypes {

	sponsored(1), desc(2), rating(3), likes(4), downloads(5), price(6), saleprice(
			7), phone(8), address(9), desc2(10), displayurl(11), ctatext(12), OTHER(
			501);

	private int type;

	private PMNativeDataAssetTypes(int type) {
		this.type = type;
	}

	public int getTypeId() {
		return this.type;
	}

}
