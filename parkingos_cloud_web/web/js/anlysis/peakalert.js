function loaddata(){
	url = "peakgantt.do?action=peakgantt&btime="+ 
	document.getElementById("btime").value;
	$.post(url, function(result) {
		var jsonData = eval("(" + result + ")");
		hiddlecontent(jsonData);
	});
}


function hiddlecontent(result){
	if(result != null && result.length > 0){
		var colors = new Array();
		for(var i=0; i< result.length; i++){
			colors.push("#FCD202");
		}
	}
	var chart = AmCharts.makeChart("chart_container1", {
	    "type": "gantt",
		"chartScrollbar": {},
	    "period": "mm",
	    "colors":colors,//每个柱形的颜色
	    "valueAxis": {
	        "type": "date"
	    },
	    "brightnessStep": 0,
	    "graph": {
	        "fillAlphas": 1,
	        "dateFormat":"JJ:NN",
	        "balloonText":"高峰预警时间段：[[open]] - [[value]]"
	    },
	    "rotate": true,
	    "dataDateFormat":"YYYY-MM-DD",
	    "categoryField": "category",
	    "segmentsField": "segments",
	    "startDate": "2016-01-01",
	    "startField": "start",
	    "endField": "end",
	    "durationField": "duration",
	    "dataProvider": result,
	    "chartCursor": {
	        "valueBalloonsEnabled": false,
	        "cursorAlpha": 1,
	        "valueLineBalloonEnabled": true,
	        "valueLineEnabled": true,
	        "valueZoomable":true,
	        "zoomable":false
	    },

	    "valueScrollbar": {
	        "position":"top",
	        "autoGridCount":true,
	        "color":"#000000"
	    }
	});
}
