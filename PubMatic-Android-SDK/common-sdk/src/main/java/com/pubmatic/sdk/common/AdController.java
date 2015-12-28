package com.pubmatic.sdk.common;

import android.util.AttributeSet;

public interface AdController {

	public AdRequest getAdRequest();
	public void setAdRequest(AdRequest adRequest);
	public RRFormatter getRRFormatter();
	public boolean checkMandatoryParams();	
	public void applyAttributeSet(AttributeSet attrs);
}
