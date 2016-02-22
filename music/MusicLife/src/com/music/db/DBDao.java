package com.music.db;

import java.util.ArrayList;
import java.util.List;

import com.cmsc.cmmusic.common.data.MusicInfo;
import com.music.bean.FolderInfo;
import com.music.bean.MusicInfoSer;
import com.music.bean.ScanInfo;
import com.music.list.FavoriteList;
import com.music.list.FolderList;
import com.music.list.LyricList;
import com.music.service.PlayingMusic;
import com.music.utilities.FormatUtil;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * <b>对数据库的增删查改等</b></br>
 * 
 * @version 实现增、查、改、是否存在<br>
 *          实现新增单条音乐更为详情的记录，更新查询方法<br>
 *          实现新增单条音乐歌词信息的记录，加入歌词查询方法<br>
 *          实现单条音乐的数据库删除<br>
 *          新增单条音乐的专辑信息，前面竟然漏掉了这么重要的数据...<br>
 *          修正扫描文件夹歌曲列表的一个错误<br>
 *          修正真机上扫描报错的问题，原因是文件名中含有'，SQL语句查询出错</br>
 */
public class DBDao {

	// 歌曲列表信息
	public static List<MusicInfoSer> musicList;
	public ContentResolver cr;
	private DBHelper helper;
	private SQLiteDatabase db;
	public Context context;
	public Cursor mCursor;
	public MusicInfoSer mi;

	/**
	 * 创建和初始化数据库，使用完记得调用close方法关闭数据库
	 * 
	 * @param context
	 */
	public DBDao(Context context) {
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
		this.context = context;
	}

	/**
	 * 新增单条音乐数据信息
	 * 
	 */
	public long add(String fileName, String musicName, String musicPath, String musicFolder, boolean isFavorite, String musicTime, String musicSize,
			String musicArtist, String musicFormat, String musicAlbum, String musicYears, String musicChannels, String musicGenre, String musicKbps,
			String musicHz) {
		ContentValues values = new ContentValues();
		values.put(DBData.MUSIC_FILE, FormatUtil.formatUTFStr(fileName));
		values.put(DBData.MUSIC_NAME, FormatUtil.formatUTFStr(musicName));
		values.put(DBData.MUSIC_PATH, FormatUtil.formatUTFStr(musicPath));
		values.put(DBData.MUSIC_FOLDER, FormatUtil.formatUTFStr(musicFolder));
		values.put(DBData.MUSIC_FAVORITE, isFavorite ? 1 : 0);// 数据库定义字段数据为整型
		values.put(DBData.MUSIC_TIME, FormatUtil.formatUTFStr(musicTime));
		values.put(DBData.MUSIC_SIZE, FormatUtil.formatUTFStr(musicSize));
		values.put(DBData.MUSIC_ARTIST, FormatUtil.formatUTFStr(musicArtist));
		values.put(DBData.MUSIC_FORMAT, FormatUtil.formatUTFStr(musicFormat));
		values.put(DBData.MUSIC_ALBUM, FormatUtil.formatUTFStr(musicAlbum));
		values.put(DBData.MUSIC_YEARS, FormatUtil.formatUTFStr(musicYears));
		values.put(DBData.MUSIC_CHANNELS, FormatUtil.formatUTFStr(musicChannels));
		values.put(DBData.MUSIC_GENRE, FormatUtil.formatUTFStr(musicGenre));
		values.put(DBData.MUSIC_KBPS, FormatUtil.formatUTFStr(musicKbps));
		values.put(DBData.MUSIC_HZ, FormatUtil.formatUTFStr(musicHz));
		long result = db.insert(DBData.MUSIC_TABLENAME, DBData.MUSIC_FILE, values);
		return result;
	}

	/**
	 * 新增单条音乐歌词信息
	 * 
	 */
	public long addLyric(String fileName, String lrcPath) {
		ContentValues values = new ContentValues();
		values.put(DBData.LYRIC_FILE, fileName);
		values.put(DBData.LYRIC_PATH, lrcPath);
		long result = db.insert(DBData.LYRIC_TABLENAME, DBData.LYRIC_FILE, values);
		return result;
	}

	/**
	 * 更新音乐相关记录，只更新用户是否标记为最喜爱音乐
	 * 
	 */
	public int update(String musicName, boolean isFavorite) {
		ContentValues values = new ContentValues();
		values.put(DBData.MUSIC_FAVORITE, isFavorite ? 1 : 0);// 数据库定义字段数据为整型
		int result = db.update(DBData.MUSIC_TABLENAME, values, DBData.MUSIC_NAME + "=?", new String[] { musicName });
		return result;
	}

	/**
	 * 查询对应条件的数据库信息是否存在
	 * 
	 * 建议此处不要写SQL语句，即rawQuery查询。因为某些文件名中就带有'，所以肯定报错！
	 */
	public boolean queryExist(String fileName, String musicFolder) {
		boolean isExist = false;
		Cursor cursor = db.query(DBData.MUSIC_TABLENAME, null, DBData.MUSIC_FILE + "=? AND " + DBData.MUSIC_FOLDER + "=?",
				new String[] { fileName, musicFolder }, null, null, null);
		if (cursor.getCount() > 0) {
			isExist = true;
		}
		return isExist;
	}

	/**
	 * 查询数据库保存的各媒体库目录下所有音乐信息和歌词
	 * 
	 * @param scanList
	 *            音乐媒体库所有目录
	 */
	public void queryAll(List<ScanInfo> scanList) {
		PlayingMusic.list.clear();
		FolderList.list.clear();
		FavoriteList.list.clear();
		LyricList.map.clear();

		final int listSize = scanList.size();
		Cursor cursor = null;
		// 查询各媒体库目录下所有音乐信息
		for (int i = 0; i < listSize; i++) {
			final String folder = scanList.get(i).getFolderPath();

			cursor = db.rawQuery("SELECT file,name,path,favorite,time,size,artist,format,album,years,channels,genre,kbps,hz FROM " + DBData.MUSIC_TABLENAME
					+ " WHERE " + DBData.MUSIC_FOLDER + "='" + folder + "'", null);
			List<MusicInfoSer> listInfo = new ArrayList<MusicInfoSer>();
			if (cursor != null && cursor.getCount() > 0) {
				FolderInfo folderInfo = new FolderInfo();
				while (cursor.moveToNext()) {
					MusicInfoSer musicInfo = new MusicInfoSer();
					final String file = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_FILE));
					String name = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_NAME));
					final String path = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_PATH));
					final int favorite = cursor.getInt(cursor.getColumnIndex(DBData.MUSIC_FAVORITE));
					final String time = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_TIME));
					final String size = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_SIZE));
					final String artist = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_ARTIST));
					final String format = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_FORMAT));
					final String album = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_ALBUM));
					final String years = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_YEARS));
					final String channels = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_CHANNELS));
					final String genre = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_GENRE));
					final String kbps = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_KBPS));
					final String hz = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_HZ));

					musicInfo.setFile(file);
					musicInfo.setName(name);
					musicInfo.setPath(path);
					musicInfo.setFavorite(favorite == 1 ? true : false);
					musicInfo.setTime(time);
					musicInfo.setSize(size);
					musicInfo.setArtist(artist);
					musicInfo.setFormat(format);
					musicInfo.setAlbum(album);
					musicInfo.setYears(years);
					musicInfo.setChannels(channels);
					musicInfo.setGenre(genre);
					musicInfo.setKbps(kbps);
					musicInfo.setHz(hz);
					// 加入所有歌曲列表
					PlayingMusic.list.add(musicInfo);
					// 加入文件夹临时列表
					listInfo.add(musicInfo);
					// 加入我的最爱列表
					if (favorite == 1) {
						FavoriteList.list.add(musicInfo);
					}
				}

				// 设置文件夹列表文件夹路径
				folderInfo.setMusicFolder(folder);
				// 设置文件夹列表歌曲信息
				folderInfo.setMusicList(listInfo);
				// 加入文件夹列表
				FolderList.list.add(folderInfo);

			}
		}
		// 查询歌词
		cursor = db.rawQuery("SELECT * FROM " + DBData.LYRIC_TABLENAME, null);
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				final String file = cursor.getString(cursor.getColumnIndex(DBData.LYRIC_FILE));
				final String path = cursor.getString(cursor.getColumnIndex(DBData.LYRIC_PATH));
				LyricList.map.put(file, path);
			}
		}
		// 记得关闭游标
		if (cursor != null) {
			cursor.close();
		}
	}

	@SuppressWarnings("resource")
	public List<MusicInfoSer> queryAllMusic(List<ScanInfo> scanList) {
		List<MusicInfoSer> musicInfoList = new ArrayList<MusicInfoSer>();
		if (null == scanList || 0 >= scanList.size())
			return musicInfoList;
		final int listSize = scanList.size();
		Cursor cursor = null;
		try {

			// 查询各媒体库目录下所有音乐信息
			for (int i = 0; i < listSize; i++) {
				final String folder = scanList.get(i).getFolderPath();
				cursor = db.rawQuery("SELECT file,name,path,favorite,time,size,artist,format,album,years,channels,genre,kbps,hz FROM " + DBData.MUSIC_TABLENAME
						+ " WHERE " + DBData.MUSIC_FOLDER + "='" + folder + "'", null);
				if (cursor != null && cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						MusicInfoSer musicInfo = new MusicInfoSer();
						final String file = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_FILE));
						String name = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_NAME));
						final String path = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_PATH));
						final int favorite = cursor.getInt(cursor.getColumnIndex(DBData.MUSIC_FAVORITE));
						final String time = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_TIME));
						final String size = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_SIZE));
						final String artist = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_ARTIST));
						final String format = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_FORMAT));
						final String album = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_ALBUM));
						final String years = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_YEARS));
						final String channels = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_CHANNELS));
						final String genre = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_GENRE));
						final String kbps = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_KBPS));
						final String hz = cursor.getString(cursor.getColumnIndex(DBData.MUSIC_HZ));

						musicInfo.setFile(file);
						musicInfo.setName(name);
						musicInfo.setPath(path);
						musicInfo.setFavorite(favorite == 1 ? true : false);
						musicInfo.setTime(time);
						musicInfo.setSize(size);
						musicInfo.setArtist(artist);
						musicInfo.setFormat(format);
						musicInfo.setAlbum(album);
						musicInfo.setYears(years);
						musicInfo.setChannels(channels);
						musicInfo.setGenre(genre);
						musicInfo.setKbps(kbps);
						musicInfo.setHz(hz);
						musicInfo.setTag("local");
						// 加入所有歌曲列表
						 PlayingMusic.list.add(musicInfo);
						// 加入文件夹临时列表
						musicInfoList.add(musicInfo);
						// 加入我的最爱列表
						if (favorite == 1) {
							FavoriteList.list.add(musicInfo);
						}
					}
				}
			}

			// 查询歌词
			cursor = db.rawQuery("SELECT * FROM " + DBData.LYRIC_TABLENAME, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					final String file = cursor.getString(cursor.getColumnIndex(DBData.LYRIC_FILE));
					final String path = cursor.getString(cursor.getColumnIndex(DBData.LYRIC_PATH));
					LyricList.map.put(file, path);
				}
			}

			return musicInfoList;
		} catch (Exception e) {
			e.printStackTrace();
			return musicInfoList;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * 根据文件路径来删除音乐信息
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 成功删除的条数
	 */
	public int delete(String filePath) {
		/*
		 * 不晓得这里的'会不会出问题，删除的方法只有这个吧???
		 */
		int result = db.delete(DBData.MUSIC_TABLENAME, DBData.MUSIC_PATH + "='" + filePath + "'", null);
		return result;
	}

	/**
	 * 删除歌词信息表
	 */
	public void deleteLyric() {
		// 可能不存在该表，需要拋异常
		try {
			// 清空表并将表序号置零
			db.execSQL("delete from " + DBData.LYRIC_TABLENAME + ";");
			db.execSQL("update sqlite_sequence set seq=0 where name='" + DBData.LYRIC_TABLENAME + "';");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 使用完数据库必须关闭
	 */
	public void close() {
		db.close();
		db = null;
	}

}
