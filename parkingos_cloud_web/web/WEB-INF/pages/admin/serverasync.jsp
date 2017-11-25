<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>服务器同步管理</title>
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
<div id="localserverobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
var _mediaField = [                                                                                //是否显示
		{fieldcnname:"编号",fieldname:"id",inputtype:"text", twidth:"60",issort:false,edit:false,fhide:true},
		{fieldcnname:"车场编号",fieldname:"comid",inputtype:"number", twidth:"80",issort:false,edit:false},
		{fieldcnname:"车场名称",fieldname:"company_name",inputtype:"text", twidth:"150",issort:false,edit:false,shide:true},
		{fieldcnname:"本地服务器状态",fieldname:"syncstate",inputtype:"select",noList:[{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"断开"},{"value_no":"1","value_name":"正常"}], twidth:"100",issort:false,shide:true},
		{fieldcnname:"表名",fieldname:"table_name",inputtype:"text", twidth:"200" ,issort:false,edit:false},
		{fieldcnname:"表记录中的id",fieldname:"table_id",inputtype:"text", twidth:"160" ,issort:false,edit:false},
		{fieldcnname:"操作属性",fieldname:"create_time",inputtype:"date", twidth:"180",issort:false,edit:false},
		{fieldcnname:"操作类型",fieldname:"operate",inputtype:"select",noList:[{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"添加"},{"value_no":"1","value_name":"修改"},{"value_no":"2","value_name":"删除"}], twidth:"100",issort:false},
		{fieldcnname:"同步状态",fieldname:"state",inputtype:"select",noList:[{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"未同步"},{"value_no":"1","value_name":"已同步"}], twidth:"100",issort:false}
	]
var _localserverT = new TQTable({
	tabletitle:"服务器同步管理",
	ischeck:false,
	tablename:"parkorderanlysis_tables",
	dataUrl:"serverasync.do",
	iscookcol:false,
	buttons:false,
	//quikcsearch:coutomsearch(),
	param:"action=query",
	tableObj:T("#localserverobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	buttons:getAuthButtons(),
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_localserverT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"order_search_w",Title:"搜索",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "order_search_f",
					formObj:tObj,
					formWinId:"order_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("order_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_localserverT.C({
							cpage:1,
							tabletitle:"高级搜索结果&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${parkinfo}",
							extparam:"&action=query&"+Serializ(formName)
						})
						//addcoms();
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
	if(bts.length <= 0){return false;}
	return bts;
	};
_localserverT.C();
</script>

</body>
</html>
