package com.sinosoft.util.struts2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.struts2.dispatcher.multipart.JakartaMultiPartRequest;
import org.apache.struts2.dispatcher.multipart.MultiPartRequest;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.sinosoft.util.NumberHelper;
import com.sinosoft.util.struts2.MultiPartProgressListener.MultiPartProgressStatus;

/**
 * 文件上传进度的控制
 * 
 * @author LuoGang
 * 
 */
public class MultiPartRequestSupport extends JakartaMultiPartRequest {
	/**
	 * 上传进度监听器
	 */
	protected ProgressListener listener;

	public ProgressListener getListener() {
		return listener;
	}

	public void setListener(ProgressListener progressListener) {
		this.listener = progressListener;
	}

	static final Logger LOG = LoggerFactory.getLogger(MultiPartRequest.class);

	@Override
	public void parse(HttpServletRequest servletRequest, String saveDir) throws IOException {
		DiskFileItemFactory fac = new DiskFileItemFactory();
		// Make sure that the data is written to file
		fac.setSizeThreshold(0);
		if (saveDir != null) {
			fac.setRepository(new File(saveDir));
		}

		// Parse the request
		try {
			ServletFileUpload upload = new ServletFileUpload(fac);
			createDefaultLinster(servletRequest);
			upload.setProgressListener(this.listener); // 添加监听器
			upload.setSizeMax(maxSize);

			List<?> items = upload.parseRequest(createRequestContext(servletRequest));
			for (Object item1 : items) {
				FileItem item = (FileItem) item1;
				if (LOG.isDebugEnabled()) LOG.debug("Found item " + item.getFieldName());
				if (item.isFormField()) {
					LOG.debug("Item is a normal form field");
					List<String> values;
					if (params.get(item.getFieldName()) != null) {
						values = params.get(item.getFieldName());
					} else {
						values = new ArrayList<String>();
					}

					// note: see http://jira.opensymphony.com/browse/WW-633
					// basically, in some cases the charset may be null, so
					// we're just going to try to "other" method (no idea if
					// this
					// will work)
					String charset = servletRequest.getCharacterEncoding();
					if (charset != null) {
						values.add(item.getString(charset));
					} else {
						values.add(item.getString());
					}
					params.put(item.getFieldName(), values);
				} else {
					LOG.debug("Item is a file upload");

					// Skip file uploads that don't have a file name - meaning
					// that no file was selected.
					if (item.getName() == null || item.getName().trim().length() < 1) {
						LOG.debug("No file has been uploaded for the field: " + item.getFieldName());
						continue;
					}

					List<FileItem> values;
					if (files.get(item.getFieldName()) != null) {
						values = files.get(item.getFieldName());
					} else {
						values = new ArrayList<FileItem>();
					}

					values.add(item);
					files.put(item.getFieldName(), values);
				}
			}
		} catch (FileUploadException e) {
			LOG.warn("Unable to parse request", e);
			errors.add(e.getMessage());
			MultiPartProgressListener.MultiPartProgressStatus status = (MultiPartProgressListener.MultiPartProgressStatus) servletRequest
					.getSession().getAttribute(MultiPartProgressListener.SESSION_PROGRESS_STATUS);
			status.setComplete(true);
			status.setMessage("上传失败,可能是文件大小超出限制,最大允许上传:" + NumberHelper.format(maxSize / 1024 / 1024) + "MB.");
		}
	}

	/**
	 * Creates a RequestContext needed by Jakarta Commons Upload.
	 * 
	 * @param req the request.
	 * @return a new request context.
	 */
	private RequestContext createRequestContext(final HttpServletRequest req) {
		return new RequestContext() {
			public String getCharacterEncoding() {
				return req.getCharacterEncoding();
			}

			public String getContentType() {
				return req.getContentType();
			}

			public int getContentLength() {
				return req.getContentLength();
			}

			public InputStream getInputStream() throws IOException {
				InputStream in = req.getInputStream();
				if (in == null) {
					throw new IOException("Missing content in the request");
				}
				return req.getInputStream();
			}
		};
	}

	/**
	 * 如果listener为空则创建一个默认的上传进度监听器
	 * 
	 * @param servletRequest
	 * @return
	 */
	public void createDefaultLinster(HttpServletRequest servletRequest) {
		if (this.listener == null) {
			this.listener = new MultiPartProgressListener(servletRequest);
		}
	}

	/**
	 * 获得上传的进度状态
	 * 
	 * @return
	 */
	public MultiPartProgressStatus getProgressStatus(HttpServletRequest servletRequest) {
		return (MultiPartProgressStatus) servletRequest.getSession().getAttribute(
				MultiPartProgressListener.SESSION_PROGRESS_STATUS);
	}

}
