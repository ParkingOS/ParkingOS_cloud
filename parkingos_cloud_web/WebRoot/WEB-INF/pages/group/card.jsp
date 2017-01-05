<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>卡片管理</title>
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
<div id="cardobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var cardstate = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"已激活"},
                 {"value_no":1,"value_name":"已注销"},{"value_no":2,"value_name":"已绑定用户"},
                 {"value_no":4,"value_name":"已绑定车牌"},{"value_no":3,"value_name":"已开卡"}];
var _mediaField = [
		{fieldcnname:"卡编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false,shide:true,hide:true,fhide:true},
		{fieldcnname:"卡片名称",fieldname:"card_name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"卡面号",fieldname:"card_number",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"卡号",fieldname:"nfc_uuid",fieldvalue:'',inputtype:"text",twidth:"180" ,height:"",issort:false},
		{fieldcnname:"车主手机号",fieldname:"mobile",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车牌号",fieldname:"carnumber",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"余额",fieldname:"balance",fieldvalue:'',inputtype:"number",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"卡片状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:cardstate, twidth:"100" ,height:"",issort:false},
		{fieldcnname:"开卡设备",fieldname:"device",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"开卡人",fieldname:"reg_id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				return setcname(value,pid,'reg_id');
			}},
		{fieldcnname:"开卡时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"激活人",fieldname:"activate_id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				return setcname(value,pid,'activate_id');
			}},
		{fieldcnname:"激活时间",fieldname:"activate_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"绑定人",fieldname:"uid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				return setcname(value,pid,'uid');
			}},
		{fieldcnname:"绑定时间",fieldname:"update_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"注销人",fieldname:"cancel_id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,shide:true,shide:true,
			process:function(value,pid){
				return setcname(value,pid,'cancel_id');
			}},
		{fieldcnname:"注销时间",fieldname:"cancel_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true}
	];
	
var _edit=[
		{fieldcnname:"卡片名称",fieldname:"card_name",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false}
	];
	
var _bind=[
   		{fieldcnname:"手机号",fieldname:"mobile",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
   		{fieldcnname:"车牌号",fieldname:"carnumber",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false}
   	];
var _charge=[
   		{fieldcnname:"充值金额",fieldname:"money",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false}
    ];
var rules =[
    	{name:"carnumber",type:"",url:"",requir:true,warn:"不能为空!",okmsg:""}
    ];
var _cardT = new TQTable({
	tabletitle:"卡片管理",
	ischeck:false,
	tablename:"card_tables",
	dataUrl:"card.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#cardobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cardT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"card_search_w",Title:"搜索",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "card_search_f",
					formObj:tObj,
					formWinId:"card_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("card_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_cardT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[1])
	bts.push({name:"编辑",fun:function(id){
		var card_name = _cardT.GD(id,"card_name");
		Twin({Id:"card_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "card_edit_f",
					formObj:tObj,
					recordid:"cardsec_id",
					suburl:"card.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_edit}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("card_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("card_edit_"+id);
							_cardT.M();
						}else{
							T.loadTip(1,"编辑失败!",2,o);
						}
					}
				});	
				T("#card_edit_f_card_name").value=card_name;
			}
		})
	}});
	if(subauth[2])
		bts.push({name:"绑定",fun:function(id){
			var carnumber = _cardT.GD(id,"carnumber");
			var mobile = _cardT.GD(id,"mobile");
			Twin({Id:"card_bind_"+id,Title:"绑定",Width:550,sysfunI:id,sysfun:function(id,tObj){
					Tform({
						formname: "card_bind_f",
						formObj:tObj,
						recordid:"cardsec_id",
						suburl:"card.do?action=bind&id="+id,
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_bind}],
							rules:rules
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消绑定",icon:"cancel.gif", onpress:function(){TwinC("card_bind_"+id);} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"绑定成功！",2,"");
								TwinC("card_bind_"+id);
								_cardT.M();
							}else{
								T.loadTip(1,ret,2,o);
							}
						}
					});	
				}
			})
			T("#card_bind_f_carnumber").value=carnumber;
			T("#card_bind_f_mobile").value=mobile;
		}});
	//if(subauth[6])
		bts.push({name:"解绑",fun:function(id){
		Tconfirm({Title:"解绑",Content:"确认解绑吗",OKFn:function(){
		T.A.sendData("card.do?action=unbind","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"操作成功！",2,"");
					_cardT.M();
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(subauth[3])
		bts.push({name:"充值",fun:function(id){
			Twin({Id:"card_charge_"+id,Title:"充值",Width:550,sysfunI:id,sysfun:function(id,tObj){
					Tform({
						formname: "card_charge_f",
						formObj:tObj,
						recordid:"cardsec_id",
						suburl:"card.do?action=charge&id="+id,
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_charge}]
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消充值",icon:"cancel.gif", onpress:function(){TwinC("card_charge_"+id);} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"充值成功！",2,"");
								TwinC("card_charge_"+id);
								_cardT.M();
							}else{
								T.loadTip(1,ret,2,o);
							}
						}
					});	
				}
			})
		}});
	
	if(subauth[4])
	bts.push({name:"注销",fun:function(id){
		Tconfirm({Title:"注销",Content:"确认注销吗",OKFn:function(){
		T.A.sendData("card.do?action=return","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"注销成功！",2,"");
					_cardT.M();
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(subauth[5])
		bts.push({name:"删除",fun:function(id){
			Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
			T.A.sendData("card.do?action=delete","post","id="+id,
				function deletebackfun(ret){
					if(ret=="1"){
						T.loadTip(1,"删除成功！",2,"");
						_cardT.M();
					}if(ret=="-2"){
						T.loadTip(1,"请先注销卡片!",2,"");
					}else{
						T.loadTip(1,ret,2,"");
					}
				}
			)}})
		}});
	if(bts.length <= 0){return false;}
	return bts;
}

function setcname(value,pid,colname){
	if(value&&value!='-1'&&value!=''){
		var url = "";
		if(colname == "reg_id" 
				|| colname == "uid" 
				|| colname == "cancel_id"
				|| colname == "activate_id"){
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
	_cardT.UCD(rowid,name,value);
}

_cardT.C();
</script>

</body>
</html>
