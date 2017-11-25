<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>SIM卡管理</title>
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
<div id="citysimobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var cityid = "${cityid}";
var groups = eval(T.A.sendData("getdata.do?action=getcitygroups&cityid="+cityid));
var _parentField = [//城市商户登录显示 
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false},
		{fieldcnname:"PIN",fieldname:"pin",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"手机号",fieldname:"mobile",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"首次充值额",fieldname:"money",fieldvalue:'',inputtype:"number",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"套餐到期时间",fieldname:"limit_time",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"所属运营集团",fieldname:"groupid",fieldvalue:'',inputtype:"select",noList:groups,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"绑定设备类型",fieldname:"type",fieldvalue:'',inputtype:"select",noList:[{"value_no":0,"value_name":"未绑定"},{"value_no":1,"value_name":"POS机"},{"value_no":2,"value_name":"基站"}], twidth:"100" ,height:"",issort:false},
		{fieldcnname:"绑定设备编号",fieldname:"device_id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
					if(value == "-1"){
						return "";
					}else{
						return value;
					}
				}},
		{fieldcnname:"分配时间",fieldname:"allot_time",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false,hide:true},
		{fieldcnname:"入库时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false,hide:true},
		{fieldcnname:"入库人",fieldname:"nickname",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,hide:true}
	];
	
var _childField = [//运营集团登录显示
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false},
		{fieldcnname:"PIN",fieldname:"pin",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"手机号",fieldname:"mobile",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"首次充值额",fieldname:"money",fieldvalue:'',inputtype:"number",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"套餐到期时间",fieldname:"limit_time",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"绑定设备类型",fieldname:"type",fieldvalue:'',inputtype:"select",noList:[{"value_no":0,"value_name":"未绑定"},{"value_no":1,"value_name":"POS机"},{"value_no":2,"value_name":"基站"}], twidth:"100" ,height:"",issort:false},
		{fieldcnname:"绑定设备编号",fieldname:"device_id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
					if(value == "-1"){
						return "";
					}else{
						return value;
					}
				}},
		{fieldcnname:"分配时间",fieldname:"allot_time",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false,hide:true},
		{fieldcnname:"入库时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false,hide:true},
		{fieldcnname:"入库人",fieldname:"nickname",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,hide:true}
	];
var _mediaField = _childField;//默认显示运营集团的内容
if(cityid != "-1"){//城市商户登录
	_mediaField = _parentField;
}
var _citysimT = new TQTable({
	tabletitle:"SIM卡管理",
	ischeck:false,
	tablename:"citysim_tables",
	dataUrl:"citysim.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery",
	tableObj:T("#citysimobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[1])
	bts.push({dname:"添加SIM卡",icon:"edit_add.png",onpress:function(Obj){
				T.each(_citysimT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
				Twin({Id:"citysim_add",Title:"添加SIM卡",Width:550,sysfun:function(tObj){
					Tform({
						formname: "parking_edit_f",
						formObj:tObj,
						recordid:"id",
						suburl:"citysim.do?action=create",
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_mediaField}]
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("citysim_add");} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"添加成功！",2,"");
								TwinC("citysim_add");
								_citysimT.M();
							}else if(ret=="-1"){
								T.loadTip(1,"请选择运营集团！",2,"");
							}else if(ret=="-2"){
								T.loadTip(1,"请选择绑定的设备类型！",2,"");
							}else {
								T.loadTip(1,ret,2,o);
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
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_citysimT.tc.tableitems,function(o,j){
			o.fieldvalue = _citysimT.GD(id)[j]
		});
		Twin({Id:"citysim_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "citysim_edit_f",
					formObj:tObj,
					recordid:"citysim_id",
					suburl:"citysim.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_citysimT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("citysim_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("citysim_edit_"+id);
							_citysimT.M()
						}else if(ret=="-1"){
							T.loadTip(1,"请选择运营集团！",2,"");
						}else if(ret=="-2"){
							T.loadTip(1,"请选择绑定的设备类型！",2,"");
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
		T.A.sendData("citysim.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_citysimT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	
	if(bts.length <= 0){return false;}
	return bts;
}


_citysimT.C();
</script>

</body>
</html>
