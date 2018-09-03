<%@ page language="java" contentType="text/html; charset=gbk"
    pageEncoding="gbk"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<title>我的账户</title>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/jquery.mobile-1.3.2.min.css?v=1" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/list.css?v=12" />
<script src="${pageContext.request.contextPath}/resources/js/jquery.js"></script>
<style type="text/css">
#scroller .li1 {
    padding:0 10px;
    height:50px;
    line-height:50px;
    background-color:#FFFFFF;
    font-size:14px;
    color:#101010;
}

.li2 {
    padding:0 10px;
    height:100px;
    line-height:0px;
    background-color:#FFFFFF;
    font-size:14px;
}

.c1{
	border-top:1px solid #CCCCCC;
}

.c2{
	border-bottom:1px solid #CCCCCC;
}

a{
	text-decoration:none;
	color:#6D6D6D;
	font-size:16px;
	
	position: relative;
	top:-36px;
	left:30px;
}

#header {
    position:absolute; z-index:2;
    top:0; left:0;
    width:100%;
    height:120px;
    line-height:100px;
    background-color:#49B9EE;
    padding:0;
    font-size:20px;
    z-index: -100;
}

.img1{
	width:22px;
	height:22px;
	margin-top:15px;
	margin-left:10px;
}
.img2{
	width:70px;
	height:70px;
	margin-top:15px;
}
</style>

<style type="text/css">
* {
	margin: 0px;
	padding: 0px;
}

body {
	font-size: 12px;
	//font: Arial, Helvetica, sans-serif;
	margin: 25PX 0PX;
	background: #eee;
}

.botton {
	color: #F00;
	cursor: pointer;
}

.mybody {
	width: 600px;
	margin: 0 auto;
	height: 1500px;
	border: 1px solid #ccc;
	padding: 20px 25px;
	background: #fff
}

#cwxBg {
	position: absolute;
	display: none;
	background: #000;
	width: 100%;
	height: 100%;
	left: 0px;
	top: 0px;
	z-index: 1000;
}

#cwxWd {
	position: absolute;
	display: none;
	border: 0px solid #CCC;
	padding: 0px;
	background: #FFF;
	z-index: 1500;
	width: 60%;
	height: 100px;
	top: 25%;
}

#cwxCn {
	background: #FFF;
	display: block;
}

.imgd {
	width: 400px;
	height: 300px;
}

.ticket {
	font-size: 15px;
	height:50px;
	margin-left:25px;
	margin-top:2px;
	line-height:50px;
	border:0px solid white;
}

.line {
	width: 90%;
	height: 1px;
	background: #ADADAD;
	margin-left:5%;
}

.wx_name{
	margin-left:50px;
	margin-top:-5px;
	color:#101010;
	font-size:20px;
}

.wx_img{
	margin-left:50px;
	margin-top:35px;
	color : gray;
}

.credit{
	float:right;
	margin-right:20px;
	margin-top: -70px;
	color:#B3B3B3;
	font-size:12px;
}

.passli {
	background-image: url(${pageContext.request.contextPath}/resources/images/wxpublic/arrow.png);
	background-size: 19px 39px;
	background-repeat: no-repeat;
	background-position: right center;
}

.sel_fee{
	font-size:12px;
	text-align:center;
	padding-top:1px;
	padding-bottom:1px;
	border-radius:5px;
	background-color:#FFFFFF;
	outline:medium;
	margin-left:5px;
}

.nopass{
	border:1px solid #CC3333;
	color:#CC3333;
}

.three{
	padding-left:5px;
	padding-right:5px;
}

.money{
	float:right;
	margin-right:50px;
}

.wx_pay{
	border-radius:5px;
	width:98%;
	margin-left:1%;
	height:40px;
	margin-top:5%;
	font-size:15px;
	background-color:white;
	color:#CC3333;
	border: 1px solid #CC3333;
}
</style>
</head>
<body style="background-color:#EEEEEE;">

	<div id="wrapper" style="margin-top:-25px;">
		<div id="scroller">
			<ul id="thelist">
				<li class="li2"><img class="img2" src="${wximg}" /><a href="#"><div class="wx_name">${wxname}</div></a></li>
				<li class="li1" style="margin-top:20px;"><img class="img1" src="${pageContext.request.contextPath}/resources/images/wxpublic/carnumber1.png" /><a href="tocarnumbers?openid=${openid}&uin=${uin}&appid=${appid}"><div><span style="color:#101010;margin-left: 15px;">我的车辆</span></div></a></li>
				<!--<li class="li1" style="margin-top:20px;"><img class="img1" src="${pageContext.request.contextPath}/resources/images/wxpublic/orderdetail.png" /><a href="wxpaccount.do?action=toorderlist&openid=${openid}"><div><span style="color:#101010;margin-left: 15px;">历史订单</span></div></a></li>
				<li class="li1" style="margin-top:1px;"><img class="img1" src="${pageContext.request.contextPath}/resources/images/wxpublic/dooller.png" /><a href="wxpaccount.do?action=toaccountdetail&openid=${openid}"><div><span style="color:#101010;margin-left: 15px;">账户明细</span></div></a></li>-->

			</ul>
		</div>
	</div>

    <script type="text/javascript">

    </script>
</body>
</html>
