<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>LED屏管理</title>
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
<div id="cityledobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var moveMode = [{"value_no":0,"value_name":"自适应"},{"value_no":1,"value_name":"从右向左移动"},{"value_no":2,"value_name":"从左向右移动"},{"value_no":3,"value_name":"从下向上移动"},{"value_no":4,"value_name":"从上向下移动"},{"value_no":5,"value_name":"从右向左展开"},{"value_no":6,"value_name":"从左向右展开"},{"value_no":7,"value_name":"从下向上展开"},{"value_no":8,"value_name":"从上向下展开"},{"value_no":9,"value_name":"立即显示"},{"value_no":10,"value_name":"从中间向两边展开"},{"value_no":11,"value_name":"从两边向中间展开"},{"value_no":12,"value_name":"从中间向上下展开"},{"value_no":13,"value_name":"从上下向中间展开"},{"value_no":14,"value_name":"闪烁"},{"value_no":15,"value_name":"右百叶"}];
var moveSpeed = [{"value_no":0,"value_name":"0"},{"value_no":1,"value_name":"1"},{"value_no":2,"value_name":"2"},{"value_no":3,"value_name":"3"},{"value_no":4,"value_name":"4"},{"value_no":5,"value_name":"5"},{"value_no":6,"value_name":"6"},{"value_no":7,"value_name":"7"},{"value_no":8,"value_name":"8"}];
var dwellTime = [{"value_no":0,"value_name":"0s"},{"value_no":1,"value_name":"1s"},{"value_no":2,"value_name":"2s"},{"value_no":3,"value_name":"3s"},{"value_no":4,"value_name":"4s"},{"value_no":5,"value_name":"5s"}];
var cityledColor = [{"value_no":0,"value_name":"单基色"},{"value_no":1,"value_name":"双基色"}];
var showColor = [{"value_no":0,"value_name":"红"},{"value_no":1,"value_name":"绿"},{"value_no":2,"value_name":"黄"}];
var typeFace = [{"value_no":1,"value_name":"宋体"},{"value_no":2,"value_name":"楷体"},{"value_no":3,"value_name":"黑体"},{"value_no":4,"value_name":"隶书"},{"value_no":5,"value_name":"行书"}];
var typeSize = [{"value_no":0,"value_name":"12×12"},{"value_no":1,"value_name":"16×16"},{"value_no":2,"value_name":"24×24"},{"value_no":3,"value_name":"32×32"},{"value_no":4,"value_name":"48×48"},{"value_no":5,"value_name":"64×64"},{"value_no":6,"value_name":"80×80"},{"value_no":7,"value_name":"96×96"}];
var _field = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text", twidth:"60" ,height:"",issort:false,edit:false,fhide:true},
		{fieldcnname:"ip地址",fieldname:"cityledip",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"端口号",fieldname:"cityledport",fieldvalue:'',defaultValue:'8888',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"素材UID",fieldname:"cityleduid",fieldvalue:'',defaultValue:'41',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"移动方式",fieldname:"movemode",fieldvalue:'',defaultValue:'立即显示||9',inputtype:"select",noList:moveMode,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"移动速度",fieldname:"movespeed",fieldvalue:'',defaultValue:'1||1',inputtype:"select",noList:moveSpeed,twidth:"50" ,height:"",issort:false},
		{fieldcnname:"停留时间",fieldname:"dwelltime",fieldvalue:'',defaultValue:'1s||1',inputtype:"select",noList:dwellTime,twidth:"50" ,height:"",issort:false},
		{fieldcnname:"显示屏颜色",fieldname:"cityledcolor",fieldvalue:'',defaultValue:'双基色||1',inputtype:"select",noList:cityledColor,twidth:"50" ,height:"",issort:false},
		{fieldcnname:"颜色",fieldname:"showcolor",fieldvalue:'',defaultValue:'红||0',inputtype:"select",noList:showColor,twidth:"50" ,height:"",issort:false},
		{fieldcnname:"字体",fieldname:"typeface",fieldvalue:'',defaultValue:'宋体||1',inputtype:"select",noList:typeFace,twidth:"50" ,height:"",issort:false},
		{fieldcnname:"字号",fieldname:"typesize",fieldvalue:'',defaultValue:'16×16||1',inputtype:"select",noList:typeSize,twidth:"50" ,height:"",issort:false},
		{fieldcnname:"素材内容",fieldname:"matercont",fieldvalue:'',defaultValue:'停车宝',inputtype:"text",twidth:"200" ,height:"",issort:false},
		{fieldcnname:"宽",fieldname:"width",fieldvalue:'',defaultValue:'64',inputtype:"text",twidth:"50" ,height:"",issort:false},
		{fieldcnname:"高",fieldname:"height",fieldvalue:'',defaultValue:'32',inputtype:"text",twidth:"50" ,height:"",issort:false},
		{fieldcnname:"类型",fieldname:"type",fieldvalue:'',inputtype:"select",noList:[{"value_no":0,"value_name":"普通屏"},{"value_no":1,"value_name":"余位屏"}],twidth:"80" ,height:"",issort:false},
		{fieldcnname:"转发端口",fieldname:"rsport",fieldvalue:'',defaultValue:'rs232-2||2',inputtype:"select",noList:[{"value_no":1,"value_name":"rs232-1"},{"value_no":2,"value_name":"rs232-2"},{"value_no":3,"value_name":"rs485"}],twidth:"80" ,height:"",issort:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,target:"worksite_id",action:"getworksite",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"所属工作站",fieldname:"worksite_id",fieldvalue:'',inputtype:"cselect",noList:[],target:"passid",action:"getpass",twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'worksite_id');
			}
		},
		{fieldcnname:"所属通道",fieldname:"passid",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"160" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'passid');
			}
		}
		];
var rules =[{name:"passid",requir:true}];
var _cityledT = new TQTable({
	tabletitle:"LED屏管理",
	ischeck:false,
	tablename:"cityled_tables",
	dataUrl:"cityled.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#cityledobj"),
	fit:[true,true,true],
	tableitems:_field,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts = [];
	if(subauth[1])
	bts.push({dname:"添加led屏",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityledT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"cityled_add",Title:"添加led屏",Width:550,sysfun:function(tObj){
				Tform({
					formname: "cityled_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"cityled.do?action=create",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_field}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("cityled_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("cityled_add");
							_cityledT.M();
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
	if(bts.length <= 0){return false;}
	return bts;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_cityledT.tc.tableitems,function(o,j){
			o.fieldvalue = _cityledT.GD(id)[j]
		});
		Twin({Id:"cityled_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityled_edit_f",
					formObj:tObj,
					recordid:"cityled_id",
					suburl:"cityled.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_cityledT.tc.tableitems}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("cityled_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("cityled_edit_"+id);
							_cityledT.M()
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
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("cityled.do?action=delete","post","selids="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_cityledT.M()
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
	_cityledT.UCD(rowid,name,value);
}

_cityledT.C();
</script>

</body>
</html>
