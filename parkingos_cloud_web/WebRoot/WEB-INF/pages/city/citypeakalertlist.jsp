<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> 
<meta name="renderer" content="webkit">
<title>城市高峰预警管理</title>
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
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<div id="citypeakalertobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var users = eval(T.A.sendData("getdata.do?action=getorgusers&cityid=${cityid}"));
var stateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"未结束"},{"value_no":1,"value_name":"已结束"}];
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false,fhide:true},
		{fieldcnname:"标题",fieldname:"title",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"内容",fieldname:"content",fieldvalue:'',inputtype:"text",twidth:"300" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:stateList,twidth:"100",height:"",issort:false,
			process:function(value,trId,colId){
					if(value==0)
						return "<font color='red'>未结束</font>";
					else 
						return "已结束";
			}},
		{fieldcnname:"开始时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"持续时长",fieldname:"duration",fieldvalue:'',inputtype:"text",twidth:"120" ,height:"",issort:false,shide:true}
	];
var back = "";
if("${from}" == "index"){
	back = "<a href='cityindex.do?authid=${index_authid}' class='sel_fee' style='float:right;margin-right:20px;'>返回</a>";
}
var _citypeakalertT = new TQTable({
	tabletitle:"城市高峰预警管理"+back,
	ischeck:false,
	tablename:"citypeakalert_tables",
	dataUrl:"citypeakalert.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#citypeakalertobj"),
	fit:[true,true,true],
	tableitems:_mediaField, 
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	/* if(subauth[1])
		bts.push({dname:"添加城市高峰事件",icon:"edit_add.png",onpress:function(Obj){
		T.each(_citypeakalertT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"citypeakalert_add",Title:"添加城市高峰事件",Width:550,sysfun:function(tObj){
				Tform({
					formname: "citypeakalert_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"citypeakalert.do?action=create",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("citypeakalert_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("citypeakalert_add");
							_citypeakalertT.M();
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
	if(subauth[2])
	bts.push({dname:"导出城市高峰",icon:"edit_add.png",onpress:function(Obj){
				T.each(_citypeakalertT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
				Twin({Id:"parkwithdraw_search_w",Title:"导出城市高峰",Width:480,sysfun:function(tObj){
						 TSform ({
							formname: "parkwithdraw_export_f",
							formObj:tObj,
							formWinId:"parkwithdraw_export_w",
							formFunId:tObj,
							dbuttonname:["确认导出"],
							formAttr:[{
								formitems:[{kindname:"",kinditemts:_mediaField}]
							}],
							SubAction:
							function(callback,formName){
								T("#exportiframe").src="citypeakalert.do?action=export&fieldsstr=id__state__type__resoure__content__handle_time__handle_user"+Serializ(formName)
								TwinC("parkwithdraw_search_w");
								T.loadTip(1,"正在导出，请稍候...",2,"");
							}
						});	
					}
				})
		}});*/
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_citypeakalertT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"pos_search_w",Title:"搜索城市高峰",Width:550,sysfun:function(tObj){
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
						_citypeakalertT.C({
							cpage:1,
							tabletitle:"高级搜索结果"+back,
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
	/* if(subauth[3])
	bts.push({name:"发布",fun:function(id){
		Tconfirm({Title:"确认发布吗",Content:"确认发布吗",
			OKFn:function(){
				var state = _citypeakalertT.GD(id,"state");
				if(state==2){
					T.loadTip(1,"已经发布过！",2,"");
					return;
				}
				T.A.sendData("citypeakalert.do?action=send","post","id="+id,
				function deletebackfun(ret){
					if(ret=="1"){
						T.loadTip(1,"已经发布！",2,"");
						_citypeakalertT.M()
					}else{
						T.loadTip(1,ret,2,"");
					}
				}
		)}})
	}}); */
	/*if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("citypeakalert.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_citypeakalertT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});*/
	
	if(bts.length <= 0){return false;}
	return bts;
}


_citypeakalertT.C();
</script>

</body>
</html>
