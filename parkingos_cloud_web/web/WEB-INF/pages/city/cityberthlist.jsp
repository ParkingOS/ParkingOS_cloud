<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>泊位管理</title>
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
<div id="cityberthobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false,shide:true,hide:true,fhide:true},
		{fieldcnname:"泊位编号",fieldname:"cid",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",twidth:"150",noList:parks,target:"berthsec_id",action:"getberthseg",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"150" ,height:"",issort:false,
			process:function(value,pid){
				return setcname(value,pid,'berthsec_id');
			}},
		/* {fieldcnname:"车检器",fieldname:"dici_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"100" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				return setcname(value,pid,'dici_id');
			}}, */
		{fieldcnname:"车检器编号",fieldname:"did",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",hide:true,shide:true},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"空闲"},{"value_no":"1","value_name":"占用"}], twidth:"80" ,height:"",hide:true},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true,shide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true,shide:true},
		/* {fieldcnname:"唯一编号",fieldname:"uuid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false}, */
		{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true}
	];
	
var _edit=[
		{fieldcnname:"泊位编号",fieldname:"cid",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true},
		/* {fieldcnname:"唯一编号",fieldname:"uuid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false} */
	];
	
var _bath=[
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,target:"berthsec_id",action:"getberthseg",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"150" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				return setcname(value,pid,'berthsec_id');
			}},
		{fieldcnname:"泊位编号",fieldname:"cid",fieldvalue:'',inputtype:"multi", twidth:"300" ,height:"100",issort:false},
		{fieldcnname:"提示：",fieldname:"",fieldvalue:'支持三种格式：1、编号区间，如：A0001-A0200，2、多个编号 ，以英文逗号隔开，如：A0099,B0099,C3399，3、单个编号，如A0033',inputtype:"textd", twidth:"400" ,height:"",issort:false}
	];
var rules =[
		{name:"comid",type:"",url:"",requir:true,warn:"不能为空!",okmsg:""},
		{name:"cid",type:"",url:"",requir:true,warn:"不能为空!",okmsg:""},
		];
var _cityberthT = new TQTable({
	tabletitle:"泊位管理",
	ischeck:false,
	tablename:"cityberth_tables",
	dataUrl:"cityberth.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#cityberthobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityberthT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); 
		Twin({Id:"sensor_search_w",Title:"搜索泊位",Width:550,sysfun:function(tObj){
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
						_cityberthT.C({
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
	bts.push({dname:"添加泊位",icon:"edit_add.png",onpress:function(Obj){
				T.each(_cityberthT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
				Twin({Id:"cityberth_add",Title:"添加泊位",Width:550,sysfun:function(tObj){
					Tform({
						formname: "parking_edit_f",
						formObj:tObj,
						recordid:"id",
						suburl:"cityberth.do?action=create",
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_mediaField}],
							rules:rules
						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("cityberth_add");} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"添加成功！",2,"");
								TwinC("cityberth_add");
								_cityberthT.M();
							}else if(ret=="-1"){
								T.loadTip(1,"请选择所属车场 ！",2,"");
							}else if(ret == "-2"){
								T.loadTip(1,"该泊位号已存在 ！",2,"");
							}else if(ret == "-3"){
								T.loadTip(1,"车检器编号不存在 ！",2,"");
							}else if(ret == "-4"){
								T.loadTip(1,"该车检器编号已和其他泊位绑定 ！",2,"");
							}else {
								T.loadTip(1,ret,2,o);
							}
						}
					});	
				}
			});
		}});
	if(subauth[4])
		bts.push({dname:"批量添加泊位",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityberthT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		
		Twin({Id:"compark_add",Title:"添加泊位",Width:550,sysfun:function(tObj){
				Tform({
					formname: "compark_edit_f",
					formObj:tObj,
					recordid:"id",
					suburl:"compark.do?action=create",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_bath}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("compark_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(parseInt(ret)>0){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("compark_add");
							_cityberthT.M();
						}else if(ret == "-2"){
							T.loadTip(1,"此泊位编号范围内包含已经存在的泊位编号，请重新调整范围！",2,"");
						}else{
							T.loadTip(1,"添加失败！",2,o);
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
	if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		/* T.each(_cityberthT.tc.tableitems,function(o,j){
			o.fieldvalue = _cityberthT.GD(id)[j];
		}); */
		var cid = _cityberthT.GD(id,"cid");
		var address = _cityberthT.GD(id,"address");
		var longitude = _cityberthT.GD(id,"longitude");
		var latitude = _cityberthT.GD(id,"latitude");
		Twin({Id:"cityberth_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityberth_edit_f",
					formObj:tObj,
					recordid:"cityberthsec_id",
					suburl:"cityberth.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_edit}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("cityberth_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("cityberth_edit_"+id);
							_cityberthT.M();
						}else if(ret=="-1"){
								T.loadTip(1,"请选择所属车场 ！",2,"");
						}else if(ret=="-2"){
								T.loadTip(1,"该泊位编号已存在！",2,"");
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});	
				T("#cityberth_edit_f_cid").value=cid;
				T("#cityberth_edit_f_address").value=address;
				T("#cityberth_edit_f_longitude").value=longitude;
				T("#cityberth_edit_f_latitude").value=latitude;
			}
		})
	}});
	if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("cityberth.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_cityberthT.M();
				}if(ret=="-2"){
					T.loadTip(1,"请先解除和车检器的绑定！",2,"");
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	if(subauth[5])
	bts.push({name:"绑定车检器",fun:function(id){
		var comid = _cityberthT.GD(id,"comid");
		Twin({
			Id:"induce_detail_"+id,
			Title:"绑定车检器  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
			Content:"<iframe src=\"cityberth.do?action=tobindsensor&berthid="+id+"&comid="+comid+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
			Width:T.gww()-100,
			Height:T.gwh()-50
		})
	}});
	if(subauth[6])
	bts.push({name:"解绑车检器",fun:function(id){
		Tconfirm({Title:"解绑车检器",Content:"确认解绑车检器吗",OKFn:function(){
		T.A.sendData("cityberth.do?action=unbindsensor","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"解绑成功！",2,"");
					_cityberthT.M();
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
		var url = "";
		if(colname == "berthsec_id"){
			url = "cityberthseg.do?action=getberthseg&id="+value;
		}else if(colname == "dici_id"){
			url = "cityberth.do?action=getdici&id="+value;
		}
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
	_cityberthT.UCD(rowid,name,value);
}

_cityberthT.C();
</script>

</body>
</html>
