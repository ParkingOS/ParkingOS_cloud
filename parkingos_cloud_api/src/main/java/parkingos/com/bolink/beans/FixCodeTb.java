package parkingos.com.bolink.beans;

public class FixCodeTb {
    private String codeSrc;

    private String code;

    private Integer timeLimit;

    private Integer moneyLimit;

    private Integer freeLimit;

    private Integer type;

    private Integer amount;

    private Long createTime;

    private Long endTime;

    private Long uin;

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

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    private Integer state;

    private Long shopId;

    public String getCodeSrc() {
        return codeSrc;
    }

    public void setCodeSrc(String codeSrc) {
        this.codeSrc = codeSrc == null ? null : codeSrc.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getMoneyLimit() {
        return moneyLimit;
    }

    public void setMoneyLimit(Integer moneyLimit) {
        this.moneyLimit = moneyLimit;
    }

    public Integer getFreeLimit() {
        return freeLimit;
    }

    public void setFreeLimit(Integer freeLimit) {
        this.freeLimit = freeLimit;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}