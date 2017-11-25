package parkingos.com.bolink.beans;

public class ParkTokenTb {
    private Long id;

    private String parkId;

    private String token;

    private Long loginTime;

    private Long beatTime;

    @Override
    public String toString() {
        return "ParkTokenTb{" +
                "id=" + id +
                ", parkId='" + parkId + '\'' +
                ", token='" + token + '\'' +
                ", loginTime=" + loginTime +
                ", beatTime=" + beatTime +
                ", serverIp='" + serverIp + '\'' +
                ", sourceIp='" + sourceIp + '\'' +
                ", localId='" + localId + '\'' +
                '}';
    }

    private String serverIp;

    private String sourceIp;

    private String localId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParkId() {
        return parkId;
    }

    public void setParkId(String parkId) {
        this.parkId = parkId == null ? null : parkId.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    public Long getBeatTime() {
        return beatTime;
    }

    public void setBeatTime(Long beatTime) {
        this.beatTime = beatTime;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp == null ? null : serverIp.trim();
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp == null ? null : sourceIp.trim();
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId == null ? null : localId.trim();
    }
}