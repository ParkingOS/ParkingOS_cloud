<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
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
		    background-color:#ffffff;
		    width:100%;
		    height:100%;
		    font-family:"微软雅黑";
		    overflow-x:auto;
		     background-size: 100% 100%;
			background-position:top center;
		    background-color:#F0F0F0;
		    background-image: url(images/flygame/b_g.png);
		    background-repeat:no-repeat;
		}
		
		*{margin:0; padding:0;}
		a{text-decoration: none;}
		img{max-width: 100%; height: auto;}
		.weixin-tip{display: none; position: fixed; left:0; top:0; bottom:0; background: rgba(0,0,0,0.8); filter:alpha(opacity=80);  height: 100%; width: 100%; z-index: 100;}
		.weixin-tip p{text-align: center; margin-top: 10%; padding:0 5%;}
		</style>
</head>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js" type="text/javascript"></script>
<script>
wx.config({
    debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
    appId: '${appid}', // 必填，公众号的唯一标识
    timestamp:'${timestamp}', // 必填，生成签名的时间戳
    nonceStr: '${nonceStr}', // 必填，生成签名的随机串
    signature: '${signature}',// 必填，签名，见附录1
    jsApiList: [
    	'checkJsApi', 'onMenuShareTimeline', 'onMenuShareAppMessage', 'onMenuShareQQ',
        'onMenuShareWeibo','hideMenuItems',  'showMenuItems', 'hideAllNonBaseMenuItem',
        'showAllNonBaseMenuItem', 'translateVoice', 'startRecord',  'stopRecord',
        'onRecordEnd','playVoice','pauseVoice','stopVoice','uploadVoice','downloadVoice',
        'chooseImage','previewImage','uploadImage','downloadImage','getNetworkType',
        'openLocation','getLocation','hideOptionMenu','showOptionMenu','closeWindow',
        'scanQRCode', 'chooseWXPay','openProductSpecificView','addCard','chooseCard','openCard'
        ] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
});

wx.hideMenuItems({
    menuList: ['menuItem:share:appMessage','menuItem:share:qq','menuItem:share:weiboApp','menuItem:favorite','menuItem:openWithQQBrowser','menuItem:copyUrl','menuItem:openWithSafari'] // 要隐藏的菜单项，只能隐藏“传播类”和“保护类”按钮，所有menu项见附录3
});

var linkurl="";
var imgUrl = "";
var desc='${bwords}';
var title ='${btitle}';
var url  ='http://s.tingchebao.com/zld/';
//var url  ='http://yxiudongyeahnet.vicp.cc/zld/';
//linkurl= url +'flygame.do?action=viewbonus&uin=${uin}&id=${bonusid}';
linkurl="https://open.weixin.qq.com/connect/oauth2/authorize?appid=${appid}&redirect_uri=http%3A%2F%2F${bakurl}%2Fzld%2Fflygame.do%3Faction%3Dgetbonus%26bid%3D${bonusid}%26uin%3D${uin}&response_type=code&scope=snsapi_base&state=123#wechat_redirect";

imgUrl = url+'images/flygame/share_b.png';

wx.ready(function() {
	//alert(title);alert(linkurl);alert(imgUrl);
	function sharetofriend(){
		wx.onMenuShareAppMessage({
		    title: title, // 分享标题
		    desc: desc, // 分享描述
		    link: linkurl, // 分享链接
		    imgUrl: imgUrl, // 分享图标
		    type: '', // 分享类型,music、video或link，不填默认为link
		    dataUrl: '', // 如果type是music或video，则要提供数据链接，默认为空
		    success: function () { 
		        // 用户确认分享后执行的回调函数
		    	//wx.closeWindow();
		    	document.getElementById("toscore").submit();
		    },
		    cancel: function () { 
		        // 用户取消分享后执行的回调函数
		        //document.getElementById("togame").submit();
		    	//$(".weixin-tip").hide();
		    }
		});
	}
	
	
	function sharetocircle(){//发送到朋友圈
		//alert(title);alert(linkurl);alert(imgUrl);
		wx.onMenuShareTimeline({
		    title: title, // 分享标题
		    link: linkurl, // 分享链接
		    imgUrl: imgUrl, // 分享图标
		    success: function () { 
		        // 用户确认分享后执行的回调函数
		    	//wx.closeWindow();
		    	document.getElementById("toscore").submit();
		    },
		    cancel: function () { 
		        // 用户取消分享后执行的回调函数
		       // document.getElementById("togame").submit();
		    	//$(".weixin-tip").hide();
		    }
		});
	}
	try{sharetocircle();sharetofriend();}catch(e){};
});
//'menuItem:share:appMessage',
function shareinfo(){
	wx.hideMenuItems({
	    menuList: ['menuItem:share:appMessage','menuItem:share:qq','menuItem:share:weiboApp','menuItem:favorite','menuItem:openWithQQBrowser','menuItem:copyUrl','menuItem:openWithSafari'] // 要隐藏的菜单项，只能隐藏“传播类”和“保护类”按钮，所有menu项见附录3
	});
	var sourcetag = navigator.userAgent
	//alert(sourcetag);
	if(navigator.userAgent.indexOf("MicroMessenger")!=-1){
		var winHeight =document.getElementById('body').offsetHeight;
		//alert(winHeight);
		function is_weixin() {
		    var ua = navigator.userAgent.toLowerCase();
		    if (ua.match(/MicroMessenger/i) == "micromessenger") {
		        return true;
		    } else {
		        return false;
		    }
		}
		var isWeixin = is_weixin();
		if(isWeixin){
			document.getElementById("weixin-tip").style.display='block';
            document.getElementById("android").style.height=parseInt(winHeight/1.2)+"px";
            document.getElementById("ios").style.height=parserInt(winHeight/2)+"px";
		}
	}else if(sourcetag.indexOf("Android")!=-1){//来自android
		window.share.share(linkurl,title,desc,imgUrl)
	}else if(sourcetag.indexOf("iPhone")!=-1){//来自iphone
		var u = 'http://s.tingchebao.com';
		//var u='http://yxiudongyeahnet.vicp.cc';
		location = u+'?desc='+desc+'&title='+title+'&imgurl='+imgUrl+'&url='+linkurl;
	}
}
</script>
<body id='body' >
<div class="weixin-tip" id='weixin-tip'>
		<p>
			<img id="android" style="height:500px;" src="images/wxpublic/mask_android1.png" />
			<img id="ios" style="height:400px;" src="images/wxpublic/mask_ios1.png" />
		</p>
	</div>
	<div> </div>
<form action="flygame.do?action=viewscore" method='post' id='toscore'>
		<input type='hidden' name='dtype' value='${dtype}'/>
		<input type='hidden' name='uin' value='${uin}'/>
</form>
</body>
<script>

var userAgent = navigator.userAgent.toLowerCase();
if(userAgent.match(/iphone os/i) == "iphone os"){
	document.getElementById("android").style.display = "none";
}else{
	document.getElementById("ios").style.display = "none";
}

shareinfo();
</script>
</html>