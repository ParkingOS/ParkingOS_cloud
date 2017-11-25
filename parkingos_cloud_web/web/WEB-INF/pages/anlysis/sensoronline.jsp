<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<title>车检器在线率</title>
<link rel="stylesheet" type="text/css" href="js/anlysis/style.css?v=20100302" />
<script type="text/javascript" src="js/anlysis/jquery.min.js"></script>
<script type="text/javascript" src="js/anlysis/highcharts.js"></script>
<script type="text/javascript" src="js/anlysis/sensoronline.js"></script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>

</head>
<body >
<div id="data_container">
<!--顶部开始-->
<div class="top">
<ul class="title"><li class="parentmenu">车检器在线率</li>
</ul>
<ul class="search">
<span id="seconddateinput" class="search_text">
日期：<input  class="Wdate" onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-01',alwaysUseStartDate:true})" type="text" name="endDateSelect" id="btime" value="${btime}" align="absmiddle" readonly   />
-&nbsp;<input  class="Wdate" onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-01',alwaysUseStartDate:true})" type="text" name="endDateSelect" id="etime" value="${etime}" align="absmiddle" readonly   />
</span>
<span class="search_button">
<span class="button_light_green"><button name="" onclick="loaddata()"><span class="confirm"><img src="js/anlysis/spacer.gif" /></span>确定</button></span>
</span>
</ul>
</div>
<!--顶部结束-->
<!--工具栏开始-->
<div class="tooles">
<span id="tips" class="count_tips" style="margin-left:29px;font-size:14px;"></span>
<!-- <span class="tooles_botton">
<span class="button"><button name="" onclick="javascript:print();"><span class="print2"><img src="js/anlysis/spacer.gif" /></span>打印</button></span>
</span> -->
</div>
<!--工具栏结束-->
<div class="float_clear"></div><!--清除浮动-->

<script type="text/javascript">
$(document).ready(function(){ 
　　loaddata();
}); 
</script>

<div id="chart_container" style="width: 100%; height: 400px; margin:0px auto;float:left"></div>

</body>
</html>
