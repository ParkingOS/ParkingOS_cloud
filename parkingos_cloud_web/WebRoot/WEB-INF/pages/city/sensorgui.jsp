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
    <link href="css/metronic/page_plugin/select2.css" rel="stylesheet" type="text/css" />
    <link href="css/metronic/page_plugin/select2-bootstrap.min.css" rel="stylesheet" type="text/css" />
    <!-- END PAGE LEVEL PLUGINS -->
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
                            <%--<h1>车检器监测图形展示
                            </h1>
                        --%>
                        <div class="btn-group btn-group-solid">
                            <a class="btn green">图形</a>
                            <a href="sensorberth.do" class="btn white">列表</a>
                        </div>
                        </div>
                        <!-- END PAGE TITLE -->
                    </div>
                    <!-- END PAGE HEAD-->
                    <!-- BEGIN PAGE BASE CONTENT -->
                    <div class="row">
                        <div class="col-md-12">
                            <div class="row">
                            	<div class="col-lg-2 col-md-4 col-xs-6">
                            		
                            		<select id="parkselect" class="form-control select2">
                                     </select>
                            	</div>
                            	<div class="col-lg-2 col-md-4 col-xs-6">
                            		<select id="berthseg" class="form-control select2">
                                     </select>
                            	</div>
                            	<div class="col-lg-2 col-md-4 col-xs-6">
                            		<select id="sensorstate" class="form-control select2">
                            			<option value="-1">全部</option>
                            			<option value="0">心跳正常</option>
                            			<option value="1">心跳异常</option>
                                     </select>
                            	</div>
                            	<div class="col-lg-2 col-md-4 col-xs-6">
                            		<button onclick="render()" type="submit" class="btn green" data-target="#modal_demo_1" data-toggle="modal">确定</button>
                            	</div>
                            	
                            </div>
                        </div>
                    </div>
                    <div style="margin-top:20px;" class="row">
                    	<div id="line-1" class="col-lg-4 col-sm-6 col-xs-12 my-hide">
                            <div class="portlet light">
                                <div class="portlet-body">
                                    <div class="mt-element-list">
                                        <div class="mt-list-container list-todo opt-2">
                                            <div class="list-todo-line bg-grey-salt"></div>
                                            <ul id="ul-1">
                                                <%--<li class="mt-list-item">
                                                    <div class="list-todo-icon bg-white font-green">
                                                        <i class="fa fa-smile-o"></i>
                                                    </div>
                                                    <div class="list-todo-item item-1">
                                                        <a class="list-toggle-container font-white" data-toggle="collapse" href="#task-1-2" aria-expanded="false">
                                                            
                                                            <div class="list-toggle done uppercase bg-green">
                                                                <div class="list-toggle-title bold">最近心跳：2016-05-16 15:02</div>
                                                                <div class="pull-right font-white bold">心跳正常</div>
                                                            </div>
                                                        </a>
                                                        <div class="task-list panel-collapse collapse in" id="task-1-2">
                                                            <ul>
                                                                <li class="task-list-item done">
                                                                    <div class="task-icon">
                                                                        <a href="javascript:;">
                                                                            <i class="fa fa-dot-circle-o"></i>
                                                                        </a>
                                                                    </div>
                                                                    <div class="task-status">
                                                                        <a class="pending" href="javascript:;">
                                                                            <i class="fa fa-battery-4"></i>
                                                                        </a>
                                                                    </div>
                                                                    <div class="task-content">
                                                                        <h4 class="uppercase bold">
                                                                            <a href="javascript:;">车检器编号：A3454MSK</a>
                                                                        </h4>
                                                                        <p>进场时间：2016-05-16 15:56:34</p>
                                                                    </div>
                                                                </li>
                                                                <li class="task-list-item">
                                                                    <div class="task-icon">
                                                                        <a href="javascript:;">
                                                                            <i class="fa fa-car"></i>
                                                                        </a>
                                                                    </div>
                                                                    <div class="task-content">
                                                                        <h4 class="uppercase bold">
                                                                            <a href="javascript:;">泊位编号：S343545</a>
                                                                        </h4>
                                                                        <p>车牌号：京5M456</p>
                                                                        <p>进场时间：2016-05-16 15:56:34</p>
                                                                    </div>
                                                                </li>
                                                            </ul>
                                                        </div>
                                                    </div>
                                                </li>
                                                <li class="mt-list-item">
                                                    <div class="list-todo-icon bg-white font-red">
                                                        <i class="fa fa-frown-o"></i>
                                                    </div>
                                                    <div class="list-todo-item item-3">
                                                        <a class="list-toggle-container font-white" data-toggle="collapse" href="#task-3-4" aria-expanded="false">
                                                            <div class="list-toggle done uppercase bg-red">
                                                                <div class="list-toggle-title bold">最近心跳：2016-05-16 15:02</div>
                                                                <div class="pull-right font-white bold">心跳异常</div>
                                                            </div>
                                                        </a>
                                                        <div class="task-list panel-collapse collapse in" id="task-3-4">
                                                            <ul>
                                                                <li class="task-list-item done">
                                                                    <div class="task-icon">
                                                                        <a href="javascript:;">
                                                                            <i class="fa fa-dot-circle-o"></i>
                                                                        </a>
                                                                    </div>
                                                                    <div class="task-status">
                                                                        <a class="pending" href="javascript:;">
                                                                            <i class="fa fa-battery-4"></i>
                                                                        </a>
                                                                    </div>
                                                                    <div class="task-content">
                                                                        <h4 class="uppercase bold">
                                                                            <a href="javascript:;">Artwork Slicing</a>
                                                                        </h4>
                                                                        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec elementum gravida mauris, a tincidunt dolor porttitor eu. </p>
                                                                    </div>
                                                                </li>
                                                                <li class="task-list-item">
                                                                    <div class="task-icon">
                                                                        <a href="javascript:;">
                                                                            <i class="fa fa-car"></i>
                                                                        </a>
                                                                    </div>
                                                                    <div class="task-content">
                                                                        <h4 class="uppercase bold">
                                                                            <a href="javascript:;">Backend Integration</a>
                                                                        </h4>
                                                                        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec elementum gravida mauris, a tincidunt dolor porttitor eu. </p>
                                                                    </div>
                                                                </li>
                                                            </ul>
                                                        </div>
                                                    </div>
                                                </li>--%>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div id="line-2" class="col-lg-4 col-sm-6 col-xs-12 my-hide">
                            <div class="portlet light">
                                <div class="portlet-body">
                                    <div class="mt-element-list">
                                        <div class="mt-list-container list-todo opt-2">
                                            <div class="list-todo-line bg-grey-salt"></div>
                                            <ul id="ul-2">
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div id="line-3" class="col-lg-4 col-sm-6 col-xs-12 my-hide">
                            <div class="portlet light">
                                <div class="portlet-body">
                                    <div class="mt-element-list">
                                        <div class="mt-list-container list-todo opt-2">
                                            <div class="list-todo-line bg-grey-salt"></div>
                                            <ul id="ul-3">
                                            </ul>
                                        </div>
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
        <!-- BEGIN PAGE LEVEL PLUGINS -->
        <script src="js/metronic/page_plugin/select2.full.js" type="text/javascript"></script>
        <!-- END PAGE LEVEL PLUGINS -->
        <!-- BEGIN THEME GLOBAL SCRIPTS -->
        <script src="js/metronic/theme_global/app.js" type="text/javascript"></script>
        <!-- END THEME GLOBAL SCRIPTS -->
        <!-- BEGIN PAGE LEVEL SCRIPTS -->
        <script src="js/metronic/page_scripts/components-select2.js" type="text/javascript"></script>
        <!-- END PAGE LEVEL SCRIPTS -->
        <!-- BEGIN THEME LAYOUT SCRIPTS -->
        <script src="js/metronic/theme_layout/layout.js" type="text/javascript"></script>
        <script src="js/metronic/theme_layout/demo.js" type="text/javascript"></script>
        <script src="js/metronic/theme_layout/quick-sidebar.js" type="text/javascript"></script>
        <script src="js/control/sensormonitor.js" type="text/javascript"></script>
        <!-- END THEME LAYOUT SCRIPTS -->
	    <script type="text/javascript">
	    	var parkinfo = '${parkinfo}';
	    	init(parkinfo);
	    </script>
    </body>

</html>