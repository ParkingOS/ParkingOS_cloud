<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>欠费会员</title>
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
<div id="planmemberobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
var role=${role};
/*权限*/
var issupperadmin=${supperadmin};
var isadmin = ${isadmin};
var authlist ="";
if((issupperadmin&&issupperadmin==1) || (isadmin&&isadmin==1))
	authlist="0,1,2";
else
	authlist= T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}

/*权限*/
var comid = ${comid};
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"昵称",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"密码",fieldname:"password",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"账号",fieldname:"strid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"性别",fieldname:"sex",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"电子邮件",fieldname:"email",fieldvalue:'',inputtype:"text", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"手机号码",fieldname:"mobile",fieldvalue:'',inputtype:"text",twidth:"60" ,height:"",issort:false},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"text",twidth:"60" ,height:"",issort:false}
		
	
	];
var _planmemberT = new TQTable({
	tabletitle:"欠费会员",
	ischeck:false,
	tablename:"planmember_tables",
	dataUrl:"planmember.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&comid="+comid,
	tableObj:T("#planmemberobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
//查看,添加,编辑,删除
	var bts =[];
	if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_planmemberT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"planmember_search_w",Title:"搜索欠费会员",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "planmember_search_f",
					formObj:tObj,
					formWinId:"planmember_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("planmember_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_planmemberT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&comid="+comid+"&action=query&"+Serializ(formName)
						})
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

_planmemberT.C();
</script>

</body>
</html>
