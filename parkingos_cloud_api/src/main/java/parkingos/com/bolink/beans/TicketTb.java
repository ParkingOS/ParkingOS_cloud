package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class TicketTb {
    private Long id;

    private Long createTime;

    private Long limitDay;

    private Integer money;

    private Integer state;

    private Long uin;

    private Long comid;

    private Long utime;

    private BigDecimal umoney;

    private Integer type;

    private Long orderid;

    private BigDecimal bmoney;

    private String wxpOrderid;

    private Long shopId;

    private Integer resources;

    private Integer isBackMoney;

    private BigDecimal pmoney;

    private Integer needSync;

    private String ticketId;

    private String operateUser;

    private String stateZh;

    private String carNumber;

    private String typeZh;

    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getLimitDay() {
        return limitDay;
    }

    public void setLimitDay(Long limitDay) {
        this.limitDay = limitDay;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getUin() {
        return uin;
    }

    public void setUin(Long uin) {
        this.uin = uin;
    }

    public Long getComid() {
        return comid;
    }

    public void setComid(Long comid) {
        this.comid = comid;
    }

    public Long getUtime() {
        return utime;
    }

    public void setUtime(Long utime) {
        this.utime = utime;
    }

    public BigDecimal getUmoney() {
        return umoney;
    }

    public void setUmoney(BigDecimal umoney) {
        this.umoney = umoney;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public BigDecimal getBmoney() {
        return bmoney;
    }

    public void setBmoney(BigDecimal bmoney) {
        this.bmoney = bmoney;
    }

    public String getWxpOrderid() {
        return wxpOrderid;
    }

    public void setWxpOrderid(String wxpOrderid) {
        this.wxpOrderid = wxpOrderid == null ? null : wxpOrderid.trim();
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Integer getResources() {
        return resources;
    }

    public void setResources(Integer resources) {
        this.resources = resources;
    }

    public Integer getIsBackMoney() {
        return isBackMoney;
    }

    public void setIsBackMoney(Integer isBackMoney) {
        this.isBackMoney = isBackMoney;
    }

    public BigDecimal getPmoney() {
        return pmoney;
    }

    public void setPmoney(BigDecimal pmoney) {
        this.pmoney = pmoney;
    }

    public Integer getNeedSync() {
        return needSync;
    }

    public void setNeedSync(Integer needSync) {
        this.needSync = needSync;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId == null ? null : ticketId.trim();
    }

    public String getOperateUser() {
        return operateUser;
    }

    public void setOperateUser(String operateUser) {
        this.operateUser = operateUser == null ? null : operateUser.trim();
    }

    public String getStateZh() {
        return stateZh;
    }

    public void setStateZh(String stateZh) {
        this.stateZh = stateZh == null ? null : stateZh.trim();
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber == null ? null : carNumber.trim();
    }

    public String getTypeZh() {
        return typeZh;
    }

    public void setTypeZh(String typeZh) {
        this.typeZh = typeZh == null ? null : typeZh.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }
}