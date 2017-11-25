<%@ page language="java" contentType="text/html; charset=gb2312"
         pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=gb2312">
    <title>泊链车场管理</title>
    <link href="css/tq.css" rel="stylesheet" type="text/css">
    <link href="css/iconbuttons.css" rel="stylesheet" type="text/css">

    <script src="js/tq.js?0817" type="text/javascript">//表格</script>
    <script src="js/tq.public.js?0817" type="text/javascript">//表格</script>
    <script src="js/tq.datatable.js?0817" type="text/javascript">//表格</script>
    <script src="js/tq.form.js?033434" type="text/javascript">//表单</script>
    <script src="js/tq.searchform.js?0817" type="text/javascript">//查询表单</script>
    <script src="js/tq.window.js?0817" type="text/javascript">//弹窗</script>
    <script src="js/tq.hash.js?0817" type="text/javascript">//哈希</script>
    <script src="js/tq.stab.js?0817" type="text/javascript">//切换</script>
    <script src="js/tq.validata.js?0817" type="text/javascript">//验证</script>
    <script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
    <script src="js/tq.newtree.js?1014" type="text/javascript"></script>

</head>
<body>
<div id="unionparksobj" style="width:100%;height:100%;margin:0px;"></div>
<script >
    function getMarketers (){
        var markets = eval(T.A.sendData("getdata.do?action=markets"));
        return markets;
    }
    function getBizcircles(){
        var bizs = eval(T.A.sendData("parking.do?action=getbizs"));
        return bizs;
    }
    var role=${role};
    var marketers=getMarketers();
    var bizcircles = getBizcircles();
    var states = [{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"否"},{"value_no":1,"value_name":"是"}]
    var add_states = [{"value_no":0,"value_name":"否"},{"value_no":1,"value_name":"是"}]
    var etc_states=[{"value_no":-1,"value_name":"全部"},{"value_no":0,"value_name":"不支持"},{"value_no":1,"value_name":"Ibeacon"},{"value_no":2,"value_name":"通道照牌"},{"value_no":3,"value_name":"手机照牌"}]
    var etc_add_states=[{"value_no":0,"value_name":"不支持"},{"value_no":1,"value_name":"Ibeacon"},{"value_no":2,"value_name":"通道照牌"},{"value_no":3,"value_name":"手机照牌"},{"value_no":4,"value_name":"Pos机照牌"}]
    var union_id = '${unionId}';
    var server_id = '${serverId}';
    var union_key='${unionKey}'
    var isfixed = false;
    if(role==7)
        isfixed=true;
    var _mediaField = [
        {fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"number", twidth:"80" ,height:"",issort:false,edit:false},
        {fieldcnname:"名称",fieldname:"company_name",fieldvalue:'',inputtype:"text", twidth:"250" ,height:"",issort:false},
        {fieldcnname:"详细地址",fieldname:"address",fieldvalue:'',inputtype:"showmap", twidth:"280" ,height:"",issort:false},
        {fieldcnname:"停车场电话",fieldname:"phone",fieldvalue:'',inputtype:"text", twidth:"160" ,height:"",issort:false},
        {fieldcnname:"创建时间",fieldname:"create_time",fieldvalue:'',inputtype:"date", twidth:"160" ,height:"",hide:true},
        {fieldcnname:"更新时间",fieldname:"update_time",fieldvalue:'',inputtype:"date", twidth:"160" ,height:"",hide:true},
        {fieldcnname:"车位总数",fieldname:"parking_total",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false},
        {fieldcnname:"分享数量",fieldname:"share_number",fieldvalue:'',inputtype:"number", twidth:"100" ,height:"",issort:false}
    ];

    var _unionparksT = new TQTable({
        tabletitle:"选择停车场上传到泊链",
        //ischeck:false,
        tablename:"unionparks_tables",
        dataUrl:"parking.do",
        iscookcol:false,
        //dbuttons:false,
        buttons:getAuthButtons(),
        //searchitem:true,
        isidentifier:false,
        param:"action=uploadpark",
        tableObj:T("#unionparksobj"),
        fit:[true,true,true],
        tableitems:_mediaField,
        checktype:"radio",
        isoperate:getAuthIsoperateButtons()
    });

    function getAuthButtons(){
        var bus = [];
        if(role!=6&&role!=8)
            bus.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
                T.each(_unionparksT.tc.tableitems,function(o,j){
                    o.fieldvalue ="";
                    if(o.fieldname=='strid'||o.fieldname=='nickname'||o.fieldname=='cmobile')
                        o.shide=true;
                });
                Twin({Id:"unionparks_search_w",Title:"搜索停车场",Width:550,sysfun:function(tObj){
                    TSform ({
                        formname: "unionparks_search_f",
                        formObj:tObj,
                        formWinId:"unionparks_search_w",
                        formFunId:tObj,
                        formAttr:[{
                            formitems:[{kindname:"",kinditemts:_mediaField}]
                        }],
                        buttons : [//工具
                            {name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("unionparks_search_w");} }
                        ],
                        SubAction:
                            function(callback,formName){
                                _unionparksT.C({
                                    cpage:1,
                                    tabletitle:"高级搜索结果",
                                    extparam:"&action=uploadpark&"+Serializ(formName)
                                })
                            }
                    });
                }
                })

            }});
        bus.push({ dname:  "上传到泊链平台", icon: "sendsms.gif", onpress:function(Obj){
            var sids = _unionparksT.GS();
            var ids="";
            if(!sids){
                T.loadTip(1,"请先选择车场",2,"");
                return;
            }
            var unionInfo = eval('('+T.A.sendData("parking.do?action=getUnionInfo&park_id="+sids)+')');
            if(unionInfo.union_id==''){
                if(union_id == ''){
                    T.loadTip(1,"请先选设置联盟厂商平台账户",2,"");
                    return;
                }
            }else{
                union_id = unionInfo.union_id;
                server_id = unionInfo.server_id;
                union_key = unionInfo.union_key;
            }
            Twin({Id:"send_message_w",Title:"同步到泊链平台",Width:550,sysfun:function(tObj){
                Tform({
                    formname: "send_message_ff",
                    formObj:tObj,
                    recordid:"id",
                    suburl:"parking.do?action=sendparktounion",
                    method:"POST",
                    Coltype:2,
                    dbuttonname:["确认上传"],
                    formAttr:[{
                        formitems:[{kindname:"",kinditemts:[
                            {fieldcnname:"车场编号",fieldname:"seleids",fieldvalue:sids,inputtype:"multi"},
                            {fieldcnname:"厂商 平台编号",fieldname:"union_id",fieldvalue:union_id,inputtype:"text",edit:false},
                            {fieldcnname:"服务商编号",fieldname:"server_id",fieldvalue:server_id,inputtype:"text",edit:false},
                            {fieldcnname:"签名KEY",fieldname:"union_key",fieldvalue:union_key,inputtype:"text",edit:false}
                        ]}]
                    }],
                    buttons : [//工具
                        {name: "cancel", dname: "取消", tit:"取消同步到泊链",icon:"cancel.gif", onpress:function(){TwinC("send_message_w");} }
                    ],
                    Callback:function(f,rcd,ret,o){
                        if(ret!==''){
                            T.loadTip(1,ret,2,"");
                            TwinC("send_message_w");
                            _unionparksT.M();
                        }else{
                            T.loadTip(1,"同步到泊链失败",2,o);
                        }
                    }
                });
            }
            })

        }})
        bus.push({dname:"返回泊链停车场管理ee",icon:"edit_add.png",onpress:function(Obj){
            location = "parking.do?action=unionparks";
        }});
        return bus;
    }
    function getAuthIsoperateButtons(){
        var bts = [];

        if(bts.length <= 0){return false;}
        return bts;
    }
    _unionparksT.C();
</script>

</body>
</html>
