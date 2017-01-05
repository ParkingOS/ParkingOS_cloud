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
<body>
<div id="citysensorobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var btime="${btime}";
var etime="${etime}";
var site_state_start = "${site_state_start}";
var stateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"空闲"},{"value_no":1,"value_name":"占用"}];
var fstateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"故障"},{"value_no":1,"value_name":"正常"}];
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"text", twidth:"60",issort:false,edit:false,shide:true,hide:true,fhide:true},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"150" ,height:"",issort:false},
		{fieldcnname:"泊位号",fieldname:"cid",fieldvalue:'',inputtype:"text",twidth:"150",height:"",issort:false},
		{fieldcnname:"车检器编号",fieldname:"did",fieldvalue:'',inputtype:"text",twidth:"150",height:"",issort:false},
        {fieldcnname:"占用状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:stateList, twidth:"60" ,height:"",issort:false},
        {fieldcnname:"车检器状态",fieldname:"site_state",fieldvalue:'',inputtype:"select",noList:fstateList, twidth:"80" ,height:"",issort:false,
        	process:function(value,trId,colId){
				if(value == 0)
					return "<font color='red'>故障</font>";
				else 
					return "正常";
			}},
        
        {fieldcnname:"电池电压",fieldname:"battery",fieldvalue:'',inputtype:"number",twidth:"80",height:"",issort:false,edit:false},
        {fieldcnname:"电容电压",fieldname:"magnetism",fieldvalue:'',inputtype:"number",twidth:"80",height:"",issort:false,edit:false},
        {fieldcnname:"心跳时间",fieldname:"beart_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,edit:false},
		{fieldcnname:"操作时间",fieldname:"operate_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"掉线次数",fieldname:"fcount",fieldvalue:'',inputtype:"text",twidth:"80",height:"",issort:false,shide:true,
			process:function(value,cid,id){
				return "<a href=# onclick=\"viewdetail('"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
			}}
	];
var back = "";
if("${from}" == "index"){
	back = "<a href='cityindex.do?authid=${index_authid}' class='sel_fee' style='float:right;margin-right:20px;'>返回</a>";
}
var _citysensorT = new TQTable({
	tabletitle:"车检器管理"+back,
	ischeck:false,
	tablename:"citysensor_tables",
	dataUrl:"citysensor.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	quikcsearch:coutomsearch(),
	param:"action=query&btime="+btime+"&etime="+etime+"&site_state_start="+site_state_start,
	tableObj:T("#citysensorobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function coutomsearch(){
	var html = "&nbsp;&nbsp;掉线时间：<input id='coutom_btime' value='"+btime+"' class='Wdate' align='absmiddle' readonly style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 00:00:00',alwaysUseStartDate:false});\"/>"
				+" - <input id='coutom_etime' value='"+etime+"' class='Wdate' align='absmiddle' readonly style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 23:59:59',alwaysUseStartDate:false});\"/>"+
				"&nbsp;&nbsp;&nbsp;&nbsp;掉线次数：<select id='fcount' name='fcount' style='width:130px'><option value=-1>全部</option><option value=0>大于零</option></select>"+
				"&nbsp;&nbsp;&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>";
	return html;
}
function searchdata(){
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;
	var fcount = T("#fcount").value;
	_citysensorT.C({
		cpage:1,
		tabletitle:"搜索结果 "+back,
		extparam:"&action=query&btime="+btime+"&etime="+etime+"&fcount="+fcount
	});
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
	T("#fcount").value=fcount;
}
function getAuthButtons(){
	var bts=[];
	
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_citysensorT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"sensor_search_w",Title:"搜索",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "sensor_search_f",
					formObj:tObj,
					formWinId:"sensor_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("sensor_search_w");} }
					],
					SubAction:
					function(callback,formName){
						btime = T("#coutom_btime").value;
						etime = T("#coutom_etime").value;
						var fcount = T("#fcount").value;
						_citysensorT.C({
							cpage:1,
							tabletitle:"高级搜索结果"+back,
							extparam:"&action=query&btime="+btime+"&etime="+etime+"&fcount="+fcount+"&"+Serializ(formName)
						})
						T("#coutom_btime").value=btime;
						T("#coutom_etime").value=etime;
						T("#fcount").value=fcount;
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
function viewdetail(value,id){
	var did =_citysensorT.GD(id,"did");
	var tip = "车检器掉线历史记录";
	Twin({
		Id:"sensor_detail_"+id,
		Title:tip+"  --> 车检器编号："+did,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='sensor_detail_'"+id+" id='sensor_detail_'"+id+" src='citysensor.do?action=detail&id="+id+"&btime="+btime+"&etime="+etime+"' width='100%' height='100%' frameborder='0' scrolling='no' style='overflow:hidden;' ></iframe>"
	});
}
_citysensorT.C();
</script>

</body>
</html>
