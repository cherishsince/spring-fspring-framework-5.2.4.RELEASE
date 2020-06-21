/*
 * Copyright 2002-2012 the original author or authors.
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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.Conventions;
import org.springframework.lang.Nullable;

/**
 * Simple {@code NamespaceHandler} implementation that maps custom attributes
 * directly through to bean properties. An important point to note is that this
 * {@code NamespaceHandler} does not have a corresponding schema since there
 * is no way to know in advance all possible attribute names.
 *
 * <p>An example of the usage of this {@code NamespaceHandler} is shown below:
 *
 * <pre class="code">
 * &lt;bean id=&quot;rob&quot; class=&quot;..TestBean&quot; p:name=&quot;Rob Harrop&quot; p:spouse-ref=&quot;sally&quot;/&gt;</pre>
 *
 * Here the '{@code p:name}' corresponds directly to the '{@code name}'
 * property on class '{@code TestBean}'. The '{@code p:spouse-ref}'
 * attributes corresponds to the '{@code spouse}' property and, rather
 * than being the concrete value, it contains the name of the bean that will
 * be injected into that property.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class SimplePropertyNamespaceHandler implements NamespaceHandler {

	private static final String REF_SUFFIX = "-ref";


	@Override
	public void init() {
	}

	@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		parserContext.getReaderContext().error(
				"Class [" + getClass().getName() + "] does not support custom elements.", element);
		return null;
	}

	@Override
	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		// <1> 这里只处理 Attr 类型的数据
		if (node instanceof Attr) {
			Attr attr = (Attr) node;
			// <2> 获取标签的 name 属性, value 属性
			String propertyName = parserContext.getDelegate().getLocalName(attr);
			String propertyValue = attr.getValue();
			// <3> MutablePropertyValues 数 BeanDefinition 用于来描述属性的 "封装类",
			// 里面是一个map集合，有多少个属性就有多少条数据。
			MutablePropertyValues pvs = definition.getBeanDefinition().getPropertyValues();
			if (pvs.contains(propertyName)) {
				// 这里是异常收集，最后统一抛出异常
				parserContext.getReaderContext().error("Property '" + propertyName + "' is already defined using " +
						"both <property> and inline syntax. Only one approach may be used per property.", attr);
			}
			// <4> 属性名 xxx-ref 结尾的名称，需要进行 substring 截取
			// Conventions：这里是名字的转换，如：name-value -> nameValue
			// RuntimeBeanReference：spring 采用这种类，来队 BeanDefinition 解耦，这样就没有直接关系，需要的时候解析即可
			if (propertyName.endsWith(REF_SUFFIX)) {
				propertyName = propertyName.substring(0, propertyName.length() - REF_SUFFIX.length());
				pvs.add(Conventions.attributeNameToPropertyName(propertyName), new RuntimeBeanReference(propertyValue));
			}
			else {
				// <5> 这里和 <4> 一样
				pvs.add(Conventions.attributeNameToPropertyName(propertyName), propertyValue);
			}
		}
		return definition;
	}

}
