package parkingos.com.bolink.beans;

import java.math.BigDecimal;

public class ShopTb {
    private Long id;

    private String name;

    private String address;

    private String mobile;

    private String phone;

    private Long comid;

    private Integer ticketLimit;

    private String description;

    private Integer state;

    private Long createTime;

    private Integer ticketfreeLimit;

    private Integer ticketType;

    private Integer ticketMoney;

    private String defaultLimit;

    private BigDecimal discountPercent;

    private BigDecimal discountMoney;

    private Integer validiteTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public Long getComid() {
        return comid;
    }

    public void setComid(Long comid) {
        this.comid = comid;
    }

    public Integer getTicketLimit() {
        return ticketLimit;
    }

    public void setTicketLimit(Integer ticketLimit) {
        this.ticketLimit = ticketLimit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getTicketfreeLimit() {
        return ticketfreeLimit;
    }

    public void setTicketfreeLimit(Integer ticketfreeLimit) {
        this.ticketfreeLimit = ticketfreeLimit;
    }

    public Integer getTicketType() {
        return ticketType;
    }

    public void setTicketType(Integer ticketType) {
        this.ticketType = ticketType;
    }

    public Integer getTicketMoney() {
        return ticketMoney;
    }

    public void setTicketMoney(Integer ticketMoney) {
        this.ticketMoney = ticketMoney;
    }

    public String getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(String defaultLimit) {
        this.defaultLimit = defaultLimit == null ? null : defaultLimit.trim();
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getDiscountMoney() {
        return discountMoney;
    }

    public void setDiscountMoney(BigDecimal discountMoney) {
        this.discountMoney = discountMoney;
    }

    public Integer getValiditeTime() {
        return validiteTime;
    }

    public void setValiditeTime(Integer validiteTime) {
        this.validiteTime = validiteTime;
    }
}