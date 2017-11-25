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
    <link href="css/metronic/page_plugin/bootstrap-datepicker3.css" rel="stylesheet" type="text/css" />
     <link href="css/metronic/page_plugin/bootstrap.touchspin.css" rel="stylesheet" type="text/css" />
    <!-- END PAGE LEVEL PLUGINS -->
    <!-- BEGIN THEME GLOBAL STYLES -->
    <link href="css/metronic/theme_global/components.css" rel="stylesheet" id="style_components" type="text/css" />
    <link href="css/metronic/theme_global/plugins.css" rel="stylesheet" type="text/css" />
    <!-- END THEME GLOBAL STYLES -->
    <!-- BEGIN PAGE LEVEL STYLES -->
    <link href="css/metronic/page_style/todo-2.css" rel="stylesheet" type="text/css" />
    <!-- END PAGE LEVEL STYLES -->
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
                            <h1 class="my-title">视频轮巡
                                <small></small>
                            </h1>
                        </div>
                        <!-- END PAGE TITLE -->
                    </div>
                    <!-- END PAGE HEAD-->
                    <!-- BEGIN PAGE BASE CONTENT -->
                    <div class="row">
                        <div class="col-md-12">
                            <!-- BEGIN TODO SIDEBAR -->
                            <div class="todo-ui">
                                <div class="todo-sidebar">
                                    <div class="portlet light bordered">
                                        <div class="portlet-title">
                                            <div class="caption my-caption" data-toggle="collapse" data-target=".todo-project-list-content">
                                                <span class="caption-subject font-green-sharp bold uppercase">轮巡间隔 </span>
                                                <span class="caption-helper">/秒</span>
                                            </div>
                                        </div>
                                        <div class="portlet-body todo-project-list-content">
                                            <div class="todo-project-list">
                                                <ul class="nav nav-stacked">
                                                    <li>
                                                        <div>
                                                    		<input id="touchspin_11" type="text" value="20" name="demo4_2"> 
                                                    	</div>
                                                    </li>
                                                    <li style="margin-top:10px;text-align:center;">
                                                        <div>
                                                    		<button type="button" id="btnSet" class="btn btn-outline dark btn-outline my-button">设定</button>
                                                    		<button type="button" id="btnPlayPause" class="btn btn-outline red btn-outline my-button">暂停</button>
                                                    	</div>
                                                    </li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="portlet light bordered">
                                        <div class="portlet-title">
                                            <div class="caption" data-toggle="collapse" data-target=".todo-project-list-content-tags">
                                                <span class="caption-subject font-red bold uppercase">视频列表 </span>
                                            </div>
                                        </div>
                                        <div class="portlet-body todo-project-list-content todo-project-list-content-tags">
                                        	<div id="site_statistics_loading">
		                                        <img src="images/metronic/img/loading.gif" alt="loading" /> 
		                                    </div>
                                            <div class="todo-project-list">
                                                <ul class="nav nav-pills nav-stacked divVideoList">
                                                    
                                                </ul>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <!-- END TODO SIDEBAR -->
                                <!-- BEGIN TODO CONTENT -->
                                <div class="todo-content">
                                    <div class="portlet light bordered">
                                        <!-- PROJECT HEAD -->
                                        <div class="portlet-title">
                                            <div class="caption">
                                                <i class="icon-bar-chart font-green-sharp hide"></i>
                                                <span class="caption-subject font-blue-sharp bold uppercase">视频展示</span>
                                            </div>
                                        </div>
                                        <!-- end PROJECT HEAD -->
                                        <div class="portlet-body">
                                            <div class="row">
                                                <div class="col-md-12">
                                                    <div>
                                                        	<object id="DPSDK_OCX" classid="CLSID:D3E383B6-765D-448D-9476-DFD8B499926D" style="width: 100%; height: 100%;" codebase="DpsdkOcx.cab#version=1.0.0.0"></object>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <!-- END TODO CONTENT -->
                            </div>
                        </div>
                        <!-- END PAGE CONTENT-->
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
	    <script src="js/metronic/page_plugin/bootstrap-datepicker.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/select2.full.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/spinner.js" type="text/javascript"></script>
	    <script src="js/metronic/page_plugin/bootstrap.touchspin.js" type="text/javascript"></script>
	    <!-- END PAGE LEVEL PLUGINS -->
        <!-- BEGIN THEME GLOBAL SCRIPTS -->
        <script src="js/metronic/theme_global/app.js" type="text/javascript"></script>
        <!-- END THEME GLOBAL SCRIPTS -->
        <!-- BEGIN PAGE LEVEL SCRIPTS -->
        <script src="js/metronic/page_scripts/todo-2.js" type="text/javascript"></script>
        <script src="js/metronic/page_scripts/components-bootstrap-touchspin.js" type="text/javascript"></script>
        <!-- BEGIN THEME LAYOUT SCRIPTS -->
        <script src="js/metronic/theme_layout/layout.js" type="text/javascript"></script>
        <script src="js/metronic/theme_layout/demo.js" type="text/javascript"></script>
        <script src="js/metronic/theme_layout/quick-sidebar.js" type="text/javascript"></script>
        <!-- END THEME LAYOUT SCRIPTS -->
        <script src="js/videoctrl.js" type="text/javascript"></script>
	    <script type="text/javascript">
	    	var playList;
	    	jQuery.ajax({
				type : "post",
				url : "videosurvillance.do",
				data : {
					'action' : 'getvideo',
					'lon' : '119.377906',
					'lat' : '32.386512',
					'r' :Math.random()
				},
				async : false,
				success : function(result) {
					$("#site_statistics_loading").hide();
					playList=$.parseJSON(result);
					initplay();
				}
			});
	        /* var playList = { "playlist": [
	                            { "name": "西部枢纽1", "channelID": "1000002$1$0$0" },
	                            { "name": "西部枢纽2", "channelID": "1000022$1$0$0" },
	                            { "name": "西部枢纽3", "channelID": "1000023$1$0$0" },
	                            { "name": "西部枢纽4", "channelID": "1000003$1$0$0" },
	                            { "name": "西部枢纽5", "channelID": "1000025$1$0$0" },
	                            { "name": "西部枢纽6", "channelID": "1000026$1$0$0" },
	                            { "name": "西部枢纽7", "channelID": "1000002$1$0$0" },
	                            { "name": "西部枢纽8", "channelID": "1000028$1$0$0" },
	                            { "name": "西部枢纽9", "channelID": "1000029$1$0$0"}]
	        }; */
	        var isTurniing = true;
	        var clockID = -1;
	        var curPoint = 0;
	        var curTurningTime = 60;
	        
	        function initplay(){
	        	fillVideoList();
	            var result = loadControl("DPSDK_OCX", "172.16.210.4", "9000", "system", "123456", 4);
	            if (result != 0) alert('视频暂时不能接入');
	            $('#btnSet').click(setTuringTime);
	            $('#btnPlayPause').click(togglePlay);
	            curTurningTime = document.getElementById("touchspin_11").value;
	            
	            turnVideo();
	            startTurning();
	        }
	        function togglePlay() {
	            if (isTurniing) {
	                pauseTuring();
	                $('#btnPlayPause').val("播放");
	            }
	            else {
	                startTurning();
	                $('#btnPlayPause').val("暂停");
	            }
	        }
	        function startTurning() {
	            isTurniing = true;
	            clockID= setInterval(turnVideo, curTurningTime*1000);
	        }
	        function pauseTuring() {
	            clearInterval(clockID);
	            isTurniing = false;
	        }
	        function setTuringTime() {
	            curTurningTime = document.getElementById("touchspin_11").value;
	            if (isTurniing) {//如果正在轮巡，则取消，再开始一个新的。如果没有在轮巡，则不需要改。下次点启动时，自动应用新的时间
	                pauseTuring();
	                startTurning();
	            }
	        }
	        //播放下一组视频
	        function turnVideo() {
	            var shortList = [];
	            for (var i = 0; i < curGridCount; i++) {
	                shortList[i] = playList.playlist[curPoint].channelID;
	                curPoint = (curPoint + 1) % playList.playlist.length;
	            }
	            //alert(shortList);
	            playVideos("DPSDK_OCX", shortList);
	        }
	        function playOneVideo(channelID, i) {
	            //alert(channelID);
	            $(".divVideoList li").removeClass("active");
	            $(".channel-"+i).addClass("active");
	            playVideo("DPSDK_OCX", channelID);
	        }
	        function fillVideoList() {
	            var htmlstr = "";
	            var i = 0;
	            $.each(playList.playlist, function (index, ele) {
	            	i++;
	            	var channelclass = "channel-"+i;
	            	htmlstr += "<li class=\"" + channelclass + "\" onclick='playOneVideo(\"" + ele.channelID + "\","+i+")'><a href='javascript:;'> " + ele.name + " </a></li>";
	            });
	            $('.divVideoList').html(htmlstr);
	        }
    </script>
    </body>

</html>