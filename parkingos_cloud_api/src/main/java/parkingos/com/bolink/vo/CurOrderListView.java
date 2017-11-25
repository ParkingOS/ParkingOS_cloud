package parkingos.com.bolink.vo;

public class CurOrderListView {

    private Integer state;
    private String total;
    private Integer isLocked;
    private String carNumber;
    private String orderId;
    private Long id;//订单主键
    private String inParkTime;//进场时间
    private String parkName;
    private Long comId;

    public Long getComId() {
        return comId;
    }

    public void setComId(Long comId) {
        this.comId = comId;
    }

    public String getParkName() {
        return parkName;
    }

    public void setParkName(String parkName) {
        this.parkName = parkName;
    }

    public Integer getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Integer isLocked) {
        this.isLocked = isLocked;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInParkTime() {
        return inParkTime;
    }

    public void setInParkTime(String inParkTime) {
        this.inParkTime = inParkTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "CurOrderListView{" +
                "state=" + state +
                ", total=" + total +
                ", isLocked=" + isLocked +
                ", carNumber='" + carNumber + '\'' +
                ", orderId='" + orderId + '\'' +
                ", id='" + id + '\'' +
                ", inParkTime='" + inParkTime + '\'' +
                '}';
    }
}
