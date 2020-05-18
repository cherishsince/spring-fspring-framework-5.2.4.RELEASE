package example.classpath.beans;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户 service
 *
 * author: sin
 * time: 2020/3/18 11:17 上午
 */
@Service
public class UserServiceExample implements BeanNameAware, InitializingBean {

	private int init = 0;

    @Autowired(required = true)
    private UserExample userExample;

    @Autowired
    private AccountExample accountExample;

    public void login() {
        System.err.println("用户信息: " + userExample);
        System.err.println("账户信息: " + accountExample);
    }

	@Override
	public void setBeanName(String name) {
		System.err.println("setBeanName: " + name);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.err.println("afterPropertiesSet...");
		init = 1;
	}
}
