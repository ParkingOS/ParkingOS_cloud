<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>抬杆记录</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.public.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.datatable.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.form.js?08555" type="text/javascript">//表单</script>
<script src="js/tq.searchform.js?0817" type="text/javascript">//查询表单</script>
<script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
<script src="js/jquery.js" type="text/javascript"></script>
</head>

<body>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<div id="liftrodobj" style="width:100%;height:100%;margin:0px;"></div>
<form action="" method="post" id="choosecom"></form>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false,edit:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,target:"uin,pass_id",action:"getcollectors,getcompass",twidth:"100" ,height:"",issort:false},
        {fieldcnname:"抬杆编号",fieldname:"liftrod_id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
        {fieldcnname:"收费员",fieldname:"uin",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'uin');
			}},
		{fieldcnname:"时间",fieldname:"ctime",fieldvalue:'',defaultValue:'',inputtype:"date", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"通道",fieldname:"out_channel_id",fieldvalue:'',inputtype:"text",noList:[],action:"",twidth:"160" ,height:"",issort:false},
			/*process:function(value,pid){
				return setcname(value,pid,'pass_id');
			}},*/
		{fieldcnname:"抬杆原因",fieldname:"resume",fieldvalue:'',defaultValue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"图片",fieldname:"img",fieldvalue:'',inputtype:"text", twidth:"145" ,issort:false,shide:true,
			process:function(value,cid,id){
                return "<a href=# onclick=\"viewdetail('hn','"+value+"','"+cid+"')\" style='color:blue'>查看抬杆图片</a>";
			}
		}
	];
var _liftrodT = new TQTable({
	tabletitle:"抬杆记录",
	ischeck:false,
	tablename:"liftrod_tables",
	dataUrl:"citylift.do",
	iscookcol:false,
	//dbuttons:false,
	quikcsearch:coutomsearch(),
	buttons:getAuthButtons(),
	//searchitem:true,
	isidentifier:false,
	param:"action=query",
	tableObj:T("#liftrodobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
//	tHeight:100,
	rpage:20,
	isoperate:getAuthIsoperateButtons()
});
function coutomsearch(){
	var html = "";
	return html;
}
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_liftrodT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"liftrod_search_w",Title:"搜索抬杆记录",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "liftrod_search_f",
					formObj:tObj,
					formWinId:"liftrod_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("liftrod_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_liftrodT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	return bts;
}
//"查看,添加,编辑,删除,编辑辅助价格"
function getAuthIsoperateButtons(){
	var bts = [];
	return false;
}
/*展示抬杆图片的方法*/
function viewdetail(type,value,id){
    var liftrodId =_liftrodT.GD(id,"liftrod_id");
    var comid = _liftrodT.GD(id,"comid");
    var tip = "抬杆记录图片";
    Twin({
        Id:"carpics_detail_"+id,
        Title:"查看图片",
        Width:T.gww()-100,
        Height:T.gwh()-50,
        sysfunI:id,
        /*修改图片注释原来调用逻辑*/
        /* Content:"<iframe name='carpics_detail_'"+id+" id='carpics_detail_'"+id+" src='order.do?action=carpics&orderid="+id+"&comid="+comid+"&r="+Math.random()+"' width='100%' height='"+(T.gwh()-100)+"' frameborder='0' style='overflow:auto;' ></iframe>" */
        Content:"<iframe name='carpics_detail_'"+id+" id='carpics_detail_'"+id+" src='liftrod.do?action=getliftrodpic&liftrodid="+liftrodId+"&comid="+comid+"&r="+Math.random()+"' width='100%' height='"+(T.gwh()-100)+"' frameborder='0' style='overflow:auto;' ></iframe>"
    })
}
function setcname(value,pid,colname){
	var url = "";
	if(colname == "uin"){
		url = "cityorder.do?action=getcollname&id="+value;
	}else if(colname == "pass_id"){
		url = "cityorder.do?action=getpassname&id="+value;
	}
	if(value&&value!='-1'&&value!=''){
		T.A.C({
			url:url,
    		method:"GET",//POST or GET
    		param:"",//GET时为空
    		async:false,//为空时根据是否有回调函数(success)判断
    		dataType:"0",//0text,1xml,2obj
    		success:function(ret,tipObj,thirdParam){
    			if(ret){
					updateRow(pid,colname,ret);
    			}
				else
					updateRow(pid,colname,value);
			},//请求成功回调function(ret,tipObj,thirdParam) ret结果
    		failure:function(ret,tipObj,thirdParam){
				return false;
			},//请求失败回调function(null,tipObj,thirdParam) 默认提示用户<网络失败>
    		thirdParam:"",//回调函数中的第三方参数
    		tipObj:null,//相关提示父级容器(值为字符串"notip"时表示不进行相关提示)
    		waitTip:"正在获取名称...",
    		noCover:true
		})
	}else{
		return "无"
	};
	return "<font style='color:#666'>获取中...</font>";
}

/*更新表格内容*/
function updateRow(rowid,name,value){
	//alert(value);
	if(value)
	_liftrodT.UCD(rowid,name,value);
}

_liftrodT.C();

var getobj=function(id){return document.getElementById(id)};

function viewpic(name){
	var url = 'viewpic.html?name='+name+'&db=lift_rod_pics'+'&r='+Math.random();
	Twin({Id:"carstops_edit_pic",Title:"查看照片",Width:850,Height:600,sysfunI:"v_pic",
			Content:"<iframe id='tactic_iframe' src='"+url+"' style='width:100%;height:100%;' frameborder='0' ></iframe>",
			buttons :[],
			CloseFn:function(){
				
			}
		})
}
function setPic(img,id){
	if(img&&img!=''){
		var url = "liftrod.do?action=liftpic&filename="+img
		return "<a href='#' onclick='viewpic(\""+img+"\")'><img src='"+url+"' style='width:140px;height:100px;border:0px;vertical-align:top'></a>"
	}else
		return "";
}


</script>

</body>
</html>
