package com.music.ui;

import android.os.Bundle;

import com.music.base.BaseActivity;
import com.music.king.app.R;

public class CopyRight extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_copyright);
		this.initTitleBar();
	}

	@Override
	protected void initTitleBar() {
		super.initTitleBar();
		tv_titlebar_title.setText("版权声明");
	}

}
