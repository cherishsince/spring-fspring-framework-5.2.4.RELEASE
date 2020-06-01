package example.classpath;

import example.classpath.beans.AccountExample;
import example.classpath.beans.UserExample;
import example.classpath.beans.UserServiceExample;
import example.classpath.customerTag.MyTagBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 配置
 *
 * author: sin
 * time: 2020/3/18 11:27 上午
 */
@Configuration
public class ApplicationContextExample {

//    @Bean
//    public UserServiceExample userServiceExample() {
//        return new UserServiceExample();
//    }
//
//    @Bean
//    public AccountExample accountExample() {
//        return new AccountExample().setAmount(10000000).setPaymentPassword("123").setUsername("sin");
//    }
//
//    @Bean
//    public UserExample userExample() {
//        return new UserExample().setUsername("sin").setGender("男").setAge(18).setPassword("123");
//    }

    public static void main(String[] args) {
    	System.setProperty("applicationName", "example");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath*:*/${applicationName}-classPathApplicationContext.xml", "classpath*:*/classPathApplicationContext.xml");
        System.err.println(context.getId());
        AccountExample accountExample = context.getBean(AccountExample.class);
        System.err.println("accountExample: " + accountExample);
        System.err.println("UserExample: " + context.getBean(UserExample.class));
        System.err.println("MyTagBean: " + context.getBean(MyTagBean.class));
    }
}
