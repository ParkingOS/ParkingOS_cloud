<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>巡查组管理</title>
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
<div id="workgroupmanageobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
var statelist = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"否"},{"value_no":1,"value_name":"是"}];
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false},
		{fieldcnname:"巡查组名称",fieldname:"inspectgroup_name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
	    {fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false,edit:false,hide:true},
	    {fieldcnname:"是否可用",fieldname:"is_active",fieldvalue:'',inputtype:"select",noList:statelist,twidth:"100" ,height:"",issort:false,hide:true}

	];
var _edit=[
		{fieldcnname:"巡查组名称",fieldname:"inspectgroup_name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
	    {fieldcnname:"更新时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false,hide:true},
	    {fieldcnname:"是否可用",fieldname:"is_active",fieldvalue:'',inputtype:"select",noList:statelist,twidth:"100" ,height:"",issort:false}

	];
/* var rules =[
		{name:"berthsec_name",type:"",url:"",requir:true,warn:"不能为空!",okmsg:""},
		{name:"comid",requir:true}
		]; */

var _workgroupmanageT = new TQTable({
	tabletitle:"巡查组管理",
	ischeck:false,
	tablename:"workgroupmanage_tables",
	dataUrl:"inspectgroupmanage.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=quickquery",
	tableObj:T("#workgroupmanageobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	if(subauth[1])
	bts.push({dname:"添加巡查组",icon:"edit_add.png",onpress:function(Obj){
				T.each(_workgroupmanageT.tc.tableitems,function(o,j){
					o.fieldvalue = "";
				});
				Twin({Id:"workgroupmanage_add",Title:"添加巡查组",Width:550,sysfun:function(tObj){
					Tform({
						formname: "group_edit_f",
						formObj:tObj,
						recordid:"id",
						suburl:"inspectgroupmanage.do?action=create",
						method:"POST",
						Coltype:2,
						formAttr:[{
							formitems:[{kindname:"",kinditemts:_mediaField}],

						}],
						buttons : [//工具
							{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("workgroupmanage_add");} }
						],
						Callback:
						function(f,rcd,ret,o){
							if(ret=="1"){
								T.loadTip(1,"添加成功！",2,"");
								TwinC("workgroupmanage_add");
								_workgroupmanageT.M();
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

		var inspectgroup_name = _workgroupmanageT.GD(id,"inspectgroup_name");
		var is_active = _workgroupmanageT.GD(id,"is_active");


		Twin({Id:"workgroupmanage_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "workgroupmanage_edit_f",
					formObj:tObj,
					recordid:"workgroupmanage_id",
					suburl:"inspectgroupmanage.do?action=edit&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_edit}],

					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("workgroupmanage_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("workgroupmanage_edit_"+id);
							_workgroupmanageT.M()
						}else if(ret=="-1"){
								T.loadTip(1,"请选择车场 ！",2,"");
						}else{
							T.loadTip(1,ret,2,o);
						}
					}
				});

				T("#workgroupmanage_edit_f_inspectgroup_name").value=inspectgroup_name;
				T("#workgroupmanage_edit_f_is_active").value=is_active;

			}
		})
	}});
	if(subauth[3])
	bts.push({name:"删除",fun:function(id){
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
		T.A.sendData("inspectgroupmanage.do?action=delete","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_workgroupmanageT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});

	if(subauth[4])
	bts.push({name:"绑定泊位段",fun:function(id){
		Twin({
			Id:"bethsec_detail_"+id,
			Title:"绑定泊位段  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
			Content:"<iframe src=\"inspectgroupmanage.do?action=addberthsec&work_group_id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
			Width:T.gww()-100,
			Height:T.gwh()-50
		})
	}});
//	if(subauth[4])
//	bts.push({name:"绑定收费员",fun:function(id){
//		Twin({
//			Id:"employee_detail_"+id,
//			Title:"绑定收费员  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
//			Content:"<iframe src=\"inspectgroupmanage.do?action=addemployee&work_group_id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
//			Width:T.gww()-100,
//			Height:T.gwh()-50
//		})
//	}});
	if(subauth[4])
		bts.push({name:"绑定巡查员",fun:function(id){
			Twin({
				Id:"inspector_detail_"+id,
				Title:"绑定巡查员  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
				Content:"<iframe src=\"inspectgroupmanage.do?action=addinspector&work_group_id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
				Width:T.gww()-100,
				Height:T.gwh()-50
			})
		}});
	if(bts.length <= 0){return false;}
	return bts;
}


function updateRow(rowid,name,value){
	//alert(value);
	if(value)
	_workgroupmanageT.UCD(rowid,name,value);
}
_workgroupmanageT.C();
</script>

</body>
</html>
