package com.sinosoft.filenet.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.sinosoft.filenet.FileEntry;
import com.sinosoft.filenet.FileHandler;
import com.sinosoft.filenet.FileIndex;
import com.sinosoft.util.ContentTypeHelper;
import com.sinosoft.util.CustomException;
import com.sinosoft.util.FileHelper;
import com.sinosoft.util.StringHelper;
import com.sinosoft.util.image.TIFFHelper;

/**
 * 用于处理EFiling上传文件的类,在EFiling上传单份资料时,系统会将上传的所有资料合并为一个tiff文档.
 * 合并后的TIFF文件大小可能会大于普通图片大小之和
 * 
 * @author LuoGang
 * 
 */
public class EFilingHandler implements FileHandler {
	/** 日志记录对象 */
	public static final Logger logger = LoggerFactory.getLogger(EFilingHandler.class);
	/** tiff文件的后缀名 */
	public static final String TIFF_SUFFIX = ".tiff";
	/** tiff文件的contentType */
	public static final String TIFF_CONTENT_TYPE = ContentTypeHelper.get(TIFF_SUFFIX);

	/**
	 * eFiling上传的档案资料会全部合并为一个TIFF文件
	 */
	public List<FileEntry> handle(final List<FileEntry> entries) {
		int count = entries.size();

		FileEntry fileEntry = entries.get(0);
		FileIndex fileIndex = fileEntry.getFileIndex();
		fileIndex.setId(StringHelper.uuid());
		String fileTitle = fileIndex.getFileTitle();
		if (StringHelper.isEmpty(fileTitle)) fileTitle = FileHelper.getSimpleFileName(fileIndex.getFileName());
		if (fileTitle.length() < 3) fileTitle = fileTitle + StringHelper.copy("_", 3 - fileTitle.length());
		fileIndex.setFileTitle(fileTitle);
		fileIndex.setFileId(fileIndex.getId());
		fileIndex.setFileCount(1);
		fileIndex.setPageCount(count);

		if (count == 1) return entries; // 一个文件不需要合并

		File[] files = new File[count];
		count = 0;
		for (FileEntry e : entries) {
			files[count++] = e.getFiles()[0];
		}
		File dest = null;
		try {
			dest = File.createTempFile(fileTitle, TIFF_SUFFIX);

			// TIFFEncodeParam.COMPRESSION_JPEG_TTN2格式进行压缩,可以减少生成的.tiff文件大小,但是会增加处理时间
			dest = TIFFHelper.toTIFF(files, dest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.warn("转换为TIFF出现异常", e);
			throw new CustomException(
					"将多个文件合并为TIFF时失败,可能是有的文件不是图片格式或者是不支持的图片格式!" + "请将文件转换为可识别的\"JPEG/BMP/GIF/PNG\"类型图片后重新上传!或者每次仅选择一个文件进行多次上传!");
		} finally {
			for (File file : files) {
				// 删除源文件
				file.delete();
			}
		}
		fileIndex.setFileSize(dest.length());
		fileIndex.setFileName(fileTitle + TIFF_SUFFIX);
		fileIndex.setFileContentType(TIFF_CONTENT_TYPE);

		fileEntry.setFiles(new File[] { dest });
		fileEntry.setFilesFileName(new String[] { fileIndex.getFileName() });
		fileEntry.setFilesContentType(new String[] { fileIndex.getFileContentType() });

		List<FileEntry> fileEntries = new ArrayList<FileEntry>();
		fileEntries.add(fileEntry);
		return fileEntries;
	}

	public static void main(String[] args) {
		File[] files = new File[] { new File("D:\\tiff\\1.jpg"), new File("D:\\tiff\\2.jpg") };
		TIFFHelper.toTIFF(files, new File("D:\\tiff\\x.tiff"));
	}
	// public static void many2one(String[] bookFilePaths, String toPath, String distFileName) {
	// if (bookFilePaths != null && bookFilePaths.length > 0) {
	// File[] files = new File[bookFilePaths.length];
	// for (int i = 0; i < bookFilePaths.length; i++) {
	// files[i] = new File("C:/export/10086/" + bookFilePaths[i]);
	// }
	// if (files != null && files.length > 0) {
	// try {
	// ArrayList pages = new ArrayList(files.length - 1);
	// FileSeekableStream[] stream = new FileSeekableStream[files.length];
	// for (int i = 0; i < files.length; i++) {
	// stream[i] = new FileSeekableStream(files[i].getCanonicalPath());
	// }
	// ParameterBlock pb = (new ParameterBlock());
	// PlanarImage firstPage = JAI.create("stream", stream[0]);
	// for (int i = 1; i < files.length; i++) {
	// PlanarImage page = JAI.create("stream", stream[i]);
	// pages.add(page);
	// }
	// TIFFEncodeParam param = new TIFFEncodeParam();
	// // boolean f = makeDirs(toPath);
	// // System.out.println(f);
	// OutputStream os = new FileOutputStream(toPath + File.separator + distFileName);
	// ImageEncoder enc = ImageCodec.createImageEncoder("tiff", os, param);
	// param.setExtraImages(pages.iterator());
	// enc.encode(firstPage);
	// for (int i = 0; i < files.length; i++) {
	// stream[i].close();
	// }
	// os.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }

}
