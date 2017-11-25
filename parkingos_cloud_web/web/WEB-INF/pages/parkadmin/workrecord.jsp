<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>上下班记录</title>
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
<div id="workrecordsetobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var issupperadmin=${supperadmin};
var isadmin = ${isadmin};
var authlist ="";
if((issupperadmin&&issupperadmin==1) || (isadmin&&isadmin==1))
	authlist="0,1";
else
	authlist= T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var collectors= eval(T.A.sendData("getdata.do?action=getuser&id=${comid}"));
var htips= "";
var role=${role};
var comid=${comid};
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false},
		{fieldcnname:"收费员",fieldname:"uid",fieldvalue:'',inputtype:"select",noList:collectors,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"上班时间",fieldname:"start_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,edit:false},
		{fieldcnname:"下班时间",fieldname:"end_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,edit:false},
		{fieldcnname:"当前状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"已签到"},{"value_no":"1","value_name":"已签退"}],twidth:"100" ,height:"",issort:false}
	];
var _workrecordsetT = new TQTable({
	tabletitle:"上下班记录",
	ischeck:false,
	tablename:"workrecordset_tables",
	dataUrl:"workrecord.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	isidentifier:false,
	param:"action=query&comid="+comid,
	tableObj:T("#workrecordsetobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:[]
});
function getAuthButtons(){
 	var bts=[];
 	if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_workrecordsetT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"workrecord_search_w",Title:"搜索收费员",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "price_search_f",
					formObj:tObj,
					formWinId:"price_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("workrecord_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_workrecordsetT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&comid="+comid+"&action=query&"+Serializ(formName)
						})
						TwinC("workrecord_search_w");
					}
				});	
			}
		})
	
	}});
	if(bts.length <= 0){return false;}
	return bts;
}


_workrecordsetT.C();
</script>

</body>
</html>
