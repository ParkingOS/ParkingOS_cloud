<%@ page language="java" pageEncoding="gb2312"%>
<html lang="">
<head>
<meta http-equiv="Content-Type" content="text/html" charset="gb2312"/>
<!-- <meta http-equiv="X-UA-Compatible" content="IE=edge"> -->
<title>概况</title>
<style type="text/css">
	.topdiv{height:30px;
		font-size:17px;
		font-weight:700;
		line-height:25px;
		font-family:"微软雅黑";
		vertical-align:middle;
		background-image: url(images/page_top_bg.png)}
	.topmesg{height:40px;
		font-size:17px;
		font-weight:700;
		line-height:30px;
		margin-left:20px;
		font-family:"微软雅黑";
		vertical-align:middle;}
	#parktable th{height:35px;border-left:1px solid #BDBDBD;border-bottom:1px solid #BDBDBD;background-color:#FAFAFA;font-family:"微软雅黑";color:#232323};
	#devtable th{height:35px;border-left:1px solid #BDBDBD;border-bottom:1px solid #BDBDBD;background-color:#FAFAFA;font-family:"微软雅黑";color:#232323};
</style>
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
<body id="body" style="overflow-y:auto">
<div id='parktable' style=''>
		<div class='topdiv' style='display:block'>
			<img src='images/park_table.png' style='height:20px;margin-left:10px;margin-right:10px;vertical-align:middle;'/>
			<span id ='top-tile' >车场收费概况</span>
		</div>
		<div id ='pdatatable' style='display:block;margin:1px auto'>
		</div>
	</div>
<div id='devtable' style=''>
		<div class='topdiv' style='display:block'>
			<img src='images/park_table.png' style='height:20px;margin-left:10px;margin-right:10px;vertical-align:middle;'/>人员设备概况
			&nbsp;&nbsp;&nbsp;&nbsp;车场--><span id="company_name">${company_name}</span>
		</div>
		<div id ='devdata' style='display:block;margin:1px auto'>
		</div>
	</div>
</body>
<script>
var getobj=function(id){return document.getElementById(id)};
var h = getobj('body').offsetHeight;
var w = getobj('body').offsetWidth;
var groupid ='${groupid}';
//alert(groupid);
var unionId = '${unionId}';
var custumgroup = '${custumgroup}';
//alert(unionId);
function setobjCss(obj,css){
	for(var c in css){
		try{obj.style[c]=css[c];}catch(e){};
	}
}
setobjCss(getobj('parktable'),{'margin':'10px auto','width':(parseInt(w)-4)+'px','height':'45%','border':'1px solid #abcdef','borderRadius':'5px'});
setobjCss(getobj('devtable'),{'margin':'10px auto','width':(parseInt(w)-4)+'px','height':'45%','border':'1px solid #abcdef','borderRadius':'5px'});
var tdw=parseInt(w*0.111)-1;
var tdw1=parseInt(w*0.49)-1;
var _mediaField1 = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:tdw1 ,height:"30",edit:false,issort:false},
		{fieldcnname:"车场名",fieldname:"company_name",fieldvalue:'',inputtype:"text",twidth:tdw1 ,height:"30",issort:false,
			process:function(value,cid,id){
						return "<a href=# onclick=\"viewdev('"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
					}}]

var _mediaField2 = [
    {fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:tdw ,height:"30",edit:false,issort:false},
    {fieldcnname:"车场名",fieldname:"company_name",fieldvalue:'',inputtype:"text",twidth:tdw ,height:"30",issort:false,
        process:function(value,cid,id){
            return "<a href=# onclick=\"viewdev('"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
        }}
    ,

    {fieldcnname:"今日共收费",fieldname:"atotal",fieldvalue:'',inputtype:"text",twidth:tdw ,height:"",issort:false,
        process:function(value,cid,id){
            return "<a href=# onclick=\"viewdetail('"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
        }},
    {fieldcnname:"电子收费",fieldname:"etotal",fieldvalue:'',inputtype:"text",twidth:tdw ,height:"30",issort:false},
    {fieldcnname:"现金收费",fieldname:"ctotal",fieldvalue:'',inputtype:"text",twidth:tdw ,height:"30",issort:false},
    {fieldcnname:"月卡订单数",fieldname:"mtotal",fieldvalue:'',inputtype:"text",twidth:tdw ,height:"30",issort:false},
    {fieldcnname:"总车位",fieldname:"parking_total",fieldvalue:'',inputtype:"text",twidth:tdw ,height:"30",issort:false},
    {fieldcnname:"空余车位",fieldname:"rlots",fieldvalue:'',inputtype:"text",twidth:tdw ,height:"30",issort:false,
        process:function(value,cid,id){
            return "<a href=# onclick=\"viewdetail('"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
        }},
    {fieldcnname:"车位利用率",fieldname:"ratio",fieldvalue:'',inputtype:"text",twidth:tdw ,height:"30",issort:false}
]
var _mediaField=_mediaField2;
if(groupid!=''&&custumgroup.indexOf(groupid)!=-1&&unionId=='200081') {
    _mediaField=_mediaField1;
}

var _parksurveyT = 
	new TQTable({
		ischeck:false,
		tablename:"parksurvey_tables",
		dataUrl:"gsurvey.do",
		iscookcol:false,
		buttons:false,
		dbuttons:false,
		searchitem:false,
		param:"action=psurvey",
		tableObj:T("#pdatatable"),
		fit:[false],
		isidentifier:false,
		//allowpage:false,
		autoH:true,
		tableitems:_mediaField
	});
	
function viewdetail(value,id){
	var park = _parksurveyT.GD(id,"company_name");
	Twin({
		Id:"parkorder_detail_"+id,
		Title:"车场："+park,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='parkorder_detail_'"+id+" id='parkorder_detail_'"+id+" src='survey.do?comid="+id+"' width='100%' height='100%' frameborder='0' scrolling='no' style='overflow:hidden;' ></iframe>"
	})
}

function viewdev(value,id){
	var park = _parksurveyT.GD(id,"company_name");
	survey_T.C({
		cpage:1,
//		tabletitle:"今天统计",
		extparam:"&action=query&comid="+id
	})
	getobj("company_name").innerHTML=park;
	resetPage1();
}
_parksurveyT.C();


//表格重置
function resetPage(){
	try{
		getobj("parksurvey_tables_tooles_div").style.display='none';
		getobj("parksurvey_tables_footer_div").style.display='none';
		getobj("parksurvey_tables_body_div").style.height='100%';
		getobj("parksurvey_tables_body_innerdiv").style.overFlowX='hiddle';
		getobj("parksurvey_tables_header_div").firstChild.style.textAlign='center';
	}catch(e){};
	var obj = getobj("parksurvey_tables_body");
	if(obj){
		try{
			if(obj.style.textAlign!='center')
				obj.style.textAlign='center';
		}catch(e){};
	}else{
		setTimeout(resetPage, 100);
	}
}

resetPage();

var tdw2=parseInt(w*0.1428)-1;
var survey_T = 
	new TQTable({
		ischeck:false,
		tablename:"survey_info",
		dataUrl:"survey.do",
		iscookcol:false,
		buttons:false,
		dbuttons:false,
		searchitem:false,
		param:"action=query&comid=${comid}",
		tableObj:T("#devdata"),
		fit:[false],
		isidentifier:false,
		//allowpage:false,
		autoH:true,
		tableitems:[
			{fieldcnname:"编号",fieldname:"uid",fieldvalue:'',inputtype:"text", twidth:tdw2 ,height:"30",issort:false},
			{fieldcnname:"通道",fieldname:"passname",fieldvalue:'',inputtype:"text",twidth:tdw2 ,height:"30",issort:false},
			{fieldcnname:"在岗",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:tdw2 ,height:"30",issort:false},
			{fieldcnname:"工作站",fieldname:"server",fieldvalue:'',inputtype:"text", twidth:tdw2 ,height:"30",issort:false,
				process:function(value,cid,id){
					if(value=='1')
						return "<img src='images/light_green.png'/>";
					else 
						return "<img src='images/light_red.png'/>";
				}
			},
			{fieldcnname:"摄像机",fieldname:"carm",fieldvalue:'',inputtype:"text", twidth:tdw2 ,height:"30",issort:false,
				process:function(value,cid,id){
					if(value=='1')
						return "<img src='images/light_green.png'/>";
					else 
						return "<img src='images/light_red.png'/>";
				}
			},
			{fieldcnname:"LED显示屏",fieldname:"led",fieldvalue:'',inputtype:"text", twidth:tdw2 ,height:"30",issort:false,
				process:function(value,cid,id){
					if(value=='1')
						return "<img src='images/light_green.png'/>";
					else 
						return "<img src='images/light_red.png'/>";
				}},
			{fieldcnname:"道闸",fieldname:"brake",fieldvalue:'',inputtype:"text",twidth:tdw2 ,height:"30",issort:false,
				process:function(value,cid,id){
					if(value=='1')
						return "<img src='images/light_green.png'/>";
					else 
						return "<img src='images/light_red.png'/>";
				}
			}
		]
	});
survey_T.C();
if(groupid!=''&&custumgroup.indexOf(groupid)!=-1&&unionId=='200081') {
   document.getElementById("devtable").style.display='none';
    document.getElementById("parktable").style.height='95%';
    document.getElementById("top-tile").innerHTML='渣土车车场概况';

}
//表格重置
function resetPage1(){
	try{
		getobj("survey_info_tooles_div").style.display='none';
		getobj("survey_info_footer_div").style.display='none';
		getobj("survey_info_body_div").style.height='100%';
		getobj("survey_info_body_innerdiv").style.overFlowX='hiddle';
		getobj("survey_info_header_div").firstChild.style.textAlign='center';
	}catch(e){};
	var obj = getobj("survey_info_body");
	if(obj){
		try{
			if(obj.style.textAlign!='center')
				obj.style.textAlign='center';
		}catch(e){};
	}else{
		setTimeout(resetPage1, 100);
	}
}

resetPage1();
</script>
</html>