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
<title>当前订单</title>
<script src="js/jquery.js" type="text/javascript"></script>
<link rel="stylesheet" href="css/prepay.css?v=2">
<style type="text/css">
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
.unprepay {
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
</style>
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
</head>
<body>
	<!-- 我的车牌[[ -->
		<dl class="my-lpn hide">
			<dt class="title">我的车牌号码</dt>
			<dd class="lpn">${carnumber}<a class="change-btn" href="wxpfast.do?action=toaddcnum&codeid=${codeid}&openid=${openid}">修改</a></dd>
		</dl>
		<!-- 我的车牌]] -->
	<section class="main">
		<form method="post" action="aliprepay.do?action=prepay" role="form" id="prepayform" class="confirm">
			<fieldset>
			<div class="info-area">	
				<dl class="totle" style="border-bottom:0px">
					<dt class="totle-title">预付停车费</dt>
					<dd class="totle-num" style="color:#04BE02;">￥<span id="aftertotal">${total}</span></dd>
					<ul class="nfc">
						<li class="list1"></li>
					</ul>
					<div class="sweepcom hide" style="border-bottom: 1px solid #E0E0E0;"></div>
				</dl>
				
				<ul class="info-list" style="padding-top:1px;">
					<li class="list"><span class="list-title">已预付金额</span><span class="list-content">${total}元</span></li>
					<li class="list"><span class="list-title">入场时间</span><span class="list-content">${starttime}</span></li>
					<li class="list"><span class="list-title">已停时长</span><span class="list-content" id="parktime">${parktime}</span></li>
					<li class="list"><span class="list-title carnumber hide">车牌号码</span><span class="list-content">${carnumber}</span></li>
				</ul>
				
				<ul class="info-list hide">
					<li class="list"><input name="orderid" value="${orderid}"></li>
					<li class="list"><input name="parkid" value="${parkid}"></li>
					<li class="list"><input name="unionid" value="${unionid}"></li>
					<li class="list"><input name="uin" value="${uin}"></li>
				</ul>
			</div>
			<input type="button" id="wx_pay" onclick='payorder();' class="wx_pay" value="去支付">
			<div class="tips"></div>
			</fieldset>
		</form>
		<div style="text-align:center;" id="error" class="error">请在10分钟之内离场</div>
	</section>
	<section class="noorder hide">
		<div>当前无订单</div>
	</section>
</body>
<script type="text/javascript">
	function payorder(){
		$("#prepayform")[0].submit();
	}
	if('${noorder}'==1){
		$(".noorder").removeClass("hide");
		$(".error").addClass("hide");
		$(".main").addClass("hide");
	}
</script>
</html>
