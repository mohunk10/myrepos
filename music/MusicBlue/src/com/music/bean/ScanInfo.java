package com.music.bean;

/**
 * <b>扫描信息，SD卡路径和用户是否勾选</b></br>
 */
public class ScanInfo {

	private String folderPath;// 文件夹路径
	private boolean isChecked;// 用户是否勾选

	public ScanInfo(String folderPath, boolean isChecked) {
		this.folderPath = folderPath;
		this.isChecked = isChecked;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}
