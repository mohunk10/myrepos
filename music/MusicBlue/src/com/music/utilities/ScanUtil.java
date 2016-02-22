package com.music.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;

import com.cmsc.cmmusic.common.data.MusicInfo;
import com.music.bean.FolderInfo;
import com.music.bean.MusicInfoSer;
import com.music.bean.ScanInfo;
import com.music.db.DBDao;
import com.music.list.FolderList;
import com.music.list.LyricList;
import com.music.service.PlayingMusic;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

/**
 * <b>扫描管理器，负责扫描数据库或者SD卡的歌曲文件(耗时操作，请使用异步线程执行)，
 * 由于在一个循环中同时判断出歌曲对应的歌词难度太大，只能新建数据库表来分别存储</b></br>
 * 
 * 
 * @version 实现扫描SD卡，数据库增加及查询操作<br>
 *          实现获取详细mp3标签，修改数据库记录信息<br>
 *          修正数据库查询不存在bug<br>
 *          新增对歌词的扫描<br>
 *          新增对专辑名称的扫描，修复扫描出现的几个错误<br>
 *          修正扫描文件夹歌曲列表的一个错误<br>
 *          修正真机上扫描报错的问题，原因是路径被全被格成小写导致空指针<br>
 *          修正多次扫描后文件夹列表重复的问题</br>
 */
public class ScanUtil {

	private Context context;
	private DBDao db;

	public ScanUtil(Context context) {
		this.context = context;
	}

	/**
	 * 查询音乐媒体库所有目录，缺点是影响一点效率，没有找到直接提供媒体库目录的方法
	 */
	public List<ScanInfo> searchAllDirectory() {
		List<ScanInfo> list = new ArrayList<ScanInfo>();
		StringBuffer sb = new StringBuffer();
		String[] projection = { MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA };
		Cursor cr = context.getContentResolver()
				.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Audio.Media.DISPLAY_NAME);
		String displayName = null;
		String data = null;
		while (cr.moveToNext()) {
			displayName = cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
			data = cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.DATA));
			if (data != null && displayName != null) {
				data = data.replace(displayName, " ");// 替换文件名留下它的上一级目录
			}

			if (!sb.toString().contains(data)) {
				list.add(new ScanInfo(data, false));
				sb.append(data);
			}
		}
		cr.close();
		return list;
	}

	/**
	 * 扫描SD卡音乐，录入数据库并加入歌曲列表，缺点是假如系统媒体库没有更新媒体库目录则扫描不到
	 * 
	 * @param scanList
	 *            音乐媒体库所有目录
	 */
	public void scanMusicFromSD(List<String> folderList, Handler handler) {
		int count = 0;// 统计新增数
		db = new DBDao(context);
		db.deleteLyric();// 不做歌词是否已经存在判断，全部删除后重新扫描
		final int size = folderList.size();
		for (int i = 0; i < size; i++) {
			final String folder = folderList.get(i);
			File dir = new File(folder.substring(0, folder.length() - 1));

			File[] file = dir.listFiles();
			if (file == null) {
				continue;
			}

			FolderInfo folderInfo = new FolderInfo();
			List<MusicInfoSer> listInfo = new ArrayList<MusicInfoSer>();
			for (File temp : file) {
				// 是文件才保存，里面还有文件夹的，那就算了吧...
				if (temp.isFile() && temp.getName().indexOf(".") != -1) {

					String fileName = FormatUtil.formatUTFStr(temp.getName());
					final String path = temp.getPath();
					final String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
					fileName = fileName.substring(0, fileName.lastIndexOf("."));
					String name = fileName;
					// 记录歌曲信息
					if (end.equalsIgnoreCase("mp3")) {// 不区分大小写
						// 查询不存在则记录
						if (!db.queryExist(name, folder)) {
							MusicInfoSer musicInfo = scanMusicTag(name, path);
							// 第一次扫描最喜爱肯定为false
							db.add(FormatUtil.formatUTFStr(name), FormatUtil.formatUTFStr(musicInfo.getSongName()), FormatUtil.formatUTFStr(path),
									FormatUtil.formatUTFStr(folder), false, FormatUtil.formatUTFStr(""),
									FormatUtil.formatUTFStr(musicInfo.getSize()), FormatUtil.formatUTFStr(musicInfo.getArtist()),
									FormatUtil.formatUTFStr(musicInfo.getFormat()), FormatUtil.formatUTFStr(musicInfo.getAlbum()),
									FormatUtil.formatUTFStr(musicInfo.getYears()), FormatUtil.formatUTFStr(musicInfo.getChannels()),
									FormatUtil.formatUTFStr(musicInfo.getGenre()), FormatUtil.formatUTFStr(musicInfo.getKbps()),
									FormatUtil.formatUTFStr(musicInfo.getHz()));
							// 加入所有歌曲列表
							PlayingMusic.list.add(musicInfo);
							// 加入文件夹临时列表
							listInfo.add(musicInfo);
							count++;
						}
						if (handler != null) {
							Message msg = handler.obtainMessage();
							msg.obj = fileName;
							// Message从handler获取，可以直接向该handler对象发送消息
							msg.sendToTarget();
						}
					}
					// 记录歌词信息(只识别LRC歌词)
					if (end.equalsIgnoreCase("lrc")) {// 不区分大小写
						db.addLyric(fileName, path);
						LyricList.map.put(fileName, path);
					}
				}

			}
			if (listInfo.size() > 0) {
				boolean exists = false;
				for (int j = 0; j < FolderList.list.size(); j++) {
					// 做对比，存在同名路径则判断有新增就合并，没有直接添加。此方法比较笨啊，肯定影响效率的...
					if (folder.equals(FolderList.list.get(j).getMusicFolder())) {
						// 有扫描到新增得歌曲就合并列表
						FolderList.list.get(j).getMusicList().addAll(listInfo);
						exists = true;
						break;// 跳出循环
					}
				}
				if (!exists) {// 不存在同名路径才新增
					// 设置文件夹列表文件夹路径
					folderInfo.setMusicFolder(folder);
					// 设置文件夹列表歌曲信息
					folderInfo.setMusicList(listInfo);
					// 加入文件夹列表
					FolderList.list.add(folderInfo);
				}
			}
		}
		if (handler != null) {
			Message msg = handler.obtainMessage();
			msg.obj = "扫描完成，新增歌曲" + count + "首";
			// Message从handler获取，可以直接向该handler对象发送消息
			msg.sendToTarget();
		}
		db.close();
	}

	public static void tree(File f) {

	}

	/**
	 * 查新数据库记录的所有歌曲
	 */
	public void scanMusicFromDB() {
		db = new DBDao(context);
		db.queryAll(searchAllDirectory());
		db.close();
	}

	public List<MusicInfoSer> scanMusicInfoFromDB() {
		List<MusicInfoSer> list = new ArrayList<MusicInfoSer>();
		db = new DBDao(context);
		list.addAll(db.queryAllMusic(searchAllDirectory()));
		
		
		
		
		
		db.close();
		return list;
	}

	/**
	 * 关键一步，获取MP3详细信息，比如歌名、歌手、比特率之类的
	 * 
	 * @param path
	 *            文件路径
	 */
	private MusicInfoSer scanMusicTag(String fileName, String path) {
		File file = new File(path);
		MusicInfoSer info = new MusicInfoSer();

		if (file.exists()) {
			try {
				MP3File mp3File = (MP3File) AudioFileIO.read(file);

				MP3AudioHeader header = mp3File.getMP3AudioHeader();

				info.setFile(FormatUtil.formatUTFStr(fileName));
				// 时长(此处可能与MediaPlayer获得长度不一致，但误差不大)
				info.setTime(FormatUtil.formatTime((int) (header.getTrackLength() * 1000)));
				info.setSize(FormatUtil.formatSize(file.length()));// 大小
				info.setPath(path);// 路径
				info.setFormat("格式: " + header.getEncodingType());// 格式(编码类型)
				info.setSongName(fileName);
				final String channels = header.getChannels();
				if (channels.equals("Joint Stereo")) {
					info.setChannels("声道: 立体声");
				} else {
					info.setChannels("声道: " + header.getChannels());// 声道
				}
				info.setKbps("比特率: " + header.getBitRate() + "Kbps");// 比特率
				info.setHz("采样率: " + header.getSampleRate() + "Hz");// 采样率
				if (mp3File.hasID3v1Tag()) {

					Tag tag = mp3File.getTag();

					try {
						final String tempName = FormatUtil.formatUTFStr(tag.getFirst(FieldKey.TITLE));
						if (tempName == null || tempName.equals("")) {
							info.setName(fileName);// 扫描不到存文件名
						} else {
							info.setName(tempName);// 歌名
						}
					} catch (KeyNotFoundException e) {
						info.setName(fileName);// 扫描出错存文件名
					}

					try {
						final String tempArtist = FormatUtil.formatUTFStr(tag.getFirst(FieldKey.ARTIST));
						if (tempArtist == null || tempArtist.equals("")) {
							info.setArtist("未知艺术家");
						} else {
							info.setArtist(tempArtist);// 艺术家
						}
					} catch (KeyNotFoundException e) {
						info.setArtist("未知艺术家");
					}

					try {
						final String tempAlbum = FormatUtil.formatUTFStr(tag.getFirst(FieldKey.ALBUM));
						if (tempAlbum == null || tempAlbum.equals("")) {
							info.setAlbum("专辑: 未知");
						} else {
							info.setAlbum("专辑: " + tempAlbum);// 专辑
						}
					} catch (KeyNotFoundException e) {
						info.setAlbum("专辑: 未知");
					}

					try {
						final String tempYears = FormatUtil.formatUTFStr(tag.getFirst(FieldKey.YEAR));
						if (tempYears == null || tempYears.equals("")) {
							info.setYears("年代: 未知");
						} else {
							info.setYears("年代: " + tempYears);// 年代
						}
					} catch (KeyNotFoundException e) {
						info.setYears("年代: 未知");
					}

					try {
						final String tempGener = FormatUtil.formatUTFStr(tag.getFirst(FieldKey.GENRE));
						if (tempGener == null || tempGener.equals("")) {
							info.setGenre("风格: 未知");
						} else {
							info.setGenre("风格: " + tempGener);// 风格
						}
					} catch (KeyNotFoundException e) {
						info.setGenre("风格: 未知");
					}
				} else if (mp3File.hasID3v2Tag()) {// 如果上面的条件不成立，才执行下面的方法
					AbstractID3v2Tag v2Tag = mp3File.getID3v2Tag();

					try {
						final String tempName = FormatUtil.formatUTFStr(v2Tag.getFirst(FieldKey.TITLE));
						if (tempName == null || tempName.equals("")) {
							info.setName(fileName);// 扫描不到存文件名
						} else {
							info.setName(tempName);// 歌名
						}
					} catch (KeyNotFoundException e) {
						info.setName(fileName);// 扫描出错存文件名
					}

					try {
						final String tempArtist = FormatUtil.formatUTFStr(v2Tag.getFirst(FieldKey.ARTIST));
						if (tempArtist == null || tempArtist.equals("")) {
							info.setArtist("未知艺术家");
						} else {
							info.setArtist(tempArtist);// 艺术家
						}
					} catch (KeyNotFoundException e) {
						info.setArtist("未知艺术家");
					}

					try {
						final String tempAlbum = FormatUtil.formatUTFStr(v2Tag.getFirst(FieldKey.ALBUM));
						if (tempAlbum == null || tempAlbum.equals("")) {
							info.setAlbum("专辑: 未知");
						} else {
							info.setAlbum("专辑: " + tempAlbum);// 专辑
						}
					} catch (KeyNotFoundException e) {
						info.setAlbum("专辑: 未知");
					}

					try {
						final String tempYears = FormatUtil.formatUTFStr(v2Tag.getFirst(FieldKey.YEAR));
						if (tempYears == null || tempYears.equals("")) {
							info.setYears("年代: 未知");
						} else {
							info.setYears("年代: " + tempYears);// 年代
						}
					} catch (KeyNotFoundException e) {
						info.setYears("年代: 未知");
					}

					try {
						final String tempGener = FormatUtil.formatUTFStr(v2Tag.getFirst(FieldKey.GENRE));
						if (tempGener == null || tempGener.equals("")) {
							info.setGenre("风格: 未知");
						} else {
							info.setGenre("风格: " + tempGener);// 风格
						}
					} catch (KeyNotFoundException e) {
						info.setGenre("风格: 未知");
					}
				} else {
					info.setName(fileName);
					info.setArtist("未知艺术家");
					info.setAlbum("专辑: 未知");
					info.setYears("年代: 未知");
					info.setGenre("风格: 未知");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return info;
	}

}
