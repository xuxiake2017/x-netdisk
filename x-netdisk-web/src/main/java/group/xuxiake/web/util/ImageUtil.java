package group.xuxiake.web.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.png.PngChunkType;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.file.FileMetadataDirectory;
import com.drew.metadata.gif.GifHeaderDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {

	public static Map<String, Object> getImgInfo(InputStream is, Long lastModifiedDate) throws ImageProcessingException, IOException {

		Map<String, Object> map = new HashMap<>();
		Date fileModifiedDate = null;
		Date dateTime = null;
		Integer imgWidth = null;
		Integer imgHeight = null;

		Metadata metadata = ImageMetadataReader.readMetadata(is);
		Iterable<Directory> directories = metadata.getDirectories();
		for (Directory directory : directories) {
			//获取图片最后修改时间
			if (directory instanceof FileMetadataDirectory) {
				fileModifiedDate = directory.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE);
			}
			//获取图片的Exif拍摄时间
			if (directory instanceof ExifIFD0Directory) {
				dateTime = directory.getDate(ExifDirectoryBase.TAG_DATETIME);
			}

			//获取图片的长、宽
			//jpeg
			if (directory instanceof JpegDirectory) {
				imgWidth = directory.getInteger(JpegDirectory.TAG_IMAGE_WIDTH);
				imgHeight = directory.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT);
			}
			//png
			if (directory instanceof PngDirectory) {
				PngDirectory pngDirectory = (PngDirectory) directory;
				PngChunkType pngChunkType = pngDirectory.getPngChunkType();
				if (pngChunkType.equals(PngChunkType.IHDR)) {
					imgWidth = directory.getInteger(PngDirectory.TAG_IMAGE_WIDTH);
					imgHeight = directory.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);
				}
			}
			//gif
			if (directory instanceof GifHeaderDirectory) {
				imgWidth = directory.getInteger(GifHeaderDirectory.TAG_IMAGE_WIDTH);
				imgHeight = directory.getInteger(GifHeaderDirectory.TAG_IMAGE_HEIGHT);
			}
		}
		if (dateTime != null) {
			map.put("shootTime", dateTime);
		} else if (fileModifiedDate != null){
			map.put("shootTime", fileModifiedDate);
		} else {
			map.put("shootTime", new Date(lastModifiedDate));
		}
		map.put("imgWidth", imgWidth);
		map.put("imgHeight", imgHeight);
		return map;
	}
}
