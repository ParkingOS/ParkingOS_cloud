<%@ page language="java" contentType="text/html; charset=gbk"
    pageEncoding="gbk"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<title>我的车牌</title>
<!-- head 中 -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/weui.min.css">
<link href="${pageContext.request.contextPath}/resources/css/jquery-weui.min.css" rel="stylesheet">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/list.css?v=12" />

<style type="text/css">
#scroller li {
    padding:0 10px;
    height:50px;
    line-height:50px;
    background-color:#FFFFFF;
    font-size:14px;
    margin-top:1px;
}

.money{
	font-size:14px;
	margin-right:50px;
}

a{
	text-decoration:none;
	color:#6D6D6D;
	font-size:16px;
	//position: relative;
	//top:-35px;
	//left:30px;
}

#header {
    position:absolute; z-index:2;
    top:0; left:0;
    width:100%;
    height:120px;
    line-height:100px;
    background-color:white;
    padding:0;
    font-size:20px;
    text-align:center;
    z-index: -100;
}
li{
	border-top: 0;
}
.img1{
	width:20px;
	height:20px;
	margin-top:16px;
}
.img2{
	width:80px;
	height:80px;
	margin-top:5px;
}
</style>

<style type="text/css">
* {
	margin: 0px;
	padding: 0px;
}

body {
	font-size: 12px;
	//font: Arial, Helvetica, sans-serif;
	margin: 25PX 0PX;
	background: #eee;
}

.botton {
	color: #F00;
	cursor: pointer;
}

.mybody {
	width: 600px;
	margin: 0 auto;
	height: 1500px;
	border: 1px solid #ccc;
	padding: 20px 25px;
	background: #fff
}

#cwxBg {
	position: absolute;
	display: none;
	background: #000;
	width: 100%;
	height: 100%;
	left: 0px;
	top: 0px;
	z-index: 1000;
}

#cwxWd {
	position: absolute;
	display: none;
	border: 0px solid #CCC;
	padding: 0px;
	background: #FFF;
	z-index: 1500;
	width: 60%;
	height: 100px;
	top: 25%;
}

#cwxCn {
	background: #FFF;
	display: block;
}

.imgd {
	width: 400px;
	height: 300px;
}

.ticket {
	font-size: 15px;
	height:50px;
	margin-left:15px;
	margin-top:2px;
	line-height:50px;
}

.line {
	width: 100%;
	height: 1px;
	background: black;
}
div{border:none;outline:none;}

.passli {
	background-image: url(${pageContext.request.contextPath}/resources/images/wxpublic/arrow.png);
	background-size: 19px 39px;
	background-repeat: no-repeat;
	background-position: right center;
}
.delete{
	background-image: url(${pageContext.request.contextPath}/resources/images/wxpublic/tf_qrcode_close.png);
	background-size: 20px 20px;
	background-repeat: no-repeat;
	background-position: center center;

}
.three{
	padding-left:5px;
	padding-right:5px;
}
.nopass{
	border:1px solid #C94C50;
	color:#C94C50;
}

.passfail{
	border:1px solid #C94C50;
	color:#C94C50;
}

.passing{
	border:1px solid #E9A916;
	color:#E9A916;
}

.pass{
	border:1px solid #00A55D;
	color:#00A55D;
}
.sel_fee{
	font-size:12px;
	text-align:center;
	padding-top:1px;
	padding-bottom:1px;
	border-radius:5px;
	background-color:#FFFFFF;
	outline:medium;
	margin-left:5px;
}
</style>
</head>
<body style="background-color:#EEEEEE;">
<div id="wrapper" style="margin-top:-45px;">
	<div id="scroller">
		<ul id="thelist">
		</ul>
	</div>
</div>
<div id="footer"></div>
<script src="${pageContext.request.contextPath}/resources/js/jquery.js"></script>
<script src="${pageContext.request.contextPath}/resources/js/jquery-weui.min.js"></script>
<script type="text/javascript">
	var count;
	function getcarnumber() {
			jQuery.ajax({
				type : "post",
				url : "getcarnumbers",
				data : {
					'openid' : '${openid}',
					'uin' : '${uin}'
				},
				async : false,
				success : function(jsonData) {
				    //console.log(result)
					//var jsonData = eval("(" + result + ")");
                    count = jsonData.length;
					for ( var i = 0; i < jsonData.length; i++) {
						var id = jsonData[i].id;
						var carnumber = jsonData[i].car_number;
						var encode_carnumber = encodeURI(carnumber)
						var is_default = jsonData[i].is_default;
						var is_auth = jsonData[i].is_auth;
						var shtml = "";
						var phtml = "";
						var classli = "";
						var url = "#";
						if(is_auth == "0"){//未认证
							shtml = "<span class='sel_fee three nopass'>未认证</span>";
							phtml = "去认证";
							classli = "passli";
							url = "wxpaccount.do?action=toupload&carnumber="+encode_carnumber+"&openid=${openid}";
						}else if(is_auth == "1"){
							shtml = "<span class='sel_fee three pass'>已认证</span>";
						}else if(is_auth == "2"){
							shtml = "<span class='sel_fee three passing'>审核中(1-3)天</span>";
						}else if(is_auth == "-1"){
							shtml = "<span class='sel_fee three passfail'>认证未通过</span>";
							phtml = "重新认证";
							classli = "passli";
							url = "wxpaccount.do?action=toupload&carnumber="+encode_carnumber+"&openid=${openid}";
						}
						
						$("#thelist").append('<li><img class="img1" src="${pageContext.request.contextPath}/resources/images/wxpublic/carnumber1.png" /><a ><span style="font-size:17px;top:-4px;left:10px;position:relative">'+carnumber+'</span></a><div class="delete" onclick="delcarnumber(\''+encode_carnumber+'\','+id+')" style="width:30px;height:50px;float:right"></div></li>');
						//$("#thelist").append('<a href="'+url+'"><li class="'+classli+'"><span>认证状态:'+shtml+'</span><span style="float:right;margin-right:30px;color:#6D6D6D">'+phtml+'</span></li></a>');
						//$("#thelist").append('<div style="height:15px"></div>');
					}
					count = parseInt(count);
					if(count < 3){
						$("#thelist").append('<li style="margin-top:20px;"><img class="img1" src="${pageContext.request.contextPath}/resources/images/wxpublic/add.png" /><a style="top:-35px;left:30px;position:relative" href="toaddcarnumber?openid=${openid}&uin=${uin}&forward=tocarnumbers"><div class="company_name"><span>点击添加车牌</span><span class="right1 money"></span></div></a></li>');
					}
				}
			});
		}
		function delcarnumber(carnumber,id) {
			$.confirm("您确定要解绑该车牌吗?", function() {
			  //点击确认后的回调函数
			  $.showLoading("解绑中...");
				jQuery.ajax({
					type : "post",
					url : "delcarnumber",
					data : {
						'car_id' : id,
						'car_number' : carnumber,
					},
					async : true,
					beforeSend :function(){
						
					},
					success : function(result) {
						$.hideLoading();
						if(result>0){
							$.alert("解绑成功!");
							$("#thelist").empty()
							count -= 1
							getcarnumber();
						}else if(result==-2){
							$.alert("解绑失败!该车牌有已锁定的在场订单,请解锁后尝试解绑");
							$("#thelist").empty()
							getcarnumber();
						}else if(result==-3){ 
							$.alert("解绑失败!您的月卡信息异常,请与客服人员联系");
							$("#thelist").empty()
							getcarnumber();
						}else{
							$.alert("解绑失败!您可以尝试重新解绑");
							$("#thelist").empty()
							getcarnumber();
						}
					}
				});
			  }, function() {
			  //点击取消后的回调函数
			  });
		}
	getcarnumber();
</script>
</body>
</html>
