<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>ECharts</title>
    <!-- 引入 echarts.js -->
    <script src="js/echarts/echarts.js"></script>
    
</head>
<body>
    <!-- 为ECharts准备一个具备大小（宽高）的Dom -->
    <div id="main" style="width: 900px;height:400px;"></div>
    <script type="text/javascript">
    require.config({
    paths: {
        echarts: 'js/echarts',
    }
});
require(
    [
        'echarts',
        'echarts/chart/line',
	    'echarts/chart/bar',
	    'echarts/chart/scatter',
	    'echarts/chart/k',
	    'echarts/chart/pie',
	    'echarts/chart/radar',
	    'echarts/chart/force',
	    'echarts/chart/chord',
	    'echarts/chart/gauge',
	    'echarts/chart/funnel',
	    'echarts/chart/eventRiver',
	    'echarts/chart/venn',
	    'echarts/chart/treemap',
	    'echarts/chart/tree',
	    'echarts/chart/wordCloud',
	    'echarts/chart/heatmap'
    ],
        // 基于准备好的dom，初始化echarts实例
         // 基于准备好的dom，初始化echarts实例
          function (ec) {
     var myChart = ec.init(document.getElementById('main'));

     option = {
    tooltip : {
        trigger: 'axis'
    },
    legend: {
        data:['最高','最低']
    },
    toolbox: {
        show : true,
        feature : {
            mark : {show: true},
            dataZoom : {show: true},
            dataView : {show: true},
            magicType : {show: true, type: ['line', 'bar', 'stack', 'tiled']},
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
            boundaryGap : false,
            data : function (){
                var list = [];
                for (var i = 1; i <= 30; i++) {
                    list.push('2013-03-' + i);
                }
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
            name:'最高',
            type:'line',
            data:function (){
                var list = [];
                for (var i = 1; i <= 30; i++) {
                    list.push(Math.round(Math.random()* 30));
                }
                return list;
            }()
        },
        {
            name:'最低',
            type:'line',
            data:function (){
                var list = [];
                for (var i = 1; i <= 30; i++) {
                    list.push(Math.round(Math.random()* 10));
                }
                return list;
            }()
        }
    ]
};
                    

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);
        }
        );
    </script>
</body>
</html>