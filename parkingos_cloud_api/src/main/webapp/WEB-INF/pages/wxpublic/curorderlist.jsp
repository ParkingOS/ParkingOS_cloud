<%@ page language="java" contentType="text/html; charset=gbk"
         pageEncoding="gbk"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=1">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta http-equiv="x-ua-compatible" content="IE=edge">
    <meta name="renderer" content="webkit">
    <title>停车缴费</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/list.css?v=6" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/weui.min.css">
    <link href="https://cdn.bootcss.com/jquery-weui/1.0.0-rc.1/css/jquery-weui.min.css" rel="stylesheet">
    <style type="text/css">
        #scroller li {
            padding:0 10px;
            height:80px;
            line-height:40px;
            background-color:#FFFFFF;
            font-size:14px;
            margin-top:1px;
        }

        .right2{
            float:right;
            margin-right:30px;

        }
        .left{
            float:left;
        //margin-right:10px;
        }

        .hide{
            display:none;
        }

        a{
            text-decoration:none;
            color:#5F5F5F;
        }

        #header {
            position:absolute; z-index:2;
            top:0; left:0;
            width:100%;
            height:45px;
            line-height:45px;
            background-color:#F3F3F3;
            padding:0;
            font-size:20px;
            text-align:center;
        }

        .red{
            color:red;
        }
        .wx_pay{
            border-radius:15px;
        //margin-left:2%;
        //height:20px;
        //margin-top:5%;
            font-size:14px;
            background-color:#04BE02;
            color:white;
        }
        .wx_checkbox{
            background-color: #FFF;
            border: 1px solid #C1CACA;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05), inset 0px -15px 10px -12px rgba(0, 0, 0, 0.05);
            border-radius: 5px;
            vertical-align:text-bottom; margin-bottom:1px;
        }
        .passli {
            background-image: url(${pageContext.request.contextPath}/resources/images/wxpublic/arrow.png);
            background-size: 19px 39px;
            background-repeat: no-repeat;
            background-position: right center;
        }
        .middle1 {
            margin-top: 45%;
            color: gray;
            position: relative;
            z-index: 99;
            text-align: center;
            font-size: 17px;
        }

        .hide1{
            display:none;
        }
        .weui-btn:after {
            content: " ";
            width: 200%;
            height: 200%;
            position: absolute;
            top: 0;
            left: 0;
            /* border: 1px solid rgba(0,0,0,.2); */
            -webkit-transform: scale(.5);
            transform: scale(.5);
            -webkit-transform-origin: 0 0;
            transform-origin: 0 0;
            box-sizing: border-box;
            border-radius: 10px;
        }
        .weui-btn:active{
            top:1px;
        }
    </style>

</head>
<body>

<div id="wrapper" style="margin-top:-45px;">
    <div id="scroller">
        <div id="pullDown" class="idle">
            <span class="pullDownIcon"></span>
            <span class="pullDownLabel">下拉刷新...</span>
        </div>

        <div class="middle1 hide1">
            <div style="margin-bottom: 15px"><i class="weui-icon-info weui-icon_msg"></i></div>
            暂无订单
            <a href="tocarnumbers?uin=${uin}" class="weui-btn weui-btn_default" style="display:none;box-shadow: 2px 2px 4px  rgba(0,0,0,0.5);background: url(${pageContext.request.contextPath}/resources/images/wxpublic/carnumber1.png) no-repeat 10px 3px;width: 45%;margin-top: 20px"><span style="margin-left: 39px;">我的车牌</span></a>
        </div>
        <ul id="thelist">
            <!--
            <div class="weui-form-preview">
                  <div class="weui-form-preview__hd">
                    <label class="weui-form-preview__label"><span style="font-size:20px;color:black">京T12312</span></label>
                    <em class="weui-form-preview__value"><span style="font-size:20px;">￥240.00</span></em>
                  </div>
                  <div class="weui-form-preview__bd">
                    <div class="weui-form-preview__item">
                      <label class="weui-form-preview__label">入场时间</label>
                      <span class="weui-form-preview__value">2017-05-12 09:37</span>
                    </div>
                    <div class="weui-form-preview__item">
                      <label class="weui-form-preview__label">锁定状态</label>
                      <span class="weui-form-preview__value"><span style="color:red">已锁定</span>&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    </div>
                  </div>
                  <div class="weui-form-preview__ft">
                    <button class="weui-form-preview__btn weui-form-preview__btn_primary">锁定车辆</button>
                    <button class="weui-form-preview__btn weui-form-preview__btn_primary">支付订单</button>
                  </div>
                </div>
                <br/>
                 -->
        </ul>

        <div id="pullUp" class="idle">
            <span class="pullUpIcon"></span>
            <span class="pullUpLabel">上拉加载更多...</span>
        </div>
    </div>
    <div class="weui-cells" style="position:absolute;bottom: 0px;width:100%;display: block">
        <a class="weui-cell weui-cell_access" href="tocarnumbers?uin=${uin}">
            <div class="weui-cell__hd"><img src="${pageContext.request.contextPath}/resources/images/wxpublic/carnumber1.png" alt="" style="width:20px;margin-right:5px;display:block"></div>
            <div class="weui-cell__bd">
                <p>我的车牌</p>
            </div>
            <div class="weui-cell__ft"></div>
        </a>
    </div>

</div>
<input id="mobile" type="text" style="display:none;" value="${mobile}"/>
<input id="openid" type="text" style="display:none;" value="${openid}"/>
<input id="domain" type="text" style="display:none;" value="${domain}"/>
<input id="uin" type="text" style="display:none;" value="${uin}"/>
<script src="${pageContext.request.contextPath}/resources/js/iscroll.js"></script>
<script src="${pageContext.request.contextPath}/resources/js/curorderlist.js?v=5"></script>
<script src="${pageContext.request.contextPath}/resources/js/jquery.js"></script>
<script src="${pageContext.request.contextPath}/resources/js/jquery-weui.min.js"></script>
</body>
</html>
