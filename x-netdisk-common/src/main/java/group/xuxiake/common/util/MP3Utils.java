package group.xuxiake.common.util;

import com.mpatric.mp3agic.*;
import group.xuxiake.common.entity.MP3Info;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.IOException;

/**
 * Author by xuxiake, Date on 2020/4/6 16:58.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Slf4j
public class MP3Utils {

    private static String stringEncode(String str, String charsetName) {
        try {
            return new String(str.getBytes("ISO-8859-1"), charsetName);
        } catch (Exception e) {
            return str;
        }
    }

    public static String guessEncoding(byte[] bytes) {
        String DEFAULT_ENCODING = "UTF-8";
        UniversalDetector detector =
                new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        return encoding;
    }

    public static MP3Info getMP3Info(String fileName) throws InvalidDataException, IOException, UnsupportedTagException, NotSupportedException {

        Mp3File mp3file = new Mp3File(fileName);

        MP3Info mp3Info = new MP3Info();
        mp3Info.setBitrate(mp3file.getBitrate());
        mp3Info.setDuration(mp3file.getLengthInSeconds());

        String title = "";
        String artist = "";
        byte[] albumImageData = null;
        if (mp3file.hasId3v1Tag()) {
            ID3v1 id3v1Tag = mp3file.getId3v1Tag();
            String charsetName = guessEncoding(id3v1Tag.toBytes());
            if (charsetName.equals("UTF-8")) {
                title = id3v1Tag.getTitle();
                artist = id3v1Tag.getArtist();
            } else {
                title = stringEncode(id3v1Tag.getTitle(), charsetName);
                artist = stringEncode(id3v1Tag.getArtist(), charsetName);
            }
        }

        if (mp3file.hasId3v2Tag()) {
            ID3v2 id3v2Tag = mp3file.getId3v2Tag();

            String charsetName = guessEncoding(id3v2Tag.toBytes());
            if (charsetName.equals("UTF-8")) {
                title = id3v2Tag.getTitle();
                artist = id3v2Tag.getArtist();
            } else {
                title = stringEncode(id3v2Tag.getTitle(), charsetName);
                artist = stringEncode(id3v2Tag.getArtist(), charsetName);
            }
            albumImageData = id3v2Tag.getAlbumImage();
        }
        mp3Info.setTitle(title);
        mp3Info.setArtist(artist);
        mp3Info.setAlbumImage(albumImageData);
        return mp3Info;
    }
}
