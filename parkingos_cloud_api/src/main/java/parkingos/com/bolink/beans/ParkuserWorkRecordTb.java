package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class ParkuserWorkRecordTb {
    private Long id;

    private Long startTime;

    private Long endTime;

    private Long worksiteId;

    private Long uid;

    private Long berthsecId;

    private String deviceCode;

    private Long chanid;

    private String uuid;

    private Integer state;

    private BigDecimal historyMoney;

    private String outLog;

    private Short logonState;

    private Short logoffState;

    private Long parkId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getWorksiteId() {
        return worksiteId;
    }

    public void setWorksiteId(Long worksiteId) {
        this.worksiteId = worksiteId;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getBerthsecId() {
        return berthsecId;
    }

    public void setBerthsecId(Long berthsecId) {
        this.berthsecId = berthsecId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode == null ? null : deviceCode.trim();
    }

    public Long getChanid() {
        return chanid;
    }

    public void setChanid(Long chanid) {
        this.chanid = chanid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null ? null : uuid.trim();
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public BigDecimal getHistoryMoney() {
        return historyMoney;
    }

    public void setHistoryMoney(BigDecimal historyMoney) {
        this.historyMoney = historyMoney;
    }

    public String getOutLog() {
        return outLog;
    }

    public void setOutLog(String outLog) {
        this.outLog = outLog == null ? null : outLog.trim();
    }

    public Short getLogonState() {
        return logonState;
    }

    public void setLogonState(Short logonState) {
        this.logonState = logonState;
    }

    public Short getLogoffState() {
        return logoffState;
    }

    public void setLogoffState(Short logoffState) {
        this.logoffState = logoffState;
    }

    public Long getParkId() {
        return parkId;
    }

    public void setParkId(Long parkId) {
        this.parkId = parkId;
    }
}