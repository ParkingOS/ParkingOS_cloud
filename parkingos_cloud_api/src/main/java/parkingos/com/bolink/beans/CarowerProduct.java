package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class CarowerProduct {
    private Long id;

    private Long pid;

    private Long uin;

    @Override
    public String toString() {
        return "CarowerProduct{" +
                "id=" + id +
                ", pid=" + pid +
                ", uin=" + uin +
                ", createTime=" + createTime +
                ", bTime=" + bTime +
                ", eTime=" + eTime +
                ", total=" + total +
                ", remark='" + remark + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", pLot='" + pLot + '\'' +
                ", actTotal=" + actTotal +
                ", etcId='" + etcId + '\'' +
                ", updateTime=" + updateTime +
                ", cardId='" + cardId + '\'' +
                ", memberId='" + memberId + '\'' +
                ", carNumber='" + carNumber + '\'' +
                ", comId=" + comId +
                ", mobile='" + mobile + '\'' +
                ", isDelete=" + isDelete +
                ", tel='" + tel + '\'' +
                ", carTypeId=" + carTypeId +
                ", limitDayType=" + limitDayType +
                '}';
    }

    private Long createTime;

    private Long bTime;

    private Long eTime;

    private BigDecimal total;

    private String remark;

    private String name;

    private String address;

    private String pLot;

    private BigDecimal actTotal;

    private String etcId;

    private Long updateTime;

    private String cardId;

    private String memberId;

    private String carNumber;

    private Long comId;

    private String mobile;

    private Long isDelete;

    private String tel;

    private Long carTypeId;

    private Integer limitDayType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Long getUin() {
        return uin;
    }

    public void setUin(Long uin) {
        this.uin = uin;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getbTime() {
        return bTime;
    }

    public void setbTime(Long bTime) {
        this.bTime = bTime;
    }

    public Long geteTime() {
        return eTime;
    }

    public void seteTime(Long eTime) {
        this.eTime = eTime;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getpLot() {
        return pLot;
    }

    public void setpLot(String pLot) {
        this.pLot = pLot == null ? null : pLot.trim();
    }

    public BigDecimal getActTotal() {
        return actTotal;
    }

    public void setActTotal(BigDecimal actTotal) {
        this.actTotal = actTotal;
    }

    public String getEtcId() {
        return etcId;
    }

    public void setEtcId(String etcId) {
        this.etcId = etcId == null ? null : etcId.trim();
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId == null ? null : cardId.trim();
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId == null ? null : memberId.trim();
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber == null ? null : carNumber.trim();
    }

    public Long getComId() {
        return comId;
    }

    public void setComId(Long comId) {
        this.comId = comId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public Long getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Long isDelete) {
        this.isDelete = isDelete;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel == null ? null : tel.trim();
    }

    public Long getCarTypeId() {
        return carTypeId;
    }

    public void setCarTypeId(Long carTypeId) {
        this.carTypeId = carTypeId;
    }

    public Integer getLimitDayType() {
        return limitDayType;
    }

    public void setLimitDayType(Integer limitDayType) {
        this.limitDayType = limitDayType;
    }
}