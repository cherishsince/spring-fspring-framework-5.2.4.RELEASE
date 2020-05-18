package example.classpath.beans;

/**
 * 用户 A
 *
 * author: sin
 * time: 2020/3/18 11:11 上午
 */
public class UserExample {

    private String username;

    private String gender;

    private Integer age;

    private String password;

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", password='" + password + '\'' +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public UserExample setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public UserExample setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public UserExample setAge(Integer age) {
        this.age = age;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserExample setPassword(String password) {
        this.password = password;
        return this;
    }
}
