package com.music.uifragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.music.base.BaseFpdl;
import com.music.base.BaseResult;
import com.music.king.app.R;

public class FpdlFrag extends BaseFpdl<BaseResult> {

	@Override
	protected void firstVisibleToUser() {
		loadListData(0, mLVHandler, ACTION_INIT);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.initView();
	}

	private void initView() {

		View loadingLayout = View.inflate(mContext, R.layout.layout_loading, null);
		setLoadingLayout(loadingLayout);

		View emptyLayout = View.inflate(mContext, R.layout.layout_empty, null);
		setEmptyLayout(emptyLayout);

	}

	@Override
	public void onListItemClick(BaseResult item, AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public boolean onListItemLongClick(BaseResult item, AdapterView<?> parent, View view, int position, long id) {
		return true;
	}

	@Override
	public List<BaseResult> loadData(Object... params) {
		debug("loadData");

		int pageIndex = 0;
		try {
			pageIndex = (Integer) params[0];
		} catch (Exception e) {
			e.printStackTrace();
			pageIndex = 0;
		}
		debug("pageIndex = " + pageIndex);

		try {
			Thread.sleep(1000 * 1);
		} catch (InterruptedException e) {
		}

		// if (true)
		// return null;

		return new ArrayList<BaseResult>() {
			private static final long serialVersionUID = 1L;
			{
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());

				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
				add(new BaseResult());
			}
		};
	}

	@Override
	public View getItemView(BaseResult item, int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (null == convertView) {
			convertView = View.inflate(mContext, android.R.layout.simple_list_item_2, null);
			holder = new ViewHolder(convertView);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (null != item && null != holder) {

			holder.text1.setText(item.toString());
			holder.text2.setText("" + position);

		}

		return convertView;

	}

	private class ViewHolder {

		public TextView text1;
		public TextView text2;

		public ViewHolder(View view) {

			text1 = (TextView) view.findViewById(android.R.id.text1);
			text2 = (TextView) view.findViewById(android.R.id.text2);

			view.setTag(this);
		}

	}

}
