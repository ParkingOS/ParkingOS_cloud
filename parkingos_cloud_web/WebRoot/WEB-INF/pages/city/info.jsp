<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<title>账户设置</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">
</head>
<body>
<script src="js/tq.js?08137" type="text/javascript">//基本</script>
<script src="js/tq.public.js?08031" type="text/javascript">//公共</script>
<script src="js/tq.window.js?008136" type="text/javascript">//弹窗</script>
<script src="js/tq.form.js?08369" type="text/javascript">//表单</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>

<div id="alllayout">
	<div style="width:100%;float:left;height:60px;border-bottom:1px solid #ccc" id="top"></div>
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
//查看,保存,提现申请
/*权限*/
//取字段
var Obj = document.getElementById("alllayout");
var topO = document.getElementById("top");
var rightO = document.getElementById("right");

rightO.style.width = T.gww()  + "px";
rightO.style.height = T.gwh() - 50 + "px";

T.bind(window,"resize",function(){
    rightO.style.width = T.gww() + "px";
    rightO.style.height = T.gwh() - 50 + "px"
})

var pursue=[{"value_no":0,"value_name":"不可以"},{"value_no":1,"value_name":"可以"}];
var fields = [
		{fieldcnname:"城市编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false,edit:false},
		{fieldcnname:"名称",fieldname:"name",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"经纬度",fieldname:"gps",fieldvalue:'',inputtype:"showmap", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"地理位置",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false,edit:false},
		{fieldcnname:"姓名",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"新密码",fieldname:"newpass",fieldvalue:'',inputtype:"password", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"确认密码",fieldname:"confirmpass",fieldvalue:'',inputtype:"password", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"是否跨运营集团追缴逃单",fieldname:"is_group_pursue",fieldvalue:'',inputtype:"select",noList:pursue ,twidth:"100" ,height:"",issort:true},
		{fieldcnname:"同一车牌可否在城市内重复入场",fieldname:"is_inpark_incity",fieldvalue:'',inputtype:"select",noList:pursue ,twidth:"100" ,height:"",issort:true}
	];
var info= eval('${info}');
var cityid = "";
for(var i=0;i<info.length;i++){
	if(info[i].name=="id"){
		cityid =info[i].value;
	}
}
var bHtml = "<div style='margin-top:20px;margin-buttom:20px;margin-left:29px;width:595px;overflow:hidden;'>编辑信息</div>";
topO.innerHTML=bHtml;

function getEditFields(){
	var e_f = [];
	for(var j=0;j<fields.length;j++){
		for(var i=0;i<info.length;i++){
			if(info[i].name==fields[j].fieldname){
				fields[j].fieldvalue=info[i].value;
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
		{kindname:"商户信息",kinditemts:fs.slice(0,4)},
		{kindname:"我的账户信息",kinditemts:fs.slice(4,9)},
		{kindname:"其他信息",kinditemts:fs.slice(9,11)}
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
	suburl:"cityinfo.do?action=edit&id="+cityid+"&uin=${uin}",
	method:"POST",
	dbuttonname:buttons,
	Callback:function(f,r,c,o){
		if(c=='1'){
			T.loadTip(1,"修改成功！",3,null);
		}else if(c=='-2'){
			T.loadTip(1,"密码长度小于6位，请重新输入！",3,null);
		}else if(c=='-3'){
			T.loadTip(1,"两次密码输入不一致，请重新输入！",3,null);
		}else
			T.loadTip(1,"操作失败！",3,null);
	},
	formAttr:[{
		formitems:getFields()
	}]
});
accountForm.C();

function getTopButtons(){
	var bus = [];
	return bus;
}

var isfixed = T("#opconfirm_isfixed").value;
if(isfixed==1){
	T("#opconfirm_address").disabled=true;
	T("#opconfirm_address_showmap").disabled=true;
}

</script>


</html>
<script type="text/javascript">
T.maskTip(0,"","");//加载结束
</script>