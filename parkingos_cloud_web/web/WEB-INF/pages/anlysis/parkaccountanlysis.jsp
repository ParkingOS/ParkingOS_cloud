<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>停车宝账户</title>
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
<div id="parkaccountanlysisobj" style="width:100%;height:100%;margin:0px;"></div>
<form action="" method="post" id="choosecom"></form>
<script >

/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false];
//查询，提现申请
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/
var comid = ${comid};
var groupid = "${groupid}";
var btime="${btime}"
var etime="${etime}";
var type =[["-1","请选择"],["0","充值"],["1","提现"],["2","返现"]];
var source =[["-1","请选择"],["0","停车费"],["1","返现"],["2","泊车费"]];
function setSelects(data){
	var d = [];
	for(var i=0;i<data.length;i++){
		d.push({"value_no":data[i][0],"value_name":data[i][1]});
	}
	return d;
}
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"text", twidth:"200" ,issort:false,fhide:true,shide:true},
		{fieldcnname:"交易日期",fieldname:"create_time",inputtype:"date", twidth:"140",issort:false},
		{fieldcnname:"停车场",fieldname:"comid",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"金额",fieldname:"amount",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"类型",fieldname:"type",inputtype:"select",noList:setSelects(type), twidth:"100",issort:false},
		{fieldcnname:"来源/去向",fieldname:"source",inputtype:"select",noList:setSelects(source), twidth:"100",issort:false},
		{fieldcnname:"说明",fieldname:"remark",inputtype:"text", twidth:"500",issort:false}
	];
var _parkaccountanlysisT = new TQTable({
	tabletitle:"停车宝交易记录",
	ischeck:false,
	tablename:"parkaccountanlysis_tables",
	dataUrl:"parkaccountaly.do",
	iscookcol:false,
	buttons:getAuthButtons(),
	quikcsearch:coutomsearch(),
	param:"action=query&isparkcloud=${isparkcloud}&comid="+comid,
	tableObj:T("#parkaccountanlysisobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	allowpage:true,
	isoperate:getAuthIsoperateButtons()
});
function coutomsearch(){
	var html = "";
	if(groupid != ""){
		html = "&nbsp;&nbsp;&nbsp;&nbsp;车场:&nbsp;&nbsp;<select id='companys' onchange='searchdata();' ></select>";
	}
	return html;
}

function searchdata(){
	comid = T("#companys").value;
	T("#choosecom").action="parkaccountaly.do?comid="+comid+"&authid=${authid}&r"+Math.random();
	T("#choosecom").submit(); 
}
function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}

function getAuthButtons(){
	if(subauth[0])
		return [
		{dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
			T.each(_parkaccountanlysisT.tc.tableitems,function(o,j){
				o.fieldvalue ="";
			});
			Twin({Id:"paccount_search_w",Title:"搜索交易记录",Width:550,sysfun:function(tObj){
					TSform ({
						formname: "paccount_search_f",
						formObj:tObj,
						formWinId:"paccount_search_w",
						formFunId:tObj,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_mediaField}]
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("paccount_search_w");} }
						],
						SubAction:
						function(callback,formName){
							_parkaccountanlysisT.C({
								cpage:1,
								tabletitle:"高级搜索结果",
								extparam:"&comid="+comid+"&action=query&"+Serializ(formName)
							})
							addcoms();
						}
					});	
				}
			})
		
		}}]
	else 
		return [];
}

_parkaccountanlysisT.C();
function addcoms(){
	if(groupid != ""){
		var childs = eval(T.A.sendData("getdata.do?action=getcoms&groupid=${groupid}"));
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
if(groupid != ""){//集团管理员登录下显示车场列表
	addcoms();
}
</script>

</body>
</html>
