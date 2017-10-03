package com.pubmatic.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class LogFragment extends Fragment {

    private TextView editText;
    private String logs;

    public void setLogs(String logs) {
        this.logs = logs;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.log_fragment, container, false);

        editText = (TextView) view.findViewById(R.id.edit_text);
        if(TextUtils.isEmpty(logs))
            editText.setText("No logs. Please press 'SHOW AD' button in Home screen to see the logs of the shown ad.");
        else
            editText.setText(logs);


        return view;
    }

}
