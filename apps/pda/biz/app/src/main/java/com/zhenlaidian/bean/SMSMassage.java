package com.zhenlaidian.bean;

public class SMSMassage {

//	mesg:-1 收费员不存在且注册失败，1成功返回，-2：生成验证码失败
//	code:验证码，在短信内容中写在开头部分，前面不要有其它字符
//	tomobile:发送短信目的地
	public String mesg;
	public String code;
	public String tomobile;
	public String getMesg() {
		return mesg;
	}
	public void setMesg(String mesg) {
		this.mesg = mesg;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getTomobile() {
		return tomobile;
	}
	public void setTomobile(String tomobile) {
		this.tomobile = tomobile;
	}
	@Override
	public String toString() {
		return "SMSMassage [mesg=" + mesg + ", code=" + code + ", tomobile="
				+ tomobile + "]";
	}
	
	
}
