<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>车场订单统计</title>
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
<div id="localserverobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"text", twidth:"60",issort:false,edit:false},
		{fieldcnname:"车场编号",fieldname:"comid",inputtype:"text", twidth:"80",issort:false,edit:false},
		{fieldcnname:"车场名称",fieldname:"company_name",inputtype:"text", twidth:"200" ,issort:false,edit:false},
		{fieldcnname:"服务器版本",fieldname:"version",inputtype:"text", twidth:"60" ,issort:false,edit:false,
			process:function(value,cid,id){
				return parseFloat(value)/10+".0";
			}},
		{fieldcnname:"cpu使用率",fieldname:"cpu",inputtype:"text", twidth:"80",issort:false,edit:false},
		{fieldcnname:"内存使用率",fieldname:"memory",inputtype:"text", twidth:"80",issort:false,edit:false},
		{fieldcnname:"硬盘(G)",fieldname:"harddisk",inputtype:"text", twidth:"80",issort:false,edit:false},
		{fieldcnname:"状态",fieldname:"create_time",inputtype:"text", twidth:"60",issort:false,edit:false,
			process:function(value,cid,id){
			var regEx = new RegExp("\\-","gi");
				if((new Date()).valueOf()-new Date(value.replace(regEx,"/")).valueOf()>300000){
					value = "断开";
				}else{
					value = "正常";
				}
				return value;
			}},
		//{fieldcnname:"日志",fieldname:"log",inputtype:"text", twidth:"100",issort:false},
		{fieldcnname:"手动升级",fieldname:"is_update",inputtype:"select",noList:[{"value_no":"0","value_name":"不可升级"},{"value_no":"1","value_name":"可升级"}], twidth:"100",issort:false},
		{fieldcnname:"最新时间",fieldname:"create_time",inputtype:"text", twidth:"130",issort:false,edit:false},
		{fieldcnname:"到期时间",fieldname:"limit_time",inputtype:"sdate", twidth:"130",issort:false}
	];
var _addField = [
		{fieldcnname:"车场编号",fieldname:"comid",inputtype:"text", twidth:"80",issort:false},
		{fieldcnname:"手动升级",fieldname:"is_update",inputtype:"select",noList:[{"value_no":"0","value_name":"不可升级"},{"value_no":"1","value_name":"可升级"}], twidth:"100",issort:false},
		{fieldcnname:"到期时间",fieldname:"limit_time",inputtype:"sdate", twidth:"130",issort:false}
	];
var _localserverT = new TQTable({
	tabletitle:"本地服务器",
	ischeck:false,
	tablename:"parkorderanlysis_tables",
	dataUrl:"localserver.do",
	iscookcol:false,
	buttons:false,
	//quikcsearch:coutomsearch(),
	param:"action=quickquery",
	tableObj:T("#localserverobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	allowpage:false,
	buttons:getAuthButtons(),
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	return [{dname:"添加双机车场",icon:"edit_add.png",onpress:function(Obj){
		T.each(_localserverT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"member_add",Title:"添加双机车场",Width:550,sysfun:function(tObj){
				Tform({
					formname: "member_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"localserver.do?action=add",
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_addField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("member_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("member_add");
							_localserverT.M();
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		})
	
	}}]
	}
function getAuthIsoperateButtons(){
	var bts = [];
	bts.push({name:"编辑",fun:function(id){
		T.each(_localserverT.tc.tableitems,function(o,j){
			o.fieldvalue = _localserverT.GD(id)[j]
		});
		Twin({Id:"localserver_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "localserver_edit_f",
					formObj:tObj,
					recordid:"localserver_id",
					suburl:"localserver.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}],
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("localserver_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("localserver_edit_"+id);
							_localserverT.M()
						}else{
							T.loadTip(1,ret,2,o)
						}
					}
				});	
			}
		})
	}});
	/*bts.push({name:"升级",
		rule:function(id){
			var state =_localserverT.GD(id,"is_update");
			if(state==1){
				this.name="禁用升级";
			}else{
				this.name="允许升级";
			}
			return true;
		},
		fun:function(id){
		var state =_localserverT.GD(id,"is_update");
		if(state==1){
			state=0;
		}else if(state==0){
			state=1;
		}
		var comid =_localserverT.GD(id,"comid");
		var type = "允许该车场升级";
		if(state==1){
			type = "禁用该车场升级";
		}
		Tconfirm({
			Title:"提示信息",
			Ttype:"alert",
			Content:"警告：您确认要 <font color='red'>"+type+"</font> 吗？",
			OKFn:function(){
			T.A.sendData("localserver.do?action=editupdate&comid="+comid+"&isupdate="+state,"GET","",
				function(ret){
					if(ret=="1"){
						T.loadTip(1,type+"成功！",2,"");
						_localserverT.C();
					}else{
						T.loadTip(1,"操作失败，请重试！",2,"")
					}
				},0,null)
			}
		});
	}});*/
	if(bts.length <= 0){return false;}
	return bts;
	};
_localserverT.C();
</script>

</body>
</html>
