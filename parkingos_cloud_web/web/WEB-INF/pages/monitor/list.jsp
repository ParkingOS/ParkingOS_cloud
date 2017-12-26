<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <c:set var="ctx" value="${pageContext.request.contextPath }"/>
<!doctype html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <meta http-equiv="X-UA-Compatible" centent="IE=edge,chrome=1">
		<title>中央监控</title>
		
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
    <style type="text/css">
        .item-box-left{ 
            display: inline-block;      
            width: 280px;
            height: 100%;
            position:relative;
            left:5px;
        }
		.item-box-left video{
		    width: 100%;
		    height: 25%;
		    display: block;
		}
		.item-box-left-div{
			display: inline-block;
			position: absolute;
			background:transparent;
			border:1px;
			border-color:#e9ecf3;
		}
		.item-box-left-button{
			z-index: 10px;
			background:transparent;
			border: 0px;
			border-color:#e9ecf3; 
			width:0px;important;
			height:0px;important;
		}
        .item-box-center{
           display: inline-block;
           width: 900px;
           height: 100%;
           position: absolute;
           padding-left: 10px
        }
        .item-box-park-div{
			display: inline-block;
			position: absolute;
			background:transparent;
			top:53px;
			right:15%;
			margin-top: -40px;
			
		}
		.item-box-channel-div{
			display: inline-block;
			position: absolute;
			background:transparent;
			top:53px;
			right:1%;
			margin-top: -40px;
			
		}
        .item-box-center-div{
			display: inline-block;
			position: absolute;
			background:transparent;
			top:80%;
			right:5%;
			margin-top: -40px;
			
		}
        .item-box-center-button{
			width:80px;
			height:40px;
			z-index: 10px;
			background: url("images/wxpublic/success.gif") fixed center center no-repeat;
			background-size: cover;
 			width: 100%;
 			border-radius:50%;
		}
		.berthDiv{
			border:0px solid #e9ecf3;
			position: absolute;
			margin-top: 10px;
		}
		.eventDiv{
			border:0px solid #e9ecf3;
			position: absolute;
			margin-top: 10px;
		}
        .item-box-center-button-number{
            color:green;
            text-align:center;
			margin-left:auto;
			margin-right:auto;
			display:block;
			line-height:90px; 
			font-size: 20px;
        }
        .item-box-right{
           display: inline-block;
           width: 440px;
           height: 100%;
           padding-left: 10px;
           right:1px;
           position: absolute;
        }
        .electradeDiv{
			border:0px solid #e1f8f5;
			
		}
		.left{
			width:300px;
			min-height:100px;
			margin:1px auto;
			float:left;
			padding-left: 5px;
		}
		.center{
			 margin:5px auto;
		 	 width:900px; 
		 	 height:129px; 
		 }
		 .leftSelect{
			width:300px;
			min-height:100px;
			margin:1px auto;
			float:left;
		}
		.leftSelect{
			float: left;
			padding: 10px;
		}
		.centerSelect{
			 margin:5px auto;
			 width: 900px; 
		 	 height:162px; 
		 	 display:table-cell;
		 	 vertical-align:middle;
		 	  width: auto; 
			 padding-left: 5px;
		 }
		 .event_table{
		 	 width: 98%;
		 }
		 .event_td1{
		 	 width: 50%;
		 }
		 .event_td2{
		 	 width: 25%;
		 }
		 .event_td3{
		 	 width: 25%;
		 	 padding:5px 0;
   			 text-align: center;
		 }
		 
		 .event_td4{
		 	 width: 30%;
		 	 padding:3px 0;
		 }
		 
		 .event_td5{
		 	 width: 50%;
		 }
		 
		 .btn{
		 	width: 86%;
		    margin: 5px 10px;
		    background: #422bdc;
		    color: #fff;
		    border: none;
		    padding: 5px;
		 }
		 #orderTab{
		 	width: 129%;
		 }
		 
		 .monitorManagerDiv{
		 	padding:10px;
		 	background-color:white;
		 	color:orange;
			width:1000px;
			height:600px;
			display:block; 
			position: absolute;
			top:50%;
			left:50%;
			margin-left:-500px;
			margin-top:-300px;
		 }
		 .portlet {
		    margin-top: 0px;
		    margin-bottom: 0px;!important;
		    padding: 0px;
		    -webkit-border-radius: 4px;
		    -moz-border-radius: 4px;
		    -ms-border-radius: 4px;
		    -o-border-radius: 4px;
		    border-radius: 4px;
		 }
		.btn {
		    display: inline-block;
		    margin-bottom: 0;
		    font-weight: normal;
		    text-align: center;
		    vertical-align: middle;
		    touch-action: manipulation;
		    cursor: pointer;
		    background-image: none;
		    border: 1px solid transparent;!important;
		    white-space: nowrap;
		    padding: 6px 12px;
		    font-size: 14px;
		    line-height: 1.42857;
		    border-radius: 4px;
		    -webkit-user-select: none;
		    -moz-user-select: none;
		    -ms-user-select: none;
		    user-select: none;
		}
		.page-head {
			background-color: #393a3e;
		    /*background-color: #3598dc;*/
		}
		.page-left{
			width:18%;
			float: left;
			position: relative;
		    min-height: 1px;
		    padding-left: 0px;
		    padding-right: 0px;
		    border-radius: 0 !important;
		    margin-left: 1.25%;
		}
		.page-center{
			width:52%;
			float: left;
			position: relative;
		    min-height: 1px;
		    padding-left: 0px;
		    padding-right: 0px;
		    border-radius: 0 !important;
		    margin-left: 1.25%;
		}
		.page-right{
			width:25%;
			float: left;
			position: relative;
		    min-height: 1px;
		    padding-left: 0px;
		    padding-right: 0px;
		    border-radius: 0 !important;
		    margin-left: 1.25%;
		}
		
		.appOrderTotalFont{
			height:50%;
			width:8%;
			margin-left:2%;
			margin-top: 5%;
			text-align: center;
			font-size: 35px;
			color:black;
			float: left;
			display:table;
		}
		.appOrderCountFont{
			width:11.5%;
			height:50%;
			margin-left:10%;
			margin-top: 5%;
			background-color: #393a3e;
			text-align: center;
			font-size: 35px;
			color:white;
			float: left;
			display:table;
		}
		.appOrderFont{
			width:11.5%;
			height:50%;
			margin-left:2%;
			margin-top: 5%;
			background-color: #393a3e;
			text-align: center;
			font-size: 35px;
			color:white;
			float: left;
			display:table; 
		}
		.centerWrap {    
          display:table;    
        }    
        .bottomContent {    
          vertical-align:bottom;    
          display:table-cell;    
       }    
	   .centerContent {    
          vertical-align:middle;    
          display:table-cell;    
       } 
		/*重写属性*/
		body{
			background-color: #393a3e;
		}
		.col-md-12, .col-lg-12 {
		    position: relative;
		    min-height: 1px;
		    padding-left: 0px;!important;
		    padding-right: 0px;!important;
		}
		h1{
			font-size: 30px;!important;
			margin-top: 10px;!important;
		}
		#monitorDiv {
			padding:0px 20px 0px 20px;!important;
		}
		#monitorTitleDiv {
			margin-bottom: 0px;!important;
		}
		#tradeCountMainDiv{
			margin-top:10px;
		}
		:-webkit-full-screen {
		    background-color: #393a3e;!important;
		    z-index: 2147483647;
		}
		#eventContent{
		}
		#eventContent>.modal-header {
		    padding: 10px;!important;
		    border-bottom: 1px solid #FB6E17;!important;
		}
		#eventContent>.modal-content {
		     border: 0px solid #999; */!important;
		     border: 0px solid rgba(0, 0, 0, 0.2);!important;
		}
		#eventContent>.modal-body{
		    padding: 0px 0px 0px 0px;!important;
		}
		#matDiv{
			padding: 0px;!important;
		}
		#matDiv>.col-lg-6{
			padding: 0px;!important;
		}
		#matFailDiv{
			padding: 0px;!important;
		}
		#matFailDiv>.col-lg-6{
			padding: 0px;!important;
		}
    </style>
    <body style="min-height: 660px">
    	<link href="css/loading/style.css" type="text/css" rel="stylesheet" />
    	<script src="js/jquery.js" type="text/javascript"></script>	
    	<script src="js/monitor/test.js" type="text/javascript"></script>
    	<script src="js/monitor/alert.js" type="text/javascript"></script>
    	<script type='text/javascript' src='${ctx}/dwr/engine.js'></script>
		<script type='text/javascript' src='${ctx}/dwr/util.js'></script>
		<script type="text/javascript" src="${ctx}/dwr/interface/Push.js"></script>
		 
    	
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
		<%--<script src="https://open.ys7.com/sdk/js/1.1/ezuikit.js"></script>--%>
    	<c:set value="${pageContext.request.contextPath}" var="path" scope="page"/>
		<script type="text/javascript">
		    var path = "${path}";
		</script>
		 <!-- BEGIN CONTAINER -->
        <div class="page-container my-container">
            
            <!-- BEGIN CONTENT -->
            <div class="page-content-wrapper">
                <!-- BEGIN CONTENT BODY -->
                <div class="page-content my-page-content">
                    <!-- BEGIN PAGE HEAD-->
                    <div class="page-head">
                        <!-- BEGIN PAGE TITLE -->
                        <div style="text-align:center;">
                        	<div style="overflow:visible;float:right;display:none">
				                <button class="btn btn-outline btn-circle btn-sm blue" onclick="exitFullScreen()">
				                         	退出全屏
				                </button> 
				            </div>
                            <div style="overflow:visible;float:right;display:none">
	                            <button class="btn btn-outline btn-circle btn-sm blue" onclick="fullScreen()">
				                         	全屏
				                </button>
			                </div>
			                <h1 style="color: white;">无人化自助停车系统中控室</h1>
                        </div>
                        <!-- END PAGE TITLE -->
                    </div>
                    <!-- END PAGE HEAD-->
                    <!-- BEGIN PAGE BASE CONTENT -->
                    <!-- BEGIN DASHBOARD STATS 1-->
                    <div class="row">
                        <div class="page-left">
                        	<div class="monitorStyleDiv">
			                	<div class="portlet light bordered" id="monitorDiv">
						               <div class="portlet-title" id="monitorTitleDiv">
						                   <div class="caption font-dark">
						                          <span class="caption-subject font-green bold uppercase">监控列表</span>
						                          <!--<span class="caption-helper">（7日之内）</span>-->
						                   </div>
						                   <div class="tools"> </div>
						                   <div class="actions">
			                                      <%-- <a onclick="managerMonitors()" class="btn btn-outline btn-circle btn-sm blue">
			                                          <i class="fa fa-share"></i> 详情 </a>--%>
			                                      <button class="btn btn-outline btn-circle btn-sm blue" data-toggle="modal" data-target="#myModal">
			                                          <i class="fa fa-share"></i>详情
			                                      </button>  
			                                </div>
						                </div>
	                       	 	</div>
		                	</div>
		                	<div>
		                		<div class="item-box-left-div">
		                			<button class="item-box-left-button" onclick="reloadCenterVideo('${videos.video1}','${videos.monitor1}')"></button>
		                		</div>
		                		<div id="sVideo1" style="margin-top: 10px;border: 1px;">
			                		<video id="myPlayer1" poster="" controls="controls"  autoplay height="157.5" width="280">
							           <source src="${videos.video1}" type=""/> 
							            <!-- <source src="http://hls.open.ys7.com/openlive/50ab2363093040589f4ceab4abf4dbed.hd.m3u8" type="application/x-mpegURL" /> -->
							
							            <!--<source src="http://hls.open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.m3u8" type="application/x-mpegURL" />--> 
							           
							        </video>
						        </div>
		                	</div>
		                	<div>
		                		<div class="item-box-left-div">
		                			<button class="item-box-left-button" onclick="reloadCenterVideo('${videos.video2}','${videos.monitor2}')"></button>
		                		</div>
		                		<div id="sVideo2" style="margin-top: 10px;border: 1px;">
			                		<video id="myPlayer2" poster="" controls="controls"  autoplay height="157.5px" width="280px">
							            <%--<source src="htmls/525D015CABC3D132FD02EF4A00E89615.mp4" type=""/> --%>
							            <source src="${videos.video2}" type=""/>
							            <!--<source src="http://hls.open.ys7.com/openlive/50ab2363093040589f4ceab4abf4dbed.hd.m3u8" type="application/x-mpegURL" />-->
							
							            <!--<source src="http://hls.open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.m3u8" type="application/x-mpegURL" />-->
							           
							        </video>
						        </div>
		                	</div>
		                	<div>
		                		<div class="item-box-left-div">
		                			<button class="item-box-left-button" onclick="reloadCenterVideo('${videos.video3}','${videos.monitor3}')"></button>
		                		</div>
		                		<div id="sVideo3" style="margin-top: 10px;border: 1px;">
			                		<video id="myPlayer3" poster="" controls="controls"  autoplay height="157.5px" width="280px">
							            <source src="${videos.video3}" type=""/> 
							            <!--<source src="http://hls.open.ys7.com/openlive/50ab2363093040589f4ceab4abf4dbed.hd.m3u8" type="application/x-mpegURL" />-->
							
							            <!--<source src="http://hls.open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.m3u8" type="application/x-mpegURL" />-->
							           
							        </video>
						        </div>
		                	</div>
		                	<div>
		                		<div class="item-box-left-div">
		                			<button class="item-box-left-button" onclick="reloadCenterVideo('${videos.video4}','${videos.monitor4}')"></button>
		                		</div>
		                		<div id="sVideo4" style="margin-top: 10px;border: 1px;">
			                		<video id="myPlayer4" poster="" controls="controls"  autoplay height="157.5px" width="280px">
							            <source src="${videos.video4}" type=""/> 
							            <!--<source src="http://hls.open.ys7.com/openlive/50ab2363093040589f4ceab4abf4dbeid.hd.m3u8" type="application/x-mpegURL" />-->
							
							            <!--<source src="http://hls.open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.m3u8" type="application/x-mpegURL" />-->
							           
							        </video>
						        </div>
		                	</div>
                        </div>
                        
                        <div class="page-center">
                        	<c:if test="${loginSign eq 'group'}">
	                        	<div class="item-box-park-div" style="display:none">
			                		<div class="btn-group">
			                		  <input type="hidden" id="parkSelect"/>
									  <button type="button" class="btn btn-Primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
									    选择车场 <span class="caret"></span>
									  </button>
									  <ul class="dropdown-menu">
									    <c:forEach items="${parks}" var="park">
						                   <c:if test="${not empty park.value_no}">
												<li><a href="javascript:void(0)" ref="${park.value_no}">${park.value_name}</a></li>
										   </c:if>
										</c:forEach>
									    <li role="separator" class="divider"></li>
									    <li><a href="#">选择车场</a></li>
									  </ul>
									</div>
			                	</div>
			                </c:if>	
		                	<div class="item-box-channel-div" style="display:none">
		                		<div class="btn-group">
								  <button type="button" class="btn btn-Primary dropdown-toggle" onclick="qryChannels()" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
								    选择通道 <span class="caret"></span>
								  </button>
								  <ul class="dropdown-menu" id="channel_ul">
								    
								  </ul>
								</div>
		                	</div>
                        	<div class="item-box-center-div">
		                		<div class="actions" id="rodDiv">
		                              <a onclick="liftRod()" class="btn btn-outline btn-circle btn-sm blue">
		                                 	 抬&nbsp&nbsp&nbsp杆 </a>
										<input type="hidden" id="rodChannelName"></input>
										<input type="hidden" id="rodChannelId"></input>
										<input type="hidden" id="rodConfirmComid"></input>
		                        </div>
		                	</div>
		                	<div id="centerVideoDiv">
		                		<div class="col-lg-12">
			                		<div class="portlet light bordered">
				                		<video id="myPlayer5" poster="" controls="controls"  autoplay width="900px" height="486.5px">
								            <!--<source src="rtmp://rtmp.open.ys7.com/openlive/50ab2363093040589f4ceab4abf4dbed" type=""/>-->
								            <%--<source src="http://hls.open.ys7.com/openlive/50ab2363093040589f4ceab4abf4dbed.hd.m3u8" type="application/x-mpegURL" />--%>
								            <source src="http://hls.open.ys7.com/openlive/f01018a141094b7fa138b9d0b856507b.m3u8" type="application/x-mpegURL" />
								           
								        </video>
							        </div>
						        </div>
		                	</div>
		                	<%--泊位使用率 --%>
                        	<div class="berthDiv">
                        		<div class="col-lg-12">
		                            <div class="portlet light bordered">
		                            	<div class="portlet-title">
		                                    <div class="caption">
		                                        <i class="icon-equalizer font-yellow"></i>
		                                        <span class="caption-subject font-yellow bold uppercase">泊位使用率</span>
		                                    </div>
		                                </div>
		                                <div class="portlet-body">
		                                	<div>
		                                    	<div id="echartmain5"></div>
		                                    </div>
		                                </div>
		                            </div>
		                        </div>
                        	</div>
                        	<%--事件处理 --%>
                        	<div class="eventDiv" style="display:none">
                        		<div style="z-index: 1; visibility: hidden;" id="loading">
									<div class="loading-indicator">
										<label style="font-size: 15px; color: red"></label>
									</div>
								</div>
							   <div class="modal-content" id="eventContent">
							        <div class="modal-header" style="background-color: #FB6E17;text-align: center">
							             <h4 class="modal-title" onclick="closeEventDiv()" style="overflow: visible;float: right;border:2px;border-color: #393a3e;color:white">关闭</h4>
							             <h4 class="modal-title" id="eventTitle" style="color:white">请手动匹配订单</h4>
							         </div>
							         <%--事件处理 #393a3e--%>
							         <div class="modal-body" style="background-color: #393a3e" id="eventBody">
										<%--匹配idv --%>
				                		<div  class="col-lg-5" style="" id="confirmDiv">
				                			<div style="text-align: center;margin-top: 20px" class="centerWrap">
				                				<div class="centerContent"><span style="color:white;font-size: 20px">出场待确认车辆</span></div>
				                			</div>
				                			<div style="text-align: center;padding: 15px;" class="centerWrap">
				                				<div class="centerContent"><img id="carpic" src=""/></div>
				                			</div>
				                			<input type="hidden" id="channelId">
				                			<input type="hidden" id="confirmComid">
				                		</div>
				                		<div id="matDiv" class="col-lg-7" style="display:none;overflow: auto" >
				                			<div id="matLeftDiv" class="col-lg-6">
				                				
				                			</div>
				                			<div id="matCenterDiv" class="col-lg-6">
				                			
				                			</div>
				                		</div>
				                		<div id="matFailDiv" class="col-lg-7" style="display:none" >
				                			<div style="text-align: center;margin-top: 20px" class="centerWrap" id="matFailFontDiv">
				                				<div class="centerContent""><span style="color:white;font-size: 20px">未找到车牌号相似的入场订单</span></div>
				                			</div>	
				                			<div>
					                			<div class="col-lg-6" >
					                				<button type="button" class="btn" style="background-color: #F59A55" onclick="" disabled="true">打开监控</button>
					                			</div>
					                			<div class="col-lg-6" >
					                				<input type="hidden" id="confirmId">
					                				<button type="button" class="btn" style="background-color: #0390CB" onclick="closeEventDiv(0)">完成</button>
					                			</div>
								         	</div>
				                		</div>
				                		<%--结算div --%>
					                	<div id="balDiv" class="col-lg-6" style="display:none;text-align: center">
					                		<div id="balOrderId" class="centerWrap">
					                			<div class="bottomContent"><h4  style="color:white">订单号:</h4></div>
					                		</div>
					                		<div id="balIntime" class="centerWrap">
					                			<div class="bottomContent"><h4  style="color:white">进场时间:</h4></div>
					                		</div>
					                		<div id="balCarNum" class="centerWrap">
					                			<div class="bottomContent"><h4  style="color:white">车牌号码:</h4></div>
					                		</div>
					                	</div>
				                		<div  class="col-lg-6" id="selectDiv" style="display:none">
				                			<div style="padding: 15px;">
				                				<img id="carpicSelected" src="" />
				                			</div>
				                		</div>
				                		<div id="balButtonDiv" style="display:none;">
				                			<div class="col-lg-6" >
				                				<button type="button" class="btn" style="background-color: #F59A55" onclick="back()">返回</button>
				                			</div>
				                			<div class="col-lg-6" >
				                				<button type="button" class="btn" style="background-color: #0390CB" onclick="balanceOrder()">确定</button>
				                			</div>
							         	</div>
				                		<%--成功div --%>
				                		<div id="sucDiv" style="display:none">
					                		<div id="sucWrap" class="centerWrap">
					                			<div class="centerContent">
					                				<div style="float: left;margin-left: 35%">
					                					<img id="sucImg" />
					                				</div>
					                				<div style="float: left;">
						                				<h2 style="color:white" id="sucMesg">处理成功</h2>
						                				<h4 style="color:white;margin-top: 10px" id="balSucCarNum">车牌号</h4>
						                			</div>
					                			</div>
					                		</div>
					                		<div>
					                			<button type="button" id="sucBtton" style="background-color: #0390CB" class="btn" onclick="closeEventDiv(1)">完成</button>
					                		</div>
				                		</div>
							         </div>
							   </div>
							</div>
                        </div>
                        
                        <div class="page-right">
                        	<div class="electradeDiv">
		                		<div class="portlet light bordered" id="tradeTotalMainDiv">
					               <div class="portlet-title">
					                   <div class="caption font-dark">
					                          <span class="caption-subject font-purple bold uppercase">今日电子交易金额</span>
					                   </div>
					                </div>
					                <div class="portlet-body table-both-scroll">
					                   <div id="tradeTotalDiv">
					                   	   
					                    </div>
					                </div>
					          	</div>
					          	<div class="portlet light bordered" id="tradeCountMainDiv">
					               <div class="portlet-title">
					                   <div class="caption font-dark">
					                          <span class="caption-subject font-purple bold uppercase">今日电子交易笔数</span>
					                   </div>
					                </div>
					                <div class="portlet-body table-both-scroll">
					                   <div id="tradeCountDiv" >
						                  
					                     </div>
					                </div>
					          	</div>
		                	</div>
		                	<%--事件管理 --%>
		                	<div style="margin-top:10px">
		                		<div id="">
			                		<div class="portlet light bordered">
						               <div class="portlet-title">
						                   <div class="caption font-dark">
						                          <i class="icon-settings font-red"></i>
						                          <span class="caption-subject font-red bold uppercase">事件管理</span>
						                          <!--<span class="caption-helper">（7日之内）</span>-->
						                   </div>
						                   <!-- <div class="tools"> </div> -->
						                   <!--<div class="actions">
			                                      <a href="citypeakalert.do?authid=${con_authid}&from=index" class="btn btn-outline btn-circle red btn-sm blue">
			                                          <i class="fa fa-share"></i> 详情 </a>
			                                </div> -->
						                </div>
						                <div class="portlet-body table-both-scroll">
						                   <div id="eventManagerDiv">
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
                        </div>
                    </div>
                  </div>
              </div>
		</div>   
    	
        <%--监控器管理div --%>
		<div id="monitorManagerDiv" class="monitorManagerDiv" style="visibility: hidden;">
			<iframe style="width:100%;height:100%" src="monitor.do?action=monitorManager"></iframe>
		</div>
		<!-- 监控列表模态框（Modal） -->
		<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		    <div class="modal-dialog" style="width:900px;">
		        <div class="modal-content">
		            <div class="modal-header">
		                <button type="button" class="close" data-dismiss="modal" aria-hidden="true" title="关闭">&times;</button>
		                <h4 class="modal-title" id="myModalLabel">监控器管理</h4>
		            </div>
		            <div class="modal-body" style="height:600px;">
		            	<iframe style="width:100%;height:100%" src="monitor.do?action=monitorManager&iframe=1"></iframe>
		            </div>
		            <%-- <div class="modal-footer">
		                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
		                <button type="button" class="btn btn-primary">提交更改</button>
		            </div> --%>
		        </div>
		    </div>
		</div>
		<!-- 播放监控视频模态框（Modal） -->
		<div class="modal fade" id="videoModal" tabindex="-1" role="dialog" aria-labelledby="videoModalLabel" aria-hidden="true">
		    <div class="modal-dialog" style="width:900px;">
		        <div class="modal-content">
		            <div class="modal-header">
		                <button type="button" class="close" data-dismiss="modal" aria-hidden="true" title="关闭">&times;</button>
		                <h4 class="modal-title" id="videoModalLabel">监控视频</h4>
		            </div>
		            <div class="modal-body" style="height:600px;">
		            	
		            </div>
		        </div>
		    </div>
		</div>
		<%--<script src="js/monitor/berthpercent.js"></script>--%>
		<script src="http://echarts.baidu.com/build/dist/echarts-all.js"></script>
		<script type="text/javascript">
			/*定义全局变量*/
			var elecTradeTotal=0;
			var elecTradeCount=0;
			//登录标记 loginSign: group-集团管理员 park-车场管理员
			var loginSign = '${loginSign}';
			//中心监控播放地址
			var centerVideoSrc='';
			//启动页面ReverseAjax 功能
			dwr.engine.setActiveReverseAjax(true);
			//设置页面关闭时 通知服务端销毁对话
			dwr.engine.setNotifyServerOnPageUnload(true);
			Push.onPageLoad("statusTag");
			//Push.onPageLoad("centerVideoTag");
			//服务器向客户端推送信息的方法 
			function popCenterVideo(autoMessage) {
				var data = eval("("+autoMessage+")");
				//呼叫主机性质和登录性质一致，切换中心视频
				if(data !=null && typeof(data)!='undefined'){
					if((loginSign=='group' && data.main_phone_type==1)
							|| (loginSign=='park' && data.main_phone_type==0)){
						if(centerVideoSrc != data.play_src){//与当前播放源不同
							reloadCenterVideo(data.play_src,data.id);
						}
					}
				}
		    }
		    function showMessage(autoMessage) {
				<%--var data = eval("("+autoMessage+")");
				//呼叫主机性质和登录性质一致，切换中心视频
				if(data !=null && typeof(data)!='undefined'){
					if((loginSign=='group' && data.main_phone_type==1)
							|| (loginSign=='park' && data.main_phone_type==0)){
						if(centerVideoSrc != data.play_src){//与当前播放源不同
							centerVideoSrc = data.play_src;
							reloadCenterVideo(data.play_src,data.id);
						}
					}
				}--%>
				//泊位使用率
				loadBerthData();
				//电子交易数据,数据一样不刷新
				var url = "monitor.do?action=electrade";
				$.post(url, function(result) {
					result=eval("("+result+")");
					if(elecTradeTotal !=result.total || elecTradeCount != result.count){
						elecTradeTotal=result.total;
						elecTradeCount=result.count;
						loadElecData();
					}
				});
				//待处理事件
				TableDatatablesScroller.init();
		    }
			/*加载电子交易数据*/
			function loadElecData(){
				var elecTradeTotalContent="<div class='appOrderTotalFont'><div class='centerContent'>￥</div></div>"; 
				elecTradeTotal=Math.round(elecTradeTotal).toString();
				for(var i=0;i<6-elecTradeTotal.length;i++){
					elecTradeTotalContent+="<div class='appOrderFont'><div class='centerContent'>0</div></div>";
				}
				for(var i=0;i<elecTradeTotal.length;i++){
					elecTradeTotalContent+="<div class='appOrderFont'><div class='centerContent'>"+elecTradeTotal.substring(i, i+1)+"</div></div>";
				}
				$("#tradeTotalDiv").html(elecTradeTotalContent);
				
				var elecTradeCountContent="";
				elecTradeCount=Math.round(elecTradeCount).toString();
				//alert(elecTradeCount);
				for(var i=0;i<6-elecTradeCount.length;i++){
					if(i==0){
						elecTradeCountContent="<div class='appOrderCountFont'><div class='centerContent'>0</div></div>";
					}else{
						elecTradeCountContent+="<div class='appOrderFont'><div class='centerContent'>0</div></div>";
					}
				}
				for(var i=0;i<elecTradeCount.length;i++){
					if(elecTradeCount.length==6 && i==0){
						elecTradeCountContent="<div class='appOrderCountFont'><div class='centerContent'>"+elecTradeCount.substring(i, i+1)+"</div></div>";
					}else{
						elecTradeCountContent+="<div class='appOrderFont'><div class='centerContent'>"+elecTradeCount.substring(i, i+1)+"</div></div>";
					}
				}
				$("#tradeCountDiv").html(elecTradeCountContent);
			}
			/*加载泊位使用率*/
			function loadBerthData(){
				// 使用刚指定的配置项和数据显示图表。
				$.post('monitor.do?action=berthpercent').done(function(result) {
					var arrDate = [];
					var berthPercent = [];
					var refreshBerth = true;//
					$.each(eval(result), function(i, data) {
					    if(data.refreshBerth != "true"){
                            refreshBerth = false;
                            return;
						}
						arrDate.push(data.time);
						berthPercent.push(data.percent);
					});
					if(!refreshBerth){
                        return;
					}
                    // 基于准备好的dom，初始化echarts实例
                    var myChart = echarts.init(document.getElementById('echartmain5'));
					myChart.setOption({
						tooltip: {
							trigger: 'axis',
							 formatter: '{b}时:\n{c}%' ,
						},
						legend: {
							data: ['使用率%']
						},
						grid: {
							containLabel: true,
							x1:50,
							x2:50,
							y1:10,
							y2:35,
						},
						xAxis: {
							type: 'category',
							boundaryGap: false,
							data: arrDate,
							axisLabel : {
				                show:true,
				                interval: 0,
				                formatter: '{value}\n时'
				            }
						},
						yAxis: {
							type: 'value',
							axisLabel: {  
					              show: true,  
					              interval: 'auto',  
					              formatter: '{value}',  
					         },  
					         show: true  
						},
						series: [{
								name: '使用率%',
								type: 'line',
								stack: '总量',
								data: berthPercent,
								itemStyle : { 
									normal: {
										label : {
											show: true,
											formatter: '{c}',
										}
									}
								},
							}]
					});
				});
			}
		    function gwh(_h) {
		    	//alert(document.body.clientHeight);
		    	var h,_h=_h?_h:0;
		    	if (window.innerHeight) {
		    		h = window.innerHeight;
		    	}else{
		    		h = document.documentElement.offsetHeight || document.body.clientHeight || 0;
		    	};
		    	h = h<_h?_h:h;
		    	return parseInt(h);
		    }
		    function gww(_w) {
		    	var w,_w=_w?_w:0;
		    	//alert(document.body.clientWidth )
		    	if (window.innerWidth) {
		    		w = window.innerWidth;
		    	}else{
		    		w = document.documentElement.offsetWidth || document.body.clientWidth || 0;
		    	};
		    	w = w<_w?_w:w;
		    	return parseInt(w);
		    }
		  	//全屏宽和高
			var widowWidth = $(window).width();
		  	//alert("全屏宽度>>>>>>"+widowWidth);
			var widowHeight = $(window).height()<660 ? 660 : $(window).height();
			//alert("全屏高度>>>>>>"+widowHeight);
			//小视频宽和高
			var sVideoWidth = widowWidth*0.18;
			var sVideoHeight = sVideoWidth*(9/16);
			//大视频宽和高
			var bVideoWidth = widowWidth*0.52;
			var bVideoHeight = bVideoWidth*(9/16);
			//事件table高度
			var eventTable = widowHeight-bVideoHeight-10-52-(50+40+12+15);
			//忽略js脚本错误
  			//window.onerror = killerrors;
  			//function killerrors() { return true; }
		</script>
		<script type="text/javascript">
			$(function(){ 
				$('#myModal').on('show.bs.modal', function (e) {  
		            // 关键代码，如没将modal设置为 block，则$modala_dialog.height() 为零  
		            $(this).css('display', 'block');  
		            var modalHeight=$(window).height() / 2 - $('#myModal .modal-dialog').height() / 2;  
		            $(this).find('.modal-dialog').css({  
		                'margin-top': modalHeight  
		            }); 
		        });
				
				$("#monitorStyleDiv").width(sVideoWidth);
			    //加载小视频
			    $(".item-box-left-button").width(sVideoWidth);
			    $(".item-box-left-button").height(sVideoHeight);
			    reloadVideo("sVideo1","myPlayer1","${videos.video1}",sVideoWidth,sVideoHeight);
			    reloadVideo("sVideo2","myPlayer2","${videos.video2}",sVideoWidth,sVideoHeight);
			    reloadVideo("sVideo3","myPlayer3","${videos.video3}",sVideoWidth,sVideoHeight);
			    reloadVideo("sVideo4","myPlayer4","${videos.video4}",sVideoWidth,sVideoHeight);
			    //车场下拉框选择事件
			    $("ul>li").bind("click", function(){
			    	//存储车场编号
			    	$(this).parent().prev().prev().val($(this).children().attr("ref"));
			    	//展示
			    	$(this).parent().prev().html($(this).text()+'<span class="caret"></span>');
			    });
			    //加载大视频
			    reloadVideo("centerVideoDiv","myPlayer5","${videos.video1}",bVideoWidth,bVideoHeight);
			    reloadCenterVideo("${videos.video1}","${videos.monitor1}");
			   	$("#centerVideoDiv").height(bVideoHeight);
			    //加载泊位使用率柱状图
			    $(".berthDiv").width(bVideoWidth);
			    $("#echartmain5").height(widowHeight-bVideoHeight-52-10-48);
			    loadBerthData();
			   	//加载电子交易div
			    $(".electradeDiv").height(bVideoHeight);
			    $("#tradeTotalDiv").height((bVideoHeight-190-10)/2);
			    $("#tradeCountDiv").height((bVideoHeight-190-10)/2);
			    var url = "monitor.do?action=electrade";
				$.post(url, function(result) {
					result=eval("("+result+")");
					elecTradeTotal=result.total;
					elecTradeCount=result.count;
					loadElecData();
				});
			    //加载事件div
			    $("#eventManagerDiv").height(widowHeight-bVideoHeight-52-10-48);
			});
			function reloadCenterVideo(src,monitor_id){
				//alert("监控id>>>>>>>>>>"+monitor_id);
				centerVideoSrc = src;
				$("#centerVideoDiv").empty();
				$("#centerVideoDiv").html("<video id='myPlayer5' poster='' controls='controls'  autoplay width='"+bVideoWidth+"' height='"+bVideoHeight+"'><source src='"+src+"' type=''/></video>");
				var player5 = new EZUIPlayer('myPlayer5');
				
				//查询监控对应的通道信息
				var url = "monitor.do?action=qryChannelByMonitId&monitor_id="+monitor_id;
				$.post(url, function(result) {
					//是否显示抬杆按钮
					if(result==null || result == "{}"){
						document.getElementById("rodDiv").style.visibility = "hidden";
					}else{
						result=eval("("+result+")");
						document.getElementById("rodDiv").style.visibility = "visible";
						$("#rodChannelName").val(result.passname);
						$("#rodChannelId").val(result.channelid);
						$("#rodConfirmComid").val(result.comid);
					}
				});
			}
			function reloadVideo(divId,videoId,src,width,height){
				$("#"+divId).empty();
				$("#"+divId).html("<video id='"+videoId+"' poster='' controls='controls'  autoplay width='"+width+"' height='"+height+"'><source src='"+src+"' type=''/></video>");
				var rplayer = new EZUIPlayer(videoId);
			}
			
			//查询车场下属通道
			function qryChannels(){
				var comid = $("#parkSelect").val();
				if(typeof(comid)=="undefined" || comid == null){
					comid = "";
				}
				$.ajax({
					url : "${path}/monitor.do",
					type : "post",
					dataType:"json",
					data : {
						'comid' : encodeURI(comid),
						'action':'qryChannels',
					},
					success : function(data) {
						var html = '';
						for(var i=0; i < data.length; i++) {
							var channel = data[i];
						   html += '<li><a href="javascript:void(0)" ref="'+channel.play_src+'" alt="'+channel.play_src+'">'+channel.value_name+'</a></li>';
						}
						html += '<li role="separator" class="divider"></li><li><a href="#">选择通道</a></li>';
						$("#channel_ul").html(html);
						//车场/通道下拉框选择事件
					    $("#channel_ul>li").bind("click", function(){
					    	//展示
					    	$(this).parent().prev().html($(this).text()+'<span class="caret"></span>');
					    	//重新加载大视频
					    	var play_src = $(this).children().attr("ref");
					    	if(typeof(play_src)!="undefined" && play_src != ""){
					    		reloadCenterVideo($(this).children().attr("ref"),$(this).children().attr("alt"));
					    	}
					    });
					}
				});
			}
			
			//展示要匹配车辆
			function showConfirmPic(event_id,car_number,channel_id,confirmComid,confirmId){
				//图片展示区高度,宽度
				var eventBodyHeight = widowHeight-bVideoHeight-10-52;
				var eventBodyWidth = bVideoWidth;
				//待确认订单图片宽度和高度
				var carpicWidth = eventBodyWidth*5/12-15-30-30;
				var carpicHeight = carpicWidth*9/16;
				if(eventBodyHeight-carpicHeight-20-30<26){//以高度为基准算图片宽和高
					carpicHeight = eventBodyHeight-20-30-30;
					carpicWidth = carpicHeight*16/9;
				}
				//匹配订单图片宽度和高度
				var matPicWidth = (eventBodyWidth*54/100)/2;
				var matPicHeight = matPicWidth*9/16;
				var matPicPadding = (eventBodyHeight-matPicHeight*2)/4
				if(matPicPadding<5){//以高度为基准算图片宽和高
					matPicHeight=(eventBodyHeight-5*4)/2//(区域高度-padding*4)/2 
					matPicWidth = matPicHeight*16/9;
				}
				//设置隐藏域
				$("#channelId").val(channel_id);//匹配事件通道编号
				$("#confirmComid").val(confirmComid);//匹配事件车场编号
				$("#confirmId").val(confirmId);//匹配事件编号
				//获取要匹配的车辆图片
				document.getElementById("loading").style.visibility = "visible";
				//修改标题
				$("#eventTitle").html("请手动匹配订单");
				$.ajax({
					url : "${path}/monitor.do",
					type : "post",
					dataType:"json",
					data : {
						'event_id' : encodeURI(event_id),
						'car_number' : encodeURI(car_number),
						'comid' : encodeURI($("#confirmComid").val()),
						'action':'getConfirmOrder',
					},
					success : function(data) {
						$("#carpic").attr("src","images/monitor/"+data.picName);
						$("#carpic").attr("alt",event_id);
						$("#carpic").attr("title",car_number);
						//待确认订单图片宽度和高度
						//var carpicWidth = eventBodyWidth*5/12-15-30-30;
						$("#carpic").attr("width",carpicWidth);
						$("#carpic").attr("height",carpicHeight);
						//待确认车辆字体宽度，高度
						$("#confirmDiv>div").height(eventBodyHeight-carpicHeight-20-30);
						$("#confirmDiv>div").width(eventBodyWidth*5/12-30);
					}
				});
				//模糊匹配进场车辆图片
				$.ajax({
					url : "${path}/monitor.do",
					type : "post",
					dataType:"json", 
					data : {
						'event_id' : encodeURI(event_id),
						'car_number' : encodeURI(car_number),
						'comid' : encodeURI($("#confirmComid").val()),
						'action':'matchConfirmOrder',
					},
					success : function(data) {
						if(data == null){
							document.getElementById("loading").style.visibility = "hidden";
							return;
						}
						$("#matLeftDiv").empty();
						$("#matCenterDiv").empty();
						//alert("匹配订单图片padding"+(eventBodyHeight-matPicHeight*2));
						var matPicPadding = (eventBodyHeight-matPicHeight*2)/4
						for(var i=0; i < data.length; i++) {
                			var html = "<img id='carpic"+(i+1)+"' title='"+data[i].carNumber+"' alt='"+data[i].orderId+"' src='images/monitor/"+data[i].picName+"' width='"+matPicWidth+"' height='"+matPicHeight+"' style='margin-top:"+matPicPadding+"px;padding-left:"+matPicPadding+"px;' onclick='selectCarPic(this)'/>";
							if(i%2==0){
								$("#matLeftDiv").append(html);
							}else if(i%2==1){
								$("#matCenterDiv").append(html);
							}
				        } 
						//隐藏泊位折线图
						$(".berthDiv").hide();
						//隐藏待确认车辆图片
						$("#confirmDiv").hide();
						//隐藏匹配在场车辆图片
						$("#matDiv").hide();
						//隐藏匹配在场车辆失败
						$("#matFailDiv").hide();
						//隐藏待结算在场车辆额订单信息
						$("#balDiv").hide();
						//隐藏待结算在场车辆图片
						$("#selectDiv").hide();
						//隐藏待结算操作按钮
						$("#balButtonDiv").hide();
						//隐藏处理成功div
						$("#sucDiv").hide();
						
						
						$(".eventDiv").width(bVideoWidth);
						$("#eventBody").height(eventBodyHeight);
						//展示事件处理div
						$(".eventDiv").show();
						//展示待确认车辆图片
						$("#confirmDiv").show();
						//展示匹配在场车辆图片
						if(data.length>0){
							$("#matDiv").show();
							$("#matDiv").height(eventBodyHeight-5*4);
						}else{
							$("#matFailDiv").show();
							//未匹配到字体宽度，高度
							var carpicWidth = eventBodyWidth*5/12-15-30-30;
							$("#matFailFontDiv>div").height(eventBodyHeight-20-30-30);
							$("#matFailFontDiv>div").width(eventBodyWidth*7/12-30);
						}
						document.getElementById("loading").style.visibility = "hidden";
					}
				});
			}
			//选中车辆
			function selectCarPic(obj){
				//图片展示区高度,宽度
				var eventBodyHeight = widowHeight-bVideoHeight-10-52-20;
				//alert("图片展示区高度"+eventBodyHeight);
				var eventBodyWidth = bVideoWidth;
				//修改标题
				$("#eventTitle").html("请手动确定订单");
				//查询订单信息
				$.ajax({
					url : "${path}/monitor.do",
					type : "post",
					dataType:"json", 
					data : {
						'order_id': $(obj)[0].alt ,
						'action':'querySelectOrder',
						'car_number':encodeURI($(obj)[0].title),
						'comid' : encodeURI($("#confirmComid").val()),
					},
					success : function(data) {
						$("#carpicSelected").attr("src",$(obj)[0].src);
						$("#carpicSelected").attr("title",$(obj)[0].title);
						$("#carpicSelected").attr("alt",$(obj)[0].alt);
						var carpicSelectWidth = eventBodyWidth*37.5/100;
						var carpicSelectHeight = carpicSelectWidth*9/16;
						//选中图片所在区域高度
						var carSelectDivHeight = eventBodyHeight-44;
						if((carSelectDivHeight-carpicSelectHeight)<10){//以高度为基准算选中图片宽和高
							carpicSelectHeight = carSelectDivHeight-10*2;
							carpicSelectWidth = carpicSelectHeight*16/9;
						}
						//alert("选中图片高度》》》》"+carpicSelectHeight+"选中区域高度>>>"+carSelectDivHeight);
						$("#carpicSelected").attr("width",carpicSelectWidth);
						$("#carpicSelected").attr("height",carpicSelectHeight);
						$("#selectDiv>div").attr("padding",(carSelectDivHeight-carpicSelectHeight)/2);
						//$("#carpicSelected").attr("height",(eventBodyWidth*5/12-15-30-30)*9/16);
						$("#balOrderId").height(($("#carpicSelected").height())/3);
						$("#balOrderId>div>h4").html("订单号："+$(obj)[0].alt);
						$("#balIntime").height(($("#carpicSelected").height())/3);
						var in_time = new Date(parseInt(data.create_time) * 1000).toLocaleString().replace(/年|月/g, "-").replace(/日/g, " ");
						$("#balIntime>div>h4").html("入场时间："+in_time);
						$("#balCarNum").height(($("#carpicSelected").height())/3);
						$("#balCarNum>div>h4").html("车牌号："+$(obj)[0].title);
						//隐藏待确认车辆图片
						$("#confirmDiv").hide();
						//隐藏匹配在场车辆图片
						$("#matDiv").hide();
						//展示待结算在场车辆额订单信息
						$("#balDiv").height(carSelectDivHeight);
						$("#balDiv").show();
						//展示待结算在场车辆图片
						$("#selectDiv").height(carSelectDivHeight);
						$("#selectDiv").show();
						//展示待结算操作按钮
						$("#balButtonDiv").show();
					}
				});
			}
			//结算订单
			function balanceOrder(){
				//图片展示区高度,宽度
				var eventBodyHeight = widowHeight-bVideoHeight-10-52;
				var eventBodyWidth = bVideoWidth;
				//通知结算订单
				$.ajax({
					url : "${path}/monitor.do",
					type : "post",
					dataType:"json", 
					data : {
						'order_id': $("#carpicSelected")[0].alt ,
						'action':'balanceOrderInfo',
						'car_number':encodeURI($("#carpic")[0].title),
						'event_id':encodeURI($("#carpic")[0].alt),
						'channel_id':encodeURI($("#channelId").val()),
						'comid' : encodeURI($("#confirmComid").val()),
					},
					success : function(data) {
						//展示待结算在场车辆额订单信息
						$("#balDiv").hide();
						//展示待结算在场车辆图片
						$("#selectDiv").hide();
						//展示待结算操作按钮
						$("#balButtonDiv").hide();
						$("#sucDiv").show();
						//结算完成按钮宽度
						$("#sucBtton").width(eventBodyWidth-46);
						//结算div宽高
						$("#sucWrap").height(eventBodyHeight-39-20);
						$("#sucWrap").width(eventBodyWidth);
						//结算完成车牌号
						$("#balSucCarNum").html("车牌号："+$("#carpicSelected")[0].title);
						//结算提示信息
						$("#sucMesg").html(data.message);
						//展示结算成功/失败 图片
						$("#sucImg").attr("src","images/monitor/"+data.img);
						//修改标题
						$("#eventTitle").html(data.message);
						//重新加载事件table列表
						$("#sample_1>tbody").empty();
						TableDatatablesScroller.init();
						//Datatable.ajax.url("monitor.do?action=alert").load();
					}
				});
			}
			
			//抬杆放行
			function liftRod(){
				$.ajax({
					url : "${path}/monitor.do",
					type : "post",
					dataType:"json", 
					data : {
						'action':'liftRod',
						'channel_name':encodeURI($("#rodChannelName").val()),
						'channel_id':encodeURI($("#rodChannelId").val()),
						'comid' : encodeURI($("#rodConfirmComid").val()),
					},
					success : function(data) {
						alert(data.message);
					}
				});
			}
			
			//返回
			function back(){
				//修改标题
				$("#eventTitle").html("请手动匹配订单");
				//展示待确认车辆图片
				$("#confirmDiv").show();
				//展示匹配在场车辆图片
				$("#matDiv").show();
				//隐藏待结算在场车辆额订单信息
				$("#balDiv").hide();
				//隐藏待结算在场车辆图片
				$("#selectDiv").hide();
				//隐藏待结算操作按钮
				$("#balButtonDiv").hide();
			}
			//关闭
			function closeEventDiv(realDeal){
				if(realDeal==0){//非真实正常处理完成
					$.ajax({
						url : "${path}/monitor.do",
						type : "post",
						dataType:"json", 
						data : {
							'action':'updateConfirmStatus',
							'id' : encodeURI($("#confirmId").val()),
						},
						success : function(data) {
							if(data.success){
								//隐藏事件div
								$(".eventDiv").hide();
								//展示泊位div
								$(".berthDiv").show();
								loadBerthData();
							}else{
								alert("处理失败");
								//隐藏事件div
								$(".eventDiv").hide();
								//展示泊位div
								$(".berthDiv").show();
								//loadBerthData();
							}
						}
					});
				}else{
					//隐藏事件div
					$(".eventDiv").hide();
					//展示泊位div
					$(".berthDiv").show();
					loadBerthData();
				}
				
			}
		</script>
		<script>  
		    function fullScreen(){  
		        var docElm = document.documentElement;  
		        //W3C   
		        if (docElm.requestFullscreen) {  
		            docElm.requestFullscreen();  
		        }  
		            //FireFox   
		        else if (docElm.mozRequestFullScreen) {  
		            docElm.mozRequestFullScreen();  
		        }  
		            //Chrome等   
		        else if (docElm.webkitRequestFullScreen) {  
		            docElm.webkitRequestFullScreen();  
		        }  
		            //IE11   
		        else if (docElm.msRequestFullscreen) {  
		         	docElm.msRequestFullscreen();   
		        }  
		    }  
		  
		    function exitFullScreen() {  
		        if (document.exitFullscreen) {  
		            document.exitFullscreen();  
		        }  
		        else if (document.mozCancelFullScreen) {  
		            document.mozCancelFullScreen();  
		        }  
		        else if (document.webkitCancelFullScreen) {  
		            document.webkitCancelFullScreen();  
		        }  
		        else if (document.msExitFullscreen) {  
		            document.msExitFullscreen();  
		        }  
		    }  
		  
		    document.addEventListener("fullscreenchange", function () {  
		    }, false);  
		      
		    document.addEventListener("mozfullscreenchange", function () {  
		    	
		    }, false);  
		      
		    document.addEventListener("webkitfullscreenchange", function () {  
		    	
		    }, false);  
		      
		    document.addEventListener("msfullscreenchange", function () {  
		    	
		    }, false);  
		  
		</script>  
		
    </body>
</html>