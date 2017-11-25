<%@ page language="java" contentType="text/html; charset=gb2312"
	pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport"
	content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<title>${cwxname}</title>
<script src="js/jquery.js"></script>
<script src="js/wxpublic/strophe.js"></script>
<script src="js/wxpublic/json2.js"></script>
<script src="js/wxpublic/easemob.im-1.0.7.js"></script>
<script src="js/wxpublic/bootstrap.js"></script>
<script src="js/wxpublic/easemob.im.config.js"></script>
<script src="js/wxpublic/localstorage.js?v=2"></script>

<link rel="stylesheet" href="css/chat.css?v=3" media="screen" type="text/css" />
<style type="text/css">
.bottom {
	background-color: #EBEBEB;
	z-index: 999;
	position: fixed;
	bottom: 0;
	left: 0;
	width: 100%;
	_position: absolute;
	_top: expression(documentElement.scrollTop +         
		       documentElement.clientHeight-this.offsetHeight);
	overflow: visible;
	background-color: white;
}

.inputmsg {
	text-align: center;
}

.send {
	float: right;
	margin-right: 10px;
	margin-top: 7px;
	margin-bottom: 5px;
	border-radius: 5px;
	width: 17%;
	background-color: #04BE02;
	color: white;
	line-height: 32px;
	border: 0px;
}

.chat-thread .me {
	float: left;
	margin-left: -30px;
	
	position: relative;
	clear: both;
	display: inline-block;
	margin-top: 20px;
	/* font: 16px/20px 'Noto Sans', sans-serif; */
	border-radius: 10px;
}

.chat-thread .contacts {
	float: right;
	margin-right: 10px;
	
	position: relative;
	clear: both;
	display: inline-block;
	margin-top: 20px;
	/* font: 16px/20px 'Noto Sans', sans-serif; */
	border-radius: 10px;
}

.imgme {
	width: 40px;
	height: 40px;
}

.imgcontacts {
	width: 40px;
	height: 40px;
	float: right;
}

.memsg {
	padding: 10px 10px 10px 10px;
	background-color: white;
	margin-top: -40px;
	margin-left: 56px;
	border-radius: 10px;
}

.contactsmsg {
	padding: 10px 10px 10px 10px;
	background-color: #04BE02;
	color: white;
	margin-right: 56px;
	border-radius: 10px;
}

.arrowleft {
	position: absolute;
	top: 10px;
	margin-left: 40px;
	width: 0;
	height: 0;
	font-size: 0;
	border: solid 8px;
	border-color: #EBEBEB white #EBEBEB #EBEBEB;
}

.arrowright {
	position: absolute;
	top: 10px;
	right: 40px;
	width: 0;
	height: 0;
	font-size: 0;
	border: solid 8px;
	border-color: #EBEBEB #EBEBEB #EBEBEB #04BE02;
}

.message {
	float: left;
	width: 75%;
	max-height: 80px;
	margin-left: 5px;
	outline: 0;
	border: 1px solid gray;
	border-radius: 5px;
	font-size: 14px;
	word-wrap: break-word;
	overflow-x: hidden;
	overflow-y: auto;
	margin-top: 7px;
	margin-bottom: 7px;
}

.msgfunc {
	height: 150px;
	width: 100%;
	background-color: white;
	margin-top: 50px;
	border-top:1px solid #EBEBEB;
}

.hide{
	display:none;
}

.history {
    position: fixed;
    right: 0;
    top: 10%;
    /* width: 100px; */
    border-top-left-radius: 15px;
    border-top-right-radius: 5px;
    border-bottom-left-radius: 15px;
    border-bottom-right-radius: 5px;
    /* background: #3B9DD6; */
    background: white;
    font-size: 13px;
    padding-right: 10px;
    padding-left: 10px;
    padding-top: 7px;
    padding-bottom: 5px;
}

.time{
	float:left;
	text-align:center;
	width:93%;
	font-size:11px;
	margin-top: 20px;
}

.loadinghis{
	float:left;
	text-align:center;
	width:93%;
	font-size:11px;
	margin-top: 0px;
}

.loadmore{
	float:left;
	text-align:center;
	width:93%;
	font-size:13px;
	margin-top: 0px;
	color:#3A6EA5;
	font-weight:bold;
}

.func{
	float:left;
	margin-left:10px;
	margin-top: 10px;
}

.funcadd{
	width:30px;
	height:30px;
}
</style>
<style type="text/css">
/* 计费方式弹出层 */
.billing-mask .content {
	background-color: #E7E7E7;
	-webkit-border-radius: 3px;
	border-radius: 8px;
	margin: 50% 20% 0;
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

.mask {
	position: fixed;
	background-color: rgba(0, 0, 0, 0.75);
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	z-index: 99999;
}
</style>
</head>
<body>
	<!-- 计费方式弹出层[[ -->
	<div class="mask billing-mask hide">
		<div class="content">
			<dl class="rules">
				<dt class="rules-title">您已掉线，正在重连...</dt>
				<dt class="rules-content"><img class="img1" src="images/wxpublic/connecting.gif" /></dt>
			</dl>
		</div>
	</div>
	<!-- 计费方式弹出层]] -->
	<div id="convo" data-from="Sonu Joshi">
		<ul id="wechat" class="chat-thread">
		</ul>
	</div>
	<div class="bottom">
		<div class="inputmsg">
			<!-- <span class="func" onclick="showfunc();"><img class="funcadd" src="images/wxpublic/add.png" /></span> -->
			<textarea id="message" class="message" onfocus="hidefunc();"></textarea>
			<input class="send" onclick="sendText();" type="button" value="发送" />
			
		</div>
		<div id="msgfuncid" class="msgfunc hide"></div>
	</div>
	<div style="display:none;">
		<input id="username" type="text" value="${username}" />
		<input id="password" type="text" value="${password}" />
		<input id="contacts" type="text" value="${contacts}" />
		<input id="fromimg" type="text" value="${fromimg}" />
		<input id="toimg" type="text" value="${toimg}" />
	</div>
	<div class="history hide" onclick="readhis();">
		历史消息
	</div>
</body>
<script src="js/wxpublic/chat.js?v=16"></script>
</html>
