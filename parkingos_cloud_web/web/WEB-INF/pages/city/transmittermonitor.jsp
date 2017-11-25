<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
	
	<title>给多个点添加信息窗口</title>
	<style type="text/css">
		body, html {width: 100%;height: 100%;margin:0;font-family:"微软雅黑";}
		#allmap{width:100%;height:100%;}
			p{margin-left:5px; font-size:14px;}
        .search {
			height: 40px;
			margin: 20px 5px 10px 5px;
		}
		.qiehua {
			float: left;
			margin-left: 0px;
		}
		.qiehua a{
		text-decoration: none;
		}
		.qiehua a {
			display:block;
			height: 24px;
		    padding:5px line-height:24px;
			color: #5ccdbe;
			letter-spacing: 0.2em;
			width: 80px;
			text-align: center;
			font-size: 16px;
			float: left;
			margin-left: 0px;
			background: #fff;
			border: #5ccdbe 1px solid;
		}
		.qiehua a:hover,.qiehua a.current{
	height: 24px;
    padding:5px line-height:24px;
	color: #fff;
	letter-spacing: 0.2em;
	width: 60px;
	text-align: center;
	font-size: 16px;
	float: left;
	margin-left: 0px;
	background: #5ccdbe;
	border: #5ccdbe 1px solid;
   }
	</style>
	<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=gomvEhrIsmCOhYbLpVNuQSug"></script>
	<script src="http://libs.baidu.com/jquery/1.9.0/jquery.js"></script>
	<script src="js/tq_utf8.js?0817" type="text/javascript">//表格</script>
</head>
<body>
<div class="search">
        <div class="qiehua"><a href="transmittermonitor.do" class="current">地图</a>
        <a id ="ctsid" href="">列表</a></div>
        <form action="" method="get">
          <div class="an">
            
          </div>
          <!--搜索按钮-->
          <div class="kuan2">
           
          </div>
          <!--搜索框-->
        </form>
      </div>
<div id="allmap"></div>
<script type="text/javascript">
	// 百度地图API功能	<img src="../../../images/icons/bus.png"></img>
	/*权限*/
var authlist = T.A.sendData("getdata.do?action=getauth&authid=${authid}");
var authcityid = T.A.sendData("getdata.do?action=getauthidbyurl&url=citytransmitter.do");
document.getElementById("ctsid").href="citytransmitter.do?authid="+authcityid;
var gps = '${gps}';

	//var myIcon = new BMap.Icon("images/icons/bus.png", new BMap.Size(60, 60));//二级诱导 绿色
	 var myTransIcon="";
 	var normaltransIcon= new BMap.Icon("images/icons/normaltrans.png", new BMap.Size(20, 20));
 	var faulttransIcon= new BMap.Icon("images/icons/faulttrans.png", new BMap.Size(20, 20));
	/* map = new BMap.Map("allmap");
	map.centerAndZoom(new BMap.Point(119.380270,32.392280), 16);
	map.enableScrollWheelZoom(true);
	map.setCurrentCity("扬州");   
	var geolocation = new BMap.Geolocation(); */
	
	map = new BMap.Map("allmap");
	map.centerAndZoom(new BMap.Point(gps.split(',')[0],gps.split(',')[1]), 16);
	map.enableScrollWheelZoom(true);
	map.setCurrentCity("");   
	
	var geolocation = new BMap.Geolocation();
	/******/
	var x=gps.split(',')[0];
	var y =gps.split(',')[1];
	var data_info = eval(T.A.sendData("transmittermonitor.do?action=gettransmitter&lon="+x+"&lat=6"+y));

	var opts = {
				width : 250,     // 信息窗口宽度
				height: 150,     // 信息窗口高度
				title : "信息窗口" , // 信息窗口标题
				enableMessage:true//设置允许信息窗发送短息
			   };
        
		for(var i=0;i<data_info.length;i++){
		     if(data_info[i][5]=="状态:正常")
	                 {
	                   myTransIcon=normaltransIcon;
	                 }
	                 else
	                 {
	                   myTransIcon=faulttransIcon;
	                 }
			var marker = new BMap.Marker(new BMap.Point(data_info[i][0],data_info[i][1]),{
                        enableDragging: false,
                        raiseOnDrag: true,
                        icon: myTransIcon
                    });  // 创建标注
		    map.addOverlay(marker);
		 	var content = data_info[i][3]+"<br>"+"基站电压:"+data_info[i][4]+"V"+"<br>"+data_info[i][2]+"<br>"+data_info[i][5];  
                     
			               // 将标注添加到地图中
			addClickHandler(content,marker);
		}
	
	function addClickHandler(content,marker){
		marker.addEventListener("click",function(e){
			openInfo(content,e)}
		);
		
	}

	function openInfo(content,e){
		var p = e.target;
		var point = new BMap.Point(p.getPosition().lng, p.getPosition().lat);
		var infoWindow = new BMap.InfoWindow(content,opts);  // 创建信息窗口对象 
		map.openInfoWindow(infoWindow,point); //开启信息窗口
	}
	
</script>
</body>
</html>