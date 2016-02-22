package com.music.ui;

import android.os.Bundle;

import com.music.base.BaseActivity;
import com.music.life.app.R;

public class About extends BaseActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		this.initTitleBar();
	}
	
	@Override
	protected void initTitleBar() {
		super.initTitleBar();
		tv_titlebar_title.setText("联系我们");
	}

}
