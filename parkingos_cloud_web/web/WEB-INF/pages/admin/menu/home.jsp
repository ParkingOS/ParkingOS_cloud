<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=gb2312" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> 
<meta name="renderer" content="webkit">
<link href="favicon.ico" mce_href="favicon.ico" rel="icon">
<title>${cloudname}</title>
<link href="css/common.css" rel="stylesheet" type="text/css">
<head>
</head>
<script>
function  logout(){
	location = 'dologin.do?action=out';
};


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
	//debugger;
    var menulength= document.getElementsByTagName('li').length-1;
    //alert(menulength);
	//if(tag.parentNode.className.indexOf("selected") != -1)return;
	for(var i=0;i<menulength;i++){
		document.getElementById("iframe-"+(i+1)+"-div").style.display = "none";
		var bb = document.getElementById("iframe-"+(i+1)+"-iframe");
		if(bb != undefined){
    		bb.style.display = "none";
		}
	}
	//tag.parentNode.className += ' selected';
    if(document.getElementById("iframe-"+(menulength+1)+"-div")){
        document.getElementById("iframe-"+(menulength+1)+"-div").style.display = "none";
    }
	var oDiv = document.getElementById(tag.id+"-div");
	var iDiv = document.getElementById(tag.id+"-iframe");
	oDiv.style.display = "block";
	oDiv.style.marginTop='80px';

	if(iDiv != undefined)
    	iDiv.style.display = "block";
	createIframe(oDiv,tag.id,url);
}

function createIframe(oDiv,id,url){
	//*********选中状态**************//
	for(var i=0;i<menus.length;i++){
		var tag = document.getElementById("iframe-"+(i+1));
		removeClass(tag, "topclasss");
	}
	var tag = document.getElementById(id);
	addClass(tag,"topclasss");
	//****************************//
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
		oFrame.style.height	= gwh() - 80 + "px";//document.documentElement.scrollHeight - 30 +"px";
		if(isIE()&&isIE()<11) {
			window.attachEvent("onresize",function(){oFrame.style.height = gwh() - 80 +"px"})
		}else{
    		window.addEventListener("resize",function(){oFrame.style.height = gwh() - 80 +"px"},false)
		};
		
		oFrame.setAttribute("src",url);
		//oDiv.insertBefore(oFrame,oDiv.childNodes[0]);
		oDiv.appendChild(oFrame);
	}
}
</script>
<body id="bg" onload="init()"  onbeforeunload=" return logout()">
<div class="header">
  <div class="logo"> <img style="width:157px;height:40px;" id ="toplogo" src='${logourl}'> </div>
  <div class="menu">
    <ul id="menues_list">
    </ul>
  </div>
  <div class="login">
    <!-- <div class="admin"><a href="home.html"><img src="images/home01.png">首页</a></div> -->
    <div class="admin"><a><img src="images/admin.png">${nickname}</a></div>
    <div class="logout" onClick="logout()"><a>退出<img src="images/page_logout.png"></a></div>
  </div>
</div>
</body>
<script language="javascript">
var authList  = ${menuauthlist};
var menus = [];

for(var i=0;i<authList.length;i++){
	if(authList[i].pid==0){
		menus.push({"name":authList[i].nname,"url":authList[i].url,"authid":authList[i].auth_id});
	}
};
var groupid ='${groupid}';
//alert(groupid);
var unionId = '${unionId}';
var custumgroup = '${custumgroup}';
if(groupid!=''&&custumgroup.indexOf(groupid)!=-1&&unionId=='200081') {
   document.getElementById("toplogo").src = "images/logo_top_zt.png";
}
function init(){
	var mulele = document.getElementById("menues_list");
	for(var i=0;i<menus.length;i++){
        var mname =menus[i].name;
	    if(groupid!=''&&custumgroup.indexOf(groupid)!=-1&&unionId=='200081') {
            if(mname=='业务订单')
                mname='车辆进出查询';
        }
		var liele = document.createElement("li");
		liele.setAttribute("id", "tag"+(i+1));
		var aurl=menus[i].url+"?authid="+menus[i].authid;
		var innerHTML = '<div></div><a href="#" id="iframe-'+(i+1)+'" tid="'+aurl+'" onClick="switchTag(this,\''+aurl+'\')">'+mname+'</a>';
        if(aurl.indexOf("monitor.do?") > -1){
            innerHTML = '<div></div><a href="'+aurl+'" target="_blank" id="iframe-'+(i+1)+'" tid="'+aurl+'">'+menus[i].name+'</a>';
            liele.innerHTML +=innerHTML;
            mulele.appendChild(liele);
        }else{
            liele.innerHTML +=innerHTML;
            mulele.appendChild(liele);
            var subdiv = document.createElement("div");
            subdiv.setAttribute("id", "iframe-"+(i+1)+"-div");
            subdiv.setAttribute("name", "iframe-"+(i+1)+"-div");
            if(i==0){
                subdiv.style.marginTop='80px';
            }else{
                subdiv.style.display='none';
            }
            document.body.appendChild(subdiv);
        }
	}
	var role ='${role}';
	createIframe(document.getElementById('iframe-1-div'),'iframe-1',menus[0].url+"?authid="+menus[0].authid);
};
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
</html>