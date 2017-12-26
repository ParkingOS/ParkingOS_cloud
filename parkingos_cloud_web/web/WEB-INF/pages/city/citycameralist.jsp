<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>摄像头管理</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.public.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.datatable.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.form.js?075417" type="text/javascript">//表单</script>
<script src="js/tq.searchform.js?0817" type="text/javascript">//查询表单</script>
<script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
</head>

<body>
<div id="cameraobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
//查看,添加,编辑,删除,修改密码
/*权限*/
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _field = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"60" ,height:"",issort:false,edit:false,fhide:true},
		{fieldcnname:"名称",fieldname:"camera_name",defaultValue:'',fieldvalue:'',inputtype:"text", twidth:"160" ,height:"",issort:false},
		{fieldcnname:"IP地址",fieldname:"ip",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"端口",fieldname:"port",fieldvalue:'',defaultValue:'554',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"用户名",fieldname:"cusername",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"制造厂商",fieldname:"manufacturer",defaultValue:'停车宝',fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,target:"worksite_id",action:"getworksite",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"所属工作站",fieldname:"worksite_id",fieldvalue:'',inputtype:"cselect",noList:[],target:"passid",action:"getpass",twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'worksite_id');
			}
		},
		{fieldcnname:"所属通道",fieldname:"passid",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"160" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'passid');
			}}
	];
var rules =[{name:"camera_name",requir:true},{name:"passid",requir:true}];
var _cameraT = new TQTable({
	tabletitle:"摄像头管理",
	ischeck:false,
	tablename:"camera_tables",
	dataUrl:"citycamera.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery",
	tableObj:T("#cameraobj"),
	fit:[true,true,true],
	tableitems:_field,
	isoperate:getAuthIsoperateButtons()
});
//查看,添加,编辑,删除,修改密码
function getAuthButtons(){
	var bts=[];
	if(subauth[1])
		bts.push({dname:"添加摄像头",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cameraT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"camera_add",Title:"添加摄像头",Width:550,sysfun:function(tObj){
				Tform({
					formname: "camera_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"citycamera.do?action=create",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_field}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("camera_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("camera_add");
							_cameraT.M();
						}else if(ret==0){
							T.loadTip(1,"添加失败！请稍候再试！",2,"");
						}else{
							T.loadTip(1,"添加失败！",2,"");
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		})
	
	}});
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cameraT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"camera_search_w",Title:"搜索摄像头",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "camera_search_f",
					formObj:tObj,
					formWinId:"camera_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_field}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("camera_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_cameraT.C({
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
//查看,添加,编辑,删除,修改密码
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_cameraT.tc.tableitems,function(o,j){
			o.fieldvalue = _cameraT.GD(id)[j]
		});
		Twin({Id:"camera_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "camera_edit_f",
					formObj:tObj,
					recordid:"camera_id",
					suburl:"citycamera.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_cameraT.tc.tableitems}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("camera_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("camera_edit_"+id);
							_cameraT.M()
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
		var id_this = id ;
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("citycamera.do?action=delete","post","selids="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_cameraT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(subauth[4])
	bts.push({name:"修改密码",fun:function(id){
		T.each(_cameraT.tc.tableitems,function(o,j){
			o.fieldvalue = _cameraT.GD(id)[j]
		});
		Twin({Id:"camera_pass_"+id,Title:"修改密码",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "camera_pass_f",
					formObj:tObj,
					recordid:"camera_pass_id",
					suburl:"citycamera.do?action=editpass&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:[
							{fieldcnname:"新密码",fieldname:"newpass",fieldvalue:'',inputtype:"password", twidth:"200" ,height:"",issort:false},
							{fieldcnname:"确认密码",fieldname:"confirmpass",fieldvalue:'',inputtype:"password", twidth:"200" ,height:"",issort:false}]}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消成功",icon:"cancel.gif", onpress:function(){TwinC("camera_pass_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"修改成功！",2,"");
							TwinC("camera_pass_"+id);
							_cameraT.M()
						}else{
							T.loadTip(1,"修改失败",2,o)
						}
					}
				});	
			}
		})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}
function setcname(value,pid,colname){
	var url = "";
	if(colname == "worksite_id"){
		url = "citypass.do?action=getworksitename&id="+value;
	}else if(colname == "passid"){
		url = "parkcamera.do?action=getname&passid="+value;
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
	_cameraT.UCD(rowid,name,value);
}

_cameraT.C();
</script>

</body>
</html>
