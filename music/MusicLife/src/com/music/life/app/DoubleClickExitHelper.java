package com.music.life.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.Toast;

import com.cmsc.cmmusic.init.InitCmmInterface;
import com.music.life.app.R;
import com.music.service.MediaService;

/**
 * 双击退出 创建日期 2014-05-12
 * 
 * @author 火蚁 (http://my.oschina.net/LittleDY)
 * 
 */
public class DoubleClickExitHelper {

	private final Activity mActivity;

	private boolean isOnKeyBacking;
	private Handler mHandler;
	private Toast mBackToast;

	public DoubleClickExitHelper(Activity activity) {
		mActivity = activity;
		mHandler = new Handler(Looper.getMainLooper());
	}

	/**
	 * Activity onKeyDown事件
	 * */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			return false;
		}
		if (isOnKeyBacking) {
			mHandler.removeCallbacks(onBackTimeRunnable);
			InitCmmInterface.exitApp();
			if (mBackToast != null) {
				mBackToast.cancel();
			}
			mActivity.stopService(new Intent(mActivity, MediaService.class));
			//应用确认退出前，务必调用此接口完成SDK的资源及内存释放，否则下次启动运行结果不可预期。
			InitCmmInterface.exitApp();
			// 退出
			AppManager.getAppManager().AppExit(mActivity);
			return true;
		} else {
			isOnKeyBacking = true;
			if (mBackToast == null) {
				mBackToast = Toast.makeText(mActivity, R.string.back_exit_tips, 2000);
			}
			mBackToast.show();
			mHandler.postDelayed(onBackTimeRunnable, 2000);
			return true;
		}
	}

	private Runnable onBackTimeRunnable = new Runnable() {

		@Override
		public void run() {
			isOnKeyBacking = false;
			if (mBackToast != null) {
				mBackToast.cancel();
			}
		}
	};
}
