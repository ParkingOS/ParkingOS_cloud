<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>停车场账户</title>
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
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
//查看,添加,编辑,删除
/*权限*/
var states = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"等待审核"},{"value_no":2,"value_name":"已审核"},{"value_no":3,"value_name":"已到帐"},{"value_no":4,"value_name":"提现失败"}];
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number",twidth:"50" ,height:"",edit:false,issort:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"收费员",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"金额",fieldname:"amount",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"类型",fieldname:"type",fieldvalue:'',inputtype:"select", noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"充值"},{"value_no":1,"value_name":"支出"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"来源/去向",fieldname:"source",fieldvalue:'',inputtype:"select", noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"停车费"},{"value_no":5,"value_name":"车场提现"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"订单号",fieldname:"orderid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
						if(value == "-1"){
							return "";
						}else{
							return value;
						}
				}},
		{fieldcnname:"提现状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:states, twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
				return setname(value,states);
			}},
		{fieldcnname:"记录日期",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false}
	];
var _accountT = new TQTable({
	tabletitle:"停车场账户",
	ischeck:false,
	tablename:"account_tables",
	dataUrl:"cityparkaccount.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#accountobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
//查看,添加,编辑,删除
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
						})
					}
				});	
			}
		})
	
	}});
	
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}

function setname(value, list){
	if(value != "" && value != "-1"){
		for(var i=0; i<list.length;i++){
			var o = list[i];
			var key = o.value_no;
			var v = o.value_name;
			if(value == key){
				return v;
			}
		}
	}else{
		return "";
	}
}

_accountT.C();
</script>

</body>
</html>
