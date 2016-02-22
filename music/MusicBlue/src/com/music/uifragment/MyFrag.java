package com.music.uifragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.music.base.BaseFragment;
import com.music.blue.app.R;

public class MyFrag extends BaseFragment {

	private TextView tv_user_name;

	@Override
	protected void firstVisibleToUser() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mContentView = inflater.inflate(R.layout.layout_my, container, false);
		return mContentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.initView();
	}

	@Override
	protected void visibleToUser() {
		super.visibleToUser();
		tv_user_name.setText(TextUtils.isEmpty(mCurrentUser) ? "" : mCurrentUser);
	}

	// @Override
	// public void onResume() {
	// super.onResume();
	// tv_user_name.setText(TextUtils.isEmpty(mCurrentUser) ? "" :
	// mCurrentUser);
	// }

	private void initView() {
		tv_user_name = (TextView) mContentView.findViewById(R.id.tv_user_name);
	}

}
