<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>基站掉线历史记录</title>
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
<div id="faultobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var id="${id}";
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"number", twidth:"60",issort:false,edit:false,shide:true,hide:true,fhide:true},
		{fieldcnname:"掉线时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,edit:false},
		{fieldcnname:"恢复时间",fieldname:"end_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"掉线时长",fieldname:"duration",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false,hide:true}
	];
var _faultT = new TQTable({
	tabletitle:"基站掉线历史记录",
	ischeck:false,
	tablename:"fault_tables",
	dataUrl:"citytransmitter.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=querydetail&siteid="+id,
	tableObj:T("#faultobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_faultT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"sensor_search_w",Title:"搜索",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "sensor_search_f",
					formObj:tObj,
					formWinId:"sensor_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("sensor_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_faultT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=querydetail&siteid="+id+"&"+Serializ(formName)
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
	
	if(bts.length <= 0){return false;}
	return bts;
}
_faultT.C();
</script>

</body>
</html>
