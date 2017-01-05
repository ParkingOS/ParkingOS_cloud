<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>车场管理</title>
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
var hotid = "${hotid}";
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField=[
			{fieldcnname:"车场名称",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"200" ,height:"",issort:false},
		];
	
var _induceT = new TQTable({
	tabletitle:"车场管理",
	ischeck:false,
	tablename:"induce_tables",
	dataUrl:"hotareamanage.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=parkquery&hotid="+hotid,
	tableObj:T("#induceobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	bts.push({dname:"添加车场",icon:"edit_add.png",onpress:function(Obj){
				T.each(_induceT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
				Twin({Id:"induce_add",Title:"添加车场",Width:550,sysfun:function(tObj){
					Tform({
						formname: "induce_edit_f",
						formObj:tObj,
						recordid:"id",
						suburl:"hotareamanage.do?action=parkcreate&hotid="+hotid,
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
								window.parent._induceT.M();
							}else {
								T.loadTip(1,ret,7,o);
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
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("hotareamanage.do?action=parkdelete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_induceT.M();
					window.parent._induceT.M();
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
