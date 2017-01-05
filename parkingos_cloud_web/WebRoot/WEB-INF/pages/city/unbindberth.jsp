<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>泊位列表</title>
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
<div id="unbindberthobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
/* var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
} */
var comid="${comid}";
var berthsegid="${berthsegid}";
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var berthsegs = eval(T.A.sendData("getdata.do?action=getberthseg&id="+comid));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false,shide:true,hide:true,fhide:true},
		{fieldcnname:"泊位编号",fieldname:"cid",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"150" ,height:"",issort:false,shide:true},
		{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"select",noList:berthsegs,twidth:"150" ,height:"",issort:false,
			process:function(value,pid){
				return setname(value,berthsegs);
			}},
		{fieldcnname:"车检器",fieldname:"did",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",hide:true,shide:true},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"空闲"},{"value_no":"1","value_name":"占用"}], twidth:"80" ,height:"",hide:true},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true,shide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true,shide:true},
		{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true}
	];
var _unbindberthT = new TQTable({
	tabletitle:"泊位列表",
	ischeck:true,
	tablename:"unbindberth_tables",
	dataUrl:"cityberthseg.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=queryunberth&berthsegid="+berthsegid+"&comid="+comid,
	tableObj:T("#unbindberthobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		/* T.each(_cityberthT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); */
		Twin({Id:"ownbind_search_w",Title:"搜索停车场",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "ownbind_search_f",
					formObj:tObj,
					formWinId:"ownbind_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("ownbind_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_unbindberthT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=queryunberth&berthsegid="+berthsegid+"&comid="+comid+"&"+Serializ(formName)
						})
					}
				});	
			}
		})
	}});
	bts.push({dname:"批量绑定泊位", icon: "sendsms.gif", onpress:function(Obj){
		var sids = _unbindberthT.GS();
		if(!sids){
			T.loadTip(1,"请先选择要绑定的泊位",2,"");
			return;
		}
		Tconfirm({Title:"批量绑定泊位",Content:"确认绑定吗？",OKFn:function(){
			T.A.sendData("cityberthseg.do?action=bindberth&berthsegid="+berthsegid,"post","id="+sids,
				function deletebackfun(ret){
					if(ret=="1"){
						T.loadTip(1,"绑定成功！",5,"");
						_unbindberthT.M();
						window.parent._cityberthsegT.M();
					}else{
						T.loadTip(1,"绑定失败！",2,"");
					}
				}
		)}})
	}})
	if(bts.length>0)
		return bts;
	return false;
}

function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}

function setname(value, list){
	if(value != "" && value != "-1"){
		for(var i=0; i<list.length;i++){
			var o = list[i];
			var key = o.value_no;
			var v = o.value_name;
			if(value == key){
				return v;
			};
		};
		return "";
	}else{
		return "";
	};
}

_unbindberthT.C();
</script>

</body>
</html>
