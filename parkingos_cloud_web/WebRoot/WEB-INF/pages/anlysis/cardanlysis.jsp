<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>储值卡收入分析</title>
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

</head>
<body>
<div id="cardanlysisobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
var btime="${btime}"
var etime="${etime}";
var _mediaField = [
		{fieldcnname:"运营集团编号",fieldname:"groupid",inputtype:"text", twidth:"200" ,issort:false,fhide:true},
		{fieldcnname:"发卡总数",fieldname:"e_all_count",inputtype:"text", twidth:"150",issort:false},
		{fieldcnname:"期初结存额",fieldname:"b_all_balance",inputtype:"text", twidth:"150",issort:false},
		{fieldcnname:"期末结存额",fieldname:"e_all_balance",inputtype:"text", twidth:"150",issort:false},
		{fieldcnname:"本期发卡数量",fieldname:"slot_act_count",inputtype:"text", twidth:"150",issort:false},
		{fieldcnname:"本期发卡面值",fieldname:"slot_act_balance",inputtype:"text", twidth:"150",issort:false},
		{fieldcnname:"本期储值",fieldname:"slot_charge",inputtype:"text", twidth:"150",issort:false},
		{fieldcnname:"本期消费",fieldname:"slot_consume",inputtype:"text", twidth:"150",issort:false},
		{fieldcnname:"本期注销卡数量",fieldname:"slot_refund_count",inputtype:"text", twidth:"150",issort:false},
		{fieldcnname:"本期注销卡金额",fieldname:"slot_refund_balance",inputtype:"text", twidth:"150",issort:false}
	];
var _cardanlysisT = new TQTable({
	tabletitle:"储值卡收入分析",
	ischeck:false,
	tablename:"cardanlysis_tables",
	dataUrl:"cardanlysis.do",
	iscookcol:false,
	buttons:false,
	quikcsearch:coutomsearch(),
	param:"action=query&btime="+btime+"&etime="+etime,
	tableObj:T("#cardanlysisobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	allowpage:true,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
	var html = "&nbsp;&nbsp;&nbsp;&nbsp;时间：&nbsp;&nbsp;<input id='coutom_btime' class='Wdate' align='absmiddle' readonly value='"+btime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 00:00:00',alwaysUseStartDate:false});\"/>"
	+" - <input id='coutom_etime' class='Wdate' align='absmiddle' readonly value='"+etime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 23:59:59',alwaysUseStartDate:false});\"/>"+
	"&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
	return html;
}
function searchdata(){
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;
	_cardanlysisT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&btime="+btime+"&etime="+etime
	})
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
}

function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}

_cardanlysisT.C();
</script>

</body>
</html>
