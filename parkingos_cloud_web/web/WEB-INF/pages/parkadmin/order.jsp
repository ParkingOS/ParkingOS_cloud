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
<script src="js/tq.datatable.js?2ee22s2" type="text/javascript">//表格</script>
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
var freereasons = eval(T.A.sendData("getdata.do?action=getfreereasons&id=${comid}"));
function getpass (){
	var pass = eval(T.A.sendData("getdata.do?action=getcompass&id=${comid}"));
	return pass;
}
var paytype=[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}];
var pass = getpass();
var cartypes = eval(T.A.sendData("getdata.do?action=getcartype&id=${comid}"));
//,noList:[{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"通道扫牌"},{"value_no":"1","value_name":"Ibeacon"},{"value_no":"2","value_name":"手机扫牌"},{"value_no":"3","value_name":"通道扫牌"},{"value_no":"4","value_name":"直付"},{"value_no":"5","value_name":"全天月卡"},{"value_no":"6","value_name":"车位二维码"},{"value_no":"7","value_name":"月卡第二辆车"},{"value_no":"8","value_name":"分段月卡"}] 
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",hide:true},
		{fieldcnname:"进场方式",fieldname:"c_type",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:true},
		{fieldcnname:"车牌号",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:true},
		{fieldcnname:"车型",fieldname:"car_type",fieldvalue:'',inputtype:"select",noList:cartypes, twidth:"100" ,height:"",issort:true},
		{fieldcnname:"进场时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",hide:true},
		{fieldcnname:"出场时间",fieldname:"end_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:true},
		{fieldcnname:"时长",fieldname:"duration",fieldvalue:'',inputtype:"text", twidth:"140" ,height:"",issort:true},
		{fieldcnname:"支付方式",fieldname:"pay_type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"账户支付"},{"value_no":1,"value_name":"现金支付"},{"value_no":2,"value_name":"手机支付"},{"value_no":3,"value_name":"包月"},{"value_no":4,"value_name":"中央预支付现金"},{"value_no":5,"value_name":"中央预支付银联卡"},{"value_no":6,"value_name":"中央预支付商家卡"},{"value_no":8,"value_name":"免费"},{"value_no":9,"value_name":"刷卡"}] ,twidth:"80" ,height:"",issort:true},
		{fieldcnname:"优惠原因",fieldname:"freereasons",fieldvalue:'',inputtype:"select",noList:freereasons ,twidth:"80" ,height:""},
		{fieldcnname:"应收金额",fieldname:"amount_receivable",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
		{fieldcnname:"实收金额",fieldname:"total",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
		{fieldcnname:"电子预付金额",fieldname:"electronic_prepay",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
		{fieldcnname:"现金预付金额",fieldname:"cash_prepay",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
		{fieldcnname:"电子结算金额",fieldname:"electronic_pay",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
		{fieldcnname:"现金结算金额",fieldname:"cash_pay",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
		{fieldcnname:"减免金额",fieldname:"reduce_amount",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
		{fieldcnname:"入场收费员",fieldname:"uid",fieldvalue:'',inputtype:"select", noList:collectors, twidth:"100" ,height:"",issort:false,issort:true},
		{fieldcnname:"收款人",fieldname:"out_uid",fieldvalue:'',inputtype:"select", noList:collectors, twidth:"100" ,height:"",issort:false,issort:true},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},
            {"value_no":0,"value_name":"未结算"},{"value_no":1,"value_name":"已结算"},{"value_no":2,"value_name":"逃单"}] ,twidth:"100" ,height:"",issort:true},
		//{fieldcnname:"结算方式",fieldname:"isclick",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"无"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}] ,twidth:"100" ,height:"",issort:true},
		{fieldcnname:"查看车辆图片",fieldname:"url",inputtype:"text", twidth:"100",issort:false
			,process:function(value,cid,id){
				return "<a href=# onclick=\"viewdetail('hn','"+value+"','"+cid+"')\" style='color:blue'>查看车辆图片</a>";
			}},
		{fieldcnname:"进场通道",fieldname:"in_passid",fieldvalue:'',inputtype:"select",noList:pass, twidth:"100" ,height:"",issort:true},
		{fieldcnname:"出场通道",fieldname:"out_passid",fieldvalue:'',inputtype:"select",noList:pass, twidth:"100" ,height:"",issort:true},
		//添加岗亭/工作站信息
		//{fieldcnname:"岗亭/工作站",fieldname:"work_station_uuid",fieldvalue:'',inputtype:"select", noList:[{"value_no":-1,"value_name":"无"}],twidth:"140" ,height:"",issort:true},
		/*添加车场收费系统的订单编号*/
		{fieldcnname:"车场订单编号",fieldname:"order_id_local",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",hidden:true}
	];
var _searchField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",hide:true},
		{fieldcnname:"进场方式",fieldname:"c_type",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:true},
		{fieldcnname:"车牌号",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"进场时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",hide:true},
		{fieldcnname:"出场时间",fieldname:"end_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:true},
		{fieldcnname:"支付方式",fieldname:"pay_type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":1,"value_name":"现金支付"},{"value_no":2,"value_name":"手机支付"},{"value_no":3,"value_name":"包月"},{"value_no":8,"value_name":"免费"},{"value_no":9,"value_name":"刷卡"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"金额",fieldname:"total",fieldvalue:'',inputtype:"number", height:"",issort:false},
		{fieldcnname:"收款人",fieldname:"uid",fieldvalue:'',inputtype:"select", noList:collectors, twidth:"100" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"未结算"},{"value_no":1,"value_name":"已结算"},{"value_no":2,"value_name":"逃单"}] ,twidth:"100" ,height:"",issort:false},
		//{fieldcnname:"结算方式",fieldname:"isclick",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}] ,twidth:"100" ,height:"",issort:false},
		//{fieldcnname:"免费原因",fieldname:"freereasons",fieldvalue:'',inputtype:"select",noList:freereasons ,twidth:"80" ,height:"",issort:false},
		{fieldcnname:"查看车辆图片",fieldname:"id",inputtype:"text", twidth:"100",issort:false,
			process:function(value,cid,id){
				return "<a href=# onclick=\"viewdetail('hn','"+value+"','"+cid+"')\" style='color:blue'>查看车辆图片</a>";
			}},
		{fieldcnname:"进场通道",fieldname:"in_passid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"出场通道",fieldname:"out_passid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		/*添加车场收费系统的订单编号*/
		{fieldcnname:"车场订单编号",fieldname:"order_id_local",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",hidden:true}
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
		{fieldcnname:"金额",fieldname:"total",fieldvalue:'',inputtype:"number", height:"",issort:false}
		//{fieldcnname:"结算方式",fieldname:"isclick",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}] ,twidth:"100" ,height:"",issort:false}
	];

var _orderT = new TQTable({
	tabletitle:"订单管理&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${parkinfo}",
	//ischeck:false,
	tablename:"order_tables",
	dataUrl:"order.do",
	iscookcol:false,
	//dbuttons:false,
	quikcsearch:coutomsearch(),
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&comid="+comid,
	tableObj:T("#orderobj"),
	fit:[true,true,true],
	isidentifier:false,
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
	var html= "";
		//"<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp&nbsp;结算方式 <select id ='isclick' name='isclick' style='width:120px;vertical-align:middle;' onchange=searchdata(this); >"+getSelectValue(paytype)+"</select></div>";
	html +=    "<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp&nbsp;收款人 <select id ='out_uid' name='out_uid' style='width:120px;vertical-align:middle;' onchange=searchdata(this); >"+getSelectValue(collectors)+"</select></div>";
	
	if(groupid != ""){
		html += "<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp;&nbsp;&nbsp;&nbsp;车场:&nbsp;&nbsp;<select id='companys' onchange='searchcom();'></select></div>";
	}
	return html;
}

function searchcom(){
	comid = T("#companys").value;
	T("#choosecom").action="order.do?comid="+comid+"&authid=${authid}&r"+Math.random();
	T("#choosecom").submit(); 
}

var isclickValue="";
var uidValue="";
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
    addcoms();
    setSelectValue();
}
function setSelectValue(){
	var uidSelect = document.getElementById('out_uid');
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
						formitems:[{kindname:"",kinditemts:_searchField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("order_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_orderT.C({
							cpage:1,
							tabletitle:"高级搜索结果&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${parkinfo}",
							extparam:"&comid="+comid+"&action=query&"+Serializ(formName)
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

function viewdetail(type,value,id){
	var car_number =_orderT.GD(id,"car_number");
	var orderIdLocal =_orderT.GD(id,"order_id_local");
	var tip = "车辆图片";
	Twin({
		Id:"carpics_detail_"+id,
		Title:tip+"  --> 车牌："+car_number,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		/*修改图片注释原来调用逻辑*/
		/* Content:"<iframe name='carpics_detail_'"+id+" id='carpics_detail_'"+id+" src='order.do?action=carpics&orderid="+id+"&comid="+comid+"&r="+Math.random()+"' width='100%' height='"+(T.gwh()-100)+"' frameborder='0' style='overflow:auto;' ></iframe>" */
		Content:"<iframe name='carpics_detail_'"+id+" id='carpics_detail_'"+id+" src='order.do?action=carpicsnew&orderid="+orderIdLocal+"&comid="+comid+"&r="+Math.random()+"' width='100%' height='"+(T.gwh()-100)+"' frameborder='0' style='overflow:auto;' ></iframe>"
	})
}
_orderT.C();

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
