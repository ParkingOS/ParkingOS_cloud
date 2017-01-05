<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>诱导管理</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">
<style type="text/css">
.title1{
	width: 100%;
    margin: 0 auto;
    font-weight: bold;
    line-height: 25px;
    height: 40px;
    background: #EFEFEF;
    padding-top: 15px;
    border-bottom: 1px solid #ddd;
    font-size:16px !important;
}
.sel_fee{
	text-align: center;
    padding-top: 2px;
    padding-bottom: 2px;
    border-radius: 0px;
    background-color: #FFFFFF;
    outline: medium;
    border: 1px solid #5CCDBE;
    color: #5CCDBE;
    padding-left: 8px;
    padding-right: 8px;
}
.title1 a:hover{
	background:#5CCDBE;
	color:#FFFFFF;
}

.column{
	background:#5CCDBE;
	color:#FFFFFF;
}
a:link {
    color: #5CCDBE;
    text-decoration: none;
}
</style>
<script type="text/javascript" src="js/jquery.js"></script>
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
<div class="top">
	<ul class="title1">
		<a href="cityinducedmonitor.do" class="sel_fee" style="margin-left:10px;" id="map">地图</a><a class="sel_fee column" style="margin-left:-1px;">列表</a>
	</ul>
</div>
<div id="cityinduceobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
  $(function(){
  	$("#table").css('background','#5ccdbe').css('color','#fff');
  });
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var induce_state_start = "${induce_state_start}";
var typeList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"一级诱导"},{"value_no":1,"value_name":"二级诱导"},{"value_no":2,"value_name":"三级级诱导"}];
var stateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"正常"},{"value_no":1,"value_name":"故障"}];
var groups = eval(T.A.sendData("getdata.do?action=getcitygroups&cityid=${cityid}"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"number", twidth:"60",issort:false,edit:false,hide:true},
		{fieldcnname:"诱导名称",fieldname:"name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"硬件编号",fieldname:"did",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text",height:"",issort:false,fhide:true,edit:false,hide:true,shide:true},
        {fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text",height:"",issort:false,fhide:true,edit:false,hide:true,shide:true},
        {fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date" ,height:"",issort:false,edit:false,hide:true},
        {fieldcnname:"更新时间",fieldname:"update_time",fieldvalue:'',inputtype:"date" ,height:"",issort:false,edit:false,hide:true},
        {fieldcnname:"诱导类型",fieldname:"type",fieldvalue:'',inputtype:"select",noList:typeList, twidth:"120" ,height:"",issort:false,edit:false,hide:true},
        {fieldcnname:"诱导屏状态",fieldname:"induce_state",fieldvalue:'',inputtype:"select",noList:stateList, twidth:"80" ,height:"",issort:false,edit:false,hide:true,
        	process:function(value,trId,colId){
				if(value==1)
					return "<font color='red'>故障</font>";
				else 
					return "正常";
			}},
        {fieldcnname:"诱导地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",height:"",issort:false},
        {fieldcnname:"最近心跳时间",fieldname:"heartbeat_time",fieldvalue:'',inputtype:"date" ,height:"",issort:false,edit:false,hide:true},
        {fieldcnname:"掉线次数",fieldname:"fcount",fieldvalue:'',inputtype:"text",twidth:"80",height:"",issort:false,shide:true,
			process:function(value,cid,id){
				return "<a href=# onclick=\"viewdetail('"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
			}}
	]; 
var back = "";
if("${from}" == "index"){
	back = "<a href='cityindex.do?authid=${index_authid}' class='sel_fee' style='float:right;margin-right:20px;'>返回</a>";
}
var _cityinduceT = new TQTable({
	tabletitle:"全部诱导"+back,
	ischeck:false,
	tablename:"cityinduce_tables",
	dataUrl:"cityinduce.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&induce_state_start="+induce_state_start,
	tableObj:T("#cityinduceobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	
		if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
			T.each(_cityinduceT.tc.tableitems,function(o,j){
				o.fieldvalue ="";
			}); 
			Twin({Id:"cityinduce_search_w",Title:"搜索诱导",Width:550,sysfun:function(tObj){
					TSform ({
						formname: "cityinduce_search_f",
						formObj:tObj,
						formWinId:"cityinduce_search_w",
						formFunId:tObj,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_mediaField}]
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("cityinduce_search_w");} }
						],
						SubAction:
						function(callback,formName){
							_cityinduceT.C({
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
	if(subauth[1])
	bts.push({name:"编辑",fun:function(id){
		T.each(_cityinduceT.tc.tableitems,function(o,j){
			o.fieldvalue = _cityinduceT.GD(id)[j]
		});
		Twin({Id:"cityinduce_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityinduce_edit_f",
					formObj:tObj,
					recordid:"cityinduce_id",
					suburl:"cityinduce.do?action=edit&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_cityinduceT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("cityinduce_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("cityinduce_edit_"+id);
							_cityinduceT.M();
						}if(ret=="-2"){
							T.loadTip(1,"硬件编号重复了！",2,"");
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}
function viewdetail(value,id){
	var did =_cityinduceT.GD(id,"did");
	var tip = "诱导掉线历史记录";
	Twin({
		Id:"induce_detail_"+id,
		Title:tip+"  --> 诱导编号："+did,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='induce_detail_'"+id+" id='induce_detail_'"+id+" src='cityinduce.do?action=detail&id="+id+"' width='100%' height='100%' frameborder='0' scrolling='no' style='overflow:hidden;' ></iframe>"
	});
}
_cityinduceT.C();
</script>
<script type="text/javascript">
var height = document.body.clientHeight;
document.getElementById("cityinduceobj").style.height = (height - 40 - 15)+"px";
</script>
</body>
</html>
