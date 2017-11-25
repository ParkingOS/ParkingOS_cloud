<%@ page language="java" contentType="text/html; charset=gb2312"
	pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport"
	content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<title>测试工具</title>
<script src="js/jquery.js"></script>
<style>
body {
  font-family: "Microsoft Yahei", Arial, sans-serif;
  font-size: 14px;
  background: #fff;
  overflow-x:hidden;
}
.title{
	font-size: 15px;
	margin-bottom:5px;
	color:red;
}
.content{e
	margin-bottom:10px;
}
.textarea{
	background-color: #FFFCEC;
}
.module{
	border: 1px solid #DDDDDD; padding:5px; margin-bottom:10px;
}
.button {
	display: inline-block;
	position: relative;
	margin: 0px;
	padding: 0 20px;
	text-align: center;
	text-decoration: none;
	font: bold 12px/25px Arial, sans-serif;

	text-shadow: 1px 1px 1px rgba(255,255,255, .22);

	-webkit-border-radius: 30px;
	-moz-border-radius: 30px;
	border-radius: 30px;

	-webkit-box-shadow: 1px 1px 1px rgba(0,0,0, .29), inset 1px 1px 1px rgba(255,255,255, .44);
	-moz-box-shadow: 1px 1px 1px rgba(0,0,0, .29), inset 1px 1px 1px rgba(255,255,255, .44);
	box-shadow: 1px 1px 1px rgba(0,0,0, .29), inset 1px 1px 1px rgba(255,255,255, .44);

	-webkit-transition: all 0.15s ease;
	-moz-transition: all 0.15s ease;
	-o-transition: all 0.15s ease;
	-ms-transition: all 0.15s ease;
	transition: all 0.15s ease;
}
.green {
	color: #3e5706;
	background: #a5cd4e;
}
</style>
</head>
<body  style="text-align:center;">
<div>
	<div class="module">
	<div class="title">
			<span>手机号必填</span>
		</div>
		<div>
			<span>手机号：</span><input type="text" value="" maxlength="11" id="mobile" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>清除缓存</span>
		</div>
		<div>
			<span style="margin-left:10px;">
				<select id="cache">
					<option value="0">用券缓存</option>
					<option value="1">红包缓存</option>
					<option value="2">打赏缓存</option>
					<option value="3">补贴缓存</option>
					<option value="4">按车场出单补贴缓存</option>
				</select>
			</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="clearcache()" value="清除" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>今日补贴总额</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="allowance()" value="查看" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>查看每日车场停车券补贴额(缓存)</span>
		</div>
		<div style="margin-top:10px;">
			<span>车场编号：</span><input type="text" value="" id="parkid" />
			<span style="margin-left:10px;"></span>
		</div>
		<div style="margin-top:10px;">
			<span style="margin-left:10px;">
				查询内容：<select id="ticketcache">
					<option value="0">今日该车场停车券补贴额</option>
					<option value="1">今日该车场停车券补贴上限</option>
				</select>
			</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="viewticketbypark()" value="查看" />
		</div>
		
		<div class="title" style="magrin-top:10px;">
			<span>设置每日车场停车券补贴额(缓存)</span>
		</div>
		<div style="margin-top:10px;">
			<span>补贴上限：</span><input type="text" value="" id="parklimit" />
			<span style="margin-left:10px;"></span>
		</div>
		<div style="margin-top:10px;">
			<span style="margin-left:10px;">
				操作内容：<select id="ticketlimit">
					<option value="0">清除该车场今日补贴额</option>
					<option value="1">设置今日该车场停车券补贴上限</option>
				</select>
			</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="setticketbypark()" value="设置" />
		</div>
		
		<div class="title">
			<span>查看所有车场补贴上限</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="viewallparklimit()" value="查看" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>查看是否在黑名单</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="black()" value="查看" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>从黑名单中漂白</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="towhite()" value="漂白" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>查看是否认证用户</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="isauth()" value="查看" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>设置认证用户</span>
		</div>
		<div>
			<span style="margin-left:10px;">
				<select id="authcache">
					<option value="0">设置为非认证用户</option>
					<option value="1">设置为认证用户</option>
				</select>
			</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="setauth()" value="设置" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>清除首单</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="clearfirst()" value="清除" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>清除今日打赏记录</span>
		</div>
		<div style="margin-top:10px;">
			<span>收费员编号：</span><input type="text" value="" id="parkerid" />
			<span style="margin-left:10px;"></span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="clearreward()" value="清除" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>加停车券</span>
		</div>
		<div>
			<span>券金额:</span><input type="text" value="" id="begin" />--<input type="text" value="" id="end" />
			<select id="tickettype">
					<option value="0">普通券</option>
					<option value="1">专用券</option>
					<option value="2">购买券</option>
				</select>
		</div>
		<div style="margin-top:10px;">
			<span>车场编号：</span><input type="text" value="" id="comid" />
			<span style="margin-left:10px;"></span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="addticket()" value="添加" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>清除停车券</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="clearticket()" value="清除" />
		</div>
	</div>
	<div class="module">
	<div class="title">
			<span>加余额</span>
		</div>
		<div>
			<span>金额：</span><input type="text" value="" id="balance" />
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="addbalance()" value="确定" />
		</div>
	</div>
	
	<div class="module">
	<div class="title">
			<span>加积分</span>
		</div>
		<div>
			<span>积分：</span><input type="text" value="" id="score" />
		</div>
		<div style="margin-top:10px;">
			<span>收费员编号：</span><input type="text" value="" id="uid" />
			<span style="margin-left:10px;"></span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="addscore()" value="确定" />
		</div>
	</div>
	<div class="module">
		<div class="title">
			<span>解除和微信公众号的绑定关系</span>
		</div>
		<div style="margin-top:10px;text-align:center;">
			<input type="button" onclick="disbind()" value="解除" />
		</div>
	</div>
</div>
</body>
<script type="text/javascript">
	var checkmobile = function(){
		var mobile = document.getElementById("mobile").value;
		var m = /^[1][3,4,5,7,8][0-9]{9}$/; 
		if(mobile == ""){
			alert("请输入手机号码");
			return false;
		}
		if(mobile.length!=11||!mobile.match(m)){
			alert("手机号码不正确");
			return false;
		}
		return true;
	};

	var clearcache = function(){
		if(!checkmobile()){
			return false;
		}
		var type = document.getElementById("cache").value;
		var mobile = document.getElementById("mobile").value;
		cache(mobile,type);
	};

	function cache(mobile, type){//
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'clearcache',
				'mobile' : mobile,
				'type' : type,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "1"){
					alert("清除成功");
				}else if(result == "-5"){
					alert("没有缓存");
				}else if(result == "-4"){
					alert("线上不能清除补贴额度");
				}
			}
		});
	}
	
	var setauth = function(){
		if(!checkmobile()){
			return false;
		}
		var isauth = document.getElementById("authcache").value;
		var mobile = document.getElementById("mobile").value;
		setuserauth(mobile,isauth);
	};

	function setuserauth(mobile, isauth){//
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'setauth',
				'mobile' : mobile,
				'isauth' : isauth,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "1"){
					alert("设置成功");
				}
			}
		});
	}
	
	var allowance = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		viewallowance(mobile);
	};
	function viewallowance(mobile){//
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'allowance',
				'mobile' : mobile,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "-4"){
					alert("今日尚无缓存");
				}else{
					alert("今日补贴额度:"+result);
				}
			}
		});
	}
	
	var black = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		viewblack(mobile);
	};
	
	function viewblack(mobile){//
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'viewblack',
				'mobile' : mobile,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "-4"){
					alert("该手机号不在黑名单内");
				}else if(result == "1"){
					alert("该手机号在黑名单内");
				}
			}
		});
	}
	
	var setticketbypark = function(){
		if(!checkmobile()){
			return false;
		}
		var parkid = document.getElementById("parkid").value;
		var re = /^[1-9]+[0-9]*]*$/;
		if(parkid == ""){
			alert("请输入车场编号");
			return false;
		}
		if(!parkid.match(re)){
			alert("车场编号不正确");
			return false;
		}
		var ticketlimit = document.getElementById("ticketlimit").value;
		var mobile = document.getElementById("mobile").value;
		var parklimit = document.getElementById("parklimit").value;
		if(parklimit != "" && !parklimit.match(re)){
			alert("补贴上限不正确");
			return false;
		}
		setbypark(mobile,parkid,parklimit,ticketlimit);
	};
	
	function setbypark(mobile,parkid,parklimit,type){
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'settbypark',
				'mobile' : mobile,
				'type' : type,
				'comid' : parkid,
				'parklimit' : parklimit,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "-4"){
					alert("车场编号不正确");
				}else if(result == "-5"){
					alert("设置失败，没有按车场分配补贴的缓存，此时按照总补贴金额来限制补贴");
				}else if(result == "-6"){
					alert("设置失败，线上数据不可操作");
				}else if(result == "1"){
					alert("设置成功");
				}
			}
		});
	}
	
	var viewallparklimit = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		viewalllimit(mobile);
	};
	
	function viewalllimit(mobile){
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'viewallparklimit',
				'mobile' : mobile,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "-4"){
					alert("无缓存");
				}else{
					alert(result);
				}
			}
		});
	}
	
	var viewticketbypark = function(){
		if(!checkmobile()){
			return false;
		}
		var parkid = document.getElementById("parkid").value;
		var re = /^[1-9]+[0-9]*]*$/;
		if(parkid == ""){
			alert("请输入车场编号");
			return false;
		}
		if(!parkid.match(re)){
			alert("车场编号不正确");
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		var ticketcache = document.getElementById("ticketcache").value;
		viewbypark(mobile,ticketcache,parkid);
	};
	
	function viewbypark(mobile,type,parkid){
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'viewtbypark',
				'mobile' : mobile,
				'type' : type,
				'comid' : parkid,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "-4"){
					alert("车场编号不正确");
				}else if(result == "-5"){
					alert("没有按车场分配补贴的缓存，此时按照总补贴金额来限制补贴");
				}else{
					if(type == "0"){
						alert("该车场今日停车券补贴额度："+result);
					}else if(type == "1"){
						alert("该车场今日停车券补贴上限："+result);
					}
				}
			}
		});
	}
	
	var towhite = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		whiteblack(mobile);
	};
	
	function whiteblack(mobile){//
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'towhite',
				'mobile' : mobile,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "-4"){
					alert("该手机号不在黑名单内");
				}else if(result == "-5"){
					alert("漂白失败");
				}else if(result == "1"){
					alert("漂白成功");
				}
			}
		});
	}
	
	var isauth = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		checkauth(mobile);
	};
	
	function checkauth(mobile){//
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'checkauth',
				'mobile' : mobile,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "-4"){
					alert("非认证用户");
				}else if(result == "1"){
					alert("认证用户");
				}
			}
		});
	}
	
	var clearfirst = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		first(mobile);
	};
	
	function first(mobile){//
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'clearfirst',
				'mobile' : mobile,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "1"){
					alert("清除成功");
				}
			}
		});
	}
	
	var addticket = function(){
		if(!checkmobile()){
			return false;
		}
		var begin = document.getElementById("begin").value;
		var tickettype = document.getElementById("tickettype").value;
		var end = document.getElementById("end").value;
		var comid= document.getElementById("comid").value;
		var mobile = document.getElementById("mobile").value;
		var re = /^[1-9]+[0-9]*]*$/;
		if(begin == ""){
			alert("请输入停车券金额");
			return false;
		}
		if(!begin.match(re)){
			alert("金额不正确");
			return false;
		}
		if(end != "" && !end.match(re)){
			alert("金额不正确");
			return false;
		}
		if(end != ""){
			if(parseInt(end) < parseInt(begin)){
				alert("金额不正确");
				return false;
			}
		}
		if(tickettype == "1"){
			if(comid == ""){
				alert("请输入车场编号");
				return false;
			}
			
			if(!comid.match(re)){
				alert("车场编号不正确");
				return false;
			}
		}
		ticket(mobile,begin,end,tickettype,comid);
	};
	
	function ticket(mobile,begin,end,type,comid){//
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'addticket',
				'mobile' : mobile,
				'begin' : begin,
				'end' : end,
				'type' : type,
				'comid' : comid,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "-4"){
					alert("请输入停车券金额");
				}else if(result == "-5"){
					alert("请输入车场编号");
				}else if(result == "-6"){
					alert("添加失败");
				}else if(result == "1"){
					alert("添加成功");
				}
			}
		});
	}
	
	var clearreward = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		var parkerid = document.getElementById("parkerid").value;
		reward(mobile,parkerid);
	};
	
	function reward(mobile,uid){//
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'clearreward',
				'mobile' : mobile,
				'uid' : uid,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "-4"){
					alert("请输入收费员帐号");
				}else if(result == "1"){
					alert("清除成功");
				}
			}
		});
	}
	
	var clearticket = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		cticket(mobile);
	};
	
	function cticket(mobile){//
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'clearticket',
				'mobile' : mobile,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "1"){
					alert("清除成功");
				}
			}
		});
	}
	
	var addbalance = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		var balance = document.getElementById("balance").value;
		if(balance == ""){
			alert("请输入金额");
			return false;
		}
		var re = /^[1-9]+[0-9]*]*$/;
		if(balance != "0" && !balance.match(re)){
			alert("金额不正确");
			return false;
		}
		
		addmoney(mobile, balance);
	};
	
	function addmoney(mobile, balance){
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'addbalance',
				'mobile' : mobile,
				'balance' : balance,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "1"){
					alert("设置成功");
				}
			}
		});
	}
	
	var addscore = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		var score = document.getElementById("score").value;
		var uid = document.getElementById("uid").value;
		if(score == ""){
			alert("请输入积分");
			return false;
		}
		if(uid == ""){
			alert("请输入收费员帐号");
			return false;
		}
		var re = /^[1-9]+[0-9]*]*$/;
		if(score != "0" && !score.match(re)){
			alert("积分不正确");
			return false;
		}
		if(!uid.match(re)){
			alert("收费员帐号不正确");
			return false;
		}
		
		setscore(mobile, score, uid);
	};
	
	function setscore(mobile, score, uid){
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'addscore',
				'mobile' : mobile,
				'uid' : uid,
				'score' : score,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "1"){
					alert("设置成功");
				}
			}
		});
	}
	
	var disbind = function(){
		if(!checkmobile()){
			return false;
		}
		var mobile = document.getElementById("mobile").value;
		clearbind(mobile);
	};
	
	function clearbind(mobile){
		jQuery.ajax({
			type : "post",
			url : "testutil.do",
			data : {
				'action' : 'disbind',
				'mobile' : mobile,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-1"){
					alert("请输入手机号");
				}else if(result == "-2"){
					alert("该手机号不在测试名单内");
				}else if(result == "-3"){
					alert("该手机号没有注册");
				}else if(result == "1"){
					alert("解绑成功");
				}
			}
		});
	}
</script>
</html>
