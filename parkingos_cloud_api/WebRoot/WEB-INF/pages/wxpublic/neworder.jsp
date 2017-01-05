<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<title>${cname}</title>
<script type="text/javascript">
	javascript: window.history.forward(1);
</script>
<link rel="stylesheet" type="text/css" href="css/jquery.mobile-1.3.2.min.css?v=1" />
<link rel="stylesheet" type="text/css" href="css/list.css?v=12" />
<script src="js/jquery.js"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js" type="text/javascript"></script>
<style type="text/css">

.hide {
	display: none;
}

.error {
	color: red;
	font-size: 15px;
}

.success{
	text-align:center;
	color:red;
	margin-top:55%;
	font-size:16px;
}
.tishi{
	text-align:center;
	color:red;
	font-size:16px;
	margin-top:10px;
}
</style>
</head>
<body style="background-color:#EEEEEE;">
<div class="success">生成订单成功！</div>
<div class="tishi">离场时请扫描该车位二维码结算订单</div>
<div id="footer"></div>
<script type="text/javascript">
</script>
</body>
</html>
