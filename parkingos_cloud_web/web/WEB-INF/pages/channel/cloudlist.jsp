<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>系统后台LOGO管理</title>
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
<div id="clogoobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",inputtype:"text", twidth:"50" ,issort:false,edit:false,hide:true,shide:true,fhide:true},
		{fieldcnname:"名称",fieldname:"name",inputtype:"text", twidth:"150" ,issort:false},
		{fieldcnname:"系统后台LOGO",fieldname:"url_fir",inputtype:"date", twidth:"150" ,edit:false,hide:true,
			process:function(value,trId,colId){//值、行ID(记录ID)、列ID(字段名称)
				if(value!=''&&value!='null'){
					return "<a href='#' onclick='viewpic(\""+value+"\")'>点击查看</a>";
				}else
					return value;
			}
		},
		{fieldcnname:"岗亭端主界面LOGO",fieldname:"url_sec",inputtype:"date", twidth:"150" ,edit:false,hide:true,
			process:function(value,trId,colId){//值、行ID(记录ID)、列ID(字段名称)
				if(value!=''&&value!='null'){
					return "<a href='#' onclick='viewpic(\""+value+"\")'>点击查看</a>";
				}else
					return value;
			}
		}
		
	];
function viewpic(name){
	var url = 'viewpic.html?name='+name+'&db=logo_pics';
	Twin({Id:"clogo_edit_pic",Title:"查看logo",Width:850,Height:600,sysfunI:"v_pic",
			Content:"<iframe id='tactic_iframe' src='"+url+"' style='width:100%;height:100%;' frameborder='0' ></iframe>",
			buttons :[],
			CloseFn:function(){
				
			}
		})
}
var _clogoT = new TQTable({
	tabletitle:"logo管理",
	ischeck:false,
	tablename:"clogo_tables",
	dataUrl:"cloudlogo.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=querycloud",
	tableObj:T("#clogoobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	bts.push({dname:"添加LOGO",icon:"edit_add.png",onpress:function(Obj){
	T.each(_clogoT.tc.tableitems,function(o,j){
		o.fieldvalue ="";
	});
	Twin({Id:"_clogoT_add",Title:"添加LOGO",Width:550,sysfun:function(tObj){
			Tform({
				formname: "_clogoT_edit_f",
				formObj:tObj,
				recordid:"id",
				suburl:"cloudlogo.do?action=create&type=${type}",
				method:"POST",
				formAttr:[{
					formitems:[{kindname:"",kinditemts:_mediaField}]
				}],
				buttons : [//工具
					{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("_clogoT_add");} }
				],
				Callback:
				function(f,rcd,ret,o){
					if(ret=="1"){
						T.loadTip(1,"添加成功！",2,"");
						TwinC("_clogoT_add");
						_clogoT.M();
					}else if(ret=="2"){
						T.loadTip(1,"已存在记录，不能重复添加",2,o);
					}else{
						T.loadTip(1,"添加失败",2,o);
					}
				}
			});	
		}
	})
	}});
	return bts;
}
function getAuthIsoperateButtons(){
	var bts = [];
	bts.push({name:"上传系统后台logo",fun:function(id){
		var url ="upload.html?url=cloudlogo&action=uploadpic&table=logo_pics&type=${type}&logotype=1&id="+id;
		Twin({Id:"clogo_edit_"+id,Title:"上传logo",Width:350,Height:200,sysfunI:id,
			Content:"<iframe id='tactic_iframe' src='"+url+"' style='width:100%;height:100%;' frameborder='0' ></iframe>",
			buttons :[],
			CloseFn:function(){
				_clogoT.M();
			}
			})
	}});
	bts.push({name:"|	上传岗亭端主界面logo",fun:function(id){
		var url ="upload.html?url=cloudlogo&action=uploadpic&table=logo_pics&type=${type}&logotype=2&id="+id;
		Twin({Id:"clogo_edit_"+id,Title:"上传logo",Width:350,Height:200,sysfunI:id,
			Content:"<iframe id='tactic_iframe' src='"+url+"' style='width:100%;height:100%;' frameborder='0' ></iframe>",
			buttons :[],
			CloseFn:function(){
				_clogoT.M();
			}
			})
	}});
	bts.push({name:"|	编辑",fun:function(id){
		T.each(_clogoT.tc.tableitems,function(o,j){
			o.fieldvalue = _clogoT.GD(id)[j]
		});
		Twin({Id:"_clogoT_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "_clogoT_edit_f",
					formObj:tObj,
					recordid:"_clogoT_id",
					suburl:"cloudlogo.do?&action=edit&id="+id,
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_clogoT.tc.tableitems}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("_clogoT_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("_clogoT_edit_"+id);
							_clogoT.M();
						}else{
							T.loadTip(1,"编辑失败！",2,o)
						}
					}
				});	
			}
		})
	}});
	if(bts.length <= 0){return false;}
	return bts;
}
_clogoT.C();
</script>

</body>
</html>
