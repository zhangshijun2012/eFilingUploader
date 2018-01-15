package com.sinosoft.util.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sinosoft.util.FileHelper;
import com.sinosoft.util.MethodHelper;
import com.sinosoft.util.StringHelper;

/**
 * 图片压缩工具类
 * 
 * @author LuoGang
 * 
 */
public class ImageCompressor {
	/** log4j2日志记录对象 */
	protected static final Logger logger = LogManager.getLogger(ImageCompressor.class);
	/** 默认的输出文件格式 */
	public static final String DEFAULT_FORMAT_NAME = "png";
	/** 外部注入的img对象 */
	private BufferedImage img;
	/** 要压缩的源文件 */
	private File input;
	/** 压缩后的文件 */
	private File output;
	/** 输出文件的格式，如果为指定，则默认使用output的后缀名 */
	private String formatName;
	/** 压缩图片背景色 */
	private Color background;
	/** 压缩后的宽度,画布宽度 */
	private int width;
	/** 压缩后高度,画布高度 */
	private int height;

	/**
	 * 图片内容是否保持纵横比，如果为true,则会根据width/height的比例，保持压缩最大的比例.
	 * 例如，图片大小为1200*1000，指定压缩后为100*100，如果保持纵横比，
	 * 因为宽度的压缩比为12，大于高度的压缩比10，所以压缩后的图片为100*(1000/12)
	 */
	private boolean fixedAspectRatio;

	/**
	 * 得到的图片是否固定为width*height.
	 * 此参数与fixedAspectRatio不一样，此参数用于确定图片的画布大小，
	 * 如果此参数为false，则画布与图片内容是一致的，否则可能此画布比图片内容大.
	 * 如果此参数为true，则必须指定width和height参数
	 */
	private boolean fixed;

	/** 压缩比例,如果此参数0<ratio<=1,则是按比例进行压缩，忽略width，height，aspectRatio参数.如果此参数为0则必须指定width和height参数 */
	private double ratio;

	/** 图片的压缩质量，默认为0.5 */
	private float quality = 0.5F;

	/**
	 * 压缩图片的类型，默认为BufferedImage.TYPE_CUSTOM,采用来源图片的type
	 * 
	 * @see BufferedImage#TYPE_CUSTOM
	 */
	private int imageType = BufferedImage.TYPE_CUSTOM;
	/**
	 * 图片缩略算法。 默认值为Image.SCALE_SMOOTH
	 * 
	 * @see Image#getScaledInstance(int, int, int)
	 */
	private int hints = Image.SCALE_AREA_AVERAGING; // .SCALE_SMOOTH;

	/** 为压缩图片加水印 */
	private ImageWatermarker watermarker;

	public ImageCompressor() {
		super();
	}

	/**
	 * 将图片压缩为固定的宽或高，如果有一个为0，则表示按照另一个保持纵横比
	 * 
	 * @param input
	 * @param output
	 * @param width
	 * @param height
	 */
	public ImageCompressor(File input, File output, int width, int height) {
		this();
		this.input = input;
		this.output = output;
		this.width = width;
		this.height = height;
		this.fixed = true;
	}

	/**
	 * 按比例压缩图片
	 * 
	 * @param input
	 * @param output
	 * @param ratio 压缩比例
	 */
	public ImageCompressor(File input, File output, double ratio) {
		this();
		this.input = input;
		this.output = output;
		this.ratio = ratio;
	}

	/**
	 * 得到ImageWriter对象
	 * 
	 * @return
	 */
	protected ImageWriter getImageWriter() {
		String format = formatName;
		if (StringHelper.isEmpty(formatName)) {
			format = FileHelper.getSimpleFileSuffix(output);
			if (StringHelper.isEmpty(format) || "tmp".equalsIgnoreCase(format)
					|| !ImageHelper.isImageContentType(FileHelper.getContentType(format))) {
				format = DEFAULT_FORMAT_NAME;
			}
		}
		logger.info("formatName=" + format + ",file=" + output.getAbsolutePath());
		// System.out.println("formatName=" + format + "," + output.getAbsolutePath());
		// Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(format);
		// if (!iter.hasNext()) {
		// iter = ImageIO.getImageWritersBySuffix(format);
		// }
		// ImageWriter writer = (ImageWriter) iter.next(); // 得到writer
		// return writer;
		return ImageHelper.getImageWriter(format);
	}

	/**
	 * 压缩图片
	 * 
	 * @return 是否压缩成功
	 * @throws IOException
	 */
	public void compress() throws IOException {
		if (this.img == null) {
			img = ImageIO.read(input);
		}
		int x = 0;
		int y = 0;
		int imgWidth = img.getWidth(null); // 图像高度
		int imgHeight = img.getHeight(null); // 图像宽度
		final int srcImgWidth = imgWidth;
		final int srcImgHeight = imgWidth;
		if (ratio != 1) { // 改变长宽
			if (this.ratio == 0) {// width，height不能全部为0
				if (this.fixedAspectRatio) { // width，height必须全部不为0
					// 为等比缩放计算输出的图片宽度及高度
					double rateWidth = ((double) imgWidth) / (double) width;
					double rateHeight = ((double) imgHeight) / (double) height;
					// 根据缩放比率大的进行缩放控制
					if (rateWidth > rateHeight) {
						imgHeight = imgHeight * width / imgWidth;
					} else if (rateWidth < rateHeight) {
						imgWidth = imgWidth * height / imgHeight;
					}
				}
			} else {// 按比例压缩
				imgWidth = (int) (imgWidth * this.ratio);
				imgHeight = (int) (imgHeight * this.ratio);
			}
		}

		int width = this.width; // 画布宽度
		int height = this.height; // 画布高度
		if (!this.fixed || (width <= 0 && height <= 0)) {
			// 未指定画布具体高度和宽度,则以图片大小作为画布大小
			width = imgWidth;
			height = imgHeight;
		} else {
			// fixed为true，width和height至少有一个不为0
			if (width <= 0) {
				width = srcImgWidth * imgHeight / srcImgHeight;
			} else if (height <= 0) {
				height = srcImgHeight * imgWidth / srcImgWidth;
			}
		}

		Image from = img;
		if (srcImgWidth == imgWidth && srcImgHeight == imgHeight) {
			// 未改变图片大小
		} else {
			/* Image.SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的 优先级比速度高 生成的图片质量比较好 但速度慢 */
			from = img.getScaledInstance(imgWidth, imgHeight, this.hints);
		}

		x = (width - imgWidth) / 2;
		y = (height - imgHeight) / 2;

		int type = BufferedImage.TYPE_CUSTOM == this.imageType ? img.getType() : this.imageType;
		BufferedImage tag = new BufferedImage(width, height, type);
		Graphics2D grap = tag.createGraphics();
		// tag = grap.getDeviceConfiguration().createCompatibleImage(newWidth, newHeight, Transparency.TRANSLUCENT);
		// grap.dispose();
		// grap = tag.createGraphics();

		if (background != null) grap.setBackground(this.background);

		grap.drawImage(from, x, y, null);
		grap.dispose();

		// String format = StringHelper.isEmpty(formatName) ? FileHelper.getSimpleFileSuffix(output) : formatName;
		// if (StringHelper.isEmpty(format) || "tmp".equalsIgnoreCase(format)) {
		// format = DEFAULT_FORMAT_NAME;
		// }
		// logger.info("formatName={},file={}", format, output.getAbsolutePath());
		// System.out.println("formatName=" + format + "," + output.getAbsolutePath());
		// Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(format);
		// if (!iter.hasNext()) {
		// iter = ImageIO.getImageWritersBySuffix(format);
		// }
		ImageWriter writer = this.getImageWriter();

		// 得到指定writer的输出参数设置(ImageWriteParam )
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		if (iwp.canWriteCompressed()) {
			// 可压缩
			int compressionMode = iwp.getCompressionMode();
			try {

				iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); // 设置可否压缩
				// if (iwp.getCompressionTypes() != null && iwp.getCompressionType() == null) {
				// iwp.setCompressionType(iwp.getCompressionTypes()[0]);
				// }
				iwp.setCompressionQuality(quality); // 设置压缩质量参数
			} catch (Exception e) {
				// TODO
				iwp.setCompressionMode(compressionMode);
			}
		}
		// if (iwp.canWriteProgressive()) {
		// iwp.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
		// }
		// ColorModel colorModel = ColorModel.getRGBdefault();
		//
		// // 指定压缩时使用的色彩模式
		// iwp.setDestinationType(new ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));

		// // // 开始打包图片，写入byte[]
		// ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // 取得内存输出流
		// IIOImage iIamge = new IIOImage(img, null, null);
		// // 此处因为ImageWriter中用来接收write信息的output要求必须是ImageOutput
		// // 通过ImageIo中的静态方法，得到byteArrayOutputStream的ImageOutput
		// writer.setOutput(ImageIO.createImageOutputStream(output));
		// writer.write(null, iIamge, iwp);
		//
		// tag = ImageIO.read(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

		ImageOutputStream out = null;
		try {
			out = ImageIO.createImageOutputStream(output);

			writer.setOutput(out);
			IIOImage iIamge = new IIOImage(tag, null, null);
			// 此处因为ImageWriter中用来接收write信息的output要求必须是ImageOutput
			// 通过ImageIo中的静态方法，得到byteArrayOutputStream的ImageOutput
			// writer.setOutput(output);
			writer.write(null, iIamge, iwp);

			// ImageIO.write(tag, FileHelper.getSimpleFileSuffix(input), output);
			// JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			// JPEGEncodeParam param = JPEGCodec.getDefaultJPEGEncodeParam(tag);
			// param.setQuality(quality, true);
			// // encoder.setJPEGEncodeParam(param);
			// encoder.encode(tag, param);
			writer.dispose();
			out.flush();
			out.close();
		} finally {
			MethodHelper.close(out);
		}

		if (watermarker != null) {
			// 为压缩后的图片加水印
			watermarker.setInput(output);
			watermarker.setOutput(output);
			watermarker.mark();
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

	public Color getBackground() {
		return background;
	}

	public void setBackground(Color background) {
		this.background = background;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isFixedAspectRatio() {
		return fixedAspectRatio;
	}

	public void setFixedAspectRatio(boolean fixedAspectRatio) {
		this.fixedAspectRatio = fixedAspectRatio;
	}

	public boolean isFixed() {
		return fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	public float getQuality() {
		return quality;
	}

	public void setQuality(float quality) {
		this.quality = quality;
	}

	public int getImageType() {
		return imageType;
	}

	public void setImageType(int imageType) {
		this.imageType = imageType;
	}

	public int getHints() {
		return hints;
	}

	public void setHints(int hints) {
		this.hints = hints;
	}

	public ImageWatermarker getWatermarker() {
		return watermarker;
	}

	public void setWatermarker(ImageWatermarker watermarker) {
		this.watermarker = watermarker;
	}

	public String getFormatName() {
		return formatName;
	}

	public void setFormatName(String formatName) {
		this.formatName = formatName;
	}

	public BufferedImage getImg() {
		return img;
	}

	public void setImg(BufferedImage img) {
		this.img = img;
	}

	//
	// public static void main(String[] arg) {
	// ImageCompressor w = new ImageCompressor(new File("d:\\x.png"), new File("d:\\xxw.png"), 100, 100, true);
	// try {
	// w.setBackground(Color.black);
	// w.compress();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
}
