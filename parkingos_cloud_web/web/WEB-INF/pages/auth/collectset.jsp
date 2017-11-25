<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>收费设置</title>
<script type="text/javascript" src="js/tq.js"></script>
<body>
<div style="widtd:400px; margin-top:30px;margin-left:80px;line-height:26px;">
	<form motded ="post" action="adminrole.do">
		<input type="hidden" name="action" value="collectset"/>
		<input type="hidden" name="role_id" value=""/>
		<input type="hidden" name="roleid" value="${roleid}"/>
		<table>
		<tr> <td><b>拍照数量设置</b></td> <td></td></tr>
		<tr> <td>入场拍照数量:</td> <td><input name="photoset1"/></td></tr>
		<tr> <td>出场拍照数量:</td> <td><input name="photoset2"/></td></tr>
		<tr> <td>未缴费拍照数量:</td> <td><input name="photoset3"/></td></tr>
		<tr> <td>是否可以预收:</td> <td>
			<select name="isprepay" style='width:173px' onchange="prepayset(this.value)" > 
				<option value="0">否</option> 
				<option value="1" selected=true>是</option>
			 </select></td></tr>
		<tr> <td><b>预收金额选项(单位:元)</b></td> <td></td></tr>
		<tr> <td>选项1:</td> <td><input name="prepayset1"/></td></tr>
		<tr> <td>选项2:</td> <td><input name="prepayset2"/></td></tr>
		<tr> <td>选项3:</td> <td><input name="prepayset3"/></td></tr>
		<tr> <td><b>出入场打印信息</b></td> <td></td></tr>
		<tr> <td>入场打印尾信息:</td> <td><input name="print_sign1" /></td></tr>
		<tr> <td>出场打印尾信息:</td> <td><input name="print_sign2" /></td></tr>
		<tr> <td>入场打印头信息:</td> <td><input name="print_sign3" /></td></tr>
		<tr> <td>出场打印头信息:</td> <td><input name="print_sign4" /></td></tr>
		<tr> <td>是否可以更改预收金额:</td> <td>
			<select name="change_prepay" style='width:173px' > 
				<option value="0">否</option> 
				<option value="1" selected=true>是</option> 
			</select></td></tr>
		<tr> <td>是否显示泊位:</td> <td>
			<select name="view_plot" style='width:173px' > 
				<option value="0">否</option> 
				<option value="1" selected=true> 是</option>
		     </select></td></tr>
		<tr> <td>是否隐藏收费汇总:</td> <td>
			<select name="hidedetail" style='width:173px' > 
				<option value="0">否</option> 
				<option value="1" selected=true> 是</option>
		     </select></td></tr>
		<tr> <td>查看汇总密码:</td> <td><input name="collpwd" type="password" /></td></tr>
		<tr> <td>签退是否需要密码:</td> <td>
			<select name="signout_valid" style='width:173px' > 
				<option value="0" selected=true>否</option> 
				<option value="1"> 是</option>
		     </select></td></tr>
		<tr> <td>签退密码:</td> <td><input name="signpwd" type="password" /></td></tr>
		<tr> <td><b>其他设置</b></td> <td></td></tr>
	     <tr> <td>取车检器时间作为POS机录入订单时间:</td> <td>
		<select name="is_sensortime" style='width:173px' > 
			<option value="0">是</option> 
			<option value="1" selected=true> 否</option>
	     </select></td></tr>
	     <tr> <td>收费汇总是否显示卡片数据:</td> <td>
		<select name="is_show_card" style='width:173px' > 
			<option value="0">是</option> 
			<option value="1" selected=true> 否</option>
	     </select></td></tr>
	    <tr> <td>点击结算订单是否打印小票:</td> <td>
		<select name="print_order_place2" style='width:173px' > 
			<option value="0">否</option> 
			<option value="1" selected=true> 是</option>
	     </select></td></tr>
	     <tr> <td>同一车牌在不同车场重复入场:</td> <td>
		<select name="is_duplicate_order" style='width:173px' > 
			<option value="0"> 否</option>
			<option value="1" selected=true>是</option> 
	     </select></td></tr>
	     <tr> <td>小票是否打印收费员名字:</td> <td>
		<select name="is_print_name" style='width:173px' > 
			<option value="0"> 否</option>
			<option value="1" selected=true>是</option> 
	     </select></td></tr>
		<tr> <td><input type="submit" value=" 保 存 "/></td> <td></td></tr>
		</table>
	</form>
</div>
</body>
<script type="text/javascript">
var data = ${data};
var form = document.forms[0];
if(data.photoset){
	form.photoset1.value=data.photoset[0]||0;
	form.photoset2.value=data.photoset[1]||0;
	form.photoset3.value=data.photoset[2]||0;
}else{
	form.photoset1.value="1";
	form.photoset2.value="1";
	form.photoset3.value="1";
}
if(data.prepayset){
	form.prepayset1.value=data.prepayset[0]||0;
	form.prepayset2.value=data.prepayset[1]||0;
	form.prepayset3.value=data.prepayset[2]||0;
}else{
 	form.prepayset1.value="5";
	form.prepayset2.value="10";
	form.prepayset3.value="15";
}
if(data.print_sign){
	var ps = data.print_sign;
	form.print_sign1.value=ps[0]||"";
	form.print_sign2.value=ps[1]||"";
	form.print_sign3.value=ps[2]||"";
	form.print_sign4.value=ps[3]||"";
}else{
	form.print_sign1.value="欢迎光临";
	form.print_sign2.value="欢迎再次光临";
	form.print_sign3.value="欢迎光临";
	form.print_sign4.value="欢迎再次光临";
}
if(data.change_prepay){
	var options = form.change_prepay.options;
	if(data.change_prepay=='1')
		options[1].selected=true;
	else {
		options[0].selected=true;
	}
}
if(data.view_plot){
	var options = form.view_plot.options;
	if(data.view_plot=='1')
		options[1].selected=true;
	else {
		options[0].selected=true;
	}
}
if(data.hidedetail){
	var options = form.hidedetail.options;
	if(data.hidedetail=='1')
		options[1].selected=true;
	else {
		options[0].selected=true;
	}
}
if(data.signout_valid){
	var options = form.signout_valid.options;
	if(data.signout_valid=='1')
		options[1].selected=true;
	else {
		options[0].selected=true;
	}
}
if(data.is_sensortime){
	var options = form.is_sensortime.options;
	if(data.is_sensortime=='1')
		options[1].selected=true;
	else {
		options[0].selected=true;
	}
}
if(data.is_show_card){
	var options = form.is_show_card.options;
	if(data.is_show_card=='1')
		options[1].selected=true;
	else {
		options[0].selected=true;
	}
}
if(data.print_order_place2){
	var options = form.print_order_place2.options;
	if(data.print_order_place2=='1')
		options[1].selected=true;
	else {
		options[0].selected=true;
	}
}
if(data.is_duplicate_order){
	var options = form.is_duplicate_order.options;
	if(data.is_duplicate_order=='1')
		options[1].selected=true;
	else {
		options[0].selected=true;
	}
}
if(data.is_print_name){
	var options = form.is_print_name.options;
	if(data.is_print_name=='1')
		options[1].selected=true;
	else {
		options[0].selected=true;
	}
}
if(data.password){
	form.collpwd.value=data.password;
}
if(data.signout_password){
	form.signpwd.value=data.signout_password;
}
if(data.isprepay){
	var options = form.isprepay.options;
	if(data.isprepay=='1')
		options[1].selected=true;
	else {
		form.prepayset1.disabled=true;
		form.prepayset2.disabled=true;
		form.prepayset3.disabled=true;
		options[0].selected=true;
	}
}
form.role_id.value=data.role_id;
function prepayset(value){
	if(value==0){
		form.prepayset1.disabled=true;
		form.prepayset2.disabled=true;
		form.prepayset3.disabled=true;
	}else{
		form.prepayset1.disabled=false;
		form.prepayset2.disabled=false;
		form.prepayset3.disabled=false;
	}
}
</script>
</html>