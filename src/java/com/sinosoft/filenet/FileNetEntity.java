package com.sinosoft.filenet;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.springframework.beans.BeanUtils;

import com.filenet.api.property.Properties;
import com.filenet.api.util.Id;
import com.sinosoft.util.StringHelper;

public class FileNetEntity {
	public FileNetEntity() {

	}

	public FileNetEntity(Properties properties) {
		super();
		this.properties = properties;
		read();
	}

	/**
	 * 读取指定的properties的值,此方法会改变this.properties
	 * 
	 * @param properties
	 * @return this
	 */
	public FileNetEntity read(Properties properties) {
		this.properties = properties;
		return read();
	}

	/**
	 * 读取properties的属性,加载到当前对象中
	 * 
	 * @return this
	 */
	public FileNetEntity read() {
		PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(this.getClass());
		String name;
		for (PropertyDescriptor targetPd : targetPds) {
			if (targetPd.getWriteMethod() == null || targetPd.getReadMethod() == null) continue;
			name = targetPd.getName();
			if (StringHelper.indexInArray(name, new String[] { "class", "properties" }) > -1) continue;
			Method writeMethod = targetPd.getWriteMethod();
			if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
				writeMethod.setAccessible(true);
			}
			try {
				Object value;
				if (name.equals("id")) {
					value = getStringValue(properties.getIdValue(name));
				} else value = properties.get(name).getObjectValue();
				writeMethod.invoke(this, value);
			} catch (Exception ingore) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

		}
		return this;
	}

	/**
	 * 
	 * 将当前对象的字段写入properties对象中.此方法不会改变this.properties
	 * 
	 * @param properties
	 * @return properties
	 */
	public Properties write(Properties properties) {
		PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(this.getClass());
		String name;
		for (PropertyDescriptor targetPd : targetPds) {
			if (targetPd.getWriteMethod() == null || targetPd.getReadMethod() == null) continue;
			name = targetPd.getName();
			if (StringHelper.indexInArray(name, new String[] { "class", "properties" }) > -1) continue;
			Method readMethod = targetPd.getReadMethod();
			if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
				readMethod.setAccessible(true);
			}
			try {
				Object value = readMethod.invoke(this);
				if (value == null) continue; // 不写入null值
				// System.out.println(name + " = " + value);
				if (name.equals("id")) {
					properties.putValue(name, new Id(value.toString()));
				} else {
					properties.putObjectValue(name, value);
				}
			} catch (Exception ingore) {
				// TODO Auto-generated catch block
				ingore.printStackTrace();
			}
		}
		return properties;
	}

	/**
	 * 将当前对象的字段写入当前的properties中
	 * 
	 * @return this.properties
	 */
	public Properties write() {
		return write(properties);
	}

	/** FileNet的文档属性对象 */
	private Properties properties;
	/** 文档id,此字段无法写入,由FileNet服务端进行返回 */
	private String id;
	/** 文档标题 */
	private String documentTitle;
	/** 生成的业务ID,唯一,必填 */
	private String unique;
	/** 业务号 */
	private String serviceNum;
	/** 资料类型,参见EFiling系统中的FILE_TYPE表 */
	private String pageType;
	/** 操作人 */
	private String operator;
	/** 操作时间 */
	private String operateTime;
	/** 自定义属性 */
	private String def1;
	/** 自定义属性 */
	private String def2;
	/** 自定义属性 */
	private String def3;
	/** 自定义属性 */
	private String def4;
	/** 自定义属性 */
	private String def5;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	public String getUnique() {
		return unique;
	}

	public void setUnique(String unique) {
		this.unique = unique;
	}

	public String getServiceNum() {
		return serviceNum;
	}

	public void setServiceNum(String serviceNum) {
		this.serviceNum = serviceNum;
	}

	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(String operateTime) {
		this.operateTime = operateTime;
	}

	public String getDef1() {
		return def1;
	}

	public void setDef1(String def1) {
		this.def1 = def1;
	}

	public String getDef2() {
		return def2;
	}

	public void setDef2(String def2) {
		this.def2 = def2;
	}

	public String getDef3() {
		return def3;
	}

	public void setDef3(String def3) {
		this.def3 = def3;
	}

	public String getDef4() {
		return def4;
	}

	public void setDef4(String def4) {
		this.def4 = def4;
	}

	public String getDef5() {
		return def5;
	}

	public void setDef5(String def5) {
		this.def5 = def5;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * 得到properties中的key值,如果没有则返回null
	 * 
	 * @param key
	 * @return
	 */
	public Object getObjectValue(String key) {
		try {
			return this.properties.getObjectValue(key);
		} catch (Exception ingore) {
			// 没有key这个对象
			return null;
		}
	}

	/**
	 * 得到key的字符串值
	 * 
	 * @param key
	 * @return
	 */
	public String getStringValue(String key) {
		Object value = this.getObjectValue(key);
		if (value == null) return null;
		if (value instanceof Id) return getStringValue((Id) value);
		return value.toString();
	}

	/**
	 * 将id转换为字符串
	 * 
	 * @param id
	 * @return
	 */
	public static String getStringValue(Id id) {
		String value = id.toString();
		int pos1 = value.lastIndexOf("{");
		int pos2 = value.lastIndexOf("}");
		if (pos2 > pos1) {
			value = value.substring(pos1 + 1, pos2);
		}
		return value;
	}
}
