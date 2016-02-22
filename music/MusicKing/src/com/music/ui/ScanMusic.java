package com.music.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.music.base.BaseActivity;
import com.music.base.BaseFl;
import com.music.bean.ScanInfo;
import com.music.king.app.R;
import com.music.uifragment.LocalMusicFrag;
import com.music.uifragment.ScanMusicFrag;
import com.music.utilities.ScanUtil;

public class ScanMusic extends BaseActivity {

	private BaseFl<ScanInfo> mFragment;

	private TextView tv_tip;

	@SuppressLint("HandlerLeak")
	@Override
	public void tv_titlebar_right(View view) {
		List<ScanInfo> lvData = mFragment.getData();
		final List<String> folderList = new ArrayList<String>();
		for (ScanInfo info : lvData) {
			if (info.isChecked())
				folderList.add(info.getFolderPath());
		}
		if (0 == folderList.size()) {
			new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE).setTitleText("请选择需要扫描的路径").show();
			return;
		}

		mAppContext.setMemCache(LocalMusicFrag.MK_REFRESH, true);

		final ScanUtil scanUtil = new ScanUtil(mContext);
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				tv_tip.setText((null == msg.obj) ? "" : msg.obj.toString());
			}
		};
		mLoadingRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					scanUtil.scanMusicFromSD(folderList, handler);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		mLoadingThread = new Thread(mLoadingRunnable);
		mLoadingThread.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		this.initTitleBar();
		this.initView();
		this.init();
	}

	private void init() {

	}

	private void initView() {
		tv_tip = (TextView) findViewById(R.id.tv_tip);

		mFragment = new ScanMusicFrag();
		getSupportFragmentManager().beginTransaction().add(R.id.activity_fragment, (Fragment) mFragment, "activity_fragment").commit();
	}

	@Override
	protected void initTitleBar() {
		super.initTitleBar();
		tv_titlebar_title.setText("歌曲扫描");
		tv_titlebar_right.setText("扫描");
	}

}
