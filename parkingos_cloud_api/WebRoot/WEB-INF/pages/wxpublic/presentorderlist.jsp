<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<meta name="renderer" content="webkit">
<title>停车缴费</title>
<script type="text/javascript">
	var ua = navigator.userAgent.toLowerCase();
	if (ua.match(/MicroMessenger/i) != "micromessenger"){
		window.location.href = "http://s.tingchebao.com/zld/error.html";
	}
</script>

<style type="text/css">
#scroller li {
    padding:0 10px;
    height:80px;
    line-height:40px;
    background-color:#FFFFFF;
    font-size:14px;
    margin-top:1px;
}

.right2{
	float:right;
	margin-right:30px;
	
}
.left{
	float:left;
	//margin-right:10px;
}

.hide{
	display:none;
}

a{
	text-decoration:none;
	color:#5F5F5F;
}

#header {
    position:absolute; z-index:2;
    top:0; left:0;
    width:100%;
    height:45px;
    line-height:45px;
    background-color:#F3F3F3;
    padding:0;
    font-size:20px;
    text-align:center;
}

.red{
	color:red;
}
.wx_pay{
	border-radius:15px;
	//margin-left:2%;
	//height:20px;
	//margin-top:5%;
	font-size:14px;
	background-color:#04BE02;
	color:white;
}
.wx_checkbox{
	background-color: #FFF; 
    border: 1px solid #C1CACA; 
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05), inset 0px -15px 10px -12px rgba(0, 0, 0, 0.05); 
    border-radius: 5px; 
    vertical-align:text-bottom; margin-bottom:1px;
}
.passli {
	background-image: url(images/wxpublic/arrow.png);
	background-size: 19px 39px;
	background-repeat: no-repeat;
	background-position: right center;
}
.middle1 {
	margin-top: 45%;
	color: gray;
	position: relative;
	z-index: 99;
	text-align: center;
	font-size: 17px;
}

.hide1{
	display:none;
}

</style>
<link rel="stylesheet" type="text/css" href="css/list.css?v=6" />
<link rel="stylesheet" href="css/weui.min.css">
<link href="https://cdn.bootcss.com/jquery-weui/1.0.0-rc.1/css/jquery-weui.min.css" rel="stylesheet">
</head>
<body>

	<div id="wrapper" style="margin-top:-45px;">
		<div id="scroller">
			<div id="pullDown" class="idle">
				<span class="pullDownIcon"></span>
				<span class="pullDownLabel">下拉刷新...</span>
			</div>
			
			<div class="middle1 hide1">
			<div style="margin-bottom:13px"><i class="weui-icon-info weui-icon_msg"></i></div>
			暂无订单</div>
			<ul id="thelist">
			<!-- 
			<div class="weui-form-preview">
				  <div class="weui-form-preview__hd">
				    <label class="weui-form-preview__label"><span style="font-size:20px;color:black">京T12312</span></label>
				    <em class="weui-form-preview__value"><span style="font-size:20px;">￥240.00</span></em>
				  </div>
				  <div class="weui-form-preview__bd">
				    <div class="weui-form-preview__item">
				      <label class="weui-form-preview__label">入场时间</label>
				      <span class="weui-form-preview__value">2017-05-12 09:37</span>
				    </div>
				    <div class="weui-form-preview__item">
				      <label class="weui-form-preview__label">锁定状态</label>
				      <span class="weui-form-preview__value"><span style="color:red">已锁定</span>&nbsp;&nbsp;&nbsp;&nbsp;</span>
				    </div>
				  </div>
				  <div class="weui-form-preview__ft">
				    <button class="weui-form-preview__btn weui-form-preview__btn_primary">锁定车辆</button>
				    <button class="weui-form-preview__btn weui-form-preview__btn_primary">支付订单</button>
				  </div>
				</div>
				<br/>
				 -->
			</ul>
			
			<div id="pullUp" class="idle">
				<span class="pullUpIcon"></span>
				<span class="pullUpLabel">上拉加载更多...</span>
			</div>
		</div>
	</div>
	<div id="footer"></div>
	<input id="mobile" type="text" style="display:none;" value="${mobile}"/>
	<input id="openid" type="text" style="display:none;" value="${openid}"/>
	<input id="domain" type="text" style="display:none;" value="${domain}"/>
<script src="js/wxpublic/iscroll.js"></script>
<script src="js/presentorderlist.js?v=5"></script>
<script src="js/jquery.js"></script>
<script src="js/wxpublic/jquery-weui.min.js"></script>
</body>
</html>
