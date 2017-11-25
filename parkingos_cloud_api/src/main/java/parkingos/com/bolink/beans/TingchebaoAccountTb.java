package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class TingchebaoAccountTb {
    private Long id;

    private BigDecimal amount;

    private Integer type;

    private Long createTime;

    private String remark;

    private Integer utype;

    private Long orderid;

    private Long withdrawId;

    private String onlineOrderid;

    private Long uin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Integer getUtype() {
        return utype;
    }

    public void setUtype(Integer utype) {
        this.utype = utype;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public Long getWithdrawId() {
        return withdrawId;
    }

    public void setWithdrawId(Long withdrawId) {
        this.withdrawId = withdrawId;
    }

    public String getOnlineOrderid() {
        return onlineOrderid;
    }

    public void setOnlineOrderid(String onlineOrderid) {
        this.onlineOrderid = onlineOrderid == null ? null : onlineOrderid.trim();
    }

    public Long getUin() {
        return uin;
    }

    public void setUin(Long uin) {
        this.uin = uin;
    }
}