var heigth = document.documentElement.clientHeight - 90;
$(".chat-thread")[0].style.height = heigth + "px";
/* $(".chat-thread")[0].style.marginBottom = 220 + "px"; */

var username,password,contacts,fromimg,toimg;
username = document.getElementById("username").value;
password = document.getElementById("password").value;
contacts = document.getElementById("contacts").value;
fromimg = document.getElementById("fromimg").value;
toimg = document.getElementById("toimg").value;

var groupFlagMark = "group--";
var msghistory = new Array();//历史消息
var lasttime = null;
var showtime = true;//是否显示时间
var hasHis = false;//是否有历史消息
var allInfo = null;//localstorage内容

var today = new Date();
today.setHours(0);
today.setMinutes(0);
today.setSeconds(0);
today.setMilliseconds(0);
today = today/1000;//当天初始时间

//****************一个定时任务检测clientHeight的变化begin****************//
function setHeight(clientHeight){
	var nowclientHeight = document.documentElement.clientHeight;
	var height = $(".chat-thread")[0].style.height;
	height = parseInt(height.split("px")[0]);
	$(".chat-thread")[0].style.height = (height - (clientHeight - nowclientHeight)) + "px";
}

window.sessionStorage.setItem("height",document.documentElement.clientHeight);

function checkHeight(){
	var h = window.sessionStorage.getItem("height");
	if(h != null){
		var cheight = document.documentElement.clientHeight;
		if(h != cheight){
			setHeight(h);
			window.localStorage.removeItem("typewriting");
			window.localStorage.setItem("typewriting",Math.abs(h - cheight));
			window.sessionStorage.setItem("height",cheight);
		}
	}
	setTimeout("checkHeight()",100);//0.1秒后执行
}
checkHeight();
//****************一个定时任务检测clientHeight的变化end****************//
var getLoacalTimeString = function(time) {
    var date = new Date(time);
    var month = "";
    if(time < today * 1000){
    	month = add_zero(date.getMonth() + 1) + "-" + add_zero(date.getDate()) + " ";
    }
    var time =month + add_zero(date.getHours()) + ":" + add_zero(date.getMinutes())/* + ":"
            + add_zero(date.getSeconds())*/;
    return time;
};

function add_zero(temp){
	 if(temp<10){
		 return "0"+temp;
	 }else{
		 return temp; 
	 }
}

var getLongTime = function(){
	return parseInt(new Date().getTime()/1000);
};

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
checkhxuser();

//读取未读的消息
function unreadMsg(){
	var hxmsg = window.localStorage.getItem("hxmsg");
	if(hxmsg != null){
		allInfo = eval("(" + hxmsg + ")");
		for(var i=0; i<allInfo.length;i++){
			var rosterobj = allInfo[i];
			var rwho = rosterobj.who;
			var rcontent = rosterobj.content;
			if(rwho == contacts){
				for(var j=0; j<rcontent.length; j++){
					var msgobj = rcontent[j];
					var flag = msgobj.flag;
					var from = msgobj.from;//0:来自联系人 1：自己的消息
					if(parseInt(flag) == 0 && parseInt(from) == 0){//遍历该联系人发来的所有未读消息
						var msg = msgobj.msg;
						var msgtime = msgobj.time;
						if(lasttime != null){//大于1分钟显示时间
							if(Math.abs(msgtime - lasttime) > 60){
								showtime = true;
								lasttime = msgtime;
							}else{
								showtime = false;
							}
						}else{
							showtime = true;
							lasttime = msgtime;
						}
						readMsg(msg,getLoacalTimeString(parseInt(msgtime)*1000),true);//读取未读消息
						msgobj.flag = 1;//标记为已读
					}
					
					if(parseInt(flag) > 0){//有历史消息
						hasHis = true;
						msghistory.push(msgobj);
					}
				}
				break;
			}
		}
		if(hasHis){
			$(".chat-thread").prepend('<li class="loadmore"  onclick="loadhis();">查看历史消息</li>');
		}
		window.localStorage.removeItem("hxmsg");//Iphone有可能报错，所以加这一行
		window.localStorage.setItem("hxmsg",JSON.stringify(allInfo));
	}
}
unreadMsg();

function updateLocalStorage(from,flag,who,msg){
	//写消息记录
	var hascur = false;
	if(allInfo == null){
		allInfo = new Array();
	}else{
		for(var i=0; i<allInfo.length;i++){
			var rosterobj = allInfo[i];
			var rwho = rosterobj.who;
			var rcontent = rosterobj.content;
			if(rwho == who){
				var newmsg = new Object();
				newmsg.flag = flag;
				newmsg.msg = msg;
				newmsg.time = getLongTime();
				newmsg.from = from;
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
		msgObject.flag = flag;
		msgObject.msg = msg;
		msgObject.time = getLongTime();
		msgObject.from = from;
		msgInfo.push(msgObject);
		curObject.content = msgInfo;
		allInfo.push(curObject);
	}
	window.localStorage.removeItem("hxmsg");//Iphone有可能报错，所以加这一行
	window.localStorage.setItem("hxmsg",JSON.stringify(allInfo));
}

function sendMsg(msg,time){
	if(showtime){
		$(".chat-thread").append('<li class="time">'+time+'</li>');
	}
	$(".chat-thread").append('<li class="contacts"><div><img class="imgcontacts" src="'+toimg+'"/></div><div class="contactsmsg">'+msg+'</div><div class="arrowright"></div></li>');
	var convo = document.getElementById("wechat");
	convo.scrollTop = convo.scrollHeight;
	var message = document.getElementById("message");
	message.value="";
	message.focus();
	//写消息记录
	updateLocalStorage(1,1,contacts,msg);
}

function readMsg(msg,time,unread){//unread:true表示是读已经存储的未读消息 false表示刚发过来的消息，需要存储
	if(showtime){
		$(".chat-thread").append('<li class="time"><div>'+time+'</div></li>');
	}
	$(".chat-thread").append('<li class="me"><div><img class="imgme" src="'+fromimg+'"/></div><div class="memsg">'+msg+'</div><div class="arrowleft"></div></li>');
	if(!unread){
		var convo = document.getElementById("wechat");
		convo.scrollTop = convo.scrollHeight;
		//写消息记录
		updateLocalStorage(0,1,contacts,msg);
	}
}

function readbeforeMsg(msg,time,msgobj){
	if(msgobj.from == 0){
		$(".chat-thread").prepend('<li class="me"><div><img class="imgme" src="'+fromimg+'"/></div><div class="memsg">'+msgobj.msg+'</div><div class="arrowleft"></div></li>');
	}else if(msgobj.from == 1){
		$(".chat-thread").prepend('<li class="contacts"><div><img class="imgcontacts" src="'+toimg+'"/></div><div class="contactsmsg">'+msgobj.msg+'</div><div class="arrowright"></div></li>');
	}
	if(showtime){
		$(".chat-thread").prepend('<li class="time"><div>'+time+'</div></li>');
	}
	convo.scrollTop = 0;
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

// 根据用户名密码登录系统
conn.init({
      onOpened : function() {
      	conn.setPresence();// 状态置为在线
		// 启动心跳
        if (conn.isOpened()) {
            conn.heartBeat(conn);
        }
        /*if(!hasClass($(".mask")[0],"hide")){
        	$(".rules-title")[0].innerHTML = "连接成功";
 	        $(".img1")[0].src = "images/wxpublic/connected.png";
 	        setTimeout("hidemsg()",1000);//1秒后执行
        }*/
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
login();//登录

var sendText = function() {
	var msg = document.getElementById("message").value;
	if(msg == ""){
		return false;
	}
    var options = {
        to : contacts,
        msg : msg,
        type : "chat"
    };
    // 群组消息和个人消息的判断分支
    if (contacts.indexOf(groupFlagMark) >= 0) {
        options.type = 'groupchat';
        options.to = curRoomId;
    }
    //easemobwebim-sdk发送文本消息的方法 to为发送给谁，meg为文本消息对象
    conn.sendTextMessage(options);
    //当前登录人发送的信息在聊天窗口中原样显示
    var msgtime = parseInt(new Date().getTime()/1000);
    if(lasttime != null){//大于1分钟显示时间
		if(Math.abs(msgtime - lasttime) > 60){
			showtime = true;
			lasttime = msgtime;
		}else{
			showtime = false;
		}
	}else{
		showtime = true;
		lasttime = msgtime;
	}
    sendMsg(msg,getLoacalTimeString(parseInt(msgtime)*1000));
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
        	if(who == contacts){//当前联系人，直接显示并置为已读消息
        		var msgtime = parseInt(new Date().getTime()/1000);
        		if(lasttime != null){//大于1分钟显示时间
					if(Math.abs(msgtime - lasttime) > 60){
						showtime = true;
						lasttime = msgtime;
					}else{
						showtime = false;
					}
				}else{
					showtime = true;
					lasttime = msgtime;
				}
        		readMsg(data,getLoacalTimeString(parseInt(msgtime)*1000),false);
        	}else{//非当前联系人，未读消息存储起来
        		updateLocalStorage(0,0,who,data);
        	}
        }
    }
}
var handleError = function(e){
	/*$(".mask").removeClass("hide");
	$(".rules-title")[0].innerHTML = "您已掉线，正在重连...";
    $(".img1")[0].src = "images/wxpublic/connecting.gif";*/
	conn.stopHeartBeat(conn);//断掉心跳,重新连接
    setTimeout("login()",1500);//1.5秒后执行
};

function hidemsg(){
	addClass($(".mask")[0],"hide");
};

function backHeight(){
	var heigth = document.documentElement.clientHeight - 90;
	$(".chat-thread")[0].style.height = heigth + "px";
	$(".msgfunc").addClass("hide");
}

//===============上下滑动显示历史消息begin=============//
var _start = 0, _end = 0, _body = document.getElementById("convo");
_body.addEventListener("touchstart", touchStart, false);
_body.addEventListener("touchmove", touchMove, false);
function touchStart(event) {
	var touch = event.touches[0];
	_start = touch.pageY;
	
	//隐藏功能栏
	var msgfuncid = document.getElementById("msgfuncid");
	if(!hasClass(msgfuncid, "hide")){
		setTimeout("backHeight()",500);//1.5秒后执行
	}
}

function touchMove(event) {
	var touch = event.touches[0];
	_end = (_start - touch.pageY);
	if(_end > 0){
//		$(".history").addClass("hide");
	}else{
//		$(".history").removeClass("hide");
	}
}
//===============上下滑动显示历史消息end=============//

function loadhis(){
	var wechat = document.getElementById("wechat");
	var children = wechat.childNodes;
	wechat.removeChild(children[0]);
	$(".chat-thread").prepend('<li class="loadinghis"><img src="images/wxpublic/history.gif" /></li>');
	setTimeout("readHisMsg()",1000);//1.5秒后执行
}

function readHisMsg(){
	var wechat = document.getElementById("wechat");
	var children = wechat.childNodes;
	wechat.removeChild(children[0]);
	
	var count = 0;
	if(msghistory != null){
		for(var i=msghistory.length-1; i>=0; i--){
			count++;
			if(count >= 6){//每次加载5条历史消息
				break;
			}
			var msgobj = msghistory[i];
			var flag = msgobj.flag;
			if(parseInt(flag) > 0){//遍历历史消息
				var msg = msgobj.msg;
				var msgtime = msgobj.time;
				if(lasttime != null){//大于1分钟显示时间
					if(Math.abs(lasttime - msgtime) > 60){
						showtime = true;
						lasttime = msgtime;
					}else{
						showtime = false;
					}
				}else{
					showtime = true;
					lasttime = msgtime;
				}
				if(count == 5 || msghistory.length == 1){
					showtime = true;
					lasttime = msgtime;
				}
				readbeforeMsg(msg,getLoacalTimeString(parseInt(msgtime)*1000),msgobj);
				msghistory.pop();//移除已读历史消息
			}
			
		}
		if(msghistory.length > 0){//还有多余的历史消息
			$(".chat-thread").prepend('<li class="loadmore" onclick="loadhis();">更多历史消息</li>');
		}
	}
}

function setfuncHeight(){
	var msgfuncid = document.getElementById("msgfuncid");
	if(hasClass(msgfuncid,"hide")){
		var h = window.localStorage.getItem("typewriting");
		if(h != null){
			$(".msgfunc")[0].style.height = h + "px";
			var height = $(".chat-thread")[0].style.height;
			height = parseInt(height.split("px")[0]);
			$(".chat-thread")[0].style.height = (height - h) + "px";
		}
		$(".msgfunc").removeClass("hide");
	}
}

function showfunc(){
	setTimeout("setfuncHeight()",500);//0.5秒后执行
}

function hidefunc(){
	$(".msgfunc").addClass("hide");
}

//每次移除一个class
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
	currNode.className = newClass1; //IE 和FF都支持
}

//每次添加一个class
function addClass(currNode, newClass){
    var oldClass;
    oldClass = currNode.getAttribute("class") || currNode.getAttribute("className");
    if(oldClass !== null) {
	   newClass = oldClass+" "+newClass; 
	}
	currNode.className = newClass; //IE 和FF都支持
}
//检测是否包含当前class
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
