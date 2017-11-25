package parkingos.com.bolink.dto;

public class CurOrderPrice {

    private Integer state;
    private Double money;
    private Double prepay;
    private String errmsg;

    @Override
    public String toString() {
        return "CurOrderPrice{" +
                "state=" + state +
                ", money=" + money +
                ", prepay=" + prepay +
                ", errmsg='" + errmsg + '\'' +
                '}';
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public Double getPrepay() {
        return prepay;
    }

    public void setPrepay(Double prepay) {
        this.prepay = prepay;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
