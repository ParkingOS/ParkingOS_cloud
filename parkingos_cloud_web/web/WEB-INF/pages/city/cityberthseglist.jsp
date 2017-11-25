<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>泊位段管理</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">
	<script src="js/jquery.js" type="text/javascript">//表格</script>
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
<div id="cityberthsegobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number",twidth:"80" ,height:"",edit:false,issort:false},
		{fieldcnname:"唯一标识",fieldname:"uuid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,fhide:true,hide:true,shide:true},
		{fieldcnname:"泊位段名称",fieldname:"berthsec_name",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属泊位段uuid",fieldname:"park_uuid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,fhide:true,hide:true,shide:true},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"150" ,height:"",issort:false},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"200" ,height:"",issort:false},
		{fieldcnname:"泊位总数",fieldname:"berthnum",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,shide:true},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true,shide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true,shide:true},
		{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true}
	];
var _edit=[
		{fieldcnname:"唯一标识",fieldname:"uuid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,fhide:true,hide:true},
		{fieldcnname:"泊位段名称",fieldname:"berthsec_name",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属泊位段uuid",fieldname:"park_uuid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,fhide:true,hide:true},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"200" ,height:"",issort:false},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true}
	];
var rules =[
		{name:"berthsec_name",type:"",url:"",requir:true,warn:"不能为空!",okmsg:""},
		{name:"comid",requir:true}
		];
var _cityberthsegT = new TQTable({
	tabletitle:"泊位段管理",
	ischeck:false,
	tablename:"cityberthseg_tables",
	dataUrl:"cityberthseg.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#cityberthsegobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityberthsegT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"sensor_search_w",Title:"搜索泊位段",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "sensor_search_f",
					formObj:tObj,
					formWinId:"sensor_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("sensor_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_cityberthsegT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=query&"+Serializ(formName)
						})
					}
				});	
			}
		})
	
	}});
	if(subauth[1])
	bts.push({dname:"添加泊位段",icon:"edit_add.png",onpress:function(Obj){
				T.each(_cityberthsegT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
				Twin({Id:"cityberthseg_add",Title:"添加泊位段",Width:550,sysfun:function(tObj){
					Tform({
						formname: "parking_edit_f",
						formObj:tObj,
						recordid:"id",
						suburl:"cityberthseg.do?action=create",
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_mediaField}],
							rules:rules
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("cityberthseg_add");} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"添加成功！",2,"");
								TwinC("cityberthseg_add");
								_cityberthsegT.M();
							}else if(ret=="-1"){
								T.loadTip(1,"请选择车场 ！",2,"");
							}else {
								T.loadTip(1,ret,2,o);
							}
						}
					});	
				}
			});
		}});
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		/*T.each(_cityberthsegT.tc.tableitems,function(o,j){
			o.fieldvalue = _cityberthsegT.GD(id)[j];
		});*/
		var uuid = _cityberthsegT.GD(id,"uuid");
		var berthsec_name = _cityberthsegT.GD(id,"berthsec_name");
		var park_uuid = _cityberthsegT.GD(id,"park_uuid");
		var address = _cityberthsegT.GD(id,"address");
		var longitude = _cityberthsegT.GD(id,"longitude");
		var latitude = _cityberthsegT.GD(id,"latitude");
		Twin({Id:"cityberthseg_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityberthseg_edit_f",
					formObj:tObj,
					recordid:"cityberthseg_id",
					suburl:"cityberthseg.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_edit}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("cityberthseg_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("cityberthseg_edit_"+id);
							_cityberthsegT.M();
						}else if(ret=="-1"){
							T.loadTip(1,"请选择车场 ！",2,"");
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
				T("#cityberthseg_edit_f_uuid").value=uuid;
				T("#cityberthseg_edit_f_berthsec_name").value=berthsec_name;
				T("#cityberthseg_edit_f_park_uuid").value=park_uuid;
				T("#cityberthseg_edit_f_address").value=address;
				T("#cityberthseg_edit_f_longitude").value=longitude;
				T("#cityberthseg_edit_f_latitude").value=latitude;
			}
		})
	}});
	if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("cityberthseg.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_cityberthsegT.M();
				}if(ret=="-2"){
					T.loadTip(1,"请先解除绑定的泊位！",2,"");
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(subauth[4])
	bts.push({name:"绑定泊位",fun:function(id){
		var comid = _cityberthsegT.GD(id,"comid");
		Twin({
			Id:"induce_detail_"+id,
			Title:"绑定泊位 &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
			Content:"<iframe src=\"cityberthseg.do?action=tounbindberth&berthsegid="+id+"&comid="+comid+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
			Width:T.gww()-100,
			Height:T.gwh()-50
		})
	}});
	if(subauth[5])
		bts.push({name:"解绑泊位",fun:function(id){
			var comid = _cityberthsegT.GD(id,"comid");
			Twin({
				Id:"induce_detail_"+id,
				Title:"解绑泊位 &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
				Content:"<iframe src=\"cityberthseg.do?action=tobindberth&berthsegid="+id+"&comid="+comid+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
				Width:T.gww()-100,
				Height:T.gwh()-50
			})
		}});
		bts.push({name:"签退",fun:function(id){
			T.A.sendData("cityberthseg.do?","post","action=queryworker&id="+id,
					function deletebackfun(ret){
						var arr = ret.split("_");
						if(arr.length==2){
							var uid = arr[0];
							var nickname = arr[1];
						}
						Tconfirm({Title:"确认签退吗",Content:"确认签退吗？当前上班收费员:"+nickname,OKFn:function(){
							T.A.sendData("cityberthseg.do?action=workout","post","id="+id+"&uid="+uid,
									function deletebackfun(ret){
										if(ret=="1") {
											T.loadTip(1, "签退成功！", 2, "");
											_cityberthsegT.M();
										}else if(ret=="-1"){
											T.loadTip(1,"没有正在上班的收费员",2,"");
										}else{
											T.loadTip(1,"签退失败",2,"");
										}
									}
							)}})
					}
			)
		}});
	if(bts.length <= 0){return false;}
	return bts;
}


_cityberthsegT.C();
</script>

</body>
</html>
