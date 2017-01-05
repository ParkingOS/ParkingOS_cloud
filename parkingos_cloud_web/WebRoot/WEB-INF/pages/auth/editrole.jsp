<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>设置功能权限</title>
<link href="css/authrole.css" rel="stylesheet" type="text/css">
<script src="js/tq.js?08100744" type="text/javascript">//表格</script>

</head>
<body>
<div id="data_container" class="functions_container" >
<div id="maindiv" class="functions_page">
    <dl class="tooles_menu">		
      <dd ><div class='save' onclick='save();'>保 存</div></dd>
      <dd>
       <label>角色名称：${rolename } </label>
	  <dd>
    </dl>
 </div>

</div>
  <dl class="tooles_menu">		
      <dd ><div class='save' onclick='save();'>保 存</div> </dd>
    </dl>
</body>
<script>
var isadmin=${isadmin};//是否是管理员
var auths = ${allauths};//
/* [{"id":"1","pid":"0","nname":"停车场管理","sub_auth":"注册车场,修改,删除,设置,车场趋势"},
 {"id":"6","pid":"0","nname":"会员管理","sub_auth":"发送短信,审核通过"},
 {"id":"7","pid":"0","nname":"月卡会员","sub_auth":"注册,编辑,修改车牌,删除,导出"},
 {"id":"8","pid":"0","nname":"电子支付","sub_auth":""},
 {"id":"9","pid":"8","nname":"提现管理","sub_auth":"提现申请"},
 {"id":"10","pid":"8","nname":"电子收款","sub_auth":""}]*/
 var ownauths = ${ownauths};
 /*
 [{"auth_id":"6","sub_auth":"0,1"},
 {"auth_id":"8","sub_auth":""},
 {"auth_id":"9","sub_auth":"0,1,2"},
 {"auth_id":"10","sub_auth":"0,1,2"}]
 */
var getObj=function(id){return document.getElementById(id)};

var getParentId = function(_id){//查看父权限是否是顶层
	for(var i=0;i<auths.length;i++){
		var pid = auths[i].pid;
		var id = auths[i].id;
		if(_id==id)
			return pid;
	}
	return 0;
}
function check1(obj){
	var id = obj.id;
	var pid = id.split("_")[1];
	var is = document.getElementsByTagName("input");
	for(var i=0;i<is.length;i++){
		var tid = is[i].id;
		if(tid.indexOf("input_"+pid+"_")!=-1)
			is[i].checked = obj.checked;
	}
	//document.getElementsByTagName(tagname)
};
function check2(obj){
	var oid = obj.id;
	var pid = oid.split("_")[1];
	var id = oid.split("_")[2];
	var is = document.getElementsByTagName("input");
	var ischecked=false;
	for(var i=0;i<is.length;i++){
		var tid = is[i].id;
		if(tid.indexOf("input_"+pid+"_"+id+"_")!=-1){
			is[i].checked = obj.checked;
		}
	}
	for(var i=0;i<is.length;i++){
		var tid = is[i].id;
		if(tid.indexOf("input_"+pid+"_")!=-1){
			ischecked = is[i].checked||ischecked;
		}
	}
	getObj("input_"+pid).checked=ischecked;
};
function check3(obj){
	var oid = obj.id;
	var pid = oid.split("_")[2];
	var ischecked = obj.checked;
	if(!ischecked){
		var is = document.getElementsByTagName("input");
		for(var i=0;i<is.length;i++){
			var tid = is[i].id;
			if(tid.indexOf("input_"+pid+"_"+pid+"_")!=-1){
				ischecked = is[i].checked || ischecked;
			}
		}
	}
	getObj("input_"+pid).checked=ischecked;
};
function check4(obj){
	var oid = obj.id;
	var pid = oid.split("_")[1];
	var id = oid.split("_")[2];
	var ischecked = obj.checked;
	if(!ischecked){
		var is = document.getElementsByTagName("input");
		for(var i=0;i<is.length;i++){
			var tid = is[i].id;
			if(tid.indexOf("input_"+pid+"_"+id+"_")!=-1){
				ischecked = is[i].checked || ischecked;
			}
		}
	}
	getObj("input_"+pid+"_"+id).checked=ischecked;
	
	if(!ischecked){
		var is = document.getElementsByTagName("input");
		for(var i=0;i<is.length;i++){
			var tid = is[i].id;
			if(tid.indexOf("input_"+pid+"_")!=-1){
				ischecked = is[i].checked || ischecked;
			}
		}
	}
	getObj("input_"+pid).checked=ischecked;
};


function check5(obj){
	var oid = obj.id;
	var ppid = oid.split("_")[1];
	var pid = oid.split("_")[2];
	var id = oid.split("_")[3];
	var is = document.getElementsByTagName("input");
	var ischecked=false;
	for(var i=0;i<is.length;i++){
		var tid = is[i].id;
		if(tid.indexOf("input_"+ppid+"_"+pid+"_"+id+"_")!=-1){
			is[i].checked = obj.checked;
		}
	}
	for(var i=0;i<is.length;i++){
		var tid = is[i].id;
		if(tid.indexOf("input_"+ppid+"_"+pid+"_")!=-1){
			ischecked = is[i].checked||ischecked;
		}
	}
	for(var i=0;i<is.length;i++){
		var tid = is[i].id;
		if(tid.indexOf("input_"+pid+"_")!=-1){
			ischecked = is[i].checked||ischecked;
		}
	}
	getObj("input_"+ppid).checked=ischecked;
	getObj("input_"+ppid+"_"+pid).checked=ischecked;
};


function check6(obj){
	var oid = obj.id;
	var ppid = oid.split("_")[1];
	var pid = oid.split("_")[2];
	var id = oid.split("_")[3];
	var ischecked = obj.checked;
	if(!ischecked){
		var is = document.getElementsByTagName("input");
		for(var i=0;i<is.length;i++){
			var tid = is[i].id;
			if(tid.indexOf("input_"+ppid+"_"+pid+"_"+id+"_")!=-1){
				ischecked = is[i].checked || ischecked;
			}
		}
	}
	getObj("input_"+ppid+"_"+pid+"_"+id).checked=ischecked;
	
	if(!ischecked){
		var is = document.getElementsByTagName("input");
		for(var i=0;i<is.length;i++){
			var tid = is[i].id;
			if(tid.indexOf("input_"+ppid+"_"+pid+"_")!=-1){
				ischecked = is[i].checked || ischecked;
			}
		}
	}
	getObj("input_"+ppid+"_"+pid).checked=ischecked;
	
	if(!ischecked){
		var is = document.getElementsByTagName("input");
		for(var i=0;i<is.length;i++){
			var tid = is[i].id;
			if(tid.indexOf("input_"+ppid+"_")!=-1){
				ischecked = is[i].checked || ischecked;
			}
		}
	}
	getObj("input_"+ppid).checked=ischecked;
};

var init=function (){
	var div  =getObj("maindiv");
	for(var i=0;i<auths.length;i++){//一级权限
		var pid = auths[i].pid;
		var id = auths[i].id;
		if(pid!=0)
			continue;
		var cdiv = document.createElement("div");
		cdiv.setAttribute("id", "business_"+id);
		var dl = document.createElement("dl");
		dl.className="tilte";
		var dt = document.createElement("dt");
		dt.className = "width200";
		var dthtml = "<input type='checkbox' id='input_"+id+"' class='inputbox' onclick='check1(this)'/>";
		dt.innerHTML=dthtml;
		dl.appendChild(dt);
		
		var subauth = auths[i].sub_auth;
		if(subauth!=''){
			var sa = subauth.split(',');
			if(sa.length>0){
				var dd = document.createElement("dd");
				for(var j=0;j<sa.length;j++){
					var sinp = document.createElement("input");
					sinp.type="checkbox";
					sinp.className="inputbox";
					sinp.setAttribute("id","input_"+id+"_"+id+"_"+j);
					sinp.onclick=function(){check3(this)};
					var label = document.createElement("label");
					label.innerHTML=sa[j];
					dd.appendChild(sinp);
					dd.appendChild(label);
				}
				dl.appendChild(dd);
			}
		}
		
		dt.innerHTML +=auths[i].name;
		cdiv.appendChild(dl);
		div.appendChild(cdiv);
	}
	for(var k=0;k<auths.length;k++){//子权限
		var pid = auths[k].pid;
		var id = auths[k].id;
		if(pid==0)
			continue;
		var pdiv = getObj("business_"+pid);
		var sdiv = 	document.createElement("div");
		sdiv.setAttribute("id", "business_"+id);
		var dl = document.createElement("dl");
		dl.className="pl20";
		var ppid = getParentId(pid)//父级的父级
		var preId = "input_"+pid+"_"+id;
		if(ppid!=0){//父级还存在父级
			preId = "input_"+ppid+"_"+pid+"_"+id;
			dl.className="p220";
		}
		var dt = document.createElement("dt");
		dt.className = "width250";
		var dthtml = "<input type='checkbox' id='"+preId+"' class='inputbox' onclick='check2(this)'/>";
		if(ppid!=0){
			dthtml = "<input type='checkbox' id='"+preId+"' class='inputbox' onclick='check5(this)'/>";
		}
		dt.innerHTML=dthtml;
		dl.appendChild(dt);
		dt.innerHTML +=auths[k].name;
		var subauth = auths[k].sub_auth;
		if(subauth!=''){
			var sa = subauth.split(',');
			if(sa.length>0){
				var dd = document.createElement("dd");
				for(var j=0;j<sa.length;j++){
					var asinp = document.createElement("input");
					asinp.type="checkbox";
					asinp.className="inputbox";
					asinp.setAttribute("id",preId+"_"+j);
					asinp.onclick=function(){check4(this)};
					if(ppid!=0){
						asinp.onclick=function(){check6(this)};
					}
					var label = document.createElement("label");
					label.innerHTML=sa[j];
					dd.appendChild(asinp);
					dd.appendChild(label);
				}
				dl.appendChild(dd);
			}
		}
		if(pdiv){
			sdiv.appendChild(dl);
			pdiv.appendChild(sdiv);
		}
		
	}
	//加载自己的权限
	/*[{"auth_id":"6","sub_auth":"0,1","pid":"0"},{"auth_id":"8","sub_auth":"null","pid":"0"},
	{"auth_id":"9","sub_auth":"0,1,2","pid":"8"},{"auth_id":"10","sub_auth":"0,1,2","pid":"8"}]*/
	for(var m = 0;m<ownauths.length;m++){
		var sm = ownauths[m];
		var aid =sm.auth_id;
		var sa = sm.sub_auth;
		var p = sm.pid;
		var pp = getParentId(p);
		if(p==0){
			getObj("input_"+aid).checked=true;
			if(sa!=''){
				var aus  = sa.split(',');
				for(var n=0;n<aus.length;n++){
					getObj("input_"+aid+"_"+aid+"_"+aus[n]).checked=true;
				}
			}
		}else{
			if(pp==0){
				getObj("input_"+p+"_"+aid).checked=true;
				if(sa!=''){
					var aus  = sa.split(',');
					for(var f=0;f<aus.length;f++){
						getObj("input_"+p+"_"+aid+"_"+aus[f]).checked=true;
					}
				}
			}else{//还有父级
				getObj("input_"+pp+"_"+p+"_"+aid).checked=true;
				if(sa!=''){
					var aus  = sa.split(',');
					for(var f=0;f<aus.length;f++){
						getObj("input_"+pp+"_"+p+"_"+aid+"_"+aus[f]).checked=true;
					}
				}
			}
		}
	}
	
	
}

var save = function(){
	var inputs = document.getElementsByTagName("input");
	var res = "";
	for(var i=0;i<inputs.length;i++){
		var checked = inputs[i].checked;
		if(!checked)
			continue;
		var id = inputs[i].id;
		var ids = id.split('_');
		if(ids.length==2){
			res +=ids[1]+"|"
		}else if(ids.length==3){
			res +=ids[2]+"|"
		}else if(ids.length==4){
			res +=ids[2]+","+ids[3]+"|"
		}else if(ids.length==5){
			res +=ids[3]+","+ids[4]+"|"
		}
	}
	if(res.length>0){
		res = res.substring(0,res.length-1);
		T.A.sendData("authrole.do?action=edit","post","roleid=${roleid}&auths="+res,
				function deletebackfun(ret){
					if(ret=="1"){
						T.loadTip(1,"保存成功！",2,"");
					}else{
						T.loadTip(1,"保存失败！",2,"");
					}
				}
		);
	}
};



//初始化
window.onload=init();
</script>
</html>
