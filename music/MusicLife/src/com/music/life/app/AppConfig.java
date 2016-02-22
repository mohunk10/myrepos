package com.music.life.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AppConfig {

	public static boolean DEBUG = true;

	private final static String APP_CONFIG = "config";
	private Context mContext;
	private static AppConfig appConfig;
	private SharedPreferences sp;

	private AppConfig() {
	}

	public static AppConfig getAppConfig(Context context) {
		if (appConfig == null) {
			appConfig = new AppConfig();
			appConfig.mContext = context;
		}
		return appConfig;
	}

	/**
	 * 获取Preference设置
	 */
	public SharedPreferences getSharedPreferences(Context context) {
		if (sp == null) {
			sp = PreferenceManager.getDefaultSharedPreferences(context);
		}
		return sp;
	}

	// ---------------
	public String getString(String key, String defultValue) {
		return sp.getString(key, defultValue);
	}

	public Boolean getBoolean(String key, Boolean defultValue) {
		return sp.getBoolean(key, defultValue);
	}

	public int getInteger(String key, int defultValue) {
		return sp.getInt(key, defultValue);
	}

	public long getLong(String key, long defultValue) {
		return sp.getLong(key, defultValue);
	}

	public float getFloat(String key, float defultValue) {
		return sp.getFloat(key, defultValue);
	}

	public void setValue(String key, Object value) {
		Editor edit = sp.edit();
		if (value instanceof Boolean) {
			edit.putBoolean(key, (Boolean) value);
		} else if (value instanceof Integer || value instanceof Byte) {
			edit.putInt(key, (Integer) value);
		} else if (value instanceof Long) {
			edit.putLong(key, (Long) value);
		} else if (value instanceof Float) {
			edit.putFloat(key, (Float) value);
		} else if (value instanceof String) {
			edit.putString(key, (String) value);
		} else {
			edit.putString(key, value.toString());
		}
		edit.commit();
	}

	public String get(String key) {
		Properties props = get();
		return (props != null) ? props.getProperty(key) : null;
	}

	public void set(String key, String value) {
		Properties props = get();
		props.setProperty(key, value);
		setProps(props);
	}

	public void set(Properties ps) {
		Properties props = get();
		props.putAll(ps);
		setProps(props);
	}

	public void remove(String... key) {
		Properties props = get();
		for (String k : key)
			props.remove(k);
		setProps(props);
	}

	public Properties get() {
		FileInputStream fis = null;
		Properties props = new Properties();
		try {
			// 读取files目录下的config
			// fis = activity.openFileInput(APP_CONFIG);

			// 读取app_config目录下的config
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			fis = new FileInputStream(dirConf.getPath() + File.separator + APP_CONFIG);

			props.load(fis);
		} catch (Exception e) {
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return props;
	}

	private void setProps(Properties p) {
		FileOutputStream fos = null;
		try {
			// 把config建在files目录下
			// fos = activity.openFileOutput(APP_CONFIG, Context.MODE_PRIVATE);

			// 把config建在(自定义)app_config的目录下
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			File conf = new File(dirConf, APP_CONFIG);
			fos = new FileOutputStream(conf);

			p.store(fos, null);
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

}
