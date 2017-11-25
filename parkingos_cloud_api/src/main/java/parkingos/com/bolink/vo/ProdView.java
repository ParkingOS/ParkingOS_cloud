package parkingos.com.bolink.vo;

public class ProdView {

    private Integer state;
    private String cardId;
    private Long comId;
    private String carNumber;
    private String limitDate;
    private Long carOwnerProductId;
    private String parkName;
    private Long prodId;
    private Double price;
    private String prodName;
    private Long endTime;

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public Long getComId() {
        return comId;
    }

    public void setComId(Long comId) {
        this.comId = comId;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getLimitDate() {
        return limitDate;
    }

    public void setLimitDate(String limitDate) {
        this.limitDate = limitDate;
    }

    public Long getCarOwnerProductId() {
        return carOwnerProductId;
    }

    public void setCarOwnerProductId(Long carOwnerProductId) {
        this.carOwnerProductId = carOwnerProductId;
    }

    public String getParkName() {
        return parkName;
    }


    public void setParkName(String parkName) {
        this.parkName = parkName;
    }

    public Long getProdId() {
        return prodId;
    }

    public void setProdId(Long prodId) {
        this.prodId = prodId;
    }

    @Override
    public String toString() {
        return "ProdView{" +
                "state=" + state +
                ", cardId='" + cardId + '\'' +
                ", comId=" + comId +
                ", carNumber='" + carNumber + '\'' +
                ", limitDate='" + limitDate + '\'' +
                ", carOwnerProductId=" + carOwnerProductId +
                ", parkName='" + parkName + '\'' +
                ", prodId=" + prodId +
                ", price=" + price +
                ", prodName='" + prodName + '\'' +
                ", endTime=" + endTime +
                '}';
    }
}
