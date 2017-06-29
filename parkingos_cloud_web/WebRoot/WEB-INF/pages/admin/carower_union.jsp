<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>客户管理</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.public.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.datatable.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.form.js?0817" type="text/javascript">//表单</script>
<script src="js/tq.searchform.js?0817" type="text/javascript">//查询表单</script>
<script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
</head>
<body>
<div id="carowerobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">

/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
//查看 认证 发送短信 审核车牌
var role=${role};
function getbonustypes (){
	var bonustypes = eval(T.A.sendData("getdata.do?action=getbonustypes"));
	return bonustypes;
}
var bonustypes =getbonustypes();
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",hide:true},
		{fieldcnname:"手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"车牌",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"160" ,height:"",issort:false},
		{fieldcnname:"注册日期",fieldname:"reg_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text",twidth:"200" ,height:"",issort:false,shide:true}
	];
var _carowerT = new TQTable({
	tabletitle:"已上传到泊链的会员",
	ischeck:false,
	tablename:"carower_tables",
	dataUrl:"carower.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=queryunion",
	tableObj:T("#carowerobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function viewpic(name){
	var url = 'viewpic.html?name='+name+'&db=user_dirvier_pics'+'&r='+Math.random();
	Twin({Id:"carstops_edit_pic",Title:"查看照片",Width:850,Height:600,sysfunI:"v_pic",
			Content:"<iframe id='tactic_iframe' src='"+url+"' style='width:100%;height:100%;' frameborder='0' ></iframe>",
			buttons :[],
			CloseFn:function(){
				
			}
		})
}
function setcname(value,pid,colname){
	if(value&&value!='-1'&&value!=''){
		T.A.C({
			url:"camera.do?action=getname&passid="+value,
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
    		waitTip:"正在获取券个数...",
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
	_carowerT.UCD(rowid,name,value);
}

function getAuthButtons(){
	var bts =[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_carowerT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"carower_search_w",Title:"搜索客户",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "carower_search_f",
					formObj:tObj,
					formWinId:"carower_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("carower_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_carowerT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	
	bts.push({dname:"返回会员管理",icon:"edit_add.png",onpress:function(Obj){
		location = "carower.do?authid=${authid}";
	}});
	bts.push({dname:"上传会员到泊链",icon:"edit_add.png",onpress:function(Obj){
		location = "carower.do?action=unioncarowerupload&authid=${authid}";
	}});
	if(bts.length>0)
		return bts;
	else 
		return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}
_carowerT.C();
</script>

</body>
</html>
