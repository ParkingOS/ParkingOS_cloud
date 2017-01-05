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
		<form method="post" action="wxpfast.do?action=beginprepay" role="form" id="prepayform" class="confirm">
			<fieldset>
			<div class="info-area">	
				<dl class="totle" style="border-bottom:0px">
					<dt class="totle-title">预付停车费</dt>
					<dd class="totle-num" style="color:#04BE02;">￥<span id="aftertotal">${aftertotal}</span></dd>
					<ul class="nfc">
						<li class="list1"></li>
						<li class="list2"><a href=""><span>预付完成后，再将卡给收费员结算，享折扣</span></a></li>
					</ul>
					<div class="sweepcom hide" style="border-bottom: 1px solid #E0E0E0;"></div>
				</dl>
				
				<ul class="info-list" style="padding-top:1px;">
					<li class="list"><span class="list-title">已预付金额</span><span class="list-content">${pretotal}元</span></li>
					<li class="list"><span class="list-title">入场时间</span><span class="list-content">${starttime}</span></li>
					<li class="list"><span class="list-title">已停时长</span><span class="list-content" id="parktime">${parktime}</span></li>
					<li class="list freetime hide"><span class="list-title">减免时长</span><span class="list-content">${descp}</span></li>
					<li class="list"><span class="list-title carnumber hide">车牌号码</span><span class="list-content">${carnumber}</span></li>
				</ul>
				
				<ul class="info-list hide">
					<li class="list"><input name="openid" value="${openid}" /></li>
					<li class="list"><input name="orderid" value="${orderid}"></li>
				</ul>
			</div>
			<div class="leave" style="text-align:center;margin-top:20px;">
				我离到出口缴费还有：<select name="delaytime"
					style="width:95px;height:25px;padding-top:3px;font-size:17px;padding-left:6px;color:#04BE02;"
					id="delaytime">
					<option value="0">0分钟</option>
					<option value="5">5分钟</option>
					<option value="10">10分钟</option>
					<option selected="selected" value="15">15分钟</option>
					<option value="30">30分钟</option>
					<option value="60">1小时</option>
					<option value="120">2小时</option>
					<option value="240">4小时</option>
					<option value="360">6小时</option>
					<option value="480">8小时</option>
					<option value="600">10小时</option>
					<option value="720">12小时</option>
				</select>
			</div>
			<input type="button" id="wx_pay" onclick='payorder();' class="wx_pay" value="去预付">
			<div class="tips"></div>
			</fieldset>
		</form>
		<div style="text-align:center;" id="error" class="error"></div>
		<div class="wxpay-logo"></div>
	</section>
	<section class="noorder hide">
		<div>当前无订单</div>
	</section>
</body>
<script type="text/javascript">
	function getprice(){
		var delaytime = document.getElementById("delaytime").value;
		jQuery.ajax({
			type : "post",
			url : "wxpfast.do",
			data : {
				'orderid' : '${orderid}',
				'shopticketid' : '${shopticketid}',
				'curtime' : '${curtime}',
				'delaytime' : delaytime,
				'action' : 'getprice'
			},
			async : false,
			success : function(result) {
				if(result == "-1"){//出错了
					document.getElementById("error").innerHTML = "获取停车费失败";
				}
				var jsonData = eval("(" + result + ")");
				var aftertotal = jsonData.aftertotal;//总金额
				var parktime = jsonData.parktime;//已停时长
				document.getElementById("aftertotal").innerHTML = aftertotal;
				document.getElementById("parktime").innerHTML = parktime;
				if(parseFloat(aftertotal) <= 0){//停车费0元
					$(".wx_pay").addClass("hide");
					document.getElementById("error").innerHTML = delaytime+"分钟之内离场免费";
					return false;
				}else{
					$(".wx_pay").removeClass("hide");
					removeClass(document.getElementById("wx_pay"),"wait");
					document.getElementById("error").innerHTML = "";
				}
			}
		});
	}
	
	$("#delaytime").bind("change", function(){
		getprice();
	});
	//***********当返回页面的时候，如果delaytime不是15分钟，重新请求***************//
	var delaytime = document.getElementById("delaytime").value;
	delaytime = parseInt(delaytime);
	document.getElementById("delaytime").value = 15;
	if(delaytime != 15){//页面返回时重新获取15分钟的价格
		getprice();
	}
	//*******************************end*****************************//
	//*****************************订单状态初始化************************//
	var orderid = "${orderid}";
	var carnumber="${carnumber}";
	var aftertotal = "${aftertotal}";
	var prestate = "${prestate}";
	var swpcomflag = "${swpcomflag}";
	var shopticketid = "${shopticketid}";
	
	if(orderid == "-1"){
		$(".main").addClass("hide");
		$(".noorder").removeClass("hide");
	}
	if(prestate == "1"){
		$(".wx_pay").addClass("hide");
		$(".leave").addClass("hide");
		$(".nfc").addClass("hide");
		$(".sweepcom").removeClass("hide");
		document.getElementById("error").innerHTML = "您已预支付过，不能再次预支付";
	}
	
	if(swpcomflag == "1"){
		$(".my-lpn").removeClass("hide");
		$(".nfc").addClass("hide");
		$(".sweepcom").removeClass("hide");
	}
	
	if(parseFloat(aftertotal) <= 0){
		$(".wx_pay").addClass("hide");
		document.getElementById("error").innerHTML = "15分钟之内离场免费";
	}
	
	if(carnumber != ""){
		$(".carnumber").removeClass("hide");
	}
	
	if(shopticketid != "" && shopticketid != "-1"){
		$(".freetime").removeClass("hide");
	}
	
	function payorder(){
		$("#prepayform")[0].submit();
	}
	//*****************************end************************//
</script>
</html>
