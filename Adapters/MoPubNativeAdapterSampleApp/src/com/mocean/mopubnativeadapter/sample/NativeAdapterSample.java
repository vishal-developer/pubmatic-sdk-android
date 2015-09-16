package com.mocean.mopubnativeadapter.sample;

import java.util.EnumSet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mopub.nativeads.MoPubAdAdapter;
import com.mopub.nativeads.MoPubNativeAdPositioning;
import com.mopub.nativeads.MoPubNativeAdRenderer;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.RequestParameters.Builder;
import com.mopub.nativeads.RequestParameters.NativeAdAsset;
import com.mopub.nativeads.ViewBinder;

public class NativeAdapterSample extends Activity {

	private static final String MOPUB_AD_UNIT_ID = "11a17b188668469fb0412708c3d16813";
	// TODO: Remove: MoPub Sample App Id: "11a17b188668469fb0412708c3d16813"
	// Mocean Adapter test zone id: "2fd8cff353f547c189d608e9a14c9f8f"

	private Context mContext = this;
	private ListView mListView;
	private MoPubAdAdapter mAdAdapter;
	private static String[] titleArrary = { "Title 1", "Title 2", "Title 3",
			"Title 4", "Title 5", "Title 6", "Title 7", "Title 8", "Title 9",
			"Title 10" };

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_native_adapter_sample);
		mListView = (ListView) findViewById(R.id.listView);
		mListView.setAdapter(new CustomAdapter(mContext, titleArrary));

		// MoPub SDK
		ViewBinder viewBinder = new ViewBinder.Builder(R.layout.list_item)
				.mainImageId(R.id.mainImage).iconImageId(R.id.logoImage)
				.callToActionId(R.id.ctaButton).titleId(R.id.titleTextView)
				.textId(R.id.descriptionTextView).build();
		// Set up the positioning behavior your ads should have.
		MoPubNativeAdPositioning.MoPubServerPositioning adPositioning = MoPubNativeAdPositioning
				.serverPositioning();
		MoPubNativeAdRenderer adRenderer = new MoPubNativeAdRenderer(viewBinder);

		// Set up the MoPubAdAdapter
		mAdAdapter = new MoPubAdAdapter(this, new CustomAdapter(mContext,
				titleArrary), adPositioning);
		mAdAdapter.registerAdRenderer(adRenderer);

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
