package parkingos.com.bolink.vo;

public class LockCarView {

    private Integer state;
    private String lockKey;

    @Override
    public String toString() {
        return "LockCarView{" +
                "state=" + state +
                ", lockKey='" + lockKey + '\'' +
                '}';
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
