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
	<img src='images/flygame/score/today_score_w.png' id='all_score' style='position:absolute' onclick='allscore();'/>
	<img src='images/flygame/score/friend_score_g.png' id='friend_score' style='position:absolute' onclick='friendsocre();'/>
	<img src='images/flygame/score/score_arrow.png' id='score_arrow' style='position:absolute' />
	<div style='position:absolute' id='listdiv' class='listdiv'></div>
	<img src='images/flygame/score/show.png'  style='position:absolute' id ='showbtn' onclick='showScore()' >
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
setobjCss(getobj('all_score'),{'width':parseInt(w*0.26)+'px','left':parseInt(w*0.12)+'px','top':parseInt(h*0.03)+'px'});
setobjCss(getobj('friend_score'),{'width':parseInt(w*0.26)+'px','left':parseInt(w*0.55)+'px','top':parseInt(h*0.03)+'px'});
setobjCss(getobj('score_arrow'),{'width':parseInt(w*0.07)+'px','left':parseInt(w*0.66)+'px','top':parseInt(h*0.078)+'px'});
setobjCss(getobj('listdiv'),{'width':parseInt(w*0.88)+'px','left':parseInt(w*0.06)+'px','height':parseInt(h*0.74)+'px','top':parseInt(h*0.1)+'px'});
setobjCss(getobj('showbtn'),{'width':parseInt(w*0.68)+'px','left':parseInt(w*0.16)+'px','top':parseInt(h*0.87)+'px'});

var dtype='${dtype}';
if(dtype=='')
	dtype='all';
function allscore(){
	setobjCss(getobj("score_arrow"),{'left':parseInt(w*0.21)+'px'});
	getobj('all_score').src='images/flygame/score/today_score_g.png';
	getobj('friend_score').src='images/flygame/score/friend_score_w.png';
	getData('all');
	getobj('showbtn').src='images/flygame/score/show.png';
	getobj('showbtn').onclick=function(){showScore()};
	dtype='all';
}
function friendsocre(){
	setobjCss(getobj("score_arrow"),{'left':parseInt(w*0.66)+'px'});
	getobj('all_score').src='images/flygame/score/today_score_w.png';
	getobj('friend_score').src='images/flygame/score/friend_score_g.png';
	getData('friend');
	dtype='friend';
	getobj('showbtn').src='images/flygame/score/show_no.png';
	getobj('showbtn').onclick=function(){return false;};
}
var allsort={};
var mySort = 0;
var myScore=0;
function viewScore(type,data) {
	mySort = 0;
	var bos = getobj('listdiv');
	if(getobj('listdata')){
		bos.removeChild(getobj('listdata'));
		bos.removeChild(getobj('buttdiv'));
	}
	var fsdiv = document.createElement("div");
	fsdiv.setAttribute('id','listdata');
	setobjCss(fsdiv,{'top':parseInt(h*0.016)+'px','left':parseInt(w*0.022)+'px','width':parseInt(w*0.84)+'px',
		'position':'absolute','height':parseInt(h*0.62)+'px','overflowY':'auto'});
	var dlen=data.length;
	if(dlen>0){
		for(var i=0;i<dlen;i++){
			var fdiv = document.createElement("div");
			fdiv.setAttribute('id','score_'+i);
			setobjCss(fdiv,{'top': parseInt((i)*h*0.5*0.19)+'px','backgroundColor':'#FFFFFF','width':'100%',
				'position':'absolute','height':parseInt(h*0.095*0.9)+'px'});
			//排名	
			var sortnum = document.createElement("span");
			var sortleft =parseInt(w*0.015)+'px'
			var numberfontcolor='#c2cece';
			if(i==0)
				numberfontcolor='#f7898f';
			else if(i==1)
				numberfontcolor='#f3d251';
			else if(i==2)
				numberfontcolor='#a7e54c';
			if(i>=99)
				sortleft ='0px'
			setobjCss(sortnum,{'top':parseInt(h*0.001)+'px','left':sortleft,'width':parseInt(w*0.04)+'px',
				'position':'absolute','fontWeight':'700','color':numberfontcolor,'fontSize':parseInt(w*0.043)+'px','fontStyle':'oblique'});
			sortnum.innerHTML='<b>'+(i+1)+'</b>';	
			fdiv.appendChild(sortnum);
			//每一条
			var eimg= document.createElement("img");
			setobjCss(eimg,{'top':parseInt(h*0.01)+'px','left':parseInt(w*0.07)+'px','width':parseInt(w*0.1)+'px',
			'position':'absolute','borderRadius':parseInt(w*0.05)+'px'});
			eimg.src=data[i].wximgurl;
			
			fdiv.appendChild(eimg);
			
			var words = document.createElement("span");
			setobjCss(words,{'top':parseInt(h*0.01)+'px','left':parseInt(w*0.228)+'px','width':parseInt(w*0.4)+'px',
				'position':'absolute','fontWeight':'600','color':'#333333','fontSize':parseInt(w*0.043)+'px'});
			words.innerHTML='<b>'+data[i].car+'</b>';
			
			var datetime = document.createElement("span");
			setobjCss(datetime,{'top':parseInt(h*0.048)+'px','left':parseInt(w*0.228)+'px','width':parseInt(w*0.3)+'px',
				'position':'absolute','fontWeight':'600','color':'#999999','fontSize':parseInt(w*0.033)+'px'});
			datetime.innerHTML='<b>最高积分：'+data[i].score+'</b>';
			
			var viewdetail = document.createElement("span");
			viewdetail.setAttribute('id',''+data[i].uin);
			
			setobjCss(viewdetail,{'top':parseInt(h*0.02)+'px','left':parseInt(w*0.61)+'px','width':parseInt(w*0.17)+'px',
				'position':'absolute','fontWeight':'700','color':'#333333','fontSize':parseInt(w*0.033)+'px','textAlign':'center',
				'border':'solid 2px #bbbbbb','height':parseInt(h*0.038)+'px','lineHeight':parseInt(h*0.038)+'px','borderRadius':'4px'});
			viewdetail.innerHTML='<b>战绩详情</b>';
			viewdetail.onclick=function(){viewDetail(type,this)};
			
			if(data[i].uin==='${uin}'){
				var meimg= document.createElement("img");
				setobjCss(meimg,{'top':parseInt(h*0.001)+'px','left':parseInt(w*0.76)+'px','width':parseInt(w*0.07)+'px',
				'position':'absolute'});
				meimg.src='images/flygame/score/me.png';
				fdiv.appendChild(meimg);
				mySort=i+1;
				myScore=data[i].score;
			}
			
			allsort[data[i].uin]={'sort':i+1,'tid':data[i].tid};
			
			fdiv.appendChild(datetime);
			fdiv.appendChild(viewdetail);
			fdiv.appendChild(words);
			fsdiv.appendChild(fdiv);
		}
		//底部div
		var buttdiv = document.createElement("div");
		setobjCss(buttdiv,{'top':parseInt(h*0.65)+'px','left':parseInt(w*0.022)+'px','width':parseInt(w*0.84)+'px',
			'position':'absolute','height':parseInt(h*0.5*0.19)+'px'});
		buttdiv.setAttribute('id','buttdiv');
		
		var butwords = document.createElement("span");
		butwords.setAttribute('id','butwords');
			setobjCss(butwords,{'top':parseInt(h*0.016)+'px','left':parseInt(w*0.19)+'px','width':parseInt(w*0.7)+'px',
				'position':'absolute','fontWeight':'700','color':'#666666','fontSize':parseInt(w*0.043)+'px'});
		if(mySort==0){
			butwords.innerHTML='<b>您未进入排行榜</b>';
		}else
			butwords.innerHTML='<b>我在所有车友中，排第<font color="#f7898f">'+mySort+'</font>名</b>';
		
		var cupimg= document.createElement("img");
		setobjCss(cupimg,{'top':parseInt(h*0.01)+'px','left':parseInt(w*0.062)+'px','width':parseInt(w*0.1)+'px','position':'absolute'});
		cupimg.src='images/flygame/score/score_cup.png';
			
		buttdiv.appendChild(cupimg);
		buttdiv.appendChild(butwords);
			
		
		bos.appendChild(fsdiv);
		bos.appendChild(buttdiv);
	}
}
function getData(type){
	var data = eval(T.A.sendData("flygame.do?action=viewscore&uin=${uin}&dtype="+type));
	if(data){
		viewScore(type,data);
	}
}
//选择停车券
function viewDetail(type,obj){
	location = "flygame.do?action=scoredetail&uin="+obj.id+"&itsort="+allsort[obj.id].sort+"&tid="+allsort[obj.id].tid;
}

function showScore(){
	if(mySort==0){
		alert('您没有排名，不能分享！');
		return ;
	}
	var ishare=${ishare};
	if(ishare==0){
		getobj('showbtn').onclick=function(){return false;};
		//var words=getobj('butwords').innerText;
		var words='我最高得分'+myScore+'，在所有车主中排名第'+mySort;
		location = "flygame.do?action=tosharescore&uin=${uin}&type="+dtype+"&words="+encodeURI(encodeURI(words));
	}else
		alert('今天已经分享过哟！');
}
if(dtype=='all')
	allscore();
else if(dtype='friend')
	friendsocre();
</script>
</html>