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

package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.security.AccessControlContext;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

/**
 * 大多数bean工厂要实现的配置接口。提供
 * 配置bean工厂的工具，以及bean工厂
 * {@link org.springframework.beans.factory.BeanFactory}中的客户机方法
 * 接口。
 * <p>
 * Configuration interface to be implemented by most bean factories. Provides
 * facilities to configure a bean factory, in addition to the bean factory
 * client methods in the {@link org.springframework.beans.factory.BeanFactory}
 * interface.
 *
 * <p>This bean factory interface is not meant to be used in normal application
 * code: Stick to {@link org.springframework.beans.factory.BeanFactory} or
 * {@link org.springframework.beans.factory.ListableBeanFactory} for typical
 * needs. This extended interface is just meant to allow for framework-internal
 * plug'n'play and for special access to bean factory configuration methods.
 *
 * @author Juergen Hoeller
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see ConfigurableListableBeanFactory
 * @since 03.11.2003
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

	/**
	 * 标准单例作用域的作用域标识符：{@value}。
	 * <p>可以通过{@code registerScope}添加自定义作用域。
	 * <p>
	 * Scope identifier for the standard singleton scope: {@value}.
	 * <p>Custom scopes can be added via {@code registerScope}.
	 *
	 * @see #registerScope
	 */
	String SCOPE_SINGLETON = "singleton";

	/**
	 * 标准原型作用域的作用域标识符：{@value}。
	 * <p>可以通过{@code registerScope}添加自定义作用域。
	 * <p>
	 * Scope identifier for the standard prototype scope: {@value}.
	 * <p>Custom scopes can be added via {@code registerScope}.
	 *
	 * @see #registerScope
	 */
	String SCOPE_PROTOTYPE = "prototype";

	/**
	 * 设置此bean工厂的父级。
	 * <p>请注意，不能更改父项：它只能设置在外部
	 * 在工厂实例化时不可用的构造函数。
	 * <p>
	 * Set the parent of this bean factory.
	 * <p>Note that the parent cannot be changed: It should only be set outside
	 * a constructor if it isn't available at the time of factory instantiation.
	 *
	 * @param parentBeanFactory the parent BeanFactory
	 * @throws IllegalStateException if this factory is already associated with
	 *                               a parent BeanFactory
	 * @see #getParentBeanFactory()
	 */
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

	/**
	 * 设置用于加载bean类的类加载器。
	 * 默认为线程上下文类加载器。
	 * <p>注意，这个类加载器只适用于bean定义
	 * 它还没有携带已解析的bean类。从那时起就是这样
	 * Spring2.0默认情况下：Bean定义只携带Bean类名，
	 * 一旦工厂处理了bean定义就被解决。
	 * <p>
	 * Set the class loader to use for loading bean classes.
	 * Default is the thread context class loader.
	 * <p>Note that this class loader will only apply to bean definitions
	 * that do not carry a resolved bean class yet. This is the case as of
	 * Spring 2.0 by default: Bean definitions only carry bean class names,
	 * to be resolved once the factory processes the bean definition.
	 *
	 * @param beanClassLoader the class loader to use,
	 *                        or {@code null} to suggest the default class loader
	 */
	void setBeanClassLoader(@Nullable ClassLoader beanClassLoader);

	/**
	 * 返回此工厂的类装入器以加载bean类
	 * （仅当系统类加载器不可访问时，{@code null}）。
	 * <p>
	 * Return this factory's class loader for loading bean classes
	 * (only {@code null} if even the system ClassLoader isn't accessible).
	 *
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 */
	@Nullable
	ClassLoader getBeanClassLoader();

	/**
	 * 指定用于类型匹配目的的临时类加载器。
	 * 默认值是none，只需使用标准的bean类加载器。
	 * <p>如果
	 * <i>涉及装载时间编织，以确保实际的bean
	 * 类的加载尽可能慢。临时加载程序是
	 * 然后在BeanFactory完成引导阶段后删除。
	 * <p>
	 * Specify a temporary ClassLoader to use for type matching purposes.
	 * Default is none, simply using the standard bean ClassLoader.
	 * <p>A temporary ClassLoader is usually just specified if
	 * <i>load-time weaving</i> is involved, to make sure that actual bean
	 * classes are loaded as lazily as possible. The temporary loader is
	 * then removed once the BeanFactory completes its bootstrap phase.
	 *
	 * @since 2.5
	 */
	void setTempClassLoader(@Nullable ClassLoader tempClassLoader);

	/**
	 * 返回用于类型匹配的临时类加载器，
	 * 如果有的话。
	 * <p>
	 * Return the temporary ClassLoader to use for type matching purposes,
	 * if any.
	 *
	 * @since 2.5
	 */
	@Nullable
	ClassLoader getTempClassLoader();

	/**
	 * 设置是否缓存bean元数据，如给定的bean定义
	 * （以合并的方式）并解析bean类。默认设置为启用。
	 * <p>关闭此标志以启用bean定义对象的热刷新
	 * 尤其是bean类。如果这个标志是关闭的，那么任何bean的创建
	 * 实例将为新解析的类重新查询bean类加载器。
	 * <p>
	 * Set whether to cache bean metadata such as given bean definitions
	 * (in merged fashion) and resolved bean classes. Default is on.
	 * <p>Turn this flag off to enable hot-refreshing of bean definition objects
	 * and in particular bean classes. If this flag is off, any creation of a bean
	 * instance will re-query the bean class loader for newly resolved classes.
	 */
	void setCacheBeanMetadata(boolean cacheBeanMetadata);

	/**
	 * 返回是否缓存bean元数据，如给定的bean定义
	 * （以合并的方式）并解析bean类。
	 * <p>
	 * Return whether to cache bean metadata such as given bean definitions
	 * (in merged fashion) and resolved bean classes.
	 */
	boolean isCacheBeanMetadata();

	/**
	 * 为bean定义值中的表达式指定解析策略。
	 * <p>默认情况下，BeanFactory中没有激活的表达式支持。
	 * ApplicationContext通常会设置一个标准的表达式策略
	 * 在这里，以统一的EL兼容样式支持“{…}”表达式。
	 * <p>
	 * Specify the resolution strategy for expressions in bean definition values.
	 * <p>There is no expression support active in a BeanFactory by default.
	 * An ApplicationContext will typically set a standard expression strategy
	 * here, supporting "#{...}" expressions in a Unified EL compatible style.
	 *
	 * @since 3.0
	 */
	void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver);

	/**
	 * 返回bean定义值中表达式的解析策略。
	 * <p>
	 * Return the resolution strategy for expressions in bean definition values.
	 *
	 * @since 3.0
	 */
	@Nullable
	BeanExpressionResolver getBeanExpressionResolver();

	/**
	 * 指定用于转换的Spring3.0 ConversionService
	 * 属性值，作为JavaBeans属性编辑器的替代。
	 * <p>
	 * Specify a Spring 3.0 ConversionService to use for converting
	 * property values, as an alternative to JavaBeans PropertyEditors.
	 *
	 * @since 3.0
	 */
	void setConversionService(@Nullable ConversionService conversionService);

	/**
	 * 返回关联的ConversionService（如果有）。
	 * <p>
	 * Return the associated ConversionService, if any.
	 *
	 * @since 3.0
	 */
	@Nullable
	ConversionService getConversionService();

	/**
	 * 添加要应用于所有bean创建过程的PropertyEditorRegistrar。
	 * <p>这样的注册器创建新的PropertyEditor实例并注册它们
	 * 在给定的注册表中，为每个bean创建尝试刷新。这样可以避免
	 * 需要在自定义编辑器上进行同步；因此，通常
	 * 最好使用此方法而不是{@link\registerCustomEditor}。
	 * <p>
	 * Add a PropertyEditorRegistrar to be applied to all bean creation processes.
	 * <p>Such a registrar creates new PropertyEditor instances and registers them
	 * on the given registry, fresh for each bean creation attempt. This avoids
	 * the need for synchronization on custom editors; hence, it is generally
	 * preferable to use this method instead of {@link #registerCustomEditor}.
	 *
	 * @param registrar the PropertyEditorRegistrar to register
	 */
	void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);

	/**
	 * 注册一个 自定义 PropertyEditor
	 * <p>
	 * Register the given custom property editor for all properties of the
	 * given type. To be invoked during factory configuration.
	 * <p>Note that this method will register a shared custom editor instance;
	 * access to that instance will be synchronized for thread-safety. It is
	 * generally preferable to use {@link #addPropertyEditorRegistrar} instead
	 * of this method, to avoid for the need for synchronization on custom editors.
	 *
	 * @param requiredType        type of the property
	 * @param propertyEditorClass the {@link PropertyEditor} class to register
	 */
	void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);

	/**
	 * (暂时没用到) 促织注册的 Editor 到 PropertyEditorRegistry
	 * <p>
	 * Initialize the given PropertyEditorRegistry with the custom editors
	 * that have been registered with this BeanFactory.
	 *
	 * @param registry the PropertyEditorRegistry to initialize
	 */
	void copyRegisteredEditorsTo(PropertyEditorRegistry registry);

	/**
	 * 设置此BeanFactory应用于转换的自定义类型转换器
	 * bean属性值、构造函数参数值等。
	 * <p>这将覆盖默认的PropertyEditor机制，从而使
	 * 任何自定义编辑器或自定义编辑器注册器都不相关。
	 * <p>
	 * Set a custom type converter that this BeanFactory should use for converting
	 * bean property values, constructor argument values, etc.
	 * <p>This will override the default PropertyEditor mechanism and hence make
	 * any custom editors or custom editor registrars irrelevant.
	 *
	 * @see #addPropertyEditorRegistrar
	 * @see #registerCustomEditor
	 * @since 2.5
	 */
	void setTypeConverter(TypeConverter typeConverter);

	/**
	 * 获取此BeanFactory使用的类型转换器。这可能是新的
	 * 每个调用的实例，因为类型转换器通常是线程安全的。
	 * <p>如果默认PropertyEditor机制处于活动状态，则返回
	 * TypeConverter将知道所有已注册的自定义编辑器。
	 * <p>
	 * Obtain a type converter as used by this BeanFactory. This may be a fresh
	 * instance for each call, since TypeConverters are usually <i>not</i> thread-safe.
	 * <p>If the default PropertyEditor mechanism is active, the returned
	 * TypeConverter will be aware of all custom editors that have been registered.
	 *
	 * @since 2.5
	 */
	TypeConverter getTypeConverter();

	/**
	 * 为嵌入值（如批注属性）添加字符串解析器。
	 * Add a String resolver for embedded values such as annotation attributes.
	 *
	 * @param valueResolver the String resolver to apply to embedded values
	 * @since 3.0
	 */
	void addEmbeddedValueResolver(StringValueResolver valueResolver);

	/**
	 * 确定是否已将嵌入式值解析程序注册到此
	 * bean工厂，将通过{@link#resolvedembeddedvalue（String）}应用。
	 * <p>
	 * Determine whether an embedded value resolver has been registered with this
	 * bean factory, to be applied through {@link #resolveEmbeddedValue(String)}.
	 *
	 * @since 4.3
	 */
	boolean hasEmbeddedValueResolver();

	/**
	 * 解析给定的嵌入值，例如注释属性。 采用 StringValueResolver 来解析
	 *
	 * <p>
	 * Resolve the given embedded value, e.g. an annotation attribute.
	 *
	 * @param value the value to resolve
	 * @return the resolved value (may be the original value as-is)
	 * @since 3.0
	 */
	@Nullable
	String resolveEmbeddedValue(String value);

	/**
	 * 添加将应用于创建的bean的新BeanPostProcessor
	 * 在这个工厂。在工厂配置期间调用。
	 * <p>注意：此处提交的后处理程序将按以下顺序应用
	 * 注册；通过实现
	 * {@link org.springframework.core.Ordered}接口将被忽略。注意
	 * 自动检测到的后处理器（例如，作为ApplicationContext中的bean）
	 * 将始终在以编程方式注册后应用。
	 * <p>
	 * Add a new BeanPostProcessor that will get applied to beans created
	 * by this factory. To be invoked during factory configuration.
	 * <p>Note: Post-processors submitted here will be applied in the order of
	 * registration; any ordering semantics expressed through implementing the
	 * {@link org.springframework.core.Ordered} interface will be ignored. Note
	 * that autodetected post-processors (e.g. as beans in an ApplicationContext)
	 * will always be applied after programmatically registered ones.
	 *
	 * @param beanPostProcessor the post-processor to register
	 */
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * 返回 beanPostProcessor 数量
	 * <p>
	 * Return the current number of registered BeanPostProcessors, if any.
	 */
	int getBeanPostProcessorCount();

	/**
	 * 注册给定的作用域，由给定的作用域实现支持。
	 * <p>
	 * Register the given scope, backed by the given Scope implementation.
	 *
	 * @param scopeName the scope identifier
	 * @param scope     the backing Scope implementation
	 */
	void registerScope(String scopeName, Scope scope);

	/**
	 * 返回当前注册的所有作用域的名称。
	 * <p>这将只返回显式注册作用域的名称。
	 * 诸如“singleton”和“prototype”之类的内置作用域不会公开。
	 * <p>
	 * Return the names of all currently registered scopes.
	 * <p>This will only return the names of explicitly registered scopes.
	 * Built-in scopes such as "singleton" and "prototype" won't be exposed.
	 *
	 * @return the array of scope names, or an empty array if none
	 * @see #registerScope
	 */
	String[] getRegisteredScopeNames();

	/**
	 * 返回给定作用域名称的作用域实现（如果有）。
	 * <p>这将只返回显式注册的作用域。
	 * 诸如“singleton”和“prototype”之类的内置作用域不会公开。
	 * <p>
	 * Return the Scope implementation for the given scope name, if any.
	 * <p>This will only return explicitly registered scopes.
	 * Built-in scopes such as "singleton" and "prototype" won't be exposed.
	 *
	 * @param scopeName the name of the scope
	 * @return the registered Scope implementation, or {@code null} if none
	 * @see #registerScope
	 */
	@Nullable
	Scope getRegisteredScope(String scopeName);

	/**
	 * 提供与此工厂相关的安全访问控制上下文。
	 * Provides a security access control context relevant to this factory.
	 *
	 * @return the applicable AccessControlContext (never {@code null})
	 * @since 3.0
	 */
	AccessControlContext getAccessControlContext();

	/**
	 * 从给定的其他工厂复制所有相关配置。
	 * <p>应包括所有标准配置设置以及
	 * BeanPostProcessors、作用域和工厂特定的内部设置。
	 * 不应该包含任何实际bean定义的元数据，
	 * 例如BeanDefinition对象和bean名称别名。
	 * <p>
	 * Copy all relevant configuration from the given other factory.
	 * <p>Should include all standard configuration settings as well as
	 * BeanPostProcessors, Scopes, and factory-specific internal settings.
	 * Should not include any metadata of actual bean definitions,
	 * such as BeanDefinition objects and bean name aliases.
	 *
	 * @param otherFactory the other BeanFactory to copy from
	 */
	void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

	/**
	 * 给定bean名称，创建别名。我们通常使用这种方法
	 * 支持XML id中非法的名称（用于bean名称）。
	 * <p>通常在工厂配置期间调用，但也可以
	 * 用于别名的运行时注册。因此，一个工厂
	 * 实现应同步别名访问。
	 * <p>
	 * Given a bean name, create an alias. We typically use this method to
	 * support names that are illegal within XML ids (used for bean names).
	 * <p>Typically invoked during factory configuration, but can also be
	 * used for runtime registration of aliases. Therefore, a factory
	 * implementation should synchronize alias access.
	 *
	 * @param beanName the canonical name of the target bean
	 * @param alias    the alias to be registered for the bean
	 * @throws BeanDefinitionStoreException if the alias is already in use
	 */
	void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;

	/**
	 * 解析在此中注册的所有别名目标名称和别名
	 * 工厂，对其应用给定的StringValueResolver。
	 * <p>值解析器可以例如解析占位符
	 * 在目标bean名称中，甚至在别名中。
	 * 报错
	 * <p>
	 * Resolve all alias target names and aliases registered in this
	 * factory, applying the given StringValueResolver to them.
	 * <p>The value resolver may for example resolve placeholders
	 * in target bean names and even in alias names.
	 *
	 * @param valueResolver the StringValueResolver to apply
	 * @since 2.5
	 */
	void resolveAliases(StringValueResolver valueResolver);

	/**
	 * 返回给定bean名称的合并BeanDefinition，
	 * 必要时将子bean定义与其父bean定义合并。
	 * 同时考虑祖先工厂中的bean定义。
	 * <p>
	 * Return a merged BeanDefinition for the given bean name,
	 * merging a child bean definition with its parent if necessary.
	 * Considers bean definitions in ancestor factories as well.
	 *
	 * @param beanName the name of the bean to retrieve the merged definition for
	 * @return a (potentially merged) BeanDefinition for the given bean
	 * @throws NoSuchBeanDefinitionException if there is no bean definition with the given name
	 * @since 2.5
	 */
	BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 确定具有给定名称的bean是否为FactoryBean。
	 * <p>
	 * Determine whether the bean with the given name is a FactoryBean.
	 *
	 * @param name the name of the bean to check
	 * @return whether the bean is a FactoryBean
	 * ({@code false} means the bean exists but is not a FactoryBean)
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 2.5
	 */
	boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 显式控制指定bean的当前创建状态。
	 * 仅供集装箱内部使用。
	 * <p>
	 * Explicitly control the current in-creation status of the specified bean.
	 * For container-internal use only.
	 *
	 * @param beanName   the name of the bean
	 * @param inCreation whether the bean is currently in creation
	 * @since 3.1
	 */
	void setCurrentlyInCreation(String beanName, boolean inCreation);

	/**
	 * 确定指定的bean当前是否正在创建中。
	 * <p>
	 * Determine whether the specified bean is currently in creation.
	 *
	 * @param beanName the name of the bean
	 * @return whether the bean is currently in creation
	 * @since 2.5
	 */
	boolean isCurrentlyInCreation(String beanName);

	/**
	 * 为给定的bean注册一个依赖bean，
	 * 在给定的豆子被销毁之前被销毁。
	 * <p>
	 * Register a dependent bean for the given bean,
	 * to be destroyed before the given bean is destroyed.
	 *
	 * @param beanName          the name of the bean
	 * @param dependentBeanName the name of the dependent bean
	 * @since 2.5
	 */
	void registerDependentBean(String beanName, String dependentBeanName);

	/**
	 * 返回依赖于指定bean的所有bean的名称（如果有）。
	 * <p>
	 * Return the names of all beans which depend on the specified bean, if any.
	 *
	 * @param beanName the name of the bean
	 * @return the array of dependent bean names, or an empty array if none  依赖bean名称的数组，如果没有，则为空数组
	 * @since 2.5
	 */
	String[] getDependentBeans(String beanName);

	/**
	 * 返回指定bean所依赖的所有bean的名称（如果有）。
	 * <p>
	 * Return the names of all beans that the specified bean depends on, if any.
	 *
	 * @param beanName the name of the bean
	 * @return the array of names of beans which the bean depends on,  bean所依赖的bean的名称数组，
	 * or an empty array if none
	 * @since 2.5
	 */
	String[] getDependenciesForBean(String beanName);

	/**
	 * 销毁给定的bean实例（通常是原型实例
	 * 从这个工厂获得）根据它的bean定义。
	 * <p>销毁过程中出现的任何异常都应被捕获
	 * 并记录而不是传播到此方法的调用方。
	 * <p>
	 * Destroy the given bean instance (usually a prototype instance
	 * obtained from this factory) according to its bean definition.
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 *
	 * @param beanName     the name of the bean definition
	 * @param beanInstance the bean instance to destroy
	 */
	void destroyBean(String beanName, Object beanInstance);

	/**
	 * 销毁当前目标作用域中的指定作用域bean（如果有）。
	 * <p>销毁过程中出现的任何异常都应被捕获
	 * 并记录而不是传播到此方法的调用方。
	 * <p>
	 * Destroy the specified scoped bean in the current target scope, if any.
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 *
	 * @param beanName the name of the scoped bean
	 */
	void destroyScopedBean(String beanName);

	/**
	 * 销毁此工厂中的所有单例bean，包括
	 * 已登记为一次性使用。被要求关闭工厂。
	 * <p>销毁过程中出现的任何异常都应被捕获
	 * 并记录而不是传播到此方法的调用方。
	 * <p>
	 * Destroy all singleton beans in this factory, including inner beans that have
	 * been registered as disposable. To be called on shutdown of a factory.
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 */
	void destroySingletons();

}
