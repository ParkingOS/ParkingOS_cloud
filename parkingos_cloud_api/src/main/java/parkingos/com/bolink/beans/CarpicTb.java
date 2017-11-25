package parkingos.com.bolink.beans;

public class CarpicTb {
    private Integer id;

    private String orderId;

    private Integer createTime;

    private String carNumber;

    private String picType;

    private String resume;

    private String comid;

    private Integer updateTime;

    private String content;

    private String pictureSource;

    private String liftrodId;

    private String parkOrderType;

    private String carpicTableName;

    private String liftpicTableName;

    private String eventId;

    private String confirmpicTableName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber == null ? null : carNumber.trim();
    }

    public String getPicType() {
        return picType;
    }

    public void setPicType(String picType) {
        this.picType = picType == null ? null : picType.trim();
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume == null ? null : resume.trim();
    }

    public String getComid() {
        return comid;
    }

    public void setComid(String comid) {
        this.comid = comid == null ? null : comid.trim();
    }

    public Integer getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Integer updateTime) {
        this.updateTime = updateTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getPictureSource() {
        return pictureSource;
    }

    public void setPictureSource(String pictureSource) {
        this.pictureSource = pictureSource == null ? null : pictureSource.trim();
    }

    public String getLiftrodId() {
        return liftrodId;
    }

    public void setLiftrodId(String liftrodId) {
        this.liftrodId = liftrodId == null ? null : liftrodId.trim();
    }

    public String getParkOrderType() {
        return parkOrderType;
    }

    public void setParkOrderType(String parkOrderType) {
        this.parkOrderType = parkOrderType == null ? null : parkOrderType.trim();
    }

    public String getCarpicTableName() {
        return carpicTableName;
    }

    public void setCarpicTableName(String carpicTableName) {
        this.carpicTableName = carpicTableName == null ? null : carpicTableName.trim();
    }

    public String getLiftpicTableName() {
        return liftpicTableName;
    }

    public void setLiftpicTableName(String liftpicTableName) {
        this.liftpicTableName = liftpicTableName == null ? null : liftpicTableName.trim();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId == null ? null : eventId.trim();
    }

    public String getConfirmpicTableName() {
        return confirmpicTableName;
    }

    public void setConfirmpicTableName(String confirmpicTableName) {
        this.confirmpicTableName = confirmpicTableName == null ? null : confirmpicTableName.trim();
    }
}