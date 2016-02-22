package com.music.ui;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.music.base.BaseActivity;
import com.music.base.Result;
import com.music.bean.DataLogin;
import com.music.constant.Constants;
import com.music.constant.Urls;
import com.music.king.app.R;
import com.music.view.Input;

public class Login extends BaseActivity {
	public final static String PK_AUTO_LOGIN_USER = "property_key_auto_login_phone";
	public final static String PK_AUTO_LOGIN_PWD = "property_key_auto_login_pwd";

	private Input in_user;
	private Input in_pwd;
	private TextView tv_login;

	@Override
	public void tv_titlebar_right(View view) {
		Intent intent = new Intent(mContext, Register.class);
		startActivity(intent);
		finish();
	}

	public void tv_login(View view) {

		if (in_user.isInputError())
			return;

		if (in_pwd.isInputError())
			return;

		String user = in_user.getInput().toString().trim();
		String pwd = in_pwd.getInput().toString().trim();

		login(user, pwd);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		this.initTitleBar();
		this.initView();
		this.init();
	}

	private void init() {

	}

	@Override
	protected void onResume() {
		super.onResume();
		this.initLogin();
	}

	private void initLogin() {
		String user = mAppContext.getProperty(PK_AUTO_LOGIN_USER);
		String pwd = mAppContext.getProperty(PK_AUTO_LOGIN_PWD);

		in_user.setInput(TextUtils.isEmpty(user) ? "" : user);

	}

	private void login(final String user, final String pwd) {

		Map<String, String> mapParam = new ConcurrentHashMap<String, String>();
		mapParam.put("loginname", user);
		mapParam.put("pwd", pwd);

		req(Urls.URL_LOGIN, mapParam, "处理中", true, null, new OnRequestSuccess() {
			@Override
			public void onSuccess(final SweetAlertDialog alert, int statusCode, org.apache.http.Header[] headers, final byte[] responseBody) {
				mLoadingRunnable = new Runnable() {
					@Override
					public void run() {
						try {

							String json = new String(responseBody);
							debug(json);

							Type type = new TypeToken<Result<DataLogin>>() {
							}.getType();
							final Result<DataLogin> result = new Gson().fromJson(json, type);
							final int msgFlag = getMsgFlag(result);

							switch (msgFlag) {
							case Constants.MSG_SUCCESS:

								// save something on memcache
								mAppContext.setMemCache(MK_CURRENT_USER, user);

								mAppContext.setProperty(PK_AUTO_LOGIN_USER, user);
								mAppContext.setProperty(PK_AUTO_LOGIN_PWD, pwd);

								DataLogin data = null;
								if (null != (data = result.getRetdata())) {
									mAppContext.setMemCache(MK_CURRENT_USER, data.getNickname());
									mAppContext.setMemCache(MK_CURRENT_USER_PHONE, data.getLoginname());
								}

								runOnUiThread(new Runnable() {
									public void run() {
										hideAlert(alert);
										finish();
									}
								});

								return;
							case Constants.MSG_FAIL:
								break;
							case Constants.MSG_NULL:
								break;
							case Constants.MSG_EXCEPTION:
								break;
							default:
								break;
							}

							runOnUiThread(new Runnable() {
								public void run() {
									alertMsg(msgFlag, result);
								}
							});

						} catch (Exception e) {
							e.printStackTrace();
							runOnUiThread(new Runnable() {
								public void run() {
									hideAlert(alert);
									new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE).setTitleText("数据解析错误").show();
								}
							});
						}
					}
				};
				mLoadingThread = new Thread(mLoadingRunnable);
				mLoadingThread.start();
			}
		});

	}

	private void initView() {
		in_user = (Input) findViewById(R.id.in_user);
		in_pwd = (Input) findViewById(R.id.in_pwd);
		tv_login = (TextView) findViewById(R.id.tv_login);

	}

	@Override
	protected void initTitleBar() {
		super.initTitleBar();
		tv_titlebar_title.setText("用户登录");
		tv_titlebar_right.setText("注册");
	}

}
