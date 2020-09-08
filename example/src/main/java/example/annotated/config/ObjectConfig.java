package example.annotated.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * author: sin
 * time: 2020/5/18 16:32
 */
@Configuration
@ComponentScan("example")
public class ObjectConfig {

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			if (i % 2 == 0) {
				System.err.println((i - 1) % 2);
			}
		}
	}
}
