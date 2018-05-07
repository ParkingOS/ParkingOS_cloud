package parkingos.com.bolink.beans;

public class MonthPriceTb {
    private Long id;

    private String tradeNo;

    private String datastr;

    private Long ctime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo == null ? null : tradeNo.trim();
    }

    public String getDatastr() {
        return datastr;
    }

    public void setDatastr(String datastr) {
        this.datastr = datastr == null ? null : datastr.trim();
    }

    public Long getCtime() {
        return ctime;
    }

    public void setCtime(Long ctime) {
        this.ctime = ctime;
    }

    @Override
    public String toString() {
        return "MonthPriceTb{" +
                "id=" + id +
                ", tradeNo='" + tradeNo + '\'' +
                ", datastr='" + datastr + '\'' +
                ", ctime=" + ctime +
                '}';
    }
}