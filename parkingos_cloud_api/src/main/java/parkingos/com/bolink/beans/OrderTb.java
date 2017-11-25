package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class OrderTb {
    private Long id;

    private Long createTime;

    private Long comid;

    private Long uin;

    private BigDecimal total;

    private Integer state;

    private Long endTime;

    private Integer autoPay;

    private Integer payType;

    private String nfcUuid;

    private Long uid;

    private String carNumber;

    private String imei;

    private Integer pid;

    private Integer preState;

    private Integer type;

    private Integer needSync;

    private Integer ishd;

    private Integer isclick;

    private BigDecimal prepaid;

    private Long prepaidPayTime;

    private Long berthnumber;

    private Long berthsecId;

    private Long groupid;

    private Long outUid;

    private Integer isUnionUser;

    private String carTypeZh;

    private String orderIdLocal;

    private Long duration;

    private String payTypeEn;

    private String freereasonsLocal;

    private Integer islocked;

    private String lockKey;

    private String inPassid;

    private String outPassid;

    private BigDecimal amountReceivable;

    private BigDecimal electronicPrepay;

    private BigDecimal electronicPay;

    private BigDecimal cashPrepay;

    private BigDecimal cashPay;

    private BigDecimal reduceAmount;

    private String cType;

    private String carType;

    private String freereasons;

    private String remark;

    private String carpicTableName;

    private String workStationUuid;

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

    public Long getComid() {
        return comid;
    }

    public void setComid(Long comid) {
        this.comid = comid;
    }

    public Long getUin() {
        return uin;
    }

    public void setUin(Long uin) {
        this.uin = uin;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getAutoPay() {
        return autoPay;
    }

    public void setAutoPay(Integer autoPay) {
        this.autoPay = autoPay;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getNfcUuid() {
        return nfcUuid;
    }

    public void setNfcUuid(String nfcUuid) {
        this.nfcUuid = nfcUuid == null ? null : nfcUuid.trim();
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber == null ? null : carNumber.trim();
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei == null ? null : imei.trim();
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getPreState() {
        return preState;
    }

    public void setPreState(Integer preState) {
        this.preState = preState;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getNeedSync() {
        return needSync;
    }

    public void setNeedSync(Integer needSync) {
        this.needSync = needSync;
    }

    public Integer getIshd() {
        return ishd;
    }

    public void setIshd(Integer ishd) {
        this.ishd = ishd;
    }

    public Integer getIsclick() {
        return isclick;
    }

    public void setIsclick(Integer isclick) {
        this.isclick = isclick;
    }

    public BigDecimal getPrepaid() {
        return prepaid;
    }

    public void setPrepaid(BigDecimal prepaid) {
        this.prepaid = prepaid;
    }

    public Long getPrepaidPayTime() {
        return prepaidPayTime;
    }

    public void setPrepaidPayTime(Long prepaidPayTime) {
        this.prepaidPayTime = prepaidPayTime;
    }

    public Long getBerthnumber() {
        return berthnumber;
    }

    public void setBerthnumber(Long berthnumber) {
        this.berthnumber = berthnumber;
    }

    public Long getBerthsecId() {
        return berthsecId;
    }

    public void setBerthsecId(Long berthsecId) {
        this.berthsecId = berthsecId;
    }

    public Long getGroupid() {
        return groupid;
    }

    public void setGroupid(Long groupid) {
        this.groupid = groupid;
    }

    public Long getOutUid() {
        return outUid;
    }

    public void setOutUid(Long outUid) {
        this.outUid = outUid;
    }

    public Integer getIsUnionUser() {
        return isUnionUser;
    }

    public void setIsUnionUser(Integer isUnionUser) {
        this.isUnionUser = isUnionUser;
    }

    public String getCarTypeZh() {
        return carTypeZh;
    }

    public void setCarTypeZh(String carTypeZh) {
        this.carTypeZh = carTypeZh == null ? null : carTypeZh.trim();
    }

    public String getOrderIdLocal() {
        return orderIdLocal;
    }

    public void setOrderIdLocal(String orderIdLocal) {
        this.orderIdLocal = orderIdLocal == null ? null : orderIdLocal.trim();
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getPayTypeEn() {
        return payTypeEn;
    }

    public void setPayTypeEn(String payTypeEn) {
        this.payTypeEn = payTypeEn == null ? null : payTypeEn.trim();
    }

    public String getFreereasonsLocal() {
        return freereasonsLocal;
    }

    public void setFreereasonsLocal(String freereasonsLocal) {
        this.freereasonsLocal = freereasonsLocal == null ? null : freereasonsLocal.trim();
    }

    public Integer getIslocked() {
        return islocked;
    }

    public void setIslocked(Integer islocked) {
        this.islocked = islocked;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey == null ? null : lockKey.trim();
    }

    public String getInPassid() {
        return inPassid;
    }

    public void setInPassid(String inPassid) {
        this.inPassid = inPassid == null ? null : inPassid.trim();
    }

    public String getOutPassid() {
        return outPassid;
    }

    public void setOutPassid(String outPassid) {
        this.outPassid = outPassid == null ? null : outPassid.trim();
    }

    public BigDecimal getAmountReceivable() {
        return amountReceivable;
    }

    public void setAmountReceivable(BigDecimal amountReceivable) {
        this.amountReceivable = amountReceivable;
    }

    public BigDecimal getElectronicPrepay() {
        return electronicPrepay;
    }

    public void setElectronicPrepay(BigDecimal electronicPrepay) {
        this.electronicPrepay = electronicPrepay;
    }

    public BigDecimal getElectronicPay() {
        return electronicPay;
    }

    public void setElectronicPay(BigDecimal electronicPay) {
        this.electronicPay = electronicPay;
    }

    public BigDecimal getCashPrepay() {
        return cashPrepay;
    }

    public void setCashPrepay(BigDecimal cashPrepay) {
        this.cashPrepay = cashPrepay;
    }

    public BigDecimal getCashPay() {
        return cashPay;
    }

    public void setCashPay(BigDecimal cashPay) {
        this.cashPay = cashPay;
    }

    public BigDecimal getReduceAmount() {
        return reduceAmount;
    }

    public void setReduceAmount(BigDecimal reduceAmount) {
        this.reduceAmount = reduceAmount;
    }

    public String getcType() {
        return cType;
    }

    public void setcType(String cType) {
        this.cType = cType == null ? null : cType.trim();
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType == null ? null : carType.trim();
    }

    public String getFreereasons() {
        return freereasons;
    }

    public void setFreereasons(String freereasons) {
        this.freereasons = freereasons == null ? null : freereasons.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    @Override
    public String toString() {
        return "OrderTb{" +
                "id=" + id +
                ", createTime=" + createTime +
                ", comid=" + comid +
                ", uin=" + uin +
                ", total=" + total +
                ", state=" + state +
                ", endTime=" + endTime +
                ", autoPay=" + autoPay +
                ", payType=" + payType +
                ", nfcUuid='" + nfcUuid + '\'' +
                ", uid=" + uid +
                ", carNumber='" + carNumber + '\'' +
                ", imei='" + imei + '\'' +
                ", pid=" + pid +
                ", preState=" + preState +
                ", type=" + type +
                ", needSync=" + needSync +
                ", ishd=" + ishd +
                ", isclick=" + isclick +
                ", prepaid=" + prepaid +
                ", prepaidPayTime=" + prepaidPayTime +
                ", berthnumber=" + berthnumber +
                ", berthsecId=" + berthsecId +
                ", groupid=" + groupid +
                ", outUid=" + outUid +
                ", isUnionUser=" + isUnionUser +
                ", carTypeZh='" + carTypeZh + '\'' +
                ", orderIdLocal='" + orderIdLocal + '\'' +
                ", duration=" + duration +
                ", payTypeEn='" + payTypeEn + '\'' +
                ", freereasonsLocal='" + freereasonsLocal + '\'' +
                ", islocked=" + islocked +
                ", lockKey='" + lockKey + '\'' +
                ", inPassid='" + inPassid + '\'' +
                ", outPassid='" + outPassid + '\'' +
                ", amountReceivable=" + amountReceivable +
                ", electronicPrepay=" + electronicPrepay +
                ", electronicPay=" + electronicPay +
                ", cashPrepay=" + cashPrepay +
                ", cashPay=" + cashPay +
                ", reduceAmount=" + reduceAmount +
                ", cType='" + cType + '\'' +
                ", carType='" + carType + '\'' +
                ", freereasons='" + freereasons + '\'' +
                ", remark='" + remark + '\'' +
                ", carpicTableName='" + carpicTableName + '\'' +
                ", workStationUuid='" + workStationUuid + '\'' +
                '}';
    }

    public String getCarpicTableName() {
        return carpicTableName;
    }

    public void setCarpicTableName(String carpicTableName) {
        this.carpicTableName = carpicTableName == null ? null : carpicTableName.trim();
    }

    public String getWorkStationUuid() {
        return workStationUuid;
    }

    public void setWorkStationUuid(String workStationUuid) {
        this.workStationUuid = workStationUuid == null ? null : workStationUuid.trim();
    }
}