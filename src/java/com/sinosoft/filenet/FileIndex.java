package com.sinosoft.filenet;

import java.util.Date;

import com.sinosoft.util.hibernate.entity.EntitySupport;

/**
 * 文件索引表,用于存放文件索引,各系统可通过对索引表进行查询.
 * 最后通过索引表中的fileId到FileNet中导出文件
 * 
 * @author LuoGang
 * 
 */
public class FileIndex extends EntitySupport<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8616973018585309114L;

	// private String id;
	private String batchNo;
	private String operator;
	private Date operateTime;
	private String fileId;
	private String fileNo;
	private long fileSize;
	private String fileTitle;
	private String fileName;
	private String fileContentType;
	private int fileCount;
	private int pageCount;

	private String systemCode;
	private String businessNo;
	private String keywords;
	private String property00;
	private String property01;
	private String property02;
	private String property03;
	private String property04;
	private String property05;
	private String property06;
	private String property07;
	private String property08;
	private String property09;
	private Long property10;
	private Long property11;
	private Long property12;
	private Long property13;
	private Long property14;
	private Long property15;
	private String text;

	/** 文件的预览地址,此字段没有存入数据库中 */
	private String preview;

	public FileIndex() {
		this.operateTime = new Date();
		this.fileCount = 1;
		this.pageCount = 1;
	}

	// public String getId() {
	// return this.id;
	// }
	//
	// public void setId(String id) {
	// this.id = id;
	// }

	public String getBatchNo() {
		return this.batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getOperator() {
		return this.operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Date getOperateTime() {
		return this.operateTime;
	}

	public void setOperateTime(Date operateTime) {
		this.operateTime = operateTime;
	}

	public String getFileId() {
		return this.fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileNo() {
		return this.fileNo;
	}

	public void setFileNo(String fileNo) {
		this.fileNo = fileNo;
	}

	public long getFileSize() {
		return this.fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileTitle() {
		return fileTitle;
	}

	public void setFileTitle(String fileTitle) {
		this.fileTitle = fileTitle;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileContentType() {
		return this.fileContentType;
	}

	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public String getSystemCode() {
		return this.systemCode;
	}

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

	public String getBusinessNo() {
		return this.businessNo;
	}

	public void setBusinessNo(String businessNo) {
		this.businessNo = businessNo;
	}

	public String getKeywords() {
		return this.keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getProperty00() {
		return this.property00;
	}

	public void setProperty00(String property00) {
		this.property00 = property00;
	}

	public String getProperty01() {
		return this.property01;
	}

	public void setProperty01(String property01) {
		this.property01 = property01;
	}

	public String getProperty02() {
		return this.property02;
	}

	public void setProperty02(String property02) {
		this.property02 = property02;
	}

	public String getProperty03() {
		return this.property03;
	}

	public void setProperty03(String property03) {
		this.property03 = property03;
	}

	public String getProperty04() {
		return this.property04;
	}

	public void setProperty04(String property04) {
		this.property04 = property04;
	}

	public String getProperty05() {
		return this.property05;
	}

	public void setProperty05(String property05) {
		this.property05 = property05;
	}

	public String getProperty06() {
		return this.property06;
	}

	public void setProperty06(String property06) {
		this.property06 = property06;
	}

	public String getProperty07() {
		return this.property07;
	}

	public void setProperty07(String property07) {
		this.property07 = property07;
	}

	public String getProperty08() {
		return this.property08;
	}

	public void setProperty08(String property08) {
		this.property08 = property08;
	}

	public String getProperty09() {
		return this.property09;
	}

	public void setProperty09(String property09) {
		this.property09 = property09;
	}

	public Long getProperty10() {
		return this.property10;
	}

	public void setProperty10(Long property10) {
		this.property10 = property10;
	}

	public Long getProperty11() {
		return this.property11;
	}

	public void setProperty11(Long property11) {
		this.property11 = property11;
	}

	public Long getProperty12() {
		return this.property12;
	}

	public void setProperty12(Long property12) {
		this.property12 = property12;
	}

	public Long getProperty13() {
		return this.property13;
	}

	public void setProperty13(Long property13) {
		this.property13 = property13;
	}

	public Long getProperty14() {
		return this.property14;
	}

	public void setProperty14(Long property14) {
		this.property14 = property14;
	}

	public Long getProperty15() {
		return this.property15;
	}

	public void setProperty15(Long property15) {
		this.property15 = property15;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

}
