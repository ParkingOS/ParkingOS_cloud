<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>金额设定</title>
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
<div id="moneysetobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"类型",fieldname:"mtype",fieldvalue:'',inputtype:"select",noList:[{"value_no":0,"value_name":"停车费"}],twidth:"100" ,height:"",issort:false},
		{fieldcnname:"支付给",fieldname:"giveto",fieldvalue:'',inputtype:"select",noList:[{"value_no":0,"value_name":"公司账户"},{"value_no":2,"value_name":"运营集团账户"}],twidth:"200" ,height:"",issort:false}
	];
var _moneysetT = new TQTable({
	tabletitle:"收费设定",
	ischeck:false,
	tablename:"moneyset_tables",
	dataUrl:"citymoneyset.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#moneysetobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bus = [];
	if(subauth[1])
	bus.push({dname:"添加设定",icon:"edit_add.png",onpress:function(Obj){
		Twin({Id:"mset_add",Title:"添加设定",Width:550,sysfun:function(tObj){
				Tform({
					formname: "mset_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"citymoneyset.do?action=create",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("mset_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("mset_add");
							_moneysetT.M();
						}else if(ret=='-2'){
							T.loadTip(1,"不能重复设定 ！",2,"");
						}else {
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		});
	}});
	if(subauth[0])
	bus.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		/* T.each(_msetT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); */
		Twin({Id:"mset_search_w",Title:"搜索",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "mset_search_f",
					formObj:tObj,
					formWinId:"mset_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("mset_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_moneysetT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						});
					}
				});	
			}
		});
	
	}});
	return bus;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_moneysetT.tc.tableitems,function(o,j){
			o.fieldvalue = _moneysetT.GD(id)[j];
		});
		Twin({Id:"mset_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "mset_edit_f",
					formObj:tObj,
					recordid:"mset_id",
					suburl:"citymoneyset.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_moneysetT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("mset_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("mset_edit_"+id);
							_moneysetT.M();
						}else if(ret=='-2'){
							T.loadTip(1,"不能重复设定！",2,"");
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		});
	}});
	if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("citymoneyset.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_moneysetT.M();
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}});
	}});
	if(bts.length <= 0){return false;}
	return bts;
}
_moneysetT.C();
</script>

</body>
</html>
