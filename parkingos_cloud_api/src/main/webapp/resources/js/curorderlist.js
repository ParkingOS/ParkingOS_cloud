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
        //var uin=$("#uin")[0].value;
        var uin=$("#uin")[0].value;
        loaded(uin);
    });
}, false);

function loaded(uin) {
    pullDownEl = document.getElementById('pullDown');
    pullDownOffset = pullDownEl.offsetHeight;
    pullUpEl = document.getElementById('pullUp');
    pullUpOffset = pullUpEl.offsetHeight;

    hasMoreData = false;
    // $("#thelist").hide();
    $("#pullUp").hide();

    pullDownEl.className = 'loading';
    pullDownEl.querySelector('.pullDownLabel').innerHTML = '加载中....';

    page = 1;
    $.post("getwxcurorderlist", {
            "page": page,
            "size": PAGESIZE,
            "uin" : uin,
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
                //$("#thelist").append('<div style="height:17px"></div>')
                $.each(response, function(key, value) {
                    var checked=''
                    var lockhtml = '<span>未锁定</span>'
                    var lockbtn = '锁定车辆'
                    var payhtml = '缴费停车'
                    if(value.is_locked==1||value.is_locked==5||value.is_locked==4){
                        checked = 'checked="true"'
                        lockhtml = '<span style="color:red">已锁定</span>'
                        lockbtn = '解锁车辆'
                    }
                    if(value.prestate==1){
                        payhtml = '已预付'
                    }

                    $("#thelist").append(
                        '<div class="weui-form-preview"><div class="weui-form-preview__hd"><label class="weui-form-preview__label">'
                        +'<span style="font-size:20px;color:black">'+value.car_number+'</span></label><em class="weui-form-preview__value">'
                        +'<span style="font-size:20px;">￥'+value.total+'</span></em></div><div class="weui-form-preview__bd">'
                        +'<div class="weui-form-preview__item"><label class="weui-form-preview__label">车场名称</label><span class="weui-form-preview__value">'
                        +value.park_name+'</span></div>'
                        +'<div class="weui-form-preview__item"><label class="weui-form-preview__label">入场时间</label><span class="weui-form-preview__value">'
                        +value.in_park_time+'</span></div><div class="weui-form-preview__item"><label class="weui-form-preview__label">锁定状态</label>'
                        +'<span id="lockhtml'+value.id+'" class="weui-form-preview__value">'+lockhtml+'</span>'
                        +'</div></div><div class="weui-form-preview__ft">'
                        +'<input id="lock'+value.id+'" type="checkbox" '+checked+' style="display:none">'
                        +'<a id="l'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary" >'+lockbtn+'</a>'
                        +'<a id="pay'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+payhtml+'</a></div></div>'
                        +'</div></div><div style="height:15px"></div>'
                    )
                    $("#l"+value.id).click(function(event){
                        console.log(value.id,value.is_locked)
                        lock1(value.id,value.is_locked);
                    })
                    if(value.state!=1){
                        $("#pay"+value.id).click(function(event){
                            $.alert("网络异常,请稍后重试")
                        })
                    } else{
                        $("#pay"+value.id).click(function(event){
                            orderdetail(value.com_id,value.order_id,value.car_number);
                        })
                    }
                });

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


function refresh() {
    var uin=$("#uin")[0].value;
    page = 1;
    $.post("getwxcurorderlist", {
            "page": page,
            "size": PAGESIZE,
            "uin" : uin,
        },
        function(response, status) {
            if (status == "success") {
                $("#thelist").empty();
                //$("#thelist").append('<div style="height:17px"></div>')
                myScroll.refresh();

                if (response.length < PAGESIZE) {
                    hasMoreData = false;
                    $("#pullUp").hide();
                } else {
                    hasMoreData = true;
                    $("#pullUp").show();
                }
                $.each(response, function(key, value) {
                    var checked=''
                    var lockhtml = '<span>未锁定</span>'
                    var lockbtn = '锁定车辆'
                    var payhtml = '缴费停车'
                    if(value.is_locked==1||value.is_locked==5||value.is_locked==4){
                        checked = 'checked="true"'
                        lockhtml = '<span style="color:red">已锁定</span>'
                        lockbtn = '解锁车辆'
                    }
                    if(value.prestate==1){
                        payhtml = '已预付'
                    }

                    $("#thelist").append(
                        '<div class="weui-form-preview"><div class="weui-form-preview__hd"><label class="weui-form-preview__label">'
                        +'<span style="font-size:20px;color:black">'+value.car_number+'</span></label><em class="weui-form-preview__value">'
                        +'<span style="font-size:20px;">￥'+value.total+'</span></em></div><div class="weui-form-preview__bd">'
                        +'<div class="weui-form-preview__item"><label class="weui-form-preview__label">车场名称</label><span class="weui-form-preview__value">'
                        +value.park_name+'</span></div>'
                        +'<div class="weui-form-preview__item"><label class="weui-form-preview__label">入场时间</label><span class="weui-form-preview__value">'
                        +value.in_park_time+'</span></div><div class="weui-form-preview__item"><label class="weui-form-preview__label">锁定状态</label>'
                        +'<span id="lockhtml'+value.id+'" class="weui-form-preview__value">'+lockhtml+'</span>'
                        +'</div></div><div class="weui-form-preview__ft">'
                        +'<input id="lock'+value.id+'" type="checkbox" '+checked+' style="display:none">'
                        +'<a id="l'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+lockbtn+'</a>'
                        +'<a id="pay'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+payhtml+'</a></div></div>'
                        +'</div></div><div style="height:15px"></div>'
                    )
                    $("#l"+value.id).click(function(event){
                        console.log(value.id,value.is_locked)
                        lock1(value.id,value.is_locked);
                    })
                    if(value.state!=1){
                        $("#pay"+value.id).click(function(event){
                            $.alert("网络异常,请稍后重试")
                        })
                    } else{
                        $("#pay"+value.id).click(function(event){
                            orderdetail(value.com_id,value.order_id,value.car_number);
                        })
                    }
                });
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
    var uin=$("#uin")[0].value;
    page++;
    $.post("getwxcurorderlist", {
            "page": page,
            "size": PAGESIZE,
            "uin" : uin,
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
                //$("#thelist").append('<div style="height:17px"></div>')
                $.each(response, function(key, value) {
                    var checked=''
                    var lockhtml = '<span>未锁定</span>'
                    var lockbtn = '锁定车辆'
                    var payhtml = '缴费停车'
                    if(value.is_locked==1||value.is_locked==5||value.is_locked==4){
                        checked = 'checked="true"'
                        lockhtml = '<span style="color:red">已锁定</span>'
                        lockbtn = '解锁车辆'
                    }
                    if(value.prestate==1){
                        payhtml = '已预付'
                    }

                    $("#thelist").append(
                        '<div class="weui-form-preview"><div class="weui-form-preview__hd"><label class="weui-form-preview__label">'
                        +'<span style="font-size:20px;color:black">'+value.car_number+'</span></label><em class="weui-form-preview__value">'
                        +'<span style="font-size:20px;">￥'+value.total+'</span></em></div><div class="weui-form-preview__bd">'
                        +'<div class="weui-form-preview__item"><label class="weui-form-preview__label">车场名称</label><span class="weui-form-preview__value">'
                        +value.park_name+'</span></div>'
                        +'<div class="weui-form-preview__item"><label class="weui-form-preview__label">入场时间</label><span class="weui-form-preview__value">'
                        +value.in_park_time+'</span></div><div class="weui-form-preview__item"><label class="weui-form-preview__label">锁定状态</label>'
                        +'<span id="lockhtml'+value.id+'" class="weui-form-preview__value">'+lockhtml+'</span>'
                        +'</div></div><div class="weui-form-preview__ft">'
                        +'<input id="lock'+value.id+'" type="checkbox" '+checked+' style="display:none">'
                        +'<a id="l'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+lockbtn+'</a>'
                        +'<a id="pay'+value.id+'" class="weui-form-preview__btn weui-form-preview__btn_primary">'+payhtml+'</a></div></div>'
                        +'</div></div><div style="height:15px"></div>'
                    )
                    $("#l"+value.id).click(function(event){
                        console.log(value.id,value.is_locked)
                        lock1(value.id,value.is_locked);
                    })
                    if(value.state!=1){
                        $("#pay"+value.id).click(function(event){
                            $.alert("网络异常,请稍后重试")
                        })
                    } else{
                        $("#pay"+value.id).click(function(event){
                            orderdetail(value.com_id,value.order_id,value.car_number);
                        })
                    }
                });
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
function orderdetail(com_id,order_id,car_number){

    console.log(car_number)
    var url = "toprepaycurorder?com_id="+com_id+"&car_number="+car_number+"&order_id="+order_id
    window.location.href = url
}

function lock1(oid,is_locked){
    //重置隐藏
    $.hideLoading();

    var lockstatus;
    if($("#lock"+oid).is(":checked")){
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

    setTimeout(function(){  $.hideLoading(); }, 300);

    console.log(oid,lockstatus)
    setTimeout(function(){
        $.ajax({

            type:'post',
            url:'toadduser',
            data:{
                'lock_status':lockstatus,
                'oid':oid,
            },
            success:function(ret){
                //隐藏loading
                $.hideLoading();
                var lockStatus = ret.lock_status;
                var orderId = ret.oid;
                if(ret.state==-10){
                    //没有手机号
                    //$.alert("发送手机验证码异常,锁车失败!");
                    $.hideLoading();
                    location.href="adduser.do?lockStatus="+lockStatus+"&orderId="+orderId;
                }

                //ret.state: -2系统异常 -1通知处理失败  0解锁成功  1锁定成功  3锁定失败 5解锁失败 6已锁定 7未锁定 9车场离线
                else if(ret.state==-2){
                    //系统异常
                    $.alert("系统异常!");
                }else if(ret.state==-1){
                    //通知处理失败
                    $.alert("网络异常!");
                }else if(ret.state==0){
                    //解锁成功
                    //修改按钮,改变checked状态
                    $("#lock"+oid).removeAttr("checked")
                    $("#lockhtml"+oid).empty()
                    $("#l"+oid).empty()
                    $("#lockhtml"+oid).append('<span>未锁定</span>')
                    $("#l"+oid).append('锁定车辆')
                    $.alert("解锁成功!您的车辆已经处于解锁状态,可以正常出场");
                }else if(ret.state==1){
                    //锁定成功
                    //修改按钮
                    $("#lock"+oid).prop("checked",true)
                    $("#lockhtml"+oid).empty()
                    $("#l"+oid).empty()
                    $("#lockhtml"+oid).append('<span style="color:red">已锁定</span>')
                    $("#l"+oid).append('解锁车辆')
                    $.alert("锁定成功!您的车辆已经处于锁定状态,请在出场前解锁,否则无法出场");
                }else if(ret.state==3){
                    //锁定失败
                    $.alert("锁定失败!请稍后再试或下拉刷新查看车辆状态!");
                }else if(ret.state==5){
                    //解锁失败
                    $.alert("解锁失败!请稍后再试或下拉刷新查看车辆状态;仍无法解锁请联系车场人员,解锁码:"+ret.lock_key);
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
    },400)
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
