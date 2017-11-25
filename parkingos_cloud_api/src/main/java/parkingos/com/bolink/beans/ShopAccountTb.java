package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class ShopAccountTb {
    private Integer id;

    private Integer shopId;

    private String shopName;

    private Integer ticketLimit;

    private Integer ticketfreeLimit;

    private Integer ticketMoney;

    private Long operateTime;

    private Long parkId;

    private Long operator;

    private String strid;

    private BigDecimal addMoney;

    private Integer operateType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName == null ? null : shopName.trim();
    }

    public Integer getTicketLimit() {
        return ticketLimit;
    }

    public void setTicketLimit(Integer ticketLimit) {
        this.ticketLimit = ticketLimit;
    }

    public Integer getTicketfreeLimit() {
        return ticketfreeLimit;
    }

    public void setTicketfreeLimit(Integer ticketfreeLimit) {
        this.ticketfreeLimit = ticketfreeLimit;
    }

    public Integer getTicketMoney() {
        return ticketMoney;
    }

    public void setTicketMoney(Integer ticketMoney) {
        this.ticketMoney = ticketMoney;
    }

    public Long getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Long operateTime) {
        this.operateTime = operateTime;
    }

    public Long getParkId() {
        return parkId;
    }

    public void setParkId(Long parkId) {
        this.parkId = parkId;
    }

    public Long getOperator() {
        return operator;
    }

    public void setOperator(Long operator) {
        this.operator = operator;
    }

    public String getStrid() {
        return strid;
    }

    public void setStrid(String strid) {
        this.strid = strid == null ? null : strid.trim();
    }

    public BigDecimal getAddMoney() {
        return addMoney;
    }

    public void setAddMoney(BigDecimal addMoney) {
        this.addMoney = addMoney;
    }

    public Integer getOperateType() {
        return operateType;
    }

    public void setOperateType(Integer operateType) {
        this.operateType = operateType;
    }
}