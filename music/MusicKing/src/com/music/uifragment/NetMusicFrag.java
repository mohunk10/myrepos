package com.music.uifragment;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.cmsc.cmmusic.common.MusicQueryInterface;
import com.cmsc.cmmusic.common.data.MusicInfo;
import com.cmsc.cmmusic.common.data.MusicListRsp;
import com.music.base.BaseActivity;
import com.music.base.BaseFlv;
import com.music.bean.MusicInfoSer;
import com.music.king.app.R;
import com.music.service.MediaService;
import com.music.service.PlayingMusic;
import com.music.view.AlertList;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

public class NetMusicFrag extends BaseFlv<MusicInfoSer> {

	private String musickey = "";
	private String paramtype = "";

	@Override
	protected void firstVisibleToUser() {
		// loadListData(0, mLVHandler, ACTION_INIT);
		refresh();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.initView();
		this.init();
	}

	private void init() {
		if (TextUtils.isEmpty(musickey = mPublicParams.get(BaseActivity.MUSIC_KEY))) {
			musickey = "";
		}
		if (TextUtils.isEmpty(paramtype = mPublicParams.get(BaseActivity.PARAM_TYPE))) {
			paramtype = "";
		}
	}

	private void initView() {

		View loadingLayout = View.inflate(mContext, R.layout.layout_loading, null);
		setLoadingLayout(loadingLayout);

		View emptyLayout = View.inflate(mContext, R.layout.layout_empty, null);
		setEmptyLayout(emptyLayout);

	}

	@Override
	public void onListItemClick(MusicInfoSer item, AdapterView<?> parent, View view, int position, long id) {
		if (null == item)
			return;
		PlayingMusic.list.clear();
		PlayingMusic.list.addAll(mLVData);
		Intent intent = new Intent(mContext, MediaService.class);
		intent.putExtra(MediaService.EK_PLAYING_POSITION, mLVData.indexOf(item));
		mContext.startService(intent);
	}

	@Override
	public boolean onListItemLongClick(MusicInfoSer item, AdapterView<?> parent, View view, int position, long id) {
		if (null == item)
			return false;

		List<String> list = new ArrayList<String>();
		list.add("单曲下载");
		list.add("包月下载");
		new AlertList(mContext).setTitleText("歌曲下载").setList(list, item).show();

		return true;
	}

	@Override
	public List<MusicInfoSer> loadData(Object... params) {
		List<MusicInfoSer> musicInfos = new ArrayList<MusicInfoSer>();
		try {
			MusicListRsp m = new MusicListRsp();
			if ("1".equals(paramtype)) {
				m = MusicQueryInterface.getMusicsByChartId(NetMusicFrag.this.getActivity(), musickey, 1, 30);
			} else if ("2".equals(paramtype)) {
				m = MusicQueryInterface.getMusicsByKey(NetMusicFrag.this.getActivity(), URLEncoder.encode(musickey),
						"0", 1, 30);
			}
			for (MusicInfo mInfo : m.getMusics()) {
				MusicInfoSer mInfoSer = new MusicInfoSer();
				mInfoSer.setAlbumPicDir(mInfo.getAlbumPicDir());
				mInfoSer.setCount(mInfo.getCount());
				mInfoSer.setCrbtListenDir(mInfo.getCrbtListenDir());
				mInfoSer.setCrbtValidity(mInfo.getCrbtValidity());
				mInfoSer.setHasDolby(mInfo.getHasDolby());
				mInfoSer.setLrcDir(mInfo.getLrcDir());
				mInfoSer.setMusicId(mInfo.getMusicId());
				mInfoSer.setPrice(mInfo.getPrice());
				mInfoSer.setRingListenDir(mInfo.getRingListenDir());
				mInfoSer.setRingValidity(mInfo.getRingValidity());
				mInfoSer.setSingerId(mInfo.getSingerId());
				mInfoSer.setSingerName(mInfo.getSingerName());
				mInfoSer.setSingerPicDir(mInfo.getSingerPicDir());
				mInfoSer.setSongName(mInfo.getSongName());
				mInfoSer.setSongListenDir(mInfo.getSongListenDir());
				mInfoSer.setSongValidity(mInfo.getSongValidity());
				mInfoSer.setTag("net");
				musicInfos.add(mInfoSer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return musicInfos;
	}

	@Override
	public View getItemView(MusicInfoSer item, int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (null == convertView) {
			convertView = View.inflate(mContext, R.layout.listitem_net_music, null);
			holder = new ViewHolder(convertView);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.ll_operation.setVisibility(View.GONE);

		if (null != item && null != holder) {
			ImageLoader.getInstance().displayImage(item.getSingerPicDir(), holder.iv_singer_img, options);
			holder.tv_name.setText(item.getSongName());
			holder.tv_artist.setText(item.getSingerName());
			holder.ll_download.setTag(item);
			holder.ll_ringtone.setTag(item);
			holder.ll_download.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					notifyDataSetChanged();
					List<String> list = new ArrayList<String>();
					list.add("全曲下载");
					list.add("全曲赠送");
					list.add("3元包月(20首)");
					list.add("5元包月(50首)");
					list.add("10元包月(200首)");
					new AlertList(mContext).setTitleText("歌曲下载").setList(list, (MusicInfoSer) v.getTag()).show();
				}
			});
			holder.ll_ringtone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					notifyDataSetChanged();
					List<String> list = new ArrayList<String>();
					list.add("彩铃订购");
					list.add("彩铃赠送");
					list.add("振铃订购");
					list.add("振铃赠送");
					new AlertList(mContext).setTitleText("彩铃设置").setList(list, (MusicInfoSer) v.getTag()).show();
				}
			});

		}

		return convertView;

	}

	private class ViewHolder {

		ImageView iv_favorite;
		ImageView iv_singer_img;
		TextView tv_name;
		TextView tv_artist;
		ImageView iv_menu;
		View ll_operation;
		View ll_download;
		View ll_ringtone;

		public ViewHolder(View view) {

			iv_favorite = (ImageView) view.findViewById(R.id.iv_favorite);
			iv_singer_img = (ImageView) view.findViewById(R.id.iv_singer_img);
			tv_name = (TextView) view.findViewById(R.id.tv_name);
			tv_artist = (TextView) view.findViewById(R.id.tv_artist);
			iv_menu = (ImageView) view.findViewById(R.id.iv_menu);
			ll_operation = view.findViewById(R.id.ll_operation);
			ll_download = view.findViewById(R.id.ll_download);
			ll_ringtone = view.findViewById(R.id.ll_ringtone);

			iv_favorite.setVisibility(View.GONE);
			ll_operation.setVisibility(View.GONE);

			iv_menu.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					final int visible = ll_operation.getVisibility();
					notifyDataSetChanged();
					v.postDelayed(new Runnable() {
						@Override
						public void run() {
							ll_operation.setVisibility((visible == View.VISIBLE) ? View.GONE : View.VISIBLE);
						}
					}, 10);
				}
			});

			view.setTag(this);
		}

	}

	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.player_control_album).showImageForEmptyUri(R.drawable.player_control_album)
			.showImageOnFail(R.drawable.player_control_album).cacheInMemory(true).cacheOnDisk(true)
			.considerExifParams(true).displayer(new RoundedBitmapDisplayer(20)).build();
}
