package com.music.base;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.pedant.SweetAlert.ProgressWheel;

import com.music.blue.app.AppContext;
import com.music.blue.app.R;
import com.music.utilities.StringUtils;

@SuppressLint("NewApi")
public abstract class BaseFpdl<T> extends BaseFragment implements BaseFl<T> {

	protected ProgressBar mTitleBarProgress;

	protected ListView mLV;
	private View mLVfooter;
	private TextView mLVFootMore;
	private ProgressWheel mLVFootProgress;

	protected List<T> mLVData = new ArrayList<T>();
	protected PTRAdapter mLVAdapter = new PTRAdapter();
	protected Handler mLVHandler;

	private View mLoadingLayout;
	private View mEmptyLayout;

	private int mActionType = ACTION_INIT;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mContentView = inflater.inflate(R.layout.layout_listview, container, false);
		return mContentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.initView();
	}

	private void initView() {
		initListView();
		mLVHandler = getLVHandler(mLV, mLVAdapter, mLVFootMore, mLVFootProgress, AppContext.PAGE_SIZE);
	}

	@SuppressLint("HandlerLeak")
	private Handler getLVHandler(final ListView lv, final BaseAdapter adapter, final TextView more, final ProgressWheel progress, final int pageSize) {
		return new Handler() {
			public void handleMessage(Message msg) {
				isDataLoading = false;

				if (null == msg || !isAdded())
					return;

				if (msg.what >= 0) {

					handleListData(msg);
					adapter.notifyDataSetChanged();

					if (msg.what < pageSize) {
						lv.setTag(DATA_FULL);
						more.setText(R.string.load_full);
						adapter.notifyDataSetChanged();
					} else if (msg.what == pageSize) {
						lv.setTag(DATA_MORE);
						more.setText(R.string.load_more);
						adapter.notifyDataSetChanged();
					}

				} else if (msg.what == -1) {
					lv.setTag(DATA_MORE);
					more.setText(R.string.load_error);
				}

				if (adapter.getCount() == 0) {
					lv.setTag(DATA_EMPTY);
					more.setText(R.string.load_empty);
				}

				progress.setVisibility(ProgressBar.GONE);
				if (mTitleBarProgress != null)
					mTitleBarProgress.setVisibility(ProgressBar.GONE);

				// if (mActionType == ACTION_REFRESH) {
				// lv.onRefreshComplete(getActivity().getString(R.string.pull_to_refresh_update)
				// + new Date().toLocaleString());
				// lv.setSelection(0);
				// } else if (mActionType == ACTION_CHANGE_CATALOG) {
				// lv.onRefreshComplete();
				// lv.setSelection(0);
				// }

				if (mActionType != ACTION_SCROLL)
					mLV.smoothScrollToPosition(0);

				// change action type default
				mActionType = ACTION_INIT;

				if (adapter.getCount() == 0) {
					onLoadEmpty();
				} else {
					onLoadComplete();
				}

			}
		};
	}

	private void initListView() {

		// mLVAdapter = getAdapter();

		mLVfooter = getActivity().getLayoutInflater().inflate(R.layout.ptr_footer, null);
		mLVFootMore = (TextView) mLVfooter.findViewById(R.id.listview_foot_more);
		mLVFootProgress = (ProgressWheel) mLVfooter.findViewById(R.id.listview_foot_progress);

		mLV = (ListView) mContentView.findViewById(R.id.ptr_listview);
		mLV.setTag(DATA_MORE);
		mLV.addFooterView(mLVfooter);
		mLV.setAdapter(mLVAdapter);

		mLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if (view == mLVfooter)
					return;

				try {

					@SuppressWarnings("unchecked")
					T item = (T) ((ListView) parent).getItemAtPosition(position);

					onListItemClick(item, parent, view, position, id);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		mLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				if (view == mLVfooter)
					return false;

				try {

					@SuppressWarnings("unchecked")
					T item = (T) ((ListView) parent).getItemAtPosition(position);

					return onListItemLongClick(item, parent, view, position, id);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return false;
			}
		});

		mLV.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {

				debug("LV onscroll isDataLoading = " + isDataLoading);

				if (isDataLoading)
					return;

				// 数据为空--不用继续下面代码了
				if (mLVData.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(mLVfooter) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				debug("LV scrollend = " + scrollEnd);

				int lvDataState = StringUtils.toInt(mLV.getTag());

				debug("lvDataState = " + lvDataState + " " + (lvDataState == DATA_MORE));
				if (scrollEnd && lvDataState == DATA_MORE) {
					mLV.setTag(DATA_LOADING);
					mLVFootMore.setText(R.string.load_ing);
					mLVFootProgress.setVisibility(View.VISIBLE);
					if (null != mTitleBarProgress)
						mTitleBarProgress.setVisibility(View.VISIBLE);

					// 当前pageIndex
					int pageIndex = mLVData.size() / AppContext.PAGE_SIZE;
					debug("LV onscroll scrollend true");

					isDataLoading = true;

					mActionType = ACTION_SCROLL;
					loadListData(pageIndex, mLVHandler, mActionType);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

	}

	protected void loadListData(final int pageIndex, final Handler handler, final int action) {

		mLoadingRunnable = new Runnable() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				try {

					// int curPageIndex = 0;
					// switch (action) {
					// case Constants.LISTVIEW_ACTION_INIT:
					// case Constants.LISTVIEW_ACTION_CHANGE_CATALOG:
					// case Constants.LISTVIEW_ACTION_REFRESH:
					// curPageIndex = 0;
					// break;
					// case Constants.LISTVIEW_ACTION_SCROLL:
					// curPageIndex = lvData.size() / AppContext.PAGE_SIZE;
					// break;
					// }

					List<T> list = loadData(pageIndex);

					if (null == list)
						list = new ArrayList<T>();

					msg.what = list.size();
					msg.obj = list;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}

				handler.sendMessage(msg);

			}
		};
		mLoadingThread = new Thread(mLoadingRunnable);
		mLoadingThread.start();
	}

	private class PTRAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (null == mLVData)
				return 0;
			return mLVData.size();
		}

		@Override
		public Object getItem(int position) {
			try {
				if (null != mLVData)
					return mLVData.get(position);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			T item = null;
			try {
				item = mLVData.get(position);
			} catch (IndexOutOfBoundsException e) {
			}

			return getItemView(item, position, convertView, parent);
		}

	}

	@Override
	public void refresh() {

		if (isDataLoading)
			return;

		if (null != mLoadingLayout) {
			mLV.setVisibility(View.GONE);
			mLoadingLayout.setVisibility(View.VISIBLE);
		} else {
			mLV.setVisibility(View.VISIBLE);
		}

		if (null != mEmptyLayout) {
			mEmptyLayout.setVisibility(View.GONE);
		}

		isDataLoading = true;

		mActionType = ACTION_REFRESH;
		loadListData(0, mLVHandler, mActionType);

	}

	@Override
	public List<T> getData() {
		return mLVData;
	}

	@Override
	public ListView getListView() {
		return mLV;
	}

	@Override
	public void notifyDataSetChanged() {
		if (null == mLVAdapter)
			return;
		mLVAdapter.notifyDataSetChanged();
	}

	@Override
	public void setLoadingLayout(View loadingLayout) {
		if (null == loadingLayout)
			return;

		this.mLoadingLayout = loadingLayout;
		loadingLayout.setVisibility(View.GONE);
		((ViewGroup) mContentView).addView(loadingLayout, new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
	}

	@Override
	public void setEmptyLayout(View emptyLayout) {
		if (null == emptyLayout)
			return;

		this.mEmptyLayout = emptyLayout;
		emptyLayout.setVisibility(View.GONE);
		((ViewGroup) mContentView).addView(emptyLayout, new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
	}

	@Override
	public void onLoadEmpty() {

		if (null != mLoadingLayout) {
			mLoadingLayout.setVisibility(View.GONE);
		}

		if (null != mEmptyLayout) {
			mLV.setVisibility(View.GONE);
			mEmptyLayout.setVisibility(View.VISIBLE);
			mEmptyLayout.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.alert_error_x_in));
		} else {
			mLV.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onLoadComplete() {

		if (null != mLoadingLayout) {
			mLoadingLayout.setVisibility(View.GONE);
		}

		if (null != mEmptyLayout) {
			mEmptyLayout.setVisibility(View.GONE);
		}

		mLV.setVisibility(View.VISIBLE);

	}

	@Override
	public void handleListData(Message msg) {
		if (null == msg || null == msg.obj)
			return;

		try {
			@SuppressWarnings("unchecked")
			List<T> list = (List<T>) msg.obj;

			switch (mActionType) {
			case ACTION_INIT:
			case ACTION_REFRESH:
			case ACTION_CHANGE_CATALOG:

				mLVData.clear();
				mLVData.addAll(list);

				break;
			case ACTION_SCROLL:

				mLVData.addAll(list);

				break;
			}
		} catch (NullPointerException e) {
		} catch (ClassCastException e) {
		}

	}

}
