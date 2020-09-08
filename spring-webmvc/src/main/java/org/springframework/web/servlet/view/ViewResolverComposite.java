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

package org.springframework.web.servlet.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * 一个{@链接org.springframework.web.servlet.ViewResolver}委托给其他人。
 *
 * A {@link org.springframework.web.servlet.ViewResolver} that delegates to others.
 *
 * @author Sebastien Deleuze
 * @author Rossen Stoyanchev
 * @since 4.1
 */
public class ViewResolverComposite implements ViewResolver, Ordered, InitializingBean,
		ApplicationContextAware, ServletContextAware {

	/**
	 * 注册的 ViewResolver 集合
	 */
	private final List<ViewResolver> viewResolvers = new ArrayList<>();

	private int order = Ordered.LOWEST_PRECEDENCE;


	/**
	 * Set the list of view viewResolvers to delegate to.
	 */
	public void setViewResolvers(List<ViewResolver> viewResolvers) {
		this.viewResolvers.clear();
		if (!CollectionUtils.isEmpty(viewResolvers)) {
			this.viewResolvers.addAll(viewResolvers);
		}
	}

	/**
	 * Return the list of view viewResolvers to delegate to.
	 */
	public List<ViewResolver> getViewResolvers() {
		return Collections.unmodifiableList(this.viewResolvers);
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		for (ViewResolver viewResolver : this.viewResolvers) {
			if (viewResolver instanceof ApplicationContextAware) {
				((ApplicationContextAware)viewResolver).setApplicationContext(applicationContext);
			}
		}
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		for (ViewResolver viewResolver : this.viewResolvers) {
			if (viewResolver instanceof ServletContextAware) {
				((ServletContextAware)viewResolver).setServletContext(servletContext);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 扫描所有 ViewResolver 的 InitializingBean 接口，调用 afterPropertiesSet()
		// 疑问？为啥 Spring 容器自己不调用呢？
		for (ViewResolver viewResolver : this.viewResolvers) {
			if (viewResolver instanceof InitializingBean) {
				((InitializingBean) viewResolver).afterPropertiesSet();
			}
		}
	}

	@Override
	@Nullable
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		// 遍历 viewResolvers 数组，逐个进行解析，但凡成功，则返回该 View 对象
		for (ViewResolver viewResolver : this.viewResolvers) {
			// 执行解析
			View view = viewResolver.resolveViewName(viewName, locale);
			// 解析成功，则返回该 View 对象
			if (view != null) {
				return view;
			}
		}
		return null;
	}

}
