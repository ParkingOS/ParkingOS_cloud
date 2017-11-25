<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>商户管理</title>
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
<div id="shopmemberobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
var shop_id = ${shop_id};
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"姓名",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"登录账号",fieldname:"strid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"角色",fieldname:"auth_flag",fieldvalue:'',inputtype:"select",noList:[{"value_no":14,"value_name":"负责人"},{"value_no":15,"value_name":"工作人员"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"创建时间",fieldname:"reg_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",edit:false},
		{fieldcnname:"最近登录时间",fieldname:"logon_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",edit:false}
	];
var _addMemberField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"姓名",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"角色",fieldname:"auth_flag",fieldvalue:'',inputtype:"select",noList:[{"value_no":14,"value_name":"负责人"},{"value_no":15,"value_name":"工作人员"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"创建时间",fieldname:"reg_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",edit:false},
		{fieldcnname:"最近登录时间",fieldname:"logon_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",edit:false}
	];
var rules =[{name:"strid",type:"ajax",url:"member.do?action=check&value=",requir:true,warn:"账号已存在！",okmsg:""}];
var _shopmemberT = new TQTable({
	tabletitle:"员工管理",
	ischeck:false,
	tablename:"shopmember_tables",
	dataUrl:"shopmember.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery&shop_id="+shop_id,
	tableObj:T("#shopmemberobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	return [{dname:"注册员工",icon:"edit_add.png",onpress:function(Obj){
		T.each(_shopmemberT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"shopmember_add",Title:"添加员工",Width:550,sysfun:function(tObj){
				Tform({
					formname: "shopmember_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"shopmember.do?action=create&shop_id="+shop_id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_addMemberField}]
						//rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("shopmember_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("shopmember_add");
							_shopmemberT.M();
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		})
	
	}}
	]
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	bts.push({name:"编辑",fun:function(id){
		T.each(_shopmemberT.tc.tableitems,function(o,j){
			o.fieldvalue = _shopmemberT.GD(id)[j]
		});
		Twin({Id:"shopmember_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "shopmember_edit_f",
					formObj:tObj,
					recordid:"shopmember_id",
					suburl:"shopmember.do?shop_id="+shop_id+"&action=edit&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_shopmemberT.tc.tableitems}]
						//rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("shopmember_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("shopmember_edit_"+id);
							_shopmemberT.M();
						}else{
							T.loadTip(1,"编辑失败！",2,o)
						}
					}
				});	
			}
		})
	}});
	bts.push({name:"删除",fun:function(id){
		var id_this = id ;
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("shopmember.do?action=delete","post","selids="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_shopmemberT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	
	bts.push({name:"修改密码",fun:function(id){
		T.each(_shopmemberT.tc.tableitems,function(o,j){
			o.fieldvalue = _shopmemberT.GD(id)[j]
		});
		Twin({Id:"shopmember_pass_"+id,Title:"修改密码",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "shopmember_pass_f",
					formObj:tObj,
					recordid:"shopmember_id",
					suburl:"shopmember.do?action=editpass&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:[
							{fieldcnname:"新密码",fieldname:"newpass",fieldvalue:'',inputtype:"password", twidth:"200" ,height:"",issort:false},
							{fieldcnname:"确认密码",fieldname:"confirmpass",fieldvalue:'',inputtype:"password", twidth:"200" ,height:"",issort:false}]}]
						//rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消成功",icon:"cancel.gif", onpress:function(){TwinC("shopmember_pass_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"修改成功！",2,"");
							TwinC("shopmember_pass_"+id);
							_shopmemberT.M()
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
_shopmemberT.C();
</script>

</body>
</html>
