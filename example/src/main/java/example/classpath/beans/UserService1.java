package example.classpath.beans;

/**
 * author: sin
 * time: 2020/6/15 11:22
 */
public class UserService1 {

	private UserService2 userService2;

	public void setUserService2(UserService2 userService2) {
		this.userService2 = userService2;
	}

	@Override
	public String toString() {
		return "UserService1{" +
				"userService2=" + userService2 +
				'}';
	}
}
