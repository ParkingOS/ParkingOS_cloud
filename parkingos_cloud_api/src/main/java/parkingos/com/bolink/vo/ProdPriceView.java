package parkingos.com.bolink.vo;

public class ProdPriceView {
    private Integer state;
    private String errmsg;
    private String tradeNo;
    private String price;

    @Override
    public String toString() {
        return "ProdPriceView{" +
                "state=" + state +
                ", errmsg='" + errmsg + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", price='" + price + '\'' +
                '}';
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
