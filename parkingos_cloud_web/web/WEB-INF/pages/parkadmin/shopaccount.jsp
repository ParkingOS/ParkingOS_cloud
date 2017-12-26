<%@ page language="java" contentType="text/html; charset=gb2312"
         pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=gb2312">
    <title>充值流水</title>
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
<div id="shopaccountobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
    /*权限*/
    var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
    var subauth=[false,false,false,false,false];
    var ownsubauth=authlist.split(",");
    for(var i=0;i<ownsubauth.length;i++){
        subauth[ownsubauth[i]]=true;
    }
    var groupid = "${groupid}";
    var operate_types = [{"value_no":0,"value_name":"全部"},{"value_no":1,"value_name":"续费"},{"value_no":2,"value_name":"回收充值"}];
    //查看
    var _showmediaField = [
        {fieldcnname:"商户编号",fieldname:"shop_id",fieldvalue:'',inputtype:"text", twidth:"100" ,height:"",issort:false,edit:false},
        {fieldcnname:"商户名称",fieldname:"shop_name",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
        {fieldcnname:"额度(分钟)",fieldname:"ticket_limit_minute",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",hide:true},
        {fieldcnname:"额度(小时)",fieldname:"ticket_limit_hour",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",hide:true},
        {fieldcnname:"额度(天)",fieldname:"ticket_limit_day",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",hide:true},
        {fieldcnname:"额度(张)",fieldname:"ticketfree_limit",fieldvalue:'',inputtype:"text",twidth:"200" ,height:"",issort:false},
        {fieldcnname:"额度(元)",fieldname:"ticket_money",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
        {fieldcnname:"缴费金额(元)",fieldname:"add_money",fieldvalue:'',inputtype:"text",twidth:"130" ,height:"",issort:false},
        {fieldcnname:"操作人",fieldname:"operate_name",fieldvalue:'',inputtype:"text", twidth:"150" ,height:"",issort:false},
        {fieldcnname:"操作时间",fieldname:"operate_time",fieldvalue:'',inputtype:"date", twidth:"130" ,height:"",hide:true},
        {fieldcnname:"流水类型",fieldname:"operate_type",fieldvalue:'',inputtype:"select", noList:operate_types,twidth:"130" ,height:"",issort:false},
    ];
    //高级查询
    var _mediaField = [
        {fieldcnname:"商户名称",fieldname:"shop_name",inputtype:"text", twidth:"500",issort:false},
        {fieldcnname:"操作人",fieldname:"operator_name",inputtype:"text", twidth:"500",issort:false},
        {fieldcnname:"操作时间",fieldname:"operate_time",inputtype:"date", twidth:"500",issort:false},
        {fieldcnname:"优惠单位",fieldname:"ticket_unit",inputtype:"select", noList:[{"value_no":1,"value_name":"分钟"},{"value_no":2,"value_name":"小时"},{"value_no":3,"value_name":"天"},{"value_no":4,"value_name":"元"}], twidth:"500",issort:false},
        {fieldcnname:"流水类型",fieldname:"operate_type",inputtype:"select", noList:operate_types,twidth:"130" ,height:"",issort:false},
    ];
    var _shopT = new TQTable({
        tabletitle:"流水查询",
        ischeck:false,
        tablename:"shopaccount_tables",
        dataUrl:"shopaccount.do",
        iscookcol:false,
        //dbuttons:false,
        buttons:getAuthButtons(),
        //searchitem:true,
        //quikcsearch:coutomsearch(),
        param:"action=quickquery",
        tableObj:T("#shopaccountobj"),
        fit:[true,true,true],
        tableitems:_showmediaField,
        //isoperate:getAuthIsoperateButtons()
    });
    function getAuthButtons(){
        var bts = [];
        if(subauth[0]){
            bts.push({dname:"高级查询",icon:"edit_add.png",onpress:function(Obj){
                T.each(_shopT.tc.tableitems,function(o,j){
                    o.fieldvalue ="";
                });
                Twin({Id:"shopaccount_search_w",Title:"流水查询",Width:550,sysfun:function(tObj){
                    TSform ({
                        formname: "shopaccount_search_f",
                        formObj:tObj,
                        formWinId:"shopaccount_search_w",
                        formFunId:tObj,
                        formAttr:[{
                            formitems:[{kindname:"",kinditemts:_mediaField}]
                        }],
                        buttons : [//工具
                            {name: "cancel", dname: "取消", tit:"取消查询",icon:"cancel.gif", onpress:function(){TwinC("shopaccount_search_w");} }
                        ],
                        SubAction:
                            function(callback,formName){
                                _shopT.C({
                                    cpage:1,
                                    tabletitle:"高级搜索结果",
                                    extparam:"&action=query&"+Serializ(formName)
                                })
                                addcoms();
                            }
                    });
                }
                })

            }});
        }
        return bts;
    }
    function coutomsearch(){
        var html = "";
        if(groupid != ""){
            html = "&nbsp;&nbsp;&nbsp;&nbsp;车场:&nbsp;&nbsp;<select id='companys' onchange='searchdata();' ></select>";
        }
        return html;
    }
    function addcoms(){
        if(groupid != ""){
            var childs = eval(T.A.sendData("getdata.do?action=getcoms&groupid=${groupid}"));
            jQuery("#companys").empty();
            for(var i=0;i<childs.length;i++){
                var child = childs[i];
                var id = child.value_no;
                var name = child.value_name;
                jQuery("#companys").append("<option value='"+id+"'>"+name+"</option>");
            }
            T("#companys").value = comid;
        }
    }
    _shopT.C();
</script>

</body>
</html>
