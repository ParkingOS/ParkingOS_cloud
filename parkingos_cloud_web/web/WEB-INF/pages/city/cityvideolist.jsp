<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>视频监控管理</title>
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
<div id="cityvideoobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var typeList = [{"value_no":0,"value_name":"路侧监控"},{"value_no":1,"value_name":"封闭停车场监控"}];
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"number", twidth:"60",issort:false,fhide:true,hide:true,edit:false},
		{fieldcnname:"监控类型",fieldname:"type",fieldvalue:'',inputtype:"select",noList:typeList,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"视频名称",fieldname:"video_name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false,hide:false,edit:true},
		{fieldcnname:"IP",fieldname:"ip",fieldvalue:'',inputtype:"text" ,height:"",issort:false},
		{fieldcnname:"端口",fieldname:"port",fieldvalue:'',inputtype:"text" ,height:"",issort:false,edit:true},
		{fieldcnname:"设备ID",fieldname:"deviceid",fieldvalue:'',inputtype:"text" ,height:"",issort:false},
		{fieldcnname:"通道号",fieldname:"channelid",fieldvalue:'',inputtype:"text" ,height:"",issort:false,edit:true},
        {fieldcnname:"用户名",fieldname:"cusername",fieldvalue:'',inputtype:"text" ,height:"",issort:false,edit:true},
        {fieldcnname:"密码",fieldname:"cpassword",fieldvalue:'',inputtype:"text",height:"",issort:false,fhide:true},
        {fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text",height:"",issort:false,fhide:false,edit:false,fhide:true,shide:true},
        {fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text" ,height:"",issort:false,edit:false,fhide:true,shide:true},
        {fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",height:"",issort:false,shide:true},
        {fieldcnname:"制造商",fieldname:"manufacture",fieldvalue:'',inputtype:"text" ,height:"",issort:false,edit:true}      
	]; 
	  
var _cityvideoT = new TQTable({
	tabletitle:"视频监控管理",
	ischeck:false,
	tablename:"citytransmitter_tables",
	dataUrl:"cityvideo.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery",
	tableObj:T("#cityvideoobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityvideoT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"video_search_w",Title:"搜索视频",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "video_search_f",
					formObj:tObj,
					formWinId:"video_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("sensor_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_cityvideoT.C({
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
	bts.push({dname:"添加视频监控",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityvideoT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"videomanage_add",Title:"添加视频监控",Width:550,sysfun:function(tObj){
				Tform({
					formname: "videolist_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"cityvideo.do?action=create",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("videomanage_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("videomanage_add");
							_cityvideoT.M();
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
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[1])
	bts.push({name:"编辑",fun:function(id){
		T.each(_cityvideoT.tc.tableitems,function(o,j){
			o.fieldvalue = _cityvideoT.GD(id)[j]
		});
		Twin({Id:"citytransmitter_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "citytransmitter_edit_f",
					formObj:tObj,
					recordid:"citytransmitter_id",
					suburl:"cityvideo.do?action=edit&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_cityvideoT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("citytransmitter_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("citytransmitter_edit_"+id);
							_cityvideoT.M()
						
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
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("cityvideo.do?action=delete","post","id="+id_this,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_cityvideoT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}



_cityvideoT.C();
</script>

</body>
</html>
