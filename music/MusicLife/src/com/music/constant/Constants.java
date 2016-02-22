package com.music.constant;

import java.io.File;

import android.os.Environment;

public class Constants {

	public final static int MSG_INVALID = -2;
	public final static int MSG_EXCEPTION = -1;
	public final static int MSG_SUCCESS = 0;
	public final static int MSG_FAIL = 1;
	public final static int MSG_NULL = 2;

	public final static String PHOTO_SAVEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FirstCarLoans" + File.separator
			+ "Photos" + File.separator;

}
