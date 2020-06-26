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

package org.springframework.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.core.SimpleAliasRegistry;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Generic registry for shared bean instances, implementing the
 * {@link org.springframework.beans.factory.config.SingletonBeanRegistry}.
 * Allows for registering singleton instances that should be shared
 * for all callers of the registry, to be obtained via bean name.
 *
 * <p>Also supports registration of
 * {@link org.springframework.beans.factory.DisposableBean} instances,
 * (which might or might not correspond to registered singletons),
 * to be destroyed on shutdown of the registry. Dependencies between
 * beans can be registered to enforce an appropriate shutdown order.
 *
 * <p>This class mainly serves as base class for
 * {@link org.springframework.beans.factory.BeanFactory} implementations,
 * factoring out the common management of singleton bean instances. Note that
 * the {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}
 * interface extends the {@link SingletonBeanRegistry} interface.
 *
 * <p>Note that this class assumes neither a bean definition concept
 * nor a specific creation process for bean instances, in contrast to
 * {@link AbstractBeanFactory} and {@link DefaultListableBeanFactory}
 * (which inherit from it). Can alternatively also be used as a nested
 * helper to delegate to.
 *
 * @author Juergen Hoeller
 * @see #registerSingleton
 * @see #registerDisposableBean
 * @see org.springframework.beans.factory.DisposableBean
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory
 * @since 2.0
 */
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

	/**
	 * singleton对象的缓存：bean名到bean实例。
	 * <p>
	 * Cache of singleton objects: bean name to bean instance.
	 */
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	/**
	 * 单例工厂的缓存：对象工厂的bean名称。
	 * <p>
	 * Cache of singleton factories: bean name to ObjectFactory.
	 */
	private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

	/**
	 * 早期单例对象的缓存：bean名到bean实例。
	 * <p>
	 * Cache of early singleton objects: bean name to bean instance.
	 */
	private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

	/**
	 * 已经注册的单例
	 * <p>
	 * Set of registered singletons, containing the bean names in registration order.
	 */
	private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

	/**
	 * 当前正在创建的bean的名称容器
	 * <p>
	 * Names of beans that are currently in creation.
	 */
	private final Set<String> singletonsCurrentlyInCreation =
			Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/**
	 * 在创建检查中排除的bean的名称。
	 * <p>
	 * Names of beans currently excluded from in creation checks.
	 */
	private final Set<String> inCreationCheckExclusions =
			Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/**
	 * 异常列表，可用于关联相关原因。
	 * <p>
	 * List of suppressed Exceptions, available for associating related causes.
	 */
	@Nullable
	private Set<Exception> suppressedExceptions;

	/**
	 * 指示我们当前是否在DestroySingleton中的标志。
	 * <p>
	 * Flag that indicates whether we're currently within destroySingletons.
	 */
	private boolean singletonsCurrentlyInDestruction = false;

	/**
	 * 一次性bean实例：一次性实例的bean名称。
	 * <p>
	 * Disposable bean instances: bean name to disposable instance.
	 */
	private final Map<String, Object> disposableBeans = new LinkedHashMap<>();

	/**
	 * bean名称之间依赖：bean name设置bean包含的bean名称。
	 * <p>
	 * Map between containing bean names: bean name to Set of bean names that the bean contains.
	 */
	private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<>(16);

	/**
	 * tips: 谁依赖他
	 * 保存的是依赖 beanName 之间的映射关系：beanName - > 依赖 beanName 的集合
	 * <p>
	 * Map between dependent bean names: bean name to Set of dependent bean names.
	 */
	private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

	/**
	 * tips: 他依赖谁
	 * 保存的是依赖 beanName 之间的映射关系：依赖 beanName - > beanName 的集合
	 * <p>
	 * Map between depending bean names: bean name to Set of bean names for the bean's dependencies.
	 */
	private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);


	@Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		Assert.notNull(beanName, "Bean name must not be null");
		Assert.notNull(singletonObject, "Singleton object must not be null");
		synchronized (this.singletonObjects) {
			Object oldObject = this.singletonObjects.get(beanName);
			if (oldObject != null) {
				throw new IllegalStateException("Could not register object [" + singletonObject +
						"] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
			}
			addSingleton(beanName, singletonObject);
		}
	}

	/**
	 * Add the given singleton object to the singleton cache of this factory.
	 * <p>To be called for eager registration of singletons.
	 *
	 * @param beanName        the name of the bean
	 * @param singletonObject the singleton object
	 */
	protected void addSingleton(String beanName, Object singletonObject) {
		synchronized (this.singletonObjects) {
			// 单例的objects 缓存容器
			this.singletonObjects.put(beanName, singletonObject);
			// 删除单例缓存
			this.singletonFactories.remove(beanName);
			// 早期的 singleton 用于标记，正在创建进行中的 beanName
			this.earlySingletonObjects.remove(beanName);
			// 已经注册的
			this.registeredSingletons.add(beanName);
		}
	}

	/**
	 * Add the given singleton factory for building the specified singleton
	 * if necessary.
	 * <p>To be called for eager registration of singletons, e.g. to be able to
	 * resolve circular references.
	 *
	 * @param beanName         the name of the bean
	 * @param singletonFactory the factory for the singleton object
	 */
	protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(singletonFactory, "Singleton factory must not be null");
		// tips: 这里是添加 object factory 对象
		synchronized (this.singletonObjects) {
			// singletonObjects 不存在进入
			if (!this.singletonObjects.containsKey(beanName)) {
				// factory单例
				this.singletonFactories.put(beanName, singletonFactory);
				// 早期的单例
				this.earlySingletonObjects.remove(beanName);
				// 已注册的单例
				this.registeredSingletons.add(beanName);
			}
		}
	}

	@Override
	@Nullable
	public Object getSingleton(String beanName) {
		return getSingleton(beanName, true);
	}

	/**
	 * Return the (raw) singleton object registered under the given name.
	 * <p>Checks already instantiated singletons and also allows for an early
	 * reference to a currently created singleton (resolving a circular reference).
	 *
	 * @param beanName            the name of the bean to look for
	 * @param allowEarlyReference whether early references should be created or not
	 * @return the registered singleton object, or {@code null} if none found
	 */
	@Nullable
	protected Object getSingleton(String beanName, boolean allowEarlyReference) {
		// 从 singletonObjects 缓存中获取 singleton 对象
		Object singletonObject = this.singletonObjects.get(beanName);
		// 不存在 并且 beanName 没有在创建中，进入
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
			// 对 singletonObjects 进心加锁
			synchronized (this.singletonObjects) {
				// 从早期的容器中获取
				singletonObject = this.earlySingletonObjects.get(beanName);
				if (singletonObject == null && allowEarlyReference) {
					// 从 object factory 中获取
					ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
					if (singletonFactory != null) {
						// 通过 object factory 获取 object
						singletonObject = singletonFactory.getObject();
						// 添加到早期的缓存中，下次来可以直接使用
						this.earlySingletonObjects.put(beanName, singletonObject);
						// 将已创建的 object factory 删除，避免重复创建
						this.singletonFactories.remove(beanName);
					}
				}
			}
		}
		// 存在就直接返回
		return singletonObject;
	}

	/**
	 * Return the (raw) singleton object registered under the given name,
	 * creating and registering a new one if none registered yet.
	 *
	 * @param beanName         the name of the bean
	 * @param singletonFactory the ObjectFactory to lazily create the singleton
	 *                         with, if necessary
	 * @return the registered singleton object
	 */
	public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(beanName, "Bean name must not be null");
		// <1> 对 singletonObjects 加锁
		synchronized (this.singletonObjects) {
			// <2> 获取当前 beanName 实力对象
			Object singletonObject = this.singletonObjects.get(beanName);
			// <3> 如果为 null 进入
			// tips: singletonObject 就是缓存对象，如果存在了就直接返回
			if (singletonObject == null) {
				// <4> 检查状态，单曲是否在销毁中
				if (this.singletonsCurrentlyInDestruction) {
					throw new BeanCreationNotAllowedException(beanName,
							"Singleton bean creation not allowed while singletons of this factory are in destruction " +
									"(Do not request a bean from a BeanFactory in a destroy method implementation!)");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
				}
				// <5> 创建 singleton 之前的，钩子方法
				// (这里将正在创建中的 beanName 添加到 singletonsCurrentlyInCreation 检查 inCreationCheckExclusions)
				beforeSingletonCreation(beanName);
				// <6> 用于标记是否是一个 新的单例对象
				boolean newSingleton = false;
				// <7> 用于记录一下过程中发生的 exception
				boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
				if (recordSuppressedExceptions) {
					this.suppressedExceptions = new LinkedHashSet<>();
				}
				try {
					// <8> 调用 singleton factory 获取 object 对象，没有则创建
					// tips: singletonFactory：是外面传入的 function 函数
					singletonObject = singletonFactory.getObject();
					// <9> 通过调用 getObject 后标记为 true
					// tips: singletonFactory.getObject() 每次创建的都是一个 new 的object 对象
					newSingleton = true;
				} catch (IllegalStateException ex) {
					// Has the singleton object implicitly appeared in the meantime ->
					// if yes, proceed with it since the exception indicates that state.
					singletonObject = this.singletonObjects.get(beanName);
					if (singletonObject == null) {
						throw ex;
					}
				} catch (BeanCreationException ex) {
					if (recordSuppressedExceptions) {
						for (Exception suppressedException : this.suppressedExceptions) {
							ex.addRelatedCause(suppressedException);
						}
					}
					throw ex;
				} finally {
					// <10> 清理一下所记录的异常
					if (recordSuppressedExceptions) {
						this.suppressedExceptions = null;
					}
					// <11> 创建 singleton 之后的，钩子方法
					// 从删除 singletonsCurrentlyInCreation 中删除，移除正在创建
					afterSingletonCreation(beanName);
				}
				// <12> 新的单例对象才添加到 cache 中
				if (newSingleton) {
					addSingleton(beanName, singletonObject);
				}
			}
			// <13> 返回单例实例
			return singletonObject;
		}
	}

	/**
	 * Register an Exception that happened to get suppressed during the creation of a
	 * singleton bean instance, e.g. a temporary circular reference resolution problem.
	 *
	 * @param ex the Exception to register
	 */
	protected void onSuppressedException(Exception ex) {
		synchronized (this.singletonObjects) {
			if (this.suppressedExceptions != null) {
				this.suppressedExceptions.add(ex);
			}
		}
	}

	/**
	 * Remove the bean with the given name from the singleton cache of this factory,
	 * to be able to clean up eager registration of a singleton if creation failed.
	 *
	 * @param beanName the name of the bean
	 * @see #getSingletonMutex()
	 */
	protected void removeSingleton(String beanName) {
		synchronized (this.singletonObjects) {
			this.singletonObjects.remove(beanName);
			this.singletonFactories.remove(beanName);
			this.earlySingletonObjects.remove(beanName);
			this.registeredSingletons.remove(beanName);
		}
	}

	@Override
	public boolean containsSingleton(String beanName) {
		return this.singletonObjects.containsKey(beanName);
	}

	@Override
	public String[] getSingletonNames() {
		synchronized (this.singletonObjects) {
			return StringUtils.toStringArray(this.registeredSingletons);
		}
	}

	@Override
	public int getSingletonCount() {
		synchronized (this.singletonObjects) {
			return this.registeredSingletons.size();
		}
	}


	public void setCurrentlyInCreation(String beanName, boolean inCreation) {
		Assert.notNull(beanName, "Bean name must not be null");
		if (!inCreation) {
			this.inCreationCheckExclusions.add(beanName);
		} else {
			this.inCreationCheckExclusions.remove(beanName);
		}
	}

	public boolean isCurrentlyInCreation(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		return (!this.inCreationCheckExclusions.contains(beanName) && isActuallyInCreation(beanName));
	}

	protected boolean isActuallyInCreation(String beanName) {
		return isSingletonCurrentlyInCreation(beanName);
	}

	/**
	 * Return whether the specified singleton bean is currently in creation
	 * (within the entire factory).
	 *
	 * @param beanName the name of the bean
	 */
	public boolean isSingletonCurrentlyInCreation(String beanName) {
		return this.singletonsCurrentlyInCreation.contains(beanName);
	}

	/**
	 * Callback before singleton creation.
	 * <p>The default implementation register the singleton as currently in creation.
	 *
	 * @param beanName the name of the singleton about to be created
	 * @see #isSingletonCurrentlyInCreation
	 */
	protected void beforeSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName)
				&& !this.singletonsCurrentlyInCreation.add(beanName)) {
			throw new BeanCurrentlyInCreationException(beanName);
		}
	}

	/**
	 * Callback after singleton creation.
	 * <p>The default implementation marks the singleton as not in creation anymore.
	 *
	 * @param beanName the name of the singleton that has been created
	 * @see #isSingletonCurrentlyInCreation
	 */
	protected void afterSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName)
				&& !this.singletonsCurrentlyInCreation.remove(beanName)) {
			throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
		}
	}


	/**
	 * Add the given bean to the list of disposable beans in this registry.
	 * <p>Disposable beans usually correspond to registered singletons,
	 * matching the bean name but potentially being a different instance
	 * (for example, a DisposableBean adapter for a singleton that does not
	 * naturally implement Spring's DisposableBean interface).
	 *
	 * @param beanName the name of the bean
	 * @param bean     the bean instance
	 */
	public void registerDisposableBean(String beanName, DisposableBean bean) {
		synchronized (this.disposableBeans) {
			this.disposableBeans.put(beanName, bean);
		}
	}

	/**
	 * Register a containment relationship between two beans,
	 * e.g. between an inner bean and its containing outer bean.
	 * <p>Also registers the containing bean as dependent on the contained bean
	 * in terms of destruction order.
	 *
	 * @param containedBeanName  the name of the contained (inner) bean
	 * @param containingBeanName the name of the containing (outer) bean
	 * @see #registerDependentBean
	 */
	public void registerContainedBean(String containedBeanName, String containingBeanName) {
		synchronized (this.containedBeanMap) {
			Set<String> containedBeans =
					this.containedBeanMap.computeIfAbsent(containingBeanName, k -> new LinkedHashSet<>(8));
			if (!containedBeans.add(containedBeanName)) {
				return;
			}
		}
		registerDependentBean(containedBeanName, containingBeanName);
	}

	/**
	 * Register a dependent bean for the given bean,
	 * to be destroyed before the given bean is destroyed.
	 *
	 * @param beanName          the name of the bean
	 * @param dependentBeanName the name of the dependent bean
	 */
	public void registerDependentBean(String beanName, String dependentBeanName) {
		// beanName 是依赖的 bean
		// dependentBeanName 是 beangetSingleton

		// 获取原来的 beanName
		String canonicalName = canonicalName(beanName);

		// tips：谁依赖他
		synchronized (this.dependentBeanMap) {
			// 有就获取，没有就 new LinkedHashSet<>(8)
			Set<String> dependentBeans =
					this.dependentBeanMap.computeIfAbsent(canonicalName, k -> new LinkedHashSet<>(8));
			// 添加成功 失败直接返回
			if (!dependentBeans.add(dependentBeanName)) {
				return;
			}
		}

		// tips: 他依赖谁
		synchronized (this.dependenciesForBeanMap) {
			Set<String> dependenciesForBean =
					this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName, k -> new LinkedHashSet<>(8));
			dependenciesForBean.add(canonicalName);
		}
	}

	/**
	 * 确定指定的依赖bean是否已注册为
	 * 依赖于给定的bean或其任何可传递依赖项。
	 * <p>
	 * Determine whether the specified dependent bean has been registered as
	 * dependent on the given bean or on any of its transitive dependencies.
	 *
	 * @param beanName          the name of the bean to check
	 * @param dependentBeanName the name of the dependent bean
	 * @since 4.0
	 */
	protected boolean isDependent(String beanName, String dependentBeanName) {
		synchronized (this.dependentBeanMap) {
			return isDependent(beanName, dependentBeanName, null);
		}
	}

	private boolean isDependent(String beanName, String dependentBeanName, @Nullable Set<String> alreadySeen) {
		// alreadySeen 已经检测的依赖 bean
		if (alreadySeen != null && alreadySeen.contains(beanName)) {
			return false;
		}
		// 获取原始 beanName
		String canonicalName = canonicalName(beanName);
		// 获取当前 beanName 的依赖集合
		Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
		if (dependentBeans == null) {
			return false;
		}
		if (dependentBeans.contains(dependentBeanName)) {
			return true;
		}

		// 检查类依赖的类，的依赖
		for (String transitiveDependency : dependentBeans) {
			if (alreadySeen == null) {
				alreadySeen = new HashSet<>();
			}
			// 添加到 alreadySeen 中
			alreadySeen.add(beanName);
			// 递推
			if (isDependent(transitiveDependency, dependentBeanName, alreadySeen)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine whether a dependent bean has been registered for the given name.
	 *
	 * @param beanName the name of the bean to check
	 */
	protected boolean hasDependentBean(String beanName) {
		return this.dependentBeanMap.containsKey(beanName);
	}

	/**
	 * Return the names of all beans which depend on the specified bean, if any.
	 *
	 * @param beanName the name of the bean
	 * @return the array of dependent bean names, or an empty array if none
	 */
	public String[] getDependentBeans(String beanName) {
		Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
		if (dependentBeans == null) {
			return new String[0];
		}
		synchronized (this.dependentBeanMap) {
			return StringUtils.toStringArray(dependentBeans);
		}
	}

	/**
	 * Return the names of all beans that the specified bean depends on, if any.
	 *
	 * @param beanName the name of the bean
	 * @return the array of names of beans which the bean depends on,
	 * or an empty array if none
	 */
	public String[] getDependenciesForBean(String beanName) {
		Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(beanName);
		if (dependenciesForBean == null) {
			return new String[0];
		}
		synchronized (this.dependenciesForBeanMap) {
			return StringUtils.toStringArray(dependenciesForBean);
		}
	}

public void destroySingletons() {
	if (logger.isTraceEnabled()) {
		logger.trace("Destroying singletons in " + this);
	}
	// <1> 设置 Destruction 销毁标识为 true，代表销毁中
	synchronized (this.singletonObjects) {
		this.singletonsCurrentlyInDestruction = true;
	}
	// <2> disposableBeanNames 是已经销毁的 bean 名称
	String[] disposableBeanNames;
	synchronized (this.disposableBeans) {
		disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
	}
	// <3> 循环销毁 bean
	for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
		destroySingleton(disposableBeanNames[i]);
	}
	// <4> 清除，BeanFactory 缓存
	this.containedBeanMap.clear();
	this.dependentBeanMap.clear();
	this.dependenciesForBeanMap.clear();
	// <5> 清除，单例的缓存，这里是clear 清除所有的，上面是 remove 删除单个
	clearSingletonCache();
}

	/**
	 * Clear all cached singleton instances in this registry.
	 *
	 * @since 4.3.15
	 */
	protected void clearSingletonCache() {
		synchronized (this.singletonObjects) {
			this.singletonObjects.clear();
			this.singletonFactories.clear();
			this.earlySingletonObjects.clear();
			this.registeredSingletons.clear();
			this.singletonsCurrentlyInDestruction = false;
		}
	}

	/**
	 * Destroy the given bean. Delegates to {@code destroyBean}
	 * if a corresponding disposable bean instance is found.
	 *
	 * @param beanName the name of the bean
	 * @see #destroyBean
	 */
public void destroySingleton(String beanName) {
	// <1> 删除 singleton，删除的是单例 三级缓存
	// Remove a registered singleton of the given name, if any.
	removeSingleton(beanName);

	// <2> 销毁相应的DisposableBean实例。
	// Destroy the corresponding DisposableBean instance.
	DisposableBean disposableBean;
	synchronized (this.disposableBeans) {
		disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
	}
	// <3> 去销毁 bean 实例
	destroyBean(beanName, disposableBean);
}

	/**
	 * Destroy the given bean. Must destroy beans that depend on the given
	 * bean before the bean itself. Should not throw any exceptions.
	 *
	 * @param beanName the name of the bean
	 * @param bean     the bean instance to destroy
	 */
protected void destroyBean(String beanName, @Nullable DisposableBean bean) {
	// tips:
	// bean的销毁逻辑，优先销毁bean的依赖，然后销毁bean

	// <1> 首先从 bean 的依赖开始销毁
	// Trigger destruction of dependent beans first...
	Set<String> dependencies;
	synchronized (this.dependentBeanMap) {
		// <2> 将谁依赖他，从map中删除
		// 完全同步，命令保证断开设置
		// Within full synchronization in order to guarantee a disconnected Set
		dependencies = this.dependentBeanMap.remove(beanName);
	}
	// <3> 有其他class 依赖这个 class 进入
	if (dependencies != null) {
		if (logger.isTraceEnabled()) {
			logger.trace("Retrieved dependent beans for bean '" + beanName + "': " + dependencies);
		}
		// <4> 这里是个递归调用，会再次进入到这里
		for (String dependentBeanName : dependencies) {
			destroySingleton(dependentBeanName);
		}
	}

	// <5> 销毁实现 DisposableBean 的 bean 实例
	// Actually destroy the bean now...
	if (bean != null) {
		try {
			// <6> 调用bean的 destroy() 方法
			bean.destroy();
		} catch (Throwable ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Destruction of bean with name '" + beanName + "' threw an exception", ex);
			}
		}
	}

	// <6> 销毁容器里面的 bean，
	// containedBeans 是移除 beanName 后其他的依赖，这里使用一个递归继续销毁
	// Trigger destruction of contained beans...
	Set<String> containedBeans;
	// <7> 销毁容器的 bean，从 containedBeanMap 移除，
	synchronized (this.containedBeanMap) {
		// Within full synchronization in order to guarantee a disconnected Set
		// 这里的map关系是：bean名称之间依赖，bean name设置bean包含的bean名称。
		containedBeans = this.containedBeanMap.remove(beanName);
	}
	// <8> 对移除后的，依赖进行销毁动作。
	if (containedBeans != null) {
		for (String containedBeanName : containedBeans) {
			destroySingleton(containedBeanName);
		}
	}

	// <9> 从其他bean的依赖项中删除销毁的bean。
	// Remove destroyed bean from other beans' dependencies.
	synchronized (this.dependentBeanMap) {
		// <9> 迭代 dependentBeanMap 这个map，从 value 中移除
		for (Iterator<Map.Entry<String, Set<String>>> it = this.dependentBeanMap.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, Set<String>> entry = it.next();
			Set<String> dependenciesToClean = entry.getValue();
			dependenciesToClean.remove(beanName);
			// 如果 dependenciesToClean 为空了，就把这个 key 直接删除了
			if (dependenciesToClean.isEmpty()) {
				it.remove();
			}
		}
	}

	// <10> 删除他依赖谁，的map缓存。
	// Remove destroyed bean's prepared dependency information.
	this.dependenciesForBeanMap.remove(beanName);
}

	/**
	 * Exposes the singleton mutex to subclasses and external collaborators.
	 * <p>Subclasses should synchronize on the given Object if they perform
	 * any sort of extended singleton creation phase. In particular, subclasses
	 * should <i>not</i> have their own mutexes involved in singleton creation,
	 * to avoid the potential for deadlocks in lazy-init situations.
	 */
	@Override
	public final Object getSingletonMutex() {
		return this.singletonObjects;
	}

}
