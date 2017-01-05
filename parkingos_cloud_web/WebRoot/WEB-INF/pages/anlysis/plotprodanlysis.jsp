<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>停车位月卡收费台账</title>
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
<div id="plotprodanlyobj" style="width:100%;height:100%;margin:0px;"></div>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<script >
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
} 
var btime="${btime}"
var etime="${etime}";
var viewtype="curyear";
var tip = "";
var _mediaField = [
		{fieldcnname:"车位编号",fieldname:"p_lot",inputtype:"text", twidth:"120",issort:false},
		{fieldcnname:"房号",fieldname:"address",inputtype:"text", twidth:"120",issort:false},
		{fieldcnname:"收取次数",fieldname:"count",inputtype:"text", twidth:"120",issort:false,
			process:function(value,cid,id){
					return "<a href=# onclick=\"viewdetail('h','"+value+"','"+cid+"',0)\" style='color:blue'>"+value+"</a>";
				}},
		{fieldcnname:"实收取金额",fieldname:"atotal",inputtype:"text", twidth:"120",issort:false},
		{fieldcnname:"优惠金额",fieldname:"dtotal",inputtype:"text", twidth:"120",issort:false},
		{fieldcnname:"去年金额",fieldname:"lasttotal",inputtype:"text", twidth:"120",issort:false},
		{fieldcnname:"今年金额",fieldname:"curtotal",inputtype:"text", twidth:"120",issort:false},
		{fieldcnname:"明年金额",fieldname:"nexttotal",inputtype:"text", twidth:"120",issort:false}
	];
var _plotprodanlyT = new TQTable({
	tabletitle:"停车位月卡收费台账",
	ischeck:false,
	tablename:"plotprodanly_tables",
	dataUrl:"plotprod.do",
	iscookcol:false,
	buttons:false,
	quikcsearch:coutomsearch(),
	param:"action=query",
	tableObj:T("#plotprodanlyobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	allowpage:false,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
	var html= "<input type='button' onclick='searchcuryear();' value='今年'/>&nbsp;&nbsp;<input type='button' onclick='searchlstyear();' value='去年'/>&nbsp;&nbsp;<input type='button' onclick='exportExcel();' value='导出台账'/>";
	return html;
}

function searchcuryear(){
	viewtype = 'curyear';
	tip = "今年统计";
	_plotprodanlyT.C({
		cpage:1,
		tabletitle:tip,
		extparam:"&action=query&time="+viewtype
	})
}

function searchlstyear(){
	viewtype = 'lastyear';
	tip = "去年统计";
	_plotprodanlyT.C({
		cpage:1,
		tabletitle:tip,
		extparam:"&action=query&time="+viewtype
	})
}

function exportExcel(){
	T("#exportiframe").src="plotprod.do?action=expexcel&time="+viewtype
}
function searchdata(){
	viewtype="custom";
	tip = "区间查询";
	_plotprodanlyT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&time="+viewtype
	})
}

function viewdetail(type,value,id,pay_type){
	var p_lot =_plotprodanlyT.GD(id,"p_lot");
	Twin({
		Id:"plotprod_detail_"+id,
		Title:tip+"  --> 车位编号："+p_lot,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='plotprod_detail_'"+id+" id='plotprod_detail_'"+id+" src='plotprod.do?action=detail&p_lot="+p_lot+"' width='100%' height='100%' frameborder='0' scrolling='no' style='overflow:hidden;' ></iframe>"
	})
}

function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}
_plotprodanlyT.C();
</script>

</body>
</html>
