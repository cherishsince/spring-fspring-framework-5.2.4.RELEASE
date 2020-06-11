package example.classpath.beans;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * author: sin
 * time: 2020/6/9 15:27
 */
@Component
public class ValueBean {

	@Value("${spring.application}")
	private String application;

	private String name;

	@Override
	public String toString() {
		return "ValueBean{" +
				"application='" + application + '\'' +
				", name='" + name + '\'' +
				'}';
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
