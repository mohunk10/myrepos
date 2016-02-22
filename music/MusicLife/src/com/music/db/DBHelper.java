package com.music.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <b>对表的操作</b></br>
 */
public class DBHelper extends SQLiteOpenHelper {

	/**
	 * 创建数据库
	 * 
	 * @param context
	 *            上下文
	 */
	public DBHelper(Context context) {
		super(context, DBData.MUSIC_DB_NAME, null, DBData.MUSIC_DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 创建音乐表
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DBData.MUSIC_TABLENAME + " (" + DBData.MUSIC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBData.MUSIC_FILE
				+ " NVARCHAR(100), " + DBData.MUSIC_NAME + " NVARCHAR(100), " + DBData.MUSIC_PATH + " NVARCHAR(300), " + DBData.MUSIC_FOLDER
				+ " NVARCHAR(300), " + DBData.MUSIC_FAVORITE + " INTEGER, " + DBData.MUSIC_TIME + " NVARCHAR(100), " + DBData.MUSIC_SIZE + " NVARCHAR(100), "
				+ DBData.MUSIC_ARTIST + " NVARCHAR(100), " + DBData.MUSIC_FORMAT + " NVARCHAR(100), " + DBData.MUSIC_ALBUM + " NVARCHAR(100), "
				+ DBData.MUSIC_YEARS + " NVARCHAR(100), " + DBData.MUSIC_CHANNELS + " NVARCHAR(100), " + DBData.MUSIC_GENRE + " NVARCHAR(100), "
				+ DBData.MUSIC_KBPS + " NVARCHAR(100), " + DBData.MUSIC_HZ + " NVARCHAR(100))");
		// 创建歌词表
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DBData.LYRIC_TABLENAME + " (" + DBData.LYRIC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBData.LYRIC_FILE
				+ " NVARCHAR(100), " + DBData.LYRIC_PATH + " NVARCHAR(300))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + DBData.MUSIC_TABLENAME);
		db.execSQL("DROP TABLE IF EXISTS " + DBData.LYRIC_TABLENAME);
	}

}
