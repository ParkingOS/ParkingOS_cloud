<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<title>我的账户</title>
<link rel="stylesheet" href="css/weui.min.css">
<link href="https://cdn.bootcss.com/jquery-weui/1.0.0-rc.1/css/jquery-weui.min.css" rel="stylesheet">
<style type="text/css">
.img2{
	width:30px
}

</style>
</head>
<body style="background-color:#EEEEEE;">
<div class="weui-cells">
  <a class="weui-cell weui-cell_access" href="javascript:;">
    <div class="weui-cell__hd"><img src="${wximg}" style="width:60px;margin-right:5px;display:block"></div>
    <div class="weui-cell__bd">
      <p style="font-size:20px">&nbsp;&nbsp;${wxname}</p>
    </div>
  </a>
</div>

<div class="weui-cells">
  <a class="weui-cell weui-cell_access" href="wxpaccount.do?action=tocarnumber&openid=${openid}">
    <div class="weui-cell__hd"><img src="images/wxpublic/carnumber1.png" style="width:20px;margin-right:5px;display:block"></div>
    <div class="weui-cell__bd">
      <p>我的车辆</p>
    </div>
    <div class="weui-cell__ft"></div>
  </a>
</div>

<div class="weui-cells">
  <a class="weui-cell weui-cell_access" href="wxpaccount.do?action=toorderlist&openid=${openid}">
    <div class="weui-cell__hd"><img src="images/wxpublic/orderdetail.png" style="width:20px;margin-right:5px;display:block"></div>
    <div class="weui-cell__bd">
      <p>历史订单</p>
    </div>
    <div class="weui-cell__ft"></div>
  </a>
  <a class="weui-cell weui-cell_access" href="wxpaccount.do?action=toaccountdetail&openid=${openid}">
    <div class="weui-cell__hd"><img src="images/wxpublic/dooller.png" style="width:20px;margin-right:5px;display:block"></div>
    <div class="weui-cell__bd">
      <p>账户明细</p>
    </div>
    <div class="weui-cell__ft"></div>
  </a>
</div>
<script src="js/jquery.js"></script>
<script src="js/wxpublic/jquery-weui.min.js"></script>
<script type="text/javascript">
function usable(type){
	window.location.href = "wxpaccount.do?action=toticketpage&type="+type+"&openid=${openid}";
}
</script>
<script type="text/javascript">

</script>
</body>
</html>
