<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<title>${cloudname}</title>
<link href="css/base.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">
<style>
	html, body{ background-position:0 -65px;margin:0;padding:0}
	#page-tabs {margin: 0px; padding:0px;  overflow: hidden; position: absolute;left:200px;top:33px;height:26px;}
	</style>
</head>
<script>
function  logout(){
	location = 'dologin.do?action=out';
};

/**function getWType(){
	var w = navigator.userAgent.toLowerCase();
	if(/msie/.test(w) && !/opera/.test(w))
		return "ie";
	else if(/chrome/.test(w)){
		return "ch";
	}else if(/mozilla/.test(w) && !/(compatible|webkit)/.test(w)){
		return "mz";
	}else if(/webkit/.test(w) && !/chrome/.test(w)){
		return "sa";
	}
}
if(getWType()=='mz'){
	window.onbeforeunload=function(e){
		location = 'dologin.do?action=out';
	}
}else if(getWType()=='ch'){
	window.addEventListener("beforeunload", function(event) {
   		 logout();
   		 event.returnValue = "确定 要退出吗？";
	});
}**/
</script>
<body onload="init()"  onbeforeunload=" return logout()">

<script>

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
		h -= 3
	}
	h = h<_h?_h:h;
	return parseInt(h);
}
function gww(_w) {
	var w,_w=_w?_w:0;
	//alert(document.body.clientWidth )
	if (window.innerWidth) {
		w = window.innerWidth;
	}else{
		w = document.documentElement.offsetWidth || document.body.clientWidth || 0;
	};
	w = w<_w?_w:w;
	return parseInt(w);
}
function switchTag(tag,url){
	url = url||tag.title;
    var menulength= document.getElementsByTagName('li').length;
	if(tag.parentNode.className.indexOf("selected") != -1)return;
	for(var i=0;i<menulength;i++){
		if(document.getElementsByTagName('li')[i].className.indexOf("last") != -1){
			document.getElementsByTagName('li')[i].className='last';
		}else{
    		document.getElementsByTagName('li')[i].className='';
		};
		document.getElementById("iframe-"+(i+1)+"-div").style.display = "none";
		var bb = document.getElementById("iframe-"+(i+1)+"-iframe");
		if(bb != undefined){
    		bb.style.display = "none";
		}
	}
	tag.parentNode.className += ' selected';
	
	var oDiv = document.getElementById(tag.id+"-div");
	var iDiv = document.getElementById(tag.id+"-iframe");
	oDiv.style.display = "block";
	oDiv.style.marginTop='66px';
	if(iDiv != undefined)
    	iDiv.style.display = "block";
	createIframe(oDiv,tag.id,url);
}

function createIframe(oDiv,id,url){
	var oFrameName = id +"-iframe";
	var iframe =  document.getElementById(oFrameName);//jQuery("#"+tag.id+"-iframe");
	if(iframe == undefined){
		var oFrame = isIE()&&isIE()<8 ? document.createElement("<iframe name=\"" + oFrameName + "\" id=\"" + oFrameName + "\">") : document.createElement("iframe");
		oFrame.name = oFrameName;
		oFrame.setAttribute("frameborder","0");
		oFrame.setAttribute("scrolling","auto");
		oFrame.id = oFrameName;
		oFrame.style.width = "100%";
		//oFrame.style.height = "100%";
		oFrame.style.height	= gwh() - 67 + "px";//document.documentElement.scrollHeight - 30 +"px";
		if(isIE()&&isIE()<11) {
			window.attachEvent("onresize",function(){oFrame.style.height = gwh() - 67 +"px"})
		}else{
    		window.addEventListener("resize",function(){oFrame.style.height = gwh() - 67 +"px"},false)
		};
		
		oFrame.setAttribute("src",url);
		//oDiv.insertBefore(oFrame,oDiv.childNodes[0]);
		oDiv.appendChild(oFrame);
	}
}
</script>
<div style="width:100%;float:left;height:58px;left:0px;top:0px;background-image:url('images/page_top_bg.png');position: absolute;overflow:hidden;">
   <div style='width:200px;margin-left:19px;margin-top:10px;float:left;'><img src='images/logo_top.png'/></div>
    <div id="page-tabs">
   		<ul id="menues_list"></ul>
    </div>
     <div style='float:right;margin-right:35px;margin-top:13px;' >欢迎您 【${nickname}】&nbsp;&nbsp;&nbsp;&nbsp;<span style='cursor:pointer;' onclick='logout()'><img src='images/page_logout.png'/>&nbsp;退出</span></div>
</div>
<div style="width:100%;height:8px;top:58px;position: absolute;background-image:url('images/page_top_buttom.png');overflow:hidden;">
</div>

<script language="javascript">
var authList  = ${menuauthlist};
var menus = [
	//{"name":"概况","url":"member.do"},
	//{"name":"订单管理","url":"order.do"},
	//{"name":"月卡会员","url":"vipuser.do"},
	//{"name":"电子支付","url":"parkepaymenu.do"},
	//{"name":"统计分析","url":"parkanlysis.do"},
	//{"name":"车位管理","url":"compark.do?comid=${comid}"},
	//{"name":"设备管理","url":"carplate.do"},
	//{"name":"员工权限 ","url":"membermanage.do"},
	//{"name":"系统管理","url":"parkmanagemenu.do"}
];

for(var i=0;i<authList.length;i++){
	if(authList[i].pid==0){
		menus.push({"name":authList[i].nname,"url":authList[i].url,"authid":authList[i].auth_id});
	}
};

function init(){
	var mulele = document.getElementById("menues_list");
	for(var i=0;i<menus.length;i++){
		var liele = document.createElement("li");
		liele.setAttribute("id", "tag"+(i+1));
		var aurl=menus[i].url+"?authid="+menus[i].authid;
		var innerHTML = '<div></div><a href="#" id="iframe-'+(i+1)+'" title="'+aurl+'" onclick="switchTag(this)">'+menus[i].name+'</a>';
		/*var aele = document.createElement("a");
		aele.setAttribute("id", "iframe-"+(i+1));
		aele.url=menus[i].url+"?authid="+menus[i].authid;
		aele.addEventListener('click',function(){switchTag(this)});
		//aele.onclick = function(){switchTag(this)};
		aele.innerText = menus[i].name;
		aele.style.cursor='pointer';*/
		liele.innerHTML +=innerHTML;
		//liele.appendChild(aele);
		if(i==menus.length-1){
			liele.className="last";
			var lastindiv = document.createElement("div");
			lastindiv.className='last';
			liele.appendChild(lastindiv);
		}
		mulele.appendChild(liele);
		var subdiv = document.createElement("div");
		subdiv.setAttribute("id", "iframe-"+(i+1)+"-div");
		subdiv.setAttribute("name", "iframe-"+(i+1)+"-div");
		if(i==0){
			subdiv.style.marginTop='66px';
		}else{
			subdiv.style.display='none';
		}
		document.body.appendChild(subdiv);
	}
	var role ='${role}';
	/*if(role=='3'){
		document.getElementById('tag2').style.display='none';
		document.getElementById('tag3').style.display='none';
		document.getElementById('tag4').style.display='none';
	}*/
	createIframe(document.getElementById('iframe-1-div'),'iframe-1',menus[0].url+"?authid="+menus[0].authid);
	document.getElementById('tag1').className='last selected';
	if(i==1)
	document.getElementById('tag1').className='last selected';
};


</script>
 <!-- <ul>
     <li id="tag5">
		<div></div> <a href="#" id="iframe-5" onclick="switchTag(this,'order.do')">订单管理</a>
	</li>
	 <li id="tag5">
		<div></div> <a href="#" id="iframe-5" onclick="switchTag(this,'order.do')">订单管理</a>
	</li>
	 <li id="tag3">
		<div></div> <a href="#" id="iframe-3" onclick="switchTag(this,'vipuser.do')">月卡管理</a>
	</li>
	<li id="tag2">
		<div></div> <a href="#" id="iframe-2" onclick="switchTag(this,'parkaccount.do')">电子支付</a>
	</li>
	<li id="tag1">
		<div></div> <a href="#" id="iframe-11" onclick="switchTag(this,'parkanlysis.do')">统计分析</a>
	</li>
	<li id="tag8">
		<div></div> <a href="#" id="iframe-8" onclick="switchTag(this,'compark.do?comid=${comid}')"> 车位管理</a>
	</li>
	<li id="tag9">
		<div></div> <a href="#" id="iframe-9" onclick="switchTag(this,'carplate.do')">设备管理</a>
	</li>
	 <li id="tag4">
		<div></div> <a href="#" id="iframe-4" onclick="switchTag(this,'member.do')">员工权限 </a>
	</li>
     <li id="tag1">
		<div></div> <a href="#" id="iframe-1" onclick="switchTag(this,'withdraw.do')">提现管理</a>
	</li>
	 <li id="tag6">
		<div></div> <a href="#" id="iframe-6" onclick="switchTag(this,'price.do')">价格管理</a>
	</li>
	<li id="tag7">
		<div></div> <a href="#" id="iframe-7" onclick="switchTag(this,'package.do')">套餐管理</a>
	</li>
	<li id="tag10">
		<div></div> <a href="#" id="iframe-10" onclick="switchTag(this,'parkinfo.do')">账户管理</a>
	</li>
        </ul> 
        <div id="iframe-1-div" name = "iframe-1-div" style="display:block;margin-top:66px;"></div>
<div id="iframe-2-div" name = "iframe-2-div" style="display:none"></div>
<div id="iframe-3-div" name = "iframe-3-div" style="display:none"></div>
<div id="iframe-4-div" name = "iframe-4-div" style="display:none"></div>
<div id="iframe-5-div" name = "iframe-5-div" style="display:none"></div>
<div id="iframe-6-div" name = "iframe-6-div" style="display:none"></div>
<div id="iframe-7-div" name = "iframe-7-div" style="display:none"></div>
<div id="iframe-8-div" name = "iframe-8-div" style="display:none"></div>
<div id="iframe-9-div" name = "iframe-9-div" style="display:none"></div>
<div id="iframe-10-div" name = "iframe-10-div" style="display:none"></div>
<div id="iframe-11-div" name = "iframe-11-div" style="display:none"></div>
        -->
</body>
</html>