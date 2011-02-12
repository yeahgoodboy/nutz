package org.nutz.mvc2.impl;

import org.nutz.ioc.Ioc;
import org.nutz.ioc.Ioc2;
import org.nutz.ioc.IocContext;
import org.nutz.ioc.impl.ComboContext;
import org.nutz.lang.Lang;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.ioc.RequestIocContext;
import org.nutz.mvc.ioc.SessionIocContext;
import org.nutz.mvc2.ActionChain;

/**
 * 负责获取需要执行的具体方法所对应的对象
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class ActionInvokeObjectNode extends AbstractActionNode {

	@Override
	public void filter(ActionChain chain) throws Throwable {
		RequestIocContext reqContext = null;
		try {
			Object obj = chain.get(ActionFilters.moduleObject);
			if(obj == null) {
				Class<?> moduleType = (Class<?>) chain.get(ActionFilters.moduleType);
				String moduleName = (String) chain.get(ActionFilters.moduleName);
				Ioc ioc = Mvcs.getIoc(getServletContext(chain));
				if (null == ioc)
					throw Lang.makeThrow(	"Moudle with @InjectName('%s') but you not declare a Ioc for this app",
										moduleName);
				/*
				 * 如果 Ioc 容器实现了高级接口，那么会为当前请求设置上下文对象
				 */
				if (ioc instanceof Ioc2) {
					reqContext = new RequestIocContext(getRequest(chain));
					SessionIocContext sessionContext = new SessionIocContext(getRequest(chain).getSession());
					IocContext myContext = new ComboContext(reqContext, sessionContext);
					obj = ((Ioc2) ioc).get(moduleType, moduleName, myContext);
				}
				/*
				 * 否则，则仅仅简单的从容器获取
				 */
				else
					obj = ioc.get(moduleType, moduleName);
			}
			chain.put(ActionFilters.methodObj, obj);
			
			chain.doChain();
		} finally {
			if (reqContext != null)
				reqContext.depose();
		}
	}
	
}