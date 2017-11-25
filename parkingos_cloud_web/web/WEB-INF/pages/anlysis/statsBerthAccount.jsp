<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>泊位收入分析</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.public.js?000817" type="text/javascript">//表格</script>
<script src="js/tq.datatable.js?1020" type="text/javascript">//表格</script>
<script src="js/tq.form.js?0817" type="text/javascript">//表单</script>
<script src="js/tq.searchform.js?000817" type="text/javascript">//查询表单</script>
<script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
</head>
<body>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>

<div id="parkduranlayobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">

/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var btime="${btime}";
var etime="${etime}";
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
//查看,添加,编辑,删除
/*权限*/
var _mediaField =[
		{kindname:"",kinditemts: [
           {fieldcnname:"主键",fieldname:"id",inputtype:"text",twidth:"80",issort:false,shide:true }
        ]},
		{kindname:"",kinditemts: [
		  {fieldcnname:"泊位编号",fieldname:"cid",inputtype:"text",twidth:"80",issort:false }
		]},
		{kindname:"",kinditemts: [
			{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",twidth:"150",noList:parks,target:"berthsec_id",action:"getberthseg",twidth:"150" ,height:"",issort:false}
		]},
		{kindname:"",kinditemts: [
   			{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"150" ,height:"",issort:false,
				process:function(value,pid){
					return setcname(value,pid,'berthsec_id');
			}}
   		]},
      {kindname:"停车费-现金支付",kinditemts: [
    	{fieldcnname:"普通订单",fieldname:"cashCustomFee",inputtype:"text",twidth:"80",issort:false,shide:true},
    	{fieldcnname:"追缴订单",fieldname:"cashPursueFee",inputtype:"text",twidth:"80",issort:false,shide:true},
    	{fieldcnname:"合计",fieldname:"cashTotalFee",inputtype:"text",twidth:"80",issort:false,shide:true}
     ]},
     {kindname:"停车费-电子支付",kinditemts: [
		{fieldcnname:"普通订单",fieldname:"ePayCustomFee",inputtype:"text",twidth:"80",issort:false,shide:true},
		{fieldcnname:"追缴订单",fieldname:"ePayPursueFee",inputtype:"text",twidth:"80",issort:false,shide:true},
		{fieldcnname:"合计",fieldname:"ePayTotalFee",inputtype:"text",twidth:"80",issort:false,shide:true}
	 ]},
	{kindname:"停车费-刷卡支付",kinditemts: [
   		{fieldcnname:"普通订单",fieldname:"cardCustomFee",inputtype:"text",twidth:"80",issort:false,shide:true},
		{fieldcnname:"追缴订单",fieldname:"cardPursueFee",inputtype:"text",twidth:"80",issort:false,shide:true},
		{fieldcnname:"合计",fieldname:"cardTotalFee",inputtype:"text",twidth:"80",issort:false,shide:true}
   	]},
   	{kindname:"停车费-合计",kinditemts: [
   		{fieldcnname:"实收停车费",fieldname:"totalFee",inputtype:"text",twidth:"80",issort:false,shide:true},
   		{fieldcnname:"未缴停车费",fieldname:"escapeFee",inputtype:"text",twidth:"80",issort:false,shide:true},
   		{fieldcnname:"应收停车费",fieldname:"allTotalFee",inputtype:"text",twidth:"80",issort:false,shide:true}
   	]}
  ];
var _parkduranlayT = new TQTable({
	tabletitle:"泊位收入分析",
	ischeck:false,
	tablename:"parkduranlay_tables",
	dataUrl:"statsberth.do",
	iscookcol:false,
	headrows:true,
	//tableFields:[{kindname:"测试",kinditemts:[{fieldcnname:"车场",fieldname:"comid1",inputtype:"text", twidth:"150" }]}],
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&btime="+btime+"&etime="+etime,
	tableObj:T("#parkduranlayobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	quikcsearch:coutomsearch(),
	isoperate:getAuthIsoperateButtons()
});
//查看,添加,编辑,删除
function getAuthButtons(){
	var bts=[];
	//if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
			T.each(_parkduranlayT.tc.tableitems,function(o,j){
				o.fieldvalue ="";
			}); 
			Twin({Id:"sensor_search_w",Title:"搜索泊位",Width:550,sysfun:function(tObj){
					TSform ({
						formname: "sensor_search_f",
						formObj:tObj,
						formWinId:"sensor_search_w",
						formFunId:tObj,
						formAttr:[{
							formitems:_mediaField
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("sensor_search_w");} }
						],
						SubAction:
						function(callback,formName){
							_parkduranlayT.C({
								cpage:1,
								tabletitle:"高级搜索结果",
								extparam:"&action=query&btime="+btime+"&etime="+etime+"&"+Serializ(formName)
							});
							T("#coutom_btime").value=btime;
							T("#coutom_etime").value=etime;
						}
					});	
				}
			})
		
		}});
	return bts;
}
function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}

function coutomsearch(){
	var html = "&nbsp;&nbsp;&nbsp;&nbsp;时间区间：<input id='coutom_btime' value='"+btime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 00:00:00',alwaysUseStartDate:false});\"/>"
	+" - <input id='coutom_etime' value='"+etime+"' style='width:150px' onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',startDate:'%y-%M-%d 23:59:59',alwaysUseStartDate:false});\"/>"
	+ "&nbsp;&nbsp;<input type='button' onclick='searchdata();' value=' 查 询 '/>&nbsp;&nbsp;";
	return html;
}

function searchdata(){
	btime = T("#coutom_btime").value;
	etime = T("#coutom_etime").value;
	_parkduranlayT.C({
		cpage:1,
		tabletitle:"搜索结果",
		extparam:"&action=query&btime="+btime+"&etime="+etime
	})
	T("#coutom_btime").value=btime;
	T("#coutom_etime").value=etime;
}

function setcname(value,pid,colname){
	if(value&&value!='-1'&&value!=''){
		var url = "";
		if(colname == "berthsec_id"){
			url = "cityberthseg.do?action=getberthseg&id="+value;
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
		_parkduranlayT.UCD(rowid,name,value);
}

_parkduranlayT.C();
</script>

</body>
</html>
