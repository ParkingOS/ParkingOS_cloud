<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>车检器管理</title>
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
<body onload="initSelect()">
<div id="sensormanageobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var stateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"空闲"},{"value_no":1,"value_name":"占用"}];
var parks =[];// eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var citys = eval(T.A.sendData("getdata.do?action=getallcitygroups"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"text", twidth:"60",dici_tbissort:false,edit:false},
		{fieldcnname:"所属城市商户",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"150" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,"city");
				
			}},
		{fieldcnname:"所属集团",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"150" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,"group");
				
			}},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"150" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,"park");
				
			}},
        {fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:stateList, twidth:"60" ,height:"",issort:false},
        {fieldcnname:"车检器编号",fieldname:"did",fieldvalue:'',inputtype:"text",twidth:"150",height:"",issort:false},
        {fieldcnname:"电池电压",fieldname:"battery",fieldvalue:'',inputtype:"number",twidth:"80",height:"",issort:false,edit:false},
        {fieldcnname:"电容电压",fieldname:"magnetism",fieldvalue:'',inputtype:"number",twidth:"80",height:"",issort:false,edit:false},
        {fieldcnname:"心跳时间",fieldname:"beart_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,edit:false},
		{fieldcnname:"操作时间",fieldname:"operate_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"X0",fieldname:"x0",fieldvalue:'',inputtype:"text",twidth:"80" ,height:"",issort:false,edit:false,hide:true,shide:true},
   		{fieldcnname:"Y0",fieldname:"y0",fieldvalue:'',inputtype:"text",twidth:"80" ,height:"",issort:false,edit:false,hide:true,shide:true},
   		{fieldcnname:"Z0",fieldname:"z0",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",issort:false,edit:false,hide:true,shide:true},
   		{fieldcnname:"(天泊)概率",fieldname:"rate",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",issort:false,edit:false,hide:true,shide:true,
   			process:function(value,pid){
				if(value == "-1"){
					return "";
				}
				return value;
			}
   		}
	];
var _sensormanageT = new TQTable({
	tabletitle:"车检器管理",
	ischeck:'checkbox',
	tablename:"sensormanage_tables",
	dataUrl:"sensormanage.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	quikcsearch:coutomsearch(),
	param:"action=query",
	tableObj:T("#sensormanageobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
var cityslist = [];
if(citys){
	for(var i=0;i<citys.length;i++){
		var cid = citys[i];
		cityslist.push({"value_no":cid.cid,"value_name":cid.cname});
	}
}
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"绑定车检器",icon:"edit_add.png",onpress:function(Obj){
		var sids = _sensormanageT.GS();
		if(sids==''){
			T.loadTip(1,"请先选择数据！",2,"");
			return ;
		}
		Twin({Id:"sensormanage_search_w",Title:"绑定车检器",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "sensormanage_search_f",
					formObj:tObj,
					formWinId:"sensormanage_search_w",
					formFunId:tObj,
					dbuttonname:["确认绑定"],
					formAttr:[{
						formitems:[{kindname:"",kinditemts:[
						 {fieldcnname:"城市商户",fieldname:"city",inputtype:"cselect",noList:cityslist,target:"group",action:"getcitygroups"},
						 {fieldcnname:"集团",fieldname:"group",inputtype:"cselect",noList:[],target:"park",action:"getparkbygroupid"},
						 {fieldcnname:"绑定到停车场",fieldname:"park",inputtype:"cselect",noList:[]}]}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("sensormanage_search_w");} }
					],
					SubAction:
					function(callback,formName){
						Tconfirm({Title:"确认绑定吗",Content:"确认绑定吗",OKFn:function(){
							T.A.sendData("sensormanage.do?action=bindsensor","post","ids="+sids+"&"+Serializ(formName),
							function deletebackfun(ret){
								if(ret=="1"){
									T.loadTip(1,"绑定成功！",2,"");
									TwinC("sensormanage_search_w");
									_sensormanageT.M();
								}else{
									T.loadTip(1,ret,2,"");
								}
							}
						)}})
					}
				});	
			}
		})
	
	}});
	bts.push({dname:"取消绑定",icon:"edit_add.png",onpress:function(Obj){
		var sids = _sensormanageT.GS();
		if(sids==''){
			T.loadTip(1,"请先选择数据！",2,"");
			return ;
		}
		Tconfirm({Title:"确认取消绑定吗",Content:"确认取消绑定吗",OKFn:function(){
			T.A.sendData("sensormanage.do?action=cancalbind","post","ids="+sids,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"取消绑定成功！",2,"");
					_sensormanageT.M();
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}});
	
	}});
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[1])
	bts.push({name:"初始化地磁",fun:function(id){
		Tconfirm({Title:"初始化地磁",Content:"确认初始化地磁吗",OKFn:function(){
		T.A.sendData("sensormanage.do?action=intixyz","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"操作成功！",2,"");
					_sensormanageT.M();
				}else if(ret == "-2"){
					T.loadTip(1,"只有天泊车检器（车检器编号以TB开头）可初始化!",2,"");
				}else{
					T.loadTip(1,"操作失败!",2,"");
				}
			}
		)}})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}


function coutomsearch(){
	var html = "&nbsp;&nbsp; 城市：<select id='city' style='width:159px' onChange='changegroup(this.value)'/> </select> "+
				"&nbsp;&nbsp;集团：<select id='group' style='width:159px' onChange='changepark(this.value)'/> </select>"+
				"&nbsp;&nbsp;车场：<select id='park' style='width:159px' /> </select>"+
				"&nbsp;&nbsp;地磁：<input id ='dici'/>"+
				"&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
	return html;
}

function initSelect(){
	var cityselect = document.getElementById('city');
	cityselect.options.add(new Option("请选择", "-1"));
	if(citys){
		for(var i=0;i<citys.length;i++){
			var cid = citys[i];
			var varItem = new Option(cid.cname, cid.cid);      
			cityselect.options.add(varItem);  
		}
	}
}
function changegroup(value){
	var groupselect = document.getElementById('group');
	for(var i=groupselect.options.length-1;i>=0;i--)
		groupselect.options.remove(i);
	var parkselect = document.getElementById('park');
	for(var i=parkselect.options.length-1;i>=0;i--)
		parkselect.options.remove(i);
	groupselect.options.add(new Option("请选择", "-1"));
	for(var i=0;i<citys.length;i++){
		var cid = citys[i].cid;
		if(cid==value){
			var groups = citys[i].groups;
			for(var j=0;j<groups.length;j++){
				var _group = groups[j];
				var varItem = new Option(_group.gname, _group.gid);
				groupselect.options.add(varItem);  
			}
			break;
		}
	}
}
function changepark(value){
	var parkselect = document.getElementById('park');
	for(var i=parkselect.options.length-1;i>=0;i--)
		parkselect.options.remove(i);
	var parks = eval(T.A.sendData("getdata.do?action=getparkbygroupid&groupid="+value));
	
	for(var i=0;i<parks.length;i++){
		var varItem = new Option(parks[i].value_name, parks[i].value_no);
		parkselect.options.add(varItem);  
	}
	
}
function searchdata(type){
	var city = T("#city").value;
	var group = T("#group").value;
	var park = T("#park").value;
	var dici = T("#dici").value;
	//data=eval(T.A.sendData("parkingturnover.do?action=echarts&btime="+btime+"&etime="+etime));
	_sensormanageT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&city="+city+"&group="+group+"&park="+park+"&dici="+dici,
	});
	T("#dici").value=dici;
	initSelect();
}

var cachedgroup = [];
function setcname(value,rowid,type){
	var group = cachedgroup["'"+value+"'"];
	var groupid = '';
	if(!group&&parseInt(value)>1000){
		group = eval(T.A.sendData("getdata.do?action=getgroupidbyparkid&parkid="+value));
		cachedgroup["'"+value+"'"]=group;
	}
	
	if(citys&&group){
		if(type=='park'){
			return group[0].parkname;
		}else if(type=='group'){
			groupid=group[0].groupid;
			for(var i=0;i<citys.length;i++){
				var cid = citys[i];
				var groups = citys[i].groups;
				for(var j=0;j<groups.length;j++){
					var _group = groups[j];
					var gid = _group.gid;
					if(gid==groupid){
						return _group.gname;
					}
				}
			}
		}else if(type=='city'){
			groupid=group[0].groupid;
			for(var i=0;i<citys.length;i++){
				var cid = citys[i];
				var groups = citys[i].groups;
				for(var j=0;j<groups.length;j++){
					var _group = groups[j];
					var gid = _group.gid;
					if(gid==groupid){
						return citys[i].cname;
					}
				}
			}
		}
	}else {
		return value;
	}
	//_sensormanageT.UCD(pid,name,value);	
}
_sensormanageT.C();
</script>

</body>
</html>
