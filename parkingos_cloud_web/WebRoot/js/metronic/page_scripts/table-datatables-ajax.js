var TableDatatablesAjax = function () {

    var initPickers = function () {
        //init date pickers
        $('.date-picker').datepicker({
            rtl: App.isRTL(),
            autoclose: true
        });
    }

    var handleRecords = function () {

        var grid = new Datatable();

        grid.init({
            src: $("#datatable_ajax"),
            onSuccess: function (grid, response) {
                // grid:        grid object
                // response:    json object of server side ajax response
                // execute some code after table records loaded
            },
            onError: function (grid) {
                // execute some code on network or other general error  
            	alert("onError");
            },
            onDataLoad: function(grid) {
                // execute some code on ajax data load
            	alert("onDataLoad");
            },
            loadingMessage: '加载中...',
            dataTable: { // here you can define a typical datatable settings from http://datatables.net/usage/options 

                // Uncomment below line("dom" parameter) to fix the dropdown overflow issue in the datatable cells. The default datatable layout
                // setup uses scrollable div(table-scrollable) with overflow:auto to enable vertical scroll(see: assets/global/scripts/datatable.js). 
                // So when dropdowns used the scrollable div should be removed. 
                //"dom": "<'row'<'col-md-8 col-sm-12'pli><'col-md-4 col-sm-12'<'table-group-actions pull-right'>>r>t<'row'<'col-md-8 col-sm-12'pli><'col-md-4 col-sm-12'>>",
                
                "bStateSave": false, // save datatable state(pagination, sort, etc) in cookie.
                "sScrollX" : 800, //DataTables的宽
                "bAutoWidth" : true, //是否自适应宽度
//               scroller:true,
                "deferRender":true,
                "scrollX":true,
                "scrollCollapse": true,   
                "lengthMenu": [
                    [10, 20, 50, -1],
                    [10, 20, 50, "All"] // change per page values here
                ],
                /*"buttons": [
                          { extend: 'print', className: 'btn dark btn-outline' },
                          { extend: 'pdf', className: 'btn green btn-outline' },
                          { extend: 'csv', className: 'btn purple btn-outline ' }
                      ],*/
                "aoColumnDefs": [
                                 { "sWidth": "80%", "aTargets": [ 0 ] }
                               ],
                "pageLength": 10, // default record count per page
                /*"aoColumns": [//设定各列宽度   
	                {"sWidth": "70%"},   
	                {"sWidth": "100px"},   
	                {"sWidth": "100px"},   
	                {"sWidth": "100px"},   
	                {"sWidth": "100px"},   
	                {"sWidth": "100px"},   
	                {"sWidth": "100px"}, 
	                {"sWidth": "100px"}, 
	                {"sWidth": "100px"} 
	                null,
	                null,
	                null,
	                null,
	                null,
	                null,
	                null,
	                null
	            ], */
	            "language": {
	                "lengthMenu": "每页显示  _MENU_ 条记录",
	                "sInfo": "当前数据为第 _START_ 到第  _END_ 条数据；总共有  _TOTAL_ 条记录",
	                "sZeroRecords": "没有检索到数据",
	                "sInfoEmtpy": "没有数据",
	                "sLengthMenu": "每页显示 _MENU_ 条记录",
	                "sProcessing": "处理中...",
	                "sInfoFiltered": "(由_MAX_项结果过滤)",
	                "sSearch": "搜索:",
	                "oPaginate": {   
	                    "sFirst": "首页",   
	                    "sPrevious": "前页",   
	                    "sNext": "后页",   
	                    "sLast": "尾页",
	                    "page": "第",
                        "pageOf": "页&nbsp;&nbsp"
	                }
	            },
                "sAjaxSource":"test.do?action=getorgtype",
                "fnServerData": function (sSource, aoData, fnCallback, oSettings){
                	$.ajax( {
                		"dataType": 'json',
                		"type": "POST",
                		"url": sSource,
                		"data": aoData,
                		"success": fnCallback
                		} );
                  },//这个是结合服务器模式的回调函数，用来处理服务器返回过来的数据
                "order": [
                    [1, "asc"]
                ]// set first column as a default sort by asc
            }
        });

        // handle group actionsubmit button click
        grid.getTableWrapper().on('click', '.table-group-action-submit', function (e) {
            e.preventDefault();
            var action = $(".table-group-action-input", grid.getTableWrapper());
            if (action.val() != "" && grid.getSelectedRowsCount() > 0) {
                grid.setAjaxParam("customActionType", "group_action");
                grid.setAjaxParam("customActionName", action.val());
                grid.setAjaxParam("id", grid.getSelectedRows());
                grid.getDataTable().ajax.reload();
                grid.clearAjaxParams();
            } else if (action.val() == "") {
                App.alert({
                    type: 'danger',
                    icon: 'warning',
                    message: 'Please select an action',
                    container: grid.getTableWrapper(),
                    place: 'prepend'
                });
            } else if (grid.getSelectedRowsCount() === 0) {
                App.alert({
                    type: 'danger',
                    icon: 'warning',
                    message: 'No record selected',
                    container: grid.getTableWrapper(),
                    place: 'prepend'
                });
            }
        });

        grid.setAjaxParam("customActionType", "group_action");
        grid.getDataTable().ajax.reload();
        grid.clearAjaxParams();
    }

    return {

        //main function to initiate the module
        init: function () {

            initPickers();
            handleRecords();
        }

    };

}();

jQuery(document).ready(function() {
    TableDatatablesAjax.init();
});