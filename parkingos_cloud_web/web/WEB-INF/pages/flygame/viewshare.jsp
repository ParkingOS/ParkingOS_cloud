<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<html>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
<meta content="yes" name="apple-mobile-web-app-capable">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="x-ua-compatible" content="IE=edge">
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<head>
<title>我在打灰机，求超越</title>
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
		.topdiv{
			background-size: 100% 100%;
			background-position:top center;
		    background-image: url(images/flygame/score/share_top.jpg);
		    background-repeat:no-repeat;
		}
		.middlediv{
			background-size: 100% 100%;
			background-position:top center;
		    background-image: url(images/flygame/score/share_middle.png);
		    background-repeat:no-repeat;
		}
		</style>
</head>
<script src="js/tq.js?081744" type="text/javascript">//表格</script>
<body id='body' >
	<div style='position:absolute' id='top' class='topdiv'>
		<img src='${wximgurl}' id='headimg' style='position:absolute' onclick='allscore();'/>
		<span id='imgborder' style='position:absolute'></span>
		<span id='topwords' style='position:absolute'>${words}，求超越</span>
	</div>
	<div style='position:absolute' id='middle' class='middlediv'></div>
	<img src='images/flygame/score/to_over.png' id='buttom' style='position:absolute' onclick='toOver();'/>
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
setobjCss(getobj('top'),{'width':parseInt(w)+'px','height':parseInt(h*0.45)+'px','top':parseInt(h*0)+'px'});
setobjCss(getobj('topwords'),{'width':parseInt(w*0.9)+'px','left':parseInt(w*0.05)+'px','top':parseInt(h*0.41)+'px',
	'textAlign':'center','fontSize':parseInt(w*0.04)+'px','color':'#333333','fontWeight':'700'});
setobjCss(getobj('headimg'),{'width':parseInt(w*0.5)+'px','height':parseInt(w*0.5)+'px','left':parseInt(w*0.25)+'px','top':parseInt(h*0.05)+'px',
	'borderRadius':parseInt(w*0.25)+'px'});
setobjCss(getobj('imgborder'),{'width':parseInt(w*0.49)+'px','height':parseInt(w*0.49)+'px','left':parseInt(w*0.25)+'px',
	'top':parseInt(h*0.05)+'px','border':'solid 3px #ffffff','borderRadius':parseInt(w*0.25)+'px'});
setobjCss(getobj('middle'),{'width':parseInt(w*0.8)+'px','left':parseInt(w*0.1)+'px','height':parseInt(h*0.4)+'px','top':parseInt(h*0.47)+'px'});
setobjCss(getobj('buttom'),{'width':parseInt(w*0.8)+'px','left':parseInt(w*0.1)+'px','top':parseInt(h*0.9)+'px'});

function toOver(){
	location="https://open.weixin.qq.com/connect/oauth2/authorize?appid=${appid}&redirect_uri=http%3A%2F%2F${bakurl}%2Fzld%2Fflygame.do%3Faction%3Dgetbonus%26bid%3D${bid}%26uin%3D${uin}&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
}
</script>
</html>