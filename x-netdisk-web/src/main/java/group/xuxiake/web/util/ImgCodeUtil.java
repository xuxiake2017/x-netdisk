package group.xuxiake.web.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 生成图片验证码的工具类
 * @author 13155
 *
 */
public class ImgCodeUtil {

	private static final char[] CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'q', 'w', 'e', 'r', 't',
			'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
			'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X',
			'C', 'V', 'B', 'N', 'M' };

	private static final int SIZE = 4;
	private static final int LINES=5;
	private static final int WIDTH=80;
	private static final int HEIGHT=30;
	private static final int FONT_SIZE=30;
	
	public static Object[] createImage(){
		
		StringBuffer stringBuffer = new StringBuffer();
		BufferedImage image=new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_BGR);
		Graphics g=image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		Random random = new Random();
		
		for(int i=0;i<SIZE;i++){
			int n=random.nextInt(CHARS.length);
			g.setColor(getRandomColor());
			g.setFont(new Font(null, Font.BOLD, FONT_SIZE));
			g.drawString(CHARS[n]+"", i*WIDTH/SIZE, 25);
			stringBuffer.append(CHARS[n]);
		}
		for(int i=0;i<LINES;i++){
			g.setColor(getRandomColor());
			g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), random.nextInt(WIDTH), random.nextInt(HEIGHT));
		}
		return new Object[]{image,stringBuffer.toString()};
		
	}
	public static Color getRandomColor(){
		Random r = new Random();
		Color color = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
		return color;
	}
}
