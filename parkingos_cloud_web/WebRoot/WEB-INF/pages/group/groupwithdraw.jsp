<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>提现管理</title>
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
<script src="js/jquery.js" type="text/javascript"></script>
</head>
<body>
<div id="withdrawobj" style="width:100%;height:100%;margin:0px;"></div>
<form action="" method="post" id="choosecom"></form>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false];
//查询，提现申请
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/
var balance="${balance}";
var states=[{"value_no":-1,"value_name":"请选择"},{"value_no":0,"value_name":"等待处理"},{"value_no":2,"value_name":"处理中"},{"value_no":3,"value_name":"已支付"},{"value_no":4,"value_name":"提现失败"},{"value_no":5,"value_name":"延迟处理"}];
var _mediaField = [
		{fieldcnname:"提现金额",fieldname:"amount",fieldvalue:'',inputtype:"number", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"提现人",fieldname:"uin",fieldvalue:'',inputtype:"text", twidth:"140" ,height:"",hide:true,
			process:function(value,pid){
				return setcname(value,pid,'uin');
			}},
		{fieldcnname:"类型",fieldname:"wtype",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"请选择"},{"value_no":1,"value_name":"个人提现"},{"value_no":0,"value_name":"公司提现"},{"value_no":2,"value_name":"对公提现"}], twidth:"140" ,height:"",hide:true},
		
		{fieldcnname:"申请时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",hide:true},
		{fieldcnname:"处理日期",fieldname:"update_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:states, twidth:"140" ,height:"",issort:false}
	];
var _withdrawT = new TQTable({
	tabletitle:"提现管理     账户余额："+balance+"元",
	ischeck:false,
	tablename:"withdraw_tables",
	dataUrl:"groupwithdraw.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	quikcsearch:coutomsearch(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#withdrawobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function coutomsearch(){
	var html = "";
	return html;
}
var _mField = [{fieldcnname:"请输入金额(最大:"+balance+"元)",fieldname:"money",defaultValue:(parseInt(balance)),inputtype:"number",width:200}];
//查询，提现申请
function getAuthButtons(){
	var bus = [];
	if(subauth[0])
	bus.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_withdrawT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"withdraw_search_w",Title:"搜索",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "withdraw_search_f",
					formObj:tObj,
					formWinId:"withdraw_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("withdraw_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_withdrawT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						});
					}
				});	
			}
		});
	
	}});
	
	if(parseInt(balance)>=10&&subauth[1])
	bus.push({dname:"提现申请",icon:"edit_add.png",onpress:function(Obj){
		Twin({Id:"user_account_add",Title:"提现申请",Width:280,Height:190,sysfun:function(tObj){
				Tform({
					formname: "user_account_edit_f",
					formObj:tObj,
					recordid:"id",
					Coltype:2,
					nmCls:"r",
					//dbbuttons:[true,false],
					suburl:"groupwithdraw.do?action=withdraw",
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消提现",icon:"cancel.gif", onpress:function(){TwinC("user_account_add");} }
					],
					Callback:function(f,r,c,o){
						if(c=='1'){
							T.loadTip(1,"提现申请成功！",3,null);
							TwinC("user_account_add");
							_withdrawT.M();
						}else if(c=='-1'){
							T.loadTip(1,"您还未绑定支付账号，请联系真来电公司为您绑定账号！",3,null);
							TwinC("user_account_add");
						}else
							T.loadTip(1,"操作失败！",3,null);
					}
				});	
			}
		});
	
	}});
	
	return bus;
}
function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}


function setcname(value,pid,colname){
	if(value&&value!='-1'&&value!=''){
		T.A.C({
			url:"parkwithdraw.do?action=getusername&uin="+value,
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
    		waitTip:"正在获取姓名...",
    		noCover:true
		});
	}else{
		return "";
	};
	return "<font style='color:#666'>获取中...</font>";
}
/*更新表格内容*/
function updateRow(rowid,name,value){
	//alert(value);
	if(value)
	_withdrawT.UCD(rowid,name,value);
}
_withdrawT.C();
</script>

</body>
</html>
