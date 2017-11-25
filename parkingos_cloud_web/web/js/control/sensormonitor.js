function init(parkinfo){
	var comid = null;
	$.each(eval(parkinfo), function(i, value){
		if(value.id != "-1"){
			if(comid == null){
				comid = value.id;
			}
			$("#parkselect").append('<option value="'+value.id+'">' + value.company_name + '</option>');
		}
	});
	if(comid != null){
		loadberthseg(comid);
	}
}

function loadberthseg(comid){
	var url = "getdata.do?action=getberthseg&id="+comid;
	$.post(url, function(result) {
		renderberthseg(result);
	});
}

function renderberthseg(result){
	$("#berthseg")[0].options.length = 0;
	var berthsegid = null;
	$.each(eval(result), function(i, value){
		if(value.value_no != "-1"){
			if(berthsegid == null){
				berthsegid = value.id;
			}
			$("#berthseg").append('<option value="'+value.value_no+'">' + value.value_name + '</option>');
		}
	});
	 $("#berthseg option[value='"+berthsegid+"'] ").attr("selected",true)
}

$("#parkselect").bind("change",function(){
	loadberthseg(this.value);
});

function render(){
	$("#ul-1")[0].innerHTML = "";
	$("#ul-2")[0].innerHTML = "";
	$("#ul-3")[0].innerHTML = "";
	$("#line-1").addClass("my-hide");
	$("#line-2").addClass("my-hide");
	$("#line-3").addClass("my-hide");
	var berthsegid = $('#berthseg').val();
	var sensorstate = $('#sensorstate').val();
	url = "sensorberth.do?action=querysensor&berthseg_id="+berthsegid+"&sensor_state_start="+sensorstate;
	$.post(url, function(result) {
		$.each(eval(result), function(i, value){
			var sensorstate = "心跳正常";
			var sensor_bg = "bg-green";
			var sensor_state = value.sensor_state;
			var sensor_id = value.id;
			var item = 1;
			var face = "fa fa-smile-o";
			var face_bg = "font-green";
			if(sensor_state == "1"){
				face_bg = "font-red";
				face = "fa fa-frown-o";
				item = 3;
				sensorstate = "心跳异常";
				sensor_bg = "bg-red";
			}
			var sensor_in_time = "";
			if(value.sensor_in_time != undefined){
				sensor_in_time = value.sensor_in_time;
			}
			var car_number = "";
			if(value.car_number != undefined){
				car_number = value.car_number;
			}
			var berth_flag = "";
			var order_in_time = "";
			if(value.order_in_time != undefined){
				order_in_time = value.order_in_time;
				berth_flag = " bg-grey";
			}
			var sensor_flag = "";
			var heartbeat_time = "";
			if(value.heartbeat_time != undefined){
				heartbeat_time = value.heartbeat_time;
				sensor_flag = " bg-grey";
			}
			var did = "";
			if(value.did != undefined){
				did = value.did;
			}
			var battery_icon = "fa fa-battery-4";
			if(value.battery >= 3.7){
				battery_icon = "fa fa-battery-full";
			}else if(value.battery >= 3.5){
				battery_icon = "fa fa-battery-3";
			}else if(value.battery >= 3.3){
				battery_icon = "fa fa-battery-half";
			}else if(value.battery >= 3){
				battery_icon = "fa fa-battery-1";
			}else{
				battery_icon = "fa fa-battery-0";
			}
			var content = '<li class="mt-list-item">'+
                			'<div class="list-todo-icon bg-white '+face_bg+'">'+
                				'<i class="'+face+'"></i>'+
                			'</div>'+
					        '<div class="list-todo-item item-'+item+'">'+
					            '<a class="list-toggle-container font-white" data-toggle="collapse" href="#task-'+sensor_id+'" aria-expanded="false">'+
					                '<div class="list-toggle done uppercase '+sensor_bg+'">'+
					                    '<div class="list-toggle-title bold">最近心跳时间：'+heartbeat_time+'</div>'+
					                    '<div class="pull-right font-white bold">'+sensorstate+'</div>'+
					                '</div>'+
					            '</a>'+
					            '<div class="task-list panel-collapse collapse in" id="task-'+sensor_id+'">'+
					                '<ul>'+
					                    '<li class="task-list-item done '+sensor_flag+'">'+
					                        '<div class="task-icon">'+
					                            '<a href="javascript:;">'+
					                                '<i class="fa fa-dot-circle-o"></i>'+
					                            '</a>'+
					                        '</div>'+
					                        '<div class="task-status">'+
					                            '<a class="pending" href="javascript:;">'+
					                                '<i class="'+battery_icon+'"></i>'+
					                            '</a>'+
					                        '</div>'+
					                        '<div class="task-content">'+
					                            '<h4 class="uppercase bold">'+
					                                '<a href="javascript:;">车检器编号：'+did+'</a>'+
					                            '</h4>'+
					                            '<p>进场时间：'+sensor_in_time+'</p>'+
					                        '</div>'+
					                    '</li>'+
					                    '<li class="task-list-item '+berth_flag+'">'+
					                        '<div class="task-icon">'+
					                            '<a href="javascript:;">'+
					                                '<i class="fa fa-car"></i>'+
					                            '</a>'+
					                        '</div>'+
					                        '<div class="task-content">'+
					                           '<h4 class="uppercase bold">'+
					                                '<a href="javascript:;">泊位编号：'+value.cid+'</a>'+
					                            '</h4>'+
					                            '<p>车牌号：'+car_number+'</p>'+
					                            '<p>进场时间：'+order_in_time+'</p>'+
					                        '</div>'+
					                    '</li>'+
					                '</ul>'+
					            '</div>'+
					        '</div>'+
					    '</li>';
			var k = i%3;
			if(k == 0){
				$("#line-1").removeClass("my-hide");
				$("#ul-1").append(content);
			}else if(k == 1){
				$("#line-2").removeClass("my-hide");
				$("#ul-2").append(content);
			}else if(k == 2){
				$("#line-3").removeClass("my-hide");
				$("#ul-3").append(content);
			}
		});
	});
}
