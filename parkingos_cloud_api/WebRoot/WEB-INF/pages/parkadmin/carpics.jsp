<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>显示图片 </title>
<style type="text/css">
html,body {
	overflow:auto;
}
</style>
</head>
<body>
<div>
<span style="color:red;">&nbsp;&nbsp;&nbsp; 入口车辆照片</span>
<div><img src="" id="p1" width="600px" height="600px"></img></div><br/>
<span style="color:red;">出口车辆图片</span>
<div><img src="" id="p2" width="600px" height="600px"></img></div>
</div>
</body>
<script>
	document.getElementById('p1').src="carpicsup.do?action=downloadpic&comid=0&type=0&orderid=${orderid}&r="+Math.random();
	document.getElementById('p2').src="carpicsup.do?action=downloadpic&comid=0&type=1&orderid=${orderid}&r="+Math.random();
</script>
</html>
