<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>上班时间管理</title>
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
<div id="cameraobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">

var role_id="${role_id}"
var _field = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"60" ,height:"",issort:false,edit:false,fhide:true},
		{fieldcnname:"上班小时",fieldname:"b_hour",fieldvalue:'',defaultValue:'7||7',inputtype:"hour", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"上班分钟",fieldname:"b_minute",fieldvalue:'',defaultValue:'0',inputtype:"minute", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"下班小时",fieldname:"e_hour",fieldvalue:'',defaultValue:'21||21',inputtype:"hour", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"下班分钟",fieldname:"e_minute",fieldvalue:'',defaultValue:'0',inputtype:"minute", twidth:"60" ,height:"",issort:false}
	];
var _cameraT = new TQTable({
	tabletitle:"上班时间管理",
	ischeck:false,
	tablename:"camera_tables",
	dataUrl:"worktime.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&role_id="+role_id,
	tableObj:T("#cameraobj"),
	fit:[true,true,true],
	tableitems:_field,
	isoperate:getAuthIsoperateButtons()
});
//查看,添加,编辑,删除,修改密码
function getAuthButtons(){
	var bts=[];
	bts.push({dname:"添加",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cameraT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"camera_add",Title:"添加",Width:550,sysfun:function(tObj){
				Tform({
					formname: "camera_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"worktime.do?action=create&role_id="+role_id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_field}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("camera_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("camera_add");
							_cameraT.M();
						}else if(ret==0){
							T.loadTip(1,"添加失败！请稍候再试！",2,"");
						}else{
							T.loadTip(1,"添加失败！",2,"");
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		});
	}});
	return bts;
}
//查看,添加,编辑,删除,修改密码
function getAuthIsoperateButtons(){
	var bts = [];
	bts.push({name:"编辑",fun:function(id){
		T.each(_cameraT.tc.tableitems,function(o,j){
			o.fieldvalue = _cameraT.GD(id)[j]
		});
		Twin({Id:"camera_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "camera_edit_f",
					formObj:tObj,
					recordid:"camera_id",
					suburl:"worktime.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_cameraT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("camera_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("camera_edit_"+id);
							_cameraT.M()
						}else{
							T.loadTip(1,ret,2,o)
						}
					}
				});	
			}
		})
	}});
	bts.push({name:"删除",fun:function(id){
		var id_this = id ;
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("worktime.do?action=delete","post","id="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_cameraT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}});
	}});
	if(bts.length <= 0){return false;}
	return bts;
}
_cameraT.C();
</script>

</body>
</html>
