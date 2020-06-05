package example.classpath.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * author: sin
 * time: 2020/6/4 16:24
 */
@Repository
public class UserA {

	@Autowired
	private UserB userB;


}
