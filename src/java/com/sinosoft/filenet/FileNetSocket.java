package com.sinosoft.filenet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.sinosoft.util.CustomException;
import com.sinosoft.util.FileHelper;
import com.sinosoft.util.LoggableImpl;
import com.sinosoft.util.MethodHelper;
import com.sinosoft.util.StringHelper;

public class FileNetSocket extends LoggableImpl {
	private String host;
	private int port;
	private Socket socket;
	private OutputStream out;
	private InputStream in;

	public FileNetSocket() {
		host = FileNetHelper.SOCKET_HOST;
		port = FileNetHelper.SOCKET_PORT;

		// connect();
	}

	/**
	 * 创建Socket连接
	 * 
	 * @return
	 */
	public Socket connect() {
		if (socket != null) return socket;
		try {
			getLogger().info("socket=" + host + ":" + port);
			socket = new Socket(host, port);
			out = socket.getOutputStream();
			in = socket.getInputStream();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.getLogger().error("socket连接失败", e);
			throw new RuntimeException(e);
		}
		return socket;
	}

	/** 关闭连接 */
	public void close() {
		if (socket != null) try {
			MethodHelper.close(out);
			MethodHelper.close(in);

			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket = null;
	}

	/**
	 * 保存多个文件对象
	 * 
	 * @param entries
	 * @return
	 */
	public FileEntry[] save(FileEntry[] entries) {
		close();
		port = FileNetHelper.SOCKET_PORT_UPLOAD; // 使用上传端口
		for (FileEntry fileEntry : entries) {
			save(fileEntry);
		}
		getLogger().debug("FileNetSocket.save(FileEntry[]) success!");
		return entries;
	}

	/**
	 * 保存文件到FileNet中
	 * 
	 * @param fileEntry 使用socket上传的文件,此对象的files只允许有一个
	 */
	public FileEntry save(FileEntry fileEntry) {
		try {
			connect();
			FileIndex fileIndex = fileEntry.getFileIndex();
			if (StringHelper.isEmpty(fileIndex.getId())) fileIndex.setId(StringHelper.uuid());
			if (StringHelper.isEmpty(fileIndex.getFileId())) fileIndex.setFileId(fileIndex.getId());
			if (StringHelper.isEmpty(fileIndex.getFileNo())) fileIndex.setFileNo(fileIndex.getFileId());
			String fileId = fileIndex.getFileId();
			String fileName = fileIndex.getFileName();
			write(fileName);
			String content = fileId;
			write(content);
			write(fileEntry.getFiles()[0]);

			String result = readResult();
			if (!StringHelper.parseBoolean(result)) {
				getLogger().debug("FileNetSocket.save(FileEntry) failure.return false!id=" + fileId);
				throw new CustomException("上传失败,服务端返回false");
			}
			getLogger().debug("FileNetSocket.save(FileEntry) success!id=" + fileId);
		} finally {
			close();
		}

		return fileEntry;
	}

	/**
	 * 输出内容
	 * 
	 * @param content
	 */
	public void write(String content) {
		byte[] data = content.getBytes();
		try {
			out.write(toBytes(data.length));
			out.write(data);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			getLogger().error("FileNetSocket.write(String) 异常!", e);
		}
	}

	/**
	 * 输出文件
	 * 
	 * @param file
	 */
	public void write(File file) {
		FileInputStream in = null;
		try {
			out.write(toBytes(file.length()));
			byte[] buffer = new byte[4096];
			int size;
			in = new FileInputStream(file);
			while ((size = in.read(buffer)) != -1) {
				out.write(buffer, 0, size);
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			getLogger().error("FileNetSocket.write(File) 异常!", e);
		} finally {
			MethodHelper.close(in);
		}
	}

	/**
	 * 文件上传后读取处理结果
	 * 
	 * @return
	 */
	public String readResult() {
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		int count = 0;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			char[] c = new char[1024];
			while ((count = reader.read(c)) != -1) {
				buffer.append(c, 0, count);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			getLogger().error("FileNetSocket.readResult() 异常!", e);
		} finally {
			MethodHelper.close(reader);
		}
		return buffer.toString();
	}

	/**
	 * 从FileNet读取 第一个文件并保存写入file文件中
	 * 
	 * @param id
	 * @param file
	 * @return
	 */
	public File read(String id, File file) {
		try {
			close();
			port = FileNetHelper.SOCKET_PORT_DOWNLOAD; // 使用下载端口
			connect();
			write(id);
			int length = 4;
			byte[] bytes = new byte[length];
			read(bytes);
			length = toInteger(bytes);
			bytes = new byte[length];
			read(bytes); // 读取文件名,这里不需要

			read(file);
		} finally {
			close();
		}
		// FileHelper.write(file, in);
		return file;
	}

	/**
	 * 读取socket流,输出到文件file
	 * 
	 * @param file
	 */
	public void read(File file) {
		try {
			FileHelper.write(file, in);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			getLogger().error("FileNetSocket.read(File) 异常!", e);
		} finally {
			// MethodHelper.close(in);
		}
	}

	/**
	 * 读取数据放入bytes数组中
	 * 
	 * @param bytes
	 * @return 读取到的bytes数,如果读到了结尾,则可能小于bytes.length
	 * @see #read(byte[], int, int)
	 */
	public int read(byte[] bytes) {
		return this.read(bytes, 0, bytes.length);
	}

	/**
	 * 
	 * 读取lengh个bytes
	 * 
	 * @param bytes
	 * @param off 放入bytes数组中的起始位置
	 * @param length
	 * @return 读取到的bytes数,如果读到了结尾,则可能小于length
	 */
	public int read(byte[] bytes, int off, int length) {
		// byte[] bytes = new byte[length];
		int count = 0;
		int c = 0;
		while (c != -1 && count < length) {
			try {
				c = in.read(bytes, off + count, length - count);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				getLogger().error("FileNetSocket.read(int) 异常!", e);
			}
			if (c == -1) break; // 已经读到末尾
			count += c;
		}
		return count;
	}

	/**
	 * 将int整数转换为4位的byte数组
	 * 
	 * @param integer
	 * @return
	 */
	public static byte[] toBytes(int integer) {
		return new byte[] { (byte) ((integer >> 24) & 0xFF), (byte) ((integer >> 16) & 0xFF),
				(byte) ((integer >> 8) & 0xFF), (byte) (integer & 0xFF) };
	}

	/**
	 * 将long整数转换为8位的byte数组
	 * 
	 * @param lng
	 * @return
	 */
	public static byte[] toBytes(long lng) {
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) {
			bytes[i] = (byte) (lng >>> (56 - (i * 8)));
		}
		return bytes;
	}

	/**
	 * 将byte数组转换为int整数
	 * 
	 * @param bytes 长度为4的byte数组
	 * @return
	 */
	public static int toInteger(byte[] bytes) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (bytes[i] & 0x000000FF) << shift;
		}
		return value;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

}
