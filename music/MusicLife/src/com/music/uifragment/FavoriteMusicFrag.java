package com.music.uifragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.music.base.BaseFlv;
import com.music.bean.MusicInfoSer;
import com.music.db.DBDao;
import com.music.life.app.R;
import com.music.service.MediaService;
import com.music.service.PlayingMusic;
import com.music.utilities.AlbumUtil;
import com.music.utilities.ScanUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

public class FavoriteMusicFrag extends BaseFlv<MusicInfoSer> {
	public static final String MK_REFRESH = "mk_refresh";
	AlbumUtil mAlbumUtil;
	@Override
	protected void firstVisibleToUser() {
		 loadListData(0, mLVHandler, ACTION_INIT);
		 refresh();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAlbumUtil = new AlbumUtil();
		this.initView();
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
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
		return true;
	}

	@Override
	public List<MusicInfoSer> loadData(Object... params) {
		debug("loadData");

		List<MusicInfoSer> list = new ArrayList<MusicInfoSer>();
		ScanUtil scanUtil = new ScanUtil(mContext);
		List<MusicInfoSer> all = scanUtil.scanMusicInfoFromDB();
		if (null != all)
			for (MusicInfoSer info : all) {
				if (info.isFavorite())
					list.add(info);
			}
		 Collections.sort(list, new MusicInfoSer());
		return list;
	}

	@Override
	public View getItemView(final MusicInfoSer item, int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (null == convertView) {
			convertView = View.inflate(mContext, R.layout.listitem_local_music, null);
			holder = new ViewHolder(convertView);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (null != item && null != holder) {
			Bitmap bitmap = mAlbumUtil.scanAlbumImage(item.getPath());
			holder.iv_image.setImageResource(R.drawable.player_control_album);
			if(bitmap!=null){
				holder.iv_image.setImageBitmap(bitmap);
			}
			holder.tv_name.setText(item.getName());
			holder.tv_artist.setText(item.getArtist());
			holder.tv_time.setText(item.getTime());
			holder.iv_favorite.setImageResource(item.isFavorite() ? R.drawable.music_item_btn_favourite_pressed : R.drawable.music_item_btn_favourite_normal);

			final ImageView favorite = holder.iv_favorite;
			favorite.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					item.setFavorite(!item.isFavorite());
					DBDao db = new DBDao(mContext);
					db.update(item.getName(), item.isFavorite());
					favorite.setImageResource(item.isFavorite() ? R.drawable.music_item_btn_favourite_pressed : R.drawable.music_item_btn_favourite_normal);
				}
			});

		}

		return convertView;

	}

	private class ViewHolder {
		ImageView iv_image;
		ImageView iv_favorite;
		TextView tv_name;
		TextView tv_artist;
		TextView tv_time;
		ImageView iv_menu;

		public ViewHolder(View view) {
			iv_image = (ImageView) view.findViewById(R.id.iv_image);
			iv_favorite = (ImageView) view.findViewById(R.id.iv_favorite);
			tv_name = (TextView) view.findViewById(R.id.tv_name);
			tv_artist = (TextView) view.findViewById(R.id.tv_artist);
			tv_time = (TextView) view.findViewById(R.id.tv_time);
			iv_menu = (ImageView) view.findViewById(R.id.iv_menu);

			iv_favorite.setVisibility(View.GONE);
			iv_menu.setVisibility(View.GONE);

			view.setTag(this);
		}

	}
}
