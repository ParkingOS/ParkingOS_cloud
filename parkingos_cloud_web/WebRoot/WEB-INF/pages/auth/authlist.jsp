<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>功能权限管理</title>
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
<div id="authmanageobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var role=${role};
var comid=${comid};
function getSelData(type){
	var cartypes = eval(T.A.sendData("authmanage.do?action=getdata&type="+type+"&oid=${oid}"));
	return cartypes;
}
var states = getSelData('state');
var authlist = getSelData('auths');
var orgtypelist = getSelData('allorgtype');
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false,fhide:true},
		{fieldcnname:"名称",fieldname:"nname",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"组织类型",fieldname:"oid",fieldvalue:'',inputtype:"select",noList:orgtypelist,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:states,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"父权限",fieldname:"pid",fieldvalue:'',inputtype:"select",noList:authlist,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"请求",fieldname:"url",fieldvalue:'',inputtype:"text",twidth:"200" ,height:"",issort:false},
		{fieldcnname:"子权限",fieldname:"sub_auth",fieldvalue:'',inputtype:"text",twidth:"500" ,height:"",issort:false},
		{fieldcnname:"Action(子权限)",fieldname:"actions",fieldvalue:'',inputtype:"text",twidth:"500" ,height:"",issort:false}
	];
var subtitle ="车场云";
if(${oid}==5)
	subtitle="停车宝";
var _authmanageT = new TQTable({
	tabletitle:"功能权限管理-"+subtitle,
	ischeck:false,
	tablename:"authmanage_tables",
	dataUrl:"authmanage.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&oid=${oid}&comid="+comid,
	tableObj:T("#authmanageobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[1])
	bts.push({dname:"添加功能权限",icon:"edit_add.png",onpress:function(Obj){
				Twin({Id:"cartype_add",Title:"添加  <font color='red'>"+subtitle+"</font> 功能权限",Width:550,sysfun:function(tObj){
					Tform({
						formname: "parking_edit_f",
						formObj:tObj,
						recordid:"id",
						suburl:"authmanage.do?action=create",
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_mediaField}]
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("cartype_add");} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"添加成功！",2,"");
								TwinC("cartype_add");
								_authmanageT.M();
							}else if(ret=='-2'){
								T.loadTip(1,"不能重复添加 ！",2,"");
							}else {
								T.loadTip(1,ret,2,o);
							}
						}
					});	
				}
			});
		}});
	bts.push(	{dname:"功能权限管理-车场云",icon:"edit_add.png",onpress:function(Obj){
			location = "authmanage.do?authid=${authid}&oid=${org_comid}&comid="+comid;
		}});
	bts.push(	{dname:"功能权限管理-停车宝",icon:"edit_add.png",onpress:function(Obj){
			location = "authmanage.do?authid=${authid}&oid=${org_tcbid}&comid="+comid;
		}});
	bts.push(	{dname:"功能权限管理-渠道",icon:"edit_add.png",onpress:function(Obj){
			location = "authmanage.do?authid=${authid}&oid=${org_chanid}&comid="+comid;
		}});
	bts.push(	{dname:"功能权限管理-集团",icon:"edit_add.png",onpress:function(Obj){
			location = "authmanage.do?authid=${authid}&oid=${org_groupid}&comid="+comid;
		}});
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_authmanageT.tc.tableitems,function(o,j){
			o.fieldvalue = _authmanageT.GD(id)[j]
		});
		Twin({Id:"cartype_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cartype_edit_f",
					formObj:tObj,
					recordid:"cartype_id",
					suburl:"authmanage.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_authmanageT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("cartype_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("cartype_edit_"+id);
							_authmanageT.M()
						}else{
							T.loadTip(1,ret,2,o)
						}
					}
				});	
			}
		})
	}});
	if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("authmanage.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_authmanageT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}}
	);
	
	if(bts.length <= 0){return false;}
	return bts;
}


_authmanageT.C();
</script>

</body>
</html>
