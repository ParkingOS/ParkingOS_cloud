<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>诱导屏管理</title>
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
<div id="induceobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var stateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"正常"},{"value_no":1,"value_name":"故障"}];
var _mediaField=[
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false,hide:true,fhide:true,shide:true},
		{fieldcnname:"诱导屏名称",fieldname:"name",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"硬件编号",fieldname:"did",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"induce_state",fieldvalue:'',inputtype:"select",noList:stateList, twidth:"80" ,height:"",issort:false,edit:false,hide:true,shide:true,
			process:function(value,trId,colId){
				if(value==1)
					return "<font color='red'>故障</font>";
				else 
					return "正常";
			}},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"200" ,height:"",issort:false},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,shide:true,fhide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,shide:true,fhide:true},
		{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"修改时间",fieldname:"update_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"创建人",fieldname:"creator_name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,hide:true,shide:true},
		{fieldcnname:"修改人",fieldname:"update_name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,hide:true,shide:true},
		{fieldcnname:"最近心跳时间",fieldname:"heartbeat_time",fieldvalue:'',inputtype:"date" ,height:"",issort:false,edit:false,hide:true}
	];
	
var _induceT = new TQTable({
	tabletitle:"诱导屏管理",
	ischeck:false,
	tablename:"induce_tables",
	dataUrl:"inducel3.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#induceobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	
	if(subauth[1])
	bts.push({dname:"添加诱导屏",icon:"edit_add.png",onpress:function(Obj){
				T.each(_induceT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
				Twin({Id:"induce_add",Title:"添加诱导屏",Width:550,sysfun:function(tObj){
					Tform({
						formname: "induce_edit_f",
						formObj:tObj,
						recordid:"id",
						suburl:"inducel3.do?action=create",
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_mediaField}]
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("induce_add");} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"添加成功！",2,"");
								TwinC("induce_add");
								_induceT.M();
							}if(ret=="-2"){
								T.loadTip(1,"硬件编号重复了！",2,"");
							}else {
								T.loadTip(1,ret,7,o);
							}
						}
					});	
				}
			});
		}});
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_induceT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"induce_search_w",Title:"搜索停车场",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "induce_search_f",
					formObj:tObj,
					formWinId:"induce_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("induce_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_induceT.C({
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
		T.each(_induceT.tc.tableitems,function(o,j){
			o.fieldvalue = _induceT.GD(id)[j]
		});
		Twin({Id:"induce_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "induce_edit_f",
					formObj:tObj,
					recordid:"induce_id",
					suburl:"inducel3.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_induceT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("induce_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("induce_edit_"+id);
							_induceT.M()
						}if(ret=="-2"){
							T.loadTip(1,"硬件编号重复了！",2,"");
						}else{
							T.loadTip(1,ret,7,o)
						}
					}
				});	
			}
		})
	}});
	if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("inducel3.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_induceT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	
	if(bts.length <= 0){return false;}
	return bts;
}

function setname(value, list){
	if(value != "-1"){
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

_induceT.C();
</script>

</body>
</html>
