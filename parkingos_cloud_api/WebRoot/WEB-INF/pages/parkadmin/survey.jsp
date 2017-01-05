<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>


<html lang="">
<head>
<meta http-equiv="Content-Type" content="text/html" charset="utf-8"/>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>概况</title>
<style type="text/css">
	.topdiv{height:30px;
	font-size:17px;
	font-weight:700;
	line-height:25px;
	font-family:"微软雅黑";
	vertical-align:middle;
	background-image: url(images/page_top_bg.png)}
	.topmesg{height:40px;
	font-size:17px;
	font-weight:700;
	line-height:30px;
	margin-left:20px;
	font-family:"微软雅黑";
	vertical-align:middle;}
</style>

</head>
<body id="body">
<div id="maindiv" >
	<div id='money'>
		<div class='topdiv'><img src='images/money_att.png' style='height:20px;margin-left:10px;margin-right:10px;vertical-align:middle;'/>收费趋势图</div>
		<div class='topmesg'>${today}</div>
		<div id="main" style="width:100%;height:400px"></div>
	</div>
	<div id='park'>
		<div class='topdiv'><img src='images/line_att.png' style='height:20px;margin-left:10px;margin-right:10px;vertical-align:middle;'/>车位利用趋势图</div>
		<div class='topmesg'>${parktotal}</div>
		<div id="main1" style="width:100%;height:400px"></div>
	</div>
	
</div>
<div id='parktable' style='display:none'>
		<div class='topdiv'>
			<img src='images/park_table.png' style='height:20px;margin-left:10px;margin-right:10px;vertical-align:middle;'/>人员设备情况
		</div>
	</div>
</body>
<script src="js/chart/echarts.js"></script>


<script>
var getobj=function(id){return document.getElementById(id)};
var h = getobj('body').offsetHeight;
var w = getobj('body').offsetWidth;
function setobjCss(obj,css){
	for(var c in css){
		try{obj.style[c]=css[c];}catch(e){};
	}
}
setobjCss(getobj('parktable'),{'margin':'10px auto','width':(parseInt(w)-4)+'px','height':(h-597)+'px','border':'1px solid #abcdef','borderRadius':'5px'});
setobjCss(getobj('maindiv'),{'margin':'10px auto','width':parseInt(w)+'px','height':'470px'});
setobjCss(getobj('money'),{'width':(parseInt(w*0.5)-7)+'px','float':'left','border':'1px solid #abcdef','borderRadius':'5px'});
setobjCss(getobj('park'),{'marginLeft':'8px','width':(parseInt(w*0.5)-7)+'px','float':'left','border':'1px solid #abcdef','borderRadius':'5px'});
// 路径配置
require.config({
  paths: {
    echarts: 'js'
  }
});

/*var moneyData={title:['总收费','电子收费','现金收费'],xname:'时间',xtime:['12/7','12/8','12/9','12/10','12/11','12/12','12/13','12/14','12/15'],
	yname:'金额(元)',data:[{name:'总收费',data:[5500.00, 4805.20, 5349.20, 5343.00, 5221.90, 5530, 5140,5322,5455]},
						{name:'电子收费',data:[3000, 3455, 3555, 3222, 3444, 3330, 3310,4000,3220]},
						{name:'现金收费',data:[1500, 2320, 2010, 1540, 1900, 2300, 2100,2000,3000]}]};*/
var moneyData=eval(${moneyData});
var parkData =eval(${parkData}); 
// 使用
var data1={
legend:{data:moneyData.title},
x:[{name:moneyData.xname,
  	type : 'category',
  	boundaryGap : false,
    data : moneyData.xtime
    }],
y:[{name:moneyData.yname,
    type : 'value'
    ,splitArea : {show : true}
    }],
series:[{name:moneyData.data[0].name,
        type:'line',
		smooth: true,
        data:moneyData.data[0].data
        },{name:moneyData.data[1].name,
           type:'line',
			smooth: true,
            data:moneyData.data[1].data
        },{name:moneyData.data[2].name,
            type:'line',
			smooth: true,
            data:moneyData.data[2].data
        }
    ]
};

var data2={
legend:{data:parkData.title},
x:[{name:parkData.xname,
  	type : 'category',
  	boundaryGap : false,
    data : parkData.xtime
    }],
y:[{name:parkData.yname,
    type : 'value'
    ,splitArea : {show : true}
    }],
series:[{name:parkData.data[0].name,
        type:'line',
		smooth: true,
        data:parkData.data[0].data}
    ]
};
/*
var data2={
legend:
	{data:['车位数']},
x:[{name:'时间',type : 'category',boundaryGap:false,
	data:['12/7','12/8','12/9','12/10','12/11','12/12','12/13','12/14','12/15']}
	],
y:[{name:'车位数',type : 'value',splitArea : {show : true}}],
series:[{name:'车位数', type:'line',smooth: true,
	data:[5500.00, 4805.20, 5349.20, 5343.00, 5221.90, 5530, 5140,5322,5455]}]
};
*/
function createChart(obj,data){
	require(
		  [
		    'echarts',
		    'echarts/chart/line', // 使用柱状图就加载bar模块，按需加载
			'echarts/chart/bar' // 使用柱状图就加载bar模块，按需加载
		  ],
		  function (ec) {
		    // 基于准备好的dom，初始化echarts图表
		    var myChart = ec.init(obj); 
		    //设置数据
			option = {
			    tooltip : {
			        trigger: 'axis'
			    },
			    legend: data.legend,
			    toolbox: {
			        show : true,
			        feature : {
			            //mark : {show: true},
			           // dataView : {show: true, readOnly: false},
			            magicType : {show: true, type: ['line', 'bar']},
			            restore : {show: true},
			            saveAsImage : {show: true}
			        }
			    },
			    calculable : true,
			    xAxis :data.x,
			    yAxis :data.y,
			    series:data.series
			};
		    // 为echarts对象加载数据 
		    myChart.setOption(option); 
		  }
	);
}
createChart(getobj('main'),data1);
createChart(getobj('main1'),data2);
</script>

</html>