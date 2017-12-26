<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>会员管理</title>
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.public.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.datatable.js?0817" type="text/javascript">//表格</script>
<script src="js/tq.form.js?0819" type="text/javascript">//表单</script>
<script src="js/tq.searchform.js?0817" type="text/javascript">//查询表单</script>
<script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>
<script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
<script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
<script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
<script src="js/jquery.js" type="text/javascript"></script>
</head>
<body onload="addcoms();">
<div id="cityvipobj" style="width:100%;height:100%;margin:0px;"></div>
<iframe src="" id ="exportiframe" frameborder="0" style="width:0px;height:0px;"></iframe>
<form action="" method="post" id="choosecom"></form>
<script language="javascript">
var role=${role};
if(parseInt(role)==15||parseInt(role)==3){
	window.onload = jslimit()
}
/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var subauth=[false,false,false,false,false];
//"查看,注册,编辑,修改车牌,删除,导出"
var ownsubauth=authlist.split(",");
for(var i=0;i<ownsubauth.length;i++){
	subauth[ownsubauth[i]]=true;
}
/*权限*/

var parks = eval(T.A.sendData("cityberthseg.do?action=getcityparks"));
var month_select = [];
for(var i=1;i<36;i++){
	month_select.push({"value_no":i,"value_name":i});
}
var pnames = eval(T.A.sendData("getdata.do?action=getpackage&id=${groupid}"));
var carTypes= eval(T.A.sendData("getdata.do?action=getcartype&groupid=${groupid}"));
var _mediaField = [
		/*{fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false,edit:false,hide:true,fhide:true},
		{fieldcnname:"车主手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
		{fieldcnname:"车主账户",fieldname:"uin",fieldvalue:'',inputtype:"number", twidth:"80" ,height:"",issort:false},
		{fieldcnname:"名字",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",issort:false},
		{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false},
		{fieldcnname:"车牌号码",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"230" ,height:"",issort:false,edit:false,
			process:function(value,trId,colId){//值、行ID(记录ID)、列ID(字段名称)
					return setname(trId,'car_number');
				}},
		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false},
		{fieldcnname:"最近一次购买时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:""},
		{fieldcnname:"月卡结束时间",fieldname:"e_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",edit:false},
		{fieldcnname:"购买月卡次数(包含续费)",fieldname:"bcount",fieldvalue:'',inputtype:"number", twidth:"150" ,height:"",edit:false,
			process:function(value,cid,id){
					return "<a href=# onclick=\"viewdetail('"+value+"','"+cid+"')\" style='color:blue'>"+value+"</a>";
				}},
		{fieldcnname:"应收金额合计",fieldname:"atotal",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",edit:false},
		{fieldcnname:"实收金额合计",fieldname:"acttotal",fieldvalue:'',inputtype:"number", twidth:"100" ,height:""}*/
    {fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"60" ,height:"",issort:false,edit:false,hide:true},
    //{fieldcnname:"套餐名称",fieldname:"pid",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"无"}], twidth:"100" ,height:"",edit:false},
    {fieldcnname:"套餐名称",fieldname:"pid",fieldvalue:'',inputtype:"select",noList:pnames, twidth:"140" ,height:"",edit:false},
    //注释掉月卡会员编号，手机号码，地址不做展示
    {fieldcnname:"所属车场",fieldname:"com_id",fieldvalue:'',inputtype:"select",noList:parks,twidth:"100" ,height:"",issort:false},
    //{fieldcnname:"月卡编号",fieldname:"card_id",fieldvalue:'',inputtype:"text", twidth:"160" ,height:"",issort:false,edit:false},
    //{fieldcnname:"手机号码",fieldname:"mobile",fieldvalue:'',inputtype:"text",twidth:"130" ,height:"",issort:false},
    {fieldcnname:"车主姓名",fieldname:"name",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false},
    //{fieldcnname:"地址",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false},
    {fieldcnname:"车牌号码",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"130" ,height:"",issort:false},
    {fieldcnname:"购买时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",edit:false},
    {fieldcnname:"开始时间",fieldname:"b_time",fieldvalue:'',inputtype:"date",twidth:"140" ,height:"",issort:false,edit:false,
        //将时间展示完整，包括时分秒
        /*process:function(value,trId,colId){
            if(value.length>10)
                return value.substring(0,10);
        }*/
    },
    {fieldcnname:"结束时间",fieldname:"e_time",fieldvalue:'',inputtype:"date", twidth:"140" ,height:"",edit:false,
        /*process:function(value,trId,colId){
            if(value.length>10)
                return value.substring(0,10);
        }*/
    },
    {fieldcnname:"应收金额",fieldname:"total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",edit:false},
    {fieldcnname:"实收金额",fieldname:"act_total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",edit:false},
    {fieldcnname:"联系电话",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
    {fieldcnname:"车型类型",fieldname:"car_type_id",fieldvalue:'',inputtype:"select",noList:carTypes, twidth:"150" ,height:"",issort:false},
    {fieldcnname:"单双日限行",fieldname:"limit_day_type",fieldvalue:'',inputtype:"select",noList:[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"不限制"},{"value_no":1,"value_name":"限制"}], twidth:"150" ,height:"",issort:false},

    //{fieldcnname:"车位编号",fieldname:"p_lot",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
    {fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false}

];
var sparks = parks;
sparks[0]={"value_no":"-1","value_name":"全部"};
var _addField = [
		{fieldcnname:"车主手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false},
		{fieldcnname:"名字(选填)",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",issort:false},
		{fieldcnname:"地址(选填)",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false},
        {fieldcnname:"车牌号码",fieldname:"car_number",fieldvalue:'',inputtype:"text", twidth:"130" ,height:"",issort:false},
        {fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"checkbox",noList:sparks,target:"p_name",action:"getpname",twidth:"100" ,height:"",issort:false},
//		{fieldcnname:"包月产品",fieldname:"p_name",fieldvalue:'',inputtype:"cselect",noList:[],action:"",target:"total",params:["months","p_name"],action:"getprodsum",twidth:"100" ,height:"",issort:false,shide:true,
//			process:function(value,pid){
//				return setcname(value,pid,'p_name');
//			}},
		{fieldcnname:"起始时间",fieldname:"b_time",fieldvalue:'',inputtype:"sdate",twidth:"150" ,height:"",issort:false},
        {fieldcnname:"结束时间",fieldname:"e_time",fieldvalue:'',inputtype:"sdate", twidth:"140" ,height:""},
        //{fieldcnname:"购买月数",fieldname:"months",fieldvalue:'',inputtype:"cselect",noList:month_select,target:"total",params:["months","p_name"],action:"getprodsum", twidth:"150" ,height:"",issort:false},
		//{fieldcnname:"应收金额",fieldname:"total",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false,edit:false},
		{fieldcnname:"实收金额",fieldname:"act_total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
	//	{fieldcnname:"车位编号",fieldname:"p_lot",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false}
	];
var _editField = [
         		{fieldcnname:"名字(选填)",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",issort:false},
				{fieldcnname:"地址(选填)",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false}
         	];
var _renewField = [
         		{fieldcnname:"车主手机",fieldname:"mobile",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false,edit:false},
         		{fieldcnname:"名字(选填)",fieldname:"nickname",fieldvalue:'',inputtype:"text", twidth:"80" ,height:"",issort:false,edit:false},
				{fieldcnname:"地址(选填)",fieldname:"address",fieldvalue:'',inputtype:"text", twidth:"180" ,height:"",issort:false,edit:false,fhide:true,shide:true},
         		{fieldcnname:"所属车场",fieldname:"comid",fieldvalue:'',inputtype:"cselect",noList:parks,target:"p_name",action:"getpname",twidth:"100" ,height:"",issort:false,edit:false},
				{fieldcnname:"包月产品",fieldname:"p_name",fieldvalue:'',inputtype:"cselect",noList:[],action:"",target:"total",params:["months","p_name"],action:"getprodsum",twidth:"100" ,height:"",issort:false,shide:true,
					process:function(value,pid){
						return setcname(value,pid,'p_name');
					}},
         		{fieldcnname:"起始时间",fieldname:"b_time",fieldvalue:'',inputtype:"sdate",twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"续费月数",fieldname:"months",fieldvalue:'',inputtype:"cselect",noList:month_select,target:"total",params:["months","p_name"],action:"getprodsum", twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"应收金额",fieldname:"total",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false,edit:false},
				{fieldcnname:"实收金额",fieldname:"act_total",fieldvalue:'',inputtype:"text", twidth:"100" ,height:""},
         		{fieldcnname:"车位编号",fieldname:"p_lot",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
         		{fieldcnname:"备注",fieldname:"remark",fieldvalue:'',inputtype:"text", twidth:"200" ,height:"",issort:false}
         	];
var rules =[
    //{name:"p_name",type:"",url:"",requir:true,warn:"请选择产品",okmsg:""},
    {name:"car_number",type:"",url:"",requir:true,warn:"请输入车牌",okmsg:""},
    {name:"b_time",type:"",url:"",requir:true,warn:"请选择时间",okmsg:""},
    {name:"e_time",type:"",url:"",requir:true,warn:"请选择时间",okmsg:""},
    {name:"comid",type:"",url:"",requir:true,warn:"请选择车场",okmsg:""}
			//{name:"mobile",type:"ajax",url:"vipuser.do?action=checkmobile&mobile=",requir:true,warn:"请填写用户真实手机号码!",okmsg:""}
			];
var groupid ='${groupid}';
//alert(groupid);
var unionId = '${unionId}';
var custumgroup = '${custumgroup}';
var title_page ='VIP会员管理';

if(groupid!=''&&custumgroup.indexOf(groupid)!=-1&&unionId=='200081') {
    title_page='渣土车管理';
}
var _cityvipT = new TQTable({
	tabletitle:title_page,
	ischeck:false,
	tablename:"cityvip_tables",
	dataUrl:"cityvip.do",
	iscookcol:false,
	//dbuttons:false,
	quikcsearch:coutomsearch(),
	buttons:getAuthButtons(),
	//searchitem:true,
	param:"action=query",
	tableObj:T("#cityvipobj"),
	fit:[true,true,true],
	tableitems:_mediaField,
	isoperate:getAuthIsoperateButtons()
});

function coutomsearch(){
    var html = "";
    //if(groupid != ""){
        html = "<div style='vertical-align:middle;margin-top:5px;float:left;'>&nbsp;&nbsp;&nbsp;&nbsp;车场:&nbsp;&nbsp;<select id='companys' onchange='searchdata();' ></select></div>";
  //  }
    html += "&nbsp;&nbsp;<span id='total_money'></span>";
    return html;
}
function searchdata(){
    comid = T("#companys").value;
    _cityvipT.C({
        cpage:1,
        tabletitle:"高级搜索结果",
        extparam:"action=query&comid="+comid,
    });
    addcoms(comid);
}

//"查看,注册,编辑,修改车牌,删除,导出"
function getAuthButtons(){
	var authButs=[];
	if(subauth[1])
		authButs.push({dname:"注册会员 ",icon:"edit_add.png",onpress:function(Obj){
		T.each(_cityvipT.tc.tableitems,function(o,j){
			o.fieldvalue ="";
		});
		Twin({Id:"cityvip_add",Title:"注册会员<font style='color:red;'></font>",Width:550,sysfun:function(tObj){
				Tform({
					formname: "cityvip_add_f",
					formObj:tObj,
					formWinId:"cityvip_add_w",
					formFunId:tObj,
					recordid:"id",
					Coltype:2,
					suburl:"cityvip.do?action=create",
					method:"POST",
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_addField}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("cityvip_add");} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret>0){
							T.loadTip(1,"添加成功！",2,"");
							TwinC("cityvip_add");
							_cityvipT.M();
						}else if(ret==-1){
							T.loadTip(1,"添加失败！",2,"");
						}else if(ret==-2){
						 	T.loadTip(2,"产品已超出有效期，请重新选择产品或更改购买月数！",7,"");
						}else 
							T.loadTip(2,ret,7,"");
					}
				});	
			}
		})}});
	if(subauth[0])
	authButs.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
        T.each(_cityvipT.tc.tableitems,function(o,j){
            o.fieldvalue ="";
        });
        Twin({Id:"cityvip_search_w",Title:"搜索会员",Width:550,sysfun:function(tObj){
            TSform ({
                formname: "cityvip_search_f",
                formObj:tObj,
                formWinId:"cityvip_search_w",
                formFunId:tObj,
                formAttr:[{
                    formitems:[{kindname:"",kinditemts:_mediaField}]
                }],
                buttons : [//工具
                    {name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("cityvip_search_w");} }
                ],
                SubAction:
                    function(callback,formName){
                        comid = T("#companys").value;
                        _cityvipT.C({
                            cpage:1,
                            tabletitle:"高级搜索结果",
                            extparam:"action=query&"+Serializ(formName)
                        })
                        addcoms(comid);
                    }
            });
        }
        })

    }});
    authButs.push({dname:"上传月卡",icon:"edit_add.png",onpress:function(Obj){
        var url ="uploadmonth.html";
        Twin({Id:"uploadmonth",Title:"上传月卡",Width:650,Height:400,sysfunI:"upload",
            Content:"<iframe id='tactic_iframe' src='"+url+"' style='width:100%;height:100%;' frameborder='0' ></iframe>",
            buttons :[],
            CloseFn:function(){
                _cityvipT.M();
            }
        })

    }});

	return authButs;
}
//"注册,编辑,修改车牌,删除,导出"
function getAuthIsoperateButtons(){
	var bts = [];
	/*if(subauth[2])
	bts.push({name:"编辑",fun:function(id){
		var uin = _cityvipT.GD(id,"uin");
		var name = _cityvipT.GD(id,"nickname");
		var address = _cityvipT.GD(id,"address");
		Twin({Id:"cityvip_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityvip_edit_f",
					formObj:tObj,
					recordid:"cityvip_id",
					suburl:"cityvip.do?action=edit&uin="+uin,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_editField}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("cityvip_edit_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"编辑成功！",2,"");
							TwinC("cityvip_edit_"+id);
							_cityvipT.M();
						}else{
							T.loadTip(1,"编辑失败！",2,"");
						}
					}
				});	
				T("#cityvip_edit_f_nickname").value=name;
				T("#cityvip_edit_f_address").value=address;
			}
		})
	}}); 
	 
	if(subauth[3])
	bts.push({name:"修改车牌",fun:function(id){
		var uin =_cityvipT.GD(id,"uin");
		var cars = T.A.sendData("vipuser.do?action=getcar&uin="+uin);
		Twin({Id:"cityvip_addcar_"+id,Title:"修改车牌",Width:450,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityvip_addcar_f",
					formObj:tObj,
					recordid:"cityvip_id",
					suburl:"vipuser.do?action=addcar&uin="+uin,
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:[{fieldcnname:"车牌号码(多个车牌，用,隔开)",fieldname:"carnumber",inputtype:"text",width:"300"}]}]
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消添加车牌",icon:"cancel.gif", onpress:function(){TwinC("cityvip_addcar_"+id);} }
					],
					Callback:function(f,rcd,ret,o){
						if(parseInt(ret)>0){
							T.loadTip(1,"修改了"+ret+"个车牌！",2,"");
							TwinC("cityvip_addcar_"+id);
							_cityvipT.M();
						}else {
							T.loadTip(2,ret,7,"");
						}
					}
				});	
			}
		})
		T("#cityvip_addcar_f_carnumber").value=cars;
	}});
	
	if(subauth[4])
	bts.push({name:"续费",fun:function(id){
		var mobile = _cityvipT.GD(id,"mobile");
		var b_time = _cityvipT.GD(id,"e_time");
		var name = _cityvipT.GD(id,"nickname");
		var address = _cityvipT.GD(id,"address");
		var comid = _cityvipT.GD(id,"comid");
		
		Twin({Id:"cityvip_renew_"+id,Title:"月卡续费",Width:550,sysfunI:id,sysfun:function(id,tObj){
				Tform({
					formname: "cityvip_renew_f",
					formObj:tObj,
					recordid:"cityvip_id",
					suburl:"cityvip.do?action=renew",
					method:"POST",
					Coltype:2,
					formAttr:[{
						formitems:[{kindname:"",kinditemts:_renewField}],
						rules:rules
					}],
					buttons : [//工具
						{name: "cancel", dname: "取消", tit:"取消续费",icon:"cancel.gif", onpress:function(){TwinC("cityvip_renew_"+id);} }
					],
					Callback:
					function(f,rcd,ret,o){
						if(ret=="1"){
							T.loadTip(1,"续费成功！",2,"");
							TwinC("cityvip_renew_"+id);
							_cityvipT.M();
						}else if(ret==-1){
							T.loadTip(1,"续费失败！",2,"");
						}else if(ret==-2){
						 	T.loadTip(2,"产品已超出有效期，请重新选择产品或更改购买月数！",7,"");
						}else 
							T.loadTip(2,ret,7,"");
					}
				});	
				T("#cityvip_renew_f_comid").value = comid;
				T("#cityvip_renew_f_mobile").value=mobile;
				T("#cityvip_renew_f_b_time").value=b_time;
				T("#cityvip_renew_f_nickname").value=name;
				T("#cityvip_renew_f_address").value=address;
				cactic_Select(comid,"cityvip_renew_f_p_name","getpname");
			}
		})
	}});*/

	if(bts.length <= 0){return false;}
	return bts;
}

function viewdetail(value,id){
	//alert(type+","+value);
	var comid =_cityvipT.GD(id,"comid");
	var mobile = _cityvipT.GD(id,"mobile");
	var nickname =_cityvipT.GD(id,"nickname");
	var company_name = "";
	if(parks != null && parks.length>0){
		for(var i=0; i<parks.length;i++){
			var cid = parks[i].value_no;
			if(cid == comid){
				company_name = parks[i].value_name;
				break;
			}
		}
	}
	var tip = "购买月卡记录，车场："+company_name;
		
	Twin({
		Id:"vip_detail_"+id,
		Title:tip+"  --> 车主："+nickname,
		Width:T.gww()-100,
		Height:T.gwh()-50,
		sysfunI:id,
		Content:"<iframe name='vip_detail_'"+id+" id='vip_detail_'"+id+" src='cityvip.do?action=detail&mobile="+mobile+"&comid="+comid+"' width='100%' height='100%' frameborder='0' scrolling='no' style='overflow:hidden;' ></iframe>"
	})
}

function setname(pid,colname){
	var uin = _cityvipT.GD(pid,"uin");
	T.A.C({
		url:"vipuser.do?action=getcar&uin="+uin,
	  		method:"GET",//POST or GET
	  		param:"",//GET时为空
	  		async:false,//为空时根据是否有回调函数(success)判断
	  		dataType:"0",//0text,1xml,2obj
	  		success:function(ret,tipObj,thirdParam){
	  			if(ret&&ret!='null'){
					updateRow(pid,colname,ret);
	  			}
		},//请求成功回调function(ret,tipObj,thirdParam) ret结果
	  		failure:function(ret,tipObj,thirdParam){
			return false;
		},//请求失败回调function(null,tipObj,thirdParam) 默认提示用户<网络失败>
	  		thirdParam:"",//回调函数中的第三方参数
	  		tipObj:null,//相关提示父级容器(值为字符串"notip"时表示不进行相关提示)
	  		waitTip:"正在获取审核数据...",
	  		noCover:true
	})
	return "<font style='color:#666'>获取中...</font>";
}

function setcname(value,pid,colname){
	if(value&&value!='-1'&&value!=''){
		T.A.C({
			url:"cityvip.do?action=getprodname&id="+value,
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

function addcoms(comid){
    var childs ;
    if(parseInt(${groupid})>0)
        childs= eval(T.A.sendData("getdata.do?action=getcoms&groupid=${groupid}"));
    else if(parseInt(${cityid})>0)
        childs= eval(T.A.sendData("getdata.do?action=getcoms&cityid=${cityid}"));
    jQuery("#companys").empty();
    jQuery("#companys").append("<option value='-1'>请选择</option>");
    for(var i=0;i<childs.length;i++){
        var child = childs[i];
        var id = child.value_no;
        var name = child.value_name;
        if(comid&&comid ==id)
            jQuery("#companys").append("<option value='"+id+"' selected>"+name+"</option>");
        else
            jQuery("#companys").append("<option value='"+id+"'>"+name+"</option>");
    }
    //T("#companys").value = comid;
}

/*更新表格内容*/
function updateRow(rowid,name,value){
	//alert(value);
	if(value)
		_cityvipT.UCD(rowid,name,value);
}

_cityvipT.C();
</script>

</body>
</html>
