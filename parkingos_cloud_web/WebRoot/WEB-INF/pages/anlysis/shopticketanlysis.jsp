<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>减免券统计</title>
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
</head>
<body>
<div id="shopanlysisobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
var btime="${btime}"
var etime="${etime}";
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"text", twidth:"50" ,issort:false},
		{fieldcnname:"商家名称",fieldname:"name",inputtype:"text", twidth:"100",issort:false},
		{fieldcnname:"额度上限",fieldname:"uplimit",inputtype:"text", twidth:"100",issort:false},
		{fieldcnname:"已打印量(张)",fieldname:"allpcount",inputtype:"text", twidth:"100",issort:false/* ,
			process:function(value,cid,id){
					return "<a href=# onclick=\"viewdetail('all','"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
				} */},
		{fieldcnname:"已打印面额(小时)",fieldname:"allptotal",inputtype:"text", twidth:"120",issort:false},
		{fieldcnname:"已使用量(张)",fieldname:"upcount",inputtype:"text", twidth:"100",issort:false/* ,
			process:function(value,cid,id){
						return "<a href=# onclick=\"viewdetail('used','"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
					} */},
		{fieldcnname:"已抵扣额度(小时)",fieldname:"uptotal",inputtype:"text", twidth:"120",issort:false},
		{fieldcnname:"实际抵扣额度(小时)",fieldname:"dtotal",inputtype:"text", twidth:"120",issort:false},
		{fieldcnname:"实际抵扣金额(元)",fieldname:"dmoney",inputtype:"text", twidth:"120",issort:false}
	];
var _shopanlysisT = new TQTable({
	tabletitle:"商户减免券",
	ischeck:false,
	tablename:"shopanlysis_tables",
	dataUrl:"shopticketanlysis.do",
	iscookcol:false,
	buttons:false,
	quikcsearch:coutomsearch(),
	param:"action=query",
	tableObj:T("#shopanlysisobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	allowpage:true
});

function coutomsearch(){
	var html=    "&nbsp;&nbsp;时间：<input id='coutom_btime' value='"+btime+"' style='width:70px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"
				+" - <input id='coutom_etime' value='"+etime+"' style='width:70px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"+
				"&nbsp;&nbsp;&nbsp;&nbsp;类型：<select id ='ttype' name='media' style='width:120px' ><option value='3'>减免券</option><option value='4'>全免券</option></select>&nbsp;&nbsp;&nbsp;&nbsp;<input type='button' onclick='searchdata();' "+
				"value=' 查 询 '/>";//"&nbsp;&nbsp;总计：900.00元";
	return html;
}

function searchdata(){
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;
	ttype = T("#ttype").value;
	_shopanlysisT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&btime="+btime+"&etime="+etime+"&type="+ttype
	})
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
	T("#ttype").value = ttype;
}


function detail(flag,value,id){
	var park =_shopanlysisT.GD(id,"name");
	var ttype = T("#ttype").value;
	var tip = "商户减免券打印记录";
	Twin({
		Id:"shop_detail_"+id,
		Title:tip+"  --> 商户："+park,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='shop_detail_'"+id+" id='shop_detail_'"+id+" src='shopticketanlysis.do?action=detail&shop_id="+id+"&btime="+btime+"&etime="+etime+"&flag="+flag+"&ttype="+ttype+"' width='100%' height='100%' frameborder='0' scrolling='no' style='overflow:hidden;' ></iframe>"
	})
}

_shopanlysisT.C();
</script>
</body>
</html>
