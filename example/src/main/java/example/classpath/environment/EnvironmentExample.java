package example.classpath.environment;

import org.springframework.core.env.StandardEnvironment;

/**
 * author: sin
 * time: 2020/5/9 11:38
 */
public class EnvironmentExample {

	public static void main(String[] args) {
		StandardEnvironment standardEnvironment = new StandardEnvironment();
		System.err.println(standardEnvironment.getProperty("age"));
	}
}
