<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>电子标签管理</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.public.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.datatable.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.form.js?075417" type="text/javascript">//表单</script>
<script src="js/tq.searchform.js?0817" type="text/javascript">//查询表单</script>
<script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
</head>

<body>
<div id="cityrfidobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
//查看,编辑,启用_停用,添加,速通卡用户
var role=${role};
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"收费员",fieldname:"nickname",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,shide:true},
		{fieldcnname:"设备类型",fieldname:"type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"NFC"},{"value_no":1,"value_name":"电子标签"}] , twidth:"100" ,height:"",issort:false},
		{fieldcnname:"设备编号",fieldname:"nfc_uuid",fieldvalue:'',inputtype:"text",twidth:"160" ,height:"",issort:false},
		{fieldcnname:"手机号",fieldname:"mobile",fieldvalue:'',inputtype:"text",twidth:"160" ,height:"",issort:false,shide:true},
		{fieldcnname:"车牌号",fieldname:"carnumber",fieldvalue:'',inputtype:"text",twidth:"160" ,height:"",issort:false,shide:true},
		{fieldcnname:"账户余额",fieldname:"balance",fieldvalue:'',inputtype:"number",twidth:"160" ,height:"",issort:false,shide:true},
		{fieldcnname:"注册时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",issort:false,hide:true},
		{fieldcnname:"绑定时间",fieldname:"update_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",issort:false,hide:true},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"正常"},{"value_no":1,"value_name":"停用"}] , twidth:"100" ,height:"",issort:false,
			process:function(value,trId,colId){
				if(value==1)
					return "<font color='red'>停用</font>";
				else 
					return "正常";
			}}
	];
var _cityrfidT = new TQTable({
	tabletitle:"电子标签管理",
	ischeck:false,
	tablename:"cityrfid_tables",
	dataUrl:"cityrfid.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#cityrfidobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
//查看,编辑,启用_停用,添加,速通卡用户
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityrfidT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"cityrfid_search_w",Title:"搜索收费员",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "cityrfid_search_f",
					formObj:tObj,
					formWinId:"cityrfid_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("cityrfid_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_cityrfidT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"action=query&"+Serializ(formName)
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
	bts.push({name:"启用/停用",
		rule:function(id){
			var state =_cityrfidT.GD(id,"state");
			if(state==0){//状态：可用
				this.name="停用";
			}else
				this.name="启用";
			return true
		 },
		fun:function(id){
		var id_this = id ;
		var state =_cityrfidT.GD(id,"state");
		var conmessage="启用";
		if(state==0){//状态：可用
			conmessage="停用";
		}
		Tconfirm({Title:"确认"+conmessage+"吗",Content:"确认"+conmessage+"吗",OKFn:function(){T.A.sendData("nfcmanage.do?action=modify","post","selids="+id_this+"&state="+state,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,conmessage+"成功！",2,"");
					_cityrfidT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}
_cityrfidT.C();
</script>

</body>
</html>
