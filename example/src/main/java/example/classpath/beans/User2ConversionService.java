package example.classpath.beans;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ReflectionUtils;

/**
 * author: sin
 * time: 2020/6/15 11:23
 */
public class User2ConversionService implements Converter<String, UserService2> {
	@Override
	public UserService2 convert(String source) {
		UserService2 userService2 = new UserService2();
		ReflectionUtils.findField(userService2.getClass(), "name").setAccessible(true);
		ReflectionUtils.setField(ReflectionUtils.findField(userService2.getClass(), "name"), userService2, "刘德华");
		return userService2;
	}
}
