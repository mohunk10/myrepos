package com.music.base;

public class Result<T> extends BaseResult {

	private static final long serialVersionUID = 1L;

	private T retdata;

	public Result() {
		super();
	}

	public Result(int retcode, String retmsg) {
		super(retcode, retmsg);
	}

	public Result(String retmsg) {
		super(retmsg);
	}

	public T getRetdata() {
		return retdata;
	}

	public void setRetdata(T retdata) {
		this.retdata = retdata;
	}

}
