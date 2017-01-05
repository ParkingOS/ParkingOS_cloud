<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>dd记录</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.public.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.datatable.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
<script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>

</head>
<body>
<div id="dddetailobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
var btime="${btime}";
var etime="${etime}";
var p_lot="${p_lot}";
var tip = "停车位月卡购买详情";
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",hide:true},
		{fieldcnname:"包月产品名称",fieldname:"p_name",inputtype:"text", twidth:"150" ,issort:false},
		{fieldcnname:"收费标准(元/月)",fieldname:"price",inputtype:"text", twidth:"100" ,issort:false},
		{fieldcnname:"购买日期",fieldname:"create_time",inputtype:"text", twidth:"150" ,issort:false},
		{fieldcnname:"生效日期",fieldname:"b_time",inputtype:"text", twidth:"150" ,issort:false},
		{fieldcnname:"过期日期",fieldname:"e_time",inputtype:"text", twidth:"150",issort:false},
		{fieldcnname:"应付金额",fieldname:"total",inputtype:"text", twidth:"100" ,issort:false},
		{fieldcnname:"实收金额",fieldname:"act_total",inputtype:"text", twidth:"100" ,issort:false},
		{fieldcnname:"优惠金额",fieldname:"favtotal",inputtype:"text", twidth:"100" ,issort:false},
		{fieldcnname:"会员姓名",fieldname:"name",inputtype:"text", twidth:"100" ,issort:false},
		{fieldcnname:"会员手机",fieldname:"mobile",inputtype:"text", twidth:"100" ,issort:false},
		{fieldcnname:"会员车牌",fieldname:"car_number",inputtype:"text",twidth:"200",issort:false}
	];
var _dddetailT = new TQTable({
	tabletitle:tip,
	ischeck:false,
	tablename:"dddetail_tables",
	dataUrl:"plotprod.do",
	iscookcol:false,
	buttons:false,
	quikcsearch:coutomsearch(),
	param:"action=ppdetail&btime="+btime+"&etime="+etime+"&p_lot="+p_lot,
	tableObj:T("#dddetailobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	allowpage:false,
	isoperate:false
});

function coutomsearch(){
	/* var tip = "时间："+btime+" 至 "+etime;
	if(otype=='today')
	 tip="今日订单";
	else if(otype=='toweek')
	 tip="本周订单";
	else if(otype=='lastweek')
	 tip="上周订单";
	else if(otype=='tomonth')
	 tip="本月订单";
	if(pay_type==7){
		var html=   tip+" ，合计免费：<font color='red'>"+total+"</font> 元 ";//"&nbsp;&nbsp;合计免费：900.00元";
	}else{
		var html=   tip+" ，合计：<font color='red'>"+total+"</font> 元，其中现金支付 ：<font color='red'>"+pmoney+"</font>元，手机支付 ：<font color='red'>"+pmobile+"</font>元，共<font color='red'> "+count+" </font>条 ";//"&nbsp;&nbsp;总计：900.00元";
	}
	return html; */
}

function viewdetail(type,value,id){
	var car_number =_dddetailT.GD(id,"car_number");
	var tip = "车辆图片";
	Twin({
		Id:"carpics_detail_"+id,
		Title:tip+"  --> 车牌："+car_number,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='carpics_detail_'"+id+" id='carpics_detail_'"+id+" src='order.do?action=carpics&orderid="+id+"' width='100%' height='"+(T.gwh()-100)+"' frameborder='0' style='overflow:auto;' ></iframe>"
	})
}

_dddetailT.C();
</script>

</body>
</html>
