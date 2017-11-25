<%@ page language="java" contentType="text/html; charset=gb2312"
         pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=gb2312">
    <title>城市管理</title>
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
<div id="cityobj" style="width:100%;height:100%;margin:0px;"></div>
<script language="javascript">
    /*权限*/
    var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
    var subauth=[false,false,false,false,false];
    var ownsubauth=authlist.split(",");
    for(var i=0;i<ownsubauth.length;i++){
        subauth[ownsubauth[i]]=true;
    }
    function getSelData(type){
        var channels = eval(T.A.sendData("getdata.do?action=getchans"));
        return channels;
    }
    var citys = getSelData();
    var _mediaField = [
        {fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false},
        {fieldcnname:"名称",fieldname:"name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
        {fieldcnname:"创建时间",fieldname:"ctime",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false,hide:true},
        {fieldcnname:"厂商平台编号",fieldname:"union_id",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
        {fieldcnname:"厂商平台密钥",fieldname:"ukey",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false}
    ];
    var _addMediaField = [
        {fieldcnname:"编号",fieldname:"id",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",edit:false,issort:false},
        {fieldcnname:"名称",fieldname:"name",fieldvalue:'',inputtype:"text",twidth:"100" ,height:"",issort:false},
        {fieldcnname:"创建时间",fieldname:"ctime",fieldvalue:'',inputtype:"date",twidth:"100" ,height:"",issort:false,hide:true},
        {fieldcnname:"厂商平台编号",fieldname:"union_id",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false},
        {fieldcnname:"厂商平台密钥",fieldname:"ukey",fieldvalue:'',inputtype:"text",twidth:"150" ,height:"",issort:false}
    ];
    var _cityT = new TQTable({
        tabletitle:"城市管理",
        ischeck:false,
        tablename:"city_tables",
        dataUrl:"city.do",
        iscookcol:false,
        //dbuttons:false,
        buttons:getAuthButtons(),
        //searchitem:true,
        param:"action=quickquery",
        tableObj:T("#cityobj"),
        fit:[true,true,true],
        tableitems:_mediaField,
        isoperate:getAuthIsoperateButtons()
    });
    function getAuthButtons(){
        var bts=[];
        if(subauth[1])
            bts.push({dname:"添加城市",icon:"edit_add.png",onpress:function(Obj){
                Twin({Id:"city_add",Title:"添加城市",Width:550,sysfun:function(tObj){
                    Tform({
                        formname: "parking_edit_f",
                        formObj:tObj,
                        recordid:"id",
                        suburl:"city.do?action=create",
                        method:"POST",
                        Coltype:2,
                        formAttr:[{
                            formitems:[{kindname:"",kinditemts:_addMediaField}]
                        }],
                        buttons : [//工具
                            {name: "cancel", dname: "取消", tit:"取消添加",icon:"cancel.gif", onpress:function(){TwinC("city_add");} }
                        ],
                        Callback:
                            function(f,rcd,ret,o){
                                if(ret=="1"){
                                    T.loadTip(1,"添加成功！",2,"");
                                    TwinC("city_add");
                                    _cityT.M();
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
                T.each(_cityT.tc.tableitems,function(o,j){
                    o.fieldvalue = _cityT.GD(id)[j]
                });
                Twin({Id:"city_edit_"+id,Title:"编辑",Width:550,sysfunI:id,sysfun:function(id,tObj){
                    Tform({
                        formname: "city_edit_f",
                        formObj:tObj,
                        recordid:"city_id",
                        suburl:"city.do?action=edit&id="+id,
                        method:"POST",
                        Coltype:2,
                        formAttr:[{
                            formitems:[{kindname:"",kinditemts:_cityT.tc.tableitems}]
                        }],
                        buttons : [//工具
                            {name: "cancel", dname: "取消", tit:"取消编辑",icon:"cancel.gif", onpress:function(){TwinC("city_edit_"+id);} }
                        ],
                        Callback:
                            function(f,rcd,ret,o){
                                if(ret=="1"){
                                    T.loadTip(1,"编辑成功！",2,"");
                                    TwinC("city_edit_"+id);
                                    _cityT.M()
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
                Tconfirm({Title:"确认删除吗",Content:"确认删除吗",OKFn:function(){
                    T.A.sendData("city.do?action=delete","post","id="+id,
                        function deletebackfun(ret){
                            if(ret=="1"){
                                T.loadTip(1,"删除成功！",2,"");
                                _cityT.M()
                            }else{
                                T.loadTip(1,ret,2,"");
                            }
                        }
                    )}})
            }});

        if(subauth[4])
            bts.push({name:"设置",fun:function(id){
                Twin({
                    Id:"client_detail_"+id,
                    Title:"城市设置  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
                    Content:"<iframe src=\"city.do?action=set&id="+id+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
                    Width:T.gww()-100,
                    Height:T.gwh()-50
                })
            }});

        if(bts.length <= 0){return false;}
        return bts;
    }


    _cityT.C();
</script>

</body>
</html>
