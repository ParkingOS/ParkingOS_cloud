<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<title>我的机友</title>
<script src="js/jquery.js"></script>
<script src="js/wxpublic/strophe.js"></script>
<script src="js/wxpublic/json2.js"></script>
<script src="js/wxpublic/easemob.im-1.0.7.js"></script>
<script src="js/wxpublic/bootstrap.js"></script>
<script src="js/wxpublic/easemob.im.config.js"></script>
<script src="js/wxpublic/localstorage.js"></script>

<link rel="stylesheet" type="text/css" href="css/jquery.mobile-1.3.2.min.css?v=11" />
<link rel="stylesheet" type="text/css" href="css/list.css?v=2" />
<script src="js/wxpublic/iscroll.js"></script>
<link rel="stylesheet" href="css/chat.css?v=3" media="screen" type="text/css" />
<style type="text/css">
#scroller li {
    padding:0 10px;
    height:60px;
    line-height:35px;
    background-color:#FFFFFF;
    font-size:14px;
    margin-top:1px;
    overflow: hidden;
}

a{
	text-decoration:none;
	color:#6D6D6D;
	font-size:16px;
	
	position: relative;
	top:-35px;
	left:30px;
}

img{
	width:50px;
	height:50px;
	margin-top:5px;
	position:relative;
	z-index:99999;
}

.username {
    margin-left: 50px;
    margin-top: -7px;
    color: #101010;
    font-size: 15px;
    position: relative;
    top: -35px;
    left: 30px;
}

.hide{
	display:none;
}

.circle {
    /* width: 3px; */
    height: 18px;
    line-height: 19px;
    padding-left: 6px;
    padding-bottom: 0px;
    border-top-left-radius: 13px;
    border-bottom-left-radius: 13px;
    border-top-right-radius: 13px;
    border-bottom-right-radius: 13px;
    padding-top: 0px;
    padding-right: 6px;
    position: relative;
    margin-top: 8px;
    background: #f30808;
    float: right;
    margin-right: 49px;
    font-size:11px;
    color:white;
}
</style>
</head>
<body>
	<div id="wrapper" style="margin-top:-45px;">
		<div id="scroller">
			<div id="pullDown" class="loading">
				<span class="pullDownIcon"></span>
				<span class="pullDownLabel">加载机友列表中...</span>
			</div>
 			<ul id="thelist" style="margin-top:50px;">
				<!-- <li id="mydiv"><img src="" /><div class="username">金鱼家化</div></li>
				<li><img src="" /><div class="username">金鱼家化</div></li> -->
				
			</ul>
			<div id="pullUp" class="idle hide">
				<span class="pullUpIcon"></span>
				<span class="pullUpLabel">上拉加载更多...</span>
			</div>
		</div>
	</div>
	<div style="display:none;">
		<input id="username" type="text" value="${username}" />
		<input id="password" type="text" value="${password}" />
	</div>
</body>
<script src="js/wxpublic/hxrosters.js?v=8"></script>
</html>
