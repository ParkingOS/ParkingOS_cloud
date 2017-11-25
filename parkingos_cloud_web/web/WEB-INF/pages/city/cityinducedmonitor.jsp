<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	
	<title>诱导监测</title>
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
			float:left;
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
   .sitesimg{background: url(images/carcount.png) no-repeat;}
   .sitesimg1{background: url(images/carcount1.png) no-repeat;}
	</style>
	<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=gomvEhrIsmCOhYbLpVNuQSug"></script>
	<script src="http://libs.baidu.com/jquery/1.9.0/jquery.js"></script>
	<script src="js/tq_utf8.js?0817" type="text/javascript">//表格</script>
</head>
<body>
<div class="search">
        <div class="qiehua"><a href="cityinducedmonitor.do" class="current">地图</a>
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
var authcityid = T.A.sendData("getdata.do?action=getauthidbyurl&url=cityinduce.do");
document.getElementById("ctsid").href="cityinduce.do?authid="+authcityid;
var gps = '${gps}';

	//var myIcon = new BMap.Icon("images/icons/bus.png", new BMap.Size(60, 60));//二级诱导 绿色
	        var myInduceIcon="";
 	var threeinduceIcon=new BMap.Icon("images/icons/threeinduce.png",new BMap.Size(20, 20));
	var secondinduceIcon =new BMap.Icon("images/icons/secondinduce.png",new BMap.Size(20, 20));
	
	map = new BMap.Map("allmap");
	map.centerAndZoom(new BMap.Point(gps.split(',')[0],gps.split(',')[1]), 16);
	map.enableScrollWheelZoom(true);
	map.setCurrentCity("");   
	
	var geolocation = new BMap.Geolocation();
	/******/
	var x=gps.split(',')[0];
	var y =gps.split(',')[1];
	var data_induce_info=eval(T.A.sendData("cityinducedmonitor.do?action=getinduce&lon="+x+"&lat="+y)); 
	var opts = {
				width : 250,     // 信息窗口宽度
				height: 245,     // 信息窗口高度
				title : "信息窗口" , // 信息窗口标题
				enableMessage:true//设置允许信息窗发送短息
			   };
	     for(var i=0;i<data_induce_info.length;i++){
                if(data_induce_info[i][5]==1)
                {
                  myInduceIcon=secondinduceIcon;
                }
                else if (data_induce_info[i][5]==2)
                {
                  myInduceIcon=threeinduceIcon;
                }
			var marker = new BMap.Marker(new BMap.Point(data_induce_info[i][0],data_induce_info[i][1]),{
                      enableDragging: false,
                      raiseOnDrag: true,
                      icon: myInduceIcon
                  });  // 创建标注
 			map.addOverlay(marker); 
			var indata=data_induce_info[i][7];
 			var indoue_content="<table>";
		/* 	if(indata){
				for(var j=0;j<indata[0].parklist.length;j++){
				    var remain = indata[0].parklist[j].remain;
					if (indata[0].parklist.length == 1) {
					    var color1 = 'green';
	           		 	if(parseInt(remain)<10){
	           		 		color1='yellow';
	           		 		if(parseInt(remain)<5)
	           		 			color1='red';
	           		 	}
	               		 indoue_content +=
	                   "<tr align='right'><td width='236px' height='145px' algin='' class='sitesimg1' /><span style='font-size:24px;font-weight:bold;color:white;'>" + indata[0].parklist[j].parkname + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</br></br></br><span style='font-size:33px;font-weight:bold;"+color+":green;margin-top:10px'><div style='height:10px'></div>0" + remain + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>";
	           		 }
	           		 else {
	           		 	var loadName = indata[0].parklist[j].parkname;	
	           		 	var fontsize=20;
	           		 	if(loadName.length>5)
	           		 		fontsize=13;
	           		 	var color = 'green';
	           		 	if(parseInt(remain)<10){
	           		 		color='yellow';
	           		 		if(parseInt(remain)<5)
	           		 			color='red';
	           		 	}
	             	  	 indoue_content +=
	                    "<tr align='right'><td width='240px' height='50px' class='sitesimg' /><span style='font-size:"+fontsize+"px;font-weight:bold;color:white;'>" +  loadName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='font-size:16px;font-weight:bold;color:"+color+";'>" + remain + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</br></td></tr>";
	            	}
				}
				indoue_content+="</table>";

				} */
				if(indata){
				if (indata[0].parklist.length == 1) {
				for(var j=0;j<indata[0].parklist.length;j++){
					var remain = indata[0].parklist[j].remain;
					var color1 = 'green';
	           		 	if(parseInt(remain)<10){
	           		 		color1='yellow';
	           		 		if(parseInt(remain)<5)
	           		 			color1='red';
	           		 	}
	               		 	 indoue_content +=
	                   "<tr align='right'><td width='226px' height='145px' algin='' background='images/carcount1.png' /><span style='font-size:24px;font-weight:bold;color:white;'>" + indata[0].parklist[j].parkname.substring(0,4) + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</br></br></br><span style='font-size:33px;font-weight:bold;color:"+color1+";margin-top:10px'>" + indata[0].parklist[j].remain + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>";
	           		 }
				}else{
				    for(var j=0;j<indata[0].module.length;j++){
	           		    var remain1 = indata[0].module[j].remain;
	           		 	var loadName = indata[0].module[j].modulename;
	           		 	var fontsize=23;
	           		 	if(loadName.length>5)
	           		 		loadName=loadName.substring(0, 4);
	           		 
	           		 	var color = 'green';
	           		 	if(parseInt(remain1)<10){
	           		 		color='yellow';
	           		 		if(parseInt(remain1)<5)
	           		 			color='red';
	           		 	}
	             	  	 indoue_content +=
	                    "<tr align='right'><td width='240px' height='50px' background='images/carcount.png' class='sitesimg' /><span style='font-size:"+fontsize+"px;font-weight:bold;color:white;'>" +  loadName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='font-size:16px;font-weight:bold;color:"+color+";'>" + remain1 + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</br></td></tr>";
	            	}
	           		 
				}

	              indoue_content+="</table>";
				}

			
			addClickHandler(indoue_content,marker);
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