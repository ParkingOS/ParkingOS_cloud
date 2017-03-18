/**
 * 
 */
package com.zhenlaidian.bean;

import java.util.ArrayList;

/**
 * 57：收费员发停车券时选车主：按照本周打赏次数倒叙排列
 * collectorrequest.do?action=rewardlist&token=&page=&size= 返回值类型：JSON数组
 * uin:车主ID rcount：本周打赏次数 rmoney：本周打赏金额 carnumber：车主车牌号（为null时表示没有车牌号未知）
 * 
 * 58：收费员发停车券时选车主：按照本周停车次数倒叙排列
 * collectorrequest.do?action=parkinglist&token=&page=&size= 返回参数类型：JSON数组
 * uin:车主ID pcount：本周停车次数 carnumber：车主车牌号（为null时表示没有车牌号未知）
 * 
 * @author Administrator 2015年7月17日
 */
public class SelectUserInfo {

	public int count;
	public ArrayList<SelectUser> info;

	public SelectUserInfo() {
		super();
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public ArrayList<SelectUser> getInfo() {
		return info;
	}

	public void setInfo(ArrayList<SelectUser> info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "SelectUserInfo [count=" + count + ", info=" + info + "]";
	}

	public class SelectUser {
		private String uin;// 车主ID
		private String rcount;// 本周打赏次数
		private String rmoney;// 本周打赏金额
		private Boolean select;// 是否被选中
		private String pcount;// 本周停车次数
		private String carnumber;// 车主车牌号

		public SelectUser() {
			super();
		}

		public String getUin() {
			return uin;
		}

		public void setUin(String uin) {
			this.uin = uin;
		}

		public String getRcount() {
			return rcount;
		}

		public void setRcount(String rcount) {
			this.rcount = rcount;
		}

		public String getRmoney() {
			return rmoney;
		}

		public void setRmoney(String rmoney) {
			this.rmoney = rmoney;
		}

		public String getPcount() {
			return pcount;
		}

		public void setPcount(String pcount) {
			this.pcount = pcount;
		}

		public String getCarnumber() {
			return carnumber;
		}

		public void setCarnumber(String carnumber) {
			this.carnumber = carnumber;
		}

		public Boolean getSelect() {
			return select;
		}

		public void setSelect(Boolean select) {
			this.select = select;
		}

		@Override
		public String toString() {
			return "SelectUser [uin=" + uin + ", rcount=" + rcount + ", rmoney=" + rmoney + ", select=" + select + ", pcount="
					+ pcount + ", carnumber=" + carnumber + "]";
		}

	}

}
