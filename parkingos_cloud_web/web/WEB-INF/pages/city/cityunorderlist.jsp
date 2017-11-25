<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>在场车辆</title>
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
var paytype=[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"自动结算"},{"value_no":1,"value_name":"手动结算"}];
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
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
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"80" ,height:"",hide:true},
		{fieldcnname:"所属集团",fieldname:"groupid",fieldvalue:'',inputtype:"cselect",noList:groups,target:"comid",action:"getparks",twidth:"170" ,height:"",issort:false,shide:ishiddlegroup,fhide:ishiddlegroup},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,target:"in_passid,berthsec_id",action:"getcompass,getberthseg",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"150" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'berthsec_id');
			}},
		{fieldcnname:"泊位编号",fieldname:"cid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:true},
		{fieldcnname:"进场收费员",fieldname:"uid",fieldvalue:'',inputtype:"text",twidth:"80" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'uid');
			}},
		{fieldcnname:"预付金额",fieldname:"prepaid",fieldvalue:'',inputtype:"number",twidth:"100", height:"",issort:false,issort:true,
			process:function(value,cid,id){
				if(value <= 0){
					return "";
				}else{
					return value;
				}
			}},
		{fieldcnname:"车场类型",fieldname:"parking_type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"地面"},{"value_no":1,"value_name":"地下"},{"value_no":2,"value_name":"占道"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"进场方式",fieldname:"c_type",fieldvalue:'',inputtype:"select",noList:[{"value_no":"-1","value_name":"全部"},{"value_no":"","value_name":"通道扫牌"},{"value_no":"0","value_name":"NFC刷卡"},{"value_no":"1","value_name":"Ibeacon"},{"value_no":"2","value_name":"手机扫牌"},{"value_no":"3","value_name":"通道扫牌"},{"value_no":"4","value_name":"直付"},{"value_no":"5","value_name":"月卡"},{"value_no":"6","value_name":"车位二维码"},{"value_no":"7","value_name":"月卡第二辆车"}] ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车牌号",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"进场时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",hide:true,shide:true},
		{fieldcnname:"时长",fieldname:"duration",fieldvalue:'',inputtype:"text", twidth:"140" ,height:"",issort:true,shide:true},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"未支付"},{"value_no":1,"value_name":"已支付"},{"value_no":2,"value_name":"逃单"}] ,twidth:"100" ,height:"",issort:false,shide:true},
		{fieldcnname:"查看车辆图片",fieldname:"id",inputtype:"text", twidth:"100",issort:false,shide:true,
			process:function(value,cid,id){
				return "<a href=# onclick=\"viewdetail('"+value+"','"+cid+"')\" style='color:blue'>查看车辆图片</a>";
			}},
		{fieldcnname:"进场通道",fieldname:"in_passid",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"160" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'in_passid');
			}},
        {fieldcnname:"车场订单编号",fieldname:"order_id_local",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",hidden:true}

];

var _orderT = new TQTable({
	tabletitle:"在场车辆",
	//ischeck:false,
	tablename:"order_tables",
	dataUrl:"cityunorder.do",
	iscookcol:false,
	//dbuttons:false,
	quikcsearch:coutomsearch(),
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&btime="+btime+"&etime="+etime,
	tableObj:T("#orderobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isidentifier:false,
	isoperate:getAuthIsoperateButtons()
});

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
	/*if(subauth[1])
		authButs.push({dname:"0元结算",icon:"toxls.gif",onpress:function(Obj){
		var sids = _orderT.GS();
		//var a = _orderT.GSByField("c_type");
		var ids="";
		if(!sids){
			T.loadTip(1,"请先选择订单",2,"");
			return;
		}
		Tconfirm({Title:"确认0元结算订单吗?",Content:"确认0元结算所选订单吗?",OKFn:function(){
			T.A.sendData("cityunorder.do?action=completezeroorder","post","ids="+sids,
		function complete(ret){
			T.loadTip(1,"结算成功"+ret+"条",2,"");
			location.reload();
		}
		)}})
	}})*/
	return authButs;
}
function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}
function coutomsearch(){
	var html = "&nbsp;&nbsp;&nbsp;&nbsp;进场时间：&nbsp;&nbsp;<input id='coutom_btime' class='Wdate' align='absmiddle' readonly value='"+btime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 00:00:00',alwaysUseStartDate:false});\"/>"
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
	var groupid = '';
	if(!ishiddlegroup)
		groupid = T("#groups").value;
	_orderT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&btime="+btime+"&etime="+etime+"&groupid_start="+groupid
	});
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
	addgroups();
	if(!ishiddlegroup)
		T("#groups").value = groupid;
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

_orderT.C();
</script>

</body>
</html>
