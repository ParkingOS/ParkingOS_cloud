<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>NFC记录</title>
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
</head>
<body>
<div id="productanlysisobj" style="width:100%;height:100%;margin:0px;"></div>
<form action="" method="post" id="choosecom"></form>
<script >
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
var comid = "${comid}";
var groupid = "${groupid}";
var cityid = "${cityid}";
var btime="${btime}"
var etime="${etime}";
var mobile = '${mobile}';
var carnumber='${carnumber}';
var viewtype="custom";
var tip = "区间查询";
var _mediaField = [
		{fieldcnname:"套餐名称",fieldname:"p_name",inputtype:"text", twidth:"200",issort:false},
		{fieldcnname:"购买日期",fieldname:"create_time",inputtype:"text", twidth:"200",issort:false},
		{fieldcnname:"车牌号",fieldname:"car_number",inputtype:"text", twidth:"100",issort:false},
		{fieldcnname:"手机号",fieldname:"mobile",inputtype:"text", twidth:"100",issort:false},
		{fieldcnname:"价格",fieldname:"price",inputtype:"text", twidth:"100" ,issort:false},
		{fieldcnname:"金额 ",fieldname:"total",inputtype:"text", twidth:"100" ,issort:false},
		{fieldcnname:"有效期",fieldname:"exprise",inputtype:"text", twidth:"200",issort:false}
	];
var _productanlysisT = new TQTable({
	tabletitle:"套餐统计",
	ischeck:false,
	tablename:"productanlysis_tables",
	dataUrl:"productanlysis.do",
	iscookcol:false,
	buttons:false,
	quikcsearch:coutomsearch(),
	param:"action=query&comid="+comid,
	tableObj:T("#productanlysisobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	allowpage:false,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
	var html=    "<input type='button' onclick='searchtomonth();' value='本月'/>&nbsp;&nbsp;时间：<input id='coutom_btime' value='"+btime+"' style='width:70px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"
				+" - <input id='coutom_etime' value='"+etime+"' style='width:70px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"+
				"&nbsp;&nbsp;车牌号：<input id='carnumber' style='width:100px'/>&nbsp;&nbsp;手机号<input id='mobile'  style='width:100px'/>&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";//"&nbsp;&nbsp;总计：900.00元";
	
	if(groupid != "" || cityid != ""){
		html += "&nbsp;&nbsp;&nbsp;&nbsp;车场:&nbsp;&nbsp;<select id='companys' onchange='searchcoms();' ></select>";
	}
	html += "&nbsp;&nbsp;<span id='total_money'></span>";
	return html;
}
function searchcoms(){
	comid = T("#companys").value;
	T("#choosecom").action="productanlysis.do?comid="+comid+"&authid=${authid}&r"+Math.random();
	T("#choosecom").submit(); 
}
function searchtomonth(){
	viewtype='tomonth';
	tip = "本月统计";
	_productanlysisT.C({
		cpage:1,
		tabletitle:"本月统计",
		extparam:"&action=query&type=tomonth&comid="+comid
	})
	addcoms();
}
function searchdata(){
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;
	mobile = T("#mobile").value;
	carnumber = T("#carnumber").value
	var cn=encodeURI(encodeURI(carnumber));
	viewtype="custom";
	_productanlysisT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&comid="+comid+"&action=query&btime="+btime+"&etime="+etime+"&mobile="+mobile+"&carnumber="+cn
	})
	addcoms();
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
	T("#mobile").value=mobile;
	T("#carnumber").value=carnumber;
}

function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}
_productanlysisT.C();
function addcoms(){
	if(groupid != "" || cityid != ""){
		var childs = eval(T.A.sendData("getdata.do?action=getcoms&groupid=${groupid}&cityid=${cityid}"));
		jQuery("#companys").empty();
		for(var i=0;i<childs.length;i++){
			var child = childs[i];
			var id = child.value_no;
			var name = child.value_name;
			jQuery("#companys").append("<option value='"+id+"'>"+name+"</option>"); 
		}
		T("#companys").value = comid;
	}
}
if(groupid != "" || cityid != ""){//集团管理员登录下显示车场列表
	addcoms();
}
</script>

</body>
</html>
