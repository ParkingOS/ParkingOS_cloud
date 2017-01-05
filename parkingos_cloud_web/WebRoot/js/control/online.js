
function loaddata(){
	url = "cityindex.do?action=online";
	$.post(url, function(result) {
		hiddle(result);
	});
}

function hiddle(result){
	var data1 = new Array();
	var data2 = new Array();
	var yAxisTitle = "数量";
	var titleText = "";
	var subtitleText = "";
	xAxisTitle = "";
	var xAxisCategories = new Array();
	$.each(eval(result), function(i, paydate) {
		if (xAxisCategories[i] == "") {

		} else {
			xAxisCategories[i] = paydate.time;
			data1[i] = parseInt(paydate.collector_online);
			data2[i] = parseInt(paydate.inspector_online);
		}
	});
	require(
	    [
	        'echarts',
		    'echarts/chart/bar',
		    'echarts/chart/line'
	    ],
	    function (ec) {
	        var myChart1 = ec.init(document.getElementById('echartmain5')); 
	        var option1 = {
	        	    title : {
	        	        text: titleText,
	        	        subtext: ''
	        	    },
	        	    tooltip : {
	        	        trigger: 'axis',
	        	        formatter: function (params) {
	        	            var res = params[0].name;
	        	            res += '<br/>' + params[0].seriesName + params[0].value;
	        	            res += '<br/>' + params[1].seriesName + params[1].value;
	        	            return res;
	        	        }
	        	    },
	        	    legend: {
	        	        data:['收费员在线数','巡查员在线数']
	        	    },
	        	    toolbox: {
	        	        show : false,
	        	        feature : {
	        	            mark : {show: true},
	        	            dataView : {show: true, readOnly: false},
	        	            magicType : {show: true, type: ['line', 'bar']},
	        	            restore : {show: true},
	        	            saveAsImage : {show: true}
	        	        }
	        	    },
	        	    calculable : false,
	        	    grid: {
	        	        y:40,
	        	        y2:60
	        	    },
	        	    xAxis : [
	        	        {
	        	            type : 'category',
	        	            data : xAxisCategories,
	        	            position:'bottom',
	        	            axisLabel : {
	        	                show:true,
	        	                interval: 'auto',    // {number}
	        	                rotate: 40
	        	            },
	        	            splitLine : {
	        	                show:true,
	        	                lineStyle: {
	        	                    color: '#483d8b',
	        	                    type: 'dashed',
	        	                    width: 1
	        	                }
	        	            },
	        	            boundaryGap : true
	        	        }
	        	    ],
	        	    yAxis : [
	        	        {
	        	            type : 'value',
	        	            name : yAxisTitle,
	        	            splitNumber: 4,
	        	            splitLine : {
	        	                show:true,
	        	                lineStyle: {
	        	                    color: '#483d8b',
	        	                    type: 'solid',
	        	                    width: 1
	        	                }
	        	            },
	        	        }
	        	    ],
	        	    series : [
	        	        {
	        	            name:'收费员在线数',
	        	            type:'bar',
	        	            data:data1
	        	        },
	        	        {
	        	            name:'巡查员在线数',
	        	            type:'bar',
	        	            data:data2
	        	        }
	        	    ]
	        	};
	        	myChart1.setOption(option1); 
	        }
	);
}
