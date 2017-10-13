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

import static android.os.Build.ID;
import static com.pubmatic.sample.R.string.help_text;

public class HelpDialogFragment extends DialogFragment {

    private String getHelpText() {
        return "<h2>Welcome to the PubMatic's SDK Sample app.</h2><br/>" +
                "    This application helps you test PubMatic's ad tag.<br/>" +
                "    <br/>How to use:<br/>" +
                "    1. Select the desired ad type.<br/>" +
                "    2. Input your ad tag details below. These can be obtained while creating an ad tag in the PubMatic UI or from the PubMatic Sales team, who can get new ad tags for your app.<br/>" +
                "    &nbsp;* PubID = Publisher ID<br/>"+
                "    &nbsp;* SiteID = Publisher Site ID<br/>"+
                "    &nbsp;* AdID = Ad Slot ID<br/>"+
                "    3. (Optional but Recommended) Configure additional targeting parameters for better monetization.<br/>" +
                "    4. (Optional but Recommended) Use the Settings screen to apply global settings.<br/><br/>" +
                "        <h3>Contact us:</h3><br/>" +
                "    To monetize your app with PubMatic, please contact support@pubmatic.com</a>";
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
