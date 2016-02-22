package com.music.base;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.music.blue.app.AppConfig;
import com.music.blue.app.AppContext;
import com.music.blue.app.AppManager;
import com.music.blue.app.R;
import com.music.constant.Constants;
import com.music.constant.Urls;
import com.music.utilities.SystemBarTintManager;

public abstract class BaseActivity extends FragmentActivity {

	public static final String EK_DEFAULT = "extra_key_default";
	public static final String SK_DEFAULT = "serializable_key_default";
	public static final String MK_REQTOKEN = "memcache_key_reqtoken";
	public static final String MK_CURRENT_USER = "memcache_key_current_user";
	public static final String MK_CURRENT_USER_PHONE="memcache_key_current_user_phone";
	public static final String MK_BPUSH_API_KEY = "memcache_key_bpush_api_key";
	public final static String PK_USER = "property_key_user";

	//排行榜ID或歌名关键字
	public static final String MUSIC_KEY="music_key";
	//检索类型1:排行榜ID;2:歌名关键字
	public static final String PARAM_TYPE="param_type";
	
	public static final String PPK_DEFAULT = "public_params_key_default";
	
	protected String Tag = this.getClass().getSimpleName();
	protected Map<String, String> mPublicParams = new HashMap<String, String>();

	public Map<String, String> getPublicParams() {
		return mPublicParams;
	}

	protected Method mCurrentItemMethod;

	public Method getCurrentItemMethod() {
		return mCurrentItemMethod;
	}

	protected TextView tv_titlebar_back;
	protected TextView tv_titlebar_title;
	protected TextView tv_titlebar_right;

	public void tv_titlebar_back(View view) {
		finish();
	}

	public void tv_titlebar_title(View view) {
	}

	public void tv_titlebar_right(View view) {
		finish();
	}

	protected void initTitleBar() {
		tv_titlebar_back = (TextView) findViewById(R.id.tv_titlebar_back);
		tv_titlebar_title = (TextView) findViewById(R.id.tv_titlebar_title);
		tv_titlebar_right = (TextView) findViewById(R.id.tv_titlebar_right);
	}

	/**
	 * debug调试TAG
	 */
	private final String TAG = this.getClass().getSimpleName();

	protected Context mContext;
	protected AppContext mAppContext;

	public SweetAlertDialog mAlert;

	protected String mReqToken;
	protected String mCurrentUser;
	protected String mCurrentUserPhone;

	protected Thread mLoadingThread;
	protected Runnable mLoadingRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SystemBarTintManager sysbarmgr = new SystemBarTintManager(this);
		sysbarmgr.setImmersionStatus(R.color.player_control_bg);
		AppManager.getAppManager().addActivity(this);
		mContext = this;
		mAppContext = (AppContext) getApplication();
		this.initParamData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.initParamData();
	}

	private void initParamData() {
		mReqToken = (String) mAppContext.getMemCache(MK_REQTOKEN);
		if (TextUtils.isEmpty(mReqToken))
			mReqToken = "";
		mCurrentUser = (String) mAppContext.getMemCache(MK_CURRENT_USER);
		if (TextUtils.isEmpty(mCurrentUser))
			mCurrentUser = "";
		mCurrentUserPhone = (String) mAppContext.getMemCache(MK_CURRENT_USER_PHONE);
		if (TextUtils.isEmpty(mCurrentUserPhone))
			mCurrentUserPhone = "";
	}

	@Override
	protected void onStop() {
		super.onStop();

		try {
			if (null != mLoadingThread) {
				mLoadingThread.interrupt();
				mLoadingRunnable = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		try {
			super.onRestoreInstanceState(savedInstanceState);
		} catch (Exception e) {
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().removeActivity(this);
	}

	// protected abstract Result loadData(String url, Map<String, String>
	// params, Map<String, String> files);
	//
	// protected abstract boolean handleMsg(android.os.Message msg);
	//
	// @SuppressLint("HandlerLeak")
	// protected void loadData(final String url, final Map<String, String>
	// params, final Map<String, String> files, String loadingMsg) {
	//
	// mAlert = showLoadingAlert(loadingMsg);
	//
	// final Handler handler = new Handler() {
	// public void handleMessage(android.os.Message msg) {
	//
	// hideAlert();
	//
	// if (null == msg)
	// return;
	//
	// try {
	//
	// if (!handleMsg(msg))
	// alertMsg(msg);
	//
	// } catch (NullPointerException e) {
	// } catch (ClassCastException e) {
	// }
	//
	// };
	// };
	//
	// mLoadingRunnable = new Runnable() {
	// @Override
	// public void run() {
	//
	// Message msg = Message.obtain();
	//
	// try {
	//
	// Result result = loadData(url, params, files);
	//
	// if (null == result) {
	// msg.what = Constants.MSG_NULL;
	// } else {
	// if (result.getRetcode() == Constants.RESULT_SUCCESSS) {
	// msg.obj = result;
	// msg.what = Constants.MSG_SUCCESS;
	// } else {
	// msg.obj = result;
	// msg.what = Constants.MSG_FAIL;
	// }
	// }
	//
	// } catch (Exception e) {
	// msg.what = Constants.MSG_EXCEPTION;
	// msg.obj = e;
	// }
	//
	// handler.sendMessage(msg);
	//
	// }
	// };
	// mLoadingThread = new Thread(mLoadingRunnable);
	// mLoadingThread.start();
	//
	// }

	public SweetAlertDialog showLoadingAlert(String msg) {
		SweetAlertDialog alert = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		alert.setTitleText(msg);
		alert.setCanceledOnTouchOutside(false);
		alert.setCancelable(true);
		alert.show();
		return alert;
	}

	public void hideAlert() {
		hideAlert(mAlert);
	}

	public void hideAlert(SweetAlertDialog alert) {
		if (null == alert || !alert.isShowing())
			return;
		alert.dismissWithAnimation();
	}

	public int getMsgFlag(BaseResult result) {

		int msgFlag = Integer.MIN_VALUE;

		if (result != null) {
			if (result.getRetcode() == BaseResult.CODE_SUCCESSS) {
				// mAppContext.setMemCache(MK_REQTOKEN, result.getRettoken());
				msgFlag = Constants.MSG_SUCCESS;
			} else if (result.getRetcode() == BaseResult.CODE_INVALID) {
				msgFlag = Constants.MSG_INVALID;
			} else if (result.getRetcode() == BaseResult.CODE_FAIL) {
				msgFlag = Constants.MSG_FAIL;
			} else {
				msgFlag = Constants.MSG_FAIL;
			}
		} else {
			msgFlag = Constants.MSG_NULL;
		}

		return msgFlag;

	}

	public void alertMsg(android.os.Message msg) {
		alertMsg(msg.what, msg.obj);
	}

	public void alertMsg(int msgFlag, Object obj) {
		alertMsg(mAlert, msgFlag, obj);
	}

	public void alertMsg(SweetAlertDialog alert, int msgFlag, Object obj) {

		try {

			hideAlert(alert);

			String retmsg = (null != obj && obj instanceof BaseResult) ? TextUtils.isEmpty(((BaseResult) obj).getRetmsg()) ? "" : ((BaseResult) obj)
					.getRetmsg() : "";
			switch (msgFlag) {
			case Constants.MSG_SUCCESS:
				new SweetAlertDialog(mContext, SweetAlertDialog.SUCCESS_TYPE).setTitleText(TextUtils.isEmpty(retmsg) ? "操作成功" : retmsg).show();
				break;
			case Constants.MSG_INVALID:
				new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE).setTitleText(TextUtils.isEmpty(retmsg) ? "TOKEN失效，请重新登录" : retmsg)
						.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
							@Override
							public void onClick(SweetAlertDialog sweetAlertDialog) {
								sweetAlertDialog.dismissWithAnimation();

								// Intent intent = new Intent(mContext,
								// Login.class);
								// intent.putExtra(EK_DEFAULT,
								// Login.LOGIN_FROM_INVALID);
								// startActivity(intent);

							}
						}).show();
				break;
			case Constants.MSG_FAIL:
				new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE).setTitleText(TextUtils.isEmpty(retmsg) ? "操作失败" : retmsg).show();
				break;
			case Constants.MSG_NULL:
				new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE).setTitleText(TextUtils.isEmpty(retmsg) ? "未找到对应数据" : retmsg).show();
				break;
			case Constants.MSG_EXCEPTION:
				new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE).setTitleText(TextUtils.isEmpty(retmsg) ? "连接错误" : retmsg).show();
				break;
			default:
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// protected RequestParams createRequestParams(Map<String, String>
	// mapParams) {
	// return createRequestParams(mapParams, null, null);
	// }
	//
	// protected RequestParams createRequestParams(Map<String, String>
	// mapParams, Map<String, String> mapFiles) {
	// return createRequestParams(mapParams, mapFiles, null);
	// }

	protected RequestParams createRequestParams(Map<String, String> mapParams, Map<String, String> mapFiles, Map<String, List<String>> mapArrayFiles) {
		RequestParams requestParams = new RequestParams();
		try {
			if (mapParams != null) {
				for (Map.Entry<String, String> entry : mapParams.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value))
						continue;
					requestParams.put(key, value);
				}
			}
			if (mapFiles != null) {
				for (Map.Entry<String, String> entry : mapFiles.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value))
						continue;
					File file = new File(value);
					if (file.exists())
						requestParams.put(key, file);
				}
			}
			if (mapArrayFiles != null) {
				for (Map.Entry<String, List<String>> entry : mapArrayFiles.entrySet()) {
					String key = entry.getKey();
					List<String> value = entry.getValue();
					if (TextUtils.isEmpty(key) || null == value || 0 >= value.size())
						continue;
					List<File> fileList = new ArrayList<File>();
					for (String path : value) {
						File file = new File(path);
						if (file.exists())
							fileList.add(file);
					}
					if (0 < fileList.size()) {
						File[] files = new File[fileList.size()];
						fileList.toArray(files);
						requestParams.put(key, files);
					}
				}
			}
			// File[] files = { new
			// File(Environment.getExternalStorageDirectory() +
			// File.separator + "st_museum.apk"),
			// new File(Environment.getExternalStorageDirectory() +
			// File.separator +
			// "汽车监管20150727.apk"),
			// new File(Environment.getExternalStorageDirectory() +
			// File.separator +
			// "manifestcustomer.apk"),
			// new File(Environment.getExternalStorageDirectory() +
			// File.separator +
			// "manifestcustomer.apk") };
			// requestParams.put("aaa", files);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return requestParams;
	}

	protected Map<String, String> formatToStrParam(String reqType, Map<String, String> mapStrParam) {
		String strParam = "";
		if (mapStrParam != null) {
			try {
				strParam = new Gson().toJson(mapStrParam);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return formatToStrParam(reqType, strParam);
	}

	protected Map<String, String> formatToStrParam(String reqType, List<Map<String, String>> listStrParam) {
		String strParam = "";
		if (listStrParam != null) {
			try {
				strParam = new Gson().toJson(listStrParam);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return formatToStrParam(reqType, strParam);
	}

	protected Map<String, String> formatToStrParam(String reqType, String strParam) {
		debug("StrParam = " + (TextUtils.isEmpty(strParam) ? "" : strParam));
		Map<String, String> mapParams = new ConcurrentHashMap<String, String>();
		mapParams.put("reqtoken", TextUtils.isEmpty(mReqToken) ? "" : mReqToken);
		mapParams.put("reqtype", TextUtils.isEmpty(reqType) ? "" : reqType);
		mapParams.put("reqparam", TextUtils.isEmpty(strParam) ? "" : strParam);
		return mapParams;
	}

	protected void req(Map<String, String> mapParams, Map<String, String> mapFiles, Map<String, List<String>> mapArrayFiles, String loadingMsg,
			boolean showProgress, OnNoNetwork onNoNetwork, final OnRequestSuccess onRequestSuccess) {
		this.req(Urls.HTTP_HOST_SCHEMA, mapParams, mapFiles, mapArrayFiles, loadingMsg, showProgress, onNoNetwork, onRequestSuccess);
	}

	protected void req(String url, Map<String, String> mapParams, String loadingMsg, boolean showProgress, OnNoNetwork onNoNetwork,
			final OnRequestSuccess onRequestSuccess) {
		this.req(url, mapParams, null, null, loadingMsg, showProgress, onNoNetwork, onRequestSuccess);
	}

	protected void req(String url, Map<String, String> mapParams, Map<String, String> mapFiles, String loadingMsg, boolean showProgress,
			OnNoNetwork onNoNetwork, final OnRequestSuccess onRequestSuccess) {
		this.req(url, mapParams, mapFiles, null, loadingMsg, showProgress, onNoNetwork, onRequestSuccess);
	}

	protected void req(String url, Map<String, String> mapParams, Map<String, String> mapFiles, Map<String, List<String>> mapArrayFiles, String loadingMsg,
			boolean showProgress, OnNoNetwork onNoNetwork, final OnRequestSuccess onRequestSuccess) {
		RequestParams params = this.createRequestParams(mapParams, mapFiles, mapArrayFiles);
		debug("RequestParams = " + params);
		this.reqServer(url, null, params, null, loadingMsg, showProgress, onNoNetwork, onRequestSuccess);
	}

	protected void reqServer(String url, Header[] headers, RequestParams params, String contentType, final String loadingMsg, final boolean showProgress,
			OnNoNetwork onNoNetwork, final OnRequestSuccess onRequestSuccess) {

		debug("AsyncHttpClient url = " + url);

		if (!mAppContext.isNetworkConnected()) {
			if (null != onNoNetwork) {
				onNoNetwork.noNetwork();
				return;
			}
			new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE).setTitleText("无网络，请检查网络连接设置").show();
			return;
		}

		Urls.mAsyncHttpClient.post(mContext, url, headers, params, contentType, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				debug("AsyncHttpClient onStart");
				mAlert = showLoadingAlert(loadingMsg);
			}

			@Override
			public void onFinish() {
				debug("AsyncHttpClient onFinish");
				// something was wrong alert will hide on next code anywhere.
				// hideAlert(mAlert);
			}

			@Override
			public void onPreProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {
				super.onPreProcessResponse(instance, response);
				debug("AsyncHttpClient onPreProcessResponse");
			}

			@Override
			public void onPostProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {
				super.onPostProcessResponse(instance, response);
				debug("AsyncHttpClient onPostProcessResponse");
			}

			@Override
			public void onRetry(int retryNo) {
				super.onRetry(retryNo);

				if (null == mAlert || !mAlert.isShowing()) {
					mAlert.getProgressHelper().setProgress(0.0f);
					mAlert.getProgressHelper().setInstantProgress(0.0f);
					mAlert.getProgressHelper().spin();
				}
			}

			@Override
			public void onCancel() {
				super.onCancel();
				hideAlert(mAlert);
			}

			@Override
			public void onUserException(Throwable error) {
				// super.onUserException(error);
				debug("AsyncHttpClient onUserException " + error);
				error.printStackTrace();
				hideAlert(mAlert);
				new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE).setTitleText("程序错误，请稍后重试").show();
			}

			@Override
			public void onProgress(long bytesWritten, long totalSize) {
				super.onProgress(bytesWritten, totalSize);

				if (!showProgress || null == mAlert || !mAlert.isShowing())
					return;

				if (0 >= totalSize || bytesWritten > totalSize)
					return;

				float progress = (int) ((totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : -1);
				debug("AsyncHttpClient  progress = " + progress);

				mAlert.getProgressHelper().setProgress(progress / 100.0f);
				mAlert.getProgressHelper().setInstantProgress(progress / 100.0f);

			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				debug("AsyncHttpClient onSuccess \nstatusCode = " + statusCode + "\nheaders = " + headers + "\nresponseBody = " + responseBody);
				if (null == onRequestSuccess) {
					hideAlert(mAlert);
					return;
				}

				onRequestSuccess.onSuccess(mAlert, statusCode, headers, responseBody);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				debug("AsyncHttpClient onFailure statusCode = " + statusCode + "\nheaders = " + headers + "\nresponseBody = " + responseBody + "\nerror = "
						+ error);
				hideAlert(mAlert);

				new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE).setTitleText("连接失败，错误代码:" + statusCode).show();
			}
		});

	}

	public interface OnNoNetwork {
		public void noNetwork();
	}

	public interface OnRequestSuccess {
		public void onSuccess(SweetAlertDialog alert, int statusCode, Header[] headers, byte[] responseBody);
	}

	@SuppressLint("DefaultLocale")
	protected String createSetMethodName(String fieldName) {
		if (TextUtils.isEmpty(fieldName))
			return "";

		try {
			return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	protected Method getItemMethod(Class<?> item, String name, Class<?>... parameterTypes) {
		try {
			return item.getDeclaredMethod(name, parameterTypes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected boolean invokeItemMethod(Object receiver, Object... args) {
		return invokeItemMethod(mCurrentItemMethod, receiver, args);
	}

	protected boolean invokeItemMethod(Method method, Object receiver, Object... args) {
		try {
			method.setAccessible(true);
			method.invoke(receiver, args);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 用于调试logcat打印
	 * 
	 * @param msg
	 *            要打印的信息字符串
	 */
	protected void debug(String msg) {
//		if (AppConfig.DEBUG && null != msg)
//			Log.e(TAG, msg);
	}

	public int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

}
