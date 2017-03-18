/**
 * 
 */
package com.zhenlaidian.bean;

import java.util.ArrayList;

/**
 * @author zhangyunfei 2015年9月19日 车主支付记录
 */
public class OwnerPayLogsInfo {
	// {"count":2,"carinfo":该车主有2个车牌:/n京GTN019,京QLL122,"info":[{"id":"29504","uin":"20551","amount":"2.00",
	// "type":"0","create_time":"1442487418","remark":"停车费_京QLL122","target":"4","orderid":"795868","carnumber":"京QLL122"},{}]}

	// id 流水明细编号；
	// uin 车主账号；
	// remark 标题；
	// type 都是0；不用管；
	// target 不用；
	// amount 支付的金额；

	public String count;
	public String carinfo;
	public ArrayList<OwnerPayLogLists> info;

	public OwnerPayLogsInfo() {
		super();
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getCarinfo() {
		return carinfo;
	}

	public void setCarinfo(String carinfo) {
		this.carinfo = carinfo;
	}

	public ArrayList<OwnerPayLogLists> getInfo() {
		return info;
	}

	public void setInfo(ArrayList<OwnerPayLogLists> info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "OwnerPayLogsInfo [count=" + count + ", carinfo=" + carinfo + ", info=" + info + "]";
	}

	public class OwnerPayLogLists {

		public String id;
		public String uin;
		public String type;
		public String amount;
		public String orderid;
		public String create_time;
		public String remark;
		public String target;
		public String carnumber;

		public OwnerPayLogLists() {
			super();
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getUin() {
			return uin;
		}

		public void setUin(String uin) {
			this.uin = uin;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getAmount() {
			return amount;
		}

		public void setAmount(String amount) {
			this.amount = amount;
		}

		public String getOrderid() {
			return orderid;
		}

		public void setOrderid(String orderid) {
			this.orderid = orderid;
		}

		public String getCreate_time() {
			return create_time;
		}

		public void setCreate_time(String create_time) {
			this.create_time = create_time;
		}

		public String getRemark() {
			return remark;
		}

		public void setRemark(String remark) {
			this.remark = remark;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public String getCarnumber() {
			return carnumber;
		}

		public void setCarnumber(String carnumber) {
			this.carnumber = carnumber;
		}

		@Override
		public String toString() {
			return "OwnerPayLogLists [id=" + id + ", uin=" + uin + ", type=" + type + ", amount=" + amount + ", orderid="
					+ orderid + ", create_time=" + create_time + ", remark=" + remark + ", target=" + target + ", carnumber="
					+ carnumber + "]";
		}

	}

}
