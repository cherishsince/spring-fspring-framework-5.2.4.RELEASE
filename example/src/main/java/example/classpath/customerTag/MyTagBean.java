package example.classpath.customerTag;

/**
 * author: sin
 * time: 2020/5/27 9:29
 */
public class MyTagBean {

	private String name;

	private String gender;

	@Override
	public String toString() {
		return "MyTagBean{" +
				"name='" + name + '\'' +
				", gender='" + gender + '\'' +
				'}';
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
}
