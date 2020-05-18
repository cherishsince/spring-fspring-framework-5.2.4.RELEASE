package example.annotated;

import example.annotated.config.ObjectConfig;
import example.classpath.beans.UserExample;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * author: sin
 * time: 2020/5/13 14:27
 */
public class AnnotatedApplication {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(ObjectConfig.class);
		applicationContext.scan("example");
		applicationContext.refresh();

		UserExample userExample = applicationContext.getBean(UserExample.class);
		System.err.println(userExample);
	}
}
