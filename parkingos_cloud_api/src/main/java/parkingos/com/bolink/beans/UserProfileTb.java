package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class UserProfileTb {
    private Long id;

    private Long uin;

    private Integer lowRecharge;

    private Integer voiceWarn;

    private Integer autoCash;

    private Integer enterWarn;

    private Long createTime;

    private Long updateTime;

    private Integer limitMoney;

    private BigDecimal bolinkLimit;

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

    public Integer getLowRecharge() {
        return lowRecharge;
    }

    public void setLowRecharge(Integer lowRecharge) {
        this.lowRecharge = lowRecharge;
    }

    public Integer getVoiceWarn() {
        return voiceWarn;
    }

    public void setVoiceWarn(Integer voiceWarn) {
        this.voiceWarn = voiceWarn;
    }

    public Integer getAutoCash() {
        return autoCash;
    }

    public void setAutoCash(Integer autoCash) {
        this.autoCash = autoCash;
    }

    public Integer getEnterWarn() {
        return enterWarn;
    }

    public void setEnterWarn(Integer enterWarn) {
        this.enterWarn = enterWarn;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getLimitMoney() {
        return limitMoney;
    }

    public void setLimitMoney(Integer limitMoney) {
        this.limitMoney = limitMoney;
    }

    public BigDecimal getBolinkLimit() {
        return bolinkLimit;
    }

    public void setBolinkLimit(BigDecimal bolinkLimit) {
        this.bolinkLimit = bolinkLimit;
    }
}