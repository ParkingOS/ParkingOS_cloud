//*****isScroll参数*****//
var OFFSET = 5;
var page = 1;
var PAGESIZE = 9999;

var myScroll,
	pullDownEl, pullDownOffset,
	pullUpEl, pullUpOffset,
	generatedCount = 0;
var maxScrollY = 0;
var hasMoreData = false;
//********************//
var timeout;
var t1 = null;
var choosedEle;
var username,password;
username = document.getElementById("username").value;
password = document.getElementById("password").value;

function checkhxuser(){//当前用户非localStorage存储的用户，清除掉所有信息
	var hxuser = window.localStorage.getItem("hxuser");
	if(hxuser != null && hxuser != username){
		var hxmsg = window.localStorage.getItem("hxmsg");
		if(hxmsg != null){
			window.localStorage.removeItem("hxmsg");
		}
	}
	window.localStorage.removeItem("hxuser");
	window.localStorage.setItem("hxuser",username);
}
if(username != "" && password != ""){//信息完整就检查当前登录用户和存储的用户是不是一个，不是一个就清掉之前的用户所有信息
	checkhxuser();
}

var conn = null;
conn = new Easemob.im.Connection();
var login = function(){
	conn.open({
	     apiUrl : Easemob.im.config.apiURL,
	     user : username,
	     pwd : password,
	     // 连接时提供appkey
	     appKey : Easemob.im.config.appkey
	});
};

function getImages(roster){
	if(roster.length > 0){
		var rosters;
		for(var i=0;i<roster.length;i++){
			if(i == 0){
				rosters = roster[i].name;
			}else{
				rosters += "," + roster[i].name;
			}
		}
		jQuery.ajax({
			type : "post",
			url : "wxpchat.do",
			data : {
				'action' : 'getimages',
				'rosters' : rosters,
				'r' : Math.random()
			},
			async : false,
			success : function(result) {
				var jsonData = eval("(" + result + ")");
				for(var i=0;i<jsonData.length;i++){
					var contacts = jsonData[i];
					var hxname = contacts.hx_name;
					var wxname = contacts.wx_name;
					var wximg = contacts.wx_imgurl;
					for(var j=0;j<roster.length;j++){
						var r = roster[j].name;
						if(r == hxname){
							roster[j].wxname = wxname;
							roster[j].wximg = wximg;
							break;
						}
					}
				}
			}
		});
	}
}

function loaded(roster) {
	pullDownEl = document.getElementById('pullDown');
	pullDownOffset = pullDownEl.offsetHeight;
	pullUpEl = document.getElementById('pullUp');
	pullUpOffset = pullUpEl.offsetHeight;
	hasMoreData = false;
	page = 1;
	$("#thelist").show();
	$("#pullDown").hide();
	myScroll = new iScroll('wrapper', {
		useTransition: true,
		topOffset: pullDownOffset,
		onRefresh: function() {
			
		},
		onScrollMove: function() {
			
		},
		onScrollEnd: function() {
			
		}
	});
	$("#thelist").empty();
	
	// 获取当前登录人的好友列表
	for (var i=0;i< roster.length;i++) {
		var ros = roster[i]; // 好友的对象
		$("#thelist").append('<li id="roster_'+ros.name+'"><span class="subscription hide">'+ros.subscription+'</span><img src="'+ros.wximg+'" /><div class="username">'+ros.wxname+'<span class="circle hide"></span></div></li>');
		//==============长按、短按功能begin====================//
		$("#roster_"+ros.name).bind("click", function(){
			choosedEle = window.event.currentTarget.id;
			window.location.href = "wxpchat.do?action=tochat&username="+username+"&contacts="+choosedEle.split("_")[1];
		});
		
		/*$("#roster_"+ros.name).bind("touchstart", function(){
			t1 = new Date().getTime();
			choosedEle = window.event.currentTarget.id;
		    timeout = setTimeout(function() {
		    	delroster();
			   clearTimeout(timeout); 
		    }, 1500); 
		});
		
		$("#roster_"+ros.name).bind("touchend", function(){
			choosedEle = window.event.currentTarget.id;
			var t2 = new Date().getTime();
			if(t2 - t1 > 1000){// 算长按
//				 alert("1秒了");
			}else{// 短按
//				 alert("少于1s");
				//window.location.href = "wxpchat.do?action=tochat&username="+username+"&password="+password+"&contacts="+choosedEle.split("_")[1];
			}
			clearTimeout(timeout);
		});*/
		//==============长按、短按功能end====================//
		var hxmsg = window.localStorage.getItem("hxmsg");
		var allInfo = null;
		if(hxmsg != null){
			allInfo = eval("(" + hxmsg + ")");
		}
		setUnread(allInfo,ros.name);
	 }
	myScroll.refresh();
}

//根据用户名密码登录系统
conn.init({
      onOpened : function() {
      	conn.setPresence();// 状态置为在线
		// 启动心跳
        if (conn.isOpened()) {
            conn.heartBeat(conn);
        }
        conn.getRoster({//获取好友列表
		       success : function(roster) {
		    	   if($("#thelist")[0].children.length == 0){
		    		   getImages(roster);
		    		   loaded(roster);
		    	   }
				}
        });
      },
      onTextMessage : function(message){
		var from = message.from;//消息的发送者
        var mestype = message.type;//消息发送的类型是群组消息还是个人消息
        var messageContent = message.data;//文本消息体
        //TODO  根据消息体的to值去定位那个群组的聊天记录
        var room = message.to;
        if (mestype == 'groupchat') {
            appendMsg(message.from, message.to, messageContent, mestype);
        } else {
            appendMsg(from, from, messageContent);
        }
	},
	//异常时的回调方法
    onError : function(message) {
        handleError(message);
    }
});
if(username != "" && password != ""){//信息完整就登陆
	login();//登录
}else{//信息不全就不登陆
	$("#pullDown").hide();
}

var handleError = function(e){
	conn.stopHeartBeat(conn);//断掉心跳,重新连接
    setTimeout("login()",1500);//两秒后执行
};

function appendMsg(who, contact, message, chattype){
    if (chattype && chattype == 'groupchat') {
    	
    }
    // 消息体 {isemotion:true;body:[{type:txt,msg:ssss}{type:emotion,msg:imgdata}]}
    var localMsg = null;
    if (typeof message == 'string') {
        localMsg = Easemob.im.Helper.parseTextMessage(message);
        localMsg = localMsg.body;
    } else {
        localMsg = message.data;
    }
    var messageContent = localMsg;
    for (var i = 0; i < messageContent.length; i++) {
        var msg = messageContent[i];
        var type = msg.type;
        var data = msg.data;
        
        if (type == "emotion") {
            
        } else if (type == "pic" || type == 'audio' || type == 'video') {
            var filename = msg.filename;
           	
            if(type == 'audio' && msg.audioShim) {
                
            }
        } else {
        	var hxmsg = window.localStorage.getItem("hxmsg");
        	var allInfo;
        	var hascur = false;
        	if(hxmsg == null){
        		allInfo = new Array();
        	}else{
        		allInfo = eval("(" + hxmsg + ")");
        		for(var i=0; i<allInfo.length;i++){
        			var rosterobj = allInfo[i];
        			var rwho = rosterobj.who;
        			var rcontent = rosterobj.content;
        			if(rwho == who){
        				var newmsg = new Object();
        				newmsg.flag = 0;
        				newmsg.msg = data;
        				newmsg.time = getLongTime();
        				newmsg.from = 0;
        				rcontent.push(newmsg);
        				hascur = true;
        				break;
        			}
        		}
        	}
        	
        	if(!hascur){
    			var curObject = new Object();
        		curObject.who = who;
        		var msgInfo = new Array();
        		var msgObject = new Object();
        		msgObject.flag = 0;
        		msgObject.msg = data;
        		msgObject.time = getLongTime();
        		msgObject.from = 0;
        		msgInfo.push(msgObject);
        		curObject.content = msgInfo;
        		allInfo.push(curObject);
    		}
    		window.localStorage.removeItem("hxmsg");//Iphone有可能报错，所以加这一行
    		window.localStorage.setItem("hxmsg",JSON.stringify(allInfo));
        	setUnread(allInfo,who);
        }
    }
}

function setUnread(allInfo,who){//设置联系人未读消息个数
	if(allInfo != null){
		var count = 0;
		for(var i=0; i<allInfo.length;i++){
			var rosterobj = allInfo[i];
			var rwho = rosterobj.who;
			var rcontent = rosterobj.content;
			if(rwho == who){
				for(var j=0;j<rcontent.length;j++){
					var curobject = rcontent[j];
					var flag = curobject.flag;
					var from = curobject.from;//0:来自联系人 1：自己的消息
					if(parseInt(flag) == 0 && parseInt(from) == 0){//获取未读消息个数
						count++;
					}
				}
				break;
			}
		}
		if(count > 0){
			for(var i=0; i<$("#thelist")[0].children.length;i++){
				var id = $("#thelist")[0].children[i].id;
				var roster = id.split("_")[1];
				if(roster == who){
					removeClass($(".circle")[i],"hide");
					if(count > 99){
						count = "99+";
					}
					$(".circle")[i].innerHTML = count;
				}
			}
		}
	}
}

function delroster(){
	alert(choosedEle);
}

function tochat(){
	
}

// 每次移除一个class
function removeClass(currNode, curClass){
	var oldClass,newClass1 = "";
    oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
    if(oldClass !== null) {
	   oldClass = oldClass.split(" ");
	   for(var i=0;i<oldClass.length;i++){
		   if(oldClass[i] != curClass){
			   if(newClass1 == ""){
				   newClass1 += oldClass[i]
			   }else{
				   newClass1 += " " + oldClass[i];
			   }
		   }
	   }
	}
	currNode.className = newClass1; // IE 和FF都支持
}

// 每次添加一个class
function addClass(currNode, newClass){
    var oldClass;
    oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
    if(oldClass !== null) {
	   newClass = oldClass+" "+newClass; 
	}
	currNode.className = newClass; // IE 和FF都支持
}
// 检测是否包含当前class
function hasClass(currNode, curClass){
	var oldClass;
	oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
	if(oldClass !== null){
		oldClass = oldClass.split(" ");
		for(var i=0;i<oldClass.length;i++){
		   if(oldClass[i] == curClass){
			   return true;
		   }
	   }
	}
	return false;
}

// 扩展Date的format方法
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
 * 转换long值为日期字符串
 * 
 * @param l
 *            long值
 * @param isFull
 *            是否为完整的日期数据, 为true时, 格式如"2000-03-05 01:05:04" 为false时, 格式如
 *            "2000-03-05"
 * @return 符合要求的日期字符串
 */    

function getSmpFormatDateByLong(l, isFull) {  
   return getSmpFormatDate(new Date(l), isFull);  
}  

/**
 * 转换日期对象为日期字符串
 * 
 * @param date
 *            日期对象
 * @param isFull
 *            是否为完整的日期数据, 为true时, 格式如"2000-03-05 01:05:04" 为false时, 格式如
 *            "2000-03-05"
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
 * 转换日期对象为日期字符串
 * 
 * @param l
 *            long值
 * @param pattern
 *            格式字符串,例如：yyyy-MM-dd hh:mm:ss
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

 var getLoacalTimeString = function() {
	    var date = new Date();
	    var time =date.getMonth() + "-" + date.getDay() + " " + date.getHours() + ":" + date.getMinutes() + ":"
	            + date.getSeconds();
	    return time;
	};
	
var getLongTime = function(){
	return parseInt(new Date().getTime()/1000);
};