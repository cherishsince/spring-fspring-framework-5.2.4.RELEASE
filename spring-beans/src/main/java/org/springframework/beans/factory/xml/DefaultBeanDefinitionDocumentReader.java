/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link BeanDefinitionDocumentReader} interface that
 * reads bean definitions according to the "spring-beans" DTD and XSD format
 * (Spring's default XML bean definition format).
 *
 * <p>The structure, elements, and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). {@code <beans>} does not need to be the root
 * element of the XML document: this class will parse all bean definition elements
 * in the XML file, regardless of the actual root element.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

	public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

	public static final String NESTED_BEANS_ELEMENT = "beans";

	public static final String ALIAS_ELEMENT = "alias";

	public static final String NAME_ATTRIBUTE = "name";

	public static final String ALIAS_ATTRIBUTE = "alias";

	public static final String IMPORT_ELEMENT = "import";

	public static final String RESOURCE_ATTRIBUTE = "resource";

	public static final String PROFILE_ATTRIBUTE = "profile";


	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private XmlReaderContext readerContext;

	@Nullable
	private BeanDefinitionParserDelegate delegate;


	/**
	 * This implementation parses bean definitions according to the "spring-beans" XSD
	 * (or DTD, historically).
	 * <p>Opens a DOM Document; then initializes the default settings
	 * specified at the {@code <beans/>} level; then parses the contained bean definitions.
	 */
	@Override
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		this.readerContext = readerContext;
		doRegisterBeanDefinitions(doc.getDocumentElement());
	}

	/**
	 * Return the descriptor for the XML resource that this parser works on.
	 */
	protected final XmlReaderContext getReaderContext() {
		Assert.state(this.readerContext != null, "No XmlReaderContext available");
		return this.readerContext;
	}

	/**
	 * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor}
	 * to pull the source metadata from the supplied {@link Element}.
	 */
	@Nullable
	protected Object extractSource(Element ele) {
		return getReaderContext().extractSource(ele);
	}


	/**
	 * Register each bean definition within the given root {@code <beans/>} element.
	 */
	@SuppressWarnings("deprecation")  // for Environment.acceptsProfiles(String...)
	protected void doRegisterBeanDefinitions(Element root) {

		//任何嵌套的<beans>元素都将导致此方法中的递归。在
		//为了正确地传播和保留<beans>default-*属性，
		//跟踪当前（父）委托，该委托可能为空。创建
		//新的（子）委托，它引用父委托以进行回退，
		//然后最终将this.delegate重置回其原始（父）引用。
		//此行为模拟一个委托堆栈，而不实际需要一个委托。

		// Any nested <beans> elements will cause recursion in this method. In
		// order to propagate and preserve <beans> default-* attributes correctly,
		// keep track of the current (parent) delegate, which may be null. Create
		// the new (child) delegate with a reference to the parent for fallback purposes,
		// then ultimately reset this.delegate back to its original (parent) reference.
		// this behavior emulates a stack of delegates without actually necessitating one.

		// <1> 记录一下 old delegate，这里只是一个临时的保存，方法最后面会重新设置回去
		// 作用：是为了保留 this.delegate 是默认的 BeanDefinitionParserDelegate
		BeanDefinitionParserDelegate parent = this.delegate;
		// <2> 创建一个新的 delegate，每次解析都创建一个新的
		this.delegate = createDelegate(getReaderContext(), root, parent);

		// <3> 检查是 xml头(dtd、xsd) 只有是 http://www.springframework.org/schema/beans 下的标签才可以
		if (this.delegate.isDefaultNamespace(root)) {
			// <3.1> 这里就是获取 namespace 空间 <bean profile="dev"> 用于区分环境
			// 检查 profile 标记环境加载 dev prod
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			if (StringUtils.hasText(profileSpec)) {
				// <3.2> 就是字符串分割，不过空的会过滤掉 “,; ”
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
						profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				// <3.3> 如果所有 profiles 都无效，则不注册
				// We cannot use Profiles.of(...) since profile expressions are not supported
				// in XML config. See SPR-12458 for details.
				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
								"] not matching: " + getReaderContext().getResource());
					}
					return;
				}
			}
		}

		// <4> 钩子，跳过(读取前)
		preProcessXml(root);
		// <5> 解析 beanDefinitions
		parseBeanDefinitions(root, this.delegate);
		// <6> 钩子，跳过(读取后)
		postProcessXml(root);

		// <7> 将 old 的 delegate 返回去，始终都只保留默认的
		this.delegate = parent;
	}

	protected BeanDefinitionParserDelegate createDelegate(
			XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {
		// 创建 delegate 对象
		BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
		// 初始化默认
		delegate.initDefaults(root, parentDelegate);
		return delegate;
	}

	/**
	 * Parse the elements at the root level in the document:
	 * "import", "alias", "bean".
	 *
	 * @param root the DOM root element of the document
	 */
	protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		// <1> 默认命名空间，执行如下，检查是不是 http://www.springframework.org/schema/beans，下的标签
		if (delegate.isDefaultNamespace(root)) {
			// <2> 处理所有子节点，进行解析，里面还存在 递归情况，没有子节点就不会进入 for，一般root节点会有的
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					Element ele = (Element) node;
					// <2.1> 默认命名空间，执行如下，检查是不是 http://www.springframework.org/schema/beans，下的标签
					if (delegate.isDefaultNamespace(ele)) {
						parseDefaultElement(ele, delegate);
					} else {
						// <2.2> 自定义 或 扩展标签 解析，扩展的 BeanDefinition 解析全在里面了
						// 这里调用 parseCustomElement 原因是，默认的标签下，有自定义标签情况
						delegate.parseCustomElement(ele);
					}
				}
			}
		} else {
			// <3> 不是默认命名空间执行(自定义 或 扩展标签 解析)
			// 如：<tx:annotation-driven> <component-scan>
			delegate.parseCustomElement(root);
		}
	}

	private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
		// <1> 不同的标签，调用不同的解析规则
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			// 解析import
			importBeanDefinitionResource(ele);
		} else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			// 解析alias
			processAliasRegistration(ele);
		} else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			// 解析bean
			processBeanDefinition(ele, delegate);
		} else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			// 解析beans 这是一个递归
			doRegisterBeanDefinitions(ele);
		}
	}

	/**
	 * 解析 import 节点，并且加载 bean definitions
	 * <p>
	 * Parse an "import" element and load the bean definitions
	 * from the given resource into the bean factory.
	 */
	protected void importBeanDefinitionResource(Element ele) {
		// 获取 resource 属性
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		// location 如果是空，就 problemReporter 记异常
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		// 使用 resolve 解析 properties 值
		// Resolve system properties: e.g. "${user.dir}"
		location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);

		// 实际是，import resource 集合，因为可以 "," 导入多个
		Set<Resource> actualResources = new LinkedHashSet<>(4);

		// 判断 location 是 绝对路径 还是 相对路径
		// Discover whether the location is an absolute or relative URI
		boolean absoluteLocation = false;
		try {
			absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
		} catch (URISyntaxException ex) {
			// cannot convert to an URI, considering the location relative
			// unless it is the well-known Spring prefix "classpath*:"
		}

		// 处理：绝对路径
		// Absolute or relative?
		if (absoluteLocation) {
			try {
				int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
				if (logger.isTraceEnabled()) {
					logger.trace("Imported " + importCount + " bean definitions from URL location [" + location + "]");
				}
			} catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from URL location [" + location + "]", ele, ex);
			}
		} else {
			// 处理：相对路径
			// No URL -> considering resource location as relative to the current file.
			try {
				// 导入的数量
				int importCount;
				// 通过上下文的 resource，获取相对路径 location
				Resource relativeResource = getReaderContext().getResource().createRelative(location);
				// resource 存在
				if (relativeResource.exists()) {
					// 加载 bean definition
					importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
					// 将加载的 resource 放入集合
					actualResources.add(relativeResource);
				} else {
					// resource 不存在情况

					// 获取 base location，就是当前环境的 root 路径
					String baseLocation = getReaderContext().getResource().getURL().toString();
					// StringUtils.applyRelativePath(baseLocation, location)
					// 创建一个 相对路径的path
					// loadBeanDefinitions 加载 bean definition
					importCount = getReaderContext().getReader().loadBeanDefinitions(
							StringUtils.applyRelativePath(baseLocation, location), actualResources);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Imported " + importCount + " bean definitions from relative location [" + location + "]");
				}
			} catch (IOException ex) {
				getReaderContext().error("Failed to resolve current resource location", ele, ex);
			} catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from relative location [" + location + "]", ele, ex);
			}
		}

		// 发布 <import> 导入成功event事件
		Resource[] actResArray = actualResources.toArray(new Resource[0]);
		getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
	}

	/**
	 * 解析 <alias></alias> 标签，并注册
	 * <p>
	 * Process the given alias element, registering the alias with the registry.
	 */
	protected void processAliasRegistration(Element ele) {
		// 获取 name 属性(就是 <bean> name 属性一致 alias 属性就是别名)
		String name = ele.getAttribute(NAME_ATTRIBUTE);
		// 获取 alias 属性
		String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
		// 是否检查通过
		boolean valid = true;
		// name 是空，就抛出异常
		if (!StringUtils.hasText(name)) {
			getReaderContext().error("Name must not be empty", ele);
			valid = false;
		}
		// alias 是空，就抛出异常
		if (!StringUtils.hasText(alias)) {
			getReaderContext().error("Alias must not be empty", ele);
			valid = false;
		}
		// 检查通过，开始注册 alias组件
		if (valid) {
			try {
				// 调用 BeanDefinitionRegister 注册 alias
				// 最终调用的是 AliasRegistry，实现的是 BeanFactory
				getReaderContext().getRegistry().registerAlias(name, alias);
			} catch (Exception ex) {
				getReaderContext().error("Failed to register alias '" + alias +
						"' for bean with name '" + name + "'", ele, ex);
			}
			// 注册完成，发送 alias event事件通知
			getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
		}
	}

	/**
	 * Process the given bean element, parsing the bean definition
	 * and registering it with the registry.
	 */
	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		// <1> 创建 beanDefinition，并解析 xml 文件属性 设置到 beanDefinition
		// bdHolder 里面就是 BeanDefinition，
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		if (bdHolder != null) {
			// <2> 装饰 BeanDefinition，一般用于自定义标签
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				// <3> 注册 BeanDefinition
				// Register the final decorated instance.
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			} catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to register bean definition with name '" +
						bdHolder.getBeanName() + "'", ele, ex);
			}

			// <4> 注册完后，发送通知
			// Send registration event.
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}


	/**
	 * Allow the XML to be extensible by processing any custom element types first,
	 * before we start to process the bean definitions. This method is a natural
	 * extension point for any other custom pre-processing of the XML.
	 * <p>The default implementation is empty. Subclasses can override this method to
	 * convert custom elements into standard Spring bean definitions, for example.
	 * Implementors have access to the parser's bean definition reader and the
	 * underlying XML resource, through the corresponding accessors.
	 *
	 * @see #getReaderContext()
	 */
	protected void preProcessXml(Element root) {
	}

	/**
	 * Allow the XML to be extensible by processing any custom element types last,
	 * after we finished processing the bean definitions. This method is a natural
	 * extension point for any other custom post-processing of the XML.
	 * <p>The default implementation is empty. Subclasses can override this method to
	 * convert custom elements into standard Spring bean definitions, for example.
	 * Implementors have access to the parser's bean definition reader and the
	 * underlying XML resource, through the corresponding accessors.
	 *
	 * @see #getReaderContext()
	 */
	protected void postProcessXml(Element root) {
	}

}
