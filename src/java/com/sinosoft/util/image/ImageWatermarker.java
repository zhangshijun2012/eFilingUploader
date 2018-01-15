package com.sinosoft.util.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import com.sinosoft.util.FileHelper;
import com.sinosoft.util.MethodHelper;

/**
 * 图片添加水印工具类
 * 
 * @author LuoGang
 * 
 */
public class ImageWatermarker {
	/** 要添加水印的图片文件 */
	private File input;
	/** 添加水印后的图片文件 */
	private File output;
	/** 要添加的水印对象，可以为图片文件或者文字 */
	private Object watermark;
	/** 如果水印为文字，指定文字的字体 */
	private Font font;
	/** 如果水印为文字，指定文字的颜色 */
	private Color color;
	/** 添加水印的x坐标 */
	private int x = -1;
	/** 添加水印的y坐标 */
	private int y = -1;

	/**
	 * 按图片文件构造对象
	 * 
	 * @param input 图片文件
	 * @param watermark 水印
	 */
	public ImageWatermarker(File input, Object watermark) {
		super();
		this.input = input;
		this.output = input;
		this.watermark = watermark;
	}

	public ImageWatermarker(File input, Object watermark, int x, int y) {
		this(input, watermark);
		this.x = x;
		this.y = y;
	}

	/**
	 * 按图片文件构造对象
	 * 
	 * @param input 图片文件
	 * @param output 加水印后输出的图片文件
	 * @param watermark 水印
	 */
	public ImageWatermarker(File input, File output, Object watermark) {
		super();
		this.input = input;
		this.output = output;
		this.watermark = watermark;
	}

	public ImageWatermarker(File input, File output, Object watermark, int x, int y) {
		super();
		this.input = input;
		this.output = output;
		this.watermark = watermark;
		this.x = x;
		this.y = y;
	}

	/**
	 * 给文件加文字
	 * 
	 * @param input
	 * @param output
	 * @param watermark
	 * @param font
	 * @param color
	 * @param x
	 * @param y
	 */
	public ImageWatermarker(File input, File output, String watermark, Font font, Color color, int x, int y) {
		super();
		this.input = input;
		this.output = output;
		this.watermark = watermark;
		this.font = font;
		this.color = color;
		this.x = x;
		this.y = y;
	}

	public void mark() throws IOException {
		FileOutputStream out = null;
		try {
			BufferedImage image = ImageIO.read(input);
			Graphics grap = image.getGraphics();
			int x = this.x;
			int y = this.y;
			if (watermark instanceof String) {
				// 加文字
				String logo = (String) watermark;
				if (font != null) grap.setFont(font);
				if (color != null) grap.setColor(color);
				if (x == -1 && y == -1) {
					// 未指定水印图片添加的为止，则添加在右下脚
					FontMetrics fm = grap.getFontMetrics();
					Rectangle2D bound = fm.getStringBounds(logo, grap);
					x = (int) (image.getWidth(null) - bound.getWidth());
					y = (int) (image.getHeight(null) - bound.getHeight());
				}

				x = Math.max(x, 0);
				y = Math.max(y, 0);
				grap.drawString(logo, x, y);
			} else {
				Image logo;
				if (watermark instanceof File) {
					logo = ImageIO.read((File) watermark);
				} else if (watermark instanceof InputStream) {
					logo = ImageIO.read((InputStream) watermark);
				} else if (watermark instanceof URL) {
					logo = ImageIO.read((URL) watermark);
				} else if (watermark instanceof ImageInputStream) {
					logo = ImageIO.read((ImageInputStream) watermark);
				} else if (watermark instanceof Image) {
					logo = (Image) watermark;
				} else {
					throw new RuntimeException(watermark.getClass() + "无法转换为Image!");
				}

				if (x == -1 && y == -1) {
					// 未指定水印图片添加的为止，则添加在右下脚
					x = image.getWidth(null) - logo.getWidth(null);
					y = image.getHeight(null) - logo.getHeight(null);
				}
				x = Math.max(x, 0);
				y = Math.max(y, 0);

				grap.drawImage(logo, x, y, null);
			}

			grap.dispose();

			out = new FileOutputStream(output);
			// JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			// JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
			// param.setQuality(1.0F, true);
			// encoder.encode(image);
			ImageIO.write(image, FileHelper.getSimpleFileSuffix(output), out);
		} finally {
			MethodHelper.close(out);
		}
	}

	public File getInput() {
		return input;
	}

	public void setInput(File input) {
		this.input = input;
	}

	public File getOutput() {
		return output;
	}

	public void setOutput(File output) {
		this.output = output;
	}

	public Object getWatermark() {
		return watermark;
	}

	public void setWatermark(Object watermark) {
		this.watermark = watermark;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public static void main(String[] arg) {
		ImageWatermarker w = new ImageWatermarker(new File("d:\\fq.jpg"), new File("d:\\fq2.jpg"), "哈河xffg鞍山道街日体育教育X");
		try {
			w.setColor(Color.white);
			w.mark();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
