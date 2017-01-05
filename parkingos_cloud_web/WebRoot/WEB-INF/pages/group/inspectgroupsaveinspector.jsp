<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>保存泊位段</title>
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
<div id="savinspectorobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">

/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var work_group_id = "${work_group_id}";
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"50" ,height:"",hide:true},
		{fieldcnname:"巡查员名称",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false}
		
	];
var _savinspectorT = new TQTable({
	tabletitle:"保存巡查员",
	//ischeck:false,
	tablename:"savinspector_tables",
	dataUrl:"inspectgroupmanage.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=queryallinspector",
	tableObj:T("#savinspectorobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function getAuthButtons(){
	var bts =[];
	bts.push({ dname:  "保存", icon: "sendsms.gif", onpress:function(Obj){
		var sids = _savinspectorT.GS();
		var ids="";
		if(!sids){
			T.loadTip(1,"请先选择巡查员",2,"");
			return;
		}
		Twin({Id:"send_message_w",Title:"保存巡查员",Width:550,sysfun:function(tObj){
			Tform({
				formname: "send_message_ff",
				formObj:tObj,
				recordid:"id",
				suburl:"inspectgroupmanage.do?action=insertinspector&work_group_id="+work_group_id,
				method:"POST",
				Coltype:2,
				dbuttonname:["保存"],
				formAttr:[{
					formitems:[{kindname:"",kinditemts:[
					{fieldcnname:"巡查员编号",fieldname:"ids",fieldvalue:sids,inputtype:"multi",height:"80",edit:false}]}]
				}],
				buttons : [//工具
					{name: "cancel", dname: "取消", tit:"取消保存",icon:"cancel.gif", onpress:function(){TwinC("send_message_w");} }
				],
				Callback:function(f,rcd,ret,o){
					if(ret=="1"){
						T.loadTip(1,"保存成功！",2,"");
						TwinC("send_message_w");
						_savinspectorT.C();
						window.parent._workgroupinspectorT.C();
					}else{
						T.loadTip(1,"保存失败！",2,o);
						TwinC("send_message_w");
					}
				}
			});	
			}
		})
		
	}})
	
	if(bts.length>0)
		return bts;
	else 
		return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}
_savinspectorT.C();
window.parent._savinspectorT.M();
</script>

</body>
</html>
