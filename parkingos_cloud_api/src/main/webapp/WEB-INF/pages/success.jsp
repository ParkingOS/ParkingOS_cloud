<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta content="black" name="apple-mobile-web-app-status-bar-style">
<meta content="telephone=no" name="format-detection">
<meta content="email=no" name="format-detection">
<title>访客申请</title>
   <script src="${pageContext.request.contextPath}/resources/js/jquery.js" type="text/javascript"></script>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/prepay.css?v=2111">

	<style type="text/css">
	*{margin:0; padding:0;}
	a{text-decoration: none;}
	img{max-width: 100%; height: auto;}
	.weixin-tip{display: none; position: fixed; left:0; top:0; bottom:0; background: rgba(0,0,0,0.8); filter:alpha(opacity=80);  height: 100%; width: 100%; z-index: 1000;}
	.weixin-tip p{text-align: center; margin-top: 10%; padding:0 5%;}
	
	.bonus{
	width:60px;
	height:70px;
}

.share{
	border-radius:5px;
	width:96%;
	margin-left:2%;
	height:40px;
	margin-top:3%;
	font-size:15px;
	background-color:#04BE02;
	color:white;
}

.first{
	background-image: url(${pageContext.request.contextPath}/resource/images/wxpublic/first_ticket.png);
	background-repeat:no-repeat;
}
</style>
<style type="text/css">
/* 计费方式弹出层 */
.billing-mask .content {
	background-color: #E7E7E7;
	-webkit-border-radius: 3px;
	border-radius: 8px;
	margin: 20% 30px 0;
	padding-bottom: 14px;
}

.billing-mask .billing-top {
	padding-top: 10px;
	padding-left: 10px;
	background-color: #40A18D;
	border-top-left-radius: 6px;
	border-top-right-radius: 6px;
}

.billing-mask .billing-title {
	padding: 35px 0;
	text-align: center;
	font-size: 18px;
	color: #333333;
	font-weight: 600;
	background-color: #40A18D;
}

.billing-mask .billing-middle {
	padding: 40px 0;
	text-align: center;
	font-size: 18px;
	color: #333333;
	font-weight: 600;
	background-color: #FFFFFF;
}

.billing-mask .billing-bottom {
	padding: 40px 0;
	text-align: center;
	font-size: 18px;
	color: #333333;
	font-weight: 600;
	background-color: #FFFFFF;
}

.billing-mask .billing-list {
	padding: 0 15px;
}

.billing-mask .billing-list .list {
	line-height: 20px;
}

.billing-mask .billing-list .totle-list {
	padding-top: 8px;
	position: relative
}

.billing-mask .billing-list .totle-list:before {
	content: "";
	width: 100%;
	display: block;
	border-top: 1px solid #CECECE;
	position: absolute;
	left: 0;
	top: 4px;
}

.billing-mask .billing-list .green-font {
	color: #22AC38;
}

.billing-mask .rules {
	padding: 0 15px;
	background-color: white;
}

.billing-mask .rules .rules-title {
	font-size: 14px;
	color: #666666;
	padding: 45px 0 12px;
	font-weight: 600;
	text-align: center;
	color: red;
}

.billing-mask .rules .rules-content {
	font-size: 14px;
	color: #666666;
	padding: 0px 0 45px;
	font-weight: 600;
	text-align: center;
	color: gray;
}

.billing-mask .rules .rules-main {
	font-size: 10px;
	color: #999999;
	line-height: 35px;
}

.billing-mask .close-btn {
	margin: 0px 0px -15px;
}

.mask {
	position: fixed;
	background-color: rgba(0, 0, 0, 0.75);
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	z-index: 999;
}

.btn {
	display: block;
	height: 42px;
	line-height: 42px;
	text-align: center;
	border-bottom-left-radius: 8px;
	border-bottom-right-radius: 8px;
	border-top-left-radius: 0px;
	border-top-right-radius: 0px;
	font-size: 18px;
	-webkit-box-sizing: border-box;
	box-sizing: border-box;
	-webkit-tap-highlight-color: rgba(0, 0, 0, 0);
}

.img1 {
	width: 60px;
	height: 60px;
	margin-top:-10px;
}

.img2 {
	height: 15px;
}
</style>
</head>
<body>
<div class="weixin-tip">
	<p>
		<img id="android"  />
		<img id="ios" style="height:420px;"  />
	</p>
</div>

	<!-- 计费方式弹出层]] -->
	<section>
		<div class="success">
			<div class="tips">
				<span class="icon-area ok-icon" id="image"><!-- 图标 --></span>

				<div class="tips-title" id="noticetitle">${errmsg}</div>
			</div>
		</div>

	</section>

<script type="text/javascript">
	$(document).ready(function() {
		var userAgent = navigator.userAgent.toLowerCase();
		var winHeight = $(window).height();
		if(userAgent.match(/iphone os/i) == "iphone os"){
			document.getElementById("android").style.display = "none";
			$("#ios").css("height",winHeight*0.85);
		}else{
			document.getElementById("ios").style.display = "none";
			$("#android").css("height",winHeight*0.85);
		}
	});



</script>

</body>
</html>
