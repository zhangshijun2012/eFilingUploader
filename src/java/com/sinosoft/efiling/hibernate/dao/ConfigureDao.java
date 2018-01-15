package com.sinosoft.efiling.hibernate.dao;

import java.util.List;
import java.util.Properties;

import com.sinosoft.efiling.hibernate.entity.Configure;
import com.sinosoft.efiling.hibernate.entity.ConfigureId;
import com.sinosoft.efiling.util.SystemUtils;
import com.sinosoft.util.hibernate.dao.EntityDaoSupport;

public class ConfigureDao extends EntityDaoSupport<Configure> {
	/**
	 * 得到type,key对应的Configure对象
	 * 
	 * @param type
	 * @param key
	 * @return
	 */
	public Configure get(String type, String key) {
		return get(new ConfigureId(type, key));
	}

	/**
	 * 得到type所对应的所有配置
	 * 
	 * @param type
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public Properties getProperties(String type) {
		Properties props = new Properties();
		String hql = "FROM " + getEntityClassName() + " WHERE id.type = ? AND status = ?";
		List<Configure> list = (List<Configure>) query(hql, new Object[] { type, SystemUtils.STATUS_VALID });
		for (Configure cfg : list) {
			props.put(cfg.getKey(), cfg.getValue());
		}
		return props;
	}

	/**
	 * 得到type,key对应的value
	 * 
	 * @param type
	 * @param key
	 * @return
	 */
	public String getProperty(String type, String key) {
		Configure cfg = get(type, key);
		return cfg == null ? null : cfg.getValue();
	}
}
