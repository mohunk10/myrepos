package com.music.bean;

import java.io.Serializable;

public class DataLogin implements Serializable {
	private static final long serialVersionUID = 1L;

	private String loginname;
	private String nickname;

	public String getLoginname() {
		return loginname;
	}

	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

}
