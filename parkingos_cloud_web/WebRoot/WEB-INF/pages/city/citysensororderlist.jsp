<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>车检器订单</title>
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
<script src="js/jquery.js" type="text/javascript"></script>
<style type="text/css">
.sel_fee{
	text-align: center;
    /* padding-top: 0px; */
    /* padding-bottom: 0px; */
    border-radius: 0px;
    background-color: #FFFFFF;
    outline: medium;
    border: 1px solid #5CCDBE;
    color: #5CCDBE;
    padding-left: 8px;
    padding-right: 8px;
    font-size: 12px;
    height: 24px;
    margin-top: 3px;
    line-height: 24px;
}
a:hover{
	background:#5CCDBE;
	color:#FFFFFF;
}
</style>
</head>
<body onload='addgroups()'>
<div id="orderobj" style="width:100%;height:100%;margin:0px;"></div>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<form action="" method="post" id="choosecom"></form>
<script language="javascript">
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var states = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"未出场"},{"value_no":1,"value_name":"已出场"}];
var cityid = '${cityid}';
var btime="${btime}";
var etime="${etime}";
var groups = [];
var ishiddlegroup = true;
if(cityid!=''){
	groups = eval(T.A.sendData("getdata.do?action=getgroups&cityid=${cityid}"));
	ishiddlegroup = false;
}
var _mediaField = [
		{fieldcnname:"订单编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",twidth:"150",noList:parks,target:"berthsec_id",action:"getberthseg",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"150" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'berthsec_id');
			}},
		{fieldcnname:"泊位编号",fieldname:"cid",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:true},
		{fieldcnname:"车检器编号",fieldname:"berth_id",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"进场收费员账号",fieldname:"in_uid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
				if(value == "-1"){
					return "";
				}else{
					return value;
				}
			}},
		{fieldcnname:"进场收费员名称",fieldname:"in_collector",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,shide:true},
		{fieldcnname:"出场收费员账号",fieldname:"out_uid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
				if(value == "-1"){
					return "";
				}else{
					return value;
				}
			}},
		{fieldcnname:"出场收费员名称",fieldname:"out_collector",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,shide:true},
		{fieldcnname:"车检器进场时间",fieldname:"in_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",shide:true},
		{fieldcnname:"车检器出场时间",fieldname:"out_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:true},
		{fieldcnname:"车检器订单时长",fieldname:"dura",fieldvalue:'',inputtype:"text", twidth:"140" ,height:"",issort:true,shide:true},
		{fieldcnname:"车检器应收金额",fieldname:"total",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
		{fieldcnname:"POS机实收金额",fieldname:"order_total",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
		{fieldcnname:"车检器订单状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList: states,twidth:"100" ,height:"",issort:true},
		{fieldcnname:"Pos机订单编号",fieldname:"orderid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				if(value == "-1"){
					return "";
				}else{
					return value;
				}
			}},
		{fieldcnname:"Pos机订单进场时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false,shide:true},
		{fieldcnname:"Pos机订单出场时间",fieldname:"end_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false,shide:true},
		{fieldcnname:"Pos机订单车牌号",fieldname:"car_number",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false}
	];
var _orderT = new TQTable({
	tabletitle:"车检器订单",
	ischeck:false,
	tablename:"order_tables",
	dataUrl:"citysensororder.do",
	iscookcol:false,
	//dbuttons:false,
	quikcsearch:coutomsearch(),
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&btime="+btime+"&etime="+etime,
	tableObj:T("#orderobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
	var html = "&nbsp;&nbsp;&nbsp;&nbsp;车检器进场时间：&nbsp;&nbsp;<input id='coutom_btime' class='Wdate' align='absmiddle' readonly value='"+btime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 00:00:00',alwaysUseStartDate:false});\"/>"
	+" - <input id='coutom_etime' class='Wdate' align='absmiddle' readonly value='"+etime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 23:59:59',alwaysUseStartDate:false});\"/>";
	if(!ishiddlegroup)
		html+="&nbsp;&nbsp;&nbsp;&nbsp;所属集团：&nbsp;&nbsp;<select style='width:130px' id='groups'></select>";
	html += "&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
	return html;
}
function addgroups(){
	if(ishiddlegroup)
		return ;
	var childs = groups;
	var groupselect = document.getElementById("groups");
	for(var i=0;i<childs.length;i++){
		var child = childs[i];
		var id = child.value_no;
		var name = child.value_name;
		groupselect.options.add(new Option(name, id));
	}
}
function searchdata(){
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;
	var groupid ="";
	if(!ishiddlegroup)
		groupid =T("#groups").value;
	_orderT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&groupid="+groupid+"&btime="+btime+"&etime="+etime,
	});
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
	addgroups();
	if(!ishiddlegroup)
		T("#groups").value = groupid;
}
function getAuthButtons(){
	var authButs = [];
	if(subauth[0])
		authButs.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_orderT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"order_search_w",Title:"搜索订单",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "order_search_f",
					formObj:tObj,
					formWinId:"order_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("order_search_w");} }
					],
					SubAction:
					function(callback,formName){
						btime =  T("#coutom_btime").value;
						etime =  T("#coutom_etime").value;
						_orderT.C({
							cpage:1,
							tabletitle:"高级搜索结果&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${parkinfo}",
							extparam:"action=query&btime="+btime+"&etime="+etime+"&"+Serializ(formName)
						});
						T("#coutom_btime").value=btime;
						T("#coutom_etime").value=etime;
						addgroups();
					}
				});	
			}
		})
	
	}});
	return authButs;
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
		_orderT.UCD(rowid,name,value);
}

_orderT.C();
</script>

</body>
</html>
