package com.sinosoft.util.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import com.sinosoft.util.NumberHelper;
import com.sinosoft.util.StringHelper;

/**
 * 图片处理工具
 * 
 * @author LuoGang
 * 
 */
public class ImageHelper {

	/**
	 * 压缩图片
	 * 
	 * @param src 原图片
	 * @param dest 压缩后的图片
	 * @param width 压缩后的宽度
	 * @param height 压缩后的高度
	 * @param fixedAspectRatio 是否等比压缩,为true则压缩后的图片比例不变
	 * @return
	 */
	public static void compress(File src, File dest, int width, int height, boolean fixedAspectRatio) {
		ImageCompressor compressor = new ImageCompressor(src, dest, width, height);
		compressor.setFixedAspectRatio(fixedAspectRatio);
		try {
			compressor.compress();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 压缩图片
	 * 
	 * @param srcName 图片文件名(含路径)
	 * @param destName 输出图片名(含路径)
	 * @param width 压缩后的宽度
	 * @param height 压缩后的高度
	 * @param fixedAspectRatio 是否等比压缩,为true则压缩后的图片比例不变
	 * @return
	 */
	public static void compress(String srcName, String destName, int width, int height, boolean fixedAspectRatio) {
		compress(new File(srcName), new File(destName), width, height, fixedAspectRatio);
	}

	/**
	 * 等比压缩图片
	 * 
	 * @param src
	 * @param dest
	 * @param width
	 * @param height
	 * @return
	 */
	public static void compress(File src, File dest, int width, int height) {
		compress(src, dest, width, height, true);
	}

	/**
	 * 等比压缩图片
	 * 
	 * @param srcName 图片文件名(含路径)
	 * @param destName 输出图片名(含路径)
	 * @param width
	 * @param height
	 * @return
	 */
	public static void compress(String srcName, String destName, int width, int height) {
		compress(srcName, destName, width, height, true);
	}

	/**
	 * 压缩图片的质量
	 * 
	 * @param src
	 * @param dest
	 * @param formatName 图片格式
	 * @param ratio
	 * @param quality
	 */
	public static void compress(File src, File dest, String formatName, double ratio, float quality) {
		ImageCompressor compressor = new ImageCompressor(src, dest, ratio);
		compressor.setFormatName(formatName);
		compressor.setQuality(quality);
		try {
			compressor.compress();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 压缩图片的质量，按比例改变图片的大小
	 * 
	 * @param src
	 * @param dest
	 * @param ratio 图片宽高压缩比例
	 * @param quality 压缩后的图片质量
	 */
	public static void compress(File src, File dest, double ratio, float quality) {
		ImageCompressor compressor = new ImageCompressor(src, dest, ratio);
		compressor.setQuality(quality);
		try {
			compressor.compress();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 压缩图片的质量，不改变图片的大小.图片格式默认为dest的后缀名
	 * 
	 * @param src
	 * @param dest
	 * @param quality 压缩后的图片质量
	 */
	public static void compress(File src, File dest, float quality) {
		compress(src, dest, 1.0, quality);
	}

	/**
	 * 压缩图片的质量，不改变图片的大小
	 * 
	 * @param src
	 * @param dest
	 * @param formatName 图片格式
	 * @param quality
	 */
	public static void compress(File src, File dest, String formatName, float quality) {
		compress(src, dest, formatName, 1.0, quality);
	}

	public static void write(BufferedImage img, File file) {

	}

	/**
	 * 判断是否为图片类
	 * 
	 * @param contentType
	 * @return
	 */
	public static boolean isImageContentType(String contentType) {
		if (StringHelper.isEmpty(contentType)) return false;
		contentType = contentType.toLowerCase();
		return (contentType.startsWith("image") || contentType.startsWith("img/"));
	}

	/**
	 * 根据图片格式得到ImageWriter
	 * 
	 * @param format
	 * @return
	 */
	public static ImageWriter getImageWriter(String format) {
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(format);
		if (!iter.hasNext()) {
			iter = ImageIO.getImageWritersBySuffix(format);
		}
		ImageWriter writer = (ImageWriter) iter.next(); // 得到writer
		while (iter.hasNext()) {
			((ImageWriter) iter.next()).dispose();
		}
		return writer;
	}

	// main测试
	// compressPic(大图片路径,生成小图片路径,大图片文件名,生成小图片文名,生成小图片宽度,生成小图片高度,是否等比缩放(默认为true))
	public static void main(String[] arg) {
		long ms = System.currentTimeMillis();
		TIFFHelper.toIMG(new File("d:\\xxx.tiff"), new File("d:\\y.jpg"), "jpeg", 0.1f);
		System.out.println(System.currentTimeMillis() - ms);
		System.exit(0);
		ImageHelper.compress(new File("d:\\1.png"), new File("d:\\2.png"), 0.5f);
		// TIFFHelper.random();
		// System.exit(0);
		File folder = null;
		File file = null;
		File[] files = null;
		long time = System.currentTimeMillis();
		//
		folder = new File("d:\\");
		files = new File[] { new File(folder, "1.png"), new File(folder, "2.png"), new File(folder, "lz.jpg") };
		// // time = System.currentTimeMillis();
		// // file = ImageHelper.toTIFF(files, new File("D:\\vc2.tiff"));
		// // time = System.currentTimeMillis() - time;
		// // System.out.println("不压缩，耗时：" + time + "ms, 文件大小：" + NumberHelper.format(file.length() / 1024.0 / 1024.0) + "M");
		//
		// // folder = new File("F:\\LG\\LuoGang\\图片\\");
		// // files = new File[] { new File(folder, "显卡.png"), new File(folder, "显卡.png"), new File(folder, "显卡.png") };
		time = System.currentTimeMillis();
		file = TIFFHelper.toTIFF(files, file = new File("D:\\vc3.tiff"));
		time = System.currentTimeMillis() - time;
		System.out.println("不压缩，耗时：" + time + "ms, 文件大小：" + NumberHelper.format(file.length() / 1024.0 / 1024.0) + "M");

	}
}
