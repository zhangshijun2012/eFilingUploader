package com.sinosoft.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * 集合处理工具类
 * 
 * @author LuoGang
 * 
 */
public class CollectionHelper {
	/**
	 * 将数组o的每个值分别加到集合中
	 * 
	 * @param c 集合对象
	 * @param o 数组
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int add(Collection c, Object[] o) {
		if (o == null) {
			return 0;
		}
		int l = o.length;
		for (int i = 0; i < l; i++) {
			c.add(o[i]);
		}
		return l;
	}

	/**
	 * 
	 * @param <V>
	 * @param c
	 * @param iterator
	 * @return c中增加的数量
	 */
	public static <V> int add(Collection<V> c, Iterator<V> iterator) {
		if (iterator == null) {
			return 0;
		}

		int result = 0;
		while (iterator.hasNext()) {
			if (c.add(iterator.next())) {
				result++;
			}
		}

		return result;
	}

	/**
	 * 
	 * @param <V>
	 * @param c
	 * @param iterable
	 * @return c中增加的数量
	 * @see #add(Collection, Iterator)
	 */
	public static <V> int add(Collection<V> c, Iterable<V> iterable) {
		if (iterable == null) {
			return 0;
		}
		return add(c, iterable.iterator());
	}
}
