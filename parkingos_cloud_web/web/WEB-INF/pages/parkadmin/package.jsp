<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>套餐管理</title>
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
<div id="packageobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
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
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
var comid = ${comid};
var coms= eval(T.A.sendData("getdata.do?action=getsubcoms&id="+comid));
var carTypes= eval(T.A.sendData("getdata.do?action=getcartype&id="+comid));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",hide:true},
		{fieldcnname:"名称",fieldname:"p_name",fieldvalue:'',inputtype:"text", twidth:"200" ,height:""},
		/*{fieldcnname:"车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:coms, twidth:"200" ,height:""},
		{fieldcnname:"开始小时 ",fieldname:"b_time",fieldvalue:'',inputtype:"hour",defaultValue:'8||8', twidth:"80" ,height:""},
		{fieldcnname:"开始分钟",fieldname:"bmin",fieldvalue:'',inputtype:"pminute",defaultValue:'0||0', twidth:"80" ,height:""},
		{fieldcnname:"结束小时",fieldname:"e_time",fieldvalue:'',inputtype:"hour",defaultValue:'8||8', twidth:"80" ,height:"",issort:false},
		{fieldcnname:"结束分钟",fieldname:"emin",fieldvalue:'',inputtype:"pminute",defaultValue:'0||0', twidth:"80" ,height:""},
		{fieldcnname:"剩余数量",fieldname:"remain_number",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"价格",fieldname:"price",fieldvalue:'',inputtype:"doub", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"原价",fieldname:"old_price",fieldvalue:'',inputtype:"doub", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"有效期至",fieldname:"limitday",fieldvalue:'',inputtype:"sdate", twidth:"150" ,height:"",issort:false},

		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"有效"},{"value_no":1,"value_name":"无效"}] ,twidth:"100" ,height:"",issort:false},
		//,{"value_no":4,"value_name":"指定小时免费"},{"value_no":3,"value_name":"包月优惠"}
		{fieldcnname:"类型",fieldname:"type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"全天包月"},{"value_no":1,"value_name":"夜间包月"},{"value_no":2,"value_name":"日间包月"}] ,twidth:"100" ,height:"",issort:false},
	{fieldcnname:"有效范围",fieldname:"scope",fieldvalue:'',inputtype:"select",noList:[{"value_no":0,"value_name":"本车场有效"},{"value_no":1,"value_name":"本车场和子车场有效"}] ,twidth:"100" ,height:"",issort:false},
//	{fieldcnname:"不包括日期",fieldname:"exclude_date",fieldvalue:'',inputtype:"text" ,twidth:"100" ,height:"",issort:false,edit:false}
	{fieldcnname:"描述",fieldname:"describe",fieldvalue:'',inputtype:"multi", twidth:"200" ,height:"",issort:false}*/
	//{fieldcnname:"优惠百分比(1-100)",fieldname:"favourable_precent",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false},
		//{fieldcnname:"超出优惠百分比(1-100)",fieldname:"out_favourable_precent",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false},
		//{fieldcnname:"免费时长(分钟)",fieldname:"free_minutes",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false}
		{fieldcnname:"价格",fieldname:"price",fieldvalue:'',inputtype:"doub", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",edit:false},
		{fieldcnname:"修改时间",fieldname:"update_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",edit:false},
		{fieldcnname:"车型类型",fieldname:"car_type_id",fieldvalue:'',inputtype:"select",noList:carTypes, twidth:"150" ,height:"",issort:false},
		{fieldcnname:"月卡描述 ",fieldname:"describe",fieldvalue:'',inputtype:"multi", twidth:"240" ,height:"",edit:true},
		{fieldcnname:"续费周期 ",fieldname:"period",fieldvalue:'',inputtype:"select",noList:[{"value_no":"月","value_name":"月"},{"value_no":"季","value_name":"季"},{"value_no":"半年","value_name":"半年"},{"value_no":"年","value_name":"年"}], twidth:"140" ,height:"",edit:true}
	];
var rules =[
		{name:"remain_number",type:"number",url:"",requir:true,warn:"请输入数字!",okmsg:""},
		{name:"b_time",type:"number",url:"",requir:true,warn:"请输入数字!",okmsg:""},
		{name:"e_time",type:"number",url:"",requir:true,warn:"请输入数字!",okmsg:""},
		{name:"price",type:"doub",url:"",requir:true,warn:"请输入价格!",okmsg:""},
		{name:"limitday",type:"date",url:"",requir:true,warn:"请输入有效期!",okmsg:""}];
var _packageT = new TQTable({
	tabletitle:"套餐管理&nbsp;&nbsp;<font color='#a9a9a9'>（续费周期只作显示，云端续费仍按月延期）</font>",
	ischeck:false,
	tablename:"package_tables",
	dataUrl:"package.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery&comid="+comid,
	tableObj:T("#packageobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[1])
		bts.push({dname:"添加套餐",icon:"edit_add.png",onpress:function(Obj){
		T.each(_packageT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"package_add",Title:"添加套餐",Width:550,sysfun:function(tObj){
				Tform({
					formname: "package_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"package.do?action=create",
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
						//rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("package_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("package_add");
							_packageT.M();
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
			}
		})
	
	}});
	if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_packageT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"package_search_w",Title:"搜索套餐",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "package_search_f",
					formObj:tObj,
					formWinId:"package_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("package_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_packageT.C({
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
//"查看,添加,编辑"
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_packageT.tc.tableitems,function(o,j){
			o.fieldvalue = _packageT.GD(id)[j]
		});
		Twin({Id:"package_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "package_edit_f",
					formObj:tObj,
					recordid:"package_id",
					suburl:"package.do?action=edit&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_packageT.tc.tableitems}]
						//rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("package_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("package_edit_"+id);
							_packageT.M()
						}else{
							T.loadTip(1,ret,2,o)
						}
					}
				});	
			}
		})
	}});
	 bts.push({name:"删除",fun:function(id){
		var id_this = id ;
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("package.do?action=delete","post","id="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_packageT.M()
				}else {
					T.loadTip(1,ret,10,"");
				}
			}
		)}})
	}});
//	bts.push(
//			{name:"重复",
//				fun:function(id){
//					Twin({
//						Id:"edit_role"+id,
//						Title:"特别收费日设置  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
//						Content:"<iframe src=\"package.do?action=week&id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
//						Width:T.gww()-300,
//						Height:T.gwh()-200
//					})
//
//				}});
	if(bts.length <= 0){return false;}
	return bts;
}
_packageT.C();
</script>

</body>
</html>
