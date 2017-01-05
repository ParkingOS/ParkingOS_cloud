<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>车型管理</title>
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
<div id="freereasonssetobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
/*权限*/
var issupperadmin=${supperadmin};
var authlist ="";
if(issupperadmin&&issupperadmin==1)
	authlist="0,1,2,3,4";
else
	authlist= T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/


var role=${role};
var comid=${comid};
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false},
		{fieldcnname:"名称",fieldname:"name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
		{fieldcnname:"排序",fieldname:"sort",fieldvalue:'',inputtype:"number",twidth:"100" ,height:"",issort:false}
	];
var _freereasonssetT = new TQTable({
	tabletitle:"免费原因设定",
	ischeck:false,
	tablename:"freereasons_tables",
	dataUrl:"freereasons.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query&comid="+comid,
	tableObj:T("#freereasonssetobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
 	var bts=[];
 	if(subauth[1])
 		bts.push({dname:"添加免费原因",icon:"edit_add.png",onpress:function(Obj){
 			T.each(_freereasonssetT.tc.tableitems,function(o,j){
			o.fieldvalue = "";
		});
		Tconfirm({
				Title:"提示信息",
				Ttype:"alert",
				Content:"提示：添加免费原因需要更新车场端HD，版本在1.2.5以上才可以使用！",
				OKFn:function(){
					Twin({Id:"freereasons_add",Title:"添加免费原因",Width:550,sysfun:function(tObj){
						Tform({
							formname: "parking_edit_f",
							formObj:tObj,
							recordid:"id",
							suburl:"freereasons.do?action=create&comid="+comid,
							method:"POST",
							Coltype:2,
							formAttr:[{
								formitems:[{kindname:"",kinditemts:_mediaField}]
							}],
							buttons : [//工具
								{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("freereasons_add");} }
							],
							subFun:checkSort,
							Callback:
							function(f,rcd,ret,o){
								if(ret=="1"){
									T.loadTip(1,"添加成功！",2,"");
									TwinC("freereasons_add");
									_freereasonssetT.M();
								}else if(ret=='-2'){
									T.loadTip(1,"不能重复添加 ！",2,"");
								}else {
									T.loadTip(1,ret,2,o);
								}
							}
						});	
			}
		})
				}
			});
		
	}});
	
	return bts;
}

//查看,添加,编辑,删除,设置区分大小车
function getAuthIsoperateButtons(){
	var bts = [];
	if(subauth[2])
		bts.push({name:"编辑",fun:function(id){
		T.each(_freereasonssetT.tc.tableitems,function(o,j){
			o.fieldvalue = _freereasonssetT.GD(id)[j]
		});
		Twin({Id:"freereasons_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "freereasons_edit_f",
					formObj:tObj,
					recordid:"freereasons_id",
					suburl:"freereasons.do?action=edit&comid="+comid+"&id="+id,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_freereasonssetT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("cartype_edit_"+id);} }
					],
					subFun:checkSort,
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("freereasons_edit_"+id);
							_freereasonssetT.M()
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
		//id = _freereasonssetT.GD(id)[1];
		Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){T.A.sendData("freereasons.do?action=delete&comid="+comid,"post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"删除成功！",2,"");
					_freereasonssetT.M()
				}else{
					T.loadTip(1,ret,2,"");
				}
			}
		)}})
	}});
	
	if(bts.length <= 0){return false;}
	return bts;
}
//检查序号
var checkSort=function(formname){
	var val = T("#"+formname+"_sort").value;
	var cid =  T("#"+formname+"_id").value;
	var talbleData = _freereasonssetT.oGridData;
	if(talbleData){
		var rows =talbleData.rows; 
		//alert(rows);
		for (var i=0;i<rows.length;i++){
			var id = rows[i].id;
			var fval = _freereasonssetT.GD(id,"sort");
			//alert(val+"--->"+fval);
			if(cid==''){//新加记录
				if(val==fval){
					alert('排序号：'+val+'已存在！');
					return false;
				}
			}else{//修改记录
				if(val==fval&&id!=cid){
					alert('排序号：'+val+'已存在！');
					return false;
				}
			}
			
		}
	}
	return true;
}

_freereasonssetT.C();
</script>

</body>
</html>
