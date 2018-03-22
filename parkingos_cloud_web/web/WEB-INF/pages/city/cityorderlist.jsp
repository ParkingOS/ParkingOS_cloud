<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>订单记录</title>
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
<body>
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
var btime="${btime}";
var etime="${etime}";
var collector="";
var comid = "";
/*权限*/
var paytype=[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}];
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
//var paytypes = eval(T.A.sendData("getdata.do?action=getkeyvalues&name=orderpaytype&cityid=${cityid}"));
var paytypes = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"账户支付"},{"value_no":1,"value_name":"现金支付"},{"value_no":2,"value_name":"手机支付"},{"value_no":3,"value_name":"包月"},{"value_no":4,"value_name":"中央预支付现金"},{"value_no":5,"value_name":"中央预支付银联卡"},{"value_no":6,"value_name":"中央预支付商家卡"},{"value_no":8,"value_name":"免费"},{"value_no":9,"value_name":"刷卡"}];
var states = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"未支付"},{"value_no":1,"value_name":"已支付"},{"value_no":2,"value_name":"逃单"}];
var cityid = '${cityid}';
var groups = [];
var ishiddlegroup = true;
if(cityid!=''){
	groups = eval(T.A.sendData("getdata.do?action=getgroups&cityid=${cityid}"));
	ishiddlegroup = false;
}

var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"80" ,height:""},
		{fieldcnname:"所属集团",fieldname:"groupid",fieldvalue:'',inputtype:"cselect",noList:groups,target:"comid",action:"getparks",twidth:"170" ,height:"",issort:false,shide:ishiddlegroup,fhide:ishiddlegroup},
		{fieldcnname:"车场名称",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,twidth:"150",target:"in_passid,out_passid,freereasons,berthsec_id",action:"getcompass,getcompass,getfreereasons,getberthseg",twidth:"100" ,height:"",issort:false},
		//{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"150" ,height:"",issort:false,
		//	process:function(value,pid){
		//		return setcname(value,pid,'berthsec_id');
		//	}},
		//{fieldcnname:"泊位编号",fieldname:"cid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:true},
		{fieldcnname:"收款人账号",fieldname:"out_uid",fieldvalue:'',inputtype:"text",twidth:"80" ,height:"",issort:false},
		{fieldcnname:"收款人名称",fieldname:"collector",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,shide:true},
		{fieldcnname:"进场方式",fieldname:"c_type",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车牌号",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"进场时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",hide:true},
		{fieldcnname:"出场时间",fieldname:"end_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:true,shide:true},
		{fieldcnname:"时长",fieldname:"duration",fieldvalue:'',inputtype:"text", twidth:"140" ,height:"",issort:true,shide:true},
        {fieldcnname:"支付方式",fieldname:"pay_type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"账户支付"},{"value_no":1,"value_name":"现金支付"},{"value_no":2,"value_name":"手机支付"},{"value_no":3,"value_name":"包月"},{"value_no":4,"value_name":"中央预支付现金"},{"value_no":5,"value_name":"中央预支付银联卡"},{"value_no":6,"value_name":"中央预支付商家卡"},{"value_no":8,"value_name":"免费"},{"value_no":9,"value_name":"刷卡"}] ,twidth:"80" ,height:"",issort:true},
        {fieldcnname:"应收金额",fieldname:"amount_receivable",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
        {fieldcnname:"实收金额",fieldname:"total",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
        {fieldcnname:"电子预付金额",fieldname:"electronic_prepay",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
        {fieldcnname:"现金预付金额",fieldname:"cash_prepay",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
        {fieldcnname:"电子结算金额",fieldname:"electronic_pay",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
        {fieldcnname:"现金结算金额",fieldname:"cash_pay",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
        {fieldcnname:"减免金额",fieldname:"reduce_amount",fieldvalue:'',inputtype:"number", height:"",issort:false,issort:true},
        {fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},
        {"value_no":0,"value_name":"未结算"},{"value_no":1,"value_name":"已结算"},{"value_no":2,"value_name":"逃单"}] ,twidth:"100" ,height:"",issort:true},
        {fieldcnname:"结算方式",fieldname:"isclick",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"查看车辆图片",fieldname:"id",inputtype:"text", twidth:"100",issort:false,shide:true,
			process:function(value,cid,id){
				return "<a href=# onclick=\"viewdetail('"+value+"','"+cid+"')\" style='color:blue'>查看车辆图片</a>";
			}},
		{fieldcnname:"进场通道",fieldname:"in_passid",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"160" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'in_passid');
			}},
		{fieldcnname:"出场通道",fieldname:"out_passid",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"160" ,height:"",issort:false,
		process:function(value,pid){
			return setcname(value,pid,'out_passid');
		}},
        {fieldcnname:"车场订单编号",fieldname:"order_id_local",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",hidden:true}

];

var _excelField = [
   		{fieldcnname:"进场时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"200" ,height:"",hide:true},
   		{fieldcnname:"金额",fieldname:"total",fieldvalue:'',inputtype:"number", height:"",issort:false},
   		{fieldcnname:"结算方式",fieldname:"isclick",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}],twidth:"100" ,height:"",issort:false},
        {fieldcnname:"车场名称",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"150",twidth:"100" ,height:"",issort:false}
   	];
var back = "";
if("${from}" == "index"){
	back = "<a href='cityindex.do?authid=${index_authid}' class='sel_fee' style='float:right;margin-right:20px;'>返回</a>";
}
var _orderT = new TQTable({
	tabletitle:"订单记录<font style='font-size:14px;'>（已结算订单）<span id='total_money'></span></font>"+back,
	ischeck:false,
	tablename:"order_tables",
	dataUrl:"cityorder.do",
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
	var html = "&nbsp;&nbsp;&nbsp;&nbsp;出场时间：&nbsp;&nbsp;<input id='coutom_btime' class='Wdate' align='absmiddle' readonly value='"+btime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 00:00:00',alwaysUseStartDate:false});\"/>"
	+" - <input id='coutom_etime' class='Wdate' align='absmiddle' readonly value='"+etime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 23:59:59',alwaysUseStartDate:false});\"/>&nbsp;&nbsp;收款人:&nbsp;&nbsp;<select style='width:130px' id='collectors'></select>"+
	"&nbsp;&nbsp;车场:&nbsp;&nbsp;<select style='width:130px' id='parks'></select>";
	if(!ishiddlegroup)
		html+="&nbsp;&nbsp;&nbsp;&nbsp;所属集团：&nbsp;&nbsp;<select style='width:130px' id='groups'></select>";
	html += "&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
	return html;
}

function searchdata(){
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;
	collector = T("#collectors").value;
	comid = T("#parks").value;
	var groupid = -1;
	if(!ishiddlegroup)
		 groupid=T("#groups").value;
	_orderT.C({
		cpage:1,
		tabletitle:"搜索结果<font style='font-size:14px;'>（已结算订单）<span id='total_money'></span></font>",
		extparam:"&action=query&btime="+btime+"&etime="+etime+"&out_uid="+collector+"&comid_start="+comid+"&groupid_start="+groupid
	});
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
	addcollectors();
	addparks();
	addgroups();
	T("#collectors").value = collector;
	T("#parks").value = comid;
	if(!ishiddlegroup)
		T("#groups").value=groupid;
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
							tabletitle:"高级搜索结果&nbsp;&nbsp;&nbsp;<span id='total_money'></span></font>"+back,
							extparam:"action=query&btime="+btime+"&etime="+etime+"&"+Serializ(formName)
						});
						T("#coutom_btime").value=btime;
						T("#coutom_etime").value=etime;
						addcollectors();
						addparks();
						addgroups();

					}
				});
			}
		})

	}});
	if(subauth[1])
	authButs.push({dname:"导出订单",icon:"toxls.gif",onpress:function(Obj){
		Twin({Id:"order_export_w",Title:"导出订单<font style='color:red;'>（如果没有设置，默认全部导出!）</font>",Width:480,sysfun:function(tObj){
				 TSform ({
					formname: "order_export_f",
					formObj:tObj,
					formWinId:"order_export_w",
					formFunId:tObj,
					dbuttonname:["确认导出"],
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_excelField}],
					}],
					//formitems:[{kindname:"",kinditemts:_excelField}],
					SubAction:
					function(callback,formName){
						T("#exportiframe").src="cityorder.do?action=exportExcel&fieldsstr=id__comid__c_type__car_number__create_time__end_time__duration__pay_type__total__uid__state__isclick__id__in_passid__out_passid&"+Serializ(formName)
						TwinC("order_export_w");
						T.loadTip(1,"正在导出，请稍候...",2,"");
					}
				});
			}
		})
	}})

	return authButs;
}
function getAuthIsoperateButtons(){
	var bts = [];

	if(bts.length <= 0){return false;}
	return bts;
}

function viewdetail(value,id){
    var car_number =_orderT.GD(id,"car_number");
    var comid = _orderT.GD(id,"comid");
    var orderIdLocal =_orderT.GD(id,"order_id_local");
    var tip = "车辆图片";
    Twin({
        Id:"carpics_detail_"+id,
        Title:tip+"  --> 车牌："+car_number,
        Width:T.gww()-100,
        Height:T.gwh()-50,
        sysfunI:id,
        //Content:"<iframe name='carpics_detail_'"+id+" id='carpics_detail_'"+id+" src='order.do?action=carpics&orderid="+id+"&comid="+comid+"&r="+Math.random()+"' width='100%' height='"+(T.gwh()-100)+"' frameborder='0' style='overflow:auto;' ></iframe>"
        Content:"<iframe name='carpics_detail_'"+id+" id='carpics_detail_'"+id+" src='order.do?action=carpicsnew&comid="+comid+"&orderid="+orderIdLocal+"&r="+Math.random()+"' width='100%' height='"+(T.gwh()-100)+"' frameborder='0' style='overflow:auto;' ></iframe>"
    })
}

function setcname(value,pid,colname){
	var url = "";
	if(colname == "uid"){
		url = "cityorder.do?action=getcollname&id="+value;
	}else if(colname == "freereasons"){
		url = "cityorder.do?action=getfreereason&id="+value;
	}else if(colname == "in_passid" || colname == "out_passid"){
		url = "cityorder.do?action=getpassname&id="+value;
	}else if(colname == "berthsec_id"){
		url = "cityberthseg.do?action=getberthseg&id="+value;
	}
	if(value&&value!='-1'&&value!=''){
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

function addcollectors(){
	var childs = eval(T.A.sendData("cityorder.do?action=getcollectors"));
	jQuery("#collectors").empty();
	for(var i=0;i<childs.length;i++){
		var child = childs[i];
		var id = child.value_no;
		var name = child.value_name;
		jQuery("#collectors").append("<option value='"+id+"'>"+name+"</option>");
	}
}
function addparks(){
	var childs = parks;
	jQuery("#parks").empty();
	for(var i=0;i<childs.length;i++){
		var child = childs[i];
		var id = child.value_no;
		var name = child.value_name;
		jQuery("#parks").append("<option value='"+id+"'>"+name+"</option>");
	}
}
function addgroups(){
	if(ishiddlegroup)
		return;
	var childs = groups;
	var groupselect = document.getElementById("groups");
	for(var i=0;i<childs.length;i++){
		var child = childs[i];
		var id = child.value_no;
		var name = child.value_name;
		groupselect.options.add(new Option(name, id));
	}
}
_orderT.C();
addcollectors();
addparks();
addgroups();
</script>

</body>
</html>
