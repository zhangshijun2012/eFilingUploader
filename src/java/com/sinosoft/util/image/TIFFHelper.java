package com.sinosoft.util.image;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import com.sinosoft.util.CustomException;
import com.sinosoft.util.FileHelper;
import com.sinosoft.util.MethodHelper;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

/**
 * 用于处理tiff图片
 * 
 * @author LuoGang
 *
 */
public class TIFFHelper {

	/** tiff */
	public static final String TIFF = "tiff";
	/** jpeg */
	public static final String JPEG = "jpeg";

	/** 依次尝试的压缩算法,COMPRESSION_JPEG_TTN2图片大小比较小,速度较慢 */
	static final int[] TRY_COMPRESSIONS = { TIFFEncodeParam.COMPRESSION_JPEG_TTN2, TIFFEncodeParam.COMPRESSION_DEFLATE };

	/**
	 * 将多个文件合并为tiff,未指定特殊的压缩算法,依次使用TRY_COMPRESSIONS定义的压缩算法，直到成功一个.如果都不成功，则不指定压缩参数
	 * 
	 * @param srcs 要合并的文件
	 * @param dest 合并后的tiff文件
	 * @return
	 */
	public static File toTIFF(File[] srcs, File dest) {
		for (int compression : TRY_COMPRESSIONS) {
			try {
				return toTIFF(srcs, dest, compression);
			} catch (Exception e) {
				// TODO
				// e.printStackTrace();
			}
		}

		return toTIFF(srcs, dest, null);
	}

	/**
	 * 将多个文件合并为tiff文件
	 * 
	 * @param srcs 要合并的文件
	 * @param dest 合并后的tiff文件
	 * @param compression tiff的压缩算法,为null则不传参数
	 */
	public static File toTIFF(File[] srcs, File dest, Integer compression) {
		OutputStream out = null;
		List<FileSeekableStream> streams = new ArrayList<FileSeekableStream>();
		try {
			out = new FileOutputStream(dest);
			TIFFEncodeParam param = new TIFFEncodeParam();
			if (compression != null) param.setCompression(compression);
			FileSeekableStream stream;
			int l = srcs.length;
			if (l > 1) {
				List<RenderedOp> pages = new ArrayList<RenderedOp>();
				for (int i = 1; i < l; i++) {
					stream = new FileSeekableStream(srcs[i]);
					RenderedOp page = JAI.create("stream", stream);
					pages.add(page);

					streams.add(stream);
				}
				param.setExtraImages(pages.iterator());
			}

			stream = new FileSeekableStream(srcs[0]);
			streams.add(stream);
			ImageEncoder enc = ImageCodec.createImageEncoder(TIFF, out, param);
			enc.encode(JAI.create("stream", stream));

			stream.close();
			out.close();
		} catch (IOException e) {
			throw new CustomException(e);
		} finally {
			MethodHelper.close(out);
			for (FileSeekableStream s : streams) {
				MethodHelper.close(s);
			}
		}
		return dest;
	}

	/**
	 * 将tiff图片转换为imgFile，使用imgFile的后缀作为转换格式,压缩质量默认为0.75
	 * 
	 * @param tiffFile
	 * @param imgFile 图片文件，必须是正确的后缀
	 * @return imgFile
	 */
	public static File toIMG(File tiffFile, File imgFile) {
		return toIMG(tiffFile, imgFile, FileHelper.getSimpleFileSuffix(imgFile), 0.5f);
	}

	/**
	 * 将tiff图片转换为imgFile，使用formatName作为转换格式,压缩质量默认为0.5
	 * 
	 * @param tiffFile
	 * @param imgFile 图片文件
	 * @param formatName 图片格式
	 * @return imgFile
	 */
	public static File toIMG(File tiffFile, File imgFile, String formatName) {
		return toIMG(tiffFile, imgFile, formatName, 0.5f);
	}

	/**
	 * 将多页tiff拆分后合并为一个图片
	 * 
	 * @param tiffFile
	 * @param imgFile 要保存的文件
	 * @param formatName 图片格式
	 * @param quality 压缩质量，如果格式不支持则此参数无效
	 * @return imgFile
	 */
	public static File toIMG(File tiffFile, File imgFile, String formatName, float quality) {
		FileSeekableStream stream = null;
		ImageOutputStream out = null;
		try {
			stream = new FileSeekableStream(tiffFile);
			ImageDecoder decoder = ImageCodec.createImageDecoder(TIFF, stream, null);

			int count = decoder.getNumPages();

			int width = 0;
			int height = 0;
			int gap = 2; // 每张图片之间的间隙
			List<RenderedImage> pages = new ArrayList<RenderedImage>();
			for (int i = 0; i < count; i++) {
				RenderedImage page = decoder.decodeAsRenderedImage(i);
				pages.add(page);
				width = Math.max(width, page.getWidth());
				height += page.getHeight();
				if (i > 0) height += gap;
			}

			BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			// 获取图形上下文
			Graphics2D grap = tag.createGraphics();
			if ("png".equalsIgnoreCase(formatName)) {
				// 透明背景
				tag = grap.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
				grap.dispose();
				grap = tag.createGraphics();
			} else {
				// 调用fillRect后背景为白色，不调用则背景为黑色
				// grap.fillRect(0, 0, width, height);
			}

			int x;
			int y = 0;
			Iterator<RenderedImage> it = pages.iterator();
			for (int i = 0; i < count; i++) {
				RenderedImage page = it.next();
				it.remove();
				x = (width - page.getWidth()) / 2;
				grap.drawImage(convertRenderedImage(page), x, y, null);
				y = y + page.getHeight() + gap;
				grap.dispose();
			}
			pages = null;
			stream.close();

			ImageWriter writer = ImageHelper.getImageWriter(formatName);
			out = ImageIO.createImageOutputStream(imgFile);
			writer.setOutput(out);
			IIOImage iIamge = new IIOImage(tag, null, null);
			ImageWriteParam iwp = writer.getDefaultWriteParam();
			if (iwp.canWriteCompressed()) {
				// 可压缩
				int compressionMode = iwp.getCompressionMode();
				try {
					iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); // 设置可否压缩
					iwp.setCompressionQuality(quality); // 设置压缩质量参数
				} catch (Exception e) {
					// TODO
					iwp.setCompressionMode(compressionMode);
				}
			}
			writer.write(null, iIamge, iwp);

			out.flush();

			writer.dispose();

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			MethodHelper.close(stream);
			MethodHelper.close(out);
		}
		return imgFile;
	}

	/**
	 * 将读取的RenderedImage转换为BufferedImage
	 * 
	 * @param img
	 * @return
	 */
	public static BufferedImage convertRenderedImage(RenderedImage img) {
		if (img instanceof BufferedImage) { return (BufferedImage) img; }
		ColorModel cm = img.getColorModel();
		WritableRaster raster = cm.createCompatibleWritableRaster(img.getWidth(), img.getHeight());
		Properties props = new Properties();
		String[] keys = img.getPropertyNames();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				props.put(keys[i], img.getProperty(keys[i]));
			}
		}
		img.copyData(raster);
		BufferedImage result = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), props);
		return result;
	}

}
