<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>服务器管理</title>
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
<div id="cityserverobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"text", twidth:"60",issort:false,edit:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false},
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
		{fieldcnname:"手动升级",fieldname:"is_update",inputtype:"select",noList:[{"value_no":"0","value_name":"不可升级"},{"value_no":"1","value_name":"可升级"}], twidth:"100",issort:false},
		{fieldcnname:"最新时间",fieldname:"create_time",inputtype:"text", twidth:"130",issort:false,edit:false},
		{fieldcnname:"到期时间",fieldname:"limit_time",inputtype:"sdate", twidth:"130",issort:false},
		{fieldcnname:"车场密钥",fieldname:"secret",inputtype:"text", twidth:"140",issort:false},
		{fieldcnname:"备注",fieldname:"remark",inputtype:"text", twidth:"180",issort:false}
	];
var _addField = [
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"手动升级",fieldname:"is_update",inputtype:"select",noList:[{"value_no":"0","value_name":"不可升级"},{"value_no":"1","value_name":"可升级"}], twidth:"100",issort:false},
		{fieldcnname:"车场密钥",fieldname:"secret",inputtype:"text", twidth:"140",issort:false}
	];
var _cityserverT = new TQTable({
	tabletitle:"本地服务器",
	ischeck:false,
	tablename:"parkorderanlysis_tables",
	dataUrl:"cityserver.do",
	iscookcol:false,
	buttons:false,
	//quikcsearch:coutomsearch(),
	param:"action=quickquery",
	tableObj:T("#cityserverobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	buttons:getAuthButtons(),
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[1])
	bts.push({dname:"添加本地服务器",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityserverT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"member_add",Title:"添加本地服务器",Width:550,sysfun:function(tObj){
				Tform({
					formname: "member_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"cityserver.do?action=create",
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
							_cityserverT.M();
						}if(ret=="-1"){
							T.loadTip(1,"请选择车场",2,"");
						}else{
							T.loadTip(1,ret,2,o);
						}
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
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_cityserverT.tc.tableitems,function(o,j){
			o.fieldvalue = _cityserverT.GD(id)[j]
		});
		Twin({Id:"cityserver_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityserver_edit_f",
					formObj:tObj,
					recordid:"cityserver_id",
					suburl:"cityserver.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}],
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("cityserver_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("cityserver_edit_"+id);
							_cityserverT.M()
						}if(ret=="-1"){
							T.loadTip(1,"请选择车场",2,"");
						}else{
							T.loadTip(1,ret,2,o)
						}
					}
				});	
			}
		})
	}});

	if(bts.length <= 0){return false;}
	return bts;
	};
_cityserverT.C();
</script>

</body>
</html>
