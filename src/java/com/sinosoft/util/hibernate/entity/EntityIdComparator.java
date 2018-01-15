package com.sinosoft.util.hibernate.entity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sinosoft.util.StringHelper;

/**
 * 对实例按主键排序.将主键转换为String后进行排序
 * 
 * @author LuoGang
 * 
 */
@SuppressWarnings("rawtypes")
public class EntityIdComparator implements Comparator<Entity> {

	public int compare(Entity entity, Entity another) {
		String id = StringHelper.trim(entity.getId());
		String anotherId = StringHelper.trim(another.getId());
		return id.compareToIgnoreCase(anotherId);
	}

	public static final EntityIdComparator getComparator() {
		return comparator;
	}

	static final EntityIdComparator comparator = new EntityIdComparator();

	/**
	 * 对list中的数据按主键排序
	 * 
	 * @param list
	 */
	public static void sort(List<? extends Entity> list) {
		Collections.sort(list, comparator);
	}

}
