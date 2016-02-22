package com.music.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.music.bean.MusicInfoSer;
import com.music.blue.app.R;
import com.music.list.CoverList;
import com.music.list.LyricList;
import com.music.lyric.LyricItem;
import com.music.lyric.LyricParser;
import com.music.lyric.LyricView;
import com.music.service.MediaBinder.OnGetMusicInfoListener;
import com.music.service.MediaBinder.OnServiceBinderListener;
import com.music.ui.Main;
import com.music.utilities.AlbumUtil;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * <b>控制播放服务</b></br>
 * 
 * @version 实现基础播放服务，通过接口与外界通信，引入歌词的解析处理 <br>
 *          处理BindService等bug的衔接的问题<br>
 *          实现对音乐的控制播放(播放、暂停、上下首切换等)<br>
 *          支持对文件夹列表歌曲的播放<br>
 *          支持快退、快进播放<br>
 *          实现Notification跳转能够回到原来的界面，加上launchMode="singleTask"<br>
 *          修复暂停后重新播放歌词消失的问题<br>
 *          实现耳机线控及来电监听</br>
 */
public class MediaService extends Service {

	public static final String EK_PLAYING_POSITION = "ek_playing_position";

	public static final int CONTROL_COMMAND_PLAY = 0;// 控制命令：播放或者暂停
	public static final int CONTROL_COMMAND_PREVIOUS = 1;// 控制命令：上一首
	public static final int CONTROL_COMMAND_NEXT = 2;// 控制命令：下一首
	public static final int CONTROL_COMMAND_MODE = 3;// 控制命令：播放模式切换
	public static final int CONTROL_COMMAND_REWIND = 4;// 控制命令：快退
	public static final int CONTROL_COMMAND_FORWARD = 5;// 控制命令：快进
	public static final int CONTROL_COMMAND_REPLAY = 6;// 控制命令：用于快退、快进后的继续播放

	public static final String BROADCAST_ACTION_SERVICE = "com.cwd.cmeplayer.action.service";// 广播标志

	private static final int MEDIA_PLAY_ERROR = 0;
	private static final int MEDIA_PLAY_START = 1;
	private static final int MEDIA_PLAY_UPDATE = 2;
	private static final int MEDIA_PLAY_COMPLETE = 3;
	private static final int MEDIA_PLAY_UPDATE_LYRIC = 4;
	private static final int MEDIA_PLAY_REWIND = 5;
	private static final int MEDIA_PLAY_FORWARD = 6;
	private static final int MEDIA_BUTTON_ONE_CLICK = 7;
	private static final int MEDIA_BUTTON_DOUBLE_CLICK = 8;

	private final String PREFERENCES_NAME = "preferences_player_setting";
	private final String PREFERENCES_MODE = "preferences_play_mode";
	private final int MODE_NORMAL = 0;// 顺序播放，放到最后一首停止
	private final int MODE_REPEAT_ONE = 1;// 单曲循环
	private final int MODE_REPEAT_ALL = 2;// 全部循环
	private final int MODE_RANDOM = 3;// 随即播放
	private final int UPDATE_LYRIC_TIME = 150;// 歌词更新间隔0.15秒
	private final int UPDATE_UI_TIME = 500;// UI更新间隔1秒

	private String mMp3Path;// mp3文件路径
	private MusicInfoSer mMusicInfo;// 歌曲详情

	private boolean isHasLyric = false;// 是否有歌词
	private String mLyricPath;// 歌词文件路径
	private List<LyricItem> mLyricList;// 歌词列表

	private int mPlayingposition = 0;// 列表当前项
	private int mMp3Current = 0;// 歌曲当前时间
	private int mMp3Duration = 0;// 歌曲总时间
	private int mPlayMode = MODE_NORMAL;// 播放模式(默认顺序播放)
	private int mButtonClickCounts = 0;

	private MediaPlayer mMediaPlayer;
	private MediaBinder mBinder;
	private AlbumUtil mAlbumUtil;
	private LyricView mLyricView;

	private RemoteViews mRemoteViews;
	private ServiceHandler mHandler;
	private ServiceReceiver mReceiver;
	private Notification mNotification;
	private SharedPreferences mPreferences;

	@Override
	public void onCreate() {
		super.onCreate();
		mMediaPlayer = new MediaPlayer();
		mBinder = new MediaBinder();
		mAlbumUtil = new AlbumUtil();
		mLyricList = new ArrayList<LyricItem>();
		mHandler = new ServiceHandler(this);

		mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				mMp3Current = 0;// 重置
				resetLyricView();// 准备播放
				mHandler.sendEmptyMessage(MEDIA_PLAY_START);// 通知歌曲已播放
			}
		});
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mLyricList.clear();
				removeAllMsg();// 移除所有消息
				mHandler.sendEmptyMessage(MEDIA_PLAY_COMPLETE);
			}
		});
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				removeAllMsg();// 移除所有消息
				mp.reset();

				if (null == mMp3Path || mMp3Path.contains("http")) {
					Toast.makeText(getApplicationContext(), "歌曲加载失败", Toast.LENGTH_SHORT).show();
					mPlayingposition = 0;
					mMusicInfo = null;
					mMp3Path = null;
					mHandler.sendEmptyMessage(MEDIA_PLAY_ERROR);
					return true;
				}

				File file = new File(mMp3Path);
				if (file.exists()) {
					Toast.makeText(getApplicationContext(), "播放出错", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "文件已不存在", Toast.LENGTH_SHORT).show();
				}

				mPlayingposition = 0;
				mMusicInfo = null;
				mMp3Path = null;
				mHandler.sendEmptyMessage(MEDIA_PLAY_ERROR);
				return true;
			}
		});
		mBinder.setOnServiceBinderListener(new OnServiceBinderListener() {
			@Override
			public void seekBarStartTrackingTouch() {
				if (mMediaPlayer.isPlaying()) {
					removeUpdateMsg();
				}
			}

			@Override
			public void seekBarStopTrackingTouch(int progress) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.seekTo(progress);
					update();
				}
			}

			@Override
			public void lyric(LyricView lyricView, boolean isKLOK) {
				MediaService.this.mLyricView = lyricView;// 获得歌词视图
				if (MediaService.this.mLyricView != null) {
					MediaService.this.mLyricView.setKLOK(isKLOK);
				}
			}

			@Override
			public void control(int command) {
				switch (command) {
				case CONTROL_COMMAND_PLAY:// 播放与暂停
					if (mMediaPlayer.isPlaying()) {
						pause();
					} else {
						if (mMp3Path != null) {
							mMediaPlayer.start();
							update();
							resetLyricView();
						} else {// 无指定情况下播放全部歌曲列表的第一首
							mPlayingposition = 0;
							play();
						}
					}
					break;

				case CONTROL_COMMAND_PREVIOUS:// 上一首
					previous();
					break;

				case CONTROL_COMMAND_NEXT:// 下一首
					next();
					break;

				case CONTROL_COMMAND_MODE:// 播放模式
					if (mPlayMode < MODE_RANDOM) {
						mPlayMode++;
					} else {
						mPlayMode = MODE_NORMAL;
					}
					switch (mPlayMode) {
					case MODE_NORMAL:
						Toast.makeText(getApplicationContext(), "顺序播放", Toast.LENGTH_SHORT).show();
						break;

					case MODE_REPEAT_ONE:
						Toast.makeText(getApplicationContext(), "单曲循环", Toast.LENGTH_SHORT).show();
						break;

					case MODE_REPEAT_ALL:
						Toast.makeText(getApplicationContext(), "全部循环", Toast.LENGTH_SHORT).show();
						break;

					case MODE_RANDOM:
						Toast.makeText(getApplicationContext(), "随机播放", Toast.LENGTH_SHORT).show();
						break;
					}
					mBinder.modeChange(mPlayMode);
					break;

				case CONTROL_COMMAND_REWIND:// 快退
					if (mMediaPlayer.isPlaying()) {
						removeAllMsg();
						rewind();
					}
					break;

				case CONTROL_COMMAND_FORWARD:// 快进
					if (mMediaPlayer.isPlaying()) {
						removeAllMsg();
						forward();
					}
					break;

				case CONTROL_COMMAND_REPLAY:// 用于快退、快进后的继续播放
					if (mMediaPlayer.isPlaying()) {
						replay();
					}
					break;
				}
			}
		});
		mBinder.setOnGetMusicInfoListener(new OnGetMusicInfoListener() {
			@Override
			public MusicInfoSer getCurrentPlayingMusicInfo() {
				return mMusicInfo;
			}

			@Override
			public int getAudioSessionId() {
				if (null != mMediaPlayer)
					return mMediaPlayer.getAudioSessionId();
				return 0;
			}
		});
		mPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPlayMode = mPreferences.getInt(PREFERENCES_MODE, MODE_NORMAL);// 取出上次的播放模式

		mNotification = new Notification();// 通知栏相关
		mNotification.icon = R.drawable.ic_launcher;
		mNotification.flags = Notification.FLAG_NO_CLEAR;
		mNotification.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
				new Intent(getApplicationContext(), Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
		mRemoteViews = new RemoteViews(getPackageName(), R.layout.layout_playing_notification_item);

		mReceiver = new ServiceReceiver();// 注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		intentFilter.addAction(BROADCAST_ACTION_SERVICE);
		registerReceiver(mReceiver, intentFilter);

		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);// 获取电话通讯服务
		telephonyManager.listen(new ServicePhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);// 创建一个监听对象，监听电话状态改变事件
	}

	// sdk2.0以上还是不使用onStart了吧...
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			mPlayingposition = intent.getIntExtra(EK_PLAYING_POSITION, 0);
			if (-1 == mPlayingposition)
				return super.onStartCommand(intent, flags, startId);
			play();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMediaPlayer != null) {
			stopForeground(true);
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			removeAllMsg();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
		mPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPreferences.edit().putInt(PREFERENCES_MODE, mPlayMode).commit();// 保存上次的播放模式
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mLyricView = null;
		removeAllMsg();// 移除所有消息
		return true;// 一定返回true，允许执行onRebind
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		if (mMediaPlayer.isPlaying()) {// 如果正在播放重新绑定服务的时候重新注册
			update();
			resetLyricView();// 因为消息已经移除，所有需要重新开启更新操作
		} else {
			if (mMp3Path != null) {// 暂停原先播放重新开页面需要恢复原先的状态
				mMp3Duration = mMediaPlayer.getDuration();
				mBinder.playStart(mMusicInfo);
				mMp3Current = mMediaPlayer.getCurrentPosition();
				mBinder.playUpdate(mMp3Current);
				mBinder.playPause();
			}
		}
		mBinder.modeChange(mPlayMode);
	}

	/**
	 * 播放操作
	 */
	private void play() {
		int size = 0;
		size = PlayingMusic.list.size();
		if (0 > mPlayingposition)
			mPlayingposition = 0;
		if ((size - 1) < mPlayingposition)
			mPlayingposition = size - 1;

		if (size > 0) {
			mMusicInfo = PlayingMusic.list.get(mPlayingposition);
			String tag = mMusicInfo.getTag();
			if ("local".equals(tag)) {
				mMp3Path = mMusicInfo.getPath();
				mLyricPath = LyricList.map.get(mMusicInfo.getFile());
				if (!TextUtils.isEmpty(mMp3Path)) {
					initMedia();// 先初始化音乐
					initLrc();// 再初始化歌词
				}
			} else if ("net".equals(tag)) {
				mMp3Path = mMusicInfo.getSongListenDir();
				final String lrcDir = mMusicInfo.getLrcDir();
				if (!TextUtils.isEmpty(mMp3Path)) {
					initMedia();// 先初始化音乐
					if(TextUtils.isEmpty(lrcDir)){
						return;
					}
					final String lrcName = mMusicInfo.getSingerName() + " - " + mMusicInfo.getSongName() + ".lrc";
					final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/music");
					if (!dir.exists()) {
						dir.mkdirs();
					}
					final File file = new File(dir, lrcName);
					if (!file.exists()) {
						mHandler.sendEmptyMessage(MEDIA_PLAY_UPDATE_LYRIC);
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									URL url = new URL(lrcDir);
									URLConnection con = url.openConnection();
									InputStream inStream = con.getInputStream();
									byte[] data = new byte[1024];
									int b = -1;
									FileOutputStream fos = new FileOutputStream(file);
									while ((b = inStream.read(data)) != -1) {
										fos.write(data, 0, data.length);
									}
									fos.flush();
									fos.close();
									inStream.close();
									File _file = new File(dir, lrcName);
									mLyricPath = _file.getAbsolutePath();
									initLrc();// 再初始化歌词
									mHandler.sendEmptyMessage(MEDIA_PLAY_UPDATE_LYRIC);
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						}).start();
					} else {
						mLyricPath = file.getAbsolutePath();
						initLrc();// 再初始化歌词
					}

				}
			}
		}
	}

	/**
	 * 自动播放操作
	 */
	private void autoPlay() {
		if (mPlayMode == MODE_NORMAL) {
			if (mPlayingposition != getSize() - 1) {
				next();
			} else {
				mBinder.playPause();
			}
		} else if (mPlayMode == MODE_REPEAT_ONE) {
			play();
		} else {
			next();
		}
	}

	/**
	 * 上一首操作
	 */
	private void previous() {
		mLyricList.clear();
		int size = getSize();
		if (size > 0) {
			if (mPlayMode == MODE_RANDOM) {
				mPlayingposition = (int) (Math.random() * size);
			} else {
				if (mPlayingposition == 0) {
					mPlayingposition = size - 1;
				} else {
					mPlayingposition--;
				}
			}
			play();
		}
	}

	/**
	 * 下一首操作
	 */
	private void next() {
		mLyricList.clear();
		int size = getSize();
		if (size > 0) {
			if (mPlayMode == MODE_RANDOM) {
				mPlayingposition = (int) (Math.random() * size);
			} else {
				if (mPlayingposition == size - 1) {
					mPlayingposition = 0;
				} else {
					mPlayingposition++;
				}
			}
			play();
		}
	}

	/**
	 * 快退
	 */
	private void rewind() {
		int current = mMp3Current - 1000;
		mMp3Current = current > 0 ? current : 0;
		mBinder.playUpdate(mMp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_REWIND, 100);
	}

	/**
	 * 快进
	 */
	private void forward() {
		int current = mMp3Current + 1000;
		mMp3Current = current < mMp3Duration ? current : mMp3Duration;
		mBinder.playUpdate(mMp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_FORWARD, 100);
	}

	/**
	 * 用于快退、快进后的继续播放
	 */
	private void replay() {
		if (mHandler.hasMessages(MEDIA_PLAY_REWIND)) {
			mHandler.removeMessages(MEDIA_PLAY_REWIND);
		}
		if (mHandler.hasMessages(MEDIA_PLAY_FORWARD)) {
			mHandler.removeMessages(MEDIA_PLAY_FORWARD);
		}
		mMediaPlayer.seekTo(mMp3Current);
		mHandler.sendEmptyMessage(MEDIA_PLAY_UPDATE);
		if (mLyricView != null && isHasLyric) {
			mLyricView.setSentenceEntities(mLyricList);
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC, UPDATE_LYRIC_TIME);// 通知刷新歌词
		}
	}

	/**
	 * 获得列表歌曲数量
	 * 
	 * @return 数量
	 */
	private int getSize() {
		return PlayingMusic.list.size();
	}

	/**
	 * 初始化媒体播放器
	 */
	private void initMedia() {
		try {
			removeAllMsg();// 对于重新播放需要移除所有消息
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(mMp3Path);
			mMediaPlayer.prepareAsync();
			stopForeground(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化歌词
	 */
	private void initLrc() {
		isHasLyric = false;
		if (mLyricPath != null) {
			try {
				LyricParser parser = new LyricParser(mLyricPath);
				mLyricList = parser.parser();
				isHasLyric = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (mLyricView != null) {
				mLyricView.clear();
			}
		}
	}

	/**
	 * 释放歌词，加载新歌词
	 */
	private void resetLyricView() {
		removeUpdateLrcViewMsg();
		if (mLyricView != null) {
			mLyricView.clear(); // 清空会把现有的lyricList也清空，内存共用了???
			if (isHasLyric) {
				mLyricView.setSentenceEntities(mLyricList);
				mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC, UPDATE_LYRIC_TIME);// 通知刷新歌词
			}
		}
	}

	/**
	 * 开始播放，获得总时间和AudioSessionId，并启动更新UI任务
	 */
	@SuppressLint("NewApi")
	private void start() {
		String tag = mMusicInfo.getTag();
		String artist = "";
		String name = "";
		if ("local".equals(tag)) {
			mMp3Duration = mMediaPlayer.getDuration();
			mMusicInfo.setMp3Duration(mMp3Duration);
			mMusicInfo.setAudioSessionId(mMediaPlayer.getAudioSessionId());
			CoverList.cover = mAlbumUtil.scanAlbumImage(mMusicInfo.getPath());
			mBinder.playStart(mMusicInfo);
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);

			artist = mMusicInfo.getArtist();
			name = mMusicInfo.getSongName();
			mNotification.tickerText = artist + " - " + name;
			if (CoverList.cover == null) {
				mRemoteViews.setImageViewResource(R.id.notification_item_album, R.drawable.player_control_album);
			} else {
				mRemoteViews.setImageViewBitmap(R.id.notification_item_album, CoverList.cover);
			}
		} else if ("net".equals(tag)) {
			mBinder.playStart(mMusicInfo);
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);
			artist = mMusicInfo.getSingerName();
			name = mMusicInfo.getSongName();
			mNotification.tickerText = artist + " - " + name;
			String singerPic = mMusicInfo.getSingerPicDir();
			if (TextUtils.isEmpty(singerPic)) {
				mRemoteViews.setImageViewResource(R.id.notification_item_album, R.drawable.player_control_album);
			} else {
				try {
					URL url = new URL(singerPic);
					URLConnection conn = url.openConnection();
					InputStream inStream = conn.getInputStream();
					Bitmap bitmap = BitmapFactory.decodeStream(inStream);
					inStream.close();
					mRemoteViews.setImageViewBitmap(R.id.notification_item_album, bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		mRemoteViews.setTextViewText(R.id.notification_item_name, name);
		mRemoteViews.setTextViewText(R.id.notification_item_artist, artist);
		mNotification.contentView = mRemoteViews;
		startForeground(1, mNotification);// id设为0将不会显示Notification
	}

	/**
	 * 更新UI，发现MediaPlayer.getCurrentPosition()的bug很严重，感觉指的不是时间而是帧数，
	 * 而且Handler处理事务要话费时间，虽然间隔1秒的延时时间，但处理完成就不止1秒的时间，
	 * 所以换算后会出现跳秒的情况，机子配置越差的感觉越明显，本想通过自增来实现，但发现误差更大，暂无其他方法了
	 */
	private void update() {
		removeUpdateMsg();
		mMp3Current = mMediaPlayer.getCurrentPosition();
		mBinder.playUpdate(mMp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);
	}

	/**
	 * 暂停音乐
	 */
	private void pause() {
		removeAllMsg();// 移除所有消息
		mMediaPlayer.pause();
		mBinder.playPause();
		stopForeground(true);
	}

	/**
	 * 移除更新UI的消息
	 */
	private void removeUpdateMsg() {
		if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE)) {
			mHandler.removeMessages(MEDIA_PLAY_UPDATE);
		}
	}

	/**
	 * 播放完成
	 */
	private void complete() {
		mBinder.playComplete();
		mBinder.playUpdate(mMp3Duration);
		autoPlay();
	}

	/**
	 * 播放出错
	 */
	private void error() {
		mBinder.playError();
		mBinder.playPause();
	}

	/**
	 * 刷新歌词
	 */
	private void updateLrcView() {
		if (null == mLyricView)
			return;
		if (mLyricList.size() > 0) {
			mLyricView.setIndex(getLrcIndex(mMediaPlayer.getCurrentPosition(), mMp3Duration));
			mLyricView.invalidate();
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC, UPDATE_LYRIC_TIME);
		}
	}

	/**
	 * 移除更新歌词的消息
	 */
	private void removeUpdateLrcViewMsg() {
		if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE_LYRIC)) {
			mHandler.removeMessages(MEDIA_PLAY_UPDATE_LYRIC);
		}
	}

	/**
	 * 移除所有消息
	 */
	private void removeAllMsg() {
		removeUpdateMsg();
		removeUpdateLrcViewMsg();
	}

	/**
	 * 耳机线控-处理单击过渡事件
	 */
	private void buttonOneClick() {
		mButtonClickCounts++;
		mHandler.sendEmptyMessageDelayed(MEDIA_BUTTON_DOUBLE_CLICK, 300);
	}

	/**
	 * 耳机线控-响应单击和双击事件
	 */
	private void buttonDoubleClick() {
		if (mButtonClickCounts == 1) {
			mBinder.setControlCommand(CONTROL_COMMAND_PLAY);
		} else if (mButtonClickCounts > 1) {
			mBinder.setControlCommand(CONTROL_COMMAND_NEXT);
		}
		mButtonClickCounts = 0;
	}

	/**
	 * 歌词同步处理
	 */
	private int[] getLrcIndex(int currentTime, int duration) {
		int index = 0;
		int size = mLyricList.size();
		if (currentTime < duration) {
			for (int i = 0; i < size; i++) {
				if (i < size - 1) {
					if (currentTime < mLyricList.get(i).getTime() && i == 0) {
						index = i;
					}
					if (currentTime > mLyricList.get(i).getTime() && currentTime < mLyricList.get(i + 1).getTime()) {
						index = i;
					}
				}
				if (i == size - 1 && currentTime > mLyricList.get(i).getTime()) {
					index = i;
				}
			}
		}
		int temp1 = mLyricList.get(index).getTime();
		int temp2 = (index == (size - 1)) ? 0 : mLyricList.get(index + 1).getTime() - temp1;
		return new int[] { index, currentTime, temp1, temp2 };
	}

	private class ServicePhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if (state == TelephonyManager.CALL_STATE_RINGING && mMediaPlayer != null && mMediaPlayer.isPlaying()) { // 来电
				pause();
			}
		}
	}

	private class ServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				boolean isActionMediaButton = Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction());
				if (isActionMediaButton) {// 由于广播优先级的限制，此功能未必能够很好的支持
					KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
					if (event == null) {
						return;
					}
					long eventTime = event.getEventTime() - event.getDownTime();// 按键按下到松开的时长
					if (eventTime > 1000) {
						mBinder.setControlCommand(CONTROL_COMMAND_PREVIOUS);
						abortBroadcast();// 终止广播(不让别的程序收到此广播，免受干扰)
					} else {
						if (event.getAction() == KeyEvent.ACTION_UP) {
							mHandler.sendEmptyMessage(MEDIA_BUTTON_ONE_CLICK);
							abortBroadcast();// 终止广播(不让别的程序收到此广播，免受干扰)
						}
					}
				} else {
				}
			}
		}
	}

	private static class ServiceHandler extends Handler {
		private WeakReference<MediaService> reference;

		public ServiceHandler(MediaService service) {
			reference = new WeakReference<MediaService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			if (reference.get() != null) {
				MediaService theService = reference.get();
				switch (msg.what) {
				case MEDIA_PLAY_START:
					theService.start();// 播放开始
					break;
				case MEDIA_PLAY_UPDATE:
					theService.update();// 更新UI
					break;
				case MEDIA_PLAY_COMPLETE:
					theService.complete();// 播放完成
					break;
				case MEDIA_PLAY_ERROR:
					theService.error();// 播放出错
					break;
				case MEDIA_PLAY_UPDATE_LYRIC:
					theService.updateLrcView();// 刷新歌词
					break;
				case MEDIA_PLAY_REWIND:
					theService.rewind();// 快退线程
					break;
				case MEDIA_PLAY_FORWARD:
					theService.forward();// 快进线程
					break;
				case MEDIA_BUTTON_ONE_CLICK:
					theService.buttonOneClick();// 线控单机事件
					break;
				case MEDIA_BUTTON_DOUBLE_CLICK:
					theService.buttonDoubleClick();// 线控双击事件
					break;
				}
			}
		}
	}

}
