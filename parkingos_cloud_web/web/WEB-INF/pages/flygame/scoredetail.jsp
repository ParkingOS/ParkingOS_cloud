<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<html>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<head>
<title>打灰机了~~</title>
<style type="text/css">
		html,body {
		    padding: 0 !important;
			margin: 0 !important;
		    background-color:#f5f5f5;
		    width:100%;
		    height:100%;
		    font-family:"微软雅黑";
		    overflow-x:hidden !important;
		    background-size: 100% 100%;
			background-position:top center
		}
		.listdiv{
			background-size: 100% 100%;
			background-position:top center;
		    background-image: url(images/flygame/score/score_bg.png);
		    background-repeat:no-repeat;
		}
		</style>
</head>
<script src="js/tq.js?081744" type="text/javascript">//表格</script>
<body id='body' >
	<img src='${wximgurl}' id='head' style='position:absolute' onclick='allscore();'/>
	<div style='position:absolute' id='wxname'>${car}</div>
	<div style='position:absolute' id='listdiv' class='listdiv'></div>
	<img src='images/flygame/score/send_message.png'  style='position:absolute' id ='sendmesgbtn' onclick='sendMesage()' >
</body>

<script language="JavaScript" >
var getobj=function(id){return document.getElementById(id)};
var h = getobj('body').offsetHeight;
var w = getobj('body').offsetWidth;

function setobjCss(obj,css){
	for(var c in css){
		obj.style[c]=css[c];
	}
}
setobjCss(getobj('head'),{'width':parseInt(w*0.3)+'px','left':parseInt(w*0.35)+'px','top':parseInt(h*0.05)+'px','borderRadius':parseInt(w*0.15)+'px'});
setobjCss(getobj('wxname'),{'width':parseInt(w*0.3)+'px','left':parseInt(w*0.35)+'px','top':parseInt(h*0.27)+'px','fontSize':parseInt(w*0.06)+'px','textAlign':'center','color':'#666666'});
setobjCss(getobj('listdiv'),{'width':parseInt(w*0.88)+'px','left':parseInt(w*0.06)+'px','height':parseInt(h*0.55)+'px','top':parseInt(h*0.32)+'px'});
setobjCss(getobj('sendmesgbtn'),{'width':parseInt(w*0.68)+'px','left':parseInt(w*0.16)+'px','top':parseInt(h*0.87)+'px','display':'none'});

function showData() {
	var alldata = ${data};
	if(!alldata)
		return;
	var bos = getobj('listdiv');
	var fsdiv = document.createElement("div");
	fsdiv.setAttribute('id','listdata');
	setobjCss(fsdiv,{'top':parseInt(h*0.016)+'px','left':parseInt(w*0.022)+'px','width':parseInt(w*0.84)+'px',
		'position':'absolute','height':parseInt(h*0.45)+'px','overflowY':'auto'});
	for(var i=0;i<14;i++){
		var data = alldata[(i+1)+''];
		var fdiv = document.createElement("div");
		fdiv.setAttribute('id','score_'+i);
		setobjCss(fdiv,{'top': parseInt((i)*h*0.5*0.15)+'px','backgroundColor':'#FFFFFF','width':'100%',
			'position':'absolute','height':parseInt(h*0.12*0.6)+'px'});
		//每一条
		var eimg= document.createElement("img");
		setobjCss(eimg,{'top':parseInt(h*0.01)+'px','left':parseInt(w*0.03)+'px','width':parseInt(w*0.07)+'px','position':'absolute'});
		eimg.src='images/flygame/score/sort_00'+(i+1)+'.png'
		
		fdiv.appendChild(eimg);
		
		var words = document.createElement("span");
		setobjCss(words,{'top':parseInt(h*0.02)+'px','left':parseInt(w*0.15)+'px','width':parseInt(w*0.4)+'px',
			'position':'absolute','fontWeight':'600','color':'#333333','fontSize':parseInt(w*0.043)+'px'});
		if(i==12)
			words.innerHTML='<b>'+getType(i+1)+'&nbsp;&nbsp</b>';
		else
			words.innerHTML='<b>'+getType(i+1)+'&nbsp;&nbsp;x&nbsp;'+parseInt(data.count)+'</b>';
		
		
		var viewdetail = document.createElement("span");
		
		setobjCss(viewdetail,{'top':parseInt(h*0.02)+'px','left':parseInt(w*0.58)+'px','width':parseInt(w*0.23)+'px',
			'position':'absolute','fontWeight':'700','color':'#666666','fontSize':parseInt(w*0.033)+'px','textAlign':'left'});
		viewdetail.innerHTML='<b>积分：'+data.score+'</b>';
		
		fdiv.appendChild(viewdetail);
		fdiv.appendChild(words);
		fsdiv.appendChild(fdiv);
	}
	//底部div
	var buttdiv = document.createElement("div");
	setobjCss(buttdiv,{'top':parseInt(h*0.48)+'px','left':parseInt(w*0.046)+'px','width':parseInt(w*0.84)+'px',
		'position':'absolute','height':parseInt(h*0.5*0.19)+'px'});
	buttdiv.setAttribute('id','buttdiv');
	//处理总积分
	
	/*var butleftwords = document.createElement("span");
		setobjCss(butleftwords,{'top':parseInt(h*0.005)+'px','left':parseInt(w*0.05)+'px','width':parseInt(w*0.3)+'px',
			'position':'absolute','fontWeight':'700','color':'#666666','fontSize':parseInt(w*0.043)+'px'});
	butleftwords.innerHTML='<b>最高排名：<font color="#f7898f">'+alldata['sort']+'</font></b>';
	*/

	var butrightwords = document.createElement("span");
	setobjCss(butrightwords,{'top':parseInt(h*0.005)+'px','left':parseInt(w*0.2)+'px','width':parseInt(w*0.4)+'px',
		'position':'absolute','fontWeight':'700','color':'#666666','fontSize':parseInt(w*0.043)+'px'});
	butrightwords.innerHTML='<b>最高积分：<font color="#f7898f">'+alldata['score']+'</font></b>';
	
	//buttdiv.appendChild(butleftwords);
	buttdiv.appendChild(butrightwords);
	
	bos.appendChild(fsdiv);
	bos.appendChild(buttdiv);
}
//选择停车券
function sendMesage(uin){
	alert(uin);
}
function getType(type){
	if(type==4)
		return "翻倍弹";
	else if(type==3)
		return "清空弹";
	else if(type==2)
		return "余额券灰机";
	else if(type==1)
		return "停车券灰机";
	else if(type==6)
		return "乌云";
	else if(type==5)
		return "乌鸦";
	else if(type==7)
		return "子弹数翻倍";
	else if(type==8)
		return "子弹数减半";
	else if(type==9)
		return "积分翻倍";
	else if(type==10)
		return "积分减半";
	else if(type==11)
		return "弹弓满血";
	else if(type==12)
		return "弹弓血减半";
	else if(type==13)
		return "道具影响";
	else if(type==14)
		return "礼券灰机";
}
showData();
</script>
</html>