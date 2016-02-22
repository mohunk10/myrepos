package com.music.service;

import com.music.bean.MusicInfoSer;
import com.music.lyric.LyricView;

import android.os.Binder;

/**
 * <b>控制播放Binder类</b></br>
 */
public class MediaBinder extends Binder {

	private OnPlayerListener mOnPlayerListener;
	private OnServiceBinderListener mOnServiceBinderListener;
	private OnGetMusicInfoListener mOnGetMusicInfoListener;

	protected void playStart(MusicInfoSer info) {
		if (mOnPlayerListener != null) {
			mOnPlayerListener.onStart(info);
		}
	}

	protected void playUpdate(int currentPosition) {
		if (mOnPlayerListener != null) {
			mOnPlayerListener.onPlay(currentPosition);
		}
	}

	protected void playPause() {
		if (mOnPlayerListener != null) {
			mOnPlayerListener.onPause();
		}
	}

	protected void playComplete() {
		if (mOnPlayerListener != null) {
			mOnPlayerListener.onPlayComplete();
		}
	}

	protected void playError() {
		if (mOnPlayerListener != null) {
			mOnPlayerListener.onPlayError();
		}
	}

	protected void modeChange(int mode) {
		if (mOnPlayerListener != null) {
			mOnPlayerListener.onModeChange(mode);
		}
	}

	/**
	 * 触及SeekBar时响应
	 */
	public void seekBarStartTrackingTouch() {
		if (mOnServiceBinderListener != null) {
			mOnServiceBinderListener.seekBarStartTrackingTouch();
		}
	}

	/**
	 * 离开SeekBar时响应
	 * 
	 * @param progress
	 *            当前进度
	 */
	public void seekBarStopTrackingTouch(int progress) {
		if (mOnServiceBinderListener != null) {
			mOnServiceBinderListener.seekBarStopTrackingTouch(progress);
		}
	}

	/**
	 * 设置歌词视图
	 * 
	 * @param lrcView
	 *            歌词视图
	 * @param isKLOK
	 *            是否属于卡拉OK模式
	 */
	public void setLyricView(LyricView lrcView, boolean isKLOK) {
		if (mOnServiceBinderListener != null) {
			mOnServiceBinderListener.lyric(lrcView, isKLOK);
		}
	}

	/**
	 * 设置控制命令
	 * 
	 * @param command
	 *            控制命令
	 */
	public void setControlCommand(int command) {
		if (mOnServiceBinderListener != null) {
			mOnServiceBinderListener.control(command);
		}
	}

	public MusicInfoSer getPlayingMusicInfo() {
		if (null != mOnGetMusicInfoListener) {
			return mOnGetMusicInfoListener.getCurrentPlayingMusicInfo();
		}
		return null;
	}

	public int getPlayingAudioSessionId() {
		if (null != mOnGetMusicInfoListener) {
			return mOnGetMusicInfoListener.getAudioSessionId();
		}
		return 0;
	}

	public void setOnPlayerListener(OnPlayerListener onPlayerListener) {
		this.mOnPlayerListener = onPlayerListener;
	}

	protected void setOnServiceBinderListener(OnServiceBinderListener onServiceBinderListener) {
		this.mOnServiceBinderListener = onServiceBinderListener;
	}

	public void setOnGetMusicInfoListener(OnGetMusicInfoListener onGetMusicInfoListener) {
		this.mOnGetMusicInfoListener = onGetMusicInfoListener;
	}

	/**
	 * 播放监听器
	 */
	public interface OnPlayerListener {

		/**
		 * 开始播放
		 * 
		 * @param info
		 *            歌曲详细信息
		 */
		public void onStart(MusicInfoSer info);

		/**
		 * 开始播放
		 * 
		 * @param current
		 *            当前播放时间(String类型)
		 */
		public void onPlay(int currentPosition);

		/**
		 * 暂停播放
		 */
		public void onPause();

		/**
		 * 播放完成
		 */
		public void onPlayComplete();

		/**
		 * 播放出错
		 */
		public void onPlayError();

		/**
		 * 播放模式变更
		 */
		public void onModeChange(int mode);
	}

	/**
	 * 回调接口，只允许service使用
	 */
	protected interface OnServiceBinderListener {

		/**
		 * 触及SeekBar时响应
		 */
		void seekBarStartTrackingTouch();

		/**
		 * 离开SeekBar时响应
		 * 
		 * @param progress
		 *            当前进度
		 */
		void seekBarStopTrackingTouch(int progress);

		/**
		 * 设置歌词
		 * 
		 * @param lyricView
		 *            歌词视图
		 * @param isKLOK
		 *            是否属于卡拉OK模式
		 */
		void lyric(LyricView lyricView, boolean isKLOK);

		/**
		 * 播放控制(播放、暂停、上一首、下一首、播放模式切换)
		 * 
		 * @param command
		 *            控制命令
		 */
		void control(int command);
	}

	/**
	 * 回调接口，获取当前播放的音乐信息
	 */
	protected interface OnGetMusicInfoListener {

		/**
		 * 后去当前播放音乐
		 */
		MusicInfoSer getCurrentPlayingMusicInfo();

		int getAudioSessionId();
	}

}
