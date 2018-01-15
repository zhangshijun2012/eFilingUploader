package com.sinosoft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

/**
 * 文件处理
 * 
 * @author AnywnYu 2009-1-19 09:57
 */
public class FileHelper {

	/** .文件后缀名的点 */
	public static final String POINT = ".";

	/**
	 * 将filePath转换为当前系统下的标准文件路径,将/和\替换为{@link File#separator}
	 * 
	 * @param filePath
	 * @return
	 * 
	 * @see File#separator
	 */
	public static final String getFilePath(String filePath) {
		if (!File.separator.equals("/")) filePath = filePath.replace("/", File.separator);
		if (!File.separator.equals("\\")) filePath = filePath.replace("\\", File.separator);
		return filePath;
	}

	/**
	 * 得到文件后缀名,包括点号
	 * 
	 * @param fileName 文件名
	 * @return
	 */
	public static final String getFileSuffix(String fileName) {
		String path = getFilePath(fileName);
		int indexPoint = path.lastIndexOf(POINT);
		if (indexPoint <= 0) return ""; // 没有点号,则没有后缀名
		int indexSeparator = path.indexOf(File.separator, indexPoint);
		if (indexSeparator > -1) return ""; // 点号后面还发现了文件分隔符,即没有后缀名
		return path.substring(indexPoint); // 返回含有点号的后缀名
	}

	/**
	 * 得到文件的后缀名,包括点号
	 * 
	 * @param file 文件对象
	 * @return
	 */
	public static final String getFileSuffix(File file) {
		return getFileSuffix(file.getName());
	}

	/**
	 * 得到文件后缀名,不包括点号
	 * 
	 * @param fileName 文件名
	 * @return
	 */
	public static final String getSimpleFileSuffix(String fileName) {
		String fileSuffix = getFileSuffix(fileName);
		if (StringHelper.isEmpty(fileSuffix)) return fileSuffix;
		return fileSuffix.substring(1);
	}

	/**
	 * 
	 * 得到文件后缀名,不包括点号
	 * 
	 * @param file 文件对象
	 * @return
	 */
	public static final String getSimpleFileSuffix(File file) {
		String fileSuffix = getFileSuffix(file);
		if (StringHelper.isEmpty(fileSuffix)) return fileSuffix;
		return fileSuffix.substring(1);
	}

	public static final String DEFAULT_CONTENT_TYPE = StringHelper.trim(ContentTypeHelper.get(".*"), "application/octet-stream");

	/**
	 * 根据文件名得到文件的contentType
	 * 
	 * @param fileName
	 * @return
	 * 
	 * @see ContentTypeHelper#get(String)
	 */
	public static final String getContentType(String fileName) {
		String suffix = getFileSuffix(fileName);
		String contentType = ContentTypeHelper.get(suffix);
		return Helper.isEmpty(contentType) ? DEFAULT_CONTENT_TYPE : contentType;
	}

	/**
	 * 得到文件的content type
	 * 
	 * @param file
	 * @return
	 */
	public static final String getContentType(File file) {
		return FileHelper.getContentType(file.getName());
	}

	/**
	 * 得到文件名,不含后缀名
	 * 
	 * @param fileName
	 * @return
	 */
	public static final String getSimpleFileName(String fileName) {
		String path = getFilePath(fileName);
		int indexSeparator = path.lastIndexOf(File.separator) + 1;
		int indexPoint = path.lastIndexOf(POINT, path.length());
		if (indexPoint < indexSeparator) return path.substring(indexSeparator);
		return path.substring(indexSeparator, indexPoint);
	}

	/**
	 * 创建新文件,如果文件存在,则在后面加上fileName(i)的形式,直到得到一个不存在的新文件为止
	 * 
	 * @param filePath 要创建的文件全路径
	 * @return 返回的应当生成的文件.
	 *         注意此时文件尚未创建,需要外部调用 {@link File#createNewFile()} 方法进行创建
	 */
	public static final File createFile(String filePath) {
		File file = new File(filePath);
		int index = 1;
		String dir = null;
		String simpleFileName = null;
		String fileSuffix = null;
		while (file.exists()) {
			// 文件存在则继续创建
			if (dir == null) {
				dir = file.getParent() + File.separator;
				simpleFileName = getSimpleFileName(file.getName());
				fileSuffix = getFileSuffix(file.getName());
			}
			file = new File(dir + simpleFileName + "(" + (index++) + ")" + fileSuffix);
		}
		return file;
	}

	/** 进制,每个单位之间的基数 */
	public static final long RADIX = 1024;
	/** 字节 */
	public static final long B = 1;
	/** 1KB=1024B */
	public static final long KB = B * RADIX;
	/** 1MB=1024KB=1024*1024B */
	public static final long MB = KB * RADIX;
	/** GB */
	public static final long GB = MB * RADIX;
	/** TB */
	public static final long TB = GB * RADIX;
	/** 显示的文件单位 */
	public static final String[] UNITS = { "B", "KB", "MB", "GB", "TB" };

	/**
	 * 将文件大小格式化为最近单位的文件大小显示,显示的格式如:"0.###" KB
	 * 
	 * @param size
	 * @return
	 */
	public static String formatFileSize(double size) {
		int u = 0;
		String unit = UNITS[u++];
		double min = size;
		while (min > RADIX) {
			unit = UNITS[u++];
			min = min / RADIX;
		}
		return NumberHelper.format(min, "0.#") + " " + unit;
	}

	public static String formatFileSize(double size, String pattern) {
		int u = 0;
		String unit = UNITS[u++];
		double min = size;
		while (min > RADIX) {
			unit = UNITS[u++];
			min = min / RADIX;
		}
		return NumberHelper.format(min, pattern) + " " + unit;
	}

	/**
	 * 移动文件
	 * 
	 * @param src 源文件
	 * @param dest 目标文件
	 * @return
	 */
	public static boolean move(File src, File dest) {
		if (src.renameTo(dest)) return true; // 首先调用renameTo操作
		FileInputStream in = null;
		try {
			in = new FileInputStream(src);
			boolean result = write(dest, in);
			in.close();
			src.delete();
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new CustomException(e);
		} finally {
			MethodHelper.close(in);
		}
		// return false;
	}

	/**
	 * 移动文件src至destDir目录
	 * 
	 * @param src 源文件
	 * @param destDir 要移动到的目录
	 * @return
	 */
	public static boolean moveTo(File src, File destDir) {
		File dest = new File(destDir, src.getName());
		if (dest.exists()) throw new CustomException("目标文件:\"" + dest.getAbsolutePath() + "\"已经存在!");
		return move(src, dest);
	}

	/**
	 * 复制文件
	 * 
	 * @param src 源文件
	 * @param dest 目标文件
	 * @return
	 */
	public static boolean copy(File src, File dest) {
		FileInputStream in = null;
		try {
			in = new FileInputStream(src);
			boolean result = write(dest, in);
			return result;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new CustomException(e);
		} finally {
			MethodHelper.close(in);
		}
		// return false;
	}

	/**
	 * 复制文件src至destDir目录中
	 * 
	 * @param src 源文件
	 * @param destDir 要复制到的目录
	 * @return
	 */
	public static boolean copyTo(File src, File destDir) {
		File dest = new File(destDir, src.getName());
		if (dest.exists()) throw new CustomException("目标文件:\"" + dest.getAbsolutePath() + "\"已经存在!");
		return copy(src, dest);
	}

	/**
	 * 将输入流的数据写入file中
	 * 
	 * @param file
	 * @param in
	 * @return
	 */
	public static boolean write(File file, InputStream in) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			byte[] b = new byte[1024];
			int c = 0;
			while ((c = in.read(b)) > 0)
				out.write(b, 0, c);
			out.flush();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new CustomException(e);
		} finally {
			MethodHelper.close(out);
			// MethodHelper.close(in);
		}
		// return false;
	}

	// /**
	// * 得到文件的后缀名.如.txt
	// *
	// * @param fileName
	// * @return
	// */
	// public static String getType(String fileName) {
	// if (fileName == null || fileName.trim().equals("")) {
	// return null;
	// }
	// int start = Math.max(fileName.lastIndexOf("/"), fileName.lastIndexOf("\\"));
	// int end = fileName.lastIndexOf(".");
	// return (end > start) ? fileName.substring(end) : "";
	// }
	//
	// /**
	// * 得到file的后缀名
	// *
	// * @param file
	// * @return
	// */
	// public static String getType(File file) {
	// return file == null ? null : (file.isDirectory() ? "" : getType(file.getName()));
	// }
	//
	// /**
	// * 得到文件的名称,不含后缀名
	// *
	// * @param fileName
	// * @return
	// */
	// public static String getName(String fileName) {
	// if (fileName == null || fileName.trim().equals("")) {
	// return null;
	// }
	//
	// int start = Math.max(fileName.lastIndexOf("/"), fileName.lastIndexOf("\\")) + 1;
	// int end = fileName.lastIndexOf(".");
	// return (end == start) ? fileName : ((end > start) ? fileName.substring(start, end) : "");
	// }
	//
	// /**
	// * 得到file的名称,不含后缀名
	// *
	// * @param file
	// * @return
	// */
	// public static String getName(File file) {
	// return file == null ? null : (file.isDirectory() ? file.getName() : getName(file.getName()));
	// }

	/**
	 * 得到文件的内容,文件的大小最好不要超出2147483647(Integer.MAX_VALUE)字节
	 * 
	 * @param file 文件对象
	 * @param encoding 文件使用的编码
	 * @return
	 */
	public static String getText(File file, String encoding) {
		if (file == null || !file.exists() || !file.isFile()) { return null; }
		encoding = StringHelper.noEmpty(encoding, SystemHelper.ENCODING);
		StringBuffer text = new StringBuffer();
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(file), encoding);

			// reader = new FileReader(new FileInputStream(file));
			char[] cbuf = new char[1024];
			int c;
			while ((c = reader.read(cbuf)) != -1) {
				if (c > 0) text.append(cbuf, 0, c);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			MethodHelper.close(reader);
		}

		// byte[] content = null;
		// MappedByteBuffer inputBuffer = null;
		// FileInputStream fis = null;
		// FileChannel fileChannel = null;
		// try {
		// /* 读取大文件 */
		// fis = new FileInputStream(file);
		// fileChannel = fis.getChannel();
		// long maxSize = fileChannel.size();
		// inputBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, maxSize);
		// int length = 0;
		// while (inputBuffer.hasRemaining()) {
		// content = new byte[length = inputBuffer.remaining()];
		// inputBuffer.get(content, 0, length);
		// try {
		// text.append(new String(content, encoding));
		// } catch (UnsupportedEncodingException e) {
		// text.append(new String(content));
		// }
		// }
		// inputBuffer.clear();
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// } finally {
		// try {
		// if (fis != null) fis.close();
		// if (fileChannel != null) fileChannel.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// MethodHelper.close(fis);
		// MethodHelper.close(fileChannel);
		// }

		return text.toString();
	}

	/**
	 * 得到文件的内容
	 * 
	 * @param file 文件对象
	 * @return
	 */
	public static String getText(File file) {
		return getText(file, null);

	}

	/**
	 * 得到文件的内容
	 * 
	 * @param file 文件名称(路径)
	 * @param encoding 文件使用的编码
	 * @return
	 */
	public static String getText(String file, String encoding) {
		return file == null ? null : getText(new File(StringHelper.trim(file)), encoding);

	}

	/**
	 * 得到文件的内容
	 * 
	 * @param file 文件名称(路径)
	 * @return
	 */
	public static String getText(String file) {
		return getText(file, null);
	}

	/**
	 * 创建文件,若文件为目录则创建目录.
	 * 
	 * @param file
	 * @param isFile true:生成文件;false:生成目录
	 * @return 文件是否存在
	 */
	public static boolean create(File file, boolean isFile) {
		if (file == null) { return false; }
		if (!file.exists()) {
			if (!isFile) {
				file.mkdirs();
			} else {
				if (file.getParentFile() != null && !file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}

				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return file.exists() && ((file.isFile() && isFile) || (file.isDirectory() && !isFile));
	}

	/**
	 * 生成目录
	 * 
	 * @param file
	 * @return
	 */
	public static boolean createDirectory(File file) {
		return create(file, false);
	}

	/**
	 * 生成文件
	 * 
	 * @param file
	 * @return
	 */
	public static boolean create(File file) {
		return create(file, true);
	}

	/**
	 * 删除文件file，若为目录则删除其子目录和所有文件
	 * 
	 * @param file
	 * @return
	 */
	public static boolean delete(File file) {
		if (file == null || !file.exists()) { return true; }

		if (file.isDirectory()) {
			File[] files = file.listFiles();
			int l = files == null ? 0 : files.length;
			for (int i = 0; i < l; i++) {
				if (!delete(files[i])) { return false; }
			}
		}

		return file.delete();
	}

	/**
	 * 删除文件file，若为目录则删除其子目录和所有文件
	 * 
	 * @param file 文件路径
	 * @return
	 */
	public static boolean delete(String file) {
		return file == null ? true : delete(new File(file));
	}

	public static boolean print(File file, Object text) {
		return print(file, text, false);
	}

	public static boolean print(File file, Object text, boolean append) {
		return print(file, text, append, SystemHelper.ENCODING);
	}

	/**
	 * 将text写入文件
	 * 
	 * @param file
	 * @param text
	 * @param append 是否进行追加
	 * @param encoding 输出的编码
	 * @return
	 */
	public static boolean print(File file, Object text, boolean append, String encoding) {
		if (FileHelper.create(file)) {
			// FileWriter fw = null;
			// PrintWriter pw = null;
			OutputStreamWriter writer = null;
			try {
				writer = new OutputStreamWriter(new FileOutputStream(file, append), encoding);
				writer.write(text.toString());
				// fw = new FileWriter(file, append);
				// pw = new PrintWriter(fw, true);
				// pw.print(text);
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				MethodHelper.close(writer);
			}
		}
		return false;
	}

	/**
	 * 将对象o写入文件中
	 * 
	 * @param file
	 * @param o
	 * @param append 是否增加
	 * @return
	 */
	public static boolean writeObject(File file, Object o, boolean append) {
		FileOutputStream out = null;
		ObjectOutputStream objectOutputStream = null;

		try {
			out = new FileOutputStream(file, append);
			objectOutputStream = new ObjectOutputStream(out);

			objectOutputStream.writeObject(o);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (objectOutputStream != null) objectOutputStream.close();
				if (out != null) out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

	public static boolean writeObject(File file, Object o) {
		return writeObject(file, o, false);
	}

	/**
	 * 从文件中读取对象
	 * 
	 * @param file
	 * @param i 读取第i个对象
	 * @return
	 */
	public static Object readObject(File file, int i) {
		Object o = null;
		FileInputStream in = null;
		ObjectInputStream objectInputStream = null;
		try {
			in = new FileInputStream(file);
			objectInputStream = new ObjectInputStream(in);
			int j = -1;
			while (j < i) {
				o = objectInputStream.readObject();
				j++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (objectInputStream != null) objectInputStream.close();
				if (in != null) in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return o;
	}

	public static Object readObject(File file) {
		return readObject(file, 0);
	}

	public static void main(String[] args) {
		File dir = new File("D:\\work\\workspace\\libao\\src\\org\\anywnyu\\helper");

		for (File file : dir.listFiles()) {
			String text = getText(file, "UTF-8");
			text = text.replace("org.mortal.helper", "org.anywnyu.helper");
			// try {
			// text = new String(text.getBytes("utf-8"), SystemHelper.ENCODING_ISO_8859_1);
			// text = new String(text.getBytes(SystemHelper.ENCODING_ISO_8859_1), SystemHelper.ENCODING_GBK);
			// } catch (UnsupportedEncodingException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			FileHelper.print(file, text, false, SystemHelper.ENCODING_GBK);
		}

	}

}