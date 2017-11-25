<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>综合监测</title>
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
		}00
		.qiehua a:hover{
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
    <link href='./css/bootstrap.min.css' charset="utf-8" rel='stylesheet' />
    <link href='./css/bootstrap-responsive.min.css' charset="utf-8" rel='stylesheet' />
     <script type="text/javascript" charset="utf-8" src="js/jquery.js"></script>
    <script src = 'js/SuperMap.Include.js' charset="utf-8" type="text/javascript"></script>
    <script src="js/tq_utf8.js?0817" type="text/javascript" charset="utf-8">//表格</script>
     <script type="text/javascript" charset="utf-8" src="http://api.map.baidu.com/api?v=2.0&ak=gomvEhrIsmCOhYbLpVNuQSug"></script>
	<script src="http://libs.baidu.com/jquery/1.9.0/jquery.js" charset="utf-8"></script>
</head>
<body>
<div class="search">
        
        <div class="qiehua">
        <a id ="ctsid"  href="synthetictest.do?authid=${authid}&datatype=all" class="all">全部</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=park" class="park">停车场</a>&nbsp;
        <%--<a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=bike" class="bike"> 自行车</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=bus" class="bus"> 公交车</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=plot" class="plot">充电桩</a>&nbsp; --%>
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=induce" class="induce">诱导屏</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=trans" class="trans">基站</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=video" class="video">视频</a>
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=pda" class="pda">PDA</a>
      
        </div>
        <form action="" method="get">
          <div class="an">
              <img src="images/icons/normalpark.png" width="15" height="15"/>车位正常
        <img src="images/icons/warnpark.png" width="15" height="15"/>车位紧张
          <img src="images/icons/normalinduce.png" width="15" height="15"/>诱导正常
        <img src="images/icons/faultinduce.png" width="15" height="15"/>诱导故障
  <!--<img src="images/icons/normalbike.png" width="15" height="15"/>车位正常
        <img src="images/icons/warnbike.png" width="15" height="15""/>车位正常
        <img src="images/icons/normalcharge.png" width="15" height="15"/>桩位正常
        <img src="images/icons/warncharge.png" width="15" height="15"/>桩位紧张 -->
        <img src="images/icons/normalcharge.png" width="15" height="15"/>PDA正常
        <img src="images/icons/warncharge.png" width="15" height="15"/>PDA故障
          </div>
          <div class="kuan2">
        <img src="images/icons/normaltrans.png" width="15" height="15"/>基站正常
        <img src="images/icons/faulttrans.png" width="15" height="15"/>基站故障
        <img src="images/icons/normalvideo.png" width="15" height="15"/>视频正常
        <img src="images/icons/faultvideo.png" width="15" height="15"/>视频故障
         
          </div>
        </form>
      </div>
<div id="allmap"></div>
  <script type="text/javascript">
  	var gps = '${gps}';
     var type='${datatype}'||'all';

	 var mybusIcon = new BMap.Icon("images/icons/bus.png", new BMap.Size(20, 20));//二级诱导 绿色

    var normalbikeIcon = new BMap.Icon("images/icons/normalbike.png",  new BMap.Size(20, 20));
    var warnbikeIcon = new BMap.Icon("images/icons/warnbike.png", new BMap.Size(20, 20));
    var mybikeIcon="";
    
    var normalparkIcon = new BMap.Icon("images/icons/normalpark.png", new BMap.Size(20, 20));
    var warnparkIcon = new BMap.Icon("images/icons/warnpark.png", new BMap.Size(20, 20));
    var myparkIcon="";
    
    var myChargeIcon="";
    var normalchargeIcon = new BMap.Icon("images/icons/normalcharge.png", new BMap.Size(20, 20));
    var warnchargeIcon = new BMap.Icon("images/icons/warncharge.png", new BMap.Size(20, 20));
    
    var myTransIcon="";
 	var normaltransIcon= new BMap.Icon("images/icons/normaltrans.png", new BMap.Size(20, 20));
 	var faulttransIcon= new BMap.Icon("images/icons/faulttrans.png", new BMap.Size(20, 20));
 
	var myInduceIcon="";
 	var threeinduceIcon=new BMap.Icon("images/icons/threeinduce.png",new BMap.Size(20, 20));
	var secondinduceIcon =new BMap.Icon("images/icons/secondinduce.png",new BMap.Size(20, 20));
	
	var myPDAIcon="";
    var normalPDAIcon = new BMap.Icon("images/icons/normalcharge.png", new BMap.Size(20, 20));
    var warnPDAIcon = new BMap.Icon("images/icons/warncharge.png", new BMap.Size(20, 20));
    
	map = new BMap.Map("allmap");
	map.centerAndZoom(new BMap.Point(gps.split(',')[0],gps.split(',')[1]), 16);
	map.enableScrollWheelZoom(true);
	map.setCurrentCity("");   
	
	var geolocation = new BMap.Geolocation();
	/******/
	var x=gps.split(',')[0];
	var y =gps.split(',')[1];
	var data_park_info = eval(T.A.sendData("synthetictest.do?action=getparktation&lon="+x+"&lat="+y));
	var data_trans_info=eval(T.A.sendData("synthetictest.do?action=gettransmitter&lon="+x+"&lat="+y));//基站数据
	var data_induce_info=eval(T.A.sendData("synthetictest.do?action=getinduce&lon="+x+"&lat="+y)); //诱导数据
	var data_pda_info = eval(T.A.sendData("synthetictest.do?action=getpda&lon="+x+"&lat="+y));//pda数据
		//停车场
	if(type=='all'||type=='park'){
		for(var i=0;i<data_park_info.length;i++){
		      if(data_park_info[i][4]==0||data_park_info[i][4]==2){
	               		 myparkIcon=normalparkIcon;
	            	 }else{
		                myparkIcon=warnparkIcon ;
		             }
			var marker = new BMap.Marker(new BMap.Point(data_park_info[i][0],data_park_info[i][1]),{
                        enableDragging: false,
                        raiseOnDrag: true,
                        icon: myparkIcon
                    });  // 创建标注
		 map.addOverlay(marker);   
		 var content = data_park_info[i][3]+"<br>"+data_park_info[i][2]+"<br>"+"空位数："+data_park_info[i][5]+"<br>"+"总数："+data_park_info[i][6]+"<br>"+data_park_info[i][7];          // 将标注添加到地图中
			addClickHandler(content,marker);
		} 
	}
	
		//基站
	if(type=='all'||type=='trans'){
		for(var i=0;i<data_trans_info.length;i++){
		       if(data_trans_info[i][5]=="状态:正常")
	                 {
	                   myTransIcon=normaltransIcon;
	                 }
	                 else
	                 {
	                   myTransIcon=faulttransIcon;
	                 }
	                 
			var marker = new BMap.Marker(new BMap.Point(data_trans_info[i][0],data_trans_info[i][1]),{
                        enableDragging: false,
                        raiseOnDrag: true,
                        icon: myTransIcon
                    });  // 创建标注
		 map.addOverlay(marker);   
		var content = data_trans_info[i][3]+"<br>"+"基站电压:"+data_trans_info[i][4]+"V"+"<br>"+data_trans_info[i][2]+"<br>"+data_trans_info[i][5];        // 将标注添加到地图中
			addClickHandler(content,marker);
		}
	}
		//诱导屏
	if(type=='all'||type=='induce'){
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
			if(indata){
			  if (indata[0].parklist.length == 1){
			  for(var j=0;j<indata[0].parklist.length;j++){
					var remain = indata[0].parklist[j].remain;
					var color1 = 'green';
	           		 	if(parseInt(remain)<10){
	           		 		color1='yellow';
	           		 		if(parseInt(remain)<5)
	           		 			color1='red';
	           		 	}
	               		 indoue_content +=
	                   "<tr align='right'><td width='226px' height='145px' algin='' background='images/carcount1.png' /><span style='font-size:24px;font-weight:bold;color:white;'>" + indata[0].parklist[j].parkname.substring(0,4) + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</br></br></br><span style='font-size:33px;font-weight:bold;color:"+color1+";margin-top:10px'><div style='height:18px'></div>0" + indata[0].parklist[j].remain + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>";
	           		 }
			  } else { 
			       for(var j=0;j<indata[0].module.length;j++){
			            var remain1 = indata[0].module[j].remain;	
			            var loadName = indata[0].module[j].modulename;
	           	        var fontsize=20;
	           		 	if(loadName.length>5)
	           		 		loadName=loadName.substring(0, 4);
	           		 	var color = 'green';
	           		 	if(parseInt(remain1)<10){
	           		 		color='yellow';
	           		 		if(parseInt(remain1)<5)
	           		 			color='red';
	           		 	}
	             	  	 indoue_content +=
	                    "<tr align='right'><td width='226px' height='56px' background='images/carcount.png' /><span style='font-size:"+fontsize+"px;font-weight:bold;color:white;'>" +  loadName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style='font-size:16px;font-weight:bold;color:"+color+";'>" + remain1 + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</br></td></tr>";
	            	}
			    }
			    indoue_content+="</table>";
	           		 
		}

			addClickHandler(indoue_content,marker);
		} 
	}

	if(type == 'all' || type == 'pda'){
		for(var i=0;i<data_pda_info.length;i++){
		      if(data_pda_info[i].is_onset== 1){
	               		 myparkIcon=normalPDAIcon;
	            	 }else{
		                myparkIcon=warnPDAIcon ;
		             }
			var marker = new BMap.Marker(new BMap.Point(data_pda_info[i].longtitude,data_pda_info[i].latitude),{
                      enableDragging: false,
                      raiseOnDrag: true,
                      icon: myparkIcon
                  });  // 创建标注
		 map.addOverlay(marker);   
		 var content =  "收费员:"+data_pda_info[i].nickname+"<br>是否在位:"+ (data_pda_info[i].is_onseat == 1?"是":"否") +"<br>更新时间:"+data_pda_info[i].update_time;       // 将标注添加到地图中
			addClickHandler(content,marker);
		} 
	}

	var opts = {
				width : 250,     // 信息窗口宽度
				height: 245,     // 信息窗口高度
				title : "信息窗口" , // 信息窗口标题
				enableMessage:true//设置允许信息窗发送短息
			   };		
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
 	$(function(){
      	$('.'+type).css('background','#5ccdbe').css('color','#fff');
     });
      
    </script>

</body>
</html>