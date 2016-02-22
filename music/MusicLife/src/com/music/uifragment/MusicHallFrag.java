package com.music.uifragment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.music.base.BaseFlv;
import com.music.bean.BannerItem;
import com.music.bean.MusicHallItem;
import com.music.bean.MusicHallListItem;
import com.music.constant.Constants;
import com.music.constant.Urls;
import com.music.life.app.AppClient;
import com.music.life.app.R;
import com.music.ui.NetMusicList;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MusicHallFrag extends BaseFlv<MusicHallListItem> {

	private SliderLayout mSliderLayout;

	@Override
	protected void firstVisibleToUser() {
		// loadListData(0, mLVHandler, ACTION_INIT);
		refresh();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.layout_music_hall_listview, container, false);
		return mContentView;
	}

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
		mSliderLayout.setLayoutParams(new android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, dp2px(180)));

		((ListView) mContentView.findViewById(R.id.ptr_listview)).addHeaderView(mSliderLayout);
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
						mBaseActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {

								for (BannerItem item : bannerItems) {
									BaseSliderView textSliderView = new TextSliderView(mContext);
									textSliderView.description("").image(Urls.HTTP_HOST + item.getsImg()).setScaleType(BaseSliderView.ScaleType.Fit)
											.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
												@Override
												public void onSliderClick(BaseSliderView slider) {
													String tid = slider.getBundle().getString("extra");
													if (TextUtils.isEmpty(tid))
														return;
													Intent intent = new Intent(mContext, NetMusicList.class);
													intent.putExtra(EK_DEFAULT, tid);
													startActivity(intent);
												}
											});
									textSliderView.getBundle().putString("extra", item.gettId());
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
	public void onListItemClick(MusicHallListItem item, AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public boolean onListItemLongClick(MusicHallListItem item, AdapterView<?> parent, View view, int position, long id) {
		return true;
	}

	@Override
	public List<MusicHallListItem> loadData(Object... params) {
		debug("loadData");
		List<MusicHallListItem> list = new ArrayList<MusicHallListItem>();
		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("appNo", Urls.APP_NO));
			String json = AppClient.getAppClient().http_post(Urls.URL_MUSIC_HALL_LIST, parameters);
			Type type = new TypeToken<List<MusicHallItem>>() {
			}.getType();
			final List<MusicHallItem> musicHallItems = new Gson().fromJson(json, type);
			if (null != musicHallItems) {
				MusicHallItem previousItem = null;
				for (MusicHallItem item : musicHallItems) {
					if (null == previousItem) {
						previousItem = item;
					} else {
						MusicHallListItem listItem = new MusicHallListItem();
						listItem.setItem1(previousItem);
						listItem.setItem2(item);
						list.add(listItem);
						previousItem = null;
					}
				}
				if (null != previousItem) {
					MusicHallListItem listItem = new MusicHallListItem();
					listItem.setItem1(previousItem);
					listItem.setItem2(null);
					list.add(listItem);
					previousItem = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public View getItemView(MusicHallListItem item, int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (null == convertView) {
			convertView = View.inflate(mContext, R.layout.listitem_music_hall, null);
			holder = new ViewHolder(convertView);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (null != item && null != holder) {

			MusicHallItem curItem = null;
			if (null != (curItem = item.getItem1())) {
				holder.ll_item1.setVisibility(View.VISIBLE);
				ImageLoader.getInstance().displayImage(Urls.HTTP_HOST + curItem.gettImg(), holder.iv_image1);
				holder.tv_desc1.setText(curItem.getName());
				final String tid = curItem.getId();
				holder.ll_item1.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext, NetMusicList.class);
						intent.putExtra(EK_DEFAULT, tid);
						startActivity(intent);
					}
				});
			}
			if (null != (curItem = item.getItem2())) {
				holder.ll_item2.setVisibility(View.VISIBLE);
				ImageLoader.getInstance().displayImage(Urls.HTTP_HOST + curItem.gettImg(), holder.iv_image2);
				holder.tv_desc2.setText(curItem.getName());
				final String tid = curItem.getId();
				holder.ll_item2.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext, NetMusicList.class);
						intent.putExtra(EK_DEFAULT, tid);
						startActivity(intent);
					}
				});
			} else {
				holder.ll_item2.setVisibility(View.INVISIBLE);
			}

		}

		return convertView;

	}

	private class ViewHolder {

		public ImageView iv_image1;
		public ImageView iv_image2;
		public TextView tv_desc1;
		public TextView tv_desc2;
		public LinearLayout ll_item1;
		public LinearLayout ll_item2;

		public ViewHolder(View view) {

			iv_image1 = (ImageView) view.findViewById(R.id.iv_image1);
			iv_image2 = (ImageView) view.findViewById(R.id.iv_image2);
			tv_desc1 = (TextView) view.findViewById(R.id.tv_desc1);
			tv_desc2 = (TextView) view.findViewById(R.id.tv_desc2);
			ll_item1 = (LinearLayout) view.findViewById(R.id.ll_item1);
			ll_item2 = (LinearLayout) view.findViewById(R.id.ll_item2);

			view.setTag(this);
		}

	}

}
