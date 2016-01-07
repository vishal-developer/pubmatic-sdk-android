package com.pubmatic.sdk.banner;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Map;

/**
 * It loads the interstitial ad.
 * In order to show the Interstitial ad, Publisher should call the
 * showInterstitial() or showInterstitialWithDuration()
 */
public class PMInterstitialAdView extends PMBannerAdView
{
    public PMInterstitialAdView(Context context)
    {
        super(context, true);
        init(true);
    }

    public PMInterstitialAdView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        super.applyAttributeSet(attrs);
        init(true);
    }

    public PMInterstitialAdView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        super.applyAttributeSet(attrs);
        init(true);
    }

    @Override
    protected void init(boolean interstitial)
    {
        super.init(interstitial);
    }

}