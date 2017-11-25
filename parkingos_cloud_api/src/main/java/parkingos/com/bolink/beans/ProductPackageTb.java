package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class ProductPackageTb {
    private Long id;

    private String pName;

    private Integer bTime;

    private Integer eTime;

    private Integer remainNumber;

    private Integer state;

    private Long comid;

    private BigDecimal price;

    private Integer bmin;

    private Integer emin;

    private String resume;

    private BigDecimal oldPrice;

    private Integer type;

    private Integer reserved;

    private Long limitday;

    private Integer favourablePrecent;

    private Integer freeMinutes;

    private Integer outFavourablePrecent;

    private Long scope;

    private String cardId;

    private Long updateTime;

    private Long createTime;

    private String describe;

    private Integer operateType;

    private Integer isSync;

    private Long isDelete;

    private String carTypeId;

    private String period;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName == null ? null : pName.trim();
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

    public Integer getRemainNumber() {
        return remainNumber;
    }

    public void setRemainNumber(Integer remainNumber) {
        this.remainNumber = remainNumber;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
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

    public Integer getBmin() {
        return bmin;
    }

    public void setBmin(Integer bmin) {
        this.bmin = bmin;
    }

    public Integer getEmin() {
        return emin;
    }

    public void setEmin(Integer emin) {
        this.emin = emin;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume == null ? null : resume.trim();
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getReserved() {
        return reserved;
    }

    public void setReserved(Integer reserved) {
        this.reserved = reserved;
    }

    public Long getLimitday() {
        return limitday;
    }

    public void setLimitday(Long limitday) {
        this.limitday = limitday;
    }

    public Integer getFavourablePrecent() {
        return favourablePrecent;
    }

    public void setFavourablePrecent(Integer favourablePrecent) {
        this.favourablePrecent = favourablePrecent;
    }

    public Integer getFreeMinutes() {
        return freeMinutes;
    }

    public void setFreeMinutes(Integer freeMinutes) {
        this.freeMinutes = freeMinutes;
    }

    public Integer getOutFavourablePrecent() {
        return outFavourablePrecent;
    }

    public void setOutFavourablePrecent(Integer outFavourablePrecent) {
        this.outFavourablePrecent = outFavourablePrecent;
    }

    public Long getScope() {
        return scope;
    }

    public void setScope(Long scope) {
        this.scope = scope;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId == null ? null : cardId.trim();
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
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

    public String getCarTypeId() {
        return carTypeId;
    }

    public void setCarTypeId(String carTypeId) {
        this.carTypeId = carTypeId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period == null ? null : period.trim();
    }
}