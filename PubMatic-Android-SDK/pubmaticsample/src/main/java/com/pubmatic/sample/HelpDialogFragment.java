package com.pubmatic.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Sagar on 4/24/2017.
 */

public class HelpDialogFragment extends DialogFragment {

    private AlertDialog.Builder mBuilder;
    private LayoutInflater mInflater;

    public HelpDialogFragment() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final View view;

        mBuilder = new AlertDialog.Builder(getActivity());

        mInflater = getActivity().getLayoutInflater();
        view = mInflater.inflate(R.layout.fragment_help, null);

        mBuilder.setView(view);

        Dialog dialog = mBuilder.create();

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
