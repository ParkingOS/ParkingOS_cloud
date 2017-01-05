<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>订单管理</title>
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
<div id="orderobj" style="width:100%;height:100%;margin:0px;"></div>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<form action="" method="post" id="choosecom"></form>
<script language="javascript">
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/
var comid = ${comid};
var groupid = "${groupid}";
var collectors = eval(T.A.sendData("order.do?action=getalluser"));
//function addcollectors(){
//	var childs = eval(T.A.sendData("cityorder.do?action=getcollectors"));
//	jQuery("#collectors").empty();
//	for(var i=0;i<childs.length;i++){
//		var child = childs[i];
//		var id = child.value_no;
//		var name = child.value_name;
//		collectors.append("<option value='"+id+"'>"+name+"</option>");
//	}
//}
var freereasons = eval(T.A.sendData("getdata.do?action=getfreereasons&id=${comid}"));
var pass = eval(T.A.sendData("getdata.do?action=getcompassbygroupid&id="+groupid));

var states = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"未支付"},{"value_no":1,"value_name":"已支付"},{"value_no":2,"value_name":"逃单"}];
var parks = eval(T.A.sendData("getdata.do?action=getparksbygroup&id="+groupid));
var paytype=[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}];
var _mediaField = [
	{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
	{fieldcnname:"车场名称",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,twidth:"150",target:"in_passid,out_passid,freereasons",action:"getcompass,getcompass,getfreereasons",twidth:"100" ,height:"",issort:false},
	{fieldcnname:"收款人账号",fieldname:"uid",fieldvalue:'',inputtype:"text",twidth:"80" ,height:"",issort:false},
	{fieldcnname:"收款人名称",fieldname:"uidname",fieldvalue:'',inputtype:"select",noList:collectors,twidth:"100" ,height:"",issort:false,shide:true},
	{fieldcnname:"进场方式",fieldname:"c_type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"NFC刷卡"},{"value_no":1,"value_name":"Ibeacon"},{"value_no":2,"value_name":"手机扫牌"},{"value_no":3,"value_name":"通道扫牌"},{"value_no":4,"value_name":"直付"},{"value_no":5,"value_name":"全天月卡"},{"value_no":6,"value_name":"车位二维码"},{"value_no":7,"value_name":"月卡第二辆车"},{"value_no":8,"value_name":"分段月卡"}] ,twidth:"100" ,height:"",issort:true},
	{fieldcnname:"车牌号",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:true},
	{fieldcnname:"泊位号",fieldname:"berthnumber",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"160" ,height:"",issort:false,
		process:function(value,pid){
			return setcname(value,pid,'berthnumber');
		}},
	{fieldcnname:"进场时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",hide:true},
	{fieldcnname:"出场时间",fieldname:"end_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:true},
	{fieldcnname:"时长",fieldname:"duration",fieldvalue:'',inputtype:"text", twidth:"140" ,height:"",issort:true,shide:true},
	{fieldcnname:"支付方式",fieldname:"pay_type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"账户支付"},{"value_no":1,"value_name":"现金支付"},{"value_no":2,"value_name":"手机支付"},{"value_no":3,"value_name":"包月"},{"value_no":4,"value_name":"中央预支付现金"},{"value_no":5,"value_name":"中央预支付银联卡"},{"value_no":6,"value_name":"中央预支付商家卡"},{"value_no":8,"value_name":"免费"},{"value_no":9,"value_name":"刷卡"}] ,twidth:"80" ,height:"",issort:true},
	{fieldcnname:"免费原因",fieldname:"freereasons",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"160" ,height:"",issort:false,
		process:function(value,pid){
			return setcname(value,pid,'freereasons');
		}},
	{fieldcnname:"金额",fieldname:"total",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
	{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList: states,twidth:"100" ,height:"",issort:true,shide:true},
	{fieldcnname:"结算方式",fieldname:"isclick",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}] ,twidth:"100" ,height:"",issort:true},
	{fieldcnname:"查看车辆图片",fieldname:"id",inputtype:"text", twidth:"100",issort:false,shide:true,
		process:function(value,cid,id){
			return "<a href=# onclick=\"viewdetail('"+value+"','"+cid+"')\" style='color:blue'>查看车辆图片</a>";
		}},
	{fieldcnname:"进场通道",fieldname:"in_passid",fieldvalue:'',inputtype:"select",noList:pass,action:"",twidth:"160" ,height:"",issort:false},
	{fieldcnname:"出场通道",fieldname:"out_passid",fieldvalue:'',inputtype:"select",noList:pass,action:"",twidth:"160" ,height:"",issort:false}
];
/*var _excelField = [
		{fieldcnname:"进场方式",fieldname:"c_type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"NFC刷卡"},{"value_no":1,"value_name":"Ibeacon"},{"value_no":2,"value_name":"手机扫牌"},{"value_no":3,"value_name":"通道扫牌"},{"value_no":4,"value_name":"直付"},{"value_no":5,"value_name":"月卡"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"进场时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"200" ,height:"",hide:true},
		{fieldcnname:"出场时间",fieldname:"end_time",fieldvalue:'',inputtype:"date", twidth:"200" ,height:"",issort:true},
		{fieldcnname:"支付方式",fieldname:"pay_type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"账户支付"},{"value_no":1,"value_name":"现金支付"},{"value_no":2,"value_name":"手机支付"},{"value_no":3,"value_name":"包月"},{"value_no":4,"value_name":"中央预支付现金"},{"value_no":5,"value_name":"中央预支付银联卡"},{"value_no":6,"value_name":"中央预支付商家卡"},{"value_no":8,"value_name":"免费"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"金额",fieldname:"total",fieldvalue:'',inputtype:"number", height:"",issort:false},
		{fieldcnname:"收款人",fieldname:"uid",fieldvalue:'',inputtype:"select", noList:collectors, twidth:"100" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"未支付"},{"value_no":1,"value_name":"已支付"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"结算方式",fieldname:"isclick",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"系统结算"},{"value_no":1,"value_name":"手动结算"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"进场通道",fieldname:"in_passid",fieldvalue:'',inputtype:"select",noList:pass, twidth:"100" ,height:"",issort:false},
		{fieldcnname:"出场通道",fieldname:"out_passid",fieldvalue:'',inputtype:"select",noList:pass, twidth:"100" ,height:"",issort:false}
	];*/
var _excelField = [
		{fieldcnname:"进场时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"200" ,height:"",hide:true},
		{fieldcnname:"金额",fieldname:"total",fieldvalue:'',inputtype:"number", height:"",issort:false},
		{fieldcnname:"结算方式",fieldname:"isclick",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}] ,twidth:"100" ,height:"",issort:false}
	];

var _orderT = new TQTable({
	tabletitle:"订单管理&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${parkinfo}",
	//ischeck:false,
	tablename:"order_tables",
	dataUrl:"grouporder.do",
	iscookcol:false,
	//dbuttons:false,
	quikcsearch:coutomsearch(),
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&groupid="+groupid,
	tableObj:T("#orderobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function getSelectValue(valuse){
	var m = "";
	for(var a=0;a<valuse.length;a++){
		m +="<option value='"+valuse[a].value_no+"'>"+valuse[a].value_name+"</option>";
	}
	return m;
}

function coutomsearch(){
	var html=  "<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp&nbsp;结算方式 <select id ='isclick' name='isclick' style='width:120px;vertical-align:middle;' onchange=searchdata(this); >"+getSelectValue(paytype)+"</select></div>";
	html +=    "<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp&nbsp;收款人 <select id ='uid' name='uid' style='width:120px;vertical-align:middle;' onchange=searchdata(this); >"+getSelectValue(collectors)+"</select></div>";

	if(groupid != ""){
		html += "<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp;&nbsp;&nbsp;&nbsp;车场:&nbsp;&nbsp;<select id='companys' onchange='searchcom();'></select></div>";
	}
	return html;
}

function searchdatabycom(obj){
	var comid = T("#companys").value;
	var value =obj.value;
	var extp ='comid='+comid;
	if(comid==-2){
		extp+='&groupid='+groupid;
	}
	extp+='&fieldsstr=id__c_type__car_number__create_time__end_time__duration__pay_type__total__uid__state__isclick__id__in_passid__out_passid';
	_orderT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&"+extp
	})
	setSelectValue();
	addcoms();
	T("#companys").value = comid;
}
var isclickValue="";
var uidValue="";
var comValue="-2";
function searchdata(obj){
	var oid = obj.id;
	var value =obj.value;
	var extp = oid+"_start="+value;
	if(oid=='isclick'&&value=='0'){//自动结算过滤掉未结算的订单
		extp+='&state_start=1';
	}
	if(oid=='isclick'){
		if(uidValue!='')
			extp +="&uid_start="+uidValue;
		isclickValue =value;
	}else if(oid='uid'){
		if(isclickValue!=''){
			extp +="&isclick_start="+isclickValue;
			if(isclickValue=='0'){//自动结算过滤掉未结算的订单
				extp+='&state_start=1';
			}
		}
		uidValue=value;
	}
	if(groupid != ""){
		comid = T("#companys").value;
		extp += "&comid="+comid;
	}
	_orderT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&"+extp
	})
	setSelectValue();
	addcoms();
	T("#companys").value = comValue;
}
function searchcom(){
	comid = T("#companys").value;
	var extp = "comid="+comid+"&authid=${authid}&r"+Math.random();
	comId = comid;
	T("#companys").value =comid;
	_orderT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&"+extp
	})
	addcoms();
	T("#companys").value = comid;
}
function setSelectValue(){
	var isclickSelect = T("#isclick");
	var uidSelect = T("#uid");
	for(var i=0;i<isclickSelect.options.length;i++){
		if(isclickSelect.options[i].value==isclickValue)
			isclickSelect.options[i].selected = true;
	}
	for(var i=0;i<uidSelect.options.length;i++){
		if(uidSelect.options[i].value==uidValue)
			uidSelect.options[i].selected = true;
	}
}
function getAuthButtons(){
	var authButs = [];
	if(subauth[0])
		authButs.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_orderT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"order_search_w",Title:"搜索订单",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "order_search_f",
					formObj:tObj,
					formWinId:"order_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("order_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_orderT.C({
							cpage:1,
							tabletitle:"高级搜索结果&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${parkinfo}",
							extparam:"&groupid="+groupid+"&action=query&"+Serializ(formName)
						})
						addcoms();
					}
				});
			}
		})

	}});

	/**authButs.push({dname:"查看自动结算",icon:"toxls.gif",onpress:function(Obj){
		_orderT.C({
			cpage:1,
			tabletitle:"订单管理-->自动结算",
			extparam:"&comid="+comid+"&action=query&otype=0"
		})
		addcoms();
		}});
	authButs.push({dname:"查看手动结算",icon:"toxls.gif",onpress:function(Obj){
		_orderT.C({
			cpage:1,
			tabletitle:"订单管理-->手动结算",
			extparam:"&comid="+comid+"&action=query&otype=1"
		})
		addcoms();
		}});
	authButs.push({dname:"查看全部订单",icon:"toxls.gif",onpress:function(Obj){
		_orderT.C({
			cpage:1,
			tabletitle:"订单管理-->全部订单",
			extparam:"&comid="+comid+"&action=query"
		})
		addcoms();
		}});**/
	if(subauth[1])
		authButs.push({dname:"导出订单",icon:"toxls.gif",onpress:function(Obj){
		Twin({Id:"order_export_w",Title:"导出订单<font style='color:red;'>（如果没有设置，默认全部导出!）</font>",Width:480,sysfun:function(tObj){
				 TSform ({
					formname: "order_export_f",
					formObj:tObj,
					formWinId:"order_export_w",
					formFunId:tObj,
					dbuttonname:["确认导出"],
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_excelField}],
					}],
					//formitems:[{kindname:"",kinditemts:_excelField}],
					SubAction:
					function(callback,formName){
						T("#exportiframe").src="order.do?action=exportExcel&comid="+comid+"&rp="+2147483647+"&fieldsstr="+"id__c_type__car_number__create_time__end_time__duration__pay_type__total__uid__state__isclick__id__in_passid__out_passid&"+Serializ(formName)
						TwinC("order_export_w");
						T.loadTip(1,"正在导出，请稍候...",2,"");
					}
				});
			}
		})
	}})
	if(subauth[2])
		authButs.push({dname:"0元结算",icon:"toxls.gif",onpress:function(Obj){
		var sids = _orderT.GS();
		//var a = _orderT.GSByField("c_type");
		var ids="";
		if(!sids){
			T.loadTip(1,"请先选择订单",2,"");
			return;
		}
		Tconfirm({Title:"确认0元结算订单吗",Content:"确认0元结算所选订单吗",OKFn:function(){
			T.A.sendData("order.do?action=completezeroorder&comid="+comid,"post","ids="+sids,
		function complete(ret){
			T.loadTip(1,"结算成功"+ret+"条",2,"");
			location.reload();
		}
		)}})
	}})


	return authButs;
}
function getAuthIsoperateButtons(){
	var bts = [];

	if(bts.length <= 0){return false;}
	return bts;
}

function viewdetail(value,id){
	var car_number =_orderT.GD(id,"car_number");
	var tip = "车辆图片";
	Twin({
		Id:"carpics_detail_"+id,
		Title:tip+"  --> 车牌："+car_number,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='carpics_detail_'"+id+" id='carpics_detail_'"+id+" src='order.do?action=carpics&orderid="+id+"&comid="+comid+"&r="+Math.random()+"' width='100%' height='"+(T.gwh()-100)+"' frameborder='0' style='overflow:auto;' ></iframe>"
	})
}
_orderT.C();

function addcoms(){
	if(groupid != ""){
		var childs = eval(T.A.sendData("getdata.do?action=getcoms&groupid=${groupid}"));
		jQuery("#companys").empty();
		jQuery("#companys").append("<option value='-2'>全部</option>");
		for(var i=0;i<childs.length;i++){
			var child = childs[i];
			var id = child.value_no;
			var name = child.value_name;
			jQuery("#companys").append("<option value='"+id+"'>"+name+"</option>");
		}
		T("#companys").value = -2;
	}
}
function setcname(value,pid,colname){
	var url = "";
	if(colname == "uid"){
		url = "cityorder.do?action=getcollname&id="+value;
	}else if(colname == "freereasons"){
		url = "cityorder.do?action=getfreereason&id="+value;
	}else if(colname == "in_passid" || colname == "out_passid"){
		url = "cityorder.do?action=getpassname&id="+value;
	}else if(colname == "berthnumber"){
		url = "grouporder.do?action=getcid&id="+value;
	}
	if(value&&value!='-1'&&value!=''){
		T.A.C({
			url:url,
			method:"GET",//POST or GET
			param:"",//GET时为空
			async:false,//为空时根据是否有回调函数(success)判断
			dataType:"0",//0text,1xml,2obj
			success:function(ret,tipObj,thirdParam){
				if(ret){
					updateRow(pid,colname,ret);
				}
				else
					updateRow(pid,colname,value);
			},//请求成功回调function(ret,tipObj,thirdParam) ret结果
			failure:function(ret,tipObj,thirdParam){
				return false;
			},//请求失败回调function(null,tipObj,thirdParam) 默认提示用户<网络失败>
			thirdParam:"",//回调函数中的第三方参数
			tipObj:null,//相关提示父级容器(值为字符串"notip"时表示不进行相关提示)
			waitTip:"正在获取名称...",
			noCover:true
		})
	}else{
		return "无"
	};
	return "<font style='color:#666'>获取中...</font>";
}
/*更新表格内容*/
function updateRow(rowid,name,value){
	//alert(value);
	if(value)
		_orderT.UCD(rowid,name,value);
}
if(groupid != ""){//集团管理员登录下显示车场列表
	addcoms();
}
</script>

</body>
</html>
