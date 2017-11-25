package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class PriceTb {
    private Long id;

    private Long comid;

    private BigDecimal price;

    private Long state;

    private Integer unit;

    private Integer payType;

    private Long createTime;

    private Integer bTime;

    private Integer eTime;

    private Integer isSale;

    private Integer firstTimes;

    private BigDecimal fprice;

    private Integer countless;

    private Integer freeTime;

    private Integer fpayType;

    private Integer isnight;

    private Integer isedit;

    private Integer carType;

    private Integer isFulldaytime;

    private BigDecimal total24;

    private Integer bMinute;

    private Integer eMinute;

    private Long updateTime;

    private String priceId;

    private String carTypeZh;

    private String describe;

    private Integer operateType;

    private Integer isSync;

    private Long isDelete;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComid() {
        return comid;
    }

    public void setComid(Long comid) {
        this.comid = comid;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getState() {
        return state;
    }

    public void setState(Long state) {
        this.state = state;
    }

    public Integer getUnit() {
        return unit;
    }

    public void setUnit(Integer unit) {
        this.unit = unit;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getbTime() {
        return bTime;
    }

    public void setbTime(Integer bTime) {
        this.bTime = bTime;
    }

    public Integer geteTime() {
        return eTime;
    }

    public void seteTime(Integer eTime) {
        this.eTime = eTime;
    }

    public Integer getIsSale() {
        return isSale;
    }

    public void setIsSale(Integer isSale) {
        this.isSale = isSale;
    }

    public Integer getFirstTimes() {
        return firstTimes;
    }

    public void setFirstTimes(Integer firstTimes) {
        this.firstTimes = firstTimes;
    }

    public BigDecimal getFprice() {
        return fprice;
    }

    public void setFprice(BigDecimal fprice) {
        this.fprice = fprice;
    }

    public Integer getCountless() {
        return countless;
    }

    public void setCountless(Integer countless) {
        this.countless = countless;
    }

    public Integer getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(Integer freeTime) {
        this.freeTime = freeTime;
    }

    public Integer getFpayType() {
        return fpayType;
    }

    public void setFpayType(Integer fpayType) {
        this.fpayType = fpayType;
    }

    public Integer getIsnight() {
        return isnight;
    }

    public void setIsnight(Integer isnight) {
        this.isnight = isnight;
    }

    public Integer getIsedit() {
        return isedit;
    }

    public void setIsedit(Integer isedit) {
        this.isedit = isedit;
    }

    public Integer getCarType() {
        return carType;
    }

    public void setCarType(Integer carType) {
        this.carType = carType;
    }

    public Integer getIsFulldaytime() {
        return isFulldaytime;
    }

    public void setIsFulldaytime(Integer isFulldaytime) {
        this.isFulldaytime = isFulldaytime;
    }

    public BigDecimal getTotal24() {
        return total24;
    }

    public void setTotal24(BigDecimal total24) {
        this.total24 = total24;
    }

    public Integer getbMinute() {
        return bMinute;
    }

    public void setbMinute(Integer bMinute) {
        this.bMinute = bMinute;
    }

    public Integer geteMinute() {
        return eMinute;
    }

    public void seteMinute(Integer eMinute) {
        this.eMinute = eMinute;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId == null ? null : priceId.trim();
    }

    public String getCarTypeZh() {
        return carTypeZh;
    }

    public void setCarTypeZh(String carTypeZh) {
        this.carTypeZh = carTypeZh == null ? null : carTypeZh.trim();
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe == null ? null : describe.trim();
    }

    public Integer getOperateType() {
        return operateType;
    }

    public void setOperateType(Integer operateType) {
        this.operateType = operateType;
    }

    public Integer getIsSync() {
        return isSync;
    }

    public void setIsSync(Integer isSync) {
        this.isSync = isSync;
    }

    public Long getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Long isDelete) {
        this.isDelete = isDelete;
    }
}