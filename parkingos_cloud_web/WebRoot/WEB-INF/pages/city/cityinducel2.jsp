<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>诱导屏管理</title>
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
<div id="induceobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var stateList = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"正常"},{"value_no":1,"value_name":"故障"}];
var _mediaField=[
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false,hide:true,fhide:true,shide:true},
		{fieldcnname:"诱导屏名称",fieldname:"name",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"硬件编号",fieldname:"did",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"诱导屏状态",fieldname:"induce_state",fieldvalue:'',inputtype:"select",noList:stateList, twidth:"80" ,height:"",issort:false,edit:false,hide:true,shide:true,
			process:function(value,trId,colId){
				if(value==1)
					return "<font color='red'>故障</font>";
				else 
					return "正常";
			}},
		{fieldcnname:"包含车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false,fhide:true,hide:true},
		{fieldcnname:"发布记录",fieldname:"hcount",fieldvalue:'',inputtype:"number", twidth:"80" ,height:"",edit:false,shide:true,hide:true,
			process:function(value,cid,id){
					return "<a href=# onclick=\"viewdetail('h','"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
				}},
		{fieldcnname:"信息",fieldname:"ad",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false,shide:true,hide:true},
		{fieldcnname:"信息生效时间",fieldname:"begin_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true,shide:true},
		{fieldcnname:"信息结束时间",fieldname:"end_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true,shide:true},
		{fieldcnname:"信息发布时间",fieldname:"publish_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true,shide:true},
		{fieldcnname:"发布状态",fieldname:"isactive",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"未发布"},{"value_no":1,"value_name":"已发布"}] , twidth:"100" ,height:"",issort:false,hide:true,
			process:function(value,trId,colId){
				if(value==1)
					return "<font color='red'>已发布</font>";
				else 
					return "未发布";
			}},
		{fieldcnname:"最近心跳时间",fieldname:"heartbeat_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"200" ,height:"",issort:false,shide:true},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,shide:true,fhide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,shide:true,fhide:true},
		{fieldcnname:"诱导屏创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"诱导屏修改时间",fieldname:"update_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true},
		{fieldcnname:"诱导屏创建人",fieldname:"creator_name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,hide:true,shide:true},
		{fieldcnname:"诱导屏修改人",fieldname:"update_name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,hide:true,shide:true}
	];
	
var _ad = [
		{fieldcnname:"信息",fieldname:"ad",fieldvalue:'',inputtype:"multi",twidth:"150" ,height:"100",issort:false},
		{fieldcnname:"信息生效时间",fieldname:"begin_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"信息结束时间",fieldname:"end_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false}
	];
	
var _induceT = new TQTable({
	tabletitle:"诱导屏管理",
//	ischeck:false,
	tablename:"induce_tables",
	dataUrl:"inducel2.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&type=${type}",
	tableObj:T("#induceobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[1])
	bts.push({dname:"添加诱导屏",icon:"edit_add.png",onpress:function(Obj){
				T.each(_induceT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
				Twin({Id:"induce_add",Title:"添加诱导屏",Width:550,sysfun:function(tObj){
					Tform({
						formname: "induce_edit_f",
						formObj:tObj,
						recordid:"id",
						suburl:"inducel2.do?action=create&type=${type}",
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_mediaField}]
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("induce_add");} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"添加成功！",2,"");
								TwinC("induce_add");
								_induceT.M();
							}if(ret=="-2"){
								T.loadTip(1,"硬件编号重复了！",2,"");
							}else {
								T.loadTip(1,ret,7,o);
							}
						}
					});	
				}
			});
		}});
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_induceT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"induce_search_w",Title:"搜索停车场",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "induce_search_f",
					formObj:tObj,
					formWinId:"induce_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("induce_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_induceT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&type=${type}&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	if(subauth[7])
	bts.push({ dname: "批量发布", icon: "sendsms.gif", onpress:function(Obj){
		var sids = _induceT.GS();
		var ids="";
		if(!sids){
			T.loadTip(1,"请先选择诱导屏",2,"");
			return;
		}
		Twin({Id:"send_message_w",Title:"批量发布",Width:550,sysfun:function(tObj){
			Tform({
				formname: "send_message_ff",
				formObj:tObj,
				recordid:"id",
				suburl:"inducel2.do?action=bathpublish",
				method:"POST",
				Coltype:2,
				dbuttonname:["批量发布"],
				formAttr:[{
					formitems:[{kindname:"",kinditemts:[
					{fieldcnname:"诱导屏编号",fieldname:"ids",fieldvalue:sids,inputtype:"multi",height:"80",edit:false}]}]
				}],
				buttons : [//工具
					{name: "cancel", dname: "取消", tit:"取消发布",icon:"cancel.gif", onpress:function(){TwinC("send_message_w");} }
				],
				Callback:function(f,rcd,ret,o){
					if(ret=="1"){
						T.loadTip(1,"发布成功！",2,"");
						TwinC("send_message_w");
						_induceT.M();
					}else if(ret == "-2"){
						T.loadTip(1,"请至少选择一个未发布的诱导屏!",2,o);
					}else{
						T.loadTip(1,"发布失败",2,o);
					}
				}
			});	
			}
		})
		
	}})
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		T.each(_induceT.tc.tableitems,function(o,j){
			o.fieldvalue = _induceT.GD(id)[j];
		});
		Twin({Id:"induce_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "induce_edit_f",
					formObj:tObj,
					recordid:"induce_id",
					suburl:"inducel2.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_induceT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("induce_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("induce_edit_"+id);
							_induceT.M();
						}if(ret=="-2"){
							T.loadTip(1,"硬件编号重复了！",2,"");
						}else{
							T.loadTip(1,ret,7,o);
						}
					}
				});	
			}
		})
	}});
	if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("inducel2.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_induceT.M();
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(false)
	bts.push({name:"绑定车场",fun:function(id){
		Twin({
			Id:"induce_detail_"+id,
			Title:"绑定车场  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
			Content:"<iframe src=\"inducel2.do?action=parkdetail&induce_id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
			Width:T.gww()-100,
			Height:T.gwh()-50
		})
	}});
	if(subauth[5])
	bts.push({name:"编辑信息",fun:function(id){
		var induce_id = _induceT.GD(id,"id");
		var ad = _induceT.GD(id,"ad");
		var begin_time = _induceT.GD(id,"begin_time");
		var end_time = _induceT.GD(id,"end_time");
		Twin({Id:"induce_ad_"+id,Title:"编辑信息",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "induce_ad_f",
					formObj:tObj,
					recordid:"induce_id",
					suburl:"inducel2.do?action=editad&induce_id="+induce_id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_ad}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("induce_ad_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("induce_ad_"+id);
							_induceT.M();
						}else{
							T.loadTip(1,ret,7,o)
						}
					}
				});	
				T("#induce_ad_f_ad").value=ad;
				T("#induce_ad_f_begin_time").value=begin_time;
				T("#induce_ad_f_end_time").value=end_time;
			}
		})
	}});
	if(subauth[6])
	bts.push({name:"显示区域",fun:function(id){
		Twin({
			Id:"induce_detail_"+id,
			Title:"显示区域  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
			Content:"<iframe src=\"inducemodule.do?induce_id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
			Width:T.gww()-100,
			Height:T.gwh()-50
		})
	}});
	if(subauth[7])
	bts.push({name:"发布",
		rule:function(id){
			var isactive =_induceT.GD(id,"isactive");
			if(isactive==0){//状态：可用
				this.name="发布";
				return true;
			}else{
				return false;
			}
		 },
		fun:function(id){
			var id_this = id ;
			var isactive =_induceT.GD(id,"isactive");
			var conmessage="已发布";
			if(isactive==0){//状态：可用
				conmessage="发布";
				
				Tconfirm({Title:"确认"+conmessage+"吗",Content:"确认"+conmessage+"吗",OKFn:function(){T.A.sendData("inducel2.do?action=publish","post","induce_id="+id_this,
					function deletebackfun(ret){
						if(ret=="1"){
							T.loadTip(1,conmessage+"成功！",2,"");
							_induceT.M();
						}else{
							T.loadTip(1,ret,2,"");
						}
					}
				)}});
			}
	}});
	if(bts.length <= 0){return false;}
	return bts;
}

function viewdetail(type,value,id){
	var induce_id =_induceT.GD(id,"id");
	var name = _induceT.GD(id,"name");
	var tip = "诱导屏："+name;
	var url = "";
	if(type == "h"){
		url = "inducel2.do?action=hisdetail&induce_id="+induce_id;
	}
	Twin({
		Id:"induce_detail_"+id,
		Title:tip,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='induce_detail_'"+id+" id='induce_detail_'"+id+" src='"+url+"' width='100%' height='100%' frameborder='0' scrolling='no' style='overflow:hidden;' ></iframe>"
	})
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

_induceT.C();
</script>

</body>
</html>
