package example.messageSource;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Locale;

/**
 * 消息 国际化
 *
 * author: Sin
 * time: 2020/6/23 9:06
 */
public class MessageSourceExample {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("messageSource/message-source.xml");

//		String appName = messageSource.getMessage("appName", null, Locale.ENGLISH);
//		String label_1 = messageSource.getMessage("label_1", null, Locale.ENGLISH);
//		System.err.println("label_1: " + label_1);
//		System.err.println("appName: " + appName);

		System.err.println(context.getMessage("appName", null, Locale.getDefault()));
		System.err.println(context.getMessage("appName", null, Locale.CANADA));
		System.err.println(context.getMessage("appName", null, Locale.US));
	}
}
