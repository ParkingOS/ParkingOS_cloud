<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>角色管理</title>
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
<div id="adminroleobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
//查看,添加,编辑,删除,编辑权限
/*权限*/
var loginuin=${loginuin};
var comid=${comid};
var isadmin=${isadmin};
function getSelData(type){
	var cartypes = eval(T.A.sendData("organize.do?action=getdata&type="+type));
	return cartypes;
}
var states = getSelData('state');
var rolelist=[{"value_no":loginuin,"value_name":"${rolename}"}]
var _mediaField = [
		{fieldcnname:"角色编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"名称",fieldname:"role_name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:states,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"所属角色",fieldname:"adminid",fieldvalue:'',inputtype:"select",noList:rolelist,twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"所属管理员",fieldname:"nickname",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"备注",fieldname:"resume",fieldvalue:'',inputtype:"text",twidth:"200" ,height:"",issort:false}
	];
var tabtip = "角色管理";
if(isadmin!=1)
	tabtip +=" （您不是管理员，仅有只读权限）"
var _adminroleT = new TQTable({
	tabletitle:tabtip,
	ischeck:false,
	tablename:"adminrole_tables",
	dataUrl:"adminrole.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&loginuin="+loginuin,
	tableObj:T("#adminroleobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	if(subauth[1]&&isadmin==1)
	return [
		{dname:"添加角色",icon:"edit_add.png",onpress:function(Obj){
				Twin({Id:"cartype_add",Title:"添加车型",Width:550,sysfun:function(tObj){
					Tform({
						formname: "parking_edit_f",
						formObj:tObj,
						recordid:"id",
						suburl:"adminrole.do?action=create&parentorgid=${parentorgid}",
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
								_adminroleT.M();
							}else if(ret=='-2'){
								T.loadTip(1,"不能重复添加 ！",2,"");
							}else {
								T.loadTip(1,ret,2,o);
							}
						}
					});	
				}
			});
		}}
	]
	return false;
}
//查看,添加,编辑,删除,编辑权限
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_adminroleT.tc.tableitems,function(o,j){
			o.fieldvalue = _adminroleT.GD(id)[j]
		});
		Twin({Id:"cartype_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cartype_edit_f",
					formObj:tObj,
					recordid:"cartype_id",
					suburl:"adminrole.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_adminroleT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("cartype_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("cartype_edit_"+id);
							_adminroleT.M()
						}else{
							T.loadTip(1,ret,2,o)
						}
					}
				});	
			}
		})
	}});
	if(subauth[3])
	bts.push(
	{name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("adminrole.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_adminroleT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(subauth[4])
	bts.push(
	{name:"编辑权限",
		fun:function(id){
			Twin({
				Id:"edit_role"+id,
				Title:"权限设置  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
				Content:"<iframe src=\"adminrole.do?action=editrole&loginroleid=${loginroleid}&roleid="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
				Width:T.gww()-300,
				Height:T.gwh()-200
			})
			
		}});
	
	if(bts.length <= 0||isadmin==0){return false;}
	return bts;
}


_adminroleT.C();
</script>

</body>
</html>
