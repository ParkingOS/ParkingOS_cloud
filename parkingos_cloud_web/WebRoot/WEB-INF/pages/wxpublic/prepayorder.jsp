<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta content="black" name="apple-mobile-web-app-status-bar-style">
<meta content="telephone=no" name="format-detection">
<meta content="email=no" name="format-detection">
<title>确认缴费</title>
<script type="text/javascript">
	javascript: window.history.forward(1);
</script>
<script src="js/jquery.js" type="text/javascript"></script>
<link rel="stylesheet" href="css/prepay.css?v=1">
<style type="text/css">
.ticket{
	border-radius:8px;
	margin-left:5px;
	background-color:#00A55D;
	color:white;
	padding-left:2px;
	padding-right:2px;
	padding-top:1px;
	padding-bottom:1px;
	font-size:12px;
}

.error {
	color: red;
	font-size: 15px;
	margin-top:5%;
}

.noorder{
	text-align:center;
	color:red;
	margin-top:55%;
}

.errororder{
	text-align:center;
	color:red;
	margin-top:55%;
}
.wx_pay{
	border-radius:5px;
	width:96%;
	margin-left:2%;
	height:40px;
	margin-top:5%;
	font-size:15px;
	background-color:#04BE02;
	color:white;
}

.ticket {
	text-align: center;
	padding-top: 2px;
	padding-bottom: 1px;
	border-radius: 3px;
	background-color: #04BE02;
	outline: medium;
	color: white;
	padding-left: 3px;
	padding-right: 3px;
	font-size: 11px;
}
.hide{display:none;}
</style>
</head>
<body>
	<section class="main">
		<form method="post" action="wxpublic.do?action=balancepayinfo" role="form" id="payform" class="confirm">
			<fieldset>
			<div class="info-area">	
				<dl class="totle">
					<dt class="totle-title">&nbsp;实付停车费</dt>
					<dd class="totle-num" style="color:#04BE02;">￥${otherpay}</dd>
					<dd class="totle-num othermoney hide" style="text-decoration:line-through;font-size:20px;padding-top:10px;">￥${money}</dd>
				</dl>
				<ul class="info-list hide">
					<li class="list wxdiscount hide"><span class="list-title">微信打折券</span><span class="ticket">${ticketdescp}</span><span class="list-content">抵扣${tcbtlimit}元</span></li>
					<li class="list tcbdiscount hide"><span class="list-title">停车券</span><span class="ticket">${ticketdescp}</span><span class="list-content">抵扣${tcbtlimit}元</span></li>
					<li class="list balancepay hide"><span class="list-title">余额支付</span><span class="list-content">${balancepay}元</span></li>
					<li class="list onlinepay hide"><span class="list-title">在线支付</span><span class="list-content">${wx_pay}元</span></li>
				</ul>
			</div>
			<ul class="hide">
					<input type="text" name="money" value="${money}">
					<input type="text" name="uid" value="${uid}">
					<input type="text" name="ticketid" value="${ticketid}">
					<input type="text" name="mobile" value="${mobile}">
					<input type="text" name="openid" value="${openid}">
					<input type="text" name="notice_type" value="${notice_type}">
					<input type="text" name="leaving_time" value="${delaytime}">
					<input type="text" name="paytype" value="${paytype}">
			</ul>
			<input type="button" id="wx_pay" onclick='checkorder();' class="wx_pay" value="支付">
			<div class="tips"></div>
			</fieldset>
		</form>
		<div style="text-align:center;" id="error" class="error"></div>
		<div class="wxpay-logo"></div>
	</section>
	<section class="noorder hide">
		<div>当前订单已结算</div>
	</section>
</body>
<script type="text/javascript">
	//**************订单初始化**************//
	var showorder = "${showorder}";
	if(showorder == "1"){
		$(".main").addClass("hide");
		$(".noorder").removeClass("hide");
	}
	
	var tcbtlimit = "${tcbtlimit}";
	var tickettype = "${tickettype}";
	var paytype = "${paytype}";
	var wx_pay = "${wx_pay}";
	var balancepay = "${balancepay}";
	wx_pay = parseFloat(wx_pay);
	balancepay = parseFloat(balancepay);
	tcbtlimit = parseFloat(tcbtlimit);
	if(tcbtlimit > 0){
		$(".info-list").removeClass("hide");
		$(".othermoney").removeClass("hide");
		if(tickettype == "2"){
			$(".wxdiscount").removeClass("hide");
		}else{
			$(".tcbdiscount").removeClass("hide");
		}
	}
	if(balancepay > 0){
		$(".info-list").removeClass("hide");
		$(".balancepay").removeClass("hide");
	}
	if(wx_pay > 0){
		$(".info-list").removeClass("hide");
		$(".onlinepay").removeClass("hide");
	}
	
	function checkorder(){
		if(paytype == "0"){
			check();
		}else if(paytype == "1"){
			pay();
		}
	}
	
	function check(){
		jQuery.ajax({
			type : "post",
			url : "wxpfast.do",
			data : {
				'orderid' : '${orderid}',
				'action' : 'checkorder',
				'r' :Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "-2" || result == "-3"){
					$(".main").addClass("hide");
					$(".noorder").removeClass("hide");
				}else{
					pay();
				}
			}
		});
	}
	
	function pay(){
		if(wx_pay > 0){
			callpay();
		}else{
			if(paytype == "1"){
				balanpay();
			}else{
				balanprepay();
			}
		}
	}
	
	function balanpay(){
		jQuery.ajax({
			type : "post",
			url : "carowner.do",
			data : {
				'mobile' : '${mobile}',
				'orderid' : '${orderid}',
				'action' : 'payorder',
				'total' : '${money}',
				'ptype' : '2',
				'ticketid' : '${ticketid}',
				'r' :Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "1"){
					$("#payform")[0].submit();
				}else{
					document.getElementById("error").innerHTML = "支付失败";
				}
			}
		});
	}
	
	function balanprepay(){
		jQuery.ajax({
			type : "post",
			url : "wxpfast.do",
			data : {
				'openid' : '${openid}',
				'orderid' : '${orderid}',
				'action' : 'prepayorder',
				'total' : '${money}',
				'ticketid' : '${ticketid}',
				'r' :Math.random()
			},
			async : false,
			success : function(result) {
				if(result == "1"){
					$("#payform")[0].submit();
				}else{
					document.getElementById("error").innerHTML = "支付失败";
				}
			}
		});
	}
	
	function callpay(){//调起微信支付
		 WeixinJSBridge.invoke('getBrandWCPayRequest',{  
             "appId" : '${appid}',                  //公众号名称，由商户传入  
             "timeStamp":'${timestamp}',          //时间戳，自 1970 年以来的秒数  
             "nonceStr" : '${nonceStr}',         //随机串  
             "package" : '${packagevalue}',      //<span style="font-family:微软雅黑;">商品包信息</span>  
             "signType" : '${signType}',        //微信签名方式:  
             "paySign" : '${paySign}'           //微信签名  
             },function(res){
            	 if(res.err_msg == "get_brand_wcpay_request:ok"){
//            		 alert("开始跳转成功页面。。。");
            		 window.location.href = "wxpublic.do?action=balancepayinfo&openid=${openid}&money=${money}&notice_type=${notice_type}&leaving_time=${delaytime}&paytype=${paytype}&orderid=${orderid}";
            	 }
         });
	}
</script>
<script type="text/javascript">
			//每次添加一个class
			function addClass(currNode, newClass){
		        var oldClass;
		        oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
		        if(oldClass !== null) {
				   newClass = oldClass+" "+newClass; 
				}
				currNode.className = newClass; //IE 和FF都支持
    		}
			
			//每次移除一个class
			function removeClass(currNode, curClass){
				var oldClass,newClass1 = "";
		        oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
		        if(oldClass !== null) {
				   oldClass = oldClass.split(" ");
				   for(var i=0;i<oldClass.length;i++){
					   if(oldClass[i] != curClass){
						   if(newClass1 == ""){
							   newClass1 += oldClass[i]
						   }else{
							   newClass1 += " " + oldClass[i];
						   }
					   }
				   }
				}
				currNode.className = newClass1; //IE 和FF都支持
			}
			
			//检测是否包含当前class
			function hasClass(currNode, curClass){
				var oldClass;
				oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
				if(oldClass !== null){
					oldClass = oldClass.split(" ");
					for(var i=0;i<oldClass.length;i++){
					   if(oldClass[i] == curClass){
						   return true;
					   }
				   }
				}
				return false;
			}
</script>
</html>
