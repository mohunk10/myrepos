package com.music.uifragment;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.music.base.BaseFlv;
import com.music.bean.ScanInfo;
import com.music.blue.app.R;
import com.music.utilities.ScanUtil;

public class ScanMusicFrag extends BaseFlv<ScanInfo> {

	@Override
	protected void firstVisibleToUser() {
		// loadListData(0, mLVHandler, ACTION_INIT);
		refresh();
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
	public void onListItemClick(ScanInfo item, AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public boolean onListItemLongClick(ScanInfo item, AdapterView<?> parent, View view, int position, long id) {
		return true;
	}

	@Override
	public List<ScanInfo> loadData(Object... params) {
		debug("loadData");

		ScanUtil scanUtil = new ScanUtil(mContext);
		return scanUtil.searchAllDirectory();
	}

	@Override
	public View getItemView(final ScanInfo item, int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (null == convertView) {
			convertView = View.inflate(mContext, R.layout.listitem_scan_music, null);
			holder = new ViewHolder(convertView);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (null != item && null != holder) {

			holder.cb_checked.setChecked(item.isChecked());
			holder.tv_path.setText(item.getFolderPath());

			holder.cb_checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					item.setChecked(isChecked);
				}
			});

		}

		return convertView;

	}

	private class ViewHolder {

		public CheckBox cb_checked;
		public TextView tv_path;

		public ViewHolder(View view) {

			cb_checked = (CheckBox) view.findViewById(R.id.cb_checked);
			tv_path = (TextView) view.findViewById(R.id.tv_path);

			view.setTag(this);
		}

	}

}
