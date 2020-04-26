/*
 * Copyright 2002-2017 the original author or authors.
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

import java.util.Iterator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.lang.Nullable;

/**
 * Configuration interface to be implemented by most listable bean factories.
 * In addition to {@link ConfigurableBeanFactory}, it provides facilities to
 * analyze and modify bean definitions, and to pre-instantiate singletons.
 *
 * <p>This subinterface of {@link org.springframework.beans.factory.BeanFactory}
 * is not meant to be used in normal application code: Stick to
 * {@link org.springframework.beans.factory.BeanFactory} or
 * {@link org.springframework.beans.factory.ListableBeanFactory} for typical
 * use cases. This interface is just meant to allow for framework-internal
 * plug'n'play even when needing access to bean factory configuration methods.
 *
 * @author Juergen Hoeller
 * @see org.springframework.context.support.AbstractApplicationContext#getBeanFactory()
 * @since 03.11.2003
 */
public interface ConfigurableListableBeanFactory
		extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

	/**
	 * 忽略自动连接的给定依赖项类型：
	 * 例如，字符串。默认为“无”。
	 * <p>
	 * Ignore the given dependency type for autowiring:
	 * for example, String. Default is none.
	 *
	 * @param type the dependency type to ignore
	 */
	void ignoreDependencyType(Class<?> type);

	/**
	 * 忽略自动连接的给定依赖接口。
	 * <p>这通常由应用程序上下文用来注册
	 * 以其他方式解决的依赖项，如BeanFactory-through
	 * BeanFactoryAware或ApplicationContext，通过ApplicationContextAware。
	 * <p>默认情况下，仅忽略BeanFactoryAware接口。
	 * 要忽略其他类型，请为每个类型调用此方法。
	 * <p>
	 * Ignore the given dependency interface for autowiring.
	 * <p>This will typically be used by application contexts to register
	 * dependencies that are resolved in other ways, like BeanFactory through
	 * BeanFactoryAware or ApplicationContext through ApplicationContextAware.
	 * <p>By default, only the BeanFactoryAware interface is ignored.
	 * For further types to ignore, invoke this method for each type.
	 *
	 * @param ifc the dependency interface to ignore
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	void ignoreDependencyInterface(Class<?> ifc);

	/**
	 * 使用相应的自动连线值注册特殊的依赖项类型。
	 * <p>这是用于工厂/上下文引用的
	 * 可自动装配，但在工厂中不定义为豆子：
	 * 例如，ApplicationContext类型的依赖项解析为
	 * bean所在的ApplicationContext实例。
	 * <p>注意：在纯BeanFactory中没有注册这样的默认类型，
	 * 甚至连BeanFactory接口本身也不行。
	 * <p>
	 * Register a special dependency type with corresponding autowired value.
	 * <p>This is intended for factory/context references that are supposed
	 * to be autowirable but are not defined as beans in the factory:
	 * e.g. a dependency of type ApplicationContext resolved to the
	 * ApplicationContext instance that the bean is living in.
	 * <p>Note: There are no such default types registered in a plain BeanFactory,
	 * not even for the BeanFactory interface itself.
	 *
	 * @param dependencyType the dependency type to register. This will typically
	 *                       be a base interface such as BeanFactory, with extensions of it resolved
	 *                       as well if declared as an autowiring dependency (e.g. ListableBeanFactory),
	 *                       as long as the given value actually implements the extended interface.
	 * @param autowiredValue the corresponding autowired value. This may also be an
	 *                       implementation of the {@link org.springframework.beans.factory.ObjectFactory}
	 *                       interface, which allows for lazy resolution of the actual target value.
	 */
	void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue);

	/**
	 * 确定指定的bean是否符合自动连线候选条件，
	 * 要注入到声明匹配类型依赖项的其他bean中。
	 * <p>此方法还检查祖先工厂。
	 * <p>
	 * Determine whether the specified bean qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 * <p>This method checks ancestor factories as well.
	 *
	 * @param beanName   the name of the bean to check
	 * @param descriptor the descriptor of the dependency to resolve
	 * @return whether the bean should be considered as autowire candidate
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 */
	boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException;

	/**
	 * 返回指定bean的注册BeanDefinition，允许访问
	 * 其属性值和构造函数参数值（可以是
	 * 在豆子工厂后处理期间修改）。
	 * <p>返回的BeanDefinition对象不应是副本，而应是原始的
	 * 已在工厂中注册的定义对象。这意味着它应该
	 * 如果需要，可以转换为更具体的实现类型。
	 * <p><b>注意：</b>此方法不考虑祖先工厂。
	 * 它只用于访问这个工厂的本地bean定义。
	 * <p>
	 * Return the registered BeanDefinition for the specified bean, allowing access
	 * to its property values and constructor argument value (which can be
	 * modified during bean factory post-processing).
	 * <p>A returned BeanDefinition object should not be a copy but the original
	 * definition object as registered in the factory. This means that it should
	 * be castable to a more specific implementation type, if necessary.
	 * <p><b>NOTE:</b> This method does <i>not</i> consider ancestor factories.
	 * It is only meant for accessing local bean definitions of this factory.
	 *
	 * @param beanName the name of the bean
	 * @return the registered BeanDefinition
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 *                                       defined in this factory
	 */
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 返回此工厂管理的所有bean名称的统一视图。
	 * <p>包括bean定义名称和手动注册的名称
	 * 单例实例，bean定义名称始终排在第一位，
	 * 类似于bean名称的类型/注释特定检索的工作方式。
	 * <p>
	 * Return a unified view over all bean names managed by this factory.
	 * <p>Includes bean definition names as well as names of manually registered
	 * singleton instances, with bean definition names consistently coming first,
	 * analogous to how type/annotation specific retrieval of bean names works.
	 *
	 * @return the composite iterator for the bean names view
	 * @see #containsBeanDefinition
	 * @see #registerSingleton
	 * @see #getBeanNamesForType
	 * @see #getBeanNamesForAnnotation
	 * @since 4.1.2
	 */
	Iterator<String> getBeanNamesIterator();

	/**
	 * 清除合并的bean定义缓存，删除bean的条目
	 * 尚未被视为符合完全元数据缓存条件的。
	 * <p>通常在更改原始bean定义之后触发，
	 * 例如，在应用{@link BeanFactoryPostProcessor}之后。注意，元数据
	 * 对于在这一点上已经创建的bean，将保留在周围。
	 * <p>
	 * Clear the merged bean definition cache, removing entries for beans
	 * which are not considered eligible for full metadata caching yet.
	 * <p>Typically triggered after changes to the original bean definitions,
	 * e.g. after applying a {@link BeanFactoryPostProcessor}. Note that metadata
	 * for beans which have already been created at this point will be kept around.
	 *
	 * @see #getBeanDefinition
	 * @see #getMergedBeanDefinition
	 * @since 4.2
	 */
	void clearMetadataCache();

	/**
	 * 冻结所有bean定义，表示已注册的bean定义
	 * 不会被进一步修改或后处理。
	 * <p>这允许工厂积极地缓存bean定义元数据。
	 * <p>
	 * Freeze all bean definitions, signalling that the registered bean definitions
	 * will not be modified or post-processed any further.
	 * <p>This allows the factory to aggressively cache bean definition metadata.
	 */
	void freezeConfiguration();

	/**
	 * 返回此工厂的bean定义是否已冻结，
	 * 也就是说，不应该被进一步修改或后处理。
	 * <p>
	 * Return whether this factory's bean definitions are frozen,
	 * i.e. are not supposed to be modified or post-processed any further.
	 *
	 * @return {@code true} if the factory's configuration is considered frozen
	 */
	boolean isConfigurationFrozen();

	/**
	 * 确保所有非惰性init单例都被实例化，同时考虑
	 * {@link org.springframework.beans.factory.FactoryBean FactoryBeans}。
	 * 如果需要，通常在工厂设置结束时调用。
	 * <p>
	 * Ensure that all non-lazy-init singletons are instantiated, also considering
	 * {@link org.springframework.beans.factory.FactoryBean FactoryBeans}.
	 * Typically invoked at the end of factory setup, if desired.
	 *
	 * @throws BeansException if one of the singleton beans could not be created.
	 *                        Note: This may have left the factory with some beans already initialized!
	 *                        Call {@link #destroySingletons()} for full cleanup in this case.
	 * @see #destroySingletons()
	 */
	void preInstantiateSingletons() throws BeansException;

}
