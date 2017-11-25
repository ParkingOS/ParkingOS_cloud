<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<title>停车收费趋势曲线</title>
<link rel="stylesheet" type="text/css" href="js/anlysis/style.css?v=20100302" />
<script type="text/javascript" src="js/anlysis/jquery.min.js"></script>
<script type="text/javascript" src="js/anlysis/highcharts.js"></script>
<script type="text/javascript" src="js/anlysis/colltrend.js?v=2"></script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
<style type="text/css">
.title1{
	width: 100%;
    margin: 0 auto;
    font-weight: bold;
    line-height: 25px;
    height: 40px;
    background: #EFEFEF;
    padding-top: 15px;
    border-bottom: 1px solid #ddd;
    font-size:16px !important;
}
.sel_fee{
	text-align: center;
    padding-top: 2px;
    padding-bottom: 2px;
    border-radius: 0px;
    background-color: #FFFFFF;
    outline: medium;
    border: 1px solid #5CCDBE;
    color: #5CCDBE;
    padding-left: 8px;
    padding-right: 8px;
}
.title1 a:hover{
	background:#5CCDBE;
	color:#FFFFFF;
}

.column{
	background:#5CCDBE;
	color:#FFFFFF;
}
a:link {
    color: #5CCDBE;
    text-decoration: none;
}
a:visited {
    color: #5CCDBE;
    text-decoration: none;
}
</style>
</head>
<body >
<div id="data_container">
<!--顶部开始-->
<div class="top">
<ul class="title1">
		<a class="sel_fee column" style="margin-left:10px;">图形</a><a href="colltrend.do?action=list" class="sel_fee" style="margin-left:-1px;">列表</a>
	</ul>
<ul class="search">
<span id="seconddateinput" class="search_text">
日期：<input  class="Wdate" onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 00:00:00',alwaysUseStartDate:false})" type="text" name="endDateSelect" id="btime" value="${btime}" align="absmiddle" readonly   />
-&nbsp;<input  class="Wdate" onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 23:59:59',alwaysUseStartDate:false})" type="text" name="endDateSelect" id="etime" value="${etime}" align="absmiddle" readonly   />
</span>
<span class="search_button">
<span class="button_light_green"><button name="" onclick="loaddata()"><span class="confirm"><img src="js/anlysis/spacer.gif" /></span>确定</button></span>
</span>
</ul>
</div>
<!--顶部结束-->
<!--工具栏开始-->
<div class="tooles">
<%-- <span id="tips" class="count_tips" style="margin-left:29px;font-size:14px;">当前泊位总数：${asum}&nbsp;个</span> --%>
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
