package parkingos.com.bolink.beans;

public class LiftRodTb {
    private Long id;

    private Long ctime;

    private Long uin;

    private Long comid;

    private String img;

    private Integer reason;

    private String liftrodId;

    private Long updateTime;

    private String inChannelId;

    private String outChannelId;

    private String name;

    private String carNumber;

    private String orderId;

    private String resume;

    private String passId;

    private String liftpicTableName;

    private Integer isDelete;

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCtime() {
        return ctime;
    }

    public void setCtime(Long ctime) {
        this.ctime = ctime;
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

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img == null ? null : img.trim();
    }

    public Integer getReason() {
        return reason;
    }

    public void setReason(Integer reason) {
        this.reason = reason;
    }

    public String getLiftrodId() {
        return liftrodId;
    }

    public void setLiftrodId(String liftrodId) {
        this.liftrodId = liftrodId == null ? null : liftrodId.trim();
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getInChannelId() {
        return inChannelId;
    }

    public void setInChannelId(String inChannelId) {
        this.inChannelId = inChannelId == null ? null : inChannelId.trim();
    }

    public String getOutChannelId() {
        return outChannelId;
    }

    public void setOutChannelId(String outChannelId) {
        this.outChannelId = outChannelId == null ? null : outChannelId.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber == null ? null : carNumber.trim();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume == null ? null : resume.trim();
    }

    public String getPassId() {
        return passId;
    }

    public void setPassId(String passId) {
        this.passId = passId == null ? null : passId.trim();
    }

    public String getLiftpicTableName() {
        return liftpicTableName;
    }

    public void setLiftpicTableName(String liftpicTableName) {
        this.liftpicTableName = liftpicTableName == null ? null : liftpicTableName.trim();
    }
}