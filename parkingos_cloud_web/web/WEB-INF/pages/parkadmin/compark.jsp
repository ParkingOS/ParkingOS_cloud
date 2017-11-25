<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>车位管理</title>
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
<div id="comparkobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
var role=${role};
/*权限*/
var issupperadmin=${supperadmin};
var isadmin = ${isadmin};
var authlist ="";
if((issupperadmin&&issupperadmin==1) || (isadmin&&isadmin==1))
	authlist="0,1,2";
else
	authlist= T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/
var comid = ${comid};
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"车位编号",fieldname:"cid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"空闲"},{"value_no":1,"value_name":"占用"}], twidth:"100" ,height:"",issort:false},
		{fieldcnname:"二维 码号",fieldname:"qid",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"地磁编号 ",fieldname:"did",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"入场时间",fieldname:"enter_time",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"出场时间",fieldname:"end_time",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false},
		{fieldcnname:"订单编号 ",fieldname:"order_id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false}
		
	];
var _comparkT = new TQTable({
	tabletitle:"车位管理",
	ischeck:false,
	tablename:"compark_tables",
	dataUrl:"compark.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&comid="+comid,
	tableObj:T("#comparkobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
//查看,添加,编辑,删除
	var bts =[];
	if(subauth[1])
		bts.push({dname:"添加车位",icon:"edit_add.png",onpress:function(Obj){
		T.each(_comparkT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"compark_add",Title:"添加车位",Width:550,sysfun:function(tObj){
				Tform({
					formname: "compark_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"compark.do?action=create&comid="+comid,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:[
							{fieldcnname:"车位编号",fieldname:"cid",fieldvalue:'',inputtype:"multi", twidth:"300" ,height:"100",issort:false},
							{fieldcnname:"提示：",fieldname:"",fieldvalue:'支持三种格式：1、编号区间，如：A0001-A0200，2、多个编号 ，以英文逗号隔开，如：A0099,B0099,C3399，3、单个编号，如A0033',inputtype:"textd", twidth:"400" ,height:"",issort:false}
						]}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("compark_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(parseInt(ret)>0){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("compark_add");
							_comparkT.M();
						}else{
							T.loadTip(1,"添加失败！",2,o);
						}
					}
				});	
			}
		})
	
	}});
	if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_comparkT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"compark_search_w",Title:"搜索车位",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "compark_search_f",
					formObj:tObj,
					formWinId:"compark_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("compark_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_comparkT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&comid="+comid+"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	return bts;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_comparkT.tc.tableitems,function(o,j){
			o.fieldvalue = _comparkT.GD(id)[j]
		});
		Twin({Id:"compark_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "compark_edit_f",
					formObj:tObj,
					recordid:"compark_id",
					suburl:"compark.do?comid="+comid+"&action=edit&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_comparkT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("compark_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("compark_edit_"+id);
							_comparkT.M();
						}else{
							T.loadTip(1,"编辑失败！",2,o)
						}
					}
				});	
			}
		})
	}});
	if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		var id_this = id ;
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("compark.do?action=delete","post","ids="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_comparkT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	
	if(bts.length <= 0){return false;}
	return bts;
}
_comparkT.C();
</script>

</body>
</html>
