<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<title>修改停车场</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">
</head>
<body>
<script src="js/tq.js?08137" type="text/javascript">//基本</script>
<script src="js/tq.public.js?08031" type="text/javascript">//公共</script>
<script src="js/tq.window.js?008136" type="text/javascript">//弹窗</script>
<script src="js/tq.form.js?08301" type="text/javascript">//表单</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>

<div id="alllayout">
	<div style="width:100%;float:left;height:45px;border-bottom:1px solid #ccc" id="top"></div>
	<div style="width:100%;float:left;">
    	<div id="right" style="width:auto;border-left:1px solid #ccc;float:left"></div>
	</div>
</div>	
<div id="loadtip" style="display:none;"></div>
<div id="cover" style="display:none;"></div>
</body>
<script type="text/javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/
//取字段
var add_states = [{"value_no":0,"value_name":"否"},{"value_no":1,"value_name":"是"}];
var etc_states=[{"value_no":0,"value_name":"不支持"},{"value_no":1,"value_name":"Ibeacon"},{"value_no":2,"value_name":"通道照牌"},{"value_no":3,"value_name":"手机照牌"}]
var Obj = document.getElementById("alllayout");
var topO = document.getElementById("top");
var rightO = document.getElementById("right");
var type = '${type}';
var ishide=type==='set'?true:false;
rightO.style.width = T.gww()  + "px";
rightO.style.height = T.gwh() - 50 + "px";

T.bind(window,"resize",function(){
    rightO.style.width = T.gww() + "px";
    rightO.style.height = T.gwh() - 50 + "px"
})


var fields = [
		{fieldcnname:"车场编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false,edit:false},
		{fieldcnname:"区分大小车",fieldname:"car_type",fieldvalue:'',defaultValue:'不区分||0',inputtype:"select", noList:[{"value_no":0,"value_name":"不区分"},{"value_no":1,"value_name":"区分"}] , twidth:"100" ,height:"",issort:false},
		{fieldcnname:"免费结算订单",fieldname:"passfree",fieldvalue:'',defaultValue:'允许||0',inputtype:"select", noList:[{"value_no":0,"value_name":"允许"},{"value_no":1,"value_name":"不允许"}] , twidth:"100" ,height:"",issort:false},
		{fieldcnname:"HD结算按钮",fieldname:"ishidehdbutton",fieldvalue:'',defaultValue:'显示||0',inputtype:"select", noList:[{"value_no":0,"value_name":"显示"},{"value_no":1,"value_name":"隐藏"}] , twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车主自动支付",fieldname:"isautopay",fieldvalue:'',defaultValue:'显示||0',inputtype:"select", noList:[{"value_no":0,"value_name":"不支持"},{"value_no":1,"value_name":"支持"}] , twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车位满进场设置",fieldname:"full_set",fieldvalue:'',defaultValue:'可进||0',inputtype:"select", noList:[{"value_no":0,"value_name":"可进"},{"value_no":1,"value_name":"禁止"}] , twidth:"100" ,height:"",issort:false},
		{fieldcnname:"离场设置",fieldname:"leave_set",fieldvalue:'',defaultValue:'默认设置||0',inputtype:"select", noList:[{"value_no":0,"value_name":"默认设置"},{"value_no":1,"value_name":"识别就抬杆"}] , twidth:"100" ,height:"",issort:false},
		{fieldcnname:"优先识别省份",fieldname:"firstprovince",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false}, 
		{fieldcnname:"非月卡车",fieldname:"entry_set",fieldvalue:'',defaultValue:'不限制||0',inputtype:"select", noList:[{"value_no":0,"value_name":"不限制"},{"value_no":1,"value_name":"禁止进入"}] , twidth:"100" ,height:"",issort:false},
		{fieldcnname:"月卡第二辆车",fieldname:"entry_month2_set",fieldvalue:'',defaultValue:'不限制||0',inputtype:"select", noList:[{"value_no":0,"value_name":"不限制"},{"value_no":1,"value_name":"禁止进入"}], twidth:"100" ,height:"",issort:false},
		{fieldcnname:"是否隐藏收费金额",fieldname:"ishdmoney",fieldvalue:'',defaultValue:'不隐藏||0',inputtype:"select", noList:[{"value_no":0,"value_name":"不隐藏"},{"value_no":1,"value_name":"隐藏"}] , twidth:"100" ,height:"",issort:false}
	];
var comid="";
var company_name = 0;
var total=0;
var cominfo= eval('${cominfo}');
for(var i=0;i<cominfo.length;i++){
		if(cominfo[i].name=="id"){
			comid =cominfo[i].value;
		}
		if(cominfo[i].name=="company_name"){
			company_name =cominfo[i].value;
		}
}
var bHtml = "<div style='margin-top:13px;margin-buttom:20px;margin-left:19px;width:595px;overflow:hidden;font-size:18px;color:red'>";
	bHtml += company_name;
	bHtml += "</div>";
topO.innerHTML=bHtml;

function getEditFields(){
	var e_f = [];
	for(var j=0;j<fields.length;j++){
		for(var i=0;i<cominfo.length;i++){
			if(cominfo[i].name==fields[j].fieldname){
				fields[j].fieldvalue=cominfo[i].value;
				e_f.push(fields[j]);
				break;
			}
			if(fields[j].inputtype=='select')
				fields[j].width=200;
		}
	}
	return e_f;
}

function getFields(){
	var fs = getEditFields();
	var mfs = [
		{kindname:"设置信息",kinditemts:fs}
		//{kindname:"账户信息",kinditemts:fs.slice(21)}
		];
	return mfs;
}

var buttons=[false,false];
if(subauth[1])
	buttons=["保存","重置"];


var accountForm =
new TQForm({
	formname: "opconfirm",
	formObj:rightO,
	suburl:"parkinfo.do?action=parkset&id="+comid,
	method:"POST",
	dbuttonname:buttons,
	//dbbuttons:[true,false],
	//buttons:getTopButtons(),
	Callback:function(f,r,c,o){
		if(c=='1'){
			T.loadTip(1,"修改成功！",3,null);
		}else
			T.loadTip(1,"操作失败！",3,null);
	},
	formAttr:[{
		formitems:getFields()
	}]
});
accountForm.C();

</script>


</html>
<script type="text/javascript">
T.maskTip(0,"","");//加载结束
</script>