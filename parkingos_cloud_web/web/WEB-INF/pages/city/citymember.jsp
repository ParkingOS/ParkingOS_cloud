<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.group/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>人员管理</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.public.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.datatable.js?0818" type="text/javascript">//表格</script>
<script src="js/tq.form.js?0817" type="text/javascript">//表单</script>
<script src="js/tq.searchform.js?0817" type="text/javascript">//查询表单</script>
<script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
<style type="text/css">
.sel_fee{
	text-align: center;
    /* padding-top: 0px; */
    /* padding-bottom: 0px; */
    border-radius: 0px;
    background-color: #FFFFFF;
    outline: medium;
    border: 1px solid #5CCDBE;
    color: #5CCDBE;
    padding-left: 8px;
    padding-right: 8px;
    font-size: 12px;
    height: 24px;
    margin-top: 3px;
    line-height: 24px;
}
a:hover{
	background:#5CCDBE;
	color:#FFFFFF;
}
</style>
</head>
<body>
<div id="memobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var issupperadmin=${supperadmin};
var isadmin = ${isadmin};
var authlist ="";
if(issupperadmin&&issupperadmin==1)
	authlist="0,1,2,3,4";
else
	authlist= T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}

var adminroles=eval(T.A.sendData("citymember.do?action=getrole&cityid=${cityid}&chanid=${chanid}&groupid=${groupid}"))
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"姓名",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"登录账号",fieldname:"strid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"角色",fieldname:"role_id",fieldvalue:'',inputtype:"select",noList:adminroles ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"创建时间",fieldname:"reg_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",edit:false},
		{fieldcnname:"备注",fieldname:"resume",fieldvalue:'',inputtype:"text", twidth:"130" ,height:""}
	];
var _addMemberField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"姓名",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"角色",fieldname:"role_id",fieldvalue:'',inputtype:"select",noList:adminroles ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"创建时间",fieldname:"reg_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",edit:false},
		{fieldcnname:"备注",fieldname:"resume",fieldvalue:'',inputtype:"text", twidth:"130" ,height:""}
	];
var rules =[{name:"strid",type:"ajax",url:"citymember.do?action=check&cityid=${cityid}&chanid=${chanid}&groupid=${groupid}&value=",requir:true,warn:"账号已存在！",okmsg:""}];
var back = "";
if("${from}" == "index"){
	back = "<a href='cityindex.do?authid=${index_authid}' class='sel_fee' style='float:right;margin-right:20px;'>返回</a>";
}
var tabtip = "人员管理"+back;
var _memT = new TQTable({
	tabletitle:tabtip,
	ischeck:false,
	tablename:"mem_tables",
	dataUrl:"citymember.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&cityid=${cityid}&chanid=${chanid}&groupid=${groupid}",
	tableObj:T("#memobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[1])
	bts.push({dname:"注册员工",icon:"edit_add.png",onpress:function(Obj){
	T.each(_memT.tc.tableitems,function(o,j){
		o.fieldvalue ="";
	});
	Twin({Id:"mem_add",Title:"添加员工",Width:550,sysfun:function(tObj){
			Tform({
				formname: "mem_edit_f",
				formObj:tObj,
				recordid:"id",
				suburl:"citymember.do?action=create&&cityid=${cityid}&chanid=${chanid}&groupid=${groupid}",
				method:"POST",
				formAttr:[{
					formitems:[{kindname:"",kinditemts:_addMemberField}]
					//rules:rules
				}],
				buttons : [//工具
					{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("mem_add");} }
				],
				Callback:
				function(f,rcd,ret,o){
					if(ret=="1"){
						T.loadTip(1,"添加成功！",2,"");
						TwinC("mem_add");
						_memT.M();
					}else{
						T.loadTip(1,ret,2,o);
					}
				}
			});	
		}
	})
	}});
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_memT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"mem_search_w",Title:"搜索员工",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "mem_search_f",
					formObj:tObj,
					formWinId:"mem_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("mem_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_memT.C({
							cpage:1,
							tabletitle:"高级搜索结果"+back,
							extparam:"&cityid=${cityid}&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	}});
	return bts;
}
//查看,注册,编辑,不可收费,删除,修改密码
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_memT.tc.tableitems,function(o,j){
			o.fieldvalue = _memT.GD(id)[j]
		});
		Twin({Id:"mem_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "mem_edit_f",
					formObj:tObj,
					recordid:"mem_id",
					suburl:"citymember.do?&action=edit&cityid=${cityid}&chanid=${chanid}&groupid=${groupid}&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_memT.tc.tableitems}]
						//rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("mem_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("mem_edit_"+id);
							_memT.M();
						}else{
							T.loadTip(1,"编辑失败！",2,o)
						}
					}
				});	
			}
		})
	}});
	if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		var id_this = id ;
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("citymember.do?action=delete&cityid=${cityid}&chanid=${chanid}&groupid=${groupid}","post","selids="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_memT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(subauth[4])
	bts.push({name:"修改密码",fun:function(id){
		T.each(_memT.tc.tableitems,function(o,j){
			o.fieldvalue = _memT.GD(id)[j]
		});
		Twin({Id:"mem_pass_"+id,Title:"修改密码",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "mem_pass_f",
					formObj:tObj,
					recordid:"mem_id",
					suburl:"citymember.do?action=editpass&cityid=${cityid}&chanid=${chanid}&groupid=${groupid}&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:[
							{fieldcnname:"新密码",fieldname:"newpass",fieldvalue:'',inputtype:"password", twidth:"200" ,height:"",issort:false},
							{fieldcnname:"确认密码",fieldname:"confirmpass",fieldvalue:'',inputtype:"password", twidth:"200" ,height:"",issort:false}]}]
						//rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消成功",icon:"cancel.gif", onpress:function(){TwinC("mem_pass_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"修改成功！",2,"");
							TwinC("mem_pass_"+id);
							_memT.M()
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
}
_memT.C();
</script>

</body>
</html>
