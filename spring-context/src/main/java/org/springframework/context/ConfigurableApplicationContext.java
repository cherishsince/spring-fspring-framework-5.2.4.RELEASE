/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.context;

import java.io.Closeable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.lang.Nullable;

/**
 * SPI interface to be implemented by most if not all application contexts.
 * Provides facilities to configure an application context in addition
 * to the application context client methods in the
 * {@link org.springframework.context.ApplicationContext} interface.
 *
 * <p>Configuration and lifecycle methods are encapsulated here to avoid
 * making them obvious to ApplicationContext client code. The present
 * methods should only be used by startup and shutdown code.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Sam Brannen
 * @since 03.11.2003
 */
public interface ConfigurableApplicationContext extends ApplicationContext, Lifecycle, Closeable {

	/**
	 * 这些字符的任何数量都被视为介于
	 * 一个字符串值中有多个上下文配置路径。
	 * <p>
	 * Any number of these characters are considered delimiters between
	 * multiple context config paths in a single String value.
	 *
	 * @see org.springframework.context.support.AbstractXmlApplicationContext#setConfigLocation
	 * @see org.springframework.web.context.ContextLoader#CONFIG_LOCATION_PARAM
	 * @see org.springframework.web.servlet.FrameworkServlet#setContextConfigLocation
	 */
	String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

	/**
	 * 工厂中ConversionService bean的名称。
	 * 如果未提供，则应用默认转换规则。
	 * <p>
	 * Name of the ConversionService bean in the factory.
	 * If none is supplied, default conversion rules apply.
	 *
	 * @see org.springframework.core.convert.ConversionService
	 * @since 3.0
	 */
	String CONVERSION_SERVICE_BEAN_NAME = "conversionService";

	/**
	 * 工厂中的LoadTimeWeaver bean的名称。如果提供了这样的bean，
	 * 上下文将使用临时类加载器进行类型匹配，顺序如下
	 * 允许LoadTimeWeaver处理所有实际的bean类。
	 * <p>
	 * Name of the LoadTimeWeaver bean in the factory. If such a bean is supplied,
	 * the context will use a temporary ClassLoader for type matching, in order
	 * to allow the LoadTimeWeaver to process all actual bean classes.
	 *
	 * @see org.springframework.instrument.classloading.LoadTimeWeaver
	 * @since 2.5
	 */
	String LOAD_TIME_WEAVER_BEAN_NAME = "loadTimeWeaver";

	/**
	 * Name of the {@link Environment} bean in the factory.
	 *
	 * @since 3.1
	 */
	String ENVIRONMENT_BEAN_NAME = "environment";

	/**
	 * 工厂中系统属性bean的名称。
	 *
	 * Name of the System properties bean in the factory.
	 *
	 * @see java.lang.System#getProperties()
	 */
	String SYSTEM_PROPERTIES_BEAN_NAME = "systemProperties";

	/**
	 * 工厂中系统环境bean的名称。
	 *
	 * Name of the System environment bean in the factory.
	 *
	 * @see java.lang.System#getenv()
	 */
	String SYSTEM_ENVIRONMENT_BEAN_NAME = "systemEnvironment";

	/**
	 * {@link Thread#getName() Name} of the {@linkplain #registerShutdownHook()
	 * shutdown hook} thread: {@value}.
	 *
	 * @see #registerShutdownHook()
	 * @since 5.2
	 */
	String SHUTDOWN_HOOK_THREAD_NAME = "SpringContextShutdownHook";

	/**
	 * 为 ApplicationContext 设置唯一 ID
	 *
	 * Set the unique id of this application context.
	 *
	 * @since 3.0
	 */
	void setId(String id);

	/**
	 * 为 ApplicationContext 设置 parent
	 * 父类不应该被修改：如果创建的对象不可用时，则应该在构造函数外部设置它
	 *
	 * 设置此应用程序上下文的父级。
	 * <p>请注意，不应更改父项：它只应设置在外部
	 * 一个构造函数，如果创建此类的对象时它不可用，
	 * 例如，在WebApplicationContext设置的情况下。
	 * <p>
	 * Set the parent of this application context.
	 * <p>Note that the parent shouldn't be changed: It should only be set outside
	 * a constructor if it isn't available when an object of this class is created,
	 * for example in case of WebApplicationContext setup.
	 *
	 * @param parent the parent context
	 * @see org.springframework.web.context.ConfigurableWebApplicationContext
	 */
	void setParent(@Nullable ApplicationContext parent);

	/**
	 * 设置 Environment
	 *
	 * Set the {@code Environment} for this application context.
	 *
	 * @param environment the new environment
	 * @since 3.1
	 */
	void setEnvironment(ConfigurableEnvironment environment);

	/**
	 * 在configurable中返回此应用程序上下文的{@code Environment}
	 * 形式，允许进一步定制。
	 * <p>
	 * Return the {@code Environment} for this application context in configurable
	 * form, allowing for further customization.
	 *
	 * @since 3.1
	 */
	@Override
	ConfigurableEnvironment getEnvironment();

	/**
	 * 添加将应用于内部的新BeanFactoryPostProcessor
	 * 刷新时此应用程序上下文的bean工厂，在
	 * 计算bean定义。在上下文配置期间调用。
	 * <p>
	 * Add a new BeanFactoryPostProcessor that will get applied to the internal
	 * bean factory of this application context on refresh, before any of the
	 * bean definitions get evaluated. To be invoked during context configuration.
	 *
	 * @param postProcessor the factory processor to register
	 */
	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor);

	/**
	 * 添加一个新的ApplicationListener，它将在上下文事件时得到通知
	 * 例如上下文刷新和上下文关闭。
	 * <p>请注意，在此注册的任何ApplicationListener都将被应用
	 * 如果上下文尚未处于活动状态，则在刷新时，或使用
	 * 当前事件多主机（如果上下文已处于活动状态）。
	 * <p>
	 * Add a new ApplicationListener that will be notified on context events
	 * such as context refresh and context shutdown.
	 * <p>Note that any ApplicationListener registered here will be applied
	 * on refresh if the context is not active yet, or on the fly with the
	 * current event multicaster in case of a context that is already active.
	 *
	 * @param listener the ApplicationListener to register
	 * @see org.springframework.context.event.ContextRefreshedEvent
	 * @see org.springframework.context.event.ContextClosedEvent
	 */
	void addApplicationListener(ApplicationListener<?> listener);

	/**
	 * 将给定的协议解析器注册到此应用程序上下文中，
	 * 允许处理其他资源协议。
	 * <p>任何此类解析器都将在此上下文标准之前调用
	 * 解决规则。因此，它也可以覆盖任何默认规则。
	 * <p>
	 * Register the given protocol resolver with this application context,
	 * allowing for additional resource protocols to be handled.
	 * <p>Any such resolver will be invoked ahead of this context's standard
	 * resolution rules. It may therefore also override any default rules.
	 *
	 * @since 4.3
	 */
	void addProtocolResolver(ProtocolResolver resolver);

	/**
	 * 加载或刷新配置的持久表示，
	 * 可能是XML文件、属性文件或关系数据库架构。
	 * <p>由于这是一个启动方法，它应该销毁已经创建的单例
	 * 如果失败了，要避免资源悬空。换句话说，在调用之后
	 * 对于该方法，应该实例化所有或根本不实例化单例。
	 * <p>
	 * Load or refresh the persistent representation of the configuration,
	 * which might an XML file, properties file, or relational database schema.
	 * <p>As this is a startup method, it should destroy already created singletons
	 * if it fails, to avoid dangling resources. In other words, after invocation
	 * of that method, either all or no singletons at all should be instantiated.
	 *
	 * @throws BeansException        if the bean factory could not be initialized
	 * @throws IllegalStateException if already initialized and multiple refresh
	 *                               attempts are not supported
	 */
	void refresh() throws BeansException, IllegalStateException;

	/**
	 * 在JVM运行时注册一个关闭钩子，关闭这个上下文
	 * 在JVM关闭时，除非此时它已经关闭。
	 * <p>此方法可以多次调用。只有一个关闭挂钩
	 * （最大值）将为每个上下文实例注册。
	 * <p>从Spring Framework 5.2开始，{@linkplain Thread#getName（）name}
	 * 关闭挂接线程应该是{@link#shutdown＠hook＠thread＠NAME}。
	 * <p>
	 * Register a shutdown hook with the JVM runtime, closing this context
	 * on JVM shutdown unless it has already been closed at that time.
	 * <p>This method can be called multiple times. Only one shutdown hook
	 * (at max) will be registered for each context instance.
	 * <p>As of Spring Framework 5.2, the {@linkplain Thread#getName() name} of
	 * the shutdown hook thread should be {@link #SHUTDOWN_HOOK_THREAD_NAME}.
	 *
	 * @see java.lang.Runtime#addShutdownHook
	 * @see #close()
	 */
	void registerShutdownHook();

	/**
	 * 关闭此应用程序上下文，释放所有资源并锁定
	 * 实现可能会成功。这包括销毁所有缓存的singleton bean。
	 * <p>注意：是否对父上下文调用{@code close}；
	 * 父上下文有自己独立的生命周期。
	 * <p>此方法可多次调用，且无副作用：后续
	 * {@code close}将忽略对已关闭上下文的调用。
	 * <p>
	 * Close this application context, releasing all resources and locks that the
	 * implementation might hold. This includes destroying all cached singleton beans.
	 * <p>Note: Does <i>not</i> invoke {@code close} on a parent context;
	 * parent contexts have their own, independent lifecycle.
	 * <p>This method can be called multiple times without side effects: Subsequent
	 * {@code close} calls on an already closed context will be ignored.
	 */
	@Override
	void close();

	/**
	 * 确定此应用程序上下文是否处于活动状态，即，
	 * 是否至少刷新过一次并且尚未关闭。
	 * <p>
	 * Determine whether this application context is active, that is,
	 * whether it has been refreshed at least once and has not been closed yet.
	 *
	 * @return whether the context is still active
	 * @see #refresh()
	 * @see #close()
	 * @see #getBeanFactory()
	 */
	boolean isActive();

	/**
	 * 返回此应用程序上下文的内部bean工厂。
	 * 可用于访问底层工厂的特定功能。
	 * <p>注意：不要使用这个来后期处理bean工厂；singleton
	 * 以前就已经被实例化了。使用BeanFactoryPostProcessor
	 * 在触碰beans之前拦截BeanFactory设置过程。
	 * <p>通常，只有在上下文
	 * 处于活动状态，即介于{@link#refresh（）}和{@link#close（）之间。
	 * {@link\isActive（）}标志可用于检查上下文
	 * 处于适当的状态。
	 * <p>
	 * Return the internal bean factory of this application context.
	 * Can be used to access specific functionality of the underlying factory.
	 * <p>Note: Do not use this to post-process the bean factory; singletons
	 * will already have been instantiated before. Use a BeanFactoryPostProcessor
	 * to intercept the BeanFactory setup process before beans get touched.
	 * <p>Generally, this internal factory will only be accessible while the context
	 * is active, that is, in-between {@link #refresh()} and {@link #close()}.
	 * The {@link #isActive()} flag can be used to check whether the context
	 * is in an appropriate state.
	 *
	 * @return the underlying bean factory
	 * @throws IllegalStateException if the context does not hold an internal
	 *                               bean factory (usually if {@link #refresh()} hasn't been called yet or
	 *                               if {@link #close()} has already been called)
	 * @see #isActive()
	 * @see #refresh()
	 * @see #close()
	 * @see #addBeanFactoryPostProcessor
	 */
	ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;

}
