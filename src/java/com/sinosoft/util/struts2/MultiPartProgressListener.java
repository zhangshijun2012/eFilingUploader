package com.sinosoft.util.struts2;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.ProgressListener;

public class MultiPartProgressListener implements ProgressListener {
	/**
	 * 默认每上传100字节后才更新进度
	 */
	public static final long DEFAULT_STEP = 1;

	private HttpServletRequest request;

	private MultiPartProgressStatus status = null;

	private long step = DEFAULT_STEP;

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public MultiPartProgressStatus getStatus() {
		return status;
	}

	public void setStatus(MultiPartProgressStatus status) {
		this.status = status;
	}

	public long getStep() {
		return step;
	}

	public void setStep(long step) {
		this.step = step;
	}

	@SuppressWarnings("unused")
	private MultiPartProgressListener() {
	}

	public MultiPartProgressListener(HttpServletRequest request) {
		super();
		this.request = request;
		this.status = new MultiPartProgressStatus();
		this.request.getSession().setAttribute(SESSION_PROGRESS_STATUS, this.status);
	}

	/**
	 * 判断当前是否可以更新状态
	 * 
	 * @param pBytesRead
	 * @param pContentLength
	 * @param pItems
	 * @return
	 */
	public boolean canUpdate(long pBytesRead, long pContentLength, int pItems) {
		return this.status == null || this.status.size < 0
				|| (this.status.getProgress() != pBytesRead && (this.status.progress <= (pBytesRead - this.step)));
	}

	public static final String SESSION_PROGRESS_STATUS = "MultiPartProgressStatus";

	/**
	 * @param readedBytes 已经读取的字节数
	 * @param totalBytes 要上传的总字节数
	 * @param currentItem 当前是第几个文件
	 */
	/**
	 * Updates the listeners status information.
	 * 
	 * @param pBytesRead The total number of bytes, which have been read so far.
	 * @param pContentLength The total number of bytes, which are being read.
	 *            May be -1, if this number is unknown.
	 * @param pItems The number of the field, which is currently being read. (0 =
	 *            no item so far, 1 = first item is being read, ...)
	 */
	public void update(long pBytesRead, long pContentLength, int pItems) {
		if (!this.canUpdate(pBytesRead, pContentLength, pItems)) {
			return;
		}
		this.status.setProgress(pBytesRead);
		this.status.setSize(pContentLength);
		this.status.setItem(pItems);
		if (this.status.getSize() == this.status.getProgress()) {
			status.setMessage("数据上传完毕,正在保存文件...");
		}
		status.setChanged(true);
	}

	public static class MultiPartProgressStatus implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7175712904498198232L;

		public MultiPartProgressStatus() {
		}

		public MultiPartProgressStatus(long progress, long size, int item) {
			super();
			this.progress = progress;
			this.size = size;
			this.item = item;
		}

		/**
		 * 表示数据是否已改变
		 */
		private boolean changed = false;

		/**
		 * 当前读取的进度
		 */
		private long progress = 0;

		/**
		 * 要读取的总进度,< 0表示还没开始
		 */
		private long size = -1;

		/**
		 * 第几个表单
		 */
		private int item = -1;

		/**
		 * 该状态是否结束.只有此结束状态标志为true此对象才表示结束.否则就算是progress == size也未结束.<br>
		 * 因为虽然上传完毕了,但是还需要在业务中做其他操作.需要在业务处理完毕时将此字段手动设置为true
		 */
		private boolean complete = false;

		/**
		 * 判断上传是否成功,complete为true时有效
		 */
		private boolean success = false;

		private String message;

		/**
		 * 上传完成之后的文件对象.注意此对象不是java.io.file对象,而是项目中对应的数据库表对象
		 */
		private Object file;

		public int getItem() {
			return item;
		}

		public void setItem(int item) {
			this.item = item;
		}

		public long getProgress() {
			return progress;
		}

		public void setProgress(long progress) {
			this.progress = progress;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public boolean isComplete() {
			return complete;
		}

		public void setComplete(boolean complete) {
			this.complete = complete;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Object getFile() {
			return file;
		}

		public void setFile(Object file) {
			this.file = file;
		}

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public boolean isChanged() {
			return changed;
		}

		public void setChanged(boolean changed) {
			this.changed = changed;
		}
	}

}
