package com.sinosoft.util.struts2.intercepter;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import com.sinosoft.util.struts2.action.EntityActionSupport;

/**
 * 用于在执行save/update方法时事先创建EntityActionSupport中的entity.因为entity的类型为泛型参数,struts2自动设置的时候无法实例化
 * 
 * @author LuoGang
 * 
 */
public class CreateEntityIntercepter extends MethodFilterInterceptor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8876160766472299882L;

	@Override
	public String doIntercept(ActionInvocation invocation) {
		Object action = invocation.getAction();
		if (action instanceof EntityActionSupport) {
			// 将action的entity对象初始化
			((EntityActionSupport<?, ?, ?, ?>) action).createEntity();
		}
		try {
			return invocation.invoke();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
