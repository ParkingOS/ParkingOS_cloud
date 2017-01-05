<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>综合监测</title>
    <style type="text/css">
        body, html {width: 100%;height: 100%;margin:0;font-family:"微软雅黑";}
		#map{width:100%;height:100%;}
		p{margin-left:5px; font-size:14px;}
        .search {
			height: 40px;
			margin: 10px 5px 10px 5px;
		}
		.qiehua {
			float: left;
			margin-left: 0px;
		}
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
    <link href='./css/bootstrap.min.css' rel='stylesheet' charset="utf-8" />
    <link href='./css/bootstrap-responsive.min.css' rel='stylesheet' charset="utf-8" />
    <script type="text/javascript" src="js/jquery.js" charset="utf-8"></script>
    <script src = 'js/SuperMap.Include.js' type="text/javascript" charset="utf-8"></script>
    <script src="js/tq_utf8.js?0817" type="text/javascript" charset="utf-8" >//表格</script>
    <script type="text/javascript">
        var cityid='${cityid}'
        var type='${datatype}'||'all';
        var map,layer, markerlayer ,marker,
        
       // host = document.location.toString().match(/file:\/\//) ? "http://localhost:8090" : 'http://' + document. .host;
        url =  "http://121.40.136.236:8090/iserver/services/map-YangZhouShi/rest/maps/扬州市";
        //url =  "http://172.16.220.21:8090/iserver/services/map-YangZhouShi/rest/maps/扬州市";
        var data_bike_info="";
        var data_bus_info="";
        var data_park_info="";
        var data_charge_info="";
        var data_trans_info="";
        var data_induce_info="";s
        var data_video_info="";
        function init() {
            map = new SuperMap.Map("map",{controls:[
                new SuperMap.Control.Zoom() ,
                new SuperMap.Control.Navigation() ,
                new SuperMap.Control.LayerSwitcher()
            ]});
            layer= new SuperMap.Layer.TiledDynamicRESTLayer("World", url, null,{maxResolution:"auto"});
            markerlayer = new  SuperMap.Layer.Markers("markerLayer");
            layer.events.on({"layerInitialized":addLayer});
            data_bike_info = eval(T.A.sendData("synthetictest.do?action=getbiketation&lon=119.377906&lat=32.386512"));//获取自行车数据
            data_bus_info = eval(T.A.sendData("synthetictest.do?action=getbusstation&lon=119.377906&lat=32.386512"));//获取公交数据
            data_park_info = eval(T.A.sendData("synthetictest.do?action=getparktation&lon=119.377906&lat=32.386512"));//获取停车场数据
            data_charge_info = eval(T.A.sendData("synthetictest.do?action=getchargepolestation&lon=119.377906&lat=32.386512"));//获取充电桩数据
            data_trans_info=eval(T.A.sendData("synthetictest.do?action=gettransmitter&lon=119.377906&lat=32.386512"));//基站数据
            data_induce_info=eval(T.A.sendData("synthetictest.do?action=getinduce&lon=119.377906&lat=32.386512"));//诱导数据
            data_video_info=eval(T.A.sendData("synthetictest.do?action=getvideo&lon=119.377906&lat=32.386512"));//视频监控
            addData(type);
        }
        function addLayer(){
            map.addLayers([layer,markerlayer]);
            map.setCenter(new SuperMap.LonLat(119.357906 , 32.386512), 6);
            
            //显示地图范围
        }
        //添加数据
        function addData(type) {
        	//alert(temp.length);
	        markerlayer.removeMarker(marker);
            var size = new SuperMap.Size(20,20);
            var offset = new SuperMap.Pixel(-(size.w/2), -size.h);
            
            var normalbikeIcon = new SuperMap.Icon('images/icons/normalbike.png', size, offset);
            var warnbikeIcon = new SuperMap.Icon('images/icons/warnbike.png', size, offset);
            var mybikeIcon="";
            
            var normalparkIcon = new SuperMap.Icon('images/icons/normalpark.png', size, offset);
            var warnparkIcon = new SuperMap.Icon('images/icons/warnpark.png', size, offset);
            var myparkIcon="";
            
            var myChargeIcon="";
            var normalchargeIcon = new SuperMap.Icon('images/icons/normalcharge.png', size, offset);
            var warnchargeIcon = new SuperMap.Icon('images/icons/warncharge.png', size, offset);
            
            var myTransIcon="";
	        var normaltransIcon= new SuperMap.Icon('images/icons/normaltrans.png', size, offset);
	        var faulttransIcon= new SuperMap.Icon('images/icons/faulttrans.png', size, offset);
	        
	        var myInduceIcon="";
	        var normalinduceIcon=new SuperMap.Icon('images/icons/normalinduce.png', size, offset);
	        var faultinduceIcon =new SuperMap.Icon('images/icons/faultinduce.png', size, offset);
	        
	        var myVideoIcon="";
	        var normalvideoIcon=new SuperMap.Icon('images/icons/normalvideo.png', size, offset);
	        var faultvideoIcon =new SuperMap.Icon('images/icons/faultvideo.png', size, offset);
            
            
            
            //自行车
            if(type=='all'||type=='bike'){
	            for(var i=0;i<data_bike_info.length;i++){
		             if(data_bike_info[i][4]==0) {
		                mybikeIcon=normalbikeIcon;
		             }else{
		                mybikeIcon=warnbikeIcon;
		             }
		             marker =new SuperMap.Marker(new SuperMap.LonLat(data_bike_info[i][0],data_bike_info[i][1]),mybikeIcon);
		             marker.M_RAINSTATION = data_bike_info[i];
		             
		             //var content = "自行车站名称："+data_bike_info[i][2]+"<br>"+"地址："+data_bike_info[i][3]+"<br>"+"空位数："+data_bike_info[i][5]+"<br>"+"总数："+data_bike_info[i][6];
		              marker.events.on({
		               "click":openInfoWin,
		               "touchstart":openInfoWin,    //假如要在移动端的浏览器也实现点击弹框，则在注册touch类事件
		               "scope": marker
		             });
		              markerlayer.addMarker(marker);
	           }
            }
           //公交
            if(type=='all'||type=='bus'){
	            for(var i=0;i<data_bus_info.length;i++){
	             var mybusIcon=new SuperMap.Icon('images/icons/bus.png', size, offset);
	             var busmarker =new SuperMap.Marker(new SuperMap.LonLat(data_bus_info[i][0],data_bus_info[i][1]), mybusIcon);
	             busmarker.M_RAINSTATION = data_bus_info[i];
	             
	             //var content = "自行车站名称："+data_bike_info[i][2]+"<br>"+"地址："+data_bike_info[i][3]+"<br>"+"空位数："+data_bike_info[i][5]+"<br>"+"总数："+data_bike_info[i][6];
	              busmarker.events.on({
	               "click":openBusInfoWin,
	               "touchstart":openBusInfoWin,    //假如要在移动端的浏览器也实现点击弹框，则在注册touch类事件
	               "scope": busmarker
	             });
	              markerlayer.addMarker(busmarker);
	           }
            }
             //停车场
            if(type=='all'||type=='park'){
	            for(var i=0;i<data_park_info.length;i++){
	             	 if(data_park_info[i][4]==0||data_park_info[i][4]==2){
	               		 myparkIcon=normalparkIcon;
	            	 }else{
		                myparkIcon=warnparkIcon ;
		             }
	         		  var  parkmarker =new SuperMap.Marker(new SuperMap.LonLat(data_park_info[i][0],data_park_info[i][1]),myparkIcon);
	            	 parkmarker.M_RAINSTATION = data_park_info[i];
	             
	             	//var content = "自行车站名称："+data_bike_info[i][2]+"<br>"+"地址："+data_bike_info[i][3]+"<br>"+"空位数："+data_bike_info[i][5]+"<br>"+"总数："+data_bike_info[i][6];
	             	 parkmarker.events.on({
	               			"click":openParkInfoWin,
	               			"touchstart":openParkInfoWin,    //假如要在移动端的浏览器也实现点击弹框，则在注册touch类事件
	               			"scope": parkmarker
	             	});
	                markerlayer.addMarker(parkmarker);
	           }
            }
              //充电桩
            if(type=='all'||type=='plot'){
	            for(var i=0;i<data_charge_info.length;i++){
	                 if (data_charge_info[i][6]==1)
	                 {
	                   myChargeIcon=normalchargeIcon;
	                 }
	                 else if(data_charge_info[i][6]==0)
	                 {
	                    myChargeIcon=warnchargeIcon;
	                 }
	                 
	         		 var  chargemarker =new SuperMap.Marker(new SuperMap.LonLat(data_charge_info[i][0],data_charge_info[i][1]),myChargeIcon);
	            	 chargemarker.M_RAINSTATION = data_charge_info[i];
	             
	             	//var content = "自行车站名称："+data_bike_info[i][2]+"<br>"+"地址："+data_bike_info[i][3]+"<br>"+"空位数："+data_bike_info[i][5]+"<br>"+"总数："+data_bike_info[i][6];
	             	 chargemarker.events.on({
	               			"click":openChargeInfoWin,
	               			"touchstart":openChargeInfoWin,    //假如要在移动端的浏览器也实现点击弹框，则在注册touch类事件
	               			"scope": chargemarker
	             	});
	                markerlayer.addMarker(chargemarker);
	           }
            } //基站
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
	         		var  Transmarker =new SuperMap.Marker(new SuperMap.LonLat(data_trans_info[i][0],data_trans_info[i][1]),myTransIcon);
	            	 Transmarker.M_RAINSTATION = data_trans_info[i];
	             	 Transmarker.events.on({
	               			"click":openTransInfoWin,
	               			"touchstart":openTransInfoWin,    //假如要在移动端的浏览器也实现点击弹框，则在注册touch类事件
	               			"scope": Transmarker
	             	});
	                markerlayer.addMarker(Transmarker);
	             }
	           }
	           //诱导
	            if(type=='all'||type=='induce'){
	              for(var i=0;i<data_induce_info.length;i++){
	                 if(data_induce_info[i][5]=="状态:正常")
	                 {
	                   myInduceIcon=normalinduceIcon;
	                 }
	                 else
	                 {
	                   myInduceIcon=faultinduceIcon;
	                 }
	         		var  Inducemarker =new SuperMap.Marker(new SuperMap.LonLat(data_induce_info[i][0],data_induce_info[i][1]),myInduceIcon);
	            	 Inducemarker.M_RAINSTATION = data_induce_info[i];
	             	 Inducemarker.events.on({
	               			"click":openInduceInfoWin,
	               			"touchstart":openInduceInfoWin,    //假如要在移动端的浏览器也实现点击弹框，则在注册touch类事件
	               			"scope": Inducemarker
	             	});
	                markerlayer.addMarker(Inducemarker);
	           }
	        }
	            //视频监控
	            if(type=='all'||type=='video'){
	              for(var i=0;i<data_video_info.length;i++){
	              	 var video = data_video_info[i];
	                 if(video[2]=="1") {
	                   myVideoIcon=normalvideoIcon;
	                 } else if (video[2]=="0") {
	                   myVideoIcon=faultvideoIcon;
	                 }
	         		var  Videomarker =new SuperMap.Marker(new SuperMap.LonLat(video[0],video[1]),myVideoIcon);
	            	 Videomarker.M_RAINSTATION = video;
	                Videomarker.events.on({
	               		"click":openVideoInfoWin,
	             		"touchstart":openVideoInfoWin,    //假如要在移动端的浏览器也实现点击弹框，则在注册touch类事件
	               		"scope": Videomarker
	             	});
	                markerlayer.addMarker(Videomarker);
	           }
	        }
           // alert("adddata over");
        }
        
        
        //打开公交车信息框
        var infowin = null;
        function   openBusInfoWin()
        {
            closeInfoWin();
            var busmarker = this;
            var data_bus_info=busmarker.M_RAINSTATION;
            var station_id=data_bus_info[4];
           T.A.sendData("synthetictest.do?action=getroute&stationID="+station_id,"GET","",function(ret){
           		ret = eval(ret)
           		var data = ret.data;
	            var str = "";
	            for(var i=0;i<data.length;i++){
	            	str += data[i].RouteName+"--首班车："+data[i].FirstTime+",末班车："+data[i].LastTime+"<br>";
	            }
	          	var content = "公交站名称："+data_bus_info[2]+"<br>"+"描述信息："+data_bus_info[3]+"<br>"+str;
	            var lonlat = busmarker.getLonLat();
	            var size = new SuperMap.Size(0, 33);
	            var offset = new SuperMap.Pixel(11, -30);
	            var mybusIcon=new SuperMap.Icon('images/icons/bus.png', size, offset);
	            var popup = new SuperMap.Popup.FramedCloud("popwin",
	                    new SuperMap.LonLat(lonlat.lon,lonlat.lat),
	                    null,
	                    content,
	                    mybusIcon,
	                    true);
	            infowin = popup;
	            map.addPopup(popup);
           },2,null); 
          //  {"data":[{"RouteID":1013,"RealtimeInfoList":[{"BusID":"55141227152019327026","BusName":"3581","ArriveStaName":"奥都花城南门","ArriveTime":"2016-04-08 15:50:56","BusPostion":{"Longitude":119.38405,"Latitude":32.38043},"StationID":"100274","SpaceNum":5,"RunTime":10,"ISLast":0,"SendTime":null,"ForeCastInfo1":"2 分之前到达","ForeCastInfo2":"还有 5 站"},{"BusID":"55141227161611003130","BusName":"3592","ArriveStaName":"大润发邗江店（广发银行）","ArriveTime":"2016-04-08 15:50:12","BusPostion":{"Longitude":119.39696,"Latitude":32.38932},"StationID":"101133","SpaceNum":10,"RunTime":10,"ISLast":0,"SendTime":null,"ForeCastInfo1":"3 分之前离开","ForeCastInfo2":"还有 10 站"}],"StationID":"107114","StationName":"万豪西花苑","RouteName":"13路","EndStaInfo":"石油山庄-->西部客运枢纽","FirstTime":"2015-06-30 06:05:00","LastTime":"2015-06-30 18:00:00","FirtLastShiftInfo":"首末班：06:05--18:00","FirtLastShiftInfo2":null},{"RouteID":1016,"RealtimeInfoList":[{"BusID":"55130821095918566133","BusName":"2288","ArriveStaName":"邗江区政府","ArriveTime":"2016-04-08 15:50:53","BusPostion":{"Longitude":119.39943,"Latitude":32.37885},"StationID":"102571","SpaceNum":10,"RunTime":10,"ISLast":0,"SendTime":null,"ForeCastInfo1":"2 分之前离开","ForeCastInfo2":"还有 10 站"},{"BusID":"55130823145602315029","BusName":"2269","ArriveStaName":"邗江区政府","ArriveTime":"2016-04-08 15:50:34","BusPostion":{"Longitude":119.39951,"Latitude":32.3783},"StationID":"102571","SpaceNum":10,"RunTime":10,"ISLast":0,"SendTime":null,"ForeCastInfo1":"3 分之前到达","ForeCastInfo2":"还有 10 站"}],"StationID":"107114","StationName":"万豪西花苑","RouteName":"16路","EndStaInfo":"润扬森林公园-->西部客运枢纽","FirstTime":"2015-08-27 05:50:00","LastTime":"2015-08-27 18:40:00","FirtLastShiftInfo":"首末班：05:50--18:40","FirtLastShiftInfo2":null},{"RouteID":2026,"RealtimeInfoList":[{"BusID":"55150102143037274159","BusName":"2910","ArriveStaName":"汽车东站","ArriveTime":"2016-04-08 17:41:00","BusPostion":{"Longitude":0,"Latitude":0},"StationID":"105434","SpaceNum":30,"RunTime":10,"ISLast":0,"SendTime":null,"ForeCastInfo1":"预计17:41发车","ForeCastInfo2":"还有 30 站"}],"StationID":"107114","StationName":"万豪西花苑","RouteName":"26路晚","EndStaInfo":"汽车东站-->西部客运枢纽","FirstTime":"2016-01-13 18:50:00","LastTime":"2016-01-13 21:50:00","FirtLastShiftInfo":"首末班：18:50--21:50","FirtLastShiftInfo2":null},{"RouteID":1026,"RealtimeInfoList":[{"BusID":"55150102143631379176","BusName":"2928","ArriveStaName":"昌建广场","ArriveTime":"2016-04-08 15:51:00","BusPostion":{"Longitude":119.36745,"Latitude":32.38332},"StationID":"104184","SpaceNum":1,"RunTime":10,"ISLast":0,"SendTime":null,"ForeCastInfo1":"2 分之前离开","ForeCastInfo2":"还有 1 站 344 米"},{"BusID":"55151224094504792000","BusName":"2060","ArriveStaName":"京华城（南门）","ArriveTime":"2016-04-08 15:51:20","BusPostion":{"Longitude":119.37461,"Latitude":32.38394},"StationID":"104194","SpaceNum":2,"RunTime":10,"ISLast":0,"SendTime":null,"ForeCastInfo1":"2 分之前离开","ForeCastInfo2":"还有 2 站 975 米"}],"StationID":"107114","StationName":"万豪西花苑","RouteName":"26路","EndStaInfo":"汽车东站-->西部客运枢纽","FirstTime":"2015-05-22 06:10:00","LastTime":"2015-05-22 18:30:00","FirtLastShiftInfo":"首末班：06:10--18:30","FirtLastShiftInfo2":null},{"RouteID":1062,"RealtimeInfoList":[{"BusID":"55131107171008014189","BusName":"1045","ArriveStaName":"邗江公安局（玛丽妇科医院）","ArriveTime":"2016-04-08 15:50:24","BusPostion":{"Longitude":119.39978,"Latitude":32.38081},"StationID":"102504","SpaceNum":10,"RunTime":10,"ISLast":0,"SendTime":null,"ForeCastInfo1":"3 分之前离开","ForeCastInfo2":"还有 10 站"},{"BusID":"55131107170715743179","BusName":"1039","ArriveStaName":"双虹桥","ArriveTime":"2016-04-08 15:50:05","BusPostion":{"Longitude":119.42712,"Latitude":32.38572},"StationID":"106384","SpaceNum":14,"RunTime":10,"ISLast":0,"SendTime":null,"ForeCastInfo1":"3 分之前离开","ForeCastInfo2":"还有 14 站"}],"StationID":"107114","StationName":"万豪西花苑","RouteName":"62路","EndStaInfo":"友谊广场-->杨庙客运站","FirstTime":"2015-09-25 06:00:00","LastTime":"2015-09-25 18:25:00","FirtLastShiftInfo":"首末班：06:00--18:25","FirtLastShiftInfo2":null}]}
           
            //var content=this;
            
        }
        //打开自行车信息框
        function   openInfoWin()
        {
            closeInfoWin();
            var marker = this;
            var data_bike_info=marker.M_RAINSTATION;
            //var content=this;
            var content = "自行车站名称："+data_bike_info[2]+"<br>"+"地址："+data_bike_info[3]+"<br>"+"空位数："+data_bike_info[5]+"<br>"+"总数："+data_bike_info[6]+"<br>"+data_bike_info[7];
            var lonlat = marker.getLonLat();
            var size = new SuperMap.Size(0, 33);
            var offset = new SuperMap.Pixel(11, -30);
            var icon = new SuperMap.Icon("images/icons/normalbike.png", size, offset);
           
            var popup = new SuperMap.Popup.FramedCloud("popwin",
                    new SuperMap.LonLat(lonlat.lon,lonlat.lat),
                    null,
                    content,
                    icon,
                    true);
            infowin = popup;
            map.addPopup(popup);
        }
      //打开停车场信息框
        function   openParkInfoWin()
        {
            closeInfoWin();
            var parkmarker = this;
            var data_park_info=parkmarker.M_RAINSTATION;
            //var content=this;
           var content = data_park_info[3]+"<br>"+data_park_info[2]+"<br>"+"空位数："+data_park_info[5]+"<br>"+"总数："+data_park_info[6]+"<br>"+data_park_info[7];
            var lonlat = parkmarker.getLonLat();
            var size = new SuperMap.Size(0, 33);
            var offset = new SuperMap.Pixel(11, -30);
            var myparkIcon= new SuperMap.Icon('images/icons/warnpark.png', size, offset);
            var popup = new SuperMap.Popup.FramedCloud("popwin",
                    new SuperMap.LonLat(lonlat.lon,lonlat.lat),
                    null,
                    content,
                    myparkIcon,
                    true);
            infowin = popup;
            map.addPopup(popup);
        }
          //打开充电桩信息框
        function   openChargeInfoWin()
        {
            closeInfoWin();
            var chargemarker = this;
            var data_charge_info=chargemarker.M_RAINSTATION;
            //var content=this;
            var content ="充电桩名称："+data_charge_info[2]+"<br>"+"地址:"+data_charge_info[3]+"<br>"+"剩余桩位数："+data_charge_info[4]+"<br>"+"总数："+data_charge_info[5]+"<br>"+data_charge_info[7];
            var lonlat = chargemarker.getLonLat();
            var size = new SuperMap.Size(0, 33);
            var offset = new SuperMap.Pixel(11, -30);
            var myChargeIcon=new SuperMap.Icon('images/icons/normalcharge.png', size, offset);
            var popup = new SuperMap.Popup.FramedCloud("popwin",
                    new SuperMap.LonLat(lonlat.lon,lonlat.lat),
                    null,
                    content,
                    myChargeIcon,
                    true);
            infowin = popup;
            map.addPopup(popup);
        }
        //打开基站
         function openTransInfoWin()
        {
            closeInfoWin();
            var Transmarker = this;
            var data_trans_info=Transmarker.M_RAINSTATION;
            //var content=this;
            var content = data_trans_info[3]+"<br>"+"基站电压:"+data_trans_info[4]+"V"+"<br>"+data_trans_info[2]+"<br>"+data_trans_info[5];  
            var lonlat = Transmarker.getLonLat();
            var size = new SuperMap.Size(0, 33);
            var offset = new SuperMap.Pixel(11, -30);
            var myTransIcon= new SuperMap.Icon('images/icons/normaltrans.png', size, offset);
            var popup = new SuperMap.Popup.FramedCloud("popwin",
                    new SuperMap.LonLat(lonlat.lon,lonlat.lat),
                    null,
                    content,
                    myTransIcon,
                    true);
            infowin = popup;
            map.addPopup(popup);
        }
        //打开诱导
        function openInduceInfoWin()
        {
            closeInfoWin();
            var Inducemarker = this;
            var data_induce_info=Inducemarker.M_RAINSTATION;
            //var content=this;
            var content =data_induce_info[3]+"<br>"+data_induce_info[2]+"<br>"+data_induce_info[4]+"<br>"+data_induce_info[5]; 
            var lonlat = Inducemarker.getLonLat();
            var size = new SuperMap.Size(0, 33);
            var offset = new SuperMap.Pixel(11, -30);
            var myInduceIcon= new SuperMap.Icon('images/icons/normalinduce.png', size, offset);
            var popup = new SuperMap.Popup.FramedCloud("popwin",
                    new SuperMap.LonLat(lonlat.lon,lonlat.lat),
                    null,
                    content,
                    myInduceIcon,
                    true);
            infowin = popup;
            map.addPopup(popup);
        }
        //打开视频
         function openVideoInfoWin(){
         	var video=this.M_RAINSTATION;
            var videoid=video[4];
            closeInfoWin();
            Twin({
				Id:"client_detail_8551",
				Title:"播放视频  &nbsp;&nbsp;&nbsp;&nbsp;<font color='red'> 提示：双击关闭此对话框</font>",
				Content:"<iframe src=\"synthetictest.do?action=playvideo&videoid="+videoid+"\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>",
				Width:T.gww()/2+100,
				Height:T.gwh()/2+130,
			})
           /*  var Videomarker = this;
            var data_video_info=Videomarker.M_RAINSTATION;
            //var content=this;
            var content =data_video_info[3]+"<br>"+data_video_info[2];
            var lonlat = Videomarker.getLonLat();
            var size = new SuperMap.Size(0, 33);
            var offset = new SuperMap.Pixel(11, -30);
            var myVideoIcon= new SuperMap.Icon('images/icons/normalvideo.png', size, offset);
            var popup = new SuperMap.Popup.FramedCloud("popwin",
                    new SuperMap.LonLat(lonlat.lon,lonlat.lat),
                    null,
                    content,
                    myVideoIcon,
                    true);
            infowin = popup;
            map.addPopup(popup); */
        }
        //关闭信息框
        function closeInfoWin(){
            if(infowin){
                try{
                    infowin.hide();
                    infowin.destroy();
                }
                catch(e){}
            }
        }
          $(function(){
            
        	$('.'+type).css('background','#5ccdbe').css('color','#fff');
        });
        function geticon()
        {
        alert(1);
        }
    </script>
</head>
<body onload="init()" >
<div class="search">
        
        <div class="qiehua">
        <a id ="ctsid"  href="synthetictest.do?authid=${authid}&datatype=all" class="all">全部</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=park" class="park">停车场</a>&nbsp;
       
        
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=bike" class="bike"> 自行车</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=bus" class="bus"> 公交车</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=plot" class="plot">充电桩</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=induce" class="induce">诱导屏</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=trans" class="trans">基站</a>&nbsp;
        <a id ="ctsid" href="synthetictest.do?authid=${authid}&datatype=video" class="video">视频</a>
      
        </div>
        <form action="" method="get">
          <div class="an">
              <img src="images/icons/normalpark.png" width="15" height="15"/>车位正常
        <img src="images/icons/warnpark.png" width="15" height="15"/>车位紧张
        <img src="images/icons/normalbike.png" width="15" height="15"/>车位正常
        <img src="images/icons/warnbike.png" width="15" height="15""/>车位正常
        <img src="images/icons/normalcharge.png" width="15" height="15"/>桩位正常
        <img src="images/icons/warncharge.png" width="15" height="15"/>桩位紧张
          </div>
          <div class="kuan2">
        <img src="images/icons/normalinduce.png" width="15" height="15"/>诱导正常
        <img src="images/icons/faultinduce.png" width="15" height="15"/>诱导故障
        <img src="images/icons/normaltrans.png" width="15" height="15"/>基站正常
        <img src="images/icons/faulttrans.png" width="15" height="15"/>基站故障
        <img src="images/icons/normalvideo.png" width="15" height="15"/>视频正常
        <img src="images/icons/faultvideo.png" width="15" height="15"/>视频故障
          </div>
        </form>
      </div>
<div id="map"></div>

</body>
</html>