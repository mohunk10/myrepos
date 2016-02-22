package com.music.base;

import java.io.Serializable;

public class BaseResult implements Serializable {

	private static final long serialVersionUID = 1L;

	public final static int CODE_INVALID = -2;
	public final static int CODE_FAIL = -1;
	public final static int CODE_SUCCESSS = 0;

	private int retcode = CODE_FAIL;
	private String retmsg = "";

	private String rettoken;

	public BaseResult() {
	}

	public BaseResult(String retmsg) {
		this.retmsg = retmsg;
	}

	public BaseResult(int retcode, String retmsg) {
		this.retcode = retcode;
		this.retmsg = retmsg;
	}

	public int getRetcode() {
		return retcode;
	}

	public void setRetcode(int retcode) {
		this.retcode = retcode;
	}

	public String getRetmsg() {
		return retmsg;
	}

	public void setRetmsg(String retmsg) {
		this.retmsg = retmsg;
	}

	public String getRettoken() {
		return rettoken;
	}

	public void setRettoken(String rettoken) {
		this.rettoken = rettoken;
	}

}
