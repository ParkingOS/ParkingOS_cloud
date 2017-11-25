<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!-- 
Template Name: Metronic - Responsive Admin Dashboard Template build with Twitter Bootstrap 3.3.5
Version: 4.5.2
Author: KeenThemes
Website: http://www.keenthemes.com/
Contact: support@keenthemes.com
Follow: www.twitter.com/keenthemes
Like: www.facebook.com/keenthemes
Purchase: http://themeforest.net/item/metronic-responsive-admin-dashboard-template/4021469?ref=keenthemes
License: You must have a valid license purchased only from themeforest(the above link) in order to legally use the theme for your project.
-->
<!--[if IE 8]> <html lang="en" class="ie8 no-js"> <![endif]-->
<!--[if IE 9]> <html lang="en" class="ie9 no-js"> <![endif]-->
<!--[if !IE]><!-->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="en" class="no-js">
  <head>
    <base href="<%=basePath%>">
    <title>停车云管理系统</title>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1" name="viewport" />
    <meta content="" name="description" />
    <meta content="" name="author" />
	
	<!-- BEGIN GLOBAL MANDATORY STYLES -->
   <!--  <link href="http://fonts.googleapis.com/css?family=Open+Sans:400,300,600,700&subset=all" rel="stylesheet" type="text/css" /> -->
    <link href="css/metronic/global_mandatory/font-awesome.css" rel="stylesheet" type="text/css" />
    <link href="css/metronic/global_mandatory/simple-line-icons.css" rel="stylesheet" type="text/css" />
    <link href="css/metronic/global_mandatory/bootstrap.css" rel="stylesheet" type="text/css" />
    <link href="css/metronic/global_mandatory/uniform.default.css" rel="stylesheet" type="text/css" />
    <link href="css/metronic/global_mandatory/bootstrap-switch.css" rel="stylesheet" type="text/css" />
    <!-- END GLOBAL MANDATORY STYLES -->
    <link href="css/metronic/mystyle/mystyle.css" rel="stylesheet" type="text/css" />
    <!-- BEGIN PAGE LEVEL PLUGINS -->
    <link href="css/metronic/page_plugin/daterangepicker.css" rel="stylesheet" type="text/css" />
    <link href="css/metronic/page_plugin/morris.css" rel="stylesheet" type="text/css" />
    <!-- END PAGE LEVEL PLUGINS -->
    <!-- datatable.edittable--BEGIN PAGE LEVEL PLUGINS -->
    <link href="css/metronic/page_plugin/datatables.css" rel="stylesheet" type="text/css" />
    <link href="css/metronic/page_plugin/datatables.bootstrap.css" rel="stylesheet" type="text/css" />
    <link href="css/metronic/page_plugin/bootstrap-datepicker3.css" rel="stylesheet" type="text/css" />
	<!-- datatable.edittable--END PAGE LEVEL PLUGINS -->
    <!-- BEGIN THEME GLOBAL STYLES -->
    <link href="css/metronic/theme_global/components.css" rel="stylesheet" id="style_components" type="text/css" />
    <link href="css/metronic/theme_global/plugins.css" rel="stylesheet" type="text/css" />
    <!-- END THEME GLOBAL STYLES -->
    <!-- BEGIN THEME LAYOUT STYLES -->
    <link href="css/metronic/theme_layout/layout.css" rel="stylesheet" type="text/css" />
    <link href="css/metronic/theme_layout/light.css" rel="stylesheet" type="text/css" id="style_color" />
    <link href="css/metronic/theme_layout/custom.css" rel="stylesheet" type="text/css" />
    <!-- END THEME LAYOUT STYLES -->
    <link rel="shortcut icon" href="favicon.ico" />
  </head>
  
  <body class="page-container-bg-solid page-header-fixed page-sidebar-closed-hide-logo page-sidebar-fixed page-footer-fixed">
        
        <!-- BEGIN CONTAINER -->
        <div class="page-container my-container">
            
            <!-- BEGIN CONTENT -->
            <div class="page-content-wrapper">
                <!-- BEGIN CONTENT BODY -->
                <div class="page-content my-page-content">
                    <!-- BEGIN PAGE HEAD-->
                    <div class="page-head">
                        <!-- BEGIN PAGE TITLE -->
                        <div class="page-title">
                            <h1>控制台</h1>
                        </div>
                        <!-- END PAGE TITLE -->
                    </div>
                    <!-- END PAGE HEAD-->
                    <!-- BEGIN PAGE BASE CONTENT -->
                    <!-- BEGIN DASHBOARD STATS 1-->
                    <div class="row">
                        <div class="col-lg-3 col-md-3 col-sm-6 col-xs-12">
                            <div class="dashboard-stat blue">
                                <div class="visual">
                                    <i class="fa fa-comments"></i>
                                </div>
                                <div class="details">
                                    <div class="number">
                                        <span data-counter="counterup" data-value="${income}"></span><span style="font-size:14px;">&nbsp;元</span>
                                    </div>
                                    <div class="desc"> 今日停车费收入 </div>
                                </div>
                                <a class="more" href="javascript:;"> &nbsp;
                                    <i class=""></i>
                                </a>
                            </div>
                        </div>
                        <div class="col-lg-3 col-md-3 col-sm-6 col-xs-12">
                            <div class="dashboard-stat red">
                                <div class="visual">
                                    <i class="fa fa-bar-chart-o"></i>
                                </div>
                                <div class="details">
                                    <div class="number">
                                        <span data-counter="counterup" data-value="${newvip}">0</span>/<span data-counter="counterup" data-value="${allvip}">0</span></div>
                                    <div class="desc"> 今日新增/总会员 </div>
                                </div>
                                <a class="more" href="javascript:;"> &nbsp;
                                    <i class=""></i>
                                </a>
                            </div>
                        </div>
                        <div class="col-lg-3 col-md-3 col-sm-6 col-xs-12">
                            <div class="dashboard-stat green">
                                <div class="visual">
                                    <i class="fa fa-shopping-cart"></i>
                                </div>
                                <div class="details">
                                    <div class="number">
                                        <span data-counter="counterup" data-value="${fail_device}">0</span>
                                    </div>
                                    <div class="desc"> 设备故障数 </div>
                                </div>
                                <a class="more dropdown-toggle" data-toggle="dropdown" data-hover="dropdown" data-close-others="true" href="#"> 查看详情
                                    <i class="m-icon-swapright m-icon-white "></i>
                                    <a href="" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown" data-close-others="true">
                                            </a>
                                            <ul class="dropdown-menu pull-right" style="margin:-20px 15px 0px 0px;">
                                                <li>
                                                    <a href="citytransmitter.do?authid=${site_authid}&site_state_start=0&from=index"> 基站
                                                        <span class="label label-sm label-default" style="float:right;"> ${failSite} </span>
                                                    </a>
                                                </li>
                                                <li  class="active">
                                                    <a href="citysensor.do?authid=${sensor_authid}&site_state_start=0&from=index"> 车检器
                                                        <span class="label label-sm label-default" style="float:right;"> ${failSensor} </span>
                                                    </a>
                                                </li>
                                                <li>
                                                    <a href="cityinduce.do?authid=${induce_authid}&induce_state_start=1&from=index"> 诱导屏
                                                        <span class="label label-sm label-default" style="float:right;"> ${failInduce} </span>
                                                    </a>
                                                </li>
                                            </ul>
                                </a>
                            
                            </div>
                        </div>
                        <div class="col-lg-3 col-md-3 col-sm-6 col-xs-12">
                            <div class="dashboard-stat purple">
                                <div class="visual">
                                    <i class="fa fa-globe"></i>
                                </div>
                                <div class="details">
                                    <div class="number">
                                        <span data-counter="counterup" data-value="${alertcount}"></span></div>
                                    <div class="desc"> 未处理事件 </div>
                                </div>
                                <a class="more" href="alertevent.do?authid=${alert_authid}&from=index"> 查看详情
                                    <i class="m-icon-swapright m-icon-white"></i>
                                </a>
                            </div>
                        </div>
                    </div>
                    <div class="clearfix"></div>
                    <!-- END DASHBOARD STATS 1-->
                    <div class="row">
                    	<div class="col-md-4">
                            <!-- BEGIN PORTLET-->
                            <div class="portlet light bordered">
                                <div class="portlet-title">
                                    <div class="caption">
                                        <i class="icon-bar-chart font-green"></i>
                                        <span class="caption-subject font-green bold uppercase">利用率</span>
                                    </div>
                                </div>
                                <div class="portlet-body">
                                	<div style="height:303px;">
                                	<div class="row">
                                        <div class="col-md-6">
                                            <div id="echartmain1" class="chart my-gauge-chart"></div>
                                        </div>
                                        <div class="margin-bottom-10 visible-sm"> </div>
                                        <div class="col-md-6">
                                            <div id="echartmain2" class="chart my-gauge-chart"></div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6">
                                            <div id="echartmain3" class="chart my-gauge-chart"></div>
                                        </div>
                                        <div class="margin-bottom-10 visible-sm"> </div>
                                        <div class="col-md-6">
                                            <div id="echartmain4" class="chart my-gauge-chart"></div>
                                        </div>
                                    </div>
                                    </div>
                                </div>
                            </div>
                            <!-- END PORTLET-->
                        </div>
                        <div class="col-md-4 col-sm-4">
                            <div class="portlet light bordered">
                                <div class="portlet-title">
                                    <div class="caption">
                                        <i class=" icon-layers font-green"></i>
                                        <span class="caption-subject font-green bold uppercase">今日停车费收入构成</span>
                                    </div>
                                    <div class="actions">
                                        <a href="feebypark.do?authid=${order_authid}&from=index" class="btn btn-outline btn-circle btn-sm blue">
                                                            <i class="fa fa-share"></i> 详情 </a>
                                    </div>
                                </div>
                                <div class="portlet-body">
                                    <div id="pie_chart_1" style="height:300px;"> </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4 col-sm-2">
			                 <div class="portlet light bordered">
			                     <div class="portlet-title">
			                         <div class="caption font-dark">
			                             <i class="icon-settings font-dark"></i>
			                             <span class="caption-subject bold uppercase">预警管理</span>
			                             <span class="caption-helper">（7日之内）</span>
			                         </div>
			                         <!-- <div class="tools"> </div> -->
			                         <div class="actions">
                                        <a href="citypeakalert.do?authid=${con_authid}&from=index" class="btn btn-outline btn-circle red btn-sm blue">
                                                            <i class="fa fa-share"></i> 详情 </a>
                                    </div>
			                     </div>
			                     <div class="portlet-body table-both-scroll">
			                     	<div style="height:300px;">
				                     	<div id="site_statistics_loading">
	                                        <img src="images/metronic/img/loading.gif" alt="loading" /> 
	                                    </div>
				                         <table class="table table-striped table-bordered table-hover order-column datatable-top-border" id="sample_1">
				                             <tbody>
				                             </tbody>
				                         </table>
			                         </div>
			                     </div>
			                 </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-4 col-sm-4">
                            <div class="portlet light bordered">
                                <div class="portlet-title">
                                    <div class="caption">
                                        <i class="icon-cursor font-purple"></i>
                                        <span class="caption-subject font-purple bold uppercase">设备正常率</span>
                                    </div>
                                    <!-- <div class="actions">
                                        <a href="javascript:;" class="btn btn-sm btn-circle red easy-pie-chart-reload">
                                            <i class="fa fa-repeat"></i> 刷新 </a>
                                    </div> -->
                                </div>
                                <div class="portlet-body">
                                	<div style="height:313px;">
                                    <div style="margin-top:25px;" class="row">
                                        <div class="col-md-4">
                                            <div class="easy-pie-chart">
                                                <div class="number transactions" data-percent="${sensor_rate}">
                                                    <span>${sensor_rate}</span>% </div>
                                                <a class="title" href="javascript:;"> 车检器正常率
                                                </a>
                                            </div>
                                        </div>
                                        <div class="margin-bottom-10 visible-sm"> </div>
                                        <div class="col-md-4">
                                            <div class="easy-pie-chart">
                                                <div class="number visits" data-percent="${camera_rate}">
                                                    <span>${camera_rate}</span>% </div>
                                                <a class="title" href="javascript:;"> 视频头正常率
                                                </a>
                                            </div>
                                        </div>
                                        <div class="margin-bottom-10 visible-sm"> </div>
                                        <div class="col-md-4">
                                            <div class="easy-pie-chart">
                                                <div class="number bounce" data-percent="100">
                                                    <span>100</span>% </div>
                                                <a class="title" href="javascript:;"> POS机正常率
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                    <div style="margin-top:25px;;" class="row">
                                        <div class="col-md-4">
                                            <div class="easy-pie-chart">
                                                <div class="number charging" data-percent="${site_rate}">
                                                    <span>${site_rate}</span>% </div>
                                                <a class="title" href="javascript:;"> 基站正常率
                                                </a>
                                            </div>
                                        </div>
                                        <div class="margin-bottom-10 visible-sm"> </div>
                                        <div class="col-md-4">
                                            <div class="easy-pie-chart">
                                                <div class="number induce" data-percent="${induce_rate}">
                                                    <span>${induce_rate}</span>% </div>
                                                <a class="title" href="javascript:;"> 诱导屏正常率
                                                </a>
                                            </div>
                                        </div>
                                        <div class="margin-bottom-10 visible-sm"> </div>
                                        <div class="col-md-4">
                                            <div class="easy-pie-chart">
                                                <div class="number lock" data-percent="100">
                                                    <span>100</span>% </div>
                                                <a class="title" href="javascript:;"> 地锁正常率
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-8">
                            <div class="portlet light bordered">
                            	<div class="portlet-title">
                                    <div class="caption">
                                        <i class="icon-equalizer font-yellow"></i>
                                        <span class="caption-subject font-yellow bold uppercase">在线数</span>
                                    </div>
                                </div>
                                <div class="portlet-body">
                                	<div style="height:337px;">
                                    	<div id="echartmain5" style="height:337px;"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- END PAGE BASE CONTENT -->
                </div>
                <!-- END CONTENT BODY -->
            </div>
            <!-- END CONTENT -->
        </div>
        <!-- END CONTAINER -->
        <!--[if lt IE 9]>
		<script src="../assets/global/plugins/respond.min.js"></script>
		<script src="../assets/global/plugins/excanvas.min.js"></script> 
		<![endif]-->
        <!-- BEGIN CORE PLUGINS -->
         
        <script src="js/metronic/core_plugins/jquery.min.js" type="text/javascript"></script>
        <script src="js/metronic/core_plugins/bootstrap.js" type="text/javascript"></script>
        <script src="js/metronic/core_plugins/js.cookie.min.js" type="text/javascript"></script>
        <script src="js/metronic/core_plugins/bootstrap-hover-dropdown.js" type="text/javascript"></script>
        <script src="js/metronic/core_plugins/jquery.slimscroll.js" type="text/javascript"></script>
        <script src="js/metronic/core_plugins/jquery.blockui.min.js" type="text/javascript"></script>
        <script src="js/metronic/core_plugins/jquery.uniform.js" type="text/javascript"></script>
        <script src="js/metronic/core_plugins/bootstrap-switch.js" type="text/javascript"></script>
        <!-- END CORE PLUGINS -->
        <!-- datatable.ajaxtable--BEGIN PAGE LEVEL PLUGINS -->
	    <script src="js/metronic/page_plugin/datatable.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/datatables.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/datatables.bootstrap.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/bootstrap-datepicker.js" type="text/javascript"></script>
	    <script src="js/metronic/page_scripts/alert.js" type="text/javascript"></script>
	    <!-- datatable.ajaxtable--END PAGE LEVEL PLUGINS -->
        <!-- BEGIN PAGE LEVEL PLUGINS -->
	    <script src="js/metronic/page_plugin/moment.min.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/daterangepicker.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/morris.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/raphael-min.js" type="text/javascript"></script>
	    <!-- BEGIN-最上层控制js -->
	    <script src="js/metronic/page_plugin/jquery.counterup.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/jquery.waypoints.min.js" type="text/javascript"></script>
	    <!-- END-最上层控制js -->
	    <script src="js/metronic/page_plugin/amcharts.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/serial.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/pie.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/radar.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/light.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/patterns.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/chalk.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/ammap.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/worldLow.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/amstock.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/fullcalendar.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/jquery.easypiechart.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/jquery.sparkline.min.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/jquery.flot.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/jquery.flot.resize.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/jquery.flot.categories.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/jquery.flot.pie.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/jquery.flot.stack.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/jquery.flot.crosshair.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/echarts.js" type="text/javascript"></script>
	    
	    <!-- END PAGE LEVEL PLUGINS -->
        <!-- BEGIN THEME GLOBAL SCRIPTS -->
        <script src="js/metronic/theme_global/app.js" type="text/javascript"></script>
        <!-- END THEME GLOBAL SCRIPTS -->
        <!-- BEGIN PAGE LEVEL SCRIPTS -->
        <script src="js/control/income.js?v=1" type="text/javascript"></script>
        <script src="js/metronic/page_scripts/dashboard.js" type="text/javascript"></script>
        
        <!-- BEGIN THEME LAYOUT SCRIPTS -->
        <script src="js/metronic/theme_layout/layout.js" type="text/javascript"></script>
        <script src="js/metronic/theme_layout/demo.js" type="text/javascript"></script>
        <script src="js/metronic/theme_layout/quick-sidebar.js" type="text/javascript"></script>
        <!-- END THEME LAYOUT SCRIPTS -->
        <script src="js/echarts/echarts.js"></script>
         <script type="text/javascript">
	       var pospercent=${percent};
	       var parkturn=${parkturn};
	       var escaperate=${eacaperate};
	    </script>
        <script src="js/control/myEchart.js?1"></script>
        <script src="js/control/online.js"></script>
	    <script type="text/javascript">
	    	$("#option2").bind("onclick",function(){
	    		window.location.href = "citymember.do?authid=${con_authid}";
	    	});
	    	initUtili('${berth_rate}');
    		loaddata();
    		
    		jQuery(document).ready(function() {//饼形图
   			   ChartsFlotcharts.init();
   			   ChartsFlotcharts.initPieCharts('${income_struct}');
    		});
    		
	    </script>
    </body>

</html>