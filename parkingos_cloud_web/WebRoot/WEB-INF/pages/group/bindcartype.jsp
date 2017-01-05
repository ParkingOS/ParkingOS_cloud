<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>绑定车型</title>
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
<div id="bindcartypesetobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var issupperadmin=${supperadmin};
var isadmin = ${isadmin};
var authlist ="";
if((issupperadmin&&issupperadmin==1) || (isadmin&&isadmin==1))
	authlist="0,1,2,3,4";
else
	authlist= T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/

var role=${role};
var comid=${comid};
var groupid=${groupid};
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
//function getCarType(){
//	var cartypes = eval(T.A.sendData("price.do?action=getcartypes&comid="+comid));
//	return cartypes;
//}
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number",twidth:"100" ,height:"",edit:false,issort:false},
		{fieldcnname:"车牌",fieldname:"car_number",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,target:"typeid",action:"getCarType",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"车型",fieldname:"typeid",inputtype:"cselect", noList:[],twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'typeid');
			}},
		{fieldcnname:"修改时间",fieldname:"update_time",inputtype:"date",twidth:"150" ,height:"",edit:false,issort:false}
];
var _bindcartypesetT = new TQTable({
	tabletitle:"绑定车型",
	ischeck:false,
	tablename:"bindcartypeset_tables",
	dataUrl:"groupbindcartype.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery&groupid="+groupid,
	tableObj:T("#bindcartypesetobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
 	var bts=[];
 	if(subauth[1])
 		bts.push({dname:"绑定车型",icon:"edit_add.png",onpress:function(Obj){
			T.each(_bindcartypesetT.tc.tableitems,function(o,j){
				o.fieldvalue = "";
			});
			Twin({Id:"bindcartype_add",Title:"绑定车型",Width:550,sysfun:function(tObj){
				Tform({
					formname: "parking_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"groupbindcartype.do?action=create",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("bindcartype_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("bindcartype_add");
							_bindcartypesetT.M();
						}else {
							T.loadTip(1,ret,2,o);
						}

					}
				});
			}
		})
	}});
	if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
			T.each(_bindcartypesetT.tc.tableitems,function(o,j){
				o.fieldvalue ="";
			});
			Twin({Id:"bindcartype_search_w",Title:"搜索收费员",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "bindcartype_search_f",
					formObj:tObj,
					formWinId:"bindcartype_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("bindcartype_search_w");} }
					],
					SubAction:
							function(callback,formName){
								_bindcartypesetT.C({
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

//查看,添加,编辑,删除,设置区分大小车
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
		bts.push({name:"编辑",fun:function(id){
		T.each(_bindcartypesetT.tc.tableitems,function(o,j){
			o.fieldvalue = _bindcartypesetT.GD(id)[j]
		});
		Twin({Id:"bindcartype_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "bindcartype_edit_f",
					formObj:tObj,
					recordid:"bindcartype_id",
					suburl:"groupbindcartype.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_bindcartypesetT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("bindcartype_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("bindcartype_edit_"+id);
							_bindcartypesetT.M()
						}else{
							T.loadTip(1,ret,2,o)
						}
					}
				});	
			}
		})
	}});
	if(subauth[3])
		bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("groupbindcartype.do?action=delete&comid="+comid,"post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_bindcartypesetT.M()
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
		T.A.C({
			url:"cartype.do?action=getname&passid="+value,
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
			waitTip:"正在获取车型...",
			noCover:true
		})
	}else{
		return "无"
	};
	return "<font style='color:#666'>获取中...</font>";
}
function updateRow(rowid,name,value){
	//alert(value);
	if(value)
		_bindcartypesetT.UCD(rowid,name,value);
}

_bindcartypesetT.C();
</script>

</body>
</html>
