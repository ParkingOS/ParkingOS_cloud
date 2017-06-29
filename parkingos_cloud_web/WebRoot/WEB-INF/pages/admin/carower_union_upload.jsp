<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>客户管理11</title>
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
var union_id = '${unionId}';
var server_id = '${serverId}';
var union_key='${unionKey}'
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
	tabletitle:"选择会员上传到泊链",
	//ischeck:false,
	tablename:"carower_tables",
	dataUrl:"carower.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	isidentifier:false,
	//searchitem:true,
	param:"action=queryunionupload",
	tableObj:T("#carowerobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});


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
	bts.push({ dname:  "上传到泊链平台", icon: "sendsms.gif", onpress:function(Obj){
		var sids = _carowerT.GS();
		var ids="";
		if(!sids){
			T.loadTip(1,"请先选择会员",2,"");
			return;
		}
		Twin({Id:"send_message_w",Title:"同步到泊链平台",Width:550,sysfun:function(tObj){
			Tform({
				formname: "send_message_ff",
				formObj:tObj,
				recordid:"id",
				suburl:"carower.do?action=uploadcarowertounion",
				method:"POST",
				Coltype:2,
				dbuttonname:["确认上传"],
				formAttr:[{
					formitems:[{kindname:"",kinditemts:[
					{fieldcnname:"会员 编号",fieldname:"seleids",fieldvalue:sids,inputtype:"multi"},
					{fieldcnname:"厂商 平台编号",fieldname:"server_id",fieldvalue:union_id,inputtype:"text",edit:false},
					{fieldcnname:"服务商编号",fieldname:"server_id",fieldvalue:server_id,inputtype:"text",edit:false},
					{fieldcnname:"签名KEY",fieldname:"union_key",fieldvalue:union_key,inputtype:"text",edit:false}
					]}]
				}],
				buttons : [//工具
					{name: "cancel", dname: "取消", tit:"取消同步到泊链",icon:"cancel.gif", onpress:function(){TwinC("send_message_w");} }
				],
				Callback:function(f,rcd,ret,o){
					if(ret!==''){
						T.loadTip(1,ret,2,"");
						TwinC("send_message_w");
						_carowerT.C();
					}else{
						T.loadTip(1,"同步到泊链失败",2,o);
					}
				}
			});	
			}
		})
		
	}})
	bts.push({dname:"返回泊链会员管理",icon:"edit_add.png",onpress:function(Obj){
		location = "carower.do?action=unioncarowner&authid=${authid}";
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
