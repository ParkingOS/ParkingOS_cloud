<%@ page language="java" contentType="text/html; charset=gb2312"
    pageEncoding="gb2312"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<title>真来电ETC</title>
<script src="js/My97DatePicker/WdatePicker.js" type="text/javascript">//日期</script>
<script src="js/tq.js?0817" type="text/javascript">//表格</script>
<script src="js/jquery.js" type="text/javascript">//表格</script>
<style type="text/css">
	 *{font-size: 24px;}
    body{height: 100%;}
    body{background-color:Transparent;}
    input,select{border-radius:10px;}
    #content{ padding: 50px;}
	.tc{text-align:center;margin-top:20px}
	.ntb th,.ntb td{border:none;}
	.w150{width:150px}
	.p10{padding:10px;}
	.w200{width:200px}
	.ml30{margin-left:30px}
	.c{width: 300px;height: 30px;line-height: 30px;margin: 20px;}
 </style>

</head>
<body>
<div id="content" class="tc">

<form action="" method="post">       
 <table class="ntb" style="margin: auto;">
 			
            <tr>
                <td>
                </td>
                <td><input class="c"  id="berth" />
                </td>
            </tr>
            <tr>
                <td>
                </td>
                <td><select class="c"  id="comid" ></select>
                </td>
            </tr>
             <%--<tr>
                <td>
                   测试结果：
                </td>
                <td><textarea id="result" rows="5" cols="30" readonly="readonly">
			</textarea>

                </td>
            </tr>
            --%><tr>
                <td colspan='2'>
                    <input type="button" value='刷新1' onclick="gen()" class='p10 w280 ml30' title="" />
					<input type="button" value='刷新2' onclick="settle()" class='p10 w200 ml30' title="" />
                </td>
            </tr>
        </table>
</form>
</div>
<script type="text/javascript">
function gen(){
	var berth =document.getElementById("berth").value;
	var parkid =document.getElementById("comid").value;
	var result = T.A.sendData("cheatsensor.do?action=genSensorOrder&berth="+berth+"&parkid="+parkid);
	//document.getElementById("result").innerText=result;
}

function settle(){
	var berth =document.getElementById("berth").value;
	var parkid =document.getElementById("comid").value;
	var result = T.A.sendData("cheatsensor.do?action=settleSensorOrder&berth="+berth+"&parkid="+parkid);
	//document.getElementById("result").innerText=result;
}

function vehicletype(){
	$.post("cheatsensor.do?action=getparks", function(v) {
		var childs = eval(v);
		jQuery("#comid").empty();
		for(var i=0;i<childs.length;i++){
			var child = childs[i];
			var id = child.value_no;
			var name = child.value_name;
			jQuery("#comid").append("<option value='"+id+"'>"+name+"</option>"); 
		}
	});
}
vehicletype();
</script>
</body>

