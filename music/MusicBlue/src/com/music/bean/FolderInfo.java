package com.music.bean;

import java.util.List;

import com.cmsc.cmmusic.common.data.MusicInfo;

/**
 * <br>
 * <b>歌曲文件夹对应的歌曲信息</b></br>
 * 
 */
public class FolderInfo {

	private String musicFolder;// 歌曲隶属文件夹
	private List<MusicInfoSer> musicList;// 歌曲列表

	/**
	 * 获得文件夹路径名
	 * 
	 * @return 文件夹路径名
	 */
	public String getMusicFolder() {
		return musicFolder;
	}

	/**
	 * 设置文件夹路径名
	 * 
	 * @param musicFolder
	 *            文件夹路径名
	 */
	public void setMusicFolder(String musicFolder) {
		this.musicFolder = musicFolder;
	}

	/**
	 * 获得文件夹下的歌曲列表
	 * 
	 * @return 歌曲列表
	 */
	public List<MusicInfoSer> getMusicList() {
		return musicList;
	}

	/**
	 * 设置文件夹下歌曲列表
	 * 
	 * @param musicList
	 *            歌曲列表
	 */
	public void setMusicList(List<MusicInfoSer> musicList) {
		this.musicList = musicList;
	}

}
