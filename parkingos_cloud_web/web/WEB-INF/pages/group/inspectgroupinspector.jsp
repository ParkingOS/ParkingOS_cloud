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
<div id="workgroupinspectorobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var work_group_id = "${work_group_id}";
//var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField=[
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"50" ,height:"",edit:false,issort:false,hide:true,shide:true},
		{fieldcnname:"巡查员名称",fieldname:"nickname",fieldvalue:'',inputtype:"text",twidth:"200" ,height:"",issort:false},
		];
	
var _workgroupinspectorT = new TQTable({
	tabletitle:"巡查员管理",
	ischeck:false,
	tablename:"workgroupinspector_tables",
	dataUrl:"inspectgroupmanage.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=inspectorquery&work_group_id="+work_group_id,
	tableObj:T("#workgroupinspectorobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	bts.push({dname:"添加巡查员",icon:"edit_add.png",onpress:function(Obj){
				T.each(_workgroupinspectorT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
		     Twin({
			Id:"emolyee_detail",
			Title:"保存巡查员  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
			Content:"<iframe src=\"inspectgroupmanage.do?action=saveinspector&work_group_id="+work_group_id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
			Width:T.gww()-100,
			Height:T.gwh()-50
		})
	}});
		
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("inspectgroupmanage.do?action=deleteinspector","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_workgroupinspectorT.C();
				}else{
					T.loadTip(1,ret,2,"");
					_workgroupinspectorT.C();
				}
			}
		)}})
	}});
	
	if(bts.length <= 0){return false;}
	return bts;
}


_workgroupinspectorT.C();
</script>

</body>
</html>
