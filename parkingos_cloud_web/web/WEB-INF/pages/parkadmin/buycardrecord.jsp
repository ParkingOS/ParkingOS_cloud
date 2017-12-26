<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>月卡续费记录</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.public.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.datatable.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.form.js?08555" type="text/javascript">//表单</script>
<script src="js/tq.searchform.js?0817" type="text/javascript">//查询表单</script>
<script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
<script src="js/jquery.js" type="text/javascript"></script>
</head>

<body>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<div id="buycardobj" style="width:100%;height:100%;margin:0px;"></div>
<form action="" method="post" id="choosecom"></form>
<script language="javascript">
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var users =eval(T.A.sendData("getdata.do?action=getuser&id=${comid}")); 
var allpass =eval(T.A.sendData("getdata.do?action=getcompass&id=${comid}"));
var subauth=[false,false];
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
var groupid = "${groupid}";
//var liftreason=eval('${liftreason}');
var cityid="${cityid}";

var _mediaField = [
		{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false,edit:false},
		{fieldcnname:"购买流水号",fieldname:"trade_no",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"月卡编号",fieldname:"card_id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"月卡续费时间",fieldname:"pay_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"应收金额",fieldname:"amount_receivable",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"实收金额",fieldname:"amount_pay",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"收费员",fieldname:"collector",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"缴费类型",fieldname:"pay_type",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车牌号",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"140" ,height:"",issort:false},
		{fieldcnname:"用户编号",fieldname:"user_id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		//{fieldcnname:"购买月数",fieldname:"buy_month",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"有效期",fieldname:"limit_time",fieldvalue:'',inputtype:"date", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"备注",fieldname:"resume",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false}
	];
var _buycardT = new TQTable({
	tabletitle:"月卡续费记录",
	ischeck:false,
	tablename:"buycard_tables",
	dataUrl:"buycardrecord.do",
	iscookcol:false,
	//dbuttons:false,
	quikcsearch:coutomsearch(),
	buttons:getAuthButtons(),
	//searchitem:true,
	isidentifier:false,
	param:"action=query&comid="+comid,
	tableObj:T("#buycardobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	tHeight:100,
	rpage:20,
	isoperate:getAuthIsoperateButtons()
});
function coutomsearch(){
	var html = "";
	if(groupid != ""){
		html = "<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp;&nbsp;&nbsp;&nbsp;车场:&nbsp;&nbsp;<select id='companys' onchange='searchdata();' ></select></div>";
	}
    if(cityid != ""){
        html += "<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp;&nbsp;&nbsp;&nbsp;所属集团:&nbsp;&nbsp;<select id='groups' onchange='searchgroupdata();'></select></div>";
        html += "<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp;&nbsp;&nbsp;&nbsp;车场:&nbsp;&nbsp;<select id='newCompanys' onchange='searchCompanydata();' ></select></div>";
    }

    html += "&nbsp;&nbsp;<span id='total_money'></span>";
	return html;
}

function searchgroupdata(){
	groupid = T("#groups").value;
    _buycardT.C({
        cpage:1,
        tabletitle:"高级搜索结果",
        extparam:"&groupid="+groupid+"&action=query"
    })
    addgroups();
    addCitycoms();
}

function searchCompanydata(){
    comid = T("#newCompanys").value;
    _buycardT.C({
        cpage:1,
        tabletitle:"高级搜索结果",
        extparam:"&comid="+comid+"&action=query"
    })
    addgroups();
    addCitycoms();
}

function searchdata(){
    comid = T("#companys").value;
    T("#choosecom").action="buycardrecord.do?comid="+comid+"&authid=${authid}&r"+Math.random();
    T("#choosecom").submit();
}
function getAuthButtons(){
	var bts=[];
	if(subauth[0])
		bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
		T.each(_buycardT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"buycard_search_w",Title:"月卡续费记录",Width:550,sysfun:function(tObj){
				TSform ({
					formname: "buycard_search_f",
					formObj:tObj,
					formWinId:"buycard_search_w",
					formFunId:tObj,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("buycard_search_w");} }
					],
					SubAction:
					function(callback,formName){
						_buycardT.C({
							cpage:1,
							tabletitle:"高级搜索结果",
							extparam:"&comid="+comid+"&action=query&"+Serializ(formName)
						})
                        addgroups();
                        addCitycoms();
					}
				});	
			}
		})
	
	}});
	if(subauth[1])
		bts.push({dname:"导出",icon:"toxls.gif",onpress:function(Obj){
		Twin({Id:"buycard_export_w",Title:"导出<font style='color:red;'>（如果没有设置，默认全部导出!）</font>",Width:480,sysfun:function(tObj){
				 TSform ({
					formname: "buycard_export_f",
					formObj:tObj,
					formWinId:"buycard_export_w",
					formFunId:tObj,
					dbuttonname:["确认导出"],
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_mediaField}],
					}],
					//formitems:[{kindname:"",kinditemts:_excelField}],
					SubAction:
					function(callback,formName){
						T("#exportiframe").src="buycardrecord.do?action=exportExcel&comid="+comid
						+"&rp="+2147483647+"&fieldsstr="+"id__trade_no__card_id__pay_time__amount_receivable__amount_pay__collector__pay_type__car_number__user_id__limit_time__resume&"+Serializ(formName)
						TwinC("buycard_export_w");
						T.loadTip(1,"正在导出，请稍候...",2,"");
					}
				});	
			}
		})
	}});
	
	return bts;
}
//"查看,添加,编辑,删除,编辑辅助价格"
function getAuthIsoperateButtons(){
	var bts = [];
	return false;
}
_buycardT.C();

function addcoms(){
	if(groupid != ""){
		var childs = eval(T.A.sendData("getdata.do?action=getcoms&groupid=${groupid}"));
		jQuery("#companys").empty();
        jQuery("#companys").append("<option value='-1' selected >请选择</option>");
		for(var i=0;i<childs.length;i++){
			var child = childs[i];
			var id = child.value_no;
			var name = child.value_name;
            jQuery("#companys").append("<option value='"+id+"'>"+name+"</option>");
		}
		T("#companys").value = comid;
	}
}
if(groupid != ""){//集团管理员登录下显示车场列表
	addcoms();
}


function addgroups(){
    if(cityid != ""){
        var childs = eval(T.A.sendData("getdata.do?action=getgroups&cityid=${cityid}"));
        jQuery("#groups").empty();
        for(var i=0;i<childs.length;i++){
            var child = childs[i];
            var id = child.value_no;
            var name = child.value_name;
            jQuery("#groups").append("<option value='"+id+"'>"+name+"</option>");
        }
        T("#groups").value = groupid;
    }
}
function addCitycoms(){
    if(cityid != ""){
        var childs = eval(T.A.sendData("getdata.do?action=getcoms&cityid=${cityid}"));
        jQuery("#newCompanys").empty();
        jQuery("#newCompanys").append("<option value='-1' selected>请选择</option>");
        for(var i=0;i<childs.length;i++){
            var child = childs[i];
            var id = child.value_no;
            var name = child.value_name;
            jQuery("#newCompanys").append("<option value='"+id+"'>"+name+"</option>");
        }
        if(comid){
            T("#newCompanys").value = comid;
        };
    }
}

if(cityid != ""){//集团管理员登录下显示车场列表
    groupid=-1;
    comid=-1;
    addgroups();
    addCitycoms();
}


var getobj=function(id){return document.getElementById(id)};
</script>

</body>
</html>
