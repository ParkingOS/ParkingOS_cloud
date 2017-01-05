<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>热点地区管理</title>
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
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<div id="hotareamanageobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
//查看,添加,编辑,删除,导出
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var users = eval(T.A.sendData("getdata.do?action=getorgusers&cityid=${cityid}"));

var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number",twidth:"100" ,height:"",edit:false,issort:false},
		{fieldcnname:"名称",fieldname:"name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"详细地址",fieldname:"adress",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"描述",fieldname:"reason",fieldvalue:'',inputtype:"text",twidth:"100",height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"新建"},{"value_no":1,"value_name":"删除"}],twidth:"100",height:"",issort:false},
		{fieldcnname:"新建日期",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"创建人",fieldname:"create_user",fieldvalue:'',inputtype:"select",noList:users,twidth:"120" ,height:"",issort:false,hide:true}
	];
var _hotareamanageT = new TQTable({
	tabletitle:"热点地区管理",
	ischeck:false,
	tablename:"hotareamanage_tables",
	dataUrl:"hotareamanage.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#hotareamanageobj"),
	fit:[true,true,true],
	tableitems:_mediaField, 
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_hotareamanageT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"pos_search_w",Title:"搜索热点地区",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "pos_search_f",
					formObj:tObj,
					formWinId:"pos_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消搜索",icon:"cancel.gif", onpress:function(){TwinC("pos_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_hotareamanageT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	if(subauth[1])
		bts.push({dname:"添加热点地区",icon:"edit_add.png",onpress:function(Obj){
		T.each(_hotareamanageT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"hotareamanage_add",Title:"添加摄像头",Width:550,sysfun:function(tObj){
				Tform({
					formname: "hotareamanage_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"hotareamanage.do?action=create",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("hotareamanage_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("hotareamanage_add");
							_hotareamanageT.M();
						}else if(ret==0){
							T.loadTip(1,"添加失败！请稍候再试！",2,"");
						}else{
							T.loadTip(1,"添加失败！",2,"");
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		})
	
	}});
	if(subauth[4])
	bts.push({dname:"导出热点地区",icon:"edit_add.png",onpress:function(Obj){
				T.each(_hotareamanageT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
				Twin({Id:"hotarea_search_w",Title:"导出热点地区",Width:480,sysfun:function(tObj){
						 TSform ({
							formname: "hotarea_export_f",
							formObj:tObj,
							formWinId:"hotarea_export_w",
							formFunId:tObj,
							dbuttonname:["确认导出"],
							formAttr:[{
								formitems:[{kindname:"",kinditemts:_mediaField}]
							}],
							SubAction:
							function(callback,formName){
								T("#exportiframe").src="hotareamanage.do?action=export&fieldsstr=id__state__name__adress__reason__create_time__create_user"+Serializ(formName)
								TwinC("hotarea_search_w");
								T.loadTip(1,"正在导出，请稍候...",2,"");
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
	if(subauth[5])
	bts.push({name:"绑定车场",fun:function(id){
		Twin({
			Id:"hotareamanage_detail_"+id,
			Title:"绑定车场  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
			Content:"<iframe src=\"hotareamanage.do?action=parkdetail&id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
			Width:T.gww()-100,
			Height:T.gwh()-50
		})
	}});
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_hotareamanageT.tc.tableitems,function(o,j){
			o.fieldvalue = _hotareamanageT.GD(id)[j]
		});
		Twin({Id:"hotareamanage_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "hotareamanage_edit_f",
					formObj:tObj,
					recordid:"hotareamanage_id",
					suburl:"hotareamanage.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_hotareamanageT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("hotareamanage_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("hotareamanage_edit_"+id);
							_hotareamanageT.M()
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
		var id_this = id ;
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("hotareamanage.do?action=delete","post","id="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_hotareamanageT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}


_hotareamanageT.C();
</script>

</body>
</html>
