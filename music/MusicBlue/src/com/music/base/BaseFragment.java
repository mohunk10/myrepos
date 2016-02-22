package com.music.base;

import java.lang.reflect.Method;
import java.util.Map;

import com.music.blue.app.AppContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class BaseFragment extends Fragment {
	protected String TAG = this.getClass().getSimpleName();

	public static final String EK_DEFAULT = BaseActivity.EK_DEFAULT;
	public static final String SK_DEFAULT = BaseActivity.SK_DEFAULT;
	public static final String MK_REQTOKEN = BaseActivity.MK_REQTOKEN;
	public static final String MK_CURRENT_USER = BaseActivity.MK_CURRENT_USER;
	public static final String MK_CURRENT_USER_PHONE = BaseActivity.MK_CURRENT_USER_PHONE;
	public final static String PK_USER = BaseActivity.PK_USER;

	public static final String PPK_DEFAULT = BaseActivity.PPK_DEFAULT;
	
	public SweetAlertDialog mAlert;

	protected Map<String, String> mPublicParams;
	protected Method mCurrentItemMethod;

	protected String mReqToken;
	protected String mCurrentUser;
	protected String mCurrentUserPhone;

	protected boolean isDataLoading;

	private boolean isVisibleToUser = true;
	private boolean isResume;
	private boolean isFirstRun = true;

	protected Context mContext;
	protected AppContext mAppContext;
	protected BaseActivity mBaseActivity;

	protected View mContentView;

	protected Thread mLoadingThread;
	protected Runnable mLoadingRunnable;

	public BaseFragment() {
	}

	public BaseFragment(int debugTagIndex) {
		TAG = this.getClass().getSimpleName() + debugTagIndex;
	}

	protected void firstVisibleToUser() {
	}

	protected void visibleToUser() {
		this.initParamData();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		debug("Fragment onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug("Fragment onCreate savedInstanceState == null ? " + (savedInstanceState == null));

		mContext = getActivity();
		mAppContext = (AppContext) getActivity().getApplication();
		mBaseActivity = (BaseActivity) getActivity();
		mAlert = mBaseActivity.mAlert;

		mPublicParams = mBaseActivity.getPublicParams();
		mCurrentItemMethod = mBaseActivity.getCurrentItemMethod();

		this.initParamData();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		debug("Fragment onCreateView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		debug("Fragment onActivityCreated");
	}

	@Override
	public void onStart() {
		super.onStart();
		debug("Fragment onStart");
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		debug("Fragment setUserVisibleHint isVisibleToUser = " + isVisibleToUser);

		this.isVisibleToUser = isVisibleToUser;
		if (isFirstRun && isResume && isVisibleToUser) {
			isFirstRun = false;
			debug("Begin firstLoading");
			this.firstVisibleToUser();
		} else if (isVisibleToUser) {
			visibleToUser();
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		debug("Fragment onResume");

		// this.initParamData();

		isResume = true;
		if (isFirstRun && isVisibleToUser) {
			isFirstRun = false;
			debug("Begin firstLoading");
			this.firstVisibleToUser();
		} else if (isVisibleToUser) {
			visibleToUser();
		}

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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		debug("Fragment onSaveInstanceState");
	}

	@Override
	public void onPause() {
		super.onPause();
		debug("Fragment onPause");
		isResume = false;
	}

	@Override
	public void onStop() {
		super.onStop();
		debug("Fragment onStop");

		isResume = false;

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
	public void onDestroyView() {
		super.onDestroyView();
		debug("Fragment onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		debug("Fragment onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		debug("Fragment onDetach");
	}

	public SweetAlertDialog showLoadingAlert(String msg) {
		return mBaseActivity.showLoadingAlert(msg);
	}

	public void hideAlert(SweetAlertDialog alert) {
		mBaseActivity.hideAlert(alert);
	}

	public void alertMsg(android.os.Message msg) {
		mBaseActivity.alertMsg(msg);
	}

	@SuppressLint("DefaultLocale")
	protected String createSetMethodName(String fieldName) {
		return mBaseActivity.createSetMethodName(fieldName);
	}

	protected Method getItemMethod(Class<?> item, String name, Class<?>... parameterTypes) {
		return mBaseActivity.getItemMethod(item, name, parameterTypes);
	}

	protected boolean invokeItemMethod(Object receiver, Object... args) {
		return mBaseActivity.invokeItemMethod(mCurrentItemMethod, receiver, args);
	}

	protected boolean invokeItemMethod(Method method, Object receiver, Object... args) {
		return mBaseActivity.invokeItemMethod(method, receiver, args);
	}

	/**
	 * 用于调试logcat打印
	 * 
	 * @param msg
	 *            要打印的信息字符串
	 */
	protected void debug(String msg) {
		// if (AppConfig.DEBUG)
		// Log.e(TAG, msg);
	}

	public int dp2px(int dp) {
		return mBaseActivity.dp2px(dp);
	}
}
