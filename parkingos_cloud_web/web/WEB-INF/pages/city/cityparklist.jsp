<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>停车场管理</title>
<link href="css/tq_old.css" rel="stylesheet" type="text/css">
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
<script src="js/tq.newtree.js?1014" type="text/javascript"></script>
</head>
<body>
<body onload='addParktype()'>
<div id="parkingobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
var etc_add_states=[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"无"},{"value_no":1,"value_name":"Ibeacon"},{"value_no":2,"value_name":"通道照牌"},{"value_no":3,"value_name":"手机照牌"},{"value_no":4,"value_name":"Pos机照牌"}]
var parktype=[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"地面"},{"value_no":1,"value_name":"地下"},{"value_no":2,"value_name":"占道"},{"value_no":3,"value_name":"室内"},{"value_no":4,"value_name":"室外"},{"value_no":5,"value_name":"室内外"}];
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var stateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"已审核"},{"value_no":2,"value_name":"未审核"}];
//*****************************************城市登录显示的内容*********************************************//
var cityid = "${cityid}";
var groups = eval(T.A.sendData("getdata.do?action=getcitygroups&cityid="+cityid));
var _parentField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"80" ,height:"",issort:false,edit:false},
		{fieldcnname:"名称",fieldname:"company_name",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"车场类型",fieldname:"parking_type",fieldvalue:'',defaultValue:'地面||0',inputtype:"select",noList:parktype ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车位总数",fieldname:"parking_total",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"进场方式",fieldname:"etc",fieldvalue:'',defaultValue:'通道照牌||2',inputtype:"select",noList:etc_add_states, twidth:"80" ,height:"",issort:false,shide:true},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:stateList, twidth:"60" ,height:"",issort:false},
		{fieldcnname:"所属运营集团",fieldname:"groupid",fieldvalue:'',inputtype:"cselect",noList:groups,target:"areaid",action:"getareas",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"所属区域",fieldname:"areaid",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'areaid');
			}},
		{fieldcnname:"行政地区",fieldname:"city",fieldvalue:"",inputtype:"select",noList:[],dataurl:"parking.do?action=localdata",edit:false,issort:false,
			process:function(value,trId,colId){//值、行ID(记录ID)、列ID(字段名称)
				if(value!=''&&value!='null'&&parseInt(value)>0){
					var local = T.A.sendData("parking.do?action=getlocalbycode&code="+value);
					return local;
				}else
					return value;
			}},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"180" ,height:"",issort:false,shide:true},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true},
		{fieldcnname:"停车场电话",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",hide:true,issort:false,},
		{fieldcnname:"更新时间",fieldname:"update_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",hide:true,issort:false,},
		//添加车场秘钥展示列表
		{fieldcnname:"车场秘钥",fieldname:"ukey",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",edit:false}
		];
//*****************************************集团登录显示的内容*********************************************//
var areas = eval(T.A.sendData("getdata.do?action=getareas&id=${groupid}"));
var _childField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"40" ,height:"",issort:false,edit:false},
		{fieldcnname:"名称",fieldname:"company_name",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"车场类型",fieldname:"parking_type",fieldvalue:'',defaultValue:'地面||0',inputtype:"select",noList:parktype ,twidth:"100" ,height:"",issort:true},
		{fieldcnname:"车位总数",fieldname:"parking_total",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"进场方式",fieldname:"etc",fieldvalue:'',defaultValue:'通道照牌||2',inputtype:"select",noList:etc_add_states, twidth:"80" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:stateList, twidth:"60" ,height:"",issort:false},
		{fieldcnname:"所属区域",fieldname:"areaid",fieldvalue:'',inputtype:"select",noList:areas,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"行政地区",fieldname:"city",fieldvalue:"",inputtype:"select",noList:[],dataurl:"parking.do?action=localdata",edit:true,
			process:function(value,trId,colId){//值、行ID(记录ID)、列ID(字段名称)
				if(value!=''&&value!='null'&&parseInt(value)>0){
					var local = T.A.sendData("parking.do?action=getlocalbycode&code="+value);
					return local;
				}else
					return value;
			}},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"180" ,height:"",issort:false,shide:true},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true},
		{fieldcnname:"停车场电话",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",hide:true},
		{fieldcnname:"更新时间",fieldname:"update_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",hide:true},
		//添加车场秘钥展示列表
		{fieldcnname:"车场秘钥",fieldname:"ukey",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",edit:false}
		];
var _mediaField = _childField;
if(cityid != "-1"){
	_mediaField = _parentField;
}

var _edit=[
		{fieldcnname:"名称",fieldname:"company_name",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"车场类型",fieldname:"parking_type",fieldvalue:'',defaultValue:'地面||0',inputtype:"select",noList:parktype ,twidth:"100" ,height:"",issort:true},
		{fieldcnname:"车位总数",fieldname:"parking_total",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"进场方式",fieldname:"etc",fieldvalue:'',defaultValue:'通道照牌||2',inputtype:"select",noList:etc_add_states, twidth:"60" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:stateList, twidth:"60" ,height:"",issort:false},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"180" ,height:"",issort:false,shide:true},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true},
		{fieldcnname:"停车场电话",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"行政地区",fieldname:"city",fieldvalue:"",inputtype:"select",noList:[],dataurl:"parking.do?action=localdata",edit:true,
			process:function(value,trId,colId){//值、行ID(记录ID)、列ID(字段名称)
				if(value!=''&&value!='null'&&parseInt(value)>0){
					var local = T.A.sendData("parking.do?action=getlocalbycode&code="+value);
					return local;
				}else
					return value;
			}}
		];
var rules =[
		{name:"company_name",type:"",url:"",requir:true,warn:"不能为空!",okmsg:""}
		];
var _parkingT = new TQTable({
	tabletitle:"停车场列表",
	ischeck:false,
	tablename:"parking_tables",
	dataUrl:"citypark.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	quikcsearch:coutomsearch(),
	param:"action=query",
	tableObj:T("#parkingobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
	var html = "&nbsp;&nbsp;&nbsp;&nbsp;进场方式：&nbsp;&nbsp;<select id='parktype' style='width:130px'></select>";
	html += "&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
	return html;
}

function searchdata(){
	var parktypeid = T("#parktype").value;
	_parkingT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&etc_start="+parktypeid
	});
	addParktype();
	T("#parktype").value = parktypeid;
}

function getAuthButtons(){
	var bus = [];
	if(subauth[1])
	bus.push({dname:"注册停车场",icon:"edit_add.png",onpress:function(Obj){
		T.each(_parkingT.tc.tableitems,function(o,j){
				o.fieldvalue ="";
			});
		Twin({Id:"parking_add",Title:"添加停车场",Width:550,sysfun:function(tObj){
				Tform({
					formname: "parking_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"citypark.do?action=create",
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("parking_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("parking_add");
							_parkingT.M();
						}else if(ret=="-1"){
							T.loadTip(1,"请选择运营集团！",2,"");
						}else if(ret=="-2"){
							T.loadTip(1,"请标注地理位置!",2,"");
						}else if(ret=="-3"){
							T.loadTip(1,"地理位置冲突，请重新标注!",2,"");
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		})
	
	}});
	if(subauth[0])
	bus.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		/* T.each(_parkingT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); */
		Twin({Id:"parking_search_w",Title:"搜索停车场",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "parking_search_f",
					formObj:tObj,
					formWinId:"parking_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("parking_search_w");} }
					],
					SubAction:
					function(callback,formName){
						var parktypeid = T("#parktype").value;
						_parkingT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&etc_start="+parktypeid+"&"+Serializ(formName)
						});
						addParktype();
						T("#parktype").value = parktypeid;
					}
				});	
			}
		})
	
	}});
	return bus;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		var comid = _parkingT.GD(id,"id");
		var company_name = _parkingT.GD(id,"company_name");
		var parking_type = _parkingT.GD(id,"parking_type");
		var parking_total = _parkingT.GD(id,"parking_total");
		var etc = _parkingT.GD(id,"etc");
		var state = _parkingT.GD(id,"state");
		var address = _parkingT.GD(id,"address");
		var longitude = _parkingT.GD(id,"longitude");
		var latitude = _parkingT.GD(id,"latitude");
		var mobile = _parkingT.GD(id,"mobile");
		var city = _parkingT.GD(id,"city");
		Twin({Id:"parking_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "parking_edit_f",
					formObj:tObj,
					recordid:"parking_id",
					suburl:"citypark.do?action=edit&id="+comid,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_edit}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("parking_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("parking_edit_"+id);
							_parkingT.M();
						}else if(ret=="-1"){
							T.loadTip(1,"请选择运营集团！",2,"");
						}else if(ret=="-2"){
							T.loadTip(1,"请标注地理位置!",200,"");
						}else if(ret=="-3"){
							T.loadTip(1,"地理位置冲突，请重新标注!",2,"");
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
				T("#parking_edit_f_company_name").value=company_name;
				T("#parking_edit_f_parking_type").value=parking_type;
				T("#parking_edit_f_parking_total").value=parking_total;
				T("#parking_edit_f_etc").value=etc;
				T("#parking_edit_f_state").value=state;
				T("#parking_edit_f_address").value=address;
				T("#parking_edit_f_mobile").value=mobile;
				T("#parking_edit_f_city").value=city;
				T("#parking_edit_f_longitude").value=longitude;
				T("#parking_edit_f_latitude").value=latitude;
			}
		})
	}});
	if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		var id_this = id ;
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("citypark.do?action=delete","post","selids="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_parkingT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(subauth[4])
	bts.push({name:"设置",fun:function(id){
		Twin({
			Id:"client_detail_"+id,
			Title:"停车场设置  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
			Content:"<iframe src=\"citypark.do?action=set&id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
			Width:T.gww()-100,
			Height:T.gwh()-50
		})
		}});
	bts.push({name:"上传照片",fun:function(id){
		var url ="upload.html?url=citypark&action=uploadpic&id="+id;
		Twin({Id:"parkattendant_edit_"+id,Title:"上传照片",Width:350,Height:200,sysfunI:id,
			Content:"<iframe id='tactic_iframe' src='"+url+"' style='width:100%;height:100%;' frameborder='0' ></iframe>",
			buttons :[],
			CloseFn:function(){
				_parkingT.M();
			}
			})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}

function setcname(value,pid,colname){
	if(value&&value!='-1'&&value!=''){
		T.A.C({
			url:"getdata.do?action=getarea&areaid="+value,
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
	_parkingT.UCD(rowid,name,value);
}

function addParktype(){
	var parktypesel = document.getElementById("parktype");
	for(var i=0;i<etc_add_states.length;i++){
		var child = etc_add_states[i];
		var id = child.value_no;
		var name = child.value_name;
		parktypesel.options.add(new Option(name, id));
	}
}

_parkingT.C();
</script>

</body>
</html>
