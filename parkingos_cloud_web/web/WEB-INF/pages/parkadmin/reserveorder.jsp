<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>预约订单</title>
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
<div id="reserveorderobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
var role=${role};
/*权限*/
var issupperadmin=${supperadmin};
var isadmin = ${isadmin};
var authlist ="";
if((issupperadmin&&issupperadmin==1) || (isadmin&&isadmin==1))
	authlist="0,1,2";
else
	authlist= T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var stateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"欠费"},{"value_no":1,"value_name":"已补缴"},{"value_no":2,"value_name":"未入场"},{"value_no":3,"value_name":"已取消"}];
var payList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"账户支付"},{"value_no":1,"value_name":"现金支付"},
                {"value_no":2,"value_name":"电子支付"},{"value_no":3,"value_name":"包月"},
                {"value_no":4,"value_name":"现金预支付"},{"value_no":5,"value_name":"包月"},
                {"value_no":6,"value_name":"商家卡"},{"value_no":7,"value_name":"免费放行"}];
var typeList= [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"车位预约"},{"value_no":1,"value_name":"充电桩预约"}];
/*权限*/
var comid = ${comid};
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"订单编号",fieldname:"order_id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"车主编号",fieldname:"uin",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"预约时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"预计到达时间",fieldname:"arrive_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"最晚到达时间",fieldname:"limit_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"支付时间",fieldname:"prepaid_pay_time",fieldvalue:'',inputtype:"date", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车牌号",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select", twidth:"100" ,noList:stateList,height:"",issort:false},
		{fieldcnname:"电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"支付类型",fieldname:"pay_type",fieldvalue:'',inputtype:"select", twidth:"100" ,noList:payList,height:"",issort:false},
		{fieldcnname:"预约类型",fieldname:"type",fieldvalue:'',inputtype:"select", twidth:"100" ,noList:typeList,height:"",issort:false},
	    {fieldcnname:"预付金额",fieldname:"prepaid",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false}

	];
var _reserveorderT = new TQTable({
	tabletitle:"预约订单",
	ischeck:false,
	tablename:"reserveorder_tables",
	dataUrl:"reserveorder.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&comid="+comid,
	tableObj:T("#reserveorderobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
//查看,添加,编辑,删除
	var bts =[];
	if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_reserveorderT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"reserveorder_search_w",Title:"搜索车位",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "reserveorder_search_f",
					formObj:tObj,
					formWinId:"reserveorder_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("planmember_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_reserveorderT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&comid="+comid+"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	return bts;
}
function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}

_reserveorderT.C();
</script>

</body>
</html>
