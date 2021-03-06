package plugins.Sharesite;

import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Font;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.io.*;

public class ActivelinkCreator {
	public static BufferedImage create(String name) throws IOException {
		String str=name.substring(0,Math.min(18,name.length()));
		InputStream is = Plugin.class.getClassLoader().getResourceAsStream("resources/templates/activelink.png");
		BufferedImage bg = ImageIO.read(is);
		BufferedImage out = new BufferedImage(108, 36, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ogr = out.createGraphics();

		// use existing image  as background
		ogr.drawImage(bg,0,0,null);

		// add text
		int width=0, height=0;
		int fontsize=28;
		do {
			Font font = new Font("Arial", Font.BOLD, fontsize);
			ogr.setFont(font);
			ogr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			FontMetrics metrics = ogr.getFontMetrics();

			width = metrics.stringWidth(str);
			height = metrics.getAscent();

			fontsize--;

		} while (fontsize>8 && (width > 100 || height > 30));

		ogr.setPaint(Color.black);
		ogr.drawString(str, 4, 28);


		//ImageIO.write(out, "PNG", new File("/tmp/test.png"));
		return out;
	}
}
