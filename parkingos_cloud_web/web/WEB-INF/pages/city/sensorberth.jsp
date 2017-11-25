<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>车检器监测</title>
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
		<a href="sensorberth.do?action=sensorgraph" class="sel_fee" style="margin-left:10px;">图形</a><a class="sel_fee column" style="margin-left:-1px;">列表</a>
	</ul>
</div>

<!--顶部结束-->
<div id="cityberthobj" style="width:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var sensor_states = [{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"心跳正常"},{"value_no":"1","value_name":"心跳异常"}];
var states = [{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"空闲"},{"value_no":"1","value_name":"在停"}];
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false,shide:true,hide:true,fhide:true},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",twidth:"90",noList:parks,target:"berthsec_id",action:"getberthseg",height:"",issort:false},
		{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"120" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'berthsec_id');
			}},
		{fieldcnname:"泊位编号",fieldname:"cid",fieldvalue:'',inputtype:"text",twidth:"80" ,height:""},
		{fieldcnname:"车检器编号",fieldname:"did",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车检器电压",fieldname:"battery",fieldvalue:'',inputtype:"text", twidth:"90" ,height:"",hide:true,shide:true},
		{fieldcnname:"车检器状态",fieldname:"sensor_state",fieldvalue:'',inputtype:"select",noList:sensor_states, twidth:"90" ,height:"",hide:true,
			process:function(value,trId,colId){
				if(value==1)
					return "<font color='red'>心跳异常</font>";
				else 
					return "心跳正常";
			}},
		{fieldcnname:"最近心跳时间",fieldname:"heartbeat_time",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",hide:true,shide:true},
		{fieldcnname:"占用状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:states, twidth:"90" ,height:"",hide:true,
			process:function(value,trId,colId){
				if(value==1)
					return "<font color='#1BA39C'>在停</font>";
				else 
					return "空闲";
			}},
		{fieldcnname:"车检器检测入场时间",fieldname:"sensor_in_time",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",shide:true},
		{fieldcnname:"POS机记录入场时间",fieldname:"order_in_time",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",edit:false,hide:true,shide:true},
		{fieldcnname:"车牌号",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",edit:false,hide:true,shide:true,issort:false}
	];
var rules =[
		{name:"berthsec_id",type:"",url:"",requir:true,warn:"不能为空!",okmsg:""}
		];
var _cityberthT = new TQTable({
	tabletitle:"",
	ischeck:false,
	tablename:"cityberth_tables",
	dataUrl:"sensorberth.do",
	iscookcol:false,
	isodbyserver:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#cityberthobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityberthT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"sensor_search_w",Title:"搜索",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "sensor_search_f",
					formObj:tObj,
					formWinId:"sensor_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("sensor_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_cityberthT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
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

function setcname(value,pid,colname){
	if(value&&value!='-1'&&value!=''){
		var url = "";
		if(colname == "berthsec_id"){
			url = "cityberthseg.do?action=getberthseg&id="+value;
		}else if(colname == "dici_id"){
			url = "cityberth.do?action=getdici&id="+value;
		}
		T.A.C({
			url:url,
    		method:"GET",//POST or GET
    		param:"",//GET时为空
    		async:false,//为空时根据是否有回调函数(success)判断
    		dataType:"0",//0text,1xml,2obj
    		success:function(ret,tipObj,thirdParam){
    			if(ret){
					updateRow(pid,colname,ret);
    			}
				else
					updateRow(pid,colname,value);
			},//请求成功回调function(ret,tipObj,thirdParam) ret结果
    		failure:function(ret,tipObj,thirdParam){
				return false;
			},//请求失败回调function(null,tipObj,thirdParam) 默认提示用户<网络失败>
    		thirdParam:"",//回调函数中的第三方参数
    		tipObj:null,//相关提示父级容器(值为字符串"notip"时表示不进行相关提示)
    		waitTip:"正在获取名称...",
    		noCover:true
		})
	}else{
		return "无"
	};
	return "<font style='color:#666'>获取中...</font>";
}

/*更新表格内容*/
function updateRow(rowid,name,value){
	//alert(value);
	if(value)
	_cityberthT.UCD(rowid,name,value);
}

_cityberthT.C();
</script>
<script type="text/javascript">
var height = document.body.clientHeight;
document.getElementById("cityberthobj").style.height = (height - 40 - 15)+"px";
</script>
</body>
</html>
