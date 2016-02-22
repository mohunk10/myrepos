package com.music.ui;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.music.base.BaseActivity;
import com.music.base.Result;
import com.music.base.BaseActivity.OnRequestSuccess;
import com.music.bean.DataLogin;
import com.music.constant.Constants;
import com.music.constant.Urls;
import com.music.king.app.R;
import com.music.view.Input;

public class Register extends BaseActivity {

	private Input in_name;
	private Input in_phone;
	private Input in_pwd;
	private Input in_confirm_pwd;
	private TextView tv_register;

	public void tv_register(View view) {

		if (in_name.isInputError())
			return;
		if (in_phone.isInputError())
			return;
		if (in_pwd.isInputError())
			return;
		if (in_confirm_pwd.isInputError())
			return;

		String name = in_name.getInput().toString().trim();
		String phone = in_phone.getInput().toString().trim();
		String pwd = in_pwd.getInput().toString().trim();
		String confirmPwd = in_confirm_pwd.getInput().toString().trim();

		if (!pwd.equals(confirmPwd)) {
			new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE).setTitleText("输入的密码不一致").show();
			return;
		}

		register(name, phone, pwd);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		this.initTitleBar();
		this.initView();
		this.init();
	}

	private void init() {
	}

	private void register(final String name, final String phone, final String pwd) {

		Map<String, String> mapParam = new ConcurrentHashMap<String, String>();
		mapParam.put("nickname", name);
		mapParam.put("loginname", phone);
		mapParam.put("pwd", pwd);

		req(Urls.URL_REGISTER, mapParam, "处理中", true, null, new OnRequestSuccess() {
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
								mAppContext.setMemCache(MK_CURRENT_USER, name);
								mAppContext.setMemCache(MK_CURRENT_USER_PHONE, phone);

								mAppContext.setProperty(Login.PK_AUTO_LOGIN_USER, phone);
								mAppContext.setProperty(Login.PK_AUTO_LOGIN_PWD, pwd);

								DataLogin data = null;
								if (null != (data = result.getRetdata())) {

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
		in_name = (Input) findViewById(R.id.in_name);
		in_phone = (Input) findViewById(R.id.in_phone);
		in_pwd = (Input) findViewById(R.id.in_pwd);
		in_confirm_pwd = (Input) findViewById(R.id.in_confirm_pwd);
		tv_register = (TextView) findViewById(R.id.tv_register);

	}

}
