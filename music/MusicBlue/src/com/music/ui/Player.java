package com.music.ui;

import java.util.ArrayList;

import com.cmsc.cmmusic.common.data.MusicInfo;
import com.music.base.BaseActivity;
import com.music.bean.MusicInfoSer;
import com.music.blue.app.R;
import com.music.db.DBDao;
import com.music.list.CoverList;
import com.music.lyric.LyricView;
import com.music.service.MediaBinder;
import com.music.service.MediaBinder.OnPlayerListener;
import com.music.service.MediaService;
import com.music.utilities.FormatUtil;
import com.music.view.PushView;
import com.music.view.VisualizerView;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class Player extends BaseActivity {

	private final int[] modeImage = { R.drawable.player_btn_mode_normal_style,
			R.drawable.player_btn_mode_repeat_one_style, R.drawable.player_btn_mode_repeat_all_style,
			R.drawable.player_btn_mode_random_style };

	private TextView currentTime;// 当前时间
	private TextView totalTime;// 总时间
	private SeekBar seekBar;// 进度条
	private PushView mp3Name;// 歌名
	private PushView mp3Info;// 歌曲信息集合
	private PushView mp3Artist;// 艺术家
	private ImageView mp3Cover;// 专辑图片
	private ImageView mp3Favorite;// 我的最爱动画图片
	private LyricView lyricView;// 歌词视图
	private VisualizerView visualizer;// 均衡器视图

	private ImageButton btnMode;// 播放模式按钮
	private ImageButton btnReturn;// 返回按钮
	private ImageButton btnPrevious;// 上一首按钮
	private ImageButton btnPlay;// 播放和暂停按钮
	private ImageButton btnNext;// 下一首按钮
	private ImageButton btnFavorite;// 我的最爱按钮

	public MediaBinder mBinder;
	public ServiceConnection mServiceConnection;
	public Intent mPlayerServiceIntent;

	private MusicInfoSer mCurrentMusicInfo;

	private boolean isFirstTransition3dAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		this.initView();
		this.init();
		this.initServiceConnection();
	}

	@Override
	protected void onResume() {
		super.onResume();
		bindService(mPlayerServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mServiceConnection)
			unbindService(mServiceConnection);
		if (null != visualizer)
			visualizer.releaseVisualizerFx();
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
							btnPlay.setImageResource(R.drawable.player_btn_pause_style);
							viewMusicInfo();
						}

						@Override
						public void onPlay(int currentPosition) {
							seekBar.setProgress(currentPosition);
							currentTime.setText(FormatUtil.formatTime(currentPosition));
							btnPlay.setImageResource(R.drawable.player_btn_pause_style);
						}

						@Override
						public void onPause() {
							btnPlay.setImageResource(R.drawable.player_btn_play_style);
						}

						@Override
						public void onPlayError() {
						}

						@Override
						public void onPlayComplete() {
						}

						@Override
						public void onModeChange(int mode) {
							btnMode.setImageResource(modeImage[mode]);
						}
					});
					mBinder.setLyricView(lyricView, true);// 无歌词视图

					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							viewMusicInfo();
						}
					}, 100);

				}
			}
		};
	}

	private void viewMusicInfo() {
		if (null == mCurrentMusicInfo)
			return;
		mp3Name.setText(mCurrentMusicInfo.getSongName());
		ArrayList<String> list = new ArrayList<String>();
		String tag = mCurrentMusicInfo.getTag();
		if ("local".equals(tag)) {
			list.add(mCurrentMusicInfo.getArtist());
			list.add(mCurrentMusicInfo.getFormat());
			list.add("大小: " + mCurrentMusicInfo.getSize());
			list.add(mCurrentMusicInfo.getGenre());
			list.add(mCurrentMusicInfo.getAlbum());
			list.add(mCurrentMusicInfo.getYears());
			list.add(mCurrentMusicInfo.getChannels());
			list.add(mCurrentMusicInfo.getKbps());
			list.add(mCurrentMusicInfo.getHz());
		} else if ("net".equals(tag)) {
			list.add(mCurrentMusicInfo.getCount());
			list.add(mCurrentMusicInfo.getSingerName());
		}
		mp3Info.setTextList(list);
		mp3Artist.setText(mCurrentMusicInfo.getSongName());
		// 启动更新音乐可视化界面动画
		visualizer.releaseVisualizerFx();
		visualizer.setupVisualizerFx((null == mBinder) ? 0 : mBinder.getPlayingAudioSessionId());
		isFirstTransition3dAnimation = true;
		if ("local".equals(tag)) {
			if (CoverList.cover == null) {
				startTransition3dAnimation(
						BitmapFactory.decodeResource(getResources(), R.drawable.player_control_album));
			} else {
				startTransition3dAnimation(CoverList.cover);
			}

		} else if ("net".equals(tag)) {
			String imgPath = mCurrentMusicInfo.getSingerPicDir();
			if (TextUtils.isEmpty(imgPath)) {
				startTransition3dAnimation(
						BitmapFactory.decodeResource(getResources(), R.drawable.player_control_album));
			} else {
				ImageLoader.getInstance().displayImage(imgPath, mp3Cover);
			}
		}

		// if (null != mCurrentMusicInfo.getPath() &&
		// mCurrentMusicInfo.getPath().contains("http"))
		// btnFavorite.setVisibility(View.INVISIBLE);
		// btnFavorite.setImageResource(mCurrentMusicInfo.isFavorite() ?
		// R.drawable.player_btn_favorite_star_style :
		// R.drawable.player_btn_favorite_nostar_style);
	}

	private void clearMusicInfo() {
		mp3Name.setText("");
		ArrayList<String> list = new ArrayList<String>();
		mp3Info.setTextList(list);
		mp3Artist.setText("");
		// 启动更新音乐可视化界面动画
		visualizer.releaseVisualizerFx();
		currentTime.setText("00:00");
		totalTime.setText("00:00");
		seekBar.setMax(0);
		// mp3Cover.setImageBitmap(null);
		btnFavorite.setImageResource(R.drawable.player_btn_favorite_nostar_style);
	}

	private void init() {

	}

	private void initView() {

		btnReturn = (ImageButton) findViewById(R.id.activity_player_ib_return);
		btnMode = (ImageButton) findViewById(R.id.activity_player_ib_mode);
		btnPrevious = (ImageButton) findViewById(R.id.activity_player_ib_previous);
		btnPlay = (ImageButton) findViewById(R.id.activity_player_ib_play);
		btnNext = (ImageButton) findViewById(R.id.activity_player_ib_next);
		btnFavorite = (ImageButton) findViewById(R.id.activity_player_ib_favorite);
		seekBar = (SeekBar) findViewById(R.id.activity_player_seek);
		currentTime = (TextView) findViewById(R.id.activity_player_tv_time_current);
		totalTime = (TextView) findViewById(R.id.activity_player_tv_time_total);
		mp3Name = (PushView) findViewById(R.id.activity_player_tv_name);
		mp3Info = (PushView) findViewById(R.id.activity_player_tv_info);
		mp3Artist = (PushView) findViewById(R.id.activity_player_tv_artist);
		mp3Cover = (ImageView) findViewById(R.id.activity_player_cover);
		lyricView = (LyricView) findViewById(R.id.activity_player_lyric);
		visualizer = (VisualizerView) findViewById(R.id.activity_player_visualizer);

		btnReturn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (null != mBinder) {
					mBinder.seekBarStopTrackingTouch(seekBar.getProgress());
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if (null != mBinder) {
					mBinder.seekBarStartTrackingTouch();
				}
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (null != mBinder) {
				}
			}
		});
		btnMode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mBinder) {
					mBinder.setControlCommand(MediaService.CONTROL_COMMAND_MODE);
				}
			}
		});
		btnPrevious.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mBinder) {
					clearMusicInfo();
					mBinder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
				}
			}
		});
		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mBinder) {
					mBinder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
				}
			}
		});
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != mBinder) {
					clearMusicInfo();
					mBinder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
				}
			}
		});
		btnFavorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null == mCurrentMusicInfo)
					return;
				mCurrentMusicInfo.setFavorite(!mCurrentMusicInfo.isFavorite());
				DBDao db = new DBDao(mContext);
				db.update(mCurrentMusicInfo.getName(), mCurrentMusicInfo.isFavorite());
				btnFavorite.setImageResource(mCurrentMusicInfo.isFavorite() ? R.drawable.player_btn_favorite_star_style
						: R.drawable.player_btn_favorite_nostar_style);
			}
		});

	}

	/**
	 * 专辑封面翻转动画
	 * 
	 * @param bitmap
	 *            专辑封面图
	 */
	private void startTransition3dAnimation(final Bitmap bitmap) {
		final int w = mp3Cover.getWidth() / 2;
		final int h = mp3Cover.getHeight() / 2;
		final MarginLayoutParams params = (MarginLayoutParams) mp3Cover.getLayoutParams();

		final Rotate3dAnimation rotation1 = new Rotate3dAnimation(0.0f, 90.0f, params.leftMargin + w,
				params.topMargin + h, 300.0f, true);
		rotation1.setDuration(500);
		rotation1.setFillAfter(true);
		rotation1.setInterpolator(new AccelerateInterpolator());

		final Rotate3dAnimation rotation2 = new Rotate3dAnimation(270.0f, 360.0f, params.leftMargin + w,
				params.topMargin + h, 300.0f, false);// 反转动画
		rotation2.setDuration(500);
		rotation2.setFillAfter(true);
		rotation2.setInterpolator(new AccelerateInterpolator());

		rotation1.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (isFirstTransition3dAnimation) {
					isFirstTransition3dAnimation = false;
					mp3Cover.setImageBitmap(bitmap);
					mp3Cover.startAnimation(rotation2);
				}
			}
		});
		mp3Cover.startAnimation(rotation1);
	}

	/**
	 * 移植于ApiDemos里的的Rotate3dAnimation
	 * 
	 * 这里有个bug，180度翻转图片会反过来，所以只能折中的旋转一半又反转回来，效果就差了，交给各位去完善了
	 */
	private class Rotate3dAnimation extends Animation {
		private final float mFromDegrees;
		private final float mToDegrees;
		private final float mCenterX;
		private final float mCenterY;
		private final float mDepthZ;
		private final boolean mReverse;
		private Camera mCamera;

		/**
		 * Creates a new 3D rotation on the Y axis. The rotation is defined by
		 * its start angle and its end angle. Both angles are in degrees. The
		 * rotation is performed around a center point on the 2D space, definied
		 * by a pair of X and Y coordinates, called centerX and centerY. When
		 * the animation starts, a translation on the Z axis (depth) is
		 * performed. The length of the translation can be specified, as well as
		 * whether the translation should be reversed in time.
		 * 
		 * @param fromDegrees
		 *            the start angle of the 3D rotation
		 * @param toDegrees
		 *            the end angle of the 3D rotation
		 * @param centerX
		 *            the X center of the 3D rotation
		 * @param centerY
		 *            the Y center of the 3D rotation
		 * @param reverse
		 *            true if the translation should be reversed, false
		 *            otherwise
		 */
		public Rotate3dAnimation(float fromDegrees, float toDegrees, float centerX, float centerY, float depthZ,
				boolean reverse) {
			mFromDegrees = fromDegrees;
			mToDegrees = toDegrees;
			mCenterX = centerX;
			mCenterY = centerY;
			mDepthZ = depthZ;
			mReverse = reverse;
		}

		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			mCamera = new Camera();
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			final float fromDegrees = mFromDegrees;
			float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

			final float centerX = mCenterX;
			final float centerY = mCenterY;
			final Camera camera = mCamera;

			final Matrix matrix = t.getMatrix();

			camera.save();
			if (mReverse) {
				camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
			} else {
				camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
			}
			camera.rotateY(degrees);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);
		}
	}

}
