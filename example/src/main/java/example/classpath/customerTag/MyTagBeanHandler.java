package example.classpath.customerTag;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * author: sin
 * time: 2020/5/27 9:38
 */
public class MyTagBeanHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("my-tag", new MyTagParser());
	}
}
