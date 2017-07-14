/*
 * PubMatic Inc. (�PubMatic�) CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2014 PubMatic, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained
 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed 
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes  
 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, 
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE 
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS  
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.                
 */

package com.pubmatic.sampleapp;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pubmatic.sampleapp.banner.BannerSamplesListActivity;
import com.pubmatic.sampleapp.interstitial.InterstitialSamplesListActivity;
import com.pubmatic.sampleapp.nativead.NativeSamplesListActivity;

public class SamplesListActivity extends ListActivity {
	SamplesListAdapter samplesListAdapter = null;

	private static final int MULTIPLE_PERMISSIONS_REQUEST_CODE = 12355;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Use to check for potential leaks
		// android.os.StrictMode.setVmPolicy(new
		// android.os.StrictMode.VmPolicy.Builder().detectActivityLeaks().penaltyLog().build());

		samplesListAdapter = new SamplesListAdapter();

		// @formatter:off
		samplesListAdapter.addItem(new SamplesItem("Banner Demo", BannerSamplesListActivity.class));
        samplesListAdapter.addItem(new SamplesItem("Interstitial Demo", InterstitialSamplesListActivity.class));
		samplesListAdapter.addItem(new SamplesItem("Native Demo", NativeSamplesListActivity.class));

		super.setListAdapter(samplesListAdapter);

        int LocationPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(LocationPermissionCheck != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE }, MULTIPLE_PERMISSIONS_REQUEST_CODE);
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		SamplesItem item = (SamplesItem) listView.getAdapter()
				.getItem(position);

		Intent intent = new Intent(this, item.activity);
		startActivity(intent);
	}

	private class SamplesItem {
		public final String name;
		public final String header;
		public final Class<?> activity;

		public SamplesItem(String header, String name, Class<?> activity) {
			this.name = name;
			this.header = header;
			this.activity = activity;
		}

		public SamplesItem(String name, Class<?> activity) {
			this.name = name;
			this.header = null;
			this.activity = activity;
		}
	}

	private class SamplesListAdapter extends BaseAdapter {
		private List<SamplesItem> items = new ArrayList<SamplesItem>();

		public void addItem(SamplesItem item) {
			items.add(item);
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(getBaseContext()).inflate(
					R.layout.samples_list_item, parent, false);

			SamplesItem item = (SamplesItem) getItem(position);

			TextView headerView = (TextView) view.findViewById(R.id.header);
			if (TextUtils.isEmpty(item.header) == false) {
				headerView.setVisibility(View.VISIBLE);
				headerView.setText(item.header);
			}

			TextView nameView = (TextView) view.findViewById(R.id.name);
			nameView.setText(item.name);

			return view;
		}
	}

}
