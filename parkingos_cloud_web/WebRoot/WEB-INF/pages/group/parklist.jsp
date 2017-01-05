<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>停车场管理</title>
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
<div id="parkingobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var etc_add_states=[{"value_no":0,"value_name":"不支持"},{"value_no":1,"value_name":"Ibeacon"},{"value_no":2,"value_name":"通道照牌"},{"value_no":3,"value_name":"手机照牌"},{"value_no":4,"value_name":"Pos机照牌"}];
var stateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"已审核"},{"value_no":2,"value_name":"未审核"}];
var areas = eval(T.A.sendData("getdata.do?action=getareas&id=${groupid}"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"40" ,height:"",issort:false,edit:false},
		{fieldcnname:"名称",fieldname:"company_name",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"车位总数",fieldname:"parking_total",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false},
		{fieldcnname:"车场类型",fieldname:"etc",fieldvalue:'',defaultValue:'通道照牌||2',inputtype:"select",noList:etc_add_states, twidth:"60" ,height:"",issort:false},
		{fieldcnname:"所属区域",fieldname:"areaid",fieldvalue:'',inputtype:"select",noList:areas,twidth:"100" ,height:"",issort:false,
			process:function(value,pid){
					return setname(value,areas);
				}},
		{fieldcnname:"详细地址",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false},
		{fieldcnname:"停车场电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",hide:true},
		{fieldcnname:"更新时间",fieldname:"update_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",hide:true},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:stateList, twidth:"60" ,height:"",issort:false}
		];
var rules =[
		{name:"company_name",type:"",url:"",requir:true,warn:"不能为空!",okmsg:""}
		];
var _parkingT = new TQTable({
	tabletitle:"停车场列表",
	ischeck:false,
	tablename:"parking_tables",
	dataUrl:"grouppark.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery",
	tableObj:T("#parkingobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function getAuthButtons(){
	var bus = [];
	if(subauth[1])
	bus.push({dname:"注册停车场",icon:"edit_add.png",onpress:function(Obj){
		T.each(_parkingT.tc.tableitems,function(o,j){
				o.fieldvalue ="";
			});
		Twin({Id:"parking_add",Title:"添加停车场",Width:550,sysfun:function(tObj){
				Tform({
					formname: "parking_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"grouppark.do?action=create",
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("parking_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("parking_add");
							_parkingT.M();
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		})
	
	}});
	if(subauth[0])
	bus.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_parkingT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
			if(o.fieldname=='strid'||o.fieldname=='nickname'||o.fieldname=='cmobile')
				o.shide=true;
		});
		Twin({Id:"parking_search_w",Title:"搜索停车场",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "parking_search_f",
					formObj:tObj,
					formWinId:"parking_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("parking_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_parkingT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	return bus;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_parkingT.tc.tableitems,function(o,j){
			o.fieldvalue = _parkingT.GD(id)[j]
		});
		Twin({Id:"parking_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "parking_edit_f",
					formObj:tObj,
					recordid:"parking_id",
					suburl:"grouppark.do?action=edit&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_parkingT.tc.tableitems}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("parking_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("parking_edit_"+id);
							_parkingT.M()
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		})
	}});
	if(subauth[3])
	bts.push({name:"设置",fun:function(id){
		Twin({
			Id:"client_detail_"+id,
			Title:"停车场设置  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
			Content:"<iframe src=\"grouppark.do?action=set&id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
			Width:T.gww()-100,
			Height:T.gwh()-50
		})
		}});
	if(bts.length <= 0){return false;}
	return bts;
}

function setname(value, list){
	if(value != "-1"){
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

_parkingT.C();
</script>

</body>
</html>
