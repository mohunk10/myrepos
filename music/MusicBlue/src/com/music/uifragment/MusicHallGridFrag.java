package com.music.uifragment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.music.base.BaseActivity;
import com.music.base.BaseFgv;
import com.music.bean.BannerItem;
import com.music.bean.MusicHallItem;
import com.music.blue.app.AppClient;
import com.music.blue.app.R;
import com.music.constant.Urls;
import com.music.ui.NetMusicList;
import com.music.view.GridViewWithHeaderAndFooter;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicHallGridFrag extends BaseFgv<MusicHallItem> {

	private SliderLayout mSliderLayout;

	@Override
	protected void firstVisibleToUser() {
		// loadListData(0, mLVHandler, ACTION_INIT);
		refresh();
	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// mContentView = inflater.inflate(R.layout.layout_music_hall_listview,
	// container, false);
	// return mContentView;
	// }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		this.initSliderLayout();
		super.onActivityCreated(savedInstanceState);
		this.initView();
	}

	private void initSliderLayout() {
		mSliderLayout = new SliderLayout(getActivity());
		mSliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
		mSliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
		mSliderLayout.setCustomAnimation(new DescriptionAnimation());
		mSliderLayout.setSliderTransformDuration(1100, null);
		mSliderLayout.setDuration(6000);
		mSliderLayout.setLayoutParams(new android.widget.AbsListView.LayoutParams(
				android.widget.AbsListView.LayoutParams.MATCH_PARENT, dp2px(180)));

		((GridViewWithHeaderAndFooter) mContentView.findViewById(R.id.ptr_listview)).addHeaderView(mSliderLayout);
		loadSliderItem();
	}

	private void loadSliderItem() {
		new Thread() {
			public void run() {
				try {
					List<NameValuePair> parameters = new ArrayList<NameValuePair>();
					parameters.add(new BasicNameValuePair("appNo", Urls.APP_NO));

					String json = AppClient.getAppClient().http_post(Urls.URL_BANNER_LIST, parameters);
					Type type = new TypeToken<List<BannerItem>>() {
					}.getType();
					final List<BannerItem> bannerItems = new Gson().fromJson(json, type);
					if (null != bannerItems) {
						final int size = Math.min(bannerItems.size(), chartInfos.length);
						mBaseActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								for (int i = 0; i < size; i++) {
									BannerItem bannerItem = bannerItems.get(i);
									BaseSliderView textSliderView = new TextSliderView(mContext);
									textSliderView.description("").image(Urls.HTTP_HOST + bannerItem.getsImg())
											.setScaleType(BaseSliderView.ScaleType.Fit)
											.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
										@Override
										public void onSliderClick(BaseSliderView slider) {
											String albumId = slider.getBundle().getString(BaseActivity.MUSIC_KEY);
											if (TextUtils.isEmpty(albumId))
												return;
											Intent intent = new Intent(mContext, NetMusicList.class);
											intent.putExtra(BaseActivity.MUSIC_KEY, albumId);
											intent.putExtra(BaseActivity.PARAM_TYPE, "1");
											startActivity(intent);
										}
									});
									textSliderView.getBundle().putString(BaseActivity.MUSIC_KEY, chartInfos[i]);
									mSliderLayout.addSlider(textSliderView);
								}

							}
						});
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	private void initView() {

		View loadingLayout = View.inflate(mContext, R.layout.layout_loading, null);
		setLoadingLayout(loadingLayout);

		View emptyLayout = View.inflate(mContext, R.layout.layout_empty, null);
		setEmptyLayout(emptyLayout);

	}

	@Override
	public void onListItemClick(MusicHallItem item, AdapterView<?> parent, View view, int position, long id) {
		if (null == item)
			return;

		String tid = item.getId();
		Intent intent = new Intent(mContext, NetMusicList.class);
		intent.putExtra(EK_DEFAULT, tid);
		startActivity(intent);
	}

	@Override
	public boolean onListItemLongClick(MusicHallItem item, AdapterView<?> parent, View view, int position, long id) {
		return true;
	}

	@Override
	public List<MusicHallItem> loadData(Object... params) {
		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("appNo", Urls.APP_NO));
			String json = AppClient.getAppClient().http_post(Urls.URL_MUSIC_HALL_LIST, parameters);
			Type type = new TypeToken<List<MusicHallItem>>() {
			}.getType();
			
			final List<MusicHallItem> musicHallItems = new Gson().fromJson(json, type);
			return musicHallItems;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public View getItemView(MusicHallItem item, int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (null == convertView) {
			convertView = View.inflate(mContext, R.layout.griditem_music_hall, null);
			holder = new ViewHolder(convertView);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (null != item && null != holder) {
			ImageLoader.getInstance().displayImage(Urls.HTTP_HOST + item.gettImg(), holder.iv_image);
			final String topName =item.getName();
			holder.tv_desc.setText(topName);
			final String keyword = item.getKeyword();
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, NetMusicList.class);
					intent.putExtra(BaseActivity.MUSIC_KEY,keyword);
					intent.putExtra(BaseActivity.PARAM_TYPE, "2");
					startActivity(intent);
				}
			});
		}

		return convertView;

	}

	private class ViewHolder {

		public ImageView iv_image;
		public TextView tv_desc;

		public ViewHolder(View view) {

			iv_image = (ImageView) view.findViewById(R.id.iv_image);
			tv_desc = (TextView) view.findViewById(R.id.tv_desc);

			view.setTag(this);
		}

	}
	
	private static final String[] chartInfos={"100000006","100000007","100000008","100000009","10000010","100000001","100000002","100000003","100000004"};

}
