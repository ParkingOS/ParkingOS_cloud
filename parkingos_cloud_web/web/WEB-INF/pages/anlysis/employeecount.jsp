<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>泊位周转率</title>
<link href="css/zTreeStyle1.css" rel="stylesheet" type="text/css">
<link href="css/demo.css" rel="stylesheet" type="text/css">
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.public.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.datatable.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.form.js?0817" type="text/javascript">//表单</script>
<script src="js/tq.searchform.js?0817" type="text/javascript">//查询表单</script>
<script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
<script src="js/jquery.js" type="text/javascript"></script>
<script src="js/jquery.ztree.core-3.5.js" type="text/javascript"></script>
 <script src="js/echarts/echarts.js"></script>


</head>
<body>

<div id="employeecountobj" style="width:100%;height:100%;margin:0px;"></div>

<script type="text/javascript" >
var btime="${btime}";
var etime="${etime}";
var _mediaField = [
		{fieldcnname:"收费员编号",fieldname:"comid",inputtype:"text", twidth:"100" ,issort:false},
		{fieldcnname:"收费员",fieldname:"company_name",inputtype:"text", twidth:"100",issort:false},
		{fieldcnname:"总收入",fieldname:"count",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"车检器应收",fieldname:"total",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"订单应收",fieldname:"count",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"订单应收占比(%)",fieldname:"total",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"实收",fieldname:"count",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"实收占比(%)",fieldname:"total",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"未缴",fieldname:"count",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"未缴占比(%)",fieldname:"total",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"追缴",fieldname:"count",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"追缴占比(%)",fieldname:"total",inputtype:"number", twidth:"100",issort:false}
		];
var _employeecountT = new TQTable({     
	tabletitle:"未缴情况汇总",
	ischeck:false,
	tablename:"employeecount_tables",
	dataUrl:"employeecount.do",
	iscookcol:false,
	buttons:false,
	quikcsearch:coutomsearch(),
	param:"action=query",
	tableObj:T("#employeecountobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
	var html = "&nbsp;&nbsp; 时间：<input id='coutom_btime' value='"+btime+"' style='width:70px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"+
				"&nbsp;&nbsp;<input id='coutom_etime' value='"+etime+"' style='width:70px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"+
				"&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
	return html;
}

function searchdata(){
    
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;

	//data=eval(T.A.sendData("parkingturnover.do?action=echarts&btime="+btime+"&etime="+etime));
	_escapecountT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&btime="+btime+"&etime="+etime,
	});
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;

   
}

function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}
_employeecountT.C();

</script>
</body>
</html>
