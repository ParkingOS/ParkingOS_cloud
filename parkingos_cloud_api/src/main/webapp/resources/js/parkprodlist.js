var OFFSET = 5;
var page = 1;
var PAGESIZE = 9999;

var myScroll,
    pullDownEl, pullDownOffset,
    pullUpEl, pullUpOffset,
    generatedCount = 0;
var maxScrollY = 0;

var hasMoreData = false;
var today = new Date();
today.setHours(0);
today.setMinutes(0);
today.setSeconds(0);
today.setMilliseconds(0);
today = today/1000 + 24*60*60;//当天初始时间
var openid;
var uin;

document.addEventListener('touchmove', function(e) {
    e.preventDefault();
}, false);

document.addEventListener('DOMContentLoaded', function() {
    $(document).ready(function() {
        //var mobile=$("#mobile")[0].value;
        openid=$("#openid")[0].value;
        uin=$("#uin")[0].value;
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
    pullDownEl.querySelector('.pullDownLabel').innerHTML = '加载中...';

    page = 1;
    $.post("getwxprodlist", {
            "page": page,
            "pagesize": PAGESIZE,
            "uin" : uin,
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

                $("#thelist").empty();
                $.each(response, function(key, value) {
                    var end_time = value.end_time;
                    var car_owner_product_id = value.car_owner_product_id;//云端月卡记录编号
                    var com_id = value.com_id;
                    var card_id = value.card_id;
                    var car_number = value.car_number;
                    var mid = value.mid;
                    var state = value.state;//0：未开始 1:使用中 2已过期
                    var park_name = value.park_name;//停车场名称
                    var prod_name = value.prod_name;//套餐名称
                    var price = value.price;//套餐单价
                    var prod_id = value.prod_id;
                    //var car_owner_product_id = value.car_owner_product_id;//套餐单价
                    var limit_date = "有效期 " + value.limit_date;//有效期
                    //var limittime = "可用时段："+ value.limittime;//有效时段
                    var limitday = value.limitday;//剩余天数
                    var isthirdpay = value.isthirdpay;//是否去第三方支付
                    var money_class = "money";
                    var ticketname_class = "ticketname";
                    var ticketinfo_class = "ticketinfo";
                    var ticketlimit_class = "normal";
                    var line_class = "line";
                    var useinfo_class = "useinfoused";

                    var guoqi = "未使用";
                    if(state == 1){
                        guoqi = "使用中";
                    }else if(state == 2){
                        guoqi = "已过期";
                        money_class = "moneyused";
                        ticketname_class = "ticketnameused";
                        ticketinfo_class = "ticketinfoused";
                        ticketlimit_class = "normalused";
                        line_class = "lineuesd";
                        useinfo_class = "useinfoexp";
                    }
                    //var click=' onclick="rewand('+prod_id+','+car_owner_product_id+')"';
                    var click=' onclick="rewand(\''+card_id+'\',\''+prod_id+'\',\''+car_owner_product_id+'\',\''+com_id+'\',\''+end_time+'\',\''+park_name+'\',\''+car_number+'\')"';
                    $("#thelist").append('<li '+click+' class="li1"><div class="moneyouter"><span class="'+money_class+'">'+price+'<span class="fuhao">元</span></span></div><a class="a1" href="#"><div class="'+ticketname_class+'">'+
                        park_name+'</div><div class="ticketlimit"><div style="height:10px"></div><span class="sel_fee '+ticketlimit_class+'">'+prod_name+'</span></div><div style="height:8px"></div><div class="demo"><span>'+car_number+'</span></div></a><div class="rewand">续费</div></li>');
                    $("#thelist").append('</div><li class="li2"><div style="height:5px"><div class="'+line_class+'"></div><a class="a2" href="#"><div class="'+useinfo_class+'">'+guoqi+'</div><div class="limittime">'+limit_date+'</div></a></li>');

                });

                myScroll.refresh();
                if(response.length == 0){
                    $(".middle").removeClass("hide");
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
function rewand(card_id,prod_id,car_owner_product_id,com_id,end_time,park_name,car_number){
    console.log(park_name)
    park_name=encodeURI(park_name)
    car_number=encodeURI(encodeURI(car_number))
    console.log(park_name)
    var url = "tobuyprod?uin="+uin+"&card_id="+card_id+"&prod_id="+prod_id+"&car_owner_product_id="+car_owner_product_id+"&com_id="+com_id+"&type=1&end_time="+end_time+"&park_name="+park_name+"&car_number="+car_number;
    console.log(url)
    window.location.href = url;
}
function refresh() {
    var uin = $("#uin")[0].value;
    page = 1;
    $.post("getwxprodlist", {
            "page": page,
            "pagesize": PAGESIZE,
            "uin" : uin,
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

                $("#thelist").empty();
                $.each(response, function(key, value) {
                    var end_time = value.end_time;
                    var car_owner_product_id = value.car_owner_product_id;//云端月卡记录编号
                    var com_id = value.com_id;
                    var card_id = value.card_id;
                    var car_number = value.car_number;
                    var mid = value.mid;
                    var state = value.state;//0：未开始 1:使用中 2已过期
                    var park_name = value.park_name;//停车场名称
                    var prod_name = value.prod_name;//套餐名称
                    var price = value.price;//套餐单价
                    var prod_id = value.prod_id;
                    //var car_owner_product_id = value.car_owner_product_id;//套餐单价
                    var limit_date = "有效期 " + value.limit_date;//有效期
                    //var limittime = "可用时段："+ value.limittime;//有效时段
                    var limitday = value.limitday;//剩余天数
                    var isthirdpay = value.isthirdpay;//是否去第三方支付
                    var money_class = "money";
                    var ticketname_class = "ticketname";
                    var ticketinfo_class = "ticketinfo";
                    var ticketlimit_class = "normal";
                    var line_class = "line";
                    var useinfo_class = "useinfoused";

                    var guoqi = "未使用";
                    if(state == 1){
                        guoqi = "使用中";
                    }else if(state == 2){
                        guoqi = "已过期";
                        money_class = "moneyused";
                        ticketname_class = "ticketnameused";
                        ticketinfo_class = "ticketinfoused";
                        ticketlimit_class = "normalused";
                        line_class = "lineuesd";
                        useinfo_class = "useinfoexp";
                    }
                    //var click=' onclick="rewand('+prod_id+','+car_owner_product_id+')"';
                    var click=' onclick="rewand(\''+card_id+'\',\''+prod_id+'\',\''+car_owner_product_id+'\',\''+com_id+'\',\''+end_time+'\',\''+park_name+'\')"';
                    $("#thelist").append('<li '+click+' class="li1"><div class="moneyouter"><span class="'+money_class+'">'+price+'<span class="fuhao">元</span></span></div><a class="a1" href="#"><div class="'+ticketname_class+'">'+
                        park_name+'</div><div class="ticketlimit"><div style="height:10px"></div><span class="sel_fee '+ticketlimit_class+'">'+prod_name+'</span></div><div style="height:8px"></div><div class="demo"><span>'+car_number+'</span></div></a><div class="rewand">续费</div></li>');
                    $("#thelist").append('</div><li class="li2"><div style="height:5px"><div class="'+line_class+'"></div><a class="a2" href="#"><div class="'+useinfo_class+'">'+guoqi+'</div><div class="limittime">'+limit_date+'</div></a></li>');

                });
                myScroll.refresh();
                if(response.length == 0){
                    $(".middle").removeClass("hide");
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
    var uin = $("#uin")[0].value;
    page++;
    $.post("getwxprodlist", {
            "page": page,
            "pagesize": PAGESIZE,
            "openid" : uin,
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

                $("#thelist").empty();
                $.each(response, function(key, value) {
                    var end_time = value.end_time;
                    var car_owner_product_id = value.car_owner_product_id;//云端月卡记录编号
                    var com_id = value.com_id;
                    var card_id = value.card_id;
                    var car_number = value.car_number;
                    var mid = value.mid;
                    var state = value.state;//0：未开始 1:使用中 2已过期
                    var park_name = value.park_name;//停车场名称
                    var prod_name = value.prod_name;//套餐名称
                    var price = value.price;//套餐单价
                    var prod_id = value.prod_id;
                    //var car_owner_product_id = value.car_owner_product_id;//套餐单价
                    var limit_date = "有效期 " + value.limit_date;//有效期
                    //var limittime = "可用时段："+ value.limittime;//有效时段
                    var limitday = value.limitday;//剩余天数
                    var isthirdpay = value.isthirdpay;//是否去第三方支付
                    var money_class = "money";
                    var ticketname_class = "ticketname";
                    var ticketinfo_class = "ticketinfo";
                    var ticketlimit_class = "normal";
                    var line_class = "line";
                    var useinfo_class = "useinfoused";

                    var guoqi = "未使用";
                    if(state == 1){
                        guoqi = "使用中";
                    }else if(state == 2){
                        guoqi = "已过期";
                        money_class = "moneyused";
                        ticketname_class = "ticketnameused";
                        ticketinfo_class = "ticketinfoused";
                        ticketlimit_class = "normalused";
                        line_class = "lineuesd";
                        useinfo_class = "useinfoexp";
                    }
                    //var click=' onclick="rewand('+prod_id+','+car_owner_product_id+')"';
                    var click=' onclick="rewand(\''+card_id+'\',\''+prod_id+'\',\''+car_owner_product_id+'\',\''+com_id+'\',\''+end_time+'\',\''+park_name+'\')"';
                    $("#thelist").append('<li '+click+' class="li1"><div class="moneyouter"><span class="'+money_class+'">'+price+'<span class="fuhao">元</span></span></div><a class="a1" href="#"><div class="'+ticketname_class+'">'+
                        park_name+'</div><div class="ticketlimit"><div style="height:10px"></div><span class="sel_fee '+ticketlimit_class+'">'+prod_name+'</span></div><div style="height:8px"></div><div class="demo"><span>'+car_number+'</span></div></a><div class="rewand">续费</div></li>');
                    $("#thelist").append('</div><li class="li2"><div style="height:5px"><div class="'+line_class+'"></div><a class="a2" href="#"><div class="'+useinfo_class+'">'+guoqi+'</div><div class="limittime">'+limit_date+'</div></a></li>');

                });
                myScroll.refresh();
                if(response.length == 0){
                    $(".middle").removeClass("hide");
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
