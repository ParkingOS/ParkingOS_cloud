package parkingos.com.bolink.beans;

public class ComPassTb {
    private Long id;

    private Long worksiteId;

    private String passname;

    private String passtype;

    private String description;

    private Long comid;

    private Integer state;

    private Integer monthSet;

    @Override
    public String toString() {
        return "ComPassTb{" +
                "id=" + id +
                ", worksiteId=" + worksiteId +
                ", passname='" + passname + '\'' +
                ", passtype='" + passtype + '\'' +
                ", description='" + description + '\'' +
                ", comid=" + comid +
                ", state=" + state +
                ", monthSet=" + monthSet +
                ", month2Set=" + month2Set +
                ", channelId='" + channelId + '\'' +
                '}';
    }

    private Integer month2Set;

    private String channelId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorksiteId() {
        return worksiteId;
    }

    public void setWorksiteId(Long worksiteId) {
        this.worksiteId = worksiteId;
    }

    public String getPassname() {
        return passname;
    }

    public void setPassname(String passname) {
        this.passname = passname == null ? null : passname.trim();
    }

    public String getPasstype() {
        return passtype;
    }

    public void setPasstype(String passtype) {
        this.passtype = passtype == null ? null : passtype.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Long getComid() {
        return comid;
    }

    public void setComid(Long comid) {
        this.comid = comid;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getMonthSet() {
        return monthSet;
    }

    public void setMonthSet(Integer monthSet) {
        this.monthSet = monthSet;
    }

    public Integer getMonth2Set() {
        return month2Set;
    }

    public void setMonth2Set(Integer month2Set) {
        this.month2Set = month2Set;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId == null ? null : channelId.trim();
    }
}