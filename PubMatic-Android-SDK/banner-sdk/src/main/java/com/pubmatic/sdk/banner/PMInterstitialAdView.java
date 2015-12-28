package com.pubmatic.sdk.banner;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Map;

/**
 * Created by shrawangupta on 24/12/15.
 */
public class PMInterstitialAdView extends PMBannerAdView implements PMBannerAdView.BannerAdViewDelegate.RequestListener
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

        setRequestListener(this);
    }

    @Override
    public void onFailedToReceiveAd(PMBannerAdView adView, Exception ex)
    {

    }

    @Override
    public void onReceivedAd(PMBannerAdView adView)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                showInterstitial();
            }
        });
    }

    @Override
    public void onReceivedThirdPartyRequest(PMBannerAdView adView,
                                            Map<String, String> properties, Map<String, String> parameters)
    {

    }
}