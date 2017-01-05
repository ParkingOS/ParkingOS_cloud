<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>月卡购买记录</title>
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
<div id="vipuserobj" style="width:100%;height:100%;margin:0px;"></div>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<form action="" method="post" id="choosecom"></form>
<script language="javascript">
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
//"查看,注册,编辑,修改车牌,删除,导出"
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/

var comid = "${comid}";
var mobile = "${mobile}";
var pnames= eval(T.A.sendData("getdata.do?action=getpname&id="+comid));
var month_select = [];
for(var i=1;i<36;i++){
	month_select.push({"value_no":i,"value_name":i});
}
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false,edit:false},
		{fieldcnname:"车主手机",fieldname:"mobile",fieldvalue:mobile,inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"包月产品名称",fieldname:"p_name",fieldvalue:'',inputtype:"select",noList:pnames, twidth:"100" ,height:"",issort:false},
		{fieldcnname:"购买时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:""},
		{fieldcnname:"开始时间",fieldname:"b_time",fieldvalue:'',inputtype:"date",twidth:"140" ,height:"",issort:false},
		{fieldcnname:"结束时间",fieldname:"e_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",edit:false},
		{fieldcnname:"应收金额",fieldname:"total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",edit:false},
		{fieldcnname:"实收金额",fieldname:"act_total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
		{fieldcnname:"月数",fieldname:"months",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",edit:false,fhide:true,shide:true},
		{fieldcnname:"车位编号",fieldname:"p_lot",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",edit:false},
		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false}
	];
var _addField = [
		{fieldcnname:"车主手机",fieldname:"mobile",fieldvalue:mobile,inputtype:"text", twidth:"200" ,height:"",issort:false,edit:false},
		{fieldcnname:"包月产品",fieldname:"p_name",fieldvalue:'',inputtype:"cselect",noList:pnames,target:"total",params:["months","p_name"],action:"getprodsum",twidth:"180" ,height:"",issort:false},
		{fieldcnname:"起始时间",fieldname:"b_time",fieldvalue:'',inputtype:"sdate",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"购买月数",fieldname:"months",fieldvalue:'',inputtype:"cselect",noList:month_select,target:"total",params:["months","p_name"],action:"getprodsum", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"应收金额",fieldname:"total",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false,edit:false},
		{fieldcnname:"实收金额",fieldname:"act_total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
		{fieldcnname:"车位编号",fieldname:"p_lot",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false}
	];
var _editField = [
         		{fieldcnname:"车主手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false},
         		{fieldcnname:"包月产品",fieldname:"p_name",fieldvalue:'',inputtype:"cselect",noList:pnames,target:"total",params:["months","p_name"],action:"getprodsum",twidth:"180" ,height:"",issort:false},
         		{fieldcnname:"起始时间",fieldname:"b_time",fieldvalue:'',inputtype:"sdate",twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"购买月数",fieldname:"months",fieldvalue:'',inputtype:"cselect",noList:month_select,target:"total",params:["months","p_name"],action:"getprodsum", twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"应收金额",fieldname:"total",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false,edit:false},
				{fieldcnname:"实收金额",fieldname:"act_total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
         		{fieldcnname:"车位编号",fieldname:"p_lot",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false}
         	];
var _renewField = [
				{fieldcnname:"车主手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false,edit:false},
				{fieldcnname:"包月产品",fieldname:"p_name",fieldvalue:'',inputtype:"cselect",noList:pnames,target:"total",params:["months","p_name"],action:"getprodsum",twidth:"180" ,height:"",issort:false},
         		{fieldcnname:"起始时间",fieldname:"b_time",fieldvalue:'',inputtype:"sdate",twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"续费月数",fieldname:"months",fieldvalue:'',inputtype:"cselect",noList:month_select,target:"total",params:["months","p_name"],action:"getprodsum", twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"应收金额",fieldname:"total",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false,edit:false},
				{fieldcnname:"实收金额",fieldname:"act_total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
         		{fieldcnname:"车位编号",fieldname:"p_lot",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false}
         	];
var rules =[{name:"p_name",type:"",url:"",requir:true,warn:"请选择产品",okmsg:""},
			{name:"b_time",type:"",url:"",requir:true,warn:"请选择时间",okmsg:""},
			{name:"mobile",type:"ajax",url:"vipuser.do?action=checkmobile&mobile=",requir:true,warn:"请填写用户真实手机号码!",okmsg:""}
			];
var _vipuserT = new TQTable({
	tabletitle:"VIP会员管理",
	ischeck:false,
	tablename:"vipuser_tables",
	dataUrl:"cityvip.do",
	iscookcol:false,
	//dbuttons:false,
	quikcsearch:coutomsearch(),
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=vipdetail&comid="+comid+"&mobile="+mobile,
	tableObj:T("#vipuserobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
	var html = "";
	return html;
}

//"查看,注册,编辑,修改车牌,删除,导出"
function getAuthButtons(){
	var authButs=[];
	authButs.push({dname:"购买月卡 ",icon:"edit_add.png",onpress:function(Obj){
	T.each(_vipuserT.tc.tableitems,function(o,j){
		o.fieldvalue ="";
	});
	Twin({Id:"vipuser_add",Title:"购买月卡<font style='color:red;'></font>",Width:550,sysfun:function(tObj){
			Tform({
				formname: "vipuser_add_f",
				formObj:tObj,
				formWinId:"vipuser_add_w",
				formFunId:tObj,
				recordid:"id",
				Coltype:2,
				suburl:"cityvip.do?action=create&comid="+comid+"&mobile="+mobile,
				method:"POST",
				formAttr:[{
					formitems:[{kindname:"",kinditemts:_addField}],
					rules:rules
				}],
				buttons : [//工具
					{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("vipuser_add");} }
				],
				Callback:
				function(f,rcd,ret,o){
					if(ret=="1"){
						T.loadTip(1,"添加成功！",2,"");
						TwinC("vipuser_add");
						_vipuserT.M();
						window.parent._cityvipT.M();
					}else if(ret==-1){
						T.loadTip(1,"添加失败！",2,"");
					}else if(ret==-2){
					 	T.loadTip(2,"产品已超出有效期，请重新选择产品或更改购买月数！",7,"");
					}else 
						T.loadTip(2,ret,7,"");
				}
			});	
		}
	})}});
	return authButs;
}
//"注册,编辑,修改车牌,删除,导出"
function getAuthIsoperateButtons(){
	var bts = [];
	bts.push({name:"编辑",fun:function(id){
		var p_name = _vipuserT.GD(id,"p_name");
		var mobile = _vipuserT.GD(id,"mobile");
		var b_time = _vipuserT.GD(id,"b_time");
		var months = _vipuserT.GD(id,"months");
		var remark = _vipuserT.GD(id,"remark");
		var p_lot = _vipuserT.GD(id,"p_lot");
		var total = _vipuserT.GD(id,"total");
		var act_total = _vipuserT.GD(id,"act_total");
		Twin({Id:"vipuser_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "vipuser_edit_f",
					formObj:tObj,
					recordid:"vipuser_id",
					suburl:"vipuser.do?comid="+comid+"&action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_editField}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("vipuser_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("vipuser_edit_"+id);
							_vipuserT.M();
							window.parent._cityvipT.M();
						}else if(ret==-1){
							T.loadTip(1,"编辑失败！",2,"");
						}else if(ret==-2){
						 	T.loadTip(2,"产品已超出有效期，请重新选择产品或更改购买月数！",7,"");
						}else 
							T.loadTip(2,ret,7,"");
					}
				});	
				T("#vipuser_edit_f_p_name").value=p_name;
				T("#vipuser_edit_f_mobile").value=mobile;
				T("#vipuser_edit_f_b_time").value=b_time;
				T("#vipuser_edit_f_months").value=months;
				T("#vipuser_edit_f_remark").value=remark;
				T("#vipuser_edit_f_p_lot").value=p_lot;
				T("#vipuser_edit_f_total").value=total;
				T("#vipuser_edit_f_act_total").value=act_total;
			}
		})
	}}); 
	
	bts.push({name:"删除",fun:function(id){
		var id_this = id ;
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("vipuser.do?action=delete","post","selids="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_vipuserT.M();
					window.parent._cityvipT.M();
				}else if(ret=="-1"){
					T.loadTip(1,"此车主已购买了其它车场包月产品",2,"");
				}else if(ret=="-2"){
					T.loadTip(1,"此车主不能删除，月卡已删除",2,"");
				}else if(ret=="-3"){
					T.loadTip(1,"删除车主失败，月卡已删除",2,"");
				}else {
					T.loadTip(1,ret,2,"");
				}
				_vipuserT.M();
			}
		)}})
	}});
	bts.push({name:"续费",fun:function(id){
		var p_name = _vipuserT.GD(id,"p_name");
		var mobile = _vipuserT.GD(id,"mobile");
		var b_time = _vipuserT.GD(id,"e_time");
		var remark = _vipuserT.GD(id,"remark");
		var p_lot = _vipuserT.GD(id,"p_lot");
		var total = T.A.sendData("getdata.do?action=getprodsum&p_name="+p_name+"&months=1");
		Twin({Id:"vipuser_renew_"+id,Title:"月卡续费",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "vipuser_renew_f",
					formObj:tObj,
					recordid:"vipuser_id",
					suburl:"vipuser.do?comid="+comid+"&mobile="+mobile+"&action=renew",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_renewField}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消续费",icon:"cancel.gif", onpress:function(){TwinC("vipuser_renew_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"续费成功！",2,"");
							TwinC("vipuser_renew_"+id);
							_vipuserT.M();
							window.parent._cityvipT.M();
						}else if(ret==-1){
							T.loadTip(1,"续费失败！",2,"");
						}else if(ret==-2){
						 	T.loadTip(2,"产品已超出有效期，请重新选择产品或更改购买月数！",7,"");
						}else 
							T.loadTip(2,ret,7,"");
					}
				});	
				T("#vipuser_renew_f_p_name").value=p_name;
				T("#vipuser_renew_f_mobile").value=mobile;
				T("#vipuser_renew_f_b_time").value=b_time;
				T("#vipuser_renew_f_remark").value=remark;
				T("#vipuser_renew_f_p_lot").value=p_lot;
				T("#vipuser_renew_f_total").value=total;
			}
		})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}

_vipuserT.C();
</script>

</body>
</html>
