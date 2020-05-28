package example.classpath.customerTag;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * author: sin
 * time: 2020/5/27 9:27
 */
public class MyTagParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return MyTagBean.class;
	}

	@Override
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
		builder.addPropertyValue("name", element.getAttribute("name"));
		builder.addPropertyValue("gender", element.getAttribute("gender"));
	}
}
