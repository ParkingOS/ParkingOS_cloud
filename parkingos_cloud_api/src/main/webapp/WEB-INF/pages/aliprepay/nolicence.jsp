<!DOCTYPE html>
<%@ page contentType="text/html;charset=gb2312" %>
<html lang="zh">

	<head>
		<meta charset="UTF-8" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		<meta http-equiv="X-UA-Compatible" content="ie=edge" />
		<title>µ±Ç°¶©µ¥</title>
		<style type="text/css">
			.spinner {
				margin: 100px auto 0;
				width: 150px;
				text-align: center;
			}
			
			.info{
				display:none
			}
			
			.spinner>div {
				width: 30px;
				height: 30px;
				background-color: #67CF22;
				border-radius: 100%;
				display: inline-block;
				-webkit-animation: bouncedelay 1.4s infinite ease-in-out;
				animation: bouncedelay 1.4s infinite ease-in-out;
				/* Prevent first frame from flickering when animation starts */
				-webkit-animation-fill-mode: both;
				animation-fill-mode: both;
			}
			
			.spinner .bounce1 {
				-webkit-animation-delay: -0.32s;
				animation-delay: -0.32s;
			}
			
			.spinner .bounce2 {
				-webkit-animation-delay: -0.16s;
				animation-delay: -0.16s;
			}
			
			@-webkit-keyframes bouncedelay {
				0%,
				80%,
				100% {
					-webkit-transform: scale(0.0)
				}
				40% {
					-webkit-transform: scale(1.0)
				}
			}
			
			@keyframes bouncedelay {
				0%,
				80%,
				100% {
					transform: scale(0.0);
					-webkit-transform: scale(0.0);
				}
				40% {
					transform: scale(1.0);
					-webkit-transform: scale(1.0);
				}
			}
		</style>
	</head>

	<body>
		<div class="spinner">
			<div class="bounce1"></div>
			<div class="bounce2"></div>
			<div class="bounce3"></div>
			<div style="padding:5px;height:45px;padding-top:8px;border-radius:4px;background:white;">
				<form method="get" role="form" action="${furl}" id="checkform">
					<input type="text" name="action" id = "action" value="${action}" class="info">
					<input type="text" name="code" value="${code}" class="info">
					<input type="text" name="fixcode" value="${fixcode}" class="info">
					<input type="text" name="openid" value="${openid}" class="info">
					<input type="text" name="parkid" value="${comid}" class="info">
					<input type="text" name="ticketid" value="${ticketid}" class="info">
					<input type="text" name="isnolicence" value="${isnolicence}" class="info">
					<input type="text" name="licence" value="" id = "licence" class="info">
				</form>
			</div>
		</div>
		<script type="text/javascript">
            function domainData(url, fn)
            {
                var isFirst = true;
                var iframe = document.createElement('iframe');
                iframe.style.width=0;
                iframe.style.height=0;
                iframe.style.display = 'none';
                var loadfn = function(){
                    if(isFirst){
                        iframe.contentWindow.location = 'http://${fromurl}/zld/nolicence.jsp';
                        isFirst = false;
                    } else {
                        var cache = iframe.contentWindow.name;
                        if(cache != "" && cache != undefined && cache != null){
                            document.getElementById("licence").value=cache
                        }

                        document.getElementById("checkform").setAttribute("action","aliprepay.do")
                        document.getElementById("checkform").submit();
//                        else{
//                            document.getElementById("checkform").setAttribute("action","aliprepay.do")
//                            document.getElementById("action").value="toddcar"
//                            document.getElementById("checkform").submit();
//                        }
                        iframe.contentWindow.document.write('');
                        iframe.contentWindow.close();
                        document.body.removeChild(iframe);
                        iframe.src = '';
                        iframe = null;
                    }
                };
                iframe.src = url;
                if(iframe.attachEvent){
                    iframe.attachEvent('onload', loadfn);
                } else {
                    iframe.onload = loadfn;
                }

                document.body.appendChild(iframe);
            }
		</script>
	</body>
	<script type="text/javascript">
        domainData('${tourl}nolicence2.jsp?123', function(data){
            alert(data);
        });
	</script>
</html>