/**
 * 
 */
package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Boolean add 是否加法运算 int type,运算类型 Double score 运算数据 1手机收费，2打赏，3积分，4入场，5离场
 * 
 * @author Administrator 2015年7月31日
 */
@SuppressWarnings("serial")
public class MainUiInfo implements Serializable {
	// boolean add,是否加法运算
	// int type,运算类型
	// Double score 运算数据
	public Boolean add;
	public int type;
	public Double score;

	public Boolean getAdd() {
		return add;
	}

	public void setAdd(Boolean add) {
		this.add = add;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public MainUiInfo() {
		super();
	}

	/**
	 * 
	 * @param boolean add 是否加法运算
	 * @param int type 运算类型 1手机收费，2打赏，3积分，4入场，5离场
	 * @param double score 运算数据
	 */
	public MainUiInfo(Boolean add, int type, Double score) {
		super();
		this.add = add;
		this.type = type;
		this.score = score;
	}

	@Override
	public String toString() {
		return "MainUiInfo [add=" + add + ", type=" + type + ", score=" + score + "]";
	}

}
