package com.pubmatic.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.pubmatic.sdk.common.PubMaticSDK;

import static com.pubmatic.sample.R.string.help_text;

public class HelpDialogFragment extends DialogFragment {

    private String getHelpText() {
        return "<h2>Welcome to the PubMatic's SDK Sample app.</h2><br/>" +
                "    This application helps you to test PubMatic's ad tag.<br/>" +
                "    <br/>How to use:<br/>" +
                "    1. Please select the desired ad format<br/>" +
                "    2. Input your ad tag details. By default app is configured with test ad tag details. Please contact PubMatic to get new ad tags for your app.<br/>" +
                "    3. Optionally you can configure other targeting parameters for better monetisation.<br/>" +
                "    4. App also provides Settings screen which allows to apply global settings for all platform and ad type.<br/><br/>" +
                "        <h3>Contact:</h3><br/>" +
                "    To monetize your app using PubMatic platform, Please <a href='https://pubmatic.com/contact-us/'>contact us</a>";
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_help, null);

        TextView sdkVersionText  = (TextView)view.findViewById(R.id.sdk_version_text);
        sdkVersionText.setText("PM SDK v"+PubMaticSDK.getSDKVersion());

        WebView webView = (WebView)view.findViewById(R.id.webview);
        webView.loadData(getHelpText(), "text/html", "UTF-8");
        builder.setView(view);

        Dialog dialog = builder.create();
        Drawable drawable = new ColorDrawable(Color.WHITE);
        drawable.setAlpha(240);
        dialog.getWindow().setBackgroundDrawable(drawable);

        return dialog;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

}
