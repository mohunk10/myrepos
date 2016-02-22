package com.music.constant;

import com.loopj.android.http.AsyncHttpClient;

public class Urls {

	public static AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient() {
		{
			setURLEncodingEnabled(true);
			setConnectTimeout(1000 * 3);
			setMaxRetriesAndTimeout(3, 1000 * 3);
			// setResponseTimeout(1000 * 3);
			setURLEncodingEnabled(true);
		}
	};

	// 乐动生活
	public static final String APP_NO = "8a7ca0bc4f8d3599014f8d38d0d20002";
	public static final String HTTP_HOST = "http://182.92.237.7/";
	public static final String HTTP_HOST_SCHEMA = HTTP_HOST + "musicplat/";

	public static final String URL_LOGIN = HTTP_HOST_SCHEMA + "user/login";
	public static final String URL_REGISTER = HTTP_HOST_SCHEMA + "user/reg";
	public static final String URL_BANNER_LIST = HTTP_HOST_SCHEMA + "slide/loadSlideList";
	public static final String URL_MUSIC_HALL_LIST = HTTP_HOST_SCHEMA + "topic/loadTopicListBy";
	public static final String URL_NET_MUSIC_LIST = HTTP_HOST_SCHEMA + "music/loadMusicList";

	public static final String APP_UPDATE_URL = HTTP_HOST_SCHEMA + "";

	public static final String APP_CRASH_REPORT_URL = HTTP_HOST_SCHEMA + "app/genCrashReport?crashReport=";

}
