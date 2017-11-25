<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>支付方式分析</title>
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
<style type="text/css">
.sel_fee{
	text-align: center;
    /* padding-top: 0px; */
    /* padding-bottom: 0px; */
    border-radius: 0px;
    background-color: #FFFFFF;
    outline: medium;
    border: 1px solid #5CCDBE;
    color: #5CCDBE;
    padding-left: 8px;
    padding-right: 8px;
    font-size: 12px;
    height: 24px;
    margin-top: 3px;
    line-height: 24px;
}
a:hover{
	background:#5CCDBE;
	color:#FFFFFF;
}
</style>
</head>
<body onload='addgroups()'>

<div id="paytypeanlysisobj" style="width:100%;height:100%;margin:0px;"></div>

<script type="text/javascript" >
var btime="${btime}";
var etime="${etime}";
var groups = eval(T.A.sendData("getdata.do?action=getgroups&cityid=${cityid}"));

var _mediaField = [
		{fieldcnname:"车场编号",fieldname:"comid",inputtype:"text", twidth:"60" ,issort:false},
		{fieldcnname:"停车场",fieldname:"company_name",inputtype:"text", twidth:"80",issort:false},
		{fieldcnname:"总收入(总实收+已追缴)",fieldname:"allfact",inputtype:"number", twidth:"140",issort:false},
		{fieldcnname:"现金收费",fieldname:"cash",inputtype:"number", twidth:"80",issort:false},
		//{fieldcnname:"账户支付",fieldname:"account",inputtype:"number", twidth:"80",issort:false},
		{fieldcnname:"包月",fieldname:"bymonth",inputtype:"number", twidth:"80",issort:false}
		//{fieldcnname:"现金预支付",fieldname:"prepaid",inputtype:"number", twidth:"80",issort:false},
		//{fieldcnname:"银联",fieldname:"unionpay",inputtype:"number", twidth:"80",issort:false},
		//{fieldcnname:"商家卡",fieldname:"storcard",inputtype:"number", twidth:"80",issort:false},
		//{fieldcnname:"免费放行",fieldname:"free",inputtype:"number", twidth:"80",issort:false},
		//{fieldcnname:"已追缴",fieldname:"payment",inputtype:"number", twidth:"80",issort:false}
		];
var back = "";
if("${from}" == "index"){
	back = "<a href='cityindex.do?authid=${index_authid}' class='sel_fee' style='float:right;margin-right:20px;'>返回</a>";
}
var _escapecountT = new TQTable({
	tabletitle:"支付方式分析"+back,
	ischeck:false,
	tablename:"paytypeanlysis_tables",
	dataUrl:"paytypeanlysis.do",
	iscookcol:false,
	buttons:false,
	quikcsearch:coutomsearch(),
	param:"action=query",
	tableObj:T("#paytypeanlysisobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
	var html = "&nbsp;&nbsp; 时间：<input id='coutom_btime' value='"+btime+"' style='width:70px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"+
				"&nbsp;&nbsp;<input id='coutom_etime' value='"+etime+"' style='width:70px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"+
				"&nbsp;&nbsp;&nbsp;&nbsp;所属集团：&nbsp;&nbsp;<select style='width:130px' id='groups'></select>&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
	return html;
}
function addgroups(){
	var childs = groups;
	var groupselect = document.getElementById("groups");
	for(var i=0;i<childs.length;i++){
		var child = childs[i];
		var id = child.value_no;
		var name = child.value_name;
		groupselect.options.add(new Option(name, id));
	}
}
function searchdata(){
    var groupid = T("#groups").value;
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;

	//data=eval(T.A.sendData("parkingturnover.do?action=echarts&btime="+btime+"&etime="+etime));
	_escapecountT.C({
		cpage:1,
		tabletitle:"搜索结果"+back,
		extparam:"&action=query&btime="+btime+"&etime="+etime+"&groupid="+groupid
	});
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
	addgroups();
	T("#groups").value = groupid;
   
}

function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}
_escapecountT.C();

</script>
</body>
</html>
