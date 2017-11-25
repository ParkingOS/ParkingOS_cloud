var TableDatatablesScroller = function () {

    var initTable1 = function () {
        var table = $('#sample_1');

        var oTable = table.dataTable({
        	//参考：http://datatables.club/reference/#options
        	//http://my.oschina.net/miaowang/blog/409124?p=1
        	//http://datatables.club/example/
        	// Internationalisation. For more info refer to http://datatables.net/manual/i18n
            "language": {
            	"aria": {
                    "sortAscending": ": activate to sort column ascending",
                    "sortDescending": ": activate to sort column descending"
                },
                "emptyTable": "没有数据记录",
                "info": "当前数据为第 _START_ 到第  _END_ 条数据；总共有  _TOTAL_ 条记录",
                "infoEmpty": "没有数据记录",
                "infoFiltered": "(由  _MAX_ 项结果过滤)",
                "lengthMenu": "每页显示   _MENU_ 条记录",
                "search": "搜索：",
                "zeroRecords": "没有数据记录",
                "processing":"加载中...",
                "paginate": {
                    "previous":"上一页",
                    "next": "下一页",
                    "last": "尾页",
                    "first": "首页",
                    "page": "第 ",
                    "pageOf": "页，总页数 "
                }
            },

//          "scrollY": 300,
	        "scrollCollapse": true,
	        "deferRender": true,
	        "stateSave": false,
	        "lengthChange": true,
	        "paging": true,
	        "scrollX": false,//打开后表格的头部和底部样式总是有点乱
	        "autoWidth": true,
	        "info": true,
	        "ordering": true,//全局排序设置
	        "processing": true,
	        "filter":false,
	        "destroy": false,//摧毁一个已经存在的Datatables，并且用新的options重新创建表格
	        "orderClasses": true,
	        "ordering": false,
	        "pagingType": "bootstrap_extended",
            "columnDefs": [
                         { 
                        	 className:"mystyle"
                        	 /*targets: [0], 
                        	 visible: true, 
                        	 cellType: "td", 
                        	 
                        	 orderSequence:[]*/
                        },
                         { "width": "200", "targets": 3}
                     ],
            "columns":[
				{  
				    "data": null, 
				    "width": "50px",
				    "title": "序号",
				    "orderable": false,  
				    "defaultContent": ""
				},
				{  
				    "data": null, 
				    "width": "200px",
				    "title": "&nbsp;操作&nbsp;",
				    "orderable": false,  
				    "defaultContent": "<a class='edit' href='javascript:;'>编辑</a>|<a class='delete' href='javascript:;'>删除</span>"
				},
                { 
                	"data": "id", 
                	"title": "编号",
                	"width": "200px",
                	"name":"testname",//参考：http://datatables.net/reference/type/column-selector
	                "editField": "id",
	                "orderable": false,
	                "defaultContent": ""
                }, 
                { 
                	"data": "nickname",  
                	"title": "名称",
                	"width": "200px",
                	"render": function ( data, type, full, meta ){
                        return '<a href="'+data+'">' + data + '</a>';  
                    } ,
                    "defaultContent": ""
                }, 
                { 
                	"data": "phone", 
                	"title": "电话",
                	"width": "200px",
                	"createdCell": function (td, cellData, rowData, row, col) {
                        if ( row < 1 ) {
                          $(td).css('color', 'red');
                        }
                    },
                    "defaultContent": ""
                }, 
                { 
                	"data": "address",  
                	"title": "地址",
                	"width": "300",
                	"searchable": true,
                	"defaultContent": ""
                }, 
                { 
                	"data": "create_time",  
                	"title": "开始日期",
                	"type": "date",
                	"width": "100px",
                	"defaultContent": ""
                },
                { 
                	"data": "auth_flag",  
                	"width": "200px",
                	"title": "角色",
                	"defaultContent": "",
                	"render": function ( data, type, full, meta ){
                        if(data == "1"){
                        	return "车场管理员";
                        } else if(data == "2"){
                        	return "车场收费员";
                        }
                    }
                },
                { 
                	"data": null,  
                	"title": "性别",
                	"width": "200px",
                	"defaultContent": ""
                },
                { 
                	"data": null,  
                	"title": "职业1",
                	"width": "200px",
                	"defaultContent": ""
                },
                { 
                	"data": null,  
                	"title": "职业2",
                	"width": "200px",
                	"defaultContent": ""
                },
                { 
                	"data": null,  
                	"title": "职业3",
                	"width": "200px",
                	"defaultContent": ""
                },
                { 
                	"data": null,  
                	"title": "职业4",
                	"width": "200px",
                	"defaultContent": ""
                },
                { 
                	"data": null,  
                	"title": "职业5",
                	"width": "200px",
                	"defaultContent": ""
                },
                { 
                	"data": null,  
                	"title": "职业6",
                	"width": "200px",
                	"defaultContent": ""
                }
            ],
            
            "createdRow": function( row, data, dataIndex ) {
//            	alert("createdRow");
	            if ( data.name == "2" ) {
	            	$(row).addClass( 'important' );
	            }
            },
            "drawCallback": function( settings ) {
 //           	alert("drawCallback");
            	var pageInfo = this.api().page.info();
            	this.api().column(0, {
    	            search: 'applied',
    	            order: 'applied'
    	        }).nodes().each(function(cell, i) {
    	        	debugger
    	            cell.innerHTML = i + 1;
    	        });
            },
            "headerCallback": function( thead, data, start, end, display ) {
//                $(thead).find('th').eq(0).html( 'Displaying '+(end-start)+' records' );
            },
            "infoCallback": function( settings, start, end, max, total, pre ) {
 //               alert("infoCallback");
            	
            },
            "initComplete": function(settings, json) {
//            	alert("initComplete");
            	$("div.btn-group").html('<button id="sample_editable_1_new" class="btn green"> 添加<i class="fa fa-plus"></i></button>');
            },
            "preDrawCallback": function( settings ) {
//                alert("preDrawCallback");
            },
	        "rowCallback": function( row, data, index ) {
//        	    alert("rowCallback");
            },
 /*           "autoFill": {
                focus: 'click',
                columns: [ 1, 2, 3,4 ],
                update: false
            },*/
            "buttons": {
                buttons: [
                    {
                        text: '导出表格',
                        action: function ( e, dt, node, config ) {
                        	
                        },
                        className: 'btn red btn-outline',
                        disable: false,
                        init: function ( dt, node, config ) {
                            var that = this;
                            dt.on( 'select.dt.DT deselect.dt.DT', function () {
                                that.enable( dt.rows( { selected: true } ).any() );
                            } );
                            this.disable();
                        }
                    },
                    {
                        text: '打印页面',
                        action: function ( e, dt, node, config ) {
                            alert( 'Activated!' );
 //                           this.disable(); // disable button
                        },
                        className: 'btn green btn-outline',
                        titleAttr: '会打印页面'
                    },
                    { 
                    	extend: 'copy', 
                    	className: 'btn dark btn-outline', 
                    	text:'导出word',
                    	action: function ( e, dt, node, config ) {
                    		
                        },
                    }
                ]/*,
                dom: {
                    container: {
                        tag: 'ul'
                    },
                    buttonContainer: {
                        tag: 'li',
                        className: 'button-item'
                    },
                    button: {
                        tag: 'a'
                    },
                    buttonLiner: {
                        tag: 'i'
                    }
                }*/
            },
//            "fixedColumns": {
//                leftColumns: 2
//            },
            "serverSide": true,
            "ajax":{
            	"url": "test.do?action=getorgtype", // ajax URL
                "type": "POST", // request type
                "timeout": 20000,
                "data": function(data) { // add request parameters before submit
                    data.name="whx";
                }
            },
            "lengthMenu": [
                [1 ,10, 15, 20, -1],
                [1 ,10, 15, 20, "All"] // change per page values here
            ],
            // set the initial value
            "pageLength": 10,
            
//            "rowId": 'staffId',
//            "retrieve": true,//这里获得的是之前的Datatables实例而不是重新实例化的
            "renderer": "bootstrap",

            "dom": "<'row' <'col-md-12'B>><'row'<'col-md-6 col-sm-12'l><'col-md-6 col-sm-12'f>r><'table-scrollable't><'row'<'col-md-5 col-sm-12'i><'col-md-7 col-sm-12'p>>", // horizobtal scrollable datatable
//            "dom": "<'row' <'col-md-12'B>><'row'<'col-md-6 col-sm-12'l><'col-md-6 col-sm-12'f>r><'table-scrollable't><'row'<'col-md-5 col-sm-12'i><'col-md-7 col-sm-12'p>>", // horizobtal scrollable datatable
            // Uncomment below line("dom" parameter) to fix the dropdown overflow issue in the datatable cells. The default datatable layout
            // setup uses scrollable div(table-scrollable) with overflow:auto to enable vertical scroll(see: assets/global/plugins/datatables/plugins/bootstrap/dataTables.bootstrap.js). 
            // So when dropdowns used the scrollable div should be removed. 
            //"dom": "<'row' <'col-md-12'T>><'row'<'col-md-6 col-sm-12'l><'col-md-6 col-sm-12'f>r>t<'row'<'col-md-5 col-sm-12'i><'col-md-7 col-sm-12'p>>",
        	
        });
//        oTable.on('click', 'tr', function () {
//        	$(this).toggleClass('selected');
//        });
//        
        oTable.api().on('order.dt search.dt', function() {
    	 	oTable.column(0, {
	            search: 'applied',
	            order: 'applied'
	        }).nodes().each(function(cell, i) {
	            cell.innerHTML = i + 1;
	        });
    	}).draw();
        
        
        function restoreRow(oTable, nRow) {
            var aData = oTable.fnGetData(nRow);
            var jqTds = $('>td', nRow);

            for (var i = 0, iLen = jqTds.length; i < iLen; i++) {
                oTable.fnUpdate(aData[i], nRow, i, false);
            }

            oTable.fnDraw();
        }

        function editRow(oTable, nRow) {
            var aData = oTable.fnGetData(nRow);
            var jqTds = $('>td', nRow);
            jqTds[0].innerHTML = '<input type="text" class="form-control input-small" value="' + aData[0] + '">';
            jqTds[1].innerHTML = '<input type="text" class="form-control input-small" value="' + aData[1] + '">';
            jqTds[2].innerHTML = '<input type="text" class="form-control input-small" value="' + aData[2] + '">';
            jqTds[3].innerHTML = '<input type="text" class="form-control input-small" value="' + aData[3] + '">';
            jqTds[4].innerHTML = '<a class="edit" href="">Save</a>';
            jqTds[5].innerHTML = '<a class="cancel" href="">Cancel</a>';
        }

        function saveRow(oTable, nRow) {
            var jqInputs = $('input', nRow);
            oTable.fnUpdate(jqInputs[0].value, nRow, 0, false);
            oTable.fnUpdate(jqInputs[1].value, nRow, 1, false);
            oTable.fnUpdate(jqInputs[2].value, nRow, 2, false);
            oTable.fnUpdate(jqInputs[3].value, nRow, 3, false);
            oTable.fnUpdate('<a class="edit" href="">Edit</a>', nRow, 4, false);
            oTable.fnUpdate('<a class="delete" href="">Delete</a>', nRow, 5, false);
            oTable.fnDraw();
        }

        function cancelEditRow(oTable, nRow) {
            var jqInputs = $('input', nRow);
            oTable.fnUpdate(jqInputs[0].value, nRow, 0, false);
            oTable.fnUpdate(jqInputs[1].value, nRow, 1, false);
            oTable.fnUpdate(jqInputs[2].value, nRow, 2, false);
            oTable.fnUpdate(jqInputs[3].value, nRow, 3, false);
            oTable.fnUpdate('<a class="edit" href="">Edit</a>', nRow, 4, false);
            oTable.fnDraw();
        }
        
        var nEditing = null;
        var nNew = false;

        $('#sample_editable_1_new').click(function (e) {
            e.preventDefault();
            debugger
            if (nNew && nEditing) {
                if (confirm("Previose row not saved. Do you want to save it ?")) {
                    saveRow(oTable, nEditing); // save
                    $(nEditing).find("td:first").html("Untitled");
                    nEditing = null;
                    nNew = false;

                } else {
                    oTable.fnDeleteRow(nEditing); // cancel
                    nEditing = null;
                    nNew = false;
                    
                    return;
                }
            }

            var aiNew = oTable.fnAddData(['', '', '', '', '', '']);
            var nRow = oTable.fnGetNodes(aiNew[0]);
            editRow(oTable, nRow);
            nEditing = nRow;
            nNew = true;
        });

        table.on('click', '.delete', function (e) {
            e.preventDefault();

            if (confirm("确定删除 ?") == false) {
                return;
            }

            var nRow = $(this).parents('tr')[0];
            oTable.fnDeleteRow(nRow);
            alert("Deleted! Do not forget to do some ajax to sync with backend :)");
        });

        table.on('click', '.cancel', function (e) {
            e.preventDefault();
            if (nNew) {
                oTable.fnDeleteRow(nEditing);
                nEditing = null;
                nNew = false;
            } else {
                restoreRow(oTable, nEditing);
                nEditing = null;
            }
        });

        table.on('click', '.edit', function (e) {
            e.preventDefault();
            debugger
            /* Get the row as a parent of the link that was clicked on */
            var nRow = $(this).parents('tr')[0];

            if (nEditing !== null && nEditing != nRow) {
                /* Currently editing - but not this row - restore the old before continuing to edit mode */
                restoreRow(oTable, nEditing);
                editRow(oTable, nRow);
                nEditing = nRow;
            } else if (nEditing == nRow && this.innerHTML == "Save") {
                /* Editing this row and want to save it */
                saveRow(oTable, nEditing);
                nEditing = null;
                alert("Updated! Do not forget to do some ajax to sync with backend :)");
            } else {
                /* No edit in progress - let's start one */
                editRow(oTable, nRow);
                nEditing = nRow;
            }
        });

    }

    return {

        //main function to initiate the module
        init: function () {

            if (!jQuery().dataTable) {
                return;
            }

            initTable1();
        }

    };

}();

jQuery(document).ready(function() {
    TableDatatablesScroller.init();
});