package com.sinosoft.efiling.hibernate.dao;

import com.sinosoft.efiling.hibernate.entity.User;
import com.sinosoft.util.hibernate.dao.EntityDaoSupport;

public class UserDao extends EntityDaoSupport<User> {
	public User getByNo(String userNo) {
		return getByProperty("no", userNo);
	}

}
