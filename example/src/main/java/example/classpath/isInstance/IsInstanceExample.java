package example.classpath.isInstance;

/**
 * isInstance 测试
 *
 * author: sin
 * time: 2020/4/9 9:32 上午
 */
public class IsInstanceExample {

	public static void main(String[] args) {
		A a = new A();
		B b = new B();
		A ab = new B();

		System.err.println(a instanceof A);
		System.err.println(A.class.isInstance(a));

		System.err.println("isInstance 1.");
		System.err.println(b.getClass().isInstance(a));
	}

	public static class A {

	}

	public static class B extends A {

	}
}
