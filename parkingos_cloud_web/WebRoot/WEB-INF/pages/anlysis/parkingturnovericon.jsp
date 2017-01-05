<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>泊位周转率</title>
<link href="css/zTreeStyle1.css" rel="stylesheet" type="text/css">
<link href="css/demo.css" rel="stylesheet" type="text/css">
<link href="css/tq.css" rel="stylesheet" type="text/css">
<link href="css/iconbuttons.css" rel="stylesheet" type="text/css">
<link rel="stylesheet" type="text/css" href="js/anlysis/style.css?v=20100302" />

<script src="js/tq_utf8.js?0817" type="text/javascript">//表格</script>
<script src="js/My97DatePicker/WdatePicker.js" charset="gb2312" type="text/javascript">//日期</script>
<script src="js/jquery.js" type="text/javascript"></script>
<script src="js/echarts/echarts.js" charset="utf-8"></script>
   	<style type="text/css">

   
		.search {
	height: 40px;
	margin: 25px 5px 10px 5px;
	
}
		.qiehua {
	float: left;
	margin-left: 0px;
}
		.qiehua a {
	display:block;
	height: 24px;
    padding:5px line-height:24px;
	color: #5ccdbe;
	letter-spacing: 0.2em;
	width: 60px;
	text-align: center;
	font-size: 16px;
	float: left;
	margin-left: 0px;
	background: #fff;
	border: #5ccdbe 1px solid;
}
	</style>

</head>
<body>
<div class="search">
        <div class="qiehua">
        <a  href="parkingturnover.do"  id="table">列表</a>
        <a href="" id="icon" >图表</a>
        </div>
        <form action="" method="get">
          <div class="an">
            
          </div>
          <!--搜索按钮-->
          <div class="kuan2">
           
          </div>
          <!--搜索框-->
        </form>
</div >
<div class="top">

<ul class="search">
<span id="seconddateinput" class="search_text">
日期：<input  class="Wdate" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',startDate:'%y-%M-01',alwaysUseStartDate:true})" type="text" name="endDateSelect" id="btime" value="${btime}" align="absmiddle" readonly   />
</span>
<span class="search_button">
<span class="button_light_green"><button name="" onclick="searchdata()"><span class="confirm"><img src="js/anlysis/spacer.gif" /></span>查询</button></span>
</span>
</ul>
</div>

<!-- 为ECharts准备一个具备大小（宽高）的Dom -->
 <div id="main" style="width: 100%;height:400px;margin:0px;"></div> 
<script type="text/javascript" >
      

      $(function(){
        
        	$("#icon").css('background','#5ccdbe').css('color','#fff');
        });
        
var btime="${btime}";
function searchdata(){
    
	btime = T("#btime").value;
	//etime = T("#etime").value;
	//data=eval(T.A.sendData("parkingturnover.do?action=echarts&btime="+btime+"&etime="+etime));
	
	//url = "reganlysis.do?action=echarts&btime="+btime+"&etime="+etime;
	
	T("#btime").value=btime;
	//T("#etime").value=etime;
    loading("1");
}
      
var xlist = [];
var ylist = [];
function initCharData(operate) {
	xlist=[];	
	ylist = [];
	    var result = '${json}';
	  var data_info ="[]";// eval("(" + result + ")");
	  if(operate==''){
	  	data_info=eval("(" + result + ")");
	  }else{
	      data_info=eval(T.A.sendData("parkingturnover.do?action=echarts&operate=1&btime="+btime));
	  }
      for(var i=0;i<data_info.length;i++)
      {
        xlist.push(data_info[i].company_name);
      }
    
      for(var i=0;i<data_info.length;i++)
      {
        ylist.push(data_info[i].parkingturn);
      }
}

require.config({
    paths: {
        echarts: 'js/echarts',
 	}
});

function charData(){
require(
    [
        'echarts',
	    'echarts/chart/bar',
	    'echarts/chart/line'
	    //'echarts/chart/scatter',
	   // 'echarts/chart/k',
	   // 'echarts/chart/pie',
	   // 'echarts/chart/radar',
	   // 'echarts/chart/force',
	   // 'echarts/chart/chord',0
	  //  'echarts/chart/gauge',
	   // 'echarts/chart/funnel',
	   // 'echarts/chart/eventRiver',
	   // 'echarts/chart/venn',
	   // 'echarts/chart/treemap',
	   // 'echarts/chart/tree',
	   // 'echarts/chart/wordCloud',
	    //'echarts/chart/heatmap'
    ],
        // 基于准备好的dom，初始化echarts实例
         // 基于准备好的dom，初始化echarts实例
          function (ec) {
     var myChart = ec.init(document.getElementById('main'));

      option = {
		    tooltip : {
		        trigger: 'axis'
		    },
		     title : {
        text: '泊位周转率',
        subtext: '按每天'
             },
		    legend: {
		        data:['周转次数']
		    },
		    toolbox: {
		        show : true,
		        feature : {
		            mark : {show: true},
		            dataZoom : {show: true},
		            dataView : {show: true},
		            magicType : {show: true, type: ['bar','line',]},
		            restore : {show: true},
		            saveAsImage : {show: true}
		        }
		    },
		    calculable : true,
		    dataZoom : {
		        show : true,
		        realtime : true,
		        start : 20,
		        end : 80
		    },
		    xAxis : [
		        {
		            type : 'category',
		            data : function (){
		                var list = xlist;

		              
		                return list;
		            }()
		        }
		    ],
		    yAxis : [
		        {
		            type : 'value'
		        }
		    ],
		    series : [
		        {
		            name:'周转次数',
		            type:'bar',
		            data:function (){
		                var list = ylist;
		                return list;
		            }()
		        },
		       
		    ]
		}; 
		
		

                    

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);
        }
        );
} 

                    
function loading(operate){
	initCharData(operate);
	charData();
}
loading('');
</script>
</body>
</html>
