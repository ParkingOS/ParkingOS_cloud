package parkingos.com.bolink.beans;

public class ComBrakeTb {
    private Long id;

    private String brakeName;

    private String serial;

    private String ip;

    private Long passid;

    private Integer state;

    private Long uploadTime;

    private Long comid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrakeName() {
        return brakeName;
    }

    public void setBrakeName(String brakeName) {
        this.brakeName = brakeName == null ? null : brakeName.trim();
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial == null ? null : serial.trim();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public Long getPassid() {
        return passid;
    }

    public void setPassid(Long passid) {
        this.passid = passid;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Long getComid() {
        return comid;
    }

    public void setComid(Long comid) {
        this.comid = comid;
    }
}