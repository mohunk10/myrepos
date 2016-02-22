package com.music.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.cmsc.cmmusic.init.InitCmmInterface;
import com.music.base.BaseActivity;
import com.music.blue.app.R;
import com.music.constant.Constants;
import com.music.service.MediaService;

public class Splash extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		this.loading();
		Intent intent = new Intent(mContext, MediaService.class);
		intent.putExtra(MediaService.EK_PLAYING_POSITION, -1);
		startService(intent);
	}

	@SuppressLint("HandlerLeak")
	private void loading() {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case Constants.MSG_SUCCESS:
					toMain();
					break;
				case Constants.MSG_FAIL:
					break;
				case Constants.MSG_INVALID:
					break;
				case Constants.MSG_NULL:
					break;
				case Constants.MSG_EXCEPTION:
					break;
				default:
					break;
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = Message.obtain();
				try {

					Thread.sleep(1000 * 3);

					msg.what = Constants.MSG_SUCCESS;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = Constants.MSG_EXCEPTION;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			};
		}.start();
	}

	private void toMain() {
		Intent intent = new Intent(mContext, Main.class);
		startActivity(intent);
		finish();
	}

}
