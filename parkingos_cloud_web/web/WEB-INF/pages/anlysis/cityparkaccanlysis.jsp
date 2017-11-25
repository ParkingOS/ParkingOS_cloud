<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>停车场电子收费汇总</title>
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
<body onload='addgroups()'>
<div id="anlysisobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
var btime="${btime}"
var etime="${etime}";
var groups = eval(T.A.sendData("getdata.do?action=getgroups&cityid=${cityid}"));

var _mediaField = [
		{fieldcnname:"车场编号",fieldname:"comid",inputtype:"text", twidth:"100" ,issort:false},
		{fieldcnname:"停车场",fieldname:"company_name",inputtype:"text", twidth:"200",issort:false},
		{fieldcnname:"结算金额",fieldname:"total",inputtype:"text", twidth:"100",issort:false},
		{fieldcnname:"交易数量",fieldname:"scount",inputtype:"text", twidth:"100",issort:false,
			process:function(value,cid,id){
				return "<a href=# onclick=\"mpaydetail('hn','"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
			}}
		];
var _anlysisT = new TQTable({
	tabletitle:"车场电子收费记录",
	ischeck:false,
	tablename:"anlysis_tables",
	dataUrl:"cityparkaccanlysis.do",
	iscookcol:false,
	buttons:false,
	quikcsearch:coutomsearch(),
	param:"action=query",
	tableObj:T("#anlysisobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	allowpage:false,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
	var html = "&nbsp;&nbsp; 时间：<input id='coutom_btime' value='"+btime+"' style='width:70px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"
				+" - <input id='coutom_etime' value='"+etime+"' style='width:70px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"+
				"&nbsp;&nbsp;&nbsp;&nbsp;所属集团：&nbsp;&nbsp;<select style='width:130px' id='groups'></select>&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
				//+"&nbsp;&nbsp;&nbsp;&nbsp;总单量：<span style='color:red;' id='count'>"+${count}+"</span>";
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
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;
	var groupid = T("#groups").value;
	_anlysisT.C({
		cpage:1,
		tabletitle:"搜索结果 ",
		extparam:"&action=query&btime="+btime+"&etime="+etime+"&groupid="+groupid
	});
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
	//gettotal(btime,etime);
	addgroups();
	T("#groups").value = groupid;
}

function gettotal(btime,etime){
	jQuery.ajax({
		type:"post",
		url:"cityparkaccanlysis.do?action=gettotal",
		data:{'btime':btime,'etime':etime},
	    async:false,
	    success:function(result){
	    	var countobj = $("#count");
	    	var count = eval("("+result+")");
	    	countobj[0].innerHTML = count.count;
	      }
	});
}

function mpaydetail(type,value,id){
	var park =_anlysisT.GD(id,"company_name");
	var tip = "车场电子收费记录";
	Twin({
		Id:"mobile_detail_"+id,
		Title:tip+"  --> 停车场："+park,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='mobile_detail_'"+id+" id='mobile_detail_'"+id+" src='cityparkaccanlysis.do?action=tompaydetail&parkid="+id+"&btime="+btime+"&etime="+etime+"' width='100%' height='100%' frameborder='0' scrolling='no' style='overflow:hidden;' ></iframe>"
	});
}

function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}
_anlysisT.C();
</script>
</body>
</html>
