<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>未绑定车场列表</title>
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
<div id="parkobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
/* var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
} */
var moduleid="${moduleid}";
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"车场编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"50" ,height:"",edit:false,issort:false,hide:true,shide:true},
		{fieldcnname:"车场名称",fieldname:"company_name",fieldvalue:'',inputtype:"text",noList:parks,twidth:"200" ,height:"",issort:false}
	];
var _parkT = new TQTable({
	tabletitle:"未绑定车场列表",
	ischeck:true,
	tablename:"park_tables",
	dataUrl:"inducemodule.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=queryunpark&moduleid="+moduleid,
	tableObj:T("#parkobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	bts.push({dname:"批量绑定车场", icon: "sendsms.gif", onpress:function(Obj){
		var sids = _parkT.GS();
		if(!sids){
			T.loadTip(1,"请先选择要绑定的车场",2,"");
			return;
		}
		Tconfirm({Title:"批量绑定车场",Content:"确认绑定吗？",OKFn:function(){
			T.A.sendData("inducemodule.do?action=bindpark&moduleid="+moduleid,"post","id="+sids,
				function deletebackfun(ret){
					if(ret=="1"){
						T.loadTip(1,"绑定成功！",5,"");
						_parkT.M();
						window.parent._moduleT.M();
					}else if(ret=="-2"){
						T.loadTip(1,"该诱导屏已经绑定该车场！",2,"");
					}else{
						T.loadTip(1,"绑定失败！",2,"");
					}
				}
		)}})
	}})
	if(bts.length>0)
		return bts;
	return false;
}

function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}

function setname(value, list){
	if(value != "" && value != "-1"){
		for(var i=0; i<list.length;i++){
			var o = list[i];
			var key = o.value_no;
			var v = o.value_name;
			if(value == key){
				return v;
			};
		};
		return "";
	}else{
		return "";
	};
}

_parkT.C();
</script>

</body>
</html>
