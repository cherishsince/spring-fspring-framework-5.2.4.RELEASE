package example.classpath.postProcessors;

import example.classpath.beans.UserServiceExample;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * author: sin
 * time: 2020/5/11 13:57
 */
public class PostProcessorsT1 implements BeanPostProcessor {

	private int init = 0;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		System.err.println(beanName + " 初始化.");
		if (bean instanceof UserServiceExample) {
			UserServiceExample serviceExample = (UserServiceExample) bean;
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		System.err.println(beanName + " 初始化完成.");
		if (bean instanceof UserServiceExample) {
			UserServiceExample serviceExample = (UserServiceExample) bean;
		}
		return bean;
	}
}
