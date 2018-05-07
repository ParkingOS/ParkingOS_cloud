package parkingos.com.bolink.vo;

public class LockCarView {

    private Integer state;
    private String lockKey;
    private String openid;
    private Long oid;
    private Integer lockStatus;

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public void setLockStatus(Integer lockStatus) {
        this.lockStatus = lockStatus;
    }

    @Override
    public String toString() {
        return "LockCarView{" +
                "state=" + state +
                ", lockKey='" + lockKey + '\'' +
                ", openid='" + openid + '\'' +
                ", oid=" + oid +
                ", lockStatus=" + lockStatus +
                '}';
    }

    public String getOpenid() {
        return openid;
    }

    public Long getOid() {
        return oid;
    }

    public Integer getLockStatus() {
        return lockStatus;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }
}
