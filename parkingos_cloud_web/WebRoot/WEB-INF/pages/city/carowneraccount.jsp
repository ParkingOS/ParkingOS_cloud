<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>用户账户</title>
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
<div id="accountobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var paytype = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"余额"},{"value_no":1,"value_name":"支付宝"},{"value_no":2,"value_name":"微信"},{"value_no":3,"value_name":"网银"},
{"value_no":4,"value_name":"余额+支付宝"},{"value_no":5,"value_name":"支付宝"},{"value_no":6,"value_name":"余额+网银"},{"value_no":7,"value_name":"停车券"},{"value_no":8,"value_name":"活动奖励"},
{"value_no":9,"value_name":"微信公众号"},{"value_no":10,"value_name":"微信公众号+余额"},{"value_no":11,"value_name":"微信打折券"},{"value_no":12,"value_name":"预支付返款"}];
var target = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"计时停车费"},{"value_no":1,"value_name":"非计时停车费"}];
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"text", twidth:"80" ,issort:false,shide:true},
		{fieldcnname:"交易日期",fieldname:"create_time",inputtype:"date", twidth:"200",issort:false},
		{fieldcnname:"车主账号",fieldname:"uin",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"金额",fieldname:"amount",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"类型",fieldname:"type",inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"充值"},{"value_no":1,"value_name":"消费"}], twidth:"100",issort:false},
		{fieldcnname:"收费员账号",fieldname:"uid",inputtype:"number", twidth:"100",issort:false},
		{fieldcnname:"支付方式",fieldname:"pay_type",inputtype:"select",noList:paytype, twidth:"100",issort:false},
		{fieldcnname:"来源/去向",fieldname:"target",inputtype:"select",noList:target, twidth:"200",issort:false},
		{fieldcnname:"订单号",fieldname:"orderid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
					if(value == "-1"){
						return "";
					}else{
						return value;
					}
				}},
		{fieldcnname:"说明",fieldname:"remark",inputtype:"text", twidth:"500",issort:false}
	];
var _accountT = new TQTable({
	tabletitle:"用户账户记录",
	ischeck:false,
	tablename:"account_tables",
	dataUrl:"cityuseraccount.do",
	iscookcol:false,
	buttons:getAuthButtons(),
	//quikcsearch:coutomsearch(),
	param:"action=query",
	tableObj:T("#accountobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	allowpage:true,
	isoperate:getAuthIsoperateButtons()
});
function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}

function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_accountT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"account_search_w",Title:"搜索账户明细",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "account_search_f",
					formObj:tObj,
					formWinId:"account_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("account_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_accountT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						});
					}
				});	
			}
		});
	
	}});
	
	if(bts.length>0)
		return bts;
	return false;
}

_accountT.C();
</script>

</body>
</html>
