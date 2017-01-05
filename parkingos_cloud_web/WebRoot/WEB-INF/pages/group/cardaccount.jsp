<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>卡片明细</title>
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
<div id="accountobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var type = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"充值"},
            {"value_no":1,"value_name":"消费"},{"value_no":2,"value_name":"开卡"},
            {"value_no":3,"value_name":"激活卡片"},{"value_no":4,"value_name":"绑定卡片"}
            ,{"value_no":6,"value_name":"解绑卡片"},{"value_no":5,"value_name":"注销卡片"}];
var charge_type = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"现金充值"},
            {"value_no":1,"value_name":"微信公众号充值"},{"value_no":2,"value_name":"微信客户端充值"},
            {"value_no":3,"value_name":"支付宝充值"},{"value_no":4,"value_name":"预支付退款"},
            {"value_no":5,"value_name":"订单退款"}];
var consume_type = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"支付停车费"},
                   {"value_no":1,"value_name":"预付停车费 "},{"value_no":2,"value_name":"补缴停车费"}
                   ,{"value_no":3,"value_name":"追缴停车费"}];

var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [//城市商户登录显示
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number",twidth:"50" ,height:"",edit:false,issort:false,shide:true,fhide:true},
		{fieldcnname:"卡号",fieldname:"nfc_uuid",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false},
		{fieldcnname:"车主编号",fieldname:"uin",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
				if(value == "-1"){
					return "";
				}else{
					return value;
				}
		}},
		{fieldcnname:"操作类型",fieldname:"type",fieldvalue:'',inputtype:"select", noList:type ,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"充值方式",fieldname:"charge_type",fieldvalue:'',inputtype:"select", noList:charge_type,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"消费方式",fieldname:"consume_type",fieldvalue:'',inputtype:"select", noList:consume_type,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"金额",fieldname:"amount",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"订单号",fieldname:"orderid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
						if(value == "-1"){
							return "";
						}else{
							return value;
						}
				}},
		{fieldcnname:"操作人",fieldname:"uid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				return setcname(value,pid,'uid');
			}},
		{fieldcnname:"记录日期",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false}
	];

var _accountT = new TQTable({
	tabletitle:"卡片明细",
	ischeck:false,
	tablename:"account_tables",
	dataUrl:"cardaccount.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#accountobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_accountT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"account_search_w",Title:"搜索账户明细",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "account_search_f",
					formObj:tObj,
					formWinId:"account_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("account_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_accountT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						});
					}
				});	
			}
		});
	
	}});
	
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	
	if(bts.length <= 0){return false;}
	return bts;
}

function setcname(value,pid,colname){
	if(value&&value!='-1'&&value!=''){
		var url = "";
		if(colname == "uid"){
			url = "getdata.do?action=nickname&id="+value;
		}
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
		_accountT.UCD(rowid,name,value);
}

function setname(value, list){
	if(value != "" && value != "-1"){
		for(var i=0; i<list.length;i++){
			var o = list[i];
			var key = o.value_no;
			var v = o.value_name;
			if(value == key){
				return v;
			}
		}
	}else{
		return "";
	}
}

_accountT.C();
</script>

</body>
</html>
