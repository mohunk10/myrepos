package com.music.ui;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.cmsc.cmmusic.init.InitCmmInterface;
import com.music.base.BaseActivity;
import com.music.bean.MusicInfoSer;
import com.music.life.app.DoubleClickExitHelper;
import com.music.life.app.R;
import com.music.list.CoverList;
import com.music.service.MediaBinder;
import com.music.service.MediaBinder.OnPlayerListener;
import com.music.service.MediaService;
import com.music.uifragment.LocalMusicFrag;
import com.music.uifragment.MusicHallGridFrag;
import com.music.uifragment.MyFrag;
import com.music.utilities.FormatUtil;
import com.music.view.PagerSlidingTabStrip;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends BaseActivity {

	private DoubleClickExitHelper mDoubleClickExitHelper;
	private PagerSlidingTabStrip mTabs;
	private ViewPager mPager;
	private MyPagerAdapter mPagerAdapter;
	private List<PagerItem> mPagers = new ArrayList<PagerItem>();

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

	public void ll_login(View view) {
		Intent intent = new Intent(mContext, Login.class);
		startActivity(intent);
	}

	public void ll_my_music(View view) {
		mPager.setCurrentItem(1);
	}

	public void ll_scan_local_music(View view) {
		Intent intent = new Intent(mContext, ScanMusic.class);
		startActivity(intent);
	}

	public void ll_favorite(View view) {
		Intent intent = new Intent(mContext, FavoriteMusicList.class);
		startActivity(intent);
	}

	public void ll_copyright(View view) {
		Intent intent = new Intent(mContext, CopyRight.class);
		startActivity(intent);
	}

	public void ll_about(View view) {
		Intent intent = new Intent(mContext, About.class);
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.initView();
		this.init();
		this.initPlayerControl();
		this.initServiceConnection();
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
							// tv_player_control_time.setText(mp3Current + " - "
							// + mCurrentMusicInfo.getTime());
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
		String tag = mCurrentMusicInfo.getTag();
		if ("local".equals(tag)) {
			tv_player_control_artist.setText(mCurrentMusicInfo.getArtist());
			tv_player_control_name.setText(mCurrentMusicInfo.getName());
			tv_player_control_time.setText("00:00" + " - " + mCurrentMusicInfo.getTime());
			if (null == CoverList.cover) {
				iv_player_control_album
						.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.player_control_album));
			} else {
				iv_player_control_album.setImageBitmap(CoverList.cover);
			}
		} else if ("net".equals(tag)) {
			tv_player_control_artist.setText(mCurrentMusicInfo.getSingerName());
			tv_player_control_name.setText(mCurrentMusicInfo.getSongName());
			String imgPath = mCurrentMusicInfo.getSingerPicDir();
			if (TextUtils.isEmpty(imgPath)) {
				iv_player_control_album
						.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.player_control_album));
			} else {
				ImageLoader.getInstance().displayImage(imgPath, iv_player_control_album);
			}
		}

		tv_player_control_play.setBackgroundResource(R.drawable.player_control_btn_play);
	}

	private void clearPlayerControlMusicInfo() {
		tv_player_control_artist.setText("");
		tv_player_control_name.setText("");
		// tv_player_control_time.setText("00:00 - 00:00");
		iv_player_control_album.setImageBitmap(null);
		tv_player_control_play.setBackgroundResource(R.drawable.player_control_btn_play);
	}

	private void init() {
		InitCmmInterface.initSDK(this);
		if (!InitCmmInterface.initCheck(Main.this)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					final Hashtable<String, String> hb = InitCmmInterface.initCmmEnv(Main.this);
					final String val = hb.get("code");
					if (!"0".equals(val)) {
						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(Main.this, hb.get("desc"), Toast.LENGTH_LONG).show();
							}
						});
					}

				}
			}).start();
		}

		mDoubleClickExitHelper = new DoubleClickExitHelper(this);

		mPagers.add(new PagerItem("音乐馆", new MusicHallGridFrag()));
		mPagers.add(new PagerItem("本地畅听", new LocalMusicFrag()));
		mPagers.add(new PagerItem("我的", new MyFrag()));

		mPager.setOffscreenPageLimit(mPagers.size());
		mPagerAdapter.notifyDataSetChanged();
		mTabs.notifyDataSetChanged();
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
		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mPager = (ViewPager) findViewById(R.id.pager);

		mTabs.setShouldExpand(true);
		mTabs.setTabPaddingLeftRight(dp2px(5));
		mTabs.setTextColor(Color.WHITE);
		mTabs.setTextSize(dp2px(18));
		mTabs.setIndicatorColor(Color.WHITE);
		mTabs.setIndicatorHeight(dp2px(3));
		mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mTabs.setViewPager(mPager);
	}

	private class MyPagerAdapter extends FragmentStatePagerAdapter {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			try {
				return mPagers.get(position).getTitle();
			} catch (IndexOutOfBoundsException e) {
			}
			return "";
		}

		@Override
		public Fragment getItem(int position) {
			try {
				return mPagers.get(position).getFragment();
			} catch (IndexOutOfBoundsException e) {
			}
			return null;
		}

		@Override
		public int getCount() {
			return mPagers.size();
		}

	}

	private class PagerItem {

		private String title;
		private Fragment fragment;

		private PagerItem() {
		}

		public PagerItem(String title, Fragment fragment) {
			this.title = title;
			this.fragment = fragment;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public Fragment getFragment() {
			return fragment;
		}

		public void setFragment(Fragment fragment) {
			this.fragment = fragment;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag = true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return mDoubleClickExitHelper.onKeyDown(keyCode, event);
		} else {
			flag = super.onKeyDown(keyCode, event);
		}
		return flag;
	}

}
