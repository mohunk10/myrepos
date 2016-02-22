package com.music.life.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class AppClient {

	private final String TAG = "AppClient";
	private boolean debug = true;

	private String JSESSIONID = "";

	public static final String UTF_8 = "UTF-8";

	private final static int TIMEOUT_CONNECTION = 20 * 1000;
	private final static int TIMEOUT_SOCKET = 20 * 1000;
	private final static int RETRY_TIME = 3;

	private static AppClient mAppClient;

	private AppClient() {
	}

	public static AppClient getAppClient() {
		if (mAppClient == null)
			mAppClient = new AppClient();
		return mAppClient;
	}

	public String http_post(String url, List<NameValuePair> parameters) throws AppException {
		debug("http_post");
		debug("url = " + url);
		debug("parameters = " + parameters);

		HttpPost httpPost = null;
		HttpClient httpClient = null;

		int time = 0;
		do {
			try {

				httpPost = getHttpPost(url, parameters);
				httpClient = getHttpClient();
				HttpResponse httpResponse = httpClient.execute(httpPost);
				int code = httpResponse.getStatusLine().getStatusCode();
				if (code != HttpStatus.SC_OK) {
					debug("StatusCode = " + code);
					throw AppException.http(code);
				}
				HttpEntity entity = httpResponse.getEntity();
				String result = EntityUtils.toString(entity);

				CookieStore cookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
				List<Cookie> cookies = cookieStore.getCookies();
				for (Cookie cookie : cookies) {
					if ("JSESSIONID".equals(cookie.getName())) {
						this.JSESSIONID = cookie.getValue();
						debug("JSESSIONID = " + this.JSESSIONID);
						break;
					}
				}

				if (result != null && !"".equals(result)) {
					debug("DES result = " + result);
					// result = decryptDES(result);
					debug(result);
					return result;
				} else {
					return "";
				}

			} catch (Exception e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				e.printStackTrace();
				if (e instanceof HttpException)
					throw AppException.http(e);
				else if (e instanceof IOException)
					throw AppException.network(e);
				else
					throw AppException.run(e);
			} finally {
				httpPost = null;
				httpClient = null;
			}
		} while (time < RETRY_TIME);

		return "";
	}

	public String http_get(String url) throws AppException {
		debug("http_post");
		debug("url = " + url);

		HttpGet httpGet = null;
		HttpClient httpClient = null;

		int time = 0;
		do {
			try {

				httpGet = getHttpGet(url);
				httpClient = getHttpClient();
				HttpResponse httpResponse = httpClient.execute(httpGet);
				int code = httpResponse.getStatusLine().getStatusCode();
				if (code != HttpStatus.SC_OK) {
					debug("StatusCode = " + code);
					throw AppException.http(code);
				}
				HttpEntity entity = httpResponse.getEntity();
				String result = EntityUtils.toString(entity);

				CookieStore cookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
				List<Cookie> cookies = cookieStore.getCookies();
				for (Cookie cookie : cookies) {
					if ("JSESSIONID".equals(cookie.getName())) {
						this.JSESSIONID = cookie.getValue();
						debug("JSESSIONID = " + this.JSESSIONID);
						break;
					}
				}

				if (result != null && !"".equals(result)) {
					debug(result);
					return result;
				} else {
					return "";
				}

			} catch (Exception e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				e.printStackTrace();
				if (e instanceof HttpException)
					throw AppException.http(e);
				else if (e instanceof IOException)
					throw AppException.network(e);
				else
					throw AppException.run(e);
			} finally {
				httpGet = null;
				httpClient = null;
			}
		} while (time < RETRY_TIME);

		return "";
	}

	private HttpPost getHttpPost(String url, List<NameValuePair> parameters) throws Exception {
		debug("getHttpPost JSESSIONID = " + this.JSESSIONID);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Cookie", "JSESSIONID=" + this.JSESSIONID);
		httpPost.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, TIMEOUT_CONNECTION);
		httpPost.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, TIMEOUT_SOCKET);
		if (parameters != null && parameters.size() > 0) {
			// parameters = encryptDES(parameters);
			debug("DES parameters = " + parameters);
			try {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, UTF_8);
				httpPost.setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return httpPost;
	}

	private HttpGet getHttpGet(String url) {
		debug("getHttpGet JSESSIONID = " + this.JSESSIONID);
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Cookie", "JSESSIONID=" + this.JSESSIONID);
		httpGet.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, TIMEOUT_CONNECTION);
		httpGet.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, TIMEOUT_SOCKET);
		return httpGet;
	}

	private HttpClient getHttpClient() {
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, TIMEOUT_CONNECTION);
		httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, TIMEOUT_SOCKET);
		return httpClient;
	}

	private void debug(String msg) {
//		if (debug)
//			Log.e(TAG, msg);
	}

}
