package com.music.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.cmsc.cmmusic.common.CMMusicCallback;
import com.cmsc.cmmusic.common.FullSongManagerInterface;
import com.cmsc.cmmusic.common.PaymentManagerInterface;
import com.cmsc.cmmusic.common.RingbackManagerInterface;
import com.cmsc.cmmusic.common.VibrateRingManagerInterface;
import com.cmsc.cmmusic.common.data.CrbtOpenCheckRsp;
import com.cmsc.cmmusic.common.data.DownloadResult;
import com.cmsc.cmmusic.common.data.Result;
import com.music.bean.MusicInfoSer;
import com.music.life.app.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;

public class AlertList extends Dialog {

	private TextView tv_title;
	private ListView lv_listview;
	private TextView tv_cancel;
	private View v_view;

	private List<String> mListData = new ArrayList<String>();
	private MyListAdapter mListAdapter = new MyListAdapter();

	public AlertList(Context context) {
		super(context, R.style.alert_dialog);
		this.mContext = context;

		setContentView(R.layout.alert_list_picker);
		LayoutParams lp = getWindow().getAttributes();
		lp.width = LayoutParams.MATCH_PARENT;
		lp.height = LayoutParams.MATCH_PARENT;
		lp.gravity = Gravity.BOTTOM;
		getWindow().setAttributes(lp);
		this.initView();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	public AlertList setTitleText(CharSequence text) {
		tv_title.setText(text);
		return this;
	}

	public AlertList setList(List<String> list, MusicInfoSer musicInfo) {
		this.musicInfo = musicInfo;
		mListData.addAll(list);
		mListAdapter.notifyDataSetChanged();
		return this;
	}

	public AlertList setOnItemClickListener(OnItemClickListener listener) {
		lv_listview.setOnItemClickListener(listener);
		return this;
	}

	private void initView() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		lv_listview = (ListView) findViewById(R.id.lv_listview);
		tv_cancel = (TextView) findViewById(R.id.tv_cancel);
		v_view = findViewById(R.id.v_view);

		lv_listview.setAdapter(mListAdapter);
		lv_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				String name = mListData.get(position);
				String musicId = musicInfo.getMusicId();
				final String destmusicId = musicId;
				String phone = getPhoneNumber();
				if (phone.indexOf("+86") > -1) {
					phone = phone.substring(3);
				}
				final String destphone = phone;

				final Handler h = new Handler() {
					public void handleMessage(android.os.Message msg) {
						switch (msg.what) {
						case 1:
							CrbtOpenCheckRsp result = (CrbtOpenCheckRsp) msg.obj;
							if (!"000000".equals(result.getResCode())) {

								mDialog.setTitleText(result.getResMsg()).setContentText(result.getDescription())
										.setConfirmText("立即开通并订购").setConfirmClickListener(new OnSweetClickListener() {

									@Override
									public void onClick(SweetAlertDialog sweetAlertDialog) {
										// 开通彩铃功能并订购单首彩铃。
										RingbackManagerInterface.buyRingBackByOpenRingBack(mContext, destmusicId,
												new CMMusicCallback<Result>() {
											@Override
											public void operationResult(Result result) {
												mDialog.dismiss();
												if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
													new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
															.setTitleText(result.getResMsg()).show();
												}
											}
										});

									}
								}).showCancelButton(true).changeAlertType(SweetAlertDialog.NORMAL_TYPE);
								return;
							}
							mDialog.dismiss();
							RingbackManagerInterface.buyRingBack(mContext, destmusicId, new CMMusicCallback<Result>() {
								@Override
								public void operationResult(Result result) {
									if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
										new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
												.setTitleText(result.getResMsg()).show();
									}
								}
							});
							break;

						case 2:
							CrbtOpenCheckRsp result_ = (CrbtOpenCheckRsp) msg.obj;
							if (!"000000".equals(result_.getResCode())) {
								mDialog.setTitleText(result_.getResMsg()).setContentText(result_.getDescription())
										.setConfirmText("立即开通").setConfirmClickListener(new OnSweetClickListener() {

									@Override
									public void onClick(SweetAlertDialog sweetAlertDialog) {
										RingbackManagerInterface.openRingback(mContext, new CMMusicCallback<Result>() {
											@Override
											public void operationResult(Result result) {
												mDialog.dismiss();
												if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
													new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
															.setTitleText(result.getResMsg()).show();
												}
											}
										});
									}
								}).showCancelButton(true).changeAlertType(SweetAlertDialog.NORMAL_TYPE);
								return;
							}
							mDialog.dismiss();
							RingbackManagerInterface.giveRingBack(mContext, destmusicId, new CMMusicCallback<Result>() {

								@Override
								public void operationResult(Result result) {
									if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
										new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
												.setTitleText(result.getResMsg()).show();
									}
								}

							});
							break;
						}

					};
				};
				if ("彩铃订购".equals(name)) {

					// 根据手机号查询用户是否开通了彩铃。
					new Thread(new Runnable() {

						@Override
						public void run() {
							Message msg = Message.obtain();
							CrbtOpenCheckRsp result = RingbackManagerInterface.crbtOpenCheck(mContext, destphone);
							msg.what = 1;
							msg.obj = result;
							h.sendMessage(msg);
						}
					}).start();
					mDialog = new SweetAlertDialog(mContext, SweetAlertDialog.PROGRESS_TYPE).setTitleText("正在处理业务");
					mDialog.show();

					// 实现订购单首彩铃。

				} else if ("彩铃赠送".equals(name)) {
					// 根据手机号查询用户是否开通了彩铃。
					new Thread(new Runnable() {

						@Override
						public void run() {
							Message msg = Message.obtain();
							CrbtOpenCheckRsp result = RingbackManagerInterface.crbtOpenCheck(mContext, destphone);
							msg.what = 2;
							msg.obj = result;
							h.sendMessage(msg);
						}
					}).start();
					mDialog = new SweetAlertDialog(mContext, SweetAlertDialog.PROGRESS_TYPE).setTitleText("正在处理业务");
					mDialog.show();

				} else if ("振铃订购".equals(name)) {
					VibrateRingManagerInterface.queryVibrateRingDownloadUrl(mContext, musicId,
							new CMMusicCallback<DownloadResult>() {

						@Override
						public void operationResult(DownloadResult result) {
							if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
								new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
										.setTitleText(result.getResMsg()).show();
							}
						}
					});
				} else if ("振铃赠送".equals(name)) {
					mAlertDialog = showParameterDialog("请输入赠送手机号", new ParameterCallback() {
						@Override
						public void callback(EditText edt) {
							String receivemdn = edt.getText().toString();
							if (TextUtils.isEmpty(receivemdn) || !iscmcmphone(receivemdn)) {
								try {
									Field field = mAlertDialog.getClass().getSuperclass().getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(mAlertDialog, false);
									edt.setError("请输入正确的手机号码");
								} catch (Exception e) {
									e.printStackTrace();
								}
								return;
							}
							try {
								Field field = mAlertDialog.getClass().getSuperclass().getDeclaredField("mShowing");
								field.setAccessible(true);
								field.set(mAlertDialog, true);
							} catch (Exception e) {
								e.printStackTrace();
							}
							VibrateRingManagerInterface.giveVibrateRing(mContext, receivemdn, destmusicId,
									new CMMusicCallback<Result>() {

								@Override
								public void operationResult(Result result) {
									if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
										new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
												.setTitleText(result.getResMsg()).show();
									}
								}

							});
						}
					});

				} else if ("全曲下载".equals(name)) {
					FullSongManagerInterface.getFullSongDownloadUrl(mContext, musicId,
							new CMMusicCallback<DownloadResult>() {

						@Override
						public void operationResult(DownloadResult result) {
							if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
								if (!"000000".equals(result.getResCode())) {
									new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
											.setTitleText(result.getResMsg()).show();
									return;
								}
								String downUrl = result.getDownUrl();
								View _view = View.inflate(mContext, R.layout.activity_cricle_progress, null);
								final RoundProgressBar pbar = (RoundProgressBar) _view
										.findViewById(R.id.roundProgressBar);
								new AlertDialog.Builder(mContext).setTitle(musicInfo.getSongName()).setView(_view)
										.show();
								downloadsong(downUrl, new DownloadProgressListener() {
									@Override
									public void onFileLen(int len) {
										pbar.setMax(len);
									}

									@Override
									public void onDownloadSize(int size) {
										pbar.setProgress(size);
									}

								});
							}
						}

					});
				} else if ("全曲赠送".equals(name)) {
					mAlertDialog = showParameterDialog("请输入赠送手机号", new ParameterCallback() {
						@Override
						public void callback(EditText edt) {
							String receivemdn = edt.getText().toString();
							if (TextUtils.isEmpty(receivemdn) || !iscmcmphone(receivemdn)) {
								try {
									Field field = mAlertDialog.getClass().getSuperclass().getDeclaredField("mShowing");
									field.setAccessible(true);
									field.set(mAlertDialog, false);
									edt.setError("请输入正确的手机号码");
								} catch (Exception e) {
									e.printStackTrace();
								}
								return;
							}
							try {
								Field field = mAlertDialog.getClass().getSuperclass().getDeclaredField("mShowing");
								field.setAccessible(true);
								field.set(mAlertDialog, true);
							} catch (Exception e) {
								e.printStackTrace();
							}
							FullSongManagerInterface.giveFullSong(mContext, receivemdn, destmusicId,
									new CMMusicCallback<Result>() {

								@Override
								public void operationResult(Result result) {
									if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
										new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
												.setTitleText(result.getResMsg()).show();
									}
								}

							});
						}
					});

				} else if ("3元包月(20首)".equals(name)) {
					PaymentManagerInterface.getOrderOpenResult(mContext, destphone, "0", "1",
							new CMMusicCallback<Result>() {

						@Override
						public void operationResult(Result result) {
							if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
								new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
										.setTitleText(result.getResMsg()).show();
							}
						}

					});
				} else if ("5元包月(50首)".equals(name)) {
					PaymentManagerInterface.getOrderOpenResult(mContext, destphone, "1", "1",
							new CMMusicCallback<Result>() {

						@Override
						public void operationResult(Result result) {
							if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
								new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
										.setTitleText(result.getResMsg()).show();
							}
						}

					});
				} else if ("10元包月(200首)".equals(name)) {
					PaymentManagerInterface.getOrderOpenResult(mContext, destphone, "2", "1",
							new CMMusicCallback<Result>() {

						@Override
						public void operationResult(Result result) {
							if (null != result && !TextUtils.isEmpty(result.getResMsg())) {
								new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE)
										.setTitleText(result.getResMsg()).show();
							}
						}

					});
				}
				dismiss();
			}
		});

		tv_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		v_view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

	}

	private class MyListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mListData.size();
		}

		@Override
		public Object getItem(int position) {
			try {
				return mListData.get(position);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("ResourceAsColor")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView mTextView = null;
			if (null == convertView) {
				mTextView = new TextView(getContext());
				mTextView.setPadding(20, 20, 20, 20);
				mTextView.setGravity(Gravity.CENTER);
				mTextView.setTextColor(getContext().getResources().getColor(R.color.white));
				mTextView.setTextSize(18);
				convertView = mTextView;
			} else {
				mTextView = (TextView) convertView;
			}

			String item = mListData.get(position);

			mTextView.setText(item);

			return convertView;
		}

	}

	// 获取本机手机号
	public String getPhoneNumber() {
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}

	public AlertDialog showParameterDialog(String title, final ParameterCallback callback) {
		View view = View.inflate(mContext, R.layout.parameter_dialog, null);
		final EditText edt = (EditText) view.findViewById(R.id.editText1);
		return new AlertDialog.Builder(mContext).setTitle(title).setView(view)
				.setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
							field.setAccessible(true);
							field.set(dialog, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).setPositiveButton("确认", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (callback != null) {
							callback.callback(edt);
						}
					}
				}).show();
	}

	interface ParameterCallback {
		void callback(EditText edt);
	}

	interface DownloadProgressListener {
		void onDownloadSize(int size);

		void onFileLen(int len);
	}

	boolean iscmcmphone(String phone) {
		Pattern p = Pattern.compile(PATTERN_CMCMOBILENUM);
		return p.matcher(phone).matches();
	}

	private void downloadsong(final String downloadUrl, final DownloadProgressListener listener) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					URL url = new URL(downloadUrl);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					InputStream inStream = conn.getInputStream();
					File dir = new File(Environment.getExternalStorageDirectory() + "/music");
					if (!dir.exists()) {
						dir.mkdir();
					}
					File file = new File(dir, musicInfo.getSongName() + ".mp3");
					FileOutputStream outputStream = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					mFileLen = conn.getContentLength();
					listener.onFileLen(mFileLen);
					int downedLen = 0;
					int offset = -1;
					while ((offset = inStream.read(buffer)) != -1) {
						downedLen += offset;
						outputStream.write(buffer, 0, buffer.length);
						listener.onDownloadSize(downedLen);
					}
					outputStream.flush();
					outputStream.close();
					inStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private int mFileLen;
	private final Context mContext;
	private MusicInfoSer musicInfo;
	private SweetAlertDialog mDialog;
	private static final String TAG = "AlertList";
	public static String PATTERN_CMCMOBILENUM = "^1(3[4-9]|5[012789]|8[78])\\d{8}$";
	private AlertDialog mAlertDialog;
}
