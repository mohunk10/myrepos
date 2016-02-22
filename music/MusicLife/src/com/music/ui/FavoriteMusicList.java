package com.music.ui;

import com.cmsc.cmmusic.common.data.MusicInfo;
import com.music.base.BaseActivity;
import com.music.base.BaseFl;
import com.music.bean.MusicInfoSer;
import com.music.life.app.R;
import com.music.list.CoverList;
import com.music.service.MediaBinder;
import com.music.service.MediaBinder.OnPlayerListener;
import com.music.service.MediaService;
import com.music.uifragment.FavoriteMusicFrag;
import com.music.utilities.FormatUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FavoriteMusicList extends BaseActivity {
	public static final String PK_TID = "pk_tid";

	private BaseFl<MusicInfoSer> mFragment;

	public MediaBinder mBinder;
	public ServiceConnection mServiceConnection;
	public Intent mPlayerServiceIntent;

	private ImageView iv_player_control_album;
	private TextView tv_player_control_artist;
	private TextView tv_player_control_name;
	private TextView tv_player_control_time;
	private TextView tv_player_control_previous;
	private TextView tv_player_control_play;
	private TextView tv_player_control_next;
	private LinearLayout ll_player_control;

	private MusicInfoSer mCurrentMusicInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net_music_list);
		this.initTitleBar();
		this.initView();
		this.init();
		this.initPlayerControl();
		this.initServiceConnection();
	}

	private void init() {
		String tid = this.getIntent().getStringExtra(EK_DEFAULT);
		mPublicParams.put(PK_TID, TextUtils.isEmpty(tid) ? "" : tid);
	}

	@Override
	protected void onResume() {
		super.onResume();
		bindService(mPlayerServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (null != mServiceConnection)
			unbindService(mServiceConnection);
	}

	/*
	 * 初始化服务绑定
	 */
	private void initServiceConnection() {
		mPlayerServiceIntent = new Intent(getApplicationContext(), MediaService.class);
		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				mBinder = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mBinder = (MediaBinder) service;
				mCurrentMusicInfo = mBinder.getPlayingMusicInfo();
				if (mBinder != null) {
					mBinder.setOnPlayerListener(new OnPlayerListener() {
						@Override
						public void onStart(MusicInfoSer info) {
							mCurrentMusicInfo = info;
							tv_player_control_play.setBackgroundResource(R.drawable.player_control_btn_pause);
							viewPlayerControlMusicInfo();
						}

						@Override
						public void onPlay(int currentPosition) {
							if (null == mCurrentMusicInfo)
								return;
							String mp3Current = FormatUtil.formatTime(currentPosition);
//							tv_player_control_time.setText(mp3Current + " - " + mCurrentMusicInfo.getTime());
							tv_player_control_play.setBackgroundResource(R.drawable.player_control_btn_pause);
						}

						@Override
						public void onPause() {
							tv_player_control_play.setBackgroundResource(R.drawable.player_control_btn_play);
						}

						@Override
						public void onPlayError() {
							mCurrentMusicInfo = null;
						}

						@Override
						public void onPlayComplete() {
						}

						@Override
						public void onModeChange(int mode) {
						}
					});
					mBinder.setLyricView(null, true);// 无歌词视图

					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							viewPlayerControlMusicInfo();
						}
					}, 100);

				}
			}
		};
	}

	private void viewPlayerControlMusicInfo() {
		if (null == mCurrentMusicInfo)
			return;

		tv_player_control_artist.setText(mCurrentMusicInfo.getArtist());
		tv_player_control_name.setText(mCurrentMusicInfo.getName());
//		tv_player_control_time.setText("00:00" + " - " + mCurrentMusicInfo.getTime());
		if (null == CoverList.cover) {
			iv_player_control_album.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.player_control_album));
		} else {
			iv_player_control_album.setImageBitmap(CoverList.cover);
		}
		tv_player_control_play.setBackgroundResource(R.drawable.player_control_btn_play);
	}

	private void clearPlayerControlMusicInfo() {
		tv_player_control_artist.setText("");
		tv_player_control_name.setText("");
		tv_player_control_time.setText("00:00 - 00:00");
		iv_player_control_album.setImageBitmap(null);
		tv_player_control_play.setBackgroundResource(R.drawable.player_control_btn_play);
	}

	private void initPlayerControl() {
		iv_player_control_album = (ImageView) findViewById(R.id.iv_player_control_album);
		tv_player_control_artist = (TextView) findViewById(R.id.tv_player_control_artist);
		tv_player_control_name = (TextView) findViewById(R.id.tv_player_control_name);
		tv_player_control_time = (TextView) findViewById(R.id.tv_player_control_time);
		tv_player_control_previous = (TextView) findViewById(R.id.tv_player_control_previous);
		tv_player_control_play = (TextView) findViewById(R.id.tv_player_control_play);
		tv_player_control_next = (TextView) findViewById(R.id.tv_player_control_next);
		ll_player_control = (LinearLayout) findViewById(R.id.ll_player_control);

		ll_player_control.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, Player.class);
				startActivity(intent);
			}
		});
		tv_player_control_previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mBinder) {
					clearPlayerControlMusicInfo();
					mBinder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
				}
			}
		});
		tv_player_control_play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mBinder) {
					mBinder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
				}
			}
		});
		tv_player_control_next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mBinder) {
					clearPlayerControlMusicInfo();
					mBinder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
				}
			}
		});
	}

	private void initView() {
		mFragment = new FavoriteMusicFrag();
		getSupportFragmentManager().beginTransaction().add(R.id.activity_fragment, (Fragment) mFragment, "activity_fragment").commit();
	}

	@Override
	protected void initTitleBar() {
		super.initTitleBar();
		tv_titlebar_title.setText("我的收藏");
	}

}
