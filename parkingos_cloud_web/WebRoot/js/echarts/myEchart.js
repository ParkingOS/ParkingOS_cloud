// 路径配置
require.config({
	paths: {
		echarts: 'js/echarts',
	}
});

function initUtili(berth){
	// 使用
	require(
			[
			 'echarts',
			 'echarts/chart/line',
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
			 function (ec) {
				// 基于准备好的dom，初始化echarts图表
				var myChart1 = ec.init(document.getElementById('echartmain1')); 
				var myChart2 = ec.init(document.getElementById('echartmain2')); 
				var myChart3 = ec.init(document.getElementById('echartmain3')); 
				var myChart4 = ec.init(document.getElementById('echartmain4')); 
			
				
				var option1 = {
						tooltip : {
							formatter: "{a} <br/>{b} : {c}%"
						},
						toolbox: {
							show : true,
							feature : {
								mark : {show: false},
								restore : {show: false},
								saveAsImage : {show: false}
							}
						},
						series : [
						          {
						        	  name:'业务指标',
						        	  type:'gauge',
						        	  startAngle: 180,
						        	  endAngle: 0,
						        	  center : ['50%', '70%'],    // 默认全局居中
						        	  radius : 70,
						        	  axisLine: {            // 坐标轴线
						        		  lineStyle: {       // 属性lineStyle控制线条样式
						        			  color: [[0.2, '#2ec7c9'],[0.8, '#5ab1ef'],[1, '#d87a80']], 
						        			  width: 30
						        		  }
						        	  },
						        	  axisTick: {            // 坐标轴小标记
						        		  splitNumber: 10,   // 每份split细分多少段
						        		  length :15,        // 属性length控制线长
						        		  lineStyle: {       // 属性lineStyle控制线条样式
						        			  color: 'auto'
						        		  }
						        	  },
						        	  splitLine: {           // 分隔线
						        		  length :0,         // 属性length控制线长
						        		  lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
						        			  color: 'auto'
						        		  }
						        	  },
						        	  axisLabel: {           // 坐标轴文本标签，详见axis.axisLabel
						        		  formatter: function(v){
						        			  switch (v+''){
						        			  case '10': return '低';
						        			  case '50': return '中';
						        			  case '90': return '高';
						        			  default: return '';
						        			  }
						        		  },
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  color: '#fff',
						        			  fontSize: 10,
						        			  fontWeight: 'bolder'
						        		  }
						        	  },
						        	  pointer : {
						        		  width : 3
						        	  },
						        	  title : {
						        		  show : true,
						        		  offsetCenter: [-15, -100],       // x, y，单位px
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  color: 'black',
						        			  fontSize: 10
						        		  }
						        	  },
						        	  detail : {
						        		  show : true,
						        		  backgroundColor: 'rgba(0,0,0,0)',
						        		  borderWidth: 0,
						        		  borderColor: '#ccc',
						        		  width: 10,
						        		  height: 10,
						        		  offsetCenter: [40, -105],       // x, y，单位px
						        		  formatter:'{value}%',
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  fontSize : 10
						        		  }
						        	  },
						        	  data:[{value: berth, name: '泊位利用率：'}]
						          }
						          ]
				};
				var option2 = {
						tooltip : {
							formatter: "{a} <br/>{b} : {c}%"
						},
						toolbox: {
							show : true,
							feature : {
								mark : {show: false},
								restore : {show: false},
								saveAsImage : {show: false}
							}
						},
						series : [
						          {
						        	  name:'业务指标',
						        	  type:'gauge',
						        	  startAngle: 180,
						        	  endAngle: 0,
						        	  center : ['50%', '70%'],    // 默认全局居中
						        	  radius : 70,
						        	  axisLine: {            // 坐标轴线
						        		  lineStyle: {       // 属性lineStyle控制线条样式
						        			  color: [[0.2, '#2ec7c9'],[0.8, '#5ab1ef'],[1, '#d87a80']], 
						        			  width: 30
						        		  }
						        	  },
						        	  axisTick: {            // 坐标轴小标记
						        		  splitNumber: 10,   // 每份split细分多少段
						        		  length :15,        // 属性length控制线长
						        		  lineStyle: {       // 属性lineStyle控制线条样式
						        			  color: 'auto'
						        		  }
						        	  },
						        	  splitLine: {           // 分隔线
						        		  length :0,         // 属性length控制线长
						        		  lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
						        			  color: 'auto'
						        		  }
						        	  },
						        	  axisLabel: {           // 坐标轴文本标签，详见axis.axisLabel
						        		  formatter: function(v){
						        			  switch (v+''){
						        			  case '10': return '低';
						        			  case '50': return '中';
						        			  case '90': return '高';
						        			  default: return '';
						        			  }
						        		  },
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  color: '#fff',
						        			  fontSize: 10,
						        			  fontWeight: 'bolder'
						        		  }
						        	  },
						        	  pointer : {
						        		  width : 3
						        	  },
						        	  title : {
						        		  show : true,
						        		  offsetCenter: [-15, -100],       // x, y，单位px
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  color: 'black',
						        			  fontSize: 10
						        		  }
						        	  },
						        	  detail : {
						        		  show : true,
						        		  backgroundColor: 'rgba(0,0,0,0)',
						        		  borderWidth: 0,
						        		  borderColor: '#ccc',
						        		  width: 10,
						        		  height: 10,
						        		  offsetCenter: [40, -105],       // x, y，单位px
						        		  formatter:'{value}%',
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  fontSize : 10
						        		  }
						        	  },
						        	  data:[{value: 60, name: '泊位周转率：'}]
						          }
						          ]
				};
				
				var option3 = {
						tooltip : {
							formatter: "{a} <br/>{b} : {c}%"
						},
						toolbox: {
							show : true,
							feature : {
								mark : {show: false},
								restore : {show: false},
								saveAsImage : {show: false}
							}
						},
						series : [
						          {
						        	  name:'业务指标',
						        	  type:'gauge',
						        	  startAngle: 180,
						        	  endAngle: 0,
						        	  center : ['50%', '70%'],    // 默认全局居中
						        	  radius : 70,
						        	  axisLine: {            // 坐标轴线
						        		  lineStyle: {       // 属性lineStyle控制线条样式
						        			  color: [[0.2, '#2ec7c9'],[0.8, '#5ab1ef'],[1, '#d87a80']], 
						        			  width: 30
						        		  }
						        	  },
						        	  axisTick: {            // 坐标轴小标记
						        		  splitNumber: 10,   // 每份split细分多少段
						        		  length :15,        // 属性length控制线长
						        		  lineStyle: {       // 属性lineStyle控制线条样式
						        			  color: 'auto'
						        		  }
						        	  },
						        	  splitLine: {           // 分隔线
						        		  length :0,         // 属性length控制线长
						        		  lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
						        			  color: 'auto'
						        		  }
						        	  },
						        	  axisLabel: {           // 坐标轴文本标签，详见axis.axisLabel
						        		  formatter: function(v){
						        			  switch (v+''){
						        			  case '10': return '低';
						        			  case '50': return '中';
						        			  case '90': return '高';
						        			  default: return '';
						        			  }
						        		  },
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  color: '#fff',
						        			  fontSize: 10,
						        			  fontWeight: 'bolder'
						        		  }
						        	  },
						        	  pointer : {
						        		  width : 3
						        	  },
						        	  title : {
						        		  show : true,
						        		  offsetCenter: [-15, -100],       // x, y，单位px
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  color: 'black',
						        			  fontSize: 10
						        		  }
						        	  },
						        	  detail : {
						        		  show : true,
						        		  backgroundColor: 'rgba(0,0,0,0)',
						        		  borderWidth: 0,
						        		  borderColor: '#ccc',
						        		  width: 10,
						        		  height: 10,
						        		  offsetCenter: [40, -105],       // x, y，单位px
						        		  formatter:'{value}%',
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  fontSize : 10
						        		  }
						        	  },
						        	  data:[{value: 73, name: 'POS机订单占比：'}]
						          }
						          ]
				};
				
				var option4 = {
						tooltip : {
							formatter: "{a} <br/>{b} : {c}%"
						},
						toolbox: {
							show : true,
							feature : {
								mark : {show: false},
								restore : {show: false},
								saveAsImage : {show: false}
							}
						},
						series : [
						          {
						        	  name:'业务指标',
						        	  type:'gauge',
						        	  startAngle: 180,
						        	  endAngle: 0,
						        	  center : ['50%', '70%'],    // 默认全局居中
						        	  radius : 70,
						        	  axisLine: {            // 坐标轴线
						        		  lineStyle: {       // 属性lineStyle控制线条样式
						        			  color: [[0.2, '#2ec7c9'],[0.8, '#5ab1ef'],[1, '#d87a80']], 
						        			  width: 30
						        		  }
						        	  },
						        	  axisTick: {            // 坐标轴小标记
						        		  splitNumber: 10,   // 每份split细分多少段
						        		  length :15,        // 属性length控制线长
						        		  lineStyle: {       // 属性lineStyle控制线条样式
						        			  color: 'auto'
						        		  }
						        	  },
						        	  splitLine: {           // 分隔线
						        		  length :0,         // 属性length控制线长
						        		  lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
						        			  color: 'auto'
						        		  }
						        	  },
						        	  axisLabel: {           // 坐标轴文本标签，详见axis.axisLabel
						        		  formatter: function(v){
						        			  switch (v+''){
						        			  case '10': return '低';
						        			  case '50': return '中';
						        			  case '90': return '高';
						        			  default: return '';
						        			  }
						        		  },
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  color: '#fff',
						        			  fontSize: 10,
						        			  fontWeight: 'bolder'
						        		  }
						        	  },
						        	  pointer : {
						        		  width : 3
						        	  },
						        	  title : {
						        		  show : true,
						        		  offsetCenter: [-15, -100],       // x, y，单位px
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  color: 'black',
						        			  fontSize: 10
						        		  }
						        	  },
						        	  detail : {
						        		  show : true,
						        		  backgroundColor: 'rgba(0,0,0,0)',
						        		  borderWidth: 0,
						        		  borderColor: '#ccc',
						        		  width: 10,
						        		  height: 10,
						        		  offsetCenter: [40, -105],       // x, y，单位px
						        		  formatter:'{value}%',
						        		  textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
						        			  fontSize : 10
						        		  }
						        	  },
						        	  data:[{value: 56, name: '追缴率：'}]
						          }
						          ]
				};
				// 为echarts对象加载数据 
				myChart1.setOption(option1); 
				// 为echarts对象加载数据 
				myChart2.setOption(option2); 
				myChart3.setOption(option3); 
				myChart4.setOption(option4); 
			}
	);
}
