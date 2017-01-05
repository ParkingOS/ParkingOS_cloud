<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>商户优惠券</title>
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
<div id="parkaccountobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
var shop_id="${shop_id}";
var type="${type}";
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",hide:true},
		{fieldcnname:"打印时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"200" ,height:"",issort:false},
		{fieldcnname:"过期日期",fieldname:"limit_day",fieldvalue:'',inputtype:"date",twidth:"200" ,height:"",issort:false},
		{fieldcnname:"优惠的时间(小时)",fieldname:"money",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",hide:true},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"未使用"},{"value_no":1,"value_name":"已使用"}],twidth:"100" ,height:"",issort:false},
		{fieldcnname:"类型",fieldname:"type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":3,"value_name":"优惠券"},{"value_no":4,"value_name":"全免券"}],twidth:"100" ,height:"",hide:true}
	];
var _parkaccountT = new TQTable({
	tabletitle:"商户优惠券",
	ischeck:false,
	tablename:"money_tables",
	dataUrl:"shopticketanlysis.do",
	iscookcol:false,
	param:"action=quickquerydetail&shop_id="+shop_id+"&type="+type,
	tableObj:T("#parkaccountobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}
_parkaccountT.C();
</script>

</body>
</html>
