package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class WxpUserTb {
    private Long id;

    private String openid;

    private Long createTime;

    private Long uin;

    private BigDecimal balance;

    private String carNumber;

    private Integer unionState;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUin() {
        return uin;
    }

    public void setUin(Long uin) {
        this.uin = uin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber == null ? null : carNumber.trim();
    }

    public Integer getUnionState() {
        return unionState;
    }

    public void setUnionState(Integer unionState) {
        this.unionState = unionState;
    }
}