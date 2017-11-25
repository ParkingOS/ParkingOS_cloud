<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>巡查事件监测</title>
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
<body>
<div id="cityinspecteventobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var groupid = "${groupid}";
var berthseglist = eval(T.A.sendData("getdata.do?action=getberthsegbygroupid&groupid="+groupid));
var tasktype = eval(T.A.sendData("getdata.do?action=gettasktype"));
var getinspects = eval(T.A.sendData("getdata.do?action=getinspectsbygroup&groupid="+groupid));
var subauth=[false,false,false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var states = [{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"处理中"},{"value_no":"1","value_name":"处理成功"},{"value_no":"2","value_name":"处理失败"}];
//var types = [{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"进场事件"},{"value_no":"1","value_name":"出场事件"}];
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number",twidth:"80" ,height:"",issort:false},
		{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"130" ,height:"",issort:false},
		{fieldcnname:"完成时间",fieldname:"end_time",fieldvalue:'',inputtype:"date",twidth:"130" ,height:"",issort:false},
		{fieldcnname:"任务类别",fieldname:"type",fieldvalue:'',inputtype:"cselect",noList:tasktype,target:"detailtype",action:"getdetailtype",twidth:"80" ,height:"",issort:false},
		{fieldcnname:"任务详情",fieldname:"detailtype",fieldvalue:'',inputtype:"select",noList:[],twidth:"80" ,height:"",issort:false,
			process:function(value,pid){
				return setname(value,pid,'detailtype');
			}},
		{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"select",noList:berthseglist,twidth:"150" ,height:"",issort:false},
		{fieldcnname:"泊位号",fieldname:"dici_id",fieldvalue:'',inputtype:"cselect", twidth:"120" ,height:"",
			process:function(value,pid){
				return setname(value,pid,'dici_id');
			}},
		{fieldcnname:"收费员编号",fieldname:"uid",fieldvalue:'',inputtype:"text",noList:states, twidth:"80" ,height:"",
			process:function(value,pid){
				if(value==null||value==""||value<0) {
					return "无";
				}else{
					return value;
				}
			}},
		{fieldcnname:"巡查员",fieldname:"inspectid",fieldvalue:'',inputtype:"select",noList:getinspects,twidth:"80" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:states, twidth:"60" ,height:""},
		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
		{fieldcnname:"查看图片",fieldname:"id",inputtype:"text", twidth:"100",issort:false,edit:false,
			process:function(value,cid,id){
				return "<a href=# onclick=\"viewdetail('hn','"+value+"','"+cid+"')\" style='color:blue'>查看图片</a>";
		}},
	];
var _seachField = [
	{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number",twidth:"80" ,height:"",issort:false},
	{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"130" ,height:"",issort:false},
	{fieldcnname:"完成时间",fieldname:"end_time",fieldvalue:'',inputtype:"date",twidth:"130" ,height:"",issort:false},
	{fieldcnname:"任务类别",fieldname:"type",fieldvalue:'',inputtype:"cselect",noList:tasktype,target:"detailtype",action:"getdetailtype",twidth:"80" ,height:"",issort:false},
	{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"select",noList:berthseglist,twidth:"150" ,height:"",issort:false},
	{fieldcnname:"收费员编号",fieldname:"uid",fieldvalue:'',inputtype:"text",noList:states, twidth:"80" ,height:""},
	{fieldcnname:"巡查员",fieldname:"inspectid",fieldvalue:'',inputtype:"select",noList:getinspects,twidth:"80" ,height:"",issort:false},
	{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:states, twidth:"60" ,height:""},
];
var _addField = [
	{fieldcnname:"任务类别",fieldname:"type",fieldvalue:'',inputtype:"cselect",noList:tasktype,target:"detailtype",action:"getdetailtype",twidth:"80" ,height:"",issort:false},
	{fieldcnname:"任务详情",fieldname:"detailtype",fieldvalue:'',inputtype:"select",noList:[],twidth:"80" ,height:"",issort:false,
		process:function(value,pid){
			return setname(value,pid,'detailtype');

		}},
	{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"cselect",noList:berthseglist,target:"dici_id,inspectid",action:"getberth,getinspects",twidth:"120" ,height:"",issort:false},
	{fieldcnname:"泊位编号",fieldname:"dici_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"80" ,height:"30",issort:false,
		process:function(value,pid){
			return setname(value,pid,'dici_id');
		}},
	//{fieldcnname:"收费员",fieldname:"uid",fieldvalue:'',inputtype:"text",noList:states, twidth:"80" ,height:""},
	{fieldcnname:"巡查员",fieldname:"inspectid",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"80" ,height:"",issort:false,
		process:function(value,pid){
			return setcname(value,pid,'inspectid');
		}},
	{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
];
var rules =[
		{name:"inspecteventsec_id",type:"",url:"",requir:true,warn:"不能为空!",okmsg:""}
		];
var _cityinspecteventT = new TQTable({
	tabletitle:"巡查员事件",
	ischeck:false,
	tablename:"cityinspectevent_tables",
	dataUrl:"inspectevent.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery&groupid="+groupid,
	tableObj:T("#cityinspecteventobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	quikcsearch:coutomsearch(),
	isoperate:getAuthIsoperateButtons()
});
function coutomsearch(){
	var html=  "<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp&nbsp;&nbsp;状态 <select id ='state' name='state' style='width:120px;vertical-align:middle;' onchange=searchdata(this); >"+getSelectValue(states)+"</select></div>";
	return html;
}
function getSelectValue(valuse){
	var m = "";
	for(var a=0;a<valuse.length;a++){
		m +="<option value='"+valuse[a].value_no+"'>"+valuse[a].value_name+"</option>";
	}
	return m;
}
var stateValue="";
function searchdata(obj){
	var oid = obj.id;
	var value =obj.value;
	var extp = oid+"_start="+value;
	if(oid=='isclick'&&value=='0'){//自动结算过滤掉未结算的订单
		extp+='&state_start=1';
	}
	if(oid='uid'){
		stateValue=value;
	}
	_cityinspecteventT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&"+extp
	})
	setSelectValue();
//	addcoms();
}
function setSelectValue(){
	var uidSelect = T("#state");
	for(var i=0;i<uidSelect.options.length;i++){
		if(uidSelect.options[i].value==stateValue)
			uidSelect.options[i].selected = true;
	}
}
function addcoms(){
	if(groupid != ""){
		var childs = eval(T.A.sendData("getdata.do?action=getcoms&groupid=${groupid}"));
		jQuery("#companys").empty();
		for(var i=0;i<childs.length;i++){
			var child = childs[i];
			var id = child.value_no;
			var name = child.value_name;
			jQuery("#companys").append("<option value='"+id+"'>"+name+"</option>");
		}
	}
}
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityinspecteventT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"sensor_search_w",Title:"搜索",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "sensor_search_f",
					formObj:tObj,
					formWinId:"sensor_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_seachField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("sensor_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_cityinspecteventT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	if(subauth[0])
		bts.push({dname:"创建巡查事件",icon:"edit_add.png",onpress:function(Obj){
			T.each(_cityinspecteventT.tc.tableitems,function(o,j){
				o.fieldvalue ="";
			});
			Twin({Id:"member_add",Title:"创建事件",Width:700,sysfun:function(tObj){
				Tform({
					formname: "member_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"inspectevent.do?action=create",
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_addField}],
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("member_add");} }
					],
					Callback:
							function(f,rcd,ret,o){
								if(ret=="1"){
									T.loadTip(1,"添加成功！",2,"");
									TwinC("member_add");
									_cityinspecteventT.M();
								}else{
									T.loadTip(1,ret,2,o);
								}
							}
				});
			}
			})

		}});

	if(bts.length>0)
		return bts;
	return false;
}


function viewdetail(type,value,id){
	var eventid =_cityinspecteventT.GD(id,"id");
	var tip = "图片";
	Twin({
		Id:"pics_detail_"+id,
		Title:tip+"  --> ："+id,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='pics_detail_'"+id+" id='pics_detail_'"+id+" src='inspectevent.do?action=downinspectpic&eventid="+eventid+"&r="+Math.random()+"' width='100%' height='"+(T.gwh()-100)+"' frameborder='0' style='overflow:auto;' ></iframe>"
	})
}

function getAuthIsoperateButtons(){
	var bts = [];
	if(bts.length <= 0){return false;}
	return bts;
}

function setname(value,pid,colname){
	if(value&&value!='-1'&&value!=''){
		var type = _cityinspecteventT.GD(pid,"type");
		var url = "getdata.do?action=getdetailtypename&id="+type+"&value="+value;
//		alert(url);
//		if(colname == "berthsec_id"){
//			url = "cityberthseg.do?action=getberthseg&id="+value;
//		}else if(colname == "dici_id"){
//			url = "cityberth.do?action=getdici&id="+value;
//		}
		if(colname == "dici_id"){
			var url = "getdata.do?action=getcid&id="+value;
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
//	alert(value);
	if(value)
	_cityinspecteventT.UCD(rowid,name,value);
}

_cityinspecteventT.C();
</script>

</body>
</html>
