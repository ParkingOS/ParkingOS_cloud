var OFFSET = 5;
var page = 1;
var PAGESIZE = 20;

var myScroll,
	pullDownEl, pullDownOffset,
	pullUpEl, pullUpOffset,
	generatedCount = 0;
var maxScrollY = 0;

var hasMoreData = false;



document.addEventListener('touchmove', function(e) {
	e.preventDefault();
}, false);

document.addEventListener('DOMContentLoaded', function() {
	$(document).ready(function() {
		//var mobile=$("#mobile")[0].value;
		//loaded(mobile);
		var openid=$("#openid")[0].value;
		loaded(openid);
	});
}, false);

function loaded(openid) {
	pullDownEl = document.getElementById('pullDown');
	pullDownOffset = pullDownEl.offsetHeight;
	pullUpEl = document.getElementById('pullUp');
	pullUpOffset = pullUpEl.offsetHeight;
	
	hasMoreData = false;
	// $("#thelist").hide();
	$("#pullUp").hide();

	pullDownEl.className = 'loading';
	pullDownEl.querySelector('.pullDownLabel').innerHTML = '加载中...';

	page = 1;
	$.post("wxpaccount.do", {
			"page": page,
			"size": PAGESIZE,
			"openid" : openid,
			"action" : "presentorderlist",
			"present" : 1,
			"r" : Math.random()
		},
		function(response, status) {
			if (status == "success") {
				$("#thelist").show();

				if (response.length < PAGESIZE) {
					hasMoreData = false;
					$("#pullUp").hide();
				} else {
					hasMoreData = true;
					$("#pullUp").show();
				}

				// document.getElementById('wrapper').style.left = '0';

				myScroll = new iScroll('wrapper', {
					useTransition: true,
					topOffset: pullDownOffset,
					onRefresh: function() {
						if (pullDownEl.className.match('loading')) {
							pullDownEl.className = 'idle';
							pullDownEl.querySelector('.pullDownLabel').innerHTML = '下拉刷新...';
							this.minScrollY = -pullDownOffset;
						}
						if (pullUpEl.className.match('loading')) {
							pullUpEl.className = 'idle';
							pullUpEl.querySelector('.pullUpLabel').innerHTML = '上拉加载更多...';
						}
					},
					onScrollMove: function() {
						if (this.y > OFFSET && !pullDownEl.className.match('flip')) {
							pullDownEl.className = 'flip';
							pullDownEl.querySelector('.pullDownLabel').innerHTML = '松手开始刷新...';
							this.minScrollY = 0;
						} else if (this.y < OFFSET && pullDownEl.className.match('flip')) {
							pullDownEl.className = 'idle';
							pullDownEl.querySelector('.pullDownLabel').innerHTML = '下拉刷新...';
							this.minScrollY = -pullDownOffset;
						} 
						if (this.y < (maxScrollY - pullUpOffset - OFFSET) && !pullUpEl.className.match('flip')) {
							if (hasMoreData) {
								this.maxScrollY = this.maxScrollY - pullUpOffset;
								pullUpEl.className = 'flip';
								pullUpEl.querySelector('.pullUpLabel').innerHTML = '松手开始刷新...';
							}
						} else if (this.y > (maxScrollY - pullUpOffset - OFFSET) && pullUpEl.className.match('flip')) {
							if (hasMoreData) {
								this.maxScrollY = maxScrollY;
								pullUpEl.className = 'idle';
								pullUpEl.querySelector('.pullUpLabel').innerHTML = '上拉加载更多...';
							}
						}
					},
					onScrollEnd: function() {
						if (pullDownEl.className.match('flip')) {
							pullDownEl.className = 'loading';
							pullDownEl.querySelector('.pullDownLabel').innerHTML = '加载中...';
							// pullDownAction(); // Execute custom function (ajax call?)
							refresh();
						}
						if (hasMoreData && pullUpEl.className.match('flip')) {
							pullUpEl.className = 'loading';
							pullUpEl.querySelector('.pullUpLabel').innerHTML = '加载中...';
							// pullUpAction(); // Execute custom function (ajax call?)
							nextPage();
						}
					}
				});

				$("#thelist").empty()
				$("#thelist").append('<div style="height:17px"></div>')
				$.each(response, function(key, value) {
					//[{"total":"10.0","parkname":"上地六街","orderid":"232","date":"2015-04-03"},{"
					var checked=''
					var lockhtml = '<span>未锁定</span>'
					var lockbtn = '锁定车辆'
					var payhtml = '缴费停车'
					if(value.islocked==1||value.islocked==5||value.islocked==4){
						checked = 'checked="true"'
						lockhtml = '<span style="color:red">已锁定</span>'
						lockbtn = '解锁车辆'
					}
					if(value.prestate==1){
						payhtml = '已预付'
					}
					
					$("#thelist").append(
					'<div class="weui-form-preview"><div class="weui-form-preview__hd"><label class="weui-form-preview__label">'
					+'<span style="font-size:20px;color:black">'+value.carnumber+'</span></label><em class="weui-form-preview__value">'
					+'<span style="font-size:20px;">￥'+value.total+'</span></em></div><div class="weui-form-preview__bd">'
					+'<div class="weui-form-preview__item"><label class="weui-form-preview__label">车场名称</label><span class="weui-form-preview__value">'
					+value.parkname+'</span></div>'
					+'<div class="weui-form-preview__item"><label class="weui-form-preview__label">入场时间</label><span class="weui-form-preview__value">'
					+value.date+'</span></div><div class="weui-form-preview__item"><label class="weui-form-preview__label">锁定状态</label>'
					+'<span id="lockhtml'+value.id+'" class="weui-form-preview__value">'+lockhtml+'</span>'
					+'</div></div><div class="weui-form-preview__ft">'
					+'<input id="lock'+value.id+'" type="checkbox" '+checked+' style="display:none">'
					+'<a id="l'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+lockbtn+'</a>'
					+'<a id="pay'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+payhtml+'</a></div></div>'
					+'</div></div><div style="height:15px"></div>'
					)
					$("#l"+value.id).click(function(event){
						 console.log(value.id,value.islocked)					
					     lock1(value.id,value.islocked);
					})
					//onclick="orderdetail(\''+value.comid+'\',\''+value.orderid+'\',\''+value.carnumber+'\')"
					if(value.status==0){
						$("#pay"+value.id).click(function(event){
							$.alert("泊链订单查询异常!")
						})
					}else if(value.status==3){
						$("#pay"+value.id).click(function(event){
							$.alert("网络异常,订单价格获取失败!")
						})
					}else if(value.prestate==1){
						$("#pay"+value.id).click(function(event){
							$.alert("您已预付过该订单!预付金额为"+value.pretotal+"元")
						})
					}else if(value.prestate==0){
						$("#pay"+value.id).click(function(event){
							orderdetail(value.comid,value.orderid,value.carnumber);
						})
					}
					
					//comid,orderid,carnumber
					//oid="+oid+"&uin="+uin+"&parkid="+comid+"&car_number"+carnumber+"&orderid="+orderid
				});
				
				//$("#thelist").listview("refresh");
				myScroll.refresh(); // Remember to refresh when contents are loaded (ie: on ajax completion)
				if(response.length == 0){
					$(".middle1").removeClass("hide1");
				}
				// pullDownEl.className = 'idle';
				// pullDownEl.querySelector('.pullDownLabel').innerHTML = 'Pull down to refresh...';
				// this.minScrollY = -pullDownOffset;

				if (hasMoreData) {
					myScroll.maxScrollY = myScroll.maxScrollY + pullUpOffset;
				} else {
					myScroll.maxScrollY = myScroll.maxScrollY;
				}
				maxScrollY = myScroll.maxScrollY;
			};
		},
		"json");
}


function refresh() {
	var openid=$("#openid")[0].value;
	page = 1;
	$.post("wxpaccount.do", {
			"page": page,
			"size": PAGESIZE,
			"openid" : openid,
			"present" : 1,
			"action" : "presentorderlist"
		},
		function(response, status) {
			if (status == "success") {
				$("#thelist").empty();
				$("#thelist").append('<div style="height:17px"></div>')
				myScroll.refresh();

				if (response.length < PAGESIZE) {
					hasMoreData = false;
					$("#pullUp").hide();
				} else {
					hasMoreData = true;
					$("#pullUp").show();
				}
				$.each(response, function(key, value) {
					//[{"total":"10.0","parkname":"上地六街","orderid":"232","date":"2015-04-03"},{"
					var checked=''
					var lockhtml = '<span>未锁定</span>'
					var lockbtn = '锁定车辆'
					var payhtml = '缴费停车'
					if(value.islocked==1||value.islocked==5||value.islocked==4){
						checked = 'checked="true"'
						lockhtml = '<span style="color:red">已锁定</span>'
						lockbtn = '解锁车辆'
					}
					if(value.prestate==1){
						payhtml = '已预付'
					}
					
					$("#thelist").append(
					'<div class="weui-form-preview"><div class="weui-form-preview__hd"><label class="weui-form-preview__label">'
					+'<span style="font-size:20px;color:black">'+value.carnumber+'</span></label><em class="weui-form-preview__value">'
					+'<span style="font-size:20px;">￥'+value.total+'</span></em></div><div class="weui-form-preview__bd">'
					+'<div class="weui-form-preview__item"><label class="weui-form-preview__label">车场名称</label><span class="weui-form-preview__value">'
					+value.parkname+'</span></div>'
					+'<div class="weui-form-preview__item"><label class="weui-form-preview__label">入场时间</label><span class="weui-form-preview__value">'
					+value.date+'</span></div><div class="weui-form-preview__item"><label class="weui-form-preview__label">锁定状态</label>'
					+'<span id="lockhtml'+value.id+'" class="weui-form-preview__value">'+lockhtml+'</span>'
					+'</div></div><div class="weui-form-preview__ft">'
					+'<input id="lock'+value.id+'" type="checkbox" '+checked+' style="display:none">'
					+'<a id="l'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+lockbtn+'</a>'
					+'<a id="pay'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+payhtml+'</a></div></div>'
					+'</div></div><div style="height:15px"></div>'
					)
					$("#l"+value.id).click(function(event){
						 console.log(value.id,value.islocked)					
					     lock1(value.id,value.islocked);
					})
					//onclick="orderdetail(\''+value.comid+'\',\''+value.orderid+'\',\''+value.carnumber+'\')"
					if(value.status==0){
						$("#pay"+value.id).click(function(event){
							$.alert("泊链订单查询异常!")
						})
					}else if(value.status==3){
						$("#pay"+value.id).click(function(event){
							$.alert("网络异常,订单价格获取失败!")
						})
					}else if(value.prestate==1){
						$("#pay"+value.id).click(function(event){
							$.alert("您已预付过该订单!预付金额为"+value.pretotal+"元")
						})
					}else if(value.prestate==0){
						$("#pay"+value.id).click(function(event){
							orderdetail(value.comid,value.orderid,value.carnumber);
						})
					}
				});
				// $("#thelist").listview("refresh");
				myScroll.refresh(); // Remember to refresh when contents are loaded (ie: on ajax completion)
				if(response.length == 0){
					$(".middle1").removeClass("hide1");
				}
				if (hasMoreData) {
					myScroll.maxScrollY = myScroll.maxScrollY + pullUpOffset;
				} else {
					myScroll.maxScrollY = myScroll.maxScrollY;
				}
				maxScrollY = myScroll.maxScrollY;
			};
		},
		"json");
}

function nextPage() {
	var openid=$("#openid")[0].value;
	page++;
	$.post("wxpaccount.do", {
			"page": page,
			"size": PAGESIZE,
			"openid" : openid,
			"present" : 1,
			"action" : "presentorderlist"
		},
		function(response, status) {
			if (status == "success") {
				if (response.length < PAGESIZE) {
					hasMoreData = false;
					$("#pullUp").hide();
				} else {
					hasMoreData = true;
					$("#pullUp").show();
				}
				$("#thelist").empty();
				$("#thelist").append('<div style="height:17px"></div>')
				$.each(response, function(key, value) {
					//[{"total":"10.0","parkname":"上地六街","orderid":"232","date":"2015-04-03"},{"
					var checked=''
					var lockhtml = '<span>未锁定</span>'
					var lockbtn = '锁定车辆'
					var payhtml = '缴费停车'
					if(value.islocked==1||value.islocked==5||value.islocked==4){
						checked = 'checked="true"'
						lockhtml = '<span style="color:red">已锁定</span>'
						lockbtn = '解锁车辆'
					}
					if(value.prestate==1){
						payhtml = '已预付'
					}
					
					$("#thelist").append(
					'<div class="weui-form-preview"><div class="weui-form-preview__hd"><label class="weui-form-preview__label">'
					+'<span style="font-size:20px;color:black">'+value.carnumber+'</span></label><em class="weui-form-preview__value">'
					+'<span style="font-size:20px;">￥'+value.total+'</span></em></div><div class="weui-form-preview__bd">'
					+'<div class="weui-form-preview__item"><label class="weui-form-preview__label">车场名称</label><span class="weui-form-preview__value">'
					+value.parkname+'</span></div>'
					+'<div class="weui-form-preview__item"><label class="weui-form-preview__label">入场时间</label><span class="weui-form-preview__value">'
					+value.date+'</span></div><div class="weui-form-preview__item"><label class="weui-form-preview__label">锁定状态</label>'
					+'<span id="lockhtml'+value.id+'" class="weui-form-preview__value">'+lockhtml+'</span>'
					+'</div></div><div class="weui-form-preview__ft">'
					+'<input id="lock'+value.id+'" type="checkbox" '+checked+' style="display:none">'
					+'<a id="l'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+lockbtn+'</a>'
					+'<a id="pay'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+payhtml+'</a></div></div>'
					+'</div></div><div style="height:15px"></div>'
					)
					$("#l"+value.id).click(function(event){
						 console.log(value.id,value.islocked)					
					     lock1(value.id,value.islocked);
					})
					//onclick="orderdetail(\''+value.comid+'\',\''+value.orderid+'\',\''+value.carnumber+'\')"
					if(value.status==0){
						$("#pay"+value.id).click(function(event){
							$.alert("泊链订单查询异常!")
						})
					}else if(value.status==3){
						$("#pay"+value.id).click(function(event){
							$.alert("网络异常,订单价格获取失败!")
						})
					}else if(value.prestate==1){
						$("#pay"+value.id).click(function(event){
							$.alert("您已预付过该订单!预付金额为"+value.pretotal+"元")
						})
					}else if(value.prestate==0){
						$("#pay"+value.id).click(function(event){
							orderdetail(value.comid,value.orderid,value.carnumber);
						})
					}
				});
				
				// $("#thelist").listview("refresh");
				myScroll.refresh(); // Remember to refresh when contents are loaded (ie: on ajax completion)
				if(response.length == 0){
					$(".middle1").removeClass("hide1");
				}
				if (hasMoreData) {
					myScroll.maxScrollY = myScroll.maxScrollY + pullUpOffset;
				} else {
					myScroll.maxScrollY = myScroll.maxScrollY;
				}
				maxScrollY = myScroll.maxScrollY;
			};
		},
		"json");
}
////oid="+oid+"&uin="+uin+"&parkid="+comid+"&car_number"+carnumber+"&orderid="+orderid
//park_id="+comid+"&order_id="+orderid+"&car_number="+carnumber
function orderdetail(comid,orderid,carnumber){
	var domain=$("#domain")[0].value;
	carnumber = Gb2312ToUtf8(carnumber)
	carnumber = encodeURI(carnumber)
	console.log(carnumber)
	var url = "http://"+domain+"/zld/wxpfast.do?action=topaypresentorder&park_id="+comid+"&car_number="+carnumber+"&order_id="+orderid
	//var u = "https://"+domain+"/zld/wxpfast.do?action=thirdsuccess2"
	//u = encodeURI(u)
	//var url = "http://s.tingchebao.com/zld/wxpfast.do?action=tothirdorderdetail&park_id="+comid+"&order_id="+orderid+"&car_number="+carnumber
	//+"&backurl="+u
	//var url = "http://jarvisqh.vicp.io/api-web/toorderprepay?park_id="+comid+"&order_id="+orderid+"&car_number="+carnumber
	//+"&backurl="+u//
	//var url = "http://"+domain+"/zld/wxpfast.do?action=toorderprepay&park_id="+comid+"&order_id="+orderid+"&car_number="+carnumber
	$.showLoading("跳转中...");
	//setTimeout('window.location.href = url',100)
	setTimeout(()=>{window.location.href = url},100)
	setTimeout(()=>{$.hideLoading()},5000)
	/*if(isthirdpay==1){
	}else{
		window.location.href = "http://"+domain+"/zld/wxpfast.do?action=sweepcom&openid="+openid+"&carnumber="+carnumber+"&comid="+comid+"&from=presentorderlist";
	}*/
}

function Gb2312ToUtf8(s1){  
    var s = escape(s1);  
    var sa = s.split("%");  
    var retV ="";  
    if(sa[0] != ""){  
      retV = sa[0];  
    }  
    for(var i = 1; i < sa.length; i ++){  
      if(sa[i].substring(0,1) == "u"){  
    retV += Hex2Utf8(Str2Hex(sa[i].substring(1,5)));  
   if(sa[i].length){  
    retV += sa[i].substring(5);  
   }  
      }  
      else{  
     retV += unescape("%" + sa[i]);  
   if(sa[i].length){  
    retV += sa[i].substring(5);  
   }  
   }  
    }  
    return retV;  
}  
function Hex2Utf8(s){  
    var retS = "";  
    var tempS = "";  
    var ss = "";  
    if(s.length == 16){  
    tempS = "1110" + s.substring(0, 4);  
    tempS += "10" + s.substring(4, 10);   
    tempS += "10" + s.substring(10,16);   
    var sss = "0123456789ABCDEF";  
    for(var i = 0; i < 3; i ++){  
       retS += "%";  
       ss = tempS.substring(i * 8, (eval(i)+1)*8);  
       retS += sss.charAt(Dig2Dec(ss.substring(0,4)));  
       retS += sss.charAt(Dig2Dec(ss.substring(4,8)));  
    }  
    return retS;  
    }  
    return "";  
}   

function Str2Hex(s){  
    var c = "";  
    var n;  
    var ss = "0123456789ABCDEF";  
    var digS = "";  
    for(var i = 0; i < s.length; i ++){  
   c = s.charAt(i);  
   n = ss.indexOf(c);  
   digS += Dec2Dig(eval(n));  
    }  
    return digS;  
}  

function Dig2Dec(s){  
    var retV = 0;  
    if(s.length == 4){  
    for(var i = 0; i < 4; i ++){  
        retV += eval(s.charAt(i)) * Math.pow(2, 3 - i);  
    }  
    return retV;  
    }  
    return -1;  
}   

function Dec2Dig(n1){  
    var s = "";  
    var n2 = 0;  
    for(var i = 0; i < 4; i++){  
   n2 = Math.pow(2,3 - i);  
   if(n1 >= n2){  
      s += '1';  
      n1 = n1 - n2;  
    }  
   else  
    s += '0';  
    }  
    return s;        
}  

function GB2312UTF8(){  
    this.Dig2Dec=function(s){  
          var retV = 0;  
          if(s.length == 4){  
          for(var i = 0; i < 4; i ++){  
              retV += eval(s.charAt(i)) * Math.pow(2, 3 - i);  
          }  
          return retV;  
          }  
          return -1;  
    }   
    this.Hex2Utf8=function(s){  
         var retS = "";  
         var tempS = "";  
         var ss = "";  
         if(s.length == 16){  
         tempS = "1110" + s.substring(0, 4);  
         tempS += "10" + s.substring(4, 10);   
         tempS += "10" + s.substring(10,16);   
         var sss = "0123456789ABCDEF";  
         for(var i = 0; i < 3; i ++){  
            retS += "%";  
            ss = tempS.substring(i * 8, (eval(i)+1)*8);  
            retS += sss.charAt(this.Dig2Dec(ss.substring(0,4)));  
            retS += sss.charAt(this.Dig2Dec(ss.substring(4,8)));  
         }  
         return retS;  
         }  
         return "";  
    }   
    this.Dec2Dig=function(n1){  
          var s = "";  
          var n2 = 0;  
          for(var i = 0; i < 4; i++){  
         n2 = Math.pow(2,3 - i);  
         if(n1 >= n2){  
            s += '1';  
            n1 = n1 - n2;  
          }  
         else  
          s += '0';  
          }  
          return s;        
    }  
  
    this.Str2Hex=function(s){  
          var c = "";  
          var n;  
          var ss = "0123456789ABCDEF";  
          var digS = "";  
          for(var i = 0; i < s.length; i ++){  
         c = s.charAt(i);  
         n = ss.indexOf(c);  
         digS += this.Dec2Dig(eval(n));  
          }  
          return digS;  
    }  
    this.Gb2312ToUtf8=function(s1){  
        var s = escape(s1);  
        var sa = s.split("%");  
        var retV ="";  
        if(sa[0] != ""){  
          retV = sa[0];  
        }  
        for(var i = 1; i < sa.length; i ++){  
          if(sa[i].substring(0,1) == "u"){  
        retV += this.Hex2Utf8(this.Str2Hex(sa[i].substring(1,5)));  
       if(sa[i].length){  
        retV += sa[i].substring(5);  
       }  
          }  
          else{  
         retV += unescape("%" + sa[i]);  
       if(sa[i].length){  
        retV += sa[i].substring(5);  
       }  
       }  
        }  
        return retV;  
    }  
    this.Utf8ToGb2312=function(str1){  
        var substr = "";  
        var a = "";  
        var b = "";  
        var c = "";  
        var i = -1;  
        i = str1.indexOf("%");  
        if(i==-1){  
          return str1;  
        }  
        while(i!= -1){  
        if(i<3){  
            substr = substr + str1.substr(0,i-1);  
            str1 = str1.substr(i+1,str1.length-i);  
            a = str1.substr(0,2);  
            str1 = str1.substr(2,str1.length - 2);  
            if(parseInt("0x" + a) & 0x80 == 0){  
              substr = substr + String.fromCharCode(parseInt("0x" + a));  
            }  
            else if(parseInt("0x" + a) & 0xE0 == 0xC0){ //two byte  
                b = str1.substr(1,2);  
                str1 = str1.substr(3,str1.length - 3);  
                var widechar = (parseInt("0x" + a) & 0x1F) << 6;  
                widechar = widechar | (parseInt("0x" + b) & 0x3F);  
                substr = substr + String.fromCharCode(widechar);  
            }  
            else{  
                b = str1.substr(1,2);  
                str1 = str1.substr(3,str1.length - 3);  
                c = str1.substr(1,2);  
                str1 = str1.substr(3,str1.length - 3);  
                var widechar = (parseInt("0x" + a) & 0x0F) << 12;  
                widechar = widechar | ((parseInt("0x" + b) & 0x3F) << 6);  
                widechar = widechar | (parseInt("0x" + c) & 0x3F);  
                substr = substr + String.fromCharCode(widechar);  
            }  
         }  
         else {  
          substr = substr + str1.substring(0,i);  
          str1= str1.substring(i);  
         }  
              i = str1.indexOf("%");  
        }  
  
        return substr+str1;  
    }  
}  

function lock1(orderid,islocked){
	var lockstatus;
	if($("#lock"+orderid).is(":checked")){
		//解锁
		lockstatus = 0
		console.log('解锁')
		$.showLoading("解锁中...");
	}else{
		//锁定
		lockstatus = 1
		console.log('锁定')
		$.showLoading("锁定中...");
	}
	console.log(orderid,lockstatus)
	$.ajax({
		type:'post',
		url:'wxpaccount.do',
		data:{
			'lockstatus':lockstatus,
			'orderid':orderid,
			'action':'lockcar'
		},
		success:function(result){
			//隐藏loading
			$.hideLoading();
			var ret = eval('('+result+')')
			//ret.state: -2系统异常 -1通知处理失败  0解锁成功  1锁定成功  3锁定失败 5解锁失败 6已锁定 7未锁定 9车场离线
			if(ret.state==-2){
				//系统异常
				$.alert("系统异常!");
			}else if(ret.state==-1){
				//通知处理失败
				$.alert("网络异常!");
			}else if(ret.state==0){
				//解锁成功
				//修改按钮,改变checked状态
				$("#lock"+orderid).removeAttr("checked")
				$("#lockhtml"+orderid).empty()
				$("#l"+orderid).empty()
				$("#lockhtml"+orderid).append('<span>未锁定</span>')
				$("#l"+orderid).append('锁定车辆')
				$.alert("解锁成功!您的车辆已经处于解锁状态,可以正常出场");
			}else if(ret.state==1){
				//锁定成功
				//修改按钮
				$("#lock"+orderid).prop("checked",true)
				$("#lockhtml"+orderid).empty()
				$("#l"+orderid).empty()
				$("#lockhtml"+orderid).append('<span style="color:red">已锁定</span>')
				$("#l"+orderid).append('解锁车辆')
				$.alert("锁定成功!您的车辆已经处于锁定状态,请在出场前解锁,否则无法出场");
			}else if(ret.state==3){
				//锁定失败
				$.alert("锁定失败!请稍后再试或下拉刷新查看车辆状态!");
			}else if(ret.state==5){
				//解锁失败
				$.alert("解锁失败!请稍后再试或下拉刷新查看车辆状态;仍无法解锁请联系车场人员,解锁码:"+ret.lockKey);
			}else if(ret.state==6){
				//已锁定
				$.alert("您的车辆已处于锁定状态,下拉刷新车辆状态!");
			}else if(ret.state==7){
				//未锁定
				$.alert("您的车辆已处于未锁定状态,下拉刷新车辆状态!");
			}else if(ret.state==9){
				//车场离线
				$.alert("停车场处于断网状态,锁车失败!")
			}
		}
	})
}

//扩展Date的format方法   
Date.prototype.format = function (format) {  
  var o = {  
      "M+": this.getMonth() + 1,  
      "d+": this.getDate(),  
      "h+": this.getHours(),  
      "m+": this.getMinutes(),  
      "s+": this.getSeconds(),  
      "q+": Math.floor((this.getMonth() + 3) / 3),  
      "S": this.getMilliseconds()  
  }  
  if (/(y+)/.test(format)) {  
      format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));  
  }  
  for (var k in o) {  
      if (new RegExp("(" + k + ")").test(format)) {  
          format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));  
      }  
  }  
  return format;  
}

/**   
*转换long值为日期字符串   
* @param l long值   
* @param isFull 是否为完整的日期数据,   
*               为true时, 格式如"2000-03-05 01:05:04"   
*               为false时, 格式如 "2000-03-05"   
* @return 符合要求的日期字符串   
*/    

function getSmpFormatDateByLong(l, isFull) {  
   return getSmpFormatDate(new Date(l), isFull);  
}  

/**   
*转换日期对象为日期字符串   
* @param date 日期对象   
* @param isFull 是否为完整的日期数据,   
*               为true时, 格式如"2000-03-05 01:05:04"   
*               为false时, 格式如 "2000-03-05"   
* @return 符合要求的日期字符串   
*/    
function getSmpFormatDate(date, isFull) {  
    var pattern = "";  
    if (isFull == true || isFull == undefined) {  
        pattern = "yyyy-MM-dd hh:mm:ss";  
    } else {  
        pattern = "yyyy-MM-dd";  
    }  
    return getFormatDate(date, pattern);  
} 

/**   
 *转换日期对象为日期字符串   
 * @param l long值   
 * @param pattern 格式字符串,例如：yyyy-MM-dd hh:mm:ss   
 * @return 符合要求的日期字符串   
 */    
 function getFormatDate(date, pattern) {  
     if (date == undefined) {  
         date = new Date();  
     }  
     if (pattern == undefined) {  
         pattern = "yyyy-MM-dd hh:mm:ss";  
     }  
     return date.format(pattern);  
 }
 
function C$(id){return document.getElementById(id);}

//定义窗体对象
var cwxbox = {};

cwxbox.box = function(){
	var bg,wd,cn,ow,oh,o = true,time = null;
	return {
		show:function(c,t,w,h){
			if(o){
				bg = document.createElement('div'); bg.id = 'cwxBg';	
				wd = document.createElement('div'); wd.id = 'cwxWd';
				cn = document.createElement('div'); cn.id = 'cwxCn';
				document.body.appendChild(bg);
				document.body.appendChild(wd);
				wd.appendChild(cn);
				bg.onclick = cwxbox.box.hide;
				window.onresize = this.init;
				window.onscroll = this.scrolls;
				o = false;
			}
			if(w && h){
				var inhtml = '<iframe src="'+ c +'" width="'+ w +'" height="'+ h +'" frameborder="0"></iframe>';
			}else{
				var inhtml	 = c;
			}
			cn.innerHTML = inhtml;
			oh = this.getCss(wd,'offsetHeight');
			ow = this.getCss(wd,'offsetWidth');
			this.init();
			this.alpha(bg,50,1);
			this.drag(wd);
			if(t){
				time = setTimeout(function(){cwxbox.box.hide()},t*1000);
			}
		},
		hide:function(){
			cwxbox.box.alpha(wd,0,-1);
			clearTimeout(time);
		},
		init:function(){
			bg.style.height = cwxbox.page.total(1)+'px';
			bg.style.width = '';
			bg.style.width = cwxbox.page.total(0)+'px';
			var h = (cwxbox.page.height() - oh) /2;
			wd.style.top=(h+cwxbox.page.top())+'px';
			wd.style.left=(cwxbox.page.width() - ow)/2+'px';
		},
		scrolls:function(){
			var h = (cwxbox.page.height() - oh) /2;
			wd.style.top=(h+cwxbox.page.top())+'px';
		},
		alpha:function(e,a,d){
			clearInterval(e.ai);
			if(d==1){
				e.style.opacity=0; 
				e.style.filter='alpha(opacity=0)';
				e.style.display = 'block';
			}
			e.ai = setInterval(function(){cwxbox.box.ta(e,a,d)},40);
		},
		ta:function(e,a,d){
			var anum = Math.round(e.style.opacity*100);
			if(anum == a){
				clearInterval(e.ai);
				if(d == -1){
					e.style.display = 'none';
					if(e == wd){
						this.alpha(bg,0,-1);
					}
				}else{
					if(e == bg){
						this.alpha(wd,100,1);
					}
				}
			}else{
				var n = Math.ceil((anum+((a-anum)*.5)));
				n = n == 1 ? 0 : n;
				e.style.opacity=n/100;
				e.style.filter='alpha(opacity='+n+')';
			}
		},
		getCss:function(e,n){
			var e_style = e.currentStyle ? e.currentStyle : window.getComputedStyle(e,null);
			if(e_style.display === 'none'){
				var clonDom = e.cloneNode(true);
				clonDom.style.cssText = 'position:absolute; display:block; top:-3000px;';
				document.body.appendChild(clonDom);
				var wh = clonDom[n];
				clonDom.parentNode.removeChild(clonDom);
				return wh;
			}
			return e[n];
		},
		drag:function(e){
			var startX,startY,mouse;
			mouse = {
				mouseup:function(){
					if(e.releaseCapture)
					{
						e.onmousemove=null;
						e.onmouseup=null;
						e.releaseCapture();
					}else{
						document.removeEventListener("mousemove",mouse.mousemove,true);
						document.removeEventListener("mouseup",mouse.mouseup,true);
					}
				},
				mousemove:function(ev){
					var oEvent = ev||event;
					e.style.left = oEvent.clientX - startX + "px";  
					e.style.top = oEvent.clientY - startY + "px"; 
				}
			}
			e.onmousedown = function(ev){
				var oEvent = ev||event;
				startX = oEvent.clientX - this.offsetLeft;  
				startY = oEvent.clientY - this.offsetTop;
				if(e.setCapture)
				{
					e.onmousemove= mouse.mousemove;
					e.onmouseup= mouse.mouseup;
					e.setCapture();
				}else{
					document.addEventListener("mousemove",mouse.mousemove,true);
					document.addEventListener("mouseup",mouse.mouseup,true);	
				}
			} 
			
		}
	}
}()

cwxbox.page = function(){
	return{
		top:function(){return document.documentElement.scrollTop||document.body.scrollTop},
		width:function(){return self.innerWidth||document.documentElement.clientWidth||document.body.clientWidth},
		height:function(){return self.innerHeight||document.documentElement.clientHeight||document.body.clientHeight},
		total:function(d){
			var b=document.body, e=document.documentElement;
			return d?Math.max(Math.max(b.scrollHeight,e.scrollHeight),Math.max(b.clientHeight,e.clientHeight)):
			Math.max(Math.max(b.scrollWidth,e.scrollWidth),Math.max(b.clientWidth,e.clientWidth))
		}
	}	
}()
