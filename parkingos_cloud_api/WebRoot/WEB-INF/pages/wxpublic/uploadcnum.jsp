<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<html>
<head>
<title>添加车牌</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<link rel="stylesheet" href="css/weui.min.css">
<link href="css/jquery-weui.min.css" rel="stylesheet">
<style type="text/css">
 .carnum-input{
 	text-align:center;
 	height:30px;
 	width:25px;
 	font-size:19px;
 	marigin:0px;
 	vertical-align:middle; 
 	border-radius:0px;
 	text-decoration: none;
 	border-color:gray;
	border-top-width: 0px;
	border-right-width: 0px; 
	border-bottom-width: 1px;
	border-left-width: 0px;
 }
 .carnum-select{
 	border-color:#4BC1CD;
 	border-bottom-width: 2px;
 }
 .hide{
 	display:none;
 }
 .keyboard{
 	background:white;
 	text-align:center;
 	position:absolute;
 	bottom:0px;
 	//padding-left:2px;
 	//padding-right:2px;
 }
 .weui-btn_mini{
	 padding:0 0.44em;
	 line-height:1.8;
	 font-size:18px;
 }
 .custom-btn{
 	margin-top:0px;
 	margin-bottom:0px;
 }
 .cartype{
	color:gray;
	padding:5px;
	font-size:18px;
 }
 .cartype-select{
 	color:black;
 	border-radius:15px;
	box-shadow:1px 1px 3px #888888;
 }
</style>
</head>

<body style="background-color:#EEEEEE">
 <div style="box-shadow:2px 2px 4px #888888;background:white;padding:12px;border-radius:2px;width:150px;margin-top:15px;margin-left:10px">
 	<a href="javascript:;" id="cartype1" onclick="choseCar(1)" class="cartype">汽油车</a>&nbsp;&nbsp;
 	<a href="javascript:;" id="cartype2" onclick="choseCar(2)" class="cartype">新能源</a>
 </div>
   <!-- 输入车牌区 -->
	<div align="center" style="margin-top:15px;padding-left:10px;padding-right:10px">
		<div style="padding:4px;height:50px;padding-top:20px;border-radius:4px;background:white;">
		<form method="post" role="form" action="wxpaccount.do?action=tocarnumber&openid=${openid}" id="checkform">
			<input readonly style="vertical-align:middle;border:0px solid;width:55px;text-decoration: none;font-size:18px" value="车牌号">
			<input  max-length="1" onfocus="this.blur();" onclick="clickCum(this)" id="carnum1" name="carnum1" class="carnum-input" value="">
			<input  max-length="1" onfocus="this.blur();" onclick="clickCum(this)" id="carnum2" name="carnum2" class="carnum-input" value="">
			<input  max-length="1" onfocus="this.blur();" onclick="clickCum(this)" id="carnum3" name="carnum3" class="carnum-input" value="">
			<input  max-length="1" onfocus="this.blur();" onclick="clickCum(this)" id="carnum4" name="carnum4" class="carnum-input" value="">
			<input  max-length="1" onfocus="this.blur();" onclick="clickCum(this)" id="carnum5" name="carnum5" class="carnum-input" value="">
			<input  max-length="1" onfocus="this.blur();" onclick="clickCum(this)" id="carnum6" name="carnum6" class="carnum-input" value="">
			<input  max-length="1" onfocus="this.blur();" onclick="clickCum(this)" id="carnum7" name="carnum7" class="carnum-input" value="">
			<input  max-length="1" onfocus="this.blur();" onclick="clickCum(this)" id="carnum8" name="carnum8" class="carnum-input hide" value="">
			<input  max-length="1" onfocus="this.blur();" onclick="clickCum(this)" id="carnum9" name="carnum9" class="carnum-input hide" value="">
		</form>
		</div>
	</div>
    
    <div style="margin-top:24px">
		<a href="javascript:;" class="weui-btn weui-btn_primary" style="width:90%" name="99" style="box-shadow:2px 2px 4px #888888;">确定</a>  
    </div>
    
<!-- 键盘区 -->
    <div>
	<!-- 汉字键盘区 -->
	<div id="cha" class="keyboard hide">
		<div style="margin-top:5px">
			<a href="javascript:;" id="char0" class="weui-btn weui-btn_mini weui-btn_plain-default" name="京">京</a>
			<a href="javascript:;" id="char1" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="沪">沪</a>
			<a href="javascript:;" id="char2" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="浙">浙</a>
			<a href="javascript:;" id="char3" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="粤">粤</a>
			<a href="javascript:;" id="char4" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="苏">苏</a>
			<a href="javascript:;" id="char5" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="鲁">鲁</a>
			<a href="javascript:;" id="char6" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="晋">晋</a>
		</div>
		<div>
			<a href="javascript:;" id="char7" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="冀">冀</a>
			<a href="javascript:;" id="char8" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="豫">豫</a>
			<a href="javascript:;" id="char9" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="川">川</a>
			<a href="javascript:;" id="char10" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="渝">渝</a>
			<a href="javascript:;" id="char11" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="辽">辽</a>
			<a href="javascript:;" id="char12" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="吉">吉</a>
			<a href="javascript:;" id="char13" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="黑">黑</a>
		</div>
		<div>       		    
			<a href="javascript:;" id="char14" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="皖">皖</a>
			<a href="javascript:;" id="char15" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="鄂">鄂</a>
			<a href="javascript:;" id="char16" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="湘">湘</a>
			<a href="javascript:;" id="char17" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="赣">赣</a>
			<a href="javascript:;" id="char18" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="闽">闽</a>
			<a href="javascript:;" id="char19" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="陕">陕</a>
			<a href="javascript:;" id="char20" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="甘">甘</a>
		</div>
		<div >                 				  
			<a href="javascript:;" id="char21" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="宁">宁</a>
			<a href="javascript:;" id="char22" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="蒙">蒙</a>
			<a href="javascript:;" id="char23" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="津">津</a>
			<a href="javascript:;" id="char24" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="贵">贵</a>
			<a href="javascript:;" id="char25" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="云">云</a>
			<a href="javascript:;" id="char26" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="桂">桂</a>
			<a href="javascript:;" id="char27" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="琼">琼</a>
		</div>
		<div style="margin-bottom:12px">
			<a href="javascript:;" id="char28" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="青">青</a>
			<a href="javascript:;" id="char29" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="新">新</a>
			<a href="javascript:;" id="char30" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="藏">藏</a>
			<a href="javascript:;" id="char31" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="港">港</a>
			<a href="javascript:;" id="char32" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="澳">澳</a>
			<a href="javascript:;" id="char33" style="margin-top:7px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="台">台</a>
		</div>
	</div>

	<!-- 数字键盘区 -->
	<div id="num" class="keyboard hide">
		<div style="margin-top:5px">
			<a href="javascript:;" id="num1" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="1">1</a>
			<a href="javascript:;" id="num2" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="2">2</a>
			<a href="javascript:;" id="num3" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="3">3</a>
			<a href="javascript:;" id="num4" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="4">4</a>
			<a href="javascript:;" id="num5" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="5">5</a>
			<a href="javascript:;" id="num6" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="6">6</a>
			<a href="javascript:;" id="num7" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="7">7</a>
			<a href="javascript:;" id="num8" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="8">8</a>
			<a href="javascript:;" id="num9" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="9">9</a>
			<a href="javascript:;" id="num10" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.5em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="0">0</a>
		</div>
		<div>
			<a href="javascript:;" id="letter1" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.7em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="Q">Q</a>
			<a href="javascript:;" id="letter2" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.7em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="W">W</a>
			<a href="javascript:;" id="letter3" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.7em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="E">E</a>
			<a href="javascript:;" id="letter4" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.7em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="R">R</a>
			<a href="javascript:;" id="letter5" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.7em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="T">T</a>
			<a href="javascript:;" id="letter6" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.7em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="Y">Y</a>
			<a href="javascript:;" id="letter7" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.7em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="U">U</a>
			<a href="javascript:;" id="letter8" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.7em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="P">P</a>
			<a href="javascript:;" id="letter0" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="-1">回删</a>
		</div>
		<div>       		    
			<a href="javascript:;" id="letter9" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="A">A</a>
			<a href="javascript:;" id="letter10" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="S">S</a>
			<a href="javascript:;" id="letter11" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="D">D</a>
			<a href="javascript:;" id="letter12" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="F">F</a>
			<a href="javascript:;" id="letter13" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="G">G</a>
			<a href="javascript:;" id="letter14" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="H">H</a>
			<a href="javascript:;" id="letter15" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="J">J</a>
			<a href="javascript:;" id="letter16" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="K">K</a>
			<a href="javascript:;" id="letter17" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="L">L</a>
		</div>
		<div style="margin-bottom:12px">                 				  
			<a href="javascript:;" id="letter18" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="Z">Z</a>
			<a href="javascript:;" id="letter19" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="X">X</a>
			<a href="javascript:;" id="letter20" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="C">C</a>
			<a href="javascript:;" id="letter21" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="V">V</a>
			<a href="javascript:;" id="letter22" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="B">B</a>
			<a href="javascript:;" id="letter23" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="N">N</a>
			<a href="javascript:;" id="letter24" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="M">M</a>
			<a href="javascript:;" id="letter25" style="margin-top:7px;line-height:2.3;font-size:16px;padding:0 0.6em;" class="weui-btn weui-btn_mini weui-btn_plain-default" name="-2">关闭</a>
		</div>
	</div>
	</div>


<script src="js/jquery.js"></script>
<script src="js/wxpublic/jquery-weui.min.js"></script>
<script src="js/wxpublic/fastclick.js"></script>
<script type="text/javascript">
	var cNode="carnum1";
	var cType="1"
	
	$(function(){
		FastClick.attach(document.body);
		var cha = document.getElementById("cha")
		var carnum1 = document.getElementById("carnum1")
		removeClass(cha, "hide")
		addClass(carnum1,"carnum-select")
		var cartype1 = document.getElementById("cartype1")
		addClass(cartype1,"cartype-select")
		var dwidth = document.body.offsetWidth
		var num = document.getElementById("num")
		var cha = document.getElementById("cha")
		console.log(dwidth)
		num.style.width = dwidth+"px";
		cha.style.width = dwidth+"px";
		
		var a1 = 1/200
		var b1 = 1.1
		var w1 = a1*(screen.width)-b1-0.03
		for(var i=0;i<34;i++){
			var char = document.getElementById("char"+i);
			char.style.padding="0 "+w1+"em";
		}
		
		var a2=1/400
		var b2=0.3
		var w2=a2*(screen.width)-b2-0.02
		for(var i=1;i<11;i++){
			var char = document.getElementById("num"+i);
			char.style.padding="0 "+w2+"em";
		}
		
		var a3=1/400
		var b3=0.2
		var w3=a3*(screen.width)-b3-0.03
		for(var i=9;i<26;i++){
			var char = document.getElementById("letter"+i);
			char.style.padding="0 "+w3+"em";
		}
		for(var i=0;i<9;i++){
			var char2 = document.getElementById("letter"+i);
			char2.style.padding="0 "+(w3-0.1)+"em";
		}
	})
	
	$(".weui-btn").click(function(event){
		var value = this.name;
		if(value=='99'){
			//获取完整车牌
			var sum = "7"
			var carnumber = ""
			if(cType=="2"){
				sum = "8"
			}
			var submitable = true;
			for(var i=1;i<=sum;i++){
				var carnum = document.getElementById("carnum"+i)
				if(i=="2"){
					var m = /^[A-Z]{1}$/;
					if(!carnum.value.match(m)){
						$.alert("车牌号不正确!")
						submitable = false;
						break;
					}
				}
				if(carnum.value==""||typeof(carnum.value)=="undefined"){
					$.alert("车牌号不正确!")
					submitable = false;
					break;
				}
				carnumber += carnum.value
			}
			console.log(carnumber)
			if(submitable){
				carnumber = encodeURI(carnumber);
				uploadcnum(carnumber)				
			}
		}else if(value=='-1'){
			//清除当前节点值
			var cur = document.getElementById(cNode)
			removeClass(cur, "carnum-select")
			var i = parseInt(cNode.substr(6))-1;
			if(i==1){
				var cha = document.getElementById("cha")
				var num = document.getElementById("num")
				addClass(num, "hide");
				removeClass(cha, "hide");
			}else if(i<1){
				i=1
			}
			cNode = "carnum"+i
			var last = document.getElementById(cNode)
			last.value = ""
			addClass(document.getElementById(cNode),"carnum-select")
		}else if(value=='-2'){
			var num = document.getElementById("num")
			addClass(num,"hide")	
		}else{
			var cur = document.getElementById(cNode)
			var i = parseInt(cNode.substr(6))
			if(cType=="1"){
				if(i>=8){
					i=8
				}else{
					cur.value = value;
					i+=1;
				}
			}else{
				if(i>=9){
					i=9
				}else{
					cur.value = value;
					i+=1;
				}
			}
			
			removeClass(cur, "carnum-select")
			cNode = "carnum"+i
			addClass(document.getElementById(cNode),"carnum-select")
			if(i=2){
				var cha = document.getElementById("cha")
				var num = document.getElementById("num")
				addClass(cha, "hide");
				removeClass(num, "hide");
			}
		}
	});
		
		
	function clickCum(node){
		console.log(node);
		var nid = node.id;
		cNode = nid;
		var k = nid.substr(6)//激活的节点
		for(var i=1;i<=8;i++){
			if(i==k){
				addClass(document.getElementById("carnum"+i),"carnum-select")
			}else{
				removeClass(document.getElementById("carnum"+i),"carnum-select")
			}
		}
		
		if(nid==='carnum1'){
			var cha = document.getElementById("cha")
			var num = document.getElementById("num")
			addClass(num, "hide")
			removeClass(cha, "hide");
		}else{
			var cha = document.getElementById("cha")
			var num = document.getElementById("num")
			addClass(cha, "hide");
			removeClass(num, "hide");
		}
	}
	
	function choseCar(type){
		console.log(type)
		var carnum7 =document.getElementById("carnum8")
		var cartype1 =document.getElementById("cartype1")
		var cartype2 =document.getElementById("cartype2")
		if(type=='1'){
			cType="1"
			addClass(cartype1,"cartype-select")
			removeClass(cartype2,"cartype-select")
			addClass(carnum7, "hide")
		}else{
			cType="2"
			addClass(cartype2,"cartype-select")
			removeClass(cartype1,"cartype-select")
			removeClass(carnum7, "hide")
		}
	}
	
	function uploadcnum(carnumber){
		$.showLoading("上传中,请稍后...");
		jQuery.ajax({
				type : "post",
				url : "wxpaccount.do",
				data : {
					'openid' : '${openid}',
					'carid' : '${carid}',
					'carnumber' : carnumber,
					'action' : 'upload',
				},
				//async : false,
				success : function(result) {
					if(result == "-1"){
						setTimeout('$.hideLoading();$.alert("请重新提交")',500)
						//document.getElementById("error").innerHTML = "请重新提交";
					}else if(result == "-2"){
						setTimeout('$.hideLoading();$.alert("该车牌已被注册<br>在公众号内点击【联系客服】解决")',500)
						//$.hideLoading();$.alert("该车牌已被注册<br>在公众号内点击【联系客服】解决")
						//document.getElementById("error").innerHTML = "该车牌已被注册<br>在公众号内点击【联系客服】解决";
					}else if(result == "-3"){
						//$.hideLoading();$.alert("该车牌已被注册<br>在公众号内点击【联系客服】解决")
						setTimeout('$.hideLoading();$.alert("您已注册该车牌!")',500)
						//document.getElementById("error").innerHTML = "您已注册该车牌";
					}else if(result == "-4"){
						setTimeout('$.hideLoading();$.alert("最多添加三个车牌")',500)
						//document.getElementById("error").innerHTML = "最多添加三个车牌";
					}else{
						setTimeout(()=>{$.hideLoading();$.showLoading("添加车牌成功!");setTimeout('$.hideLoading();$("#checkform")[0].submit()',500)},500)
						//setTimeout('$.showLoading("上传成功");$.hideLoading();$("#checkform")[0].submit()',500)
					}
				}
			});
	}
	
</script>
<script type="text/javascript">
			//每次添加一个class
			function addClass(currNode, newClass){
		        var oldClass;
		        oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
		        if(oldClass !== null) {
				   newClass = oldClass+" "+newClass; 
				}
				currNode.className = newClass; //IE 和FF都支持
    		}
			
			//每次移除一个class
			function removeClass(currNode, curClass){
				var oldClass,newClass1 = "";
		        oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
		        if(oldClass !== null) {
				   oldClass = oldClass.split(" ");
				   for(var i=0;i<oldClass.length;i++){
					   if(oldClass[i] != curClass){
						   if(newClass1 == ""){
							   newClass1 += oldClass[i]
						   }else{
							   newClass1 += " " + oldClass[i];
						   }
					   }
				   }
				}
				currNode.className = newClass1; //IE 和FF都支持
			}
			
			//检测是否包含当前class
			function hasClass(currNode, curClass){
				var oldClass;
				oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
				if(oldClass !== null){
					oldClass = oldClass.split(" ");
					for(var i=0;i<oldClass.length;i++){
					   if(oldClass[i] == curClass){
						   return true;
					   }
				   }
				}
				return false;
			}
</script>
</body>
</html>
