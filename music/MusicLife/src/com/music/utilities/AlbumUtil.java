package com.music.utilities;

import java.io.File;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.images.Artwork;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * <b>扫描音乐内嵌专辑图片处理类</b></br>
 */
public class AlbumUtil {

	/**
	 * 扫描音乐内嵌专辑图片
	 * 
	 * @param path
	 *            音乐SD卡路径
	 * @return 有则返回图片，无则返回null
	 */
	public Bitmap scanAlbumImage(String path) {
		File file = new File(path);
		Bitmap bitmap = null;

		if (file.exists()) {
			try {
				MP3File mp3File = (MP3File) AudioFileIO.read(file);
				if (mp3File.hasID3v1Tag()) {
					Tag tag = mp3File.getTag();
					Artwork artwork = tag.getFirstArtwork();// 获得专辑图片
					if (artwork != null) {
						byte[] byteArray = artwork.getBinaryData();// 将读取到的专辑图片转成二进制
						bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length); // 通过BitmapFactory转成Bitmap
					}
				} else if (mp3File.hasID3v2Tag()) {// 如果上面的条件不成立，才执行下面的方法
					AbstractID3v2Tag v2Tag = mp3File.getID3v2Tag();
					Artwork artwork = v2Tag.getFirstArtwork();// 获得专辑图片
					if (artwork != null) {
						byte[] byteArray = artwork.getBinaryData();// 将读取到的专辑图片转成二进制
						bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length); // 通过BitmapFactory转成Bitmap
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return bitmap;
	}

}
