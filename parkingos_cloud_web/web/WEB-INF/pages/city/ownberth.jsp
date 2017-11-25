<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>已绑定泊位</title>
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
/* var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false];
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
} */
var comid="${comid}";
var berthsegid="${berthsegid}";
var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false,shide:true,hide:true,fhide:true},
		{fieldcnname:"泊位编号",fieldname:"cid",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",twidth:"150",noList:parks,target:"berthsec_id",action:"getberthseg",twidth:"150" ,height:"",issort:false,shide:true},
		{fieldcnname:"所属泊位段",fieldname:"berthsec_id",fieldvalue:'',inputtype:"cselect",noList:[],action:"",twidth:"150" ,height:"",issort:false,shide:true,
			process:function(value,pid){
				return setcname(value,pid,'berthsec_id');
			}},
		{fieldcnname:"车检器",fieldname:"did",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",hide:true,shide:true},
		{fieldcnname:"状态",fieldname:"state",fieldvalue:'',inputtype:"select",noList:[{"value_no":"-1","value_name":"全部"},{"value_no":"0","value_name":"空闲"},{"value_no":"1","value_name":"占用"}], twidth:"80" ,height:"",hide:true},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"showmap",twidth:"150" ,height:"",issort:false},
		{fieldcnname:"经度",fieldname:"longitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true,shide:true},
		{fieldcnname:"纬度",fieldname:"latitude",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",edit:false,hide:true,fhide:true,shide:true},
		/* {fieldcnname:"唯一编号",fieldname:"uuid",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false}, */
		{fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date",twidth:"150" ,height:"",issort:false,hide:true}
	];
var _cityberthT = new TQTable({
	tabletitle:"已绑定泊位",
	ischeck:true,
	tablename:"cityberth_tables",
	dataUrl:"cityberthseg.do",
	iscookcol:false,
	//dbuttons:false,
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=queryberth&berthsegid="+berthsegid,
	tableObj:T("#cityberthobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});
function getAuthButtons(){
	var bts=[];
	bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		/* T.each(_cityberthT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		}); */
		Twin({Id:"ownbind_search_w",Title:"搜索停车场",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "ownbind_search_f",
					formObj:tObj,
					formWinId:"ownbind_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("ownbind_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_cityberthT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&action=queryberth&berthsegid="+berthsegid+"&"+Serializ(formName)
						})
					}
				});	
			}
		})
	}});
	bts.push({dname:"批量解绑泊位", icon: "sendsms.gif", onpress:function(Obj){
		var sids = _cityberthT.GS();
		if(!sids){
			T.loadTip(1,"请先选择要解绑的泊位",2,"");
			return;
		}
		Tconfirm({Title:"批量解绑泊位",Content:"确认解绑吗？",OKFn:function(){
			T.A.sendData("cityberthseg.do?action=unbindberth","post","id="+sids,
				function deletebackfun(ret){
					if(ret=="1"){
						T.loadTip(1,"解绑成功！",5,"");
						_cityberthT.M();
						window.parent._cityberthsegT.M();
					}else{
						T.loadTip(1,"解绑失败！",2,"");
					}
				}
		)}})
	}})
	if(bts.length>0)
		return bts;
	return false;
}
function getAuthIsoperateButtons(){
	var bts = [];
	bts.push({name:"解绑",fun:function(id){
		Tconfirm({Title:"解绑",Content:"确认解绑吗",OKFn:function(){
		T.A.sendData("cityberthseg.do?action=unbindberth","post","id="+id,
			function deletebackfun(ret){
				if(ret=="1"){
					T.loadTip(1,"解绑成功！",2,"");
					_cityberthT.M();
					window.parent._cityberthsegT.M();
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
