package example.classpath.beans;

/**
 * 账户资金
 *
 * author: sin
 * time: 2020/3/18 11:12 上午
 */
public class AccountExample {

    private String username;

    private Integer amount;

    private String paymentPassword;

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", amount=" + amount +
                ", paymentPassword='" + paymentPassword + '\'' +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public AccountExample setUsername(String username) {
        this.username = username;
        return this;
    }

    public Integer getAmount() {
        return amount;
    }

    public AccountExample setAmount(Integer amount) {
        this.amount = amount;
        return this;
    }

    public String getPaymentPassword() {
        return paymentPassword;
    }

    public AccountExample setPaymentPassword(String paymentPassword) {
        this.paymentPassword = paymentPassword;
        return this;
    }
}
