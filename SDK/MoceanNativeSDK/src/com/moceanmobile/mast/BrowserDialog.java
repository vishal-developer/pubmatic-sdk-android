/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2014
 * PubMatic, All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property of
 * PubMatic. The intellectual and technical concepts contained herein are
 * proprietary to PubMatic and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from PubMatic.
 * Access to the source code contained herein is hereby forbidden to anyone
 * except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such
 * access.
 * 
 * The copyright notice above does not evidence any actual or intended
 * publication or disclosure of this source code, which includes information
 * that is confidential and/or proprietary, and is a trade secret, of PubMatic.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC
 * DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN
 * CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE
 * CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO
 * REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR
 * SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.moceanmobile.mast;

import java.net.URI;
import java.net.URISyntaxException;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.SslError;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class BrowserDialog extends Dialog {
    static private final int ActionBarHeightDp = 40;

    private final Handler handler;
    private String url = null;
    private ImageView backButton = null;
    private ImageView forwardButton = null;
    private android.webkit.WebView webView = null;
    RelativeLayout mContentView;

    @SuppressLint("ClickableViewAccessibility")
	public BrowserDialog(Context context, String url, Handler handler) {
        super(context, android.R.style.Theme_Black_NoTitleBar);

        this.url = url;
        this.handler = handler;

        Resources resources = getContext().getResources();

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mContentView = new RelativeLayout(getContext());
        mContentView.setBackgroundColor(0xffffffff);
        setContentView(mContentView, layoutParams);

        RelativeLayout.LayoutParams actionBarLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                dpToPx(ActionBarHeightDp));
        actionBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        LinearLayout actionBar = new LinearLayout(getContext());
        actionBar.setId(100);

        actionBar.setBackgroundColor(0xFF1A1A1A);

        actionBar.setOrientation(LinearLayout.HORIZONTAL);
        actionBar.setVerticalGravity(Gravity.CENTER_VERTICAL);
        mContentView.addView(actionBar, actionBarLayoutParams);

        @SuppressWarnings("static-access")
        LinearLayout.LayoutParams imageButtonLayout = new LinearLayout.LayoutParams(layoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, 1);
        imageButtonLayout.setMargins(2,4,2,2);
        ScaleType imageScaleType = ScaleType.FIT_CENTER;

        ImageView imageButton = new ImageView(getContext());
        imageButton.setScaleType(imageScaleType);
        imageButton.setImageDrawable(new BitmapDrawable(resources, BrowserDialog.class
                .getResourceAsStream("/ic_action_cancel.png")));
        imageButton.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_dark));
        imageButton.setOnTouchListener(mButtonTouchListener);
        imageButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowserDialog.this.dismiss();
            }
        });
        actionBar.addView(imageButton, imageButtonLayout);

        backButton = new ImageView(getContext());
        backButton.setImageDrawable(new BitmapDrawable(resources, BrowserDialog.class
                .getResourceAsStream("/ic_action_back.png")));
        backButton.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_dark));
        backButton.setScaleType(imageScaleType);
        backButton.setEnabled(false);
        imageButton.setOnTouchListener(mButtonTouchListener);
        backButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goBack();
            }
        });
        actionBar.addView(backButton, imageButtonLayout);

        forwardButton = new ImageView(getContext());
        forwardButton.setImageDrawable(new BitmapDrawable(resources, BrowserDialog.class
                .getResourceAsStream("/ic_action_forward.png")));
        forwardButton.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_dark));
        forwardButton.setScaleType(imageScaleType);
        forwardButton.setEnabled(false);
        imageButton.setOnTouchListener(mButtonTouchListener);
        forwardButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goForward();
            }
        });
        actionBar.addView(forwardButton, imageButtonLayout);

        imageButton = new ImageView(getContext());
        imageButton.setScaleType(imageScaleType);
        imageButton.setImageDrawable(new BitmapDrawable(resources, BrowserDialog.class
                .getResourceAsStream("/ic_action_refresh.png")));
        imageButton.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_dark));
        imageButton.setOnTouchListener(mButtonTouchListener);
        imageButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });
        actionBar.addView(imageButton, imageButtonLayout);

        imageButton = new ImageView(getContext());
        imageButton.setScaleType(imageScaleType);
        imageButton.setImageDrawable(new BitmapDrawable(resources, BrowserDialog.class
                .getResourceAsStream("/ic_action_web_site.png")));
        imageButton.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_dark));
        imageButton.setOnTouchListener(mButtonTouchListener);
        imageButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowserDialog.this.handler.browserDialogOpenUrl(BrowserDialog.this, webView.getUrl(), true);
            }
        });
        actionBar.addView(imageButton, imageButtonLayout);

        RelativeLayout.LayoutParams webViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
        webViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        webViewLayoutParams.addRule(RelativeLayout.ABOVE, actionBar.getId());
        webView = new android.webkit.WebView(getContext());
        webView.setWebViewClient(new Client());
        // To set normal zooming level of webView.
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        mContentView.addView(webView, webViewLayoutParams);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                BrowserDialog.this.handler.browserDialogDismissed(BrowserDialog.this);
            }
        });
    }
    
	private View.OnTouchListener mButtonTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.setBackgroundColor(getContext().getResources().getColor(
						android.R.color.background_light));
				break;
			default:
				v.setBackgroundColor(getContext().getResources().getColor(
						android.R.color.background_dark));
				break;
			}
			return false;
		}
	};

	public int pxToDp(float px)
	{
		DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
		int dp = (int) (px / displayMetrics.density + .5f);
		return dp;
	}
	
	public int dpToPx(int dp)
	{
		DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
		int px = (int) (dp * displayMetrics.density + .5f);
		return px;
	}
	
    public void loadUrl(String url) {
        this.url = url;

        webView.stopLoading();
        webView.clearHistory();
        webView.loadUrl(url);
    }

    @Override
    protected void onStart() {
        super.onStart();

        webView.loadUrl(url);
    }

    private class Client extends WebViewClient {
		ProgressBar progressBar = new ProgressBar(getContext(), null,
				android.R.attr.progressBarStyle);

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (progressBar.getParent() == null) {
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
				mContentView.addView(progressBar, layoutParams);
			}
			progressBar.setVisibility(View.VISIBLE);
			super.onPageStarted(view, url, favicon);
		}
    	
    	@Override
    	public void onReceivedSslError(WebView view, SslErrorHandler handler,
    			SslError error) {
    		// Ignore SSL error in InApp Browser
    		handler.proceed();
    	}
    	
    	@Override
    	public void onReceivedError(WebView view, int errorCode,
    			String description, String failingUrl) {
    		progressBar.setVisibility(View.GONE);
    		super.onReceivedError(view, errorCode, description, failingUrl);
    	}
    	
        @Override
        public void onPageFinished(WebView view, String url) {
            backButton.setEnabled(view.canGoBack());
            forwardButton.setEnabled(view.canGoForward());
            progressBar.setVisibility(View.GONE);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                URI uri = new URI(url);
                String scheme = uri.getScheme().toLowerCase();
                if (scheme.startsWith("http")) {
                    return false;
                }
            } catch (URISyntaxException e) {
                // If it can't be parsed, don't try it.
            }

            BrowserDialog.this.handler.browserDialogOpenUrl(BrowserDialog.this, url, false);
            return true;
        }
    }

    public interface Handler {
        public void browserDialogDismissed(BrowserDialog browserDialog);

        public void browserDialogOpenUrl(BrowserDialog browserDialog, String url, boolean dismiss);
    }
}
