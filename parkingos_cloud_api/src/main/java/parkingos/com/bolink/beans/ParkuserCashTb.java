package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class ParkuserCashTb {
    private Long id;

    private Long uin;

    private BigDecimal amount;

    private Long orderid;

    private Integer type;

    private Long createTime;

    private Integer ctype;

    private Integer target;

    private Long cardAccountId;

    private Long comid;

    private Long berthsegId;

    private Long berthId;

    private Long groupid;

    private Integer isDelete;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUin() {
        return uin;
    }

    public void setUin(Long uin) {
        this.uin = uin;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
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

    public Integer getCtype() {
        return ctype;
    }

    public void setCtype(Integer ctype) {
        this.ctype = ctype;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public Long getCardAccountId() {
        return cardAccountId;
    }

    public void setCardAccountId(Long cardAccountId) {
        this.cardAccountId = cardAccountId;
    }

    public Long getComid() {
        return comid;
    }

    public void setComid(Long comid) {
        this.comid = comid;
    }

    public Long getBerthsegId() {
        return berthsegId;
    }

    public void setBerthsegId(Long berthsegId) {
        this.berthsegId = berthsegId;
    }

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public Long getGroupid() {
        return groupid;
    }

    public void setGroupid(Long groupid) {
        this.groupid = groupid;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}