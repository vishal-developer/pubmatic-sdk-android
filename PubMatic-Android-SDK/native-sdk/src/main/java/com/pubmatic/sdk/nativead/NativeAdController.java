package com.pubmatic.sdk.nativead;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.AdController;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.utils.CommonConstants.CHANNEL;


public class NativeAdController implements AdController {

	protected CHANNEL			mChannel;
	protected Context 			mContext 		= null;
	protected NativeAdRequest 	mAdRequest 		= null;
	protected RRFormatter 		mRRFormatter 	= null;
	
	
	public NativeAdController(CHANNEL channel, Context cnt) {
		mChannel = channel;
		mContext = cnt;
		createDefaultAdRequest();
	}

	public NativeAdController(CHANNEL channel, Context cnt, AdRequest adRequest) {
		mChannel = channel;
		mContext = cnt;
		if(adRequest==null)
			createDefaultAdRequest();
		else
			setAdRequest(adRequest);
	}
	
	@Override
	public AdRequest getAdRequest() {
		return mAdRequest;
	}

	//TODO :: Need to verify this method
	private void createDefaultAdRequest() {

		String adRequestName = null;
		Class<?>  className     = null;
		Method m			 = null;
		try {
			

			switch (mChannel) {
				case MOCEAN:
					adRequestName = "com.pubmatic.sdk.nativead.mocean.MoceanNativeAdRequest";
					className = Class.forName(adRequestName);
					m = className.getMethod("createMoceanNativeAdRequest", Context.class, String.class);
					mAdRequest = (NativeAdRequest)m.invoke(null, mContext, null);
					break;
				case PUBMATIC:
					adRequestName = "com.pubmatic.sdk.nativead.pubmatic.PubMaticNativeAdRequest";
					className = Class.forName(adRequestName);
					m = className.getMethod("createPubMaticNativeAdRequest", 
							Context.class, String.class, String.class, String.class);
					mAdRequest = (NativeAdRequest)m.invoke(null, mContext, null, null, null);
					break;
		
				default:
					break;
			}
			
			createRRFormatter();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassCastException ex) {
			
		}
	}
	
	@Override
	public void setAdRequest(AdRequest adRequest) {
		
		if (adRequest == null)
			throw new IllegalArgumentException("AdRequest object is null");

		if (adRequest instanceof NativeAdRequest) {
			((NativeAdRequest)adRequest).copyRequestParams(mAdRequest);
			mAdRequest = (NativeAdRequest)adRequest;
		} else
			throw new IllegalStateException(
					"AdRequest and channel type do not match.");
		
		//Create RRFormater
		createRRFormatter();
	}

	private void createRRFormatter() {
		if(mAdRequest != null)
		{
			//Create RRFormater
			String rrFormaterName = mAdRequest.getFormatter();
			
			try {
				Class<?> className = Class.forName(rrFormaterName);
				mRRFormatter = (RRFormatter) className.newInstance();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (ClassCastException ex) {
				
			}
		}
	}
	
	@Override
	public RRFormatter getRRFormatter() {
		return mRRFormatter;
	}

	@Override
	public boolean checkMandatoryParams() {
		return mAdRequest==null ? false : mAdRequest.checkMandatoryParams();
	}

	@Override
	public void applyAttributeSet(AttributeSet attrs) {
		// TODO Auto-generated method stub
		
	}
}
