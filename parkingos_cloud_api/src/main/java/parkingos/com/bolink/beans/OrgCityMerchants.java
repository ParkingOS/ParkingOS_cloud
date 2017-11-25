package parkingos.com.bolink.beans;


import java.math.BigDecimal;

public class OrgCityMerchants {
    private Long id;

    private Long ctime;

    private String name;

    private Integer state;

    private BigDecimal balance;

    private String gps;

    private String address;

    private Integer isGroupPursue;

    private Integer isInparkIncity;

    private String unionId;

    private String ukey;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps == null ? null : gps.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public Integer getIsGroupPursue() {
        return isGroupPursue;
    }

    public void setIsGroupPursue(Integer isGroupPursue) {
        this.isGroupPursue = isGroupPursue;
    }

    public Integer getIsInparkIncity() {
        return isInparkIncity;
    }

    public void setIsInparkIncity(Integer isInparkIncity) {
        this.isInparkIncity = isInparkIncity;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId == null ? null : unionId.trim();
    }

    public String getUkey() {
        return ukey;
    }

    public void setUkey(String ukey) {
        this.ukey = ukey == null ? null : ukey.trim();
    }
}