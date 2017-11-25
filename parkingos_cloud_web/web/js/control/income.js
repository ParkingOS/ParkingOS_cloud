var ChartsFlotcharts = function(income) {

    return {
        //main function to initiate the module

        init: function() {

            App.addResizeHandler(function() {
//                Charts.initPieCharts();
            });

        },
        initPieCharts: function(income) {

            var data = [];
            var series = Math.floor(Math.random() * 10) + 1;
            series = series < 5 ? 5 : series;

            for (var i = 0; i < series; i++) {
                data[i] = {
                    label: "Series" + (i + 1),
                    data: Math.floor(Math.random() * 100) + 1
                };
            }

            // DEFAULT
            if ($('#pie_chart').size() !== 0) {
                $.plot($("#pie_chart"), data, {
                    series: {
                        pie: {
                            show: true
                        }
                    }
                });
            }
            var jsonData = eval("(" + income + ")");
            var data=[{label:"现金",data:jsonData.cash},
                      {label:"电子支付",data:jsonData.epay},
                      {label:"卡片支付",data:jsonData.card}];
            // GRAPH 1
            if ($('#pie_chart_1').size() !== 0) {
                $.plot($("#pie_chart_1"), data, {
                    series: {
                        pie: {
                            show: true
                        }
                    },
                    legend: {
                        show: false
                    }
                });
            }

            // DONUT
            if ($('#donut').size() !== 0) {
                $.plot($("#donut"), data, {
                    series: {
                        pie: {
                            innerRadius: 0.5,
                            show: true
                        }
                    }
                });
            }

            // INTERACTIVE
            if ($('#interactive').size() !== 0) {
                $.plot($("#interactive"), data, {
                    series: {
                        pie: {
                            show: true
                        }
                    },
                    grid: {
                        hoverable: true,
                        clickable: true
                    }
                });
                $("#interactive").bind("plothover", pieHover);
                $("#interactive").bind("plotclick", pieClick);
            }

            function pieHover(event, pos, obj) {
                if (!obj)
                    return;
                percent = parseFloat(obj.series.percent).toFixed(2);
                $("#hover").html('<span style="font-weight: bold; color: ' + obj.series.color + '">' + obj.series.label + ' (' + percent + '%)</span>');
            }

            function pieClick(event, pos, obj) {
                if (!obj)
                    return;
                percent = parseFloat(obj.series.percent).toFixed(2);
                alert('' + obj.series.label + ': ' + percent + '%');
            }

        }

    };

}();

