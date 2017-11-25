package parkingos.com.bolink.beans;

public class SyncInfoPoolTb {
    private Long id;

    private Long comid;

    private String tableName;

    private Long tableId;

    @Override
    public String toString() {
        return "SyncInfoPoolTb{" +
                "id=" + id +
                ", comid=" + comid +
                ", tableName='" + tableName + '\'' +
                ", tableId=" + tableId +
                ", createTime=" + createTime +
                ", operate=" + operate +
                ", state=" + state +
                '}';
    }

    private Long createTime;

    private Integer operate;

    private Integer state;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComid() {
        return comid;
    }

    public void setComid(Long comid) {
        this.comid = comid;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName == null ? null : tableName.trim();
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getOperate() {
        return operate;
    }

    public void setOperate(Integer operate) {
        this.operate = operate;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}