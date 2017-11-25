<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title></title>
    <script src="js/jquery.js" type="text/javascript"></script>
    <script src="js/videoctrl.js" type="text/javascript"></script>
    <script type="text/javascript">
        function loadOne() {
            var result = loadDefaultControl("DPSDK_OCX", 1);
        }
        function playOne(channelID) {
            var result = playVideo("DPSDK_OCX", channelID);
        }
		function play(channelID){
			loadOne();
			playOne(channelID);
		}
		$(document).ready(function() {
			play("${channelID}");
		}); 
    </script>
</head>
<body>
    <object id="DPSDK_OCX" classid="CLSID:D3E383B6-765D-448D-9476-DFD8B499926D" style="width: 300px; height: 220px" codebase="DpsdkOcx.cab#version=1.0.0.0">
    </object>
    <!-- <input type="button" id="btnPlayOne" value="播放单个视频" onclick="play('1000002$1$0$0');" /> -->
</body>
</html>
