package com.mocean.mopubnativeadapter.sample;

import java.util.EnumSet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mopub.common.MoPub;
import com.mopub.nativeads.MoPubAdAdapter;
import com.mopub.nativeads.MoPubNativeAdLoadedListener;
import com.mopub.nativeads.MoPubNativeAdPositioning;
import com.mopub.nativeads.MoPubNativeAdRenderer;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.RequestParameters.Builder;
import com.mopub.nativeads.RequestParameters.NativeAdAsset;
import com.mopub.nativeads.ViewBinder;

public class NativeAdapterSample extends Activity {

	private static final String MOPUB_AD_UNIT_ID = "7fde290feefe477795b4ef58c1a99887"; // TODO: Add MoPub AdUnitId here

	private Context mContext = this;
	private ListView mListView;
	private MoPubAdAdapter mAdAdapter;
	private static String[] listItemPlaceholders = { "List item 1",
			"List item 2", "List item 3", "List item 4", "List item 5" };
	private static final String TAG = NativeAdapterSample.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_native_adapter_sample);

		mListView = (ListView) findViewById(R.id.listView);

		// MoPub SDK Initialization
		ViewBinder viewBinder = new ViewBinder.Builder(R.layout.list_item)
				.mainImageId(R.id.mainImage).iconImageId(R.id.logoImage)
				.callToActionId(R.id.ctaButton).titleId(R.id.titleTextView)
				.textId(R.id.descriptionTextView).build();

		// Set up the positioning behavior your ads should have.
		MoPubNativeAdPositioning.MoPubServerPositioning adPositioning = MoPubNativeAdPositioning
				.serverPositioning();

		// Set up the MoPubAdAdapter
		mAdAdapter = new MoPubAdAdapter(this, new CustomAdapter(mContext,
				listItemPlaceholders), adPositioning);
		
		MoPubNativeAdRenderer adRenderer = new MoPubNativeAdRenderer(viewBinder);
		mAdAdapter.registerAdRenderer(adRenderer);
		
		// Enable Location awareness
		MoPub.setLocationAwareness(MoPub.LocationAwareness.NORMAL);

		// Set Native ad listener
		mAdAdapter.setAdLoadedListener(new MoPubNativeAdLoadedListener() {

			@Override
			public void onAdRemoved(int position) {
				Log.i(TAG, "onAdRemoved. List position:" + position);
			}

			@Override
			public void onAdLoaded(int position) {
				Log.i(TAG, "onAdLoaded. List position:" + position);
			}
		});

		mListView.setAdapter(mAdAdapter);
	}

	final EnumSet<NativeAdAsset> desiredAssets = EnumSet.of(
			NativeAdAsset.TITLE, NativeAdAsset.TEXT, NativeAdAsset.ICON_IMAGE,
			NativeAdAsset.MAIN_IMAGE, NativeAdAsset.CALL_TO_ACTION_TEXT,
			NativeAdAsset.STAR_RATING);

	@Override
	protected void onResume() {
		// Set up your request parameters
		Builder builder = new Builder();
		builder.desiredAssets(desiredAssets);
		RequestParameters adRequestParameters = builder.build();

		// Request ads when the user returns to this activity.
		mAdAdapter.loadAds(MOPUB_AD_UNIT_ID, adRequestParameters);

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mAdAdapter.destroy();
		super.onDestroy();
	}

	class CustomAdapter extends ArrayAdapter<String> {
		private final Context mContext;
		private final String[] values;

		public CustomAdapter(Context context, String[] objects) {
			super(context, android.R.layout.simple_list_item_1, objects);
			this.mContext = context;
			this.values = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) mContext)
						.getLayoutInflater();
				convertView = inflater.inflate(
						android.R.layout.simple_list_item_1, parent, false);
				holder = new ViewHolder();
				holder.text = (TextView) convertView
						.findViewById(android.R.id.text1);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String title = values[position];
			if (title != null) {
				holder.text.setText(title);
			}
			return convertView;
		}

		class ViewHolder {
			TextView text;

		}
	}
}
