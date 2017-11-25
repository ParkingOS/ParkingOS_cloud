package parkingos.com.bolink.dto;

public class UnionInfo {

    private Long unionId;
    private String unionKey;

    @Override
    public String toString() {
        return "UnionInfo{" +
                "unionId=" + unionId +
                ", unionKey='" + unionKey + '\'' +
                '}';
    }


    public Long getUnionId() {
        return unionId;
    }

    public void setUnionId(Long unionId) {
        this.unionId = unionId;
    }

    public String getUnionKey() {
        return unionKey;
    }

    public void setUnionKey(String unionKey) {
        this.unionKey = unionKey;
    }
}
