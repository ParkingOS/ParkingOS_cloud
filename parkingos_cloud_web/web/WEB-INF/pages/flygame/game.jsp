<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<html>
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<head>
<title>打灰机了~~</title>
<script src="js/tq.js?08100744" type="text/javascript">//表格</script>
<script src="js/createjs-2015.05.21.min.js" type="text/javascript">//phaser游戏引擎</script>
<script src="js/flygame.first.1.1.min.js?a=733377447" type="text/javascript">//打灰机核心js</script>
<link rel="stylesheet" type="text/css" href="css/game.css?v=20100302" />
<style type="text/css">
		html,body {
		    padding: 0 !important;
			margin: 0 !important;
			border:0px solid #FF0000;
		    background-color: #D2F1FC;
		    width:100%;
		    height:100%;
		    font-family:"微软雅黑";
		    overflow-x:hidden;
		    overflow-y:hidden;
		    background-size: 100% 100%;
			background-position:top center;
		}
			.friendbg{
		    padding: 0 !important;
			margin: 0 !important;
		    font-family:"微软雅黑";
		    background-size: 100% 100%;
			background-position:top center;
		    background-image: url(images/flygame/open_red.png);
		    background-repeat:no-repeat;
		}
			.car_friendbg{
		    padding: 0 !important;
			margin: 0 !important;
		    font-family:"微软雅黑";
		    background-size: 100% 100%;
			background-position:top center;
		    background-image: url(images/flygame/wcar_bg.png);
		    background-repeat:no-repeat;
		}
			.close_friendbg{
		    padding: 0 !important;
			margin: 0 !important;
		    font-family:"微软雅黑";
		    background-size: 100% 100%;
			background-position:top center;
		    background-image: url(images/flygame/open_rec.png);
		    background-repeat:no-repeat;
		}
		.pre_load{
             width: 100%;
             text-align: center;
             color: #6e7a7e;
             font-size: 4em;
             font-weight: bold;
             position: absolute;
             left: 0;
             top: 40%;
         }

        .progress_bg {
            background-color: #cadde3;
            width: 70%;
            height: 50px;
            margin: 0 auto;
            border-radius: 30px;
            box-shadow:1px 1px 2px 1px #aaaaaa inset;
            position: absolute;
            left: 15%;
            top: 75%;
        }
        .progress {
            background-color: #BFEE88;
        	  /**  background: linear-gradient(to bottom, rgba(217,255,169,1),rgba(160,216,91,1));*/
            /*width: 70%;*/
            height: 50px;
            margin: 0 auto;
            border-radius: 30px;
            box-shadow:-1px 1px 2px 1px #aaaaaa;
            position: absolute;
            left: 15%;
            top: 75%;
        }
		</style>
</head>
<img style='display:none' src='images/flygame/ret_ticket.png'/>
<img style='display:none' src='images/flygame/res_cash_noauth.png'/>
<img style='display:none' src='images/flygame/res_cash.png'/>
<img style='display:none' src='images/flygame/res_empty.png'/>
<img style='display:none' src='images/flygame/ret_alert.png'/>
<img style='display:none' src='images/flygame/ret_empty.png'/>
<img style='display:none' src='images/flygame/wcar_nuser.png'/>
<img style='display:none' src='images/flygame/add_friend.png'/>
<img style='display:none' src='images/flygame/share.png'/>
<img style='display:none' src='images/flygame/to_auth.png'/>
<img style='display:none' src='images/flygame/to_view.png'/>
<img style='display:none' src='images/flygame/wcar_touse.png'/>
<img style='display:none' src='images/flygame/wcar_close.png'/>
<img style='display:none' src='images/flygame/close_red.png'/>
<img style='display:none' src='images/flygame/open_red.png'/>
<img style='display:none' src='images/flygame/wcar_bg.png'/>
<img style='display:none' src='images/flygame/open_rec.png'/>


<body id='body' >
	<div id='emptymessage' style='display:none;position:absolute;top:63%;left:25%;z-index:10000;font-size:50px;color:#FFFFFF;width:50%;height:70px;line-height:50px;text-align:center;padding-top:13px;background-color:#8bd3a3'>您打中了一个空灰机</div>
	<form action="flygame.do" method='post' id='togame'>
		<input type='hidden' id='action' name='action' value='pregame'/>
		<input type='hidden' name='uin' value='${uin}'/>
		<input type='hidden' name='agin' value='1'/>
	</form>
	<canvas id="game_canvas"></canvas>
</body>
<script>

function getClientType(){//bnums 剩余子弹数量
	var sourcetag = navigator.userAgent;
	if(navigator.userAgent.indexOf("MicroMessenger")==-1){
		if(sourcetag.indexOf("Android")!=-1){//来自android
			return "and";
		}else if(sourcetag.indexOf("iPhone")!=-1){//来自iphone
			return "ios";
		}
	}else{
		return "wx";
	}
}
var getobj=function(id){return document.getElementById(id)};
var h = getobj('body').offsetHeight;
var w = getobj('body').offsetWidth;

function setobjCss(obj,css){
	for(var c in css){
		obj.style[c]=css[c];
	}
}

var nextindex =0;

function getScroe(type,bnums,gbnums){//bnums 剩余子弹数量
	//type='fly_bird';
	var data =false;
	try{
		//bnums = parseInt(bnums)+ parseInt(gbnums)
		data=eval(T.A.sendData("flygame.do","POST","action=getscore&uin=${uin}&tid=${tid}&type="+type+"&bnums="+bnums+"&gbnums="+gbnums+"&token="+getzldtoken(type+bnums,nextindex)));
	}catch(e){
		resumeGame();
	}
	if(!data){
		resumeGame();
		return false;
	}else{
		if(bnums==0&&gbnums==0){
			noBullets();
			resumeGame();
			return data[0];
		}
	}
	var ret = parseInt(data[0].type);
	var ptype = parseInt(data[0].ptype);
	nextindex = data[0].index;
	showitscore(type,ret,data[0].money,ptype);
	if(data[0].isbz!=null&&data[0].isbz=='1'){
		resumeGame();
		var messa='';
		if(type=='plane_ticket'||type=='plane_cash')
			messa='飞机进入愤怒模式';
		else {
			messa='小鸟进入愤怒模式';
		}
		getobj('emptymessage').innerText=messa;
		getobj('emptymessage').style.display='';
		setTimeout(function(){closeMessage();},2000);
		return data[0];
	}//进入暴走模式 
	
	if(type=='cloud'||type=='crow'){
		resumeGame();
		return data[0];
	}
	
	
	
	var cover = document.createElement("div");
	cover.setAttribute('id','cover');
	setobjCss(cover,{'display':'block','visibility':'visible','width':'100%','height':'100%','background':'#000','position':'absolute',
				'top':'0px','left':'0px','cursor':'not-allowed','filter':'alpha(opacity=40)','opacity':'0.6'});
	//列表框
	var dis = document.createElement("div");
	dis.setAttribute('id','dis');
	if(type=='plane_gift'){
		dis.className='car_friendbg';
	}else
		dis.className='friendbg';
	setobjCss(dis,{'top':parseInt(h*0.20)+'px','left':parseInt(w*0.1)+'px','width':parseInt(w*0.8)+'px',
				'height':parseInt(h*0.6)+'px','position':'absolute','align':'center'});
	dis.zIndex = 1;
	
	//标题
	var title = document.createElement("div");
	setobjCss(title,{'top':parseInt(h*0.028)+'px','left':parseInt(w*0.038)+'px','width':parseInt(w*0.7)+'px',
		'position':'absolute','color':'#F5A2A6','fontWeight':'700','fontSize':parseInt(w*0.038)+'px','textAlign':'center'});
	if(type=='plane_ticket'){//停车券
		var ssize = parseInt(w*0.048)+"px";
		if(ret==1){//普通停车券
			title.innerHTML=data[0].info+"<span style='font-size:"+ssize+"'>"+data[0].money+"元</span>车友的停车券灰机";
		}else if(ret==2){//购买停车券
			title.innerHTML=data[0].info+"<span style='font-size:"+ssize+"'>购买</span>的停车券";
		}else//停车宝停车券
			title.innerHTML=data[0].info;
	}else
		title.innerHTML=data[0].info;
	dis.appendChild(title);
	//提示图片 
	var vimg= document.createElement("img");
	setobjCss(vimg,{'top':parseInt(h*0.14)+'px','left':parseInt(w*0.22)+'px','width':parseInt(w*0.4)+'px','position':'absolute'});
	var addBtn="toshare";
	var inspan = document.createElement("span");
	if(type=='plane_ticket'){//停车券
		if(ret==1){//普通停车券
			vimg.src="images/flygame/ret_ticket_empty.png";
			//车主头像
			var faceimg= document.createElement("img");
			setobjCss(faceimg,{'top':parseInt(h*0.18)+'px','left':parseInt(w*0.31)+'px','width':parseInt(w*0.2)+'px',
				'position':'absolute','borderRadius':'10px'});
			faceimg.src=data[0].imgurl;
			inspan.appendChild(faceimg);
			//认证标志
			var isauth= data[0].isauth;
			if(isauth=='1'){
				var authimg= document.createElement("img");
				setobjCss(authimg,{'top':parseInt(h*0.28)+'px','left':parseInt(w*0.41)+'px','width':parseInt(w*0.1)+'px','position':'absolute'});
				authimg.src="images/flygame/authed.png";
				inspan.appendChild(authimg);
			}
		}else if(ret==2){//购买停车券
			vimg.src="images/flygame/ret_ticket_buy.png";
		}else//停车宝停车券
			vimg.src="images/flygame/ret_ticket.png";
	}else if(type=='plane_cash'){//现金券
		if(ret==-1){
			addBtn="toauth";
			vimg.src="images/flygame/res_cash_noauth.png";
		}else
			vimg.src="images/flygame/res_cash.png";
	}else if(type=='plane_empty')
		vimg.src="images/flygame/res_empty.png";
	else if(type=='fly_bird'){//福袋 
		addBtn="fudai";
		if(ptype&&ptype<7){
			if(ret==1)
				vimg.src="images/flygame/ret_double.png";
			else if(ret==-1){
				addBtn="alert";
				vimg.src="images/flygame/ret_alert.png";
				setobjCss(vimg,{'left':parseInt(w*0.07)+'px','top':parseInt(h*0.03)+'px','width':parseInt(w*0.65)+'px'});
				dis.removeChild(title);
			}else if(ret==0){
				vimg.src="images/flygame/ret_empty.png";
			}else if(ret==2){//第二关入口，关注公众号
				vimg.src="images/flygame/sec_game.png";
				addBtn="topublic";
				setobjCss(vimg,{'top':parseInt(h*0.093)+'px','left':parseInt(w*0.12)+'px','width':parseInt(w*0.55)+'px'});
			}
		}else if(ptype==7){
			vimg.src="images/flygame/bet_double.png";
		}else if(ptype==8){
			vimg.src="images/flygame/bet_halve.png";
		}else if(ptype==9){
			vimg.src="images/flygame/score_double.png";
		}else if(ptype==10){
			vimg.src="images/flygame/score_halve.png";
		}else if(ptype==11){
			vimg.src="images/flygame/catapult_full.png";
		}else if(ptype==12){
			vimg.src="images/flygame/catapult_halve.png";
		}
	}else if(type=='plane_gift'){//广告券
		if(ret==1){
			addBtn="towcar";
			vimg.src="images/flygame/wcar_nuser.png";
		}else if(ret==0){
			addBtn="towcar";
			vimg.src="images/flygame/wcar_ouser.png";
		}else if(ret==2){
			addBtn="renren";
			vimg.src="images/flygame/ren_ren.png";
		}else if(ret==3){
			addBtn="1818";
			vimg.src="images/flygame/1818.png";
		}else if(ret==4){
			addBtn="easypai";
			vimg.src="images/flygame/ret_easypai.png";
		}
	}
	dis.appendChild(vimg);
	dis.appendChild(inspan);
	if(type=='plane_ticket'||type=='plane_cash'){//提示图片中的文字
		if(type=='plane_ticket'&&ret==1){
			
		}else{
			var words = document.createElement("span");
			setobjCss(words,{'top':parseInt(h*0.175)+'px','left':parseInt(w*0.044)+'px','width':parseInt(w*0.73)+'px',
				'position':'absolute','fontWeight':'700','color':'#F5A2A6','fontSize':parseInt(w*0.063)+'px','textAlign':'center'});
			if(parseInt(data[0].money)>0)
				words.innerHTML='<b>'+parseInt(data[0].money)+'元</b>';
			else
				words.innerHTML='<b>'+data[0].money+'元</b>';	
			dis.appendChild(words);
		}
			
	}else if(type=='plane_gift'&&ret==0){
		var words = document.createElement("span");
		var price = data[0].price;
		var le = parseInt(w*0.29)+'px';
		if(parseInt(price)>9)
			le =  parseInt(w*0.24)+'px'
		setobjCss(words,{'top':parseInt(h*0.173)+'px','left':le,'width':parseInt(w*0.33)+'px',
			'position':'absolute','fontWeight':'700','color':'#F5A2A6','fontSize':parseInt(w*0.053)+'px'});
		if(data[0].price){
			words.innerHTML='<b>'+data[0].price+'元</b>';	
		}else
			words.innerHTML='<b></b>';	
		dis.appendChild(words);
	}else if(type=='fly_bird'&&ret==2){
		var allc = data[0].count;//总数量
		var dcou = data[0].dcount;//剩余数量
		var words = document.createElement("span");
		setobjCss(words,{'top':parseInt(h*0.225)+'px','left':parseInt(w*0.03)+'px','width':parseInt(w*0.73)+'px',
			'position':'absolute','fontWeight':'700','color':'#d63201','fontSize':parseInt(w*0.04)+'px','textAlign':'center'});
		words.innerHTML='<b>前期限量发行'+allc+'张</b>';
		
		var twords = document.createElement("span");
		var fsize= parseInt(w*0.06)+'px'
		setobjCss(twords,{'top':parseInt(h*0.255)+'px','left':parseInt(w*0.03)+'px','width':parseInt(w*0.73)+'px',
			'position':'absolute','fontWeight':'700','color':'#d63201','fontSize':parseInt(w*0.045)+'px','textAlign':'center'});
		twords.innerHTML='<b>剩余<font style="font-size:'+fsize+'">'+dcou+'</font>张</b>';	
		
		dis.appendChild(words);
		dis.appendChild(twords);
	}
	var btype = getClientType();
	//分享标题
	var sharetitle = document.createElement("div");
	setobjCss(sharetitle,{'top':parseInt(h*0.45)+'px','left':parseInt(w*0.024)+'px','width':parseInt(w*0.75)+'px',
		'position':'absolute','color':'#F5A2A6','fontWeight':'600','fontSize':parseInt(w*0.031)+'px','textAlign':'center'});
	if(addBtn=='toshare'){
		if(type=='plane_ticket'&&ret==1){
			var wxnamespan = document.createElement("span");
			setobjCss(wxnamespan,{'top':parseInt(h*0.4)+'px','left':parseInt(w*0.024)+'px','width':parseInt(w*0.75)+'px',
				'position':'absolute','color':'#AAABBB','fontWeight':'700','fontSize':parseInt(w*0.035)+'px','textAlign':'center'});
			wxnamespan.innerHTML=data[0].wxname;
			dis.appendChild(wxnamespan);
			sharetitle.innerHTML='';
			//sharetitle.innerHTML='加为好机友后，App中的车友同步添加';
		}else if(type=='plane_ticket'&&ret==2){
			sharetitle.innerHTML='停车券过期后，会退还您账户'+data[0].backmoney+'元余额';
		}else{
			if(btype!='ios'&&((type=='plane_ticket'&&data[0].money>3)||(type=='plane_cash'&&data[0].money>=1)))
				sharetitle.innerHTML='';
			else
				sharetitle.innerHTML='';
		}
	}else if(addBtn=='toauth'){
		if(btype!='ios')
			sharetitle.innerHTML='';
	}else if(addBtn=='fudai'){
		/*var tiptitle = document.createElement("div");
		setobjCss(tiptitle,{'top':parseInt(h*0.37)+'px','left':parseInt(w*0.024)+'px','width':parseInt(w*0.75)+'px',
			'position':'absolute','color':'#B7B7B7','fontWeight':'600','fontSize':parseInt(w*0.031)+'px','textAlign':'center'});
		tiptitle.innerHTML='当前翻倍弹剩余<b>'+data[0].dcount+'</b>,清空弹剩余<b>'+data[0].bcount+'</b>';
		dis.appendChild(tiptitle);*/
		if(btype!='ios'&&((type=='plane_ticket'&&data[0].money>3)||(type=='plane_cash'&&data[0].money>=1)))
			sharetitle.innerHTML='';
		else
			sharetitle.innerHTML='';
	}else if(addBtn=='towcar'){
		var ht= '限北京';
		if(ret==1)
			ht+='<b>新</b>';
		ht+='用户，15天内使用有效';
		sharetitle.innerHTML=ht;
	}else if(addBtn=='renren'){
		sharetitle.innerHTML='';
	}else if(addBtn=='1818'){
		sharetitle.innerHTML= '108现金！投资即可提现！！';
	}else if(addBtn=='easypai'){
		sharetitle.innerHTML= '免费估价，透明买卖';
	}else if(addBtn=='topublic'){
		ht='公众号回复“第二关”了解详情';
		if(btype!='ios')
			sharetitle.innerHTML=ht;
	}
	if(addBtn!='alert')
		dis.appendChild(sharetitle);
	
	//分享图片 
	
	//alert(btype);
	//if(btype=='ios'){//ios客户端不分享和跳转
		var sharespan= document.createElement("span");
		setobjCss(sharespan,{'top':parseInt(h*0.48)+'px','left':parseInt(w*0.015)+'px','width':parseInt(w*0.75)+'px',
			'position':'absolute','color':'#F5A2A6','fontWeight':'600','fontSize':parseInt(w*0.039)+'px','textAlign':'center'});
		if(addBtn=='toshare'||addBtn=='fudai'){
			sharespan.innerHTML='可以我的战绩中分享';
			if(type=='plane_ticket'&&ret==1){//打中了车主停车券，点击加为好机友
				var bu='${uin}';
				var eu = data[0].touin+'';
				if(bu!=eu){
					sharespan=document.createElement("img");
					setobjCss(sharespan,{'top':parseInt(h*0.47)+'px','left':parseInt(w*0.14)+'px','width':parseInt(w*0.54)+'px','position':'absolute'});
					sharespan.src="images/flygame/add_friend.png";
					sharespan.onclick=function(){addfriend(data[0].touin,data[0].sid)};
				}
			}
		}else if(addBtn=='toauth'){
			sharespan.innerHTML='公众号中回复“认证”了解详情';
		}else if(addBtn=='alert'){
			sharespan=document.createElement("img");
			setobjCss(sharespan,{'top':parseInt(h*0.47)+'px','left':parseInt(w*0.14)+'px','width':parseInt(w*0.54)+'px','position':'absolute'});
			sharespan.src="images/flygame/to_view.png";
			sharespan.onclick=function(){toview()};
		}else if(addBtn=='towcar'){
			sharespan.innerHTML='可以我的战绩中查看';
		}else if(addBtn=='renren'){
			sharespan.innerHTML='可以我的战绩中查看';
		}else if(addBtn=='1818'||addBtn=='easypai'){
			sharespan.innerHTML='可以我的战绩中查看';
		}else if(addBtn=='topublic'){
			sharespan.style.top=parseInt(h*0.49)+'px';
			sharespan.innerHTML='您得到了一个第二关入场券';
		}
		dis.appendChild(sharespan);
	/*}else{
		var shareimg= document.createElement("img");
		setobjCss(shareimg,{'top':parseInt(h*0.49)+'px','left':parseInt(w*0.14)+'px','width':parseInt(w*0.54)+'px','position':'absolute'});
		if(addBtn=='toshare'||addBtn=='fudai'){
			if(type=='plane_ticket'&&ret>0){//打中了车主停车券，点击加为好机友
				var bu='${uin}';
				var eu = data[0].touin+'';
				if(bu!=eu){
					shareimg.src="images/flygame/add_friend.png";
					shareimg.onclick=function(){addfriend(data[0].touin,data[0].sid)};
				}else{
					if(data[0].money>3){
						shareimg.src="images/flygame/share.png";
						shareimg.onclick=function(){shareinfo(bnums,data[0].sid,data[0].money,type);closeCover();resumeGame();};
					}
				}
			}else{
				if(addBtn=='fudai'||(type=='plane_cash'&&data[0].money>=1)||(type=='plane_ticket'&&data[0].money>3)){
					shareimg.src="images/flygame/share.png";
					shareimg.onclick=function(){shareinfo(bnums,data[0].sid,data[0].money,type);closeCover();resumeGame();};
				}
			}
		}else if(addBtn=='toauth'){
			shareimg.src="images/flygame/to_auth.png";
			shareimg.onclick=function(){toauth()};
		}else if(addBtn=='alert'){
			shareimg.src="images/flygame/to_view.png";
			shareimg.onclick=function(){toview()};
		}else if(addBtn=='towcar'){
			shareimg.src="images/flygame/wcar_touse.png";
			shareimg.onclick=function(){tousewcar(data[0].url,data[0].gid)};
		}else if(addBtn=='renren'){
			shareimg.src="images/flygame/ren_ren_btn.png";
			shareimg.onclick=function(){tousewcar(data[0].url,data[0].gid)};
		}else if(addBtn=='1818'){
			shareimg.src="images/flygame/1818_btn.png";
			shareimg.onclick=function(){tousewcar(data[0].url,data[0].gid)};
		}else if(addBtn=='topublic'){
			shareimg= document.createElement("span");
			setobjCss(shareimg,{'top':parseInt(h*0.49)+'px','left':parseInt(w*0.015)+'px','width':parseInt(w*0.75)+'px',
				'position':'absolute','color':'#F5A2A6','fontWeight':'600','fontSize':parseInt(w*0.039)+'px','textAlign':'center'});
			shareimg.style.top=parseInt(h*0.51)+'px';
			shareimg.innerHTML='您得到了一个第二关入场券';
		}
		dis.appendChild(shareimg);
	}*/
		
	
	//关闭按键
	var img= document.createElement("img");
	img.setAttribute('id','aimg');
	setobjCss(img,{'top':parseInt(h*0.18)+'px','left':parseInt(w*0.82)+'px','width':parseInt(w*0.1)+'px','position':'absolute'});
	if(type=='plane_gift'){
		img.src="images/flygame/wcar_close.png";
	}else
		img.src="images/flygame/close_red.png";
		
	img.onclick=function(){closeCover();resumeGame();};
	
	document.body.appendChild(cover);
	document.body.appendChild(dis);
	document.body.appendChild(img);
	notify(data[0].mesgs)
	
	return data[0];
}
function showitscore(type,ret,money,ptype){
	var inmesg='积分';
	if(type=='plane_gift'){
		if(ret==4)
			inmesg +='+1.5';
		else
			inmesg +='+0.3';
	}else if(type=='plane_ticket'){
		inmesg +='+'+parseFloat(money*0.1);
	}else if(type=='plane_cash'){
		inmesg +='+'+parseFloat(money*0.5);
	}else if(type=='fly_bird'){
		if(ptype<7){
			if(ret==0){
				inmesg +='-1';
			}else if(ret==1){
				inmesg +='+1';
			}else if(ret==2){
				inmesg +='+2';
			}
		}else{
			if(ptype==7||ptype==9||ptype==11)
				inmesg +='+1';
			else
				inmesg +='-1';
		}
	}else if(type=='cloud'){
		inmesg +='+3';
	}else if(type=='crow'){
		inmesg +='+50';
	}
	if(inmesg.indexOf('.')!=-1)
		inmesg=inmesg.substring(0,inmesg.indexOf('.')+3);
	getobj('emptymessage').innerText=inmesg;
	getobj('emptymessage').style.display='';
	setTimeout(function(){closeMessage();},2000);
}
function shareinfo(title){//bnums 剩余子弹数量
	//type=='plane_ticket'||type=='plane_cash'
	var btype = getClientType();
	var sourcetag = navigator.userAgent;
	if(btype=='wx'){
		location = "flygame.do?action=toshare&uin=${uin}&page=pregame&tid=${tid}&bnums=0&sid=-1&title="+encodeURI(encodeURI(title));
	}else{
		var ds = T.A.sendData("flygame.do","POST","action=getbid&uin=${uin}&sid=-1",false,2);
		var desc=ds.words;//'打中谁放的灰机，就可以成为谁的好机友，更有停车券，余额券任性随你打';
		//var title ='车主'+ds.carid+'正在打灰机，邀你做他的好机友';
		var url  ='http://s.tingchebao.com/zld/';
		//var url  ='http://192.168.199.240/zld/';
		var linkurl = url +'carowner.do?action=getobonus&id='+ds.bid;
		var imgUrl = url+'images/flygame/share_b.png';
		
		if(btype=='and'){//来自android
			window.share.share(linkurl,title,desc,imgUrl)
		}else if(btype=='ios'){//来自iphone
			var u = 'http://s.tingchebao.com';
			location = u+'?desc='+desc+'&title='+title+'&imgurl='+imgUrl+'&url='+linkurl;
		}
	}
}

function tousewcar(url,gid){
	T.A.sendData("flygame.do","POST","action=recordhits&uin=${uin}&gid="+gid,function(ret){
		if(ret=='1')
		location = url;
	});
}
//加为好机友
function addfriend(touin,sid){
	T.A.sendData("flygame.do","POST","action=addfriend&uin=${uin}&touin="+touin+"&sid="+sid,function(ret){
		if(ret==1||ret==2){
			closeCover();
			T.loadTip(1,"加为好机友成功！",2,"",40);
			resumeGame();
			//getNewMessage(touin);
		}else{
			closeCover();
			T.loadTip(1,"抱歉，加入好机友失败，下次吧~~",2,"",40);
			resumeGame();
		}
		
		//resumeGame();
	});
}
/**没有子弹了*/
function noBullets(top_title){
	//bullet_gold
	var data =  eval(T.A.sendData("flygame.do","POST","action=tidscore&tid=${tid}&uin=${uin}"));
	var th_top = top_title||'子弹打光，本局结束';
	var cover = document.createElement("div");
	cover.setAttribute('id','cover');
	setobjCss(cover,{'display':'block','visibility':'visible','width':'100%','height':'100%','background':'#000','position':'absolute',
				'top':'0px','left':'0px','cursor':'not-allowed','filter':'alpha(opacity=40)','opacity':'0.6'});
	//列表框
	var dis = document.createElement("div");
	dis.setAttribute('id','dis');
	dis.className='close_friendbg';
	setobjCss(dis,{'top':parseInt(h*0.20)+'px','left':parseInt(w*0.1)+'px','width':parseInt(w*0.8)+'px',
				'height':parseInt(h*0.6)+'px','position':'absolute','align':'center'});
	dis.zIndex = 10000;
	//标题
	var title = document.createElement("div");
	setobjCss(title,{'top':parseInt(h*0.028)+'px','left':parseInt(w*0.038)+'px','width':parseInt(w*0.7)+'px',
		'position':'absolute','color':'#F5A2A6','fontWeight':'700','fontSize':parseInt(w*0.038)+'px','textAlign':'center'});
	title.innerHTML=th_top;
	dis.appendChild(title);
	//提示图片 
	var vimg= document.createElement("img");
	setobjCss(vimg,{'top':parseInt(h*0.14)+'px','left':parseInt(w*0.22)+'px','width':parseInt(w*0.4)+'px','position':'absolute'});
	vimg.src="images/flygame/game_over.png";
	dis.appendChild(vimg);
	
	var sp1 = document.createElement("span");
	setobjCss(sp1,{'top':parseInt(h*0.17)+'px','left':parseInt(w*0.024)+'px','width':parseInt(w*0.75)+'px',
		'position':'absolute','fontWeight':'600','fontSize':parseInt(w*0.05)+'px','textAlign':'center'});
	sp1.innerHTML='本局积分';
	dis.appendChild(sp1);
	
	var sp2 = document.createElement("span");
	setobjCss(sp2,{'top':parseInt(h*0.21)+'px','left':parseInt(w*0.024)+'px','width':parseInt(w*0.75)+'px',
		'position':'absolute','fontWeight':'600','fontSize':parseInt(w*0.065)+'px','textAlign':'center'});
	sp2.innerHTML= data[0].score;
	dis.appendChild(sp2);
	
	var sp3 = document.createElement("span");
	setobjCss(sp3,{'top':parseInt(h*0.27)+'px','left':parseInt(w*0.024)+'px','width':parseInt(w*0.75)+'px',
		'position':'absolute','fontWeight':'600','fontSize':parseInt(w*0.04)+'px','textAlign':'center'});
	sp3.innerHTML= '本局排名'+data[0].sort;
	dis.appendChild(sp3);
	
	var sp5 = document.createElement("span");
	setobjCss(sp5,{'top':parseInt(h*0.3)+'px','left':parseInt(w*0.15)+'px','width':parseInt(w*0.75)+'px',
		'position':'absolute','color':'#F7898E','fontWeight':'600','fontSize':parseInt(w*0.03)+'px','textAlign':'center'});
	sp5.innerHTML= '更多 >';
	sp5.onclick=function(){getAllScroe()};
	dis.appendChild(sp5);
	
	//分享图片 
	/**var shareimg= document.createElement("img");
	setobjCss(shareimg,{'top':parseInt(h*0.47)+'px','left':parseInt(w*0.43)+'px','width':parseInt(w*0.25)+'px','position':'absolute'});
	shareimg.src="images/flygame/play_agin.png";
	shareimg.onclick=function(){playAgin()};*/
	
	var sp4 = document.createElement("span");
	setobjCss(sp4,{'top':parseInt(h*0.4)+'px','left':parseInt(w*0.024)+'px','width':parseInt(w*0.75)+'px',
		'position':'absolute','color':'#F7898E','fontWeight':'600','fontSize':parseInt(w*0.04)+'px','textAlign':'center'});
	sp4.innerHTML= '再玩一局>';
	sp4.onclick=function(){playAgin()};
	dis.appendChild(sp4);
	
	var shareimg= document.createElement("img");
	setobjCss(shareimg,{'top':parseInt(h*0.47)+'px','left':parseInt(w*0.15)+'px','width':parseInt(w*0.5)+'px','position':'absolute'});
	shareimg.src="images/flygame/share.png";
	shareimg.onclick=function(){shareinfo('我本局得分'+data[0].score+'，在所有车主中排名第'+data[0].sort)};
	
	dis.appendChild(shareimg);
	
	//战绩榜
	/*var rightscore = document.createElement("img");
	setobjCss(rightscore,{'top':parseInt(h*0.47)+'px','left':parseInt(w*0.14)+'px','width':parseInt(w*0.25)+'px','position':'absolute'});
	rightscore.src="images/flygame/view_score.png";
	rightscore.onclick=function(){
		getAllScroe()	}
	
	
	dis.appendChild(rightscore);*/
	//关闭按键
	/*var img= document.createElement("img");
	img.setAttribute('id','aimg');
	setobjCss(img,{'top':parseInt(h*0.18)+'px','left':parseInt(w*0.82)+'px','width':parseInt(w*0.1)+'px','position':'absolute'});
	img.src="images/flygame/close_red.png";
	img.onclick=function(){closeCover();};*/
	
	document.body.appendChild(cover);
	document.body.appendChild(dis);
	//document.body.appendChild(img);
	//recordBullets(0);//提交子弹数
}
//规则
function viewRule(){
	//var url="http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208930805&idx=1&sn=2196e63ca2ecedad4960bee99688f46a#rd";
	
	var cover = document.createElement("div");
	cover.setAttribute('id','cover');
	setobjCss(cover,{'display':'block','visibility':'visible','width':'100%','height':'100%','background':'#000','position':'absolute',
				'top':'0px','left':'0px','cursor':'not-allowed','filter':'alpha(opacity=40)','opacity':'0.6'});
	//列表框
	var dis = document.createElement("div");
	dis.setAttribute('id','dis');
	dis.className='friendbg';
	setobjCss(dis,{'top':parseInt(h*0.20)+'px','left':parseInt(w*0.1)+'px','width':parseInt(w*0.8)+'px',
				'height':parseInt(h*0.5)+'px','position':'absolute','align':'center'});
	dis.zIndex = 1;
	
	//标题图标
	var frimg = document.createElement("img");
	frimg.src="images/flygame/rule_title.png";
	setobjCss(frimg,{'top':parseInt(h*0.038)+'px','left':parseInt(w*0.25)+'px','width':parseInt(w*0.3)+'px','position':'absolute'});
	dis.appendChild(frimg);
	
	
	//停车券列表
	var fsdiv = document.createElement("div");
	setobjCss(fsdiv,{'top':parseInt(h*0.5*0.2)+'px','left':parseInt(w*0.022)+'px','width':parseInt(w*0.76)+'px',
		'position':'absolute','height':parseInt(h*0.5*0.75)+'px','overflowY':'auto'});
	
	//规则图标
	var frimg = document.createElement("img");
	frimg.src="images/flygame/fly_rule.jpg";
	setobjCss(frimg,{'left':parseInt(w*0.01)+'px','width':parseInt(w*0.74)+'px','position':'absolute'});
	fsdiv.appendChild(frimg);
	
	dis.appendChild(fsdiv);
	
	//关闭按键
	var img= document.createElement("img");
	img.setAttribute('id','aimg');
	setobjCss(img,{'top':parseInt(h*0.18)+'px','left':parseInt(w*0.82)+'px','width':parseInt(w*0.1)+'px','position':'absolute'});
	img.src="images/flygame/close_red.png";
	img.onclick=function(){closeCover();resumeGame();};
	
	document.body.appendChild(cover);
	document.body.appendChild(dis);
	document.body.appendChild(img);
}

function getNewMessage(touin){
	var data = eval(T.A.sendData("flygame.do","POST","action=message&fuin=${uin}&tuin="+touin));
	if(data&&data.length>0){
		closeCover();
		mymessages(data,touin);
	}
}

function mymessages(data,touin){
	//覆屏层
	var cover = document.createElement("div");
	cover.setAttribute('id','cover');
	setobjCss(cover,{'display':'block','visibility':'visible','width':'100%','height':'100%','background':'#000','position':'absolute',
				'top':'0px','left':'0px','cursor':'not-allowed','filter':'alpha(opacity=40)','opacity':'0.6'});
	
	//列表框
	var dis = document.createElement("div");
	dis.setAttribute('id','dis');
	dis.className='friendbg';
	//dis.style.backgroundImage='images/flygame/open.png';
	setobjCss(dis,{'top':parseInt(h*0.15)+'px','left':parseInt(w*0.1)+'px','width':parseInt(w*0.8)+'px',
		'position':'absolute','height':parseInt(h*0.65)+'px','align':'center','borderRadius':parseInt(w*0.05)+'px'});
	
	dis.zIndex = 1;
	var cdiv = document.createElement("div");
	setobjCss(cdiv,{'borderBottom':'2px solid #dddddd','position':'absolute','left':parseInt(w*0.02)+'px','width':parseInt(w*0.76)+'px','height':parseInt(h*0.5*0.19)+'px'});
	//好机友
	var frname = document.createElement("span");
	//机友头像
	var owerimg = document.createElement("img");
	var _oname='';//自己名称
	var _oimg='';//自己头像
	var _fname='';//机友名称
	var _fimg='';//机友头像
	for(var i=0;i<2;i++){
		var uin = data[i].id;
		if(uin==touin){
			_fname=data[i].wx_name;
			_fimg=data[i].wx_imgurl;
			owerimg.src=data[i].wx_imgurl;
			frname.innerText=data[i].wx_name;
		}else{
			_oname=data[i].wx_name;
			_oimg=data[i].wx_imgurl;
		}
	}
	//owerimg.src=data[0].wx_imgurl;
	setobjCss(owerimg,{'top':parseInt(h*0.028)+'px','left':parseInt(w*0.022)+'px','width':parseInt(w*0.1)+'px',
		'position':'absolute','borderRadius':parseInt(w*0.05)+'px'});
	
	cdiv.appendChild(owerimg);
	
	//好机友
	setobjCss(frname,{'top':parseInt(h*0.038)+'px','left':parseInt(w*0.18)+'px','width':parseInt(w*0.5)+'px',
		'position':'absolute','fontSize':parseInt(w*0.06)+'px','fontWeight':'600','color':'#BBBBBB'});
	cdiv.appendChild(frname);
	
	//消息列表
	var fsdiv = document.createElement("div");
	fsdiv.setAttribute('id','fsdiv');
	setobjCss(fsdiv,{'top':parseInt(h*0.5*0.2)+'px','left':parseInt(w*0.022)+'px','width':parseInt(w*0.76)+'px',
		'position':'absolute','height':parseInt(h*0.5*0.875)+'px','borderBottom':'2px solid #dddddd','overflowY':'auto'});
	
	var lasttop = 0;
	if(data.length>2){
		for(var i=2;i<data.length;i++){
			var line = 0;
			if(i>2)
				line=parseInt(data[i-1].leng);
			var fdiv = document.createElement("div");
			var dss = fdiv.style;
			fdiv.setAttribute('id',data[i].id);
			dss.position ='absolute';
			var ltop =0;
			if(i>2){
				if(line>2){
					ltop = lasttop+parseInt(line*h*0.5*0.095);
					//dss.height=parseInt(line*h*0.5*0.095)+'px';
				}else{
					//dss.height=parseInt(h*0.5*0.19)+'px';
					ltop = lasttop+parseInt(h*0.5*0.19);
				}
			}
			dss.top = ltop+"px";
			lasttop= ltop;
			//dss.backgroundColor='#dddddd';
			dss.width='100%';
			
			//dss.height=parseInt(h*0.095*0.95)+"px";
			
			var _fuin=data[i].fuin;
			var _tuin=data[i].tuin; 
			//每一个好机友
			var words = document.createElement("span");
			setobjCss(words,{'top':parseInt(h*0.020)+'px','left':parseInt(w*0.14)+'px','width':(parseInt(w*0.45)-16)+'px',
				'position':'absolute','paddingLeft':'8px','paddingRight':'8px','lineHeight':parseInt(h*0.5*0.08)+'px',
				'fontSize':parseInt(w*0.04)+'px','borderRadius':'8px'});
			var wss = words.style;
			//会话头像
			var eimg= document.createElement("img");
			//头像箭头
			var arrowimg=document.createElement("img");
			
			var arss = arrowimg.style;
			var lr = 'left';//消息在左边/右边？
			
			if(_tuin==touin){
				lr='right';
				eimg.src=_oimg;
			}else{
				eimg.src=_fimg;
			}
			var ess = eimg.style;
			ess.position ='absolute';
			ess.top = parseInt(h*0.018)+"px";
			ess.width = parseInt(w*0.1)+"px";
			
			arss.position ='absolute';
			arss.top = parseInt(h*0.03)+"px";
			arss.width = parseInt(w*0.020)+"px";
			
			var wlength =data[i].wleng;
			var ab=8;
			if(lr=='left'){
				words.innerHTML=data[i].message;
				wss.backgroundColor='#A7E54C';
				ess.left =parseInt(w*0.022)+"px";
				arss.left =parseInt(w*0.127)+"px";
				arrowimg.src='images/flygame/left_arrow.png';
				if(wlength<20){
					wss.width=wlength*ab+"px";
				}
			}else{
				words.innerHTML=data[i].message;
				arrowimg.src='images/flygame/right_arrow.png';
				if(parseInt(data[i].leng)<2){
					wss.textAlign='right';
				}
				wss.backgroundColor='#FFFFFF';
				if(wlength<20){
					wss.width=wlength*ab+"px";
					wss.left =  (parseInt(w*0.14)+((parseInt(w*0.45)-16)-wlength*ab))+"px";
				}
				arss.left =parseInt(w*0.585)+"px";
				ess.left =parseInt(w*0.62)+"px";
			}
			fdiv.appendChild(words);
			fdiv.appendChild(arrowimg);
			fdiv.appendChild(eimg);
			fsdiv.appendChild(fdiv);
		}
	}
	//关闭按键
	var img= document.createElement("img");
	img.setAttribute('id','aimg');
	setobjCss(img,{'top':parseInt(h*0.125)+'px','left':parseInt(w*0.82)+'px','width':parseInt(w*0.1)+'px','position':'absolute'});
	img.src="images/flygame/close_red.png";
	img.onclick=function(){closeCover();resumeGame();}
	
	//底部输入框
	var bdiv = document.createElement("div");
	setobjCss(bdiv,{'top':parseInt(h*0.55)+'px','left':parseInt(w*0.022)+'px','width':parseInt(w*0.76)+'px',
		'position':'absolute','height':parseInt(h*0.5*0.2)+'px'});
	bdiv.innerHTML="<input name='mesg' id='mesginput' value='给他留言' onclick='levemesg(this);'/>";
	//bss.backgroundColor="#FF0000";
	//底部右侧图片
	var brimg= document.createElement("img");
	setobjCss(brimg,{'top':parseInt(h*0.56)+'px','left':parseInt(w*0.66)+'px','width':parseInt(w*0.1)+'px','position':'absolute'});
	brimg.src="images/flygame/message_right_red.png";
	brimg.onclick=function sendmesg(){sendmessage(touin);};
	
	
	dis.appendChild(cdiv);
	dis.appendChild(fsdiv);
	dis.appendChild(bdiv);
	dis.appendChild(brimg);
	
	document.body.appendChild(cover);
	document.body.appendChild(dis);
	document.body.appendChild(img);
	
	var mesginput = getobj('mesginput');
	setobjCss(mesginput,{'height':parseInt(h*0.07)+'px','width':parseInt(w*0.54)+'px','marginLeft':'10px',
		'border':'1px solid #BBBBBB','color':'#BBBBBB','fontSize':parseInt(w*0.04)+'px','borderRadius':'15px','paddingLeft':'10px'});
	fsdiv.scrollTop =fsdiv.scrollHeight;
}

/*输入框清空*/
function levemesg(obj){
	//closeTimeout();
	obj.value='';
	//setobjCss(obj,{'color':'#666666'});
}
/*发送消息*/
function sendmessage(touin){
	var message=getobj('mesginput').value;
	if(message=='给他留言'||message==''){
		return ;
	}
	//alert(message);
	if(message&&message.length>0){
		var data = eval(T.A.sendData("flygame.do","POST","action=sendmessge&fuin=${uin}&tuin="+touin+"&message="+encodeURI(encodeURI(message))));
		closeCover();
		mymessages(data,touin);
	}
}


function closeCover(){
	document.body.removeChild(getobj('aimg'));
	document.body.removeChild(getobj('dis'));
	document.body.removeChild(getobj('cover'));
	//调用游戏
	//resumeGame();
}
function hitEmpty(){
	getobj('emptymessage').style.display='';
	setTimeout(function(){closeMessage();},2000);
}
function closeMessage(){
	getobj('emptymessage').innerText='您打中了一个空灰机';
	getobj('emptymessage').style.display='none';
}
function toauth(){
	location='http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=205938292&idx=1&sn=76c6259270d762df187a187fac9e9a8d#rd';
}
function topublic(){
	location='http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209280360&idx=1&sn=c84f017607c19f6bbb0870ca7706fee2#rd';
}

function toview(){
	closeCover();
	viewRule();
	//location.href='http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208930805&idx=1&sn=2196e63ca2ecedad4960bee99688f46a#rd';
}
function playAgin(){
	getobj('togame').submit();
}

function getData(){
	var d = T.A.sendData("flygame.do?action=getdata&uin=${uin}&tid=${tid}","GET","",null,2);
	nextindex = d.index;
	return d;
}
//记录子弹数
function recordBullets(number){
	T.A.sendData("flygame.do","POST","action=recordbets&uin=${uin}&tid=${tid}&bnums="+number);
}

//消息通知 
var index=0;
var scollKeys ;
function notify(mesgs){
	//return ;
	if(getobj('nwords'))
		document.body.removeChild(getobj('nwords'));
	if(getobj('notify'))
		document.body.removeChild(getobj('notify'));
	if(scollKeys)
		clearTimeout(scollKeys); 
	index=0;
	var messages = "";
	if(mesgs&&mesgs.length>0){
		messages +=mesgs[0];
		//index=1;
	}else
		return ;
	var notify = document.createElement("div");
	notify.setAttribute('id','notify');
	setobjCss(notify,{'width':parseInt(w*1)+'px','height':parseInt(h*0.038)+'px','background':'#FFFFFF','position':'absolute',
				'top':'0px','left':'0px','cursor':'not-allowed','filter':'alpha(opacity=40)','opacity':'0.2'});
				
	var nwords = document.createElement("div");
	nwords.setAttribute('id','nwords');
	setobjCss(nwords,{'top':'0px','left':'0px','width':parseInt(w*0.9)+'px','color':'#de8442','fontSize':parseInt(w*0.035)+'px',
				'height':parseInt(h*0.033)+'px','position':'absolute','textAlign':'center','lineHeight':parseInt(h*0.038)+'px'});
	nwords.innerHTML=messages;
	nwords.zIndex=1000;
	
	var notilaba = document.createElement("img");
	setobjCss(notilaba,{'top':'2px','left':'10px','width':parseInt(w*0.038)+'px','position':'absolute'});
	notilaba.src="images/flygame/score/win_logo.png";


	//notify.appendChild(nwords);

	document.body.appendChild(notify);
	document.body.appendChild(nwords);
	document.body.appendChild(notilaba);
	//document.body.appendChild(rightscore);
	//字体移动
	if(mesgs&&mesgs.length>1)
		scrollWords(mesgs);
}
function getAllScroe() {
	// if(confirm("部分手机查看排名后，无法返回继续上次游戏，子弹也可能丢失！                                                                             确定要查看排名吗"))
	 //{
		 location="flygame.do?action=viewscore&uin=${uin}";
	// }
}

//查看本轮占线 
function showTidData() {//查看本轮占线 
	var alldata = eval(T.A.sendData("flygame.do?action=scoredetail&uin=${uin}&tid=${tid}&type=1"));
	if(!alldata)
		return;
	//覆屏层
	var cover = document.createElement("div");
	cover.setAttribute('id','cover');
	setobjCss(cover,{'display':'block','visibility':'visible','width':'100%','height':'100%','background':'#000','position':'absolute',
				'top':'0px','left':'0px','cursor':'not-allowed','filter':'alpha(opacity=40)','opacity':'0.6'});
	
	//列表框
	var dis = document.createElement("div");
	dis.setAttribute('id','dis');
	dis.className='friendbg';
	//dis.style.backgroundImage='images/flygame/open.png';
	setobjCss(dis,{'top':parseInt(h*0.15)+'px','left':parseInt(w*0.1)+'px','width':parseInt(w*0.8)+'px',
		'position':'absolute','height':parseInt(h*0.65)+'px','align':'center','borderRadius':parseInt(w*0.05)+'px'});
	
	dis.zIndex = 1;
	
	var toptitle = document.createElement("span");
	setobjCss(toptitle,{'top':parseInt(h*0.02)+'px','width':parseInt(w*0.8)+'px','height':parseInt(h*0.02)+'px',
		'position':'absolute','fontWeight':'700','color':'#f7898f','fontSize':parseInt(w*0.063)+'px','textAlign':'center'});
	toptitle.innerHTML='本局实时战绩';
	dis.appendChild(toptitle);
	
	var fsdiv = document.createElement("div");
	fsdiv.setAttribute('id','listdata');
	setobjCss(fsdiv,{'top':parseInt(h*0.086)+'px','left':parseInt(w*0.022)+'px','width':parseInt(w*0.76)+'px',
		'position':'absolute','height':parseInt(h*0.47)+'px','overflowY':'auto'});
	var d = alldata[0];
	
	for(var i=0;i<14;i++){
		var data = d[(i+1)+''];
		var fdiv = document.createElement("div");
		fdiv.setAttribute('id','score_'+i);
		setobjCss(fdiv,{'top': parseInt((i)*h*0.5*0.15)+'px','backgroundColor':'#FFFFFF','width':'100%',
			'position':'absolute','height':parseInt(h*0.12*0.6)+'px'});
		//每一条
		var eimg= document.createElement("img");
		setobjCss(eimg,{'top':parseInt(h*0.01)+'px','left':parseInt(w*0.03)+'px','width':parseInt(w*0.08)+'px','position':'absolute'});
		eimg.src='images/flygame/score/sort_00'+(i+1)+'.png'
		
		fdiv.appendChild(eimg);
		
		var words = document.createElement("span");
		setobjCss(words,{'top':parseInt(h*0.02)+'px','left':parseInt(w*0.15)+'px','width':parseInt(w*0.4)+'px',
			'position':'absolute','fontWeight':'600','color':'#333333','fontSize':parseInt(w*0.043)+'px'});
		words.innerHTML='<b>'+getType(i+1)+'&nbsp;&nbsp;x&nbsp;'+parseInt(data.count)+'</b>';
		
		var viewdetail = document.createElement("span");
		setobjCss(viewdetail,{'top':parseInt(h*0.02)+'px','left':parseInt(w*0.52)+'px','width':parseInt(w*0.23)+'px',
			'position':'absolute','fontWeight':'700','color':'#666666','fontSize':parseInt(w*0.033)+'px','textAlign':'left'});
		viewdetail.innerHTML='<b>积分：'+data.score+'</b>';
		
		fdiv.appendChild(viewdetail);
		fdiv.appendChild(words);
		fsdiv.appendChild(fdiv);
	}
	//底部div
	var buttdiv = document.createElement("div");
	setobjCss(buttdiv,{'top':parseInt(h*0.54)+'px','left':parseInt(w*0.022)+'px','width':parseInt(w*0.75)+'px',
		'position':'absolute','height':parseInt(h*0.5*0.19)+'px'});
	buttdiv.setAttribute('id','buttdiv');
	//处理总积分
	
	var butleftwords = document.createElement("span");
		setobjCss(butleftwords,{'top':parseInt(h*0.036)+'px','left':parseInt(w*0.05)+'px','width':parseInt(w*0.4)+'px',
			'position':'absolute','fontWeight':'700','color':'#666666','fontSize':parseInt(w*0.043)+'px'});
	butleftwords.innerHTML='<b>当前积分：<font color="#f7898f">'+d['score']+'</font></b>';
	

	var butrightwords = document.createElement("span");
	setobjCss(butrightwords,{'top':parseInt(h*0.036)+'px','left':parseInt(w*0.4)+'px','width':parseInt(w*0.4)+'px',
		'position':'absolute','fontWeight':'700','color':'#666666','fontSize':parseInt(w*0.043)+'px'});
	butrightwords.innerHTML='<b>实时排名：<font color="#f7898f">'+d['sort']+'</font></b>';
	
	buttdiv.appendChild(butleftwords);
	buttdiv.appendChild(butrightwords);
	
	//关闭按键
	var img= document.createElement("img");
	img.setAttribute('id','aimg');
	setobjCss(img,{'top':parseInt(h*0.125)+'px','left':parseInt(w*0.82)+'px','width':parseInt(w*0.1)+'px','position':'absolute'});
	img.src="images/flygame/close_red.png";
	img.onclick=function(){closeCover();resumeGame();}
	
	dis.appendChild(fsdiv);
	dis.appendChild(buttdiv);
	
	document.body.appendChild(cover);
	document.body.appendChild(dis);
	document.body.appendChild(img);
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
//更换内容
function scrollWords(mesgs){
	var obj=getobj('nwords');
	obj.innerHTML=mesgs[index];
	index++;
	if(index==mesgs.length)
		index =0;
	scollKeys = setTimeout(function (){scrollWords(mesgs);},4000);  
}
//弹弓，被打死
function slingshotDie(){
	noBullets('武器阵亡，本局结束！');
}
//playAgin();

startGame();

//getScroe("fly_bird",1);
var messages = eval('${messages}');
if(messages&&messages.length>0)
	notify(messages);

getobj('emptymessage').innerText='打死绿帽乌鸦积50分';
getobj('emptymessage').style.display='';
setTimeout(function(){closeMessage();},3000);

//hitEmpty();
//getScroe('plane_ticket')// 
//plane_gift   礼品灰机
//plane_ticket 券灰机      停车宝券或车主券
//plane_cash   现金灰机
//fly_bird     是否有鸟  清空福袋或翻倍福袋
//plane_empty  空灰机
</script>
</html>