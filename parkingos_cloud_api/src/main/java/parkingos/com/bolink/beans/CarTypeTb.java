package parkingos.com.bolink.beans;

public class CarTypeTb {
    private Long id;

    private String name;

    private Long comid;

    private Integer sort;

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    private String cartypeId;

    private Long createTime;

    @Override
    public String toString() {
        return "CarTypeTb{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", comid=" + comid +
                ", sort=" + sort +
                ", cartypeId='" + cartypeId + '\'' +
                ", createTime=" + createTime +
                ", isDelete=" + isDelete +
                ", updateTime=" + updateTime +
                '}';
    }

    private  Integer isDelete;

    private Long updateTime;

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

    public Long getComid() {
        return comid;
    }

    public void setComid(Long comid) {
        this.comid = comid;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getCartypeId() {
        return cartypeId;
    }

    public void setCartypeId(String cartypeId) {
        this.cartypeId = cartypeId == null ? null : cartypeId.trim();
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}