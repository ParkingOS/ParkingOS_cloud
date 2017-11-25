<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>未缴管理</title>
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
<body onload='addgroups()'>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<div id="parkescapeobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
var role=${role};
/*权限*/
var issupperadmin=${supperadmin};
var isadmin = ${isadmin};
var authlist ="";
if((issupperadmin&&issupperadmin==1) || (isadmin&&isadmin==1))
	authlist="0,1,2";
else
	authlist= T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var stateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"未处理"},{"value_no":1,"value_name":"已处理"}];
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
/*权限*/
var comid = ${comid};
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false,fhide:true,hide:true,shide:true},
		{fieldcnname:"所属集团",fieldname:"groupid",fieldvalue:'',inputtype:"cselect",noList:groups,target:"comid",action:"getparks",twidth:"150" ,height:"",issort:false,shide:ishiddlegroup,fhide:ishiddlegroup},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,target:"berthseg_id",action:"getberthseg",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属泊位段",fieldname:"berthseg_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"150" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'berthseg_id');
			}},
		{fieldcnname:"泊位编号",fieldname:"cid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"订单生成时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"订单结算时间",fieldname:"end_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false,shide:true},
		{fieldcnname:"追缴时间",fieldname:"pursue_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"车牌号",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车主编号",fieldname:"uin",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
					if(value == "-1"){
						return "未注册";
					}else{
						return value;
					}
				}},
		{fieldcnname:"订单编号",fieldname:"order_id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"订单金额",fieldname:"total",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"预付金额",fieldname:"prepay",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				if(value <= 0){
					return "";
				}else{
					return value;
				}
			}},
		{fieldcnname:"欠费金额",fieldname:"overdue",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,shide:true},
		{fieldcnname:"实收金额",fieldname:"act_total",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"订单状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:stateList, twidth:"60" ,height:"",issort:false},
		{fieldcnname:"追缴人",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"查看车辆图片",fieldname:"id",inputtype:"text", twidth:"100",issort:false,shide:true,
			process:function(value,cid,id){
				return "<a href=# onclick=\"viewdetail('"+value+"','"+cid+"')\" style='color:blue'>查看车辆图片</a>";
			}}
	];
var _parkescapeT = new TQTable({
	tabletitle:"未缴明细",
	//ischeck:false,
	tablename:"parkescape_tables",
	dataUrl:"parkescape.do",
	iscookcol:false,
	//dbuttons:false,
	quikcsearch:coutomsearch(),
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&comid="+comid+"&btime="+btime+"&etime="+etime,
	tableObj:T("#parkescapeobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
//查看,添加,编辑,删除
	var bts =[];
	if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_parkescapeT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"parkescape_search_w",Title:"搜索未缴明细",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "parkescape_search_f",
					formObj:tObj,
					formWinId:"parkescape_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("parkescape_search_w");} }
					],
					SubAction:
					function(callback,formName){
						btime =  T("#coutom_btime").value;
						etime =  T("#coutom_etime").value;
						var groupid = '';
						if(!ishiddlegroup)
							groupid = T("#groups").value;
						_parkescapeT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&group_start="+groupid+"&btime="+btime+"&etime="+etime+"&"+Serializ(formName)
						});
						T("#coutom_btime").value=btime;
						T("#coutom_etime").value=etime;
						addgroups();
						if(!ishiddlegroup)
							T("#groups").value = groupid;
					}
				});	
			}
		})
	
	}});
	if(subauth[1])
		bts.push({dname:"0元追缴",icon:"toxls.gif",onpress:function(Obj){
		var sids = _parkescapeT.GS();
		var state=_parkescapeT.GSByField("state");
        var statestr=state.replace(/,/,"");
		if(statestr.indexOf("1")>-1){ 
		   	T.loadTip(1,"所选订单包含已经处理的！",2,"");
			return;
		}
		var ids="";
		if(!sids){
			T.loadTip(1,"请先选择订单",2,"");
			return;
		}
		Tconfirm({Title:"确认追缴吗?",Content:"确认追缴所选订单吗?",OKFn:function(){
			T.A.sendData("parkescape.do?action=recover","post","ids="+sids,
				function complete(ret){
					T.loadTip(1,"追缴成功！",2,"");
					_parkescapeT.C();
				}
			)}
		})
	}});
	bts.push({dname:"导出未缴明细",icon:"toxls.gif",onpress:function(Obj){
		Twin({Id:"order_export_w",Title:"导出未缴明细<font style='color:red;'>（如果没有设置，默认最大单次导出6万条!）</font>",Width:480,sysfun:function(tObj){
				 TSform ({
					formname: "order_export_f",
					formObj:tObj,
					formWinId:"order_export_w",
					formFunId:tObj,
					dbuttonname:["确认导出"],
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}],
					}],
					SubAction:
					function(callback,formName){
						T("#exportiframe").src="parkescape.do?action=exportExcel&fieldsstr=id__groupid__comid__berthseg_id__cid__create_time__end_time__pursue_time__car_number__uin__order_id__total__prepay__overdue__act_total__state__nickname__id"+Serializ(formName)
						TwinC("order_export_w");
						T.loadTip(1,"正在导出，请稍候...",2,"");
					}
				});	
			}
		})}});
	return bts;
}
function coutomsearch(){
	var html = "&nbsp;&nbsp;&nbsp;&nbsp;逃单时间：&nbsp;&nbsp;<input id='coutom_btime' class='Wdate' align='absmiddle' readonly value='"+btime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 00:00:00',alwaysUseStartDate:false});\"/>"
	+" - <input id='coutom_etime' class='Wdate' align='absmiddle' readonly value='"+etime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 23:59:59',alwaysUseStartDate:false});\"/>";
	if(!ishiddlegroup)
		html+="&nbsp;&nbsp;&nbsp;&nbsp;所属集团：&nbsp;&nbsp;<select style='width:130px' id='groups'></select>";
	html += "&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
	return html;
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
function searchdata(){
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;
	var groupid = '';
	if(!ishiddlegroup)
		groupid = T("#groups").value;
	_parkescapeT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&groupid_start="+groupid+"&btime="+btime+"&etime="+etime
	});
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
	addgroups();
	if(!ishiddlegroup)
		T("#groups").value = groupid;
}
function viewdetail(value,id){
	var car_number =_parkescapeT.GD(id,"car_number");
	
	var comid = _parkescapeT.GD(id,"comid");
	var orderid = _parkescapeT.GD(id,"order_id");
	var tip = "车辆图片";
	Twin({
		Id:"carpics_detail_"+id,
		Title:tip+"  --> 车牌："+car_number,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='carpics_detail_'"+id+" id='carpics_detail_'"+id+" src='order.do?action=escarpics&orderid="+orderid+"&comid="+comid+"&r="+Math.random()+"' width='100%' height='"+(T.gwh()-100)+"' frameborder='0' style='overflow:auto;' ></iframe>"
	})
}
function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}

function setcname(value,pid,colname){
	var url = "";
	if(colname == "uid"){
		url = "cityorder.do?action=getcollname&id="+value;
	}else if(colname == "freereasons"){
		url = "cityorder.do?action=getfreereason&id="+value;
	}else if(colname == "in_passid" || colname == "out_passid"){
		url = "cityorder.do?action=getpassname&id="+value;
	}else if(colname == "berthseg_id"){
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
		_parkescapeT.UCD(rowid,name,value);
}

_parkescapeT.C();
</script>

</body>
</html>
