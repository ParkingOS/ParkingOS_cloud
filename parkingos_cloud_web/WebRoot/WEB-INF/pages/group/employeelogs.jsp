<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>收费员日志</title>
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
<script src="js/echarts/echarts.js"></script>
<style type="text/css">
	body{
		overflow:auto 
	}
</style>

</head>
<body>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<div id="employeelogsobj" style="width:100%;height:100%;margin:0px;"></div>

<script type="text/javascript" >   
var btime="${btime}";
var etime="${etime}";
var parks =eval(T.A.sendData("getdata.do?action=getparksbygroup&id=${groupid}"));
var users=eval(T.A.sendData("getdata.do?action=getcollectbygroupid&groupid=${groupid}"));
var states = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"签入"},{"value_no":1,"value_name":"签退"}];
var logon_state = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"正常"},{"value_no":1,"value_name":"迟到"}];
var logoff_state = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"正常"},{"value_no":1,"value_name":"早退"}];
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"number",twidth:"100" ,issort:false,fhide:true},
		{fieldcnname:"收费员",fieldname:"nickname",inputtype:"select",noList:users, twidth:"100" ,issort:false},
		{fieldcnname:"账号",fieldname:"uid",inputtype:"number", twidth:"200",issort:false},
		{fieldcnname:"停车场",fieldname:"comid",inputtype:"select",noList:parks, twidth:"100",issort:false},
		{fieldcnname:"泊位段名称",fieldname:"berthsec_name",inputtype:"text", twidth:"100",issort:false,shide:true},
		{fieldcnname:"状态",fieldname:"state",inputtype:"select",noList:states, twidth:"50" ,issort:false},
		{fieldcnname:"签入日期",fieldname:"start_time",inputtype:"date", twidth:"200",issort:false},
		{fieldcnname:"签入状态",fieldname:"logon_state",inputtype:"select",noList:logon_state, twidth:"80" ,issort:false,
			process:function(value,trId,colId){
				if(value==1)
					return "<font color='red'>迟到</font>";
				else 
					return "正常";
			}},
		{fieldcnname:"签出日期",fieldname:"end_time",inputtype:"date", twidth:"200",issort:false},
		{fieldcnname:"签出状态",fieldname:"logoff_state",inputtype:"select",noList:logoff_state, twidth:"80" ,issort:false,
			process:function(value,trId,colId){
				if(value==1)
					return "<font color='red'>早退</font>";
				else 
					return "正常";
			}},
		{fieldcnname:"设备编号",fieldname:"device_code",inputtype:"text", twidth:"100",issort:false}
		];
var _exportField = [
		{fieldcnname:"账号",fieldname:"uid",inputtype:"number", twidth:"200",issort:false},
		{fieldcnname:"签入日期",fieldname:"start_time",inputtype:"date", twidth:"200",issort:false},
		{fieldcnname:"签出日期",fieldname:"end_time",inputtype:"date", twidth:"200",issort:false},
		{fieldcnname:"设备编号",fieldname:"device_code",inputtype:"text", twidth:"100",issort:false}
		];
var _employeelogsT = new TQTable({
	tabletitle:"收费员日志",
	ischeck:false,
	tablename:"employeelogs_tables",
	dataUrl:"employeelogs.do",
	iscookcol:false,  
	buttons:getAuthButtons(),
	//quikcsearch:coutomsearch(),
	param:"action=query&btime=${btime}&etime=${etime}",
	tableObj:T("#employeelogsobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	allowpage:true,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_employeelogsT.tc.tableitems,function(o,j){
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
						_employeelogsT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						});
					}
				});	
			}
		});
	
	}});
		bts.push({dname:"导出报表",icon:"toxls.gif",onpress:function(Obj){
			Twin({Id:"parklogs_export_w",Title:"导出报表<font style='color:red;'>（如果没有设置，默认全部导出!）</font>",Width:480,sysfun:function(tObj){
					 TSform ({
						formname: "parklogs_export_f",
						formObj:tObj,
						formWinId:"parklogs_export_w",
						formFunId:tObj,
						dbuttonname:["确认导出"],
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_exportField}],
						}],
						SubAction:
						function(callback,formName){
							T("#exportiframe").src="employeelogs.do?action=export&fieldsstr=uid__start_time__end_time__device_code&"+Serializ(formName)
							TwinC("parklogs_export_w");
							T.loadTip(1,"正在导出，请稍候...",2,"");
						}
					});	
				}
			})
		}});
		return bts;
}
function coutomsearch(){
	 var html = "&nbsp;&nbsp; 签入日期：<input id='coutom_btime' value='"+btime+"' style='width:120px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"+
				"&nbsp;-&nbsp; <input id='coutom_etime' value='"+etime+"' style='width:120px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-01',alwaysUseStartDate:true});\"/>"+
				//"&nbsp;&nbsp;<input 
				"&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
	return html; 
}

function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}
_employeelogsT.C();

</script>
</body>
</html>
