
var TableDatatablesEditable = function () {

    var handleTable = function () {

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

        var table = $('#sample_editable_1');

        var oTable = table.dataTable({
        	//参考：http://blog.csdn.net/yibing548/article/details/45078123
        	//http://datatables.club/reference/#options
            // Uncomment below line("dom" parameter) to fix the dropdown overflow issue in the datatable cells. The default datatable layout
            // setup uses scrollable div(table-scrollable) with overflow:auto to enable vertical scroll(see: assets/global/plugins/datatables/plugins/bootstrap/dataTables.bootstrap.js). 
            // So when dropdowns used the scrollable div should be removed. 
            //"dom": "<'row'<'col-md-6 col-sm-12'l><'col-md-6 col-sm-12'f>r>t<'row'<'col-md-5 col-sm-12'i><'col-md-7 col-sm-12'p>>",

            "lengthMenu": [
                [5, 10, 20, -1],
                [5, 10, 20, "All"] // change per page values here
            ],

            // set the initial value
            "pageLength": 5,//初始化时默认显示的记录条数
            "bFilter": false,//是否启动过滤、搜索功能  
            "bProcessing": true,//显示是否加载
            "bServerSide": true, //开启服务器模式，使用服务器端处理配置datatable。注意：sAjaxSource参数也必须配置。开启此模式后，你对datatables的每个操作 每页显示多少条记录、下一页、上一页、排序（表头）、搜索，这些都会传给服务器相应的值。
            "sAjaxSource":"test.do?action=getorgtype",
            "sServerMethod":"POST",//设置使用Ajax方式调用的服务器端的处理方法或者Ajax数据源的HTTP请求方式
            "fnServerParams": function (aoData){
                aoData.push({ "name": "more_data", "value": "my_value" });
              },
            "fnServerData": function (sSource, aoData, fnCallback, oSettings){
            	$.ajax( {
            		"dataType": 'json',
            		"type": "POST",
            		"url": sSource,
            		"data": aoData,
            		"success": fnCallback
            		} );
              },//这个是结合服务器模式的回调函数，用来处理服务器返回过来的数据
            
	        "fnRowCallback" : function(nRow, aData, iDisplayIndex) {  //行的回调函数
	        	
	            /* 用来改写用户权限的 */  
	            /*if (aData.ISADMIN == '1')  
	                $('td:eq(5)', nRow).html('管理员');  
	            if (aData.ISADMIN == '2')  
	                $('td:eq(5)', nRow).html('资料下载');  
	            if (aData.ISADMIN == '3')  
	                $('td:eq(5)', nRow).html('一般用户');  */
	              
	            return nRow;  
	        }, 
            "bStateSave" : false, //是否打开客户端状态记录功能,此功能在ajax刷新纪录的时候不会将个性化设定回复为初始化状态  ;状态保存，使用了翻页或者改变了每页显示数据数量，会保存在cookie中，下回访问时会显示上一次关闭页面时的内容。
            "bJQueryUI" : false, //是否使用 jQury的UI theme
            "bAutoWidth" : false, //是否自适应宽度  
            "bLengthChange": false, //开启一页显示多少条数据的下拉菜单，允许用户从下拉框(10、25、50和100)，注意需要分页(bPaginate：true)。
            //"bScrollInfinite" : false, //是否启动初始化滚动条  
            "bScrollCollapse" : true, //是否开启DataTables的高度自适应，当数据条数不够分页数据条数的时候，插件高度是否随数据条数而改变  
            "bPaginate" : true, //是否显示（应用）分页器  
            "bInfo" : true, //是否显示页脚信息，DataTables插件左下角显示记录数  ;这个参数在bServerSide配置后需要用到,如果这个参数不传到后台去，服务器分页会报错，据说这个参数包含了表的所有信息
//            "sPaginationType" : "full_numbers", //详细分页组，可以支持直接跳转到某页  'full_numbers' or 'two_button', default 'two_button'
            "bSort" : false, //是否启动各个字段的排序功能 
            "bDeferRender": false,//根据官网的介绍翻译过来就是，延期渲染，可以有个速度的提升，当datatable 使用Ajax或者JS源表的数据。这个选项设置为true,将导致datatable推迟创建表元素每个元素,直到他们都创建完成——本参数的目的是节省大量的时间
            "bScrollInfinite":false,//是否开启不限制长度的滚动条（和sScrollY属性结合使用），不限制长度的滚动条意味着当用户拖动滚动条的时候DataTable会不断加载数据当数据集十分大的时候会有些用处，该选项无法和分页选项同时使用，分页选项会被自动禁止，注意，额外推荐的滚动条会优先与该选项
            "bSortClasses": true,//是否在当前被排序的列上额外添加sorting_1,sorting_2,sorting_3三个class，当该列被排序的时候，可以切换其背景颜色，该选项作为一个来回切换的属性会增加执行时间（当class被移除和添加的时候），当对大数据集进行排序的时候你或许希望关闭该选项
//            "sScrollX": "100%",//是否开启水平滚动，当一个表格过于宽以至于无法放入一个布局的时候，或者表格有太多列的时候，你可以开启该选项从而在一个可横向滚动的视图里面展示表格，该属性可以是css设置，或者一个数字（作为像素量度来使用）
//            "sScrollY":"200px",//是否开启垂直滚动，垂直滚动会驱使DataTable设置为给定的长度，任何溢出到当前视图之外的数据可以通过垂直滚动进行察看当在小范围区域中显示大量数据的时候，可以在分页bPaginate和垂直滚动中选择一种方式，该属性可以是css设置，或者一个数字（作为像素量度来使用）
//            "iDisplayStart":5,//当开启分页的时候，定义展示的记录的起始序号，不是页数，因此如果你每个分页有10条记录而且想从第三页开始，需要把该参数指定为20
//            "iScrollLoadGap":50,//滚动余界是指DataTable在当前页面还有多少条数据可供滚动时自动加载新的数据，你可能希望指定一个足够大的余界，以便滚动加载数据的操作对用户来说是平滑的，同时也不会大到加载比需要的多的多的数据
//            "sDom": '',这是用于定义DataTable布局的一个强大的属性，包括分页，显示多少条数据和搜索
            
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
                    "sLast": "尾页"  
                }
            },
            /*"aoColumns" : [{  
                "mDataProp" : "id",  
                "sDefaultContent" : "", //此列默认值为""，以防数据中没有此值，DataTables加载数据的时候报错  
                "bVisible" : true //此列不显示  
            }, {  
                "mDataProp" : "nickname",  
                "sTitle" : "用户名",  
                "sDefaultContent" : "",  
                "sClass" : "center"  
            }, {  
                "mDataProp" : "email",  
                "sTitle" : "电子邮箱",  
                "sDefaultContent" : "",  
                "sClass" : "center"  
            }, {  
                "mDataProp" : "mobile",  
                "sTitle" : "手机",  
                "sDefaultContent" : "",  
                "sClass" : "center"  
            }, {  
                "mDataProp" : "balance",  
                "sTitle" : "余额",  
                "sDefaultContent" : "",  
                "sClass" : "center"  
            }], */
            "columnDefs": [{ // set default column settings
                'orderable': true,
                'targets': [0]
            }, {
                "searchable": true,
                "targets": [0]
            }],
            "order": [
                [0, "asc"]
            ] // set first column as a default sort by asc
        });

        var tableWrapper = $("#sample_editable_1_wrapper");

        var nEditing = null;
        var nNew = false;

        $('#sample_editable_1_new').click(function (e) {
            e.preventDefault();

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
            handleTable();
        }

    };

}();

jQuery(document).ready(function() {
    TableDatatablesEditable.init();
});