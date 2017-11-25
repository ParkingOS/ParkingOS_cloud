package parkingos.com.bolink.dto;

import java.math.BigDecimal;

public class WXUserView {

    private Integer bindflag;
    private Long uin ;
    private String mobile ;
    private BigDecimal balance;

    public Integer getBindflag() {
        return bindflag;
    }

    public void setBindflag(Integer bindflag) {
        this.bindflag = bindflag;
    }

    public Long getUin() {
        return uin;
    }

    public void setUin(Long uin) {
        this.uin = uin;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "WXUserView{" +
                "bindflag=" + bindflag +
                ", uin=" + uin +
                ", mobile='" + mobile + '\'' +
                ", balance=" + balance +
                '}';
    }
}
