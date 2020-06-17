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

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.lang.Nullable;

/**
 *方法覆盖的集合，确定a上的哪个方法(如果有的话)
 * Spring IoC容器将在运行时覆盖
 *
 * tips:
 * parseLookupOverrideSubElements 和 parseReplacedMethodSubElements
 * 这两个方法分别用于解析 lookup-method 和 replaced-method 属性。采用 MethodOverrides 实现覆盖
 *
 * Set of method overrides, determining which, if any, methods on a
 * managed object the Spring IoC container will override at runtime.
 *
 * <p>The currently supported {@link MethodOverride} variants are
 * {@link LookupOverride} and {@link ReplaceOverride}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 * @see MethodOverride
 */
public class MethodOverrides {

	private final Set<MethodOverride> overrides = new CopyOnWriteArraySet<>();


	/**
	 * 默认构造器
	 *
	 * Create new MethodOverrides.
	 */
	public MethodOverrides() {
	}

	/**
	 * 深拷贝构造函数。
	 *
	 * Deep copy constructor.
	 */
	public MethodOverrides(MethodOverrides other) {
		// tips:
		// 这就是将一个 method overrides 创建另一个 method overrides
		// 就是 overrides set 集合，合并操作
		addOverrides(other);
	}


	/**
	 * 复制 overrides
	 *
	 * Copy all given method overrides into this object.
	 */
	public void addOverrides(@Nullable MethodOverrides other) {
		if (other != null) {
			this.overrides.addAll(other.overrides);
		}
	}

	/**
	 * 添加 overrides
	 *
	 * Add the given method override.
	 */
	public void addOverride(MethodOverride override) {
		this.overrides.add(override);
	}

	/**
	 * Return all method overrides contained by this object.
	 * @return a Set of MethodOverride objects
	 * @see MethodOverride
	 */
	public Set<MethodOverride> getOverrides() {
		return this.overrides;
	}

	/**
	 * Return whether the set of method overrides is empty.
	 */
	public boolean isEmpty() {
		return this.overrides.isEmpty();
	}

	/**
	 * Return the override for the given method, if any.
	 * @param method method to check for overrides for
	 * @return the method override, or {@code null} if none
	 */
	@Nullable
	public MethodOverride getOverride(Method method) {
		MethodOverride match = null;
		for (MethodOverride candidate : this.overrides) {
			if (candidate.matches(method)) {
				match = candidate;
			}
		}
		return match;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MethodOverrides)) {
			return false;
		}
		MethodOverrides that = (MethodOverrides) other;
		return this.overrides.equals(that.overrides);
	}

	@Override
	public int hashCode() {
		return this.overrides.hashCode();
	}

}
