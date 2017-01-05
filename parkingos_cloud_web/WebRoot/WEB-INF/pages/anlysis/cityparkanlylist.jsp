<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>停车场分布情况分析</title>
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
<style type="text/css">
.title1{
	width: 100%;
    margin: 0 auto;
    font-weight: bold;
    line-height: 25px;
    height: 40px;
    background: #EFEFEF;
    padding-top: 15px;
    border-bottom: 1px solid #ddd;
    font-size:16px !important;
}
.sel_fee{
	text-align: center;
    padding-top: 2px;
    padding-bottom: 2px;
    border-radius: 0px;
    background-color: #FFFFFF;
    outline: medium;
    border: 1px solid #5CCDBE;
    color: #5CCDBE;
    padding-left: 8px;
    padding-right: 8px;
}
.title1 a:hover{
	background:#5CCDBE;
	color:#FFFFFF;
}

.column{
	background:#5CCDBE;
	color:#FFFFFF;
}
a:link {
    color: #5CCDBE;
    text-decoration: none;
}
</style>
</head>
<body>
<!--顶部开始-->
<div class="top">
	<ul class="title1">
		<a href="cityparkanly.do" class="sel_fee" style="margin-left:10px;">图形</a><a class="sel_fee column" style="margin-left:-1px;">列表</a>
	</ul>
</div>
<!--顶部结束-->
<div id="cityobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/

var _mediaField = [
		{fieldcnname:"城市编号",fieldname:"city",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false,fhide:true},
		{fieldcnname:"行政区名称",fieldname:"local",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"道路停车场数量",fieldname:"road",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"封闭停车场数量",fieldname:"indoor",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"泊位数量",fieldname:"psum",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false}
	];
var _cityT = new TQTable({
	tabletitle:"",
	ischeck:false,
	tablename:"city_tables",
	dataUrl:"cityparkanly.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=querylist",
	tableObj:T("#cityobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}


_cityT.C();
</script>

</body>
</html>
