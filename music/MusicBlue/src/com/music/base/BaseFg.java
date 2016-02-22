package com.music.base;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public interface BaseFg<T> {

	public final static int ACTION_INIT = 0x01;
	public final static int ACTION_REFRESH = 0x02;
	public final static int ACTION_SCROLL = 0x03;
	public final static int ACTION_CHANGE_CATALOG = 0x04;

	public final static int DATA_MORE = 0x01;
	public final static int DATA_LOADING = 0x02;
	public final static int DATA_FULL = 0x03;
	public final static int DATA_EMPTY = 0x04;

	public void refresh();

	public List<T> getData();

	public GridView getListView();

	public void notifyDataSetChanged();

	public void setLoadingLayout(View loadingLayout);

	public void setEmptyLayout(View emptyLayout);

	public void onLoadEmpty();

	public void onLoadComplete();

	public void handleListData(android.os.Message msg);

	public void onListItemClick(T item, AdapterView<?> parent, View view, int position, long id);

	public boolean onListItemLongClick(T item, AdapterView<?> parent, View view, int position, long id);

	public List<T> loadData(Object... params);

	public View getItemView(T item, int position, View convertView, ViewGroup parent);

}
