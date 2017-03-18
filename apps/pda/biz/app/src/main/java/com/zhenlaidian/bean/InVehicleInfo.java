/**
 *
 */
package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * 在场车辆信息
 *
 * @author zhangyunfei 2015年9月9日
 */
public class InVehicleInfo implements Serializable {
    // [{"cid":"A0001","state":"1","car_number":"京AFY123","btime":"1441185409"}]
//
//	public String cid;// 车位位置
//	public String state;// 1在场 0空闲
//	public String car_number;
//	public String btime;
//	public String orderid;
//
//	public String getCid() {
//		return cid;
//	}
//
//	public void setCid(String cid) {
//		this.cid = cid;
//	}
//
//	public String getState() {
//		return state;
//	}
//
//	public void setState(String state) {
//		this.state = state;
//	}
//
//	public String getCar_number() {
//		return car_number;
//	}
//
//	public void setCar_number(String car_number) {
//		this.car_number = car_number;
//	}
//
//	public String getOrderid() {
//		return orderid;
//	}
//
//	public void setOrderid(String orderid) {
//		this.orderid = orderid;
//	}
//
//	public String getBtime() {
//		return btime;
//	}
//
//	public void setBtime(String btime) {
//		this.btime = btime;
//	}
//
//	public InVehicleInfo() {
//		super();
//	}
//
//	@Override
//	public String toString() {
//		return "InVehicleInfo [cid=" + cid + ", state=" + state + ", car_number=" + car_number + ", btime=" + btime
//				+ ", orderid=" + orderid + "]";
//	}
//	2016.4.18 泊位列表接口更改
    /**
     * "id":"364",
     * "ber_name":"1002",
     * "orderid":"830003",
     * "car_number":"苏K45555"
     */

//	private String state;
    private String id;
    private String ber_name;
    private String orderid;
    private String car_number;

    //2016.5.10增加 是否会员，prepay
    private String prepay;
    private String ismonthuser;
    //2016.5.27增加车检器状态，车检器订单编号
    private String sensor_state;
    private String berthorderid;
    //2016.11.18增加 is_card 是否绑定了卡片
    private String is_card;
    @Override
    public String toString() {
        return "InVehicleInfo{" +
                "id='" + id + '\'' +
                ", ber_name='" + ber_name + '\'' +
                ", orderid='" + orderid + '\'' +
                ", car_number='" + car_number + '\'' +
                ", prepay='" + prepay + '\'' +
                ", ismonthuser='" + ismonthuser + '\'' +
                ", sensor_state='" + sensor_state + '\'' +
                ", berthorderid='" + berthorderid + '\'' +
                ", is_card='" + is_card + '\'' +
                '}';
    }

    public String getIs_card() {
        return is_card;
    }

    public void setIs_card(String is_card) {
        this.is_card = is_card;
    }

    public String getPrepay() {
        return prepay;
    }

    public void setPrepay(String prepay) {
        this.prepay = prepay;
    }

    public String getIsmonthuser() {
        return ismonthuser;
    }

    public void setIsmonthuser(String ismonthuser) {
        this.ismonthuser = ismonthuser;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBer_name() {
        return ber_name;
    }

    public void setBer_name(String ber_name) {
        this.ber_name = ber_name;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getCar_number() {
        return car_number;
    }

    public void setCar_number(String car_number) {
        this.car_number = car_number;
    }

    public String getSensor_state() {
        return sensor_state;
    }

    public void setSensor_state(String sensor_state) {
        this.sensor_state = sensor_state;
    }

    public String getBerthorderid() {
        return berthorderid;
    }

    public void setBerthorderid(String berthorderid) {
        this.berthorderid = berthorderid;
    }
}
