<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> 
<meta name="renderer" content="webkit">
<title>智慧停车云</title>
<link href="favicon.ico" mce_href="favicon.ico" rel="icon">
<link href="css/common.css" rel="stylesheet" type="text/css">
	<script type="text/javascript" src="js/tq.js"></script>

<head>
</head>

<body id="bg">

<!--左侧菜单开始-->
<div class="container">
<div class="leftsidebar_box">
  <div class="leftmenu" id="leftdiv">
  </div>
</div>
<!--左侧菜单结束--> 

<!--内容开始-->
<div class="main">
    <div class="out">
    	<iframe id="contentiframe" name="framePage" style="width: 100%;" frameborder="0" scrolling="auto" style="margin:0px;padding:0px;"></iframe>
    </div>
</div>
<!--内容结束--> 

<script type="text/javascript" src="js/jquery.js"></script> 
<script type="text/javascript">
var isIE = function(){
	var w = navigator.userAgent.toLowerCase();
	var ieMode = document.documentMode;
	return (/msie/.test(w) && !/opera/.test(w))?(!window.XMLHttpRequest?6:(ieMode?ieMode:7)):false
	//return document.documentMode == 10 ?10:((s =navigator.userAgent.toLowerCase().match(/msie ([\d.]+)/))?  parseInt(s[1]) : false);
};
function gwh(_h) {
	var h,_h=_h?_h:0;
	if (window.innerHeight) {
		h = window.innerHeight;
	}else{
		h = document.documentElement.offsetHeight || document.body.clientHeight || 0;
	};
	if(isIE&&isIE<9)
	{
		h -= 3;
	}
	h = h<_h?_h:h;
	return parseInt(h);
}
var iframe = document.getElementById("contentiframe");
iframe.style.height	= gwh() + "px";
</script>
<script type="text/javascript">
$(".leftsidebar_box dt");
$(".leftsidebar_box dt img").attr("src","images/left/select_xl01.png");
$(function(){
	initFirPage();
	
	$(".leftsidebar_box dt").click(function(){
		dtclick(this);
	});
	
	$(".leftsidebar_box dd").click(function(){
		ddclick(this);
	});
});

function initFirPage(){
	$(".leftsidebar_box dd").hide();
	var firstdt = $(".leftsidebar_box dt")[0];
	dtclick(firstdt);
	var firstdd = $(firstdt).parent().find('dd')[0];
	if(firstdd != undefined){//有二级菜单就打开二级菜单的第一个
		ddclick(firstdd);
		$(firstdd).trigger("click");
	}else{//没有二级菜单就打开一级菜单的第一个
		$(firstdt).trigger("click");
	}
}

function dtclick(tag){
	$(".leftsidebar_box dt").css({"background-color":"#999"});
	$(".leftsidebar_box dd").css({"background-color":"#999"});
	$(tag).css({"background-color": "#5ccdbe"});
	$(tag).parent().find('dd').removeClass("menu_chioce");
	$(".leftsidebar_box dt img").attr("src","images/left/select_xl01.png");
	$(tag).parent().find('img').attr("src","images/left/select_xl.png");
	$(".menu_chioce").slideUp(); 
	$(tag).parent().find('dd').slideToggle();
	$(tag).parent().find('dd').addClass("menu_chioce");
}

function ddclick(tag){
	$(".leftsidebar_box dd").css({"background-color":"#999"});
	$(".leftsidebar_box dt").css({"background-color":"#999"});
	$(tag).css({"background-color": "#5ccdbe"});
	$(tag).parent().find('dd').removeClass("menu_chioce");
	$(tag).parent().find('img').attr("src","images/left/select_xl.png");
	$(tag).parent().find('dd').addClass("menu_chioce");
}
</script>

<script type="text/javascript">
    var groupid ='${groupid}';
    //alert(groupid);
    var unionId = '${unionId}';
    var custumgroup = '${custumgroup}';
var authmenu = eval(T.A.sendData("getdata.do?action=getauthmenu&authid=${authid}"));
	var role=${role};
var getobj=function(id){return document.getElementById(id);};
	for(var i=0;i<authmenu.length;i++){
		var ld = getobj('leftdiv');
		var dl = document.createElement("dl");
		dl.className='xitong';
		var subauths = authmenu[i].subauth;
		if(subauths&&subauths.length>0){
			//dt.onclick=function(){changeImage();};
			dl.innerHTML+='<dt>'+authmenu[i].nname+'<img src="images/left/select_xl01.png"></img></dt>';
			for(var j=0;j<subauths.length;j++){
				var murl = subauths[j].url;
				var surl = murl+"?authid="+subauths[j].auth_id;
				if(murl.indexOf('?')!=-1)
					surl = murl+"&authid="+subauths[j].auth_id;
				dl.innerHTML+='<dd onclick="GotoUrl(\''+surl+'\')"><a href="#">'+subauths[j].nname+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a></dd>';
				//dd.click=function(){getFun(surl)};
			}
		}else{
			var murl = authmenu[i].url;
			var url = murl+"?authid="+authmenu[i].auth_id;
			var uname = authmenu[i].nname;
            if(groupid!=''&&custumgroup.indexOf(groupid)!=-1&&unionId=='200081') {
                if(uname=='月卡会员')
                    uname='渣土车管理';
            }
			if(murl.indexOf('?')!=-1)
				url = murl+"&authid="+authmenu[i].auth_id;
			dl.innerHTML+='<dt onclick="GotoUrl(\''+url+'\')"><a href="#">'+uname+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a></dt>';
		}
		ld.appendChild(dl);
	}

function GotoUrl(url){
	document.getElementById("contentiframe").setAttribute('src',url);
	//document.getElementById("contentiframe").style.heigth='400px';
}

</script>	
</body>
</html>
