<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.chan/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>员工管理</title>
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
</head>
<body>
<div id="chanmemobj" style="width:100%;height:100%;margin:0px;"></div>
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

var chanid = ${chanid};
var adminroles=eval(T.A.sendData("chanmember.do?action=getrole&chanid="+chanid))
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"姓名",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"登录账号",fieldname:"strid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"角色",fieldname:"role_id",fieldvalue:'',inputtype:"select",noList:adminroles ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"创建时间",fieldname:"reg_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",edit:false}
	];
var _addMemberField = [
	{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,edit:false},
	{fieldcnname:"姓名",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
	{fieldcnname:"电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
	{fieldcnname:"手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
	{fieldcnname:"角色",fieldname:"role_id",fieldvalue:'',inputtype:"select",noList:adminroles ,twidth:"100" ,height:"",issort:false},
	{fieldcnname:"创建时间",fieldname:"reg_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",edit:false}
];
var rules =[{name:"role_id",type:"",url:"",requir:true,warn:"",okmsg:""}];
/*var rules =[{name:"strid",type:"ajax",url:"chanmember.do?action=check&chanid="+chanid+"&value=",requir:true,warn:"账号已存在！",okmsg:""},
			{name:"role_id",type:"",url:"",requir:true,warn:"",okmsg:""}];*/
var tabtip = "员工管理";
var _chanmemT = new TQTable({
	tabletitle:tabtip,
	ischeck:false,
	tablename:"chanmem_tables",
	dataUrl:"chanmember.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery&chanid="+chanid,
	tableObj:T("#chanmemobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[1])
	bts.push({dname:"注册员工",icon:"edit_add.png",onpress:function(Obj){
	T.each(_chanmemT.tc.tableitems,function(o,j){
		o.fieldvalue ="";
	});
	Twin({Id:"chanmem_add",Title:"添加员工",Width:550,sysfun:function(tObj){
			Tform({
				formname: "chanmem_edit_f",
				formObj:tObj,
				recordid:"id",
				suburl:"chanmember.do?action=create&chanid="+chanid,
				method:"POST",
				formAttr:[{
					formitems:[{kindname:"",kinditemts:_addMemberField}],
					rules:rules
				}],
				buttons : [//工具
					{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("chanmem_add");} }
				],
				Callback:
				function(f,rcd,ret,o){
					if(ret=="1"){
						T.loadTip(1,"添加成功！",2,"");
						TwinC("chanmem_add");
						_chanmemT.M();
					}else{
						T.loadTip(1,ret,2,o);
					}
				}
			});	
		}
	})
	}});
	if(subauth[0])
	/* bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_chanmemT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"chanmem_search_w",Title:"搜索员工",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "chanmem_search_f",
					formObj:tObj,
					formWinId:"chanmem_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("chanmem_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_chanmemT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&chanid="+chanid+"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	}}); */
	return bts;
}
//查看,注册,编辑,不可收费,删除,修改密码
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_chanmemT.tc.tableitems,function(o,j){
			o.fieldvalue = _chanmemT.GD(id)[j]
		});
		Twin({Id:"chanmem_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "chanmem_edit_f",
					formObj:tObj,
					recordid:"chanmem_id",
					suburl:"chanmember.do?chanid="+chanid+"&action=edit&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_chanmemT.tc.tableitems}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("chanmem_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("chanmem_edit_"+id);
							_chanmemT.M();
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
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("chanmember.do?action=delete&chanid="+chanid,"post","selids="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_chanmemT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(subauth[4])
	bts.push({name:"修改密码",fun:function(id){
		T.each(_chanmemT.tc.tableitems,function(o,j){
			o.fieldvalue = _chanmemT.GD(id)[j]
		});
		Twin({Id:"chanmem_pass_"+id,Title:"修改密码",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "chanmem_pass_f",
					formObj:tObj,
					recordid:"chanmem_id",
					suburl:"chanmember.do?action=editpass&chanid="+chanid+"&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:[
							{fieldcnname:"新密码",fieldname:"newpass",fieldvalue:'',inputtype:"password", twidth:"200" ,height:"",issort:false},
							{fieldcnname:"确认密码",fieldname:"confirmpass",fieldvalue:'',inputtype:"password", twidth:"200" ,height:"",issort:false}]}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消成功",icon:"cancel.gif", onpress:function(){TwinC("chanmem_pass_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"修改成功！",2,"");
							TwinC("chanmem_pass_"+id);
							_chanmemT.M()
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
_chanmemT.C();
</script>

</body>
</html>
