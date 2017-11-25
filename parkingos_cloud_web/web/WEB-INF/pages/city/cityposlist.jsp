<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>POS机管理</title>
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
</head>
<body>
<div id="cityposobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];s
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var groups = eval(T.A.sendData("getdata.do?action=getcitygroups&cityid=${cityid}"));
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));

var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"60" ,height:"",issort:false,edit:false},
		{fieldcnname:"手机型号",fieldname:"mode",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"集团",fieldname:"comid",fieldvalue:'',inputtype:"text",twidth:"200" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				return setcname(value,pid);
			}},
		{fieldcnname:"停车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"160" ,height:"",issort:false},
		{fieldcnname:"收费员账户",fieldname:"uid",fieldvalue:'',inputtype:"number",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"收费员姓名",fieldname:"nickname",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,shide:true},
		{fieldcnname:"设备串号",fieldname:"device_code",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"串号是否认证",fieldname:"device_auth",fieldvalue:'',inputtype:"select",twidth:"100",noList:[{"value_no":"0","value_name":"未认证"},{"value_no":"1","value_name":"已认证"}],height:"",issort:false},
		{fieldcnname:"认证人",fieldname:"auth_user",fieldvalue:'',inputtype:"number",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"认证日期",fieldname:"auth_time",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false}
		];
var _cityposT = new TQTable({
	tabletitle:"POS机管理",
	ischeck:false,
	tablename:"citypos_tables",
	dataUrl:"citypos.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#cityposobj"),
	fit:[true,true,true],
	tableitems:_mediaField, 
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[1])
	/*bts.push({dname:"添加POS机",icon:"edit_add.png",onpress:function(Obj){
				T.each(_cityposT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
				Twin({Id:"citypos_add",Title:"添加POS机",Width:550,sysfun:function(tObj){
					Tform({
						formname: "parking_edit_f",
						formObj:tObj,
						recordid:"id",
						suburl:"citypos.do?action=create",
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_mediaField}]
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("citypos_add");} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"添加成功！",2,"");
								TwinC("citypos_add");
								_cityposT.M();
							}else if(ret=="-1"){
								T.loadTip(1,"请选择运营集团！",2,"");
							}else {
								T.loadTip(1,ret,2,o);
							}
						}
					});	
				}
			});
		}});*/
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityposT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"pos_search_w",Title:"搜索POS机",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "pos_search_f",
					formObj:tObj,
					formWinId:"pos_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("pos_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_cityposT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						})
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
		T.each(_cityposT.tc.tableitems,function(o,j){
			o.fieldvalue = _cityposT.GD(id)[j]
		});
		Twin({Id:"citypos_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "citypos_edit_f",
					formObj:tObj,
					recordid:"citypos_id",
					suburl:"citypos.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_cityposT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("citypos_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("citypos_edit_"+id);
							_cityposT.M()
						}else if(ret=="-1"){
							T.loadTip(1,"请选择运营集团！",2,"");
						}else{
							T.loadTip(1,ret,2,o)
						}
					}
				});	
			}
		})
	}});
	/*if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("citypos.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_cityposT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	bts.push({name:"串号认证",fun:function(id){
			var devicCode = _cityposT.GD(id,"device_code");
			if(devicCode==''){
				T.loadTip(1,"k",2,"");
			}
			
			Twin({Id:"device_auth_edit_"+id,Title:"串号认证",Width:550,sysfunI:id,sysfun:function(id,tObj){
					Tform({
						formname: "device_auth_edit_f",
						formObj:tObj,
						recordid:"mobilemanage_id",
						suburl:"citypos.do?action=deviceauth&id="+id,
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:[{fieldcnname:"串号是否认证",fieldname:"device_auth",fieldvalue:'',inputtype:"select",twidth:"100",noList:[{"value_no":"0","value_name":"未认证"},{"value_no":"1","value_name":"已认证"}],height:"",issort:false},
							                            		]}],
							rules:rules
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("device_auth_edit_"+id);} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"编辑成功！",2,"");
								TwinC("device_auth_edit_"+id);
								_cityposT.M()
							}else{
								T.loadTip(1,ret,2,o)
							}
						}
					});	
				}
			})
		}});*/
	if(bts.length <= 0){return false;}
	return bts;
}

var cachedgroup = [];
function setcname(value,rowid){
	var group = cachedgroup["'"+value+"'"];
	var groupid = '';
	if(!group&&parseInt(value)>1000){
		group = eval(T.A.sendData("getdata.do?action=getgroupidbyparkid&parkid="+value));
		cachedgroup["'"+value+"'"]=group;
	}
	
	if(groups&&group){
		groupid=group[0].groupid;
		for(var i=0;i<groups.length;i++){
			var gid  = groups[i].value_no;
			if(gid==groupid){
				return groups[i].value_name;
			}
		}
		return "";
	}else {
		return value;
	}
}

_cityposT.C();
</script>

</body>
</html>
