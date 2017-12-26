<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>黑名单管理</title>
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
<div id="zldblacksetobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var issupperadmin=${supperadmin};
var isadmin = ${isadmin};
var authlist ="";
if((issupperadmin&&issupperadmin==1) || (isadmin&&isadmin==1))
	authlist="0,1,2,3,4";
else
	authlist= T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var htips= "";
var role=${role};
var comid=${comid};
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false},
		{fieldcnname:"车牌",fieldname:"car_number",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"添加时间",fieldname:"ctime",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"修改时间",fieldname:"utime",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"操作人",fieldname:"operator",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":"0","value_name":"正常"},{"value_no":"1","value_name":"删除"}],twidth:"100" ,height:"",issort:false}
	];
var _zldblacksetT = new TQTable({
	tabletitle:"黑名单管理",
	ischeck:false,
	tablename:"zldblackset_tables",
	dataUrl:"zldblack.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&comid="+comid,
	tableObj:T("#zldblacksetobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
 	var bts=[];
 	if(subauth[1])
 		bts.push({dname:"添加黑名单",icon:"edit_add.png",onpress:function(Obj){
			Twin({Id:"zldblack_add",Title:"添加黑名单",Width:550,sysfun:function(tObj){
				Tform({
					formname: "parking_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"zldblack.do?action=create&comid="+comid,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("zldblack_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("zldblack_add");
							_zldblacksetT.M();
						}else if(ret=='-2'){
							T.loadTip(1,"不能重复添加 ！",2,"");
						}else {
							T.loadTip(1,ret,2,o);
						}
					}
				});	
	
				}
		});
		
	}});
	
	return bts;
}

//查看,添加,编辑,删除,设置区分大小车
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
		bts.push({name:"编辑",fun:function(id){
		T.each(_zldblacksetT.tc.tableitems,function(o,j){
			o.fieldvalue = _zldblacksetT.GD(id)[j]
		});
		Twin({Id:"zldblack_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "zldblack_edit_f",
					formObj:tObj,
					recordid:"zldblack_id",
					suburl:"zldblack.do?action=edit&comid="+comid+"&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_zldblacksetT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("zldblack_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("zldblack_edit_"+id);
							_zldblacksetT.M()
						}else{
							T.loadTip(1,ret,2,o)
						}
					}
				});	
			}
		})
	}});
	if(subauth[3])
		bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("zldblack.do?action=delete&comid="+comid,"post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_zldblacksetT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	
	if(bts.length <= 0){return false;}
	return bts;
}


_zldblacksetT.C();
</script>

</body>
</html>
