/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.context;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

/**
 * 这个 Interface 给 web application 提供配置。
 * application 容器运行的时候，是只读的。
 * 如果实现这个 Interface，需要支持 reloaded 重新急啊在
 *
 * Interface to provide configuration for a web application. This is read-only while
 * the application is running, but may be reloaded if the implementation supports this.
 *
 * <p>This interface adds a {@code getServletContext()} method to the generic
 * ApplicationContext interface, and defines a well-known application attribute name
 * that the root context must be bound to in the bootstrap process.
 *
 * <p>Like generic application contexts, web application contexts are hierarchical.
 * There is a single root context per application, while each servlet in the application
 * (including a dispatcher servlet in the MVC framework) has its own child context.
 *
 * <p>In addition to standard application context lifecycle capabilities,
 * WebApplicationContext implementations need to detect {@link ServletContextAware}
 * beans and invoke the {@code setServletContext} method accordingly.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since January 19, 2001
 * @see ServletContextAware#setServletContext
 */
public interface WebApplicationContext extends ApplicationContext {

	/**
	 * Context 属性成功启动，绑定 WebApplicationContext 容器
	 *
	 * Context attribute to bind root WebApplicationContext to on successful startup.
	 * <p>Note: If the startup of the root context fails, this attribute can contain
	 * an exception or error as value. Use WebApplicationContextUtils for convenient
	 * lookup of the root WebApplicationContext.
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#getWebApplicationContext
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#getRequiredWebApplicationContext
	 */
	String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";

	/**
	 * spring scope定义，request scope 范围是 `request`，
	 * 除此之外，还支持标准的 singleton、prototype
	 *
	 * (这里是Spring的Scope：request、session、singleton、prototype)
	 *
	 * Scope identifier for request scope: "request".
	 * Supported in addition to the standard scopes "singleton" and "prototype".
	 */
	String SCOPE_REQUEST = "request";

	/**
	 * spring scope定义，session scope 范围是 'session'
	 * 除此之外，还支持标准的 singleton、prototype
	 *
	 * Scope identifier for session scope: "session".
	 * Supported in addition to the standard scopes "singleton" and "prototype".
	 */
	String SCOPE_SESSION = "session";

	/**
	 * spring scope定义，全局的 web程序 scope 范围是 'application'
	 * 除此之外，还支持标准的 singleton、prototype
	 *
	 * Scope identifier for the global web application scope: "application".
	 * Supported in addition to the standard scopes "singleton" and "prototype".
	 */
	String SCOPE_APPLICATION = "application";

	/**
	 * 在 BeanFactory 容器中 ServletContext 叫 ServletContext
	 *
	 * Name of the ServletContext environment bean in the factory.
	 *
	 * @see javax.servlet.ServletContext
	 */
	String SERVLET_CONTEXT_BEAN_NAME = "servletContext";

	/**
	 * BeanFactory 容器中 init-params(ServletContext) 叫 contextParameters
	 * （web.xml 中的 Servlet 标签，和 init-params 标签）
	 *
	 * Name of the ServletContext init-params environment bean in the factory.
	 * <p>Note: Possibly merged with ServletConfig parameters.
	 * ServletConfig parameters override ServletContext parameters of the same name.
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 * @see javax.servlet.ServletContext#getInitParameter(String)
	 * @see javax.servlet.ServletConfig#getInitParameterNames()
	 * @see javax.servlet.ServletConfig#getInitParameter(String)
	 */
	String CONTEXT_PARAMETERS_BEAN_NAME = "contextParameters";

	/**
	 * 在 BeanFactory 容器中 ServletContext 的 attributes 叫 contextAttributes
	 *
	 * Name of the ServletContext attributes environment bean in the factory.
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 * @see javax.servlet.ServletContext#getAttribute(String)
	 */
	String CONTEXT_ATTRIBUTES_BEAN_NAME = "contextAttributes";


	/**
	 * Application 返回一个标准的 ServletContext(ServletAPI)
	 *
	 * Return the standard Servlet API ServletContext for this application.
	 */
	@Nullable
	ServletContext getServletContext();

}
