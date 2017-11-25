<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>车检器进出场日志</title>
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
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
</head>
<body>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<div id="sensorcarinoutlogobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var issupperadmin=${supperadmin};
var authlist ="";
if(issupperadmin&&issupperadmin==1)
	authlist="0,1";
else
	authlist= T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/
var users = eval(T.A.sendData("getdata.do?action=getuserbyuin"));
var operateType = [{value_no:-1,value_name:"全部"},{value_no:"in",value_name:"进场"},{value_no:"out",value_name:"出场"}];
var oType=eval(T.A.sendData("sensorlog.do?action=getOtype"));
var role=${role};
var comid=${comid};
var _mediaField = [
         {fieldcnname:"收费员编号",fieldname:"uid",fieldvalue:'',inputtype:"text",twidth:"" ,height:"",issort:false},
		{fieldcnname:"车检器编号",fieldname:"sensornumber",fieldvalue:'',inputtype:"text",twidth:"" ,height:"",issort:false},
		{fieldcnname:"配对标示",fieldname:"indicate",fieldvalue:'',inputtype:"text",twidth:"" ,height:"",issort:false},
		{fieldcnname:"上传时间",fieldname:"ctime",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
	    {fieldcnname:"进场时间",fieldname:"carintime",fieldvalue:'',inputtype:"date",twidth:"" ,height:"",issort:false},
	    {fieldcnname:"出场时间",fieldname:"carouttime",fieldvalue:'',inputtype:"date",twidth:"" ,height:"",issort:false},
		{fieldcnname:"进场/出场",fieldname:"type",fieldvalue:'',inputtype:"text",inputtype:"select",noList:operateType,twidth:"" ,height:"",issort:false}
		
	];
var _sensorcarinoutT = new TQTable({
	tabletitle:"车检器进出场日志",
	ischeck:false,
	tablename:"sensorcarinout_tables",
	dataUrl:"sensorcarinout.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&comid="+comid,
	tableObj:T("#sensorcarinoutlogobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
 	var bts=[];
 	if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_sensorcarinoutT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"sensorcarinout_search_w",Title:"搜索操作记录",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "sensorcarinout_search_f",
					formObj:tObj,
					formWinId:"sensorcarinout_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("sensorcarinout_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_sensorcarinoutT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&comid="+comid+"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	if(subauth[1])
		bts.push({dname:"导出日志",icon:"toxls.gif",onpress:function(Obj){
		Twin({Id:"sensorlog_export_w",Title:"导出<font style='color:red;'>（如果没有设置，默认全部导出!）</font>",Width:480,sysfun:function(tObj){
				 TSform ({
					formname: "sensorlog_export_f",
					formObj:tObj,
					formWinId:"sensorlog_export_w",
					formFunId:tObj,
					dbuttonname:["确认导出"],
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}],
					}],
					//formitems:[{kindname:"",kinditemts:_excelField}],
					SubAction:
					function(callback,formName){
						T("#exportiframe").src="sensorlog.do?action=export&comid="+comid+"&"+Serializ(formName)
						TwinC("sensorlog_export_w");
						T.loadTip(1,"正在导出，请稍候...",2,"");
					}
				});	
			}
		})
	}});
	return bts;
}


function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}

_sensorcarinoutT.C();
</script>

</body>
</html>
