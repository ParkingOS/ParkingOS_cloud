package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by xulu on 2016/8/30.
 */
public class Card implements Serializable{
    /**
     * "card":{
     "balance":80,
     "card_name":"啊卡",
     "card_number":"001",
     "comid":-1,
     "create_time":1472438428,
     "device":"97261630180230",
     "group_id":26,
     "id":64425,
     "is_delete":0,
     "nfc_uuid":"041c44f2a44881",
     "nid":0,
     "qrcode":"",
     "state":0,
     "tenant_id":-1,
     "type":2,
     "uid":-1,
     "uin":-1,
     "update_time":0,
     "use_times":
     2016.10.10增加收费公司
     group_name
     }
     */
    String balance;
    String card_name;
    String card_number;
    String comid;
    String create_time;
    String device;
    String group_id;
    String id;
    String is_delete;
    String nfc_uuid;
    String nid;
    String qrcode;
    String tenant_id;
    String type;
    String uid;
    String uin;
    String use_times;
    String update_time;
    String state;

    String group_name;

    @Override
    public String toString() {
        return "Card{" +
                "balance='" + balance + '\'' +
                ", card_name='" + card_name + '\'' +
                ", card_number='" + card_number + '\'' +
                ", comid='" + comid + '\'' +
                ", create_time='" + create_time + '\'' +
                ", device='" + device + '\'' +
                ", group_id='" + group_id + '\'' +
                ", id='" + id + '\'' +
                ", is_delete='" + is_delete + '\'' +
                ", nfc_uuid='" + nfc_uuid + '\'' +
                ", nid='" + nid + '\'' +
                ", qrcode='" + qrcode + '\'' +
                ", tenant_id='" + tenant_id + '\'' +
                ", type='" + type + '\'' +
                ", uid='" + uid + '\'' +
                ", uin='" + uin + '\'' +
                ", use_times='" + use_times + '\'' +
                ", update_time='" + update_time + '\'' +
                ", state='" + state + '\'' +
                ", group_name='" + group_name + '\'' +
                '}';
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCard_name() {
        return card_name;
    }

    public void setCard_name(String card_name) {
        this.card_name = card_name;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getComid() {
        return comid;
    }

    public void setComid(String comid) {
        this.comid = comid;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIs_delete() {
        return is_delete;
    }

    public void setIs_delete(String is_delete) {
        this.is_delete = is_delete;
    }

    public String getNfc_uuid() {
        return nfc_uuid;
    }

    public void setNfc_uuid(String nfc_uuid) {
        this.nfc_uuid = nfc_uuid;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getTenant_id() {
        return tenant_id;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public String getUse_times() {
        return use_times;
    }

    public void setUse_times(String use_times) {
        this.use_times = use_times;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
