<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>提现管理</title>
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
<div id="withdrawobj" style="width:100%;height:100%;margin:0px;"></div>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var states=[{"value_no":0,"value_name":"等待处理"},{"value_no":2,"value_name":"处理中"},{"value_no":3,"value_name":"已支付"},{"value_no":4,"value_name":"提现失败"},{"value_no":5,"value_name":"延迟处理"}];
var cityid = "${cityid}";
var groups = eval(T.A.sendData("getdata.do?action=getcitygroups&cityid="+cityid));
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number",twidth:"50" ,height:"",edit:false,issort:false},
		{fieldcnname:"运营集团编号",fieldname:"groupid",fieldvalue:'',inputtype:"number",twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
						if(value == "-1"){
							return "";
						}else{
							return value;
						}
				}},
		{fieldcnname:"运营集团名称",fieldname:"gname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",hide:true,shide:true},
		{fieldcnname:"车场编号",fieldname:"comid",fieldvalue:'',inputtype:"number",twidth:"150" ,height:"",issort:false,
			process:function(value,pid){
							if(value == "-1"){
								return "";
							}else{
								return value;
							}
				}},
		{fieldcnname:"车场名称",fieldname:"company_name",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",hide:true,shide:true},
		{fieldcnname:"提现人",fieldname:"name",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",hide:true,shide:true},
		{fieldcnname:"类型",fieldname:"wtype",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":1,"value_name":"个人提现"},{"value_no":0,"value_name":"公司提现"},{"value_no":2,"value_name":"对公提现"}], twidth:"200" ,height:"",hide:true},
		{fieldcnname:"提现金额",fieldname:"amount",fieldvalue:'',inputtype:"number", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"申请时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"200" ,height:"",hide:true},
		{fieldcnname:"处理日期",fieldname:"update_time",fieldvalue:'',inputtype:"date", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:states, twidth:"200" ,height:"",issort:false}
	];
var editField=[{fieldcnname:"处理状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:states, twidth:"200" ,height:"",issort:false}];
var _withdrawT = new TQTable({
	tabletitle:"提现管理",
	//ischeck:false,
	tablename:"withdraw_tables",
	dataUrl:"3plwithdraw.do",
	iscookcol:false,
	//checktype:"checkbox",
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#withdrawobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_withdrawT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"withdraw_search_w",Title:"搜索",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "withdraw_search_f",
					formObj:tObj,
					formWinId:"withdraw_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("withdraw_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_withdrawT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						});
					}
				});	
			}
		});
	
	}});
	if(subauth[2])
	bts.push({ dname:  "批量处理", icon: "sendsms.gif", onpress:function(Obj){
		var sids = _withdrawT.GS();
		if(!sids){
			T.loadTip(1,"请先选择要处理的数据",2,"");
			return ;
		}
		Twin({Id:"muli_edit_w",Title:"批量处理",Width:250,sysfun:function(tObj){
			Tform({
				formname: "muli_edit_ff",
				formObj:tObj,
				recordid:"id",
				suburl:"3plwithdraw.do?action=multiedit&ids="+sids,
					method:"POST",
					Coltype:2,
					dbuttonname:["处理"],
					formAttr:[{
						formitems:[{kindname:"",kinditemts:editField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消处理",icon:"cancel.gif", onpress:function(){TwinC("muli_edit_w");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"处理成功！",2,"");
							TwinC("muli_edit_w");
							_withdrawT.M();
						}else{
							T.loadTip(1,"处理失败！",2,"");
						}
					}
			});	
			}
		});
	}});
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[1])
	bts.push({name:"处理",fun:function(id){
		T.each(_withdrawT.tc.tableitems,function(o,j){
			o.fieldvalue = _withdrawT.GD(id)[j];
		});
		Twin({Id:"account_edit_"+id,Title:"处理",Width:300,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "account_edit_f",
					formObj:tObj,
					recordid:"account_id",
					suburl:"3plwithdraw.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					dbuttonname:["保存"],
					formAttr:[{
						formitems:[{kindname:"",kinditemts:editField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("account_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("account_edit_"+id);
							_withdrawT.M();
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		});
	}
	});
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

_withdrawT.C();
</script>

</body>
</html>
