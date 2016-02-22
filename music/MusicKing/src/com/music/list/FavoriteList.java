package com.music.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cmsc.cmmusic.common.data.MusicInfo;
import com.music.bean.MusicInfoSer;

/**
 * <b>创建一个公用的最喜爱歌曲列表</b></br>
 */
public class FavoriteList {

	public static final List<MusicInfoSer> list = new ArrayList<MusicInfoSer>();

	/**
	 * 按字母排序
	 */
	public static void sort() {
		Collections.sort(list, new MusicInfoSer());
	}

}
