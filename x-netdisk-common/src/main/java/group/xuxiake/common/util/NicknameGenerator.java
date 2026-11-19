package group.xuxiake.common.util;

import java.util.Random;

/**
 * 默认用户昵称生成器。
 */
public final class NicknameGenerator {

    private static final String[] ADJECTIVES = {
            "快乐", "阳光", "可爱", "聪明",
            "勇敢", "活泼", "温柔", "开心"
    };

    private static final String[] NOUNS = {
            "小熊", "小兔", "小鹿", "星星",
            "月亮", "鲸鱼", "海豚", "小猫"
    };

    private static final Random RANDOM = new Random();

    private NicknameGenerator() {
    }

    public static String generate() {
        String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        int number = RANDOM.nextInt(10000);
        return adjective + noun + String.format("%04d", number);
    }
}
